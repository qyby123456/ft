import hashlib
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
    scratch.mkdir()
    project = scratch / 'native-test'
    project.mkdir()
    original = root / 'worldqa-natural7/native-test'
    shutil.copytree(original / 'src', project / 'src')
    for name in ('build.gradle', 'settings.gradle', 'gradle.properties'):
        shutil.copyfile(original / name, project / name)
    datapacks = scratch / 'server/world/datapacks'
    datapacks.mkdir(parents=True)
    archive = root / 'access-package/s1_access_1_20_1.zip'
    shutil.copyfile(archive, datapacks / archive.name)
    assert archive.read_bytes() == (datapacks / archive.name).read_bytes()
    environment = dict(os.environ)
    environment['JAVA_HOME'] = r'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot'
    environment['PATH'] = str(Path(environment['JAVA_HOME']) / 'bin') + os.pathsep + environment['PATH']
    gradle = r'C:\Users\YOUR_USERNAME\.gradle\wrapper\dists\gradle-8.8-bin\dl7vupf4psengwqhwktix4v1\gradle-8.8\bin\gradle.bat'
    command = [gradle, '--offline', '--no-daemon', '--console=plain', '-Dnet.minecraftforge.gradle.check.certs=false', 'runGameTestServer']
    with (scratch / 'run.log').open('wb') as stream:
        process = subprocess.Popen(command, cwd=project, env=environment, stdout=stream, stderr=subprocess.STDOUT)
        (scratch / 'progress.json').write_text(json.dumps({'stage': 'RUNNING', 'gradle_launcher_pid': process.pid, 'accepted': False}), encoding='utf-8')
        result = process.wait(timeout=1200)
    assert result == 0, str(scratch / 'run.log')
    report = load(scratch / 'natural-travel-report.json')
    assert report['status'] == 'PASS' and len(report['completed']) == 23
    assert report['forced_chunks_at_end'] == 0 and not report['watch_chunk_loaded_before_entry']
    assert report['max_up_step'] == 0.6 and report['candidate_sha256'] == load(root / 'ready-for-review.json')['merged']['sha256']
    count = 0
    for name in ('submission-snapshot.json', 'supplemental-snapshot.json', 'delivery-snapshot.json'):
        for item in load(root / name)['files']:
            assert hashlib.sha256(Path(item['snapshot']).read_bytes()).hexdigest() == item['sha256']
            count += 1
    summary = {'stage': 'PARENT_INDEPENDENT_FORGE_NATURAL_TRAVEL_REEXECUTION', 'status': 'PASS_SCOPED', 'natural_waypoints': 23, 'natural_server_ticks': report['elapsed_server_ticks'], 'trace_frames': len(report['trace']), 'exact_zip_sha256': hashlib.sha256(archive.read_bytes()).hexdigest(), 'candidate_sha256': report['candidate_sha256'], 'frozen_record_hashes_rechecked': count, 'accepted': False, 'limitations': ['Server FakePlayer, no real network client or damage certification', 'Natural route covers declared library ladder and watch stair roundtrip; all other target connectivity separately checked']}
    (scratch / 'independent-result.json').write_text(json.dumps(summary, ensure_ascii=False, indent=2), encoding='utf-8')
    (scratch / 'progress.json').write_text(json.dumps(summary, ensure_ascii=False, indent=2), encoding='utf-8')
    print(json.dumps(summary, ensure_ascii=False), flush=True)


if __name__ == '__main__':
    main()
