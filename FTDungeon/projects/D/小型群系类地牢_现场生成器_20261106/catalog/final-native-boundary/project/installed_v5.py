import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import time
import zipfile

ROOT = Path(__file__).resolve().parent
PROJECT = ROOT.parents[1]
JAVA = Path(os.environ['APPDATA']) / '.minecraft/runtime/java-runtime-gamma-snapshot/bin/java.exe'
LIBRARY_SOURCE = PROJECT / 'runtime/next-unified/run/installed-candidate/libraries'
JAR = ROOT / 'build/libs/ft-biome-dungeon-generator-forge-1.20.1-0.5.1-commit-iofix-candidate.jar'
SEED = 267611119455246637


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--ordinal', type=int, required=True, choices=range(4))
    parser.add_argument('--forcekill', action='store_true')
    parser.add_argument('--attempt', default='')
    options = parser.parse_args()
    ordinal = options.ordinal
    assert not options.attempt or re.fullmatch('[a-z0-9-]+', options.attempt)
    attempt = '-' + options.attempt if options.attempt else ''
    run = ROOT / (f'run/installed-v5-q{ordinal}-forcekill' if options.forcekill else f'run/installed-v5-q{ordinal}')
    run = run.with_name(run.name + attempt)
    with zipfile.ZipFile(JAR) as archive:
        assert archive.testzip() is None
        assert 'cn/ft/biomedungeon/core/next/DungeonEnvelopePlanner.class' in archive.namelist()
        assert 'cn/ft/biomedungeon/runtime/CommitEvidence.class' in archive.namelist()
        assert not any('GameTests.class' in name for name in archive.namelist())
        assert hashlib.sha256(archive.read('data/ftbd_candidate/s8_07_widearch_side_doublegate_v5.nbt')).hexdigest() == 'bd7d0661b818164a3617629096580c646d17d95832269e6aca989933dc815ae3'
    run.mkdir(parents=True, exist_ok=False)
    shutil.copytree(LIBRARY_SOURCE, run / 'libraries')
    (run / 'mods').mkdir()
    shutil.copy2(JAR, run / 'mods' / JAR.name)
    (run / 'eula.txt').write_text('eula=true\n', encoding='utf-8')
    (run / 'server.properties').write_text(f'server-ip=127.0.0.1\nserver-port=0\nonline-mode=false\nlevel-name=world\nlevel-seed={SEED}\nview-distance=2\nsimulation-distance=2\nspawn-protection=0\n', encoding='utf-8')
    world = run / 'world'
    world.mkdir()
    (world / 'ALLOW_CANDIDATE_EVIDENCE_WORLD_V5').write_text('PARENT_AUTHORIZED_EIGHT_ORIGINALS_ONE_DERIVED_V5_EVIDENCE_ONLY\n', encoding='utf-8')
    command = [str(JAVA), '-Xmx2G', '@libraries/net/minecraftforge/forge/1.20.1-47.4.10/win_args.txt', 'nogui']
    result = {'jarSha256': digest(JAR), 'ordinal': ordinal, 'worldSeed': SEED, 'run': str(run), 'productionPromotion': False, 'completeGeneratorReady': False,
              'scope': 'EIGHT_ORIGINALS_ONE_AUTHORIZED_DERIVED_ENTRANCE_INSTALLED_COMMAND_NOT_INDEPENDENT_ACCEPTANCE', 'processes': []}

    def stage(name, identity=None):
        log = run / f'{name}.log'
        with log.open('w', encoding='utf-8') as output:
            process = subprocess.Popen(command, cwd=run, stdin=subprocess.PIPE, stdout=output, stderr=subprocess.STDOUT, text=True)
            record = {'stage': name, 'pid': process.pid, 'log': str(log)}
            result['processes'].append(record)

            def send(text):
                process.stdin.write(text + '\n')
                process.stdin.flush()

            def wait(predicate, timeout):
                end = time.monotonic() + timeout
                while time.monotonic() < end:
                    text = log.read_text(encoding='utf-8', errors='replace')
                    if predicate(text):
                        return text
                    if process.poll() is not None:
                        raise RuntimeError(f'{name} exited {process.returncode}')
                    time.sleep(1)
                raise TimeoutError(name)

            try:
                wait(lambda text: 'Done (' in text, 240)
                if identity is None:
                    send(f'ftbiomedungeon_candidate plan 16000 200 16000 s8 9 {ordinal}')
                    text = wait(lambda text: re.search(r'DRY RUN (ftde-[a-f0-9]+)', text) is not None or 'Rejected:' in text, 120)
                    match = re.search(r'DRY RUN ([a-z]+-[a-f0-9]+)', text)
                    if not match:
                        raise RuntimeError('Budget-constrained real planner failed before writes')
                    identity = match.group(1)
                send(f'ftbiomedungeon_candidate build {identity} CONFIRM_ISOLATED_AIRSPACE')
                if name == 'forcekill':
                    text = wait(lambda text: 'V5_DURABLE_CHECKPOINT' in text and re.search(r'completed=[1-9][0-9]{3,}', text), 180)
                    checkpoints = re.findall(r'V5_DURABLE_CHECKPOINT id=([a-z]+-[a-f0-9]+) completed=([0-9]+) owned=([0-9]+) saveMillis=([0-9]+)', text)
                    assert checkpoints and checkpoints[-1][0] == identity
                    record['lastDurableCheckpoint'] = {'completed': int(checkpoints[-1][1]), 'owned': int(checkpoints[-1][2]), 'saveMillis': int(checkpoints[-1][3])}
                    process.kill()
                    process.wait(timeout=30)
                    record['status'] = 'FORCED_PROCESS_TERMINATION_AFTER_DURABLE_CHECKPOINT_NOT_GRACEFUL'
                    return identity
                deadline = time.monotonic() + 480
                while time.monotonic() < deadline:
                    send(f'ftbiomedungeon_candidate status {identity}')
                    time.sleep(2)
                    text = log.read_text(encoding='utf-8', errors='replace')
                    if any(marker in text for marker in ['phase=FAILED', 'phase=ROLLBACK_REQUIRED', 'Rejected:']):
                        raise RuntimeError('Installed v5 quarantined/rejected; retain world/log')
                    if 'active=false tickets=0 stage=PAYLOAD_VERIFIED' in text:
                        break
                else:
                    raise TimeoutError('Installed job')
                assert 'V5_DERIVED_ENTRANCE_NATIVE_PASS' in text
                send(f'ftbiomedungeon_candidate acceptance-context {identity}')
                context_text = wait(lambda text: '"commitChallenge":' in text and '"jarSha256":' in text, 30)
                lines = [line for line in context_text.splitlines() if '"commitChallenge":' in line and '"jarSha256":' in line]
                binding = json.loads(lines[-1][lines[-1].index('{'):])
                assert binding['jarSha256'] == result['jarSha256']
                if name == 'prepare' or name == 'resume' and options.forcekill:
                    acceptance = world / 'candidate-acceptance'
                    acceptance.mkdir(exist_ok=True)
                    rejected = {'schema': 1, 'binding': binding, 'purpose': 'ISOLATED_EXPERIMENTAL_JOB_COMMIT_ONLY', 'independentExecutor': 'NEGATIVE_GATE_TEST_NOT_AN_APPROVAL',
                                'gates': {}, 'reports': {}, 'unresolvedBlockers': ['NEW_ARTIFACT_NOT_INDEPENDENTLY_ACCEPTED']}
                    receipt = acceptance / f'{identity}.json'
                    receipt.write_text(json.dumps(rejected, indent=2), encoding='utf-8')
                    send(f'ftbiomedungeon_candidate commit {identity} {digest(receipt)} CONFIRM_INDEPENDENT_EVIDENCE_COMMIT')
                    wait(lambda text: 'Every mandatory gate must be independently decided' in text, 30)
                    send(f'ftbiomedungeon_candidate status {identity}')
                    time.sleep(2)
                    latest = log.read_text(encoding='utf-8', errors='replace')
                    assert 'phase=COMMITTED' not in latest
                    record['incompleteCommitRefused'] = True
                send(f'ftbiomedungeon_candidate inspect {identity}')
                send(f'ftbiomedungeon_candidate evidence {identity}')
                time.sleep(2)
                record['status'] = 'INSTALLED_DERIVED_WIDTH_SIDE_DOOR_PAYLOAD_PASS_NOT_COMMITTED'
                record['binding'] = binding
                return identity
            finally:
                if process.poll() is None:
                    send('stop')
                    try:
                        process.wait(timeout=90)
                    except subprocess.TimeoutExpired:
                        process.kill()
                        process.wait(timeout=30)
                record['exitCode'] = process.returncode
                record['logSha256'] = digest(log)

    try:
        identity = stage('forcekill' if options.forcekill else 'prepare')
        result['instanceId'] = identity
        stage('resume', identity)
        result['status'] = 'PASS_INSTALLED_V5_DURABLE_CHECKPOINT_FORCEKILL_RESUME_NOT_POWERLOSS' if options.forcekill else 'PASS_INSTALLED_V5_ORIGINAL_WIDTH_THRESHOLD_SIDE_DOOR_RESTART_COMMIT_REFUSAL'
    except Exception as error:
        result['status'] = 'FAILED_NOT_ACCEPTED'
        result['error'] = repr(error)
        raise
    finally:
        result_name = f'installed-v5-q{ordinal}-forcekill{attempt}-result.json' if options.forcekill else f'installed-v5-q{ordinal}{attempt}-result.json'
        (ROOT / result_name).write_text(json.dumps(result, indent=2, ensure_ascii=False), encoding='utf-8')


if __name__ == '__main__':
    main()
