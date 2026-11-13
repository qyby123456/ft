import hashlib
import json
from datetime import datetime, timedelta, timezone
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
REVIEWED_NBT = 'd60c6db9493324ef5343aa89e43846f01d02f57f105776a0a50fe77ba4765d19'
REVIEWED_IMAGES = {
    'preview.png': 'e2b0282a0aeb40b6b5ae3b91c647b111069e371bc84e4ccc3d20480a2819b2b1',
    'views/north-interior.png': 'ff0dd858e6e9ce25d0ef2c2112b09864616222ea1a2cada1cc9a2d82a98fe29b',
    'views/north-exterior.png': '5e99d10fe94100498e1b355f50c0c053bf280902ab1012612433460ef975cc0c',
    'views/south-interior.png': '1add3549d15c26dc1320173794d48d3aa2a478911a18563b19a69b17c13d379c',
    'views/south-exterior.png': '02bd6dd5d97c7eaf493e78fb82e1b8661c5bfcab1bc8428ae7d4a067dd86118c',
    'views/west-interior.png': '831d87b671a06e84a246bd89923ad5e7420022f86dcd04c8c7ae61358c5c6402',
    'views/west-exterior.png': '7293a7515456d2bc3c0e795e2364b8b7a37c4f0c318b7b784b72fb609e9ce59e',
    'views/east-interior.png': '56f76cb69248700cca7b277e9dc1ed06b45b23f68f0465e8d967018465df8a22',
    'views/east-exterior.png': '176fc570b8f6895838747ae8dcb18d493d91e2d82791ffe4111d3355caa193f3'
}


def load(relative):
    return json.loads((ROOT / relative).read_text(encoding='utf-8'))


