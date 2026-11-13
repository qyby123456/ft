import hashlib
import json
from pathlib import Path
import subprocess
import zipfile

ROOT = Path(__file__).resolve().parents[3]
WORK = ROOT / "work/session-boss"
source_context = json.loads((WORK / "build/boss-module-boundary-v5/compile-context.json").read_text(encoding="utf-8"))
base = WORK / "releases/boss-core-stage02-absorption-20261112/ftboss-boss-core-1.21.1-stage02.jar"
base_hash = hashlib.sha256(base.read_bytes()).hexdigest()
deployment = json.loads((base.parent / "deployment-receipt.json").read_text(encoding="utf-8"))
assert base_hash == deployment["deployedSha256"]
output = WORK / "build/boss-status-immunity-v1"
assert not output.exists()
output.mkdir()
classes = output / "classes"
classes.mkdir()
sdk = []
for entry in source_context["classpath"].split(";"):
    path = Path(entry)
    if path.suffix.lower() != ".jar":
        continue
    with zipfile.ZipFile(path) as archive:
        if "net/farmingtales/ftboss/FTBossMod.class" in archive.namelist():
            continue
    sdk.append(str(path))
classpath = str(base) + ";" + ";".join(sdk)
source = WORK / "boss-core/src/main/java/net/farmingtales/ftboss/boss/ai/ManorStatusImmunity.java"
command = ["C:/Program Files/Zulu/zulu-21/bin/javac.exe", "-proc:none", "--release", "21", "-encoding", "UTF-8", "-cp", classpath, "-d", str(classes), str(source)]
with (output / "compile.log").open("wb") as stream:
    compiled = subprocess.run(command, cwd=ROOT, stdout=stream, stderr=subprocess.STDOUT)
context = {"exitCode": compiled.returncode, "classpath": classpath, "source": str(source), "sourceSha256": hashlib.sha256(source.read_bytes()).hexdigest(), "output": str(classes), "baseJar": str(base), "baseJarSha256": base_hash, "noStage03MaterialChangesIncluded": True, "weaponCandidateCompiledOrValidated": False}
with (output / "compile-context.json").open("x", encoding="utf-8") as stream:
    json.dump(context, stream, ensure_ascii=False, indent=2)
with (output / "baseline-build-receipt.json").open("x", encoding="utf-8") as stream:
    json.dump({"jar": str(base), "jarSha256": base_hash, "proposal": str(WORK / "build/boss-split-9339206b/boss-core-proposal"), "mode": "baseline", "deployed": True}, stream, ensure_ascii=False, indent=2)
print(json.dumps({"exitCode": compiled.returncode, "compiledOnlyFeatureSources": 1, "classes": len(list(classes.rglob("*.class")))}))
assert compiled.returncode == 0
