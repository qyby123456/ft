import hashlib
import json
from pathlib import Path
import shutil
import struct
import zipfile


ROOT = Path(__file__).resolve().parents[1]
VERSION = "1.0.0"
RELEASE = ROOT.parent / "导出" / f"FT地牢交易-Forge-1.20.1-{VERSION}"
QA = ROOT / "model_sources/qa"


def write_json(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def archive_files(destination, entries):
    with zipfile.ZipFile(destination, "w", zipfile.ZIP_DEFLATED, compresslevel=9) as archive:
        for source, target in sorted(entries, key=lambda entry: entry[1]):
            archive.write(source, target)
    with zipfile.ZipFile(destination) as archive:
        if archive.testzip() is not None:
            raise RuntimeError(f"Archive CRC failure: {destination}")


def audit_jar(jar):
    with zipfile.ZipFile(jar) as archive:
        assert archive.testzip() is None
        entries = archive.namelist()
        required = ["META-INF/mods.toml", "pack.mcmeta", "net/farmingtales/ftdungeontrading/FTDungeonTrading.class",
                    "net/farmingtales/ftdungeontrading/api/DungeonTradingApi.class",
                    "net/farmingtales/ftdungeontrading/client/TradingTableRenderer.class",
                    "assets/ftdungeontrading/models/block/trading_table.json",
                    "assets/ftdungeontrading/textures/block/trading_table.png"]
        for entry in required:
            assert entry in entries, entry
        assert not any("TradingRuntimeTest" in entry or "test_currency" in entry or "structures/empty" in entry for entry in entries)
        assert not any(entry.endswith((".java", ".bbmodel", ".ps1")) for entry in entries)
        classes = [entry for entry in entries if entry.endswith(".class")]
        for entry in classes:
            assert struct.unpack(">H", archive.read(entry)[6:8])[0] == 61, entry
        resource_files = [path for path in (ROOT / "src/main/resources").rglob("*") if path.is_file()]
        for path in resource_files:
            assert archive.read(path.relative_to(ROOT / "src/main/resources").as_posix()) == path.read_bytes(), path
        return {"passed": True, "jar": jar.name, "entries": len(entries), "classes": len(classes),
                "java_class_major": 61, "source_resource_files_identical": len(resource_files), "test_assets_absent": True,
                "sha256": hashlib.sha256(jar.read_bytes()).hexdigest()}


def main():
    jar = ROOT / f"build/libs/ft-dungeon-trading-forge-1.20.1-{VERSION}.jar"
    assert "TRADING_RUNTIME_TEST passed with 130 assertions" in (QA / "runtime-release.log").read_text(encoding="utf-8", errors="replace")
    assert "BUILD SUCCESSFUL" in (QA / "build-release.log").read_text(encoding="utf-8", errors="replace")
    report = audit_jar(jar)
    write_json(QA / "jar-audit.json", report)
    if RELEASE.exists():
        raise FileExistsError(f"Refusing to overwrite an existing release: {RELEASE}")
    RELEASE.mkdir(parents=True)
    shutil.copy2(jar, RELEASE / f"FT地牢交易-Forge-1.20.1-{VERSION}.jar")
    shutil.copy2(ROOT / "README.md", RELEASE / "README.md")
    shutil.copytree(ROOT / "docs", RELEASE / "docs")
    shutil.copy2(ROOT / "model_sources/地牢交易桌预览.png", RELEASE / "地牢交易桌预览.png")
    models = RELEASE / "模型源文件"
    models.mkdir()
    for name in ["地牢交易桌.bbmodel", "budget.json", "model-report.json", "hidden-face-proofs.json"]:
        shutil.copy2(ROOT / "model_sources" / name, models / name)
    shutil.copy2(ROOT / "src/main/resources/assets/ftdungeontrading/textures/block/trading_table.png", models / "trading_table.png")
    proof = RELEASE / "验证记录"
    proof.mkdir()
    proof_names = ["jar-audit.json", "resource-check.json", "minecraft-parser.json", "model-audit.json", "cost-audit.json",
                   "roundtrip.json", "roundtrip-render-check.json", "blockbench-native-export.json", "all-views.png", "all-displays.png",
                   "runtime-release.log", "build-release.log", "view-settings.json", "display-settings.json"]
    for name in proof_names:
        shutil.copy2(QA / name, proof / name)
    datapack = ROOT / "examples/datapack"
    archive_files(RELEASE / "交易数据包示例.zip", [(path, path.relative_to(datapack).as_posix()) for path in datapack.rglob("*") if path.is_file()])
    source_entries = []
    for directory in ["src", "tools", "docs", "examples", "gradle"]:
        for path in (ROOT / directory).rglob("*"):
            if path.is_file() and "__pycache__" not in path.parts:
                source_entries.append((path, path.relative_to(ROOT).as_posix()))
    for name in ["settings.gradle", "build.gradle", "gradle.properties", "BUILD.ps1", ".gitignore", "README.md"]:
        source_entries.append((ROOT / name, name))
    for path in (ROOT / "model_sources").iterdir():
        if path.is_file():
            source_entries.append((path, path.relative_to(ROOT).as_posix()))
    for name in proof_names:
        source_entries.append((QA / name, f"model_sources/qa/{name}"))
    archive_files(RELEASE / f"FT地牢交易-可构建源码-{VERSION}.zip", source_entries)
    manifest = {"name": "FT Dungeon Trading", "version": VERSION, "minecraft": "1.20.1", "forge_tested": "47.4.10",
                "java": 17, "deployed": False, "client_tested": False, "gametest_assertions": 130, "files": {}}
    for path in sorted(RELEASE.rglob("*")):
        if path.is_file():
            manifest["files"][path.relative_to(RELEASE).as_posix()] = {"bytes": path.stat().st_size, "sha256": hashlib.sha256(path.read_bytes()).hexdigest()}
    write_json(RELEASE / "manifest.json", manifest)
    print(json.dumps({"release": str(RELEASE), "jar_audit": report, "files": len(manifest["files"])}, ensure_ascii=False))


if __name__ == "__main__":
    main()
