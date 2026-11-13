import hashlib
import json
import math
import sys
from pathlib import Path

sys.dont_write_bytecode = True
ROOT = Path(__file__).resolve().parent
sys.path.insert(0,str(Path(r"C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2") / "output/dungeon-expansion-20261101/tools"))
from nbtio import Structure


def load(relative):
    return json.loads((ROOT / relative).read_text(encoding="utf-8"))


def main():
    layout = load("layout.json")
    sha256 = hashlib.sha256((ROOT / "coral-connected.nbt").read_bytes()).hexdigest()
    assert sha256==layout["sha256"]
    structure = Structure.read(ROOT / "coral-connected.nbt")
    assembly = load("assembly.json")
    expected = {(east,0,south) for east in range(0,262,48) for south in range(0,250,48)}
    origins = [tuple(entry["origin"]) for entry in assembly["parts"]]
    assert len(origins)==len(set(origins)) and set(origins)==expected
    for entry in assembly["parts"]:
        assert entry["size"]==[min(48,262-entry["origin"][0]),34,min(48,250-entry["origin"][2])]
    specification = load("qa/route-input.json")[0]
    report = load("qa/independent-routes.json")[0]
    assert report["sha256"]==sha256 and len(report["targets"])==len(specification["targets"])
    assert all(target["standing_clear"] and target["reachable"] for target in report["targets"])
    connected = {(215,8,485)}
    paths = load("qa/swim-paths.json")
    assert len(paths)==len(specification["targets"])
    wet_samples = 0
    def is_wet(position):
        block = structure.get(position)
        return block.name in ("minecraft:water","minecraft:kelp","minecraft:kelp_plant","minecraft:seagrass","minecraft:tall_seagrass","minecraft:bubble_column") or block.get("waterlogged")=="true"
    for target,entry in zip(specification["targets"],paths):
        assert entry["label"]==target["label"]
        positions = [tuple(point[key] for key in ("eastHalf","upHalf","southHalf")) for point in entry["path_half_grid"]]
        assert positions and positions[0] in connected
        assert positions[-1]==(round(target["position"][0]*2)+1,round(target["position"][1]*2),round(target["position"][2]*2)+1)
        for first,second in zip(positions,positions[1:]):
            assert sum(abs(left-right) for left,right in zip(first,second))==1
            for sample in range(6):
                east,up,south = [(left+(right-left)*sample/5)/2 for left,right in zip(first,second)]
                for rise in (0.001,1.799):
                    position = (math.floor(east),math.floor(up+rise),math.floor(south))
                    assert is_wet(position),(entry["label"],position,structure.get(position))
                    wet_samples += 1
        connected.update(positions)
    for relative in ("qa/ocean-integration.json","qa/verification.json","qa/render.json","deployment/deployment-report.json"):
        assert load(relative)["source_sha256"]==sha256
    for entry in load("qa/preview-sources.json")["sources"]:
        assert hashlib.sha256(Path(entry["file"]).read_bytes()).hexdigest()==entry["sha256"]
    for view in load("qa/render.json")["views"]:
        assert (ROOT / (view["id"]+".png")).stat().st_size>10000
    deployment = load("deployment/deployment-report.json")
    for relative,expected_hash in deployment["files"].items():
        assert hashlib.sha256((Path(deployment["world"])/relative).read_bytes()).hexdigest()==expected_hash
    result = {"source_sha256":sha256,"part_grid_disjoint_exact_cover":True,"targets":len(paths),"connected_path_positions":len(connected),"sampled_body_center_feet_and_head_wet":wet_samples,"render_sources_hash_bound":True,"installed_files_rehashed":True,"runtime_tested":False,"oxygen_guarantee":False}
    (ROOT / "qa/final-audit.json").write_text(json.dumps(result,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
    print(json.dumps(result,ensure_ascii=False))


if __name__=="__main__":
    main()
