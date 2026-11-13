import argparse
import os
import shutil
import subprocess
from datetime import datetime, timedelta, timezone
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
DEFAULT_JAVA = Path('C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/runtime/java-runtime-gamma-snapshot')


def main():
    parser = argparse.ArgumentParser(description='Run the isolated headless Forge GameTest only; never launches a player client or opens a production save.')
    parser.add_argument('--java-home', type=Path, default=DEFAULT_JAVA)
    parser.add_argument('--gradle', type=Path)
    args = parser.parse_args()
    gradle = args.gradle
    if gradle is None:
        candidates = sorted((Path.home() / '.gradle/wrapper/dists/gradle-8.8-bin').glob('*/gradle-8.8/bin/gradle.bat'))
        if not candidates:
            parser.error('Pass --gradle with a Gradle 8.8 launcher path.')
        gradle = candidates[0]
    if not (args.java_home / 'bin/java.exe').is_file() or not gradle.is_file():
        parser.error('Java home and Gradle launcher must exist. Java 17 is required.')
    environment = os.environ.copy()
    environment['JAVA_HOME'] = str(args.java_home)
    environment['PATH'] = str(args.java_home / 'bin') + os.pathsep + environment.get('PATH', '')
    stamp = datetime.now(timezone(timedelta(hours=8))).strftime('%Y%m%d-%H%M%S')
    log = ROOT / 'qa/forge-gametest.log'
    report = ROOT / 'qa/native.json'
    prior = sorted((ROOT / 'qa').glob('native-placed-*.nbt'))
    for path in prior:
        shutil.copy2(path, path.with_name(f'previous-{stamp}-{path.name}'))
    for path in (log, report):
        if path.exists():
            shutil.copy2(path, path.with_name(f'previous-{stamp}-{path.name}'))
    command = [str(gradle), 'runGameTestServer', '--console=plain', '--offline', '-Dnet.minecraftforge.gradle.check.certs=false']
    with log.open('wb') as stream:
        result = subprocess.run(command, cwd=ROOT, env=environment, stdout=stream, stderr=subprocess.STDOUT, creationflags=subprocess.CREATE_NO_WINDOW)
    print(log.read_text(encoding='utf-8', errors='replace')[-5000:])
    raise SystemExit(result.returncode)


if __name__ == '__main__':
    main()
