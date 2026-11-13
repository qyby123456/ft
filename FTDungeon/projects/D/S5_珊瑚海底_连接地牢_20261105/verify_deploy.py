import copy
import hashlib
import importlib.util
import json
import shutil
import sys
import zipfile
from collections import Counter
from pathlib import Path

sys.dont_write_bytecode = True
ROOT = Path(__file__).resolve().parent
INSTANCE = Path(r"C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2")
sys.path.insert(0,str(INSTANCE / "output/dungeon-expansion-20261101/tools"))
from nbtio import AIR, Structure, Tag, plain, read_nbt, state, string, write_nbt
import flat_world as writer

NAME = "FT_S5_Coral_Connected_20261105"
TITLE = "FT 第五组 · 珊瑚海底 · 九域连通地牢"
STAGING = ROOT / "deployment/world-build" / NAME
TARGET = INSTANCE / "saves" / NAME


def digest(path):
    with path.open("rb") as stream:
        return hashlib.file_digest(stream,"sha256").hexdigest()


def save(path,value):
    path.parent.mkdir(parents=True,exist_ok=True)
    path.write_text(json.dumps(value,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")


def main():
    assert not TARGET.exists() and not STAGING.exists()
    complete = Structure.read(ROOT / "coral-connected.nbt")
    layout = json.loads((ROOT / "layout.json").read_text(encoding="utf-8"))
    sha256 = digest(ROOT / "coral-connected.nbt")
    assert sha256==layout["sha256"]
    assert len(complete.blocks)==262*34*250 and not complete.entities
    native = json.loads((ROOT / "qa/independent-native-frozen.json").read_text(encoding="utf-8"))[0]
    routes = json.loads((ROOT / "qa/independent-routes.json").read_text(encoding="utf-8"))[0]
    assert native["sha256"]==sha256 and native["registry_properties"]=="PASS" and native["template_roundtrip_block_count"]=="PASS"
    assert not native["survival_failures"] and not native["survival_unsupported"]
    assert routes["sha256"]==sha256 and routes["start_clear"] and all(entry["standing_clear"] and entry["reachable"] for entry in routes["targets"])
    declared = {tuple(entry["position"]):entry for entry in layout["source_edits"]}
    preservation = []
    for module in layout["modules"]:
        source = Path(module["path"])
        assert digest(source)==module["sha256"]
        original = Structure.read(source)
        changed = 0
        for pos_x in range(original.size[0]):
            for pos_y in range(original.size[1]):
                for pos_z in range(original.size[2]):
                    local = (pos_x,pos_y,pos_z)
                    destination = tuple(value+shift for value,shift in zip(local,module["origin"]))
                    expected = original.get(local)
                    actual = complete.get(destination)
                    if actual!=expected:
                        assert destination in declared,(module["image"],destination,expected,actual)
                        assert plain(actual.as_tag())==declared[destination]["after"]
                        changed += 1
                    payload = original.block_entities.get(local)
                    if payload:
                        values = copy.deepcopy(plain(payload))
                        for axis,value in zip(("x","y","z"),destination):
                            if axis in values:
                                values[axis] = value
                        assert plain(complete.block_entities[destination])==values
        preservation.append({"module":module["image"],"source_unchanged":True,"declared_modified_cells":changed})
    assembly = json.loads((ROOT / "assembly.json").read_text(encoding="utf-8"))
    compared = 0
    for entry in assembly["parts"]:
        path = ROOT / entry["path"]
        assert digest(path)==entry["sha256"]
        part = Structure.read(path)
        assert tuple(entry["size"])==part.size and all(value<=48 for value in part.size)
        for position,block in part.blocks.items():
            destination = tuple(value+shift for value,shift in zip(position,entry["origin"]))
            assert complete.get(destination)==block
            if position in part.block_entities:
                values = copy.deepcopy(plain(part.block_entities[position]))
                for axis,value in zip(("x","y","z"),destination):
                    if axis in values:
                        values[axis] = value
                assert plain(complete.block_entities[destination])==values
            compared += 1
    assert compared==len(complete.blocks)
    with zipfile.ZipFile(ROOT / "s5-connected-datapack.zip") as archive:
        assert archive.testzip() is None
        assert hashlib.sha256(archive.read("data/s5_connected/structures/full.nbt")).hexdigest()==sha256
    barrier_count = sum(block.name=="minecraft:barrier" for block in complete.blocks.values())
    size = complete.size
    expected_barriers = size[0]*size[1]*size[2]-(size[0]-2)*(size[1]-2)*(size[2]-2)
    assert barrier_count==expected_barriers
    for position,block in complete.blocks.items():
        if any(value in (0,size[axis]-1) for axis,value in enumerate(position)):
            assert block.name=="minecraft:barrier"
    water = state("water",level=0)
    contracts = []
    for module in layout["modules"]:
        east,up,south = module["origin"]
        width,height,depth = module["size"]
        checked = 0
        for pos_x in range(east-8,east+width+8):
            for pos_z in range(south-8,south+depth+8):
                outside = not (east<=pos_x<east+width and south<=pos_z<south+depth)
                if outside:
                    for pos_y in range(up+3,up+height+8):
                        assert complete.get((pos_x,pos_y,pos_z))==water,(module["image"],"ring",(pos_x,pos_y,pos_z))
                        checked += 1
                else:
                    for pos_y in range(up+height,up+height+8):
                        assert complete.get((pos_x,pos_y,pos_z))==water,(module["image"],"cover",(pos_x,pos_y,pos_z))
                        checked += 1
                    for pos_y in range(up,up+3):
                        assert complete.get((pos_x,pos_y,pos_z)).name not in ("minecraft:air","minecraft:water"),(module["image"],"bed",(pos_x,pos_y,pos_z))
        for port in module["ports"]:
            anchor = [value+shift for value,shift in zip(port["anchor"],module["origin"])]
            side = port["name"].split("-")[0]
            dx,dz = {"north":(0,-1),"south":(0,1),"west":(-1,0),"east":(1,0)}[side]
            for outward in range(1,4):
                for across in range(-(port["width"]//2),port["width"]//2+1):
                    for rise in range(3):
                        position = (anchor[0]+outward*dx+across*abs(dz),anchor[1]+rise,anchor[2]+outward*dz+across*abs(dx))
                        assert complete.get(position)==water,(module["image"],"wet_gate",position)
        contracts.append({"module":module["image"],"source_water_cells_checked":checked,"external_ring":8,"roof_cover":8,"solid_bed_layers":3,"all_ports_outward_water":True})
    save(ROOT / "qa/ocean-integration.json",{"source_sha256":sha256,"modules":contracts,"outer_shell_cells":barrier_count,"oxygen_guarantee":False,"runtime_tested":False})
    save(ROOT / "qa/verification.json",{"source_sha256":sha256,"modules":preservation,"all_routes_reachable":len(routes["targets"]),"native_support_points":native["survival_checked"],"parts_exact_cells_compared":compared,"parts":len(assembly["parts"]),"barrier_cells":barrier_count,"source_entity_payloads_preserved":True,"world_runtime_tested":False})
    spec = importlib.util.spec_from_file_location("s1deployment",ROOT.parent / "S1_苔藓古林_连接地牢_20261105/deployment/deploy.py")
    helper = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(helper)
    before = helper.existing_saves()
    save(ROOT / "deployment/existing-saves-before.json",before)
    writer.CHUNK_X,writer.CHUNK_Z = range(-2,19),range(-2,18)
    writer.SPAWN = (107,-56,242)
    world = writer.FlatWorld()
    world.paste(complete,(0,-60,0),"coral-connected.nbt")
    STAGING.mkdir(parents=True)
    regions = world.write_regions(STAGING / "region")
    writer.write_level(STAGING / "level.dat",writer.TEMPLATE)
    _,level = read_nbt(STAGING / "level.dat")
    data = level.value["Data"].value
    data["LevelName"],data["SpawnAngle"],data["Difficulty"] = string(TITLE),Tag(5,180.0),Tag(1,0)
    data["WorldGenSettings"].value["seed"] = writer.long(2026110505)
    for key in ("Enabled","Disabled"):
        entries = data["DataPacks"].value[key].value
        data["DataPacks"].value[key].value = [value for value in entries if not value.value.startswith("file/")]
    data["DataPacks"].value["Enabled"].value.append(string("file/s5-connected-datapack.zip"))
    write_nbt(STAGING / "level.dat",level)
    (STAGING / "datapacks").mkdir()
    shutil.copy2(ROOT / "s5-connected-datapack.zip",STAGING / "datapacks/s5-connected-datapack.zip")
    block_at,entities,chunks = helper.read_world(STAGING)
    for position,expected in complete.blocks.items():
        destination = (position[0],position[1]-60,position[2])
        assert block_at(destination)==expected,position
    assert len(entities)==len(complete.block_entities)
    for position,entity in complete.block_entities.items():
        destination = (position[0],position[1]-60,position[2])
        values = copy.deepcopy(plain(entity))
        values.update(dict(zip(("x","y","z"),destination)))
        values["keepPacked"] = 0
        assert entities[destination]==values
    assert block_at((107,-56,242))==state("water",level=0) and block_at((107,-55,242))==state("water",level=0)
    assert block_at((107,-57,242)).name=="minecraft:dark_prismarine"
    (STAGING / "部署说明.txt").write_text(TITLE+"\n地牢已写入区块，无需生成命令。出生在水下入口，朝北进入门庭。\n备用传送 /tp @s 107.5 -56 242.5 180 0\n和平/创造建筑检查版；原房间源未改，八格源水环和八层顶水覆盖、完整外围屏障。\n没有刷怪/精英/Boss；没有氧气或水下生存保证，不建议直接切换生存进入。未做客户端水流和游泳实测。\n",encoding="utf-8")
    hashes = helper.fingerprint(STAGING)
    archive_path = ROOT / "deployment" / (NAME+".zip")
    with zipfile.ZipFile(archive_path,"x",zipfile.ZIP_DEFLATED) as archive:
        for relative in hashes:
            archive.write(STAGING / relative,NAME+"/"+relative)
    with zipfile.ZipFile(archive_path) as archive:
        assert archive.testzip() is None
        for relative,expected in hashes.items():
            assert hashlib.sha256(archive.read(NAME+"/"+relative)).hexdigest()==expected
    assert helper.existing_saves()==before and not TARGET.exists()
    shutil.copytree(STAGING,TARGET)
    after = helper.existing_saves()
    assert all(after[name]==entry for name,entry in before.items())
    assert helper.fingerprint(TARGET)==hashes
    save(ROOT / "deployment/deployment-report.json",{"installed":True,"world_name":TITLE,"world":str(TARGET),"origin":[0,-60,0],"spawn":[107,-56,242],"size":complete.size,"blocks_compared":len(complete.blocks),"block_entities_compared":len(entities),"chunks":chunks,"regions":regions,"archive":str(archive_path),"source_sha256":sha256,"existing_saves_unchanged":True,"runtime_tested":False,"combat_added":False,"files":hashes})
    print(json.dumps({"installed":True,"world":str(TARGET),"blocks_compared":len(complete.blocks),"entities":len(entities),"routes":len(routes["targets"]),"barriers":barrier_count},ensure_ascii=False))


if __name__=="__main__":
    main()

