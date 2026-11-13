import datetime
import os
from pathlib import Path
import shutil
import sys
import zipfile

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import integrate

HOME = Path(__file__).resolve().parent
EXPECTED = 'f7d73680b20d390a6f08a090fea66b66000deb33bc0a6556ad282340035c6188'


def freeze(source, target, component):
    marker = source / 'ready.json'
    marker_hash = integrate.digest(marker)
    ready = integrate.load(marker)
    if ready.get('component') != component or ready.get('ready') is not True or ready.get('apiVersion') != 1:
        raise integrate.GateError('Explicit v2 component readiness mismatch')
    if component == 'runtime-next':
        files = ready['files']
    else:
        inventory = source / 'SHA256SUMS.json'
        if integrate.digest(inventory) != ready['inventorySha256']:
            raise integrate.GateError('Expansion inventory hash mismatch')
        files = integrate.inventory(integrate.load(inventory))
        files['SHA256SUMS.json'] = integrate.digest(inventory)
    for relative, expected in files.items():
        original = integrate.contained(source, relative)
        destination = integrate.contained(target, relative)
        if integrate.digest(original) != expected:
            raise integrate.GateError('Changed frozen source: ' + relative)
        destination.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(original, destination)
        if integrate.digest(destination) != expected or integrate.digest(original) != expected:
            raise integrate.GateError('Snapshot hash race: ' + relative)
    if integrate.digest(marker) != marker_hash:
        raise integrate.GateError('Ready marker race')
    shutil.copyfile(marker, target / 'ready.json')
    return dict(component=component, readySha256=marker_hash, files=files, completeGeneratorReady=False, productionPromotion=False)


def main():
    HOME.mkdir(exist_ok=True)
    seal = HOME / 'v1-seal.json'
    if not seal.exists():
        checked = integrate.check_component(integrate.HOME, 'integration')
        seal_data = dict(createdAt=integrate.stamp(), status='VERIFIED_BEFORE_V2', readySha256=integrate.digest(integrate.HOME / 'ready.json'), files=checked['files'], noV1WorldWillBeReused=True)
        integrate.save(seal, seal_data)
        shutil.copyfile(integrate.HOME / 'ready.json', HOME / 'v1-ready-sealed.json')
    name = datetime.datetime.now().strftime('%Y%m%d_%H%M%S')
    staging = HOME / 'staging' / name
    checks = [freeze(integrate.ROOT / 'runtime/next', staging / 'runtime', 'runtime-next'), freeze(integrate.ROOT / 'catalog/expansion-v2', staging / 'catalog', 'catalog-expansion-v2')]
    integrate.save(staging / 'snapshot-lock.json', dict(createdAt=integrate.stamp(), components=checks))
    project = staging / 'runtime'
    ready = integrate.load(project / 'ready.json')
    jar = project / ready['artifact']
    if integrate.digest(jar) != EXPECTED or ready['artifactSha256'] != EXPECTED:
        raise integrate.GateError('Wrong requested experimental artifact')
    with zipfile.ZipFile(jar) as archive:
        if archive.testzip() is not None:
            raise integrate.GateError('JAR CRC failed')
        manifest = __import__('json').loads(archive.read('data/biomedungeon/manifest.json'))
        ids = [entry['id'] for entry in manifest['templates']]
        if set(ids) != {'s8_05', 's8_06'}:
            raise integrate.GateError('Unexpected production pool')
        forbidden = [entry for entry in archive.namelist() if 'payloadtest' in entry or 'GameTests' in entry or 'CandidateFixtures' in entry]
        if forbidden:
            raise integrate.GateError('Fixture leaked into experimental installed JAR')
    run = HOME / 'run' / name
    run.mkdir(parents=True)
    verify = run / 'verification'
    shutil.copytree(HOME / 'world-tests', verify / 'world-tests')
    shutil.copyfile(HOME / 'overlay.gradle', verify / 'overlay.gradle')
    integrate.save(run / 'verification-lock.json', {str(path.relative_to(run)): integrate.digest(path) for path in verify.rglob('*') if path.is_file()})
    environment = os.environ.copy()
    environment['JAVA_HOME'] = str(Path(os.environ['APPDATA']) / '.minecraft/runtime/java-runtime-gamma-snapshot')
    gradle = Path(os.environ['USERPROFILE']) / '.gradle/wrapper/dists/gradle-8.8-bin/dl7vupf4psengwqhwktix4v1/gradle-8.8/bin/gradle.bat'
    command = [os.environ['COMSPEC'], '/d', '/c', str(gradle), '--no-daemon', '--offline', '-Dnet.minecraftforge.gradle.check.certs=false', '--console=plain', '--project-cache-dir', str(run / 'gradle-cache'), '-I', str(verify / 'overlay.gradle'), '-PintegrationV2Root=' + str(verify), '-PintegrationV2Run=' + str(run)]
    result = integrate.execute(command + ['-PpayloadTest=true', 'runGameTestServer'], project, run / 'payload.log', 1200, environment)
    results = dict(status=result['status'], snapshot=str(staging), run=str(run), artifactSha256=EXPECTED, installedProductionPool=ids, capabilityFixturesAreNotAccepted=True, payload=result)
    integrate.save(HOME / 'progress.json', results)
    for stage in ('prepare', 'resume'):
        if result['status'] != 'PASS':
            break
        result = integrate.execute(command + ['-PbeRestartStage=' + stage, 'runGameTestServer'], project, run / ('be-restart-' + stage + '.log'), 1200, environment)
        results['restart_' + stage] = result
        integrate.save(HOME / 'progress.json', results)
    print(results)


if __name__ == '__main__':
    main()
