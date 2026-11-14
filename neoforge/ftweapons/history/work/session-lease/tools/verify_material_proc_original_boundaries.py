import hashlib
import json
import subprocess
import sys
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-lease'
sys.path.insert(0,'D:/ft工程文件/FTBoss-NeoForge-1.21.1-移植-20261022/batches/263-native-recipe-menu-accounting/tools')
from sdk_relocation263 import relocate_text
def load(path):return json.loads(path.read_text(encoding='utf-8-sig'))
def digest(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
candidate=WORK/'reports/native-repair-material-proc-integration-candidate-cf217105.json';bound=load(candidate)
for name,wanted in bound['inputHashes'].items():assert digest(name)==wanted,name
build=Path(bound['attempt']);arguments=load(build/'complete-arguments316.json')
output=WORK/'build/material-proc-original-boundary-cf217105';output.mkdir()
classes=output/'classes';classes.mkdir()
source=WORK/'tests/MaterialProcOriginalBoundaryProof.java'
classpath=str(build/'complete-classes')+';'+relocate_text(arguments[arguments.index('-classpath')+1])
commands=[('compile',['C:/Program Files/Zulu/zulu-21/bin/javac.exe','-proc:none','-implicit:none','--release','21','-encoding','UTF-8','-cp',classpath,'-d',str(classes),str(source)]),('boundary',['C:/Program Files/Zulu/zulu-21/bin/java.exe','-Xverify:all','-Xmx512m','-cp',str(classes)+';'+classpath,'net.farmingtales.ftboss.item.MaterialProcOriginalBoundaryProof',str(WORK/'build/native-repair-5f283607/complete-classes'),str(build/'complete-classes')])]
for name,command in commands:
    (output/(name+'-command.json')).write_text(json.dumps(command,ensure_ascii=False,indent=2),encoding='utf-8')
    with (output/(name+'.log')).open('xb') as stream:result=subprocess.run(command,cwd=ROOT,stdout=stream,stderr=subprocess.STDOUT)
    (output/(name+'-terminal.json')).write_text(json.dumps({'exitCode':result.returncode}),encoding='utf-8')
    assert result.returncode==0,str(output/(name+'.log'))
for name,wanted in bound['inputHashes'].items():assert digest(name)==wanted,name
print((output/'boundary.log').read_text(encoding='utf-8'),flush=True)
