import hashlib
import json
from pathlib import Path
work=Path(__file__).resolve().parents[1];root=work.parents[1]
parent=work/'reports/native-material-ranged-paid-repair-preparation-e43eb814.json'
frozen=json.loads(parent.read_text(encoding='utf-8'))
for name,wanted in frozen['inputHashes'].items():assert hashlib.sha256(Path(name).read_bytes()).hexdigest()==wanted,Path(name).name
helper=work/'tools/prepare_native_frost_ingress_runtime.py';namespace={'__file__':str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0],str(helper),'exec'),namespace)
old_source=Path(frozen['newObserver']);old_launcher=Path(frozen['newLauncher'])
source=old_source.read_text(encoding='utf-8')
start=source.index('                ItemStack unregistered =')
end=source.index('                runNativeRepair(event, repairedPlayer, actual, row);',start)
source=source[:start]+"""                ItemStack counted = actual.copyWithCount(2);
                boolean countDenied = !Boolean.TRUE.equals(policy.invoke(null, counted));
                boolean vanillaDenied = !Boolean.TRUE.equals(policy.invoke(null, new ItemStack(Items.BOW)));
                row.put("nonUnitCountMaterialPolicyDenied", countDenied);
                row.put("vanillaObjectNotMaterialPolicyGranted", vanillaDenied);
                if (!countDenied || !vanillaDenied) throw new AssertionError("Bounded material count/type policy drift");
"""+source[end:]
new_source=work/'tests/material-ranged-paid-repair-qualified-e43eb814/PositiveWeapon436.java'
namespace['add'](new_source.relative_to(root).as_posix(),source)
launcher=old_launcher.read_text(encoding='utf-8').replace(old_source.relative_to(work).as_posix(),new_source.relative_to(work).as_posix())
launcher=launcher.replace('native-repair-runtime-material-ranged-paid-repair-e43eb814.json','native-repair-runtime-material-ranged-paid-repair-qualified-e43eb814.json')
preparation=work/'reports/native-material-ranged-paid-repair-qualified-preparation-e43eb814.json'
launcher=launcher.replace(parent.relative_to(work).as_posix(),preparation.relative_to(work).as_posix())
new_launcher=work/'tools/run_native_material_ranged_paid_repair_qualified_e43eb814.py'
namespace['add'](new_launcher.relative_to(root).as_posix(),launcher)
excluded=work/'reports/native-repair-runtime-material-ranged-paid-repair-e43eb814.json'
paths=[Path(__file__),helper,parent,old_source,old_launcher,new_source,new_launcher,excluded]
with preparation.open('x',encoding='utf-8') as stream:json.dump({'newObserver':str(new_source),'newLauncher':str(new_launcher),'productChanged':False,'observerCorrection':'Remove illegal new Item construction after registry frozen; replace with existing registered-object count/type controls. Unregistered constructor path not accepted.','inputHashes':{str(path):hashlib.sha256(path.read_bytes()).hexdigest() for path in paths}},stream,ensure_ascii=False,indent=2)
print(json.dumps({'prepared':preparation.name,'sameProduct':'e43eb814','constructorFailureExcluded':True}))
