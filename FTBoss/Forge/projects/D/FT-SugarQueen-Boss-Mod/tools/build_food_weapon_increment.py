import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys
import zipfile


def sha(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


def json_object(pairs):
    result = {}
    for key, value in pairs:
        if key in result:
            raise ValueError("Duplicate JSON key: " + key)
        result[key] = value
    return result


def read_json(value):
    return json.loads(value, object_pairs_hook=json_object)


def main():
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")
    sys.stderr.reconfigure(encoding="utf-8", errors="replace")
    project = Path(__file__).resolve().parent.parent
    manifest_path = Path(sys.argv[1]).resolve()
    plan = read_json(manifest_path.read_text(encoding="utf-8"))
    baseline = Path(plan["baseline"]).resolve()
    live = Path(plan["live"]).resolve()
    task = Path(plan["output"]).resolve()
    task.relative_to(project / "_work")
    if task.exists():
        raise RuntimeError("Output must be a fresh directory; do not reuse stale candidates")
    task.mkdir(parents=True)
    expected = plan["baselineSha256"].lower()
    if sha(baseline) != expected or sha(live) != expected:
        raise RuntimeError("Baseline changed: stop and rebase")
    minecraft = Path(os.environ["APPDATA"]) / ".minecraft"
    java = minecraft / "runtime/java-runtime-gamma-snapshot/bin"
    mapping = Path.home() / ".gradle/caches/forge_gradle/minecraft_user_repo/de/oceanlabs/mcp/mcp_config/1.20.1-20230612.114412/srg_to_official_1.20.1.tsrg"
    forge = Path.home() / ".gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/1.20.1-47.4.10/forge-1.20.1-47.4.10-srg.jar"
    official_forge = Path.home() / ".gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/1.20.1-47.4.10_mapped_official_1.20.1/forge-1.20.1-47.4.10_mapped_official_1.20.1.jar"
    art = Path(plan["renamer"])
    asm = [minecraft / ("libraries/org/ow2/asm/" + name + "/9.7.1/" + name + "-9.7.1.jar")
           for name in ("asm", "asm-tree", "asm-commons", "asm-util", "asm-analysis")]
    compile_cp = Path(plan["compileClasspathFile"]).read_text(encoding="utf-8").strip()
    transcript = task / "verification.log"
    source_hashes = {}
    sources = []
    before_sources = []
    for relative in plan["javaSources"]:
        source = project / relative
        source.relative_to(project / "src/main/java")
        source_hashes[relative] = sha(source)
        target = task / "sources" / relative
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(source, target)
        sources.append(target)
        if relative in plan["beforeSources"]:
            old = Path(plan["beforeSources"][relative]).resolve()
            old_target = task / "before-sources" / relative
            old_target.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(old, old_target)
            before_sources.append(old_target)
    resource_snapshots = {}
    resource_additions = {}
    for relative in plan.get("newResources", []):
        source = (project / relative).resolve()
        entry = source.relative_to(project / "src/main/resources").as_posix()
        valid_model = entry.startswith("assets/ftboss/models/item/") and entry.endswith(".json")
        valid_texture = entry.startswith("assets/ftboss/textures/item/") and entry.endswith(".png")
        if not (valid_model or valid_texture) or entry != entry.lower() or entry in resource_additions:
            raise RuntimeError("Invalid or duplicate new item resource: " + relative)
        source_hashes[relative] = sha(source)
        resource_additions[entry] = source.read_bytes()
    for relative in ("src/main/resources/assets/ftboss/lang/zh_cn.json", "src/main/resources/assets/ftboss/lang/en_us.json", "src/main/resources/ftboss.mixins.json"):
        source = project / relative
        source_hashes[relative] = sha(source)
        resource_snapshots[relative] = read_json(source.read_text(encoding="utf-8"))
    for relative in ("tools/runtime_patches/FoodWeaponBuildPatch.java", "tools/verification/FoodWeaponBuildLinkCheck.java", "tools/verification/ZipStreamCheck.java"):
        source_hashes[relative] = sha(project / relative)
    lifecycle_checks = "src/main/java/net/farmingtales/ftboss/item/FoodWeaponLifecycle.java" in plan["javaSources"]
    ranged_checks = "src/main/java/net/farmingtales/ftboss/item/FoodWeaponRanged.java" in plan["javaSources"]
    if lifecycle_checks:
        relative = "tools/verification/FoodWeaponLifecycleHookCheck.java"
        source_hashes[relative] = sha(project / relative)
    if ranged_checks:
        relative = "tools/verification/FoodWeaponRangedHookCheck.java"
        source_hashes[relative] = sha(project / relative)
    source_hashes[str(Path(__file__).relative_to(project))] = sha(__file__)

    def run(*arguments):
        result = subprocess.run([str(argument) for argument in arguments], capture_output=True)
        with transcript.open("ab") as log:
            log.write(result.stdout + result.stderr)
        print(result.stdout.decode("utf-8", errors="replace"), end="", flush=True)
        if result.returncode:
            print(result.stderr.decode("utf-8", errors="replace"), end="", flush=True)
            raise subprocess.CalledProcessError(result.returncode, arguments)

    def arguments_file(name, options, encoding="gbk"):
        target = task / name
        target.write_text("\n".join('"' + str(value).replace("\\", "/") + '"' for value in options) + "\n", encoding=encoding)
        return "@" + str(target)

    def jar_from(directory, name):
        target = task / name
        run(java / "jar.exe", "--create", "--file", target, "-C", directory, ".")
        return target

    mapped = task / "baseline-mapped.jar"
    run(java / "java.exe", "-Xmx2G", "-jar", art, "--input", baseline, "--output", mapped,
        "--map", mapping, "--lib", forge, "--threads", "2")
    empty = task / "empty-sourcepath"
    empty.mkdir()
    before_classes = task / "before-classes"
    after_classes = task / "after-classes"
    tools = task / "tools"
    payload = task / "payload"
    for directory in (before_classes, after_classes, tools, payload):
        directory.mkdir()
    for name, directory, selected in (("before", before_classes, before_sources), ("after", after_classes, sources)):
        run(java / "javac.exe", "-J-Dfile.encoding=UTF-8", arguments_file(name + "-compile.args",
            ["--release", "17", "-encoding", "UTF-8", "-proc:none", "-sourcepath", empty,
             "-classpath", str(mapped) + os.pathsep + compile_cp, "-d", directory, *selected], encoding="utf-8"))
        official = jar_from(directory, name + "-mapped.jar")
        run(java / "java.exe", "-Xmx2G", "-jar", art, "--input", official, "--output", task / (name + "-srg.jar"),
            "--map", mapping, "--reverse", "--lib", official_forge, "--lib", mapped, "--threads", "2")
    tool_sources = [project / name for name in ("tools/runtime_patches/FoodWeaponBuildPatch.java",
                    "tools/verification/FoodWeaponBuildLinkCheck.java", "tools/verification/ZipStreamCheck.java")]
    if lifecycle_checks:
        tool_sources.append(project / "tools/verification/FoodWeaponLifecycleHookCheck.java")
    if ranged_checks:
        tool_sources.append(project / "tools/verification/FoodWeaponRangedHookCheck.java")
    tool_cp = os.pathsep.join(map(str, [tools, *asm]))
    run(java / "javac.exe", "-J-Dfile.encoding=UTF-8", arguments_file("tools-compile.args",
        ["--release", "17", "-encoding", "UTF-8", "-classpath", tool_cp, "-d", tools, *tool_sources], encoding="utf-8"))
    run(java / "java.exe", "-Xverify:all", "-cp", tool_cp, "FoodWeaponBuildPatch", baseline,
        task / "before-srg.jar", task / "after-srg.jar", payload, task / "method-delta.json")
    resource_report = {}
    with zipfile.ZipFile(baseline) as archive:
        for entry, blob in resource_additions.items():
            if entry in archive.namelist():
                raise RuntimeError("New item resource already present in latest baseline: " + entry)
            target = payload / entry
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(blob)
        for locale in ("zh_cn", "en_us"):
            entry = "assets/ftboss/lang/" + locale + ".json"
            relative = "src/main/resources/" + entry
            previous = read_json(Path(plan["resourceBefore"][relative]).read_text(encoding="utf-8"))
            current = resource_snapshots[relative]
            runtime = read_json(archive.read(entry))
            selected = {key: value for key, value in current.items() if previous.get(key) != value}
            allowed = plan.get("languageKeyWhitelist", {}).get(entry)
            if allowed is not None:
                if len(allowed) != len(set(allowed)) or any(key not in current for key in allowed):
                    raise RuntimeError("Invalid or missing language whitelist key: " + entry)
                selected = {key: value for key, value in selected.items() if key in allowed}
            if set(previous) - set(current):
                raise RuntimeError("Language key removal requires explicit rebase")
            for key, value in selected.items():
                if key in runtime and key not in previous and runtime[key] != value:
                    raise RuntimeError("Runtime-only new language key collision: " + key)
                if key in previous and key in runtime and runtime[key] not in (previous[key], value):
                    raise RuntimeError("Runtime-only language change in selected key: " + key)
                runtime[key] = value
            target = payload / entry
            target.parent.mkdir(parents=True, exist_ok=True)
            if selected:
                target.write_text(json.dumps(runtime, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
            else:
                target.write_bytes(archive.read(entry))
            resource_report[entry] = sorted(selected)
        mixins = read_json(archive.read("ftboss.mixins.json"))
        source_mixins = resource_snapshots["src/main/resources/ftboss.mixins.json"]
        additions = plan.get("mixinAdditions", ["WeaponMaterialDurabilityMixin"])
        required_lifecycle = {"WeaponMaterialDurabilityMixin", "FoodWeaponMendingMixin", "FoodWeaponRepairRecipeMixin"}
        required_ranged = {"FoodWeaponBowMixin", "FoodWeaponCrossbowMixin", "FoodWeaponStaffCastMixin"}
        permitted = required_lifecycle | required_ranged | {"CataclysmCavityUseMixin"}
        if len(additions) != len(set(additions)) or not set(additions).issubset(permitted):
            raise RuntimeError("Invalid lifecycle mixin addition whitelist")
        lifecycle_source = "src/main/java/net/farmingtales/ftboss/item/FoodWeaponLifecycle.java"
        if lifecycle_source in plan["javaSources"] and not required_lifecycle.issubset(additions):
            raise RuntimeError("Lifecycle build must include all durability, Mending and recipe hooks")
        if ranged_checks != required_ranged.issubset(additions) or bool(set(additions) & required_ranged) != ranged_checks:
            raise RuntimeError("Ranged build must include exactly all bow, crossbow and optional staff hooks")
        for name in additions:
            if name not in source_mixins["mixins"]:
                raise RuntimeError("Required mixin missing from source config: " + name)
            entry = "net/farmingtales/ftboss/mixin/" + name + ".class"
            if not (payload / entry).exists() and entry not in archive.namelist():
                raise RuntimeError("Mixin class absent from increment and baseline: " + name)
            if name not in mixins["mixins"]:
                mixins["mixins"].append(name)
        (payload / "ftboss.mixins.json").write_text(json.dumps(mixins, indent=2) + "\n", encoding="utf-8")
    entries = sorted(str(path.relative_to(payload)).replace("\\", "/") for path in payload.rglob("*") if path.is_file())
    candidate = task / "qys-bosses.validation-only.jar"
    shutil.copyfile(baseline, candidate)
    run(java / "jar.exe", arguments_file("update-jar.args", ["--update", "--file", candidate,
        *[argument for entry in entries for argument in ("-C", payload, entry)]]))
    preserved = {}
    changed = []
    with zipfile.ZipFile(baseline) as original, zipfile.ZipFile(candidate) as result:
        if len(result.namelist()) != len(set(result.namelist())) or set(result.namelist()) != set(original.namelist()) | set(entries):
            raise RuntimeError("Unexpected ZIP entry set")
        for name in original.namelist():
            if name not in entries:
                if original.read(name) != result.read(name):
                    raise RuntimeError("Unrelated runtime entry changed: " + name)
                preserved[name] = hashlib.sha256(original.read(name)).hexdigest()
        changed = [name for name in result.namelist() if name not in original.namelist() or original.read(name) != result.read(name)]
        if set(changed) - set(entries) or result.testzip() is not None:
            raise RuntimeError("Unexpected delta or ZIP CRC failure")
    runtime = [candidate, tools, forge, minecraft / "libraries/net/minecraftforge/eventbus/6.0.5/eventbus-6.0.5.jar", *asm]
    runtime.extend(path for path in (minecraft / "libraries").rglob("*.jar") if not any(marker in path.name for marker in ("natives", "sources", "javadoc")))
    runtime.extend(path for path in live.parent.glob("*.jar") if path.name != live.name)
    for index, path in enumerate(runtime):
        try:
            str(path).encode("gbk")
        except UnicodeEncodeError:
            alias = task / "runtime-library-aliases" / (sha(path) + ".jar")
            alias.parent.mkdir(exist_ok=True)
            if not alias.exists():
                shutil.copyfile(path, alias)
            runtime[index] = alias
    run(java / "java.exe", "-Dfile.encoding=UTF-8", arguments_file("runtime-link.args",
        ["-Xverify:all", "-cp", os.pathsep.join(map(str, runtime)), "FoodWeaponBuildLinkCheck", payload, candidate]))
    if lifecycle_checks:
        hook_input = task / "lifecycle-hook-input"
        with zipfile.ZipFile(candidate) as archive:
            for name in ("WeaponMaterialDurabilityMixin", "FoodWeaponMendingMixin", "FoodWeaponRepairRecipeMixin"):
                entry = "net/farmingtales/ftboss/mixin/" + name + ".class"
                target = hook_input / entry
                target.parent.mkdir(parents=True, exist_ok=True)
                target.write_bytes(archive.read(entry))
        run(java / "java.exe", "-Xverify:all", "-cp", tool_cp, "FoodWeaponLifecycleHookCheck", official_forge, forge, hook_input)
    if ranged_checks:
        actual_iron = []
        for runtime_jar in sorted(live.parent.glob("*.jar")):
            with zipfile.ZipFile(runtime_jar) as runtime_archive:
                if "io/redspace/ironsspellbooks/api/spells/AbstractSpell.class" in runtime_archive.namelist():
                    actual_iron.append(runtime_jar)
        if len(actual_iron) != 1:
            raise RuntimeError("Ranged Hook validation requires exactly one actual Iron Spellbooks runtime")
        iron_hash = sha(actual_iron[0])
        run(java / "java.exe", "-Xverify:all", "-cp", tool_cp, "FoodWeaponRangedHookCheck",
            official_forge, forge, actual_iron[0], payload)
        if sha(actual_iron[0]) != iron_hash:
            raise RuntimeError("Actual Iron Spellbooks runtime changed during Hook verification")
    run(java / "java.exe", "-Xverify:all", "-cp", tool_cp, "ZipStreamCheck", candidate)
    run("node", project / "tools/verification/check_jar_zip_rules.cjs", candidate)
    increment = task / "food-weapon-increment.jar"
    run(java / "jar.exe", arguments_file("increment-jar.args", ["--create", "--file", increment,
        *[argument for entry in entries for argument in ("-C", payload, entry)]]))
    run(java / "java.exe", "-Xverify:all", "-cp", tool_cp, "ZipStreamCheck", increment)
    for relative, expected_source in source_hashes.items():
        if sha(project / relative) != expected_source:
            raise RuntimeError("Source changed during verification: " + relative)
    if sha(live) != expected or sha(baseline) != expected:
        raise RuntimeError("Baseline changed during verification: stop and rebase, do not deploy")
    report = {"status": "PASS_OFFLINE_NOT_DEPLOYED", "baselineSha256": expected,
              "candidateSha256": sha(candidate), "incrementSha256": sha(increment),
              "actualProductionWrites": 0, "neoForgeWrites": 0, "gameTested": False,
              "strictClassAndMemberLinkPassed": True, "zipPassed": True,
              "changedEntries": changed, "preservedEntries": len(preserved), "languageKeys": resource_report,
              "newResourceSha256": {entry: hashlib.sha256(blob).hexdigest() for entry, blob in resource_additions.items()},
              "sourceHashes": source_hashes, "methodDeltaReport": "method-delta.json"}
    (task / "verification-report.json").write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    (task / "preserved-entries.json").write_text(json.dumps(preserved, indent=2) + "\n", encoding="utf-8")
    shutil.copyfile(manifest_path, task / "build-plan.json")
    print("PASS food weapon increment; formal JAR not written")


if __name__ == "__main__":
    main()
