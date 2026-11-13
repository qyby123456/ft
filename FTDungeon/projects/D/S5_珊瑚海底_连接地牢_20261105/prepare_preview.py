import hashlib
import json
import sys
from pathlib import Path

sys.dont_write_bytecode = True
ROOT = Path(__file__).resolve().parent
sys.path.insert(0,str(Path(r"C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\versions\FarmingTales_ForgeⅡ v1.5.2") / "output/dungeon-expansion-20261101/tools"))
from nbtio import Structure


def main():
    layout = json.loads((ROOT / "layout.json").read_text(encoding="utf-8"))
    source = Structure.read(ROOT / "coral-connected.nbt")
    folder = ROOT / "preview-sources"
    folder.mkdir(exist_ok=True)
    geometries = {"geometry-water-hidden":Structure(source.size),"cutaway-water-hidden":Structure(source.size)}
    for position,block in source.blocks.items():
        if block.name in ("minecraft:water","minecraft:barrier","minecraft:air"):
            continue
        east,up,south = position
        module = next((entry for entry in layout["modules"] if all(origin<=value<origin+size for origin,value,size in zip(entry["origin"],position,entry["size"]))),None)
        if module is None and up!=3:
            continue
        for name,structure in geometries.items():
            if name.startswith("cutaway") and module and up>=module["origin"][1]+8:
                continue
            structure.set(position,block,source.block_entities.get(position))
    evidence = []
    for name,structure in geometries.items():
        path = folder / (name+".nbt")
        structure.write(path)
        evidence.append({"id":name,"file":str(path),"origin":[0,0,0],"sha256":hashlib.sha256(path.read_bytes()).hexdigest(),"records":len(structure.blocks),"display_only":True})
    for number in (1,7,8,9):
        module = next(entry for entry in layout["modules"] if entry["image"]==number)
        origin = [module["origin"][0]-8,0,module["origin"][2]-8]
        size = (module["size"][0]+16,34,module["size"][2]+16)
        structure = Structure(size)
        for east in range(size[0]):
            for up in range(size[1]):
                for south in range(size[2]):
                    local = (east,up,south)
                    position = tuple(value+shift for value,shift in zip(local,origin))
                    structure.set(local,source.get(position))
        path = folder / f"actual-water-{number:02}.nbt"
        structure.write(path)
        evidence.append({"id":f"actual-water-{number:02}","file":str(path),"origin":origin,"size":size,"sha256":hashlib.sha256(path.read_bytes()).hexdigest(),"records":len(structure.blocks),"display_only":False,"rule":"Exact positional crop of full source, no block-state changes; block entities not rendered"})
    (ROOT / "qa/preview-sources.json").write_text(json.dumps({"source_sha256":layout["sha256"],"sources":evidence,"geometry_rule":"Display only: all water/barriers hidden; outside modules only Y3 seabed kept. Cutaway additionally omits module local Y>=8. Never use these previews as build/deployment sources."},ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
    print(json.dumps({"preview_sources":len(evidence)},ensure_ascii=False))


if __name__=="__main__":
    main()
