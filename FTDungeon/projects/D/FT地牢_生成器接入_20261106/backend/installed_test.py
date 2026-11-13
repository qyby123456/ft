import argparse
from concurrent.futures import ThreadPoolExecutor
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import time

ROOT = Path(__file__).resolve().parent
ORIGINAL = Path('D:/ft工程文件/小型群系类地牢_现场生成器_20261106/runtime/next-commit-iofix')
JAVA = Path(os.environ['APPDATA']) / '.minecraft/runtime/java-runtime-gamma-snapshot/bin/java.exe'
LIBRARIES = ORIGINAL.parent / 'next-unified/run/installed-candidate/libraries'
JAR = ROOT / 'build/libs/ft-biome-backend-library-1.0.0-ft-backend-test-harness-only.jar'
ATTEMPT = ''


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


class Server:
    def __init__(self, run, label):
        self.log = run / (label + '.log')
        self.output = self.log.open('w', encoding='utf-8')
        self.process = subprocess.Popen([str(JAVA), '-Xmx2G', '@libraries/net/minecraftforge/forge/1.20.1-47.4.10/win_args.txt', 'nogui'],
                                        cwd=run, stdin=subprocess.PIPE, stdout=self.output, stderr=subprocess.STDOUT, text=True)
        self.wait('Done (', 240)

    def send(self, command):
        self.process.stdin.write(command + '\n')
        self.process.stdin.flush()

    def text(self):
        return self.log.read_text(encoding='utf-8', errors='replace')

    def wait(self, token, timeout=40):
        deadline = time.monotonic() + timeout
        while time.monotonic() < deadline:
            text = self.text()
            if token in text:
                return text
            if self.process.poll() is not None:
                raise RuntimeError('Server exited before ' + token)
            time.sleep(0.5)
        raise TimeoutError(token)

    def status(self):
        previous = len(self.text())
        self.send('backendtest status')
        deadline = time.monotonic() + 30
        while time.monotonic() < deadline:
            lines = [line for line in self.text()[previous:].splitlines() if 'BACKEND_TEST_STATUS {' in line]
            if lines:
                return json.loads(lines[-1][lines[-1].index('{'):])
            time.sleep(0.2)
        raise TimeoutError('status')

    def stop(self, kill=False):
        if self.process.poll() is None:
            if kill:
                self.process.kill()
            else:
                self.send('stop')
            self.process.wait(timeout=90)
        self.output.close()
        return {'pid': self.process.pid, 'exitCode': self.process.returncode, 'log': str(self.log), 'sha256': digest(self.log), 'forced': kill}


