import json
import os
import subprocess
from datetime import datetime, timedelta, timezone
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
JAVA_HOME = Path('C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/runtime/java-runtime-gamma-snapshot')
GRADLE = next((Path.home() / '.gradle/wrapper/dists/gradle-8.8-bin').glob('*/gradle-8.8/bin/gradle.bat'))


def main():
    stamp = datetime.now(timezone(timedelta(hours=8))).strftime('%Y%m%d-%H%M%S')
    run_directory = ROOT / 'qa' / ('forge-run-' + stamp)
    run_directory.mkdir(parents=True, exist_ok=False)
    (run_directory / 'server.properties').write_text('server-port=0\nlevel-type=minecraft:flat\ngenerate-structures=false\nspawn-protection=0\n', encoding='utf-8')
    command = [str(GRADLE), 'runGameTestServer', '--offline', '--console=plain', '-Dnet.minecraftforge.gradle.check.certs=false', '-PrunDirectory=' + str(run_directory)]
    environment = os.environ.copy()
    environment['JAVA_HOME'] = str(JAVA_HOME)
    environment['PATH'] = str(JAVA_HOME / 'bin') + os.pathsep + environment.get('PATH', '')
    invocation = {'command': command, 'cwd': str(ROOT / 'forge'), 'isolated_test_world': str(run_directory / 'world'), 'player_saves_accessed': False, 'server_port': 0}
    (ROOT / 'qa/forge-invocation.json').write_text(json.dumps(invocation, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    with (ROOT / 'qa/forge-console.log').open('wb') as stream:
        result = subprocess.run(command, cwd=ROOT / 'forge', env=environment, stdout=stream, stderr=subprocess.STDOUT, creationflags=subprocess.CREATE_NO_WINDOW)
    print((ROOT / 'qa/forge-console.log').read_text(encoding='utf-8', errors='replace')[-6500:])
    raise SystemExit(result.returncode)


if __name__ == '__main__':
    main()
