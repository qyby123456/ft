import hashlib
import json
import subprocess
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'reports/manor-original-routing-mapped-audit-v1-20261114'
JAVA=Path('C:/Program Files/Zulu/zulu-21/bin')
def load(path):return json.loads(path.read_text(encoding='utf-8-sig'))
def sha(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def save(name,value):(OUT/name).write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def run(name,command):
    result=subprocess.run(command,capture_output=True,timeout=90,creationflags=subprocess.CREATE_NO_WINDOW);(OUT/(name+'.txt')).write_bytes(result.stdout+result.stderr);save(name+'-terminal.json',{'exitCode':result.returncode});assert result.returncode==0,str(OUT/(name+'.txt'));return result.stdout.decode('utf-8').strip()
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f' and owner['weaponValidationPaused'] and owner['noFurtherFormalWritesOrDeployment'] and owner['modelResourceUploadProhibited'] and owner['musicUploadProhibited']
assert not OUT.exists();OUT.mkdir();classes=OUT/'audit-classes';classes.mkdir()
built_path=WORK/'build/boss-manor-native-events-v3-20261114/candidate-build-receipt.json';built=load(built_path);candidate=Path(built['jar']);assert sha(candidate)==built['jarSha256']=='e63617ebd89d5e78055fef649db3392304a678abcafa4d74996a025d93f79c6c'
original=ROOT/'reference/forge-current.jar';assert sha(original)=='c1734a148606397f8b2c79fbcc5c45961a5ffb1923ea051756fd45d03dc7a942'
mapping=WORK/'build/boss-manor-routing-platform-audit-v1-20261114/mapping-proof.json';source=WORK/'tests/ManorOriginalDamageRoutingMappedAuditV1.java';actual=WORK/'build/sparrow-native-events-regression-v1-45d4271b/captured-defined-classes/net_farmingtales_ftboss_entity_EasterBossEntity.class'
prior=load(WORK/'build/boss-manor-damage-context-v1-20261114/compile-proof-command.json');cp=str(candidate)+';'+prior[prior.index('-cp')+1]
run('compile',[str(JAVA/'javac.exe'),'-proc:none','--release','21','-encoding','UTF-8','-cp',cp,'-d',str(classes),str(source)])
command=[str(JAVA/'java.exe'),'-Xmx256m','-cp',str(classes)+';'+cp,'ManorOriginalDamageRoutingMappedAuditV1',str(original),str(mapping),str(actual)];save('audit-command.json',command);output=run('audit',command);proof=output.splitlines()[-1];assert proof.startswith('failures=0;') and 'completeMappedOriginalMethods=6;' in proof
save('receipt.json',{'proof':proof,'candidateJarSha256':sha(candidate),'actualEasterBossSha256':sha(actual),'originalClassSha256':load(mapping)['originalClassHashes'],'usedMemberMappings':len(load(mapping)['memberNames']),'inputHashes':{str(path):sha(path) for path in [Path(__file__),source,mapping,original,actual,built_path,candidate]},'allFiveOriginalSharingMethodsAndCompleteHurtRoutingPreservedAfterThreeProvenStackEquivalences':True,'provenExistingEquivalentStackSites':3,'fiveMethodsAlreadyGraphExactIncludingWholeHurt':True,'noProductChange':True,'authenticated':False,'modelOrMusicUploaded':False,'fullBossMigrationComplete':False,'installable':False,'deployed':False});print(json.dumps({'proof':proof,'actualEasterBossSha256':sha(actual)}),flush=True)
