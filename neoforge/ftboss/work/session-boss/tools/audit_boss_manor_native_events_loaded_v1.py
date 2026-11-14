import hashlib
import json
import subprocess
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'reports/manor-native-events-loaded-audit-v1-20261114'
JAVA=Path('C:/Program Files/Zulu/zulu-21/bin')
def load(path):return json.loads(path.read_text(encoding='utf-8-sig'))
def sha(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def save(name,data):(OUT/name).write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def run(name,command):
    result=subprocess.run(command,capture_output=True,timeout=90,creationflags=subprocess.CREATE_NO_WINDOW)
    (OUT/(name+'.txt')).write_bytes(result.stdout+result.stderr)
    save(name+'-terminal.json',{'exitCode':result.returncode})
    assert result.returncode==0,str(OUT/(name+'.txt'))
    return result.stdout.decode('utf-8').strip()
owner=load(ROOT/'WRITE_OWNER.json')
assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f' and not owner['activeRuntimePids']
assert owner['weaponValidationPaused'] and owner['noFurtherFormalWritesOrDeployment'] and owner['modelResourceUploadProhibited'] and owner['musicUploadProhibited']
assert not OUT.exists();OUT.mkdir();classes=OUT/'audit-classes';classes.mkdir()
regression_path=WORK/'reports/sparrow-native-events-regression-v1-45d4271b.json';regression=load(regression_path)
matrix_path=WORK/'reports/manor-native-damage-events-native-v2-bccaec83.json';matrix=load(matrix_path)
assert regression['actualOriginalSparrowWithNativeEventsRegressionAccepted'] and matrix['actualNativeManorDamageEventMatrixAccepted']
for report in [regression,matrix]:
    assert report['terminal']['clientProcessReaped'] and report['terminal']['inputHashesUnchanged'] and report['terminal']['integratedWorldCompleteSave']
    for name,expected in report['inputHashes'].items():assert sha(Path(name))==expected,name
actual=Path(regression['attempt'])/'captured-defined-classes';prior=Path(matrix['attempt'])/'captured-defined-classes'
built_path=WORK/'build/boss-manor-native-events-v3-20261114/candidate-build-receipt.json';built=load(built_path);jar=Path(built['jar']);assert sha(jar)=='e63617ebd89d5e78055fef649db3392304a678abcafa4d74996a025d93f79c6c'
prefix='net/farmingtales/ftboss/'
names=[prefix+name for name in ['entity/EasterBossEntity','boss/ai/ManorKiting','boss/ai/ManorFinalDamage','boss/HiddenBossIntro','boss/MeatBoss','boss/ai/NativeManorDamageBridge','boss/ai/NativeManorDamageBridge$Frame','boss/ai/NativeManorDamageScopeAccess','boss/ai/NativeManorDamageBridgeInstaller','event/NativeManorDamageEventFactory','boss/ai/NativeManorFinalDamageInstaller']]+['net/minecraft/world/entity/LivingEntity']
captured={name:actual/(name.replace('/','_')+'.class') for name in names};assert len(captured)==12 and all(path.is_file() for path in captured.values())
rows=[]
with zipfile.ZipFile(jar) as archive:
    for name,path in captured.items():
        row={'class':name,'actualPath':str(path),'actualSha256':sha(path),'bytes':path.stat().st_size}
        if name in names[5:11]:
            row['candidateSha256']=hashlib.sha256(archive.read(name+'.class')).hexdigest();assert row['actualSha256']==row['candidateSha256']
        prior_path=prior/path.name
        if name in names[:3]:
            row['matrixActualPath']=str(prior_path);row['matrixActualSha256']=sha(prior_path);assert row['actualSha256']==row['matrixActualSha256']
        rows.append(row)
args=load(WORK/'build/boss-manor-damage-context-v1-20261114/compile-proof-command.json');cp=str(jar)+';'+args[args.index('-cp')+1]
source=WORK/'tests/ManorNativeDamageLoadedAuditV1.java';baseline=WORK/'build/sparrow-hit-evasion-native-v4-ba1eddca/captured-defined-classes/net_farmingtales_ftboss_entity_EasterBossEntity.class'
run('compile',[str(JAVA/'javac.exe'),'-proc:none','--release','21','-encoding','UTF-8','-cp',cp,'-d',str(classes),str(source)])
command=[str(JAVA/'java.exe'),'-Xmx256m','-cp',str(classes)+';'+cp,'ManorNativeDamageLoadedAuditV1',str(baseline),str(captured[names[0]]),str(captured[names[2]])]
save('audit-command.json',command);proof=run('audit',command);assert proof.startswith('failures=0;') and 'originalMethodsExactlyReconstructed=341;' in proof
receipt={'proof':proof,'candidateJarSha256':sha(jar),'actualCapturedClasses':rows,'allTwelveSelectedActualDefinitionsCaptured':True,'sixNewHelperDefinitionsExactlyMatchCandidate':True,'threeRelevantBossDefinitionsExactlyMatchAcceptedMatrix':True,'livingEntityCrossJvmByteEqualityClaimed':False,'inputHashes':{str(path):sha(path) for path in [Path(__file__),source,baseline,built_path,regression_path,matrix_path]},'allRuntimeFrozenInputHashesRechecked':True,'resourceBytesExported':False,'modelOrMusicUploaded':False,'fullBossMigrationComplete':False,'installable':False,'deployed':False}
save('receipt.json',receipt);print(json.dumps({'proof':proof,'capturedClasses':12,'newHelperCandidateMatches':6,'matrixRelevantDefinitionMatches':3}),flush=True)
