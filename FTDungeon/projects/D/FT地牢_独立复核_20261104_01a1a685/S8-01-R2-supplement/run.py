import hashlib
import json
import os
from pathlib import Path
import subprocess

ROOT = Path(__file__).resolve().parent
BUNDLE = Path('D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/S8-rebuild-single-20261104-01')
MAPPED = Path('D:/ft工程文件/锈铜宝库地牢_20261101/qa/vanilla-1.20.1-mapped.jar')
SUPPORT = Path('D:/ft工程文件/霜雪花园地牢_20261101/build/classpath/runData_minecraftClasspath.txt')
JAVA = Path('C:/Program Files/Common Files/Oracle/Java/javapath')


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    manifest = json.loads((BUNDLE / 'checksums.json').read_text(encoding='utf-8'))
    before = {name: digest(BUNDLE / name) for name in manifest}
    assert before == manifest
    dependencies = [line for line in SUPPORT.read_text(encoding='utf-8').splitlines() if line.strip() and 'minecraft_user_repo' not in line]
    classpath = os.pathsep.join([str(MAPPED), *dependencies])
    (ROOT / 'classes').mkdir(exist_ok=True)
    (ROOT / 'classpath.txt').write_text(classpath, encoding='utf-8')
    commands = [[str(JAVA / 'javac.exe'), '-encoding', 'UTF-8', '-classpath', classpath, '-d', str(ROOT / 'classes'), str(ROOT / 'CollisionRoutes.java')], [str(JAVA / 'java.exe'), '-Xmx2G', '-Djava.awt.headless=true', '-classpath', str(ROOT / 'classes') + os.pathsep + classpath, 'CollisionRoutes', str(BUNDLE / 'incoming/S8R_IMG01/room.nbt'), str(BUNDLE / 'incoming/S8R_IMG01/room.json'), str(ROOT / 'collision-routes.json')]]
    records = []
    for label, command in zip(('compile', 'run'), commands):
        result = subprocess.run(command, cwd=ROOT, capture_output=True, text=True, encoding='utf-8', errors='replace', timeout=240, creationflags=subprocess.CREATE_NO_WINDOW)
        (ROOT / (label + '.log')).write_text(result.stdout + result.stderr, encoding='utf-8')
        records.append({'label': label, 'argv': command, 'cwd': str(ROOT), 'exit_code': result.returncode})
        (ROOT / 'commands.json').write_text(json.dumps(records, ensure_ascii=False, indent=2), encoding='utf-8')
        if result.returncode:
            print(result.stdout + result.stderr)
            raise SystemExit(result.returncode)
    after = {name: digest(BUNDLE / name) for name in manifest}
    assert after == before
    evidence = {'bundle_manifest_entries': len(manifest), 'before_after_bundle_hashes_identical': True, 'source_nbt_sha256': digest(BUNDLE / 'incoming/S8R_IMG01/room.nbt'), 'source_metadata_sha256': digest(BUNDLE / 'incoming/S8R_IMG01/room.json'), 'java_source_sha256': digest(ROOT / 'CollisionRoutes.java'), 'mapped_jar_sha256': digest(MAPPED), 'original_classpath_source_sha256': digest(SUPPORT), 'resolved_classpath': str(ROOT / 'classpath.txt'), 'exact_commands': str(ROOT / 'commands.json'), 'actual_world_placement': 'NOT_RUN', '400tick_stability': 'NOT_RUN', 'no_official_save_mod_config_deployment_writes': True}
    (ROOT / 'provenance.json').write_text(json.dumps(evidence, ensure_ascii=False, indent=2), encoding='utf-8')
    report = json.loads((ROOT / 'collision-routes.json').read_text(encoding='utf-8'))
    print(report['status'])
    for turn in report['turns']:
        print(turn['degrees_clockwise'], [(route['label'], route['reachable_without_jump'], len(route['full_path_centers'])) for route in turn['routes']])


if __name__ == '__main__':
    main()
