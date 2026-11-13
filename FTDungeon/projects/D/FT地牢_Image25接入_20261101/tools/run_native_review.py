import os
import argparse
import subprocess
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
MAPPED = Path("D:/ft工程文件/锈铜宝库地牢_20261101/qa/vanilla-1.20.1-mapped.jar")
SUPPORT = Path("D:/ft工程文件/霜雪花园地牢_20261101/build/classpath/runData_minecraftClasspath.txt")
JAVA_BIN = Path("C:/Program Files/Common Files/Oracle/Java/javapath")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=ROOT)
    arguments = parser.parse_args()
    dependencies = [line for line in SUPPORT.read_text(encoding="utf-8").splitlines() if line.strip() and "minecraft_user_repo" not in line]
    classpath = os.pathsep.join([str(MAPPED), *dependencies])
    classes = arguments.root / "qa/native-classes"
    classes.mkdir(parents=True, exist_ok=True)
    compilation = subprocess.run([str(JAVA_BIN / "javac.exe"), "-encoding", "UTF-8", "-classpath", classpath, "-d", str(classes), str(ROOT / "tools/VerifyDeliveries.java")], cwd=ROOT, capture_output=True, text=True, encoding="utf-8", errors="replace", timeout=90, creationflags=subprocess.CREATE_NO_WINDOW)
    (arguments.root / "qa/native-compile.log").write_text(compilation.stdout + compilation.stderr, encoding="utf-8")
    if compilation.returncode:
        print(compilation.stdout + compilation.stderr)
        raise SystemExit(compilation.returncode)
    execution = subprocess.run([str(JAVA_BIN / "java.exe"), "-Djava.awt.headless=true", "-classpath", str(classes) + os.pathsep + classpath, "VerifyDeliveries", str(arguments.root)], cwd=ROOT, capture_output=True, text=True, encoding="utf-8", errors="replace", timeout=120, creationflags=subprocess.CREATE_NO_WINDOW)
    (arguments.root / "qa/native-run.log").write_text(execution.stdout + execution.stderr, encoding="utf-8")
    print(execution.stdout[-8000:])
    if execution.returncode:
        print(execution.stderr[-2500:])
        raise SystemExit(execution.returncode)


if __name__ == "__main__":
    main()
