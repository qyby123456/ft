import os
from pathlib import Path
import subprocess
import sys

root = Path(__file__).resolve().parent
mapped = Path('D:/ft工程文件/锈铜宝库地牢_20261101/qa/vanilla-1.20.1-mapped.jar')
dependency_file = Path('D:/ft工程文件/霜雪花园地牢_20261101/build/classpath/runData_minecraftClasspath.txt')
classpath = os.pathsep.join([str(root / 'classes'), str(mapped), *[line for line in dependency_file.read_text(encoding='utf-8').splitlines() if line.strip() and 'minecraft_user_repo' not in line]])
subprocess.run(['javac', '-encoding', 'UTF-8', '-cp', classpath, '-d', str(root / 'classes'), str(root / 'CaptureAudit.java')], check=True)
folder = root / (sys.argv[1] if len(sys.argv) > 1 else 'worldqa-r2')
with (folder / 'capture-audit.log').open('w', encoding='utf-8') as output:
    subprocess.run(['java', '-Xmx1536m', '-cp', classpath, 'CaptureAudit', str(folder)], check=True, stdout=output, stderr=subprocess.STDOUT, timeout=240)
