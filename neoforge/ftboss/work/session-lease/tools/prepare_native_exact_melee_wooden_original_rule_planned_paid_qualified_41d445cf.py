import hashlib
import json
from pathlib import Path

work = Path(__file__).resolve().parents[1]
root = work.parents[1]
def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()
def load(path):
    return json.loads(path.read_text(encoding='utf-8'))
parent = work / 'reports/native-exact-melee-wooden-original-rule-planned-paid-preparation-41d445cf.json'
frozen = load(parent)
for name, wanted in frozen['inputHashes'].items():
    assert digest(Path(name)) == wanted, Path(name).name
failed_compile = work / 'build/exact-melee-wooden-original-rule-planned-paid-precompile-qualified-41d445cf/terminal.json'
assert load(failed_compile)['exitCode'] == 1
old_source = Path(frozen['newObserver'])
old_launcher = Path(frozen['newLauncher'])
source = old_source.read_text(encoding='utf-8')
assert source.count('new HashMap<>(') == 4
source = source.replace('new HashMap<>(', 'new java.util.HashMap<>(')
helper = work / 'tools/prepare_native_frost_ingress_runtime.py'
namespace = {'__file__': str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0], str(helper), 'exec'), namespace)
new_source = work / 'tests/exact-melee-wooden-original-rule-planned-paid-qualified-41d445cf/PositiveWeapon436.java'
namespace['add'](new_source.relative_to(root).as_posix(), source)
new_launcher = work / 'tools/run_native_exact_melee_wooden_original_rule_planned_paid_qualified_41d445cf.py'
preparation = work / 'reports/native-exact-melee-wooden-original-rule-planned-paid-qualified-preparation-41d445cf.json'
launcher = old_launcher.read_text(encoding='utf-8').replace(old_source.relative_to(work).as_posix(), new_source.relative_to(work).as_posix())
launcher = launcher.replace(parent.relative_to(work).as_posix(), preparation.relative_to(work).as_posix())
launcher = launcher.replace('native-repair-runtime-exact-melee-wooden-original-rule-planned-paid-41d445cf.json', 'native-repair-runtime-exact-melee-wooden-original-rule-planned-paid-qualified-41d445cf.json')
namespace['add'](new_launcher.relative_to(root).as_posix(), launcher)
paths = [Path(__file__), parent, failed_compile, old_source, old_launcher, helper, new_source, new_launcher]
namespace['add'](preparation.relative_to(root).as_posix(), json.dumps({
    'newObserver': str(new_source), 'newLauncher': str(new_launcher), 'actualCases': 1,
    'productChanged': False, 'onlyCorrection': 'Fully qualify four java.util.HashMap constructions; preserve original planner and all assertions',
    'serverJvmStarted': False, 'inputHashes': {str(path): digest(path) for path in paths}
}, ensure_ascii=False, indent=2) + '\n')
print(json.dumps({'prepared': preparation.name, 'serverJvmStarted': False}))
