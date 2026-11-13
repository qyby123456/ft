from __future__ import annotations

from pathlib import Path
import json
from static_qa import ROOT, digest, dump, REFERENCE_HASH


def main():
    fingerprint = digest(ROOT / 'room.nbt')
    metadata = json.loads((ROOT / 'room.json').read_text(encoding='utf-8'))
    static = json.loads((ROOT / 'qa/static-qa.json').read_text(encoding='utf-8'))
    native = json.loads((ROOT / 'qa/native-world-qa.json').read_text(encoding='utf-8'))
    registry = json.loads((ROOT / 'qa/native-registry-qa.json').read_text(encoding='utf-8'))
    render = json.loads((ROOT / 'qa/render-manifest.json').read_text(encoding='utf-8'))
    lodestone = json.loads((ROOT / 'qa/lodestone-preview.json').read_text(encoding='utf-8'))
    for evidence in (metadata, static, native, registry, render, lodestone):
        if evidence['sha256'] != fingerprint:
            raise ValueError('Evidence has stale NBT fingerprint')
    for evidence in (static, native, registry, lodestone):
        if evidence['status'] != 'PASS':
            raise ValueError('QA evidence is not PASS')
    if len(native['rotations']) != 4 or native['stable_ticks'] != 600:
        raise ValueError('Missing native stability coverage')
    for rotation in native['rotations']:
        if rotation['initial']['status'] != 'PASS' or rotation['after_600_ticks']['status'] != 'PASS' or rotation['fixture_rollback'] != 'PASS':
            raise ValueError('Native rotation evidence incomplete')
    for capture in render['views'] + lodestone['captures']:
        if digest(capture['path']) != capture['sha256']:
            raise ValueError('View fingerprint mismatch')
    visual = {'status': 'PASS', 'sha256': fingerprint, 'review_method': 'Actual NBT four-direction interior and intact exterior sheets visually inspected; Lodestone north, south and window views inspected.', 'interior_directions': ['north', 'south', 'west', 'east'], 'exterior_directions': ['north', 'south', 'west', 'east'], 'observations': ['Four independent square columns connect floor to full roof; furniture and rubble remain outside shafts.', 'Two low bookcase remnants and the surviving east wall bookshelf rest on full support.', 'Collapsed west gallery retains wall contact, ground posts and supporting beams; rubble sits outside the central path.', 'Five-wide main route and north entrance are unobstructed.', 'Double roof remains intact in all exterior views; cutaway affects images only.', 'Window, hanging chains and lamps, papers, candles and empty barrels have visible support.'], 'limitations': ['Orthographic banner appearance is approximate; pattern NBT is preserved and native-loaded.', 'Lodestone lighting is approximate and these are not client gameplay screenshots.', 'Vanilla bookshelves cannot rotate onto their side; low bookcase remnants represent the concept toppled shelves.'], 'view_manifest': 'qa/render-manifest.json', 'lodestone_manifest': 'qa/lodestone-preview.json'}
    dump(ROOT / 'qa/four-view-review.json', visual)
    render['status'] = 'PASS'
    render['visual_review'] = 'qa/four-view-review.json'
    dump(ROOT / 'qa/render-manifest.json', render)
    logs = sorted((ROOT / 'qa').glob('forge-gametest-*.log'), key=lambda path: path.stat().st_mtime)
    successful = [path for path in logs if 'R05_NATIVE_QA_PASS' in path.read_text(encoding='utf-8') and 'BUILD SUCCESSFUL' in path.read_text(encoding='utf-8')]
    if not successful:
        raise ValueError('No successful native log')
    report = {'id': 'zone-R05', 'name': '破碎档案厅', 'status': 'PASS', 'delivery_status': 'INDEPENDENT_QA_PASSED_AWAITING_INTEGRATION', 'sha256': fingerprint, 'reference_sha256': REFERENCE_HASH, 'size': metadata['size'], 'entrance': metadata['entrance'], 'interior': metadata['interior'], 'checks': {'static': {'status': static['status'], 'count': len(static['checks']), 'evidence': 'qa/static-qa.json'}, 'four_direction_interior_exterior_visual_review': {'status': 'PASS', 'evidence': 'qa/four-view-review.json', 'interior_sheet': 'views/interior-four-views.png', 'exterior_sheet': 'views/exterior-four-views.png'}, 'minecraft_builder_lodestone': {'status': lodestone['status'], 'evidence': 'qa/lodestone-preview.json'}, 'forge_registry_and_properties': {'status': registry['status'], 'states': registry['palette_states'], 'evidence': 'qa/native-registry-qa.json'}, 'forge_four_rotation_placement_stability': {'status': native['status'], 'rotations': 4, 'stable_ticks': native['stable_ticks'], 'records_each': 12528, 'state_changes_each': 0, 'bookshelves_each': 32, 'empty_containers_each': 2, 'survival_checks_each': 6404, 'support_checks_each': 243, 'reachable_feet_each': 417, 'halo_cells_unchanged_each': 7932, 'fixture_rollback': 'PASS', 'evidence': 'qa/native-world-qa.json', 'successful_log': str(successful[-1].relative_to(ROOT))}}, 'rejected_attempts_preserved': ['Initial Forge startup lacked empty.snbt fixture; corrected isolated test scaffold.', 'First placed revision had cobweb replacing a gallery fence, and Lodestone overconnected its neighboring fence. Native exact-state check rejected it. Cobweb moved to a wall and final rebuild passed all rotations. See qa/native-world-first-fence-rejection.json and earlier Forge logs.'], 'claims': {'deployed': False, 'production_files_modified': False, 'player_worlds_opened': False, 'production_integration_tested': False, 'client_gameplay_tested': False, 'global_slot_allocated': False}, 'integration_requirements': ['North entrance feet [13,2,0]; floor y=1. Connector must match elevation.', 'Independent dead-end hall with one north port, five blocks wide and five high.', 'Refuse occupied volume, including explicit air; no unconditional clearing.', 'Core owner must allocate unique slot and test hall connection before deployment.']}
    dump(ROOT / 'qa.json', report)
    filenames = ['room.nbt', 'room.json', 'preview.png', 'qa.json']
    artifacts = {name: {'sha256': digest(ROOT / name), 'bytes': (ROOT / name).stat().st_size} for name in filenames}
    dump(ROOT / 'qa/delivery-artifacts.json', {'status': 'PASS', 'id': 'zone-R05', 'nbt_sha256': fingerprint, 'files': artifacts})
    documentation = '\n'.join(['# zone-R05 破碎档案厅 · 独立交付', '', '四件套：room.nbt、room.json、preview.png、qa.json。', '', '尺寸 27×16×29；北入口脚位 [13,2,0]，内部安全点 [13,2,4]，后部安全点 [13,2,24]。地板 y=1，脚位 y=2；入口五格宽、五格高，主路五格宽。', '', 'NBT SHA-256：`' + fingerprint + '`。', '参考图 SHA-256：`' + REFERENCE_HASH + '`。', '', '验收：17 项静态检查；北南西东内外八视图；Minecraft Builder / Lodestone 对最终 NBT 的哈希核验与四张软件渲染；Forge 1.20.1 / 47.4.10 原生注册表和属性、模板往返、四旋转放置、全部邻居通知、600 tick 后精确状态对比、完整地板屋顶、支撑与原生 0.6×1.8 玩家体积扫掠通路、空库存及夹具回滚通过。', '', '每向 12528 条显式记录、6404 次原生生存条件检查、243 次专项支撑检查、417 个可达脚位、7932 个外围 halo 单元不变。32 格书架（两组低矮倒伏书架残件加一组立式书架）、2 个空木桶、3 面带纹样墙旗、4 盏悬灯；零奖励和零战利品表。', '', '视觉：views/interior-four-views.png、views/exterior-four-views.png；Lodestone 视图见 views/lodestone-*.png。preview.png 从最终 NBT 渲染，剖切仅发生在图像，不修改模板屋顶。旗帜软件外观近似，图案 NBT 保留且原生加载。', '', '设计适配：原版书架没有横躺方块状态，使用落地低矮书架残件表达倒伏；纸张和散落书采用地毯形制，不生成可拾取实体；残廊局部缺口在完整屋顶下，具有墙体、地柱和梁支撑；图中破窗改为冷色玻璃封闭围护，保留铁格和裂纹视觉。', '', '先前拒绝证据保留：缺少空夹具的首轮启动日志；原生栏杆精确状态拒绝记录 qa/native-world-first-fence-rejection.json。最终通过日志：' + str(successful[-1].relative_to(ROOT)) + '。', '', '复核入口：qa.json；详细证据：qa/static-qa.json、qa/native-registry-qa.json、qa/native-world-qa.json、qa/four-view-review.json、qa/lodestone-preview.json。四件套文件指纹：qa/delivery-artifacts.json。', '', '重建与 Forge 复验：运行本目录 Run-QA.ps1 -Rebuild；四向软件图运行 source/render_views.py；Lodestone 按 source/capture-preview.mjs 的 URL 参数连接实际预览服务。全部测试仅用本交付目录独立 GameTest 世界。', '', '未部署、未改正式源码/JAR/玩家世界、未占用其他 ID、未分配全局槽位。等待统一接入负责人分配槽位并完成大厅连接验收。软件预览与隔离 Forge 测试不等于完整整合包客户端/多人游玩验收。', ''])
    (ROOT / 'REPORT_FOR_COORDINATOR.md').write_text(documentation, encoding='utf-8')
    print(json.dumps({'status': 'PASS', 'sha256': fingerprint, 'delivery': str(ROOT), 'artifacts': artifacts}, ensure_ascii=False))


if __name__ == '__main__':
    main()
