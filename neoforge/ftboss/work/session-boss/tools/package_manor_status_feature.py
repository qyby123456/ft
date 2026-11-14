import argparse
import hashlib
import json
from pathlib import Path
import zipfile

ROOT = Path(__file__).resolve().parents[3]
WORK = ROOT / "work/session-boss"
parser = argparse.ArgumentParser()
parser.add_argument("output_jar", type=Path)
arguments = parser.parse_args()
build = WORK / "build/boss-status-immunity-v1"
context = json.loads((build / "compile-context.json").read_text(encoding="utf-8"))
assert json.loads((build / "proof-terminal.json").read_text(encoding="utf-8"))["proofExitCode"] == 0
base = Path(context["baseJar"])
assert hashlib.sha256(base.read_bytes()).hexdigest() == context["baseJarSha256"]
output = arguments.output_jar.resolve()
assert output.is_relative_to(ROOT.resolve()) and not output.exists()
overlay = {path.relative_to(build / "classes").as_posix(): path for path in (build / "classes").rglob("*.class")}
assert set(overlay) == {"net/farmingtales/ftboss/boss/ai/ManorStatusImmunity.class", "net/farmingtales/ftboss/entity/EasterBossEntity.class"}
with zipfile.ZipFile(base) as original, zipfile.ZipFile(output, "x", compression=zipfile.ZIP_DEFLATED) as archive:
    for entry in original.infolist():
        archive.writestr(entry, overlay[entry.filename].read_bytes() if entry.filename in overlay else original.read(entry.filename))
    for name, path in overlay.items():
        if name not in original.namelist():
            archive.write(path, name)
with zipfile.ZipFile(base) as original, zipfile.ZipFile(output) as archive:
    assert archive.testzip() is None
    added = set(archive.namelist()) - set(original.namelist())
    removed = set(original.namelist()) - set(archive.namelist())
    changed = {name for name in set(original.namelist()) & set(archive.namelist()) if original.read(name) != archive.read(name)}
    assert added == {"net/farmingtales/ftboss/boss/ai/ManorStatusImmunity.class"}
    assert changed == {"net/farmingtales/ftboss/entity/EasterBossEntity.class"} and not removed
print(json.dumps({"jar": str(output), "sha256": hashlib.sha256(output.read_bytes()).hexdigest(), "allOtherBytesPreserved": True, "fullBossInstallable": False, "deployed": False}))
