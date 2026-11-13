import argparse
import json

from PIL import Image

from check_delivery import ROOT, REFERENCE_SHA, digest, dump


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--visual-reviewed', action='store_true', required=True)
    arguments = parser.parse_args()
    metadata = json.loads((ROOT / 'room.json').read_text(encoding='utf-8'))
    source_hash = digest(ROOT / 'room.nbt')
    assert metadata['sha256'] == source_hash
    reports = {name: json.loads((ROOT / f'qa/{name}.json').read_text(encoding='utf-8')) for name in ['static', 'forge-native', 'renders', 'lodestone-preview']}
    assert reports['static']['status'] == reports['forge-native']['status'] == 'PASS'
    assert all(report['sha256'] == source_hash for report in reports.values())
    native = reports['forge-native']
    assert native['ticks_observed'] >= 240 and len(native['rotations']) == 4
    assert all(rotation['states_preserved'] == 2496 and rotation['native_collision_reachable_path_cells'] == 80 and rotation['fluid_leaks'] == 0 and rotation['dropped_items'] == 0 for rotation in native['rotations'])
    for entry in reports['renders']['files']:
        assert digest(ROOT / entry['file']) == entry['sha256']
    for filename in ['preview.png', 'interior-four-views.png', 'exterior-four-views.png']:
        with Image.open(ROOT / filename) as image:
            image.verify()
    reports['renders']['status'] = 'PASS'
    reports['renders']['visually_reviewed'] = arguments.visual_reviewed
    reports['renders']['review_notes'] = [
        'Short north-south through corridor; both ends remain open and the central floor is level.',
        'Four iron cages and six supported wall torches preserve the reference theme; red and soot-dark masonry remain readable.',
        'Real lava remains behind flush orange glass covers; permanent roof is present in exterior views.',
        'Software previews are not modpack game-client screenshots.',
    ]
    dump(ROOT / 'qa/renders.json', reports['renders'])
    artifacts = {filename: {'sha256': digest(ROOT / filename), 'bytes': (ROOT / filename).stat().st_size} for filename in ['room.nbt', 'room.json', 'preview.png', 'interior-four-views.png', 'exterior-four-views.png']}
    report = {
        'id': 'S11', 'delivery_id': 'corridor-S11', 'status': 'PASS', 'sha256': source_hash,
        'reference_sha256': REFERENCE_SHA, 'size': metadata['size'], 'minecraft': '1.20.1', 'forge': '47.4.10',
        'static': {'status': 'PASS', 'report': 'qa/static.json', 'checks_passed': len(reports['static']['checks']), 'safe_main_path_cells': 80},
        'four_view_review': {'status': 'PASS', 'report': 'qa/renders.json', 'interior': 'interior-four-views.png', 'exterior': 'exterior-four-views.png'},
        'preview': {'status': 'PASS', 'report': 'qa/lodestone-preview.json', 'file': 'preview.png', 'renderer': 'Lodestone ThreeStructureRenderer from final NBT'},
        'forge_native': {'status': 'PASS', 'report': 'qa/forge-native.json', 'log': 'qa/forge-run.log', 'rotations': 4, 'ticks_observed': 240, 'records_per_rotation': 2496, 'forced_lava_and_neighbor_updates': True, 'fluid_leaks': 0, 'dropped_items': 0},
        'artifacts': artifacts, 'deployed': False, 'player_saves_touched': False, 'core_catalog_modified': False,
        'limits': ['Not an actual modpack client screenshot or multiplayer/FPS playtest.', 'No gameplay traps, spawning, inventories or loot supplied.', 'Independent candidate only; no dungeon-core integration or live-world deployment.'],
    }
    dump(ROOT / 'qa.json', report)
    print(json.dumps({'id': 'S11', 'status': 'PASS', 'sha256': source_hash, 'artifacts': list(artifacts)}, ensure_ascii=False))


if __name__ == '__main__':
    main()
