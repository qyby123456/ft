import hashlib
import json
import subprocess
import sys
from pathlib import Path

root = Path(__file__).resolve().parent
parent = root.parent / 'local-relief-v4'
sys.dont_write_bytecode = True
sys.path.insert(0, str(root))
from qa import Structure, plain
def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()
reports = []
for number in range(11, 21):
    room = root / f'S7-{number}'
    name = Path(json.loads((room / 'summary.json').read_text(encoding='utf-8'))['nbtPath']).name
    original_path = parent / room.name / name
    candidate = room / name
    original = Structure.read(original_path)
    current = Structure.read(candidate)
    old_design = json.loads((parent / room.name / 'design.json').read_text(encoding='utf-8'))
    design = json.loads((room / 'design.json').read_text(encoding='utf-8'))
    assert all(target in design['targets'] for target in old_design['targets'])
    changes = [{'position': position, 'before': str(original.get(position)), 'after': str(block)} for position, block in current.blocks.items() if original.get(position) != block]
    assert all(original.get(tuple(change['position'])).name == 'minecraft:air' for change in changes)
    assert all(current.get(position) == block for position, block in original.blocks.items() if block.name != 'minecraft:air')
    assert {position: plain(entity) for position, entity in current.block_entities.items()} == {position: plain(entity) for position, entity in original.block_entities.items()}
    assert [plain(entity) for entity in current.entities] == [plain(entity) for entity in original.entities]
    assert len(changes) == (4 if number == 15 else 8 if number == 16 else 0)
    record = {'id': room.name, 'candidate_sha256': digest(candidate), 'parent_sha256': digest(original_path), 'changes': changes, 'all_parent_goals_retained': True, 'all_parent_nonair_and_payloads_retained': True}
    if number in [15, 16]:
        output = root / 'reproduction' / room.name
        output.mkdir(parents=True, exist_ok=True)
        (output / 'design.json').write_bytes((room / 'design.json').read_bytes())
        command = ['node', r'C:\Users\YOUR_USERNAME\.codex\skills\minecraft-builder-skill\scripts\run-build.mjs', str(room / 'recipe.mjs'), '--out-dir', str(output), '--json']
        result = subprocess.run(command, capture_output=True, text=True, encoding='utf-8', errors='replace', timeout=90)
        (output / 'build.log').write_text(result.stdout + result.stderr, encoding='utf-8')
        assert result.returncode == 0
        result = subprocess.run([sys.executable, '-X', 'utf8', str(root / 'revise.py'), str(number), str(output)], capture_output=True, text=True, encoding='utf-8', errors='replace', timeout=90)
        (output / 'revise.log').write_text(result.stdout + result.stderr, encoding='utf-8')
        assert result.returncode == 0
        record['author_recipe_rebuild_sha256'] = digest(output / name)
        record['author_recipe_identical'] = record['author_recipe_rebuild_sha256'] == digest(candidate)
        assert record['author_recipe_identical']
    reports.append(record)
(root / 'access-change-audit.json').write_text(json.dumps({'status':'AUTHOR_EXACT_ACCESS_ONLY_PASS', 'scope':'Only new 15/16 recipes rebuilt; v4 independent recipe reproduction remains independently closed and is not claimed here. All 10 parent nonair cells, BE, entities and targets retained. Twelve air cells gain access blocks; no removals or decorative reclassification.', 'rooms':reports}, ensure_ascii=False, indent=2), encoding='utf-8')
print('12 air-only additions, all parent targets/payloads preserved, two new recipes byte-identical')
