import argparse
import datetime
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys
import time
import zipfile

HOME = Path(__file__).resolve().parent
ROOT = HOME.parent
COMPONENTS = ('core', 'runtime', 'catalog')
WORLD_CASES = {
    'randomness': 'Multiple seeds yield room-level world variation',
    'rotation': 'All rotations match blocks and ports',
    'player_conflict': 'Reject player blocks with zero writes',
    'empty_pool': 'Empty pool fails before writes',
    'wrong_hash': 'Corrupt NBT fails before writes',
    'preflight': 'Failed preflight leaves target snapshot unchanged',
    'restart': 'Interrupt mid-build then recover idempotently',
    'readback_payload': 'Readback blocks, block entities and entity policy',
    'boundary_tickets': 'Boundary refusal and release all chunk tickets',
    'return_route': 'Entrance reachable from every room',
    'doors_barriers': 'Door seams/perimeter preserve internal passages',
}


class GateError(Exception):
    pass


def stamp():
    return datetime.datetime.now(datetime.timezone(datetime.timedelta(hours=8))).isoformat()


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def load(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))


def save(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    temporary = path.with_suffix(path.suffix + '.tmp')
    temporary.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    temporary.replace(path)


def contained(base, relative):
    relative = str(relative)
    candidate = (base / relative).resolve()
    if Path(relative).is_absolute() or '..' in Path(relative).parts:
        raise GateError(f'Unsafe relative path: {relative}')
    if not candidate.is_relative_to(base.resolve()) or candidate == base.resolve():
        raise GateError(f'Path escapes component: {relative}')
    cursor = base / relative
    while cursor != base:
        if cursor.is_symlink() or (hasattr(cursor, 'is_junction') and cursor.is_junction()):
            raise GateError(f'Link/junction refused: {relative}')
        cursor = cursor.parent
    return candidate


def inventory(ready, allow_empty=False):
    result = {}

    def add(filename, checksum):
        if not re.fullmatch('[0-9a-fA-F]{64}', checksum):
            raise GateError(f'Invalid SHA-256: {filename}')
        if filename in result and result[filename] != checksum.lower():
            raise GateError(f'Conflicting hashes: {filename}')
        result[filename] = checksum.lower()

    def visit(value):
        if isinstance(value, dict):
            filename = value.get('path', value.get('file'))
            checksum = value.get('sha256')
            if isinstance(filename, str) and isinstance(checksum, str):
                add(filename, checksum)
            for key, item in value.items():
                if isinstance(item, str) and re.fullmatch('[0-9a-fA-F]{64}', item) and ('/' in key or '.' in key):
                    add(key, item)
                elif isinstance(item, (dict, list)):
                    visit(item)
        elif isinstance(value, list):
            for item in value:
                visit(item)

    fields = ('files', 'sources', 'artifacts', 'sourceHashes', 'artifactHashes', 'hashes', 'checksums', 'sourceFiles', 'artifactFiles', 'resources')
    declared = [key for key in fields if isinstance(ready, dict) and key in ready]
    if declared:
        for key in declared:
            visit(ready[key])
    else:
        visit(ready)
    if not result and not allow_empty:
        raise GateError('ready.json lacks explicit per-file path/SHA-256 inventory')
    return result


def check_component(base, name):
    marker = base / 'ready.json'
    if not marker.is_file():
        return dict(component=name, status='WAITING', reason='ready.json not delivered')
    marker_hash = digest(marker)
    ready = load(marker)
    if not isinstance(ready, dict):
        raise GateError(f'{name}: ready marker must be an object')
    if ready.get('component') != name or ready.get('apiVersion') != 1:
        raise GateError(f'{name}: component/apiVersion mismatch')
    status = str(ready.get('status', 'READY')).upper()
    explicit_ready = status in ('READY', 'COMPLETE', 'COMPLETED') or (ready.get('ready') is True and status.startswith('READY_'))
    if ready.get('ready') is False or not explicit_ready:
        return dict(component=name, status='WAITING', reason=f"Explicit status {ready['status']}")
    files = inventory(ready, allow_empty=isinstance(ready.get('inventory'), str))
    if isinstance(ready.get('inventory'), str):
        inventory_path = contained(base, ready['inventory'])
        if not inventory_path.is_file() or digest(inventory_path) != ready.get('inventorySha256'):
            raise GateError(f'{name}: inventory hash missing/mismatch')
        additional = inventory(load(inventory_path))
        additional[ready['inventory']] = digest(inventory_path)
        for filename, checksum in additional.items():
            if filename in files and files[filename] != checksum:
                raise GateError(f'{name}: contradictory inventory for {filename}')
            files[filename] = checksum
    for filename, expected in files.items():
        source = contained(base, filename)
        if not source.is_file() or digest(source) != expected:
            raise GateError(f'{name}: missing/changed frozen file {filename}')
    if digest(marker) != marker_hash:
        raise GateError(f'{name}: marker changed while reading')
    return dict(component=name, status='READY', readySha256=marker_hash, files=files)


def probe():
    checks = []
    for name in COMPONENTS:
        try:
            checks.append(check_component(ROOT / name, name))
        except (GateError, OSError, ValueError) as error:
            checks.append(dict(component=name, status='REJECTED', reason=str(error)))
    return checks


def snapshot(checks, target):
    if any(item['status'] != 'READY' for item in checks):
        raise GateError('WAITING: all components must pass ready/hash gate')
    target.mkdir(parents=True, exist_ok=False)
    for item in checks:
        name = item['component']
        source_base = ROOT / name
        if digest(source_base / 'ready.json') != item['readySha256']:
            raise GateError(f'{name}: marker changed before snapshot')
        for relative, expected in item['files'].items():
            source = contained(source_base, relative)
            destination = contained(target / name, relative)
            destination.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(source, destination)
            if digest(destination) != expected or digest(source) != expected:
                raise GateError(f'{name}: snapshot race/hash mismatch {relative}')
        shutil.copyfile(source_base / 'ready.json', target / name / 'ready.json')
        if digest(target / name / 'ready.json') != item['readySha256']:
            raise GateError(f'{name}: marker changed during snapshot')
    save(target / 'snapshot-lock.json', dict(createdAt=stamp(), components=checks))
    return target


def validate_manifest(path, structures, evidence_root=None):
    manifest = load(path)
    if not isinstance(manifest, dict) or manifest.get('schemaVersion') != 1 or not isinstance(manifest.get('templates'), list):
        raise GateError('Invalid manifest schema')
    if not manifest['templates']:
        raise GateError('Empty template pool')
    seen = set()
    for item in manifest['templates']:
        if not isinstance(item, dict):
            raise GateError('Template must be an object')
        identifier = item.get('id', '')
        if not re.fullmatch('[a-z0-9_/-]+', identifier) or '..' in identifier or identifier.startswith('/'):
            raise GateError(f'Invalid resource id {identifier}')
        if identifier in seen or item.get('resourceId') != 'biomedungeon:' + identifier:
            raise GateError(f'Duplicate/non-whitelisted resource {identifier}')
        seen.add(identifier)
        if item.get('accepted') is not True:
            raise GateError(f'Not independently accepted: {identifier}')
        if not any(item.get(key) for key in ('independentAcceptance', 'acceptance', 'acceptanceEvidence', 'evidence', 'acceptanceSource')):
            raise GateError(f'Missing acceptance evidence: {identifier}')
        if evidence_root is not None:
            evidence = item.get('independentAcceptance', {})
            if evidence.get('sourceSha256') != item.get('sha256') or evidence.get('unchangedBytes') is not True:
                raise GateError(f'Acceptance does not cover original bytes: {identifier}')
            records = evidence.get('acceptanceRecords')
            if not isinstance(records, list) or not records or not evidence.get('independentFileAudit'):
                raise GateError(f'Acceptance record/audit missing: {identifier}')
            for record in [evidence['independentFileAudit']] + records:
                if not isinstance(record, dict) or not isinstance(record.get('snapshot'), str):
                    raise GateError(f'Invalid frozen acceptance record: {identifier}')
                source = contained(evidence_root, record['snapshot'])
                if not source.is_file() or digest(source) != record.get('sha256'):
                    raise GateError(f'Acceptance record hash mismatch: {identifier}')
        size = item.get('size', {})
        if any(type(size.get(axis)) is not int or not 0 < size[axis] <= 512 for axis in ('x', 'y', 'z')):
            raise GateError(f'Invalid size: {identifier}')
        if not item.get('ports'):
            raise GateError(f'Missing ports: {identifier}')
        for port in item['ports']:
            if port.get('facing') not in ('north', 'east', 'south', 'west') or port.get('medium') not in ('dry', 'wet'):
                raise GateError(f'Invalid port enums: {identifier}')
            if any(type(port.get(key)) is not int or not 1 <= port[key] <= 32 for key in ('width', 'height')):
                raise GateError(f'Invalid port size: {identifier}')
            if any(type(port.get('feet', {}).get(axis)) is not int for axis in ('x', 'y', 'z')):
                raise GateError(f'Invalid port feet: {identifier}')
        nbt = contained(structures, identifier + '.nbt')
        if not nbt.is_file() or digest(nbt) != item.get('sha256'):
            raise GateError(f'NBT hash mismatch: {identifier}')
    return len(seen)


def assemble(frozen):
    project = frozen / 'assembled'
    shutil.copytree(frozen / 'runtime', project)
    if (project / 'src/main/java/cn/ft/biomedungeon/core').exists():
        raise GateError('Runtime contains core source/stub; use frozen core only')
    if not (frozen / 'core/src/main/java/cn/ft/biomedungeon/core/DungeonApi.java').is_file():
        raise GateError('Frozen real core DungeonApi missing')
    catalog = frozen / 'catalog'
    manifests = list(catalog.rglob('manifest.json'))
    if not manifests or len({digest(path) for path in manifests}) != 1:
        raise GateError('Missing or conflicting frozen manifests')
    structures = catalog / 'data/biomedungeon/structures'
    count = validate_manifest(manifests[0], structures, catalog)
    resources = project / 'src/main/resources/data/biomedungeon'
    resources.mkdir(parents=True, exist_ok=True)
    target_manifest = resources / 'manifest.json'
    if target_manifest.exists() and digest(target_manifest) != digest(manifests[0]):
        raise GateError('Runtime manifest conflicts with catalog; owner must resolve')
    for item in load(manifests[0])['templates']:
        relative = item['id'] + '.nbt'
        destination = contained(resources / 'structures', relative)
        destination.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(contained(structures, relative), destination)
    shutil.copyfile(manifests[0], target_manifest)
    save(frozen / 'assembly.json', dict(project=str(project), templates=count))
    return project


def verify_jar(path, project):
    manifest_path = project / 'src/main/resources/data/biomedungeon/manifest.json'
    manifest = load(manifest_path)
    expected = {'data/biomedungeon/structures/' + item['id'] + '.nbt': item['sha256'] for item in manifest['templates']}
    with zipfile.ZipFile(path) as archive:
        names = archive.namelist()
        if len(names) != len(set(names)):
            raise GateError('Duplicate ZIP entries in JAR')
        if 'cn/ft/biomedungeon/core/DungeonApi.class' not in names:
            raise GateError('Real core API not packaged')
        actual = {name for name in names if name.startswith('data/biomedungeon/structures/') and not name.endswith('/')}
        if actual != set(expected):
            raise GateError('Packaged structure whitelist differs from frozen manifest')
        if archive.read('data/biomedungeon/manifest.json') != manifest_path.read_bytes():
            raise GateError('Packaged manifest differs from snapshot')
        for name, checksum in expected.items():
            if hashlib.sha256(archive.read(name)).hexdigest() != checksum:
                raise GateError(f'Packaged NBT hash mismatch: {name}')
    return dict(path=str(path), sha256=digest(path), templates=len(expected))


def execute(command, cwd, log, timeout, environment=None):
    log.parent.mkdir(parents=True, exist_ok=True)
    started = time.monotonic()
    metadata = dict(command=command, cwd=str(cwd), startedAt=stamp(), timeoutSeconds=timeout)
    with log.open('w', encoding='utf-8') as stream:
        process = subprocess.Popen(command, cwd=cwd, stdout=stream, stderr=subprocess.STDOUT, env=environment)
        try:
            code = process.wait(timeout=timeout)
            metadata.update(exitCode=code, status='PASS' if code == 0 else 'FAIL')
        except subprocess.TimeoutExpired:
            if os.name == 'nt':
                metadata['timeoutCleanup'] = dict(command=['taskkill', '/PID', str(process.pid), '/T', '/F'], cwd=str(cwd))
                subprocess.run(metadata['timeoutCleanup']['command'], cwd=cwd, stdout=stream, stderr=stream)
            else:
                process.kill()
            process.wait()
            metadata.update(exitCode=process.returncode, status='TIMEOUT')
    metadata.update(durationSeconds=round(time.monotonic() - started, 3), log=str(log))
    save(log.with_suffix('.json'), metadata)
    return metadata


def run_build(project, gradle, java_home, timeout, gametest, offline=False):
    environment = os.environ.copy()
    environment['JAVA_HOME'] = str(java_home)
    run = HOME / 'run' / project.parent.name
    run.mkdir(parents=True, exist_ok=True)
    java_command = [str(java_home / 'bin/java.exe'), '-version']
    version = subprocess.run(java_command, cwd=project, capture_output=True, text=True)
    save(run / 'java-version.json', dict(command=java_command, cwd=str(project), exitCode=version.returncode, stdout=version.stdout, stderr=version.stderr))
    if not re.search(r'version "17[.\"]', version.stderr + version.stdout):
        raise GateError('Explicit Java 17 JAVA_HOME required')
    init = run / 'isolation.gradle'
    literal = str(run).replace('\\', '/').replace("'", "\\'")
    init.write_text("gradle.projectsEvaluated { allprojects { project ->\n def minecraft = project.extensions.findByName('minecraft')\n if (minecraft != null) { minecraft.runs.configureEach { workingDirectory new File('" + literal + "', name) } }\n project.tasks.withType(JavaExec).configureEach { workingDir new File('" + literal + "'); doFirst { workingDir.mkdirs() } }\n} }\n", encoding='utf-8')
    command = [str(gradle), '--no-daemon', '--console=plain', '--project-cache-dir', str(run / 'gradle-project-cache'), '-I', str(init), '-PcoreSource=' + str(project.parent / 'core/src/main/java')]
    if offline:
        command += ['--offline', '-Dnet.minecraftforge.gradle.check.certs=false']
    if os.name == 'nt' and gradle.suffix.lower() in ('.bat', '.cmd'):
        command = [os.environ.get('COMSPEC', 'cmd.exe'), '/d', '/c'] + command
    build = execute(command + ['build'], project, run / 'build.log', timeout, environment)
    if build['status'] == 'PASS':
        jars = [verify_jar(path, project) for path in (project / 'build/libs').glob('*.jar') if not path.stem.endswith(('-sources', '-javadoc'))]
        save(run / 'jars.json', jars)
        if not jars:
            raise GateError('Build exited zero but produced no JAR')
    if gametest and build['status'] == 'PASS':
        from run_smoke import empty_structure
        verification = run / 'verification'
        shutil.copytree(HOME / 'world-tests', verification / 'world-tests')
        overlay = verification / 'world-overlay.gradle'
        shutil.copyfile(HOME / 'world-overlay.gradle', overlay)
        template = run / 'test-resources/data/ft_biome_dungeon_generator/structures/integration_empty.nbt'
        template.parent.mkdir(parents=True, exist_ok=True)
        template.write_bytes(empty_structure())
        locked = {str(path.relative_to(run)): digest(path) for path in verification.rglob('*') if path.is_file()}
        locked[str(template.relative_to(run))] = digest(template)
        save(run / 'verification-lock.json', locked)
        extra = ['-I', str(overlay), '-PintegrationSourceRoot=' + str(verification), '-PintegrationRunRoot=' + str(run)]
        result = execute(command + extra + ['runGameTestServer'], project, run / 'gametest.log', timeout, environment)
        result['note'] = 'Process result only; not proof that world assertions ran/passed'
        save(run / 'gametest-process.json', result)
        build['gameTestProcess'] = result
        if result['status'] == 'PASS':
            text = (run / 'gametest.log').read_text(encoding='utf-8', errors='replace')
            observations = [line for line in text.splitlines() if line.startswith('INTEGRATION_ASSERT ')]
            evidence = dict(snapshotLockSha256=digest(project.parent / 'snapshot-lock.json'), verificationLockSha256=digest(run / 'verification-lock.json'), log=str(run / 'gametest.log'), logSha256=digest(run / 'gametest.log'), assertions=observations, jars=jars, worldAcceptance='PARTIAL' if observations else 'NOT_RUN')
            save(run / 'world-observations.json', evidence)
            build['worldObservations'] = evidence
            if observations:
                matrix = load(HOME / 'test-matrix.json')
                if any('player_conflict PASS' in line for line in observations):
                    for case in ('player_conflict', 'preflight'):
                        matrix['cases'][case].update(status='PASS', scope='Injected gold block; entire runtime operation set compared unchanged', evidence=[evidence])
                if any('room_readback_journal_reload PASS' in line for line in observations):
                    for case in ('readback_payload', 'restart'):
                        matrix['cases'][case].update(status='PARTIAL', scope='Single real room/static blocks and same-JVM journal reload only; entity payload/process restart NOT_RUN', evidence=[evidence])
                matrix.update(updatedAt=stamp(), worldAcceptance='PARTIAL')
                save(HOME / 'test-matrix.json', matrix)
    return build


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('action', choices=('probe', 'snapshot', 'build', 'gametest'))
    parser.add_argument('--gradle', type=Path)
    parser.add_argument('--java-home', type=Path)
    parser.add_argument('--timeout', type=int, default=900)
    parser.add_argument('--offline', action='store_true')
    args = parser.parse_args()
    checks = probe()
    save(HOME / 'dependency-status.json', dict(checkedAt=stamp(), components=checks))
    matrix = {key: dict(status='NOT_RUN', requirement=value, evidence=[]) for key, value in WORLD_CASES.items()}
    matrix_path = HOME / 'test-matrix.json'
    if not matrix_path.exists():
        save(matrix_path, dict(updatedAt=stamp(), worldAcceptance='NOT_RUN', cases=matrix))
    issues = [dict(id='DEPENDENCY_' + item['component'].upper(), severity='BLOCKER', status=item['status'], detail=item.get('reason')) for item in checks if item['status'] != 'READY']
    state = 'WAITING' if issues else 'READY_TO_SNAPSHOT'
    outcome = None
    try:
        if args.action != 'probe':
            frozen = snapshot(checks, HOME / 'staging' / datetime.datetime.now().strftime('%Y%m%d_%H%M%S_%f'))
            state = 'SNAPSHOT_VERIFIED'
            project = assemble(frozen)
            state = 'ASSEMBLED_NOT_BUILT'
            if args.action in ('build', 'gametest'):
                if not args.gradle or not args.java_home:
                    raise GateError('--gradle and --java-home required')
                outcome = run_build(project, args.gradle.resolve(), args.java_home.resolve(), args.timeout, args.action == 'gametest', args.offline)
                state = 'BUILT_WORLD_NOT_VERIFIED' if outcome['status'] == 'PASS' else 'BUILD_FAILED'
                if outcome['status'] != 'PASS':
                    issues.append(dict(id='BUILD_PROCESS', severity='BLOCKER', detail=outcome))
                if outcome.get('gameTestProcess', {}).get('status') in ('FAIL', 'TIMEOUT'):
                    state = 'BUILT_WORLD_LAUNCH_FAILED'
                    issues.append(dict(id='GAMETEST_PROCESS', severity='BLOCKER', detail=outcome['gameTestProcess']))
    except (GateError, OSError, ValueError) as error:
        issues.append(dict(id='INTEGRATION_GATE', severity='BLOCKER', detail=str(error)))
        state = 'WAITING' if str(error).startswith('WAITING:') else 'REJECTED'
    save(HOME / 'ISSUES.json', dict(updatedAt=stamp(), issues=issues))
    acceptance = load(HOME / 'test-matrix.json')['worldAcceptance']
    save(HOME / 'progress.json', dict(component='integration', apiVersion=1, updatedAt=stamp(), status=state, worldAcceptance=acceptance, build=outcome, dependencies=checks))
    print(json.dumps(dict(status=state, issues=issues, build=outcome), ensure_ascii=False, indent=2))
    return 2 if issues or state == 'BUILD_FAILED' else 0


if __name__ == '__main__':
    sys.exit(main())
