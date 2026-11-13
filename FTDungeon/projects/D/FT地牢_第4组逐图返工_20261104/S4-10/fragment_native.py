import os
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parent
dependencies = [line for line in Path('D:/ft工程文件/霜雪花园地牢_20261101/build/classpath/runData_minecraftClasspath.txt').read_text(encoding='utf-8').splitlines() if line.strip() and 'minecraft_user_repo' not in line]
classpath = os.pathsep.join([str(ROOT / 'qa/vanilla-1.20.1-mapped-local-fixture.jar'), *dependencies])
java = Path('C:/Program Files/Common Files/Oracle/Java/javapath')
commands = [('fragment-compile', [str(java / 'javac.exe'), '-encoding', 'UTF-8', '-classpath', classpath, '-d', str(ROOT / 'qa'), str(ROOT / 'qa/FragmentNative.java')]), ('fragment-native', [str(java / 'java.exe'), '-Djava.awt.headless=true', '-classpath', str(ROOT / 'qa') + os.pathsep + classpath, 'FragmentNative', str(ROOT)])]
for label, command in commands:
    result = subprocess.run(command, capture_output=True, text=True, encoding='utf-8', errors='replace', timeout=180, creationflags=subprocess.CREATE_NO_WINDOW)
    (ROOT / f'qa/{label}.log').write_text(result.stdout + result.stderr, encoding='utf-8')
    if result.returncode:
        print(result.stdout + result.stderr)
        raise SystemExit(result.returncode)
print((ROOT / 'qa/fragment-native.json').read_text(encoding='utf-8'))
