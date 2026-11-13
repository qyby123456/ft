import collections
import hashlib
import json
import shutil
import subprocess
import sys
from pathlib import Path

sys.dont_write_bytecode = True
TOOLS = Path(r'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2\output\dungeon-expansion-20261101\tools')
sys.path.insert(0, str(TOOLS))
from nbtio import Structure, plain

ROOT = Path(__file__).resolve().parent
OLD = Path(r'D:\ft工程文件\FT地牢_独立复核_20261104_01a1a685')

def save(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2), encoding='utf-8')

def main():
    candidate = Path(sys.argv[1]).resolve()
    room = candidate.parent
    qa = room / 'qa'
    qa.mkdir(exist_ok=True)
    design = json.loads((room / 'design.json').read_text(encoding='utf-8'))
    digest = hashlib.sha256(candidate.read_bytes()).hexdigest()
    structure = Structure.read(candidate)
    counts = collections.Counter(block.name for block in structure.blocks.values())
    entities = [{'position': position, 'nbt': plain(entity)} for position, entity in structure.block_entities.items()]
    filled = [entry for entry in entities if entry['nbt'].get('Items') or entry['nbt'].get('LootTable')]
    hazards = [name for name in counts if name in ['minecraft:lava', 'minecraft:fire', 'minecraft:magma_block', 'minecraft:cactus']]
    water = []
    for position, block in structure.blocks.items():
        if block.name != 'minecraft:water':
            continue
        east, up, south = position
        for dx, dy, dz in [(1,0,0),(-1,0,0),(0,0,1),(0,0,-1),(0,-1,0)]:
            adjacent = structure.get((east+dx, up+dy, south+dz))
            if adjacent.name in ['minecraft:air', 'minecraft:cave_air']:
                water.append([position, [east+dx, up+dy, south+dz]])
    save(qa / 'inventory.json', {'sha256': digest, 'size': structure.size, 'data_version': structure.data_version, 'counts': counts, 'block_entities': entities, 'entity_count': len(structure.entities), 'filled_containers': filled, 'hazards': hazards, 'exposed_water_faces': water})
    spec = {'group': 7, 'candidate': str(candidate), 'sha256': digest, 'size': structure.size, 'start': design['start'], 'targets': design['targets'], 'swimming': False}
    save(qa / 'route-input.json', [spec])
    shutil.copyfile(OLD / 'vanilla-block-tags.json', qa / 'vanilla-block-tags.json')
    dependencies = Path(r'D:\ft工程文件\霜雪花园地牢_20261101\build\classpath\runData_minecraftClasspath.txt').read_text(encoding='utf-8').splitlines()
    dependencies = [entry for entry in dependencies if entry and 'minecraft_user_repo' not in entry]
    classpath = ';'.join([r'D:\ft工程文件\FT地牢_第二轮独立验收_20261106\classes', str(OLD / 'classes'), r'D:\ft工程文件\锈铜宝库地牢_20261101\qa\vanilla-1.20.1-mapped.jar'] + dependencies)
    java = r'C:\Program Files\Common Files\Oracle\Java\javapath\java.exe'
    commands = []
    for checker in ['IndependentNativeAudit', 'IndependentRouteAudit', 'IndependentAllSupportAudit']:
        arguments = [java, '-Xmx1536m', '-Djava.awt.headless=true', '-cp', classpath, checker, str(qa)]
        if checker == 'IndependentNativeAudit':
            arguments.append('frozen')
        result = subprocess.run(arguments, cwd=qa, capture_output=True, text=True, encoding='utf-8', errors='replace', timeout=180)
        (qa / f'{checker}.log').write_text(result.stdout + result.stderr, encoding='utf-8')
        commands.append({'checker': checker, 'returncode': result.returncode, 'heap': '1536m', 'implementation': str(OLD / f'{checker}.java')})
        if result.returncode:
            save(qa / 'invocation.json', commands)
            raise RuntimeError(f'{checker} failed; see log')
    save(qa / 'invocation.json', commands)
    native = json.loads((qa / 'independent-native-frozen.json').read_text())[0]
    routes = json.loads((qa / 'independent-routes.json').read_text())[0]
    support = json.loads((qa / 'independent-all-support.json').read_text())[0]
    checks = {'registry': native['registry_properties'] == 'PASS', 'roundtrip': native['template_roundtrip_block_count'] == 'PASS', 'decor_support': not native['survival_failures'] and not native['survival_unsupported'], 'routes': routes['start_clear'] and all(target['reachable'] and target['standing_clear'] for target in routes['targets']), 'containers_empty': not filled, 'liquid_containment_static': not water, 'no_hazards': not hazards, 'render_hash': json.loads((qa / 'render.json').read_text())['source_sha256'] == digest}
    checks['all_nonair_support'] = not support['failures'] and not support['unsupported']
    save(qa / 'self-check.json', {'sha256': digest, 'checks': checks, 'status': 'SELF_CHECK_PASS' if all(checks.values()) else 'NEEDS_FIX', 'independent_acceptance': 'NOT_RUN: reused verifier executed by author, not independent review', 'not_certified': ['world placement', 'neighbor and random ticks', 'fluid tick simulation', 'ladder climb physics', 'combat and survival completion']})
    print(json.dumps(checks))
    if not all(checks.values()):
        raise SystemExit(2)
    completed = []
    lines = ['# S7 蘑菇真菌功能房间第二轮', '', '仅写本组目录；不写 saves/mods。所有通过均为制作方自检，不是独立验收。', '', '|房间|候选SHA256|自检|', '|---|---|---|']
    for reference in json.loads((ROOT / 'references.json').read_text(encoding='utf-8'))['references']:
        report = ROOT / reference['id'] / 'qa/self-check.json'
        if report.exists():
            value = json.loads(report.read_text(encoding='utf-8'))
            if value['status'] == 'SELF_CHECK_PASS':
                completed.append(reference['id'])
            lines.append(f"|{reference['id']} {reference['title_cn']}|{value['sha256']}|{value['status']}|")
    save(ROOT / 'progress.json', {'style': 'S7', 'current': design['id'], 'completed': completed, 'pending': [f'S7-{number}' for number in range(11,21) if f'S7-{number}' not in completed], 'state': 'individuals-in-progress', 'independent_acceptance': 'NOT_RUN', 'production_writes': False, 'next': 'Finish next reference sequentially; merge only after ten individual self-checks and visual inspections'})
    lines += ['', '每房间：recipe.mjs 可复现配方；design.json 图像观察、接口、目标、差异；完整 .nbt；full.png / cutaway.png / interior.png 真实 Lodestone 渲染；qa/ 方块与实体清单、1536m Java 原版注册/支撑/人体碰撞路线、当前候选哈希。', '', '复现：node <skill>/scripts/run-build.mjs <room>/recipe.mjs --out-dir <room>；node render.cjs <candidate.nbt>；使用工单Python运行 qa.py <candidate.nbt>。', '', '尚未验证：游戏世界放置、邻居/随机/流体 tick、攀梯物理、战斗与生存通关；待外部独立复核。十图完成前不生成组合。']
    (ROOT / 'README.md').write_text('\n'.join(lines) + '\n', encoding='utf-8')

if __name__ == '__main__':
    main()
