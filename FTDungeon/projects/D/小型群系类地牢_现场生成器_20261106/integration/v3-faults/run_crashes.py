import datetime
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys
import time

sys.dont_write_bytecode = True
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import integrate

HOME = Path(__file__).resolve().parent


def confirmed_kill(pid, launcher_pid, world, cwd):
    ps = "[Console]::OutputEncoding=[System.Text.UTF8Encoding]::new($false); $ErrorActionPreference='Stop'; $target=Get-CimInstance Win32_Process -Filter 'ProcessId=" + str(pid) + "'; if(-not $target){throw 'Target absent'}; $chain=@(); $cursor=$target; for($depth=0;$depth -lt 10 -and $cursor;$depth++){ $chain += [int]$cursor.ProcessId; $parent=[int]$cursor.ParentProcessId; $cursor=Get-CimInstance Win32_Process -Filter ('ProcessId='+$parent) }; $target | Select-Object ProcessId,ParentProcessId,Name,ExecutablePath,CommandLine,CreationDate,@{Name='AncestorPids';Expression={$chain}} | ConvertTo-Json -Depth 5"
    inspected = subprocess.run(['powershell', '-NoProfile', '-Command', ps], cwd=cwd, capture_output=True, text=True, encoding='utf-8', errors='replace')
    if inspected.returncode:
        raise integrate.GateError('CIM process identity failed: ' + inspected.stderr)
    identity = json.loads(inspected.stdout.lstrip('\ufeff'))
    if isinstance(identity.get('AncestorPids'), dict):
        identity['AncestorPids'] = identity['AncestorPids']['value']
    command = identity.get('CommandLine', '')
    if identity['ProcessId'] != pid or identity['Name'].lower() != 'java.exe' or str(world) not in command or 'ftbd.v3World' not in command or launcher_pid not in identity['AncestorPids']:
        integrate.save(cwd / 'kill-refused-identity.json', identity)
        raise integrate.GateError('Refused termination: PID/Java/command/world/launcher ancestry not all verified')
    integrate.save(cwd / 'verified-kill-identity.json', identity)
    expected_command = command.replace("'", "''")
    terminate = "$ErrorActionPreference='Stop'; $target=Get-CimInstance Win32_Process -Filter 'ProcessId=" + str(pid) + "'; if(-not $target -or $target.Name -ne 'java.exe' -or $target.CommandLine -cne '" + expected_command + "'){throw 'PID identity changed'}; Stop-Process -Id " + str(pid) + ' -Force'
    killed = subprocess.run(['powershell', '-NoProfile', '-Command', terminate], cwd=cwd, capture_output=True, text=True)
    integrate.save(cwd / 'kill-command.json', dict(command=['powershell', '-NoProfile', '-Command', terminate], cwd=str(cwd), exitCode=killed.returncode, stdout=killed.stdout, stderr=killed.stderr, exactJavaPidOnly=True, noProcessTreeTermination=True))
    if killed.returncode:
        raise integrate.GateError('Verified Java termination failed')
    return identity


