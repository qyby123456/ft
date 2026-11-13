import hashlib
import json
import os
import sys
from collections import deque
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(Path(os.environ['APPDATA']) / '.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools'))
from nbtio import Structure

folder = ROOT / 'nature-redo-b02-s1-02'
source = folder / 'room.nbt'
structure = Structure.read(source)
excluded = {'minecraft:air', 'minecraft:cave_air', 'minecraft:void_air', 'minecraft:water', 'minecraft:lava'}
solid = {point for point, state in structure.blocks.items() if state.name not in excluded}
visited = {point for point in solid if point[1] == 0}
frontier = deque(visited)
neighbors = [(1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1)]
while frontier:
    point = frontier.popleft()
    for offset in neighbors:
        candidate = tuple(value + delta for value, delta in zip(point, offset))
        if candidate in solid and candidate not in visited:
            visited.add(candidate)
            frontier.append(candidate)
detached = sorted(solid - visited)
result = {'status': 'PASS' if not detached else 'FAIL', 'source_nbt_sha256': hashlib.sha256(source.read_bytes()).hexdigest(), 'method': 'Six-face graph from Y=0, all nonair/nonfluid blocks including leaves and hanging decoration. Geometric connectivity; native survival checked separately.', 'nonfluid_blocks': len(solid), 'ground_connected_blocks': len(visited), 'detached_blocks': detached}
(folder / 'qa/support.json').write_text(json.dumps(result, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps(result, ensure_ascii=False))
assert not detached
