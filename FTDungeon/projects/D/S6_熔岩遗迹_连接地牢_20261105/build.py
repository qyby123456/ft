import copy
import hashlib
import json
import shutil
import sys
import zipfile
from pathlib import Path

sys.dont_write_bytecode = True
ROOT = Path(__file__).resolve().parent
AUDIT = Path(r"D:\ft工程文件\FT地牢_独立复核_20261104_01a1a685")
INSTANCE = Path(r"C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2")
sys.path.insert(0,str(INSTANCE / "output/dungeon-expansion-20261101/tools"))
from nbtio import AIR, Structure, plain, state

SIZE = (190,32,212)
ORIGINS = {1:(96,6,15),2:(139,5,64),3:(52,5,109),4:(90,5,110),5:(95,5,60),6:(10,5,67),7:(90,6,170),8:(150,5,112),9:(152,5,170)}
LINKS = [("警戒门庭—双列柱殿",(7,"north"),(4,"south")),("柱殿—低顶长廊",(4,"north"),(5,"south")),("长廊—熔岩古殿左拱",(5,"north"),(1,"south-7")),("古殿右拱—紫晶书房",(1,"south-21"),(2,"north")),("柱殿—中心箱小室",(4,"west"),(3,"east")),("小室—黑石折角",(3,"north"),(6,"east")),("柱殿—双层天井",(4,"east"),(8,"north")),("天井—守卫宝藏",(8,"south"),(9,"north"))]
WAYPOINTS = {3:[(117,43),(117,51),(151,51),(151,64)],5:[(61,109),(61,88),(38,88)],6:[(116,121),(135,121),(135,102),(168,102),(168,112)]}
NS = "s6_connected"
BRICK = state("polished_blackstone_bricks")


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def save(path,value):
    path.parent.mkdir(parents=True,exist_ok=True)
    path.write_text(json.dumps(value,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")


def main():
    assert not (ROOT / "deployment/deployment-report.json").exists()
    index = json.loads((AUDIT / "FINAL-80-SOURCE-INDEX.json").read_text(encoding="utf-8"))
    result = Structure(SIZE)
    modules,ports,occupied,envelope,originals = [],{},{},set(),{}
    targets,edits,caps,exclusions = [],{},set(),[]
    used = {key for _,first,last in LINKS for key in (first,last)} | {(7,"south")}
    inventory = []
    for item in index["items"]:
        if item["group"]!=6 or item["image"]==10:
            continue
        number = item["image"]
        path = Path(item["candidate"])
        assert digest(path)==item["sha256"]
        source = Structure.read(path)
        assert not source.entities
        originals[number] = source
        meta = json.loads(path.with_suffix(".json").read_text(encoding="utf-8"))
        origin = ORIGINS[number]
        def translated(position):
            return tuple(value+shift for value,shift in zip(position,origin))
        for east in range(source.size[0]):
            for up in range(source.size[1]):
                for south in range(source.size[2]):
                    local = (east,up,south)
                    position = translated(local)
                    assert position not in occupied
                    occupied[position] = number
                    envelope.add(position)
                    entity = copy.deepcopy(source.block_entities.get(local))
                    if entity:
                        for axis,value in zip(("x","y","z"),position):
                            if axis in entity.value:
                                entity.value[axis].value = value
                    result.set(position,source.get(local),entity)
        converted = []
        for connection in meta["connectors"]:
            side = connection["facing"]
            key = side+"-"+str(connection["position"][0]) if number==1 and side=="south" else side
            port = {"id":key,"side":side,"feet":connection["position"],"width":connection["width"],"clearHeight":connection["height"],"crown_width":connection.get("crown_width",0)}
            converted.append(port)
            center = translated(port["feet"])
            ports[(number,key)] = (center,port)
            if (number,key) not in used:
                for rise in range(port["clearHeight"]+(2 if port["crown_width"] else 0)):
                    width = port["width"] if rise<port["clearHeight"] else port["crown_width"]
                    for across in range(-(width//2),(width-1)//2+1):
                        east,up,south = center
                        position = (east+across,up+rise,south) if side in ("north","south") else (east,up+rise,south+across)
                        assert position not in result.block_entities
                        before = result.get(position)
                        if before==AIR:
                            result.set(position,BRICK)
                            caps.add(position)
                            edits[position] = {"reason":"封闭未使用接口，保留既有门框实体","before":plain(before.as_tag()),"after":plain(BRICK.as_tag())}
        evidence = Path(item["independent_evidence"]).parent / "route-input.json"
        accepted = json.loads(evidence.read_text(encoding="utf-8"))[0]
        assert accepted["sha256"]==item["sha256"] and not accepted["swimming"]
        for entry in accepted["targets"]:
            position = translated(entry["position"])
            feet = tuple(int(value) for value in position)
            target = {"label":f"S6-{number:02}-{entry['label']}","position":position}
            if any((feet[0],feet[1]+rise,feet[2]) in caps for rise in (0,1)):
                exclusions.append(target)
            else:
                targets.append(target)
        modules.append({"image":number,"name":meta["name"],"path":str(path),"sha256":item["sha256"],"origin":origin,"size":source.size,"ports":converted,"source_route_evidence":str(evidence)})
        inventory.append({**item,"current_sha256":digest(path),"size":source.size,"ports":converted})
    links,frames = [],[]
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
        outer = {(east+across,south+forward) for east,south in centers for across in range(-4,5) for forward in range(-4,5)}
        inner = {(east+across,south+forward) for east,south in centers for across in range(-3,4) for forward in range(-3,4)}
        for east,south in outer:
            for up in range(1,feet+8):
                position = (east,up,south)
                envelope.add(position)
                if position in occupied:
                    continue
                if up<feet:
                    block = state("polished_blackstone" if up==feet-1 else "blackstone")
                elif up==feet+7:
                    block = BRICK
                elif (east,south) not in inner:
                    block = state("cracked_polished_blackstone_bricks") if (east+up+south)%5==0 else BRICK
                else:
                    block = AIR
                result.set(position,block)
        for order,(east,south) in enumerate(centers):
            targets.append({"label":f"link-{label}-{order}","position":[east,feet,south]})
            if order%7==3 and (east,feet-1,south) not in occupied:
                result.set((east,feet-1,south),state("shroomlight"))
        for key in (first,last):
            center,port = ports[key]
            dx,dz = {"north":(0,-1),"south":(0,1),"west":(-1,0),"east":(1,0)}[port["side"]]
            patched = []
            for across in range(-4,5):
                for rise in range(7):
                    width = port["width"] if rise<port["clearHeight"] else port["crown_width"] if rise<port["clearHeight"]+2 else 0
                    if width and -(width//2)<=across<=(width-1)//2:
                        continue
                    position = (center[0]+dx+across*abs(dz),center[1]+rise,center[2]+dz+across*abs(dx))
                    assert position not in occupied
                    result.set(position,state("orange_stained_glass") if rise>=3 and abs(across)==3 else BRICK)
                    patched.append(position)
            frames.append({"module":key[0],"port":key[1],"outside_plane":True,"positions":patched})
        links.append({"name":label,"from":first,"to":last,"waypoints_xz":waypoints,"feet":feet,"inner_width":7,"clear_height":7})
    entrance = ports[(7,"south")][0]
    east,feet,south = entrance
    for pos_x in range(east-6,east+7):
        for pos_z in range(south,south+4):
            for up in range(1,feet+7):
                position = (pos_x,up,pos_z)
                envelope.add(position)
                if position not in occupied:
                    result.set(position,state("polished_blackstone") if up==feet-1 else state("blackstone") if up<feet-1 else AIR)
    shell = set()
    neighbors = [(dx,dy,dz) for dx in (-1,0,1) for dy in (-1,0,1) for dz in (-1,0,1) if (dx,dy,dz)!=(0,0,0)]
    for east,up,south in envelope:
        if all((east+dx,up+dy,south+dz) in envelope for dx,dy,dz in ((1,0,0),(-1,0,0),(0,1,0),(0,-1,0),(0,0,1),(0,0,-1))):
            continue
        for dx,dy,dz in neighbors:
            position = (east+dx,up+dy,south+dz)
            if position not in envelope:
                assert all(0<=value<limit for value,limit in zip(position,SIZE)),position
                shell.add(position)
    for position in shell:
        assert position not in occupied and result.get(position)==AIR
        result.set(position,state("barrier"))
    for east in range(SIZE[0]):
        for up in range(SIZE[1]):
            for south in range(SIZE[2]):
                position = (east,up,south)
                if position not in result.blocks:
                    result.set(position,AIR)
    source = ROOT / "volcanic-connected.nbt"
    result.write(source)
    sha256 = digest(source)
    save(ROOT / "source-inventory.json",inventory)
    save(ROOT / "layout.json",{"name":"熔心古迹·九域连通地牢","size":SIZE,"sha256":sha256,"entrance":entrance,"spawn":[103,11,204],"modules":modules,"links":links,"junction_frames":frames,"source_edits":[{"position":position,**entry} for position,entry in sorted(edits.items())],"barrier_cells":len(shell),"excluded_capped_targets":exclusions,"room_geometry":"Original stairs, paired arch crowns and upper gallery retained; external junction frames only; no new lava"})
    targets.append({"label":"entry-platform-spawn","position":[103,11,204]})
    save(ROOT / "qa/route-input.json",[{"group":6,"candidate":str(source),"sha256":sha256,"size":SIZE,"start":[103,11,204],"targets":targets,"swimming":False}])
    shutil.copy2(AUDIT / "vanilla-block-tags.json",ROOT / "qa/vanilla-block-tags.json")
    structure_dir = ROOT / f"datapack/data/{NS}/structures"
    structure_dir.mkdir(parents=True,exist_ok=True)
    shutil.copy2(source,structure_dir / "full.nbt")
    parts,commands = [],[]
    for east in range(0,SIZE[0],48):
        for south in range(0,SIZE[2],48):
            origin = (east,0,south)
            part = Structure((min(48,SIZE[0]-east),SIZE[1],min(48,SIZE[2]-south)))
            for pos_x in range(part.size[0]):
                for up in range(part.size[1]):
                    for pos_z in range(part.size[2]):
                        local = (pos_x,up,pos_z)
                        position = (pos_x+east,up,pos_z+south)
                        entity = copy.deepcopy(result.block_entities.get(position))
                        if entity:
                            for axis,value in zip(("x","y","z"),local):
                                if axis in entity.value:
                                    entity.value[axis].value = value
                        part.set(local,result.get(position),entity)
            order = len(parts)+1
            path = ROOT / f"parts/part-{order:02}.nbt"
            part.write(path)
            shutil.copy2(path,structure_dir / f"part_{order:02}.nbt")
            parts.append({"order":order,"path":path.relative_to(ROOT).as_posix(),"origin":origin,"size":part.size,"sha256":digest(path)})
            commands.append(f"place template {NS}:part_{order:02} ~{east} ~0 ~{south}")
    save(ROOT / "assembly.json",{"source_sha256":sha256,"parts":parts})
    save(ROOT / "datapack/pack.mcmeta",{"pack":{"pack_format":15,"description":"S6 Volcanic connected dungeon; Java 1.20.1"}})
    function = ROOT / f"datapack/data/{NS}/functions/assemble.mcfunction"
    function.parent.mkdir(parents=True,exist_ok=True)
    function.write_text("\n".join(commands)+"\n",encoding="utf-8")
    with zipfile.ZipFile(ROOT / "s6-connected-datapack.zip","w",zipfile.ZIP_DEFLATED) as archive:
        for path in sorted((ROOT / "datapack").rglob("*")):
            if path.is_file():
                archive.write(path,path.relative_to(ROOT / "datapack").as_posix())
    print(json.dumps({"size":SIZE,"sha256":sha256,"modules":9,"links":8,"targets":len(targets),"parts":len(parts),"barriers":len(shell)},ensure_ascii=False),flush=True)


if __name__=="__main__":
    main()
