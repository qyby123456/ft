import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys
import time

sys.dont_write_bytecode = True
import run_crashes as harness
integrate = harness.integrate
HOME = harness.HOME


def main():
    root = Path(sys.argv[1]).resolve()
    if root.parent != (HOME / 'run').resolve():
        raise integrate.GateError('Not an owned fault run')
    project = root / 'project'
    environment = os.environ.copy()
    environment['JAVA_HOME'] = str(Path(os.environ['APPDATA']) / '.minecraft/runtime/java-runtime-gamma-snapshot')
    gradle = Path(os.environ['USERPROFILE']) / '.gradle/wrapper/dists/gradle-8.8-bin/dl7vupf4psengwqhwktix4v1/gradle-8.8/bin/gradle.bat'
    command = [os.environ['COMSPEC'], '/d', '/c', str(gradle), '--no-daemon', '--offline', '-Dnet.minecraftforge.gradle.check.certs=false', '--console=plain', '--project-cache-dir', str(root / 'gradle-cache'), '-I', str(root / 'verification/overlay.gradle'), '-Pv3Source=' + str(root / 'verification'), '-Pv3Run=' + str(root)]
    results = dict(status='RUNNING', root=str(root), completeGeneratorReady=False, powerlossProven=False, scenarios={})
    for scenario in ('unknown_items', 'stale_default'):
        folder = root / scenario
        folder.mkdir(exist_ok=True)
        log = folder / 'prepare.log'
        process = None
        if scenario == 'unknown_items':
            launcher = int(sys.argv[2])
            marker = re.search(r'V3_KILL_ARMED pid=(\d+) scenario=' + scenario, log.read_text(encoding='utf-8', errors='replace'))
            if not marker:
                raise integrate.GateError('No existing owned armed experiment to continue')
        else:
            with log.open('w', encoding='utf-8') as stream:
                process = subprocess.Popen(command + ['-Pv3Scenario=' + scenario, '-Pv3Stage=prepare', 'runGameTestServer'], cwd=project, stdout=stream, stderr=subprocess.STDOUT, env=environment)
            launcher = process.pid
            deadline = time.monotonic() + 900
            marker = None
            while time.monotonic() < deadline and process.poll() is None:
                marker = re.search(r'V3_KILL_ARMED pid=(\d+) scenario=' + scenario, log.read_text(encoding='utf-8', errors='replace'))
                if marker:
                    break
                time.sleep(1)
            if not marker:
                raise integrate.GateError('No armed marker; no process terminated')
        identity = harness.confirmed_kill(int(marker.group(1)), launcher, folder / 'world-process', folder)
        if process is not None:
            process.wait(timeout=120)
        else:
            time.sleep(5)
        world = folder / 'world-process/world'
        preserve = folder / 'after-kill-evidence'
        preserve.mkdir()
        shutil.copytree(world / 'ft_biome_dungeon_generator_v2', preserve / 'journal')
        shutil.copyfile(world / 'V3_FAULT_MARKER.json', preserve / 'marker.json')
        shutil.copytree(world / 'region', preserve / 'region')
        integrate.save(preserve / 'saved-regions.json', {str(path.relative_to(world)): integrate.digest(path) for path in world.rglob('*.mca')})
        entry = dict(status='KILLED_CONFIRMED_JAVA', javaPid=identity['ProcessId'], launcherPid=launcher, identity=identity, prepareLog=str(log), prepareLogSha256=integrate.digest(log), forcedKill=True, gracefulStop=False, powerloss=False)
        results['scenarios'][scenario] = entry
        integrate.save(HOME / 'progress.json', results)
        resume = integrate.execute(command + ['-Pv3Scenario=' + scenario, '-Pv3Stage=resume', 'runGameTestServer'], project, folder / 'resume.log', 900, environment)
        text = (folder / 'resume.log').read_text(encoding='utf-8', errors='replace')
        entry.update(resume=resume, status='PASS_FAIL_CLOSED' if resume['status'] == 'PASS' and 'V3_FAIL_CLOSED_PASS scenario=' + scenario in text and 'All 1 required tests passed' in text else 'FAIL', assertions=[line for line in text.splitlines() if line.startswith('V3_')])
        integrate.save(HOME / 'progress.json', results)
    results['status'] = 'PASS_SCOPED' if all(item['status'] == 'PASS_FAIL_CLOSED' for item in results['scenarios'].values()) else 'FAILED_OR_INCOMPLETE'
    integrate.save(HOME / 'progress.json', results)
    print(results['status'])


if __name__ == '__main__':
    main()
