import json
from datetime import datetime, timedelta, timezone
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

from static_qa import digest, require, write_json


ROOT = Path(__file__).resolve().parents[1]


def main():
    timestamp = datetime.now(timezone(timedelta(hours=8))).isoformat()
    fingerprint = digest(ROOT / 'room.nbt')
    metadata = json.loads((ROOT / 'room.json').read_text(encoding='utf-8'))
    require(metadata['sha256'] == fingerprint, 'Metadata fingerprint mismatch')
    evidence = {}
    for name in ('static', 'native', 'render', 'lodestone'):
        evidence[name] = json.loads((ROOT / f'qa/{name}.json').read_text(encoding='utf-8'))
        require(evidence[name]['status'] == 'PASS' and evidence[name]['structure_sha256'] == fingerprint, f'Stale or failing {name} evidence')
    require(len(evidence['native']['rotations']) == 4, 'Incomplete native rotation verification')
    for rotation in evidence['native']['rotations']:
        require(rotation['status'] == 'PASS' and rotation['capture_matches_source'], 'Native rotation failed')
        require(digest(ROOT / rotation['capture'].replace('\\', '/')) == rotation['capture_sha256'], 'Native capture fingerprint mismatch')
    log = (ROOT / 'qa/forge-gametest.log').read_text(encoding='utf-8', errors='replace')
    require('BUILD SUCCESSFUL' in log and f'S15_CORRIDOR_NATIVE_PASS rotations=4/4 records=9600 ticks=200 items=0 sha256={fingerprint}' in log, 'Native build success evidence missing')
    with Image.open(ROOT / 'preview/lodestone-interior.png') as interior:
        preview = Image.new('RGB', (1536, 1262), '#121d23')
        preview.paste(interior.convert('RGB'), (0, 75))
    draw = ImageDraw.Draw(preview)
    draw.text((25, 18), 'S15 陈旧监牢短廊  |  15×10×16  |  南北常开 · 三宽三高', font=ImageFont.truetype('C:/Windows/Fonts/msyh.ttc', 28), fill='#ded7bc')
    draw.text((25, 1234), '最终NBT / Lodestone软件渲染，非游戏截图；牢房与吊笼为装饰，无库存或实体。', font=ImageFont.truetype('C:/Windows/Fonts/msyh.ttc', 18), fill='#c1cfc9')
    preview.save(ROOT / 'preview.png')
    metadata['preview'] = 'preview.png'
    metadata['preview_engine'] = 'Lodestone 0.6.0, final room.nbt, not a Minecraft client screenshot'
    metadata['connection_contract'] = {'floor_y': 0, 'feet_y': 1, 'axis': 'z', 'external_approach': 'Integrator must extend a solid three-wide floor north from Z=-1 and south from Z=16 at Y=0; do not cap either open end.', 'assigned_slot': None}
    metadata['qa_status'] = 'PASS'
    metadata['updated_at'] = timestamp
    write_json(ROOT / 'room.json', metadata)
    checks = [
        {'check': 'design_reference', 'status': 'PASS', 'detail': 'Reference hash verified. Gray aged/mossy stone, paired prison fronts, cross beams, cyan soul lamps and two suspended cages preserved; closed far gate intentionally changed to open through-exit.'},
        {'check': 'north_and_south', 'status': 'PASS', 'detail': 'Both corridor mouths open at X=6..8,Y=1..3; continuous level floor; no gate, step, collision prop, branch, liquid or cobweb in through-route.'},
        {'check': 'west_and_east', 'status': 'PASS', 'detail': 'Three closed cells on each side. Carpets rest on full wood platforms; no exposed redstone or accessible stock; side doors not used as corridor exits.'},
        {'check': 'suspension_and_support', 'status': 'PASS', 'detail': 'Four lamps each have chain-to-beam support. Both 2x2 cages have four full-height frame columns, cap/base plates and four chains anchored into top-layer roof; cages start at Y=4 above clear passage.'},
        {'check': 'pillars_and_beams', 'status': 'PASS', 'detail': 'Four continuous horizontal beams seat into shell and masonry piers. All solids belong to one connected support component; native canSurvive and neighbor-derived shapes pass in four rotations.'},
        {'check': 'shell_and_safety', 'status': 'PASS', 'detail': '240 top-layer roof blocks and 240 floor blocks complete; only designed north/south openings; two verified 3x3x3 safe landings. No fluids, block entities, containers, inventories, loot tables or entities.'},
        {'check': 'native_stability', 'status': 'PASS', 'detail': 'Four forced-ticking rotated placements, complete 9600-state comparison after 200 ticks, native world capture/reload, and 16 negative probes passed.'}
    ]
    four_view = {
        'status': 'PASS', 'reviewed_at': timestamp, 'structure_sha256': fingerprint,
        'basis': 'Direct visual inspection of reference PNG, final NBT four-view sheet, complete exterior, plan, Lodestone interior and Forge world-capture render; correlated with native/statical evidence.',
        'views': ['preview/north.png', 'preview/south.png', 'preview/west.png', 'preview/east.png'],
        'additional_views': ['preview/four-views.png', 'preview/exterior.png', 'preview/exterior-south.png', 'preview/plan.png', 'preview/native-world-capture.png', 'preview/lodestone-interior.png'],
        'checks': checks, 'findings_unresolved': [], 'renderer_cutaway_only': True,
        'limitations': ['Review images are software renders, not in-game screenshots; isolated Forge test is not full modpack client/multiplayer/FPS acceptance.']
    }
    write_json(ROOT / 'qa/four-view-review.json', four_view)
    render = evidence['render']
    render['preview'] = {'file': 'preview.png', 'source': 'preview/lodestone-interior.png', 'engine': 'Lodestone 0.6.0', 'cutaway': False, 'annotation_only': True}
    write_json(ROOT / 'qa/render.json', render)
    evidence_files = ['qa/static.json', 'qa/native.json', 'qa/forge-gametest.log', 'qa/render.json', 'qa/lodestone.json', 'qa/four-view-review.json', 'qa/materials.json']
    evidence_files.extend(rotation['capture'].replace('\\', '/') for rotation in evidence['native']['rotations'])
    report = {
        'status': 'PASS', 'id': metadata['id'], 'design_id': 'S15', 'name': metadata['name'], 'verified_at': timestamp,
        'structure_sha256': fingerprint, 'reference_sha256': metadata['reference_sha256'],
        'size': metadata['size'], 'four_piece_delivery': ['room.nbt', 'room.json', 'preview.png', 'qa.json'],
        'static_qa': {'status': 'PASS', 'evidence': 'qa/static.json', 'records': 2400, 'clear_passage': '3x3x16', 'supported_lamps': 4, 'supported_cages': 2},
        'four_view_qa': {'status': 'PASS', 'evidence': 'qa/four-view-review.json', 'sheet': 'preview/four-views.png', 'unresolved_findings': 0},
        'forge_native_qa': {'status': 'PASS', 'minecraft': '1.20.1', 'forge': '47.4.10', 'evidence': 'qa/native.json', 'log': 'qa/forge-gametest.log', 'rotations': 4, 'stability_ticks': 200, 'records_checked_after_ticks': 9600, 'forced_ticking_chunks': True, 'negative_probes': 16, 'capture_reload_matches': True},
        'software_preview': {'status': 'PASS', 'evidence': 'qa/lodestone.json', 'is_game_screenshot': False},
        'inventory': {'containers': 0, 'items': 0, 'loot_tables': 0, 'entities': 0},
        'evidence': [{'path': filename, 'sha256': digest(ROOT / filename)} for filename in evidence_files],
        'integration': {'state': 'standalone_not_deployed', 'ready_for_unified_integration_review': True, 'slot': None, 'production_jars_changed': False, 'player_saves_opened_or_modified': False},
        'limitations': ['Isolated Forge GameTestServer, not full modpack client, multiplayer gameplay, FPS or user save validation.', 'NBT software previews use simulated lighting; no claim of exact vanilla/modpack visual fidelity.', 'Final world slot and corridor approach floors must be assigned by the unified integration stage.']
    }
    write_json(ROOT / 'qa.json', report)
    handoff = {
        'id': metadata['id'], 'design_id': 'S15', 'delivery_directory': str(ROOT), 'status': 'PASS_STANDALONE_READY_FOR_REVIEW',
        'structure_sha256': fingerprint, 'reference': metadata['reference'], 'reference_sha256': metadata['reference_sha256'],
        'size': metadata['size'], 'entrance': metadata['entrance'], 'exit': metadata['exit'], 'safe_points': metadata['safe_points'],
        'files': ['room.nbt', 'room.json', 'preview.png', 'qa.json'], 'four_views': 'preview/four-views.png',
        'native_qa': 'qa/native.json', 'integration_state': 'not_deployed_unassigned_slot',
        'boundaries': 'Only this corridor-S15 delivery directory was written. No registry, incoming, shared source, production mod, config, resourcepack or player save edits.'
    }
    write_json(ROOT / 'handoff.json', handoff)
    files = [ROOT / name for name in ['room.nbt', 'room.json', 'preview.png', 'qa.json', 'README.md', 'handoff.json', 'recipe.mjs', 'blueprint.json', 'summary.json', 'build.gradle', 'settings.gradle', 'gradle.properties']]
    for folder in ['qa', 'preview', 'tools', 'src']:
        files.extend(path for path in (ROOT / folder).rglob('*') if path.is_file() and '__pycache__' not in path.parts and 'forge-run' not in path.parts and 'previous-' not in path.name)
    for path in files:
        if path.suffix.lower() == '.png':
            with Image.open(path) as image:
                image.load()
    lines = [f'{digest(path)}  {path.relative_to(ROOT).as_posix()}' for path in sorted(set(files))]
    (ROOT / 'SHA256SUMS.txt').write_text('\n'.join(lines) + '\n', encoding='utf-8')
    print(json.dumps({'status': 'PASS', 'files_hashed': len(lines), 'sha256': fingerprint, 'directory': str(ROOT)}, ensure_ascii=False))


if __name__ == '__main__':
    main()
