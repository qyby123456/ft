import hashlib
import importlib.util
import json
import subprocess
from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-lease'

def load(path):
    return json.loads(Path(path).read_text(encoding='utf-8-sig'))

def digest(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()

generic=WORK/'reports/native-repair-private-fingerprint-composition-candidate.json'
candidate=load(generic)
build=Path(candidate['attempt'])
assert build.name=='native-repair-613d4f6f' and candidate['failures']==0
parent_path=WORK/'reports/native-repair-private-timer-image-candidate-acf29373.json'
parent=load(parent_path)
compile_command=load(build/'compile-command.json')
classpath=compile_command[compile_command.index('-cp')+1]
proof=WORK/'tests/PrivateFingerprintCompositionBoundaryProof.java'
proof_classes=build/'boundary-classes'
proof_classes.mkdir()
for name,command in (
    ('boundary-compile',['C:/Program Files/Zulu/zulu-21/bin/javac.exe','-proc:none','--release','21','-encoding','UTF-8','-cp',classpath,'-d',str(proof_classes),str(proof)]),
    ('boundary-proof',['C:/Program Files/Zulu/zulu-21/bin/java.exe','-Xverify:all','-cp',str(proof_classes)+';'+classpath,'PrivateFingerprintCompositionBoundaryProof',str(Path(parent['attempt'])/'complete-classes'),str(build/'complete-classes')]),
):
    with (build/(name+'.log')).open('w',encoding='utf-8') as stream:result=subprocess.run(command,stdout=stream,stderr=subprocess.STDOUT)
    (build/(name+'-terminal.json')).write_text(json.dumps({'exitCode':result.returncode})+'\n',encoding='utf-8')
    assert result.returncode==0,str(build/(name+'.log'))
proof_text=(build/'boundary-proof.log').read_text(encoding='utf-8')
assert 'failures=0;' in proof_text and 'originalMapGuardByteIdentical=true' in proof_text
for key in ('requiredProductionResources','tridentProductionHooksConfigured','bindingSourcesPreserveProposalOverrides','additionalProductionResources','retiredFoodEnchantmentPreparation','retiredFoodEnchantmentRuntimeAccepted'):
    if key in parent:candidate[key]=parent[key]
for key in ('requiredProductionResources','additionalProductionResources'):
    for path in parent.get(key,{}).values():candidate['inputHashes'][path]=parent['inputHashes'][path]
for path in (proof,build/'boundary-proof.log',build/'boundary-proof-terminal.json',parent_path):candidate['inputHashes'][str(path)]=digest(path)
candidate.update({'privateFingerprintOutputBoundaryVerified':True,'boundaryProof':proof_text,'runtimeAccepted':False,'positiveSignedSessionAccepted':False,'installable':False,'deployed':False})
frozen=WORK/'reports/native-repair-private-fingerprint-candidate-613d4f6f.json'
assert not frozen.exists()
frozen.write_text(json.dumps(candidate,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
helper=ROOT/'work/session-boss/tools/prepare_boss_blood_tide_stage10.py'
spec=importlib.util.spec_from_file_location('bounded_patch_helper',helper)
module=importlib.util.module_from_spec(spec)
text=helper.read_text(encoding='utf-8')
exec(compile(text[:text.index('probe=(WORK/')],str(helper),'exec'),module.__dict__)
guard=(WORK/'tests/private-timer-image-foreign-guard/NativeExpectedImageReadGuardProbe.java').read_text(encoding='utf-8')
marker='            var trace=Class.forName("net.farmingtales.ftboss.item.FoodWeaponExpectedTrace");'
guard=module.replace(guard,marker,'            try { NativeExpectedFieldObservation.fingerprintCompoundForRead416(null,new LinkedHashMap<String,Tag>()); }\n            catch (IllegalStateException denied) { rejected++; }\n'+marker)
guard=module.replace(guard,'rejected!=2','rejected!=3')
guard=module.replace(guard,'            result.put("foreignCompoundFactoryRejected",true);','            result.put("foreignFingerprintFactoryRejected",true);\n            result.put("foreignCompoundFactoryRejected",true);')
module.create(WORK/'tests/private-fingerprint-foreign-guard/NativeExpectedImageReadGuardProbe.java',guard)
original=(WORK/'tests/native-full-nineteen-signed-dd1ad841/PositiveWeapon436.java').read_text(encoding='utf-8')
observed=(WORK/'tests/private-image-failure-diagnostic-acf29373/PositiveWeapon436.java').read_text(encoding='utf-8')
addition='                        Object earlyFailure = read(session, "firstObservationFailure");\n                        publicationRow.put("earlyFirstObservationFailure", earlyFailure == null ? null : earlyFailure.toString());\n'
assert observed.count(addition)==1 and observed.replace(addition,'')==original
runner=(WORK/'tools/run_native_full_nineteen_private_image_acf29373.py').read_text(encoding='utf-8')
for old,new in [
    ('reports/native-repair-private-timer-image-candidate-acf29373.json','reports/native-repair-private-fingerprint-candidate-613d4f6f.json'),
    ('e2b9094b511e3e82c513ed554c3528e850a3cdbd9c6f7b0203e1e2c9d1893db5',digest(frozen)),
    ('tests/native-full-nineteen-signed-dd1ad841/PositiveWeapon436.java','tests/private-image-failure-diagnostic-acf29373/PositiveWeapon436.java'),
    ('tests/private-timer-image-foreign-guard/NativeExpectedImageReadGuardProbe.java','tests/private-fingerprint-foreign-guard/NativeExpectedImageReadGuardProbe.java'),
    ('reports/native-repair-runtime-full-nineteen-private-image-acf29373.json','reports/native-repair-runtime-full-nineteen-private-fingerprint-613d4f6f.json'),
]:runner=module.replace(runner,old,new)
module.create(WORK/'tools/run_native_full_nineteen_private_fingerprint_613d4f6f.py',runner)
print(json.dumps({'candidate':str(frozen),'sha256':digest(frozen),'boundary':proof_text,'allOriginalFullSessionAssertionsPreserved':True,'additionalEarlyFailureReadOnly':True,'deployed':False}),flush=True)
