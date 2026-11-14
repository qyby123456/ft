import difflib
import hashlib
import json
import shutil
import subprocess
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-lease'
OUT=WORK/'proposal-certified-ranged-timer-frame'
PATCH='C:/Users/qyby/AppData/Local/OpenAI/Codex/bin/b6c7fc9497179c98/codex.exe'
assert not OUT.exists()
arguments=json.loads((WORK/'build/native-repair-613d4f6f/complete-arguments316.json').read_text(encoding='utf-8-sig'))
records=[]
def append(text,addition):
    index=text.rindex('\n}')
    return text[:index]+addition+'\n'+text[index:]
def change(name,transform):
    original=next(Path(value) for value in arguments if isinstance(value,str) and value.endswith(name))
    before=original.read_text(encoding='utf-8-sig');after=transform(before);assert before!=after
    target=OUT/'net/farmingtales/ftboss/item'/name;target.parent.mkdir(parents=True,exist_ok=True);shutil.copyfile(original,target)
    lines=list(difflib.unified_diff(before.splitlines(),after.splitlines(),n=3))[2:]
    patch='*** Begin Patch\n*** Update File: '+target.relative_to(ROOT).as_posix()+'\n'+''.join('@@\n' if line.startswith('@@') else line+'\n' for line in lines)+'*** End Patch\n'
    result=subprocess.run([PATCH,'--codex-run-as-apply-patch',patch],cwd=ROOT,capture_output=True,text=True)
    assert result.returncode==0,result.stderr
    assert target.read_text(encoding='utf-8').splitlines()==after.splitlines()
    records.append({'beforeSource':str(original),'beforeSourceSha256':hashlib.sha256(original.read_bytes()).hexdigest(),'proposal':str(target),'proposalSha256':hashlib.sha256(target.read_bytes()).hexdigest()})
proof=r"""
    static String nativeRangedLongMethod(ClassNode host) {
        if (host == null || !host.name.equals("net/minecraft/nbt/CompoundTag")) return null;
        MethodNode entry = null;
        MethodNode guard = null;
        for (MethodNode method : host.methods) {
            if (method.name.equals("putLong") && method.desc.equals("(Ljava/lang/String;J)V")) {
                if (entry != null) return null;
                entry = method;
            }
            if (member(method.name).equals("ftboss$fullRepairVoidMutation")
                && method.desc.equals("(Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfo;)V")
                && merged(method, "net.farmingtales.ftboss.mixin.OwnerFullRepairCompoundTagMixin")) {
                if (guard != null) return null;
                guard = method;
            }
        }
        if (entry == null || guard == null) return null;
        MethodNode original = nativeFxGuardParent(host, entry, guard);
        return original == null ? null : original.name;
    }
"""
change('OwnerFieldInstallationProof.java',lambda text:append(text,proof))
trust=r"""
    private static volatile String rangedTimerMethod;

    static synchronized void installRangedTimer(ITargetClassContext context) {
        boolean authorized = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(frames -> frames.skip(1).findFirst()
            .map(frame -> frame.getDeclaringClass() == OwnerFieldCoverageRegistry.class && frame.getMethodName().equals("lateApplied")
                && frame.getDescriptor().equals("(Lorg/spongepowered/asm/mixin/transformer/ext/ITargetClassContext;)V")).orElse(false));
        if (!authorized || context == null || NativeMapPutLateProofExtension.current(context.getClassNode()) != context
            || NativeMapPutLateProofExtension.order() == null || !"net/minecraft/nbt/CompoundTag".equals(context.getClassNode().name)
            || factoryMethods.isEmpty() || rangedTimerMethod != null) throw new IllegalStateException("Untrusted ranged timer method installation");
        String method = OwnerFieldInstallationProof.nativeRangedLongMethod(context.getClassNode());
        if (method == null) throw new IllegalStateException("Ranged timer wrapped original chain differs");
        rangedTimerMethod = method;
    }

    static boolean rangedTimerMutationFrame(StackWalker.StackFrame frame) {
        String method = rangedTimerMethod;
        return method != null && frame != null && frame.getDeclaringClass() == CompoundTag.class
            && frame.getMethodName().equals(method) && frame.getDescriptor().equals("(Ljava/lang/String;J)V");
    }
"""
change('NativeFreshFactoryTrust298.java',lambda text:append(text,trust))
def registry(text):
    old='                                        lateStages[var2] = 2;'
    assert text.count(old)==1
    return text.replace(old,'                                        if (var2 == 0) NativeFreshFactoryTrust298.installRangedTimer(var0);\n'+old)
change('OwnerFieldCoverageRegistry.java',registry)
def observation(text):
    old='            || !frames.get(2).getMethodName().equals(timer ? "putLong" : "put")'
    new='            || !(timer ? NativeFreshFactoryTrust298.rangedTimerMutationFrame(frames.get(2)) : frames.get(2).getMethodName().equals("put"))'
    assert text.count(old)==1
    return text.replace(old,new)
change('NativeExpectedFieldObservation.java',observation)
report={'scope':'Authenticate actual transformed ranged putLong frame only after unchanged complete final Map body proof','parentCandidate':'613d4f6f','sources':records,'existingPrivateWrappedChainProofReused':True,'noHardcodedOrdinal':True,'noGlobRuntimeNameAdmission':True,'originalScopeFactStepOwnerKeyValueThreadLineageAndBeforeImageChecksUnchanged':True,'rangedRootPutFrameUnchanged':True,'fullNineteenAssertionsUnchanged':True,'deployed':False,'fullSignedSessionAccepted':False}
path=WORK/'reports/certified-ranged-timer-frame-preparation-20261112.json'
with path.open('x',encoding='utf-8') as stream:json.dump(report,stream,ensure_ascii=False,indent=2)
print(json.dumps({'prepared':True,'changedProductSources':len(records),'deployed':False,'fullAcceptance':False}),flush=True)
