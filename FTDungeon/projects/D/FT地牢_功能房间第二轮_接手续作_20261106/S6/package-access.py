import hashlib
import json
from pathlib import Path
from zipfile import ZIP_DEFLATED, ZipFile, ZipInfo

root = Path(__file__).resolve().parent
output = root / 'assembly' / 'access'
pack = output / 'datapack'
destination = output / 'fts6-access-mc1.20.1.zip'
records = []
with ZipFile(destination, 'w', compression=ZIP_DEFLATED) as archive:
    for filename in sorted(pack.rglob('*')):
        if not filename.is_file():
            continue
        relative = filename.relative_to(pack).as_posix()
        payload = filename.read_bytes()
        info = ZipInfo(relative, date_time=(1980, 1, 1, 0, 0, 0))
        info.compress_type = ZIP_DEFLATED
        archive.writestr(info, payload)
        records.append({'path': relative, 'sha256': hashlib.sha256(payload).hexdigest(), 'bytes': len(payload)})
with ZipFile(destination) as archive:
    assert sorted(archive.namelist()) == sorted(record['path'] for record in records)
    for record in records:
        assert hashlib.sha256(archive.read(record['path'])).hexdigest() == record['sha256']
    assert 'pack.mcmeta' in archive.namelist()
report = {'zip': str(destination), 'sha256': hashlib.sha256(destination.read_bytes()).hexdigest(), 'zip_roundtrip': 'PASS', 'files': records}
(output / 'package.json').write_text(json.dumps(report, indent=2, ensure_ascii=False) + '\n', encoding='utf-8')
print(json.dumps({'zip': str(destination), 'files': len(records), 'sha256': report['sha256'], 'zip_roundtrip': 'PASS'}, ensure_ascii=False))
