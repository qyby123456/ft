import hashlib
import json
import subprocess
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'reports/manor-original-sharing-budget-proof-v2-20261114'
JAVA=Path('C:/Program Files/Zulu/zulu-21/bin')
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(name,value):(OUT/name).write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def run(name,command):
    result=subprocess.run(command,capture_output=True,timeout=120,creationflags=subprocess.CREATE_NO_WINDOW);(OUT/(name+'.txt')).write_bytes(result.stdout+result.stderr);save(name+'-terminal.json',{'exitCode':result.returncode});assert result.returncode==0,str(OUT/(name+'.txt'));return result.stdout.decode('utf-8').strip()
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f' and owner['weaponValidationPaused'] and owner['noFurtherFormalWritesOrDeployment'] and owner['modelResourceUploadProhibited'] and owner['musicUploadProhibited']
candidate=Path(owner['latestScopedBossCandidate']);assert sha(candidate)=='d691bdb1a2b5f540d9bcd8149e6d04baa5c2e1df1a6c01578b4b32b75cfdb69c';original=ROOT/'reference/forge-current.jar';assert sha(original)=='c1734a148606397f8b2c79fbcc5c45961a5ffb1923ea051756fd45d03dc7a942'
source=WORK/'tests/ManorOriginalSharingBudgetProofV2.java';prior=load(WORK/'build/boss-manor-damage-context-v1-20261114/compile-proof-command.json');cp=str(candidate)+';'+prior[prior.index('-cp')+1];inputs={str(path):sha(path) for path in [Path(__file__),candidate,original,source]}
assert not OUT.exists();OUT.mkdir();classes=OUT/'proof-classes';classes.mkdir();run('compile',[str(JAVA/'javac.exe'),'-proc:none','--release','21','-encoding','UTF-8','-cp',cp,'-d',str(classes),str(source)]);proof=run('original-contract',[str(JAVA/'java.exe'),'-Xmx256m','-cp',str(classes)+';'+cp,'ManorOriginalSharingBudgetProofV2',str(original),str(candidate),str(OUT/'proof.json')]);assert inputs=={path:sha(path) for path in inputs};save('receipt.json',{'proof':proof,'inputHashes':inputs,'allFrozenInputsUnchanged':True,'productChanged':False,'gameplayMethodsInvoked':False,'pureRulesOnlyInvoked':True,'modelOrMusicUploaded':False,'fullBossMigrationComplete':False,'installable':False,'deployed':False});print(proof,flush=True)
