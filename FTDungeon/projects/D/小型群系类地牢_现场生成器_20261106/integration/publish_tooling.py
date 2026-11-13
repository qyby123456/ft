import sys
from pathlib import Path

import integrate


def main():
    home = integrate.HOME
    tests = integrate.execute([sys.executable, str(home / 'test_integration.py')], home, home / 'logs/tool-tests.log', 120)
    if tests['status'] != 'PASS':
        print('Tooling tests failed; ready.json not published')
        return 1
    reader_tests = integrate.execute([sys.executable, str(home / 'test_saved_world.py')], home, home / 'logs/saved-world-tests.log', 120)
    if reader_tests['status'] != 'PASS':
        return 1
    dependencies = integrate.probe()
    sources = [home / name for name in ('integrate.py', 'test_integration.py', 'run_smoke.py', 'publish_tooling.py', 'README.md', 'world-overlay.gradle', 'installed_server.py', 'replay_restart.py', 'finalize_evidence.py', 'verify_saved_world.py', 'installed_recovery.py', 'installed_conflict.py', 'test_saved_world.py')]
    sources.extend(path for path in (home / 'smoke-src').rglob('*') if path.is_file())
    sources.extend(path for path in (home / 'world-tests').rglob('*') if path.is_file())
    artifacts = [home / 'logs/tool-tests.log', home / 'logs/tool-tests.json']
    for optional in ('final-evidence.json', 'INTEGRATION_REPORT.md', 'ISSUES.json', 'test-matrix.json', 'installed-server-result.json', 'restart-replay-result.json', 'installed-readback-initial.json', 'installed-recovery-result.json', 'installed-conflict-result.json', 'logs/official-installer-sha1.json', 'logs/saved-world-tests.log', 'logs/saved-world-tests.json'):
        if (home / optional).is_file():
            artifacts.append(home / optional)
    for lock in (home / 'staging').glob('*/snapshot-lock.json'):
        artifacts.append(lock)
    for filename in ('build.log', 'build.json', 'gametest.log', 'gametest-process.json', 'verification-lock.json', 'jars.json', 'prepare.log', 'prepare.json', 'resume.log', 'resume.json', 'result.json'):
        artifacts.extend((home / 'run').glob('20*/' + filename))
    for lock in (home / 'staging').glob('*/assembled/build/libs/*.jar'):
        artifacts.append(lock)
    for filename in ('install.log', 'install.json', 'server.log', 'installed-prepare.log', 'installed-resume.log', 'installed-conflict.log', 'installed-recovery-result.json', 'installed-conflict-result.json'):
        artifacts.extend((home / 'run').glob('*installed*/' + filename))
    for installed_run in (home / 'run').glob('*installed*'):
        artifacts.extend(installed_run.glob('installed-conflict-*.log'))
        artifacts.extend(installed_run.glob('installed-conflict-result-*.json'))
    artifacts.extend((home / 'logs').glob('installed-conflict-first-fixture-failure.json'))
    for installed_run in (home / 'run').glob('*installed*'):
        artifacts.extend(installed_run.glob('mods/*.jar'))
        artifacts.extend(installed_run.glob('forge-installer.jar'))
        artifacts.extend(installed_run.glob('libraries/net/minecraftforge/forge/1.20.1-47.4.10/win_args.txt'))
        artifacts.extend(installed_run.glob('world/region/*.mca'))
        artifacts.extend(installed_run.glob('world/ft_biome_dungeon_generator/minecraft/overworld/*.nbt'))
    for optional in (home / 'logs/catalog-gate.json', home / 'run/overlay-config/configuration.json', home / 'run/overlay-config/configuration.log'):
        if optional.is_file():
            artifacts.append(optional)
    smoke_path = home / 'environment-smoke.json'
    smoke = integrate.load(smoke_path) if smoke_path.exists() else {'status': 'NOT_RUN'}
    if smoke_path.exists():
        artifacts.append(smoke_path)
        log = Path(smoke['log'])
        if not log.is_absolute():
            log = home / log
        if log.is_file() and log.resolve().is_relative_to(home):
            artifacts.append(log)
    files = {str(path.relative_to(home)).replace('\\', '/'): integrate.digest(path) for path in sources + artifacts}
    limits = [
        'Read final-evidence.json for actual source-world and candidate build scope; READY is not full release acceptance',
        'Actual installed-JAR scope is in installed-server-result.json plus independent readback/recovery/conflict results; earlier urllib403 is historical',
        'Forced kill/powerloss, in-world empty/corrupt catalog payload injection, native entity/block-entity payload and world-border edges NOT_RUN',
        'Chunk ticket checks are owned job ticket list checks, not direct DistanceManager ticket audit',
        'Environment smoke is unrelated to dungeon correctness and never promotes dungeon acceptance',
    ]
    matrix_path = home / 'test-matrix.json'
    acceptance = integrate.load(matrix_path)['worldAcceptance'] if matrix_path.exists() else 'NOT_RUN'
    installed_pass = (home / 'final-evidence.json').is_file() and integrate.load(home / 'final-evidence.json').get('installedJarAcceptance') == 'PASS_SCOPED'
    scope = 'EXACT_HASH_INSTALLED_JAR_WORLD_SCOPED_PASS' if installed_pass else ('EXACT_HASH_BUILD_AND_SOURCE_WORLD_PARTIAL' if (home / 'final-evidence.json').is_file() else 'TOOLING_ONLY')
    ready = dict(component='integration', apiVersion=1, status='READY', scope=scope, createdAt=integrate.stamp(), files=files, tests=[tests, reader_tests], dependencies=dependencies, environmentSmoke=smoke, worldAcceptance=acceptance, limitations=limits)
    integrate.save(home / 'ready.json', ready)
    progress_path = home / 'progress.json'
    progress = integrate.load(progress_path) if progress_path.exists() else {}
    progress.update(toolingStatus='READY', toolTests=tests, environmentSmoke=smoke, updatedAt=integrate.stamp())
    integrate.save(progress_path, progress)
    print('Published integration tooling READY; dungeon world acceptance ' + acceptance)
    return 0


if __name__ == '__main__':
    sys.exit(main())
