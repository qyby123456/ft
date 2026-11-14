import os
import json
import subprocess
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
OUTPUT = ROOT / "_work/statue-ui-restyle-20261024/themes/compile"
SOURCES = ("StatueUiTheme", "StatueThemeSource", "StatueThemeRenderer", "StatueThemeButton")


def quoted(value):
    return '"' + str(value).replace("\\", "/") + '"'


def windows_library(library):
    rules = library.get("rules", [])
    allowed = not rules
    for rule in rules:
        operating_system = rule.get("os", {})
        if operating_system.get("name", "windows") == "windows" and operating_system.get("arch", "amd64") == "amd64":
            allowed = rule["action"] == "allow"
    return allowed


def main():
    minecraft = Path(os.environ["APPDATA"]) / ".minecraft"
    jdk = minecraft / "runtime/java-runtime-gamma-snapshot/bin"
    forge = Path.home() / ".gradle/caches/forge_gradle/minecraft_user_repo/net/minecraftforge/forge/1.20.1-47.4.10_mapped_official_1.20.1/forge-1.20.1-47.4.10_mapped_official_1.20.1.jar"
    baseline = ROOT / "_work/statue-ui-restyle-20261024/baseline.jar"
    version_file = Path.home() / ".gradle/caches/forge_gradle/minecraft_repo/versions/1.20.1/version.json"
    version = json.loads(version_file.read_text(encoding="utf-8"))
    libraries = [minecraft / "libraries" / library["downloads"]["artifact"]["path"] for library in version["libraries"]
                 if "artifact" in library.get("downloads", {}) and windows_library(library)]
    missing = [str(library) for library in libraries if not library.is_file()]
    if missing:
        raise FileNotFoundError("Minecraft 1.20.1 dependencies missing: " + ", ".join(missing))
    merge_api = minecraft / "libraries/net/minecraftforge/mergetool/1.1.5/mergetool-1.1.5-api.jar"
    eventbus = minecraft / "libraries/net/minecraftforge/eventbus/6.0.5/eventbus-6.0.5.jar"
    dependencies = [forge, baseline, merge_api, eventbus, *libraries]
    classpath = os.pathsep.join(map(str, dependencies))
    OUTPUT.mkdir(parents=True, exist_ok=True)
    (OUTPUT / "empty-sourcepath").mkdir(exist_ok=True)
    arguments = ["--release", "17", "-encoding", "UTF-8", "-proc:none", "-sourcepath",
                 OUTPUT / "empty-sourcepath", "-classpath", classpath, "-d", OUTPUT / "classes"]
    arguments.extend(ROOT / "src/main/java/net/farmingtales/ftboss/client" / (name + ".java") for name in SOURCES)
    (OUTPUT / "javac.args").write_text("\n".join(quoted(argument) for argument in arguments) + "\n", encoding="utf-8")
    result = subprocess.run([str(jdk / "javac.exe"), "-J-Dfile.encoding=UTF-8", "@" + str(OUTPUT / "javac.args")], cwd=ROOT, capture_output=True)
    (OUTPUT / "javac.log").write_bytes(result.stdout + result.stderr)
    print((result.stdout + result.stderr).decode("utf-8", errors="replace"))
    if result.returncode:
        raise SystemExit(result.returncode)
    subprocess.run([str(jdk / "jar.exe"), "--create", "--file", str(OUTPUT / "themes-official.jar"), "-C", str(OUTPUT / "classes"), "."], check=True)
    print("PASS: four theme classes compiled against Forge 1.20.1 and frozen baseline CandyStatueBlock")
    runtime_classpath = str(OUTPUT / "classes") + os.pathsep + classpath
    (OUTPUT / "runtime-classpath.txt").write_text(runtime_classpath, encoding="utf-8")
    check_source = ROOT / "tools/statue_ui/ThemeContractCheck.java"
    check_arguments = ["--release", "17", "-encoding", "UTF-8", "-proc:none", "-classpath", runtime_classpath,
                       "-d", OUTPUT / "checks", check_source]
    (OUTPUT / "check-javac.args").write_text("\n".join(quoted(argument) for argument in check_arguments) + "\n", encoding="utf-8")
    subprocess.run([str(jdk / "javac.exe"), "-J-Dfile.encoding=UTF-8", "@" + str(OUTPUT / "check-javac.args")], check=True)
    manifest_line = "Class-Path: " + " ".join(dependency.as_uri() for dependency in dependencies)
    manifest = "Manifest-Version: 1.0\r\n" + manifest_line[:70] + "\r\n"
    manifest += "".join(" " + manifest_line[offset:offset + 69] + "\r\n" for offset in range(70, len(manifest_line), 69)) + "\r\n"
    with zipfile.ZipFile(OUTPUT / "dependency-classpath.jar", "w") as archive:
        archive.writestr("META-INF/MANIFEST.MF", manifest)
    run_arguments = ["-Dfile.encoding=UTF-8", "-Djava.awt.headless=true", "-Xverify:all", "-cp",
                     os.pathsep.join(("checks", "classes", "dependency-classpath.jar")), "net.farmingtales.ftboss.client.ThemeContractCheck"]
    result = subprocess.run([str(jdk / "java.exe"), *run_arguments], cwd=OUTPUT, capture_output=True)
    (OUTPUT / "contract-check.log").write_bytes(result.stdout + result.stderr)
    print((result.stdout + result.stderr).decode("utf-8", errors="replace"))
    if result.returncode:
        raise SystemExit(result.returncode)


if __name__ == "__main__":
    main()
