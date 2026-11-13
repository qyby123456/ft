import hashlib
import json
import re
import struct
import subprocess
import zipfile
from pathlib import Path

work = Path(__file__).resolve().parents[1]
root = work.parents[1]
hashes = {}
def digest(path):
    path = Path(path)
    value = hashlib.sha256(path.read_bytes()).hexdigest()
    hashes[str(path)] = value
    return value

def load(path):
    digest(path)
    return json.loads(path.read_text(encoding='utf-8'))

parent = work / 'reports/native-remaining-original-implementation-static-audit-41d445cf.json'
previous = load(parent)
assert previous['failures'] == 0 and previous['registeredItems'] == 66
command_path = work / 'build/remaining-original-implementations-static-qualified-41d445cf/javap-command.json'
command = load(command_path)
assert digest(command_path) == previous['inputHashes'][str(command_path)]
roots = [Path(value) for value in command[command.index('-classpath') + 1].split(';')]
archives = {}
def class_bytes(name):
    relative = name.replace('.', '/') + '.class'
    for entry in roots:
        if entry.is_dir():
            path = entry / relative
            if path.is_file():
                return path.read_bytes(), str(path), relative
        elif entry.is_file():
            archive = archives.get(entry)
            if archive is None:
                archive = zipfile.ZipFile(entry)
                archives[entry] = archive
            try:
                return archive.read(relative), str(entry), relative
            except KeyError:
                pass
    raise AssertionError('Unresolved original superclass: ' + name)

def hierarchy(data):
    assert data[:4] == b'\xca\xfe\xba\xbe'
    count = struct.unpack_from('>H', data, 8)[0]
    pool = [None] * count
    offset = 10
    index = 1
    while index < count:
        tag = data[offset]
        offset += 1
        if tag == 1:
            length = struct.unpack_from('>H', data, offset)[0]
            offset += 2
            pool[index] = data[offset:offset + length].decode('utf-8', errors='replace')
            offset += length
        elif tag == 7:
            pool[index] = struct.unpack_from('>H', data, offset)[0]
            offset += 2
        elif tag in (3, 4, 9, 10, 11, 12, 17, 18):
            offset += 4
        elif tag in (5, 6):
            offset += 8
            index += 1
        elif tag in (8, 16, 19, 20):
            offset += 2
        elif tag == 15:
            offset += 3
        else:
            raise AssertionError('Unknown constant pool tag: ' + str(tag))
        index += 1
    flags, current, parent_index = struct.unpack_from('>HHH', data, offset)
    return pool[pool[current]].replace('/', '.'), None if parent_index == 0 else pool[pool[parent_index]].replace('/', '.')

records = {}
expected = {row['class']: row for row in previous['currentCandidateDiskClassEvidence']}
try:
    pending = list(expected)
    while pending:
        name = pending.pop()
        if name in records or name == 'java.lang.Object':
            continue
        data, container, relative = class_bytes(name)
        current, parent_name = hierarchy(data)
        assert current == name
        raw_hash = hashlib.sha256(data).hexdigest()
        if name in expected:
            assert raw_hash == expected[name]['classSha256'], name
        records[name] = {'class': name, 'superclass': parent_name, 'classSha256': raw_hash,
                         'container': container, 'archiveEntry': relative,
                         'originalConcreteIds': expected.get(name, {}).get('ids', [])}
        if parent_name and parent_name != 'java.lang.Object':
            pending.append(parent_name)
    for name in expected:
        chain = []
        current = name
        while current != 'java.lang.Object':
            assert current in records and current not in chain and len(chain) < 64
            chain.append(current)
            current = records[current]['superclass']
        records[name]['completeSuperclassChainToObject'] = chain + ['java.lang.Object']
    for container in sorted({row['container'] for row in records.values()}):
        digest(container)
finally:
    for archive in archives.values():
        archive.close()

