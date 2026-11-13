import argparse
import gzip
import os
from pathlib import Path
import re
import subprocess
import time

import integrate
import verify_saved_world


class Server:
    def __init__(self, run, java, stage):
        self.run = run
        self.log = run / (stage + '.log')
        self.stream = self.log.open('w', encoding='utf-8')
        command = [str(java / 'bin/java.exe'), '-Xmx2G', '@' + str(run / 'libraries/net/minecraftforge/forge/1.20.1-47.4.10/win_args.txt'), 'nogui']
        self.process = subprocess.Popen(command, cwd=run, stdin=subprocess.PIPE, stdout=self.stream, stderr=subprocess.STDOUT, text=True)
        self.evidence = dict(command=command, cwd=str(run), pid=self.process.pid, log=str(self.log))
        self.wait(lambda text: 'Done (' in text)

    def text(self):
        return self.log.read_text(encoding='utf-8', errors='replace')

    def send(self, command):
        self.process.stdin.write(command + '\n')
        self.process.stdin.flush()

    def wait(self, predicate, timeout=240):
        deadline = time.monotonic() + timeout
        while time.monotonic() < deadline and self.process.poll() is None:
            text = self.text()
            if predicate(text):
                return text
            time.sleep(0.1)
        raise integrate.GateError('Installed recovery stage timeout/exited: ' + str(self.log))

    def status(self, identity):
        before = len(self.text())
        self.send('ftbiomedungeon status ' + identity)
        text = self.wait(lambda text: identity + ' phase=' in text[before:], timeout=30)
        return next(line for line in text[before:].splitlines() if identity + ' phase=' in line)

    def stop(self):
        if self.process.poll() is None:
            self.send('stop')
            try:
                self.process.wait(timeout=90)
            except subprocess.TimeoutExpired:
                subprocess.run(['taskkill', '/PID', str(self.process.pid), '/T', '/F'], cwd=self.run, stdout=self.stream, stderr=self.stream)
                self.process.wait()
        self.stream.close()
        self.evidence.update(exitCode=self.process.returncode, logSha256=integrate.digest(self.log))


def journal(reader, world, identity):
    path = world / 'ft_biome_dungeon_generator/minecraft/overworld' / (identity + '.nbt')
    return verify_saved_world.decode(reader, gzip.decompress(path.read_bytes()[72:]))


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--snapshot', type=Path, required=True)
    parser.add_argument('--run', type=Path, required=True)
    parser.add_argument('--java-home', type=Path, required=True)
    args = parser.parse_args()
    run = args.run.resolve()
    if not run.is_relative_to(integrate.HOME / 'run'):
        raise integrate.GateError('Integration-owned installed run required')
    reader = verify_saved_world.load_reader(args.snapshot.resolve())
    evidence = dict(status='NOT_RUN', scope='Actual installed candidate JAR, distinct normal dedicated JVMs, clean mid-build interrupt and explicit resume; not forced kill/powerloss', stages=[])
    server = None
    try:
        server = Server(run, args.java_home.resolve(), 'installed-prepare')
        server.send('ftbiomedungeon plan 2048 200 2048 s8 3 901')
        text = server.wait(lambda text: re.search(r'DRY RUN (ftd-[a-f0-9]+)', text) is not None, 60)
        identity = re.search(r'DRY RUN (ftd-[a-f0-9]+)', text).group(1)
        evidence['instanceId'] = identity
        server.send('ftbiomedungeon build ' + identity + ' CONFIRM_ISOLATED_AIRSPACE')
        for attempt in range(1200):
            state = server.status(identity)
            if 'phase=BUILDING' in state and re.search(r'cursor=[1-9][0-9]* ', state):
                server.send('ftbiomedungeon pause ' + identity)
                paused = server.wait(lambda text: 'Paused without rollback' in text, 30)
                evidence['pausedStatus'] = server.status(identity)
                break
            if 'phase=FAILED' in state or 'phase=ROLLBACK_REQUIRED' in state or 'phase=COMMITTED' in state:
                raise integrate.GateError('Cannot establish mid-build interruption: ' + state)
            time.sleep(0.1)
        else:
            raise integrate.GateError('Never reached mid-build progress')
        server.stop()
        evidence['stages'].append(server.evidence)
        server = None
        saved = journal(reader, run / 'world', identity)
        if saved['phase'] != 'BUILDING' or saved['cursor'] <= 0:
            raise integrate.GateError('Saved interruption not partial BUILDING')
        evidence['savedCursor'] = saved['cursor']
        evidence['savedOperationHash'] = saved['operationHash']
        server = Server(run, args.java_home.resolve(), 'installed-resume')
        loaded = server.status(identity)
        if 'phase=BUILDING' not in loaded or 'active=false' not in loaded or f"cursor={saved['cursor']} " not in loaded or 'tickets=0' not in loaded:
            raise integrate.GateError('Unexpected automatic resume/load status: ' + loaded)
        evidence['loadedStatus'] = loaded
        server.send('ftbiomedungeon build ' + identity + ' CONFIRM_ISOLATED_AIRSPACE')
        for attempt in range(300):
            state = server.status(identity)
            if 'phase=FAILED' in state or 'phase=ROLLBACK_REQUIRED' in state:
                raise integrate.GateError('Installed recovery quarantined: ' + state)
            if 'phase=COMMITTED' in state and 'tickets=0' in state:
                evidence['committedStatus'] = state
                break
            time.sleep(1)
        else:
            raise integrate.GateError('Installed recovery failed to commit')
        before = len(server.text())
        server.send('ftbiomedungeon build ' + identity + ' CONFIRM_ISOLATED_AIRSPACE')
        server.wait(lambda text: 'Preflight scheduled' in text[before:], 30)
        for attempt in range(120):
            state = server.status(identity)
            if 'phase=COMMITTED' in state and 'active=false' in state and 'tickets=0' in state:
                evidence['idempotentStatus'] = state
                break
            time.sleep(1)
        else:
            raise integrate.GateError('Committed idempotent replay did not settle')
        server.stop()
        evidence['stages'].append(server.evidence)
        server = None
        readback = verify_saved_world.verify(args.snapshot.resolve(), run / 'world')
        evidence.update(status='PASS', independentSavedChunkReadback=readback)
    except Exception as error:
        evidence.update(status='FAIL', reason=str(error))
    finally:
        if server is not None:
            server.stop()
            evidence['stages'].append(server.evidence)
        integrate.save(run / 'installed-recovery-result.json', evidence)
        integrate.save(integrate.HOME / 'installed-recovery-result.json', evidence)
        print(evidence)
    return 0 if evidence['status'] == 'PASS' else 2


if __name__ == '__main__':
    raise SystemExit(main())
