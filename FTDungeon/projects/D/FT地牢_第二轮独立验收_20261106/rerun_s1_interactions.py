import json
import os
import shutil
import subprocess
import sys
from pathlib import Path

from audit_s8_submission import load


def main():
    root = Path(sys.argv[1]).resolve()
    scratch = root / 'independent-interaction-world'
    scratch.mkdir()
    project = scratch / 'native-test'
    project.mkdir()
    original = root / 'worldqa-interactions/native-test'
    shutil.copytree(original / 'src', project / 'src')
    for name in ('build.gradle', 'settings.gradle', 'gradle.properties'):
        shutil.copyfile(original / name, project / name)
    records = {}
    for name in ('submission-snapshot.json', 'supplemental-snapshot.json', 'delivery-snapshot.json'):
        records.update({item['source']: item for item in load(root / name)['files']})
    inputs = load(root / 'worldqa-interactions/inputs.json')
    for specification in inputs:
        for name in ('candidate', 'design'):
            specification[name] = records[specification[name]]['snapshot']
    (scratch / 'inputs.json').write_text(json.dumps(inputs, ensure_ascii=False, indent=2), encoding='utf-8')
    environment = dict(os.environ)
    environment['JAVA_HOME'] = r'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot'
    environment['PATH'] = str(Path(environment['JAVA_HOME']) / 'bin') + os.pathsep + environment['PATH']
    gradle = r'C:\Users\YOUR_USERNAME\.gradle\wrapper\dists\gradle-8.8-bin\dl7vupf4psengwqhwktix4v1\gradle-8.8\bin\gradle.bat'
    command = [gradle, '--offline', '--no-daemon', '--console=plain', '-Dnet.minecraftforge.gradle.check.certs=false', 'runGameTestServer']
    with (scratch / 'run.log').open('wb') as stream:
        process = subprocess.Popen(command, cwd=project, env=environment, stdout=stream, stderr=subprocess.STDOUT)
        (scratch / 'progress.json').write_text(json.dumps({'stage': 'RUNNING', 'gradle_launcher_pid': process.pid, 'accepted': False}), encoding='utf-8')
        result = process.wait(timeout=900)
    assert result == 0, str(scratch / 'run.log')
    report = load(scratch / 'interaction-report.json')
    assert report['failures'] == 0 and len(report['candidates']) == 11
    targets = [item for candidate in report['candidates'] for item in candidate['interactions']]
    assert len(targets) == 76 and all(item['passed'] for item in targets)
    summary = {'stage': 'PARENT_INDEPENDENT_FORGE_INTERACTION_REEXECUTION', 'status': 'PASS_SCOPED', 'candidates': 11, 'actual_native_ray_and_use_interactions': 76, 'max_hit_distance': max(item['distance'] for item in targets), 'accepted': False, 'scope': report['scope']}
    (scratch / 'independent-result.json').write_text(json.dumps(summary, ensure_ascii=False, indent=2), encoding='utf-8')
    (scratch / 'progress.json').write_text(json.dumps(summary, ensure_ascii=False, indent=2), encoding='utf-8')
    print(json.dumps(summary, ensure_ascii=False), flush=True)


if __name__ == '__main__':
    main()
