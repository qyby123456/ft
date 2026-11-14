import hashlib
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
OUTPUT = ROOT / "_work/statue-ui-restyle-20261024/themes"
CLASS_NAMES = ("StatueUiTheme", "StatueThemeSource", "StatueThemeRenderer", "StatueThemeButton")


def record(filename):
    return {"path": filename.relative_to(ROOT).as_posix(), "sha256": hashlib.sha256(filename.read_bytes()).hexdigest()}


def main():
    sources = [ROOT / "src/main/java/net/farmingtales/ftboss/client" / (name + ".java") for name in CLASS_NAMES]
    assets = sorted((ROOT / "src/main/resources/assets/ftboss/textures/gui/statue_themes").rglob("*.png"))
    classes = sorted((OUTPUT / "compile/classes").rglob("*.class"))
    assert len(classes) == 4 and len(assets) == 22
    data = {"baseline": record(OUTPUT.parent / "baseline.jar"), "source_files": [record(filename) for filename in sources],
            "resources": [record(filename) for filename in assets], "official_classes_not_for_direct_deployment": [record(filename) for filename in classes],
            "runtime_class_entries": ["net/farmingtales/ftboss/client/" + name + ".class" for name in CLASS_NAMES],
            "runtime_resource_entries": [filename.relative_to(ROOT / "src/main/resources").as_posix() for filename in assets],
            "deployment": "NOT DEPLOYED; coordinator must compile/remap with screen changes and merge only owned entries into the latest JAR"}
    target = OUTPUT / "delivery-manifest.json"
    target.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(target)


if __name__ == "__main__":
    main()
