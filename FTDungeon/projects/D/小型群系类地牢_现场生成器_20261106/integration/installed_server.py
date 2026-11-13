import argparse
import os
from pathlib import Path
import shutil
import subprocess
import time
import urllib.request
import hashlib
import zipfile
import datetime

import integrate


def main():
    parser = argparse.ArgumentParser(description='Install exact candidate JAR in an integration-only Forge server')
    parser.add_argument('--snapshot', type=Path, required=True)
    parser.add_argument('--java-home', type=Path, required=True)
    parser.add_argument('--timeout', type=int, default=300)
    parser.add_argument('--installer', type=Path)
    parser.add_argument('--official-sha1', required=True)
    args = parser.parse_args()
    frozen = args.snapshot.resolve()
    if not frozen.is_relative_to(integrate.HOME / 'staging'):
        raise integrate.GateError('Snapshot must be inside integration/staging')
    project = frozen / 'assembled'
    jar = next((project / 'build/libs').glob('*.jar'))
    audit = integrate.verify_jar(jar, project)
    run = integrate.HOME / 'run' / (frozen.name + '_installed_' + datetime.datetime.now().strftime('%H%M%S'))
    run.mkdir(parents=True, exist_ok=False)
    url = 'https://maven.minecraftforge.net/net/minecraftforge/forge/1.20.1-47.4.10/forge-1.20.1-47.4.10-installer.jar'
    installer = run / 'forge-installer.jar'
    result = dict(scope='Actual reobfuscated candidate JAR, normal Forge dedicated server', jar=audit, snapshotLockSha256=integrate.digest(frozen / 'snapshot-lock.json'), run=str(run), status='NOT_RUN', worldAcceptance='NOT_RUN')
    try:
        if args.installer:
            shutil.copyfile(args.installer.resolve(), installer)
        else:
            with urllib.request.urlopen(url, timeout=45) as response:
                installer.write_bytes(response.read(20_000_000))
        actual_sha1 = hashlib.sha1(installer.read_bytes()).hexdigest()
        if actual_sha1 != args.official_sha1.strip().lower():
            raise integrate.GateError('Installer official SHA1 mismatch')
        with zipfile.ZipFile(installer) as archive:
            if archive.testzip() is not None or 'install_profile.json' not in archive.namelist():
                raise integrate.GateError('Installer ZIP CRC/profile invalid')
        result['installer'] = dict(url=url, source=str(args.installer), sha256=integrate.digest(installer), officialSha1=actual_sha1, zipCrc='PASS')
        environment = os.environ.copy()
        environment['JAVA_HOME'] = str(args.java_home.resolve())
        java = str(args.java_home.resolve() / 'bin/java.exe')
        installation = integrate.execute([java, '-jar', str(installer), '--installServer', str(run)], run, run / 'install.log', args.timeout, environment)
        result['installation'] = installation
        if installation['status'] != 'PASS':
            result.update(status='INSTALL_FAILED', reason='See install.log; installed-JAR world checks NOT_RUN')
            return 2
        (run / 'mods').mkdir(exist_ok=True)
        installed = run / 'mods' / jar.name
        shutil.copyfile(jar, installed)
        if integrate.digest(installed) != audit['sha256']:
            raise integrate.GateError('Installed candidate hash mismatch')
        (run / 'eula.txt').write_text('eula=true\n', encoding='utf-8')
        (run / 'server.properties').write_text('server-ip=127.0.0.1\nserver-port=0\nonline-mode=false\nlevel-name=world\nview-distance=2\nsimulation-distance=2\nspawn-protection=0\n', encoding='utf-8')
        (run / 'world').mkdir(exist_ok=True)
        (run / 'world/ALLOW_ISOLATED_TEST_WORLD').write_text('ft_biome_dungeon_generator isolated test only\n', encoding='utf-8')
        arguments = run / 'libraries/net/minecraftforge/forge/1.20.1-47.4.10/win_args.txt'
        command = [java, '-Xmx2G', '@' + str(arguments), 'nogui']
        log = run / 'server.log'
        started = time.monotonic()
        with log.open('w', encoding='utf-8') as stream:
            process = subprocess.Popen(command, cwd=run, stdin=subprocess.PIPE, stdout=stream, stderr=subprocess.STDOUT, env=environment, text=True)
            result['process'] = dict(command=command, cwd=str(run), pid=process.pid)

            def wait_for(predicate, seconds=180):
                deadline = time.monotonic() + seconds
                while time.monotonic() < deadline and process.poll() is None:
                    text = log.read_text(encoding='utf-8', errors='replace')
                    if predicate(text):
                        return text
                    time.sleep(1)
                raise integrate.GateError('Installed server timed out/exited before expected log marker')

            def send(command_text):
                process.stdin.write(command_text + '\n')
                process.stdin.flush()

            try:
                wait_for(lambda text: 'Done (' in text)
                send('ftbiomedungeon plan 1024 200 1024 s8 2 900')
                import re
                text = wait_for(lambda text: re.search(r'DRY RUN (ftd-[a-f0-9]+)', text) is not None, 60)
                identity = re.search(r'DRY RUN (ftd-[a-f0-9]+)', text).group(1)
                result['instanceId'] = identity
                send('ftbiomedungeon build ' + identity + ' CONFIRM_ISOLATED_AIRSPACE')
                for attempt in range(180):
                    send('ftbiomedungeon status ' + identity)
                    time.sleep(1)
                    text = log.read_text(encoding='utf-8', errors='replace')
                    if 'phase=FAILED' in text or 'phase=ROLLBACK_REQUIRED' in text:
                        raise integrate.GateError('Actual installed candidate quarantined during build')
                    if 'phase=COMMITTED' in text:
                        result.update(status='PASS_COMMAND_COMMIT', worldAcceptance='PARTIAL', limitation='This command stage only checks runtime commit; separate verify_saved_world.py and installed_recovery.py evidence supplies independent saved-chunk verification')
                        break
                else:
                    raise integrate.GateError('Installed candidate did not commit within 180s')
            finally:
                if process.poll() is None:
                    send('stop')
                    try:
                        process.wait(timeout=60)
                    except subprocess.TimeoutExpired:
                        subprocess.run(['taskkill', '/PID', str(process.pid), '/T', '/F'], cwd=run, stdout=stream, stderr=stream)
                        process.wait()
                result['process'].update(exitCode=process.returncode, durationSeconds=time.monotonic() - started, log=str(log))
        result['logSha256'] = integrate.digest(log)
    except Exception as error:
        result.update(status='FAIL', reason=str(error))
    finally:
        integrate.save(run / 'result.json', result)
        integrate.save(integrate.HOME / 'installed-server-result.json', result)
        print(result)
    return 0 if result['status'] == 'PASS_COMMAND_COMMIT' else 2


if __name__ == '__main__':
    raise SystemExit(main())
