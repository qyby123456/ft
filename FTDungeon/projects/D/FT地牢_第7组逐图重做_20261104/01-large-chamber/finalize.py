import hashlib
import json
from pathlib import Path
import shutil
import zipfile

from PIL import Image

ROOT = Path(__file__).resolve().parent
ROOM = ROOT / 'incoming/S7R2-01'


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def write(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def main():
    metadata = json.loads((ROOM / 'room.json').read_text(encoding='utf-8'))
    digest = sha(ROOM / 'room.nbt')
    assert digest == metadata['sha256']
    native = json.loads((ROOT / 'qa/native-review.json').read_text(encoding='utf-8'))
    assert native['status'] == 'PASS' and len(native['rooms']) == 1 and native['rooms'][0]['sha256'] == digest
    targets = json.loads((ROOT / 'qa/native-targets.json').read_text(encoding='utf-8'))
    assert targets['status'] == 'PASS' and targets['sha256'] == digest
    preview = json.loads((ROOT / 'qa/lodestone.json').read_text(encoding='utf-8'))
    assert len(preview) == 2 and all(entry['status'] == 'PASS' and entry['source_nbt_sha256'] == digest and not entry['errors'] for entry in preview)
    for entry in preview:
        assert sha(Path(entry['displayed_nbt'])) == entry['displayed_nbt_sha256']
        with Image.open(entry['screenshot']) as image:
            image.verify()
    duplicate = json.loads((ROOT / 'qa/duplicate-review.json').read_text(encoding='utf-8'))
    assert not duplicate['exact_matches']
    assert len(list(ROOT.glob('incoming/*/room.nbt'))) == 1
    shutil.copyfile(ROOT / 'lodestone-interior.png', ROOM / 'preview.png')
    rotations = {}
    for turns in range(4):
        entries = []
        for connection in metadata['connections']:
            block_x, block_y, block_z = connection['feet']
            delta_x, delta_y, delta_z = connection['normal']
            for unused in range(turns):
                block_x, block_z = 28 - block_z, block_x
                delta_x, delta_z = -delta_z, delta_x
            entries.append({**connection, 'feet': [block_x, block_y, block_z], 'normal': [delta_x, delta_y, delta_z], 'direction': {(0, -1): 'north', (1, 0): 'east', (0, 1): 'south', (-1, 0): 'west'}[(delta_x, delta_z)], 'outside_feet': [block_x + delta_x, block_y + delta_y, block_z + delta_z]})
        rotations[str(turns * 90)] = entries
    write(ROOT / 'rotated-connectors.json', {'source_sha256': digest, 'pivot_rule': 'clockwise XZ into nonnegative 29x29 bounding box: x=28-z,z=old_x; not arbitrary engine world pivot', 'rotations': rotations})
    write(ROOT / 'assembly.json', {'split': False, 'reason': 'Full 29x20x29 faithful-scale module fits one structure template; not reduced to chunk width.', 'world_origin': None, 'local_origin': [0, 0, 0], 'assembly_order': [{'step': 1, 'id': 'S7R2-01', 'template': str(ROOM / 'room.nbt'), 'relative_origin': [0, 0, 0]}], 'connections': metadata['connections'], 'collision_policy': 'Reject occupied full volume before any placement; no unconditional clearing.'})
    qa = {'status': 'PASS_SELF_CHECK_ONLY', 'id': metadata['id'], 'sha256': digest, 'static': 'PASS', 'native_load': 'PASS', 'native_all_targets': '7/7 PASS', 'lodestone': '2/2 PASS', 'duplication': 'NO_EXACT_GEOMETRY_REUSE_IN_LOCAL_COMPARISON', 'world_placement': 'NOT RUN', 'forge_updates': 'NOT RUN', 'registration_acceptance': 'PENDING', 'next_image_allowed': False}
    write(ROOM / 'qa.json', qa)
    write(ROOT / 'qa/visual-review.json', {'status': 'INSPECTED_WITH_DOCUMENTED_DEVIATIONS', 'source_sha256': digest, 'inspected': ['reference S7-01', 'Lodestone exterior', 'Lodestone interior', 'software north', 'software south'], 'matched': ['central terraced giant mushroom focal point', 'two upper rear mushrooms and two foreground small mushrooms', 'U-shaped raised rear gallery', 'lower side arches and upper rear arch', 'warm lights, fungal soil, purple crystals, inset shallow pools'], 'added_for_buildability': ['two lateral staircases', 'north lower FT entry', 'complete final front walls and roof', 'upper edge guardrails'], 'not_claimed': ['pixel-perfect scale measurement', 'custom shader particles', 'actual world stability', 'final coordinator acceptance']})
    pending = {'status': 'AWAITING_REGISTRATION_ACCEPTANCE', 'batch': 'S7 single-image rebuild 20261104', 'thread_id': '01a1a5df-d735-7b43-9d3b-2939bfc6c0ed', 'image_number': 1, 'reference': metadata['reference'], 'reference_sha256': metadata['reference_sha256'], 'room': str(ROOM / 'room.nbt'), 'room_sha256': digest, 'size': metadata['size'], 'registration_accepted': False, 'next_image_allowed': False, 'next_image_started': False, 'rule': 'Wait for explicit coordinator registration acceptance of this one image before taking or starting image02. No cross-group work, pre-generation or batch templates.', 'world_origin': None, 'deployed': False}
    write(ROOT / 'REGISTRATION_PENDING.json', pending)
    write(ROOT.parent / 'SERIAL_GATE.json', pending)
    write(ROOT / 'manifest.json', {'status': pending['status'], 'final_rooms': [metadata], 'preview_only_derivatives': [str(ROOT / 'views/PREVIEW_ONLY_cutaway.nbt')], 'qa': qa, 'scale': {'size': [29, 20, 29], 'low_floor_feet_y': 2, 'upper_floor_feet_y': 9, 'central_cap_diameter': 9, 'stair_width': 3, 'not_chunk_compressed': True}, 'reference_analysis': str(ROOT / 'REFERENCE_ANALYSIS.md'), 'old_batch_reused': False, 'next_image_started': False})
    files = sorted(path for path in ROOT.rglob('*') if path.is_file() and path.name != 'SHA256SUMS.json' and path.suffix != '.zip' and '__pycache__' not in path.parts and 'native-classes' not in path.parts)
    write(ROOT / 'SHA256SUMS.json', {path.relative_to(ROOT).as_posix(): sha(path) for path in files})
    output = ROOT / 'S7R2-01-single-image-review.zip'
    with zipfile.ZipFile(output, 'w', compression=zipfile.ZIP_DEFLATED) as package:
        for path in [*files, ROOT / 'SHA256SUMS.json']:
            package.write(path, path.relative_to(ROOT).as_posix())
    with zipfile.ZipFile(output) as package:
        assert package.testzip() is None
        sums = json.loads(package.read('SHA256SUMS.json'))
        assert all(hashlib.sha256(package.read(filename)).hexdigest() == expected for filename, expected in sums.items())
    print(json.dumps({'status': pending['status'], 'single_room': str(ROOM / 'room.nbt'), 'room_sha256': digest, 'archive': str(output), 'archive_sha256': sha(output), 'compared_templates': len(duplicate['compared']), 'next_image_started': False}, ensure_ascii=False))


if __name__ == '__main__':
    main()
