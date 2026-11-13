import argparse
import os
import subprocess
import zipfile
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]


def run(arguments, logfile):
    result = subprocess.run([str(argument) for argument in arguments], cwd=ROOT, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    (ROOT / 'qa' / logfile).write_bytes(result.stdout)
    if result.returncode:
        raise RuntimeError(result.stdout.decode('utf-8', errors='replace')[-6000:])


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--classpath-file', type=Path, required=True)
    parser.add_argument('--java', default='java')
    parser.add_argument('--javac', default='javac')
    arguments = parser.parse_args()
    cache = Path(os.environ['USERPROFILE']) / '.gradle/caches/forge_gradle'
    minecraft = cache / 'minecraft_repo/versions/1.20.1'
    remapper = cache / 'maven_downloader/net/minecraftforge/ForgeAutoRenamingTool/1.0.2/ForgeAutoRenamingTool-1.0.2-all.jar'
    mapped = ROOT / 'qa/vanilla-1.20.1-mapped.jar'
    if not mapped.exists():
        run([arguments.java, '-jar', remapper, '--input', minecraft / 'client.jar', '--output', mapped,
             '--names', minecraft / 'client_mappings.txt', '--reverse', '--threads', '2'], 'remap.log')
    unsigned = ROOT / 'qa/vanilla-1.20.1-local-verification.jar'
    with zipfile.ZipFile(mapped) as source, zipfile.ZipFile(unsigned, 'w', zipfile.ZIP_DEFLATED) as target:
        for entry in source.infolist():
            name = entry.filename.upper()
            if name == 'META-INF/MANIFEST.MF' or (name.startswith('META-INF/') and name.endswith(('.SF', '.RSA', '.DSA', '.EC'))):
                continue
            target.writestr(entry, source.read(entry.filename))
    support = [path for path in arguments.classpath_file.read_text(encoding='utf-8-sig').splitlines() if 'minecraft_user_repo' not in path]
    classpath = os.pathsep.join([str(unsigned), *support])
    classes = ROOT / 'qa/classes'
    classes.mkdir(exist_ok=True)
    run([arguments.javac, '-encoding', 'UTF-8', '-classpath', classpath, '-d', classes, ROOT / 'tools/VerifyVault.java'], 'javac.log')
    run([arguments.java, '-Djava.awt.headless=true', '-classpath', str(classes) + os.pathsep + classpath, 'VerifyVault', ROOT], 'java.log')
    print((ROOT / 'qa/native-verification.json').read_text(encoding='utf-8'))


if __name__ == '__main__':
    main()
