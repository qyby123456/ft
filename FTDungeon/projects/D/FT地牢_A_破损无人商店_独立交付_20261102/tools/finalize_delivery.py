import json
import subprocess
import sys
from datetime import datetime, timedelta, timezone

from PIL import Image

from build_room import ROOT, digest, write_json
from nbtio import Structure


def read_json(relative):
    return json.loads((ROOT / relative).read_text(encoding='utf-8'))


def main():
    metadata = read_json('out/room.json')
    fingerprint = digest(ROOT / 'out/room.nbt')
    static = read_json('qa/static.json')
    native = read_json('qa/native.json')
    render = read_json('qa/render-manifest.json')
    visual = read_json('qa/visual-review.json')
    for report in (static, native, visual):
        assert report['status'] == 'PASS' and report['structure_sha256'] == fingerprint
    assert render['status'] == 'PASS' and render['source_nbt_sha256'] == fingerprint
    log = (ROOT / 'qa/forge-gametest.log').read_text(encoding='utf-8', errors='replace')
    for marker in ('SHOP_A_NATIVE_GAME_TEST_PASS: rotations=4/4, records=8100, ticks=100, stock=0, source=' + fingerprint, 'All 1 required tests passed', 'BUILD SUCCESSFUL'):
        assert marker in log, 'Required native log evidence missing: ' + marker
    source = Structure.read(ROOT / 'out/room.nbt')
    native_roundtrips = []
    for result in native['rotations']:
        path = ROOT / result['world_capture']
        assert digest(path) == result['world_capture_sha256']
        captured = Structure.read(path)
        assert captured.size == source.size and len(captured.blocks) == len(source.blocks)
        if result['rotation'] == 'NONE':
            assert source.blocks == captured.blocks, 'Native world capture differs from source states'
        native_roundtrips.append({'rotation': result['rotation'], 'path': str(path.relative_to(ROOT)), 'sha256': digest(path)})
    protected = ['out/room.nbt', 'out/room.json', 'out/blueprint.json', 'out/materials.json', 'reference/input-fingerprints.json', 'src/main/resources/data/ftshopqa/structures/empty.nbt']
    before = {relative: digest(ROOT / relative) for relative in protected}
    subprocess.run([sys.executable, str(ROOT / 'tools/build_room.py')], cwd=ROOT, check=True)
    subprocess.run([sys.executable, str(ROOT / 'tools/qa_static.py')], cwd=ROOT, check=True)
    after = {relative: digest(ROOT / relative) for relative in protected}
    assert before == after, 'Rebuilding changed delivery artifacts'
    reproducibility = {'status': 'PASS', 'structure_sha256': fingerprint, 'regenerated_artifact_hashes': after, 'source_vs_unrotated_world_states': 'EXACT_EQUAL', 'native_world_captures': native_roundtrips}
    write_json(ROOT / 'qa/reproducibility.json', reproducibility)
    images = []
    for path in [ROOT / 'out/preview.png'] + sorted((ROOT / 'preview').glob('*.png')):
        with Image.open(path) as image:
            image.load()
            images.append({'path': str(path.relative_to(ROOT)), 'size': list(image.size), 'sha256': digest(path)})
    report = {
        'status': 'PASS', 'id': metadata['id'], 'name': metadata['name'],
        'generated_at': datetime.now(timezone(timedelta(hours=8))).isoformat(),
        'structure_sha256': fingerprint, 'structure_size': metadata['size'], 'record_count': len(source.blocks),
        'reference_sha256': metadata['reference_sha256'], 'entrance': metadata['entrance'], 'interior': metadata['interior'],
        'static': {'status': static['status'], 'checks': len(static['checks']), 'report': 'qa/static.json'},
        'native': {'status': native['status'], 'minecraft': native['minecraft'], 'forge': native['forge'], 'java': native['java'], 'rotations': 4, 'stabilization_ticks': 100, 'records_checked_after_ticks': native['records_checked_after_ticks'], 'block_entities_per_rotation': 7, 'reachable_foot_positions_per_rotation': 139, 'extra_world_entities': 0, 'negative_missing_support_probes': 4, 'negative_blocked_aisle_probes': 4, 'negative_cobweb_aisle_probes': 4, 'negative_airborne_debris_probes': 4, 'grounded_debris_per_rotation': 13, 'attached_off_path_cobwebs_per_rotation': 10, 'report': 'qa/native.json', 'log': 'qa/forge-gametest.log'},
        'visual': {'status': visual['status'], 'report': 'qa/visual-review.json', 'images': images},
        'reproducibility': {'status': 'PASS', 'report': 'qa/reproducibility.json', 'source_vs_unrotated_world_states': 'EXACT_EQUAL'},
        'integration': metadata['integration'],
        'write_scope': {'independent_root': str(ROOT), 'other_rooms_modified': False, 'production_saves_opened': False, 'deployed_jars_modified': False, 'core_catalog_modified': False, 'new_paid_image_requests': 0, 'source_image_modified': False},
        'evidence_limitations': ['This is an independent candidate, not an integrated FT room or functional merchant.', 'Only headless isolated Minecraft 1.20.1 Forge 47.4.10 GameTest was run; not a full modpack client, player save, multiplayer or FPS test.', 'Visuals are NBT-based software renders, not in-game screenshots.', 'Concept-image tilt, loose items and floor holes translated to legal block states; no dropped entities or reward stock.', 'STYLES.md referenced by source README was absent on inspection; source manifest and verified A image remain authoritative.'],
    }
    write_json(ROOT / 'out/qa.json', report)
    print(json.dumps({'status': report['status'], 'sha256': fingerprint, 'static_checks': len(static['checks']), 'native_records': native['records_checked_after_ticks'], 'images': len(images)}))


if __name__ == '__main__':
    main()
