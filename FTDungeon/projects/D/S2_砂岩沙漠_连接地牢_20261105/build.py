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

ORIGINS = {1:(73,7,157),2:(30,3,13),3:(32,5,53),4:(71,5,47),5:(78,6,101),6:(34,5,92),7:(72,3,2),8:(125,4,46),9:(127,6,100)}
ORIGINS = {number:(origin[0],origin[1]+1,origin[2]) for number,origin in ORIGINS.items()}
LINKS = [("入口—列柱主殿",(7,"south"),(4,"north")),("主殿—积沙长廊",(4,"south"),(5,"north")),("长廊—祭台厅",(5,"south"),(1,"north")),("主殿—单箱庭院",(4,"west"),(3,"east")),("庭院—藏书泉室",(3,"north"),(2,"south")),("庭院—折角灯廊",(3,"south"),(6,"north")),("主殿—跌水庭院",(4,"east"),(8,"west")),("跌水庭院—砂日宝库",(8,"south"),(9,"north"))]
SIZE = (162,31,190)
NS = "s2_connected"


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
    used = {key for _,first,last in LINKS for key in (first,last)} | {(7,"north")}
    for item in index["items"]:
        if item["group"]!=2 or item["image"]==10:
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
                        result.set(position,state("cut_sandstone"))
                        caps.add(position)
                        edits[position] = {"reason":"封闭未使用接口","before":plain(before.as_tag()),"after":plain(state("cut_sandstone").as_tag())}
        evidence = Path(item["independent_evidence"]).parent / "route-input.json"
        accepted = json.loads(evidence.read_text(encoding="utf-8"))[0]
        assert accepted["sha256"]==item["sha256"]
        for entry in accepted["targets"]:
            position = translated(entry["position"])
            feet = tuple(int(value) for value in position)
            target = {"label":f"S2-{number:02}-{entry['label']}","position":position}
            if any((feet[0],feet[1]+rise,feet[2]) in caps for rise in (0,1)):
                exclusions.append(target)
            else:
                targets.append(target)
        modules.append({"image":number,"name":meta["name"],"path":str(path),"sha256":item["sha256"],"origin":origin,"size":source.size,"ports":converted,"source_route_evidence":str(evidence)})
    links, frames = [], []
    for label,first,last in LINKS:
        start,first_port = ports[first]
        end,last_port = ports[last]
        assert start[1]==end[1] and (start[0]==end[0] or start[2]==end[2]),(label,start,end)
        feet = start[1]
        length = abs(end[0]-start[0])+abs(end[2]-start[2])
        centers = [(start[0]+(end[0]-start[0])*step//length,start[2]+(end[2]-start[2])*step//length) for step in range(length+1)]
        outer = {(pos_x+across,pos_z+forward) for pos_x,pos_z in centers for across in range(-4,5) for forward in range(-4,5)}
        inner = {(pos_x+across,pos_z+forward) for pos_x,pos_z in centers for across in range(-3,4) for forward in range(-3,4)}
        for pos_x,pos_z in outer:
            for pos_y in range(1,feet+6):
                position = (pos_x,pos_y,pos_z)
                envelope.add(position)
                if position in occupied:
                    continue
                if pos_y<feet:
                    block = state("smooth_sandstone" if pos_y==feet-1 else "sandstone")
                elif pos_y==feet+5:
                    block = state("cut_sandstone")
                elif (pos_x,pos_z) not in inner:
                    block = state("chiseled_sandstone" if pos_y==feet+2 and (pos_x+pos_z)%4==0 else "sandstone")
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
                        result.set(position,state("cut_sandstone" if rise>=4 else "sandstone"))
                        edits[position] = {"reason":"门框与走廊补缝","before":plain(AIR.as_tag()),"after":plain(result.get(position).as_tag())}
                        patched.append(position)
            frames.append({"module":key[0],"side":key[1],"positions":patched})
        links.append({"name":label,"from":first,"to":last,"waypoints_xz":[[start[0],start[2]],[end[0],end[2]]],"feet":feet,"inner_width":7,"clear_height":4})
    entrance = ports[(7,"north")][0]
    pos_x,feet,pos_z = entrance
    for east in range(pos_x-6,pos_x+7):
        for south in range(1,3):
            for height in range(1,feet+6):
                position = (east,height,south)
                envelope.add(position)
                if position not in occupied:
                    result.set(position,state("smooth_sandstone") if height==feet-1 else state("sandstone") if height<feet-1 else AIR)
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
    source = ROOT / "sandstone-connected.nbt"
    result.write(source)
    sha256 = digest(source)
    save(ROOT / "layout.json",{"name":"砂日古国·九域连通地牢","size":SIZE,"sha256":sha256,"entrance":entrance,"modules":modules,"links":links,"junction_frames":frames,"source_edits":[{"position":position,**entry} for position,entry in sorted(edits.items())],"barrier_cells":len(shell),"excluded_capped_targets":exclusions})
    save(ROOT / "qa/route-input.json",[{"group":2,"candidate":str(source),"sha256":sha256,"size":SIZE,"start":entrance,"targets":targets,"swimming":False}])
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
    save(ROOT / "datapack/pack.mcmeta",{"pack":{"pack_format":15,"description":"S2 Sandstone Desert connected dungeon; Java 1.20.1"}})
    function = ROOT / f"datapack/data/{NS}/functions/assemble.mcfunction"
    function.parent.mkdir(parents=True,exist_ok=True)
    function.write_text("\n".join(commands)+"\n",encoding="utf-8")
    with zipfile.ZipFile(ROOT / "s2-connected-datapack.zip","w",zipfile.ZIP_DEFLATED) as archive:
        for path in sorted((ROOT / "datapack").rglob("*")):
            if path.is_file():
                archive.write(path,path.relative_to(ROOT / "datapack").as_posix())
    print(json.dumps({"size":SIZE,"source_sha256":sha256,"modules":len(modules),"links":len(links),"parts":len(parts),"targets":len(targets),"caps_excluded":len(exclusions),"barrier_cells":len(shell)},ensure_ascii=False))


if __name__=="__main__":
    main()
