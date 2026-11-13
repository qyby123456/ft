import hashlib
import json
import shutil
import subprocess
import sys
from pathlib import Path


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    root = Path(sys.argv[1]).resolve()
    target = root / 'independent-storage-reproduction'
    target.mkdir(exist_ok=True)
    prior = root.parent / 'S5-submission-20261107-065459-179761/independent-reproduction-final'
    inherited = []
    for name in ('tools.mjs', 'assemble.mjs'):
        shutil.copyfile(root / name, target / name)
    for number in range(11, 21):
        identifier = f'S5-{number}'
        folder = target / identifier
        filename = identifier.lower() + '.nbt'
        folder.mkdir(exist_ok=True)
        (folder / 'qa').mkdir(exist_ok=True)
        shutil.copyfile(root / identifier / 'design.json', folder / 'design.json')
        if number != 17:
            original = prior / identifier / filename
            assert digest(original) == digest(root / identifier / filename)
            shutil.copyfile(original, folder / filename)
            inherited.append(identifier)
        elif not (folder / filename).exists():
            shutil.copyfile(root / identifier / 'recipe.mjs', folder / 'recipe.mjs')
            process = subprocess.run([r'C:\Program Files\nodejs\node.exe', r'C:\Users\YOUR_USERNAME\.codex\skills\minecraft-builder-skill\scripts\run-build.mjs', str(folder / 'recipe.mjs'), '--out-dir', str(folder)], capture_output=True, timeout=120)
            (target / 'room17.log').write_bytes(process.stdout + process.stderr)
            assert process.returncode == 0
            assert digest(folder / filename) == digest(root / identifier / filename)
        assert digest(folder / filename) == digest(root / identifier / filename)
        (folder / 'qa/self-check.json').write_text(json.dumps({'sha256': digest(folder / filename), 'self_check_passed': True, 'scope': 'Recipe byte reproduction only'}), encoding='utf-8')
    process = subprocess.run([r'C:\Program Files\nodejs\node.exe', r'C:\Users\YOUR_USERNAME\.codex\skills\minecraft-builder-skill\scripts\run-build.mjs', str(target / 'assemble.mjs'), '--out-dir', str(target / 'merged')], capture_output=True, timeout=180)
    (target / 'merged.log').write_bytes(process.stdout + process.stderr)
    assert process.returncode == 0
    assert digest(target / 'merged/merged.nbt') == digest(root / 'merged/merged.nbt')
    report = {'stage': 'PARENT_INDEPENDENT_RECIPE_EXECUTION', 'changed_source17_and_merged_byte_exact': True, 'inherited_nine_from_parent_previous_independent_outputs': inherited, 'source17_sha256': digest(target / 'S5-17/s5-17.nbt'), 'merged_sha256': digest(target / 'merged/merged.nbt'), 'accepted': False}
    (root / 'independent-qa/storage-reproduction.json').write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding='utf-8')
    print(json.dumps(report))


if __name__ == '__main__':
    main()
