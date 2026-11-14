import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys
import zipfile


def main():
    project = Path(__file__).resolve().parent.parent
    baseline = Path(sys.argv[1]).resolve()
    task = Path(sys.argv[2]).resolve()
    task.relative_to(project / "_work")
    task.mkdir(parents=True, exist_ok=True)
    minecraft = Path(os.environ["APPDATA"]) / ".minecraft"
    jdk = minecraft / "runtime/java-runtime-gamma-snapshot/bin"
    digest = lambda path: hashlib.sha256(path.read_bytes()).hexdigest()
    baseline_hash = digest(baseline)
    source_paths = [project / "src/main/java/net/farmingtales/ftboss/integ/RagingAttributeCompat.java",
                    project / "tools/verification/RagingAttributeCompatCheck.java", Path(__file__).resolve()]
    source_hashes = {str(path): digest(path) for path in source_paths}
    backup = task / "baseline-qys-bosses.jar"
    if backup.exists() and digest(backup) != baseline_hash:
        raise RuntimeError("Task backup belongs to another baseline; use a fresh task directory")
    if not backup.exists():
        shutil.copyfile(baseline, backup)
    transcript = task / "verification.log"
    transcript.write_bytes(b"")

    def run(*arguments):
        result = subprocess.run([str(argument) for argument in arguments], capture_output=True)
        with transcript.open("ab") as output:
            output.write(result.stdout + result.stderr)
        print(result.stdout.decode("utf-8", errors="replace"), end="", flush=True)
        if result.returncode:
            print(result.stderr.decode("utf-8", errors="replace"), flush=True)
            raise subprocess.CalledProcessError(result.returncode, arguments)

    def args_file(name, options):
        path = task / name
        path.write_text("\n".join('"' + str(option).replace("\\", "/") + '"' for option in options) + "\n", encoding="gbk")
        return "@" + str(path)

    donor, classes, payload = (task / name for name in ("donor", "classes", "payload"))
    for directory in (donor, classes, payload):
        directory.mkdir(exist_ok=True)
    mapped = next((Path.home() / ".gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge").rglob("*mapped_official_1.20.1.jar"))
    libraries = [entry for entry in (minecraft / "libraries").rglob("*.jar")
                 if not any(marker in entry.name for marker in ("-sources", "-javadoc", "-slim", "-extra"))]
    forge_libraries = [minecraft / "libraries/net/minecraftforge" / name / "1.20.1-47.4.10" / f"{name}-1.20.1-47.4.10.jar"
                       for name in ("javafmllanguage", "fmlcore")]
    eventbus = minecraft / "libraries/net/minecraftforge/eventbus/6.0.5/eventbus-6.0.5.jar"
    production = "net/farmingtales/ftboss/integ/RagingAttributeCompat"
    run(jdk / "javac.exe", args_file("compile-compat.args", ["-J-Duser.language=en", "-J-Duser.country=US", "--release", "17",
        "-encoding", "UTF-8", "-proc:none", "-implicit:none", "-cp", os.pathsep.join(map(str, [mapped, *forge_libraries, eventbus, baseline, *libraries])),
        "-d", donor, project / ("src/main/java/" + production + ".java")]))
    asm = [minecraft / f"libraries/org/ow2/asm/{name}/9.7.1/{name}-9.7.1.jar" for name in ("asm", "asm-tree", "asm-commons")]
    run(jdk / "javac.exe", args_file("compile-checks.args", ["--release", "17", "-encoding", "UTF-8", "-cp", os.pathsep.join(map(str, asm)),
        "-d", classes, project / "tools/verification/RagingAttributeCompatCheck.java", project / "tools/verification/ZipStreamCheck.java"]))
    tool_cp = os.pathsep.join(map(str, [classes, *asm]))
    run(jdk / "java.exe", "-Xverify:all", "-cp", tool_cp, "RagingAttributeCompatCheck", "remap", donor / (production + ".class"), payload / (production + ".class"))
    staged = task / "qys-bosses.jar"
    shutil.copyfile(backup, staged)
    run(jdk / "jar.exe", "--update", "--file", staged, "-C", payload, "net")
    brewin = next(baseline.parent.glob("*BrewinAndChewin*.jar"))
    run(jdk / "java.exe", "-Xverify:all", "-cp", tool_cp, "RagingAttributeCompatCheck", "verify", staged, brewin, task)
    run(jdk / "java.exe", "-Xverify:all", "-cp", tool_cp, "ZipStreamCheck", staged)
    run("node", project / "tools/verification/check_jar_zip_rules.cjs", staged)
    with zipfile.ZipFile(backup) as original, zipfile.ZipFile(staged) as candidate:
        before = set(original.namelist())
        after = set(candidate.namelist())
        if before - after or after - before != {production + ".class"}:
            raise AssertionError("Unexpected runtime entry changes")
        for name in before:
            if original.read(name) != candidate.read(name):
                raise AssertionError("Existing runtime entry changed: " + name)
        preserved = len(before)
    if digest(baseline) != baseline_hash or digest(backup) != baseline_hash:
        raise RuntimeError("Live baseline changed during build; do not deploy this candidate")
    if any(digest(Path(path)) != expected for path, expected in source_hashes.items()):
        raise RuntimeError("Source changed during build; rebuild before deployment")
    report = {"status": "PASS_OFFLINE", "baseline": str(baseline), "baselineSha256": baseline_hash,
              "candidate": str(staged), "candidateSha256": digest(staged), "backup": str(backup),
              "preservedRuntimeEntries": preserved, "addedRuntimeEntries": [production + ".class"],
              "behavior": json.loads((task / "behavior-result.json").read_text(encoding="utf-8")),
              "gameplayVerified": False, "docsImpact": "none", "sources": source_hashes}
    (task / "verification.json").write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(report, ensure_ascii=False, indent=2), flush=True)


if __name__ == "__main__":
    main()
