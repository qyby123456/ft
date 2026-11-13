import datetime
import gzip
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import sys
import time
import zipfile

sys.dont_write_bytecode = True
sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import integrate
from installed_recovery import Server
import verify_saved_world as reader_base

HOME = Path(__file__).resolve().parent


def main():
    libraries = integrate.HOME / 'run/20261106_132015_129205_installed_132725/libraries'
    source_jar = integrate.HOME / 'v2/staging/20261106_134313/runtime/build/libs/ft-biome-dungeon-generator-forge-1.20.1-0.2.0-experimental.jar'
    if integrate.digest(source_jar) != 'f7d73680b20d390a6f08a090fea66b66000deb33bc0a6556ad282340035c6188':
        raise integrate.GateError('Frozen v2 artifact mismatch')
    root = HOME / 'run' / ('commands_' + datetime.datetime.now().strftime('%Y%m%d_%H%M%S'))
    reader = reader_base.load_reader(integrate.HOME / 'staging/20261106_132015_129205')
    results = dict(status='RUNNING', sourceArtifactSha256=integrate.digest(source_jar), root=str(root), cases={})
    for case in (sys.argv[1:] or ('empty_pool', 'wrong_template_hash', 'truncated_journal', 'checksum_journal')):
        run = root / case
        run.mkdir(parents=True)
        shutil.copytree(libraries, run / 'libraries')
        (run / 'mods').mkdir()
        jar = run / 'mods/candidate.jar'
        if case in ('empty_pool', 'wrong_template_hash'):
            with zipfile.ZipFile(source_jar) as source, zipfile.ZipFile(jar, 'w', compression=zipfile.ZIP_DEFLATED) as target:
                for entry in source.infolist():
                    data = source.read(entry.filename)
                    if entry.filename == 'data/biomedungeon/manifest.json':
                        manifest = json.loads(data)
                        if case == 'empty_pool':
                            manifest['templates'] = []
                        else:
                            manifest['templates'][0]['sha256'] = '0' * 64
                            manifest['templates'][0]['independentAcceptance']['sourceSha256'] = '0' * 64
                        data = json.dumps(manifest).encode('utf-8')
                    target.writestr(entry, data)
        else:
            shutil.copyfile(source_jar, jar)
        (run / 'eula.txt').write_text('eula=true\n')
        (run / 'server.properties').write_text('server-ip=127.0.0.1\nserver-port=0\nonline-mode=false\nlevel-name=world\nview-distance=2\nsimulation-distance=2\n')
        (run / 'world').mkdir()
        (run / 'world/ALLOW_ISOLATED_TEST_WORLD_V2').write_text('ft_biome_dungeon_generator v2 isolated test only\n')
        java = Path(os.environ['APPDATA']) / '.minecraft/runtime/java-runtime-gamma-snapshot'
        evidence = dict(status='RUNNING', injectedCase=case, jarSha256=integrate.digest(jar), derivedArtifactNotRelease=case in ('empty_pool', 'wrong_template_hash'), processes=[])
        server = None
        try:
            if 'journal' in case:
                server = Server(run, java, 'journal-plan')
                server.send('ftbiomedungeon plan 1024 200 1024 s8 2 701')
                text = server.wait(lambda text: re.search(r'DRY RUN (ftd-[a-f0-9]+)', text) is not None, 60)
                identity = re.search(r'DRY RUN (ftd-[a-f0-9]+)', text).group(1)
                server.stop()
                evidence['processes'].append(server.evidence)
                server = None
                journal = run / 'world/ft_biome_dungeon_generator_v2/minecraft/overworld' / (identity + '.nbt')
                saved = journal.read_bytes()
                shutil.copyfile(journal, run / 'journal-before.bin')
                if case == 'truncated_journal':
                    journal.write_bytes(saved[:32])
                else:
                    mutated = bytearray(saved); mutated[-1] ^= 1; journal.write_bytes(mutated)
                evidence['journalOriginalSha256'] = hashlib.sha256(saved).hexdigest()
                evidence['journalFaultSha256'] = integrate.digest(journal)
            server = Server(run, java, 'fault-command')
            server.send('ftbiomedungeon plan 1024 200 1024 s8 2 702')
            text = server.wait(lambda text: 'ftbiomedungeon rejected:' in text or 'Rejected:' in text, 60)
            refusals = [line for line in text.splitlines() if 'Rejected:' in line or 'ftbiomedungeon rejected:' in line]
            expected = 'Empty/oversized manifest' if case == 'empty_pool' else 'SHA256 mismatch' if case == 'wrong_template_hash' else 'Journal load failed'
            if expected.lower() not in '\n'.join(refusals).lower():
                raise integrate.GateError('Unexpected refusal cause: ' + str(refusals))
            evidence['refusals'] = refusals
            server.send('forceload add 944 944 1104 1104')
            time.sleep(8)
            server.send('forceload remove all')
            server.stop()
            evidence['processes'].append(server.evidence)
            server = None
            world = reader_base.World(reader, run / 'world')
            digest = hashlib.sha256()
            checked = 0
            for coordinate_x in range(960, 1089):
                for coordinate_z in range(960, 1089):
                    for coordinate_y in range(190, 224):
                        state = world.state((coordinate_x, coordinate_y, coordinate_z))
                        if state[0] != 'minecraft:air':
                            raise integrate.GateError('Command fault unexpectedly wrote block at ' + str((coordinate_x, coordinate_y, coordinate_z)))
                        digest.update(str(state).encode())
                        checked += 1
            evidence.update(status='PASS_REJECTED_ZERO_WRITES', checkedTargetCells=checked, targetBounds=[960, 190, 960, 1089, 224, 1089], airVolumeSha256=digest.hexdigest(), regionHashes=world.files, scope='Actual command refusal plus saved high-altitude target readback; unrelated spawn/world-generation writes not counted as generator writes')
        except Exception as error:
            evidence.update(status='FAIL', reason=str(error))
        finally:
            if server is not None:
                server.stop()
                evidence['processes'].append(server.evidence)
            integrate.save(run / 'result.json', evidence)
            results['cases'][case] = evidence
            integrate.save(HOME / 'command-faults.json', results)
    results['status'] = 'PASS_SCOPED' if all(item['status'] == 'PASS_REJECTED_ZERO_WRITES' for item in results['cases'].values()) else 'FAILED_OR_INCOMPLETE'
    integrate.save(HOME / 'command-faults.json', results)
    print(results)


if __name__ == '__main__':
    main()
