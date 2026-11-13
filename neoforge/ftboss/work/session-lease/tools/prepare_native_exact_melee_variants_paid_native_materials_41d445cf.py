import hashlib
import json
from pathlib import Path
work=Path(__file__).resolve().parents[1];root=work.parents[1]
def digest(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def load(path):return json.loads(path.read_text(encoding='utf-8'))
parent=work/'reports/native-exact-melee-variants-paid-repair-pair-qualified-preparation-41d445cf.json';frozen=load(parent)
for name,wanted in frozen['inputHashes'].items():assert digest(Path(name))==wanted,Path(name).name
binding=work/'reports/native-repair-ranged-use-candidate-41d445cf.json';product=load(binding);build=Path(product['attempt'])
for source in (work/'proposal-native').rglob('*.java'):assert source.read_bytes()==(build/'sources'/source.relative_to(work/'proposal-native')).read_bytes(),source.name
old_source=Path(frozen['newObserver']);old_launcher=Path(frozen['newLauncher']);source=old_source.read_text(encoding='utf-8')
anchor='                original.getInventory().setItem(0, weapon);';assert source.count(anchor)==1
source=source.replace(anchor,"""                if (net.farmingtales.ftboss.item.SwordCavityBuild.applies(weapon)) {
                    row.put("originalCavityMissingBeforeReferenceCapture", net.farmingtales.ftboss.item.SwordCavityBuild.missingRequired(weapon));
                    net.farmingtales.ftboss.item.SwordNativeMaterials.capture(weapon, server.overworld());
                    row.put("originalRecipeReferenceCaptureBeforeAnyRepairAuthority", true);
                    row.put("originalCavityStatusAfterReferenceCapture", net.farmingtales.ftboss.item.SwordCavityBuild.functionStatus(weapon).toString());
                    row.put("originalCavityMissingAfterReferenceCapture", net.farmingtales.ftboss.item.SwordCavityBuild.missingRequired(weapon));
                    if (net.farmingtales.ftboss.item.SwordCavityBuild.missingRequired(weapon))
                        throw new AssertionError("Original registered recipe/reference layout is not yet combat-ready: " + id + " / " + net.farmingtales.ftboss.item.SwordCavityBuild.functionStatus(weapon));
                }
                original.getInventory().setItem(0, weapon);""")
anchor='                weapon.setDamageValue(10);';assert source.count(anchor)==1
source=source.replace(anchor,"""                if (net.farmingtales.ftboss.item.FoodWeaponLifecycle.isBroken(weapon)) throw new AssertionError("Original pre-authority configured weapon is still broken: " + id);
                row.put("originalConfiguredWeaponNotBrokenBeforeAnyPaidAuthority", true);
                weapon.setDamageValue(10);""")
helper=work/'tools/prepare_native_frost_ingress_runtime.py';ns={'__file__':str(helper)};exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0],str(helper),'exec'),ns)
new_source=work/'tests/exact-melee-variants-paid-repair-native-materials-qualified-41d445cf/PositiveWeapon436.java';ns['add'](new_source.relative_to(root).as_posix(),source)
launcher=old_launcher.read_text(encoding='utf-8').replace(old_source.relative_to(work).as_posix(),new_source.relative_to(work).as_posix())
launcher=launcher.replace('native-repair-runtime-exact-melee-variants-paid-repair-pair-qualified-41d445cf.json','native-repair-runtime-exact-melee-variants-paid-repair-native-materials-qualified-41d445cf.json')
preparation=work/'reports/native-exact-melee-variants-paid-repair-native-materials-qualified-preparation-41d445cf.json';launcher=launcher.replace(parent.relative_to(work).as_posix(),preparation.relative_to(work).as_posix())
new_launcher=work/'tools/run_native_exact_melee_variants_paid_repair_native_materials_qualified_41d445cf.py';ns['add'](new_launcher.relative_to(root).as_posix(),launcher)
paths=[Path(__file__),parent,binding,old_source,old_launcher,helper,new_source,new_launcher]
with preparation.open('x',encoding='utf-8') as stream:json.dump({'ids':frozen['ids'],'newObserver':str(new_source),'newLauncher':str(new_launcher),'fixtureChange':'Call original SwordNativeMaterials.capture on fresh original registered stacks before any paid authority, keep original cavity readiness and broken refusals. Never fill fabricated parts or reset retired. New configured image requires real new paid repair; previous unconfigured19 paid evidence remains unchanged.','productChanged':False,'authorityFieldsWritten':False,'inputHashes':{str(path):digest(path) for path in paths}},stream,ensure_ascii=False,indent=2)
print(json.dumps({'prepared':preparation.name,'cases':19,'newConfiguredPhysicalInputRequiresActualPayment':True,'productUnchanged41':True}))
