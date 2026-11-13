import hashlib
import json
from pathlib import Path
import shutil
import sys

ROOT = Path(__file__).resolve().parent
OUTPUT = ROOT / (sys.argv[1] if len(sys.argv) > 1 else 'runtime-interaction-r1')
assert not (OUTPUT / 'server').exists(), 'Existing runtime attempt must be preserved'
OUTPUT.mkdir(exist_ok=True)
source_project = ROOT / 'runtime-assembly-r1/native-test'
project = OUTPUT / 'native-test'
project.mkdir(exist_ok=True)
for name in ['build.gradle', 'settings.gradle', 'gradle.properties']:
    shutil.copyfile(source_project / name, project / name)
shutil.copytree(source_project / 'src/main/resources', project / 'src/main/resources', dirs_exist_ok=True)
shutil.copyfile(ROOT / 'runtime-assembly-r1/run.ps1', OUTPUT / 'run.ps1')
fixtures = json.loads((ROOT / 'qa/interaction-range-current/real_server_posttick-interactions.json').read_text(encoding='utf-8'))
assert len(fixtures['fixtures']) == 40 and all(fixture['pass'] for fixture in fixtures['fixtures'])
source = ROOT / 'assembly/s2-desert-functional-combined.nbt'
candidate_sha256 = hashlib.sha256(source.read_bytes()).hexdigest()
assert fixtures['merged_candidate_sha256'] == candidate_sha256
(OUTPUT / 'interaction-input.json').write_text(json.dumps({'candidate': str(source), 'source_sha256': candidate_sha256, 'fixture_evidence_sha256': hashlib.sha256((ROOT / 'qa/interaction-range-current/real_server_posttick-interactions.json').read_bytes()).hexdigest(), 'fixtures': fixtures['fixtures']}, ensure_ascii=False, indent=2), encoding='utf-8')
