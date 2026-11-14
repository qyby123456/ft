import hashlib
import json
from pathlib import Path
work=Path(__file__).resolve().parents[1];root=work.parents[1]
product=json.loads((work/'reports/native-repair-ranged-use-candidate.json').read_text(encoding='utf-8'));tag=Path(product['attempt']).name.removeprefix('native-repair-')
parent=work/('reports/native-material-ranged-crossbow-signed-preparation-'+tag+'.json');frozen=json.loads(parent.read_text(encoding='utf-8'))
for name,wanted in frozen['inputHashes'].items():assert hashlib.sha256(Path(name).read_bytes()).hexdigest()==wanted,Path(name).name
helper=work/'tools/prepare_native_frost_ingress_runtime.py';namespace={'__file__':str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0],str(helper),'exec'),namespace)
old_source=Path(frozen['newObserver']);old_launcher=Path(frozen['newLauncher']);source=old_source.read_text(encoding='utf-8')
anchor='for (int sample : new int[]{6, 7, 8, 9, 10, 11})';assert source.count(anchor)==1;source=source.replace(anchor,'for (int sample : new int[]{10})')
anchor='                row.put("generationRetiredAfterFire", read(generation, "retired"));';assert source.count(anchor)==1
source=source.replace(anchor,anchor+'\n                if (Boolean.TRUE.equals(read(generation, "retired"))) throw new AssertionError("Diagnostic original Diamond Crossbow fire retired genuine generation before impact");')
new_source=work/('tests/material-ranged-crossbow-diamond-diagnostic-'+tag+'/PositiveWeapon436.java');namespace['add'](new_source.relative_to(root).as_posix(),source)
launcher=old_launcher.read_text(encoding='utf-8').replace(old_source.relative_to(work).as_posix(),new_source.relative_to(work).as_posix())
launcher=launcher.replace('native-repair-runtime-material-ranged-crossbow-signed-'+tag+'.json','native-repair-runtime-material-ranged-crossbow-diamond-diagnostic-'+tag+'.json')
preparation=work/('reports/native-material-ranged-crossbow-diamond-diagnostic-preparation-'+tag+'.json');launcher=launcher.replace(parent.relative_to(work).as_posix(),preparation.relative_to(work).as_posix())
new_launcher=work/('tools/run_native_material_ranged_crossbow_diamond_diagnostic_'+tag+'.py');namespace['add'](new_launcher.relative_to(root).as_posix(),launcher)
paths=[Path(__file__),helper,parent,old_source,old_launcher,new_source,new_launcher]
with preparation.open('x',encoding='utf-8') as stream:json.dump({'newObserver':str(new_source),'newLauncher':str(new_launcher),'actualDiagnosticCases':1,'priorFourNotRepeated':True,'authorityFieldsWritten':False,'inputHashes':{str(path):hashlib.sha256(path.read_bytes()).hexdigest() for path in paths}},stream,ensure_ascii=False,indent=2)
print(json.dumps({'prepared':preparation.name,'actualCases':1,'diagnosticOnly':True}))
