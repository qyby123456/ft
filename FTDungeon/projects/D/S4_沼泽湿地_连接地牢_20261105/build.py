import copy
import hashlib
import json
import shutil
import sys
import zipfile
from collections import Counter
from pathlib import Path

sys.dont_write_bytecode = True
ROOT = Path(__file__).resolve().parent
AUDIT = Path(r"D:\ft工程文件\FT地牢_独立复核_20261104_01a1a685")
INSTANCE = Path(r"C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2")
sys.path.insert(0,str(INSTANCE / "output/dungeon-expansion-20261101/tools"))
from nbtio import AIR, Structure, plain, state

ORIGINS = {1:(74,5,2),2:(23,6,91),3:(30,6,136),4:(80,5,120),5:(91,6,65),6:(10,6,55),7:(84,5,185),8:(140,5,133),9:(142,6,80)}
LINKS = [("双门前庭—沉水旗殿",(7,"north"),(4,"south")),("旗殿—垂藤长廊",(4,"north"),(5,"south")),("长廊—古树环水厅",(5,"north"),(1,"south")),("旗殿—盘根水窟",(4,"west"),(3,"east")),("水窟—浅水书房",(3,"north"),(2,"south")),("书房—苔灯折角",(2,"north"),(6,"east")),("旗殿—瀑水庭院",(4,"east"),(8,"west")),("瀑水庭院—双卫宝藏",(8,"north"),(9,"south"))]
WAYPOINTS = {5:[(42,91),(42,74),(34,74)]}
SIZE = (179,38,220)
NS = "s4_connected"


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def save(path,value):
    path.parent.mkdir(parents=True,exist_ok=True)
    path.write_text(json.dumps(value,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")


def main():
    index = json.loads((AUDIT / "FINAL-80-SOURCE-INDEX.json").read_text(encoding="utf-8"))
    result = Structure(SIZE)
    modules, ports, occupied, envelope = [], {}, {}, set()
    targets, edits, caps, exclusions = [], {}, set(), []
    used = {key for _,first,last in LINKS for key in (first,last)} | {(7,"south")}
    for item in index["items"]:
        if item["group"]!=4 or item["image"]==10:
            continue
        number = item["image"]
        path = Path(item["candidate"])
        assert digest(path)==item["sha256"]
        source = Structure.read(path)
        assert not source.entities
        meta = json.loads(path.with_suffix(".json").read_text(encoding="utf-8"))
        origin = ORIGINS[number]
        def translated(position):
            return tuple(value+shift for value,shift in zip(position,origin))
        for pos_x in range(source.size[0]):
            for pos_y in range(source.size[1]):
                for pos_z in range(source.size[2]):
                    position = translated((pos_x,pos_y,pos_z))
                    assert position not in occupied
                    occupied[position] = number
                    envelope.add(position)
                    block = source.get((pos_x,pos_y,pos_z))
                    entity = copy.deepcopy(source.block_entities.get((pos_x,pos_y,pos_z)))
                    if entity:
                        for axis,value in zip(("x","y","z"),position):
                            if axis in entity.value:
                                entity.value[axis].value = value
                    if block.name.endswith("_door") and block.get("open")=="false":
                        properties = dict(block.properties)
                        properties["open"] = "true"
                        edits[position] = {"reason":"入口门开启以便静态通路检查","before":plain(block.as_tag()),"after":plain(state(block.name,**properties).as_tag())}
                        block = state(block.name,**properties)
                    result.set(position,block,entity)
        converted = []
        for connection in meta["connections"]:
            port = {"side":connection["facing"],"feet":connection["position"],"width":connection["width"],"clearHeight":connection["height"]}
            converted.append(port)
            center = translated(port["feet"])
            ports[(number,port["side"])] = (center,port)
            if (number,port["side"]) not in used:
                for across in range(-(port["width"]//2), (port["width"]-1)//2+1):
                    for rise in range(port["clearHeight"]):
                        pos_x,pos_y,pos_z = center
                        position = (pos_x+across,pos_y+rise,pos_z) if port["side"] in ("north","south") else (pos_x,pos_y+rise,pos_z+across)
                        assert position not in result.block_entities
                        before = result.get(position)
                        result.set(position,state("mossy_stone_bricks"))
                        caps.add(position)
                        edits[position] = {"reason":"封闭未使用接口","before":plain(before.as_tag()),"after":plain(state("mossy_stone_bricks").as_tag())}
        evidence = Path(item["independent_evidence"]).parent / "route-input.json"
        accepted = json.loads(evidence.read_text(encoding="utf-8"))[0]
        assert accepted["sha256"]==item["sha256"]
        for entry in accepted["targets"]:
            position = translated(entry["position"])
            feet = tuple(int(value) for value in position)
            target = {"label":f"S4-{number:02}-{entry['label']}","position":position}
            if any((feet[0],feet[1]+rise,feet[2]) in caps for rise in (0,1)):
                exclusions.append(target)
            else:
                targets.append(target)
        modules.append({"image":number,"name":meta["name"],"path":str(path),"sha256":item["sha256"],"origin":origin,"size":source.size,"ports":converted,"source_route_evidence":str(evidence)})
    links, frames = [], []
    for link_index,(label,first,last) in enumerate(LINKS):
        start,first_port = ports[first]
        end,last_port = ports[last]
        assert start[1]==end[1],(label,start,end)
        feet = start[1]
        waypoints = WAYPOINTS.get(link_index,[(start[0],start[2]),(end[0],end[2])])
        assert tuple(waypoints[0])==(start[0],start[2]) and tuple(waypoints[-1])==(end[0],end[2])
        centers = [waypoints[0]]
        for initial,final in zip(waypoints,waypoints[1:]):
            assert initial[0]==final[0] or initial[1]==final[1]
            length = abs(final[0]-initial[0])+abs(final[1]-initial[1])
            centers.extend((initial[0]+(final[0]-initial[0])*step//length,initial[1]+(final[1]-initial[1])*step//length) for step in range(1,length+1))
        outer = {(pos_x+across,pos_z+forward) for pos_x,pos_z in centers for across in range(-4,5) for forward in range(-4,5)}
        inner = {(pos_x+across,pos_z+forward) for pos_x,pos_z in centers for across in range(-3,4) for forward in range(-3,4)}
        for pos_x,pos_z in outer:
            for pos_y in range(1,feet+6):
                position = (pos_x,pos_y,pos_z)
                envelope.add(position)
                if position in occupied:
                    continue
                if pos_y<feet:
                    block = state("dark_oak_planks" if pos_y==feet-1 else "stone_bricks")
                elif pos_y==feet+5:
                    block = state("dark_oak_planks")
                elif (pos_x,pos_z) not in inner:
                    block = state("cracked_stone_bricks" if (pos_x+pos_y+pos_z)%4==0 else "mossy_stone_bricks")
                else:
                    block = AIR
                result.set(position,block)
        for target_index,(pos_x,pos_z) in enumerate(centers):
            targets.append({"label":f"link-{label}-{target_index}","position":[pos_x,feet,pos_z]})
        for pos_x,pos_z in centers[5:-4:9]:
            if (pos_x,feet+4,pos_z) not in occupied:
                result.set((pos_x,feet+4,pos_z),state("lantern",hanging=True,waterlogged=False))
        for key in (first,last):
            center,port = ports[key]
            patched = []
            for across in range(-4,5):
                for rise in range(6):
                    if -(port["width"]//2)<=across<=(port["width"]-1)//2 and rise<port["clearHeight"]:
                        continue
                    pos_x,pos_y,pos_z = center
                    position = (pos_x+across,pos_y+rise,pos_z) if port["side"] in ("north","south") else (pos_x,pos_y+rise,pos_z+across)
                    if result.get(position)==AIR:
                        result.set(position,state("dark_oak_planks" if rise>=4 else "mossy_stone_bricks"))
                        edits[position] = {"reason":"门框与走廊补缝","before":plain(AIR.as_tag()),"after":plain(result.get(position).as_tag())}
                        patched.append(position)
            frames.append({"module":key[0],"side":key[1],"positions":patched})
        links.append({"name":label,"from":first,"to":last,"waypoints_xz":waypoints,"feet":feet,"inner_width":7,"clear_height":4})
    entrance = ports[(7,"south")][0]
    pos_x,feet,pos_z = entrance
    for east in range(pos_x-6,pos_x+7):
        for south in range(pos_z,pos_z+4):
            for height in range(1,feet+6):
                position = (east,height,south)
                envelope.add(position)
                if position not in occupied:
                    result.set(position,state("dark_oak_planks") if height==feet-1 else state("stone_bricks") if height<feet-1 else AIR)
    shell = set()
    neighbors = [(delta_x,delta_y,delta_z) for delta_x in (-1,0,1) for delta_y in (-1,0,1) for delta_z in (-1,0,1) if (delta_x,delta_y,delta_z)!=(0,0,0)]
    for east,height,south in envelope:
        if all((east+delta_x,height+delta_y,south+delta_z) in envelope for delta_x,delta_y,delta_z in ((1,0,0),(-1,0,0),(0,1,0),(0,-1,0),(0,0,1),(0,0,-1))):
            continue
        for delta_x,delta_y,delta_z in neighbors:
            position = (east+delta_x,height+delta_y,south+delta_z)
            if position not in envelope and all(0<=value<limit for value,limit in zip(position,SIZE)):
                shell.add(position)
    for position in shell:
        assert position not in occupied and result.get(position)==AIR
        result.set(position,state("barrier"))
    for east in range(SIZE[0]):
        for height in range(SIZE[1]):
            for south in range(SIZE[2]):
                position = (east,height,south)
                if position not in result.blocks:
                    result.set(position,AIR)
    source = ROOT / "swamp-connected.nbt"
    result.write(source)
    sha256 = digest(source)
    save(ROOT / "layout.json",{"name":"沉根湿地·九域连通地牢","size":SIZE,"sha256":sha256,"entrance":entrance,"modules":modules,"links":links,"junction_frames":frames,"source_edits":[{"position":position,**entry} for position,entry in sorted(edits.items())],"barrier_cells":len(shell),"excluded_capped_targets":exclusions})
    save(ROOT / "qa/route-input.json",[{"group":4,"candidate":str(source),"sha256":sha256,"size":SIZE,"start":entrance,"targets":targets,"swimming":False}])
    shutil.copy2(AUDIT / "vanilla-block-tags.json",ROOT / "qa/vanilla-block-tags.json")
    structure_dir = ROOT / f"datapack/data/{NS}/structures"
    structure_dir.mkdir(parents=True,exist_ok=True)
    shutil.copy2(source,structure_dir / "full.nbt")
    parts,commands = [],[]
    for east in range(0,SIZE[0],48):
        for south in range(0,SIZE[2],48):
            origin = (east,0,south)
            part = Structure((min(48,SIZE[0]-east),SIZE[1],min(48,SIZE[2]-south)))
            for position,block in result.blocks.items():
                if east<=position[0]<east+part.size[0] and south<=position[2]<south+part.size[2]:
                    local = (position[0]-east,position[1],position[2]-south)
                    entity = copy.deepcopy(result.block_entities.get(position))
                    if entity:
                        for axis,value in zip(("x","y","z"),local):
                            if axis in entity.value:
                                entity.value[axis].value = value
                    part.set(local,block,entity)
            order = len(parts)+1
            path = ROOT / f"parts/part-{order:02}.nbt"
            part.write(path)
            shutil.copy2(path,structure_dir / f"part_{order:02}.nbt")
            parts.append({"order":order,"path":str(path.relative_to(ROOT)),"origin":origin,"size":part.size,"sha256":digest(path)})
            commands.append(f"place template {NS}:part_{order:02} ~{east} ~0 ~{south}")
    save(ROOT / "assembly.json",{"source_sha256":sha256,"parts":parts})
    save(ROOT / "datapack/pack.mcmeta",{"pack":{"pack_format":15,"description":"S4 Swamp connected dungeon; Java 1.20.1"}})
    function = ROOT / f"datapack/data/{NS}/functions/assemble.mcfunction"
    function.parent.mkdir(parents=True,exist_ok=True)
    function.write_text("\n".join(commands)+"\n",encoding="utf-8")
    with zipfile.ZipFile(ROOT / "s4-connected-datapack.zip","w",zipfile.ZIP_DEFLATED) as archive:
        for path in sorted((ROOT / "datapack").rglob("*")):
            if path.is_file():
                archive.write(path,path.relative_to(ROOT / "datapack").as_posix())
    print(json.dumps({"size":SIZE,"source_sha256":sha256,"modules":len(modules),"links":len(links),"parts":len(parts),"targets":len(targets),"caps_excluded":len(exclusions),"barrier_cells":len(shell)},ensure_ascii=False))


if __name__=="__main__":
    main()
