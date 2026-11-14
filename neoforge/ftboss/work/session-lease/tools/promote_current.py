import hashlib
import os
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]
original = ROOT / 'work/session-projection/tools/promote_current.py'
assert hashlib.sha256(original.read_bytes()).hexdigest() == '39d1beb48394a4d4ac0170f5f65f6ca8adcce71654cd51d01060c0c649053fb7'
source = original.read_text(encoding='utf-8')
source = source.replace("THREAD = '01a1c40b-b9ff-7f71-8abb-dcecbb37929f'", "THREAD = os.environ['CODEX_THREAD_ID']")
source = source.replace('for path in [previous, accepted_path, candidate_path, checked, Path(__file__)]:',
                        'for path in [previous, accepted_path, candidate_path, checked, Path(__file__), Path(successor_script)]:')
exec(compile(source, str(original), 'exec'), {'__file__': str(original), 'successor_script': str(Path(__file__).resolve())})
