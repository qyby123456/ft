import hashlib
import json
import sys
from collections import Counter
from pathlib import Path

sys.dont_write_bytecode = True
ROOT = Path(__file__).resolve().parent
sys.path.insert(0,str(Path(r"C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2") / "output/dungeon-expansion-20261101/tools"))
from nbtio import AIR, Structure, plain


def main():
    path = ROOT / "fungal-connected.nbt"
    source = Structure.read(path)
    layout = json.loads((ROOT / "layout.json").read_text(encoding="utf-8"))
    sha256 = hashlib.sha256(path.read_bytes()).hexdigest()
    assert sha256==layout["sha256"]
    water = [position for position,block in source.blocks.items() if block.name=="minecraft:water"]
    boundary = 0
    for east,up,south in water:
        assert source.get((east,up,south)).get("level","0")=="0"
        for dx,dy,dz in ((-1,0,0),(1,0,0),(0,0,-1),(0,0,1),(0,-1,0)):
            neighbor = source.get((east+dx,up+dy,south+dz))
            assert neighbor!=AIR and neighbor.name not in ("minecraft:cave_air","minecraft:void_air"),((east,up,south),(dx,dy,dz))
            boundary += 1
    assert not any(block.name in ("minecraft:lava","minecraft:fire","minecraft:soul_fire") or block.name in ("minecraft:campfire","minecraft:soul_campfire") and block.get("lit","true")=="true" for block in source.blocks.values())
    entities = Counter()
    for position,tag in source.block_entities.items():
        payload = plain(tag)
        entities[payload["id"]] += 1
        if payload["id"] in ("minecraft:chest","minecraft:barrel"):
            assert not payload.get("Items",[]) and "LootTable" not in payload
        if payload["id"]=="minecraft:chest":
            assert source.get((position[0],position[1]+1,position[2]))==AIR
    unused_upper = next(entry for entry in layout["source_edits"] if entry["position"]==[104,14,18])
    assert unused_upper["after"]["Name"]=="minecraft:mossy_stone_bricks"
    result = {"source_sha256":sha256,"source_water_cells":len(water),"side_bottom_non_air_neighbors":boundary,"no_air_leak_boundary":True,"actual_flow_and_random_ticks_tested":False,"lit_fire_or_lava_added":False,"block_entity_types":dict(entities),"empty_containers_preserved":True,"upper_and_lower_north_ports_independently_closed":True,"scope":"Static source-state adjacency and payload checks. Non-air boundary alone does not prove full fluid occlusion, actual flow, plant spreading, lid interaction or long-term survival."}
    (ROOT / "qa/environment.json").write_text(json.dumps(result,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
    print(json.dumps(result,ensure_ascii=False))


if __name__=="__main__":
    main()
