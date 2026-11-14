import hashlib
import json
import subprocess
import zipfile
from pathlib import Path
ROOT = Path(__file__).resolve().parents[3]
WORK = ROOT / 'work/session-boss'
OUT = WORK / 'build/boss-manor-damage-context-v1-20261114'
JAVA = Path('C:/Program Files/Zulu/zulu-21/bin')
def load(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))
def sha(path):
    with path.open('rb') as stream:
        return hashlib.file_digest(stream, 'sha256').hexdigest()
def save(name, value):
    with (OUT / name).open('x', encoding='utf-8') as stream:
        json.dump(value, stream, ensure_ascii=False, indent=2)
def run(name, command):
    save(name + '-command.json', command)
    result = subprocess.run(command, capture_output=True, timeout=90, creationflags=subprocess.CREATE_NO_WINDOW)
    (OUT / (name + '.log')).write_bytes(result.stdout + result.stderr)
    save(name + '-terminal.json', {'exitCode': result.returncode})
    assert result.returncode == 0, str(OUT / (name + '.log'))
    return result.stdout.decode('utf-8', errors='replace')
owner = load(ROOT / 'WRITE_OWNER.json')
assert owner['activeWriterThreadId'] == load(ROOT / 'WRITE_OWNER.claim')['threadId'] == '01a1c43e-ae80-7390-b604-b43b4ab53f2f'
assert owner['weaponValidationPaused'] and owner['noFurtherFormalWritesOrDeployment'] and not owner['activeRuntimePids']
baseline_path = WORK / 'reports/sparrow-evasion-native-baseline-v2-39125bde.json'
baseline = load(baseline_path)
assert not baseline['actualPositiveHitAndNaturalBaselineAccepted'] and 'EmptyStackException' in baseline['probe']['failure'] and baseline['terminal']['processConfirmedGone']
built_path = WORK / 'build/boss-zicai-curio-silence-v1-20261113/candidate-build-receipt.json'
base = Path(load(built_path)['jar'])
assert sha(base) == 'c45cc10ffb7b572d876e869b692a75496c88c337434ed7140225df298b7bb65e'
assert not OUT.exists()
OUT.mkdir()
classes = OUT / 'classes'
classes.mkdir()
proof_classes = OUT / 'proof-classes'
proof_classes.mkdir()
context = WORK / 'build/boss-flight-ban-v1/compile-context.json'
sdk = []
for entry in load(context)['classpath'].split(';'):
    path = Path(entry)
    if path.suffix.lower() == '.jar':
        with zipfile.ZipFile(path) as bundle:
            if 'net/farmingtales/ftboss/FTBossMod.class' in bundle.namelist():
                continue
    sdk.append(entry)
classpath = str(base) + ';' + ';'.join(sdk)
source = WORK / 'src/manor-damage-context-v1/net/farmingtales/ftboss/boss/ai/NativeManorFinalDamageInstaller.java'
probe = WORK / 'tests/ManorDamageContextWrapperProofV1.java'
run('compile-installer', [str(JAVA / 'javac.exe'), '-proc:none', '--release', '21', '-encoding', 'UTF-8', '-cp', classpath, '-d', str(classes), str(source)])
run('compile-proof', [str(JAVA / 'javac.exe'), '-proc:none', '--release', '21', '-encoding', 'UTF-8', '-cp', str(classes) + ';' + classpath, '-d', str(proof_classes), str(probe)])
proof = run('owned-context-proof', [str(JAVA / 'java.exe'), '-Xmx256m', '-cp', str(proof_classes) + ';' + str(classes) + ';' + classpath, 'ManorDamageContextWrapperProofV1'])
assert proof.startswith('failures=0;') and 'negativeControls=4;' in proof
changes = {path.relative_to(classes).as_posix(): path.read_bytes() for path in classes.rglob('*.class')}
assert set(changes) == {'net/farmingtales/ftboss/boss/ai/NativeManorFinalDamageInstaller.class'}
jar = OUT / 'ftboss-boss-core-1.21.1-stage29-manor-damage-context-NOT-INSTALLABLE.jar'
with zipfile.ZipFile(base) as before, zipfile.ZipFile(jar, 'x', compression=zipfile.ZIP_DEFLATED) as after:
    for info in before.infolist():
        after.writestr(info, changes.get(info.filename, before.read(info.filename)))
with zipfile.ZipFile(base) as before, zipfile.ZipFile(jar) as after:
    assert before.namelist() == after.namelist() and after.testzip() is None
    changed = [name for name in before.namelist() if before.read(name) != after.read(name)]
    assert changed == list(changes)
inputs = {str(path): sha(path) for path in [Path(__file__), source, probe, context, built_path, base, baseline_path]}
save('candidate-build-receipt.json', {'jar': str(jar), 'jarSha256': sha(jar), 'baseJar': str(base), 'baseJarSha256': sha(base), 'changedEntries': changed, 'addedEntries': [], 'allOtherEntriesByteIdentical': True, 'originalSettlementAndArithmeticUnchanged': True, 'proof': proof.strip(), 'inputHashes': inputs, 'nativeAcceptanceRequired': True, 'candidateNativeAccepted': False, 'scope': 'Own a balanced per-invocation NeoForge DamageContainer around the two original Manor reduction calls; preserve normal and exceptional caller context and original damage order.', 'modelAndMusicUploadProhibited': True, 'installable': False, 'deployed': False, 'fullBossMigrationComplete': False})
print(json.dumps({'jarSha256': sha(jar), 'changedClasses': len(changed), 'proof': proof.strip(), 'nativeAcceptanceRequired': True}), flush=True)
