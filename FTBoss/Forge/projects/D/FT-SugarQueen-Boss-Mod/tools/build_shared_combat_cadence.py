import hashlib
import json
import os
from pathlib import Path
import subprocess
import shutil
import sys
import zipfile

TASK = Path(sys.argv[2]).resolve()
PROJECT = Path(__file__).resolve().parent.parent
TASK.relative_to(PROJECT / "_work")
TASK.mkdir(parents=True, exist_ok=True)
MINECRAFT = Path(os.environ["APPDATA"]) / ".minecraft"
JDK = MINECRAFT / "runtime/java-runtime-gamma-snapshot/bin"
BOSS = "net/farmingtales/ftboss/entity/EasterBossEntity"


def run(*arguments):
    print("RUN", arguments[0], flush=True)
    subprocess.run([str(argument) for argument in arguments], check=True)


def extract_method(source, name):
    position = source.index(" " + name + "(")
    start = source.rfind("\n", 0, position) + 1
    opening = source.index("{", position)
    depth = 1
    ending = opening + 1
    while depth:
        depth += (source[ending] == "{") - (source[ending] == "}")
        ending += 1
    return source[start:ending]


def main():
    baseline = Path(sys.argv[1]).resolve()
    baseline_hash = hashlib.sha256(baseline.read_bytes()).hexdigest()
    classes = TASK / "classes"
    payload = TASK / "payload"
    classes.mkdir(exist_ok=True)
    payload.mkdir(exist_ok=True)
    source = (PROJECT / ("src/main/java/" + BOSS + ".java")).read_text(encoding="utf-8")
    donor = TASK / "donor/EasterBossEntity.java"
    donor.parent.mkdir(exist_ok=True)
    helpers = "\n".join(extract_method(source, name) for name in ("tickSharedSkillCooldown", "expireCombatPoseTimers", "canScheduleSharedSkill"))
    goal_helper = extract_method(source, "refreshMeleeCooldown")
    donor.write_text("""package net.farmingtales.ftboss.entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
public class EasterBossEntity extends Monster {
    private static final int DORA_VARIANT = 2;
    private static EntityDataAccessor<CompoundTag> DATA_COMBAT_ANIMATION;
    private int actionTicks, castTicks, skillCd;
    private long sharedSkillClockTick;
    private boolean sharedSkillClockInitialized;
    private EasterBossEntity() { super(null, null); }
    public boolean isMini() { return false; }
    public int getVariant() { return 0; }
    private boolean skillCommitted() { return false; }
    private boolean movementLocked() { return false; }
""" + helpers + """
    private static class PlayerReachMeleeGoal extends MeleeAttackGoal {
        private EasterBossEntity boss;
        private int qingyiMeleeCooldown;
        private long meleeClockTick;
        private boolean meleeClockInitialized;
        private PlayerReachMeleeGoal() { super(null, 0, false); }
""" + goal_helper + "\n}\n}\n", encoding="utf-8")
    mapped = next((Path.home() / ".gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge").rglob("*mapped_official_1.20.1.jar"))
    asm = [MINECRAFT / f"libraries/org/ow2/asm/{name}/9.7.1/{name}-9.7.1.jar"
           for name in ("asm", "asm-tree", "asm-analysis", "asm-commons")]
    classpath = os.pathsep.join(map(str, [mapped, baseline, *asm]))
    sources = [donor, PROJECT / "src/main/java/net/farmingtales/ftboss/util/ManorCombatClock.java",
               PROJECT / "tools/runtime_patches/SharedCombatCadencePatch.java",
               PROJECT / "tools/runtime_patches/SharedCombatFrameRepair.java",
               PROJECT / "tools/verification/SharedCombatCadenceWiringCheck.java",
               PROJECT / "tools/verification/SharedCombatCadenceCheck.java",
               PROJECT / "tools/verification/SharedCombatClassLinkCheck.java",
               PROJECT / "tools/verification/ZipStreamCheck.java"]
    compile_args = TASK / "javac.args"
    compile_args.write_text("\n".join(['-encoding', 'UTF-8', '--release', '17', '-cp',
                                     '"' + classpath.replace('\\', '/') + '"', '-d',
                                     '"' + str(classes).replace('\\', '/') + '"',
                                     *['"' + str(path).replace('\\', '/') + '"' for path in sources]]), encoding="gbk")
    run(JDK / "javac.exe", "@" + str(compile_args))
    tool_classpath = os.pathsep.join(map(str, [classes, *asm]))
    run(JDK / "java.exe", "-Xverify:all", "-cp", tool_classpath, "SharedCombatCadencePatch", baseline, classes, payload)
    staged = TASK / "qys-bosses.jar"
    staged.write_bytes(baseline.read_bytes())
    run(JDK / "jar.exe", "--update", "--file", staged, "-C", payload, "net")
    run(JDK / "java.exe", "-Xverify:all", "-cp", tool_classpath, "SharedCombatCadenceWiringCheck", baseline, staged, "frames-repaired")
    run(JDK / "java.exe", "-Xverify:all", "-cp", tool_classpath, "SharedCombatCadenceCheck", staged, TASK / "timing-matrix.json")
    runtime = [staged, classes, Path.home() / ".gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/1.20.1-47.4.10/forge-1.20.1-47.4.10-srg.jar"]
    runtime += [entry for entry in (MINECRAFT / "libraries").rglob("*.jar")
                if not any(marker in entry.name for marker in ("natives", "sources", "javadoc"))]
    runtime += [entry for entry in (MINECRAFT / "versions/FarmingTales_ForgeⅡ v1.5.2/mods").glob("*.jar")
                if entry.name != "qys-bosses.jar"]
    for index, entry in enumerate(runtime):
        try:
            str(entry).encode("gbk")
        except UnicodeEncodeError:
            ascii_library = TASK / "runtime-libraries" / (hashlib.sha256(entry.read_bytes()).hexdigest() + ".jar")
            ascii_library.parent.mkdir(exist_ok=True)
            if not ascii_library.exists():
                shutil.copyfile(entry, ascii_library)
            runtime[index] = ascii_library
    runtime_args = TASK / "runtime-link.args"
    runtime_args.write_text('-Xverify:all\n-cp\n"' + os.pathsep.join(map(str, runtime)).replace('\\', '/')
                            + '"\nSharedCombatClassLinkCheck\n', encoding="gbk")
    baseline_link_args = TASK / "baseline-link.args"
    baseline_link_args.write_text(runtime_args.read_text(encoding="gbk").replace(
        str(staged).replace('\\', '/'), str(baseline).replace('\\', '/')), encoding="gbk")
    baseline_link = subprocess.run([str(JDK / "java.exe"), "@" + str(baseline_link_args)],
                                   capture_output=True)
    (TASK / "baseline-link.log").write_bytes(baseline_link.stdout + baseline_link.stderr)
    frame_args = TASK / "frame-repair.args"
    frame_args.write_text('-Xverify:all\n-cp\n"' + os.pathsep.join(map(str, runtime)).replace('\\', '/')
                          + '"\nSharedCombatFrameRepair\n"' + str(payload).replace('\\', '/') + '"\n"'
                          + str(TASK / "frame-repairs.txt").replace('\\', '/') + '"\n', encoding="gbk")
    run(JDK / "java.exe", "@" + str(frame_args))
    run(JDK / "jar.exe", "--update", "--file", staged, "-C", payload, "net")
    run(JDK / "java.exe", "-Xverify:all", "-cp", tool_classpath, "SharedCombatCadenceWiringCheck", baseline, staged, "frames-repaired")
    run(JDK / "java.exe", "@" + str(runtime_args))
    run(JDK / "java.exe", "-Xverify:all", "-cp", tool_classpath, "ZipStreamCheck", staged)
    run("node", PROJECT / "tools/verification/check_jar_zip_rules.cjs", staged)
    repeated = TASK / "idempotence"
    run(JDK / "java.exe", "-Xverify:all", "-cp", tool_classpath, "SharedCombatCadencePatch", staged, classes, repeated)
    for entry in payload.rglob("*.class"):
        if entry.read_bytes() != (repeated / entry.relative_to(payload)).read_bytes():
            raise AssertionError("Non-idempotent patch: " + str(entry))
    increment = TASK / "shared-combat-cadence-increment.jar"
    run(JDK / "jar.exe", "--create", "--file", increment, "-C", payload, "net")
    with zipfile.ZipFile(increment) as archive:
        actual = {entry.filename for entry in archive.infolist() if not entry.is_dir() and entry.filename != "META-INF/MANIFEST.MF"}
        expected = {str(entry.relative_to(payload)).replace('\\', '/') for entry in payload.rglob("*.class")}
        if actual != expected:
            raise AssertionError("Unexpected increment entries")
    protected = {}
    with zipfile.ZipFile(baseline) as original, zipfile.ZipFile(staged) as candidate:
        for entry in original.infolist():
            name = entry.filename
            if entry.is_dir() or not any(marker in name.lower() for marker in (
                    "spellvisual", "spellruntime", "reward", "voucher", "claim", "lootchest",
                    "network", "bossbar", "ftbossmod.class", "clientproxy", "meta-inf/",
                    "assets/ftboss/lang/")):
                continue
            content = original.read(name)
            if candidate.read(name) != content:
                raise AssertionError("Protected runtime entry changed: " + name)
            protected[name] = hashlib.sha256(content).hexdigest()
    (TASK / "protected-entries.json").write_text(json.dumps(protected, ensure_ascii=False, indent=2)
                                                + "\n", encoding="utf-8")
    if hashlib.sha256(baseline.read_bytes()).hexdigest() != baseline_hash:
        raise AssertionError("Baseline changed during build; discard candidate and rebase")
    timing = json.loads((TASK / "timing-matrix.json").read_text(encoding="utf-8"))
    report = {"status": "PASS_OFFLINE_BASELINE_BOUND_INCREMENT", "baseline": str(baseline),
              "baselineSha256": baseline_hash,
              "baselineStrictClassLinkPassed": baseline_link.returncode == 0,
              "strictClassLinkPassed": True, "zipValidated": True,
              "protectedEntryCount": len(protected),
              "timingScenarioCount": timing["scenarioCount"],
              "timingAssertionCount": timing["checks"],
              "frameRepairedMethods": (TASK / "frame-repairs.txt").read_text(encoding="utf-8").splitlines(),
              "validationJar": str(staged), "validationJarSha256": hashlib.sha256(staged.read_bytes()).hexdigest(),
              "increment": str(increment), "incrementSha256": hashlib.sha256(increment.read_bytes()).hexdigest(),
              "idempotent": True, "gameTested": False, "deployed": False,
              "entries": {str(entry.relative_to(payload)).replace('\\', '/'): hashlib.sha256(entry.read_bytes()).hexdigest()
                          for entry in payload.rglob("*.class")},
              "mergeRequirement": "Run method-level generator on final current shared classes; do not overwrite with old full validation JAR"}
    (TASK / "verification-report.json").write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print("PASS shared cadence candidate; source and runtime preserved; no deployment performed", flush=True)


if __name__ == "__main__":
    main()
