import hashlib
import json
from datetime import datetime, timedelta, timezone
from pathlib import Path

from PIL import Image


ROOT = Path(__file__).resolve().parents[1]


def load(relative):
    return json.loads((ROOT / relative).read_text(encoding='utf-8'))


def digest(relative):
    return hashlib.sha256((ROOT / relative).read_bytes()).hexdigest()


def main():
    metadata = load('room.json')
    static = load('qa/static.json')
    native = load('qa/native.json')
    captures = load('qa/capture-review.json')
    visuals = load('qa/visuals.json')
    fingerprint = digest('room.nbt')
    assert all(report['structure_sha256'] == fingerprint for report in (static, native, captures, visuals))
    assert metadata['sha256'] == fingerprint and static['status'] == native['status'] == captures['status'] == 'PASS'
    assert len(native['rotations']) == 4 and native['stabilization_ticks'] == 200 and captures['records_compared'] == 25584
    assert len(visuals['images']) == 12
    for entry in visuals['images']:
        assert digest(entry['path']) == entry['sha256']
        with Image.open(ROOT / entry['path']) as image:
            image.verify()
    log = (ROOT / 'qa/forge-gametest.log').read_text(encoding='utf-8', errors='replace')
    assert 'CORRIDOR_M03_FORGE_PASS: rotations=4/4, records=25584, ticks=200' in log
    assert 'BUILD SUCCESSFUL' in log and 'All 1 required tests passed' in log
    report = {
        'id': 'corridor-M03',
        'status': 'PASS',
        'time': datetime.now(timezone(timedelta(hours=8))).isoformat(),
        'structure_sha256': fingerprint,
        'reference_sha256': metadata['reference_sha256'],
        'size': metadata['size'],
        'length_chunks': metadata['length_chunks'],
        'entrance': metadata['entrance'],
        'interior': metadata['interior'],
        'exit': metadata['exit'],
        'summary': {
            'static_checks': 'PASS',
            'nbt_records': static['block_records'],
            'non_air_blocks': static['non_air_blocks'],
            'palette_states': static['palette_states'],
            'straight_corridor_clear_width': 5,
            'straight_corridor_clear_height': 3,
            'straight_lane_positions': 205,
            'turns_side_branches_or_traversal_stairs': 0,
            'roof_complete': True,
            'fluids_hazard_blocks_rewards_or_preplaced_entities': 0,
            'lit_candle_clusters': 20,
            'banner_block_entities': 10,
            'native_registry_properties_nbt_roundtrip': 'PASS',
            'forge_placements': '4/4 PASS',
            'forced_chunk_ticks': 200,
            'native_records_after_ticks': 25584,
            'world_capture_semantic_mismatches': 0,
            'missing_support_negative_probes': 8,
            'blocked_aisle_negative_probes': 4,
            'interior_views': 4,
            'exterior_views': 4,
            'uncut_axis_perspectives': 2,
        },
        'visual_review': {
            'status': 'PASS',
            'review_method': 'Assistant visually inspected north and south full-template axis perspectives, the four-direction interior sheet and exterior sheet.',
            'observations': ['Continuous level red runner and dry red side inlays; both portals remain open.', 'Six repeating arches, five paired banner/candle bays, complete roof.', 'No cutaway edits are applied to room.nbt.'],
            'renderer': visuals['renderer'],
            'game_screenshot': False,
            'approximations': ['Banner cloth and sigil entity appearance approximated from the actual banner state and fixed three-layer pattern.', 'Software candle shading is an illustrative light approximation, not Minecraft light simulation.'],
            'lodestone': {'generation': 'Minecraft Builder Lodestone recipe and gzip roundtrip PASS', 'preview_server': 'http://127.0.0.1:5303/', 'headless_capture': 'Rejected: captured loading screen; qa/lodestone-loading-rejected.png is not acceptance imagery.'},
        },
        'evidence': {
            'builder': 'builder/summary.json',
            'static': 'qa/static.json',
            'forge': 'qa/native.json',
            'forge_log': 'qa/forge-gametest.log',
            'forge_raw_log': 'qa/forge-latest.log',
            'capture_review': 'qa/capture-review.json',
            'visuals': 'qa/visuals.json',
            'interior_sheet': 'views/interior-four-directions.png',
            'exterior_sheet': 'views/exterior-four-directions.png',
            'preview': 'preview.png',
        },
        'native_log_notes': ['Initial disposable fixture generated missing server.properties and Forge config files.', 'Forge update check/network, deprecation and temporary DUMMY block-entity warnings were logged; retained in raw logs. Final native state/survival/banner and independent capture comparisons all pass.'],
        'scope': {'deployed': False, 'player_saves_touched': False, 'ft_core_modified': False, 'parallel_deliveries_modified': False, 'writes_confined_to': str(ROOT), 'scratch_world': 'forge/run/world (isolated GameTest only)'},
        'limitations': ['No full modpack client, multiplayer, performance, production integration, gallery road, saved-data idempotence, combat or loot behavior tested.', 'Explicit air in the full template must not be used as authorization to clear occupied terrain; integration owner must validate the complete placement volume first.'],
    }
    (ROOT / 'qa.json').write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding='utf-8')
    print(json.dumps({'status': report['status'], 'sha256': fingerprint, 'required': ['room.nbt', 'room.json', 'preview.png', 'qa.json']}, ensure_ascii=False))


if __name__ == '__main__':
    main()
