import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys
import zipfile


def digest(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest().upper()


def main():
    project = Path(__file__).resolve().parent.parent
    baseline, task = (Path(argument).resolve() for argument in sys.argv[1:3])
    task.relative_to(project / "_work")
    task.mkdir(parents=True, exist_ok=True)
    minecraft = Path(os.environ["APPDATA"]) / ".minecraft"
    jdk = minecraft / "runtime/java-runtime-gamma-snapshot/bin"
    live = minecraft / "versions/FarmingTales_ForgeⅡ v1.5.2/mods/qys-bosses.jar"
    baseline_hash = digest(baseline)
    if digest(live) != baseline_hash:
        raise RuntimeError("Live baseline changed; take a fresh snapshot under both locks")
    classes, payload = task / "classes", task / "payload"
    classes.mkdir(exist_ok=True)
    payload.mkdir(exist_ok=True)
    transcript = task / "verification.log"
    transcript.write_bytes(b"")

    def run(*arguments):
        result = subprocess.run([str(argument) for argument in arguments], capture_output=True)
        with transcript.open("ab") as output:
            output.write(("RUN " + str(arguments[0]) + "\n").encode("utf-8"))
            output.write(result.stdout + result.stderr)
        print(result.stdout.decode("utf-8" if str(arguments[0]) == "node" else "cp936", errors="replace"), end="", flush=True)
        if result.returncode:
            print(result.stderr.decode("cp936", errors="replace"), flush=True)
            raise subprocess.CalledProcessError(result.returncode, arguments)

    asm = [minecraft / f"libraries/org/ow2/asm/{name}/9.7.1/{name}-9.7.1.jar"
           for name in ("asm", "asm-tree", "asm-analysis")]
    helper = project / "src/main/java/net/farmingtales/ftboss/util/FullFightFlightRules.java"
    sources = [helper, project / "tools/runtime_patches/ManorBaseMovementPatch.java", project / "tools/runtime_patches/FullFightFlightPatch.java",
               project / "tools/verification/FullFightFlightCheck.java",
               project / "tools/verification/EasterBossReadOnlyCheck.java",
               project / "tools/verification/ZipStreamCheck.java"]
    tracked = sources + [Path(__file__).resolve(), project / "tools/deploy_jar.ps1",
                         project / "src/main/java/net/farmingtales/ftboss/entity/EasterBossEntity.java",
                         project / "src/main/java/net/farmingtales/ftboss/boss/ai/BossWalkingSpeed.java"]
    fixture_sources = sorted((project / "tools/verification/full_fight_flight_fixtures").rglob("*.java"))
    tracked += fixture_sources + [project / "src/main/java/net/farmingtales/ftboss/boss/ManorFight.java",
                                  project / "src/main/java/net/farmingtales/ftboss/boss/ai/ManorFlyBan.java",
                                  project / "src/main/java/net/farmingtales/ftboss/boss/ai/FullFightFlightBan.java"]
    source_hashes = {str(source): digest(source) for source in tracked}
    args = task / "javac.args"
    quote = lambda value: '"' + str(value).replace("\\", "/") + '"'
    args.write_text("\n".join(["-encoding", "UTF-8", "--release", "17", "-cp",
                               quote(os.pathsep.join(map(str, asm))), "-d", quote(classes),
                               *map(quote, sources)]), encoding="gbk")
    run(jdk / "javac.exe", "@" + str(args))
    fixtures = task / "fixtures"
    fixtures.mkdir(exist_ok=True)
    fixture_args = task / "fixtures-javac.args"
    fixture_args.write_text("\n".join(["-encoding", "UTF-8", "--release", "17", "-d", quote(fixtures),
                                        *map(quote, fixture_sources)]), encoding="gbk")
    run(jdk / "javac.exe", "@" + str(fixture_args))
    classpath = os.pathsep.join(map(str, [classes, *asm]))
    run(jdk / "java.exe", "-Xverify:all", "-cp", classpath, "FullFightFlightPatch", baseline, payload)
    owners = ["net/farmingtales/ftboss/entity/EasterBossEntity",
              "net/farmingtales/ftboss/boss/ManorFight",
              "net/farmingtales/ftboss/boss/ai/ManorFlyBan",
              "net/farmingtales/ftboss/boss/ai/FullFightFlightBan",
              "net/farmingtales/ftboss/util/FullFightFlightRules"]
    entries = [owner + ".class" for owner in owners]
    helper_entry = entries[-1]
    destination = payload / helper_entry
    destination.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(classes / helper_entry, destination)
    actual_payload = {str(path.relative_to(payload)).replace("\\", "/") for path in payload.rglob("*.class")}
    if actual_payload != set(entries):
        raise AssertionError("Unexpected payload entries")
    candidate = task / "qys-bosses.full-fight-flight-candidate.jar"
    shutil.copyfile(baseline, candidate)
    run(jdk / "jar.exe", "--update", "--file", candidate,
        *[value for entry in entries for value in ("-C", payload, entry)])
    assertion_file = task / "assertion-count.txt"
    run(jdk / "java.exe", "-Xverify:all", "-cp", classpath, "FullFightFlightCheck",
        baseline, candidate, fixtures, assertion_file)
    runtime = [candidate, classes, Path.home() / ".gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/1.20.1-47.4.10/forge-1.20.1-47.4.10-srg.jar"]
    runtime.extend(path for path in (minecraft / "libraries").rglob("*.jar")
                   if not any(marker in path.name for marker in ("natives", "sources", "javadoc")))
    runtime.extend(path for path in live.parent.glob("*.jar") if path.name != "qys-bosses.jar")
    for index, path in enumerate(runtime):
        try:
            str(path).encode("gbk")
        except UnicodeEncodeError:
            alias = task / "runtime-libraries" / (digest(path) + ".jar")
            alias.parent.mkdir(exist_ok=True)
            shutil.copyfile(path, alias)
            runtime[index] = alias
    runtime_prefix = "-Xverify:all\n-cp\n" + quote(os.pathsep.join(map(str, runtime))) + "\n"
    for checker, arguments in [("FullFightFlightCheck", "--link"), ("EasterBossReadOnlyCheck", "")]:
        link_args = task / (checker + ".args")
        link_args.write_text(runtime_prefix + checker + "\n" + arguments + "\n", encoding="gbk")
        run(jdk / "java.exe", "@" + str(link_args))
    run(jdk / "java.exe", "-Xverify:all", "-cp", classpath, "ZipStreamCheck", candidate)
    run("node", project / "tools/verification/check_jar_zip_rules.cjs", candidate)
    increment = task / "full-fight-flight-increment.jar"
    run(jdk / "jar.exe", "--create", "--file", increment,
        *[value for entry in entries for value in ("-C", payload, entry)])
    run(jdk / "java.exe", "-Xverify:all", "-cp", classpath, "ZipStreamCheck", increment)
    run("node", project / "tools/verification/check_jar_zip_rules.cjs", increment)
    preserved = {}
    with zipfile.ZipFile(baseline) as original, zipfile.ZipFile(candidate) as staged:
        original_names, staged_names = original.namelist(), staged.namelist()
        if len(original_names) != len(set(original_names)) or len(staged_names) != len(set(staged_names)):
            raise AssertionError("Duplicate entries")
        added = set(staged_names) - set(original_names)
        removed = set(original_names) - set(staged_names)
        if added != set(entries[-2:]) or removed:
            raise AssertionError("Unexpected added or removed entries")
        changed = [name for name in original_names if original.read(name) != staged.read(name)]
        if set(changed) != set(entries[:3]):
            raise AssertionError("Unexpected changed entries " + str(changed))
        for name in original_names:
            if name not in changed:
                preserved[name] = hashlib.sha256(original.read(name)).hexdigest().upper()
        if original.testzip() or staged.testzip():
            raise AssertionError("ZIP CRC failed")
    for source in tracked:
        if digest(source) != source_hashes[str(source)]:
            raise RuntimeError("Source changed during validation " + str(source))
        snapshot = task / "sources" / source.relative_to(project)
        snapshot.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(source, snapshot)
    if digest(baseline) != baseline_hash or digest(live) != baseline_hash:
        raise RuntimeError("Baseline changed during validation; STOP, do not deploy")
    (task / "preserved-entries.json").write_text(json.dumps(preserved, indent=2) + "\n", encoding="utf-8")
    report = {"status": "PASS_READY_TO_DEPLOY", "baselineSha256": baseline_hash,
              "candidate": str(candidate), "candidateSha256": digest(candidate),
              "increment": str(increment), "incrementSha256": digest(increment),
              "changedEntries": changed, "addedEntries": sorted(added), "removedEntries": [],
              "preservedEntries": len(preserved), "fixtureAssertionCount": int(assertion_file.read_text()),
              "sourceHashes": source_hashes, "strictRuntimeClassLinkPassed": True,
              "asmMethodVerificationPassed": True, "reversePatchPreservationPassed": True,
              "zipPassed": True, "idempotent": True, "gameTested": False,
              "productionJarWrites": 0, "neoForgeWrites": 0,
              "scope": "Full active manor fight flight ban; all participants and 96-block horizontal arena column; no buff flight pursuit; other combat unchanged"}
    (task / "verification-report.json").write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print("PASS ready to deploy current-baseline full-fight flight candidate " + digest(candidate), flush=True)


if __name__ == "__main__":
    main()
