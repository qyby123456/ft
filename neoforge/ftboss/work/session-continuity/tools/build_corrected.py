import re
import shutil
import subprocess
import sys
import uuid
from pathlib import Path

WORK = Path(__file__).resolve().parents[1]
ROOT = WORK.parents[1]
HISTORY = Path('D:/ft工程文件/FTBoss-NeoForge-1.21.1-移植-20261022')
sys.path.insert(0, str(HISTORY / 'batches/230-native-workbench-mock-channel/tools'))
from launch230_unlock import digest, load, save
sys.path.insert(0, str(HISTORY / 'batches/263-native-recipe-menu-accounting/tools'))
from sdk_relocation263 import relocate_text

binding = WORK / 'reports/parent-current.json'
parent_receipt = load(binding)
parent = Path(parent_receipt['attempt'])
arguments = load(parent / 'complete-arguments316.json')
sources = [Path(value) for value in arguments if value.endswith('.java')]
attempt = WORK / 'build' / ('candidate-' + uuid.uuid4().hex[:8])
attempt.mkdir(parents=True)
classes = attempt / 'complete-classes'
shutil.copytree(parent / 'complete-classes', classes)
before = {path.relative_to(parent / 'complete-classes').as_posix(): digest(path) for path in (parent / 'complete-classes').rglob('*.class')}
for relative, expected in before.items():
    assert parent_receipt['inputHashes'][str(parent / 'complete-classes' / relative)] == expected, relative
targets = {next(path for path in sources if path.name == name): WORK / 'proposal-corrected/net/farmingtales/ftboss/item' / name
    for name in ['OwnerFieldExpectedAuthority.java', 'OwnerNativeSemanticAuthority.java', 'FoodWeaponOriginalSequence.java', 'NativeItemComponentObservation.java']}
classpath = str(classes) + ';' + relocate_text(arguments[arguments.index('-classpath') + 1])
javac = ['C:/Program Files/Zulu/zulu-21/bin/javac.exe', '-proc:none', '--release', '21', '-encoding', 'UTF-8']

def run(command, name):
    save(attempt / (name + '-command.json'), command)
    with (attempt / (name + '.log')).open('wb') as stream:
        result = subprocess.run(command, cwd=ROOT, stdout=stream, stderr=subprocess.STDOUT)
    save(attempt / (name + '-terminal.json'), {'exitCode': result.returncode})
    assert result.returncode == 0, str(attempt / (name + '.log'))

run(javac + ['-cp', classpath, '-d', str(classes), *map(str, targets.values())], 'compile')
updates = []
for name in ['NativeStackFieldRequirementProof172.java', 'OwnerFieldInstallationProof.java', 'NativeCapabilityInstallationProof170.java']:
    old = next(path for path in sources if path.name == name)
    text = old.read_text(encoding='utf-8')
    replacements = {}
    for relative, old_hash in before.items():
        actual = digest(classes / relative)
        if actual != old_hash and old_hash in text:
            replacements[old_hash] = actual
            text = text.replace(old_hash, actual)
    if not replacements:
        continue
    target = attempt / 'bindings/net/farmingtales/ftboss/item' / name
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(text, encoding='utf-8')
    run(javac + ['-cp', classpath, '-d', str(classes), str(target)], name)
    targets[old] = target
    updates.append({'source': str(target), 'beforeSource': str(old), 'hashReplacements': replacements})
graph = {path.relative_to(classes).as_posix(): digest(path) for path in classes.rglob('*.class')}
assert set(graph) == set(before) and len(graph) == 1381
effective = [targets.get(path, path) for path in sources]
changed = {old_hash: graph[name] for name, old_hash in before.items() if graph[name] != old_hash}
stale = [{'source': str(source), 'oldHash': old_hash} for source in effective
    for old_hash in changed if old_hash in source.read_text(encoding='utf-8')]
assert not stale, stale
args_path = attempt / 'complete-arguments316.json'
save(args_path, arguments[:arguments.index('-d')] + ['-d', str(classes)] + list(map(str, effective)))
proof = next(source for source in effective if source.name == 'NativeCapabilityInstallationProof170.java')
pins = dict(re.findall(r'Map.entry\("([^"\n]+)",\s*"([0-9a-f]{64})"\)', proof.read_text(encoding='utf-8')))
assert len(pins) == 208
for name, expected in pins.items():
    prefix = 'net/farmingtales/ftboss/' if name.startswith(('mixin/', 'util/', 'transaction/')) else 'net/farmingtales/ftboss/item/'
    assert digest(classes / (prefix + name + '.class')) == expected, name
related = attempt / 'related-classes'
for relative, actual in graph.items():
    if actual != before[relative]:
        target = related / relative
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copyfile(classes / relative, target)
checks = attempt / 'checks'
checks.mkdir()
asm_source = HISTORY / 'batches/382-native-private-read-parity/tests/Asm382.java'
run(javac + ['-cp', classpath, '-d', str(checks), str(asm_source)], 'asm-compile')
run(['C:/Program Files/Zulu/zulu-21/bin/java.exe', '-Xverify:all', '-cp', str(checks) + ';' + classpath,
    'Asm382', str(related), str(attempt / 'asm.json')], 'asm')
hashes = {str(path): digest(path) for path in [binding, Path(__file__), args_path, asm_source, attempt / 'asm.json', *targets.values()]}
hashes.update({str(classes / name): value for name, value in graph.items()})
save(WORK / 'reports/candidate-corrected.json', {
    'attempt': str(attempt), 'failures': 0, 'classCount': len(graph), 'effectiveSourceCount': len(effective), 'guardCount': len(pins),
    'behavior': 'Advance independently expected player and carrier fields only after a verified native step returns successfully; preserve caller, generation and original-root binding.',
    'changedRawClasses': sorted(name for name in graph if graph[name] != before[name]), 'newClasses': [],
    'authorityBindingUpdates': updates, 'staleEffectiveSourceHashReferences': stale,
    'runtimeAccepted': False, 'positiveSignedSessionAccepted': False, 'installable': False, 'inputHashes': hashes})
print({'attempt': str(attempt), 'changedClasses': len(changed), 'newGuards': 0, 'asm': load(attempt / 'asm.json')}, flush=True)
