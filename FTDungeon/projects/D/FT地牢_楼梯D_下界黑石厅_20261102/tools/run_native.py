import os
import subprocess
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
JAVA = Path('C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/runtime/java-runtime-gamma-snapshot')


def main():
    gradle = next((Path.home() / '.gradle/wrapper/dists/gradle-8.8-bin').glob('*/gradle-8.8/bin/gradle.bat'))
    environment = os.environ.copy()
    environment['JAVA_HOME'] = str(JAVA)
    environment['PATH'] = str(JAVA / 'bin') + os.pathsep + environment.get('PATH', '')
    log = ROOT / 'qa/forge-gametest.log'
    command = [str(gradle), 'runGameTestServer', '--console=plain', '--offline', '-Dnet.minecraftforge.gradle.check.certs=false']
    with log.open('wb') as stream:
        result = subprocess.run(command, cwd=ROOT, env=environment, stdout=stream, stderr=subprocess.STDOUT, creationflags=subprocess.CREATE_NO_WINDOW)
    print(log.read_text(encoding='utf-8', errors='replace')[-7000:])
    raise SystemExit(result.returncode)


if __name__ == '__main__':
    main()
