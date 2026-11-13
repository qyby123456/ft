import json
import os
import shutil
import subprocess
from datetime import datetime
from pathlib import Path
from zoneinfo import ZoneInfo

from qa_static import ROOT, write_json
from nbtio import AIR, Structure


def main():
    java_home = Path('C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/runtime/java-runtime-gamma-snapshot')
    candidates = sorted((Path.home() / '.gradle/wrapper/dists/gradle-8.8-bin').glob('*/gradle-8.8/bin/gradle.bat'))
    if not candidates or not (java_home / 'bin/java.exe').is_file():
        raise SystemExit('The cached Gradle 8.8 and Java 17 runtimes are required.')
    empty = Structure((1, 1, 1))
    empty.set((0, 0, 0), AIR)
    empty.write(ROOT / 'forge/src/main/resources/data/corridors05qa/structures/empty.nbt')
    environment = os.environ.copy()
    environment['JAVA_HOME'] = str(java_home)
    environment['PATH'] = str(java_home / 'bin') + os.pathsep + environment.get('PATH', '')
    stamp = datetime.now(ZoneInfo('Asia/Shanghai')).strftime('%Y%m%d-%H%M%S')
    directory = 'isolated-run-' + stamp
    (ROOT / 'forge' / directory).mkdir(exist_ok=False)
    command = [str(candidates[0]), 'runGameTestServer', '--console=plain', '--offline', '-Dnet.minecraftforge.gradle.check.certs=false', '-PrunDirectory=' + directory]
    write_json(ROOT / 'qa/native-invocation.json', {'command': command, 'working_directory': str(ROOT / 'forge'), 'isolated_world': str(ROOT / 'forge' / directory), 'fresh_world': True, 'player_saves_opened': False, 'java_home': str(java_home)})
    log = ROOT / 'qa' / ('forge-' + stamp + '.log')
    with log.open('wb') as output:
        result = subprocess.run(command, cwd=ROOT / 'forge', env=environment, stdout=output, stderr=subprocess.STDOUT, timeout=1200, creationflags=subprocess.CREATE_NO_WINDOW)
    shutil.copy2(log, ROOT / 'qa/forge-gametest.log')
    print(log.read_text(encoding='utf-8', errors='replace')[-7000:])
    if result.returncode:
        raise SystemExit(result.returncode)
    report = json.loads((ROOT / 'qa/native.json').read_text(encoding='utf-8'))
    if report.get('status') != 'PASS':
        raise SystemExit('Forge exited without a passing native report.')


if __name__ == '__main__':
    main()
