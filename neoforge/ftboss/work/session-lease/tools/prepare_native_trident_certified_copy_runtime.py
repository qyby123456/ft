import hashlib
import json
from pathlib import Path


work = Path(__file__).resolve().parents[1]
origin = work / 'tools/prepare_native_trident_foundation_gate_runtime.py'
frozen = json.loads((work / 'reports/native-trident-foundation-gate-preparation-3413e22a.json').read_text(encoding='utf-8'))
assert hashlib.sha256(origin.read_bytes()).hexdigest() == frozen['inputHashes'][str(origin)]
text = origin.read_text(encoding='utf-8')
source_hook = '''
certificate_anchor = '                row.put("brokenBeforeOriginalUse", net.farmingtales.ftboss.item.FoodWeaponLifecycle.isBroken(actual));'
assert source.count(certificate_anchor) == 1
source = source.replace(certificate_anchor, ''' + repr('''                Class<?> tridentCoverageType = Class.forName("net.farmingtales.ftboss.item.OwnerFieldCoverageRegistry");
                var tridentCertificate = tridentCoverageType.getDeclaredMethod("nativeTridentCertified"); tridentCertificate.setAccessible(true);
                row.put("actualTridentWholeInstallationCertified", tridentCertificate.invoke(null));
''') + ''' + certificate_anchor)
'''
anchor = "namespace['add'](new_source.relative_to(root).as_posix(), source)"
assert text.count(anchor) == 1
text = text.replace(anchor, source_hook + '\n' + anchor)
resource_code = '''
production_resources = load(binding)["requiredProductionResources"]
for resource_name, frozen_name in production_resources.items():
    frozen_resource = Path(frozen_name)
    assert digest(frozen_resource) == load(binding)["inputHashes"][frozen_name]
    configuration = load(frozen_resource)
    configured = set()
    for current_configuration in proposal.glob("*.mixins.json"):
        configured.update(load(current_configuration).get("mixins", []))
    assert not configured.intersection(configuration["mixins"])
    shutil.copyfile(frozen_resource, proposal / resource_name)
    with (proposal / "META-INF/neoforge.mods.toml").open("a", encoding="utf-8") as stream:
        stream.write("\\n[[mixins]]\\nconfig = \\"" + resource_name + "\\"\\n")
'''
compile(resource_code, 'production-resource-code', 'exec')
resource_inputs = '''
inputs.update({str(proposal / name): digest(proposal / name) for name in production_resources})
inputs.update({name: digest(Path(name)) for name in production_resources.values()})
'''
launcher_hook = '''
assert receipt.get('tridentProductionHooksConfigured')
launcher_anchor = "for directory in ['mods', 'recipe-inputs', 'kubejs']:"
assert launcher.count(launcher_anchor) == 1
launcher = launcher.replace(launcher_anchor, ''' + repr(resource_code) + ''' + '\\n' + launcher_anchor)
input_anchor = "save(attempt / 'inputs-before308.json', inputs)"
assert launcher.count(input_anchor) >= 1
launcher_prefix, launcher_separator, launcher_suffix = launcher.rpartition(input_anchor)
launcher = launcher_prefix + ''' + repr(resource_inputs) + ''' + '\\n' + launcher_separator + launcher_suffix
compile(launcher, str(new_launcher), 'exec')
'''
anchor = "namespace['add'](new_launcher.relative_to(root).as_posix(), launcher)"
assert text.count(anchor) == 1
text = text.replace(anchor, launcher_hook + '\n' + anchor)
text = text.replace('[Path(__file__), preparation, helper, binding, old_source, old_launcher, new_source, new_launcher]', '[Path(__file__), origin, preparation, helper, binding, old_source, old_launcher, new_source, new_launcher]')
exec(compile(text, str(__file__), 'exec'))
