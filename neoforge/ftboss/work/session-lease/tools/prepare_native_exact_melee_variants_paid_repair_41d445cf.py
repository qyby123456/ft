import hashlib
import json
import re
from pathlib import Path
work=Path(__file__).resolve().parents[1];root=work.parents[1]
def digest(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def load(path):return json.loads(path.read_text(encoding='utf-8'))
audit=work/'reports/native-remaining-variant-gap-audit-3320e085.json'
rows=load(audit)['groups']['otherExactOriginalClassesStillNeedActualMatrix'];assert len(rows)==19
assert all(row['class'] in ['net.minecraft.world.item.SwordItem','net.minecraft.world.item.AxeItem'] and row['maxDamage']>0 for row in rows)
parent=work/'reports/native-material-ranged-paid-repair-qualified-preparation-e43eb814.json';frozen=load(parent)
for name,wanted in frozen['inputHashes'].items():assert digest(Path(name))==wanted,Path(name).name
origin=work/'reports/native-repair-runtime-six-family-paid-repair-local-channels.json';original=load(origin)
old_source=work/'tests/six-family-paid-repair-local-channels/PositiveWeapon436.java';assert digest(old_source)==original['inputHashes'][str(old_source)]
old_launcher=Path(frozen['newLauncher']);assert digest(old_launcher)==frozen['inputHashes'][str(old_launcher)]
binding=work/'reports/native-repair-ranged-use-candidate-41d445cf.json';product=load(binding);build=Path(product['attempt'])
for source in (work/'proposal-native').rglob('*.java'):assert source.read_bytes()==(build/'sources'/source.relative_to(work/'proposal-native')).read_bytes(),source.name
helper=work/'tools/prepare_native_frost_ingress_runtime.py';ns={'__file__':str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0],str(helper),'exec'),ns)
source=old_source.read_text(encoding='utf-8')
anchor='List<String> ids = List.of("minecraft:iron_sword", "minecraft:bow", "minecraft:crossbow", "minecraft:iron_axe", "minecraft:trident", "ftboss:frost_queen_staff");';assert source.count(anchor)==1
source=source.replace(anchor,'List<String> ids = List.of('+', '.join(json.dumps(row['id']) for row in rows)+');')
source=source.replace('new UUID(620, sample + 1)','new UUID(690, sample + 1)').replace('"ft_six_" + sample','"ft_exact_" + sample')
source=source.replace('sixNativeDamageableFamilyRepresentativesPaidRepairAndSameProcessReload','nineteenExactOriginalMeleeVariantsPaidRepairAndSameProcessReload')
anchor='                runNativeRepair(event, repairedPlayer, actual, row);';assert source.count(anchor)==1
classes=', '.join(json.dumps(row['class']) for row in rows);maximums=', '.join(str(row['maxDamage']) for row in rows)
replacement="""                String expectedClass = List.of(CLASS_VALUES).get(sample);
                int expectedMaximum = new int[]{MAXIMUM_VALUES}[sample];
                if (!actual.getItem().getClass().getName().equals(expectedClass) || actual.getMaxDamage() != expectedMaximum
                    || net.minecraft.core.registries.BuiltInRegistries.ITEM.get(net.minecraft.resources.ResourceLocation.parse(id)) != actual.getItem())
                    throw new AssertionError("Original exact registered implementation/durability drift for " + id);
                row.put("originalExactRegisteredClassAndDurabilityPreserved", true);
                row.put("originalMaximumDamage", expectedMaximum);
                runNativeRepair(event, repairedPlayer, actual, row);"""
source=source.replace(anchor,replacement.replace('CLASS_VALUES',classes).replace('MAXIMUM_VALUES',maximums))
new_source=work/'tests/exact-melee-variants-paid-repair-41d445cf/PositiveWeapon436.java';ns['add'](new_source.relative_to(root).as_posix(),source)
launcher=old_launcher.read_text(encoding='utf-8').replace('reports/native-repair-ranged-use-candidate-e43eb814.json',binding.relative_to(work).as_posix())
launcher,count=re.subn(r"assert digest\(binding\) == '[0-9a-f]{64}'",'assert digest(binding) == '+repr(digest(binding)),launcher);assert count==1
launcher=launcher.replace(Path(frozen['newObserver']).relative_to(work).as_posix(),new_source.relative_to(work).as_posix())
launcher=launcher.replace('native-repair-runtime-material-ranged-paid-repair-qualified-e43eb814.json','native-repair-runtime-exact-melee-variants-paid-repair-41d445cf.json')
preparation=work/'reports/native-exact-melee-variants-paid-repair-preparation-41d445cf.json'
launcher=launcher.replace(parent.relative_to(work).as_posix(),preparation.relative_to(work).as_posix())
new_launcher=work/'tools/run_native_exact_melee_variants_paid_repair_41d445cf.py';ns['add'](new_launcher.relative_to(root).as_posix(),launcher)
paths=[Path(__file__),audit,parent,origin,old_source,old_launcher,binding,helper,new_source,new_launcher]
with preparation.open('x',encoding='utf-8') as stream:json.dump({'ids':[row['id'] for row in rows],'newObserver':str(new_source),'newLauncher':str(new_launcher),'sameProduct':'41d445cf','noProductOrGameplayValueChanges':True,'authorityFieldsWritten':False,'actualSignedCombatAccepted':False,'authenticatedMultiplayerAccepted':False,'installable':False,'inputHashes':{str(path):digest(path) for path in paths}},stream,ensure_ascii=False,indent=2)
print(json.dumps({'prepared':preparation.name,'actualPaidRepairCases':19,'generation':'0->1','signedCombatStillPending':True}))
