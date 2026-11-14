import hashlib
import json
import subprocess
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'build/boss-manor-death-cancel-boundary-v1-20261114'
JAVA=Path('C:/Program Files/Zulu/zulu-21/bin')
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(name,value):(OUT/name).write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def run(name,command):
    result=subprocess.run(command,capture_output=True,timeout=120,creationflags=subprocess.CREATE_NO_WINDOW);(OUT/(name+'.txt')).write_text((result.stdout+result.stderr).decode('utf-8',errors='replace'),encoding='utf-8');save(name+'-terminal.json',{'exitCode':result.returncode});assert result.returncode==0,str(OUT/(name+'.txt'));return result.stdout.decode('utf-8')
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f' and not owner['activeRuntimePids'] and owner['weaponValidationPaused'] and owner['noFurtherFormalWritesOrDeployment'] and owner['modelResourceUploadProhibited'] and owner['musicUploadProhibited']
prior_path=WORK/'reports/shadow-sigil-render-closeout-20261114.json';prior=load(prior_path);candidate=Path(prior['candidate']);assert sha(candidate)==prior['candidateJarSha256']=='95e29f9f6986d58d4f55238776088d82e7782b0e0ece5c2d2f72e25f98ba4215'
for path,digest in prior['formalExpectedHashesVerified'].items():assert sha(path)==digest
original=ROOT/'reference/forge-current.jar';assert sha(original)=='c1734a148606397f8b2c79fbcc5c45961a5ffb1923ea051756fd45d03dc7a942'
actual_dir=WORK/'build/shadow-sigil-actual-native-v2-085dc9c7/captured-defined-classes';actual=actual_dir/'net_farmingtales_ftboss_entity_EasterBossEntity.class';living=actual_dir/'net_minecraft_world_entity_LivingEntity.class';forge_mods=Path('C:/Users/qyby/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/mods');forge_candidates=[path for path in forge_mods.glob('*.jar') if 'cataclysm' in path.name.lower()];neo_mods=WORK/'build/earned-finale-attribute-baseline-native-v2-4e7fd7f1/game/mods';neo_candidates=[path for path in neo_mods.glob('*.jar') if 'cataclysm' in path.name.lower()];assert len(forge_candidates)==len(neo_candidates)==1
source=WORK/'tests/ManorDeathCancelBoundaryProofV1.java';context_path=WORK/'build/boss-manor-damage-context-v1-20261114/compile-proof-command.json';context=load(context_path);cp=str(candidate)+';'+context[context.index('-cp')+1];inputs={str(path):sha(path) for path in [Path(__file__),source,prior_path,candidate,original,actual,living,forge_candidates[0],neo_candidates[0],context_path]}
assert not OUT.exists();OUT.mkdir();classes=OUT/'audit-classes';classes.mkdir();run('compile',[str(JAVA/'javac.exe'),'-J-Dfile.encoding=UTF-8','-J-Duser.language=en','-proc:none','--release','21','-encoding','UTF-8','-cp',cp,'-d',str(classes),str(source)])
proof=json.loads(run('boundary',[str(JAVA/'java.exe'),'-Dfile.encoding=UTF-8','-Xmx384m','-cp',str(classes)+';'+cp,'ManorDeathCancelBoundaryProofV1',str(original),str(candidate),str(actual),str(living),str(forge_candidates[0]),str(neo_candidates[0]),str(OUT/'boundary-proof.json')]))
assert proof['nativeDeadFlagFollowsUncanceledDeathEvent'] and proof['negativeControls']==2 and all(sha(path)==digest for path,digest in inputs.items())
save('receipt.json',{'candidate':str(candidate),'candidateJarSha256':sha(candidate),'boundary':proof,'inputHashes':inputs,'formalExpectedHashesVerified':prior['formalExpectedHashesVerified'],'originalAndCurrentBytecodeEvidenceOnly':True,'runtimeProviderAcceptanceClaimed':False,'productChanged':False,'modelOrMusicUploaded':False,'fullBossMigrationComplete':False,'installable':False,'deployed':False});print(json.dumps({'checks':proof['checks'],'negativeControls':2,'originalAndCurrentScriptedDiscardPairs':2,'baseManorGraph':proof['actualManorHurtGraph'],'baseDieGraph':proof['actualDieOverrideGraph'],'bothInstalledVersionsHaveSameRebirthContract':True,'productChanged':False}))
