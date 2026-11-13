import gc
import hashlib
import io
import json
import struct
import sys
import zipfile
import zlib
from collections import Counter
from pathlib import Path

sys.dont_write_bytecode = True
ROOT = Path(__file__).resolve().parent
BASE = ROOT.parent
INSTANCE = Path(r"C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2")
sys.path.insert(0,str(INSTANCE / "output/dungeon-expansion-20261101/tools"))
from nbtio import AIR, State, Structure, plain, read_payload, read_string

GROUPS = [(1,"S1_苔藓古林_连接地牢_20261105_门框补缝R2","mossy-forest-connected.nbt","FT_S1_Mossy_Connected_20261105"),(2,"S2_砂岩沙漠_连接地牢_20261105","sandstone-connected.nbt","FT_S2_Sandstone_Connected_20261105"),(3,"S3_霜原冻土_连接地牢_20261105","frost-connected.nbt","FT_S3_Frost_Connected_20261105"),(4,"S4_沼泽湿地_连接地牢_20261105","swamp-connected.nbt","FT_S4_Swamp_Connected_20261105"),(5,"S5_珊瑚海底_连接地牢_20261105","coral-connected.nbt","FT_S5_Coral_Connected_20261105"),(6,"S6_熔岩遗迹_连接地牢_20261105","volcanic-connected.nbt","FT_S6_Volcanic_Connected_20261105"),(7,"S7_菌菇遗迹_连接地牢_20261106","fungal-connected.nbt","FT_S7_Fungal_Connected_20261106"),(8,"S8_紫晶洞穴_连接地牢_20261106","crystal-connected.nbt","FT_S8_Crystal_Connected_20261106")]


def load(path):
    return json.loads(path.read_text(encoding="utf-8-sig"))


def digest(path):
    with path.open("rb") as stream:
        return hashlib.file_digest(stream,"sha256").hexdigest()


