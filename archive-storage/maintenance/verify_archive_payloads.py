import hashlib
import json
import os
import time
import zipfile
from pathlib import Path
ROOT = Path(__file__).resolve().parents[3]
OUT = ROOT / 'work/session-boss/reports/github-large-artifact-offload-v1-20261114'
PROOFS = OUT / 'payload-readback'
PROOFS.mkdir(exist_ok=True)
items = json.loads((OUT / 'archive-inventory.json').read_text(encoding='utf-8'))['files']
for item in sorted(items, key=lambda row: row['bytes'], reverse=True):
    path = ROOT / item['path']
    if not path.exists():
        continue
    with path.open('rb') as stream:
        archive_sha = hashlib.file_digest(stream, 'sha256').hexdigest()
    proof = PROOFS / (archive_sha + '.json')
    if proof.exists():
        continue
    with zipfile.ZipFile(path) as bundle:
        names = set(bundle.namelist())
        metadata = next((name for name in ['ARCHIVE_MANIFEST.json', 'MANIFEST.json'] if name in names), None)
        if metadata is None:
            continue
        raw = bundle.read(metadata)
        manifest = json.loads(raw)
        content_addressed = manifest.get('format', '').startswith('ftboss-content-addressed-')
        files = manifest['files'] if content_addressed else manifest
        if not isinstance(files, dict) or not all(isinstance(row, dict) and 'sha256' in row and 'bytes' in row for row in files.values()):
            continue
        expected = {}
        for name, row in files.items():
            member = 'blobs/' + row['sha256'] if content_addressed else name
            if content_addressed and row.get('layer') == 'base':
                continue
            assert member in names, member
            if member in expected:
                assert expected[member] == {'sha256': row['sha256'], 'bytes': row['bytes']}
            expected[member] = {'sha256': row['sha256'], 'bytes': row['bytes']}
        total = 0
        for name, row in expected.items():
            hasher = hashlib.sha256()
            count = 0
            with bundle.open(name) as stream:
                while True:
                    data = stream.read(4 * 1024 * 1024)
                    if not data:
                        break
                    hasher.update(data)
                    count += len(data)
            assert count == row['bytes'] and hasher.hexdigest() == row['sha256'], name
            total += count
        result = {'archive': item['path'], 'archiveSha256': archive_sha, 'archiveBytes': item['bytes'], 'metadataMember': metadata, 'metadataSha256': hashlib.sha256(raw).hexdigest(), 'verifiedOwnPayloads': len(expected), 'verifiedOwnPayloadBytes': total, 'contentAddressed': content_addressed, 'allOwnPayloadsReadBackAndVerified': True, 'pid': os.getpid()}
        proof.write_text(json.dumps(result, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
        print(json.dumps({'verified': path.name, 'payloads': len(expected), 'uncompressedBytes': total}), flush=True)
