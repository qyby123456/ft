import argparse
import hashlib
import json
from pathlib import Path
import subprocess
import uuid
import zipfile

root = Path(__file__).resolve().parents[3]
work = root / "work/session-boss"
parser = argparse.ArgumentParser()
parser.add_argument("classes", nargs="+")
arguments = parser.parse_args()
assert 1 <= len(arguments.classes) <= 24
context = json.loads((work / "build/boss-owner-decoupled/compile-context.json").read_text(encoding="utf-8"))
application = Path(context["originalApplication"])
decompiler = work / "tools/vineflower-1.10.1.jar"
assert hashlib.sha256(decompiler.read_bytes()).hexdigest() == "b9b208e50793b64657a6b6292067526613f549de7405f9243624b02f4276e409"
output = work / "build" / ("recovered-boundary-" + uuid.uuid4().hex[:8])
output.mkdir(parents=True)
archive = output / "input.jar"
inputs = {}
with zipfile.ZipFile(archive, "x", compression=zipfile.ZIP_DEFLATED) as stream:
    for name in arguments.classes:
        path = application / ("net/farmingtales/ftboss/" + name + ".class")
        assert path.is_file(), str(path)
        for source in [path, *sorted(path.parent.glob(path.stem + "$*.class"))]:
            relative = source.relative_to(application).as_posix()
            assert relative not in inputs
            inputs[relative] = hashlib.sha256(source.read_bytes()).hexdigest()
            stream.write(source, relative)
command = ["C:/Program Files/Zulu/zulu-21/bin/java.exe", "-Xmx768m", "-jar", str(decompiler), "-thr=1", str(archive), str(output / "sources")]
with (output / "decompile.log").open("wb") as stream:
    result = subprocess.run(command, cwd=root, stdout=stream, stderr=subprocess.STDOUT)
with (output / "recovery.json").open("x", encoding="utf-8") as stream:
    json.dump({"exitCode": result.returncode, "originalApplication": str(application), "inputClassHashes": inputs, "decompilerSha256": hashlib.sha256(decompiler.read_bytes()).hexdigest(), "sources": len(list((output / "sources").rglob("*.java"))), "sourceRecoveryOnly": True}, stream, ensure_ascii=False, indent=2)
print(json.dumps({"output": str(output), "exitCode": result.returncode, "sources": len(list((output / "sources").rglob("*.java")))}))
assert result.returncode == 0
