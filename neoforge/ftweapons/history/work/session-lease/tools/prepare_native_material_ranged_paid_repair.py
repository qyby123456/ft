import hashlib
import json
import re
from pathlib import Path
work=Path(__file__).resolve().parents[1]
root=work.parents[1]
helper=work/'tools/prepare_native_frost_ingress_runtime.py'
namespace={'__file__':str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0],str(helper),'exec'),namespace)
mutable=work/'reports/native-repair-ranged-use-candidate.json'
product=json.loads(mutable.read_text(encoding='utf-8'))
build=Path(product['attempt']);tag=build.name.removeprefix('native-repair-')
for source in (work/'proposal-native').rglob('*.java'):
    assert source.read_bytes()==(build/'sources'/source.relative_to(work/'proposal-native')).read_bytes(),source.name
binding=work/('reports/native-repair-ranged-use-candidate-'+tag+'.json')
if binding.exists(): assert json.loads(binding.read_text(encoding='utf-8'))==product
else: namespace['add'](binding.relative_to(root).as_posix(),mutable.read_text(encoding='utf-8'))
parent=work/'reports/native-material-ranged-paid-repair-baseline-preparation-99c65f41.json'
frozen=json.loads(parent.read_text(encoding='utf-8'))
for name,wanted in frozen['inputHashes'].items():assert hashlib.sha256(Path(name).read_bytes()).hexdigest()==wanted,Path(name).name
old_source=Path(frozen['newObserver']);old_launcher=Path(frozen['newLauncher'])
source=old_source.read_text(encoding='utf-8')
anchor='                runNativeRepair(event, repairedPlayer, actual, row);'
assert source.count(anchor)==1
source=source.replace(anchor,"""                Class<?> materialPolicy = Class.forName("net.farmingtales.ftboss.item.OwnerRepairGenerationAuthority");
                var policy = materialPolicy.getDeclaredMethod("nativeMaterialRangedPolicy", ItemStack.class); policy.setAccessible(true);
                boolean bounded = Boolean.TRUE.equals(policy.invoke(null, actual));
                row.put("boundRegisteredFinalMaterialClassAccepted", bounded);
                if (!bounded) throw new AssertionError("Bounded registered material policy rejected " + id);
                ItemStack unregistered = actual.getItem() instanceof net.farmingtales.ftboss.item.weapons.MaterialBowItem material
                    ? new ItemStack(new net.farmingtales.ftboss.item.weapons.MaterialBowItem(material.kind()))
                    : new ItemStack(new net.farmingtales.ftboss.item.weapons.MaterialCrossbowItem(net.minecraft.world.item.Tiers.WOOD, new net.minecraft.world.item.Item.Properties()));
                boolean unregisteredDenied = !Boolean.TRUE.equals(policy.invoke(null, unregistered));
                row.put("unregisteredSameFinalImplementationDenied", unregisteredDenied);
                if (!unregisteredDenied) throw new AssertionError("Unregistered material object unexpectedly admitted");
                runNativeRepair(event, repairedPlayer, actual, row);""")
new_source=work/('tests/material-ranged-paid-repair-'+tag+'/PositiveWeapon436.java')
namespace['add'](new_source.relative_to(root).as_posix(),source)
launcher=old_launcher.read_text(encoding='utf-8')
launcher=launcher.replace('reports/native-repair-ranged-use-candidate-99c65f41.json',binding.relative_to(work).as_posix())
launcher,count=re.subn(r"assert digest\(binding\) == '[0-9a-f]{64}'",'assert digest(binding) == '+repr(hashlib.sha256(binding.read_bytes()).hexdigest()),launcher);assert count==1
launcher=launcher.replace(old_source.relative_to(work).as_posix(),new_source.relative_to(work).as_posix())
launcher=launcher.replace('native-repair-runtime-material-ranged-paid-repair-baseline-99c65f41.json','native-repair-runtime-material-ranged-paid-repair-'+tag+'.json')
preparation=work/('reports/native-material-ranged-paid-repair-preparation-'+tag+'.json')
launcher=launcher.replace(parent.relative_to(work).as_posix(),preparation.relative_to(work).as_posix())
new_launcher=work/('tools/run_native_material_ranged_paid_repair_'+tag+'.py')
namespace['add'](new_launcher.relative_to(root).as_posix(),launcher)
paths=[Path(__file__),helper,parent,old_source,old_launcher,new_source,new_launcher,binding]
with preparation.open('x',encoding='utf-8') as stream:json.dump({'candidate':str(binding),'tag':tag,'newObserver':str(new_source),'newLauncher':str(new_launcher),'authorityFieldsWritten':False,'inputHashes':{str(path):hashlib.sha256(path.read_bytes()).hexdigest() for path in paths}},stream,ensure_ascii=False,indent=2)
print(json.dumps({'prepared':preparation.name,'tag':tag,'paidCases':12,'unregisteredControls':12}))
