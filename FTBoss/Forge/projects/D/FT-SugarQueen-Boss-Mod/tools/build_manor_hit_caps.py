import argparse
import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import zipfile


def sha256(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("task", type=Path)
    arguments = parser.parse_args()
    project = Path(__file__).resolve().parents[1]
    task = arguments.task.resolve()
    if not task.is_relative_to(project / "_work") or task.exists():
        raise ValueError("Use a fresh task directory inside project _work")
    task.mkdir(parents=True)
    minecraft = Path(os.environ["APPDATA"]) / ".minecraft"
    instance = minecraft / "versions/FarmingTales_ForgeⅡ v1.5.2"
    live = instance / "mods/qys-bosses.jar"
    jdk = minecraft / "runtime/java-runtime-gamma-snapshot/bin"
    mapped_forge = Path.home() / ".gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/1.20.1-47.4.10_mapped_official_1.20.1/forge-1.20.1-47.4.10_mapped_official_1.20.1.jar"
    mappings = project / "build/createMcpToSrg/output.tsrg"
    source_root = project / "src/main/java/net/farmingtales/ftboss"
    production = [source_root / relative for relative in ["util/ManorDamageLimitRules.java", "boss/ai/ManorDamageLimit.java",
                  "boss/ai/SculkSpecialDamage.java", "mixin/SculkSpecialDamageMixin.java"]]
    tools = [project / "tools" / relative for relative in ["runtime_patches/ZicaiVoidPatch.java",
             "runtime_patches/diamond-alloy-durability-20261020/RemapRuntimeNames.java",
             "verification/ManorHitCapRuntimeTools.java", "verification/SculkSpecialMixinCheck.java",
             "verification/ZipStreamCheck.java", "verification/WeaponUiJarClosureCheck.java"]]
    resource = project / "src/main/resources/ftboss.mixins.json"
    input_files = production + tools + [resource, mappings, Path(__file__), project / "tools/deploy_manor_hit_caps.ps1",
                  project / "tools/deploy_jar.ps1",
                  project / "tools/verification/check_manor_hit_caps.py", project / "tools/verification/ManorHitCapCheck.java",
                  project / "tools/verification/ManorPlayerBalanceCheck.java", project / "tools/verification/check_jar_zip_rules.cjs"]
    hashes = {str(path): sha256(path) for path in input_files}
    baseline = task / "baseline.jar"
    shutil.copyfile(live, baseline)
    baseline_hash = sha256(baseline)
    if sha256(live) != baseline_hash:
        raise AssertionError("Live baseline changed while snapshotting")
    for directory in ["tools", "classes", "payload", "empty-sourcepath", "source-inputs"]:
        (task / directory).mkdir()
    for path in production + [resource]:
        destination = task / "source-inputs" / path.relative_to(project)
        destination.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(path, destination)
    asm = [minecraft / f"libraries/org/ow2/asm/{name}/9.7.1/{name}-9.7.1.jar"
           for name in ["asm", "asm-tree", "asm-analysis", "asm-commons"]]
    logs = []

    def run(command):
        result = subprocess.run([str(value) for value in command], cwd=project, capture_output=True,
                                encoding="utf-8", errors="replace")
        text = result.stdout + result.stderr
        logs.append(text)
        (task / "build.log").write_text("\n".join(logs), encoding="utf-8", newline="\n")
        print(text, end="", flush=True)
        result.check_returncode()

    def compile_java(files, classpath, destination, label):
        options = ["-encoding", "UTF-8", "--release", "17", "-proc:none", "-implicit:none", "-sourcepath",
                   task / "empty-sourcepath", "-cp", classpath, "-d", destination, *files]
        argument_file = task / (label + ".args")
        argument_file.write_text("\n".join('"' + str(value).replace("\\", "/").replace('"', '\\"') + '"'
                                            for value in options) + "\n", encoding="utf-8", newline="\n")
        run([jdk / "javac.exe", "-J-Dfile.encoding=UTF-8", "@" + str(argument_file)])

    def classpath_jar(name, paths):
        line = "Class-Path: " + " ".join(path.resolve().as_uri().replace("[", "%5B").replace("]", "%5D")
                                         + ("/" if path.is_dir() else "") for path in paths)
        manifest = "Manifest-Version: 1.0\r\n"
        while len(line) > 70:
            manifest += line[:70] + "\r\n"
            line = " " + line[70:]
        manifest_file = task / (name + ".mf")
        manifest_file.write_bytes((manifest + line + "\r\n\r\n").encode("ascii"))
        archive = task / (name + ".jar")
        run([jdk / "jar.exe", "--create", "--file", archive, "--manifest", manifest_file])
        return archive

    tool_classes = task / "tools"
    compile_java(tools, os.pathsep.join(map(str, asm)), tool_classes, "compile-tools")
    tool_classpath = os.pathsep.join(map(str, [tool_classes, *asm]))
    mapped_baseline = task / "baseline-mapped.jar"
    run([jdk / "java.exe", "-Dfile.encoding=UTF-8", "-cp", tool_classpath, "RemapRuntimeNames", mappings, baseline, mapped_baseline])
    version = json.loads((instance / "FarmingTales_ForgeⅡ v1.5.2.json").read_text(encoding="utf-8"))
    libraries = []
    for library in version["libraries"]:
        if "natives" in library["name"]:
            continue
        artifact = library.get("downloads", {}).get("artifact", {}).get("path")
        if not artifact:
            coordinates = library["name"].split(":")
            group, name, release = coordinates[:3]
            classifier = "-" + coordinates[3] if len(coordinates) > 3 else ""
            artifact = f"{group.replace('.', '/')}/{name}/{release}/{name}-{release}{classifier}.jar"
        path = minecraft / "libraries" / artifact
        if path.is_file():
            libraries.append(path)
    for name in ["fmlcore", "javafmllanguage", "lowcodelanguage", "mclanguage"]:
        libraries.append(minecraft / f"libraries/net/minecraftforge/{name}/1.20.1-47.4.10/{name}-1.20.1-47.4.10.jar")
    mods = sorted(path for path in (instance / "mods").glob("*.jar") if path != live)
    compile_dependencies = classpath_jar("compile-dependencies", [mapped_forge, mapped_baseline, *libraries, *mods])
    compile_java(production, str(compile_dependencies), task / "classes", "compile-production")
    remap_classpath = os.pathsep.join(map(str, [tool_classes, *asm, mapped_forge, mapped_baseline]))
    run([jdk / "java.exe", "-Dfile.encoding=UTF-8", "-Xverify:all", "-cp", remap_classpath,
         "ManorHitCapRuntimeTools", "remap", task / "classes", mappings, task / "payload"])
    with zipfile.ZipFile(baseline) as archive:
        old_config = json.loads(archive.read("ftboss.mixins.json"))
    if "SculkSpecialDamageMixin" in old_config["mixins"]:
        raise AssertionError("This exact compatibility Mixin is already installed")
    expected_config = json.loads(json.dumps(old_config))
    expected_config["mixins"].insert(expected_config["mixins"].index("TequilaDamageEventsMixin") + 1, "SculkSpecialDamageMixin")
    source_config = json.loads(resource.read_text(encoding="utf-8"))
    source_config["mixins"] = sorted(source_config["mixins"])
    comparison_config = json.loads(json.dumps(expected_config))
    comparison_config["mixins"] = sorted(comparison_config["mixins"])
    if source_config != comparison_config:
        raise AssertionError("Source Mixin members or settings differ from the runtime baseline plus the new entry")
    (task / "payload/ftboss.mixins.json").write_text(json.dumps(expected_config, ensure_ascii=False, indent=2) + "\n",
                                                   encoding="utf-8", newline="\n")
    candidate = task / "qys-bosses.jar"
    shutil.copyfile(baseline, candidate)
    run([jdk / "jar.exe", "--update", "--file", candidate, "-C", task / "payload", "."])
    expected_existing = {"ftboss.mixins.json", "net/farmingtales/ftboss/util/ManorDamageLimitRules.class",
                         "net/farmingtales/ftboss/util/ManorDamageLimitRules$Window.class",
                         "net/farmingtales/ftboss/boss/ai/ManorDamageLimit.class"}
    expected_added = {"net/farmingtales/ftboss/boss/ai/SculkSpecialDamage.class",
                      "net/farmingtales/ftboss/mixin/SculkSpecialDamageMixin.class"}
    with zipfile.ZipFile(baseline) as before, zipfile.ZipFile(candidate) as after:
        old_names, new_names = set(before.namelist()), set(after.namelist())
        changed = {name for name in old_names & new_names if before.read(name) != after.read(name)}
        if old_names - new_names or new_names - old_names != expected_added or changed != expected_existing:
            raise AssertionError(f"Unexpected runtime changes: changed={changed}, added={new_names-old_names}, removed={old_names-new_names}")
        if len(after.namelist()) != len(new_names) or after.testzip() is not None:
            raise AssertionError("Candidate ZIP duplicate or CRC validation failed")
        preserved = len(old_names - changed)
    run([jdk / "java.exe", "-Dfile.encoding=UTF-8", "-Xverify:all", "-cp", tool_classpath,
         "ManorHitCapRuntimeTools", "audit", candidate])
    run([jdk / "java.exe", "-Dfile.encoding=UTF-8", "-Xverify:all", "-cp", tool_classpath,
         "WeaponUiJarClosureCheck", candidate, "net/farmingtales/ftboss/boss/ai/SculkSpecialDamage",
         "net/farmingtales/ftboss/mixin/SculkSpecialDamageMixin", "net/farmingtales/ftboss/boss/ai/ManorDamageLimit"])
    run([jdk / "java.exe", "-Dfile.encoding=UTF-8", "-cp", tool_classpath, "ZipStreamCheck", candidate])
    run([shutil.which("node"), project / "tools/verification/check_jar_zip_rules.cjs", candidate])
    runtime_dependencies = classpath_jar("runtime-dependencies", [candidate, tool_classes,
            minecraft / "libraries/net/minecraftforge/forge/1.20.1-47.4.10/forge-1.20.1-47.4.10-universal.jar",
            Path.home() / ".gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/1.20.1-47.4.10/forge-1.20.1-47.4.10-srg.jar",
            *libraries, *mods])
    run([jdk / "java.exe", "-Dfile.encoding=UTF-8", "-Xverify:all", "-cp", runtime_dependencies, "ManorHitCapRuntimeTools", "link"])
    tavern = next(path for path in mods if "kaleidoscopetavern-1.2.0" in path.name)
    run([jdk / "java.exe", "-Dfile.encoding=UTF-8", "-Xverify:all", "-cp",
         os.pathsep.join(map(str, [candidate, tool_classes, *asm])), "SculkSpecialMixinCheck", tavern])
    fixture = task / "regression"
    run([os.sys.executable, project / "tools/verification/check_manor_hit_caps.py", "--output", fixture,
         "--java-home", jdk.parent, "--tavern", tavern])
    mapped_candidate = task / "candidate-mapped.jar"
    run([jdk / "java.exe", "-Dfile.encoding=UTF-8", "-cp", tool_classpath, "RemapRuntimeNames", mappings, candidate, mapped_candidate])
    mapped_payload = task / "mapped-candidate-tests"
    with zipfile.ZipFile(mapped_candidate) as archive:
        for path in (task / "payload").rglob("*.class"):
            relative = path.relative_to(task / "payload")
            destination = mapped_payload / relative
            destination.parent.mkdir(parents=True, exist_ok=True)
            destination.write_bytes(archive.read(relative.as_posix()))
    test_classpath = os.pathsep.join(map(str, [mapped_payload, fixture / "fixture-classes", *asm]))
    run([jdk / "java.exe", "-Dfile.encoding=UTF-8", "-Xverify:all", "-cp", test_classpath, "ManorHitCapCheck"])
    if sha256(live) != baseline_hash or sha256(baseline) != baseline_hash:
        raise AssertionError("Installed baseline changed during build")
    for filename, expected in hashes.items():
        if sha256(Path(filename)) != expected:
            raise AssertionError(f"Source changed during build: {filename}")
    report = {"passed": True, "baseline": str(baseline), "baselineSha256": baseline_hash,
              "candidate": str(candidate), "candidateSha256": sha256(candidate), "sources": hashes,
              "normalFinalHitCap": 100, "sculkSpecialFinalHitCap": 25, "rolling20TickBudget": 400,
              "changedEntries": sorted(changed), "addedEntries": sorted(expected_added), "preservedEntries": preserved,
              "runtimeDependencies": str(runtime_dependencies), "jdk": str(jdk), "asm": list(map(str, asm)),
              "gameplayTested": False, "fullMixinBootstrapTested": False, "docsTouched": False, "deployed": False}
    (task / "build-report.json").write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8", newline="\n")
    print(f"PASS incremental hit-cap candidate: {candidate}; {preserved} existing entries preserved byte-for-byte")


if __name__ == "__main__":
    main()
