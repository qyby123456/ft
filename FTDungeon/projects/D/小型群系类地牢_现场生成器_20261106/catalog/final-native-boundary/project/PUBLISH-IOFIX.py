import gzip
import hashlib
import io
import json
from pathlib import Path
import struct
import sys
import zipfile

from test_command_io import extract

ROOT = Path(__file__).resolve().parent
PROJECT = ROOT.parents[1]
sys.path.insert(0, str(PROJECT / 'catalog'))
from nbtio import plain, read_payload, read_string


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def read(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))


def write(name, value):
    (ROOT / name).write_text(json.dumps(value, ensure_ascii=False, indent=2), encoding='utf-8')


def main():
    frozen = []
    for relative, count in [('runtime', 41), ('runtime/next', 57), ('runtime/next-fluid', 70), ('runtime/next-unified', 79), ('runtime/next-commit', 95)]:
        folder = PROJECT / relative
        ready = read(folder / 'ready.json')
        assert len(ready['files']) == count
        for name, expected in ready['files'].items():
            path = Path(name)
            if not path.is_absolute():
                path = folder / path
            assert digest(path) == expected, str(path)
        frozen.append({'ready': str(folder / 'ready.json'), 'sha256': digest(folder / 'ready.json'), 'unchangedFiles': count})
    baseline = PROJECT / 'runtime/next-commit'
    source_relative = 'src/main/java/cn/ft/biomedungeon/runtime/UnifiedEngine.java'
    source_path = ROOT / source_relative
    source = source_path.read_text(encoding='utf-8')
    previous = (baseline / source_relative).read_text(encoding='utf-8')
    restored = source
    for signature in ['String pause(String id)', 'String commit(String id,']:
        restored = restored.replace(extract(source, signature), extract(previous, signature), 1)
    assert restored == previous, 'Unexpected engine change outside pause/commit'
    for path in (ROOT / 'src/main/java').rglob('*.java'):
        if path != source_path:
            assert digest(path) == digest(baseline / path.relative_to(ROOT)), str(path)
    for path in (ROOT / 'core-snapshot').rglob('*'):
        if path.is_file():
            assert digest(path) == digest(baseline / path.relative_to(ROOT))
    lock = read(ROOT / 'input-lock.json')
    assert digest(ROOT / 'input-lock.json') == digest(baseline / 'input-lock.json')
    for name, expected in lock['files'].items():
        assert digest(ROOT / 'src/main/resources/data/ftbd_candidate' / name) == expected
    for relative, field in [('core/envelope-usability-review', 'coreReadySha256'), ('catalog/s8-07-doorway-review-v5', 'catalogReadySha256')]:
        assert digest(PROJECT / relative / 'ready.json') == lock[field]
    probe = read(ROOT / 'io-probe/extracted-methods.json')
    assert probe['sourceSha256'] == digest(source_path)
    assert 'ACTUAL_EXTRACTED_COMMAND_IO_PASS assertions=17' in (ROOT / 'io-probe/result.log').read_text(encoding='utf-8-sig')
    assert 'BUILD SUCCESSFUL' in (ROOT / 'build-0.5.1.log').read_text(encoding='utf-8-sig')
    jar = ROOT / 'build/libs/ft-biome-dungeon-generator-forge-1.20.1-0.5.1-commit-iofix-candidate.jar'
    artifact_hash = digest(jar)
    with zipfile.ZipFile(jar) as archive:
        names = archive.namelist()
        assert len(names) == len(set(names)) and archive.testzip() is None
        assert not any('GameTests.class' in name or name.startswith('data/ftbd_unified/') for name in names)
        for required in ['UnifiedEngine', 'CommitEvidence', 'TransitionVerifier']:
            assert 'cn/ft/biomedungeon/runtime/' + required + '.class' in names
        manifest = json.loads(archive.read('data/ftbd_candidate/nine-manifest.json'))
        assert len(manifest['templates']) == 9 and all(item['accepted'] is False for item in manifest['templates'])
        for name, expected in lock['files'].items():
            assert hashlib.sha256(archive.read('data/ftbd_candidate/' + name)).hexdigest() == expected
    installed = read(ROOT / 'installed-v5-q3-forcekill-result.json')
    assert installed['status'] == 'PASS_INSTALLED_V5_DURABLE_CHECKPOINT_FORCEKILL_RESUME_NOT_POWERLOSS'
    assert installed['jarSha256'] == artifact_hash and installed['worldSeed'] == 267611119455246637 and installed['ordinal'] == 3
    stages = installed['processes']
    assert len(stages) == 2 and stages[0]['pid'] != stages[1]['pid']
    assert stages[0]['exitCode'] != 0 and stages[1]['exitCode'] == 0 and stages[1]['incompleteCommitRefused'] is True
    logs = []
    for stage in stages:
        path = Path(stage['log'])
        assert digest(path) == stage['logSha256']
        logs.append(path)
    resumed = logs[1].read_text(encoding='utf-8', errors='replace')
    assert 'V5_DERIVED_ENTRANCE_NATIVE_PASS' in resumed and 'phase=COMMITTED' not in resumed
    world = Path(installed['run']) / 'world'
    journals = list(world.glob('ft_biome_dungeon_candidate_v5/minecraft/overworld/*.nbt'))
    assert len(journals) == 1
    raw = journals[0].read_bytes()
    magic, length = struct.unpack('>II', raw[:8])
    assert magic == 0x46544435 and length + 72 == len(raw)
    assert hashlib.sha256(raw[72:]).hexdigest() == raw[8:72].decode('ascii')
    stream = io.BytesIO(gzip.decompress(raw[72:]))
    kind = stream.read(1)[0]
    read_string(stream)
    data = plain(read_payload(stream, kind))
    assert data['schema'] == 5 and data['stage'] == 'PAYLOAD_VERIFIED_ROUTE_INTERACTION_ACCEPTANCE_PENDING'
    assert data['phase'] != 'COMMITTED' and not data['receiptHash']
    assert data['cursor'] == data['ownedThrough'] == data['completedThrough']
    assert data['observedTicks'] >= 2400 and data['restartValidations'] >= 1
    pause = read(ROOT / 'installed-pause-io-result.json')
    assert pause['status'] == 'PASS_ACTUAL_INSTALLED_PAUSE_IO_FAILURE_INACTIVE_ZERO_TICKETS_DISABLED'
    assert pause['jarSha256'] == artifact_hash and pause['exitCode'] == 0
    assert all(pause[field] is True for field in ['previousJournalUnchanged', 'zeroCompletedWrites', 'delayedStatusStillInactive'])
    pause_log = Path(pause['log'])
    assert digest(pause_log) == pause['logSha256']
    status = 'READY_0_5_1_COMMAND_IO_CLEANUP_INSTALLED_FORCEKILL_AND_REFUSAL_INDEPENDENT_ACCEPTANCE_PENDING'
    summary = {'status': status, 'jarSha256': artifact_hash, 'planningVersion': 'runtime-v5-explicit-commit1-envelope-v3-derived07',
               'statePolicy': 'command-journal-io-cleanup-v5.1', 'frozen': frozen,
               'minimalDiff': 'Only pause/commit method bodies changed in runtime Java; all other Java/core/input bytes unchanged',
               'exactMethodFaultAssertions': 17, 'commitIoFaultScope': 'Exact extracted actual method with persistent IOException; not manufactured Forge acceptance',
               'installedPauseIo': pause, 'installedForcekill': installed,
               'journal': {'phase': data['phase'], 'stage': data['stage'], 'completedThrough': data['completedThrough'], 'observedTicks': data['observedTicks'], 'restartValidations': data['restartValidations']},
               'incompleteEvidenceRefused': True, 'actualJobCOMMITTED': False, 'acceptedFlagsChanged': False,
               'independentNewArtifactFullAcceptance': 'PENDING', 'powerLoss': 'NOT_RUN', 'completeGeneratorReady': False, 'productionPromotion': False}
    write('result.json', summary)
    write('jar-audit.json', {'sha256': artifact_hash, 'bytes': jar.stat().st_size, 'templates': len(manifest['templates']), 'accepted': False, 'testFixturesBundled': False})
    write('world-evidence.json', {'files': [{'path': str(path.relative_to(ROOT)), 'sha256': digest(path), 'bytes': path.stat().st_size}
          for folder in [world, Path(pause['world'])] for path in sorted(folder.rglob('*'))
          if path.is_file() and (path.suffix in {'.mca', '.nbt'} or path.name == 'level.dat')]})
    write('progress.json', {'status': status, 'componentReady': True, 'completeGeneratorReady': False, 'productionPromotion': False,
                           'pending': ['Independent exact new-artifact acceptance', 'Real evidence-backed explicit commit and postcommit restart']})
    selected = [path for folder in ['src/main', 'core-snapshot', 'gradle', 'io-probe'] for path in (ROOT / folder).rglob('*') if path.is_file()]
    selected.extend(path for path in ROOT.iterdir() if path.is_file() and path.name != 'ready.json')
    selected.extend([jar, *logs, *journals, pause_log])
    files = {str(path.relative_to(ROOT)): digest(path) for path in sorted(set(selected))}
    write('ready.json', {'component': 'runtime-next-commit-iofix', 'ready': True, 'status': status,
          'artifact': str(jar.relative_to(ROOT)), 'artifactSha256': artifact_hash, 'artifactVersion': '0.5.1-commit-iofix-candidate',
          'operationVersion': summary['planningVersion'], 'statePolicy': summary['statePolicy'],
          'componentOnlyNotCompleteAcceptance': True, 'independentAcceptance': 'PENDING', 'actualJobCOMMITTED': False,
          'completeGeneratorReady': False, 'productionPromotion': False, 'result': 'result.json', 'files': files})
    print('IOFIX_READY files=' + str(len(files)) + ' jarSha256=' + artifact_hash + ' frozen=41,57,70,79,95')


if __name__ == '__main__':
    main()
