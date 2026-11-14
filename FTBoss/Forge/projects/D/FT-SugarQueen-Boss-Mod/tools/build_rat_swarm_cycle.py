import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys


def main():
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")
    project = Path(__file__).resolve().parent.parent
    task = project / "_work/rat-swarm-cycle-20261012"
    java = Path(os.environ["APPDATA"]) / ".minecraft/runtime/java-runtime-gamma-snapshot/bin"
    minecraft = Path(os.environ["APPDATA"]) / ".minecraft"
    cache = Path.home() / ".gradle/caches"
    official = cache / "forge_gradle/minecraft_user_repo/net/minecraftforge/forge/1.20.1-47.4.10_mapped_official_1.20.1/forge-1.20.1-47.4.10_mapped_official_1.20.1.jar"
    forge = cache / "forge_gradle/minecraft_user_repo/net/minecraftforge/forge/1.20.1-47.4.10/forge-1.20.1-47.4.10-srg.jar"
    mapping = cache / "forge_gradle/minecraft_user_repo/de/oceanlabs/mcp/mcp_config/1.20.1-20230612.114412/srg_to_official_1.20.1.tsrg"
    renamer = next((cache / "modules-2/files-2.1/net.neoforged/AutoRenamingTool/2.0.17").rglob("*-all.jar"))
    asm = [minecraft / f"libraries/org/ow2/asm/{name}/9.7.1/{name}-9.7.1.jar"
           for name in ("asm", "asm-tree", "asm-util", "asm-analysis", "asm-commons")]

    def run(*arguments):
        result = subprocess.run([str(argument) for argument in arguments], capture_output=True)
        with (task / "verification.log").open("ab") as transcript:
            transcript.write(result.stdout + result.stderr)
        if result.stdout:
            print(result.stdout.decode("utf-8", errors="replace"), end="", flush=True)
        if result.returncode:
            print(result.stderr.decode("utf-8", errors="replace"), end="", flush=True)
            raise subprocess.CalledProcessError(result.returncode, arguments)

    def args(name, options, encoding="utf-8"):
        target = task / name
        target.write_text("\n".join('"' + str(option).replace("\\", "/") + '"' for option in options) + "\n", encoding=encoding)
        return "@" + str(target)

    if sys.argv[1] == "compile":
        compile_cp = (project / "_work/food-weapon-build-20261012/build-project/build/verification/dandan-classpath.txt").read_text(encoding="utf-8").strip()
        sources = [project / ("src/main/java/net/farmingtales/ftboss/" + relative) for relative in (
            "boss/ai/RatSwarmRules.java", "boss/ai/RatSwarmEncounter.java", "entity/RatBossEntity.java",
            "boss/BossHandler.java", "boss/ai/FinalBossDamageGuard.java", "boss/ai/BossEmpower.java",
            "boss/BossAdvancements.java", "item/RatSwarmBaitItem.java")]
        run(java / "javac.exe", "-J-Dfile.encoding=UTF-8", args("compile.args", ["--release", "17", "-encoding", "UTF-8",
            "-proc:none", "-sourcepath", task / "empty-sourcepath", "-cp", str(task / "baseline-mapped.jar") + os.pathsep + compile_cp,
            "-d", task / "classes", *sources, project / "tools/verification/RatSwarmRulesCheck.java"]))
        run(java / "java.exe", "-cp", task / "classes", "RatSwarmRulesCheck")
        run(java / "jar.exe", "--create", "--file", task / "donor-official.jar", "-C", task / "classes", "net")
        run(java / "java.exe", "-Xmx2G", "-jar", renamer, "--input", task / "donor-official.jar",
            "--output", task / "donor-srg.jar", "--map", mapping, "--reverse", "--lib", official,
            "--lib", task / "baseline-mapped.jar", "--threads", "2")
        tool_cp = os.pathsep.join(map(str, [task / "tools", *asm]))
        (task / "tools").mkdir(exist_ok=True)
        run(java / "javac.exe", "-J-Dfile.encoding=UTF-8", args("tools-compile.args", ["--release", "17", "-encoding", "UTF-8",
            "-cp", tool_cp, "-d", task / "tools", project / "tools/runtime_patches/RatSwarmCyclePatch.java",
            project / "tools/verification/RatSwarmCycleCheck.java", project / "tools/verification/ZipStreamCheck.java"]))
        (task / "source-hashes.json").write_text(json.dumps({str(source.relative_to(project)): hashlib.sha256(source.read_bytes()).hexdigest()
            for source in sources}, indent=2), encoding="utf-8")
        return

    baseline = Path(sys.argv[2]).resolve()
    output = Path(sys.argv[3]).resolve()
    output.relative_to(task)
    output.mkdir(parents=True, exist_ok=False)
    payload = output / "payload"
    tool_cp = os.pathsep.join(map(str, [task / "tools", *asm]))
    run(java / "java.exe", "-Xverify:all", "-cp", tool_cp, "RatSwarmCyclePatch", baseline,
        task / "donor-srg.jar", payload, output / "method-delta.txt")
    resource = "data/ftboss/tags/items/rat_swarm_healing_food.json"
    target = payload / resource
    target.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(project / ("src/main/resources/" + resource), target)
    candidate = output / "qys-bosses.jar"
    shutil.copyfile(baseline, candidate)
    run(java / "jar.exe", "--update", "--file", candidate, "-C", payload, ".")
    run(java / "java.exe", "-Xverify:all", "-cp", tool_cp, "RatSwarmCycleCheck", baseline, candidate)
    runtime = [candidate, task / "tools", forge, *asm]
    runtime += [entry for entry in (minecraft / "libraries").rglob("*.jar")
                if not any(marker in entry.name for marker in ("natives", "sources", "javadoc"))]
    runtime += list((minecraft / "versions/FarmingTales_ForgeⅡ v1.5.2/mods").glob("*.jar"))
    for index, entry in enumerate(runtime):
        try:
            str(entry).encode("gbk")
        except UnicodeEncodeError:
            copied = task / "runtime-libraries" / (hashlib.sha256(entry.read_bytes()).hexdigest() + ".jar")
            copied.parent.mkdir(exist_ok=True)
            if not copied.exists():
                shutil.copyfile(entry, copied)
            runtime[index] = copied
    run(java / "java.exe", "-Dfile.encoding=UTF-8", args("runtime-link.args", ["-Xverify:all", "-cp",
        os.pathsep.join(map(str, runtime)), "RatSwarmCycleCheck"], encoding="gbk"))
    run(java / "java.exe", "-Xverify:all", "-cp", tool_cp, "ZipStreamCheck", candidate)
    run("node", project / "tools/verification/check_jar_zip_rules.cjs", candidate)
    (output / "build-receipt.json").write_text(json.dumps({"baseline": str(baseline),
        "baselineSha256": hashlib.sha256(baseline.read_bytes()).hexdigest(), "candidate": str(candidate),
        "candidateSha256": hashlib.sha256(candidate.read_bytes()).hexdigest(), "checks": "passed"}, indent=2), encoding="utf-8")


if __name__ == "__main__":
    main()
