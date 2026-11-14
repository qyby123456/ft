import hashlib
import json
import subprocess
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'reports/manor-legitimate-ingress-mapped-audit-v1-20261114'
JAVA=Path('C:/Program Files/Zulu/zulu-21/bin')
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(name,value):(OUT/name).write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def run(name,command):
    result=subprocess.run(command,capture_output=True,timeout=120,creationflags=subprocess.CREATE_NO_WINDOW);(OUT/(name+'.txt')).write_bytes(result.stdout+result.stderr);save(name+'-terminal.json',{'exitCode':result.returncode});assert result.returncode==0,str(OUT/(name+'.txt'));return result.stdout.decode('utf-8').strip()
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f' and owner['weaponValidationPaused'] and owner['noFurtherFormalWritesOrDeployment'] and owner['modelResourceUploadProhibited'] and owner['musicUploadProhibited']
assert not OUT.exists();OUT.mkdir();classes=OUT/'audit-classes';classes.mkdir();candidate=Path(owner['latestScopedBossCandidate']);assert sha(candidate)=='a49f1772a60d72a4240eb3f293b29fb38629a2112ad326120e81377eeeeca1ec';original=ROOT/'reference/forge-current.jar';assert sha(original)=='c1734a148606397f8b2c79fbcc5c45961a5ffb1923ea051756fd45d03dc7a942'
mapping=WORK/'build/boss-manor-legitimate-ingress-platform-audit-v1-20261114/mapping-proof.json';source=WORK/'tests/ManorLegitimateIngressMappedAuditV1.java';prior=load(WORK/'build/boss-manor-damage-context-v1-20261114/compile-proof-command.json');cp=str(candidate)+';'+prior[prior.index('-cp')+1]
inputs={str(path):sha(path) for path in [Path(__file__),mapping,source,candidate,original]};save('inputs-before.json',inputs)
run('compile',[str(JAVA/'javac.exe'),'-proc:none','--release','21','-encoding','UTF-8','-cp',cp,'-d',str(classes),str(source)]);proof=run('audit',[str(JAVA/'java.exe'),'-Xmx512m','-cp',str(classes)+';'+cp,'ManorLegitimateIngressMappedAuditV1',str(original),str(candidate),str(mapping),str(OUT/'method-comparison.json')]);assert inputs=={path:sha(Path(path)) for path in inputs};save('receipt.json',{'proof':proof,'inputHashes':inputs,'onlyOfficialMemberAndPlatformPackageTypeMapping':True,'productChanged':False,'authenticated':False,'modelOrMusicUploaded':False,'fullBossMigrationComplete':False,'installable':False,'deployed':False});print(proof,flush=True)
