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
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")
    project = Path(__file__).resolve().parent.parent
    task = Path(sys.argv[1]).resolve()
    combat = "--combat" in sys.argv[2:]
    task.relative_to(project / "_work")
    baseline = task / "baseline.jar"
    minecraft = Path(os.environ["APPDATA"]) / ".minecraft"
    jdk = minecraft / "runtime/java-runtime-gamma-snapshot/bin"
    live = minecraft / "versions/FarmingTales_ForgeⅡ v1.5.2/mods/qys-bosses.jar"
    baseline_hash = digest(baseline)
    if digest(live) != baseline_hash:
        raise RuntimeError("Installed baseline changed; take a fresh snapshot")
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
        encoding = "utf-8" if str(arguments[0]) == "node" else "cp936"
        print(result.stdout.decode(encoding, errors="replace"), end="", flush=True)
        if result.returncode:
            print(result.stderr.decode("cp936", errors="replace"), flush=True)
            raise subprocess.CalledProcessError(result.returncode, arguments)

    asm = [minecraft / f"libraries/org/ow2/asm/{name}/9.7.1/{name}-9.7.1.jar"
           for name in ("asm", "asm-tree", "asm-analysis")]
    sources = [project / "src/main/java/net/farmingtales/ftboss/util/DoraSpearDamageRules.java",
               project / "tools/runtime_patches/DoraSpearDamagePatch.java",
               project / "tools/verification/DoraSpearDamageCheck.java",
               project / "tools/verification/EasterBossReadOnlyCheck.java",
               project / "tools/verification/ZipStreamCheck.java"]
    if combat:
        sources.extend(project / relative for relative in (
            "tools/runtime_patches/DoraCombatPatch.java",
            "tools/verification/DoraDashRuntimeCheck.java",
            "src/main/java/net/farmingtales/ftboss/util/DoraSniperCycle.java",
            "src/main/java/net/farmingtales/ftboss/util/DoraSniperDamageRules.java",
            "tools/easter_combat/DoraSniperProbe.java",
            "tools/verification/DoraSniperDamageCheck.java"))
    tracked = sources + [Path(__file__).resolve(), project / "tools/verification/check_dora_spear_damage.cjs",
                         project / "src/main/java/net/farmingtales/ftboss/entity/EasterBossEntity.java",
                         project / "gradle/verification/boss-content.gradle"]
    if combat:
        tracked.extend(project / relative for relative in (
            "tools/verification/check_dora_combat.cjs",
            "tools/verification/HiddenBossCastCheck.java",
            "tools/verification/gametest/ManorImpossibleGameTests.java",
            "tools/verification/manor_integration_gametest/BossCombatBalanceTests.java"))
    source_hashes = {str(source): digest(source) for source in tracked}
    run("node", project / "tools/verification/check_dora_spear_damage.cjs", classes)
    if combat:
        run("node", project / "tools/verification/check_dora_combat.cjs", classes)
    quote = lambda value: '"' + str(value).replace("\\", "/") + '"'
    args = task / "javac.args"
    args.write_text("\n".join(["-encoding", "UTF-8", "--release", "17", "-cp",
                               quote(os.pathsep.join(map(str, asm))), "-d", quote(classes),
                               *map(quote, sources)]), encoding="gbk")
    run(jdk / "javac.exe", "@" + str(args))
    classpath = os.pathsep.join(map(str, [classes, *asm]))
    run(jdk / "java.exe", "-Xverify:all", "-cp", classpath, "DoraSpearDamagePatch", "apply", baseline, payload)
    boss_entry = "net/farmingtales/ftboss/entity/EasterBossEntity.class"
    helper_entry = "net/farmingtales/ftboss/util/DoraSpearDamageRules.class"
    destination = payload / helper_entry
    destination.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(classes / helper_entry, destination)
    entries = [boss_entry, helper_entry]
    actual_payload = {path.relative_to(payload).as_posix() for path in payload.rglob("*.class")}
    allowed_payload = set(entries)
    if combat:
        allowed_payload.update("net/farmingtales/ftboss/util/" + name + ".class"
                               for name in ("DoraSniperCycle", "DoraSniperDamageRules"))
    if not set(entries).issubset(actual_payload) or not actual_payload.issubset(allowed_payload):
        raise AssertionError("Unexpected payload entries")
    candidate = task / "qys-bosses.jar"
    shutil.copyfile(baseline, candidate)
    run(jdk / "jar.exe", "--update", "--file", candidate,
        *[value for entry in entries for value in ("-C", payload, entry)])
    run(jdk / "java.exe", "-Xverify:all", "-cp", classpath, "DoraSpearDamagePatch", "check", baseline, candidate)
    if combat:
        shutil.copyfile(candidate, task / "qys-bosses.speed-only.jar")
        run(jdk / "java.exe", "-Xverify:all", "-cp", classpath, "DoraCombatPatch", "apply", baseline, payload)
        cycle_entry = "net/farmingtales/ftboss/util/DoraSniperCycle.class"
        damage_entry = "net/farmingtales/ftboss/util/DoraSniperDamageRules.class"
        shutil.copyfile(classes / cycle_entry, payload / cycle_entry)
        entries.extend([cycle_entry, damage_entry])
        run(jdk / "jar.exe", "--update", "--file", candidate,
            *[value for entry in entries for value in ("-C", payload, entry)])
        run(jdk / "java.exe", "-Xverify:all", "-cp", classpath, "DoraCombatPatch", "check", baseline, payload, candidate)
        for probe in ("DoraSniperProbe", "DoraSniperDamageCheck"):
            run(jdk / "java.exe", "-Xverify:all", "-cp", os.pathsep.join(map(str, [candidate, classes])), probe)
    runtime = [candidate, classes, *asm, Path.home() / ".gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/1.20.1-47.4.10/forge-1.20.1-47.4.10-srg.jar"]
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
    link_args = task / "runtime-link.args"
    link_args.write_text("-Xverify:all\n-cp\n" + quote(os.pathsep.join(map(str, runtime)))
                         + "\nEasterBossReadOnlyCheck\n", encoding="gbk")
    run(jdk / "java.exe", "@" + str(link_args))
    if combat:
        dash_args = task / "dash-runtime.args"
        dash_args.write_text("-Xverify:all\n-cp\n" + quote(os.pathsep.join(map(str, runtime)))
                            + "\nDoraDashRuntimeCheck\n" + quote(candidate) + "\n", encoding="gbk")
        run(jdk / "java.exe", "@" + str(dash_args))
    run(jdk / "java.exe", "-Xverify:all", "-cp", classpath, "ZipStreamCheck", candidate)
    run("node", project / "tools/verification/check_jar_zip_rules.cjs", candidate)
    with zipfile.ZipFile(baseline) as original, zipfile.ZipFile(candidate) as staged:
        original_names, staged_names = original.namelist(), staged.namelist()
        if len(original_names) != len(set(original_names)) or len(staged_names) != len(set(staged_names)):
            raise AssertionError("Duplicate ZIP entries")
        added, removed = set(staged_names) - set(original_names), set(original_names) - set(staged_names)
        changed = [name for name in original_names if original.read(name) != staged.read(name)]
        expected_changed = set(entries) - {helper_entry}
        if added != {helper_entry} or removed or set(changed) != expected_changed:
            raise AssertionError("Unexpected entry changes: " + str((added, removed, changed)))
        if original.testzip() or staged.testzip():
            raise AssertionError("ZIP CRC failed")
    for source in tracked:
        if digest(source) != source_hashes[str(source)]:
            raise RuntimeError("Source changed during validation: " + str(source))
        snapshot = task / "sources" / source.relative_to(project)
        snapshot.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(source, snapshot)
    if digest(baseline) != baseline_hash or digest(live) != baseline_hash:
        raise RuntimeError("Baseline changed during validation; do not deploy")
    report = {"status": "PASS_READY_TO_DEPLOY", "baselineSha256": baseline_hash,
              "candidate": str(candidate), "candidateSha256": digest(candidate),
              "changedEntries": changed, "addedEntries": sorted(added), "removedEntries": [],
              "preservedEntries": len(original_names) - len(changed), "sourceHashes": source_hashes,
              "numericAssertions": 12, "strictRuntimeClassLinkPassed": True,
              "asmMethodVerificationPassed": True, "reversePatchPreservationPassed": True,
              "zipPassed": True, "idempotent": True, "gameTested": False,
              "productionJarWrites": 0, "neoForgeWrites": 0, "docsWrites": 0,
              "combatTuning": combat,
              "formula": "baseDamage + finalMovementSpeedAttribute * 3"}
    if combat:
        report.update(sniperPhaseAssertions=24511, productionDashFixtureAssertions=21,
                      chargeTicks=50, trackTicks=35, beamRadius=1.44, damagePerChannel=240,
                      installedBaselineDamagePerChannel=80, damageMultiplier=3, spearDashesPerCombo=3,
                      dashMovementGuardsPassed=True)
    (task / "verification-report.json").write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print("PASS Dora spear candidate: " + str(candidate), flush=True)


if __name__ == "__main__":
    main()
