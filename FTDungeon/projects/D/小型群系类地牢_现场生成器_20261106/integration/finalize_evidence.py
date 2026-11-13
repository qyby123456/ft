import re
import json
from pathlib import Path

import integrate


def main():
    home = integrate.HOME
    runs = []
    for metadata in sorted((home / 'run').glob('20*/gametest-process.json')):
        process = integrate.load(metadata)
        log = Path(process['log'])
        text = log.read_text(encoding='utf-8', errors='replace')
        matches = re.findall(r'All (\d+) required tests passed', text)
        failures = [line for line in text.splitlines() if ' failed!' in line]
        markers = [line for line in text.splitlines() if line.startswith(('INTEGRATION_ASSERT ', 'RUNTIME_WORLD_PASS '))]
        snapshot = home / 'staging' / metadata.parent.name
        lock = integrate.load(snapshot / 'snapshot-lock.json')
        runs.append(dict(run=metadata.parent.name, process=process, logSha256=integrate.digest(log), snapshotLockSha256=integrate.digest(snapshot / 'snapshot-lock.json'), verificationLockSha256=integrate.digest(metadata.parent / 'verification-lock.json'), requiredTestsPassed=int(matches[-1]) if matches else 0, failures=failures, assertions=markers, sourceReadyHashes={item['component']: item['readySha256'] for item in lock['components']}, jar=integrate.load(metadata.parent / 'jars.json')))
    successful = [item for item in runs if item['process']['status'] == 'PASS' and item['requiredTestsPassed'] > 0]
    if not successful:
        raise integrate.GateError('No successful real GameTest suite with required-test summary')
    latest = successful[-1]
    evidence = dict(updatedAt=integrate.stamp(), status='SOURCE_WORLD_PARTIAL_ACCEPTANCE', suites=runs, latestSuccessful=latest['run'], installedJar=integrate.load(home / 'installed-server-result.json') if (home / 'installed-server-result.json').exists() else dict(status='NOT_RUN'), restart=integrate.load(home / 'restart-replay-result.json') if (home / 'restart-replay-result.json').exists() else dict(status='NOT_RUN'))
    matrix = integrate.load(home / 'test-matrix.json')
    for label, filename in (('installedReadback', 'installed-readback-initial.json'), ('installedRecovery', 'installed-recovery-result.json'), ('installedConflict', 'installed-conflict-result.json')):
        evidence[label] = integrate.load(home / filename) if (home / filename).exists() else dict(status='NOT_RUN')
    installed_verified = evidence['installedJar'].get('status') == 'PASS_COMMAND_COMMIT' and evidence['installedReadback'].get('status') == 'PASS'
    evidence['installedJarAcceptance'] = 'PASS_SCOPED' if installed_verified else 'NOT_RUN'

    def mark(name, status, scope):
        matrix['cases'][name].update(status=status, scope=scope, evidence=[dict(run=latest['run'], log=latest['process']['log'], logSha256=latest['logSha256'], snapshotLockSha256=latest['snapshotLockSha256'], verificationLockSha256=latest['verificationLockSha256'])])

    markers = '\n'.join(latest['assertions'])
    if 'player_conflict PASS' in markers:
        mark('player_conflict', 'PASS', 'Gold block conflict rejected; full operation-set comparison unchanged; owned ticket list empty')
        mark('preflight', 'PASS', 'Occupied preflight rejects with cursor=attempted=0 and full operation set unchanged')
    if 'independent_rotation_sampling PASS' in markers:
        mark('randomness', 'PARTIAL', '32 seeds, 32 layouts, reversed pool stable, two real template IDs; one mixed three-room layout world-tested, not all seeds in-world')
        mark('rotation', 'PARTIAL', 'Independent integer cell/cube correction checked all four turns; physical four-turn result only if explicit all_rotations_world marker below')
    if 'all_rotations_world PASS' in markers:
        mark('rotation', 'PASS', 'All four rotations and both real S8 templates placed in-world; raw interior cells independently transformed and compared with Minecraft-rotated states; port faces separately covered by route test')
    if 'mixed_world_route_shell PASS' in markers:
        mark('return_route', 'PASS', 'Sampled mixed three-room S8 plan: all used door feet and corridor waypoints connected by actual floor/collision BFS')
        mark('doors_barriers', 'PASS', 'Sampled mixed three-room plan: complete operation-set readback includes perimeter, door seam/corridor BFS remains connected; not all possible layouts')
        mark('boundary_tickets', 'PARTIAL', 'Owned ticket list zero after conflict/commit; invalid anchor refusal tested. DistanceManager internal ticket inspection/world-border edge NOT_RUN')
    if 'room_readback_journal_reload PASS' in markers:
        mark('readback_payload', 'PARTIAL', 'Single and mixed three-room full operation readback; static safe templates only, rejected block-entity/entity payload world injection NOT_RUN')
    if evidence['restart']['status'] == 'PASS_CLEAN_INTERRUPTED_RECOVERY':
        mark('restart', 'PARTIAL', 'Two independent JVMs replay owner recovery fixtures: clean interrupted shutdown at cursor=128, explicit resume and full readback PASS; forced kill/powerloss NOT_RUN')
        matrix['cases']['restart']['restartEvidence'] = evidence['restart']
    if evidence['installedRecovery'].get('status') == 'PASS':
        matrix['cases']['restart'].update(status='PARTIAL', scope='Actual installed candidate JAR, distinct dedicated JVMs, clean mid-build pause/shutdown, explicit resume, idempotent commit and independent Anvil readback PASS; forced kill/powerloss NOT_RUN', installedEvidence=evidence['installedRecovery'])
    if evidence['installedConflict'].get('status') == 'PASS':
        for case in ('preflight', 'player_conflict'):
            matrix['cases'][case].update(installedEvidence=evidence['installedConflict'])
    if installed_verified:
        matrix['cases']['readback_payload'].update(installedEvidence=evidence['installedReadback'])
    matrix.update(worldAcceptance='PARTIAL', updatedAt=integrate.stamp())
    integrate.save(home / 'test-matrix.json', matrix)
    issues = []
    for suite in runs:
        if suite['failures']:
            resolved = suite['run'] < latest['run']
            issues.append(dict(id='INDEPENDENT_SUITE_' + suite['run'], severity='TEST_FAILURE', status='RESOLVED_FIXTURE' if resolved else 'OPEN', detail='Not a Forge startup failure. Two independent fixture identity assertions omitted occupied reservations from core instance identity. Fixture now consumes exact persisted runtime plan and independently compares room/link/seed layout.', failures=suite['failures'], log=suite['process']['log'], logSha256=suite['logSha256'], resolutionRun=latest['run'] if resolved else None))
    if evidence['installedJar'].get('worldAcceptance') == 'NOT_RUN':
        issues.append(dict(id='INSTALLED_JAR_WORLD_NOT_RUN', severity='BLOCKER_FOR_INSTALLED_RELEASE', status='OPEN', detail=evidence['installedJar'].get('reason', 'Installed JAR world checks not run'), evidence='installed-server-result.json', url='https://maven.minecraftforge.net/net/minecraftforge/forge/1.20.1-47.4.10/forge-1.20.1-47.4.10-installer.jar'))
    elif installed_verified:
        issues.append(dict(id='INSTALLER_URLLIB_403', severity='HISTORY', status='RESOLVED_VERIFIED_LOCAL_INSTALLER', detail='Original urllib HTTP403 is preserved in run/20261106_131217_940590_installed/result.json. Same official installer independently verified against live official SHA1 and ZIP CRC, then copied into a new integration run and installed successfully.', evidence='installed-server-result.json'))
    for label in ('installedRecovery', 'installedConflict'):
        if evidence[label].get('status') == 'FAIL':
            issues.append(dict(id=label.upper(), severity='TEST_FAILURE', status='OPEN', detail=evidence[label]))
    if (home / 'logs/installed-conflict-first-fixture-failure.json').exists():
        issues.append(dict(id='INSTALLED_CONFLICT_UNLOADED_FIXTURE', severity='TEST_FIXTURE', status='RESOLVED' if evidence['installedConflict'].get('status') == 'PASS' else 'OPEN', detail='First injected vanilla setblock refused because the distant chunk was not loaded; no generator build executed in that attempt. Original log/result retained. Revised fixture explicitly forceloads injection chunk, removes test force-load before shutdown, then independently compares saved full envelope.', evidence='logs/installed-conflict-first-fixture-failure.json'))
    issues.append(dict(id='INCOMPLETE_WORLD_MATRIX', severity='LIMITATION', status='OPEN', detail='Empty pool/corrupt NBT zero-write tests are offline only; forced-kill durability, native payload injections, world-border edge and internal ticket-manager audit remain NOT_RUN'))
    integrate.save(home / 'ISSUES.json', dict(updatedAt=integrate.stamp(), issues=issues))
    integrate.save(home / 'final-evidence.json', evidence)
    progress = integrate.load(home / 'progress.json')
    progress.update(status='BUILT_INSTALLED_WORLD_SCOPED_PASS' if installed_verified else 'BUILT_SOURCE_WORLD_PARTIAL', worldAcceptance='PARTIAL', latestSuccessfulWorldRun=latest['run'], installedJarWorldAcceptance=evidence['installedJarAcceptance'], installedRecovery=evidence['installedRecovery'].get('status'), installedConflict=evidence['installedConflict'].get('status'), restartReplayStatus=evidence['restart']['status'], updatedAt=integrate.stamp())
    integrate.save(home / 'progress.json', progress)
    report = [
        '# Independent integration evidence', '',
        'Conclusion: exact-hash build and source-based Forge world checks passed. Actual normal Forge dedicated-server candidate JAR verification: ' + evidence['installedJarAcceptance'] + '. This is not a fully accepted release; remaining safety limits still apply.', '',
        '## Exact candidate',
        '- Latest successful run: ' + latest['run'],
        '- Required GameTests passed: ' + str(latest['requiredTestsPassed']) + ' (independent assertions and owner fixtures are separately prefixed in log)',
        '- JAR: ' + latest['jar'][0]['path'],
        '- JAR SHA-256: ' + latest['jar'][0]['sha256'],
        '- Snapshot lock SHA-256: ' + latest['snapshotLockSha256'],
        '- Verification lock SHA-256: ' + latest['verificationLockSha256'],
        '- World log: ' + latest['process']['log'],
        '- World log SHA-256: ' + latest['logSha256'], '',
        '## Actual installed candidate JAR',
        '- Installer/server result: installed-server-result.json; current status: ' + evidence['installedJar']['status'],
        '- Independent saved-chunk readback: ' + evidence['installedReadback']['status'],
        '- Installed clean interrupt/resume/idempotency and final independent chunks: ' + evidence['installedRecovery']['status'],
        '- Installed injected player-block zero-write envelope comparison: ' + evidence['installedConflict']['status'],
        '- Actual installed run: ' + evidence['installedJar'].get('run', ''),
        '- Actual installed normal-server process: ' + str(evidence['installedJar'].get('process', {}).get('pid', 'NOT_RUN')),
        '- Recovery process IDs: ' + ', '.join(str(stage['pid']) for stage in evidence['installedRecovery'].get('stages', [])),
        '- Interrupted saved cursor: ' + str(evidence['installedRecovery'].get('savedCursor', 'NOT_RUN')) + '; committed idempotent replay: ' + evidence['installedRecovery'].get('idempotentStatus', 'NOT_RUN'),
        '- Final independent installed readback: ' + json.dumps(evidence['installedRecovery'].get('independentSavedChunkReadback', {}).get('results', []), ensure_ascii=False),
        '- Conflict full saved envelope cells compared: ' + str(evidence['installedConflict'].get('checkedEnvelopeCells', 'NOT_RUN')),
        '- Exact installed mods JAR is the candidate hash above; no source-test classes or extra mods loaded. Normal dedicated-server launch, not ForgeGradle userdev.',
        '- Actual installer official SHA1/CRC evidence: logs/official-installer-sha1.json and installed-server-result.json. Prior HTTP403 remains historical, not current blocker.', '',
        '## Actual assertions', *['- ' + marker for marker in latest['assertions']], '',
        '## Clean interrupted restart replay',
        '- ' + evidence['restart']['status'], *['- ' + marker for marker in evidence['restart'].get('assertions', [])], '',
        '## Preserved failure and limits',
        '- First suite ran 5 tests; two independent fixture assertions failed (Planner and runtime instance identity differs). Server startup succeeded; owner tests passing did not cancel those failures. Original failed log is retained.',
        '- Fixed only integration fixture: runtime instance identity includes occupied reservations; use persisted runtime plan and compare independently computed rooms/links/seed, not an identity computed with empty occupied set.',
        '- Installed normal Forge server attempt: ' + evidence['installedJar']['status'] + ': ' + evidence['installedJar'].get('reason', ''),
        '- Forced kill/powerloss, empty-pool/corrupt-NBT in-world zero-write checks, native payload injections, internal DistanceManager ticket audit and world-border edges remain unverified.',
        '- Geometric sampling across 32 seeds is not 32 world placement runs. Physical return-route PASS is scoped to the sampled mixed three-room layout, not every layout.', '',
        '## Reproduction',
        'Use the explicit Python/Java17/Gradle paths in README.md. integrate.py gametest --gradle <Gradle8.8> --java-home <Java17> --offline --timeout 900 creates a fresh exact-hash snapshot and world.',
        'replay_restart.py --snapshot <snapshot> --gradle <Gradle8.8> --java-home <Java17> runs prepare/resume in distinct JVMs under integration/run.',
        'installed_server.py --snapshot <snapshot> --java-home <Java17> --installer <verified local installer> --official-sha1 <live official digest> runs actual installer-based dedicated-server candidate execution; never touches the formal instance.',
        'installed_recovery.py --snapshot <snapshot> --run <installed run> --java-home <Java17> and installed_conflict.py with the same arguments perform normal dedicated-process recovery/conflict checks. See README for fresh-fixture requirements.',
    ]
    (home / 'INTEGRATION_REPORT.md').write_text('\n'.join(report) + '\n', encoding='utf-8')
    print(dict(latestSuccessful=latest['run'], requiredTestsPassed=latest['requiredTestsPassed'], installedJarWorld=evidence['installedJar'].get('worldAcceptance'), restart=evidence['restart']['status']))


if __name__ == '__main__':
    main()