attempt = work / 'build/remaining-original-superclass-closure-static-41d445cf'
attempt.mkdir(exist_ok=False)
new_classes = sorted(set(records) - set(expected))
javap = 'C:/Program Files/Zulu/zulu-21/bin/javap.exe'
digest(javap)
log = attempt / 'javap-additional-superclasses.txt'
with log.open('wb') as stream:
    completed = subprocess.run([javap, '-p', '-s', '-c', '-classpath', ';'.join(str(value) for value in roots), *new_classes], cwd=root, stdout=stream, stderr=subprocess.STDOUT)
assert completed.returncode == 0
log_hash = digest(log)
old_log = Path(previous['javapAllMethodsDescriptorsAndInstructions'])
assert digest(old_log) == previous['inputHashes'][str(old_log)]
all_text = old_log.read_bytes().decode('gb18030', errors='replace') + '\n' + log.read_bytes().decode('gb18030', errors='replace')
headers = list(re.finditer(r'^public (?:(?:abstract|final) )?class ([\w.$]+)(?: extends ([\w.$]+))?[^\n]*\{', all_text, re.M))
assert set(match.group(1) for match in headers) == set(records)
callback_names = {'hurtEnemy', 'postHurtEnemy', 'use', 'releaseUsing', 'onUseTick', 'finishUsingItem', 'useOn',
                  'inventoryTick', 'mineBlock', 'getUseDuration', 'getUseAnimation', 'isDamageable', 'getMaxDamage',
                  'getEnchantmentValue', 'isValidRepairItem', 'getCapability', 'initCapabilities', 'verifyComponentsAfterLoad'}
for index, match in enumerate(headers):
    section = all_text[match.end():headers[index + 1].start() if index + 1 < len(headers) else len(all_text)]
    declarations = []
    lines = section.splitlines()
    for line_index, line in enumerate(lines):
        if not re.match(r'^  (?:public|protected|private) .*\([^\n]*\)(?: throws [^;]+)?;$', line):
            continue
        method_name = line.split('(', 1)[0].rsplit(' ', 1)[-1]
        descriptor = lines[line_index + 1].strip() if line_index + 1 < len(lines) else ''
        assert descriptor.startswith('descriptor: ')
        declarations.append({'name': method_name, 'signature': line.strip(), 'descriptor': descriptor[len('descriptor: '):]})
    records[match.group(1)]['declaredSelectedOriginalCallbacks'] = [row for row in declarations if row['name'] in callback_names]
    records[match.group(1)]['allDeclaredMethodsCount'] = len(declarations)

digest(Path(__file__))
helper = work / 'tools/prepare_native_frost_ingress_runtime.py'
digest(helper)
namespace = {'__file__': str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0], str(helper), 'exec'), namespace)
output = work / 'reports/native-remaining-original-superclass-closure-static-audit-41d445cf.json'
namespace['add'](output.relative_to(root).as_posix(), json.dumps({
    'failures': 0, 'registeredItems': 66, 'concreteImplementations': len(expected),
    'completeDiskSuperclassClosureClasses': len(records), 'newlyInspectedSuperclassCount': len(new_classes),
    'newlyInspectedSuperclasses': new_classes, 'classRows': [records[name] for name in sorted(records)],
    'originalZeroDurabilityIdsMustStayZero': previous['originalZeroDurabilityIdsMustStayZero'],
    'selectedMethodListIsNotACompleteEffectOrProviderAnalysis': True,
    'diskClassEvidenceDoesNotEstablishMixinTransformedRuntimeImplementation': True,
    'actualProviderGetCapabilityOrInternalMutationAccepted': False,
    'nativePaidRepairOrSignedCombatAccepted': False, 'arbitrarySubclassAdmissionAllowed': False,
    'productChanged': False, 'authenticatedMultiplayerAccepted': False, 'installable': False,
    'inputHashes': hashes
}, ensure_ascii=False, indent=2) + '\n')
print(json.dumps({'report': output.name, 'concreteClasses': len(expected), 'closureClasses': len(records), 'additionalSuperclasses': len(new_classes), 'failures': 0, 'runtimeAcceptance': False}))
