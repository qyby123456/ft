import hashlib
import json
import subprocess
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3];WORK=ROOT/'work/session-boss';OUT=WORK/'build/boss-manor-body-chain-v2';JAVA=Path('C:/Program Files/Zulu/zulu-21/bin')
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(path,value):
    with path.open('x',encoding='utf-8') as stream:json.dump(value,stream,ensure_ascii=False,indent=2)
def run(name,command):
    save(OUT/(name+'-command.json'),command);result=subprocess.run(command,cwd=ROOT,capture_output=True,creationflags=subprocess.CREATE_NO_WINDOW);(OUT/(name+'.log')).write_bytes(result.stdout+result.stderr);save(OUT/(name+'-terminal.json'),{'exitCode':result.returncode});assert result.returncode==0,str(OUT/(name+'.log'));return result.stdout.decode('utf-8',errors='replace')
baseline_path=WORK/'reports/manor-body-chain-v1-baseline-bcfdabe1.json';baseline=load(baseline_path)
assert baseline['probe']['failures']==0 and baseline['probe']['actualOriginalBodyChainGapDemonstrated'] and baseline['probe']['completedSkills']==26 and sum(row['actualPulseHits'] for row in baseline['probe']['rows'])==74 and not baseline['probe']['bodySamples']
assert baseline['terminal']['exitCode']==0 and baseline['terminal']['stopSent'] and baseline['terminal']['completeSave'] and baseline['terminal']['inputHashesUnchanged']
for name,wanted in baseline['inputHashes'].items():assert sha(name)==wanted,name
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f' and owner['weaponValidationPaused'] and owner['noNewWeaponRuns']
previous_path=WORK/'releases/boss-core-stage23-manor-light-hooks-20261113/deployment-receipt.json';previous=load(previous_path);base=Path(previous['releasedJar']);assert sha(base)==sha(previous['target'])==previous['deployedSha256']=='97d564e42b907f13a25382e31dff798d6e8899d90d11a9de74c5694504a97c43';original=ROOT/'reference/forge-current.jar';assert sha(original)=='c1734a148606397f8b2c79fbcc5c45961a5ffb1923ea051756fd45d03dc7a942'
audit_paths=[WORK/('build/'+name+'/audit-receipt.json') for name in ['boss-manor-body-lifecycle-audit-v1-20261113','boss-manor-effective-lifecycle-audit-v1-20261113']]
for receipt in audit_paths:
    for name,wanted in load(receipt)['inputs'].items():assert sha(name)==wanted,name
assert not OUT.exists();OUT.mkdir();classes=OUT/'classes';classes.mkdir();proof_classes=OUT/'proof-classes';proof_classes.mkdir();context_path=WORK/'build/boss-flight-ban-v1/compile-context.json';sdk=[]
for entry in load(context_path)['classpath'].split(';'):
    path=Path(entry)
    if path.suffix.lower()!='.jar':continue
    with zipfile.ZipFile(path) as bundle:
        if 'net/farmingtales/ftboss/FTBossMod.class' in bundle.namelist():continue
    sdk.append(str(path))
