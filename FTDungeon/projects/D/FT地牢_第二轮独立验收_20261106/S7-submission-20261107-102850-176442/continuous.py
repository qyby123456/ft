import subprocess
from pathlib import Path

root = Path(__file__).resolve().parent
classes = root / 'audit-classes'
classes.mkdir(exist_ok=True)
dependencies = Path(r'D:\ft工程文件\霜雪花园地牢_20261101\build\classpath\runData_minecraftClasspath.txt').read_text(encoding='utf-8').splitlines()
dependencies = [entry for entry in dependencies if entry and 'minecraft_user_repo' not in entry]
classpath = ';'.join([str(classes), r'D:\ft工程文件\锈铜宝库地牢_20261101\qa\vanilla-1.20.1-mapped.jar'] + dependencies)
java = Path(r'C:\Program Files\Common Files\Oracle\Java\javapath')
commands = [[str(java / 'javac.exe'), '-encoding', 'UTF-8', '-cp', classpath, '-d', str(classes), str(root / 'ContinuousConnectorAudit.java')], [str(java / 'java.exe'), '-Xmx1024m', '-Djava.awt.headless=true', '-cp', classpath, 'ContinuousConnectorAudit', str(root)]]
for command in commands:
    result = subprocess.run(command, capture_output=True, text=True, encoding='utf-8', errors='replace', timeout=180)
    with (root / 'assembly/qa/continuous-width.log').open('a', encoding='utf-8') as stream:
        stream.write(result.stdout + result.stderr)
    print(result.stdout[-1500:], flush=True)
    if result.returncode:
        print(result.stderr[-1500:], flush=True)
        raise SystemExit(result.returncode)
