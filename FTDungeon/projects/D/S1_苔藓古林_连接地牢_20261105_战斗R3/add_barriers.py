import copy
import hashlib
import importlib.util
import io
import json
import math
import msvcrt
import os
import shutil
import struct
import sys
import time
import zipfile
import zlib
from collections import Counter, defaultdict, deque
from pathlib import Path

sys.dont_write_bytecode = True
INSTANCE = Path(r"C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2")
BASE = Path(__file__).resolve().parent.parent / "S1_苔藓古林_连接地牢_20261105_门框补缝R2"
ROOT = BASE.with_name("S1_苔藓古林_连接地牢_20261105_屏障R4")
WORLD = INSTANCE / "saves/FT_S1_Mossy_Connected_20261105"
sys.path.insert(0, str(INSTANCE / "output/dungeon-expansion-20261101/tools"))
from nbtio import AIR, State, Structure, Tag, encode_payload, plain, read_nbt, read_payload, read_string, state
import flat_world as writer


def digest(path):
    with path.open("rb") as stream:
        return hashlib.file_digest(stream, "sha256").hexdigest()


def save(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def add_box(volume, start, end):
    for pos_x in range(start[0], end[0]+1):
        for pos_y in range(start[1], end[1]+1):
            for pos_z in range(start[2], end[2]+1):
                volume.add((pos_x, pos_y, pos_z))


def envelope(layout):
    volume = set()
    for module in layout["modules"]:
        start = module["origin"]
        end = tuple(value+length-1 for value, length in zip(start, module["size"]))
        add_box(volume, start, end)
    for link in layout["links"]:
        points = link["waypoints_xz"]
        centers = {tuple(points[0])}
        for first, last in zip(points, points[1:]):
            length = abs(first[0]-last[0]) + abs(first[1]-last[1])
            for step in range(length+1):
                centers.add((first[0]+(last[0]-first[0])*step//length, first[1]+(last[1]-first[1])*step//length))
        for pos_x, pos_z in centers:
            add_box(volume, (pos_x-3,0,pos_z-3), (pos_x+3,link["feet"]+5,pos_z+3))
    add_box(volume, (68,0,172), (82,7,181))
    shell = set()
    directions = [(delta_x,delta_y,delta_z) for delta_x in (-1,0,1) for delta_y in (-1,0,1) for delta_z in (-1,0,1) if (delta_x,delta_y,delta_z)!=(0,0,0)]
    for pos_x, pos_y, pos_z in volume:
        if all((pos_x+delta_x,pos_y+delta_y,pos_z+delta_z) in volume for delta_x,delta_y,delta_z in ((1,0,0),(-1,0,0),(0,1,0),(0,-1,0),(0,0,1),(0,0,-1))):
            continue
        for delta_x,delta_y,delta_z in directions:
            adjacent = (pos_x+delta_x,pos_y+delta_y,pos_z+delta_z)
            if adjacent not in volume:
                shell.add(adjacent)
    assert not volume.intersection(shell)
    assert all((75,2,pos_z) in volume for pos_z in range(172,177))
    for position in volume:
        for delta_x,delta_y,delta_z in ((1,0,0),(-1,0,0),(0,1,0),(0,-1,0),(0,0,1),(0,0,-1)):
            adjacent = tuple(value+shift for value,shift in zip(position,(delta_x,delta_y,delta_z)))
            assert adjacent in volume or adjacent in shell
    return volume, shell


def decode(values):
    palette = [State.from_tag(entry) for entry in values["palette"].value]
    if len(palette)==1:
        return [palette[0]]*4096
    bits = max(4,(len(palette)-1).bit_length())
    words = values["data"].value
    return [palette[(words[index//(64//bits)] >> (index%(64//bits)*bits)) & ((1<<bits)-1)] for index in range(4096)]


def patch_region(source, destination, grouped):
    payload = source.read_bytes()
    header = bytearray(payload[:8192])
    output, next_sector = [], 2
    region_x, region_z = map(int,source.name.split(".")[1:3])
    edited_chunks = set()
    for index in range(1024):
        location = struct.unpack_from(">I",payload,index*4)[0]
        if not location:
            continue
        offset = (location>>8)*4096
        length = struct.unpack_from(">I",payload,offset)[0]
        record = payload[offset:offset+4+length]
        coordinates = (region_x*32+index%32,region_z*32+index//32)
        if coordinates in grouped:
            assert record[4]==2
            stream = io.BytesIO(zlib.decompress(record[5:]))
            assert stream.read(1)==b"\x0a"
            root_name = read_string(stream)
            assert root_name==""
            chunk = read_payload(stream,10)
            original = copy.deepcopy(chunk)
            positions = set()
            for section in chunk.value["sections"].value:
                section_y = section.value["Y"].value
                edits = {position for position in grouped[coordinates] if position[1]//16==section_y}
                if not edits:
                    continue
                values = section.value["block_states"].value
                blocks = decode(values)
                expected = list(blocks)
                for pos_x,pos_y,pos_z in edits:
                    expected[pos_y%16*256+pos_z%16*16+pos_x%16] = state("barrier")
                palette = list(dict.fromkeys(expected))
                indices = {block:offset for offset,block in enumerate(palette)}
                values["palette"] = Tag(9,[block.as_tag() for block in palette],10)
                if len(palette)>1:
                    values["data"] = writer.pack_indices([indices[block] for block in expected], max(4,(len(palette)-1).bit_length()))
                else:
                    values.pop("data",None)
                assert decode(values)==expected
                positions.update(edits)
            assert positions==grouped[coordinates]
            chunk.value["isLightOn"] = Tag(1,0)
            comparison = copy.deepcopy(chunk)
            for previous,current in zip(original.value["sections"].value, comparison.value["sections"].value):
                current.value["block_states"] = previous.value["block_states"]
            if "isLightOn" in original.value:
                comparison.value["isLightOn"] = original.value["isLightOn"]
            else:
                comparison.value.pop("isLightOn",None)
            assert comparison==original
            compressed = zlib.compress(b"\x0a\x00\x00"+encode_payload(chunk),6)
            record = struct.pack(">I",len(compressed)+1)+b"\x02"+compressed
            edited_chunks.add(coordinates)
            struct.pack_into(">I",header,4096+index*4,int(time.time()))
        sectors = (len(record)+4095)//4096
        assert sectors<256
        struct.pack_into(">I",header,index*4,next_sector<<8|sectors)
        output.append(record.ljust(sectors*4096,b"\0"))
        next_sector += sectors
    expected_chunks = {coordinates for coordinates in grouped if (coordinates[0]//32,coordinates[1]//32)==(region_x,region_z)}
    assert edited_chunks==expected_chunks
    destination.write_bytes(bytes(header)+b"".join(output))
    return len(edited_chunks)


def main():
    ROOT.mkdir(exist_ok=True)
    assert not (ROOT / "deployment-report.json").exists()
    layout = json.loads((BASE / "layout.json").read_text(encoding="utf-8"))
    volume, shell = envelope(layout)
    routes = json.loads((BASE / "qa/route-input.json").read_text(encoding="utf-8"))[0]
    for target in routes["targets"]:
        pos_x,pos_y,pos_z = target["position"]
        assert (pos_x,pos_y,pos_z) not in shell and (pos_x,pos_y+1,pos_z) not in shell
    encounters = json.loads((BASE.with_name("S1_苔藓古林_连接地牢_20261105_战斗R3") / "encounters.json").read_text(encoding="utf-8"))
    for room in encounters["rooms"]:
        for unit in room["units"]:
            pos_x,pos_y,pos_z = unit["local"]
            assert all((pos_x,pos_y+rise,pos_z) in volume for rise in range(4))
    save(ROOT / "barrier-local-positions.json",sorted(shell))
    spec = importlib.util.spec_from_file_location("junctions",BASE.parent / "S1_苔藓古林_连接地牢_20261105/seal_junctions.py")
    junctions = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(junctions)
    with (WORLD / "session.lock").open("r+b") as lock:
        msvcrt.locking(lock.fileno(),msvcrt.LK_NBLCK,1)
        try:
            block_at, previous_entities = junctions.read_saved_world(WORLD)
            grouped = defaultdict(set)
            replaced = Counter()
            for pos_x,pos_y,pos_z in shell:
                position = (pos_x,pos_y-60,pos_z)
                previous = block_at(position)
                assert position not in previous_entities
                assert previous.name in ("minecraft:air","minecraft:cave_air","minecraft:void_air","minecraft:grass_block","minecraft:dirt","minecraft:bedrock","minecraft:barrier"), ("Unexpected existing building at shell",position,previous)
                replaced[previous.name] += 1
                grouped[(pos_x//16,pos_z//16)].add(position)
            _, level = read_nbt(WORLD / "level.dat")
            player = level.value["Data"].value.get("Player")
            player_position = plain(player).get("Pos") if player else None
            backup = ROOT / "world-before-barriers.zip"
            if not backup.exists():
                with zipfile.ZipFile(backup,"x",zipfile.ZIP_DEFLATED) as archive:
                    for path in sorted(WORLD.rglob("*")):
                        if path.is_file() and path.name!="session.lock":
                            archive.write(path,path.relative_to(WORLD).as_posix())
            with zipfile.ZipFile(backup) as archive:
                assert archive.testzip() is None
                before = {name:hashlib.sha256(archive.read(name)).hexdigest() for name in archive.namelist()}
            assert all(digest(WORLD / relative)==expected for relative,expected in before.items())
            stage = ROOT / "staging"
            (stage / "region").mkdir(parents=True,exist_ok=True)
            edited = []
            total_chunks = 0
            for path in sorted((WORLD / "region").glob("*.mca")):
                region_x,region_z = map(int,path.name.split(".")[1:3])
                if any((chunk_x//32,chunk_z//32)==(region_x,region_z) for chunk_x,chunk_z in grouped):
                    total_chunks += patch_region(path,stage / "region" / path.name,grouped)
                    edited.append(path.relative_to(WORLD).as_posix())
                else:
                    shutil.copy2(path,stage / "region" / path.name)
            assert total_chunks==len(grouped)
            after, entities = junctions.read_saved_world(stage)
            assert entities==previous_entities
            assert all(after(position)==state("barrier") for positions in grouped.values() for position in positions)
            original = Structure.read(BASE / "mossy-forest-connected.nbt")
            changed_local = set(shell)
            for pos_x,pos_y,pos_z in original.blocks:
                position = (pos_x,pos_y-60,pos_z)
                if (pos_x,pos_y,pos_z) not in changed_local:
                    assert after(position)==block_at(position),position
            for target in routes["targets"]:
                pos_x,pos_y,pos_z = target["position"]
                for rise in (0,1):
                    position = (math.floor(pos_x),math.floor(pos_y-60+rise),math.floor(pos_z))
                    assert after(position)==block_at(position)
            assert all(digest(WORLD / relative)==expected for relative,expected in before.items())
            for relative in edited:
                path = WORLD / relative
                pending = path.with_suffix(".mca.barrier-pending")
                assert not pending.exists()
                shutil.copy2(stage / relative,pending)
                assert digest(pending)==digest(stage / relative)
                os.replace(pending,path)
            for relative,expected in before.items():
                assert digest(WORLD / relative)==(digest(stage / relative) if relative in edited else expected),relative
            save(ROOT / "deployment-report.json",{"installed":True,"world":str(WORLD),"backup":str(backup),"barrier_blocks":len(shell),"replaced":dict(replaced),"shell_thickness":1,"room_envelopes":9,"corridors":8,"includes_roof_and_floor":True,"entrance_platform_inside":True,"route_targets_unmodified":len(routes["targets"]),"spawn_points_unmodified":18,"block_entities_unmodified":True,"player_data_level_and_combat_datapack_unmodified":True,"modified_files":edited,"chunks_modified":total_chunks,"previous_player_position":player_position,"client_runtime_tested":False,"scope":"26-neighbor outer shell of union of all room/corridor bounding volumes and spawn platform. Existing interior blocks unchanged. Cannot stop teleport, spectator, creative block breaking, or external mods."})
            (ROOT / "README.md").write_text("# 苔藓古林·屏障 R4\n\n已更新原测试存档。9 个房间、8 条走廊及入口出生平台外围增加一层原版屏障，包含侧面、顶部和底部。共同接口处不加墙，不截断门洞。\n屏障只在联合包络之外；替换空气和边缘地表，不改原建筑、怪物点、玩家数据、难度或数据包。\n入口外平台现在也在围护内，无法继续走入外部超平坦地形；内部房门仍可通行。\n对普通生存/冒险玩家提供实体碰撞围护；无法阻止旁观、传送、创造破坏屏障或其他模组穿墙。手持屏障物品可查看红色标记。\n已有完整备份 world-before-barriers.zip（不含临时 session.lock）。离线验证壳层连续、全部屏障回读和内部方块不变；未启动客户端实测。\n",encoding="utf-8")
            save(ROOT / "SHA256.json",{path.relative_to(ROOT).as_posix():digest(path) for path in sorted(ROOT.rglob("*")) if path.is_file() and path.name!="SHA256.json"})
            print(json.dumps({"installed":True,"barrier_blocks":len(shell),"chunks":total_chunks,"replaced":dict(replaced),"interior_and_combat_unchanged":True},ensure_ascii=False))
        finally:
            lock.seek(0)
            msvcrt.locking(lock.fileno(),msvcrt.LK_UNLCK,1)


if __name__=="__main__":
    main()
