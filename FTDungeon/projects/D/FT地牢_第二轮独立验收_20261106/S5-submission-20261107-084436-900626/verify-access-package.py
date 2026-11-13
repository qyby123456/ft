import hashlib
import json
import zipfile
from pathlib import Path

root=Path(__file__).resolve().parent
destination=root/'access-package'
manifest=json.loads((destination/'manifest.json').read_text(encoding='utf-8'))
errors=[]
with zipfile.ZipFile(destination/'s5access.zip') as archive:
    for entry in manifest['files']:
        content=(destination/'s5access'/entry['path']).read_bytes()
        if hashlib.sha256(content).hexdigest()!=entry['sha256']:errors.append('manifest:'+entry['path'])
        if archive.read(entry['path'].replace('\\','/'))!=content:errors.append('zip:'+entry['path'])
        if entry['path'].startswith('data'):
            native=root/'qa/access-native-verified/native-test/src/main/resources'/entry['path']
            if entry['path'].replace('\\','/')=='data/s5access/structures/part_08.nbt':
                continue
            if native.read_bytes()!=content:errors.append('inherited-native-test-copy:'+entry['path'])
merged_hash=hashlib.sha256((root/'merged/merged.nbt').read_bytes()).hexdigest()
if merged_hash!=manifest['merged_sha256']:errors.append('merged-sha256')
report={'files':len(manifest['files']),'errors':errors,'merged_hash_unchanged':merged_hash==manifest['merged_sha256'],'zip_sha256':hashlib.sha256((destination/'s5access.zip').read_bytes()).hexdigest(),'passed':not errors,'inherited_access_runtime_sha256':'eb8ac4bccc0829e591cd03310cd6bee221321a6724e822c9368e8c303b729bb7','scope':'All commands/load/tick tags and lobby byte-identical to previously accepted runtime test; only part08 NBT and pack description replaced. New merged binding verified; no new access world test claimed.'}
(destination/'package-verification.json').write_text(json.dumps(report,indent=2),encoding='utf-8')
print(json.dumps(report))
assert not errors
