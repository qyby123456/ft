import argparse
import hashlib
import json
import subprocess
from pathlib import Path, PurePosixPath
parser = argparse.ArgumentParser(description='Restore one verified text/source Git blob without Windows long-path revision stat.')
parser.add_argument('--repository', type=Path, required=True)
parser.add_argument('--commit', required=True)
parser.add_argument('--git-path', required=True)
parser.add_argument('--sha256', required=True)
parser.add_argument('--output', type=Path, required=True)
arguments = parser.parse_args()
repository = arguments.repository.resolve()
relative = PurePosixPath(arguments.git_path)
assert not relative.is_absolute() and '..' not in relative.parts and relative.as_posix() == arguments.git_path
assert relative.suffix.lower() in {'.java', '.py', '.json', '.md', '.txt'}
assert len(arguments.sha256) == 64 and all(character in '0123456789abcdef' for character in arguments.sha256)
listing = subprocess.check_output(['git', '-C', str(repository), 'ls-tree', '-z', arguments.commit, '--', arguments.git_path])
entries = [entry for entry in listing.split(b'\0') if entry]
assert len(entries) == 1
metadata, name = entries[0].split(b'\t', 1)
assert name.decode('utf-8') == arguments.git_path and metadata.split()[1] == b'blob'
object_id = metadata.split()[2].decode('ascii')
data = subprocess.check_output(['git', '-C', str(repository), 'cat-file', 'blob', object_id])
assert hashlib.sha256(data).hexdigest() == arguments.sha256 and b'\0' not in data
data.decode('utf-8-sig')
output = arguments.output.resolve()
assert output.parent.is_dir() and not output.exists() and not output.is_symlink()
with output.open('xb') as stream:
    stream.write(data)
assert output.read_bytes() == data
print(json.dumps({'gitPath': arguments.git_path, 'commit': arguments.commit, 'blobObjectId': object_id, 'bytes': len(data), 'sha256': arguments.sha256, 'restoredSourceOnly': True, 'noRemoteModelOrMusicRecoveryClaimed': True}), flush=True)
