import hashlib
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent
OLD = Path(r'D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S7')

def relocate(value):
    if isinstance(value, str):
        return value.replace(str(OLD), str(ROOT)).replace(str(OLD).replace('\\', '/'), str(ROOT).replace('\\', '/'))
    if isinstance(value, list):
        return [relocate(entry) for entry in value]
    if isinstance(value, dict):
        return {key: relocate(entry) for key, entry in value.items()}
    return value

records = []
for number in range(11, 21):
    room = ROOT / f'S7-{number}'
    for filename in ['summary.json', 'design.json']:
        target = room / filename
        target.write_text(json.dumps(relocate(json.loads(target.read_text(encoding='utf-8'))), ensure_ascii=False, indent=2), encoding='utf-8')
    for candidate in room.glob('*.nbt'):
        original = OLD / room.name / candidate.name
        records.append({'room': room.name, 'source': str(original), 'candidate': str(candidate), 'sha256': hashlib.sha256(candidate.read_bytes()).hexdigest(), 'source_sha256': hashlib.sha256(original.read_bytes()).hexdigest(), 'unchanged_at_takeover': original.read_bytes() == candidate.read_bytes()})
reference_file = ROOT / 'references.json'
reference_file.write_text(json.dumps(relocate(json.loads(reference_file.read_text(encoding='utf-8'))), ensure_ascii=False, indent=2), encoding='utf-8')
(ROOT / 'takeover-source-manifest.json').write_text(json.dumps({'source_read_only': str(OLD), 'records': records, 'independent_acceptance': 'NOT_PASSED', 'inherited_qa': 'Historical only; copied reports contain old paths. New checks required.'}, ensure_ascii=False, indent=2), encoding='utf-8')
print(json.dumps(records, ensure_ascii=False, indent=2))
