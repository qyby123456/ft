import argparse
import json
from pathlib import Path
import re
import time
import datetime

import integrate
from installed_recovery import Server, journal
import verify_saved_world


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--snapshot', type=Path, required=True)
    parser.add_argument('--run', type=Path, required=True)
    parser.add_argument('--java-home', type=Path, required=True)
    args = parser.parse_args()
    label = datetime.datetime.now().strftime('%H%M%S')
    run = args.run.resolve()
    if not run.is_relative_to(integrate.HOME / 'run'):
        raise integrate.GateError('Integration run required')
    reader = verify_saved_world.load_reader(args.snapshot.resolve())
    evidence = dict(status='NOT_RUN', scope='Actual installed JAR preflight conflict; independent saved-chunk full envelope remains air except injected gold block')
    server = None
    try:
        server = Server(run, args.java_home.resolve(), 'installed-conflict-' + label)
        server.send('ftbiomedungeon plan 4608 200 4608 s8 2 903')
        text = server.wait(lambda text: re.search(r'DRY RUN (ftd-[a-f0-9]+)', text) is not None, 60)
        identity = re.search(r'DRY RUN (ftd-[a-f0-9]+)', text).group(1)
        planned = journal(reader, run / 'world', identity)
        bounds = json.loads(planned['bounds'])
        minimum = tuple(bounds['min'][axis] for axis in ('x', 'y', 'z'))
        maximum = tuple(bounds['maxExclusive'][axis] for axis in ('x', 'y', 'z'))
        intruder = tuple((minimum[index] + maximum[index]) // 2 for index in range(3))
        server.send(f'forceload add {intruder[0]} {intruder[2]}')
        time.sleep(5)
        server.send('setblock ' + ' '.join(map(str, intruder)) + ' minecraft:gold_block')
        server.wait(lambda text: 'Changed the block at' in text or 'Successfully set the block' in text, 30)
        server.send('ftbiomedungeon build ' + identity + ' CONFIRM_ISOLATED_AIRSPACE')
        for attempt in range(120):
            state = server.status(identity)
            if 'phase=FAILED' in state:
                if not all(part in state for part in ('cursor=0 ', 'attempted=0 ', 'tickets=0 ')):
                    raise integrate.GateError('Conflict failure changed cursor or leaked owned tickets')
                evidence['failedStatus'] = state
                break
            time.sleep(1)
        else:
            raise integrate.GateError('Injected occupancy was not rejected')
        server.send(f'forceload remove {intruder[0]} {intruder[2]}')
        server.stop()
        evidence['process'] = server.evidence
        server = None
        world = verify_saved_world.World(reader, run / 'world')
        checked = 0
        for coordinate_y in range(minimum[1], maximum[1]):
            for coordinate_z in range(minimum[2], maximum[2]):
                for coordinate_x in range(minimum[0], maximum[0]):
                    position = (coordinate_x, coordinate_y, coordinate_z)
                    expected = 'minecraft:gold_block' if position == intruder else 'minecraft:air'
                    actual = world.state(position)[0]
                    if actual != expected:
                        raise integrate.GateError(f'Failed preflight wrote or unexpected baseline at {position}: {actual}')
                    checked += 1
        evidence.update(status='PASS', instanceId=identity, checkedEnvelopeCells=checked, intruder=intruder, regionFiles=world.files)
    except Exception as error:
        evidence.update(status='FAIL', reason=str(error))
    finally:
        if server is not None:
            server.stop()
            evidence['process'] = server.evidence
        integrate.save(run / 'installed-conflict-result.json', evidence)
        integrate.save(run / ('installed-conflict-result-' + label + '.json'), evidence)
        integrate.save(integrate.HOME / 'installed-conflict-result.json', evidence)
        print(evidence)
    return 0 if evidence['status'] == 'PASS' else 2


if __name__ == '__main__':
    raise SystemExit(main())
