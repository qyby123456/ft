import argparse
import hashlib
import json
import zipfile
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]


def load(relative):
    return json.loads((ROOT / relative).read_text(encoding="utf-8-sig"))


def save(relative, data):
    (ROOT / relative).write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def digest(source):
    return hashlib.sha256(source.read_bytes()).hexdigest()


def sheets():
    font = ImageFont.truetype("C:/Windows/Fonts/msyh.ttc", 30)
    small = ImageFont.truetype("C:/Windows/Fonts/msyh.ttc", 20)
    for mode, title in [("cutaway", "内部剖视"), ("exterior", "完整外壳")]:
        canvas = Image.new("RGB", (1920, 1380), "#0c1219")
        draw = ImageDraw.Draw(canvas)
        draw.text((25, 15), f"corridor-M12  ·  {title}四向检查  ·  13×11×40", fill="#decaa4", font=font)
        for index, direction in enumerate(["north", "south", "west", "east"]):
            with Image.open(ROOT / f"preview/{mode}-{direction}.png") as source:
                picture = source.convert("RGB").resize((960, 640), Image.Resampling.LANCZOS)
                canvas.paste(picture, ((index % 2) * 960, 65 + (index // 2) * 640))
        draw.text((25, 1345), "实际最终 NBT 软件渲染，非游戏截图；剖切仅影响预览，不改交付结构。", fill="#a3b2c1", font=small)
        canvas.save(ROOT / f"preview/{mode}-four-views.png")
    with Image.open(ROOT / "preview/cutaway-four-views.png") as interior, Image.open(ROOT / "preview/exterior-four-views.png") as exterior:
        canvas = Image.new("RGB", (1920, 2760), "#0c1219")
        canvas.paste(interior, (0, 0))
        canvas.paste(exterior, (0, 1380))
        canvas.save(ROOT / "preview/eight-view-review.png")


def finalize():
    fingerprint = digest(ROOT / "room.nbt")
    metadata = load("room.json")
    static = load("qa/static.json")
    native = load("qa/native.json")
    execution = load("qa/native-run.json")
    rendered = load("qa/render.json")
    visual = load("qa/four-view-review.json")
    for report in [metadata, static, native, rendered, visual]:
        if report["sha256"] != fingerprint:
            raise ValueError("QA evidence fingerprint does not match final NBT")
    if any(report["status"] != "PASS" for report in [static, native, visual]):
        raise ValueError("A required QA stage has not passed")
    if execution["exit_code"] != 0 or len(rendered["frames"]) != 9 or rendered["page_errors"]:
        raise ValueError("Missing clean Forge exit or rendered views")
    for frame in rendered["frames"]:
        with Image.open(ROOT / frame["file"]) as picture:
            if picture.size != (1536, 1024):
                raise ValueError("Incorrect screenshot dimensions")
    summary = {
        "id": metadata["id"], "status": "PASS", "sha256": fingerprint,
        "reference_sha256": metadata["reference_sha256"], "size": metadata["size"],
        "static": {"status": static["status"], "report": "qa/static.json", "checks": len(static["checks"]), "records": static["records"], "non_air": static["non_air"]},
        "native_forge": {"status": native["status"], "report": "qa/native.json", "execution": "qa/native-run.json", "log": execution["log"], "minecraft": "1.20.1", "forge": "47.4.10", "rotations": 4, "placed_records": 22880, "settled_ticks": 400},
        "four_direction_review": {"status": visual["status"], "report": "qa/four-view-review.json", "render_manifest": "qa/render.json", "individual_views": 8, "sheet": "preview/eight-view-review.png"},
        "scope": {"candidate_only": True, "production_deployed": False, "player_save_used": False, "core_source_modified": False, "full_modpack_playtest": False, "paid_image_generation_called": False},
        "limitations": ["软件渲染不是整合包游戏截图；不模拟烛火粒子或完整游戏光照。", "Forge 为仅加载验证夹具的隔离 GameTestServer，不代表完整整合包多人/FPS验收。", "未分配正式接入槽位；不得与已有 M12 菌菇地窖 ID 混用。", "NBT 明确记录整个体积（含空气）；接入必须先做全体积占用、实体和液体拒绝，禁止覆盖玩家建筑。"]
    }
    save("qa.json", summary)
    files = [ROOT / filename for filename in ["room.nbt", "room.json", "preview.png", "reference.png", "qa.json", "summary.json", "README.md", "Run-QA.ps1"]]
    for directory in ["preview", "qa", "tools"]:
        files.extend(source for source in (ROOT / directory).rglob("*") if source.is_file() and "__pycache__" not in source.parts)
    files.extend(source for source in (ROOT / "native-test/src").rglob("*") if source.is_file())
    files.extend(ROOT / f"native-test/{filename}" for filename in ["build.gradle", "settings.gradle", "gradle.properties"])
    files = sorted(set(files))
    manifest = {"id": "corridor-M12", "sha256": fingerprint, "status": "PASS", "files": [{"path": source.relative_to(ROOT).as_posix(), "bytes": source.stat().st_size, "sha256": digest(source)} for source in files], "excluded": ["native-test/build", "native-test/.gradle", "native-test/runs", "generated test worlds are isolated, not player saves"]}
    save("delivery-manifest.json", manifest)
    package = ROOT / "corridor-M12-independent.zip"
    with zipfile.ZipFile(package, "w", zipfile.ZIP_DEFLATED) as archive:
        for source in [*files, ROOT / "delivery-manifest.json"]:
            archive.write(source, "corridor-M12/" + source.relative_to(ROOT).as_posix())
    save("package-receipt.json", {"file": package.name, "sha256": digest(package), "bytes": package.stat().st_size, "room_sha256": fingerprint, "files": len(files) + 1})
    print(json.dumps({"status": "PASS", "room_sha256": fingerprint, "zip_sha256": digest(package), "files": len(files) + 1}))


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--sheets", action="store_true")
    arguments = parser.parse_args()
    sheets()
    if not arguments.sheets:
        finalize()


if __name__ == "__main__":
    main()
