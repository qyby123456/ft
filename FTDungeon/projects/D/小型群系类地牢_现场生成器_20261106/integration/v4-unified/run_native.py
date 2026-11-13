import datetime
import os
from pathlib import Path
import shutil
import subprocess
import sys
import time

sys.dont_write_bytecode = True
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import integrate

HOME = Path(__file__).resolve().parent
PROGRESS = HOME / os.environ.get('FT_NATIVE_PROGRESS', 'native-progress.json')


def main():
    snapshot = Path(integrate.load(HOME / 'progress.json')['snapshot'])
    installed = integrate.load(HOME / os.environ.get('FT_INSTALLED_PROGRESS', 'installed-progress.json'))
    turns = list(map(int, sys.argv[1:])) or list(range(4))
    root = HOME / 'native-run' / datetime.datetime.now().strftime('%Y%m%d_%H%M%S')
    project = root / 'project'
    shutil.copytree(snapshot / 'runtime', project)
    shutil.copytree(HOME / 'world-tests', root / 'verification/world-tests')
    shutil.copyfile(HOME / 'overlay.gradle', root / 'verification/overlay.gradle')
    namespace = os.environ.get('FT_NATIVE_NAMESPACE', 'ftbd_independent')
    fixture = root / ('verification/test-resources/data/' + namespace + '/structures/s8_05.nbt')
    fixture.parent.mkdir(parents=True)
    shutil.copyfile(snapshot / 'catalog/candidate-resources/data/biomedungeon/structures/s8_05.nbt', fixture)
    integrate.save(root / 'verification-lock.json', {str(path.relative_to(root)): integrate.digest(path) for path in (root / 'verification').rglob('*') if path.is_file()})
    environment = os.environ.copy()
    environment['JAVA_HOME'] = str(Path(os.environ['APPDATA']) / '.minecraft/runtime/java-runtime-gamma-snapshot')
    gradle = Path(os.environ['USERPROFILE']) / '.gradle/wrapper/dists/gradle-8.8-bin/dl7vupf4psengwqhwktix4v1/gradle-8.8/bin/gradle.bat'
    result = dict(status='RUNNING', root=str(root), snapshot=str(snapshot), inheritedWorldAcceptance=False, cases={})
    integrate.save(PROGRESS, result)
    for turn in turns:
        selected = integrate.load(HOME / os.environ['FT_INSTALLED_CASE3_PROGRESS']) if turn == 3 and 'FT_INSTALLED_CASE3_PROGRESS' in os.environ else installed
        source = Path(selected['root']) / ('q' + str(turn))
        deadline = time.monotonic() + 1800
        while not (source / 'result.json').is_file():
            if time.monotonic() > deadline:
                raise integrate.GateError('Installed case still active; no duplicate world/process launched')
            time.sleep(5)
        completed = integrate.load(source / 'result.json')
        if completed['status'] == 'FAIL':
            raise integrate.GateError('Installed source case failed: ' + str(completed))
        folder = root / ('q' + str(turn))
        shutil.copytree(source / 'world', folder / 'world')
        command = [os.environ['COMSPEC'], '/d', '/c', str(gradle), '--no-daemon', '--offline', '-Dnet.minecraftforge.gradle.check.certs=false', '--console=plain', '--project-cache-dir', str(root / 'gradle-cache'), '-I', str(root / 'verification/overlay.gradle'), '-PauditSource=' + str(root / 'verification'), '-PauditWorld=' + str(folder), '-PauditMatrix=' + str(snapshot / 'catalog/path-check-matrix.json'), '-PauditNamespace=' + namespace, 'runGameTestServer']
        log = folder / 'native.log'
        with log.open('w', encoding='utf-8') as stream:
            process = subprocess.Popen(command, cwd=project, stdout=stream, stderr=subprocess.STDOUT, env=environment)
            entry = dict(status='RUNNING', launcherPid=process.pid, command=command, cwd=str(project), log=str(log))
            result['cases'][str(turn)] = entry
            integrate.save(PROGRESS, result)
            try:
                exit_code = process.wait(timeout=900)
            except subprocess.TimeoutExpired:
                entry['status'] = 'TIMEOUT_PROCESS_LEFT_LIVE_NO_UNVERIFIED_KILL'
                integrate.save(PROGRESS, result)
                raise
        text = log.read_text(encoding='utf-8', errors='replace')
        report = folder / 'world/INDEPENDENT_NATIVE_REPORT.json'
        entry.update(status='EXECUTED' if exit_code == 0 and 'All 1 required tests passed' in text and report.is_file() else 'FAIL_HARNESS', exitCode=exit_code, logSha256=integrate.digest(log))
        if report.is_file():
            data = integrate.load(report)
            entry.update(report=str(report), reportSha256=integrate.digest(report), attempted=data['attempted'], passed=data['passed'], interactions=len(data['interactions']), diagnosticStatus=data['status'])
        integrate.save(PROGRESS, result)
        integrate.save(root / 'native-results.json', result)
    result['status'] = 'SCOPED_NATIVE_PROBES_EXECUTED' if all(entry['status'] == 'EXECUTED' for entry in result['cases'].values()) else 'FAILED_OR_INCOMPLETE'
    integrate.save(PROGRESS, result)
    integrate.save(root / 'native-results.json', result)
    print(result['status'])


if __name__ == '__main__':
    main()
