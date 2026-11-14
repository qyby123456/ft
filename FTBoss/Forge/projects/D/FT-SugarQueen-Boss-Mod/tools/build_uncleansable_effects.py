import hashlib
import json
import os
from pathlib import Path
import re
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
    shutil.copytree(project / "tools/verification/uncleansable_effects", task / "tests/fixture-src", dirs_exist_ok=True)
    minecraft = Path(os.environ["APPDATA"]) / ".minecraft"
    jdk = minecraft / "runtime/java-runtime-gamma-snapshot/bin"
    live = minecraft / "versions/FarmingTales_ForgeⅡ v1.5.2/mods/qys-bosses.jar"
    digest = lambda path: hashlib.sha256(path.read_bytes()).hexdigest()
    baseline_hash = digest(baseline)
    if digest(live) != baseline_hash:
        raise RuntimeError("Baseline changed: stop and rebase")
    transcript = task / "verification.log"
    transcript.write_bytes(b"")

    def run(*arguments):
        result = subprocess.run([str(argument) for argument in arguments], capture_output=True)
        with transcript.open("ab") as output:
            output.write(result.stdout + result.stderr)
        encoding = "utf-8" if str(arguments[0]) == "node" else "cp936"
        print(result.stdout.decode(encoding, errors="replace"), end="", flush=True)
        if result.returncode:
            print(result.stderr.decode(encoding, errors="replace"), flush=True)
            raise subprocess.CalledProcessError(result.returncode, arguments)

    def args_file(name, options):
        path = task / name
        path.write_text("\n".join('"' + str(option).replace("\\", "/") + '"' for option in options) + "\n", encoding="gbk")
        return "@" + str(path)

    classes = task / "classes"
    donor = task / "donor"
    payload = task / "payload"
    for directory in (classes, donor, payload):
        directory.mkdir(exist_ok=True)
    asm = [minecraft / f"libraries/org/ow2/asm/{name}/9.7.1/{name}-9.7.1.jar" for name in ("asm", "asm-tree", "asm-commons")]
    mapped = next((Path.home() / ".gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge").rglob("*mapped_official_1.20.1.jar"))
    effect_source = project / "src/main/java/net/farmingtales/ftboss/effect/HealBanEffect.java"
    run(jdk / "javac.exe", args_file("compile-donor.args", ["--release", "17", "-proc:none", "-encoding", "UTF-8", "-cp",
        os.pathsep.join(map(str, [mapped, baseline, minecraft / "libraries/net/minecraftforge/eventbus/6.0.5/eventbus-6.0.5.jar",
                                  minecraft / "libraries/net/minecraftforge/javafmllanguage/1.20.1-47.4.10/javafmllanguage-1.20.1-47.4.10.jar"])),
        "-d", donor, effect_source]))
    sources = [project / "tools/runtime_patches/UncleansableEffectsPatch.java",
               project / "tools/verification/UncleansableEffectsLinkCheck.java", project / "tools/verification/ZipStreamCheck.java"]
    run(jdk / "javac.exe", args_file("compile-tools.args", ["--release", "17", "-encoding", "UTF-8", "-cp",
        os.pathsep.join(map(str, asm)), "-d", classes, *sources]))
    tool_cp = os.pathsep.join(map(str, [classes, *asm]))
    run(jdk / "java.exe", "-Xverify:all", "-cp", tool_cp, "UncleansableEffectsPatch", baseline,
        donor / "net/farmingtales/ftboss/effect/HealBanEffect.class", payload)
    tag = "data/irons_spellbooks/tags/mob_effects/cleanse_immune.json"
    descriptions = {
        "assets/ftboss/lang/zh_cn.json": "无法净化；无法回复血量（金苹果、生命恢复、瞬间治疗全都被拦下），自然到期后解除",
        "assets/ftboss/lang/en_us.json": "Cannot be cleansed; blocks health recovery (golden apples, regeneration and instant health) until natural expiration"}
    with zipfile.ZipFile(baseline) as archive:
        for required in ("net/farmingtales/ftboss/zheergenuncleansable/ErosionProtection.class",):
            if required not in archive.namelist():
                raise AssertionError("Missing existing erosion protection; audit baseline before patching")
        data = json.loads(archive.read(tag)) if tag in archive.namelist() else {"replace": False, "values": []}
        if data.get("replace", False):
            raise AssertionError("Unexpected replacing cleanse tag; preserve and re-audit")
        for effect in ("ftboss:shadow_erosion", "ftboss:heal_ban"):
            if not any(value == effect or isinstance(value, dict) and value.get("id") == effect for value in data["values"]):
                data["values"].append(effect)
        resources = {tag: data}
        for name, description in descriptions.items():
            translation = json.loads(archive.read(name))
            translation["effect.ftboss.heal_ban.desc"] = description
            resources[name] = translation
        for name, value in resources.items():
            path = payload / name
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    entries = [str(path.relative_to(payload)).replace("\\", "/") for path in payload.rglob("*") if path.is_file()]
    expected = {"net/farmingtales/ftboss/effect/HealBanEffect.class", "net/farmingtales/ftboss/boss/ForgeBlessing.class", tag, *descriptions}
    if set(entries) != expected:
        raise AssertionError("Unexpected payload scope")
    staged = task / "qys-bosses.validation-only.jar"
    shutil.copyfile(baseline, staged)
    run(jdk / "jar.exe", "--update", "--file", staged, *[argument for entry in entries for argument in ("-C", payload, entry)])
    runtime = [staged, classes, minecraft / "libraries/net/minecraftforge/eventbus/6.0.5/eventbus-6.0.5.jar",
               Path.home() / ".gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/1.20.1-47.4.10/forge-1.20.1-47.4.10-srg.jar"]
    runtime.extend(path for path in (minecraft / "libraries").rglob("*.jar") if not any(marker in path.name for marker in ("natives", "sources", "javadoc")))
    runtime.extend(path for path in live.parent.glob("*.jar") if path.name != "qys-bosses.jar")
    for index, path in enumerate(runtime):
        try:
            str(path).encode("gbk")
        except UnicodeEncodeError:
            alias = task / "runtime-libraries" / (digest(path) + ".jar")
            alias.parent.mkdir(exist_ok=True)
            if not alias.exists(): shutil.copyfile(path, alias)
            runtime[index] = alias
    run(jdk / "java.exe", args_file("runtime-link.args", ["-Xverify:all", "-cp", os.pathsep.join(map(str, runtime)), "UncleansableEffectsLinkCheck"]))
    fixtures = list((task / "tests/fixture-src").rglob("*.java"))
    fixture_classes = task / "tests/fixture-classes"
    fixture_classes.mkdir(exist_ok=True)
    fixture_cp = os.pathsep.join(map(str, runtime))
    run(jdk / "javac.exe", args_file("compile-fixtures.args", ["--release", "17", "-proc:none", "-encoding", "UTF-8", "-cp", fixture_cp, "-d", fixture_classes, *fixtures]))
    log_config = task / "log4j2-test.xml"
    log_config.write_text('<Configuration status="ERROR"><Appenders/><Loggers><Root level="off"/></Loggers></Configuration>\n', encoding="utf-8")
    for checker in ("CheckUncleansableEffects", "CheckBlessingCount"):
        run(jdk / "java.exe", args_file(checker + ".args", ["-Xverify:all", "-Dlog4j2.configurationFile=" + log_config.as_uri(),
            "-cp", os.pathsep.join(map(str, [fixture_classes, *runtime])), checker, staged]))
    run(jdk / "java.exe", "-Xverify:all", "-cp", tool_cp, "ZipStreamCheck", staged)
    run("node", project / "tools/verification/check_jar_zip_rules.cjs", staged)
    preserved = {}
    with zipfile.ZipFile(baseline) as original, zipfile.ZipFile(staged) as candidate:
        if len(set(candidate.namelist())) != len(candidate.namelist()) or set(original.namelist()) | expected != set(candidate.namelist()):
            raise AssertionError("Unexpected entries or duplicate names")
        changed = [name for name in candidate.namelist() if name not in original.namelist() or original.read(name) != candidate.read(name)]
        if set(changed) - expected: raise AssertionError("Unexpected changed entries: " + str(changed))
        for name in original.namelist():
            if name not in expected: preserved[name] = hashlib.sha256(original.read(name)).hexdigest()
        for name in descriptions:
            before = json.loads(original.read(name)); after = json.loads(candidate.read(name))
            before.pop("effect.ftboss.heal_ban.desc", None); after.pop("effect.ftboss.heal_ban.desc", None)
            if before != after: raise AssertionError("Other translations changed")
        if original.testzip() is not None or candidate.testzip() is not None: raise AssertionError("CRC failure")
    increment = task / "uncleansable-effects-increment.jar"
    run(jdk / "jar.exe", "--create", "--file", increment, *[argument for entry in entries for argument in ("-C", payload, entry)])
    run(jdk / "java.exe", "-Xverify:all", "-cp", tool_cp, "ZipStreamCheck", increment)
    run("node", project / "tools/verification/check_jar_zip_rules.cjs", increment)
    if digest(baseline) != baseline_hash or digest(live) != baseline_hash:
        raise RuntimeError("Production baseline changed: STOP, do not install old candidate")
    (task / "preserved-entries.json").write_text(json.dumps(preserved, indent=2) + "\n", encoding="utf-8")
    for source in [*sources, effect_source, Path(__file__).resolve()]:
        snapshot = task / "source" / source.name
        snapshot.parent.mkdir(exist_ok=True)
        shutil.copyfile(source, snapshot)
    report = {"status": "PASS_OFFLINE_NOT_DEPLOYED", "baselineSha256": baseline_hash,
              "validationJarSha256": digest(staged), "incrementSha256": digest(increment),
              "changedEntries": changed, "preservedEntries": len(preserved),
              "erosionRuntimeAndDamageUnchanged": True, "healingBlockLogicUnchanged": True,
              "healBanCategoryUnchanged": True, "finiteExpirationUnchanged": True,
              "strictClassLinkPassed": True, "zipPassed": True, "idempotent": True,
              "deployed": False, "gameTested": False, "productionWrites": 0, "neoForgeWrites": 0}
    report["removalFixtureAssertionCount"] = int(re.search(rb"mechanics assertions=(\d+)", transcript.read_bytes()).group(1))
    report["actualRuntimeBlessingLoopChecked"] = True
    (task / "verification-report.json").write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print("PASS uncleansable effects increment; all other runtime entries preserved; not deployed", flush=True)


if __name__ == "__main__":
    main()
