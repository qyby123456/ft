import hashlib
import json
import re
import sys
from pathlib import Path
work=Path(__file__).resolve().parents[1];root=work.parents[1]
def digest(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def load(path):return json.loads(path.read_text(encoding='utf-8'))
mode=sys.argv[1];assert mode in ['diagnostic','signed','controls']
base='exact-melee-wooden-sword-diagnostic' if mode=='diagnostic' else 'exact-melee-variants-'+('signed-qualified' if mode=='signed' else 'controls')
parent=work/('reports/native-'+base+'-preparation-41d445cf.json');frozen=load(parent)
for name,wanted in frozen['inputHashes'].items():assert digest(Path(name))==wanted,Path(name).name
mutable=work/'reports/native-repair-ranged-use-candidate.json';product=load(mutable);build=Path(product['attempt']);tag=build.name.removeprefix('native-repair-')
for source in (work/'proposal-native').rglob('*.java'):assert source.read_bytes()==(build/'sources'/source.relative_to(work/'proposal-native')).read_bytes(),source.name
helper=work/'tools/prepare_native_frost_ingress_runtime.py';ns={'__file__':str(helper)};exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0],str(helper),'exec'),ns)
binding=work/('reports/native-repair-ranged-use-candidate-'+tag+'.json')
if binding.exists():assert load(binding)==product
else:ns['add'](binding.relative_to(root).as_posix(),mutable.read_text(encoding='utf-8'))
old_launcher=Path(frozen['newLauncher']);launcher=old_launcher.read_text(encoding='utf-8')
anchor='reports/native-repair-ranged-use-candidate-41d445cf.json';assert launcher.count(anchor)==1;launcher=launcher.replace(anchor,binding.relative_to(work).as_posix())
launcher,count=re.subn(r"assert digest\(binding\) == '[0-9a-f]{64}'",'assert digest(binding) == '+repr(digest(binding)),launcher);assert count==1
old_output='native-repair-runtime-'+base+'-41d445cf.json';assert launcher.count(old_output)==1
launcher=launcher.replace(old_output,'native-repair-runtime-'+base+'-'+tag+'.json')
preparation=work/('reports/native-'+base+'-preparation-'+tag+'.json');launcher=launcher.replace(parent.relative_to(work).as_posix(),preparation.relative_to(work).as_posix())
new_launcher=work/('tools/run_native_'+base.replace('-','_')+'_'+tag+'.py');ns['add'](new_launcher.relative_to(root).as_posix(),launcher)
paths=[Path(__file__),parent,binding,old_launcher,Path(frozen['newObserver']),helper,new_launcher]
with preparation.open('x',encoding='utf-8') as stream:json.dump({'ids':frozen.get('ids'),'newObserver':frozen['newObserver'],'newLauncher':str(new_launcher),'candidate':str(binding),'tag':tag,'mode':mode,'sameRealNineteenPaidSeedPreserved':True,'diagnosticOnly':mode=='diagnostic','authorityFieldsWritten':False,'inputHashes':{str(path):digest(path) for path in paths}},stream,ensure_ascii=False,indent=2)
print(json.dumps({'prepared':preparation.name,'mode':mode,'tag':tag}))
