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

NAME = "FT_S7_Fungal_Connected_20261106"
TITLE = "FT 第七组 · 菌菇遗迹 · 九域连通地牢"
STAGING = ROOT / "deployment/world-build-compatible-r2" / NAME
TARGET = INSTANCE / "saves" / NAME


def digest(path):
    with path.open("rb") as stream:
        return hashlib.file_digest(stream,"sha256").hexdigest()


def save(path,value):
    path.parent.mkdir(parents=True,exist_ok=True)
    path.write_text(json.dumps(value,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")


def main():
    assert not TARGET.exists() and not STAGING.exists()
    complete = Structure.read(ROOT / "fungal-connected.nbt")
    layout = json.loads((ROOT / "layout.json").read_text(encoding="utf-8"))
    sha256 = digest(ROOT / "fungal-connected.nbt")
    assert sha256==layout["sha256"]
    assert len(complete.blocks)==264*34*222 and not complete.entities
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
                        assert plain(actual.as_tag())==declared[destination]["after"] and plain(expected.as_tag())==declared[destination]["before"]
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
    origins = [tuple(entry["origin"]) for entry in assembly["parts"]]
    assert len(origins)==len(set(origins)) and set(origins)=={(east,0,south) for east in range(0,264,48) for south in range(0,222,48)}
    compared = 0
    for entry in assembly["parts"]:
        path = ROOT / entry["path"]
        assert digest(path)==entry["sha256"]
        part = Structure.read(path)
        assert tuple(entry["size"])==part.size==(min(48,264-entry["origin"][0]),34,min(48,222-entry["origin"][2]))
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
    with zipfile.ZipFile(ROOT / "s7-connected-datapack.zip") as archive:
        assert archive.testzip() is None
        assert hashlib.sha256(archive.read("data/s7_connected/structures/full.nbt")).hexdigest()==sha256
    barrier_count = sum(block.name=="minecraft:barrier" for block in complete.blocks.values())
    assert barrier_count==layout["barrier_cells"]
    save(ROOT / "qa/verification.json",{"source_sha256":sha256,"modules":preservation,"all_routes_reachable":len(routes["targets"]),"native_support_points":native["survival_checked"],"parts_exact_cells_compared":compared,"parts":len(assembly["parts"]),"barrier_cells":barrier_count,"source_entity_payloads_preserved":True,"world_runtime_tested":False})
    spec = importlib.util.spec_from_file_location("s1deployment",ROOT.parent / "S1_苔藓古林_连接地牢_20261105/deployment/deploy.py")
    helper = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(helper)
    before = helper.existing_saves()
    save(ROOT / "deployment/existing-saves-before.json",before)
    writer.CHUNK_X,writer.CHUNK_Z = range(-2,19),range(-2,16)
    writer.SPAWN = (132,-54,218)
    world = writer.FlatWorld()
    world.paste(complete,(0,-60,0),"fungal-connected.nbt")
    STAGING.mkdir(parents=True)
    regions = world.write_regions(STAGING / "region")
    writer.write_level(STAGING / "level.dat",writer.TEMPLATE)
    _,level = read_nbt(STAGING / "level.dat")
    data = level.value["Data"].value
    data["LevelName"],data["SpawnAngle"],data["Difficulty"] = string(TITLE),Tag(5,180.0),Tag(1,0)
    data["WorldGenSettings"].value["seed"] = writer.long(2026110607)
    for key in ("Enabled","Disabled"):
        entries = data["DataPacks"].value[key].value
        data["DataPacks"].value[key].value = [value for value in entries if not value.value.startswith("file/")]
    data["DataPacks"].value["Enabled"].value.append(string("file/s7-connected-datapack.zip"))
    write_nbt(STAGING / "level.dat",level)
    (STAGING / "datapacks").mkdir()
    shutil.copy2(ROOT / "s7-connected-datapack.zip",STAGING / "datapacks/s7-connected-datapack.zip")
    block_at,entities,chunks = helper.read_world(STAGING)
    for position,expected in complete.blocks.items():
        destination = (position[0],position[1]-60,position[2])
        assert block_at(destination)==expected,position
    source_entity_positions = {(position[0],position[1]-60,position[2]) for position in complete.block_entities}
    normalized_entities = {}
    for position,block in complete.blocks.items():
        if block.name in writer.NORMALIZED_ENTITIES and position not in complete.block_entities:
            destination = (position[0],position[1]-60,position[2])
            normalized_entities[destination] = {"id":writer.NORMALIZED_ENTITIES[block.name],"x":destination[0],"y":destination[1],"z":destination[2],"keepPacked":0}
    assert set(entities)==source_entity_positions | set(normalized_entities)
    assert all(entities[position]==expected for position,expected in normalized_entities.items())
    save(ROOT / "qa/deployment-compatibility.json",{"source_sha256":sha256,"original_payloads":len(complete.block_entities),"extra_entities":[{"position":position,"payload":payload} for position,payload in normalized_entities.items()],"rule":"Existing instance FlatWorld adapter inserts visualworkbench crafting-table entities; no original block or payload replaced. Not client runtime tested."})
    for position,entity in complete.block_entities.items():
        destination = (position[0],position[1]-60,position[2])
        values = copy.deepcopy(plain(entity))
        values.update(dict(zip(("x","y","z"),destination)))
        values["keepPacked"] = 0
        assert entities[destination]==values
    assert block_at((132,-54,218))==AIR and block_at((132,-53,218))==AIR
    assert block_at((132,-55,218)).name=="minecraft:oak_planks"
    (STAGING / "部署说明.txt").write_text(TITLE+"\n地牢已写入区块，无需生成命令。出生在菌境门厅外平台，朝北进入。\n备用传送 /tp @s 132.5 -54 218.5 180 0\n和平/创造建筑检查版，未添加第七组怪物/精英/Boss。\n接缝补框、未用口封闭、外围屏障；保留入口半阶、原双梯和上层环廊。未做客户端水流、植物随机刻和实战验证。\n",encoding="utf-8")
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
    save(ROOT / "deployment/deployment-report.json",{"installed":True,"world_name":TITLE,"world":str(TARGET),"origin":[0,-60,0],"spawn":[132,-54,218],"size":complete.size,"blocks_compared":len(complete.blocks),"block_entities_compared":len(entities),"chunks":chunks,"regions":regions,"archive":str(archive_path),"source_sha256":sha256,"existing_saves_unchanged":True,"runtime_tested":False,"combat_added":False,"files":hashes})
    print(json.dumps({"installed":True,"world":str(TARGET),"blocks_compared":len(complete.blocks),"entities":len(entities),"routes":len(routes["targets"]),"barriers":barrier_count},ensure_ascii=False))


if __name__=="__main__":
    main()


