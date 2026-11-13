import argparse
import hashlib
import json
import subprocess
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'build/boss-arcane-lifetime-v1'
JAVA=Path('C:/Program Files/Zulu/zulu-21/bin')
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(path,value):
    with path.open('x',encoding='utf-8') as stream:json.dump(value,stream,ensure_ascii=False,indent=2)
def run(name,command):
    save(OUT/(name+'-command.json'),command);result=subprocess.run(command,cwd=ROOT,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,creationflags=subprocess.CREATE_NO_WINDOW);(OUT/(name+'.log')).write_bytes(result.stdout);save(OUT/(name+'-terminal.json'),{'exitCode':result.returncode});assert result.returncode==0,str(OUT/(name+'.log'));return result.stdout.decode('utf-8','replace')
parser=argparse.ArgumentParser();parser.add_argument('baseline',type=Path);args=parser.parse_args();baseline_path=args.baseline.resolve();baseline=load(baseline_path)
assert baseline['terminal']['exitCode']==0 and baseline['terminal']['completeSave'] and baseline['terminal']['stopSent'] and baseline['terminal']['inputHashesUnchanged'] and baseline['terminal']['supervisionFailure'] is None
assert baseline['probe']['failures']==0 and baseline['probe']['baselineLifetimeGapDemonstrated'] and baseline['probe']['gaps']==5 and len(baseline['probe']['rows'])==288
for name,wanted in baseline['inputHashes'].items():assert sha(name)==wanted,name
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f' and owner['weaponValidationPaused'] and owner['noNewWeaponRuns']
previous_path=WORK/'releases/boss-core-stage24-manor-body-chain-20261113/deployment-receipt.json';previous=load(previous_path);base=previous_path.parent/'ftboss-boss-core-1.21.1-stage24-manor-body-chain.jar';original=ROOT/'reference/forge-current.jar'
assert sha(base)==sha(previous['target'])==previous['deployedSha256']=='4faa8fcb3e0746bfed8176a2c51f0e2e3a0fd13ec7592cd199e016399c203bb0' and sha(original)=='c1734a148606397f8b2c79fbcc5c45961a5ffb1923ea051756fd45d03dc7a942'
archived=load(previous_path.parent/'archive-receipt.json');assert sha(archived['archive'])==archived['archiveSha256'] and archived['allFilesReadBackAndHashVerified']
audit_path=WORK/'build/boss-arcane-lifetime-oracle-v2/audit-receipt.json';audit=load(audit_path);assert audit['rowsEach']==288 and len(audit['differences'])==5 and audit['allNonLifetimeFieldsOriginalEquivalent']
for name,wanted in audit['inputs'].items():assert sha(name)==wanted,name
OUT.mkdir();classes=OUT/'classes';classes.mkdir();proof_classes=OUT/'proof-classes';proof_classes.mkdir();source=WORK/'tests/ArcaneLifetimeComposerV1.java';oracle_source=WORK/'tests/ArcaneLifetimeOracleV1.java';context_path=WORK/'build/boss-flight-ban-v1/compile-context.json';sdk=[]
for entry in load(context_path)['classpath'].split(';'):
    path=Path(entry)
    if path.suffix.lower()!='.jar':continue
    with zipfile.ZipFile(path) as bundle:
        if 'net/farmingtales/ftboss/FTBossMod.class' in bundle.namelist():continue
    sdk.append(str(path))
classpath=str(base)+';'+';'.join(sdk)
run('compile-proof',[str(JAVA/'javac.exe'),'-proc:none','--release','21','-encoding','UTF-8','-cp',classpath,'-d',str(proof_classes),str(source),str(oracle_source)])
proof=run('original-contract-proof',[str(JAVA/'java.exe'),'-Xmx256m','-cp',str(proof_classes)+';'+classpath,'ArcaneLifetimeComposerV1',str(base),str(original),str(classes)])
assert 'failures=0;originalHelperByteIdentical=true;insertedInstructions=3;' in proof and 'negativeControls=3;' in proof
changed={path.relative_to(classes).as_posix():path.read_bytes() for path in classes.rglob('*.class')};enum='net/farmingtales/ftboss/util/SpellVisualRules$Spell.class';helper='net/farmingtales/ftboss/zheergenphantom/ArcaneReadability.class';assert set(changed)=={enum,helper}
jar=OUT/'ftboss-boss-core-1.21.1-stage25-arcane-lifetime-NOT-INSTALLABLE.jar'
with zipfile.ZipFile(base) as before,zipfile.ZipFile(jar,'x',compression=zipfile.ZIP_DEFLATED) as after:
    for info in before.infolist():after.writestr(info,changed.get(info.filename,before.read(info.filename)))
    after.writestr(helper,changed[helper])
with zipfile.ZipFile(base) as before,zipfile.ZipFile(jar) as after,zipfile.ZipFile(original) as authority:
    assert after.testzip() is None and set(after.namelist())-set(before.namelist())=={helper}
    differences=sorted(name for name in before.namelist() if before.read(name)!=after.read(name));assert differences==[enum] and after.read(helper)==authority.read(helper);class_count=sum(name.endswith('.class') for name in after.namelist());assert class_count==1450
run('candidate-oracle',[str(JAVA/'java.exe'),'-Xmx128m','-cp',str(proof_classes)+';'+str(jar)+';'+';'.join(sdk),'ArcaneLifetimeOracleV1',str(OUT/'candidate-rows.json')])
assert load(OUT/'candidate-rows.json')==load(WORK/'build/boss-arcane-lifetime-oracle-v2/original-rows.json')
inputs={str(path):sha(path) for path in [Path(__file__),source,oracle_source,base,original,previous_path,previous_path.parent/'archive-receipt.json',baseline_path,context_path,audit_path,WORK/'build/boss-arcane-lifetime-oracle-v2/original-rows.json']}
save(OUT/'candidate-build-receipt.json',{'jar':str(jar),'jarSha256':sha(jar),'baseJar':str(base),'baseJarSha256':sha(base),'changedExistingEntries':differences,'addedEntries':[helper],'allOtherEntriesByteIdentical':True,'originalHelperByteIdentical':True,'pureCandidateAll288RowsExactlyOriginal':True,'pureBaselineDiffersOnlyFiveLifetimes':True,'classCount':class_count,'proof':proof.strip(),'inputHashes':inputs,'stage13Included':False,'weaponGameplayChanged':False,'clientActualRenderingOrExpiryAccepted':False,'installable':False,'deployed':False,'fullBossMigrationComplete':False,'weaponsPaused':True})
print(json.dumps({'jar':str(jar),'sha256':sha(jar),'proof':proof.strip(),'fullBossGoalStatus':'active'}),flush=True)
