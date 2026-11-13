import hashlib
import json
import sys
import zipfile
from pathlib import Path

sys.dont_write_bytecode = True
ROOT = Path(__file__).resolve().parent
sys.path.insert(0,str(Path(r"C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2") / "output/dungeon-expansion-20261101/tools"))
from nbtio import AIR, Structure


def load(relative):
    return json.loads((ROOT / relative).read_text(encoding="utf-8"))


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    layout = load("layout.json")
    sha256 = digest(ROOT / "volcanic-connected.nbt")
    assert layout["sha256"]==sha256
    structure = Structure.read(ROOT / "volcanic-connected.nbt")
    envelope = set()
    for module in layout["modules"]:
        east,up,south = module["origin"]
        width,height,depth = module["size"]
        envelope.update((pos_x,pos_y,pos_z) for pos_x in range(east,east+width) for pos_y in range(up,up+height) for pos_z in range(south,south+depth))
    for link in layout["links"]:
        points = []
        for first,last in zip(link["waypoints_xz"],link["waypoints_xz"][1:]):
            length = abs(last[0]-first[0])+abs(last[1]-first[1])
            assert first[0]==last[0] or first[1]==last[1]
            points.extend((first[0]+(last[0]-first[0])*step//length,first[1]+(last[1]-first[1])*step//length) for step in range(length+1))
        footprint = {(east+dx,south+dz) for east,south in points for dx in range(-4,5) for dz in range(-4,5)}
        envelope.update((east,up,south) for east,south in footprint for up in range(1,link["feet"]+8))
    east,feet,south = layout["entrance"]
    envelope.update((pos_x,up,pos_z) for pos_x in range(east-6,east+7) for pos_z in range(south,south+4) for up in range(1,feet+7))
    boundary_faces = 0
    for east,up,south in envelope:
        for dx,dy,dz in ((1,0,0),(-1,0,0),(0,1,0),(0,-1,0),(0,0,1),(0,0,-1)):
            adjacent = (east+dx,up+dy,south+dz)
            if adjacent not in envelope:
                assert structure.get(adjacent).name=="minecraft:barrier",adjacent
                boundary_faces += 1
    frames_checked = 0
    for frame in layout["junction_frames"]:
        for position in frame["positions"]:
            assert structure.get(tuple(position)).name in ("minecraft:polished_blackstone_bricks","minecraft:orange_stained_glass")
            frames_checked += 1
    used = {tuple(key) for link in layout["links"] for key in (link["from"],link["to"])} | {(7,"south")}
    unused = []
    for module in layout["modules"]:
        for port in module["ports"]:
            if (module["image"],port["id"]) in used:
                continue
            center = [value+shift for value,shift in zip(port["feet"],module["origin"])]
            for rise in range(port["clearHeight"]+(2 if port["crown_width"] else 0)):
                width = port["width"] if rise<port["clearHeight"] else port["crown_width"]
                for across in range(-(width//2),(width-1)//2+1):
                    east,up,south = center
                    position = (east+across,up+rise,south) if port["side"] in ("north","south") else (east,up+rise,south+across)
                    assert structure.get(position)!=AIR,position
            unused.append([module["image"],port["id"]])
    for relative in ("qa/verification.json","qa/volcanic-safety.json","qa/render.json","deployment/deployment-report.json"):
        assert load(relative)["source_sha256"]==sha256
    routes = load("qa/independent-routes.json")[0]
    assert routes["sha256"]==sha256 and routes["start_clear"] and all(entry["standing_clear"] and entry["reachable"] for entry in routes["targets"])
    assert routes["targets"]==[{**entry,"standing_clear":True,"reachable":True} for entry in load("qa/route-input.json")[0]["targets"]]
    for view in load("qa/render.json")["views"]:
        assert (ROOT / (view["id"]+".png")).stat().st_size>10000
    deployment = load("deployment/deployment-report.json")
    for relative,expected in deployment["files"].items():
        assert digest(Path(deployment["world"])/relative)==expected
    with zipfile.ZipFile(deployment["archive"]) as archive:
        assert archive.testzip() is None
        for relative,expected in deployment["files"].items():
            assert hashlib.sha256(archive.read(Path(deployment["world"]).name+"/"+relative)).hexdigest()==expected
    result = {"source_sha256":sha256,"verified_targets":len(routes["targets"]),"barrier_boundary_faces":boundary_faces,"external_frame_cells":frames_checked,"unused_ports_closed":unused,"installed_files_rehashed":True,"archive_crc_and_hashes":True,"runtime_tested":False}
    (ROOT / "qa/final-audit.json").write_text(json.dumps(result,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
    print(json.dumps(result,ensure_ascii=False))


if __name__=="__main__":
    main()
