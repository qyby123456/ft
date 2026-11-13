import argparse
import datetime
import gzip
import os
import re
from pathlib import Path
import shutil
import struct
import sys

import integrate


def text(value):
    encoded = value.encode('utf-8')
    return struct.pack('>H', len(encoded)) + encoded


def empty_structure():
    root = b'\x0a\x00\x00'
    root += b'\x03' + text('DataVersion') + struct.pack('>i', 3465)
    root += b'\x09' + text('size') + b'\x03' + struct.pack('>iiii', 3, 5, 5, 5)
    root += b'\x09' + text('palette') + b'\x0a' + struct.pack('>i', 1)
    root += b'\x08' + text('Name') + text('minecraft:air') + b'\x00'
    for name in ('blocks', 'entities'):
        root += b'\x09' + text(name) + b'\x0a' + struct.pack('>i', 0)
    return gzip.compress(root + b'\x00', mtime=0)


def main():
    parser = argparse.ArgumentParser(description='Forge environment only, NOT dungeon acceptance')
    parser.add_argument('--gradle', required=True, type=Path)
    parser.add_argument('--java-home', required=True, type=Path)
    parser.add_argument('--timeout', type=int, default=600)
    parser.add_argument('--online', action='store_true')
    args = parser.parse_args()
    run = integrate.HOME / 'run' / ('environment_' + datetime.datetime.now().strftime('%Y%m%d_%H%M%S'))
    project = run / 'project'
    shutil.copytree(integrate.HOME / 'smoke-src', project)
    template = project / 'src/main/resources/data/ftintegration_smoke/structures/empty.nbt'
    template.parent.mkdir(parents=True, exist_ok=True)
    template.write_bytes(empty_structure())
    environment = os.environ.copy()
    environment['JAVA_HOME'] = str(args.java_home.resolve())
    environment['PATH'] = str(args.java_home.resolve() / 'bin') + os.pathsep + environment.get('PATH', '')
    command = [str(args.gradle.resolve()), '--no-daemon', '--console=plain', '--project-cache-dir', str(run / 'gradle-project-cache'), 'runGameTestServer']
    if not args.online:
        command += ['--offline', '-Dnet.minecraftforge.gradle.check.certs=false']
    if os.name == 'nt':
        command = [os.environ.get('COMSPEC', 'cmd.exe'), '/d', '/c'] + command
    result = integrate.execute(command, project, run / 'forge-smoke.log', args.timeout, environment)
    output = (run / 'forge-smoke.log').read_text(encoding='utf-8', errors='replace')
    result['requiredTestsPassed'] = bool(re.search(r'All 1 required tests passed', output))
    if result['status'] == 'PASS' and not result['requiredTestsPassed']:
        result['status'] = 'FAIL'
        result['reason'] = 'Exit zero without the expected one-test GameTest summary'
    result['scope'] = 'Environment-only Forge world block readback; no dungeon generator/templates loaded'
    result['worldAcceptance'] = 'NOT_RUN'
    result['templateSha256'] = integrate.digest(template)
    result['logSha256'] = integrate.digest(run / 'forge-smoke.log')
    integrate.save(integrate.HOME / 'environment-smoke.json', result)
    print(result)
    return 0 if result['status'] == 'PASS' else 1


if __name__ == '__main__':
    sys.exit(main())
