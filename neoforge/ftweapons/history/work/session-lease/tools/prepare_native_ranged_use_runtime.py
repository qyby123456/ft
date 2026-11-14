import hashlib
import json
from pathlib import Path


work = Path(__file__).resolve().parents[1]
root = work.parents[1]
helper = work / 'tools/prepare_native_frost_ingress_runtime.py'
namespace = {'__file__': str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0], str(helper), 'exec'), namespace)


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


preparation = work / 'reports/native-crossbow-combat-preparation-2e5c6034.json'
old = json.loads(preparation.read_text(encoding='utf-8'))
old_source = Path(old['newObserver'])
old_launcher = Path(old['newLauncher'])
for path in [old_source, old_launcher]:
    assert digest(path) == old['inputHashes'][str(path)]
candidate = work / 'reports/native-repair-ranged-use-candidate.json'
receipt = json.loads(candidate.read_text(encoding='utf-8'))
tag = Path(receipt['attempt']).name.removeprefix('native-repair-')
frozen = work / ('reports/native-repair-ranged-use-candidate-' + tag + '.json')
assert not frozen.exists()
frozen.write_bytes(candidate.read_bytes())
source = work / ('tests/paid-crossbow-combat-' + tag + '/PositiveWeapon436.java')
launcher = work / ('tools/run_native_paid_crossbow_combat_' + tag + '.py')
namespace['add'](source.relative_to(root).as_posix(), old_source.read_text(encoding='utf-8'))
text = old_launcher.read_text(encoding='utf-8')
text = text.replace('native-repair-frost-ingress-candidate-2e5c6034.json', frozen.name)
text = text.replace('4b8d5adca7c6e8531088fa9d9e71f431c5c2e340bf8798ddceab04f0302ff891', digest(frozen))
text = text.replace('2e5c6034', tag)
namespace['add'](launcher.relative_to(root).as_posix(), text)
report = {
    'candidate': str(frozen), 'newObserver': str(source), 'newLauncher': str(launcher),
    'actualGenuinePaidSeedSample': 2, 'privateAuthorityWritten': False,
    'inputHashes': {str(path): digest(path) for path in [Path(__file__), helper, preparation, old_source, old_launcher, frozen, source, launcher]},
}
output = work / ('reports/native-ranged-use-preparation-' + tag + '.json')
assert not output.exists()
output.write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps({'tag': tag, 'candidateSha256': digest(frozen), 'launcher': str(launcher)}, ensure_ascii=True))
