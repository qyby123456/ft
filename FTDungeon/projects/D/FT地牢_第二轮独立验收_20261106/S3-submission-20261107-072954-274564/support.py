import json
import os
from pathlib import Path
import subprocess
import sys
ROOT=Path(__file__).resolve().parent
mapped='D:/ft工程文件/锈铜宝库地牢_20261101/qa/vanilla-1.20.1-mapped.jar'
source=Path('D:/ft工程文件/霜雪花园地牢_20261101/build/classpath/runData_minecraftClasspath.txt')
classpath=os.pathsep.join([str(ROOT/'classes'),mapped,*[line for line in source.read_text(encoding='utf-8').splitlines() if line.strip() and 'minecraft_user_repo' not in line]])
(ROOT/'classes').mkdir(exist_ok=True)
commands=[]
if not (ROOT/'classes/SupportAudit.class').exists() or (ROOT/'SupportAudit.java').stat().st_mtime>(ROOT/'classes/SupportAudit.class').stat().st_mtime:
    commands.append(['javac','-encoding','UTF-8','-cp',classpath,'-d',str(ROOT/'classes'),str(ROOT/'SupportAudit.java')])
room=ROOT/'rooms'/sys.argv[1]
commands.append(['java','-Xmx1536m','-Djava.awt.headless=true','-cp',classpath,'SupportAudit',str(room)])
for command in commands:
    result=subprocess.run(command,cwd=room/'qa',capture_output=True,text=True,encoding='utf-8',errors='replace',timeout=240,creationflags=subprocess.CREATE_NO_WINDOW)
    (room/'qa'/(command[0]+'-support.log')).write_text(result.stdout+'\n'+result.stderr,encoding='utf-8')
    if result.returncode:
        print(result.stderr[-4000:]);sys.exit(result.returncode)
(room/'qa/support-commands.json').write_text(json.dumps(commands,indent=2),encoding='utf-8')
print('All-block support / ports / actual NBT inventory:',sys.argv[1],'PASS')