classpath=str(base)+';'+';'.join(sdk);composer=WORK/'tests/ManorBodyChainComposerV2.java';proof=WORK/'tests/ManorBodyChainInstallerProofV2.java'
run('composer-compile',[str(JAVA/'javac.exe'),'-proc:none','--release','21','-encoding','UTF-8','-cp',classpath,'-d',str(proof_classes),str(composer)])
composed=run('original-full-chain-compose',[str(JAVA/'java.exe'),'-Xmx512m','-cp',str(proof_classes)+';'+classpath,'ManorBodyChainComposerV2',str(base),str(original),str(classes)])
assert 'failures=0;wholeOriginalStrikeRestored=true;remainingOriginalStartAndFinishRestored=true;' in composed
recipe=load(OUT/'composition-recipe.json');resource=classes/'net/farmingtales/ftboss/boss/ai/original-manor-body-methods-v2.bin'
installer_source=WORK/'proposal-stage24-manor-body-chain-v2/net/farmingtales/ftboss/boss/ai/NativeManorBodyChainInstallerV2.java'
installer='''package net.farmingtales.ftboss.boss.ai;
import java.util.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;
public final class NativeManorBodyChainInstallerV2 implements Opcodes {
    private static final String BOSS="net/farmingtales/ftboss/entity/EasterBossEntity",RESOURCE="/net/farmingtales/ftboss/boss/ai/original-manor-body-methods-v2.bin",TEMPLATE_SHA="@@RESOURCE@@",BEFORE_FINISH="@@BEFORE@@";
    private static final Map<String,String> AFTER=Map.of("onManorFiveCastStart","@@START@@","ftboss$nativeXuanlanCue","@@CUE@@","finishManorFiveCast","@@FINISH@@");
    private NativeManorBodyChainInstallerV2(){}
    private static void require(boolean value,String message){if(!value)throw new IllegalStateException(message);}
    private static MethodNode method(ClassNode node,String name){List<MethodNode> found=node.methods.stream().filter(value->value.name.equals(name)).toList();require(found.size()==1,"Exact original body chain method required:"+name);return found.getFirst();}
    private static String sha(byte[] bytes){try{return HexFormat.of().formatHex(java.security.MessageDigest.getInstance("SHA-256").digest(bytes));}catch(java.security.NoSuchAlgorithmException failure){throw new IllegalStateException(failure);}}
    private static String fingerprint(MethodNode original){MethodNode method=new MethodNode(original.access,original.name,original.desc,original.signature,original.exceptions.toArray(String[]::new));original.accept(method);method.localVariables=null;method.visibleLocalVariableAnnotations=null;method.invisibleLocalVariableAnnotations=null;method.maxStack=0;method.maxLocals=0;for(AbstractInsnNode instruction:method.instructions.toArray())if(instruction instanceof FrameNode||instruction instanceof LineNumberNode)method.instructions.remove(instruction);Set<LabelNode> targets=Collections.newSetFromMap(new IdentityHashMap<>());for(AbstractInsnNode instruction:method.instructions.toArray()){if(instruction instanceof JumpInsnNode branch)targets.add(branch.label);if(instruction instanceof TableSwitchInsnNode table){targets.add(table.dflt);targets.addAll(table.labels);}if(instruction instanceof LookupSwitchInsnNode table){targets.add(table.dflt);targets.addAll(table.labels);}}for(TryCatchBlockNode item:method.tryCatchBlocks){targets.add(item.start);targets.add(item.end);targets.add(item.handler);}for(AbstractInsnNode instruction:method.instructions.toArray())if(instruction instanceof LabelNode label&&!targets.contains(label))method.instructions.remove(label);org.objectweb.asm.util.Textifier text=new org.objectweb.asm.util.Textifier();method.accept(new org.objectweb.asm.util.TraceMethodVisitor(text));String canonical=method.access+":"+method.name+":"+method.desc+":"+method.signature+":"+method.exceptions+":"+text.getText();return sha(canonical.getBytes(java.nio.charset.StandardCharsets.UTF_8));}
    private static ClassNode template(){try(var stream=NativeManorBodyChainInstallerV2.class.getResourceAsStream(RESOURCE)){require(stream!=null,"Pinned original body template missing");byte[] bytes=stream.readAllBytes();require(sha(bytes).equals(TEMPLATE_SHA),"Pinned original body template changed");ClassNode node=new ClassNode();new ClassReader(bytes).accept(node,0);require(node.name.equals(BOSS)&&node.methods.size()==3,"Original body template contract changed");for(String name:AFTER.keySet())require(fingerprint(method(node,name)).equals(AFTER.get(name)),"Original body template method changed:"+name);return node;}catch(java.io.IOException failure){throw new IllegalStateException(failure);}}
    public static void install(ClassNode node){require(node.name.equals(BOSS),"Exact original Boss body owner required");boolean present=node.methods.stream().anyMatch(value->value.name.equals("onManorFiveCastStart")||value.name.equals("ftboss$nativeXuanlanCue"));if(present){verifyInstalled(node);return;}require(fingerprint(method(node,"finishManorFiveCast")).equals(BEFORE_FINISH),"Complete existing finish postimage required before original body chain");for(String name:List.of("canShadowStrike","spawnShadowPhantom"))method(node,name);for(String access:List.of("animation/qingyicast/QingyiCastAccess","body/BodyCueAccess","zheergennightfall/NightfallAccess","zheergenphantom/PhantomAccess"))require(node.interfaces.contains("net/farmingtales/ftboss/"+access),"Existing original body interface missing:"+access);ClassNode original=template();for(String name:List.of("onManorFiveCastStart","ftboss$nativeXuanlanCue"))node.methods.add(method(original,name));node.methods.set(node.methods.indexOf(method(node,"finishManorFiveCast")),method(original,"finishManorFiveCast"));verifyInstalled(node);}
    public static void verifyInstalled(ClassNode node){require(node.name.equals(BOSS),"Exact original Boss body owner required");for(String name:AFTER.keySet())require(fingerprint(method(node,name)).equals(AFTER.get(name)),"Complete original body chain postimage required:"+name);}
}
'''
for key,value in {'@@RESOURCE@@':sha(resource),'@@BEFORE@@':recipe['beforeFinish'],'@@START@@':recipe['afterMethods']['onManorFiveCastStart'],'@@CUE@@':recipe['afterMethods']['ftboss$nativeXuanlanCue'],'@@FINISH@@':recipe['afterMethods']['finishManorFiveCast']}.items():installer=installer.replace(key,value)
creator=WORK/'tools/prepare_boss_blood_tide_stage10.py';creation={'__file__':str(creator)};exec(creator.read_text('utf-8').split('probe=(WORK/')[0],creation);creation['create'](installer_source,installer)
run('late-installer-compile',[str(JAVA/'javac.exe'),'-proc:none','--release','21','-encoding','UTF-8','-cp',classpath,'-d',str(classes),str(installer_source)])
run('installer-proof-compile',[str(JAVA/'javac.exe'),'-proc:none','--release','21','-encoding','UTF-8','-cp',str(classes)+';'+str(proof_classes)+';'+classpath,'-d',str(proof_classes),str(proof)])
proved=run('late-installer-proof',[str(JAVA/'java.exe'),'-Xmx512m','-cp',str(classes)+';'+str(proof_classes)+';'+classpath,'ManorBodyChainInstallerProofV2',str(base),str(classes)])
assert 'failures=0;exactOriginalEntityMethodsInstalled=3;newEntityMethods=2;effectiveEntityMethodsAsmVerified=341;' in proved and 'lateOwnerPreimageNegativeControls=2;lateInstallIdempotent=true;' in proved
changed={path.relative_to(classes).as_posix():path.read_bytes() for path in classes.rglob('*') if path.is_file()};modified={'net/farmingtales/ftboss/boss/ai/ManorFiveSkills.class','net/farmingtales/ftboss/boss/ai/NativeManorMixinPlugin.class'};added={'net/farmingtales/ftboss/boss/ai/NativeManorBodyChainInstallerV2.class','net/farmingtales/ftboss/skill/xuanlannative/NativeSignals.class','net/farmingtales/ftboss/boss/ai/original-manor-body-methods-v2.bin'};assert set(changed)==modified|added
jar=OUT/'ftboss-boss-core-1.21.1-stage24-manor-body-chain-NOT-INSTALLABLE.jar'
with zipfile.ZipFile(base) as before,zipfile.ZipFile(jar,'x',compression=zipfile.ZIP_DEFLATED) as after:
    for info in before.infolist():after.writestr(info,changed.get(info.filename,before.read(info.filename)))
    for name in sorted(added):assert name not in before.namelist();after.writestr(name,changed[name])
