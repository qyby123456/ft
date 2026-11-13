import hashlib
import json
import subprocess
from pathlib import Path
root=Path(__file__).resolve().parents[3]
work=root/'work/session-lease'
candidate=work/'reports/native-repair-ranged-use-candidate-99c65f41.json'
receipt=json.loads(candidate.read_text(encoding='utf-8'))
arguments=json.loads((Path(receipt['attempt'])/'complete-arguments316.json').read_text(encoding='utf-8'))
history=Path('D:/ft工程文件/FTBoss-NeoForge-1.21.1-移植-20261022')
import sys
sys.path.insert(0,str(history/'batches/263-native-recipe-menu-accounting/tools'))
from sdk_relocation263 import relocate_text
checks=work/'build/retired-food-closure-99c65f41';checks.mkdir()
source=work/'tests/retired-food-closure/RetiredFoodClosure.java'
classpath=str(Path(receipt['attempt'])/'complete-classes')+';'+relocate_text(arguments[arguments.index('-classpath')+1])
def run(command,name):
    with (checks/(name+'.log')).open('wb') as stream:result=subprocess.run(command,cwd=root,stdout=stream,stderr=subprocess.STDOUT)
    (checks/(name+'-terminal.json')).write_text(json.dumps({'exitCode':result.returncode}),encoding='utf-8')
    assert result.returncode==0,str(checks/(name+'.log'))
run(['C:/Program Files/Zulu/zulu-21/bin/javac.exe','-proc:none','--release','21','-encoding','UTF-8','-cp',classpath,'-d',str(checks),str(source)],'compile')
output=work/'reports/native-retired-food-bytecode-99c65f41.json'
run(['C:/Program Files/Zulu/zulu-21/bin/java.exe','-Xverify:all','-cp',str(checks)+';'+classpath,'RetiredFoodClosure',str(Path(receipt['attempt'])/'complete-classes'),str(output)],'check')
print(json.dumps(json.loads(output.read_text(encoding='utf-8'))))
