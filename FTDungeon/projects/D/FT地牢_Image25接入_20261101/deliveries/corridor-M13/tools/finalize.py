import hashlib
import json
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
FONT_PATH = "C:/Windows/Fonts/msyh.ttc"


def read(relative):
    return json.loads((ROOT / relative).read_text(encoding="utf-8"))


def sha256(relative):
    return hashlib.sha256((ROOT / relative).read_bytes()).hexdigest()


def sheet(kind):
    names = ["north", "south", "west", "east"]
    canvas = Image.new("RGB", (1920, 1514), "#111814")
    draw = ImageDraw.Draw(canvas)
    draw.text((32, 22), "M13 · 苔覆遗迹断拱长廊 / " + ("内部四视图" if kind == "interior" else "外部四视图"), font=ImageFont.truetype(FONT_PATH, 29), fill="#e5ecd9")
    draw.text((32, 70), "真实交付 NBT 渲染 · 13×11×40 · 南北直廊 · 剖切只用于显示，交付保留完整屋顶", font=ImageFont.truetype(FONT_PATH, 19), fill="#a5b69b")
    for index, name in enumerate(names):
        image = Image.open(ROOT / f"qa/views/{kind}-{name}.png").convert("RGB")
        image.thumbnail((930, 672), Image.Resampling.LANCZOS)
        offset = (20 + (index % 2) * 960, 122 + (index // 2) * 690)
        canvas.paste(image, offset)
    target = ROOT / f"qa/{kind}-four-views.png"
    canvas.save(target)
    return str(target)


def main():
    metadata = read("room.json")
    static = read("qa/static.json")
    native = read("qa/native.json")
    captures = read("qa/captures.json")
    assert metadata["sha256"] == sha256("room.nbt") == static["sha256"] == native["structure_sha256"]
    assert static["status"] == native["status"] == "PASS"
    assert not captures["errors"]
    assert len(captures["captures"]) == 9
    for capture in captures["captures"]:
        assert capture["sha256"] == metadata["sha256"]
        assert hashlib.sha256(Path(capture["path"]).read_bytes()).hexdigest() == capture["image_sha256"]
    interior = sheet("interior")
    exterior = sheet("exterior")
    visual = {
        "status": "PASS", "id": "M13", "structure_sha256": metadata["sha256"],
        "source": "Actual final room.nbt through Lodestone 0.6.0 and bundled default-pack resources",
        "interior_four_views": interior, "exterior_four_views": exterior,
        "view_count": 9, "image_dimensions": [1440, 1040],
        "review": {
            "reference_theme": "Gray-green cracked/mossy stone, wall vines, moss floor, dark wood inset panels, hanging warm lanterns, interrupted arch masonry",
            "geometry": "Straight level 40-block north-south axis; both endpoint arches open; six arch stations; no side exits",
            "interior": "North/south eye-level views retain roof and all blocks; east/west cutaways reveal repeated arches and full-width floor",
            "exterior": "Four intact exterior views show complete roof and enclosed sides",
            "legibility": "Lanterns, chain anchors, wall support and broken arch corners visible; no characters or AI replacement images",
            "differences_from_concept": "Concept is not block-perfect: actual arches use legal 1.20.1 stair/slab geometry; side roots represented by dark oak log/plank insets. Review lighting is illustrative, not a modpack client capture."
        },
        "cutaway_does_not_modify_nbt": True,
        "captures": captures["captures"]
    }
    (ROOT / "qa/visual.json").write_text(json.dumps(visual, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    report = {
        "id": "M13", "name": metadata["name"], "status": "PASS", "size": metadata["size"],
        "sha256": metadata["sha256"], "reference_sha256": metadata["reference_sha256"],
        "static": {"status": static["status"], "report": "qa/static.json", "block_records": static["block_records"], "non_air_blocks": static["non_air_blocks"], "palette_states": static["palette_states"]},
        "visual": {"status": visual["status"], "report": "qa/visual.json", "actual_nbt_rendering": True, "interior_views": 4, "exterior_views": 4, "additional_cutaway": 1},
        "forge_native": {"status": native["status"], "report": "qa/native.json", "log": "qa/forge-gametest-java17.log", "minecraft": native["minecraft_version"], "forge": native["forge_version"], "rotations": native["rotations"], "placements": native["native_placements"], "server_ticks": native["total_server_ticks"], "observations": native["state_observations"]},
        "safety": {"entities": 0, "block_entities": 0, "fluids": 0, "unsafe_blocks": 0, "north_south_ports": "3x3 minimum", "clear_lane": "x=5..7 y=1..3 z=0..39, flat continuous full-block floor"},
        "deployment": "NOT_DEPLOYED", "player_saves": "NOT_OPENED_OR_MODIFIED", "production_mods": "NOT_MODIFIED",
        "limitations": native["limits"],
        "live_vines": "Natural random-tick vine additions/face updates are allowed outside the clear lane; load-bearing masonry and lighting must remain stable.",
        "delivery_files": {name: {"sha256": sha256(name), "bytes": (ROOT / name).stat().st_size} for name in ["room.nbt", "room.json", "preview.png"]}
    }
    (ROOT / "qa.json").write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({"status": report["status"], "id": "M13", "sha256": metadata["sha256"], "interior": interior, "exterior": exterior}, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
