import hashlib
import json
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parent
sys.path.insert(0, 'C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
from nbtio import Structure, AIR, plain

candidate = ROOT / 'incoming/S4-09/room.nbt'
structure = Structure.read(candidate)
before = dict(structure.blocks)
before_entities = dict(structure.block_entities)
removed = []
for position, state in before.items():
    east, up, south = position
    if up >= 14 and state != AIR:
        structure.set(position, AIR)
        removed.append(position)
destination = ROOT / 'preview-only/upper-cutaway.nbt'
structure.write(destination)
readback = Structure.read(destination)
assert structure.size == readback.size == (31, 22, 31)
assert readback.entities == structure.entities and readback.block_entities == structure.block_entities
assert readback.block_entities == {position: payload for position, payload in before_entities.items() if position not in removed}
changed = [position for position in set(before) | set(readback.blocks) if before.get(position, AIR) != readback.get(position)]
assert set(changed) == set(removed) and all(readback.get(position) == AIR for position in changed)
trace = {'removed_positions': [list(position) for position in sorted(removed)], 'removed_block_entities': [{'position': list(position), 'nbt': plain(payload)} for position, payload in before_entities.items() if position in removed], 'surviving_block_entities_unchanged': len(readback.block_entities)}
(ROOT / 'preview-only/removal-trace.json').write_text(json.dumps(trace, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
record = {'status': 'PASS_AIR_ONLY_DERIVATIVE', 'candidate_sha256': hashlib.sha256(candidate.read_bytes()).hexdigest(), 'derivative_sha256': hashlib.sha256(destination.read_bytes()).hexdigest(), 'candidate': str(candidate), 'derivative': str(destination), 'rule': 'At original coordinates replace non-air by air iff Y>=14; keep bounding box31x22x31, chest, 5 stair rows, both statue heads atY12, pottery shelves atY13, floor and water; upper banner blocks and their entities removed together.', 'removed_blocks': len(changed), 'no_added_or_material_changed_blocks': True, 'deployment': 'FORBIDDEN_PREVIEW_ONLY', 'scope': 'Cut-only visibility aid; not an alternative candidate or survival-valid structure.'}
(ROOT / 'cutaway-check.json').write_text(json.dumps(record, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps(record, ensure_ascii=True, indent=2))



