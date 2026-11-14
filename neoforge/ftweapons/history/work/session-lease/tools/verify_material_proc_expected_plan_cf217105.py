import hashlib
import json
import subprocess
import sys
from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-lease'
HISTORY=Path('D:/ft工程文件/FTBoss-NeoForge-1.21.1-移植-20261022')
sys.path.insert(0,str(HISTORY/'batches/263-native-recipe-menu-accounting/tools'))
from sdk_relocation263 import relocate_text

def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def digest(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(path,value):
    with path.open('x',encoding='utf-8') as stream:json.dump(value,stream,ensure_ascii=False,indent=2)

candidate=WORK/'reports/native-repair-material-proc-integration-candidate-cf217105.json'
assert digest(candidate)=='78eedf330a3e8da7f3823b1bd3e5566ef7d3f65937f801896c7ba8c41064f586'
bound=load(candidate);build=Path(bound['attempt'])
arguments=load(build/'complete-arguments316.json')
source=WORK/'proposal-material-proc-expected-plan/net/farmingtales/ftboss/item/MaterialProcExpectedPlan.java'
proof=WORK/'tests/MaterialProcExpectedPlanProof.java'
original=Path('D:/ft工程文件/FTBoss-NeoForge-1.21.1-移植-20261022/batches/316-native-semantic-alias-read/build/complete713-semantic-alias316-ad24b62e/source-at-complete/0087/WeaponMaterialPowers.java')
assert digest(original)=='8379fda8eade426c5d942bf75e21b40a2caad6009fbd1fc0ceaa74bf950e2e10'
for name,wanted in bound['inputHashes'].items():assert digest(name)==wanted,name
output=WORK/'build/material-proc-expected-plan-proof-cf217105-20261112';output.mkdir()
classes=output/'classes';classes.mkdir()
classpath=str(build/'complete-classes')+';'+relocate_text(arguments[arguments.index('-classpath')+1])
inputs={str(path):digest(path) for path in [candidate,source,proof,original,Path(__file__)]}
save(output/'inputs-before.json',inputs)
commands=[('compile',['C:/Program Files/Zulu/zulu-21/bin/javac.exe','-proc:none','-implicit:none','--release','21','-encoding','UTF-8','-cp',classpath,'-d',str(classes),str(source),str(proof)]),('oracle',['C:/Program Files/Zulu/zulu-21/bin/java.exe','-Xverify:all','-Xmx512m','-cp',str(classes)+';'+classpath,'net.farmingtales.ftboss.item.MaterialProcExpectedPlanProof'])]
for name,command in commands:
    save(output/(name+'-command.json'),command)
    with (output/(name+'.log')).open('xb') as stream:result=subprocess.run(command,cwd=ROOT,stdout=stream,stderr=subprocess.STDOUT)
    save(output/(name+'-terminal.json'),{'exitCode':result.returncode,'nativeServerStarted':False})
    assert result.returncode==0,str(output/(name+'.log'))
for name,wanted in bound['inputHashes'].items():assert digest(name)==wanted,name
for name,wanted in inputs.items():assert digest(name)==wanted,name
log=(output/'oracle.log').read_text(encoding='utf-8')
result=json.loads(next(line for line in log.splitlines() if line.startswith('{"failures"')))
assert result['failures']==0 and result['oracleCases']>1000 and result['exactIntermediateWrites']>1000 and result['noWriteBranches']>1000
save(WORK/'reports/material-proc-expected-plan-proof-cf217105-20261112.json',{'candidateUnchanged':'cf217105','candidateInputsUnchanged':len(bound['inputHashes']),'oracle':result,'productionGraphChanged':False,'nativeSessionIntegrationAccepted':False,'guardOrComparisonRelaxed':False,'authorityWritten':False,'installable':False,'deployed':False,'output':str(output),'inputHashes':inputs})
print(json.dumps(result),flush=True)