with zipfile.ZipFile(base) as before,zipfile.ZipFile(jar) as after:
    assert after.testzip() is None and set(after.namelist())==set(before.namelist())|added;differences=sorted(name for name in before.namelist() if before.read(name)!=after.read(name));assert differences==sorted(modified) and sum(name.endswith('.class') for name in after.namelist())==1449
save(OUT/'candidate-build-receipt.json',{'jar':str(jar),'jarSha256':sha(jar),'baseJar':str(base),'baseJarSha256':sha(base),'baseline':str(baseline_path),'changedExistingEntries':differences,'addedEntries':sorted(added),'allOtherEntriesByteIdentical':True,'rawBossAndOriginalInstallersUnchanged':True,'originalTemplateSha256':sha(resource),'composerProof':composed.strip(),'installerProof':proved.strip(),'compositionRecipe':str(OUT/'composition-recipe.json'),'inputHashes':{str(path):sha(path) for path in [Path(__file__),baseline_path,composer,proof,installer_source,base,original,previous_path,context_path,*audit_paths,creator]},'stage13Included':False,'networkDeliveryAccepted':False,'visibleClientEffectsAccepted':False,'naturalBossAiAndCountdownAccepted':False,'installable':False,'deployed':False,'fullBossMigrationComplete':False,'weaponsPaused':True})
print(json.dumps({'jar':str(jar),'sha256':sha(jar),'composerProof':composed.strip(),'installerProof':proved.strip(),'fullBossGoal':'active'}),flush=True)