def main():
    frozen = integrate.HOME / 'v2/staging/20261106_134313/runtime'
    seal = integrate.check_component(integrate.HOME / 'v2', 'integration-v2')
    name = datetime.datetime.now().strftime('%Y%m%d_%H%M%S')
    root = HOME / 'run' / name
    project = root / 'project'
    ready = integrate.load(frozen / 'ready.json')
    for relative, expected in ready['files'].items():
        source = integrate.contained(frozen, relative)
        if integrate.digest(source) != expected:
            raise integrate.GateError('Changed frozen v2 runtime input: ' + relative)
        target = integrate.contained(project, relative)
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(source, target)
        if integrate.digest(target) != expected:
            raise integrate.GateError('V3 snapshot copy mismatch')
    shutil.copyfile(frozen / 'ready.json', project / 'ready.json')
    shutil.copytree(HOME / 'world-tests', root / 'verification/world-tests')
    shutil.copyfile(HOME / 'overlay.gradle', root / 'verification/overlay.gradle')
    fixture = root / 'verification/test-resources/data/ftbd_faulttest/structures/s8_03.nbt'
    fixture.parent.mkdir(parents=True)
    shutil.copyfile(frozen / 'src/gametest/resources/data/ftbd_payloadtest/structures/s8_03.nbt', fixture)
    integrate.save(root / 'snapshot-lock.json', dict(v2IntegrationReadySha256=integrate.digest(integrate.HOME / 'v2/ready.json'), runtimeReadySha256=integrate.digest(frozen / 'ready.json'), runtimeFiles=ready['files'], v2AllFilesVerified=len(seal['files']), verification={str(path.relative_to(root)): integrate.digest(path) for path in (root / 'verification').rglob('*') if path.is_file()}))
    environment = os.environ.copy()
    environment['JAVA_HOME'] = str(Path(os.environ['APPDATA']) / '.minecraft/runtime/java-runtime-gamma-snapshot')
    gradle = Path(os.environ['USERPROFILE']) / '.gradle/wrapper/dists/gradle-8.8-bin/dl7vupf4psengwqhwktix4v1/gradle-8.8/bin/gradle.bat'
    base_command = [os.environ['COMSPEC'], '/d', '/c', str(gradle), '--no-daemon', '--offline', '-Dnet.minecraftforge.gradle.check.certs=false', '--console=plain', '--project-cache-dir', str(root / 'gradle-cache'), '-I', str(root / 'verification/overlay.gradle'), '-Pv3Source=' + str(root / 'verification'), '-Pv3Run=' + str(root)]
    results = dict(status='RUNNING', root=str(root), completeGeneratorReady=False, powerlossProven=False, scenarios={})
    integrate.save(HOME / 'progress.json', results)
    for scenario in ('unknown_items', 'stale_default'):
        folder = root / scenario
        folder.mkdir()
        log = folder / 'prepare.log'
        command = base_command + ['-Pv3Scenario=' + scenario, '-Pv3Stage=prepare', 'runGameTestServer']
        with log.open('w', encoding='utf-8') as stream:
            process = subprocess.Popen(command, cwd=project, stdout=stream, stderr=subprocess.STDOUT, env=environment)
            deadline = time.monotonic() + 900
            marker = None
            while time.monotonic() < deadline and process.poll() is None:
                text = log.read_text(encoding='utf-8', errors='replace')
                marker = re.search(r'V3_KILL_ARMED pid=(\d+) scenario=' + scenario, text)
                if marker:
                    break
                time.sleep(1)
            if not marker:
                results['scenarios'][scenario] = dict(status='FAIL_NO_KILL', reason='No armed marker; no process was terminated', command=command, cwd=str(project), log=str(log))
                integrate.save(HOME / 'progress.json', results)
                break
            java_pid = int(marker.group(1))
            world = folder / 'world-process'
            identity = confirmed_kill(java_pid, process.pid, world, folder)
            process.wait(timeout=120)
            crashed_world = world / 'world'
            preserve = folder / 'after-kill-evidence'
            preserve.mkdir()
            shutil.copytree(crashed_world / 'ft_biome_dungeon_generator_v2', preserve / 'journal')
            shutil.copyfile(crashed_world / 'V3_FAULT_MARKER.json', preserve / 'marker.json')
            integrate.save(preserve / 'saved-regions.json', {str(path.relative_to(crashed_world)): integrate.digest(path) for path in crashed_world.rglob('*.mca')})
            entry = dict(status='KILLED_CONFIRMED_JAVA', javaPid=java_pid, launcherPid=process.pid, identity=identity, prepareLog=str(log), prepareLogSha256=integrate.digest(log), forcedKill=True, gracefulStop=False, powerloss=False)
        resume = integrate.execute(base_command + ['-Pv3Scenario=' + scenario, '-Pv3Stage=resume', 'runGameTestServer'], project, folder / 'resume.log', 900, environment)
        text = (folder / 'resume.log').read_text(encoding='utf-8', errors='replace')
        entry.update(resume=resume, status='PASS_FAIL_CLOSED' if resume['status'] == 'PASS' and 'V3_FAIL_CLOSED_PASS scenario=' + scenario in text and 'All 1 required tests passed' in text else 'FAIL', assertions=[line for line in text.splitlines() if line.startswith('V3_')])
        results['scenarios'][scenario] = entry
        integrate.save(HOME / 'progress.json', results)
    results['status'] = 'PASS_SCOPED' if len(results['scenarios']) == 2 and all(item['status'] == 'PASS_FAIL_CLOSED' for item in results['scenarios'].values()) else 'FAILED_OR_INCOMPLETE'
    integrate.save(HOME / 'progress.json', results)
    print(results)


if __name__ == '__main__':
    main()
