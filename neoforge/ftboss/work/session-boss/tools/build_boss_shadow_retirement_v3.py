import argparse
import hashlib
import json
import subprocess
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'build/boss-shadow-retirement-v3'
JAVA=Path('C:/Program Files/Zulu/zulu-21/bin')
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(path,value):
    with path.open('x',encoding='utf-8') as stream:json.dump(value,stream,ensure_ascii=False,indent=2)
def run(name,command):
    save(OUT/(name+'-command.json'),command);result=subprocess.run(command,cwd=ROOT,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,creationflags=subprocess.CREATE_NO_WINDOW);(OUT/(name+'.log')).write_bytes(result.stdout);save(OUT/(name+'-terminal.json'),{'exitCode':result.returncode});assert result.returncode==0,str(OUT/(name+'.log'));return result.stdout.decode('utf-8','replace')
parser=argparse.ArgumentParser();parser.add_argument('baseline',type=Path);args=parser.parse_args();baseline_path=args.baseline.resolve();baseline=load(baseline_path)
assert baseline['terminal']['exitCode']==0 and baseline['terminal']['integratedWorldCompleteSave'] and baseline['terminal']['clientProcessReaped'] and baseline['terminal']['inputHashesUnchanged']
probe=baseline['probe'];assert probe['failures']==0 and probe['baselineOriginalShadowRetirementGapsDemonstrated'] and probe['gaps']==6 and probe['completedPlayerCases']==1 and probe['actualLocalPlayerNativeLoginAndEffectSynchronization'] and not probe['fakeOrSyntheticServerPlayerConstructed']
for name,wanted in baseline['inputHashes'].items():assert sha(name)==wanted,name
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f' and owner['weaponValidationPaused'] and owner['noNewWeaponRuns']
previous_path=WORK/'releases/boss-core-stage25-arcane-lifetime-20261113/deployment-receipt.json';previous=load(previous_path);base=Path(previous['releasedJar']);original=ROOT/'reference/forge-current.jar'
assert sha(base)==sha(previous['target'])==previous['deployedSha256']=='7eb3b4c2e4ef8e35c806a4df8b53cb32879a9cec9abee961a963bf558d39d7b6' and sha(original)=='c1734a148606397f8b2c79fbcc5c45961a5ffb1923ea051756fd45d03dc7a942'
archived=load(previous_path.parent/'archive-receipt.json');assert sha(archived['archive'])==archived['archiveSha256'] and archived['allFilesReadBackAndHashVerified']
audit_path=WORK/'build/boss-shadow-retirement-contract-audit-v1-20261113/audit-receipt.json';audit=load(audit_path)
for name,wanted in audit['inputs'].items():assert sha(name)==wanted,name
OUT.mkdir();classes=OUT/'classes';classes.mkdir();proof_classes=OUT/'proof-classes';proof_classes.mkdir();source=WORK/'tests/ShadowRetirementComposerV3.java';context_path=WORK/'build/boss-flight-ban-v1/compile-context.json';sdk=[]
for entry in load(context_path)['classpath'].split(';'):
    path=Path(entry)
    if path.suffix.lower()!='.jar':continue
    with zipfile.ZipFile(path) as bundle:
        if 'net/farmingtales/ftboss/FTBossMod.class' in bundle.namelist():continue
    sdk.append(str(path))
classpath=str(base)+';'+';'.join(sdk)
run('compile-proof',[str(JAVA/'javac.exe'),'-proc:none','--release','21','-encoding','UTF-8','-cp',classpath,'-d',str(proof_classes),str(source)])
proof=run('original-contract-proof',[str(JAVA/'java.exe'),'-Xmx256m','-cp',str(proof_classes)+';'+classpath,'ShadowRetirementComposerV3',str(base),str(original),str(classes)])
assert 'failures=0;originalMappedMethodsExact=17;' in proof and 'negativeControls=4;' in proof and 'priorEffectBodyPreserved=true;' in proof
changed={path.relative_to(classes).as_posix():path.read_bytes() for path in classes.rglob('*.class')};blind='net/farmingtales/ftboss/effect/ShadowBlindEffect.class';spell='net/farmingtales/ftboss/integ/spells/ManorSpellEffects.class';helper='net/farmingtales/ftboss/zheergenphantom/ErosionFilter.class';buff='net/farmingtales/ftboss/boss/ai/PlayerBuffGuard.class';assert set(changed)=={blind,spell,helper,buff}
jar=OUT/'ftboss-boss-core-1.21.1-stage26-shadow-retirement-NOT-INSTALLABLE.jar'
with zipfile.ZipFile(base) as before,zipfile.ZipFile(jar,'x',compression=zipfile.ZIP_DEFLATED) as after:
    for info in before.infolist():after.writestr(info,changed.get(info.filename,before.read(info.filename)))
    after.writestr(helper,changed[helper])
with zipfile.ZipFile(base) as before,zipfile.ZipFile(jar) as after:
    assert after.testzip() is None and set(after.namelist())-set(before.namelist())=={helper}
    differences=sorted(name for name in before.namelist() if before.read(name)!=after.read(name));assert differences==sorted([blind,spell,buff]);class_count=sum(name.endswith('.class') for name in after.namelist());assert class_count==1451
inputs={str(path):sha(path) for path in [Path(__file__),source,base,original,previous_path,previous_path.parent/'archive-receipt.json',baseline_path,context_path,audit_path]}
save(OUT/'candidate-build-receipt.json',{'jar':str(jar),'jarSha256':sha(jar),'baseJar':str(base),'baseJarSha256':sha(base),'changedExistingEntries':differences,'addedEntries':[helper],'allOtherEntriesByteIdentical':True,'originalMappedMethodsExact':17,'originalRegistryObjectToHolderAdaptations':3,'originalMobEffectCastsRemoved':2,'originalEffectApplicationBodyPreserved':True,'originalBuffPublicMethodsRetired':7,'originalBuffConstantsPinned':4,'changedBuffConstantValues':1,'classCount':class_count,'proof':proof.strip(),'inputHashes':inputs,'stage13Included':False,'weaponGameplayChanged':False,'actualClientPlayerAcceptance':False,'installable':False,'deployed':False,'fullBossMigrationComplete':False,'weaponsPaused':True})
print(json.dumps({'jar':str(jar),'sha256':sha(jar),'proof':proof.strip(),'fullBossGoalStatus':'active'}),flush=True)
