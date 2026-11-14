import hashlib
import os
from pathlib import Path
import shutil
import subprocess
import sys
from zipfile import ZipFile


def main():
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")
    sys.stderr.reconfigure(encoding="utf-8", errors="replace")
    project = Path(__file__).resolve().parent.parent
    task = Path(sys.argv[1]).resolve()
    task.relative_to(project / "_work")
    baseline = task / "baseline.jar"
    java = Path("C:/Program Files/Zulu/zulu-21/bin")
    cache = Path.home() / ".gradle/caches"
    renamer = next((cache / "modules-2/files-2.1/net.neoforged/AutoRenamingTool").rglob("*-all.jar"))
    repository = cache / "forge_gradle/minecraft_user_repo"
    mapping = repository / "de/oceanlabs/mcp/mcp_config/1.20.1-20230612.114412/srg_to_official_1.20.1.tsrg"
    forge = repository / "net/minecraftforge/forge/1.20.1-47.4.10/forge-1.20.1-47.4.10-srg.jar"
    official = repository / "net/minecraftforge/forge/1.20.1-47.4.10_mapped_official_1.20.1/forge-1.20.1-47.4.10_mapped_official_1.20.1.jar"
    compile_cp = Path(sys.argv[2]).read_text(encoding="utf-8").strip()
    empty = task / "empty-sourcepath"
    empty.mkdir(exist_ok=True)
    mapped = task / "baseline-mapped.jar"

    def run(*arguments):
        result = subprocess.run([str(argument) for argument in arguments], capture_output=True)
        with (task / "verification/build.log").open("ab") as log:
            log.write(result.stdout + result.stderr)
        if result.returncode:
            print(result.stdout.decode("utf-8", errors="replace"))
            print(result.stderr.decode("utf-8", errors="replace"))
            raise subprocess.CalledProcessError(result.returncode, arguments)

    def args_file(name, values, encoding="utf-8"):
        destination = task / name
        destination.write_text("\n".join('"' + str(value).replace("\\", "/") + '"' for value in values), encoding=encoding)
        return "@" + str(destination)

    if not mapped.exists():
        run(java / "java.exe", "-Xmx2G", "-jar", renamer, "--input", baseline, "--output", mapped,
            "--map", mapping, "--lib", forge, "--threads", "2")
    sources = ["entity/BengerEntity.java", "client/render/BengerRenderer.java"]
    for phase in ("before", "after"):
        destination = task / (phase + "-classes")
        destination.mkdir(exist_ok=True)
        selected = [(task / "before" / Path(name).name) if phase == "before"
                    else project / "src/main/java/net/farmingtales/ftboss" / name for name in sources]
        run(java / "javac.exe", args_file(phase + "-compile.args", ["--release", "17", "-proc:none", "-encoding", "UTF-8",
            "-sourcepath", empty, "-cp", str(mapped) + os.pathsep + compile_cp, "-d", destination, *selected]))
        package = task / (phase + "-mapped.jar")
        run(java / "jar.exe", "--create", "--file", package, "-C", destination, ".")
        run(java / "java.exe", "-Xmx2G", "-jar", renamer, "--input", package, "--output", task / (phase + "-srg.jar"),
            "--map", mapping, "--reverse", "--lib", official, "--lib", mapped, "--threads", "2")
        print("PASS compile and remap:", phase, flush=True)
    runtime_cp = Path(sys.argv[3]).read_text(encoding="utf-8").strip()
    urls = " ".join(Path(entry).resolve().as_uri() for entry in runtime_cp.split(os.pathsep) if Path(entry).exists())
    attribute = "Class-Path: " + urls
    folded = attribute[:70] + "\r\n"
    attribute = attribute[70:]
    while attribute:
        folded += " " + attribute[:69] + "\r\n"
        attribute = attribute[69:]
    manifest = task / "dependencies.mf"
    manifest.write_text("Manifest-Version: 1.0\r\n" + folded + "\r\n", encoding="ascii", newline="")
    dependency_jar = task / "dependencies.jar"
    run(java / "jar.exe", "--create", "--file", dependency_jar, "--manifest", manifest, "-C", empty, ".")
    runtime_cp = str(baseline) + os.pathsep + str(dependency_jar)
    tool_classes = task / "tool-classes"
    tool_classes.mkdir(exist_ok=True)
    checks = [project / "tools/runtime_patches/BengerPhaseChargePatch.java", project / "tools/verification/ZipStreamCheck.java",
              Path("D:/ft工程文件/FTBoss-Benger-高速跑步-20261010-01a11a6e/runtime/TestLocomotionEntry.java")]
    run(java / "javac.exe", args_file("tools-compile.args", ["--release", "17", "-proc:none", "-encoding", "UTF-8",
        "-cp", runtime_cp, "-d", tool_classes, *checks]))
    run(java / "java.exe", args_file("patch.args", ["-Xverify:all", "-cp", str(tool_classes) + os.pathsep + runtime_cp,
        "BengerPhaseChargePatch", baseline, task / "before-srg.jar", task / "after-srg.jar", task / "payload", task / "verification/method-delta.txt"], encoding="gbk"))
    expanded = task / ("expanded-" + hashlib.sha256(baseline.read_bytes()).hexdigest()[:12])
    expanded.mkdir(exist_ok=True)
    with ZipFile(baseline) as archive:
        archive.extractall(expanded)
    payload = task / "payload"
    for source in payload.rglob("*.class"):
        target = expanded / source.relative_to(payload)
        shutil.copyfile(source, target)
    candidate = task / "qys-bosses.jar"
    run(java / "jar.exe", "--create", "--file", candidate, "--manifest", expanded / "META-INF/MANIFEST.MF", "-C", expanded, ".")
    with ZipFile(baseline) as before, ZipFile(candidate) as after:
        delta = {source.relative_to(payload).as_posix() for source in payload.rglob("*.class")}
        assert after.testzip() is None
        assert {name for name in before.namelist() if not name.endswith("/")} == {name for name in after.namelist() if not name.endswith("/")}
        assert all(name.endswith("/") for name in set(after.namelist()) - set(before.namelist()))
        assert len(after.namelist()) == len(set(after.namelist()))
        for name in before.namelist():
            if name not in delta:
                assert before.read(name) == after.read(name), name
    entry_cp = str(candidate) + os.pathsep + str(tool_classes) + os.pathsep + runtime_cp
    run(java / "java.exe", args_file("entry.args", ["-Xverify:all", "-cp", entry_cp, "TestLocomotionEntry", candidate], encoding="gbk"))
    run(java / "java.exe", args_file("zip.args", ["-cp", tool_classes, "ZipStreamCheck", candidate], encoding="gbk"))
    print("PASS: two-entry delta, all other archive contents identical; JVM skill/locomotion hooks and ZIP stream valid", flush=True)
    print("SHA256:", hashlib.sha256(candidate.read_bytes()).hexdigest(), flush=True)


if __name__ == "__main__":
    main()
