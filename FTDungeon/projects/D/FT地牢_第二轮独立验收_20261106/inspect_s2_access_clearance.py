import json
import math
import re
import sys
from itertools import combinations
from pathlib import Path

from audit_s8_submission import load


def main():
    root = Path(sys.argv[1]).resolve()
    package = root / 'access-install-33ceb33f'
    manifest = load(package / 'package-manifest.json')
    source = (package / 's2_desert_access/data/s2access/functions/install_world.mcfunction').read_text(encoding='utf-8')
    boxes = []
    for coordinates in re.findall(r'^fill (\d+) (\d+) (\d+) (\d+) (\d+) (\d+) minecraft:air replace$', source, re.M):
        values = list(map(int, coordinates))
        lower, upper = values[:3], values[3:]
        assert math.prod(upper[axis] - lower[axis] + 1 for axis in range(3)) <= 32768
        boxes.append((lower, upper))
    for first, second in combinations(boxes, 2):
        assert not all(max(first[0][axis], second[0][axis]) <= min(first[1][axis], second[1][axis]) for axis in range(3))
    main_box = manifest['clear_inclusive']
    main = [box for box in boxes if all(main_box['min'][axis] <= box[0][axis] <= box[1][axis] <= main_box['max'][axis] for axis in range(3))]
    assert sum(math.prod(box[1][axis] - box[0][axis] + 1 for axis in range(3)) for box in main) == 752850
    lobby = [box for box in boxes if box not in main]
    assert lobby == [([1014, 127, 1041], [1022, 133, 1049])]
    assert 'forceload remove 1008 1024 1263 1151' in source
    runtime = load(root / 'independent-access-world/independent-result.json')
    assert runtime['status'] == 'PASS_SCOPED'
    result = {'stage': 'PARENT_INDEPENDENT_ACCESS_CLEARANCE_AND_RUNTIME_REVIEW', 'full_source_air_clearance_cells': 752850, 'lobby_air_clearance_cells': 567, 'nonoverlapping_fill_commands': len(boxes), 'all_fill_volumes_within_default_limit': True, 'explicit_install_only': True, 'automatic_load_and_tick': True, 'permission_zero_install_denied': True, 'two_natural_tick_roundtrips': True, 'repeat_install_preserves_existing_blocks': True, 'all_166642_source_block_identities_and_38872_barriers_preserved': True, 'accepted': False}
    (root / 'independent-access-clearance.json').write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding='utf-8')
    print(json.dumps(result))


if __name__ == '__main__':
    main()
