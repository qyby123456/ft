import hashlib
import json
import re
from pathlib import Path
work=Path(__file__).resolve().parents[1];root=work.parents[1]
parent=work/'reports/native-material-ranged-crossbow-owner-capacity-preparation-41d445cf.json';frozen=json.loads(parent.read_text(encoding='utf-8'))
for name,wanted in frozen['inputHashes'].items():assert hashlib.sha256(Path(name).read_bytes()).hexdigest()==wanted,Path(name).name
helper=work/'tools/prepare_native_frost_ingress_runtime.py';namespace={'__file__':str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0],str(helper),'exec'),namespace)
old_source=Path(frozen['newObserver']);old_launcher=Path(frozen['newLauncher']);source=old_source.read_text(encoding='utf-8')
anchor='net.farmingtales.ftboss.item.NativeVanillaImpact.snapshot(arrows.get(0))';assert source.count(anchor)==1
source=source.replace(anchor,'arrows.get(0).getPersistentData().getCompound("FTBossWeaponMaterials")')
source,count=re.subn(r'report.put\("crossbowEntry", "[^"]*"\);','report.put("crossbowEntry", "Actual6 original charge/fire factories and shared owner capacity plus foreign public guard mutation-notice fanout; no actual impact/full signed Session executed");',source);assert count==1
new_source=work/'tests/material-ranged-crossbow-owner-capacity-qualified-41d445cf/PositiveWeapon436.java';namespace['add'](new_source.relative_to(root).as_posix(),source)
launcher=old_launcher.read_text(encoding='utf-8').replace(old_source.relative_to(work).as_posix(),new_source.relative_to(work).as_posix())
launcher=launcher.replace('native-repair-runtime-material-ranged-crossbow-owner-capacity-41d445cf.json','native-repair-runtime-material-ranged-crossbow-owner-capacity-qualified-41d445cf.json')
preparation=work/'reports/native-material-ranged-crossbow-owner-capacity-qualified-preparation-41d445cf.json';launcher=launcher.replace(parent.relative_to(work).as_posix(),preparation.relative_to(work).as_posix())
new_launcher=work/'tools/run_native_material_ranged_crossbow_owner_capacity_qualified_41d445cf.py';namespace['add'](new_launcher.relative_to(root).as_posix(),launcher)
compile_failure=work/'build/repair-runtime-6a45dd11/compile-terminal320.json';failed=json.loads(compile_failure.read_text(encoding='utf-8'));assert failed['exitCode']==1 and not failed['serverStarted']
paths=[Path(__file__),helper,parent,old_source,old_launcher,new_source,new_launcher,compile_failure,compile_failure.parent/'compile308.log']
with preparation.open('x',encoding='utf-8') as stream:json.dump({'newObserver':str(new_source),'newLauncher':str(new_launcher),'productChanged':False,'compileOnlyObserverCorrection':'Use actual existing FTBossWeaponMaterials carrier data, not nonexistent convenience API; inherited positive impact label corrected. No JVM started for excluded compile failure.','inputHashes':{str(path):hashlib.sha256(path.read_bytes()).hexdigest() for path in paths}},stream,ensure_ascii=False,indent=2)
print(json.dumps({'prepared':preparation.name,'sameProduct':'41d445cf','priorFailureServerStarted':False}))
