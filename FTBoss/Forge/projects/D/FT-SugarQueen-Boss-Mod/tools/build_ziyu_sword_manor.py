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
    production = "net/farmingtales/ftboss/mixin/ZiyuSwordManorDamageMixin"
    mixin_name = "ZiyuSwordManorDamageMixin"
    config_name = "ftboss.mixins.json"
    digest = lambda path: hashlib.sha256(path.read_bytes()).hexdigest()
    baseline_hash = digest(baseline)
    native = next(baseline.parent.glob("*more_orn_plants-forge-1.20.1-1.3.3-Zhongqiu.jar"))
    native_hash = digest(native)
    source_paths = [project / ("src/main/java/" + production + ".java"), project / "src/main/resources/ftboss.mixins.json",
                    project / "tools/verification/ZiyuSwordManorDamageCheck.java", Path(__file__).resolve(),
                    project / "tools/deploy_ziyu_sword_manor.ps1"]
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

    donor, classes, payload, empty_source = (task / name for name in ("donor", "classes", "payload", "empty-source"))
    for directory in (donor, classes, payload, empty_source):
        directory.mkdir(exist_ok=True)
    mapped = next((Path.home() / ".gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge").rglob("*mapped_official_1.20.1.jar"))
    libraries = [entry for entry in (minecraft / "libraries").rglob("*.jar")
                 if not any(marker in entry.name for marker in ("-sources", "-javadoc", "-slim", "-extra"))]
    forge_libraries = [minecraft / "libraries/net/minecraftforge" / name / "1.20.1-47.4.10" / f"{name}-1.20.1-47.4.10.jar"
                       for name in ("javafmllanguage", "fmlcore")]
    eventbus = minecraft / "libraries/net/minecraftforge/eventbus/6.0.5/eventbus-6.0.5.jar"
    run(jdk / "javac.exe", args_file("compile-mixin.args", ["--release", "17", "-encoding", "UTF-8", "-proc:none", "-implicit:none",
        "-sourcepath", empty_source, "-cp", os.pathsep.join(map(str, [mapped, *forge_libraries, eventbus, baseline, *libraries])),
        "-d", donor, project / ("src/main/java/" + production + ".java")]))
    asm = [minecraft / f"libraries/org/ow2/asm/{name}/9.7.1/{name}-9.7.1.jar" for name in ("asm", "asm-tree", "asm-commons")]
    run(jdk / "javac.exe", args_file("compile-checks.args", ["--release", "17", "-encoding", "UTF-8", "-proc:none",
        "-cp", os.pathsep.join(map(str, asm)), "-d", classes,
        project / "tools/verification/ZiyuSwordManorDamageCheck.java", project / "tools/verification/ZipStreamCheck.java"]))
    tool_cp = os.pathsep.join(map(str, [classes, *asm]))
    run(jdk / "java.exe", "-Xverify:all", "-cp", tool_cp, "ZiyuSwordManorDamageCheck", "remap",
        donor / (production + ".class"), payload / (production + ".class"))
    with zipfile.ZipFile(backup) as original:
        runtime_config = json.loads(original.read(config_name))
    if mixin_name in runtime_config["mixins"]:
        raise RuntimeError("Installed baseline already contains this mixin")
    runtime_config["mixins"].insert(runtime_config["mixins"].index("SculkSpecialDamageMixin") + 1, mixin_name)
    (payload / config_name).write_text(json.dumps(runtime_config, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    staged = task / "qys-bosses.jar"
    shutil.copyfile(backup, staged)
    run(jdk / "jar.exe", "--update", "--file", staged, "-C", payload, "net", "-C", payload, config_name)
    run(jdk / "java.exe", "-Xverify:all", "-cp", tool_cp, "ZiyuSwordManorDamageCheck", "verify", staged, native, task)
    run(jdk / "java.exe", "-Xverify:all", "-cp", tool_cp, "ZipStreamCheck", staged)
    run("node", project / "tools/verification/check_jar_zip_rules.cjs", staged)
    with zipfile.ZipFile(backup) as original, zipfile.ZipFile(staged) as candidate:
        before, after = set(original.namelist()), set(candidate.namelist())
        if len(after) != len(candidate.infolist()) or before - after or after - before != {production + ".class"}:
            raise AssertionError("Unexpected runtime entry changes or duplicate entries")
        for name in before - {config_name}:
            if original.read(name) != candidate.read(name):
                raise AssertionError("Existing runtime entry changed: " + name)
        if json.loads(candidate.read(config_name)) != runtime_config:
            raise AssertionError("Unexpected Mixin configuration change")
        preserved = len(before) - 1
    if digest(baseline) != baseline_hash or digest(backup) != baseline_hash or digest(native) != native_hash:
        raise RuntimeError("Installed baseline changed during build; rebuild before deploying")
    if any(digest(Path(path)) != expected for path, expected in source_hashes.items()):
        raise RuntimeError("Source changed during build; rebuild before deploying")
    report = {"status": "PASS_OFFLINE", "baseline": str(baseline), "baselineSha256": baseline_hash,
              "candidate": str(staged), "candidateSha256": digest(staged), "backup": str(backup),
              "nativeMod": str(native), "nativeModSha256": native_hash, "preservedRuntimeEntries": preserved,
              "addedRuntimeEntries": [production + ".class"], "updatedRuntimeEntries": [config_name],
              "behavior": json.loads((task / "behavior-result.json").read_text(encoding="utf-8")),
              "gameplayVerified": False, "sources": source_hashes}
    (task / "verification.json").write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"PASS_OFFLINE: {preserved} runtime entries preserved; only one Mixin class and its registration added.", flush=True)


if __name__ == "__main__":
    main()
