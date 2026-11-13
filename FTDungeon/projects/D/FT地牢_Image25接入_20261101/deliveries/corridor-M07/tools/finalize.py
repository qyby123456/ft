import hashlib
import json
from datetime import datetime, timezone, timedelta
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
FONT = "C:/Windows/Fonts/msyh.ttc"

def load(relative):
    return json.loads((ROOT / relative).read_text(encoding="utf-8"))

def write(relative, data):
    (ROOT / relative).write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

def sha256(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

def make_sheet():
    sheet = Image.new("RGB", (1920, 1370), "#111720")
    draw = ImageDraw.Draw(sheet)
    title_font = ImageFont.truetype(FONT, 34)
    label_font = ImageFont.truetype(FONT, 25)
    detail_font = ImageFont.truetype(FONT, 21)
    draw.text((36, 23), "corridor-M07  灾变暗铁残拱长廊  |  四向结构审视", fill="#f2d18c", font=title_font)
    draw.text((36, 78), "真实 room.nbt · Minecraft Builder / Lodestone 软件渲染 · 17 × 13 × 41 · 剖切仅用于显示", fill="#bac8d9", font=detail_font)
    for index, (direction, label) in enumerate([("north", "北向斜视 / 北入口"), ("south", "南向斜视 / 南出口"), ("west", "西向剖视 / 连续柱列"), ("east", "东向剖视 / 主路与残拱")]):
        image = Image.open(ROOT / "qa/views" / (direction + ".png")).convert("RGB")
        assert image.size == (1536, 1024)
        image = image.crop((170, 180, 1366, 920))
        image.thumbnail((900, 510), Image.Resampling.LANCZOS)
        cell_x = 28 + (index % 2) * 954
        cell_y = 140 + (index // 2) * 600
        draw.rounded_rectangle((cell_x, cell_y, cell_x + 920, cell_y + 580), radius=14, fill="#18202b", outline="#3a4554", width=2)
        draw.text((cell_x + 19, cell_y + 12), label, fill="#e4eaf2", font=label_font)
        sheet.paste(image, (cell_x + (920 - image.width) // 2, cell_y + 58 + (510 - image.height) // 2))
    draw.text((36, 1350), "完整结构保留双层屋顶；未部署、未访问玩家世界。材质为 1.20.1 原版暗铁近似，不使用灾变模组方块。", fill="#93a5bc", font=detail_font, anchor="ls")
    sheet.save(ROOT / "qa/four-views.png")


def finalize():
    metadata = load("room.json")
    static = load("qa/static.json")
    native = load("qa/forge-native.json")
    render = load("qa/render.json")
    review = load("qa/visual-review.json")
    digest = sha256(ROOT / "room.nbt")
    assert metadata["id"] == static["id"] == native["id"] == render["id"] == review["id"] == "corridor-M07"
    assert metadata["sha256"] == static["sha256"] == native["sha256"] == render["source_sha256"] == review["source_sha256"] == digest
    for evidence in [static, native, render, review]:
        assert evidence["status"] == "PASS", evidence
    assert native["ticks_observed"] >= 240 and len(native["rotations"]) == 4
    assert render["errors"] == [] and len(render["views"]) == 7
    for view in render["views"]:
        image_path = ROOT / view["file"]
        assert sha256(image_path) == view["sha256"]
        with Image.open(image_path) as image:
            image.verify()
    log = (ROOT / "qa/forge-run.log").read_text(encoding="utf-8", errors="replace")
    for marker in ["CORRIDOR_M07_FORGE_PASS", "All 1 required tests passed", "BUILD SUCCESSFUL"]:
        assert marker in log, marker
    make_sheet()
    result = {
        "id": "corridor-M07", "name": metadata["name"], "status": "PASS", "sha256": digest,
        "created_at": datetime.now(timezone(timedelta(hours=8))).isoformat(timespec="seconds"),
        "reference": metadata["reference"], "reference_sha256": metadata["reference_sha256"],
        "size": metadata["size"], "minecraft": "1.20.1", "forge": "47.4.10",
        "static": static,
        "four_view_qa": {"status": "PASS", "report": "qa/visual-review.json", "render_evidence": "qa/render.json", "sheet": "qa/four-views.png", "views": ["north", "south", "west", "east"], "additional": ["preview.png", "qa/views/reverse.png", "qa/views/sealed.png"], "nbt_modified_for_cutaway": False},
        "forge_placement_stability": native,
        "roundtrip": load("qa/roundtrip.json"),
        "fixture_history": {"initial_attempt": "Initial placement succeeded; after neighbor updates natural terrain lava entered an open portal at low GameTest elevation. This run failed and is retained in timestamped log.", "correction": "No template changes. All four fixtures moved to absolute Y=180; preflight verifies every cell and one-block margin are empty before placement.", "initial_failure_not_hidden": True},
        "safety": {"deployed": False, "player_saves_accessed": False, "other_room_templates_modified": False, "chat_or_image_configuration_modified": False, "paid_image_generation_called": False},
        "limits": ["Software renders, not full modpack client screenshots or gameplay validation.", "Vanilla deepslate/blackstone substitutes for dark iron; crimson patterns are decorative glazed terracotta and nether bricks, not functional/emissive custom runes.", "No FT hall connection, runtime generator, persistence, occupancy-refusal, difficulty, rewards or deployment work performed.", "Consumer must reject occupied full volume and surrounding margin; explicit air does not authorize clearing player builds.", "corridor-M07 is not the pre-existing room M07 or M7."],
    }
    write("qa.json", result)
    assets = {}
    for relative in ["room.nbt", "room.json", "preview.png", "qa.json", "qa/four-views.png", "qa/forge-native.json", "qa/static.json", "qa/render.json", "qa/visual-review.json"]:
        item = ROOT / relative
        assets[relative] = {"sha256": sha256(item), "bytes": item.stat().st_size}
    write("delivery-manifest.json", {"id": "corridor-M07", "status": "PASS", "files": assets, "deploy": False})
    print(json.dumps({"id": "corridor-M07", "status": "PASS", "sha256": digest, "files": list(assets)}, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    import sys
    if "--sheet-only" in sys.argv:
        make_sheet()
    else:
        finalize()

