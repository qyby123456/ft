import datetime
import gzip
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys
import time
import zipfile

sys.dont_write_bytecode = True
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import integrate
from installed_recovery import Server
import verify_saved_world as base
import readback

HOME = Path(__file__).resolve().parent
PROGRESS = HOME / os.environ.get('FT_INSTALLED_PROGRESS', 'installed-progress.json')


class OwnedServer(Server):
    def stop(self):
        if self.process.poll() is None:
            self.send('stop')
            self.process.wait(timeout=120)
        self.stream.close()
        self.evidence.update(exitCode=self.process.returncode, logSha256=integrate.digest(self.log))

    def status(self, identity):
        offset = len(self.text())
        self.send('ftbiomedungeon_candidate status ' + identity)
        text = self.wait(lambda text: identity + ' phase=' in text[offset:], 30)
        return next(line for line in text[offset:].splitlines() if identity + ' phase=' in line)


def main():
    progress = integrate.load(HOME / 'progress.json')
    snapshot = Path(progress['snapshot'])
    ready = integrate.load(snapshot / 'runtime/ready.json')
    jar = integrate.contained(snapshot / 'runtime', ready['jar'])
    if integrate.digest(jar) != ready['jarSha256']:
        raise integrate.GateError('Frozen candidate artifact changed')
    with zipfile.ZipFile(jar) as archive:
        names = archive.namelist()
        if len(names) != len(set(names)) or any('GameTests.class' in name or name.startswith('data/ftbd_unified/') for name in names):
            raise integrate.GateError('Candidate JAR includes tests or duplicate entries')
        for entry in ('cn/ft/biomedungeon/core/next/DungeonYPlanner.class', 'cn/ft/biomedungeon/runtime/UnifiedEngine.class'):
            if entry not in names:
                raise integrate.GateError('Real candidate command/planner missing')
    root = HOME / 'run' / datetime.datetime.now().strftime('%Y%m%d_%H%M%S')
    result = dict(status='RUNNING', snapshot=str(snapshot), root=str(root), jarSha256=integrate.digest(jar), productionPromotion=False, completeGeneratorReady=False, cases={})
    integrate.save(PROGRESS, result)
    java = Path(os.environ['APPDATA']) / '.minecraft/runtime/java-runtime-gamma-snapshot'
    reader = base.load_reader(integrate.HOME / 'staging/20261106_132015_129205')
    for turn in (list(map(int, sys.argv[1:])) or range(4)):
        run = root / ('q' + str(turn))
        run.mkdir(parents=True)
        shutil.copytree(integrate.HOME / 'run/20261106_132015_129205_installed_132725/libraries', run / 'libraries')
        (run / 'mods').mkdir()
        shutil.copyfile(jar, run / 'mods' / jar.name)
        (run / 'eula.txt').write_text('eula=true\n')
        (run / 'server.properties').write_text('server-ip=127.0.0.1\nserver-port=0\nonline-mode=false\nlevel-name=world\nview-distance=2\nsimulation-distance=2\nspawn-protection=0\n')
        if 'FT_WORLD_SEED' in os.environ:
            with (run / 'server.properties').open('a') as properties:
                properties.write('level-seed=' + str(int(os.environ['FT_WORLD_SEED'])) + '\n')
        world = run / 'world'
        world.mkdir()
        (world / 'ALLOW_CANDIDATE_EVIDENCE_WORLD_V4').write_text('PARENT_AUTHORIZED_UNACCEPTED_NINE_ORIGINALS_V4_EVIDENCE_ONLY\n')
        evidence = dict(status='RUNNING', ordinal=turn, processes=[])
        result['cases'][str(turn)] = evidence
        server = None
        try:
            server = OwnedServer(run, java, 'prepare')
            server.send('ftbiomedungeon_candidate plan 1000 200 1000 s8 9 ' + str(turn))
            text = server.wait(lambda text: re.search(r'DRY RUN (ftdy-[a-f0-9]+)', text) is not None or 'Rejected:' in text, 90)
            match = re.search(r'DRY RUN (ftdy-[a-f0-9]+)', text)
            if match is None:
                raise integrate.GateError('Actual installed candidate plan rejected')
            identity = match.group(1)
            evidence['instanceId'] = identity
            server.send('ftbiomedungeon_candidate build ' + identity + ' CONFIRM_ISOLATED_AIRSPACE')
            if turn == 0:
                deadline = time.monotonic() + 180
                while time.monotonic() < deadline:
                    status = server.status(identity)
                    if 'phase=BUILDING' in status and int(re.search(r'cursor=(\d+)', status).group(1)) > 1024:
                        server.send('ftbiomedungeon_candidate pause ' + identity)
                        time.sleep(1)
                        evidence['pausedStatus'] = server.status(identity)
                        break
                    if 'phase=FAILED' in status or 'phase=ROLLBACK_REQUIRED' in status:
                        raise integrate.GateError(status)
                    time.sleep(0.3)
                else:
                    raise integrate.GateError('Did not establish a partial actual installed job')
                server.stop()
                evidence['processes'].append(server.evidence)
                server = None
                journal = world / 'ft_biome_dungeon_candidate_v4/minecraft/overworld' / (identity + '.nbt')
                shutil.copyfile(journal, run / 'midbuild-journal.nbt')
                record = base.decode(reader, gzip.decompress(journal.read_bytes()[72:]))
                evidence['savedCursor'] = record['cursor']
                if record['phase'] != 'BUILDING' or record['cursor'] <= 0:
                    raise integrate.GateError('Saved installed job not partial')
                server = OwnedServer(run, java, 'resume')
                loaded = server.status(identity)
                evidence['loadedStatus'] = loaded
                if 'active=false' not in loaded or 'tickets=0' not in loaded or ('cursor=' + str(record['cursor']) + ' ') not in loaded:
                    raise integrate.GateError('Automatic resume or changed prefix after restart')
                server.send('ftbiomedungeon_candidate build ' + identity + ' CONFIRM_ISOLATED_AIRSPACE')
            deadline = time.monotonic() + 480
            while time.monotonic() < deadline:
                status = server.status(identity)
                if 'phase=FAILED' in status or 'phase=ROLLBACK_REQUIRED' in status or 'Rejected:' in server.text():
                    raise integrate.GateError('Installed candidate quarantined/rejected: ' + status)
                if 'active=false tickets=0 stage=PAYLOAD_VERIFIED' in status:
                    evidence['payloadStatus'] = status
                    break
                time.sleep(2)
            else:
                raise integrate.GateError('Installed candidate payload did not complete')
            server.send('ftbiomedungeon_candidate evidence ' + identity)
            server.send('ftbiomedungeon_candidate routeaudit ' + identity)
            server.wait(lambda text: 'Native route audit attempted=' in text or 'Rejected:' in text, 900)
            server.stop()
            evidence['processes'].append(server.evidence)
            server = None
            report = world / 'ft_biome_dungeon_candidate_v4/minecraft/overworld' / (identity + '-routes.json')
            route = integrate.load(report)
            evidence['ownerNativeRouteDiagnostics'] = dict(attempted=route['attempted'], passed=route['passed'], sha256=integrate.digest(report), independentAcceptance=False)
            offline = readback.inspect(snapshot, world)
            integrate.save(run / 'independent-readback.json', offline)
            evidence.update(status='INSTALLED_PAYLOAD_READBACK_DONE_ROUTES_INTERACTIONS_PENDING', readback=str(run / 'independent-readback.json'), readbackSha256=integrate.digest(run / 'independent-readback.json'))
        except Exception as error:
            evidence.update(status='FAIL', error=repr(error))
        finally:
            if server is not None:
                server.stop()
                evidence['processes'].append(server.evidence)
            integrate.save(run / 'result.json', evidence)
            integrate.save(PROGRESS, result)
    result['status'] = 'SCOPED_INSTALLED_CASES_COMPLETE' if all(case['status'] != 'FAIL' for case in result['cases'].values()) else 'FAILED_OR_INCOMPLETE'
    integrate.save(PROGRESS, result)
    print(result['status'])


if __name__ == '__main__':
    main()
