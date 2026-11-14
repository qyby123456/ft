import hashlib
import json
from pathlib import Path
work=Path(__file__).resolve().parents[1];root=work.parents[1]
def digest(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def load(path):return json.loads(path.read_text(encoding='utf-8'))
parent=work/'reports/native-exact-melee-variants-signed-qualified-preparation-41d445cf.json';frozen=load(parent)
for name,wanted in frozen['inputHashes'].items():assert digest(Path(name))==wanted,Path(name).name
failed_path=work/'reports/native-repair-runtime-exact-melee-variants-signed-qualified-41d445cf.json';failed=load(failed_path);terminal=Path(failed['attempt'])/'terminal308.json';end=load(terminal)
assert failed['probe']['failures']==1 and failed['exitCode']==0 and end['stopSent'] and end['completeSave']
old_source=Path(frozen['newObserver']);source=old_source.read_text(encoding='utf-8').replace('for (int sample = 0; sample < 19; sample++) {','for (int sample = 0; sample < 1; sample++) {')
anchor='                capturedSource = null; capturedSigned = null;';assert source.count(anchor)==1
source=source.replace(anchor,"""                row.put("diagnosticFoundationApplies", net.farmingtales.ftboss.item.FourWeaponFoundationAdapter.applies(actual));
                row.put("diagnosticEvolutionCandidate", net.farmingtales.ftboss.item.FoodWeaponEvolution.isEvolutionCandidate(actual));
                row.put("diagnosticNativeFamily", net.farmingtales.ftboss.item.FoodWeaponUseBridge.kind(actual).name());
                Class<?> inputFields = Class.forName("net.farmingtales.ftboss.item.OwnerNativeWeaponInputFields");
                Object nativeInput = inputFields.getMethod("ftboss$ownerNativeWeaponInput").invoke(actual);
                row.put("diagnosticNativeInputPresentBeforeAttack", nativeInput != null);
                if (nativeInput != null) {
                    row.put("diagnosticNativeInputRetiredBeforeAttack", read(nativeInput, "retired"));
                    row.put("diagnosticNativeInputSameThread", read(nativeInput, "thread") == Thread.currentThread());
                    row.put("diagnosticNativeInputActualSame", ((java.lang.ref.WeakReference<?>)read(nativeInput, "actual")).get() == actual);
                }
                var permits = guard.getDeclaredMethod("permits", Object.class); permits.setAccessible(true);
                row.put("diagnosticGuardPermitsBeforeAttack", permits.invoke(null, actual));
                capturedSource = null; capturedSigned = null;""")
anchor='                row.put("serverSignaturePresent", signed.getString("ServerSignature").length() == 64);';assert source.count(anchor)==1
source=source.replace(anchor,anchor+"""
                Class<?> guard = Class.forName("net.farmingtales.ftboss.item.OwnerFullRepairGuard");
                var generation = guard.getDeclaredMethod("generation", Object.class); generation.setAccessible(true);
                Object current = generation.invoke(null, player.getMainHandItem());
                row.put("diagnosticGenerationPresentAtDamage", current != null);
                if (current != null) row.put("diagnosticGenerationRetiredAtDamage", read(current, "retired"));
                Object ingress = ((ThreadLocal<?>)readStatic(net.farmingtales.ftboss.item.FoodWeaponSourceLedger.class, "PRIVATE_INGRESS")).get();
                Object hurt = ((ThreadLocal<?>)readStatic(net.farmingtales.ftboss.item.FoodWeaponSourceLedger.class, "PRIVATE_NATIVE_HURT")).get();
                row.put("diagnosticOriginalIngressPresentAtDamage", ingress != null);
                row.put("diagnosticOriginalHurtScopePresentAtDamage", hurt != null);
""")
helper=work/'tools/prepare_native_frost_ingress_runtime.py';ns={'__file__':str(helper)};exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0],str(helper),'exec'),ns)
new_source=work/'tests/exact-melee-wooden-sword-diagnostic-41d445cf/PositiveWeapon436.java';ns['add'](new_source.relative_to(root).as_posix(),source)
old_launcher=Path(frozen['newLauncher']);launcher=old_launcher.read_text(encoding='utf-8').replace(old_source.relative_to(work).as_posix(),new_source.relative_to(work).as_posix())
launcher=launcher.replace('native-repair-runtime-exact-melee-variants-signed-qualified-41d445cf.json','native-repair-runtime-exact-melee-wooden-sword-diagnostic-41d445cf.json')
preparation=work/'reports/native-exact-melee-wooden-sword-diagnostic-preparation-41d445cf.json';launcher=launcher.replace(parent.relative_to(work).as_posix(),preparation.relative_to(work).as_posix())
new_launcher=work/'tools/run_native_exact_melee_wooden_sword_diagnostic_41d445cf.py';ns['add'](new_launcher.relative_to(root).as_posix(),launcher)
paths=[Path(__file__),parent,failed_path,terminal,old_source,old_launcher,helper,new_source,new_launcher]
with preparation.open('x',encoding='utf-8') as stream:json.dump({'newObserver':str(new_source),'newLauncher':str(new_launcher),'readOnlyDiagnosticGatesOnly':True,'originalPositiveAssertionsKept':True,'productChanged':False,'authorityFieldsWritten':False,'inputHashes':{str(path):digest(path) for path in paths}},stream,ensure_ascii=False,indent=2)
print(json.dumps({'prepared':preparation.name,'actualCases':1,'positiveAssertionsKept':True,'productChanged':False}))
