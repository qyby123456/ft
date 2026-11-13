import hashlib
import json
import shutil
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent
sys.dont_write_bytecode = True
sys.path.insert(0, str(ROOT))
from qa import Structure, plain
SKILL = Path(r'C:\Users\YOUR_USERNAME\.codex\skills\minecraft-builder-skill')
records = []
for number in range(11, 21):
    room = ROOT / f'S7-{number}'
    output = ROOT / 'reproduction' / room.name
    output.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(room / 'design.json', output / 'design.json')
    commands = [
        ['node', str(SKILL / 'scripts/run-build.mjs'), str(room / 'recipe.mjs'), '--out-dir', str(output), '--json'],
        [sys.executable, '-X', 'utf8', str(ROOT / 'revise.py'), str(number), str(output)]
    ]
    for command in commands:
        result = subprocess.run(command, capture_output=True, text=True, encoding='utf-8', errors='replace')
        with (output / 'rebuild.log').open('a', encoding='utf-8') as stream:
            stream.write(result.stdout + result.stderr)
        if result.returncode:
            raise RuntimeError(f'Reproduction failed: {room.name}')
    summary = json.loads((room / 'summary.json').read_text(encoding='utf-8'))
    source = Path(summary['nbtPath'])
    rebuilt = output / source.name
    source_hash = hashlib.sha256(source.read_bytes()).hexdigest()
    rebuilt_hash = hashlib.sha256(rebuilt.read_bytes()).hexdigest()
    original_structure = Structure.read(source)
    rebuilt_structure = Structure.read(rebuilt)
    equal_payload = original_structure.size == rebuilt_structure.size and original_structure.data_version == rebuilt_structure.data_version and original_structure.blocks == rebuilt_structure.blocks and {position: plain(entity) for position, entity in original_structure.block_entities.items()} == {position: plain(entity) for position, entity in rebuilt_structure.block_entities.items()} and [plain(entity) for entity in original_structure.entities] == [plain(entity) for entity in rebuilt_structure.entities]
    records.append({'id': room.name, 'candidate': str(source), 'sha256': source_hash, 'rebuilt': str(rebuilt), 'rebuilt_sha256': rebuilt_hash, 'identical': source_hash == rebuilt_hash, 'semantic_payload_equal': equal_payload, 'commands': commands})
    print(room.name, 'IDENTICAL' if source_hash == rebuilt_hash else 'PAYLOAD_EQUAL' if equal_payload else 'MISMATCH', flush=True)
(ROOT / 'reproduction.json').write_text(json.dumps(records, ensure_ascii=False, indent=2), encoding='utf-8')
if not all(record['semantic_payload_equal'] for record in records):
    raise SystemExit(2)
