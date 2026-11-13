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

SIZE = (262,34,250)
ORIGINS = {1:(90,1,18),2:(155,1,19),3:(10,1,218),4:(90,1,140),5:(99,1,80),6:(50,1,205),7:(92,1,205),8:(160,1,142),9:(220,1,142)}
LINKS = [("接引门庭—仪仗长殿",(7,"north-deeper"),(4,"south-open-cutaway"),None),("长殿—肋骨长廊",(4,"north"),(5,"south"),None),("长廊—珊瑚主厅",(5,"north"),(1,"north"),[(107,80),(107,65),(75,65),(75,5),(107,5),(107,18)]),("主厅—藏书泉室",(1,"east"),(2,"west"),None),("长殿—海晶灯角",(4,"west"),(6,"north"),[(90,160),(74,160),(74,205)]),("灯角—珊瑚小室",(6,"west"),(3,"east"),None),("长殿—天井庭院",(4,"east"),(8,"west"),None),("庭院—三叉戟宝库",(8,"east"),(9,"west"),None)]
WATER = state("water",level=0)


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def save(path,value):
    path.parent.mkdir(parents=True,exist_ok=True)
    path.write_text(json.dumps(value,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")


def source_ports(number,folder):
    if number==1:
        return [{"name":side,"anchor":anchor,"width":5} for side,anchor in [("north",[17,3,0]),("west",[0,3,10]),("east",[34,3,10])]]
    if number==2:
        return [{"name":"west","anchor":[0,3,9],"width":5}]
    if number==3:
        return [{"name":"west","anchor":[0,3,8],"width":3},{"name":"east","anchor":[18,3,11],"width":3}]
    integration = json.loads((folder / "qa/integration.json").read_text(encoding="utf-8"))
    return [{"name":gate["name"],"anchor":gate["anchor"],"width":gate["width"]} for gate in integration["cases"][0]["gates"]]


def source_route(number,item):
    if number==1:
        return {"swimming":True,"targets":[{"label":label,"position":position} for label,position in [("north",[17,3,0]),("west",[0,3,10]),("east",[34,3,10]),("left-ring",[8,3,19]),("right-ring",[26,3,19]),("rear-ring",[17,3,10])]]}
    folder = AUDIT / (f"S5-{number:02}-independent-r1" if number==8 else f"S5-{number:02}-independent")
    report = json.loads((folder / "route-input.json").read_text(encoding="utf-8"))[0]
    assert report["sha256"]==item["sha256"] and report["swimming"]
    return report


def main():
    assert not (ROOT / "deployment/deployment-report.json").exists()
    result = Structure(SIZE)
    for pos_x in range(SIZE[0]):
        for pos_y in range(SIZE[1]):
            for pos_z in range(SIZE[2]):
                outside = pos_x in (0,SIZE[0]-1) or pos_z in (0,SIZE[2]-1) or pos_y in (0,SIZE[1]-1)
                glass = pos_x in (1,SIZE[0]-2) or pos_z in (1,SIZE[2]-2) or pos_y==SIZE[1]-2
                block = state("barrier") if outside else state("dark_prismarine") if pos_y<=3 else state("cyan_stained_glass") if glass else WATER
                result.set((pos_x,pos_y,pos_z),block)
    index = json.loads((AUDIT / "FINAL-80-SOURCE-INDEX.json").read_text(encoding="utf-8"))
    occupied,modules,ports,targets = set(),[],{},[]
    for item in index["items"]:
        if item["group"]!=5 or item["image"]==10:
            continue
        number = item["image"]
        path = Path(item["candidate"])
        assert digest(path)==item["sha256"]
        source = Structure.read(path)
        assert not source.entities
        origin = ORIGINS[number]
        folder = path.parent.parent
        connectors = source_ports(number,folder)
        for connection in connectors:
            anchor = tuple(value+shift for value,shift in zip(connection["anchor"],origin))
            ports[(number,connection["name"])] = (anchor,connection)
        for pos_x in range(source.size[0]):
            for pos_y in range(source.size[1]):
                for pos_z in range(source.size[2]):
                    local = (pos_x,pos_y,pos_z)
                    destination = tuple(value+shift for value,shift in zip(local,origin))
                    assert destination not in occupied
                    occupied.add(destination)
                    entity = copy.deepcopy(source.block_entities.get(local))
                    if entity:
                        for axis,value in zip(("x","y","z"),destination):
                            if axis in entity.value:
                                entity.value[axis].value = value
                    result.set(destination,source.get(local),entity)
        route = source_route(number,item)
        for target in route["targets"]:
            targets.append({"label":f"S5-{number:02}-{target['label']}","position":[value+shift for value,shift in zip(target["position"],origin)]})
        modules.append({"image":number,"name":folder.name,"path":str(path),"sha256":item["sha256"],"origin":origin,"size":source.size,"ports":connectors,"acceptance_records":item["acceptance_records"]})
    corridors = []
    for label,first,last,specified in LINKS:
        start,_ = ports[first]
        end,_ = ports[last]
        assert start[1]==end[1]==4
        waypoints = specified or [(start[0],start[2]),(end[0],end[2])]
        assert tuple(waypoints[0])==(start[0],start[2]) and tuple(waypoints[-1])==(end[0],end[2])
        centers = [waypoints[0]]
        for initial,final in zip(waypoints,waypoints[1:]):
            assert initial[0]==final[0] or initial[1]==final[1]
            length = abs(final[0]-initial[0])+abs(final[1]-initial[1])
            centers.extend((initial[0]+(final[0]-initial[0])*step//length,initial[1]+(final[1]-initial[1])*step//length) for step in range(1,length+1))
        for order,(pos_x,pos_z) in enumerate(centers):
            for across in range(-3,4):
                for forward in range(-3,4):
                    position = (pos_x+across,3,pos_z+forward)
                    if position not in occupied:
                        result.set(position,state("prismarine_bricks" if abs(across)<3 and abs(forward)<3 else "prismarine"))
            position = (pos_x,3,pos_z)
            if position not in occupied and order%8==0:
                result.set(position,state("sea_lantern"))
            assert result.get((pos_x,4,pos_z))==WATER and result.get((pos_x,5,pos_z))==WATER,(label,order,(pos_x,pos_z))
            targets.append({"label":f"link-{label}-{order}","position":[pos_x,4,pos_z]})
        corridors.append({"name":label,"from":first,"to":last,"waypoints_xz":waypoints,"feet":4,"inner_width":7,"type":"source-water seabed lane; no dry tunnel, no solid blocks in required external water rings"})
    entry = ports[(7,"south-entrance")][0]
    targets.append({"label":"underwater-entry-start","position":[107,4,242]})
    source = ROOT / "coral-connected.nbt"
    result.write(source)
    sha256 = digest(source)
    save(ROOT / "layout.json",{"name":"珊瑚深庭·九域连通地牢","size":SIZE,"sha256":sha256,"entrance":entry,"spawn":[107,4,242],"modules":modules,"links":corridors,"source_edits":[],"source_blocks_unchanged":True,"unused_ports":"Remain wet and open into the same sealed ocean as required by per-source contracts; no dry caps.","integration":{"source_ring":8,"source_cover":8,"solid_bed_layers":3,"barrier_outer_shell":True,"tank_inner":"cyan_stained_glass","oxygen_guarantee":False}})
    save(ROOT / "qa/route-input.json",[{"group":5,"candidate":str(source),"sha256":sha256,"size":SIZE,"start":[107,4,242],"targets":targets,"swimming":True,"vertical_swimming":True}])
    shutil.copy2(AUDIT / "vanilla-block-tags.json",ROOT / "qa/vanilla-block-tags.json")
    structure_dir = ROOT / "datapack/data/s5_connected/structures"
    structure_dir.mkdir(parents=True,exist_ok=True)
    shutil.copy2(source,structure_dir / "full.nbt")
    parts,commands = [],[]
    for east in range(0,SIZE[0],48):
        for south in range(0,SIZE[2],48):
            origin = (east,0,south)
            part = Structure((min(48,SIZE[0]-east),SIZE[1],min(48,SIZE[2]-south)))
            for pos_x in range(part.size[0]):
                for pos_y in range(part.size[1]):
                    for pos_z in range(part.size[2]):
                        local = (pos_x,pos_y,pos_z)
                        position = (pos_x+east,pos_y,pos_z+south)
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
            commands.append(f"place template s5_connected:part_{order:02} ~{east} ~0 ~{south}")
    save(ROOT / "assembly.json",{"source_sha256":sha256,"parts":parts,"warning":"Dense complete tank including water, glass and barrier; load all chunks before placing. Runtime seam placement not tested."})
    save(ROOT / "datapack/pack.mcmeta",{"pack":{"pack_format":15,"description":"S5 sealed coral ocean connected dungeon; Java 1.20.1"}})
    function = ROOT / "datapack/data/s5_connected/functions/assemble.mcfunction"
    function.parent.mkdir(parents=True,exist_ok=True)
    function.write_text("\n".join(commands)+"\n",encoding="utf-8")
    with zipfile.ZipFile(ROOT / "s5-connected-datapack.zip","w",zipfile.ZIP_DEFLATED) as archive:
        for path in sorted((ROOT / "datapack").rglob("*")):
            if path.is_file():
                archive.write(path,path.relative_to(ROOT / "datapack").as_posix())
    print(json.dumps({"size":SIZE,"sha256":sha256,"modules":9,"links":8,"targets":len(targets),"parts":len(parts)},ensure_ascii=False),flush=True)


if __name__=="__main__":
    main()
