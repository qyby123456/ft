import argparse
import hashlib
import json
from pathlib import Path
import shutil
import subprocess
import sys
import time
import zipfile

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "tools/model3d"))
from gen_material_crossbows import VARIANTS
from render_material_crossbows import verify_geometry

JAVA_SOURCES = (
    "net/farmingtales/ftboss/item/weapons/MaterialCrossbowItem.java",
    "net/farmingtales/ftboss/item/MaterialCrossbows.java",
    "net/farmingtales/ftboss/client/model/MaterialCrossbowModel.java",
    "net/farmingtales/ftboss/util/foundations/MaterialCrossbowProfiles.java",
    "net/farmingtales/ftboss/util/foundations/FourWeaponFoundationProfiles.java",
)


def digest(data):
    return hashlib.sha256(data).hexdigest()


def run(arguments, log, cwd=ROOT):
    result = subprocess.run([str(argument) for argument in arguments], cwd=cwd, capture_output=True)
    log.write_bytes(result.stdout + result.stderr)
    if result.returncode:
        print(log.read_text(encoding="utf-8", errors="replace"))
        raise RuntimeError(f"Command failed ({result.returncode}): {arguments[0]}; log: {log}")
    print(log.name, "PASS")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--baseline", type=Path, required=True)
    parser.add_argument("--classpath-json", type=Path, required=True)
    parser.add_argument("--mappings", type=Path, required=True)
    parser.add_argument("--renamer", type=Path, required=True)
    parser.add_argument("--jdk", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    work = ROOT / "_work/material-crossbows-20261019" / ("build-" + str(time.time_ns()))
    work.mkdir(parents=True)
    classes, tests, overlay = work / "classes", work / "test-classes", work / "overlay"
    for directory in (classes, tests, overlay, work / "empty-sourcepath"):
        directory.mkdir()
    verify_geometry()
    cp_manifest = json.loads(args.classpath_json.read_text(encoding="utf-8-sig"))
    classpath = cp_manifest["pathingEntries"]
    missing = [entry for entry in classpath if not Path(entry).exists()]
    if missing:
        raise RuntimeError("Missing compile dependency: " + repr(missing))
    source_hashes = {source: digest((ROOT / "src/main/java" / source).read_bytes()) for source in JAVA_SOURCES}
    baseline = args.baseline.read_bytes()
    shutil.copy2(args.baseline, work / "baseline.jar")
    run(["node", ROOT / "tools/verification/check_jar_zip_rules.cjs", work / "baseline.jar"], work / "baseline-zip-rules.log")
    java, javac, jar = [args.jdk / "bin" / (name + ".exe") for name in ("java", "javac", "jar")]
    common = [javac, "-encoding", "UTF-8", "-proc:none", "-implicit:none", "-source", "17", "-target", "17"]
    run(common + ["-sourcepath", work / "empty-sourcepath", "-classpath", ";".join(classpath), "-d", classes]
        + [ROOT / "src/main/java" / source for source in JAVA_SOURCES], work / "compile.log")
    run(common + ["-classpath", ";".join(classpath), "-d", tests,
                  ROOT / "tools/build/InjectMaterialCrossbowProfiles.java"], work / "profile-injector-compile.log")
    run([java, "-classpath", str(tests) + ";" + ";".join(classpath), "net.farmingtales.ftboss.tools.InjectMaterialCrossbowProfiles",
         work / "baseline.jar", classes], work / "profile-injection.log")
    run(common + ["-classpath", str(classes) + ";" + ";".join(classpath), "-d", tests,
                  ROOT / "tools/verification/MaterialCrossbowModelCheck.java"], work / "test-compile.log")
    run([java, "-ea", "-classpath", str(tests) + ";" + str(classes) + ";" + ";".join(classpath),
         "net.farmingtales.ftboss.verification.MaterialCrossbowModelCheck"], work / "vertex-tests.log")
    run(common + ["-classpath", str(classes) + ";" + ";".join(classpath), "-d", tests,
                  ROOT / "tools/verification/MaterialCrossbowProfilesCheck.java"], work / "profiles-compile.log")
    run([java, "-ea", "-classpath", str(tests) + ";" + str(classes) + ";" + ";".join(classpath),
         "net.farmingtales.ftboss.verification.MaterialCrossbowProfilesCheck", work / "baseline.jar"], work / "profiles-tests.log")
    pure_sources = [ROOT / "src/main/java/net/farmingtales/ftboss/util" / source
                    for source in ("FoodWeaponFamilyRules.java", "FoodWeaponBuildRules.java")]
    run(common + ["-d", tests] + pure_sources + [ROOT / "tools/verification/MaterialCrossbowFamilyCheck.java"], work / "family-compile.log")
    run([java, "-ea", "-classpath", tests, "net.farmingtales.ftboss.verification.MaterialCrossbowFamilyCheck"],
        work / "family-tests.log")
    family = (ROOT / "src/main/java/net/farmingtales/ftboss/item/FoodWeaponFamilyBuild.java").read_text(encoding="utf-8")
    evolution = (ROOT / "src/main/java/net/farmingtales/ftboss/item/FoodWeaponEvolution.java").read_text(encoding="utf-8")
    mixin = (ROOT / "src/main/java/net/farmingtales/ftboss/mixin/FoodWeaponCrossbowMixin.java").read_text(encoding="utf-8")
    assert "instanceof CrossbowItem) return Kind.BOW" in family
    assert "item instanceof CrossbowItem" in evolution
    assert "FoodWeaponFamilyBuild.chargeTicks" in mixin and "FoodWeaponRanged.capture" in mixin and "FoodWeaponRanged.launch" in mixin
    run([jar, "--create", "--file", work / "named.jar", "-C", classes, "."], work / "named-jar.log")
    rename_args = ["-jar", args.renamer, "--input", work / "named.jar", "--output", work / "reobf.jar", "--names", args.mappings]
    for dependency in classpath:
        if Path(dependency).is_file() and dependency.endswith(".jar"):
            rename_args.extend(["--lib", dependency])
    (work / "renamer-command.json").write_text(json.dumps([str(java)] + [str(argument) for argument in rename_args],
                                                            ensure_ascii=False, indent=2) + "\n", encoding="utf-8", newline="\n")
    run([java] + rename_args, work / "reobf.log")
    with zipfile.ZipFile(work / "reobf.jar") as compiled:
        for entry in compiled.namelist():
            if entry.endswith(".class"):
                target = overlay / entry
                target.parent.mkdir(parents=True, exist_ok=True)
                target.write_bytes(compiled.read(entry))
    resources = ROOT / "src/main/resources"
    ids = [variant[0] + "_crossbow" for variant in VARIANTS]
    for item_id in ids:
        for relative in [f"assets/ftboss/models/item/{item_id}.json", f"assets/ftboss/textures/item/{item_id}.png",
                         f"data/ftboss/recipes/{item_id}.json", f"data/ftboss/advancements/recipes/combat/{item_id}.json"]:
            target = overlay / relative
            target.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(resources / relative, target)
        shutil.copytree(resources / "assets/ftboss/models/item" / (item_id + "_parts"),
                        overlay / "assets/ftboss/models/item" / (item_id + "_parts"))
    with zipfile.ZipFile(work / "baseline.jar") as original:
        for language in ("zh_cn", "en_us"):
            relative = f"assets/ftboss/lang/{language}.json"
            contents = json.loads(original.read(relative).decode("utf-8-sig"))
            current = json.loads((resources / relative).read_text(encoding="utf-8-sig"))
            for item_id in ids:
                contents["item.ftboss." + item_id] = current["item.ftboss." + item_id]
            target = overlay / relative
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_text(json.dumps(contents, ensure_ascii=False, indent=2) + "\n", encoding="utf-8", newline="\n")
        for relative in ("data/forge/tags/items/crossbows.json", "data/forge/tags/items/tools/crossbows.json", "data/minecraft/tags/items/crossbows.json"):
            contents = json.loads(original.read(relative).decode("utf-8-sig")) if relative in original.namelist() else {"replace": False, "values": []}
            for item_id in ids:
                value = "ftboss:" + item_id
                if value not in contents["values"]:
                    contents["values"].append(value)
            target = overlay / relative
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_text(json.dumps(contents, ensure_ascii=False, indent=2) + "\n", encoding="utf-8", newline="\n")
    args.output.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(work / "baseline.jar", args.output)
    run([jar, "--update", "--file", args.output, "-C", overlay, "."], work / "package.log")
    run(["node", ROOT / "tools/verification/check_jar_zip_rules.cjs", args.output], work / "zip-rules.log")
    changed, added = [], []
    with zipfile.ZipFile(work / "baseline.jar") as original, zipfile.ZipFile(args.output) as final:
        original_names, final_names = set(original.namelist()), set(final.namelist())
        assert original_names <= final_names, "Removed original entries"
        assert len(final.namelist()) == len(final_names), "Duplicate JAR entries"
        for source in overlay.rglob("*"):
            if source.is_file():
                relative = source.relative_to(overlay).as_posix()
                assert final.read(relative) == source.read_bytes(), "Packaged content mismatch: " + relative
        for entry in original_names:
            if original.read(entry) != final.read(entry):
                assert (overlay / entry).is_file(), "Unrelated change: " + entry
                changed.append(entry)
        added = sorted(final_names - original_names)
        profile_class = "net/farmingtales/ftboss/util/foundations/FourWeaponFoundationProfiles.class"
        assert not any(entry.endswith(".class") and entry != profile_class for entry in changed), "Replaced unrelated production class"
    assert source_hashes == {source: digest((ROOT / "src/main/java" / source).read_bytes()) for source in JAVA_SOURCES}
    manifest = {"baseline": str(args.baseline), "baseline_sha256": digest(baseline), "artifact": str(args.output),
                "artifact_sha256": digest(args.output.read_bytes()), "source_sha256": source_hashes, "changed_entries": sorted(changed),
                "added_entries": added, "work": str(work), "gameplay_tested": False,
                "checks": ["five-source Java17 compilation", "actual Java quad/visibility/cache regression", "BOW material matrix and Quick Charge regression",
                           "explicit foundation profiles, native readiness, recipe budgets and existing profile preservation",
                           "single-call injection into baseline catalog avoids importing unrelated unreleased source profiles",
                           "source bridge wiring", "resource geometry and 101-pose string continuity", "SRG reobfuscation", "baseline preservation",
                           "Forge ZIP entry rules before and after packaging", "packaged resource and class readback"]}
    (work / "manifest.json").write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8", newline="\n")
    (ROOT / "_work/material-crossbows-20261019/latest-build.json").write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8", newline="\n")
    print(json.dumps({"artifact": str(args.output), "sha256": manifest["artifact_sha256"], "changed": changed,
                      "added": len(added), "evidence": str(work / "manifest.json")}, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
