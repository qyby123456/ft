import collections
import hashlib
import json
import os
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
sys.path.insert(0, str(Path(os.environ['APPDATA']) / '.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools'))
from nbtio import Structure

old = ROOT.parent / 'nature-redo-b01-s1-01-r2-20261105/nature-redo-b01-s1-01/room.nbt'
new = ROOT / 'nature-redo-b01-s1-01/room.nbt'
before, after = Structure.read(old), Structure.read(new)
changes = []
for point, state in after.blocks.items():
    previous = before.get(point)
    if previous != state:
        changes.append({'pos': list(point), 'before': {'name': previous.name, 'properties': dict(previous.properties)}, 'after': {'name': state.name, 'properties': dict(state.properties)}})
assert len(changes) == 10, changes
assert all(item['before']['name'] == 'minecraft:air' for item in changes)
def components(structure):
    occupied = {point for point, state in structure.blocks.items() if state.name not in {'minecraft:air', 'minecraft:cave_air', 'minecraft:void_air', 'minecraft:water', 'minecraft:lava'}}
    remaining = set(occupied)
    floating = []
    while remaining:
        start = remaining.pop()
        connected = {start}
        queue = collections.deque([start])
        while queue:
            across, vertical, forward = queue.popleft()
            for deltaX, deltaY, deltaZ in [(1,0,0),(-1,0,0),(0,1,0),(0,-1,0),(0,0,1),(0,0,-1)]:
                neighbor = (across + deltaX, vertical + deltaY, forward + deltaZ)
                if neighbor in remaining:
                    remaining.remove(neighbor)
                    connected.add(neighbor)
                    queue.append(neighbor)
        if not any(point[1] == 0 for point in connected):
            floating.append({'count':len(connected), 'positions':[list(point) for point in sorted(connected)]})
    return floating
old_floating, new_floating = components(before), components(after)
assert len(old_floating) == 10, old_floating
assert not new_floating, new_floating
metadata = json.loads(new.with_name('room.json').read_text(encoding='utf-8'))
for across, vertical, forward in metadata['route']:
    assert all(after.get((across, layer, forward)).name == 'minecraft:air' for layer in range(vertical, vertical + 3))
result = {'status':'PASS_STATIC_SIX_FACE_CONTINUITY_NOT_STRUCTURAL_ENGINEERING_CERTIFICATE','previous_nbt':str(old),'previous_sha256':hashlib.sha256(old.read_bytes()).hexdigest(),'final_nbt':str(new),'final_sha256':hashlib.sha256(new.read_bytes()).hexdigest(),'changed_positions':changes,'changed_count':len(changes),'previous_disconnected_components':old_floating,'final_disconnected_components':new_floating,'route_cells_3high_unchanged':len(metadata['route']),'scope':'All non-air/non-fluid stored blocks, including leaves and hanging vines, are vertices; six orthogonal face neighbors are edges. Every remaining vertex connects to Y=0. This is geometric continuity, not force/bearing capacity or random-tick verification.'}
(ROOT/'qa/connections-r3.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
lines=['# S1-01 R3：精确最小补块', '', '- R2原件完整保留，未覆盖。10组断开组件→0组；仅10处空气替换为实物，未改材质/树形/尺寸/祭坛/端口。', '- 7格苔石补齐拱券上升处的对角断点，2格竖木接至完整屋顶，1格横木接树根至已有上方根段；不是口头豁免。', '', '| XYZ | 变更 |', '|---|---|']
for item in changes: lines.append(f'| {item["pos"]} | air → {item["after"]["name"]} {item["after"]["properties"]} |')
lines+=['','- 所有声明路线原样保持3格净空；日志与新预览须绑定新NBT哈希。','- 四向400tick仅randomTickSpeed=0下有限流体/邻居更新证据，不称自然随机刻验证。','- 等待独立复核，不启动02，不部署。','']
(ROOT/'CHANGES_R3.md').write_text('\n'.join(lines),encoding='utf-8')
print(json.dumps({'sha256':result['final_sha256'],'changed':len(changes),'disconnected_before':len(old_floating),'disconnected_after':len(new_floating)},ensure_ascii=False))