def save(relative, payload):
    (ROOT / relative).write_text(json.dumps(payload, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def digest(file_path):
    return hashlib.sha256(file_path.read_bytes()).hexdigest()


def record(relative):
    file_path = ROOT / relative
    return {'path': relative, 'sha256': digest(file_path), 'bytes': file_path.stat().st_size}


sha256 = digest(ROOT / 'room.nbt')
assert sha256 == REVIEWED_NBT, 'Changed NBT requires a new actual visual review'
assert all(digest(ROOT / relative) == expected for relative, expected in REVIEWED_IMAGES.items()), 'Changed images require a new actual visual review'
metadata = load('room.json')
static = load('qa/static.json')
native = load('qa/forge-native.json')
execution = load('qa/forge-result.json')
views = load('views/manifest.json')
assert all(payload['sha256'] == sha256 for payload in (metadata, static, native, execution, views))
assert static['status'] == native['status'] == execution['status'] == 'PASS'
assert native['ticks_observed'] == 240 and native['forced_fluid_ticks'] and native['forced_neighbor_updates']
assert {entry['rotation'] for entry in native['rotations']} == {'NONE', 'CLOCKWISE_90', 'CLOCKWISE_180', 'COUNTERCLOCKWISE_90'}
assert len(views['images']) == 9 and views['source_unchanged']
for image_record in views['images']:
    image_path = ROOT / image_record['path']
    assert digest(image_path) == image_record['sha256']
    with Image.open(image_path) as image:
        assert image.size == (1536, 1024)
        image.verify()
assert 'M16_FORGE_PASS' in (ROOT / 'qa/forge-run.log').read_text(encoding='utf-8', errors='replace')
assert 'BUILD SUCCESSFUL' in (ROOT / 'qa/forge-run.log').read_text(encoding='utf-8', errors='replace')
review = {
    'id': 'corridor-M16', 'status': 'PASS', 'sha256': sha256,
    'reviewer': 'Producer assistant inspected all eight full-resolution final-NBT views and the eye-level preview',
    'reviewed_images': [record(image_record['path']) for image_record in views['images']],
    'checks': {
        'reference_theme': 'PASS: wet stone, moss, teal water, warm lanterns, spruce beams and empty iron cages retained',
        'straight_no_stairs_no_branches': 'PASS: 40-block north/south axis; no side doorway or elevation change',
        'walkway_entrance_exit': 'PASS: uninterrupted five-block-wide dry central path and two clear masonry arches',
        'pillar_and_beam_joints': 'PASS: six crossbeams sit on masonry corbels, longitudinal beams meet crossbeams',
        'hanging_supports': 'PASS: lantern and cage chains meet ceiling beams; no freestanding hanging fixtures',
        'plants_and_intersections': 'PASS: vines stay on side walls, not in dry route; no obvious clipping of main fixtures',
        'liquid_boundaries': 'PASS: side channels have base and end dams; four elevated spring columns are glass-fronted and masonry-backed',
        'full_shell_and_roof': 'PASS: all exterior views retain roof and sidewalls, only north/south arches are openings',
        'containers': 'PASS: there are no containers, inventories, block entities or preplaced mobs'
    },
    'known_adaptations': metadata['design_adaptations'],
    'preview_only_cutaway': 'Interior images omit roof and the corresponding outer wall only in memory. This exposes water at the cut plane in previews; final NBT is sealed.',
    'limitations': ['Software preview, not modpack client screenshots', 'Producer review, not receiving-thread independent acceptance',
                    'Forge collision/fluid evidence is supplied separately; no claim of hall integration, idempotence, multiplayer or FPS QA']
}
save('qa/visual-review.json', review)
views['status'] = 'PASS_PRODUCER_VISUAL_REVIEW'
views['visual_review'] = record('qa/visual-review.json')
save('views/manifest.json', views)
font = ImageFont.truetype('C:/Windows/Fonts/segoeui.ttf', 24)
small_font = ImageFont.truetype('C:/Windows/Fonts/segoeui.ttf', 17)
sheet = Image.new('RGB', (1536, 1760), '#101a20')
draw = ImageDraw.Draw(sheet)
draw.text((28, 16), 'CORRIDOR M16  |  15 x 11 x 40  |  FINAL NBT REVIEW', font=font, fill='#dce8e9')
draw.text((28, 49), 'Software render: cutaways are preview-only; exterior views retain the complete roof.', font=small_font, fill='#96afb5')
for row, side in enumerate(('north', 'south', 'west', 'east')):
    for column, kind in enumerate(('interior', 'exterior')):
        position = (column * 768, 80 + row * 420)
        draw.text((position[0] + 26, position[1] + 3), f'{side.upper()} / {kind.upper()}', font=small_font, fill='#90cfbc')
        with Image.open(ROOT / f'views/{side}-{kind}.png') as image:
            image.thumbnail((744, 380), Image.Resampling.LANCZOS)
            sheet.paste(image, (position[0] + (768 - image.width) // 2, position[1] + 32))
sheet.save(ROOT / 'views/contact-sheet.png')
report = {
    'id': 'corridor-M16', 'status': 'PASS', 'stage': 'producer_delivery_ready_for_receiver_acceptance',
    'sha256': sha256, 'reference_sha256': metadata['reference_sha256'],
    'completed_at': datetime.now(timezone(timedelta(hours=8))).isoformat(timespec='seconds'),
    'checks': {'static': static['status'], 'four_direction_final_nbt_visual_review': review['status'],
               'minecraft_registry_and_properties': native['native_registry_complete_properties'],
               'native_structure_roundtrip': native['native_structure_roundtrip'],
               'forge_four_rotation_240_tick_stability': native['status'], 'forge_exit_code': execution['exit_code']},
    'metrics': {'size': metadata['size'], 'explicit_blocks_including_air': 6600, 'dry_path_width': 5,
                'dry_headroom': 3, 'roof_blocks': 600, 'source_water_cells': 208, 'hanging_cages': 6,
                'lanterns': 12, 'chains': 42, 'vines': 56, 'dropped_items': 0, 'fluid_leaks': 0},
    'evidence': [record(relative) for relative in ('room.json', 'summary.json', 'preview.png', 'qa/static.json',
                'qa/forge-native.json', 'qa/forge-result.json', 'qa/forge-invocation.json', 'qa/forge-run.log',
                'qa/native-roundtrip.nbt', 'qa/visual-review.json', 'views/manifest.json', 'views/contact-sheet.png')],
    'receiver_independent_acceptance': 'PENDING', 'runtime_slot': None, 'deployed': False,
    'formal_mods_configs_and_saves_modified': False,
    'limits': ['Forge 47.4.10 / Minecraft 1.20.1 headless isolated QA, not full modpack client gameplay',
               'No hall connection, full-volume occupancy rejection or persistent-generation/idempotence integration implemented or tested',
               'The receiving integration owner must assign a unique corridor-prefixed slot and independently accept this exact NBT hash before deployment']
}
save('qa.json', report)
handoff = {'id': 'corridor-M16', 'producer_thread': '01a19f18-3fdd-75e2-9c37-22c2e93a6b99',
           'delivery': str(ROOT), 'sha256': sha256, 'reference': metadata['reference'],
           'reference_sha256': metadata['reference_sha256'], 'status': 'PRODUCER_QA_PASS',
           'receiver_acceptance': 'PENDING', 'deployed': False, 'runtime_slot': None,
           'entry_points': {'metadata': 'room.json', 'qa': 'qa.json', 'preview': 'preview.png',
                            'four_views': 'views/manifest.json', 'visual_review': 'qa/visual-review.json',
                            'forge': 'qa/forge-native.json', 'instructions': 'README.md'}}
save('handoff.json', handoff)
files = ['room.nbt', 'room.json', 'preview.png', 'qa.json', 'handoff.json', 'README.md', 'summary.json']
for folder in ('qa', 'views', 'tools', 'forge/src'):
    files.extend(file_path.relative_to(ROOT).as_posix() for file_path in sorted((ROOT / folder).rglob('*')) if file_path.is_file())
files.extend(('forge/build.gradle', 'forge/settings.gradle', 'forge/gradle.properties'))
save('delivery-manifest.json', {'id': 'corridor-M16', 'sha256': sha256, 'status': 'PRODUCER_QA_PASS',
                               'files': [record(relative) for relative in sorted(set(files))],
                               'excluded': ['Forge generated build/cache/run files are not deployable assets', 'delivery-manifest.json excludes its own recursive hash']})
print(json.dumps({'status': 'PASS', 'sha256': sha256, 'delivery': str(ROOT), 'files': len(set(files))}, ensure_ascii=False, indent=2))
