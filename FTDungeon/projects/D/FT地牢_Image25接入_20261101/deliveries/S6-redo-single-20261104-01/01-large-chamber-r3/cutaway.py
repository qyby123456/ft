import gzip
import hashlib
import json
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parent
sys.path.insert(0, 'D:/ft工程文件/血棘祭炉祭坛厅地牢_20261101/tools')
from structure_io import load_structure, nbt_payload

source = ROOT / 'room.nbt'
data = load_structure(source)
data['blocks'] = [record for record in data['blocks'] if record['pos'][1] < 15 and not (record['pos'][1] >= 2 and (record['pos'][0] == 0 or record['pos'][2] == 0))]
target = ROOT / 'lodestone'
target.mkdir(exist_ok=True)
kind, payload = nbt_payload(data)
derived = target / 'inspection-cutaway.nbt'
derived.write_bytes(gzip.compress(bytes([kind]) + b'\x00\x00' + payload, mtime=0))
(target / 'cutaway-provenance.json').write_text(json.dumps({'final_nbt_sha256': hashlib.sha256(source.read_bytes()).hexdigest(), 'cutaway_nbt_sha256': hashlib.sha256(derived.read_bytes()).hexdigest(), 'operation': 'remove roof/upper ribs y>=15 and front/west boundary cells x0 or z0 with y>=2; no added or changed states; actual structure remains intact', 'purpose': 'Lodestone inspection-only interior view; not placeable delivery'}, indent=2), encoding='utf-8')
print('Derived inspection cutaway from final NBT, no material substitutions')
