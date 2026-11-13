import json
import os
import subprocess
from datetime import datetime, timedelta, timezone
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]


def main():
    java_home = Path('C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/runtime/java-runtime-gamma-snapshot')
    launchers = sorted((Path.home() / '.gradle/wrapper/dists/gradle-8.8-bin').glob('*/gradle-8.8/bin/gradle.bat'))
    if not launchers or not (java_home / 'bin/java.exe').is_file():
        raise RuntimeError('Cached Gradle 8.8 and Minecraft Java 17 required')
    stamp = datetime.now(timezone(timedelta(hours=8))).strftime('%Y%m%d-%H%M%S')
    run_directory = ROOT / 'qa' / ('forge-run-' + stamp)
    run_directory.mkdir(parents=True, exist_ok=False)
    environment = os.environ.copy()
    environment['JAVA_HOME'] = str(java_home)
    environment['PATH'] = str(java_home / 'bin') + os.pathsep + environment.get('PATH', '')
    command = [str(launchers[0]), 'runGameTestServer', '--offline', '--console=plain', '--max-workers=1', '-Dnet.minecraftforge.gradle.check.certs=false', '-Ds17qa.runDir=' + str(run_directory)]
    log = run_directory / 'gradle.log'
    report = ROOT / 'qa/forge.json'
    if report.exists():
        report.rename(run_directory / 'previous-forge.json')
    (ROOT / 'qa/forge-command.json').write_text(json.dumps({'command': command, 'cwd': str(ROOT / 'forge'), 'java_home': str(java_home), 'world_directory': str(run_directory), 'log': str(log), 'started_at': datetime.now(timezone(timedelta(hours=8))).isoformat()}, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    with log.open('wb') as stream:
        execution = subprocess.run(command, cwd=ROOT / 'forge', env=environment, stdout=stream, stderr=subprocess.STDOUT, creationflags=subprocess.CREATE_NO_WINDOW)
    (ROOT / 'qa/forge-exit.json').write_text(json.dumps({'exit_code': execution.returncode, 'log': str(log)}, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    contents = log.read_text(encoding='utf-8', errors='replace')
    print(contents[-6500:])
    if execution.returncode == 0:
        if not report.is_file() or json.loads(report.read_text(encoding='utf-8')).get('status') != 'PASS' or 'CORRIDOR_S17_FORGE_PASS' not in contents:
            raise RuntimeError('Gradle returned zero but Forge evidence is missing or not PASS; this is not a successful test.')
    raise SystemExit(execution.returncode)


if __name__ == '__main__':
    main()