def run_case(name):
    run = ROOT / 'run' / (name + ATTEMPT)
    run.mkdir(parents=True, exist_ok=False)
    shutil.copytree(LIBRARIES, run / 'libraries')
    (run / 'mods').mkdir()
    shutil.copy2(JAR, run / 'mods' / JAR.name)
    (run / 'eula.txt').write_text('eula=true\n', encoding='utf-8')
    (run / 'server.properties').write_text('server-ip=127.0.0.1\nserver-port=0\nonline-mode=false\nlevel-name=world\nlevel-seed=267611119455246637\nview-distance=2\nsimulation-distance=2\nspawn-protection=0\n', encoding='utf-8')
    result = {'case': name, 'harnessJarSha256': digest(JAR), 'world': str(run / 'world'), 'processes': [],
              'testOnlyMod': True, 'oldCandidateCommandsOrMarkerOrReceiptUsed': False}
    server = None
    try:
        server = Server(run, 'prepare')
        if name == 'occupied':
            server.send('backendtest obstacle')
        if name == 'journal-io':
            pending = run / 'world/ft_biome_backend_v1/ftdungeonmobs/dungeon.ftb.pending'
            pending.mkdir(parents=True)
        server.send('backendtest prepare')
        server.wait('BACKEND_TEST_PREPARE ')
        if name in {'occupied', 'journal-io'}:
            deadline = time.monotonic() + 80
            while time.monotonic() < deadline:
                status = server.status()
                if status['state'] == 'FAILED':
                    break
                time.sleep(1)
            assert status['state'] == 'FAILED' and status['completed'] == 0
            if name == 'occupied':
                server.send('backendtest edited')
                server.wait('BACKEND_TEST_EDIT_RETAINED=true')
            result['status'] = 'PASS_FAIL_CLOSED_ZERO_COMPLETED'
            result['final'] = status
        elif name == 'interrupted':
            deadline = time.monotonic() + 180
            while time.monotonic() < deadline:
                status = server.status()
                assert status['state'] != 'FAILED', status
                if status['completed'] >= 1024:
                    break
                time.sleep(1)
            assert status['state'] == 'BUILDING' and status['completed'] >= 1024
            result['beforeKill'] = status
            result['processes'].append(server.stop(kill=True))
            server = Server(run, 'restart')
            status = server.status()
            assert status['state'] == 'FAILED' and status['completed'] >= result['beforeKill']['completed']
            initial = status['completed']
            server.send('backendtest prepare')
            time.sleep(3)
            status = server.status()
            assert status['state'] == 'FAILED' and status['completed'] == initial
            result['final'] = status
            result['status'] = 'PASS_REAL_PROCESS_KILL_RESTART_LOCKED_NO_AUTOREBUILD'
        else:
            deadline = time.monotonic() + 1000
            while time.monotonic() < deadline:
                status = server.status()
                assert status['state'] != 'FAILED', status
                if status['state'] == 'READY':
                    break
                time.sleep(2)
            assert status['state'] == 'READY' and status['completed'] == status['total'] and status['observedTicks'] >= 2400
            assert 'V5_DERIVED_ENTRANCE_NATIVE_PASS' in server.text()
            result['ready'] = status
            server.send('backendtest gate')
            server.wait('BACKEND_TEST_GATE_CONTRACT_PLACED')
            server.send('backendtest edit')
            server.wait('BACKEND_TEST_EDIT_SAVED')
            result['processes'].append(server.stop())
            server = Server(run, 'restart')
            status = server.status()
            assert status['state'] == 'READY' and status['stage'] == 'READY_REUSED_NO_WORLD_WRITES'
            server.send('backendtest prepare')
            server.send('backendtest edited')
            server.wait('BACKEND_TEST_EDIT_RETAINED=true')
            assert server.status()['completed'] == status['completed']
            result['restart'] = status
            result['returnGateTestScope'] = 'Actual native test fixture matching FT block registry/property/anchor-BE contract; not full FT mod gate behavior'
            server.send('backendtest badgate')
            server.wait('BACKEND_TEST_BAD_GATE_PART_PLACED')
            server.send('backendtest prepare')
            rejected = server.status()
            assert rejected['state'] == 'FAILED' and 'Wrong FT return gate part/facing' in rejected['error']
            result['wrongGatePartRefused'] = rejected
            result['status'] = 'PASS_REAL_NINE_ROOM_NATIVE_FLUID_2400_READY_RESTART_PLAYER_EDIT_RETAINED'
    except Exception as error:
        result['status'] = 'FAILED'
        result['error'] = repr(error)
    finally:
        if server is not None:
            result['processes'].append(server.stop())
        (ROOT / (name + ATTEMPT + '-result.json')).write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding='utf-8')
    return result


def main():
    global ATTEMPT
    parser = argparse.ArgumentParser()
    parser.add_argument('--attempt', default='')
    parser.add_argument('cases', nargs='+', choices=['ready', 'occupied', 'journal-io', 'interrupted'])
    options = parser.parse_args()
    assert not options.attempt or re.fullmatch('[a-z0-9-]+', options.attempt)
    ATTEMPT = '-' + options.attempt if options.attempt else ''
    with ThreadPoolExecutor(max_workers=2) as pool:
        results = list(pool.map(run_case, options.cases))
    for result in results:
        print(result['case'] + ': ' + result['status'], flush=True)
    assert all(result['status'].startswith('PASS_') for result in results), 'Retain all failure logs/worlds'


if __name__ == '__main__':
    main()
