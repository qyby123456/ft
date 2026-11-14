import hashlib
import json
from pathlib import Path
import subprocess
import zipfile

ROOT = Path(__file__).resolve().parents[3]
WORK = ROOT / 'work/session-boss'
JAVA = Path('C:/Program Files/Zulu/zulu-21/bin')

def digest(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()

def save(path, value):
    with Path(path).open('x', encoding='utf-8') as stream:
        json.dump(value, stream, ensure_ascii=False, indent=2)

def execute(command, log):
    with log.open('xb') as stream:
        result = subprocess.run(command, cwd=ROOT, stdout=stream, stderr=subprocess.STDOUT)
    save(log.with_suffix('.terminal.json'), {'exitCode': result.returncode, 'logSha256': digest(log)})
    assert result.returncode == 0, str(log)

baseline_path = WORK / 'reports/butcher-phase-baseline-7c62ac2e.json'
baseline = json.loads(baseline_path.read_text(encoding='utf-8'))
assert baseline['baselineGapObserved'] and baseline['probe']['failures'] == 0 and baseline['terminal']['completeSave']
base = WORK / 'releases/boss-core-stage06-flight-guard-20261112/ftboss-boss-core-1.21.1-stage06.jar'
assert digest(base) == 'e4dd3c8d4fff8a66b5eefeac6aa96eb24304cf716e9cd8c5b7424c4ae65c034f'
original = ROOT / 'reference/forge-current.jar'
context_path = WORK / 'build/boss-flight-ban-v1/compile-context.json'
context = json.loads(context_path.read_text(encoding='utf-8'))
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
output = WORK / 'build/boss-butcher-phase-lifecycle-v2'
output.mkdir()
compiled = output / 'api-link-classes'
compiled.mkdir()
srcroot = WORK / 'boss-core/src/main/java/net/farmingtales/ftboss'
sources = [srcroot / name for name in ['boss/ai/ButcherPhases.java', 'boss/ai/ButcherPhaseRules.java', 'entity/MeatMountainEntity.java', 'boss/ai/ButcherCombat.java', 'boss/MeatBoss.java']]
proof = WORK / 'tests/ButcherPhaseBoundaryProof.java'
inputs = [base, original, baseline_path, context_path, proof, Path(__file__), *sources, WORK / 'build/boss-butcher-phase-callsite-audit-20261112/stage07-source-mapping.json']
before = {str(path): digest(path) for path in inputs}
save(output / 'inputs-before.json', before)
execute([str(JAVA / 'javac.exe'), '-proc:none', '-implicit:none', '--release', '21', '-encoding', 'UTF-8', '-cp', classpath, '-d', str(compiled), *map(str,sources)], output / 'api-link-compile.log')
proof_classes = output / 'proof-classes'
proof_classes.mkdir()
execute([str(JAVA / 'javac.exe'), '-proc:none', '--release', '21', '-encoding', 'UTF-8', '-cp', classpath, '-d', str(proof_classes), str(proof)], output / 'proof-compile.log')
classes = output / 'classes'
execute([str(JAVA / 'java.exe'), '-Xmx512m', '-cp', str(proof_classes) + ';' + classpath, 'ButcherPhaseBoundaryProof', str(base), str(original), str(compiled), str(classes)], output / 'proof-run.log')
proof_text = (output / 'proof-run.log').read_text(encoding='utf-8')
assert 'failures=0;' in proof_text
overlay = {path.relative_to(classes).as_posix():path for path in classes.rglob('*.class')}
assert len(overlay) == 5
candidate = output / 'ftboss-boss-core-1.21.1-stage07-butcher-phase-NOT-INSTALLABLE.jar'
with zipfile.ZipFile(base) as previous, zipfile.ZipFile(candidate,'x',compression=zipfile.ZIP_DEFLATED) as archive:
    for entry in previous.infolist():
        archive.writestr(entry, overlay[entry.filename].read_bytes() if entry.filename in overlay else previous.read(entry.filename))
    for name,path in overlay.items():
        if name not in previous.namelist():
            archive.writestr(name,path.read_bytes())
with zipfile.ZipFile(base) as previous, zipfile.ZipFile(candidate) as archive:
    assert archive.testzip() is None
    added = sorted(set(archive.namelist())-set(previous.namelist()))
    changed = sorted(name for name in previous.namelist() if archive.read(name)!=previous.read(name))
    assert set(added)|set(changed) == set(overlay) and len(added)==2 and len(changed)==3
    assert not set(previous.namelist())-set(archive.namelist())
    class_count=sum(name.endswith('.class') for name in archive.namelist())
assert all(digest(name)==wanted for name,wanted in before.items())
save(output / 'compile-context.json', {'classpath':classpath,'inputHashes':before,'allInputsUnchanged':True,'productionMethodTransplantWhitelist':{'entity/MeatMountainEntity':['hurt','tick','tickBossBar','getButcherPhase','resetButcherCombat'],'boss/MeatBoss':['tick','tryPopTotem','onPhaseChanged','lambda$tick$0'],'boss/ai/ButcherCombat':['reset']},'proof':proof_text,'stage03MaterialChangesIncluded':False,'weaponCandidateCompiledOrValidated':False})
result={'jar':str(candidate),'jarSha256':digest(candidate),'proposal':str(WORK/'build/boss-split-9339206b/boss-core-proposal'),'baseJar':str(base),'baseJarSha256':digest(base),'addedClasses':added,'changedClasses':changed,'classCount':class_count,'allOtherClassAndResourceBytesPreserved':True,'originalPhaseRulesByteIdentical':True,'combatAttackProfileAndButcherSkillsDeferred':True,'fullBossInstallable':False,'deployed':False,'weaponFunctionalDevelopmentPaused':True,'inputHashes':before}
save(output/'candidate-build-receipt.json',result)
print(json.dumps({'jarSha256':result['jarSha256'],'addedClasses':added,'changedClasses':changed,'classCount':class_count,'proof':proof_text}),flush=True)
