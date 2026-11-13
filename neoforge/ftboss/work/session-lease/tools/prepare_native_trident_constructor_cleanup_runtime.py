import ast
import hashlib
import json
from pathlib import Path

work = Path(__file__).resolve().parents[1]
lineage_origin = work / 'tools/prepare_native_trident_authoritative_build_runtime.py'
prior = json.loads((work / 'reports/native-trident-foundation-gate-preparation-6a38a668.json').read_text(encoding='utf-8'))
assert hashlib.sha256(lineage_origin.read_bytes()).hexdigest() == prior['inputHashes'][str(lineage_origin)]
namespace = {'__file__': str(lineage_origin)}
original = lineage_origin.read_text(encoding='utf-8')
exec(compile(original.split("exec(compile(text, str(__file__), 'exec'), namespace)")[0], str(lineage_origin), 'exec'), namespace)
text = namespace['text']
observation_origin = work / 'tools/prepare_native_trident_early_observation_6a38a668.py'
observation_receipt = json.loads((work / 'reports/native-trident-early-observation-preparation-6a38a668.json').read_text(encoding='utf-8'))
assert hashlib.sha256(observation_origin.read_bytes()).hexdigest() == observation_receipt['inputHashes'][str(observation_origin)]
assignments = [node for node in ast.walk(ast.parse(observation_origin.read_text(encoding='utf-8'))) if isinstance(node, ast.Assign) and any(isinstance(target, ast.Name) and target.id == 'replacement' for target in node.targets)]
assert len(assignments) == 1
replacement = ast.literal_eval(assignments[0].value)
old_assert = '                if (!earlyUnchanged) throw new AssertionError("Original early Trident release changed paid authority or image");'
extra = 'assert source.count(' + repr(old_assert) + ') == 1\n'
extra += 'source = source.replace(' + repr(old_assert) + ', ' + repr(replacement) + ')\n'
extra += 'source = source.replace("actualEarlyReleasePreservesInputImageAndAuthority", "actualEarlyReleasePreservesExactInputIdentity")\n'
anchor = "namespace['add'](new_source.relative_to(root).as_posix(), source)"
assert text.count(anchor) == 1
text = text.replace(anchor, extra + anchor)
text = text.replace('[Path(__file__), derived_origin, origin, preparation, helper, binding, old_source, old_launcher, new_source, new_launcher]', '[Path(__file__), lineage_origin, observation_origin, derived_origin, origin, preparation, helper, binding, old_source, old_launcher, new_source, new_launcher]')
namespace = namespace['namespace']
namespace.update({'__file__': str(Path(__file__).resolve()), 'lineage_origin': lineage_origin, 'observation_origin': observation_origin})
exec(compile(text, str(__file__), 'exec'), namespace)
