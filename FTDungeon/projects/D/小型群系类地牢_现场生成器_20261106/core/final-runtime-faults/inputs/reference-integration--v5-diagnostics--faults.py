import datetime
import gzip
import hashlib
import importlib.util
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys
import time

sys.dont_write_bytecode = True
HOME = Path(__file__).resolve().parent
sys.path.insert(0, str(HOME.parent))
sys.path.insert(0, str(HOME.parent / 'v4-unified'))
import integrate
import verify_saved_world as base
import readback


class Server:
    def __init__(self, run, stage):
        self.run = run
        self.log = run / (stage + '.log')
        self.stream = self.log.open('w', encoding='utf-8')
        java = Path(os.environ['APPDATA']) / '.minecraft/runtime/java-runtime-gamma-snapshot/bin/java.exe'
        command = [str(java), '-Xmx2G', '-Dftbd.v5World=' + str(run / 'world'), '@' + str(run / 'libraries/net/minecraftforge/forge/1.20.1-47.4.10/win_args.txt'), 'nogui']
        self.process = subprocess.Popen(command, cwd=run, stdin=subprocess.PIPE, stdout=self.stream, stderr=subprocess.STDOUT, text=True)
        self.evidence = dict(pid=self.process.pid, parentPid=os.getpid(), command=command, cwd=str(run), log=str(self.log))
        integrate.save(run / (stage + '-process.json'), self.evidence)
        self.wait(lambda text: 'Done (' in text, 240)

    def text(self):
        return self.log.read_text(encoding='utf-8', errors='replace')

    def send(self, text):
        self.process.stdin.write(text + '\n')
        self.process.stdin.flush()

    def wait(self, predicate, timeout=90):
        deadline = time.monotonic() + timeout
        while time.monotonic() < deadline:
            text = self.text()
            if predicate(text):
                return text
            if self.process.poll() is not None:
                raise integrate.GateError('Owned server exited: ' + str(self.log))
            time.sleep(0.2)
        raise integrate.GateError('Owned server timeout; no unverified termination: ' + str(self.log))

    def status(self, identity):
        offset = len(self.text())
        self.send('ftbiomedungeon_candidate status ' + identity)
        text = self.wait(lambda text: identity + ' phase=' in text[offset:], 30)
        return next(line for line in text[offset:].splitlines() if identity + ' phase=' in line)

    def stop(self):
        if self.process.poll() is None:
            self.send('stop')
            self.process.wait(timeout=120)
        self.stream.close()
        self.evidence.update(exitCode=self.process.returncode, logSha256=integrate.digest(self.log))

    def kill_verified(self):
        pid = self.process.pid
        query = "[Console]::OutputEncoding=[System.Text.UTF8Encoding]::new($false); $ErrorActionPreference='Stop'; Get-CimInstance Win32_Process -Filter 'ProcessId=" + str(pid) + "' | Select-Object ProcessId,ParentProcessId,Name,ExecutablePath,CommandLine,@{Name='CreationTicks';Expression={$_.CreationDate.Ticks}} | ConvertTo-Json"
        inspected = subprocess.run(['powershell', '-NoProfile', '-Command', query], capture_output=True, encoding='utf-8', errors='replace', check=True)
        identity = json.loads(inspected.stdout.lstrip('\ufeff'))
        expected_java = str(Path(os.environ['APPDATA']) / '.minecraft/runtime/java-runtime-gamma-snapshot/bin/java.exe')
        if identity['ProcessId'] != pid or identity['ParentProcessId'] != os.getpid() or identity['Name'].lower() != 'java.exe' or Path(identity['ExecutablePath']).resolve() != Path(expected_java).resolve() or '-Dftbd.v5World=' + str(self.run / 'world') not in identity['CommandLine'] or str(self.run / 'libraries') not in identity['CommandLine']:
            integrate.save(self.run / 'kill-refused.json', identity)
            raise integrate.GateError('Exact owned PID/Java/parent/world/command did not match')
        integrate.save(self.run / 'verified-kill-identity.json', identity)
        command = identity['CommandLine'].replace("'", "''")
        check = "$ErrorActionPreference='Stop'; $p=Get-CimInstance Win32_Process -Filter 'ProcessId=" + str(pid) + "'; if(-not $p -or $p.Name -ne 'java.exe' -or $p.ParentProcessId -ne " + str(os.getpid()) + " -or $p.CreationDate.Ticks -ne " + str(identity['CreationTicks']) + " -or $p.CommandLine -cne '" + command + "'){throw 'Identity changed'}; Stop-Process -Id " + str(pid) + ' -Force'
        terminated = subprocess.run(['powershell', '-NoProfile', '-Command', check], capture_output=True, text=True)
        integrate.save(self.run / 'kill-result.json', dict(pid=pid, exactJavaOnly=True, processTreeTermination=False, identityRechecked=True, exitCode=terminated.returncode, stderr=terminated.stderr, powerloss=False))
        if terminated.returncode:
            raise integrate.GateError('Verified termination failed')
        self.process.wait(timeout=30)
        self.stream.close()
        self.evidence.update(exitCode=self.process.returncode, forcedTermination=True, logSha256=integrate.digest(self.log), powerloss=False)


