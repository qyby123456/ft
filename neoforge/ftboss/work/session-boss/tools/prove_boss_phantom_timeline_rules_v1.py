import hashlib
import json
from pathlib import Path
import subprocess
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'build/boss-phantom-timeline-rules-v1'
JAVA=Path('C:/Program Files/Zulu/zulu-21/bin')
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
OUT.mkdir()
source=WORK/'tests/PhantomTimelineRulesProofV1.java'
built=json.loads((WORK/'build/boss-manor-body-chain-v5/candidate-build-receipt.json').read_text('utf-8'))
current=Path(built['jar']);original=ROOT/'reference/forge-current.jar'
assert sha(current)==built['jarSha256']=='4faa8fcb3e0746bfed8176a2c51f0e2e3a0fd13ec7592cd199e016399c203bb0'
assert sha(original)=='c1734a148606397f8b2c79fbcc5c45961a5ffb1923ea051756fd45d03dc7a942'
commands=[]
command=[str(JAVA/'javac.exe'),'-proc:none','--release','21','-cp',str(current),'-d',str(OUT),str(source)]
commands.append(command)
result=subprocess.run(command,cwd=ROOT,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,creationflags=subprocess.CREATE_NO_WINDOW)
(OUT/'compile.log').write_bytes(result.stdout)
assert result.returncode==0
proofs={}
for label,jar in [('original',original),('deployed',current)]:
    command=[str(JAVA/'java.exe'),'-Xmx128m','-cp',str(OUT)+';'+str(jar),'PhantomTimelineRulesProofV1'];commands.append(command)
    result=subprocess.run(command,cwd=ROOT,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,creationflags=subprocess.CREATE_NO_WINDOW)
    (OUT/(label+'.log')).write_bytes(result.stdout)
    assert result.returncode==0,result.stdout
    proofs[label]=json.loads(result.stdout.decode('utf-8'))
assert proofs['original']==proofs['deployed'] and proofs['deployed']['checks']==26
receipt={'failures':0,'proofs':proofs,'commands':commands,'inputHashes':{str(path):sha(path) for path in [Path(__file__),source,current,original]},'naturalEntitySchedulingClaim':False,'scope':'Original and deployed public pure delay bounds,due boundary,stable same-time/insertion order,overdue drain and exactly once controls;separate from native natural timeline acceptance.'}
with (OUT/'proof-receipt.json').open('x',encoding='utf-8') as stream:json.dump(receipt,stream,ensure_ascii=False,indent=2)
print(json.dumps(proofs))
