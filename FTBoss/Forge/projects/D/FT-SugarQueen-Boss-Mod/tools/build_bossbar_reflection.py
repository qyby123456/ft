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
    live = minecraft / "versions/FarmingTales_ForgeⅡ v1.5.2/mods/qys-bosses.jar"
    digest = lambda path: hashlib.sha256(path.read_bytes()).hexdigest()
    baseline_hash = digest(baseline)
    if digest(live) != baseline_hash:
        raise RuntimeError("Production baseline changed; acquire shared locks and take a fresh snapshot")
    classes = task / "classes"
    payload = task / "payload"
    classes.mkdir(exist_ok=True)
    payload.mkdir(exist_ok=True)
    transcript = task / "offline-validation.log"
    transcript.write_bytes(b"")

    def run(*arguments):
        result = subprocess.run([str(argument) for argument in arguments], capture_output=True)
        with transcript.open("ab") as output:
            output.write(("RUN " + str(arguments[0]) + "\n").encode("utf-8"))
            output.write(result.stdout + result.stderr)
        encoding = "utf-8" if str(arguments[0]) == "node" else "cp936"
        print(result.stdout.decode(encoding, errors="replace"), end="", flush=True)
        if result.returncode:
            print(result.stderr.decode("cp936", errors="replace"), flush=True)
            raise subprocess.CalledProcessError(result.returncode, arguments)

    asm = [minecraft / f"libraries/org/ow2/asm/{name}/9.7.1/{name}-9.7.1.jar"
           for name in ("asm", "asm-tree", "asm-commons")]
    sources = [project / "tools/runtime_patches/BossBarReflectionPatch.java",
               project / "tools/verification/BossBarReflectionCheck.java",
               project / "tools/verification/BossBarClassLinkCheck.java",
               project / "tools/verification/EasterBossReadOnlyCheck.java",
               project / "tools/verification/ZipStreamCheck.java"]
    args = task / "javac.args"
    args.write_text("\n".join(["-encoding", "UTF-8", "--release", "17", "-cp",
                               '"' + os.pathsep.join(map(str, asm)).replace("\\", "/") + '"',
                               "-d", '"' + str(classes).replace("\\", "/") + '"',
                               *['"' + str(path).replace("\\", "/") + '"' for path in sources]]), encoding="gbk")
    run(jdk / "javac.exe", "@" + str(args))
    classpath = os.pathsep.join(map(str, [classes, *asm]))
    entry = "net/farmingtales/ftboss/client/BossBarOverlay.class"
    run(jdk / "java.exe", "-Xverify:all", "-cp", classpath, "BossBarReflectionPatch", baseline, payload)
    if {str(path.relative_to(payload)).replace("\\", "/") for path in payload.rglob("*.class")} != {entry}:
        raise AssertionError("Unexpected stale payload class")
    staged = task / "qys-bosses.validation-only.jar"
    shutil.copyfile(baseline, staged)
    run(jdk / "jar.exe", "--update", "--file", staged, "-C", payload, entry)
    run(jdk / "java.exe", "-Xverify:all", "-cp", classpath, "BossBarReflectionCheck", baseline, staged)

    runtime = [staged, classes, Path.home() / ".gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/1.20.1-47.4.10/forge-1.20.1-47.4.10-srg.jar"]
    runtime.extend(path for path in (minecraft / "libraries").rglob("*.jar")
                   if not any(marker in path.name for marker in ("natives", "sources", "javadoc")))
    runtime.extend(path for path in live.parent.glob("*.jar") if path.name != "qys-bosses.jar")
    for index, path in enumerate(runtime):
        try:
            str(path).encode("gbk")
        except UnicodeEncodeError:
            alias = task / "runtime-libraries" / (digest(path) + ".jar")
            alias.parent.mkdir(exist_ok=True)
            if not alias.exists():
                shutil.copyfile(path, alias)
            runtime[index] = alias
    runtime_prefix = '-Xverify:all\n-cp\n"' + os.pathsep.join(map(str, runtime)).replace("\\", "/") + '"\n'
    for checker in ("BossBarClassLinkCheck", "EasterBossReadOnlyCheck"):
        link_args = task / (checker + ".args")
        link_args.write_text(runtime_prefix + checker + "\n", encoding="gbk")
        run(jdk / "java.exe", "@" + str(link_args))
    run(jdk / "java.exe", "-Xverify:all", "-cp", classpath, "ZipStreamCheck", staged)
    run("node", project / "tools/verification/check_jar_zip_rules.cjs", staged)
    increment = task / "bossbar-reflection-increment.jar"
    run(jdk / "jar.exe", "--create", "--file", increment, "-C", payload, entry)
    run(jdk / "java.exe", "-Xverify:all", "-cp", classpath, "ZipStreamCheck", increment)
    run("node", project / "tools/verification/check_jar_zip_rules.cjs", increment)
    preserved = {}
    with zipfile.ZipFile(baseline) as original, zipfile.ZipFile(staged) as candidate:
        names = original.namelist()
        if len(set(names)) != len(names) or len(candidate.namelist()) != len(names) or set(candidate.namelist()) != set(names):
            raise AssertionError("Duplicate, added or removed JAR entries")
        changed = [name for name in names if original.read(name) != candidate.read(name)]
        if changed not in ([entry], []):
            raise AssertionError("Unexpected runtime entry change: " + str(changed))
        for info in original.infolist():
            if info.filename != entry:
                preserved[info.filename] = hashlib.sha256(original.read(info.filename)).hexdigest()
        if original.testzip() is not None or candidate.testzip() is not None:
            raise AssertionError("JAR CRC validation failed")
    with zipfile.ZipFile(increment) as archive:
        actual = {info.filename for info in archive.infolist()
                  if not info.is_dir() and info.filename != "META-INF/MANIFEST.MF"}
        if actual != {entry}:
            raise AssertionError("Unexpected increment contents")
    if digest(baseline) != baseline_hash or digest(live) != baseline_hash:
        raise RuntimeError("Baseline changed during validation; STOP and rebase, do not deploy this candidate")
    (task / "preserved-entries.json").write_text(json.dumps(preserved, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    for source in sources + [Path(__file__).resolve(), project / "src/main/java/net/farmingtales/ftboss/client/BossBarOverlay.java"]:
        snapshot = task / "source" / source.name
        snapshot.parent.mkdir(exist_ok=True)
        shutil.copyfile(source, snapshot)
    report = {"status": "PASS_OFFLINE_NOT_DEPLOYED", "baseline": str(baseline), "baselineSha256": baseline_hash,
              "liveHashAtEnd": digest(live), "validationJar": str(staged), "validationJarSha256": digest(staged),
              "increment": str(increment), "incrementSha256": digest(increment),
              "changedEntries": changed, "payloadEntry": entry,
              "payloadSha256": digest(payload / entry), "preservedEntries": len(preserved),
              "method": "BossBarOverlay.eventsRaw()Ljava/util/Map;",
              "scope": "One LDC field name and one invocation opcode/owner/name/descriptor; no other method/field/frame/metadata semantics changed",
              "zipValidated": True, "strictRuntimeClassLinkPassed": True,
              "easterBossStrictRuntimeClassLinkPassed": True, "idempotent": True,
              "protectedSpellRewardNetworkPreserved": True, "deployed": False,
              "productionJarWrites": 0, "neoForgeWrites": 0, "gameTested": False,
              "mergeRequirement": "DSH must acquire both shared locks, snapshot and regenerate from then-current baseline; never install this full validation-only JAR"}
    (task / "verification-report.json").write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print("PASS reflection increment; production and NeoForge untouched; baseline=" + baseline_hash, flush=True)


if __name__ == "__main__":
    main()
