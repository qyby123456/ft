import hashlib
import json
from pathlib import Path

work = Path(__file__).resolve().parents[1]
root = work.parents[1]
parent = work / 'reports/native-trident-foundation-gate-preparation-338f0feb.json'
frozen = json.loads(parent.read_text(encoding='utf-8'))
for name, expected in frozen['inputHashes'].items():
    assert hashlib.sha256(Path(name).read_bytes()).hexdigest() == expected, name
helper = work / 'tools/prepare_native_frost_ingress_runtime.py'
namespace = {'__file__': str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0], str(helper), 'exec'), namespace)
old_source = Path(frozen['newObserver'])
old_launcher = Path(frozen['newLauncher'])
new_source = work / 'tests/trident-strict-qualified-338f0feb/PositiveWeapon436.java'
new_launcher = work / 'tools/run_native_trident_strict_qualified_338f0feb.py'
preparation = work / 'reports/native-trident-strict-qualified-preparation-338f0feb.json'
source = old_source.read_text(encoding='utf-8')
assert source.count('ItemStack copiedPickup') == 1 and source.count('((ItemStack)foreignCustody') == 1
source = source.replace('ItemStack copiedPickup', 'net.minecraft.world.item.ItemStack copiedPickup').replace('((ItemStack)foreignCustody', '((net.minecraft.world.item.ItemStack)foreignCustody')
if new_source.exists():
    assert new_source.read_text(encoding='utf-8') == source
else:
    namespace['add'](new_source.relative_to(root).as_posix(), source)
launcher = old_launcher.read_text(encoding='utf-8')
assert launcher.count("tests/trident-foundation-gate-338f0feb/PositiveWeapon436.java") == 1
launcher = launcher.replace('tests/trident-foundation-gate-338f0feb/PositiveWeapon436.java', 'tests/trident-strict-qualified-338f0feb/PositiveWeapon436.java')
anchor = "save(attempt / 'inputs-before308.json', inputs)"
assert launcher.count(anchor) == 3
head, tail = launcher.rsplit(anchor, 1)
launcher = head + "inputs[str(WORK / 'reports/native-trident-strict-qualified-preparation-338f0feb.json')] = digest(WORK / 'reports/native-trident-strict-qualified-preparation-338f0feb.json')\n" + anchor + tail
namespace['add'](new_launcher.relative_to(root).as_posix(), launcher)
paths = [Path(__file__), parent, helper, old_source, old_launcher, new_source, new_launcher]
result = {'candidate': frozen['candidate'], 'newObserver': str(new_source), 'newLauncher': str(new_launcher), 'authorityFieldsWritten': False, 'priorCompileOnlyFailure': 'repair-runtime-3c2b6f8e', 'inputHashes': {str(path): hashlib.sha256(path.read_bytes()).hexdigest() for path in paths}}
with preparation.open('x', encoding='utf-8') as stream:
    json.dump(result, stream, ensure_ascii=False, indent=2)
print(json.dumps({'prepared': preparation.name}))
