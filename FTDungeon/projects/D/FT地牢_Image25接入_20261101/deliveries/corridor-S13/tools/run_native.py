import os
import subprocess
from datetime import datetime, timedelta, timezone
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
JAVA = Path('C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/runtime/java-runtime-gamma-snapshot')


def main():
    gradle = sorted((Path.home() / '.gradle/wrapper/dists/gradle-8.8-bin').glob('*/gradle-8.8/bin/gradle.bat'))[0]
    environment = os.environ.copy()
    environment['JAVA_HOME'] = str(JAVA)
    environment['PATH'] = str(JAVA / 'bin') + os.pathsep + environment.get('PATH', '')
    stamp = datetime.now(timezone(timedelta(hours=8))).strftime('%Y%m%d-%H%M%S-%f')
    log = ROOT / 'qa' / ('forge-' + stamp + '.log')
    command = [str(gradle), 'runGameTestServer', '--console=plain', '--offline', '-Dnet.minecraftforge.gradle.check.certs=false', '-PqaRun=' + stamp]
    log.parent.mkdir(parents=True, exist_ok=True)
    print('Isolated Forge log: ' + str(log), flush=True)
    with log.open('wb') as stream:
        result = subprocess.run(command, cwd=ROOT, env=environment, stdout=stream, stderr=subprocess.STDOUT, creationflags=subprocess.CREATE_NO_WINDOW)
    (ROOT / 'qa' / 'latest-forge-log.txt').write_text(str(log), encoding='utf-8')
    print(log.read_text(encoding='utf-8', errors='replace')[-6500:])
    raise SystemExit(result.returncode)


if __name__ == '__main__':
    main()
