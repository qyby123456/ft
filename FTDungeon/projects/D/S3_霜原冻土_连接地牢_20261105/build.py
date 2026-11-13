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

ORIGINS = {1:(79,8,4),2:(30,8,95),3:(4,8,135),4:(79,8,118),5:(86,8,54),6:(36,5,160),7:(80,2,190),8:(139,5,104),9:(197,5,109)}
LINKS = [("雪阶入口—霜旗长殿",(7,"north-dungeon"),(4,"south-ice-arch")),("长殿—冰脊长廊",(4,"north-axis"),(5,"south-ice-passage")),("长廊—冰晶主厅",(5,"north-passage"),(1,"east-arch")),("长殿—冻池休憩室",(4,"west-20"),(2,"north-timber-portal")),("冻池—骨藏小室",(2,"west-timber-portal"),(3,"north-stone-arch")),("长殿—灯角冰桥",(4,"west-38"),(6,"north-leg")),("长殿—雪杉庭院",(4,"east-20"),(8,"west-underpass")),("庭院—骨卫宝藏室",(8,"east-underpass"),(9,"west-arch"))]
WAYPOINTS = {2:[(96,54),(96,42),(125,42),(125,17),(113,17)],3:[(79,138),(69,138),(69,82),(44,82),(44,95)],4:[(30,104),(14,104),(14,135)],5:[(79,156),(44,156),(44,160)],6:[(113,138),(127,138),(127,125),(139,125)]}
SIZE = (237,36,232)
NS = "s3_connected"


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
    used = {key for _,first,last in LINKS for key in (first,last)} | {(7,"south-approach")}
    for item in index["items"]:
        if item["group"]!=3 or item["image"]==10:
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
            port = {"key":connection["name"],"side":connection["facing"],"feet":connection["position"],"width":connection["width"],"clearHeight":connection["height"],"width_offsets":connection.get("width_offsets",[-(connection["width"]//2),(connection["width"]-1)//2])}
            converted.append(port)
            center = translated(port["feet"])
            ports[(number,port["key"])] = (center,port)
            if (number,port["key"]) not in used:
                for across in range(port["width_offsets"][0],port["width_offsets"][1]+1):
                    for rise in range(port["clearHeight"]):
                        pos_x,pos_y,pos_z = center
                        position = (pos_x+across,pos_y+rise,pos_z) if port["side"] in ("north","south") else (pos_x,pos_y+rise,pos_z+across)
                        assert position not in result.block_entities
                        before = result.get(position)
                        result.set(position,state("stone_bricks"))
                        caps.add(position)
                        edits[position] = {"reason":"封闭未使用接口","before":plain(before.as_tag()),"after":plain(state("stone_bricks").as_tag())}
        evidence = Path(item["independent_evidence"]).parent / "route-input.json"
        accepted = json.loads(evidence.read_text(encoding="utf-8"))[0]
        assert accepted["sha256"]==item["sha256"]
        for entry in accepted["targets"]:
            position = translated(entry["position"])
            feet = tuple(int(value) for value in position)
            target = {"label":f"S3-{number:02}-{entry['label']}","position":position}
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
                    block = state("polished_andesite" if pos_y==feet-1 else "stone_bricks")
                elif pos_y==feet+5:
                    block = state("spruce_planks")
                elif (pos_x,pos_z) not in inner:
                    block = state("packed_ice" if pos_y==feet+2 and (pos_x+pos_z)%4==0 else "stone_bricks")
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
                    if port["width_offsets"][0]<=across<=port["width_offsets"][1] and rise<port["clearHeight"]:
                        continue
                    pos_x,pos_y,pos_z = center
                    position = (pos_x+across,pos_y+rise,pos_z) if port["side"] in ("north","south") else (pos_x,pos_y+rise,pos_z+across)
                    if result.get(position)==AIR:
                        result.set(position,state("spruce_planks" if rise>=4 else "stone_bricks"))
                        edits[position] = {"reason":"门框与走廊补缝","before":plain(AIR.as_tag()),"after":plain(result.get(position).as_tag())}
                        patched.append(position)
            frames.append({"module":key[0],"side":key[1],"positions":patched})
        links.append({"name":label,"from":first,"to":last,"waypoints_xz":waypoints,"feet":feet,"inner_width":7,"clear_height":4})
    entrance = ports[(7,"south-approach")][0]
    pos_x,feet,pos_z = entrance
    for east in range(pos_x-6,pos_x+7):
        for south in range(pos_z,pos_z+4):
            for height in range(1,feet+6):
                position = (east,height,south)
                envelope.add(position)
                if position not in occupied:
                    result.set(position,state("polished_andesite") if height==feet-1 else state("stone_bricks") if height<feet-1 else AIR)
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
    source = ROOT / "frost-connected.nbt"
    result.write(source)
    sha256 = digest(source)
    save(ROOT / "layout.json",{"name":"霜旗冰城·九域连通地牢","size":SIZE,"sha256":sha256,"entrance":entrance,"modules":modules,"links":links,"junction_frames":frames,"source_edits":[{"position":position,**entry} for position,entry in sorted(edits.items())],"barrier_cells":len(shell),"excluded_capped_targets":exclusions})
    save(ROOT / "qa/route-input.json",[{"group":3,"candidate":str(source),"sha256":sha256,"size":SIZE,"start":entrance,"targets":targets,"swimming":False}])
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
    save(ROOT / "datapack/pack.mcmeta",{"pack":{"pack_format":15,"description":"S3 Frostland connected dungeon; Java 1.20.1"}})
    function = ROOT / f"datapack/data/{NS}/functions/assemble.mcfunction"
    function.parent.mkdir(parents=True,exist_ok=True)
    function.write_text("\n".join(commands)+"\n",encoding="utf-8")
    with zipfile.ZipFile(ROOT / "s3-connected-datapack.zip","w",zipfile.ZIP_DEFLATED) as archive:
        for path in sorted((ROOT / "datapack").rglob("*")):
            if path.is_file():
                archive.write(path,path.relative_to(ROOT / "datapack").as_posix())
    print(json.dumps({"size":SIZE,"source_sha256":sha256,"modules":len(modules),"links":len(links),"parts":len(parts),"targets":len(targets),"caps_excluded":len(exclusions),"barrier_cells":len(shell)},ensure_ascii=False))


if __name__=="__main__":
    main()
