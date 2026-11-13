import hashlib
import json
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parent
sys.path.insert(0, 'C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
from nbtio import Structure, AIR, plain

candidate = ROOT / 'incoming/S4-10/room.nbt'
structure = Structure.read(candidate)
before = dict(structure.blocks)
before_entities = dict(structure.block_entities)
removed = []
for position, state in before.items():
    if position[1] >= 10 and state != AIR:
        structure.set(position, AIR)
        removed.append(position)
destination = ROOT / 'preview-only/upper-cutaway.nbt'
structure.write(destination)
readback = Structure.read(destination)
assert readback.size == structure.size == (73, 22, 73)
assert readback.data_version == structure.data_version == 3465
assert readback.entities == structure.entities
assert readback.block_entities == before_entities
changed = [position for position in set(before) | set(readback.blocks) if before.get(position, AIR) != readback.get(position)]
assert set(changed) == set(removed) and all(readback.get(position) == AIR for position in changed)
trace = {'removed_positions': [list(position) for position in sorted(removed)], 'removed_block_entities': [], 'surviving_unchanged_block_entities': [{'position': list(position), 'nbt': plain(payload)} for position, payload in before_entities.items()]}
(ROOT / 'preview-only/removal-trace.json').write_text(json.dumps(trace, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
record = {'status': 'PASS_AIR_ONLY_DERIVATIVE', 'candidate_sha256': hashlib.sha256(candidate.read_bytes()).hexdigest(), 'derivative_sha256': hashlib.sha256(destination.read_bytes()).hexdigest(), 'candidate': str(candidate), 'derivative': str(destination), 'rule': 'Replace non-air iff Y>=10 at original coordinates. Keep 73x22x73 full volume. All 5 original container payloads unchanged; no block entity removed. Upper statue/pillar heads, gate caps, wall tops and persistent leaves removed by the same rule.', 'removed_blocks': len(changed), 'no_added_or_material_changed_blocks': True, 'deployment': 'FORBIDDEN_PREVIEW_ONLY', 'scope': 'Visibility aid only, not a survival-valid alternative candidate or placement part.'}
(ROOT / 'cutaway-check.json').write_text(json.dumps(record, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps(record, ensure_ascii=True, indent=2))