def record(reader, world, identity):
    path = world / 'ft_biome_dungeon_candidate_v4/minecraft/overworld' / (identity + '.nbt')
    frame = path.read_bytes()
    if frame[:4] != b'FTD4' or hashlib.sha256(frame[72:]).hexdigest().encode() != frame[8:72]:
        raise integrate.GateError('Actual V4 journal invalid before fault injection')
    return base.decode(reader, gzip.decompress(frame[72:])), path


def main():
    source_info = integrate.load(HOME.parent / 'v4-unified/final-evidence.json')
    snapshot = Path(source_info['snapshot'])
    source_ready = integrate.load(snapshot / 'runtime/ready.json')
    jar = integrate.contained(snapshot / 'runtime', source_ready['jar'])
    expected = 'e6ca2da6399ce75ca3dca73236557d002d3f3584c93a0772602279855894dbdd'
    if integrate.digest(jar) != expected:
        raise integrate.GateError('Exact V4 artifact mismatch')
    installed = integrate.load(HOME.parent / 'v4-unified/installed-progress.json')
    source = Path(installed['root']) / 'q0'
    source_id = installed['cases']['0']['instanceId']
    reader = base.load_reader(HOME.parent / 'staging/20261106_132015_129205')
    root = HOME / 'run' / datetime.datetime.now().strftime('%Y%m%d_%H%M%S')
    result = dict(status='RUNNING', root=str(root), jarSha256=expected, snapshot=str(snapshot), inheritedV3FaultPass=False, powerloss=False, completeGeneratorReady=False, cases={})
    integrate.save(HOME / 'fault-progress.json', result)
    for case in (sys.argv[1:] or ['truncated_journal', 'checksum_journal', 'midbuild_forcekill', 'unknown_inventory_forcekill', 'foreign_block_preflight']):
        run = root / case
        run.mkdir(parents=True)
        shutil.copytree(source / 'libraries', run / 'libraries')
        (run / 'mods').mkdir()
        shutil.copyfile(jar, run / 'mods' / jar.name)
        (run / 'eula.txt').write_text('eula=true\n')
        (run / 'server.properties').write_text('server-ip=127.0.0.1\nserver-port=0\nonline-mode=false\nlevel-name=world\nview-distance=2\nsimulation-distance=2\nlevel-seed=0\n')
        evidence = dict(status='RUNNING', processes=[])
        result['cases'][case] = evidence
        integrate.save(HOME / 'fault-progress.json', result)
        server = None
        world = run / 'world'
        try:
            if case in ('truncated_journal', 'checksum_journal', 'unknown_inventory_forcekill'):
                shutil.copytree(source / 'world', world)
                identity = source_id
                saved, journal = record(reader, world, identity)
                evidence['baselineCursor'] = saved['cursor']
            else:
                world.mkdir()
                (world / 'ALLOW_CANDIDATE_EVIDENCE_WORLD_V4').write_text('PARENT_AUTHORIZED_UNACCEPTED_NINE_ORIGINALS_V4_EVIDENCE_ONLY\n')
            if 'journal' in case:
                original = journal.read_bytes()
                shutil.copyfile(journal, run / 'journal-before.nbt')
                journal.write_bytes(original[:32] if case == 'truncated_journal' else original[:-1] + bytes([original[-1] ^ 1]))
                evidence.update(journalOriginalSha256=hashlib.sha256(original).hexdigest(), journalFaultSha256=integrate.digest(journal))
                server = Server(run, 'bad-journal-command')
                server.send('ftbiomedungeon_candidate build ' + identity + ' CONFIRM_ISOLATED_AIRSPACE')
                text = server.wait(lambda text: 'Rejected:' in text)
                rejection = [line for line in text.splitlines() if 'Rejected:' in line]
                expected_cause = 'Truncated journal' if case == 'truncated_journal' else 'checksum mismatch'
                if expected_cause.lower() not in str(rejection).lower():
                    raise integrate.GateError('Unexpected corruption rejection: ' + str(rejection))
                server.stop(); evidence['processes'].append(server.evidence); server = None
                spec = importlib.util.spec_from_file_location('native_v5', HOME.parent / 'v2/read_native_be.py')
                native_module = importlib.util.module_from_spec(spec); spec.loader.exec_module(native_module)
                before_world = native_module.NativeWorld(reader, source / 'world')
                after_world = native_module.NativeWorld(reader, world)
                plan = json.loads(saved['plan']); checked = 0; changes = []
                for room in plan['rooms']:
                    raw = reader.Structure.read(snapshot / 'catalog/candidate-resources/data/biomedungeon/structures' / (room['templateId'] + '.nbt'))
                    for local in raw.blocks:
                        position = base.block_position(local, room)
                        if before_world.state(position) != after_world.state(position) or before_world.entity(position) != after_world.entity(position):
                            changes.append(position)
                        checked += 1
                evidence.update(status='PASS_ACTUAL_COMMAND_CORRUPTION_REFUSAL_UNCHANGED' if not changes else 'FAIL_SAVED_WORLD_CHANGED', refusal=rejection, savedOriginalCellsCompared=checked, changedOriginalCells=changes, savedRegionHashes=after_world.files)
            elif case == 'midbuild_forcekill':
                server = Server(run, 'prepare')
                server.send('ftbiomedungeon_candidate plan 1000 200 1000 s8 9 0')
                text = server.wait(lambda text: re.search(r'DRY RUN (ftdy-[a-f0-9]+)', text) is not None)
                identity = re.search(r'DRY RUN (ftdy-[a-f0-9]+)', text).group(1)
                server.send('ftbiomedungeon_candidate build ' + identity + ' CONFIRM_ISOLATED_AIRSPACE')
                deadline = time.monotonic() + 180
                while time.monotonic() < deadline:
                    status = server.status(identity)
                    if 'phase=BUILDING' in status and int(re.search(r'cursor=(\d+)', status).group(1)) > 4096:
                        evidence['armedStatus'] = status
                        break
                    if 'phase=FAILED' in status or 'ROLLBACK_REQUIRED' in status:
                        raise integrate.GateError(status)
                    time.sleep(0.2)
                else:
                    raise integrate.GateError('No partial build to terminate')
                server.kill_verified(); evidence['processes'].append(server.evidence); server = None
                saved, journal = record(reader, world, identity)
                shutil.copyfile(journal, run / 'killed-journal.nbt')
                shutil.copytree(world / 'region', run / 'after-kill-regions')
                evidence['journalCursorAfterKill'] = saved['cursor']
                server = Server(run, 'resume')
                loaded = server.status(identity); evidence['loadedStatus'] = loaded
                if 'active=false' not in loaded or 'tickets=0' not in loaded:
                    raise integrate.GateError('Automatic resume or retained owned ticket after forcekill')
                server.send('ftbiomedungeon_candidate build ' + identity + ' CONFIRM_ISOLATED_AIRSPACE')
                deadline = time.monotonic() + 600
                while time.monotonic() < deadline:
                    status = server.status(identity)
                    if 'active=false' in status and ('ROLLBACK_REQUIRED' in status or 'phase=FAILED' in status or 'stage=PAYLOAD_VERIFIED' in status):
                        evidence['finalStatus'] = status
                        break
                    time.sleep(2)
                else:
                    raise integrate.GateError('Forcekill recovery not settled')
                server.stop(); evidence['processes'].append(server.evidence); server = None
                if 'stage=PAYLOAD_VERIFIED' in status:
                    offline = readback.inspect(snapshot, world)
                    integrate.save(run / 'readback.json', offline)
                    evidence.update(status='PASS_FORCEKILL_EXPLICIT_RECOVERY' if not offline['originalNativeFieldDifferences'] and not offline['escapedFluidOrFire'] and not offline['enclosureErrors'] else 'FAIL_READBACK', readback=str(run / 'readback.json'))
                else:
                    evidence.update(status='OBSERVED_FORCEKILL_QUARANTINE_REQUIRES_REVIEW', scope='Fail-closed status observed; incomplete persistence cannot be equated with successful recovery')
            elif case == 'unknown_inventory_forcekill':
                plan = json.loads(saved['plan']); position = None
                for room in plan['rooms']:
                    template = reader.Structure.read(snapshot / 'catalog/candidate-resources/data/biomedungeon/structures' / (room['templateId'] + '.nbt'))
                    candidates = [base.block_position(local, room) for local, state in template.blocks.items() if state.name in ('minecraft:chest', 'minecraft:barrel')]
                    if candidates:
                        position = candidates[0]; break
                if position is None:
                    raise integrate.GateError('No real original container')
                coordinates = ' '.join(map(str, position)); evidence['unknownInventoryPosition'] = position
                server = Server(run, 'mutate-and-kill')
                server.send('forceload add ' + str(position[0]) + ' ' + str(position[2]))
                server.send('item replace block ' + coordinates + ' container.0 with minecraft:emerald 7')
                server.wait(lambda text: 'emerald' in text.lower() or 'Replaced a slot' in text)
                offset = len(server.text()); server.send('save-all flush'); server.wait(lambda text: 'Saved the game' in text[offset:])
                server.kill_verified(); evidence['processes'].append(server.evidence); server = None
                shutil.copytree(world / 'region', run / 'after-kill-regions')
                server = Server(run, 'resume-unknown')
                evidence['loadedStatus'] = server.status(identity)
                server.send('ftbiomedungeon_candidate build ' + identity + ' CONFIRM_ISOLATED_AIRSPACE')
                deadline = time.monotonic() + 180
                while time.monotonic() < deadline:
                    status = server.status(identity)
                    if 'active=false' in status and ('ROLLBACK_REQUIRED' in status or 'phase=FAILED' in status):
                        break
                    time.sleep(1)
                else:
                    raise integrate.GateError('Unknown inventory not quarantined')
                server.stop(); evidence['processes'].append(server.evidence); server = None
                spec = importlib.util.spec_from_file_location('native_unknown', HOME.parent / 'v2/read_native_be.py'); module = importlib.util.module_from_spec(spec); spec.loader.exec_module(module)
                native = module.NativeWorld(reader, world); entity = native.entity(position)
                if not any(item.get('id') == 'minecraft:emerald' and item.get('Count') == 7 for item in entity.get('Items', [])):
                    raise integrate.GateError('Unknown native inventory overwritten')
                evidence.update(status='PASS_FORCEKILL_UNKNOWN_INVENTORY_QUARANTINED_PRESERVED', finalStatus=status, savedNativeEntity=entity, syntheticPlayerInventory=True, sourceOriginalRichPayloadClaim=False)
            elif case == 'foreign_block_preflight':
                server = Server(run, 'foreign-block')
                server.send('ftbiomedungeon_candidate plan 1000 200 1000 s8 9 0')
                text = server.wait(lambda text: re.search(r'DRY RUN (ftdy-[a-f0-9]+)', text) is not None)
                identity = re.search(r'DRY RUN (ftdy-[a-f0-9]+)', text).group(1)
                saved, journal = record(reader, world, identity)
                bounds = json.loads(saved['bounds']); lower = bounds['min']; upper = bounds['maxExclusive']
                position = (1000, 200, 1000); evidence['foreignPosition'] = position
                server.send('forceload add 1000 1000')
                time.sleep(3)
                server.send('setblock 1000 200 1000 minecraft:diamond_block')
                server.wait(lambda text: 'Changed the block' in text)
                server.send('ftbiomedungeon_candidate build ' + identity + ' CONFIRM_ISOLATED_AIRSPACE')
                deadline = time.monotonic() + 180
                while time.monotonic() < deadline:
                    status = server.status(identity)
                    if 'active=false' in status and ('ROLLBACK_REQUIRED' in status or 'phase=FAILED' in status):
                        break
                    time.sleep(1)
                else:
                    raise integrate.GateError('Foreign block not protected')
                server.stop(); evidence['processes'].append(server.evidence); server = None
                native = base.World(reader, world); count = 0; changed = []
                for coordinate_y in range(lower['y'], upper['y']):
                    for coordinate_z in range(lower['z'], upper['z']):
                        for coordinate_x in range(lower['x'], upper['x']):
                            current = (coordinate_x, coordinate_y, coordinate_z)
                            state = native.state(current)[0]; expected_state = 'minecraft:diamond_block' if current == position else 'minecraft:air'
                            if state != expected_state:
                                changed.append(dict(position=current, state=state))
                            count += 1
                evidence.update(status='PASS_FOREIGN_BLOCK_PREFLIGHT_ZERO_GENERATOR_WRITES' if not changed else 'FAIL_FOREIGN_BLOCK_CHANGED', checkedEnvelopeCells=count, changedCells=changed, finalStatus=status)
        except Exception as error:
            evidence.update(status='FAIL', error=repr(error))
        finally:
            if server is not None:
                server.stop(); evidence['processes'].append(server.evidence)
            integrate.save(run / 'result.json', evidence)
            integrate.save(HOME / 'fault-progress.json', result)
    result['status'] = 'SCOPED_V4_FAULT_CASES_CLOSED' if all(entry['status'].startswith('PASS') for entry in result['cases'].values()) else 'CLOSED_WITH_FAILURES_OR_REVIEW'
    integrate.save(HOME / 'fault-progress.json', result)
    print(result['status'])


if __name__ == '__main__':
    main()
