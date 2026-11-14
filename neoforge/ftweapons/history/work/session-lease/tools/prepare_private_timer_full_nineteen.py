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

generic=WORK/'reports/native-repair-private-timer-image-v2-candidate.json'
candidate=load(generic)
build=Path(candidate['attempt'])
assert build.name=='native-repair-acf29373' and candidate['failures']==0
parent_path=WORK/'reports/native-repair-session-timer-expectation-candidate-73d460d2.json'
parent=load(parent_path)
compile_command=load(build/'compile-command.json')
classpath=compile_command[compile_command.index('-cp')+1]
proof=WORK/'tests/PrivateTimerImageBoundaryProof.java'
proof_classes=build/'boundary-classes'
proof_classes.mkdir()
for name,command in (
    ('boundary-compile',['C:/Program Files/Zulu/zulu-21/bin/javac.exe','-proc:none','--release','21','-encoding','UTF-8','-cp',classpath,'-d',str(proof_classes),str(proof)]),
    ('boundary-proof',['C:/Program Files/Zulu/zulu-21/bin/java.exe','-Xmx512m','-cp',str(proof_classes)+';'+classpath,'PrivateTimerImageBoundaryProof',str(Path(parent['attempt'])/'complete-classes'),str(build/'complete-classes')])):
    with (build/(name+'.log')).open('xb') as stream:result=subprocess.run(command,stdout=stream,stderr=subprocess.STDOUT)
    (build/(name+'-terminal.json')).write_text(json.dumps({'exitCode':result.returncode}),encoding='utf-8')
    assert result.returncode==0,str(build/(name+'.log'))
proof_text=(build/'boundary-proof.log').read_text(encoding='utf-8')
assert 'failures=0;' in proof_text and 'originalMapGuardUnchanged=true' in proof_text
for key in ('requiredProductionResources','tridentProductionHooksConfigured','bindingSourcesPreserveProposalOverrides','additionalProductionResources','retiredFoodEnchantmentPreparation','retiredFoodEnchantmentRuntimeAccepted'):
    if key in parent:candidate[key]=parent[key]
for key in ('requiredProductionResources','additionalProductionResources'):
    for path in parent.get(key,{}).values():candidate['inputHashes'][path]=parent['inputHashes'][path]
for path in (proof,build/'boundary-proof.log',build/'boundary-proof-terminal.json',parent_path):candidate['inputHashes'][str(path)]=digest(path)
candidate.update({'privateExpectedImageBoundaryVerified':True,'boundaryProof':proof_text,'positiveSignedSessionAccepted':False,'runtimeAccepted':False,'installable':False,'deployed':False})
frozen=WORK/'reports/native-repair-private-timer-image-candidate-acf29373.json'
assert not frozen.exists();frozen.write_text(json.dumps(candidate,ensure_ascii=False,indent=2),encoding='utf-8')
helper=ROOT/'work/session-boss/tools/prepare_boss_blood_tide_stage10.py'
spec=importlib.util.spec_from_file_location('bounded_patch_helper',helper);module=importlib.util.module_from_spec(spec)
text=helper.read_text(encoding='utf-8');exec(compile(text[:text.index('probe=(WORK/')],str(helper),'exec'),module.__dict__)
runner=(WORK/'tools/run_native_full_nineteen_signed_73d460d2.py').read_text(encoding='utf-8')
runner=module.replace(runner,'reports/native-repair-session-timer-expectation-candidate-73d460d2.json','reports/native-repair-private-timer-image-candidate-acf29373.json')
runner=module.replace(runner,'e290bee4f709f7912cfadfb6f103300100f6a264cdeb13422f8df843fab3e9ac',digest(frozen))
runner=module.replace(runner,"source = WORK / 'tests/native-full-nineteen-signed-dd1ad841/PositiveWeapon436.java'","source = WORK / 'tests/native-full-nineteen-signed-dd1ad841/PositiveWeapon436.java'\nread_guard_source = WORK / 'tests/private-timer-image-foreign-guard/NativeExpectedImageReadGuardProbe.java'")
runner=module.replace(runner,"save(attempt / 'compile-command308.json', compile_command)","compile_command.append(str(read_guard_source))\nsave(attempt / 'compile-command308.json', compile_command)")
runner=module.replace(runner,"save(attempt / 'inputs-before308.json', inputs)","inputs[str(read_guard_source)] = digest(read_guard_source)\nsave(attempt / 'inputs-before308.json', inputs)",runner.count("save(attempt / 'inputs-before308.json', inputs)"))
runner=module.replace(runner,"output = WORK / 'reports/native-repair-runtime-full-nineteen-signed-73d460d2.json'","guard_probe = load(server / 'actual-private-read-guard.json') if (server / 'actual-private-read-guard.json').exists() else {'failures': 1, 'failure': 'Foreign private read guard observer did not run'}\nsave(attempt / 'private-read-guard-terminal.json', guard_probe)\noutput = WORK / 'reports/native-repair-runtime-full-nineteen-private-image-acf29373.json'")
runner=module.replace(runner,"'probe': probe, 'reusedDependencyCrcAndLinkageReceipt299'","'probe': probe, 'privateReadGuardProbe': guard_probe, 'reusedDependencyCrcAndLinkageReceipt299'")
runner=module.replace(runner,"assert exit_code == 0 and probe['failures'] == 0","assert exit_code == 0 and probe['failures'] == 0 and guard_probe['failures'] == 0")
module.create(WORK/'tools/run_native_full_nineteen_private_image_acf29373.py',runner)
print(json.dumps({'candidate':str(frozen),'candidateSha256':digest(frozen),'boundaryProof':proof_text,'originalFullNineteenAssertionsUnchanged':True,'foreignGuardAddedSeparately':True,'deployable':False}),flush=True)
