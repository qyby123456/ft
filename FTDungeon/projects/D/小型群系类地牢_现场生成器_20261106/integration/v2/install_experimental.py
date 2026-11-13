import argparse
import gzip
import json
import os
from pathlib import Path
import shutil
import sys
import time
import datetime

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import integrate
from installed_recovery import Server
import verify_saved_world


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--snapshot', required=True, type=Path)
    args = parser.parse_args()
    home = Path(__file__).resolve().parent
    frozen = args.snapshot.resolve()
    baseline = integrate.HOME / 'run/20261106_132015_129205_installed_132725'
    run = home / 'run' / (frozen.name + '_installed_' + datetime.datetime.now().strftime('%H%M%S'))
    run.mkdir(parents=True, exist_ok=False)
    shutil.copytree(baseline / 'libraries', run / 'libraries')
    jar = frozen / 'runtime/build/libs/ft-biome-dungeon-generator-forge-1.20.1-0.2.0-experimental.jar'
    (run / 'mods').mkdir()
    shutil.copyfile(jar, run / 'mods' / jar.name)
    (run / 'eula.txt').write_text('eula=true\n', encoding='utf-8')
    (run / 'server.properties').write_text('server-ip=127.0.0.1\nserver-port=0\nonline-mode=false\nlevel-name=world\nview-distance=2\nsimulation-distance=2\n', encoding='utf-8')
    (run / 'world').mkdir()
    (run / 'world/ALLOW_ISOLATED_TEST_WORLD_V2').write_text('ft_biome_dungeon_generator v2 isolated test only\n', encoding='utf-8')
    result = dict(status='NOT_RUN', artifactSha256=integrate.digest(jar), run=str(run), originalFiveRoomInstalledBE='NOT_RUN_NOT_PACKAGED', sourceSnapshotLockSha256=integrate.digest(frozen / 'snapshot-lock.json'))
    server = None
    try:
        if result['artifactSha256'] != 'f7d73680b20d390a6f08a090fea66b66000deb33bc0a6556ad282340035c6188':
            raise integrate.GateError('Unexpected v2 JAR')
        server = Server(run, Path(os.environ['APPDATA']) / '.minecraft/runtime/java-runtime-gamma-snapshot', 'normal-v2-server')
        server.send('ftbiomedungeon plan 1024 200 1024 s8 2 1')
        import re
        text = server.wait(lambda text: re.search(r'DRY RUN (ftd-[a-f0-9]+)', text) is not None, 60)
        identity = re.search(r'DRY RUN (ftd-[a-f0-9]+)', text).group(1)
        server.send('ftbiomedungeon build ' + identity + ' CONFIRM_ISOLATED_AIRSPACE')
        for attempt in range(300):
            state = server.status(identity)
            if 'phase=FAILED' in state or 'phase=ROLLBACK_REQUIRED' in state:
                raise integrate.GateError(state)
            if 'phase=COMMITTED' in state and 'tickets=0' in state:
                result['committedStatus'] = state
                break
            time.sleep(1)
        else:
            raise integrate.GateError('Experimental production command did not commit')
        server.stop()
        result['process'] = server.evidence
        server = None
        reader = verify_saved_world.load_reader(integrate.HOME / 'staging/20261106_132015_129205')
        journal = run / 'world/ft_biome_dungeon_generator_v2/minecraft/overworld' / (identity + '.nbt')
        raw = journal.read_bytes()
        import hashlib
        if raw[:4] != b'FTD2' or hashlib.sha256(raw[72:]).hexdigest().encode() != raw[8:72]:
            raise integrate.GateError('V2 native journal frame/hash mismatch')
        record = verify_saved_world.decode(reader, gzip.decompress(raw[72:]))
        plan = json.loads(record['plan'])
        used = [room['templateId'] for room in plan['rooms']]
        if not set(used) <= {'s8_05', 's8_06'}:
            raise integrate.GateError('Unaccepted template loaded by installed candidate')
        world = verify_saved_world.World(reader, run / 'world')
        count = 0
        for room in plan['rooms']:
            structure = reader.Structure.read(frozen / 'runtime/src/main/resources/data/biomedungeon/structures' / (room['templateId'] + '.nbt'))
            for local, state in structure.blocks.items():
                if local[0] in (0, structure.size[0] - 1) or local[2] in (0, structure.size[2] - 1):
                    continue
                actual = world.state(verify_saved_world.block_position(local, room))
                if actual != verify_saved_world.rotated_state(state, room['quarterTurns']):
                    raise integrate.GateError('Installed v2 baseline room interior mismatch')
                count += 1
        result.update(status='PASS_BASELINE_POOL_ONLY', actualUsedTemplates=used, savedRawInteriorCells=count, journalVersion=record.get('schema'), regionHashes=world.files, limitation='Normal installed experimental JAR runs accepted two-template baseline only. Original five BE rooms are not packaged; installed BE capability NOT_RUN, not promoted.')
    except Exception as error:
        result.update(status='FAIL', reason=str(error))
    finally:
        if server is not None:
            server.stop()
            result['process'] = server.evidence
        integrate.save(run / 'result.json', result)
        integrate.save(home / 'installed-result.json', result)
        print(result)


if __name__ == '__main__':
    main()
