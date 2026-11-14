import hashlib
import json
from pathlib import Path
work=Path(__file__).resolve().parents[1];root=work.parents[1]
def digest(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def load(path):return json.loads(path.read_text(encoding='utf-8'))
parent=work/'reports/native-exact-melee-variants-signed-preparation-41d445cf.json';frozen=load(parent)
for name,wanted in frozen['inputHashes'].items():assert digest(Path(name))==wanted,Path(name).name
paid=work/'reports/native-repair-runtime-exact-melee-variants-paid-repair-pair-qualified-41d445cf.json';result=load(paid);terminal=Path(result['attempt'])/'terminal308.json';end=load(terminal)
assert result['exitCode']==result['probe']['failures']==0 and len(result['probe']['cases'])==19 and end['completeSave'] and end['stopSent']
excluded=work/'build/repair-runtime-7ee071c3';assert excluded.is_dir() and not (excluded/'live-process308.json').exists() and not (excluded/'compile-command308.json').exists()
old_launcher=Path(frozen['newLauncher']);launcher=old_launcher.read_text(encoding='utf-8');anchor='reports/native-repair-runtime-material-ranged-paid-repair-qualified-e43eb814.json';assert launcher.count(anchor)==1
launcher=launcher.replace(anchor,paid.relative_to(work).as_posix())
launcher=launcher.replace('native-repair-runtime-exact-melee-variants-signed-41d445cf.json','native-repair-runtime-exact-melee-variants-signed-qualified-41d445cf.json')
preparation=work/'reports/native-exact-melee-variants-signed-qualified-preparation-41d445cf.json';launcher=launcher.replace(parent.relative_to(work).as_posix(),preparation.relative_to(work).as_posix())
helper=work/'tools/prepare_native_frost_ingress_runtime.py';ns={'__file__':str(helper)};exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0],str(helper),'exec'),ns)
new_launcher=work/'tools/run_native_exact_melee_variants_signed_qualified_41d445cf.py';ns['add'](new_launcher.relative_to(root).as_posix(),launcher)
paths=[Path(__file__),parent,paid,terminal,old_launcher,Path(frozen['newObserver']),helper,new_launcher]
with preparation.open('x',encoding='utf-8') as stream:json.dump({'ids':frozen['ids'],'newObserver':frozen['newObserver'],'newLauncher':str(new_launcher),'excludedPreflight':str(excluded),'excludedServerStarted':False,'launcherCorrection':'Replace exact real material12 seed with actual exact19 paid seed; prior failed before compile/JVM on strict19 count assertion, not accepted. No observer/product change.','productChanged':False,'authorityFieldsWritten':False,'inputHashes':{str(path):digest(path) for path in paths}},stream,ensure_ascii=False,indent=2)
print(json.dumps({'prepared':preparation.name,'actualPaidSeedCases':19,'priorWrongSeedPreflightExcludedNoJvm':True}))
