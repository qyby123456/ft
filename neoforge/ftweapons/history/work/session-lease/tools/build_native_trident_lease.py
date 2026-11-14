import hashlib
import json
import shutil
import subprocess
import sys
from pathlib import Path


work = Path(__file__).resolve().parents[1]
base = work / 'tools/build_native_ranged_use.py'
frozen = json.loads((work / 'reports/native-repair-ranged-use-candidate-9d1607e8.json').read_text(encoding='utf-8'))
assert hashlib.sha256(base.read_bytes()).hexdigest() == frozen['inputHashes'][str(base)]
result = subprocess.run([sys.executable, '-B', str(base)], cwd=work.parents[1])
assert result.returncode == 0
candidate = work / 'reports/native-repair-ranged-use-candidate.json'
receipt = json.loads(candidate.read_text(encoding='utf-8'))
attempt = Path(receipt['attempt'])
resource = work / 'proposal-native/ftboss.native-trident-lease.mixins.json'
configuration = json.loads(resource.read_text(encoding='utf-8'))
assert configuration['mixins'] == ['NativeTridentFactoryBoundaryMixin', 'NativeVanillaTridentImpactMixin']
for name in configuration['mixins']:
    assert (attempt / 'complete-classes/net/farmingtales/ftboss/mixin' / (name + '.class')).is_file()
target = attempt / 'resources' / resource.name
target.parent.mkdir()
shutil.copyfile(resource, target)
receipt['requiredProductionResources'] = {resource.name: str(target)}
receipt['tridentProductionHooksConfigured'] = True
for path in [Path(__file__), resource, target]:
    receipt['inputHashes'][str(path)] = hashlib.sha256(path.read_bytes()).hexdigest()
candidate.write_text(json.dumps(receipt, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps({'tridentResourceFrozen': target.name, 'newProductClasses': 0}))
