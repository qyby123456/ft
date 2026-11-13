import hashlib
import json
from pathlib import Path
work=Path(__file__).resolve().parents[1];root=work.parents[1]
def digest(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def load(path):return json.loads(path.read_text(encoding='utf-8'))
parent=work/'reports/native-exact-melee-variants-paid-repair-preparation-41d445cf.json';frozen=load(parent)
for name,wanted in frozen['inputHashes'].items():assert digest(Path(name))==wanted,Path(name).name
excluded=work/'reports/native-repair-runtime-exact-melee-variants-paid-repair-41d445cf.json';failed=load(excluded);end=load(Path(failed['attempt'])/'terminal308.json')
assert failed['probe']['failures']==1 and failed['exitCode']==0 and end['stopSent'] and end['completeSave']
old_source=Path(frozen['newObserver']);old_launcher=Path(frozen['newLauncher']);source=old_source.read_text(encoding='utf-8')
anchor='                execute.invoke(null, player, legacy);';assert source.count(anchor)==1
source=source.replace(anchor,"""                var priorQuest = net.farmingtales.ftboss.item.FoodWeaponData.read(weapon, "FTBossWeaponLifecycle").getCompound("RepairQuest");
                UUID priorTask = priorQuest.hasUUID("NativeTask") ? priorQuest.getUUID("NativeTask") : null;
                execute.invoke(null, player, legacy);""")
anchor='                UUID task = quest.getUUID("NativeTask");';assert source.count(anchor)==1
source=source.replace(anchor,"""                UUID task;
                if (quest.hasUUID("NativeTask")) task = quest.getUUID("NativeTask");
                else if (action == 4 && priorTask != null) task = priorTask;
                else throw new AssertionError("Original repair task identity absent before terminal claim for " + operation);
                if (action == 4 && (priorTask == null || !priorTask.equals(task))) throw new AssertionError("Original CLAIM task identity drift");
                if (action == 4) row.put("originalPreClaimTaskIdentityUsedForSignedTerminalCheck", true);""")
helper=work/'tools/prepare_native_frost_ingress_runtime.py';ns={'__file__':str(helper)};exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0],str(helper),'exec'),ns)
new_source=work/'tests/exact-melee-variants-paid-repair-qualified-41d445cf/PositiveWeapon436.java';ns['add'](new_source.relative_to(root).as_posix(),source)
launcher=old_launcher.read_text(encoding='utf-8').replace(old_source.relative_to(work).as_posix(),new_source.relative_to(work).as_posix())
launcher=launcher.replace('native-repair-runtime-exact-melee-variants-paid-repair-41d445cf.json','native-repair-runtime-exact-melee-variants-paid-repair-qualified-41d445cf.json')
preparation=work/'reports/native-exact-melee-variants-paid-repair-qualified-preparation-41d445cf.json';launcher=launcher.replace(parent.relative_to(work).as_posix(),preparation.relative_to(work).as_posix())
new_launcher=work/'tools/run_native_exact_melee_variants_paid_repair_qualified_41d445cf.py';ns['add'](new_launcher.relative_to(root).as_posix(),launcher)
paths=[Path(__file__),parent,excluded,old_source,old_launcher,helper,new_source,new_launcher]
with preparation.open('x',encoding='utf-8') as stream:json.dump({'ids':frozen['ids'],'newObserver':str(new_source),'newLauncher':str(new_launcher),'observerCorrection':'Read original task UUID immediately before CLAIM; successful original legacy CLAIM clears RepairQuest. Still require exact task identity and original signed journal LEASE_ISSUED/sequence5 plus genuine authority/save/replay.','productChanged':False,'authorityFieldsWritten':False,'inputHashes':{str(path):digest(path) for path in paths}},stream,ensure_ascii=False,indent=2)
print(json.dumps({'prepared':preparation.name,'observerCorrectionOnly':True,'cases':19}))
