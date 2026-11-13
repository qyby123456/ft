import hashlib
import itertools
import json
import re
import zipfile
from pathlib import Path

from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
PACK = ROOT / "out/datapack"


def main():
    geometry = json.loads((ROOT / "qa/geometry.json").read_text(encoding="utf-8"))
    datapack = json.loads((ROOT / "qa/datapack.json").read_text(encoding="utf-8"))
    native = json.loads((ROOT / "qa/minecraft-format-verification.json").read_text(encoding="utf-8"))
    assert geometry["status"] == datapack["status"] == native["status"] == "PASS"
    air_tag = json.loads((PACK / "data/ftobsidian/tags/blocks/air.json").read_text(encoding="utf-8"))
    assert set(air_tag["values"]) == {"minecraft:air", "minecraft:cave_air", "minecraft:void_air"}
    checked = set()
    for path in sorted((PACK / "data/ftobsidian/functions/internal").glob("scan_*.mcfunction")):
        for line in path.read_text(encoding="utf-8").splitlines():
            match = re.fullmatch(r"execute unless block ~([0-9]+) ~([0-9]+) ~([0-9]+) #ftobsidian:air run scoreboard players set #ok ftob.build 0", line)
            assert match is not None, (str(path), line)
            checked.add(tuple(int(value) for value in match.groups()))
    assert checked == set(itertools.product(range(35), range(18), range(47)))
    build = (PACK / "data/ftobsidian/functions/internal/build.mcfunction").read_text(encoding="utf-8")
    assert build.count("scoreboard players set #bounds ftob.build 0\nexecute store success") == 2
    assert build.count("execute unless loaded ") == 16
    assert build.index("run function ftobsidian:internal/scan") < build.index("run place template ftobsidian:chamber")
    assert "execute if score #ok ftob.build matches 1 store success score #placed ftob.build run place template" in build
    assert build.count("run place template ") == 1
    images = []
    for path in sorted((ROOT / "preview").glob("*.png")):
        with Image.open(path) as image:
            image.verify()
        with Image.open(path) as image:
            images.append({"name": path.name, "size": list(image.size)})
    assert len(images) == 4
    pack_zip = ROOT / "out/ft-obsidian-trial-1.20.1.zip"
    with zipfile.ZipFile(pack_zip) as bundle:
        assert bundle.testzip() is None
        assert "pack.mcmeta" in bundle.namelist()
        assert all(not path.startswith(("/", "../")) for path in bundle.namelist())
        for name in bundle.namelist():
            assert bundle.read(name) == (PACK / name).read_bytes(), name
    counts = geometry["materials"]
    materials = "block_id,count\n" + "".join(f"minecraft:{name},{count}\n" for name, count in counts.items())
    (ROOT / "out/materials.csv").write_text(materials, encoding="utf-8-sig")
    report = {"status": "PASS", "protected_volume_cells": len(checked), "bounds_error_fails_closed": True, "loaded_chunk_check_samples": 16, "build_only_after_full_preflight": True, "native_verification": native, "images": images, "zip_integrity": "PASS", "deployed": False, "player_world_opened_or_modified": False, "paid_image_api_requests": 0}
    (ROOT / "qa/final-verification.json").write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    files = [ROOT / "README.md", pack_zip, ROOT / "out/materials.csv", ROOT / "out/manifest.json"]
    files += sorted((ROOT / "preview").glob("*.png"))
    files += sorted((ROOT / "tools").glob("*.py")) + sorted((ROOT / "tools").glob("*.java"))
    files += [ROOT / "qa" / name for name in ("geometry.json", "datapack.json", "minecraft-format-verification.json", "final-verification.json")]
    delivery = ROOT / "out/FT_Obsidian_Trial_20261101.zip"
    with zipfile.ZipFile(delivery, "w", zipfile.ZIP_DEFLATED) as bundle:
        for path in files:
            info = zipfile.ZipInfo(path.relative_to(ROOT).as_posix(), (2026, 11, 1, 0, 0, 0))
            info.compress_type = zipfile.ZIP_DEFLATED
            bundle.writestr(info, path.read_bytes())
    with zipfile.ZipFile(delivery) as bundle:
        assert bundle.testzip() is None
        assert all(not name.endswith((".jar", ".class")) for name in bundle.namelist())
    print(json.dumps({"status": "PASS", "delivery": str(delivery), "bytes": delivery.stat().st_size, "sha256": hashlib.sha256(delivery.read_bytes()).hexdigest(), "checks": ["air_volume", "guard_order", "native_format", "png_decode", "zip_integrity", "no_game_jars"]}, ensure_ascii=False))


if __name__ == "__main__":
    main()
