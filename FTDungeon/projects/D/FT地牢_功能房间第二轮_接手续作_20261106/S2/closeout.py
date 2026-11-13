import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys
from collections import Counter

sys.dont_write_bytecode = True
ROOT = Path(__file__).resolve().parent
sys.path.insert(0, 'C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
from nbtio import Structure, plain


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def read(path):
    return json.loads(path.read_text(encoding='utf-8'))


def write(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2), encoding='utf-8')


def artifact(path, role):
    path = path.resolve()
    return {'path': str(path), 'relative_path': path.relative_to(ROOT).as_posix() if path.is_relative_to(ROOT) else None, 'sha256': digest(path), 'bytes': path.stat().st_size, 'role': role}


def audit():
    assembly = ROOT / 'assembly'
    runtime = ROOT / 'runtime-assembly-r1'
    design = read(assembly / 'design.json')
    source = assembly / design['nbt']
    after_path = runtime / 'assembly-after.nbt'
    before = Structure.read(source)
    after = Structure.read(after_path)
    report = read(runtime / 'assembly-runtime.json')
    assert report['source_sha256'] == digest(source)
    assert report['movement_roundtrip_complete'] and report['movement_waypoints_reached'] == report['movement_waypoints_total']
    assert not report['lost_water'] and not report['spread_water'] and not report['dropped_items']
    assert all(entry['opened'] and entry['restored'] and entry['lever_found'] for entry in report['doors'])
    assert all(entry['server_menu_opened'] and entry['occupied_slots'] == 0 for entry in report['containers'])
    identities = []
    changes = []
    default_count = 0
    for position in before.blocks.keys() | after.blocks.keys():
        original = before.get(position)
        final = after.get(position)
        if original.name != final.name:
            identities.append({'position': position, 'before': original.name, 'after': final.name})
        old_properties = dict(original.properties)
        new_properties = dict(final.properties)
        changed = {key: [value, new_properties.get(key)] for key, value in old_properties.items() if new_properties.get(key) != value}
        default_count += sum(key not in old_properties for key in new_properties)
        if changed:
            assert set(changed) <= {'east', 'west', 'north', 'south', 'shape', 'up'}, (position, changed)
            assert original.name.endswith(('_stairs', '_wall', '_fence')), (position, original.name, changed)
            changes.append({'position': position, 'block': original.name, 'changes': changed, 'classification': 'native_neighbor_connection'})
    assert not identities
    payload_changes = []
    assert not before.block_entities.keys() - after.block_entities.keys()
    for position, payload in before.block_entities.items():
        original = plain(payload)
        final = plain(after.block_entities[position])
        assert all(final.get(key) == value for key, value in original.items()), (position, original, final)
        additions = {key: value for key, value in final.items() if key not in original}
        if additions:
            if original['id'] == 'minecraft:decorated_pot':
                assert additions == {'sherds': ['minecraft:brick'] * 4}
            elif original['id'] == 'minecraft:furnace':
                assert additions == {'CookTime': 0, 'BurnTime': 0, 'CookTimeTotal': 0, 'RecipesUsed': {}}
            else:
                raise AssertionError((position, additions))
            payload_changes.append({'position': position, 'before': original, 'after': final, 'classification': 'native_default_fields'})
    for position in after.block_entities.keys() - before.block_entities.keys():
        payload = plain(after.block_entities[position])
        assert before.get(position).name == 'minecraft:lectern' and payload == {'id': 'minecraft:lectern'}
        payload_changes.append({'position': position, 'before': None, 'after': payload, 'classification': 'native_empty_lectern_entity'})
    assert not before.entities and not after.entities
    qa = assembly / 'qa/posttick'
    qa.mkdir(exist_ok=True)
    specifications = read(assembly / 'qa/route-input.json')
    specifications[0].update(candidate=str(after_path), sha256=digest(after_path))
    write(qa / 'route-input.json', specifications)
    shutil.copyfile(assembly / 'qa/vanilla-block-tags.json', qa / 'vanilla-block-tags.json')
    classpath = (assembly / 'qa/classpath.txt').read_text(encoding='utf-8')
    command = ['C:/Program Files/Common Files/Oracle/Java/javapath/java.exe', '-Xmx1536M', '-Djava.awt.headless=true', '-cp', classpath, 'IndependentRouteAudit', str(qa)]
    result = subprocess.run(command, cwd=qa, capture_output=True, text=True, encoding='utf-8', errors='replace', timeout=300, creationflags=subprocess.CREATE_NO_WINDOW)
    (qa / 'route.log').write_text(result.stdout + result.stderr, encoding='utf-8')
    assert result.returncode == 0, result.stderr
    routes = read(qa / 'independent-routes.json')
    assert routes[0]['start_clear'] and len(routes[0]['targets']) == 69
    assert all(target['reachable'] and target['standing_clear'] for target in routes[0]['targets'])
    write(qa / 'audit.json', {'source_sha256': digest(source), 'after': artifact(after_path, 'real_server_posttick_structure'), 'runtime_report': artifact(runtime / 'assembly-runtime.json', 'runtime_report'), 'identity_changes': identities, 'explicit_state_changes': changes, 'native_default_property_count': default_count, 'block_entity_count_before': len(before.block_entities), 'block_entity_count_after': len(after.block_entities), 'original_payload_fields_preserved': True, 'payload_changes': payload_changes, 'entities_before': 0, 'entities_after': 0, 'route_command': command, 'route_exit_code': result.returncode, 'routes': routes, 'status': 'PRODUCER_POSTTICK_AUDIT_PASS_PENDING_INDEPENDENT_REVIEW'})
    reproduced = ROOT / 'qa/assembly-independent-output'
    comparisons = []
    for relative in [Path(design['nbt']), *[Path(entry['path']).relative_to(assembly) for entry in read(assembly / 'parts/index.json')['parts']]]:
        assert (assembly / relative).read_bytes() == (reproduced / relative).read_bytes(), relative
        comparisons.append({'source': artifact(assembly / relative, 'candidate'), 'reproduced': artifact(reproduced / relative, 'separate_output_reproduction'), 'byte_equal': True})
    write(ROOT / 'qa/assembly-reproduction.json', {'source_sha256': digest(source), 'generator': artifact(ROOT / 'assemble.py', 'assembly_recipe'), 'separate_output_directory': str(reproduced), 'files': comparisons, 'all_byte_equal': True})
    print('POSTTICK_AUDIT_PASS', len(changes), len(payload_changes), 'REPRODUCED', len(comparisons))


