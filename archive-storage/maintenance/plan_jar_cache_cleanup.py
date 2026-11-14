import collections
import hashlib
import json
import os
from pathlib import Path
ROOT = Path(__file__).resolve().parents[3]
WORK = ROOT / 'work/session-boss'
OUT = WORK / 'reports/github-large-artifact-offload-v1-20261114'
load = lambda path: json.loads(Path(path).read_text(encoding='utf-8-sig'))

def sha(path):
    with Path(path).open('rb') as stream:
        return hashlib.file_digest(stream, 'sha256').hexdigest()

proofs = [load(path) for path in (OUT / 'payload-readback').glob('*.json')]
coverage = {}
for proof in proofs:
    basename = Path(proof['archive']).name.replace('.zip', '').replace('.jar', '') + '-' + proof['metadataMember']
    path = OUT / 'archive-coverage' / basename
    if sha(path) != proof['metadataSha256']:
        path = OUT / 'archive-coverage' / (proof['metadataSha256'] + '.json')
    assert sha(path) == proof['metadataSha256']
    manifest = load(path)
    rows = manifest['files'] if proof['contentAddressed'] else manifest
    for name, row in rows.items():
        if not name.lower().endswith('.jar') or row.get('layer') == 'base':
            continue
        coverage.setdefault((row['bytes'], row['sha256']), {'archive': proof['archive'], 'archiveSha256': proof['archiveSha256'], 'member': 'blobs/' + row['sha256'] if proof['contentAddressed'] else name, 'payloadReadbackProof': proof['archiveSha256'] + '.json'})
sizes = {size for size, digest in coverage}
prior = load(WORK / 'reports/zicai-curio-client-v13-candidate-6559f481.json')
protected_directories = [Path(prior['attempt']).resolve()]
protected_directories.extend(path.resolve() for path in (WORK / 'build').glob('sparrow-evasion-native-baseline-v1-*'))
protected = set()
for receipt in [prior, load(WORK / 'build/boss-zicai-curio-silence-v1-20261113/candidate-build-receipt.json')]:
    protected.update(Path(name).resolve() for name in receipt['inputHashes'])
    if 'jar' in receipt:
        protected.add(Path(receipt['jar']).resolve())
context = load(WORK / 'build/boss-flight-ban-v1/compile-context.json')
protected.update(Path(name).resolve() for name in context['classpath'].split(';'))
roots = ['work/session-lease/build', 'work/session-chain/build', 'work/session-continuity/build', 'work/session-projection/build', 'batches', 'work/session-boss/build']
planned = {}
checked, unrepresented, kept = 0, 0, 0
for relative in roots:
    for base, directories, names in os.walk(ROOT / relative):
        directories[:] = [name for name in directories if name not in {'.git'} and not any((Path(base) / name).resolve() == folder for folder in protected_directories)]
        for name in names:
            if not name.lower().endswith('.jar'):
                continue
            path = (Path(base) / name).resolve()
            assert path.is_relative_to((ROOT / relative).resolve()) and not path.is_symlink()
            if path in protected:
                kept += 1
                continue
            size = path.stat().st_size
            if size not in sizes:
                unrepresented += 1
                continue
            digest = sha(path)
            checked += 1
            source = coverage.get((size, digest))
            if source is None:
                unrepresented += 1
                continue
            planned[path.relative_to(ROOT).as_posix()] = {'bytes': size, 'sha256': digest, **source}
            if len(planned) % 50 == 0:
                (OUT / 'jar-cache-plan-progress.json').write_text(json.dumps({'matchedFiles': len(planned), 'logicalBytes': sum(row['bytes'] for row in planned.values()), 'checkedJars': checked, 'pid': os.getpid()}, indent=2) + '\n', encoding='utf-8')
    print(json.dumps({'scannedRoot': relative, 'matchedFiles': len(planned), 'logicalBytes': sum(row['bytes'] for row in planned.values()), 'checkedJars': checked}), flush=True)
result = {'format': 'ft-archive-backed-jar-cache-eviction-v1', 'roots': roots, 'protectedRuntimeDirectories': [str(path.relative_to(ROOT)) for path in protected_directories], 'protectedRequiredInputCount': len(protected), 'deleteFiles': planned, 'logicalBytes': sum(row['bytes'] for row in planned.values()), 'jarsNotRepresentedInVerifiedPayloadMapRetained': unrepresented, 'explicitRequiredJarsRetained': kept, 'noFilesRemoved': True, 'requiresIndependentAssetAndGitManifestVerificationBeforeDeletion': True}
(OUT / 'jar-cache-cleanup-plan.json').write_text(json.dumps(result, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps({key: value for key, value in result.items() if key != 'deleteFiles'}), flush=True)
