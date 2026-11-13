import json
import os
import subprocess
from datetime import datetime, timedelta, timezone
from pathlib import Path
from uuid import uuid4


ROOT = Path(__file__).resolve().parents[1]
JAVA = Path('C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/runtime/java-runtime-gamma-snapshot')


def main():
    gradle = next((Path.home() / '.gradle/wrapper/dists/gradle-8.8-bin').glob('*/gradle-8.8/bin/gradle.bat'))
    environment = os.environ.copy()
    environment['JAVA_HOME'] = str(JAVA)
    environment['PATH'] = str(JAVA / 'bin') + os.pathsep + environment.get('PATH', '')
    stamp = datetime.now(timezone(timedelta(hours=8))).strftime('%Y%m%d-%H%M%S')
    run = ROOT / f'qa/forge-runs/{stamp}-{uuid4().hex[:6]}'
    run.mkdir(parents=True, exist_ok=False)
    command = [str(gradle), 'runGameTestServer', '--offline', '--console=plain', '-Dnet.minecraftforge.gradle.check.certs=false', '-PrunDirectory=' + str(run)]
    invocation = {'command': command, 'cwd': str(ROOT / 'forge'), 'run_directory': str(run), 'fresh_disposable_world': True, 'production_world_opened': False}
    (ROOT / 'qa/forge-invocation.json').write_text(json.dumps(invocation, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    report = ROOT / 'qa/forge-native.json'
    if report.exists():
        report.rename(ROOT / f'qa/forge-native-previous-{stamp}.json')
    with (ROOT / 'qa/forge-run.log').open('wb') as output:
        result = subprocess.run(command, cwd=ROOT / 'forge', env=environment, stdout=output, stderr=subprocess.STDOUT, creationflags=subprocess.CREATE_NO_WINDOW, timeout=1800)
    print((ROOT / 'qa/forge-run.log').read_text(encoding='utf-8', errors='replace')[-8000:])
    if result.returncode:
        raise SystemExit(result.returncode)
    if not report.exists():
        raise SystemExit('Forge returned without a placement report')
    evidence = json.loads(report.read_text(encoding='utf-8'))
    if evidence['status'] != 'PASS':
        raise SystemExit('Forge placement failed')


if __name__ == '__main__':
    main()
