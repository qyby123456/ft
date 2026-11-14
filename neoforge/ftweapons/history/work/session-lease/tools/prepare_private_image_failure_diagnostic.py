import importlib.util
import json
from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-lease'
helper=ROOT/'work/session-boss/tools/prepare_boss_blood_tide_stage10.py'
spec=importlib.util.spec_from_file_location('bounded_patch_helper',helper);module=importlib.util.module_from_spec(spec)
text=helper.read_text(encoding='utf-8');exec(compile(text[:text.index('probe=(WORK/')],str(helper),'exec'),module.__dict__)
replace=module.replace
source=(WORK/'tests/native-full-nineteen-signed-dd1ad841/PositiveWeapon436.java').read_text(encoding='utf-8')
marker='                        int expectedSize = verifyActualOriginalSessionReturns(session, preparation, publicationRow);'
if marker not in source:
    matches=[line for line in source.splitlines() if 'verifyActualOriginalSessionReturns(session, preparation, publicationRow)' in line]
    assert len(matches)==1,matches
    marker=matches[0]
source=replace(source,marker,'                        Object earlyFailure = read(session, "firstObservationFailure");\n                        publicationRow.put("earlyFirstObservationFailure", earlyFailure == null ? null : earlyFailure.toString());\n'+marker)
module.create(WORK/'tests/private-image-failure-diagnostic-acf29373/PositiveWeapon436.java',source)
diagnostic=(WORK/'tests/native-map-rejection-diagnostic-20261112/NativeMapRejectionDiagnostic.java').read_text(encoding='utf-8')
diagnostic=diagnostic.replace('NativeMapRejectionDiagnostic','NativePrivateImageDiagnostic')
diagnostic=replace(diagnostic,'if (!(message instanceof StringReference text) || !text.value().equals("Nested or unbound Map write cannot claim a domain")) return;','if (!(message instanceof StringReference text)) return;')
diagnostic=replace(diagnostic,'for (var method:type.methodsByName("rejectExtra")) {','for (var method:type.methodsByName(type.name().endsWith("FoodWeaponSourceLedger")?"nativeTimerExpectationReturned":"rejectExtra")) {')
diagnostic=replace(diagnostic,'var prepare=machine.eventRequestManager().createClassPrepareRequest();','var exits=machine.eventRequestManager().createMethodExitRequest();\n            exits.addClassFilter("net.farmingtales.ftboss.item.FoodWeaponSourceLedger");\n            exits.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);\n            var ledgerPrepare=machine.eventRequestManager().createClassPrepareRequest();\n            ledgerPrepare.addClassFilter("net.farmingtales.ftboss.item.FoodWeaponSourceLedger");\n            ledgerPrepare.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);\n            ledgerPrepare.enable();\n            var prepare=machine.eventRequestManager().createClassPrepareRequest();')
diagnostic=replace(diagnostic,'if (type.name().equals(TARGET)) install(machine,type);','if (type.name().equals(TARGET) || type.name().equals("net.farmingtales.ftboss.item.FoodWeaponSourceLedger")) install(machine,type);')
diagnostic=replace(diagnostic,'if (event instanceof BreakpointEvent entered) observe(entered.thread());','''if (event instanceof BreakpointEvent entered) {
                        if (entered.location().method().name().equals("nativeTimerExpectationReturned")) {
                            System.out.println("PRIVATE_IMAGE_LEDGER_ENTER");
                            exits.enable();
                        } else observe(entered.thread());
                    }
                    if (event instanceof MethodExitEvent exited && exited.method().name().equals("nativeTimerExpectationReturned")) {
                        System.out.println("PRIVATE_IMAGE_LEDGER_RETURN result="+exited.returnValue()+" bci="+exited.location().codeIndex()+" line="+exited.location().lineNumber());
                        exits.disable();
                        System.out.flush();
                    }''')
module.create(WORK/'tests/private-image-failure-diagnostic-acf29373/NativePrivateImageDiagnostic.java',diagnostic)
runner=(WORK/'tools/run_native_map_rejection_73d460d2.py').read_text(encoding='utf-8')
runner=replace(runner,'reports/native-repair-session-timer-expectation-candidate-73d460d2.json','reports/native-repair-private-timer-image-candidate-acf29373.json')
runner=replace(runner,'e290bee4f709f7912cfadfb6f103300100f6a264cdeb13422f8df843fab3e9ac','e2b9094b511e3e82c513ed554c3528e850a3cdbd9c6f7b0203e1e2c9d1893db5')
runner=replace(runner,'tests/native-full-nineteen-signed-dd1ad841/PositiveWeapon436.java','tests/private-image-failure-diagnostic-acf29373/PositiveWeapon436.java')
runner=runner.replace('tests/native-map-rejection-diagnostic-20261112/NativeMapRejectionDiagnostic.java','tests/private-image-failure-diagnostic-acf29373/NativePrivateImageDiagnostic.java').replace('NativeMapRejectionDiagnostic','NativePrivateImageDiagnostic').replace('native-map-rejection.log','private-image-failure.log').replace('native-repair-runtime-map-rejection-73d460d2.json','native-repair-runtime-private-image-failure-acf29373.json')
module.create(WORK/'tools/run_native_private_image_failure_acf29373.py',runner)
print(json.dumps({'prepared':True,'originalFullSessionAssertionsRetained':True,'additionalObservationOnly':True,'productCandidateUnchanged':True,'formalInstancesReadOnly':True}),flush=True)
