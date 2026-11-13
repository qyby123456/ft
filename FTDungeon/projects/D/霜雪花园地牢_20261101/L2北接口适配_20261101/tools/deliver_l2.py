import hashlib
import json
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "out"
DESTINATION = Path("D:/ft工程文件/FT地牢_Image25接入_20261101/incoming/L2")


def sha256(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    metadata = json.loads((OUT / "room.json").read_text(encoding="utf-8"))
    static = json.loads((OUT / "qa-static.json").read_text(encoding="utf-8"))
    native = json.loads((OUT / "qa-minecraft.json").read_text(encoding="utf-8"))
    digest = sha256(OUT / "room.nbt")
    assert metadata["sha256"] == static["sha256"] == native["sha256"] == digest
    assert native["status"] == "PASS"
    assert metadata["size"] == [21, 12, 21]
    assert metadata["entrance"] == [10, 1, 0]
    assert metadata["interior"] == [10, 2, 6]
    assert metadata["facing"] == "north"
    assert Path(metadata["reference"]).is_file()
    assert (OUT / "preview.png").is_file() and (OUT / "four-views.png").is_file()
    report = dict(static)
    report["status"] = "PASS"
    report["minecraft_registry"] = native
    report["visual_review"] = {
        "status": "PASS",
        "reviewed": ["preview.png", "front.png", "back.png", "left.png", "right.png"],
        "final_nbt_decoded_for_render": True,
        "main_preview_cutaway_only": "North and east upper perimeter walls hidden only in preview; actual NBT and four directional views retain complete walls.",
        "checks": ["No cropped trees/pergola", "All four beds present", "North 3x3 interface clear", "Internal step remains in template", "Dry path to atrium and around fountain", "Pond and fountain ice caps retained"],
        "limitation": "Software vanilla-texture illustration; selected non-cube meshes approximated. Not a Minecraft client screenshot.",
    }
    report["reference_sha256"] = sha256(Path(metadata["reference"]))
    report["incoming_directory"] = str(DESTINATION)
    report["producer_source"] = str(ROOT)
    report["publication"] = "incoming only; no core edits, no jar/pack installation, no player save access"
    report["integration_notes"] = [
        "Template north is local z=0; entrance feet at [10,1,0], full block at y=0 under all three entry columns.",
        "Internal south-facing stone-brick stair at local z=1 raises to main feet y=2. Keep the connector floor aligned with entrance.y-1.",
        "SizeY=12 includes tall spruce and snow cap. Perimeter wall above main floor is 5 full blocks; courtyard is open to sky.",
        "All 5292 volume cells, including air, are explicit. Integrator must preflight the entire volume before first placement; this file is not a clear command.",
        "17 source-water cells are sealed on all six faces by source water or actual full-support faces, with non-melting packed-ice top caps.",
        "No reward containers, mobs, spawners, command blocks, jigsaw blocks or structure blocks are included.",
        "When integrating underground/no-skylight, retain local vanilla light blocks supporting flower illumination; no dimension weather or time overrides included.",
    ]
    files = {name: (OUT / name).read_bytes() for name in ("room.nbt", "room.json", "preview.png", "four-views.png")}
    report["artifact_sha256"] = {name: hashlib.sha256(data).hexdigest() for name, data in files.items()}
    files["qa.json"] = (json.dumps(report, ensure_ascii=False, indent=2) + "\n").encode("utf-8")
    for name, data in files.items():
        target = DESTINATION / name
        if target.exists() and target.read_bytes() != data:
            raise RuntimeError("Refusing to overwrite a different incoming L2 artifact: " + str(target))
    (OUT / "qa.json").write_bytes(files["qa.json"])
    DESTINATION.mkdir(parents=True, exist_ok=True)
    for name, data in files.items():
        target = DESTINATION / name
        if not target.exists():
            with target.open("xb") as stream:
                stream.write(data)
    assert sha256(DESTINATION / "room.nbt") == digest
    assert all((DESTINATION / name).read_bytes() == data for name, data in files.items())
    print(json.dumps({"status": "PASS", "id": "L2", "destination": str(DESTINATION), "files": list(files), "size": metadata["size"], "entrance": metadata["entrance"], "interior": metadata["interior"], "sha256": digest}, ensure_ascii=False))


if __name__ == "__main__":
    main()
