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
            if native.read_bytes()!=content:errors.append('native-test-copy:'+entry['path'])
merged_hash=hashlib.sha256((root/'merged/merged.nbt').read_bytes()).hexdigest()
if merged_hash!=manifest['merged_sha256']:errors.append('merged-sha256')
report={'files':len(manifest['files']),'errors':errors,'merged_hash_unchanged':merged_hash==manifest['merged_sha256'],'zip_sha256':hashlib.sha256((destination/'s5access.zip').read_bytes()).hexdigest(),'passed':not errors}
(destination/'package-verification.json').write_text(json.dumps(report,indent=2),encoding='utf-8')
print(json.dumps(report))
assert not errors
