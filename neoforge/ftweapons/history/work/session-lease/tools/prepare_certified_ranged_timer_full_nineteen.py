import hashlib
import importlib.util
import json
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-lease'
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def digest(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
helper=ROOT/'work/session-boss/tools/prepare_boss_blood_tide_stage10.py'
spec=importlib.util.spec_from_file_location('bounded_patch_helper',helper)
module=importlib.util.module_from_spec(spec)
text=helper.read_text(encoding='utf-8');exec(compile(text[:text.index('probe=(WORK/')],str(helper),'exec'),module.__dict__)
parent_path=WORK/'reports/native-repair-private-fingerprint-candidate-613d4f6f.json'
parent=load(parent_path)
candidate=load(WORK/'reports/native-repair-certified-ranged-timer-frame-candidate.json')
build=Path(candidate['attempt']);assert build.name=='native-repair-8199dcc2'
assert load(build/'boundary-proof-terminal.json')['exitCode']==0 and load(build/'asm.json')['failures']==0
boundary=(build/'boundary-proof.log').read_text(encoding='utf-8')
assert 'existingMethodsPreserved=437' in boundary and 'negativeChainCases=9' in boundary and 'dynamicOrdinalVerified=true' in boundary
original=(WORK/'tests/native-full-nineteen-signed-dd1ad841/PositiveWeapon436.java').read_text(encoding='utf-8')
observed=(WORK/'tests/private-image-failure-diagnostic-acf29373/PositiveWeapon436.java').read_text(encoding='utf-8')
addition='                        Object earlyFailure = read(session, "firstObservationFailure");\n                        publicationRow.put("earlyFirstObservationFailure", earlyFailure == null ? null : earlyFailure.toString());\n'
assert observed.count(addition)==1 and observed.replace(addition,'')==original
source=(WORK/'tests/private-fingerprint-foreign-guard/NativeExpectedImageReadGuardProbe.java').read_text(encoding='utf-8')
marker='            if (rejected!=3 || domains.get(null)!=before'
added=r"""            var trust=Class.forName("net.farmingtales.ftboss.item.NativeFreshFactoryTrust298");
            var registered=trust.getDeclaredField("rangedTimerMethod");
            registered.setAccessible(true);
            Object registeredBefore=registered.get(null);
            if (!(registeredBefore instanceof String methodName) || methodName.isEmpty()) throw new AssertionError("Certified native ranged timer method was not installed");
            boolean physical=java.util.Arrays.stream(net.minecraft.nbt.CompoundTag.class.getDeclaredMethods()).anyMatch(method->method.getName().equals(methodName)
                && method.getDeclaringClass()==net.minecraft.nbt.CompoundTag.class && java.lang.reflect.Modifier.isPrivate(method.getModifiers())
                && method.getReturnType()==void.class && java.util.Arrays.equals(method.getParameterTypes(),new Class<?>[]{String.class,long.class}));
            if (!physical) throw new AssertionError("Registered timer original is not the actual private CompoundTag method");
            var install=trust.getDeclaredMethod("installRangedTimer",Class.forName("org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext"));
            install.setAccessible(true);
            try { install.invoke(null,new Object[]{null}); }
            catch (InvocationTargetException denied) { if (denied.getCause() instanceof IllegalStateException) rejected++; else throw denied; }
            var frameCheck=trust.getDeclaredMethod("rangedTimerMutationFrame",StackWalker.StackFrame.class);
            frameCheck.setAccessible(true);
            var foreign=StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(frames->frames.findFirst().orElseThrow());
            if (!Boolean.FALSE.equals(frameCheck.invoke(null,foreign)) || registered.get(null)!=registeredBefore) throw new AssertionError("Foreign frame admitted or registry identity changed");
            result.put("certifiedActualRangedTimerMethodInstalled",true);
            result.put("actualPrivateMethodDescriptorVerified",true);
            result.put("foreignRangedTimerInstallerRejected",true);
            result.put("foreignStackFrameRejected",true);
            result.put("registeredTimerMethodIdentityUnchanged",true);
"""
source=module.replace(source,marker,added+'            if (rejected!=4 || domains.get(null)!=before')
guard=WORK/'tests/certified-ranged-timer-frame-foreign-guard/NativeExpectedImageReadGuardProbe.java'
module.create(guard,source)
for key,value in parent.items():
    if key not in candidate:candidate[key]=value
for key in ('requiredProductionResources','additionalProductionResources'):
    for path in parent.get(key,{}).values():candidate['inputHashes'][path]=parent['inputHashes'][path]
for path in [Path(__file__),parent_path,guard,WORK/'tests/CertifiedRangedTimerBoundaryProof.java',WORK/'reports/certified-ranged-timer-frame-preparation-20261112.json',build/'boundary-proof.log',build/'boundary-proof-terminal.json',WORK/'build/repair-runtime-98fb0193/server/.mixin.out/class/net/minecraft/nbt/CompoundTag.class',WORK/'tests/native-full-nineteen-signed-dd1ad841/PositiveWeapon436.java',WORK/'tests/private-image-failure-diagnostic-acf29373/PositiveWeapon436.java']:
    candidate['inputHashes'][str(path)]=digest(path)
for path,wanted in candidate['inputHashes'].items():assert digest(path)==wanted,path
candidate.update({'certifiedActualRangedTimerFrameBoundaryVerified':True,'boundaryProof':boundary,'originalFullNineteenAssertionsPreserved':True,'registeredOnlyAfterUnchangedCompleteMapProof':True,'runtimeAccepted':False,'positiveSignedSessionAccepted':False,'fullSignedSessionAccepted':False,'installable':False,'deployed':False})
frozen=WORK/'reports/native-repair-certified-ranged-timer-frame-candidate-8199dcc2.json'
with frozen.open('x',encoding='utf-8') as stream:json.dump(candidate,stream,ensure_ascii=False,indent=2)
runner=(WORK/'tools/run_native_full_nineteen_private_fingerprint_613d4f6f.py').read_text(encoding='utf-8')
for old,new in [('reports/native-repair-private-fingerprint-candidate-613d4f6f.json','reports/native-repair-certified-ranged-timer-frame-candidate-8199dcc2.json'),('59051d1e1377b954278e76e9deadc54d433e4e8f9f03f16ea113dcde037be4c8',digest(frozen)),('tests/private-fingerprint-foreign-guard/NativeExpectedImageReadGuardProbe.java','tests/certified-ranged-timer-frame-foreign-guard/NativeExpectedImageReadGuardProbe.java'),('reports/native-repair-runtime-full-nineteen-private-fingerprint-613d4f6f.json','reports/native-repair-runtime-full-nineteen-certified-ranged-timer-frame-8199dcc2.json')]:runner=module.replace(runner,old,new)
module.create(WORK/'tools/run_native_full_nineteen_certified_ranged_timer_frame_8199dcc2.py',runner)
print(json.dumps({'frozenCandidate':str(frozen),'sha256':digest(frozen),'originalFullNineteenAssertionsPreserved':True,'additionalForeignInstallerAndFrameControls':True,'deployed':False}),flush=True)
