import hashlib
import json
from pathlib import Path
work=Path(__file__).resolve().parents[1];root=work.parents[1]
def digest(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def load(path):return json.loads(path.read_text(encoding='utf-8'))
parent=work/'reports/native-exact-melee-variants-paid-repair-qualified-preparation-41d445cf.json';frozen=load(parent)
for name,wanted in frozen['inputHashes'].items():assert digest(Path(name))==wanted,Path(name).name
excluded=work/'reports/native-repair-runtime-exact-melee-variants-paid-repair-qualified-41d445cf.json';failed=load(excluded);terminal=Path(failed['attempt'])/'terminal308.json';end=load(terminal)
assert failed['probe']['failures']==1 and failed['exitCode']==0 and end['stopSent'] and end['completeSave']
assert len(failed['probe']['cases'])==14 and all(row['actualSuccessorOriginalSaveCommitted'] for row in failed['probe']['cases'][:13])
assert failed['probe']['cases'][-1]['id']=='ftboss:defect_claw' and failed['probe']['cases'][-1]['repairProfileBeforeStart']=='defect'
old_source=Path(frozen['newObserver']);old_launcher=Path(frozen['newLauncher']);source=old_source.read_text(encoding='utf-8')
anchor='candidate.ownTint() == 2';assert source.count(anchor)==1
source=source.replace(anchor,'net.farmingtales.ftboss.item.FoodWeaponEvolution.profileId(candidate.ownTint()).equals(net.farmingtales.ftboss.item.FoodWeaponLifecycle.repairProfile(weapon))')
anchor='        if (statue == null) throw new AssertionError("Native repair statue missing");';assert source.count(anchor)==1
source=source.replace(anchor,anchor+"""
        int originalTint = statue.ownTint();
        var originalView = net.farmingtales.ftboss.item.FoodWeaponRepair.view(weapon, originalTint);
        if (!originalView.eligible() || !originalView.correctStatue()) throw new AssertionError("Original registered repair profile/statue mismatch");
        row.put("originalRegisteredProfileMatchedStatueTint", originalTint);
        row.put("originalRegisteredProfileStatueAndFoodRulesPreserved", true);""")
source=source.replace('FoodWeaponRepair.view(weapon, 2)','FoodWeaponRepair.view(weapon, originalTint)').replace('FoodWeaponEvolution.matchesStatueFood(new ItemStack(item), 2)','FoodWeaponEvolution.matchesStatueFood(new ItemStack(item), originalTint)')
helper=work/'tools/prepare_native_frost_ingress_runtime.py';ns={'__file__':str(helper)};exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0],str(helper),'exec'),ns)
new_source=work/'tests/exact-melee-variants-paid-repair-profile-qualified-41d445cf/PositiveWeapon436.java';ns['add'](new_source.relative_to(root).as_posix(),source)
launcher=old_launcher.read_text(encoding='utf-8').replace(old_source.relative_to(work).as_posix(),new_source.relative_to(work).as_posix())
launcher=launcher.replace('native-repair-runtime-exact-melee-variants-paid-repair-qualified-41d445cf.json','native-repair-runtime-exact-melee-variants-paid-repair-profile-qualified-41d445cf.json')
preparation=work/'reports/native-exact-melee-variants-paid-repair-profile-qualified-preparation-41d445cf.json';launcher=launcher.replace(parent.relative_to(work).as_posix(),preparation.relative_to(work).as_posix())
new_launcher=work/'tools/run_native_exact_melee_variants_paid_repair_profile_qualified_41d445cf.py';ns['add'](new_launcher.relative_to(root).as_posix(),launcher)
paths=[Path(__file__),parent,excluded,terminal,old_source,old_launcher,helper,new_source,new_launcher]
with preparation.open('x',encoding='utf-8') as stream:json.dump({'ids':frozen['ids'],'newObserver':str(new_source),'newLauncher':str(new_launcher),'observerCorrection':'Preserve existing original repairProfile and matching original statue/food rules. Do not force every original weapon through butcher tint2; previous wrong-statue refusal preserved. Original claim UUID correction retained.','productChanged':False,'authorityFieldsWritten':False,'inputHashes':{str(path):digest(path) for path in paths}},stream,ensure_ascii=False,indent=2)
print(json.dumps({'prepared':preparation.name,'cases':19,'originalProfileStatueFoodPreserved':True}))
