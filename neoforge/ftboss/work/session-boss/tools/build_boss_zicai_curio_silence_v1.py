import argparse
import hashlib
import json
import subprocess
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'build/boss-zicai-curio-silence-v1-20261113'
JAVA=Path('C:/Program Files/Zulu/zulu-21/bin')
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(name,value):
    with (OUT/name).open('x',encoding='utf-8') as stream:json.dump(value,stream,ensure_ascii=False,indent=2)
def run(name,command):
    save(name+'-command.json',command)
    result=subprocess.run(command,cwd=ROOT,capture_output=True,creationflags=subprocess.CREATE_NO_WINDOW)
    (OUT/(name+'.log')).write_bytes(result.stdout+result.stderr)
    save(name+'-terminal.json',{'exitCode':result.returncode})
    assert result.returncode==0,str(OUT/(name+'.log'))
    return result.stdout.decode('utf-8',errors='replace')
parser=argparse.ArgumentParser();parser.add_argument('baseline',type=Path);args=parser.parse_args()
baseline_path=args.baseline.resolve();baseline=load(baseline_path)
assert baseline['probe']['failures']==0 and baseline['probe']['baselineOriginalZicaiSilenceGapsDemonstrated']
assert baseline['probe']['originalContractGaps']==2 and len(baseline['probe']['damageRows'])==1 and baseline['probe']['clientSawFixture']
assert baseline['probe']['naturalPlayerPostCallbacks']>=220
assert baseline['terminal']['exitCode']==0 and baseline['terminal']['integratedWorldCompleteSave'] and baseline['terminal']['clientProcessReaped'] and baseline['terminal']['inputHashesUnchanged']
for name,wanted in baseline['inputHashes'].items():assert sha(name)==wanted,name
owner=load(ROOT/'WRITE_OWNER.json')
assert owner['formalInstancesReadOnly'] and owner['noFurtherFormalWritesOrDeployment'] and owner['noNewWeaponRuns'] and owner['weaponValidationPaused'] and owner['githubSourceRemoteRecoveryVerified']
assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f'
query="Get-CimInstance Win32_Process | Where-Object { $_.Name -in @('java.exe','javaw.exe') -and $_.CommandLine -match 'session-boss|session-lease' } | Select-Object -ExpandProperty ProcessId"
assert not subprocess.check_output(['powershell','-NoProfile','-Command',query],encoding='utf-8-sig').strip()
base=WORK/'releases/boss-core-stage27-erosion-damage-20261113/ftboss-boss-core-1.21.1-stage27-erosion-damage.jar'
formal=Path('C:/Users/qyby/AppData/Roaming/.minecraft/versions/FarmingTales_NeoForge 机械动力/mods/qys-bosses-1.21.1.jar')
original=ROOT/'reference/forge-current.jar'
assert sha(base)==sha(formal)=='6563f431dca1871eb7870653b74550ba2f5c0f66c104eea90ce58a99ca30f7c0'
assert sha(original)=='c1734a148606397f8b2c79fbcc5c45961a5ffb1923ea051756fd45d03dc7a942'
assert not OUT.exists();OUT.mkdir(parents=True);classes=OUT/'classes';classes.mkdir();proof_classes=OUT/'proof-classes';proof_classes.mkdir()
source=WORK/'tests/ZicaiCurioSilenceComposerV1.java';context_path=WORK/'build/boss-flight-ban-v1/compile-context.json'
sdk=[]
for entry in load(context_path)['classpath'].split(';'):
    path=Path(entry)
    if path.suffix.lower()!='.jar':continue
    with zipfile.ZipFile(path) as bundle:
        if 'net/farmingtales/ftboss/FTBossMod.class' in bundle.namelist():continue
    sdk.append(str(path))
classpath=str(base)+';'+';'.join(sdk)
run('compile-composer',[str(JAVA/'javac.exe'),'-proc:none','--release','21','-encoding','UTF-8','-cp',classpath,'-d',str(proof_classes),str(source)])
proof=run('original-port-proof',[str(JAVA/'java.exe'),'-Xmx256m','-cp',str(proof_classes)+';'+classpath,'ZicaiCurioSilenceComposerV1',str(base),str(original),str(classes)])
assert proof.startswith('failures=0;addedClasses=3;originalHelperMappedGraphs=') and 'negativeControls=4;' in proof and 'codecBridges=2;' in proof
changed={path.relative_to(classes).as_posix():path.read_bytes() for path in classes.rglob('*.class')}
expected={'net/farmingtales/ftboss/boss/ai/ZicaiCurioSilence.class','net/farmingtales/ftboss/boss/ai/ZicaiCurioSilence$Choice.class','net/farmingtales/ftboss/util/ZicaiCurioSilenceRules.class'}
assert set(changed)==expected
jar=OUT/'ftboss-boss-core-1.21.1-stage28-zicai-curio-silence-NOT-INSTALLABLE.jar'
with zipfile.ZipFile(base) as before,zipfile.ZipFile(jar,'x',compression=zipfile.ZIP_DEFLATED) as after:
    assert not expected.intersection(before.namelist())
    for info in before.infolist():after.writestr(info,before.read(info.filename))
    for name,data in sorted(changed.items()):after.writestr(name,data)
with zipfile.ZipFile(base) as before,zipfile.ZipFile(jar) as after:
    assert after.testzip() is None and set(after.namelist())-set(before.namelist())==expected
    assert all(before.read(name)==after.read(name) for name in before.namelist())
    class_count=sum(name.endswith('.class') for name in after.namelist());assert class_count==1454
negative_classes=OUT/'double-patch-rejected';negative_classes.mkdir()
result=subprocess.run([str(JAVA/'java.exe'),'-Xmx256m','-cp',str(proof_classes)+';'+classpath,'ZicaiCurioSilenceComposerV1',str(jar),str(original),str(negative_classes)],capture_output=True,creationflags=subprocess.CREATE_NO_WINDOW)
(OUT/'double-patch-rejected.log').write_bytes(result.stdout+result.stderr)
assert result.returncode!=0 and b'DoublePatchOrUnexpectedBase' in result.stderr and not list(negative_classes.rglob('*.class'))
save('double-patch-rejected-terminal.json',{'exitCode':result.returncode,'expectedRejection':True,'noClassesWritten':True})
inputs={str(path):sha(path) for path in [Path(__file__),source,base,original,formal,baseline_path,context_path,WORK/'build/boss-zicai-curio-neoforge-abi-v2-20261113/audit-receipt.json',WORK/'build/boss-zicai-curio-adapter-design-v1-20261113/design-receipt.json']}
save('candidate-build-receipt.json',{'jar':str(jar),'jarSha256':sha(jar),'baseJar':str(base),'baseJarSha256':sha(base),'addedEntries':sorted(expected),'changedExistingEntries':[],'allExistingEntriesByteIdentical':True,'existingClassesByteIdentical':1451,'classCount':class_count,'proof':proof.strip(),'doublePatchRejected':True,'inputHashes':inputs,'scope':'Complete original event-helper bytecode,original pure rules andChoice;only bounded NeoForge/Curios/NBT platform adapters. Native candidate acceptance still required;no claim of full lifecycle/full Boss acceptance.','candidateNativeAccepted':False,'installable':False,'deployed':False,'formalInstancesReadOnly':True,'weaponsPaused':True,'fullBossMigrationComplete':False})
assert sha(formal)==sha(base)
print(json.dumps({'jar':str(jar),'sha256':sha(jar),'existingClassesUnchanged':1451,'addedClasses':3,'proof':proof.strip(),'nativeAcceptanceRequired':True}),flush=True)
