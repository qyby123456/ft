import hashlib
import json
from pathlib import Path


work = Path(__file__).resolve().parents[1]
origin = work / 'tools/prepare_native_trident_foundation_gate_runtime.py'
frozen = json.loads((work / 'reports/native-trident-foundation-gate-preparation-3413e22a.json').read_text(encoding='utf-8'))
assert hashlib.sha256(origin.read_bytes()).hexdigest() == frozen['inputHashes'][str(origin)]
text = origin.read_text(encoding='utf-8')
anchor = "namespace['add'](new_launcher.relative_to(root).as_posix(), launcher)"
assert text.count(anchor) == 1
insertion = '''
assert receipt.get('tridentProductionHooksConfigured')
resource_targets = receipt['requiredProductionResources']
resource_code = '\\nproduction_resources = load(binding)["requiredProductionResources"]\\nfor resource_name, frozen_name in production_resources.items():\\n    frozen_resource = Path(frozen_name)\\n    assert digest(frozen_resource) == load(binding)["inputHashes"][frozen_name]\\n    configuration = load(frozen_resource)\\n    configured = set()\\n    for current_configuration in proposal.glob("*.mixins.json"):\\n        configured.update(load(current_configuration).get("mixins", []))\\n    assert not configured.intersection(configuration["mixins"])\\n    shutil.copyfile(frozen_resource, proposal / resource_name)\\n    with (proposal / "META-INF/neoforge.mods.toml").open("a", encoding="utf-8") as stream:\\n        stream.write("\\\\n[[mixins]]\\\\nconfig = \\\\"" + resource_name + "\\\\"\\\\n")\\n'
launcher_anchor = "for directory in ['mods', 'recipe-inputs', 'kubejs']:"
assert launcher.count(launcher_anchor) == 1
launcher = launcher.replace(launcher_anchor, resource_code + '\\n' + launcher_anchor)
compile(launcher, str(new_launcher), 'exec')
'''
text = text.replace(anchor, insertion + '\n' + anchor)
text = text.replace('[Path(__file__), preparation, helper, binding, old_source, old_launcher, new_source, new_launcher]', '[Path(__file__), origin, preparation, helper, binding, old_source, old_launcher, new_source, new_launcher]')
exec(compile(text, str(__file__), 'exec'))
