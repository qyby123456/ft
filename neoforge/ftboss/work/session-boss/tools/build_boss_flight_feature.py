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

def execute(command, log, receipt):
    with log.open('xb') as stream:
        result = subprocess.run(command, cwd=ROOT, stdout=stream, stderr=subprocess.STDOUT)
    save(receipt, {'exitCode': result.returncode, 'command': command, 'log': str(log), 'logSha256': digest(log)})
    assert result.returncode == 0, str(log)

base = WORK / 'releases/boss-core-stage05-status-removal-20261112/ftboss-boss-core-1.21.1-stage05.jar'
assert digest(base) == '4f89f69fbc9c90f1b7cd8a29f463568448442c60085538944de199b730fa0016'
original = ROOT / 'reference/forge-current.jar'
context_path = WORK / 'build/boss-flight-ban-v1/compile-context.json'
context = json.loads(context_path.read_text(encoding='utf-8'))
baseline_path = WORK / 'reports/flight-ban-baseline-d87d1aa2.json'
baseline = json.loads(baseline_path.read_text(encoding='utf-8'))
assert baseline['baselineGapObserved'] and baseline['probe']['failures'] == 0
assert baseline['probe']['actualNativePlayerTickBothPhasesAccepted']
assert baseline['terminal']['exitCode'] == 0 and baseline['terminal']['completeSave'] and baseline['terminal']['inputHashesUnchanged']
output = WORK / 'build/boss-flight-ban-original-v1'
output.mkdir()
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
source = WORK / 'boss-core/src/main/java/net/farmingtales/ftboss/boss/ai/FullFightFlightBan.java'
proof = WORK / 'tests/FlightBanBoundaryProof.java'
inputs = [base, original, context_path, baseline_path, source, proof, Path(__file__)]
before = {str(path): digest(path) for path in inputs}
source_classes = output / 'api-link-classes'
source_classes.mkdir()
execute([str(JAVA / 'javac.exe'), '-proc:none', '-implicit:none', '--release', '21', '-encoding', 'UTF-8', '-cp', classpath, '-d', str(source_classes), str(source)], output / 'api-link-compile.log', output / 'api-link-terminal.json')
proof_classes = output / 'proof-classes'
proof_classes.mkdir()
execute([str(JAVA / 'javac.exe'), '-proc:none', '--release', '21', '-encoding', 'UTF-8', '-cp', classpath, '-d', str(proof_classes), str(proof)], output / 'proof-compile.log', output / 'proof-compile-terminal.json')
classes = output / 'classes'
execute([str(JAVA / 'java.exe'), '-Xmx512m', '-cp', str(proof_classes) + ';' + classpath, 'FlightBanBoundaryProof', str(original), str(classes)], output / 'proof-run.log', output / 'proof-terminal.json')
proof_text = (output / 'proof-run.log').read_text(encoding='utf-8', errors='replace')
assert 'failures=0;methodsBasicVerified=3' in proof_text
class_name = 'net/farmingtales/ftboss/boss/ai/FullFightFlightBan.class'
assert {path.relative_to(classes).as_posix() for path in classes.rglob('*.class')} == {class_name}
candidate = output / 'ftboss-boss-core-1.21.1-stage06-flight-guard-NOT-INSTALLABLE.jar'
with zipfile.ZipFile(base) as previous, zipfile.ZipFile(candidate, 'x', compression=zipfile.ZIP_DEFLATED) as archive:
    assert class_name not in previous.namelist()
    for entry in previous.infolist():
        archive.writestr(entry, previous.read(entry.filename))
    info = zipfile.ZipInfo(class_name, date_time=(2026, 11, 12, 0, 0, 0))
    info.compress_type = zipfile.ZIP_DEFLATED
    archive.writestr(info, (classes / class_name).read_bytes())
with zipfile.ZipFile(base) as previous, zipfile.ZipFile(candidate) as archive:
    assert archive.testzip() is None
    assert set(archive.namelist()) - set(previous.namelist()) == {class_name}
    assert not set(previous.namelist()) - set(archive.namelist())
    changed = [name for name in previous.namelist() if previous.read(name) != archive.read(name)]
    assert not changed
    assert len(archive.namelist()) == len(set(archive.namelist()))
    class_count = sum(name.endswith('.class') for name in archive.namelist())
assert all(digest(Path(path)) == value for path, value in before.items())
save(output / 'compile-context.json', {'classpath': classpath, 'inputHashes': before, 'allInputsUnchanged': True, 'sourceApiLinkCompilationOnly': True, 'productionClassDerivedFromFrozenOriginalBytecode': True, 'proof': proof_text, 'stage03MaterialChangesIncluded': False, 'weaponCandidateCompiledOrValidated': False})
result = {'jar': str(candidate), 'jarSha256': digest(candidate), 'proposal': str(WORK / 'build/boss-split-9339206b/boss-core-proposal'), 'mode': 'candidate', 'baseJar': str(base), 'baseJarSha256': digest(base), 'addedClasses': [class_name], 'changedExistingEntries': changed, 'classCount': class_count, 'allExistingClassAndResourceBytesPreserved': True, 'originalInstructionAndLowestPriorityProofPassed': True, 'methodsBasicVerified': 3, 'stage03ChangesIncluded': False, 'weaponFunctionalDevelopmentPaused': True, 'fullBossInstallable': False, 'featureRuntimeAccepted': False, 'deployed': False, 'inputHashes': before}
save(output / 'candidate-build-receipt.json', result)
print(json.dumps({key: result[key] for key in ['jar', 'jarSha256', 'addedClasses', 'changedExistingEntries', 'classCount', 'methodsBasicVerified', 'fullBossInstallable']}, ensure_ascii=False), flush=True)