def world_reader(folder):
    sections,entities = {},{}
    files_before = {path.name:digest(path) for path in (folder / "region").glob("*.mca")}
    for path in (folder / "region").glob("*.mca"):
        payload = path.read_bytes()
        if not payload:
            continue
        assert len(payload)>=8192 and len(payload)%4096==0
        region_x,region_z = map(int,path.name.split(".")[1:3])
        for index in range(1024):
            location = struct.unpack_from(">I",payload,index*4)[0]
            if not location:
                continue
            offset = (location>>8)*4096
            length = struct.unpack_from(">I",payload,offset)[0]
            assert payload[offset+4]==2
            stream = io.BytesIO(zlib.decompress(payload[offset+5:offset+4+length]))
            assert stream.read(1)==b"\x0a"
            read_string(stream)
            chunk = read_payload(stream,10).value
            east,south = chunk["xPos"].value,chunk["zPos"].value
            assert (east,south)==(region_x*32+index%32,region_z*32+index//32)
            for section in chunk["sections"].value:
                if "block_states" not in section.value:
                    continue
                values = section.value["block_states"].value
                palette = [State.from_tag(value) for value in values["palette"].value]
                words = values.get("data")
                sections[(east,south,section.value["Y"].value)] = (palette,words.value if words else [])
            for entity in chunk["block_entities"].value:
                position = tuple(entity.value[axis].value for axis in ("x","y","z"))
                assert position not in entities
                entities[position] = plain(entity)
    def block_at(position):
        east,up,south = position
        value = sections.get((east//16,south//16,up//16))
        if value is None:
            return AIR
        palette,words = value
        if len(palette)==1:
            return palette[0]
        bits = max(4,(len(palette)-1).bit_length())
        index = up%16*256+south%16*16+east%16
        return palette[(words[index//(64//bits)]>>(index%(64//bits)*bits))&((1<<bits)-1)]
    assert files_before=={path.name:digest(path) for path in (folder / "region").glob("*.mca")}
    return block_at,entities,files_before


def main():
    index = load(BASE / "FT地牢_独立复核_20261104_01a1a685/FINAL-80-SOURCE-INDEX.json")
    reports = []
    for group,directory,filename,world_name in GROUPS:
        folder,world = BASE / directory,INSTANCE / "saves" / world_name
        layout = load(folder / "layout.json")
        source_sha = digest(folder / filename)
        assert layout["sha256"]==source_sha
        assert sorted(module["image"] for module in layout["modules"])==list(range(1,10)) and len(layout["links"])==8
        seen = {7}
        for repeat in range(9):
            for link in layout["links"]:
                first,last = link["from"][0],link["to"][0]
                if first in seen or last in seen:
                    seen.update((first,last))
        assert seen==set(range(1,10))
        for module in layout["modules"]:
            accepted = next(item for item in index["items"] if item["group"]==group and item["image"]==module["image"])
            assert module["sha256"]==accepted["sha256"]==digest(Path(module["path"]))
        native = load(folder / "qa/independent-native-frozen.json")[0]
        routes = load(folder / "qa/independent-routes.json")[0]
        specification = load(folder / "qa/route-input.json")[0]
        assert native["sha256"]==routes["sha256"]==specification["sha256"]==source_sha
        assert native["registry_properties"]=="PASS" and not native["survival_failures"] and not native["survival_unsupported"]
        assert routes["start_clear"] and len(routes["targets"])==len(specification["targets"]) and all(target["standing_clear"] and target["reachable"] for target in routes["targets"])
        for expected,actual in zip(specification["targets"],routes["targets"]):
            assert expected["position"]==actual["position"] and expected["label"]==actual["label"]
        assembly = load(folder / "assembly.json")
        assert assembly["source_sha256"]==source_sha
        origins = [tuple(part["origin"]) for part in assembly["parts"]]
        assert len(origins)==len(set(origins))
        for part in assembly["parts"]:
            assert digest(folder / part["path"])==part["sha256"]
        source = Structure.read(folder / filename)
        block_at,entities,region_hashes = world_reader(world)
        special_barriers = set(map(tuple,load(BASE / "S1_苔藓古林_连接地牢_20261105_屏障R4/barrier-local-positions.json"))) if group==1 else set()
        mismatches,examples = Counter(),[]
        for position,expected in source.blocks.items():
            destination = (position[0],position[1]-60,position[2])
            actual = block_at(destination)
            if group==1 and position in special_barriers:
                assert actual.name=="minecraft:barrier"
                continue
            if actual!=expected:
                mismatches[expected.name+" -> "+actual.name] += 1
                if len(examples)<12:
                    examples.append({"position":destination,"expected":plain(expected.as_tag()),"actual":plain(actual.as_tag())})
        for position in special_barriers:
            assert block_at((position[0],position[1]-60,position[2])).name=="minecraft:barrier"
        original_entities_preserved = 0
        for position,entity in source.block_entities.items():
            destination = (position[0],position[1]-60,position[2])
            values = plain(entity)
            for key,value in values.items():
                if key in ("x","y","z"):
                    value = destination[("x","y","z").index(key)]
                assert entities[destination].get(key)==value,(group,destination,key)
            original_entities_preserved += 1
        if group==1:
            pack = world / "datapacks/s1-connected-datapack.zip"
            report = load(BASE / "S1_苔藓古林_连接地牢_20261105_屏障R4/deployment-report.json")
            assert report["installed"] and report["barrier_blocks"]==len(special_barriers)
            combat = load(BASE / "S1_苔藓古林_连接地牢_20261105_战斗R3/deployment-report.json")
            assert digest(Path(combat["pack"]))==combat["pack_sha256"]
        else:
            deployment = load(folder / "deployment/deployment-report.json")
            assert deployment["installed"] and deployment["source_sha256"]==source_sha and Path(deployment["world"])==world
            pack = world / f"datapacks/s{group}-connected-datapack.zip"
        with zipfile.ZipFile(pack) as archive:
            assert archive.testzip() is None
            assert hashlib.sha256(archive.read(f"data/s{group}_connected/structures/full.nbt")).hexdigest()==source_sha
        render = load(folder / "qa/render.json")
        assert render["source_sha256"]==source_sha and render["views"]
        for view in render["views"]:
            assert (folder / (view["id"]+".png")).is_file()
        report = {"group":group,"directory":str(folder),"world":str(world),"source_sha256":source_sha,"modules":9,"graph_links":8,"connected_modules":sorted(seen),"indexed_sources_rehashed":9,"route_targets":len(routes["targets"]),"native_support_points":native["survival_checked"],"parts_rehashed":len(assembly["parts"]),"source_cells_compared":len(source.blocks),"world_block_mismatches":dict(mismatches),"mismatch_examples":examples,"original_entity_payloads_preserved":original_entities_preserved,"world_entities":len(entities),"r4_barriers_compared":len(special_barriers),"datapack_crc_and_full_source_hash":True,"region_hashes_during_read":region_hashes,"runtime_tested":False}
        reports.append(report)
        (ROOT / f"group-{group:02}.json").write_text(json.dumps(report,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
        print(json.dumps({"group":group,"routes":len(routes["targets"]),"world_mismatches":dict(mismatches),"cells":len(source.blocks)},ensure_ascii=False),flush=True)
        del source,block_at,entities
        gc.collect()
    complete = all(not report["world_block_mismatches"] for report in reports)
    result = {"objective":"按步骤逐步完成8组地牢房间的组装合并。","architectural_assembly_verified":complete,"groups":reports,"total_modules":72,"total_graph_links":64,"total_route_targets":sum(report["route_targets"] for report in reports),"runtime_tested":False,"combat_scope":"Only group 1 has a separately adapted combat pack; groups 2-8 remain architectural test worlds. This audit does not certify client gameplay or combat."}
    (ROOT / "final-audit.json").write_text(json.dumps(result,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
    if not complete:
        raise AssertionError("World/source mismatches require investigation; inspect per-group reports")


if __name__=="__main__":
    main()
