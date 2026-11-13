import hashlib
import json
import shutil
import subprocess
import sys
from pathlib import Path

from audit_s8_submission import load


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def run(command, cwd, log):
    process = subprocess.run(command, cwd=cwd, capture_output=True, timeout=240)
    log.write_bytes(process.stdout + process.stderr)
    assert process.returncode == 0, str(log)


def main():
    root = Path(sys.argv[1]).resolve()
    records = load(root / 'submission-snapshot.json')['files']
    for item in records:
        assert digest(Path(item['snapshot'])) == item['sha256']
    target = root / 'independent-reproduction'
    target.mkdir()
    for name in ('common.mjs', 'qa.py', 'revise.py', 'assemble.py'):
        shutil.copyfile(root / name, target / name)
    ready = load(root / 'ready-for-review.json')
    rebuilt = []
    for room in ready['rooms']:
        identifier = room['id']
        folder = target / identifier
        folder.mkdir()
        for name in ('recipe.mjs', 'design.json'):
            shutil.copyfile(root / identifier / name, folder / name)
        run([r'C:\Program Files\nodejs\node.exe', r'C:\Users\YOUR_USERNAME\.codex\skills\minecraft-builder-skill\scripts\run-build.mjs', str(folder / 'recipe.mjs'), '--out-dir', str(folder), '--json'], target, target / (identifier + '.log'))
        run([sys.executable, '-X', 'utf8', str(target / 'revise.py'), identifier.split('-')[1], str(folder)], target, target / (identifier + '-revise.log'))
        candidate = folder / Path(room['candidate']).name
        assert digest(candidate) == room['sha256']
        (folder / 'qa').mkdir()
        shutil.copyfile(root / identifier / 'qa/self-check.json', folder / 'qa/self-check.json')
        rebuilt.append({'id': identifier, 'sha256': digest(candidate), 'binary_equal': True})
        print(identifier, 'EXACT', flush=True)
    run([sys.executable, '-X', 'utf8', str(target / 'assemble.py')], target, target / 'assembly.log')
    outputs = [root / 'assembly/s7-fungal-functions-connected.nbt', *sorted((root / 'assembly/parts').glob('*.nbt'))]
    assert len(outputs) == 16
    assemblies = []
    for source in outputs:
        candidate = target / source.relative_to(root)
        assert digest(candidate) == digest(source), str(source)
        assemblies.append({'file': str(source.relative_to(root)), 'sha256': digest(candidate), 'binary_equal': True})
    for item in records:
        assert digest(Path(item['snapshot'])) == item['sha256']
    result = {'stage': 'PARENT_INDEPENDENT_TEN_ROOM_MERGED_AND_PART_RECIPE_EXECUTION', 'rooms': rebuilt, 'assembly_and_parts': assemblies, 'all_26_outputs_binary_equal': True, 'candidate_nbt_used_as_reproduction_input': False, 'frozen_files_unchanged': len(records), 'accepted': False}
    (root / 'independent-qa/reproduction.json').write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding='utf-8')
    print('All 26 NBT outputs independently byte-exact', flush=True)


if __name__ == '__main__':
    main()
