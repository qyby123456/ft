import hashlib
import json
from pathlib import Path
import re
import shutil
import subprocess
import time
import zipfile

ROOT = Path(__file__).resolve().parents[1]
TASK = ROOT / "_work/material-crossbows-reload-20261019"
JDK = Path("C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/runtime/java-runtime-gamma-snapshot/bin")
INSTANCE = Path("C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2")
BASELINE = INSTANCE / "mods/qys-bosses.jar"
RENAMER = INSTANCE / "_work/_gradle_home/caches/forge_gradle/maven_downloader/net/minecraftforge/renamer/2.0.4/renamer-2.0.4-all.jar"
MAPPINGS = Path("D:/ft工程文件/食武改件武器掉帧热修_20261019_165330_candidate10/最小热修构建/build/reobfJar/mappings.tsrg")


def digest(data):
    return hashlib.sha256(data).hexdigest()


def run(command, log):
    result = subprocess.run([str(value) for value in command], cwd=ROOT, capture_output=True)
    log.write_bytes(result.stdout + result.stderr)
    if result.returncode:
        raise RuntimeError(log.read_text(encoding="utf-8", errors="replace"))
    print(log.name, "PASS", flush=True)


def main():
    work = TASK / ("build-" + str(time.time_ns()))
    classes = work / "classes"
    classes.mkdir(parents=True)
    empty = work / "empty-sourcepath"
    empty.mkdir()
    baseline = work / "baseline.jar"
    shutil.copy2(BASELINE, baseline)
    manifest = json.loads((ROOT / "_work/material-crossbows-20261019/compile-inputs.json").read_text(encoding="utf-8-sig"))
    own_classes = sorted((ROOT / "_work/material-crossbows-20261019").glob("build-*/classes"))[-1]
    classpath = [str(own_classes), *manifest["pathingEntries"]]
    sources = [ROOT / "src/main/java/net/farmingtales/ftboss/item" / name
               for name in ("FourWeaponUseGuard.java", "FoodWeaponUseBridge.java")]
    run([JDK / "javac.exe", "-encoding", "UTF-8", "-proc:none", "-implicit:none", "--release", "17",
         "-sourcepath", empty, "-classpath", ";".join(classpath), "-d", classes, *sources], work / "compile.log")
    run([JDK / "jar.exe", "--create", "--file", work / "named.jar", "-C", classes, "."], work / "jar.log")
    arguments = [JDK / "java.exe", "-jar", RENAMER, "--input", work / "named.jar", "--output", work / "srg.jar", "--names", MAPPINGS]
    for entry in classpath:
        if Path(entry).is_file() and entry.endswith(".jar"):
            arguments.extend(["--lib", entry])
    arguments.extend(["--lib", baseline])
    run(arguments, work / "reobfuscate.log")
    overlay = work / "overlay"
    with zipfile.ZipFile(work / "srg.jar") as archive:
        entries = [entry for entry in archive.namelist() if entry.endswith(".class")]
        assert set(entries) == {"net/farmingtales/ftboss/item/FourWeaponUseGuard.class", "net/farmingtales/ftboss/item/FoodWeaponUseBridge.class"}, entries
        for entry in entries:
            archive.extract(entry, overlay)
    resources = ROOT / "src/main/resources"
    for material in ("wooden", "stone", "iron", "golden", "diamond", "netherite"):
        item_id = material + "_crossbow"
        models = resources / "assets/ftboss/models/item"
        for source_model in [models / (item_id + ".json"), *(models / (item_id + "_parts")).glob("*.json")]:
            target = overlay / source_model.relative_to(resources)
            target.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(source_model, target)
            entries.append(source_model.relative_to(resources).as_posix())
    artifact = work / "qys-bosses.jar"
    shutil.copy2(baseline, artifact)
    run([JDK / "jar.exe", "--update", "--file", artifact, "-C", overlay, "."], work / "overlay.log")
    disassembly = []
    for archive in (baseline, artifact):
        output = subprocess.check_output([str(JDK / "javap.exe"), "-c", "-p", "-classpath", str(archive),
                                          "net.farmingtales.ftboss.item.FourWeaponUseGuard"], text=True)
        methods = re.split(r"(?=  public static boolean )", output)
        disassembly.append({part.split("(")[0].strip(): re.sub(r"#\d+", "#", part).strip()
                            for part in methods if part.startswith("  public static boolean ")})
    for name in ("blocksUse", "blocksItemUse"):
        key = "public static boolean " + name
        assert disassembly[0][key] == disassembly[1][key], "Concurrent guard changes require manual merge: " + name
    old_native = disassembly[0]["public static boolean blocksNativeSourceAction"]
    assert old_native.endswith("15: iconst_1\n      16: ireturn"), "Unknown native guard baseline; refusing overwrite"
    bridge_methods = []
    for archive in (baseline, artifact):
        output = subprocess.check_output([str(JDK / "javap.exe"), "-c", "-p", "-classpath", str(archive),
                                          "net.farmingtales.ftboss.item.FoodWeaponUseBridge"], text=True)
        methods = re.split(r"(?=  (?:public|private) static )", output)
        bridge_methods.append({part.split(";")[0].strip(): re.sub(r"[ \t]+", " ", re.sub(r"#\d+", "#", part)).strip()
                               for part in methods if part.startswith(("  public static ", "  private static "))})
    assert bridge_methods[0].keys() == bridge_methods[1].keys(), "Bridge methods changed concurrently"
    for name in bridge_methods[0]:
        if " Result invoke(" not in name:
            assert bridge_methods[0][name] == bridge_methods[1][name], "Concurrent bridge change requires manual merge: " + name
        else:
            instructions = []
            for methods in bridge_methods:
                operations = re.findall(r"^ \d+: (.*)$", methods[name], re.MULTILINE)
                instructions.append([re.sub(r"^((?:if\w+|goto) )\d+", r"\1TARGET", operation) for operation in operations])
            assert instructions[0] == instructions[1][:5] + instructions[1][12:], "Existing legacy source-bridge invocation changed"
            added = instructions[1][5:12]
            assert added[0] == "aload_0" and "ItemStack.m_41720_:" in added[1] and "MaterialCrossbowItem" in added[2]
            assert added[3] == "ifeq TARGET" and added[4] == "aload 4" and "Supplier.get:" in added[5] and added[6] == "areturn", added
    with zipfile.ZipFile(baseline) as before, zipfile.ZipFile(artifact) as after:
        assert set(before.namelist()) == set(after.namelist())
        changed = [entry for entry in before.namelist() if before.read(entry) != after.read(entry)]
        assert set(changed) <= set(entries) and entries[0] in changed, changed
    run(["node", ROOT / "tools/verification/check_jar_zip_rules.cjs", artifact], work / "zip-rules.log")
    receipt = {"baseline": str(baseline), "baseline_sha256": digest(baseline.read_bytes()),
               "artifact": str(artifact), "artifact_sha256": digest(artifact.read_bytes()),
               "changed_entries": changed, "source_sha256": {source.name: digest(source.read_bytes()) for source in sources}, "work": str(work)}
    (TASK / "latest-build.json").write_text(json.dumps(receipt, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(receipt, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
