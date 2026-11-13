import argparse
import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import time
import uuid
import zipfile

ROOT = Path(__file__).resolve().parents[3]
WORK = ROOT / "work/session-boss"
HISTORY = Path("D:/ft工程文件/FTBoss-NeoForge-1.21.1-移植-20261022")
JAVA = Path("C:/Program Files/Zulu/zulu-21/bin")

def load(path):
    return json.loads(Path(path).read_text(encoding="utf-8"))

def save(path, value):
    with Path(path).open("x", encoding="utf-8") as stream:
        json.dump(value, stream, ensure_ascii=False, indent=2)

def digest(path):
    checksum = hashlib.sha256()
    with Path(path).open("rb") as stream:
        for block in iter(lambda: stream.read(1048576), b""):
            checksum.update(block)
    return checksum.hexdigest()

prior_receipt = HISTORY / "batches/299-native-fresh-factory-runtime/reports/actual-native-map-version299-not-promoted.json"
assert digest(prior_receipt) == "1047eb067fb84070d2fa285bcb861980ec5ad808f5e3e5e8565057c2f7bcc18d"
prior = Path(load(prior_receipt)["actualRuntime"])
context_path = WORK / "build/boss-flight-ban-v1/compile-context.json"
context = load(context_path)
parser = argparse.ArgumentParser()
parser.add_argument("build_receipt", type=Path)
parser.add_argument("mode", choices=["baseline", "candidate"])
arguments = parser.parse_args()
build_receipt = arguments.build_receipt.resolve()
built = load(build_receipt)
original_jar = Path(built["jar"])
assert digest(original_jar) == built["jarSha256"]
source = WORK / 'tests/summon-fx-v2/BossSummonFxRuntimeProbeV2.java'
attempt = WORK / "build" / ("summon-fx-v2-" + arguments.mode + "-" + uuid.uuid4().hex[:8])
attempt.mkdir(parents=True)
server = attempt / "server"
server.mkdir()
proposal = Path(built["proposal"])
original_hashes = {str(original_jar): digest(original_jar), str(build_receipt): digest(build_receipt)}
overlay = []
for directory in ["mods", "recipe-inputs", "kubejs"]:
    if (prior / "server" / directory).exists():
        shutil.copytree(prior / "server" / directory, server / directory)
(server / "mods").mkdir(exist_ok=True)
shutil.copyfile(original_jar, server / "mods" / original_jar.name)
assert not (server / "world").exists()
(server / "server.properties").write_text("server-ip=127.0.0.1\nserver-port=0\nonline-mode=true\nmax-tick-time=-1\nlevel-type=minecraft:flat\n", encoding="ascii")
for directory in ["isolated-home", "isolated-tmp", "observer-classes", "observer-resources/META-INF"]:
    (attempt / directory).mkdir(parents=True)
sdk = []
for entry in context["classpath"].split(";"):
    path = Path(entry)
    if path.suffix.lower() != ".jar":
        continue
    with zipfile.ZipFile(path) as archive:
        if "net/farmingtales/ftboss/FTBossMod.class" in archive.namelist():
            continue
    sdk.append(entry)
classpath = str(original_jar) + ";" + ";".join(sdk)
compile_command = [str(JAVA / "javac.exe"), "-proc:none", "--release", "21", "-encoding", "UTF-8", "-cp", classpath, "-d", str(attempt / "observer-classes"), str(source)]
save(attempt / "compile-command.json", compile_command)
with (attempt / "compile.log").open("wb") as stream:
    compiled = subprocess.run(compile_command, cwd=ROOT, stdout=stream, stderr=subprocess.STDOUT)
save(attempt / "compile-terminal.json", {"exitCode": compiled.returncode, "serverStarted": False})
assert compiled.returncode == 0, str(attempt / "compile.log")
metadata = attempt / "observer-resources/META-INF/neoforge.mods.toml"
metadata.write_text('modLoader="javafml"\nloaderVersion="[4,)"\nlicense="All Rights Reserved"\n[[mods]]\nmodId="ftboss_bossprobe"\nversion="1.0"\ndisplayName="Boss owner focused probe"\n', encoding="utf-8")
command = load(prior / "actual-runtime-arguments201.json")
response_old = prior / "actual-official-dev198.args"
response = attempt / response_old.name
library_root = (prior / "server/libraries").as_posix()
response_text = response_old.read_text(encoding="utf-8").replace("libraries/", library_root + "/").replace("-DlibraryDirectory=libraries", '-DlibraryDirectory="' + library_root + '"')
response_text = "\n".join('"' + line.split(" ", 1)[0] + '" "' + line.split(" ", 1)[1] + '"' if line.startswith("-p ") else line for line in response_text.splitlines()) + "\n"
response.write_text(response_text, encoding="mbcs")
for index, value in enumerate(command):
    if value.startswith("-Dfml.modFolders="):
        minecraft = [part for part in value.removeprefix("-Dfml.modFolders=").split(";") if part.startswith("minecraft%%")]
        command[index] = "-Dfml.modFolders=" + ";".join(minecraft + ["ftboss_bossprobe%%" + str(attempt / "observer-classes"), "ftboss_bossprobe%%" + str(attempt / "observer-resources")])
    elif value.startswith("-Duser.home="):
        command[index] = "-Duser.home=" + str(attempt / "isolated-home")
    elif value.startswith("-Djava.io.tmpdir="):
        command[index] = "-Djava.io.tmpdir=" + str(attempt / "isolated-tmp")
    elif value.startswith("-DlegacyClassPath="):
        command[index] = value.replace("libraries/", library_root + "/")
    elif value.startswith("@"):
        command[index] = "@" + str(response)
