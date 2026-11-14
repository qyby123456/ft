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
def save(path,value):
    with Path(path).open('x',encoding='utf-8') as stream:json.dump(value,stream,ensure_ascii=False,indent=2)
report=load(WORK/'reports/native-repair-aliased-entity-expected-image-candidate.json')
attempt=Path(report['attempt'])
arguments=load(attempt/'complete-arguments316.json')
output=attempt/'boundary-classes';output.mkdir()
classpath=str(attempt/'complete-classes')+';'+relocate_text(arguments[arguments.index('-classpath')+1])
source=WORK/'tests/AliasedEntityExpectedImageProof.java'
commands=[('boundary-compile',['C:/Program Files/Zulu/zulu-21/bin/javac.exe','-proc:none','--release','21','-encoding','UTF-8','-cp',classpath,'-d',str(output),str(source)]),('boundary-proof',['C:/Program Files/Zulu/zulu-21/bin/java.exe','-Xverify:all','-cp',str(output)+';'+classpath,'net.farmingtales.ftboss.item.AliasedEntityExpectedImageProof',str(WORK/'build/native-repair-8199dcc2/complete-classes'),str(attempt/'complete-classes')])]
for name,command in commands:
    save(attempt/(name+'-command.json'),command)
    with (attempt/(name+'.log')).open('xb') as stream:result=subprocess.run(command,cwd=ROOT,stdout=stream,stderr=subprocess.STDOUT)
    save(attempt/(name+'-terminal.json'),{'exitCode':result.returncode,'productRuntimeStarted':False})
    if result.returncode:raise AssertionError(str(attempt/(name+'.log')))
print((attempt/'boundary-proof.log').read_text(encoding='utf-8'),flush=True)
