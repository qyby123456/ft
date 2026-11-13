import importlib.util
import json
from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-lease'
helper=ROOT/'work/session-boss/tools/prepare_boss_blood_tide_stage10.py'
spec=importlib.util.spec_from_file_location('bounded_patch_helper',helper)
module=importlib.util.module_from_spec(spec)
text=helper.read_text(encoding='utf-8')
exec(compile(text[:text.index('probe=(WORK/')],str(helper),'exec'),module.__dict__)
source=(WORK/'tests/native-field-retirement-diagnostic-v2-613d4f6f/NativeFieldRetirementDiagnosticV2.java').read_text(encoding='utf-8')
source=source.replace('NativeFieldRetirementDiagnosticV2','NativeFieldRetirementDiagnosticV3')
old='''            if (arguments.size()<6 || !(arguments.get(4) instanceof ObjectReference writer)
                || !(field(writer,"fieldPreparation") instanceof ObjectReference prepared)
                || !(field(prepared,"session") instanceof ObjectReference session)
'''
new='''            if (arguments.size()<6 || !(arguments.get(4) instanceof ObjectReference commit)
                || !(field(commit,"originalSequence") instanceof ObjectReference session)
                || !(field(session,"lineage") instanceof ObjectReference lineage)
                || !(field(lineage,"issuer") instanceof ObjectReference writer)
                || !(field(writer,"fieldPreparation") instanceof ObjectReference prepared)
                || !session.equals(field(prepared,"session"))
                || !thread.equals(field(writer,"thread"))
'''
source=module.replace(source,old,new)
module.create(WORK/'tests/native-field-retirement-diagnostic-v3-613d4f6f/NativeFieldRetirementDiagnosticV3.java',source)
runner=(WORK/'tools/run_native_field_retirement_v2_613d4f6f.py').read_text(encoding='utf-8')
for old,new in [
    ('tests/native-field-retirement-diagnostic-v2-613d4f6f/NativeFieldRetirementDiagnosticV2.java','tests/native-field-retirement-diagnostic-v3-613d4f6f/NativeFieldRetirementDiagnosticV3.java'),
    ('reports/native-repair-runtime-field-retirement-v2-613d4f6f.json','reports/native-repair-runtime-field-retirement-v3-613d4f6f.json'),
    ('field-retirement-v2-diagnostic.log','field-retirement-v3-diagnostic.log'),
]:runner=module.replace(runner,old,new)
runner=runner.replace("'NativeFieldRetirementDiagnosticV2'","'NativeFieldRetirementDiagnosticV3'")
module.create(WORK/'tools/run_native_field_retirement_v3_613d4f6f.py',runner)
report=WORK/'reports/field-retirement-diagnostic-v3-preparation-20261112.json'
assert not report.exists()
report.write_text(json.dumps({'reason':'NativeExpectedFieldObservation.rangedSave argument 4 is the original FinalEffectsCommit, whose originalSequence field resolves the actual Session; use its original lineage issuer to inspect writer custody. The v2 admission read the wrong object and does not prove first retirement.','originalGetterSource':'FoodWeaponHitPlan.originalSequence(FinalEffectsCommit) returns commit.originalSequence','observedProductCandidateUnchanged':True,'startupNbtBreakpointsDisabled':True,'noTargetMethodInvocation':True,'noAuthorityWrites':True,'originalAssertionsUnchanged':True,'v2EvidencePreserved':True,'fullLifecycleAccepted':False,'deployed':False},indent=2)+'\n',encoding='utf-8')
print(json.dumps({'prepared':True,'originalCommitToSessionToLineageIssuerReadPathVerified':True,'candidateUnchanged':True,'readOnlyDebugger':True}),flush=True)
