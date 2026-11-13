import json
from pathlib import Path
import re
import shutil
import subprocess
import time
from installed_v5 import ROOT, JAVA, LIBRARY_SOURCE, JAR, SEED, digest


def main():
    run = ROOT / 'run/installed-pause-io'
    run.mkdir(parents=True, exist_ok=False)
    shutil.copytree(LIBRARY_SOURCE, run / 'libraries')
    (run / 'mods').mkdir()
    shutil.copy2(JAR, run / 'mods' / JAR.name)
    (run / 'eula.txt').write_text('eula=true\n', encoding='utf-8')
    (run / 'server.properties').write_text(f'server-ip=127.0.0.1\nserver-port=0\nonline-mode=false\nlevel-name=world\nlevel-seed={SEED}\nview-distance=2\nsimulation-distance=2\n', encoding='utf-8')
    world = run / 'world'
    world.mkdir()
    (world / 'ALLOW_CANDIDATE_EVIDENCE_WORLD_V5').write_text('PARENT_AUTHORIZED_EIGHT_ORIGINALS_ONE_DERIVED_V5_EVIDENCE_ONLY\n', encoding='utf-8')
    log = run / 'pause-io.log'
    result = {'status': 'RUNNING', 'jarSha256': digest(JAR), 'scope': 'ACTUAL_INSTALLED_NATIVE_PAUSE_JOURNAL_IO_FAILURE', 'world': str(world), 'productionPromotion': False}
    command = [str(JAVA), '-Xmx2G', '@libraries/net/minecraftforge/forge/1.20.1-47.4.10/win_args.txt', 'nogui']
    with log.open('w', encoding='utf-8') as output:
        process = subprocess.Popen(command, cwd=run, stdin=subprocess.PIPE, stdout=output, stderr=subprocess.STDOUT, text=True)
        result['pid'] = process.pid

        def send(text):
            process.stdin.write(text + '\n')
            process.stdin.flush()

        def wait(predicate, timeout=120):
            deadline = time.monotonic() + timeout
            while time.monotonic() < deadline:
                text = log.read_text(encoding='utf-8', errors='replace')
                if predicate(text):
                    return text
                if process.poll() is not None:
                    raise RuntimeError('Pause I/O server exited')
                time.sleep(0.5)
            raise TimeoutError('Pause I/O log')

        try:
            wait(lambda text: 'Done (' in text, 240)
            send('ftbiomedungeon_candidate plan 16000 200 16000 s8 9 3')
            text = wait(lambda text: 'DRY RUN ftde-' in text)
            identity = re.search(r'DRY RUN (ftde-[a-f0-9]+)', text).group(1)
            journal = world / f'ft_biome_dungeon_candidate_v5/minecraft/overworld/{identity}.nbt'
            before = digest(journal)
            blocked_temporary = journal.with_name(journal.name + '.pending')
            blocked_temporary.mkdir()
            send(f'ftbiomedungeon_candidate build {identity} CONFIRM_ISOLATED_AIRSPACE\nftbiomedungeon_candidate pause {identity}\nftbiomedungeon_candidate status {identity}\nftbiomedungeon_candidate list')
            wait(lambda text: 'DISABLED: Pause journal persistence failed' in text)
            send(f'ftbiomedungeon_candidate status {identity}')
            time.sleep(2)
            send(f'ftbiomedungeon_candidate status {identity}')
            text = wait(lambda text: len(re.findall(r'phase=PLANNED cursor=0 attempted=0 active=false tickets=0', text)) >= 4)
            assert 'phase=COMMITTED' not in text and 'V5_DURABLE_CHECKPOINT' not in text
            assert digest(journal) == before
            result.update(status='PASS_ACTUAL_INSTALLED_PAUSE_IO_FAILURE_INACTIVE_ZERO_TICKETS_DISABLED', instanceId=identity,
                          failureInjection='Original AtomicFiles .pending path is an actual directory: FileChannel.open fails; no source instrumentation',
                          previousJournalUnchanged=True, zeroCompletedWrites=True, delayedStatusStillInactive=True,
                          limitation='Actual pause IOException/native ticket cleanup observed; commit IOException uses exact-method injection, not fabricated independent admission')
        except Exception as error:
            result.update(status='FAILED_NOT_ACCEPTED', error=repr(error))
            raise
        finally:
            if process.poll() is None:
                send('stop')
                process.wait(timeout=90)
            result.update(exitCode=process.returncode, log=str(log), logSha256=digest(log))
            (ROOT / 'installed-pause-io-result.json').write_text(json.dumps(result, indent=2), encoding='utf-8')


if __name__ == '__main__':
    main()