command.insert(1, "-Dftboss.summonfx.mode=" + arguments.mode)
save(attempt / "runtime-command.json", command)
inputs = {str(path): digest(path) for path in [Path(__file__), context_path, source, prior_receipt, response_old, response, metadata, *overlay]}
inputs.update(original_hashes)
save(attempt / "inputs-before.json", inputs)
environment = os.environ.copy()
environment.pop("MOD_CLASSES", None)
stop_sent = False
supervision_failure = None
with (attempt / "runtime.log").open("w", encoding="utf-8") as stream:
    process = subprocess.Popen(command, cwd=server, env=environment, stdin=subprocess.PIPE, stdout=stream, stderr=subprocess.STDOUT, text=True, encoding="utf-8")
    save(attempt / "live-process.json", {"pid": process.pid, "attempt": str(attempt), "freshWorld": True, "noWeaponSeedOrFunctionalChanges": True, "actualPackagedBossJarWithoutWeaponAddon": True})
    print(json.dumps({"attempt": str(attempt), "pid": process.pid}), flush=True)
    deadline = time.monotonic() + 1800
    try:
        while process.poll() is None:
            if (server / "actual-summon-fx-probe.json").exists() and not stop_sent:
                process.stdin.write("stop\n")
                process.stdin.flush()
                stop_sent = True
            if time.monotonic() > deadline:
                save(attempt / ("observation-extension-" + uuid.uuid4().hex[:8] + ".json"), {"pid": process.pid, "sameProcessStillObserved": True})
                print(json.dumps({"sameProcessObservationExtended": process.pid}), flush=True)
                deadline = time.monotonic() + 1800
            time.sleep(0.25)
    except BaseException as failure:
        supervision_failure = repr(failure)
    finally:
        if process.poll() is None:
            if not stop_sent:
                process.stdin.write("stop\n")
                process.stdin.flush()
                stop_sent = True
            process.wait()
log = (attempt / "runtime.log").read_text(encoding="utf-8", errors="replace")
probe_path = server / "actual-summon-fx-probe.json"
probe = load(probe_path) if probe_path.exists() else {"failures": 1, "failure": "Observer did not run"}
unchanged = all(digest(name) == wanted for name, wanted in inputs.items())
terminal = {"exitCode": process.returncode, "stopSent": stop_sent, "completeSave": "All dimensions are saved" in log, "inputHashesUnchanged": unchanged, "supervisionFailure": supervision_failure}
save(attempt / "terminal.json", terminal)
output = WORK / "reports" / (attempt.name + ".json")
save(output, {"attempt": str(attempt), "terminal": terminal, "probe": probe, "inputHashes": inputs, "fullBossAcceptance": False, "independentBossJarStartupAccepted": probe.get("failures") == 0, "summonFxFeatureAccepted": arguments.mode == "candidate" and probe.get("failures") == 0, "baselineGapObserved": arguments.mode == "baseline" and bool(probe.get("actualSummonFxMissingDemonstrated")), "authenticatedMultiplayerAccepted": False, "naturalBossAiLoopAcceptedInThisRun": False, "fullClientServerIndependentBossAcceptance": False, "weaponStandaloneJarAccepted": False, "deployed": False, "scope": "Native server callbacks driving directed actual registered butcher entity ticks, actual native entity admission and original summonWave with original summon FX queue, particle budget, phase/tick/cap/expiry and failure/nonliving controls. No authenticated player ingress, client rendering or complete-battle acceptance"})
print(json.dumps({"report": str(output), "terminal": terminal, "probeFailures": probe["failures"], "cases": len(probe.get("cases", []))}), flush=True)
assert process.returncode == 0 and stop_sent and terminal["completeSave"] and unchanged and supervision_failure is None and probe["failures"] == 0
