import hashlib
import json
from pathlib import Path
import subprocess
import zipfile

ROOT = Path(__file__).resolve().parents[3]
WORK = ROOT / 'work/session-boss'
JAVA = Path('C:/Program Files/Zulu/zulu-21/bin')

def save(path, value):
    with path.open('x', encoding='utf-8') as stream:
        json.dump(value, stream, ensure_ascii=False, indent=2)

def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

base = WORK / 'releases/boss-core-stage04-status-immunity-20261112/ftboss-boss-core-1.21.1-stage04.jar'
deployed = json.loads((base.parent / 'deployment-receipt.json').read_text(encoding='utf-8'))
assert digest(base) == deployed['deployedSha256']
context = json.loads((WORK / 'build/boss-status-immunity-v1/compile-context.json').read_text(encoding='utf-8'))
output = WORK / 'build/boss-status-removal-v1'
assert not output.exists()
output.mkdir()
classes = output / 'classes'
classes.mkdir()
sdk = []
for entry in context['classpath'].split(';'):
    path = Path(entry)
    if path.suffix.lower() != '.jar':
        continue
    with zipfile.ZipFile(path) as archive:
        if 'net/farmingtales/ftboss/FTBossMod.class' in archive.namelist():
            continue
    sdk.append(str(path))
classpath = str(base) + ';' + ';'.join(sdk)
source = WORK / 'boss-core/src/main/java/net/farmingtales/ftboss/effect/HealBanEffect.java'
command = [str(JAVA / 'javac.exe'), '-proc:none', '--release', '21', '-encoding', 'UTF-8', '-cp', classpath, '-d', str(classes), str(source)]
with (output / 'compile.log').open('wb') as stream:
    compiled = subprocess.run(command, cwd=ROOT, stdout=stream, stderr=subprocess.STDOUT)
save(output / 'compile-context.json', {'exitCode': compiled.returncode, 'classpath': classpath, 'source': str(source), 'sourceSha256': digest(source), 'output': str(classes), 'baseJar': str(base), 'baseJarSha256': digest(base), 'stage03MaterialChangesIncluded': False, 'weaponCandidateCompiledOrValidated': False})
assert compiled.returncode == 0, str(output / 'compile.log')
proof = WORK / 'tests/StatusRemovalBoundaryProof.java'
proof_classes = output / 'proof-classes'
proof_classes.mkdir()
with (output / 'proof-compile.log').open('wb') as stream:
    compiled = subprocess.run([str(JAVA / 'javac.exe'), '-proc:none', '--release', '21', '-encoding', 'UTF-8', '-cp', classpath, '-d', str(proof_classes), str(proof)], cwd=ROOT, stdout=stream, stderr=subprocess.STDOUT)
save(output / 'proof-compile-terminal.json', {'exitCode': compiled.returncode, 'sourceSha256': digest(proof)})
assert compiled.returncode == 0, str(output / 'proof-compile.log')
with (output / 'proof-run.log').open('wb') as stream:
    verified = subprocess.run([str(JAVA / 'java.exe'), '-Xmx512m', '-cp', str(proof_classes) + ';' + classpath, 'StatusRemovalBoundaryProof', str(base), str(ROOT / 'reference/forge-current.jar'), str(classes), str(output / 'original-classes')], cwd=ROOT, stdout=stream, stderr=subprocess.STDOUT)
save(output / 'proof-terminal.json', {'proofExitCode': verified.returncode, 'proofOutput': (output / 'proof-run.log').read_text(encoding='utf-8', errors='replace')})
assert verified.returncode == 0, str(output / 'proof-run.log')
overlay = {path.relative_to(classes).as_posix(): path for path in classes.rglob('*.class')}
assert set(overlay) == {'net/farmingtales/ftboss/effect/HealBanEffect.class', 'net/farmingtales/ftboss/effect/ShadowErosionEffect.class'}
candidate = output / 'ftboss-boss-core-1.21.1-stage05-status-removal-CANDIDATE.jar'
with zipfile.ZipFile(base) as original, zipfile.ZipFile(candidate, 'x', compression=zipfile.ZIP_DEFLATED) as archive:
    for entry in original.infolist():
        archive.writestr(entry, overlay[entry.filename].read_bytes() if entry.filename in overlay else original.read(entry.filename))
with zipfile.ZipFile(base) as original, zipfile.ZipFile(candidate) as archive:
    assert archive.testzip() is None
    assert set(archive.namelist()) == set(original.namelist())
    changed = sorted(name for name in original.namelist() if original.read(name) != archive.read(name))
    assert set(changed) == set(overlay)
    class_count = sum(name.endswith('.class') for name in archive.namelist())
proposal = str(WORK / 'build/boss-split-9339206b/boss-core-proposal')
save(output / 'baseline-build-receipt.json', {'jar': str(base), 'jarSha256': digest(base), 'proposal': proposal, 'mode': 'baseline', 'deployed': True})
result = {'jar': str(candidate), 'jarSha256': digest(candidate), 'proposal': proposal, 'mode': 'candidate', 'baseJar': str(base), 'baseJarSha256': digest(base), 'changedClasses': changed, 'classCount': class_count, 'allOtherClassAndResourceBytesPreserved': True, 'stage03ChangesIncluded': False, 'weaponFunctionalDevelopmentPaused': True, 'fullBossInstallable': False, 'deployed': False}
save(output / 'candidate-build-receipt.json', result)
print(json.dumps({'compileExitCode': 0, 'proofExitCode': 0, 'changedClasses': changed, 'classCount': class_count, 'sha256': result['jarSha256']}))
print((output / 'proof-run.log').read_text(encoding='utf-8'))
