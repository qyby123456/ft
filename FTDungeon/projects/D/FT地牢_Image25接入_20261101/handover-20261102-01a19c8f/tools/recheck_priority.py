import argparse
import importlib.util
import json
import os
import shutil
import subprocess
import sys
import zipfile
from datetime import datetime, timedelta, timezone
from pathlib import Path

from PIL import Image

from intake import HOME, INSTANCE, Structure, digest, file_record, load, png_record, save


def current(room_id):
    registry = load(load(HOME / "latest.json")["registry"]["path"])
    return next(record for record in registry["rooms"] if record["id"] == room_id)


def module_at(name, path):
    spec = importlib.util.spec_from_file_location(name, path)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def render(room_id):
    record = current(room_id)
    snapshot = Path(record["snapshot"])
    folder = HOME / "reviews" / room_id / record["candidate_sha256"]
    folder.mkdir(parents=True, exist_ok=True)
    output = folder / "four-views"
    output.mkdir(exist_ok=False)
    source = Path(record["source_root"])
    metadata = load(snapshot / "room.json")
    structure = Structure.read(snapshot / "room.nbt")
    sys.path.insert(0, str(source / "tools"))
    if room_id == "M07":
        engine = module_at("receiver_m07_render", source / "tools/render_m07.py")
        draw = lambda camera, title, exterior: engine.render_view(structure, metadata, camera, title, exterior, size=(1400, 1080))
    else:
        engine = module_at("receiver_r4_render", source / "tools/render_engine.py")
        engine.ASSETS = zipfile.ZipFile(engine.CLIENT)
        draw = lambda camera, title, exterior: engine.render(structure, camera, title, exterior=exterior, size=(1400, 1080))
    directions = (("north", "北向 / 最终结构", (-0.85, -1)), ("south", "南向 / 最终结构", (0.85, 1)), ("west", "西向 / 最终结构", (-1, 0.65)), ("east", "东向 / 最终结构", (1, -0.65)))
    images = []
    for exterior in (False, True):
        mode = "exterior" if exterior else "interior"
        sheet = Image.new("RGB", (2800, 2160), "#121d23")
        for index, (direction, title, camera) in enumerate(directions):
            picture = draw(camera, title, exterior)
            path = output / f"{mode}-{direction}.png"
            picture.save(path)
            sheet.paste(picture, ((index % 2) * 1400, (index // 2) * 1080))
            images.append(png_record(path))
        path = output / f"{mode}-four-views.png"
        sheet.save(path)
        images.append(png_record(path))
    assert digest(snapshot / "room.nbt") == record["candidate_sha256"]
    save(output / "manifest.json", {"status": "RENDERED_NOT_YET_VISUALLY_APPROVED", "id": room_id, "sha256": record["candidate_sha256"], "source": file_record(snapshot / "room.nbt"), "renderer": file_record(source / ("tools/render_m07.py" if room_id == "M07" else "tools/render_engine.py")), "images": images, "limits": "Software cutaways remove roof/near outer walls only in memory. All four exterior views retain the entire delivered shell. Not game screenshots, liquid flow or illumination simulation."})
    print("RENDERED", room_id, str(output), flush=True)


def native(room_id):
    record = current(room_id)
    source = Path(record["source_root"])
    snapshot = Path(record["snapshot"])
    stamp = datetime.now(timezone(timedelta(hours=8))).strftime("%Y%m%d-%H%M%S")
    folder = HOME / "reviews" / room_id / record["candidate_sha256"] / "native" / stamp
    folder.mkdir(parents=True, exist_ok=False)
    original = source / "forge" if room_id == "M07" else source
    project = folder / "forge" if room_id == "M07" else folder
    project.mkdir(exist_ok=True)
    copied = []
    for name in ("build.gradle", "settings.gradle", "gradle.properties"):
        path = original / name
        if path.exists():
            shutil.copy2(path, project / name)
            copied.append(file_record(path))
    shutil.copytree(original / "src", project / "src")
    copied += [file_record(path) for path in sorted((original / "src").rglob("*")) if path.is_file()]
    target = folder / "out/M07" if room_id == "M07" else folder
    target.mkdir(parents=True, exist_ok=True)
    for name in ("room.nbt", "room.json"):
        shutil.copy2(snapshot / name, target / name)
    (folder / "qa").mkdir(exist_ok=True)
    tools = folder / "tools"
    tools.mkdir(exist_ok=True)
    names = ("build_m07.py", "static_qa.py", "nbtio.py") if room_id == "M07" else ("build_room.py", "qa_static.py", "nbtio.py")
    for name in names:
        shutil.copy2(source / "tools" / name, tools / name)
        copied.append(file_record(source / "tools" / name))
    if room_id == "R4":
        shutil.copy2(source / "blueprint.json", folder / "blueprint.json")
        copied.append(file_record(source / "blueprint.json"))
    static_command = [sys.executable, "-B", "-X", "utf8"]
    if room_id == "M07":
        static_command += ["-c", "from nbtio import Structure; from static_qa import static_review; static_review(Structure.read('../out/M07/room.nbt')); print('STATIC_RECHECK_PASS')"]
    else:
        static_command += [str(tools / "qa_static.py")]
    static = subprocess.run(static_command, cwd=tools, capture_output=True, text=True, encoding="utf-8", creationflags=subprocess.CREATE_NO_WINDOW)
    (folder / "qa/static-run.log").write_text(static.stdout + static.stderr, encoding="utf-8")
    if static.returncode:
        raise RuntimeError(static.stdout + static.stderr)
    environment = os.environ.copy()
    java_home = INSTANCE.parent.parent / "runtime/java-runtime-gamma-snapshot"
    if not (java_home / "bin/java.exe").is_file():
        java_home = Path(os.environ["APPDATA"]) / ".minecraft/runtime/java-runtime-gamma-snapshot"
    environment["JAVA_HOME"] = str(java_home)
    environment["PATH"] = str(java_home / "bin") + os.pathsep + environment.get("PATH", "")
    launcher = next((Path.home() / ".gradle/wrapper/dists/gradle-8.8-bin").glob("*/gradle-8.8/lib/gradle-launcher-8.8.jar"))
    command = [str(java_home / "bin/java.exe"), "-classpath", str(launcher), "org.gradle.launcher.GradleMain", "runGameTestServer", "--offline", "--no-daemon", "--console=plain", "-Dnet.minecraftforge.gradle.check.certs=false"]
    save(folder / "invocation.json", {"id": room_id, "source_nbt_sha256": record["candidate_sha256"], "isolated_project": str(project), "fresh_test_world": True, "command": command, "copied_producer_test_sources": copied, "no_shared_core_source_writes": True})
    print("START_NATIVE", room_id, str(folder), flush=True)
    with (folder / "qa/forge-run.log").open("wb") as log:
        result = subprocess.run(command, cwd=project, env=environment, stdout=log, stderr=subprocess.STDOUT, creationflags=subprocess.CREATE_NO_WINDOW, timeout=600)
    report_path = folder / ("qa/forge-native.json" if room_id == "M07" else "qa/native.json")
    report = load(report_path) if report_path.is_file() else {}
    log = (folder / "qa/forge-run.log").read_text(encoding="utf-8", errors="replace")
    passed = result.returncode == 0 and report.get("status") == "PASS" and "BUILD SUCCESSFUL" in log
    assert digest(target / "room.nbt") == record["candidate_sha256"]
    assert all(digest(information["path"]) == information["sha256"] for information in copied)
    decision = {"id": room_id, "sha256": record["candidate_sha256"], "status": "PASS" if passed else "FAIL", "static": file_record(folder / "qa/static.json"), "native": file_record(report_path) if report else None, "log": file_record(folder / "qa/forge-run.log"), "invocation": file_record(folder / "invocation.json"), "exit_code": result.returncode, "limits": "Independent rerun of copied room-specific producer tests in a fresh isolated Forge world. Not FT hall/occupancy/idempotence integration, client gameplay, multiplayer or FPS validation."}
    save(folder / "result.json", decision)
    save(folder.parent / "latest.json", {"result": file_record(folder / "result.json")})
    print("NATIVE_RECHECK", room_id, decision["status"], str(folder / "result.json"), flush=True)
    if not passed:
        print(log[-7000:])
        raise SystemExit(1)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("action", choices=("render", "native"))
    parser.add_argument("room", choices=("M07", "R4"))
    args = parser.parse_args()
    (render if args.action == "render" else native)(args.room)


if __name__ == "__main__":
    main()
