import argparse
import collections
import json
import re
import subprocess
from pathlib import Path

ALLOWED = {'.md', '.json', '.java', '.py', '.ps1', '.in', '.mf', '.txt', '.properties', '.toml', '.xml', '.yaml', '.yml', '.gradle', '.kts', '.cfg'}
SPECIAL = {'.gitattributes', '.gitignore', 'gradlew', 'LICENSE', 'NOTICE'}
RESOURCE_DIRECTORIES = {'models', 'textures', 'animations', 'sounds', 'music', 'geo', 'geometries'}

def audit(repository, revision):
    listing = subprocess.check_output(['git', '-C', str(repository), 'ls-tree', '-r', '-z', revision])
    rows = []
    for entry in listing.split(b'\0'):
        if not entry:
            continue
        metadata, name = entry.split(b'\t', 1)
        mode, kind, object_id = metadata.split()
        assert kind == b'blob' and mode in (b'100644', b'100755'), 'Only regular source files are allowed'
        rows.append((name.decode('utf-8'), object_id.decode('ascii')))
    objects = list(dict.fromkeys(object_id for name, object_id in rows))
    result = subprocess.run(['git', '-C', str(repository), 'cat-file', '--batch'], input=('\n'.join(objects) + '\n').encode(), capture_output=True, check=True)
    payloads = {}
    offset = 0
    for object_id in objects:
        end = result.stdout.index(b'\n', offset)
        header = result.stdout[offset:end].split()
        assert header[0].decode() == object_id and header[1] == b'blob'
        size = int(header[2])
        offset = end + 1
        payloads[object_id] = result.stdout[offset:offset + size]
        offset += size + 1
    failures = []
    counts = collections.Counter()
    for name, object_id in rows:
        path = Path(name)
        suffix = path.suffix.lower()
        data = payloads[object_id]
        reason = None
        if suffix not in ALLOWED and path.name not in SPECIAL:
            reason = 'not an approved source/configuration/document file'
        elif suffix != '.java' and set(part.lower() for part in path.parts[:-1]) & RESOURCE_DIRECTORIES:
            reason = 'resource directory is excluded'
        elif b'\0' in data or data.startswith((b'PK\x03\x04', b'OggS', b'fLaC', b'ID3', b'RIFF', b'\x89PNG', b'\xff\xd8\xff', b'glTF', b'Kaydara FBX')):
            reason = 'binary/media/archive payload is excluded'
        else:
            try:
                text = data.decode('utf-8-sig')
                if re.search(r'data:(?:image|audio|model)/[^;,]+;base64,', text, re.I):
                    reason = 'embedded resource payload is excluded'
                if suffix == '.json':
                    value = json.loads(text)
                    if isinstance(value, dict) and ('minecraft:geometry' in value or isinstance(value.get('elements'), list) and isinstance(value.get('textures'), dict) or isinstance(value.get('bones'), list)):
                        reason = 'model/geometry JSON payload is excluded'
            except (UnicodeDecodeError, json.JSONDecodeError):
                reason = 'invalid text or JSON payload cannot be approved'
        if reason:
            failures.append({'path': name, 'reason': reason})
        counts[suffix or path.name] += 1
    return {'revision': revision, 'filesAudited': len(rows), 'uniqueBlobsAudited': len(objects), 'modelAndMusicUploadProhibited': True, 'binaryArchivesAndNestedPayloadsRejected': True, 'extensionCounts': dict(counts), 'issues': failures, 'passed': not failures}

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='Reject model/music resources and binary archives before source-only Git upload.')
    parser.add_argument('--repository', type=Path, required=True)
    parser.add_argument('--revision', default='HEAD')
    parser.add_argument('--output', type=Path, required=True)
    arguments = parser.parse_args()
    report = audit(arguments.repository.resolve(), arguments.revision)
    arguments.output.write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps({'filesAudited': report['filesAudited'], 'issues': len(report['issues']), 'passed': report['passed']}))
    raise SystemExit(0 if report['passed'] else 1)