def publish():
    assembly = ROOT / 'assembly'
    runtime = ROOT / 'runtime-assembly-r1'
    ready = read(ROOT / 'ready-for-review.json')
    source = Path(ready['assembly']['candidate'])
    sha256 = digest(source)
    assert read(assembly / 'qa/posttick/audit.json')['source_sha256'] == sha256
    assert read(ROOT / 'qa/assembly-reproduction.json')['source_sha256'] == sha256
    report = read(runtime / 'assembly-runtime.json')
    assert report['source_sha256'] == sha256
    ready.update(style='S2', group='S2', group_number=2, schema_version=2, independent_acceptance='PENDING')
    ready['assembly'].update(runtime_assembly_not_run=False, runtime_report=artifact(runtime / 'assembly-runtime.json', 'runtime_report'), after_nbt=artifact(runtime / 'assembly-after.nbt', 'posttick_nbt'), posttick_audit=artifact(assembly / 'qa/posttick/audit.json', 'posttick_audit'), reproduction=artifact(ROOT / 'qa/assembly-reproduction.json', 'separate_output_reproduction'), recipe=artifact(ROOT / 'assemble.py', 'assembly_recipe'), layout=artifact(assembly / 'layout.json', 'layout'), scenery=artifact(assembly / 'scenery.json', 'watch_balcony_depth'), part_index=artifact(assembly / 'parts/index.json', 'part_index'), native_rotation_exception_count=0)
    references = read(ROOT / 'references.json')['references']
    for room in ready['rooms']:
        folder = Path(room['folder'])
        reference = next(entry for entry in references if entry['id'] == room['id'])
        room.update(style='S2', group='S2', recipe_path=str(folder / 'recipe.mjs'), render_path=str(folder / 'qa/render.json'), visual_review_path=str(folder / 'qa/visual-review.json'), design=artifact(folder / 'design.json', 'room_design'), reference=artifact(Path(reference['image_path']), 'readonly_reference_image'), prompt=artifact(Path(reference['prompt_path']), 'readonly_reference_prompt'))
        assert room['recipe_sha256'] == digest(Path(room['recipe_path']))
        assert room['render_sha256'] == digest(Path(room['render_path']))
        assert room['visual_review_sha256'] == digest(Path(room['visual_review_path']))
    ready['known_limits'] = ['Producer checks are pending independent review of current hashes', 'No random tick activation coverage certificate', 'Survival FakePlayer Player.travel roundtrip is not human/client/combat acceptance', 'Container operator teleports per fixture; server menus prove use functionality, not human reach or client UI', 'Lodestone banner/skull rendering does not prove payload appearance']
    visual = read(assembly / 'qa/visual-review.json')
    visual['visual_review'] += ' 已实际查看19号窗外29格深残柱与横梁场景，前后柱尺度和遮挡可见；20号焦点图可见箱子、金块、陶罐与封闭藏宝间。场景只属于合并体，不修改十个单房。'
    write(assembly / 'qa/visual-review.json', visual)
    progress = read(ROOT / 'progress.json')
    progress.update(group='S2', assembly_sha256=sha256, assembly_runtime={'server_ticks': report['server_ticks'], 'waypoints': report['movement_waypoints_reached'], 'roundtrip': True, 'doors': len(report['doors']), 'containers': len(report['containers']), 'posttick_routes': 69}, independent_acceptance='PENDING')
    write(ROOT / 'progress.json', progress)
    text = '# S2 砂岩荒漠11—20接手续作\n\n十房、合并体及15分片已完成制作方复核，当前哈希待独立验收，尚未独立通过。\n\n合并NBT SHA-256：`' + sha256 + '`。尺寸239×25×126，166642条方块记录；15分片最大48格并逐格反拼一致。十房配方重现与合并另目录重现均有证据。合并旋转遵循原生decorated_pot行为，旋转例外为0。19号窗外新增29格深残柱场景，20号保留密道藏宝功能。\n\n隔离Forge 1.20.1服务器真实运行15612刻；生存FakePlayer使用原生Player.travel走完4215个路点并返回入口。另设操作员逐容器定位：32个服务端菜单均打开且库存为空，8扇牢门均Lever.use开关复位。无掉落、无水源损失或外泄、无方块种类变化。刻后NBT保留所有原有实体载荷字段，新增原生默认字段逐项分类，刻后69目标碰撞路线再次通过。证据：runtime-assembly-r1与assembly/qa/posttick。\n\n11号攀梯顶端侧向落脚、12号拉杆支座、13号真实水锅可见性修正均保留各自房间测试。当前清单见ready-for-review.json，逐文件路径/字节数/SHA-256见artifact-manifest.json，复核交接见REVIEW_HANDOFF.md。旧失败尝试和旧独立审阅冻结记录保留，不冒充当前通过。\n\n限制：制作方自动化测试不等于独立验收或真人客户端完整游玩；容器操作员使用定位，未证明真人交互可达性；无随机刻激活覆盖证书，Lodestone旗纹/头颅显示存在限制。\n\n仅本接手续作S2写入；旧S2只读，正式存档、mods/config/KubeJS未修改，未付费生图。隔离测试世界仅在本组runtime-*目录。\n'
    (ROOT / 'README.md').write_text(text, encoding='utf-8')
    handoff = ROOT / 'REVIEW_HANDOFF.md'
    marker = '\n## 当前合并体补测与整改（33ceb33）\n'
    previous = handoff.read_text(encoding='utf-8').split(marker)[0]
    handoff.write_text(previous + marker + '\n' + text.split('\n\n', 1)[1] + '\n请冻结并独立复核当前清单哈希，重点复验19号真实窗外景深、decorated_pot原生旋转、另目录重现、合并世界刻后载荷/69目标，以及容器实际交互可达性。旧ed982c冻结结论仅属历史。\n', encoding='utf-8')
    files = set()
    extensions = {'.json', '.md', '.py', '.mjs', '.cjs', '.ps1', '.java', '.png', '.nbt', '.log', '.txt', '.toml', '.gradle', '.mcmeta'}
    for folder in [*[Path(room['folder']) for room in ready['rooms']], assembly]:
        files.update(path for path in folder.rglob('*') if path.is_file() and path.suffix in extensions)
    files.update(path for path in ROOT.iterdir() if path.is_file() and path.suffix in extensions and path.name not in {'artifact-manifest.json', 'ready-for-review.json'})
    files.update(path for path in (ROOT / 'qa').iterdir() if path.is_file() and path.suffix in extensions)
    for folder_name in ['runtime-qa', 'runtime-qa-r2', 'runtime-qa-r3', 'runtime-qa-r4', 'runtime-assembly-r1']:
        folder = ROOT / folder_name
        files.update(path for path in folder.iterdir() if path.is_file() and path.suffix in extensions)
        source_folder = folder / 'native-test/src/main'
        files.update(path for path in source_folder.rglob('*') if path.is_file() and path.suffix in extensions)
        files.update(path for path in (folder / 'native-test').glob('*.gradle') if path.is_file())
    for reference in references:
        files.update([Path(reference['image_path']), Path(reference['prompt_path'])])
    write(ROOT / 'artifact-manifest.json', {'style': 'S2', 'group': 'S2', 'status': 'PRODUCER_VERIFIED_PENDING_INDEPENDENT_REVIEW', 'assembly_sha256': sha256, 'artifacts': [artifact(path, 'readonly_reference' if not path.is_relative_to(ROOT) else 'delivery_or_qa_evidence') for path in sorted(files)], 'excluded': ['manifest and ready file self hashes', 'Gradle caches/classes, server world internals, historical copied evidence and QA reproduction duplicates'], 'historical_evidence_directory': str(ROOT / 'inherited-evidence')})
    ready['artifact_manifest'] = artifact(ROOT / 'artifact-manifest.json', 'artifact_manifest')
    write(ROOT / 'ready-for-review.json', ready)
    print('PUBLISHED_CURRENT_HASH', sha256, 'ARTIFACTS', len(files))


if __name__ == '__main__':
    audit() if sys.argv[1] == 'audit' else publish()
