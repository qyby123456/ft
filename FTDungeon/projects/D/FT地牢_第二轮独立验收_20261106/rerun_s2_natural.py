import json
import os
import shutil
import subprocess
import sys
from pathlib import Path

from audit_s8_submission import load


def main():
    root = Path(sys.argv[1]).resolve()
    scratch = root / 'independent-natural-world'
    scratch.mkdir(exist_ok=True)
    project = scratch / 'native-test'
    project.mkdir(exist_ok=True)
    original = root / 'runtime-assembly-r1/native-test'
    shutil.copytree(original / 'src', project / 'src', dirs_exist_ok=True)
    for name in ('build.gradle', 'settings.gradle', 'gradle.properties'):
        source = original / name
        if name == 'gradle.properties' and not source.exists():
            source = root.parent / 'S2-access-20261107-092940-861925/runtime-access-r4/native-test/gradle.properties'
        shutil.copyfile(source, project / name)
    inputs = load(root / 'runtime-assembly-r1/assembly-input.json')
    inputs['candidate'] = str(root / 'assembly/s2-desert-functional-combined.nbt')
    (scratch / 'assembly-input.json').write_text(json.dumps(inputs, ensure_ascii=False, indent=2), encoding='utf-8')
    environment = dict(os.environ)
    environment['JAVA_HOME'] = r'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot'
    environment['PATH'] = str(Path(environment['JAVA_HOME']) / 'bin') + os.pathsep + environment['PATH']
    gradle = r'C:\Users\YOUR_USERNAME\.gradle\wrapper\dists\gradle-8.8-bin\dl7vupf4psengwqhwktix4v1\gradle-8.8\bin\gradle.bat'
    command = [gradle, '--offline', '--no-daemon', '--console=plain', '-Dnet.minecraftforge.gradle.check.certs=false', 'runGameTestServer']
    with (scratch / 'run.log').open('wb') as stream:
        process = subprocess.Popen(command, cwd=project, env=environment, stdout=stream, stderr=subprocess.STDOUT)
        (scratch / 'progress.json').write_text(json.dumps({'stage': 'RUNNING', 'gradle_launcher_pid': process.pid, 'accepted': False}), encoding='utf-8')
        result = process.wait(timeout=1500)
    assert result == 0, str(scratch / 'run.log')
    report = load(scratch / 'assembly-runtime.json')
    summary = {'stage': 'PARENT_INDEPENDENT_FORGE_ASSEMBLY_TRAVEL_REEXECUTION', 'process_exit_code': result, 'report': report, 'accepted': False}
    (scratch / 'independent-result.json').write_text(json.dumps(summary, ensure_ascii=False, indent=2), encoding='utf-8')
    (scratch / 'progress.json').write_text(json.dumps({'stage': 'PROCESS_COMPLETE_REPORT_REVIEW_PENDING', 'accepted': False}), encoding='utf-8')
    print('S2 independent natural assembly run exit0; inspect full report before acceptance', flush=True)


if __name__ == '__main__':
    main()
