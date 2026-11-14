import hashlib
import json
import re
from pathlib import Path
work=Path(__file__).resolve().parents[1]
root=work.parents[1]
frozen_script=work/'tools/prepare_native_material_ranged_signed.py'
proof_receipt=work/'reports/native-material-ranged-bow-signed-preparation-e43eb814.json'
proof=json.loads(proof_receipt.read_text(encoding='utf-8'))
assert hashlib.sha256(frozen_script.read_bytes()).hexdigest()==proof['inputHashes'][str(frozen_script)]
helper=work/'tools/prepare_native_frost_ingress_runtime.py';namespace={'__file__':str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0],str(helper),'exec'),namespace)
mutable=work/'reports/native-repair-ranged-use-candidate.json'
product=json.loads(mutable.read_text(encoding='utf-8'));tag=Path(product['attempt']).name.removeprefix('native-repair-')
for source in (work/'proposal-native').rglob('*.java'):
    assert source.read_bytes()==(Path(product['attempt'])/'sources'/source.relative_to(work/'proposal-native')).read_bytes(),source.name
binding=work/('reports/native-repair-ranged-use-candidate-'+tag+'.json')
if binding.exists():assert json.loads(binding.read_text(encoding='utf-8'))==product
else:namespace['add'](binding.relative_to(root).as_posix(),mutable.read_text(encoding='utf-8'))
text=frozen_script.read_text(encoding='utf-8')
anchor="tag='e43eb814'";assert text.count(anchor)==1;text=text.replace(anchor,'tag='+repr(tag))
anchor="base_preparation=work/('reports/native-material-ranged-paid-repair-qualified-preparation-'+tag+'.json')";assert text.count(anchor)==1
text=text.replace(anchor,"base_preparation=work/'reports/native-material-ranged-paid-repair-qualified-preparation-e43eb814.json'")
anchor="seed=work/('reports/native-repair-runtime-material-ranged-paid-repair-qualified-'+tag+'.json')";assert text.count(anchor)==1
text=text.replace(anchor,"seed=work/'reports/native-repair-runtime-material-ranged-paid-repair-qualified-e43eb814.json'")
anchor="launcher=old_launcher.read_text(encoding='utf-8')";assert text.count(anchor)==1
text=text.replace(anchor,anchor+"\nlauncher=launcher.replace('reports/native-repair-ranged-use-candidate-e43eb814.json',binding.relative_to(work).as_posix())\nlauncher,count=re.subn(r\"assert digest\\(binding\\) == '[0-9a-f]{64}'\",'assert digest(binding) == '+repr(hashlib.sha256(binding.read_bytes()).hexdigest()),launcher);assert count==1")
anchor="launcher=launcher.replace('native-repair-runtime-material-ranged-paid-repair-qualified-'+tag+'.json','native-repair-runtime-material-ranged-'+mode+'-signed-'+tag+'.json')";assert text.count(anchor)==1
text=text.replace(anchor,"launcher=launcher.replace('native-repair-runtime-material-ranged-paid-repair-qualified-e43eb814.json','native-repair-runtime-material-ranged-'+mode+'-signed-'+tag+'.json')")
anchor='paths=[Path(__file__),helper,';assert text.count(anchor)==1;text=text.replace(anchor,'paths=[Path(__file__),frozen_script,proof_receipt,binding,helper,')
namespace={'__file__':str(Path(__file__).resolve()),'frozen_script':frozen_script,'proof_receipt':proof_receipt,'binding':binding}
exec(compile(text,str(frozen_script),'exec'),namespace)
