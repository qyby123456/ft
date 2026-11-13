import hashlib
import json
import zipfile
from pathlib import Path
import audit

folder = Path(__file__).resolve().parent / 'S7-10-independent'
source = audit.BASE / 'FT地牢_第7组逐图重做_20261104/10-overview'
manifest = json.loads((source / 'SHA256SUMS.json').read_text(encoding='utf-8'))
for relative, expected in manifest.items():
    assert audit.digest(source / relative) == expected, relative
assert audit.digest(source / 'incoming/S7R2-10/room.nbt') == audit.digest(folder / 'room.nbt')
with zipfile.ZipFile(source / 'S7R2-10-overview-review.zip') as archive:
    assert archive.testzip() is None
    names = archive.namelist()
    assert len(names) == len(set(names))
    for relative, expected in manifest.items():
        assert hashlib.sha256(archive.read(relative)).hexdigest() == expected, relative
details = json.loads((folder / 'independent-details.json').read_text())
assert details['status'] == 'PASS_INDEPENDENT_STATIC_CHECKS_PENDING_FINAL_HANDOVER'
partitions = json.loads((folder / 'independent-partitions.json').read_text())
assert partitions['status'] == 'PASS_EXACT_DENSE_REASSEMBLY'
details.update(status='ACCEPTED_FOR_SEQUENTIAL_PRODUCTION', final_manifest_files_verified=len(manifest), zip_manifest_payloads_verified=len(manifest), next_image='NONE_AUTHORIZED_AFTER_10', series='S7_01_THROUGH_10_SEQUENTIALLY_REVIEWED_WITH_PER_IMAGE_BOUNDARIES')
(folder / 'independent-details.json').write_text(json.dumps(details, indent=2), encoding='utf-8')
print(json.dumps(details, indent=2))
