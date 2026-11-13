import hashlib
import json
import re
import shutil
import subprocess
import sys
import uuid
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]
WORK = Path(__file__).resolve().parents[1]
HISTORY = Path('D:/ft工程文件/FTBoss-NeoForge-1.21.1-移植-20261022')
sys.path.insert(0, str(HISTORY / 'batches/263-native-recipe-menu-accounting/tools'))
from sdk_relocation263 import relocate_text


def load(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def save(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


owner = load(ROOT / 'WRITE_OWNER.json')
assert owner['activeWriterThreadId'] == load(ROOT / 'WRITE_OWNER.claim')['threadId'] == '01a1c43e-ae80-7390-b604-b43b4ab53f2f'
receipt_path = WORK / 'reports/native-repair-private-timer-image-candidate-acf29373.json'
receipt = load(receipt_path)
parent = Path(receipt['attempt'])
arguments = load(parent / 'complete-arguments316.json')
sources = [Path(value) for value in arguments if value.endswith('.java')]
attempt = WORK / 'build' / ('native-repair-' + uuid.uuid4().hex[:8])
attempt.mkdir(parents=True)
classes = attempt / 'complete-classes'
shutil.copytree(parent / 'complete-classes', classes)
staged = attempt / 'compiled-changes'
staged.mkdir()
replacements = {}
for target in (WORK / 'proposal-private-fingerprint-composition').rglob('*.java'):
    original = next(source for source in sources if source.name == target.name)
    frozen = attempt / 'sources' / target.relative_to(WORK / 'proposal-private-fingerprint-composition')
    frozen.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(target, frozen)
    replacements[original] = frozen
classpath = str(staged) + ';' + str(classes) + ';' + relocate_text(arguments[arguments.index('-classpath') + 1])
javac = ['C:/Program Files/Zulu/zulu-21/bin/javac.exe', '-proc:none', '--release', '21', '-encoding', 'UTF-8', '-cp', classpath, '-d', str(staged)]


def run(command, name):
    save(attempt / (name + '-command.json'), command)
    with (attempt / (name + '.log')).open('wb') as output:
        result = subprocess.run(command, cwd=ROOT, stdout=output, stderr=subprocess.STDOUT)
    save(attempt / (name + '-terminal.json'), {'exitCode': result.returncode})
    assert result.returncode == 0, str(attempt / (name + '.log'))


run(javac + list(map(str, replacements.values())), 'compile')
before = {Path(name).relative_to(parent / 'complete-classes').as_posix(): expected
          for name, expected in receipt['inputHashes'].items() if Path(name).is_relative_to(parent / 'complete-classes')}
assert len(before) == receipt['classCount']
changed = {}
for source in staged.rglob('*.class'):
    relative = source.relative_to(staged).as_posix()
    if before.get(relative) != digest(source):
        changed[relative] = digest(source)
assert not (set(changed) - set(before)), 'Unexpected new class; inspect before continuing'
updates = []
for name in ['NativeStackFieldRequirementProof172.java', 'OwnerFieldInstallationProof.java', 'NativeCapabilityInstallationProof170.java']:
    original = next(source for source in sources if source.name == name)
    text = original.read_text(encoding='utf-8-sig')
    pins = {}
    for relative, actual in changed.items():
        if before[relative] in text:
            pins[before[relative]] = actual
            text = text.replace(before[relative], actual)
    if not pins:
        continue
    target = attempt / 'bindings/net/farmingtales/ftboss/item' / name
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(text, encoding='utf-8')
    run(javac + [str(target)], name)
    for compiled_source in staged.rglob('*.class'):
        relative = compiled_source.relative_to(staged).as_posix()
        compiled_digest = digest(compiled_source)
        if before.get(relative) != compiled_digest:
            changed[relative] = compiled_digest
    replacements[original] = target
    updates.append({'source': str(target), 'beforeSource': str(original), 'hashReplacements': pins})
for source in staged.rglob('*.class'):
    relative = source.relative_to(staged).as_posix()
    if before.get(relative) != digest(source):
        changed[relative] = digest(source)
    target = classes / relative
    target.parent.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(source, target)
effective = [replacements.get(source, source) for source in sources]
stale = [{'source': str(source), 'oldHash': before[relative]} for source in effective for relative in changed
         if before[relative] in source.read_text(encoding='utf-8-sig')]
assert not stale, stale
args_path = attempt / 'complete-arguments316.json'
save(args_path, arguments[:arguments.index('-d')] + ['-d', str(classes)] + list(map(str, effective)))
checks = attempt / 'checks'
checks.mkdir()
asm_source = HISTORY / 'batches/382-native-private-read-parity/tests/Asm382.java'
run(javac[:javac.index('-d')] + ['-d', str(checks), str(asm_source)], 'asm-compile')
run(['C:/Program Files/Zulu/zulu-21/bin/java.exe', '-Xverify:all', '-cp', str(checks) + ';' + classpath,
     'Asm382', str(staged), str(attempt / 'asm.json')], 'asm')
assert load(attempt / 'asm.json')['failures'] == 0
hashes = {str(classes / relative): changed.get(relative, expected) for relative, expected in before.items()}
for path in [receipt_path, args_path, Path(__file__), asm_source, attempt / 'asm.json', *replacements.values()]:
    hashes[str(path)] = digest(path)
report = {
    'attempt': str(attempt), 'failures': 0, 'classCount': len(before), 'effectiveSourceCount': len(effective),
    'guardCount': receipt['guardCount'], 'changedRawClasses': sorted(changed), 'newClasses': [],
    'authorityBindingUpdates': updates, 'staleEffectiveSourceHashReferences': stale,
    'nativeSignedRepairImplementation': True, 'runtimeAccepted': False, 'nonNullLeaseAccepted': False,
    'repairPaymentAccepted': False, 'positiveSignedSessionAccepted': False, 'installable': False, 'deployed': False,
    'reusedParentFrozenInputs': str(receipt_path), 'inputHashes': hashes,
}
save(WORK / 'reports/native-repair-private-fingerprint-composition-candidate.json', report)
print(json.dumps({'attempt': str(attempt), 'changedClasses': len(changed), 'asm': load(attempt / 'asm.json'), 'runtimeAccepted': False}))

