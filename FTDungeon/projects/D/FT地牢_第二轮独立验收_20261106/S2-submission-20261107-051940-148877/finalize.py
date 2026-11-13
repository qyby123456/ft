import hashlib
import json
from pathlib import Path
import subprocess
import sys
from PIL import Image, ImageDraw

sys.dont_write_bytecode = True
ROOT = Path(__file__).resolve().parent
sys.path.insert(0, 'C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
from nbtio import Structure, plain


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def write(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2), encoding='utf-8')


def main():
    qa = ROOT / 'qa'
    qa.mkdir(exist_ok=True)
    rooms = sorted(ROOT.glob('[12][0-9]-*'))
    if sys.argv[1] == 'reproduce':
        checks = []
        for folder in rooms:
            design = json.loads((folder / 'design.json').read_text(encoding='utf-8'))
            target = qa / 'reproduction' / folder.name
            target.mkdir(parents=True, exist_ok=True)
            command = ['node', 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/run-build.mjs', str(folder / 'recipe.mjs'), '--out-dir', str(target), '--json']
            result = subprocess.run(command, cwd=ROOT, capture_output=True)
            (target / 'build.log').write_bytes(result.stdout + result.stderr)
            assert result.returncode == 0
            original = Structure.read(folder / design['nbt'])
            reproduced = Structure.read(target / design['nbt'])
            assert original.size == reproduced.size and original.data_version == reproduced.data_version and original.blocks == reproduced.blocks
            assert {position: plain(payload) for position, payload in original.block_entities.items()} == {position: plain(payload) for position, payload in reproduced.block_entities.items()}
            assert [plain(entity) for entity in original.entities] == [plain(entity) for entity in reproduced.entities]
            checks.append({'room': folder.name, 'source_sha256': digest(folder / design['nbt']), 'recipe_sha256': digest(folder / 'recipe.mjs'), 'reproduced_sha256': digest(target / design['nbt']), 'voxel_payload_entities_equal': True})
        write(qa / 'reproduction.json', checks)
        return
    if sys.argv[1] == 'sheet':
        image = Image.new('RGB', (1600, 1500), '#17130f')
        draw = ImageDraw.Draw(image)
        for index, folder in enumerate(rooms):
            panel = Image.open(folder / 'preview-full.png').convert('RGB')
            panel.thumbnail((790, 270))
            east = index % 2 * 800
            south = index // 2 * 300
            image.paste(panel, (east + (800-panel.width)//2, south + 25))
            draw.text((east+10, south+5), folder.name, fill='white')
        image.save(qa / 'full-shell-contact.png')
        return
    records = []
    runtime_summary = []
    for folder in rooms:
        design = json.loads((folder / 'design.json').read_text(encoding='utf-8'))
        source = folder / design['nbt']
        sha256 = digest(source)
        review = json.loads((folder / 'qa/visual-review.json').read_text(encoding='utf-8'))
        native = json.loads((folder / 'qa/takeover-verification.json').read_text(encoding='utf-8'))
        render = json.loads((folder / 'qa/render.json').read_text(encoding='utf-8'))
        assert review['source_sha256'] == native['source_sha256'] == render['source_sha256'] == sha256
        runtime_folder = ROOT / ('runtime-qa-r4' if folder.name.startswith('11-') else 'runtime-qa-r2' if folder.name.startswith('12-') else 'runtime-qa')
        runtime_path = runtime_folder / (folder.name + '-runtime.json')
        runtime = json.loads(runtime_path.read_text(encoding='utf-8'))
        assert runtime['source_sha256'] == sha256 and runtime['ticks'] >= 220
        assert not runtime['lost_water'] and not runtime['spread_water'] and not runtime['dropped_items']
        assert all(container['occupied_slots'] == 0 for container in runtime['containers'])
        assert all(door['lever_found'] and door['after_use_open'] and not door['after_second_use_open'] for door in runtime['doors'])
        identity_changes = [change for change in runtime['differences'] if change['before'].split('}')[0] != change['after'].split('}')[0]]
        assert not identity_changes
        runtime_summary.append({'room': folder.name, 'source_sha256': sha256, 'report': str(runtime_path), 'report_sha256': digest(runtime_path), 'ticks': runtime['ticks'], 'state_only_differences': runtime['differences'], 'water_loss': 0, 'water_spread': 0, 'drops': 0, 'doors_open_close': len(runtime['doors']), 'scope': runtime['scope']})
        route = json.loads((folder / 'qa/independent-routes.json').read_text(encoding='utf-8'))[0]
        support = json.loads((folder / 'qa/independent-all-support.json').read_text(encoding='utf-8'))[0]
        records.append({'id': design['id'], 'folder': str(folder), 'candidate': str(source), 'sha256': sha256, 'recipe_sha256': digest(folder / 'recipe.mjs'), 'native_targets': len(route['targets']), 'all_nonair_support_checked': support['nonair_checked'], 'render_sha256': digest(folder / 'qa/render.json'), 'visual_review_sha256': digest(folder / 'qa/visual-review.json'), 'runtime_report': str(runtime_path), 'status': 'PRODUCER_VERIFIED_PENDING_INDEPENDENT_REVIEW'})
        summary_path = folder / 'summary.json'
        summary = json.loads(summary_path.read_text(encoding='utf-8'))
        summary.update(nbtPath=str(source), summaryPath=str(summary_path), recipePath=str(folder / 'recipe.mjs'), inherited_original_summary=str(ROOT / 'inherited-evidence/frozen-copy-manifest.json'))
        write(summary_path, summary)
        (folder / 'README.md').write_text(f"# {design['id']} 接手最终候选\n\nNBT: {design['nbt']}\n\nSHA-256: {sha256}\n\n" + review['visual_review'] + '\n\n接口: ' + json.dumps(design['ports'], ensure_ascii=False) + '\n\n与参考差异:\n' + '\n'.join('- ' + difference for difference in design['differences']) + '\n\n可复跑: 本组 takeover.py verify ' + folder.name + '；run-build.mjs 的配方为本目录 recipe.mjs；本组 render.cjs 接本目录路径。\n\n原生静态、全方块 canSurvive、原目标碰撞路线及220刻隔离ServerLevel已执行；运行时报告 ' + str(runtime_path) + '。动态差异仅楼梯/墙/栅栏连接属性变化，完整详单见 qa/runtime-summary.json；真实玩家游玩、随机刻激活覆盖、所有功能交互及战斗未验证。制作方自检不是独立通过。\n', encoding='utf-8')
    write(qa / 'runtime-summary.json', runtime_summary)
    ladder_path = ROOT / 'runtime-qa-r4/ladder-physics.json'
    ladder = json.loads(ladder_path.read_text(encoding='utf-8'))
    assert ladder['source_sha256'] == records[0]['sha256'] and ladder['climbable_observed'] and ladder['top_landing_clear']
    write(qa / 'ladder-acceptance-scope.json', {'source_sha256': records[0]['sha256'], 'evidence': str(ladder_path), 'sha256': digest(ladder_path), 'result': 'PRODUCER_NATIVE_PLAYER_TRAVEL_CLIMB_AND_SIDE_LANDING_PASS', 'not_certified': ['human control', 'client camera', 'ladder descent', 'independent acceptance'], 'historical_failure': str(ROOT / 'runtime-qa-r3/ladder-physics.json')})
    assembly = ROOT / 'assembly'
    layout = json.loads((assembly / 'layout.json').read_text(encoding='utf-8'))
    sha256 = digest(assembly / 's2-desert-functional-combined.nbt')
    assert sha256 == layout['source_sha256']
    for module in layout['modules']:
        assert module['source_sha256'] == digest(Path(module['candidate']))
    design = json.loads((assembly / 'design.json').read_text(encoding='utf-8'))
    candidate = Structure.read(assembly / design['nbt'])
    render = json.loads((assembly / 'qa/render.json').read_text(encoding='utf-8'))
    native = json.loads((assembly / 'qa/takeover-verification.json').read_text(encoding='utf-8'))
    assert render['source_sha256'] == native['source_sha256'] == sha256
    write(assembly / 'qa/visual-review.json', {'source_sha256': sha256, 'reviewer_role': 'PRODUCER', 'status': 'PENDING_INDEPENDENT_REVIEW', 'visual_review': '已实际查看完整外壳、剖切全组和监牢至厨房直走廊内景。十间功能房以实际接口串联，19二层及20密道藏宝保留。修正显示副本的走廊剖切高度按各自局部走廊计算，低层走廊不再误切高层全部通道。完整源不受显示剖切影响。', 'views': {view['id']: digest(assembly / (view['id'] + '.png')) for view in render['views']}})
    parts = json.loads((assembly / 'parts/index.json').read_text(encoding='utf-8'))
    rebuilt = Structure(candidate.size)
    for item in parts['parts']:
        part = Structure.read(item['path'])
        assert max(part.size) <= 48 and digest(Path(item['path'])) == item['sha256']
        for position, block in part.blocks.items():
            destination = tuple(value+offset for value, offset in zip(position, item['origin']))
            assert destination not in rebuilt.blocks
            rebuilt.set(destination, block, part.block_entities.get(position))
        assert not part.entities
    assert rebuilt.blocks == candidate.blocks
    assert {position: plain(payload) for position, payload in rebuilt.block_entities.items()} == {position: plain(payload) for position, payload in candidate.block_entities.items()}
    assert not candidate.entities
    write(assembly / 'qa/reassembly-final.json', {'source_sha256': sha256, 'parts': len(parts['parts']), 'max_part_dimension': max(max(item['size']) for item in parts['parts']), 'voxel_match': True, 'block_entity_payload_match': True, 'source_and_part_entities': 0})
    write(ROOT / 'ready-for-review.json', {'status': 'READY_FOR_INDEPENDENT_REVIEW_NOT_ACCEPTED', 'rooms': records, 'assembly': {'candidate': str(assembly / design['nbt']), 'sha256': sha256, 'parts': len(parts['parts']), 'targets': len(design['targets']), 'runtime_assembly_not_run': True}, 'historical_independent_evidence': str(ROOT / 'inherited-evidence'), 'known_limits': ['Lodestone banner/skull rendering does not prove payload patterns', 'Runtime room differences are native connection-shape changes', 'No random tick coverage certificate', 'No real-player travel/combat/playthrough acceptance', 'Full assembly runtime world placement not run'], 'old_source_readonly': True})
    write(ROOT / 'progress.json', {'style': 'S2', 'phase': 'READY_FOR_INDEPENDENT_REVIEW', 'completed_takeover_self_checked': 10, 'rooms': records, 'assembly_sha256': sha256, 'independent_acceptance': 'PENDING', 'writes_scope': str(ROOT), 'runtime_room_qa': '220 ticks each; 8 iron doors open/close; no drops/water escape', 'old_source_readonly': True})
    (ROOT / 'README.md').write_text('# S2 砂岩荒漠11—20接手续作\n\n状态：十房制作方复核完成，合并与15分片已生成；待独立复核，未独立通过。\n\n继承十房已有独立配方及同哈希预览，保留 inherited-evidence 的原记录和文件哈希；仅重做13号锅焦点、12号牢门拉杆支座。20号实际9e363d哈希替代旧目检8a4842记录。\n\n12号隔离世界发现旧拉杆挂门失稳，八门整改后真实Lever.use开关复位通过。所有房间220刻真实邻居/流体测试，无方块种类丢失、物品掉落、水源丢失或外泄；楼梯/墙/栅栏连接属性变化逐条记录在 qa/runtime-summary.json。旧失败尝试保留在 runtime-qa，12号最终在 runtime-qa-r2。这些是制作方测试。\n\n合并NBT SHA-256: '+sha256+'。69目标原生0.6×1.8碰撞路线通过，十房源逐格及方块实体载荷保真；15分片最大48格，反拼复核见 assembly/qa/reassembly-final.json。实际Lodestone内外与剖切图在各房和assembly。\n\n当前哈希待审清单：ready-for-review.json。可重现配方验算见qa/reproduction.json。原生Java每次-Xmx1536M，单任务串行，运行目录均在本组。\n\n限制：合并整体未在世界运行；无随机刻激活覆盖声明；无人类/真实玩家完整游玩或战斗验证。旗纹及头颅Lodestone显示有缺失，原生方块/载荷检查不冒充视觉完全复刻。\n\n仅本接手续作S2有写入；旧S2只读，正式世界、mods/config/KubeJS未修改，未付费生图。测试专用Forge项目及新世界只在本组runtime-qa*，不用于安装。\n', encoding='utf-8')
    print('READY_FOR_REVIEW', sha256, len(records), len(parts['parts']))


if __name__ == '__main__':
    main()
