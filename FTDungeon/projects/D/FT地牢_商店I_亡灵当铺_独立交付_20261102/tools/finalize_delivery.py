import hashlib
import json
import subprocess
import sys
import zipfile
from datetime import datetime, timedelta, timezone
from pathlib import Path

from PIL import Image

from build_room import ROOT, digest, write_json
from nbtio import Structure


def load(relative):
    return json.loads((ROOT / relative).read_text(encoding='utf-8'))


def main():
    metadata = load('out/room.json')
    fingerprint = digest(ROOT / 'out/room.nbt')
    before = {name: digest(ROOT / 'out' / name) for name in ('room.nbt', 'room.json', 'blueprint.json', 'materials.json')}
    subprocess.run([sys.executable, str(ROOT / 'tools/build_room.py')], cwd=ROOT, check=True, capture_output=True)
    after = {name: digest(ROOT / 'out' / name) for name in before}
    if before != after:
        raise ValueError('Rebuild is not reproducible')
    subprocess.run([sys.executable, str(ROOT / 'tools/qa_static.py')], cwd=ROOT, check=True, capture_output=True)
    write_json(ROOT / 'qa/reproducibility.json', {'status': 'PASS', 'before': before, 'after': after})
    static = load('qa/static.json')
    native = load('qa/native.json')
    render = load('qa/render-manifest.json')
    visual = load('qa/visual-review.json')
    reports = (metadata, static, native)
    if any(report.get('structure_sha256', report.get('sha256')) != fingerprint for report in reports):
        raise ValueError('QA or metadata was generated for another template')
    if static['status'] != 'PASS' or native['status'] != 'PASS' or visual['status'] != 'PASS':
        raise ValueError('A required QA gate has not passed')
    if render['source_nbt_sha256'] != fingerprint or visual['structure_sha256'] != fingerprint:
        raise ValueError('Visual evidence fingerprint mismatch')
    rotations = native['rotations']
    if len(rotations) != 4 or native['records_checked_after_ticks'] != 4576 or native['stabilization_ticks'] != 100:
        raise ValueError('Native rotation/stabilization coverage incomplete')
    if any(entry['records_checked'] != 1144 or entry['unexpected_world_entities'] != 0 for entry in rotations):
        raise ValueError('Native placement coverage mismatch')
    log = (ROOT / 'qa/forge-gametest.log').read_text(encoding='utf-8', errors='replace')
    for marker in ('SHOP_I_NATIVE_GAME_TEST_PASS: rotations=4/4, records=4576, ticks=100, stock=0', 'All 1 required tests passed', 'BUILD SUCCESSFUL'):
        if marker not in log:
            raise ValueError('Final native log lacks required marker: ' + marker)
    source = Structure.read(ROOT / 'out/room.nbt')
    captured = Structure.read(ROOT / 'qa/native-placed-none.nbt')
    if source.size != captured.size or source.blocks != captured.blocks:
        raise ValueError('Native NONE capture states differ from source NBT')
    captures = []
    for entry in rotations:
        path = ROOT / Path(entry['world_capture'])
        if digest(path) != entry['world_capture_sha256']:
            raise ValueError('Native capture fingerprint mismatch')
        captures.append({'file': str(path.relative_to(ROOT)), 'sha256': digest(path)})
    for relative in ('out/preview.png', 'preview/four-views.png', 'preview/plan.png', 'preview/native-world-capture.png', 'preview/exterior.png'):
        with Image.open(ROOT / relative) as image:
            image.verify()
    checks = {
        'static_layout_empty_inventory_supports': True,
        'forge_registry_states_block_entities_roundtrip': True,
        'native_four_rotations_100_tick_stability': True,
        'native_collision_survival_anchor_faces_guards': True,
        'native_negative_support_and_collision_probes': native['negative_missing_support_probes'] == 4 and native['negative_relic_rack_banner_support_probes'] == 12 and native['negative_blocked_aisle_probes'] == 4,
        'native_none_capture_equals_all_source_states': True,
        'all_four_capture_fingerprints_verified': True,
        'actual_nbt_four_view_visual_review': True,
        'deterministic_rebuild': True,
    }
    if not all(checks.values()):
        raise ValueError('Final QA checks incomplete')
    qa = {
        'status': 'PASS_STANDALONE_CANDIDATE',
        'generated_at': datetime.now(timezone(timedelta(hours=8))).isoformat(timespec='seconds'),
        'room_id': metadata['id'], 'variant': 'I', 'structure_sha256': fingerprint,
        'design_sha256': metadata['reference_sha256'], 'size': metadata['size'],
        'minecraft': '1.20.1', 'forge': '47.4.10', 'java': native['java'],
        'checks': checks, 'native_records': 4576, 'native_rotations': 4, 'stabilization_ticks': 100,
        'block_entities_per_rotation': rotations[0]['block_entities_checked'],
        'reachable_foot_positions_per_rotation': [entry['reachable_foot_positions'] for entry in rotations],
        'preplaced_entities': 0, 'reward_inventory_entries': 0, 'trade_logic_added': False,
        'evidence': {'static': 'qa/static.json', 'native': 'qa/native.json', 'native_log': 'qa/forge-gametest.log', 'world_captures': captures, 'render': 'qa/render-manifest.json', 'visual': 'qa/visual-review.json', 'reproducibility': 'qa/reproducibility.json'},
        'integration': metadata['integration'],
        'limitations': [
            'No formal catalog ID/slot assignment, FT gallery route, core idempotence/persistence or production JAR deployment tested.',
            'No full FarmingTales client, actual player screenshot, multiplayer, FPS or functional trade validation.',
            'Software previews use actual NBT states with documented cutaway and approximate block-entity appearances; they are not gameplay screenshots.',
            'Soul campfires remain damaging if a player deliberately climbs or dismantles guards; the ground-level entry, safe point and circulation do not traverse them.',
        ],
    }
    write_json(ROOT / 'out/qa.json', qa)
    exported = []
    for directory in ('out', 'preview', 'reference', 'tools', 'src'):
        exported.extend(path for path in (ROOT / directory).rglob('*') if path.is_file() and '__pycache__' not in path.parts)
    exported.extend(ROOT / name for name in ('README.md', 'HANDOFF.md', 'build.gradle', 'settings.gradle', 'gradle.properties'))
    exported.extend(path for path in (ROOT / 'qa').glob('*') if path.is_file() and path.name != 'file-manifest.json')
    manifest = {'files': [{'file': str(path.relative_to(ROOT)), 'bytes': path.stat().st_size, 'sha256': digest(path)} for path in sorted(exported)], 'excluded': ['Gradle caches/build outputs', 'isolated GameTest world', 'deployed JARs']}
    write_json(ROOT / 'qa/file-manifest.json', manifest)
    exported.append(ROOT / 'qa/file-manifest.json')
    archive = ROOT / 'delivery/shop-I-undead-pawn-20261102.zip'
    archive.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(archive, 'w', zipfile.ZIP_DEFLATED) as bundle:
        for path in sorted(exported):
            bundle.write(path, path.relative_to(ROOT).as_posix())
    with zipfile.ZipFile(archive) as bundle:
        if bundle.testzip() is not None:
            raise ValueError('Package CRC verification failed')
        for entry in manifest['files']:
            if hashlib.sha256(bundle.read(Path(entry['file']).as_posix())).hexdigest() != entry['sha256']:
                raise ValueError('Package contents differ from manifest')
    write_json(ROOT / 'delivery/package-verification.json', {'status': 'PASS', 'archive': str(archive), 'sha256': digest(archive), 'bytes': archive.stat().st_size, 'files_verified': len(exported), 'structure_sha256': fingerprint})
    print(json.dumps({'status': qa['status'], 'structure_sha256': fingerprint, 'archive': str(archive), 'files_verified': len(exported)}, ensure_ascii=False))


if __name__ == '__main__':
    main()
