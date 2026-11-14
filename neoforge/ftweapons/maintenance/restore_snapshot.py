import argparse
import hashlib
import json
import subprocess
from pathlib import Path

parser = argparse.ArgumentParser(description='Verify or restore the paused FT weapons source snapshot from existing Git objects.')
parser.add_argument('--repository', type=Path, required=True)
parser.add_argument('--commit', required=True)
parser.add_argument('--output', type=Path)
parser.add_argument('--verify-only', action='store_true')
arguments = parser.parse_args()
assert arguments.verify_only != (arguments.output is not None), 'Choose --verify-only or a new --output directory'
repository = arguments.repository.resolve()
prefix = 'neoforge/ftweapons/'

def git(*values):
    return subprocess.check_output(['git', '-C', str(repository), *values])

commit = git('rev-parse', '--verify', arguments.commit + '^{commit}').decode().strip()
manifest_data = git('show', commit + ':' + prefix + 'FILES.json')
manifest = json.loads(manifest_data)
entries = {}
for entry in git('ls-tree', '-r', '-z', commit, '--', prefix).split(b'\0'):
    if not entry:
        continue
    metadata, name = entry.split(b'\t', 1)
    mode, kind, object_id = metadata.split()
    assert mode == b'100644' and kind == b'blob'
    entries[name.decode('utf-8')] = object_id
assert set(entries) == set(manifest) | {prefix + 'FILES.json'}
output = arguments.output.resolve() if arguments.output is not None else None
if output is not None:
    assert not output.exists() and output.parent.is_dir()
    output.mkdir()
process = subprocess.Popen(['git', '-C', str(repository), 'cat-file', '--batch'], stdin=subprocess.PIPE, stdout=subprocess.PIPE)
verified = 0
for name, object_id in entries.items():
    process.stdin.write(object_id + b'\n')
    process.stdin.flush()
    header = process.stdout.readline().split()
    assert header[:2] == [object_id, b'blob']
    size = int(header[2])
    data = process.stdout.read(size)
    assert len(data) == size and process.stdout.read(1) == b'\n'
    if name in manifest:
        assert len(data) == manifest[name]['bytes'] and hashlib.sha256(data).hexdigest() == manifest[name]['sha256'], name
    else:
        assert data == manifest_data
    if output is not None:
        target = (output / name.removeprefix(prefix)).resolve()
        assert target.is_relative_to(output) and target != output
        target.parent.mkdir(parents=True, exist_ok=True)
        with target.open('xb') as stream:
            stream.write(data)
    verified += 1
process.stdin.close()
assert process.wait(timeout=30) == 0
print(json.dumps({'commit': commit, 'verifiedFiles': verified, 'output': str(output) if output else None, 'developmentResumed': False, 'installable': False}), flush=True)
