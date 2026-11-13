import hashlib
import json
from pathlib import Path
import subprocess
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'build/boss-erosion-strength-proof-v1'
JAVA=Path('C:/Program Files/Zulu/zulu-21/bin')
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
OUT.mkdir();source=WORK/'tests/ErosionStrengthOracleV1.java';original=ROOT/'reference/forge-current.jar';base=WORK/'releases/boss-core-stage25-arcane-lifetime-20261113/ftboss-boss-core-1.21.1-stage25-arcane-lifetime.jar'
assert sha(base)=='7eb3b4c2e4ef8e35c806a4df8b53cb32879a9cec9abee961a963bf558d39d7b6' and sha(original)=='c1734a148606397f8b2c79fbcc5c45961a5ffb1923ea051756fd45d03dc7a942'
commands=[];command=[str(JAVA/'javac.exe'),'-proc:none','--release','21','-cp',str(base),'-d',str(OUT),str(source)];commands.append(command);result=subprocess.run(command,cwd=ROOT,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,creationflags=subprocess.CREATE_NO_WINDOW);(OUT/'compile.log').write_bytes(result.stdout);assert result.returncode==0
outputs={}
for label,jar in [('original',original),('current',base)]:
    command=[str(JAVA/'java.exe'),'-Xmx128m','-cp',str(OUT)+';'+str(jar),'ErosionStrengthOracleV1'];commands.append(command);result=subprocess.run(command,cwd=ROOT,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,creationflags=subprocess.CREATE_NO_WINDOW);(OUT/(label+'.log')).write_bytes(result.stdout);assert result.returncode==0;outputs[label]=result.stdout
assert outputs['original']==outputs['current'] and outputs['current'].startswith(b'checks=205;')
with (OUT/'proof-receipt.json').open('x',encoding='utf-8') as stream:json.dump({'failures':0,'checksEach':205,'originalAndCurrentFloatBitsExactlyEqual':True,'commands':commands,'inputHashes':{str(path):sha(path) for path in [Path(__file__),source,original,base]},'scope':'Unchanged public pure ErosionRules.strength levels,clamps and exact float bits. Actual living effect amplifiers verified separately by native client candidate.'},stream,ensure_ascii=False,indent=2)
print(json.dumps({'checksEach':205,'exactOriginalFloatBits':True}),flush=True)
