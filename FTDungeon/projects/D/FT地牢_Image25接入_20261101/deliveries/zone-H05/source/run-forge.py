import argparse
import os
import subprocess
from datetime import datetime
from pathlib import Path

root = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser(description='H05 isolated Forge GameTest; all worlds stay in native-test/runs.')
parser.add_argument('--run', default='h05-' + datetime.now().strftime('%Y%m%d-%H%M%S'))
arguments = parser.parse_args()
if not arguments.run or any(character not in 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_' for character in arguments.run):
    parser.error('Run name may contain letters, numbers, hyphens and underscores only.')
working = root / 'native-test'
if (working / 'runs' / arguments.run).exists():
    parser.error('Use a new run name to preserve prior test-world evidence.')
java_home = Path('C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/runtime/java-runtime-gamma-snapshot')
gradle = next((Path.home() / '.gradle/wrapper/dists/gradle-8.8-bin').glob('*/gradle-8.8/bin/gradle.bat'))
environment = os.environ.copy()
environment['JAVA_HOME'] = str(java_home)
log = root / 'qa' / ('forge-' + arguments.run + '.log')
with log.open('w', encoding='utf-8') as output:
    result = subprocess.run([str(gradle), '--offline', '--no-daemon', '--console=plain', '--max-workers=2', 'runGameTestServer', '-PqaRun=' + arguments.run], cwd=working, env=environment, stdout=output, stderr=subprocess.STDOUT, creationflags=subprocess.CREATE_NO_WINDOW)
print('Forge exit=' + str(result.returncode) + '; log=' + str(log), flush=True)
raise SystemExit(result.returncode)
