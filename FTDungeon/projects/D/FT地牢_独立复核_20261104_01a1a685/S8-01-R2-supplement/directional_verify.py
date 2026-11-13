import hashlib
import json
from pathlib import Path
import os
import subprocess
import sys

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parent
BUNDLE = Path('D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/S8-rebuild-single-20261104-01')
SKILL = Path('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill')
sys.path.insert(0, str(BUNDLE))
from deliver import load

CUTS = {
    'north': [([0, 3, 0], [34, 19, 1]), ([4, 19, 4], [30, 19, 6])],
    'east': [([33, 3, 0], [34, 19, 34]), ([28, 19, 4], [30, 19, 30])],
    'south': [([0, 3, 33], [34, 19, 34]), ([4, 19, 28], [30, 19, 30])],
    'west': [([0, 3, 0], [1, 19, 34]), ([4, 19, 4], [6, 19, 30])],
}


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def states(data):
    return {tuple(block['pos']): data['palette'][block['state']] for block in data['blocks']}


def main():
    manifest = json.loads((BUNDLE / 'checksums.json').read_text(encoding='utf-8'))
    assert all(digest(BUNDLE / relative) == value for relative, value in manifest.items())
    source_path = BUNDLE / 'incoming/S8R_IMG01/room.nbt'
    source = states(load(source_path))
    commands = []
    records = []
    for direction, cuts in CUTS.items():
        env = dict(os.environ, S8_INSPECTION_DIRECTION=direction)
        command = ['node', str(SKILL / 'scripts/run-build.mjs'), str(ROOT / 'directional-cutaway.mjs'), '--out-dir', str(ROOT / 'directional' / direction), '--data-version', '3465', '--no-block-state-updates']
        result = subprocess.run(command, cwd=SKILL, env=env, capture_output=True, text=True, encoding='utf-8', errors='replace', timeout=90, creationflags=subprocess.CREATE_NO_WINDOW)
        commands.append({'argv': command, 'cwd': str(SKILL), 'environment_override': {'S8_INSPECTION_DIRECTION': direction}, 'exit_code': result.returncode})
        (ROOT / f'directional-build-{direction}.log').write_text(result.stdout + result.stderr, encoding='utf-8')
        if result.returncode:
            raise RuntimeError(result.stderr)
        derived_path = ROOT / 'directional' / direction / f'inspection-only-{direction}.nbt'
        derived = states(load(derived_path))
        assert source.keys() == derived.keys()
        all_cuts = [([0, 20, 0], [34, 20, 34]), *cuts]
        expected = dict(source)
        for first, last in all_cuts:
            for block_x in range(first[0], last[0] + 1):
                for block_y in range(first[1], last[1] + 1):
                    for block_z in range(first[2], last[2] + 1):
                        expected[block_x, block_y, block_z] = {'Name': 'minecraft:air'}
        assert derived == expected, direction
        changes = sum(source[position] != derived[position] for position in source)
        records.append({'direction': direction, 'source_nbt_sha256': digest(source_path), 'derived_nbt': str(derived_path), 'derived_sha256': digest(derived_path), 'air_cut_inclusive_boxes': all_cuts, 'changed_positions': changes, 'all_other_positions_states_unchanged': True, 'candidate_eligible': False, 'purpose': 'INSPECTION_ONLY_DIRECTION_SPECIFIC'})
    preview_command = ['node', str(ROOT / 'directional-preview.cjs')]
    result = subprocess.run(preview_command, cwd=ROOT, capture_output=True, text=True, encoding='utf-8', errors='replace', timeout=180, creationflags=subprocess.CREATE_NO_WINDOW)
    (ROOT / 'directional-preview.log').write_text(result.stdout + result.stderr, encoding='utf-8')
    commands.append({'argv': preview_command, 'cwd': str(ROOT), 'exit_code': result.returncode})
    if result.returncode:
        raise RuntimeError(result.stdout + result.stderr)
    browser = json.loads((ROOT / 'directional-browser.json').read_text(encoding='utf-8'))
    sheet = Image.new('RGB', (1400, 1160), '#17202b')
    draw = ImageDraw.Draw(sheet)
    font = ImageFont.truetype('C:/Windows/Fonts/msyh.ttc', 19)
    draw.text((15, 8), 'S8-01 R2：四向分别剖切；仅检查，非正式候选NBT', fill='white', font=font)
    for index, record in enumerate(browser['records']):
        with Image.open(ROOT / record['screenshot']) as image:
            image.verify()
        with Image.open(ROOT / record['screenshot']) as image:
            sheet.paste(image.resize((700, 540)), ((index % 2) * 700, 50 + (index // 2) * 555))
        assert record['derived_sha256'] == records[index]['derived_sha256']
    sheet.save(ROOT / 'directional-four-view-contact.png')
    assert all(digest(BUNDLE / relative) == value for relative, value in manifest.items())
    output = {'status': 'PASS_EXACT_DERIVATION_BROWSER_HASHES_AND_BUNDLE_IMMUTABILITY', 'source_nbt_sha256': digest(source_path), 'source_bundle_manifest_verified_before_after': len(manifest), 'world_placement_and_400ticks': 'NOT_RUN', 'derivation_directly_from_final_nbt_not_recipe_regeneration': True, 'records': records}
    (ROOT / 'directional-provenance.json').write_text(json.dumps(output, ensure_ascii=False, indent=2), encoding='utf-8')
    (ROOT / 'directional-commands.json').write_text(json.dumps(commands, ensure_ascii=False, indent=2), encoding='utf-8')
    print(output['status'])


if __name__ == '__main__':
    main()
