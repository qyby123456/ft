import re
import subprocess
import sys
import uuid
from pathlib import Path

ROOT = Path('D:\\ft工程文件\\FTBoss-NeoForge-1.21.1-移植-20261022')
BATCH = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / 'batches/230-native-workbench-mock-channel/tools'))
from launch230_unlock import digest, load, save
sys.path.insert(0, str(ROOT / 'batches/263-native-recipe-menu-accounting/tools'))
from sdk_relocation263 import relocate_text
binding = BATCH / 'reports/actual-incremental411-experimental.json'
receipt = load(binding)
for name, expected in receipt['inputHashes'].items():
    assert digest(name) == expected, name
current = Path(receipt['attempt'])
prior = Path(load(Path('C:\\ft工程文件C\\FTBoss-NeoForge-1.21.1-移植-20261022\\batches\\408-native-composition-binding\\reports\\actual-incremental408-experimental.json'))['attempt'])
arguments = load(current / 'complete-arguments316.json')
classpath = str(current / 'complete-classes') + ';' + relocate_text(arguments[arguments.index('-classpath') + 1])
tests = BATCH / 'tests'
tests.mkdir(exist_ok=True)
output = current / ('checks411-' + uuid.uuid4().hex[:8])
output.mkdir()
pattern = r'Map.entry\("([^"\n]+)",\s*"([0-9a-f]{64})"\)'
old_source = Path('C:\\ft工程文件C\\FTBoss-NeoForge-1.21.1-移植-20261022\\batches\\408-native-composition-binding\\proposal\\net\\farmingtales\\ftboss\\item\\NativeCapabilityInstallationProof170.java')
new_source = BATCH / 'proposal/net/farmingtales/ftboss/item/NativeCapabilityInstallationProof170.java'
before = dict(re.findall(pattern, old_source.read_text(encoding='utf-8')))
after = dict(re.findall(pattern, new_source.read_text(encoding='utf-8')))
assert len(before) == 191 and len(after) == 193 and set(before).issubset(after)
added = sorted(set(after) - set(before))
assert added == ['WeaponAttachmentPolicy', 'WeaponSystemScope410']
changed = sorted(name for name in before if before[name] != after[name])
parent_guard = ROOT / 'batches/389-native-put-boundary-binding/tests/GuardTable389.java'
text = parent_guard.read_text(encoding='utf-8').replace('GuardTable389', 'GuardTable411')
text = text.replace('before.size() != 185', 'before.size() != 191')
text = text.replace('!before.keySet().equals(after.keySet())', '!after.keySet().containsAll(before.keySet()) || after.size() != 193')
old = '        if (!rebound.equals(List.of("OwnerFieldInstallationProof"))) throw new AssertionError("Unexpected binding change");'
assert text.count(old) == 1
text = text.replace(old, '        if (!rebound.equals(List.of(' + ','.join('"' + name + '"' for name in changed) + '))) throw new AssertionError("Unexpected binding change");')
text = text.replace('rejected.size() != 370', 'rejected.size() != 374').replace('actual guards=185 rejected=370', 'actual guards=187 rejected=374')
text = text.replace('name.startsWith("mixin/")', '(name.startsWith("mixin/") || name.startsWith("util/"))')
text = text.replace('rejected.size() != 374', 'rejected.size() != 386').replace('actual guards=187 rejected=374', 'actual guards=193 rejected=386')
(tests / 'GuardTable411.java').write_text(text, encoding='utf-8')
for original, name in [('batches/382-native-private-read-parity/tests/Asm382.java', 'Asm411'),
        ('batches/360-native-guard-semantic-audit/tests/GuardSemantic360.java', 'Semantic411')]:
    (tests / (name + '.java')).write_text((ROOT / original).read_text(encoding='utf-8').replace(Path(original).stem, name), encoding='utf-8')


def run(command, name):
    save(output / (name + '-command.json'), command)
    with (output / (name + '.log')).open('wb') as stream:
        terminal = subprocess.run(command, cwd=BATCH, stdout=stream, stderr=subprocess.STDOUT)
    save(output / (name + '-terminal.json'), {'exitCode': terminal.returncode})
    assert terminal.returncode == 0, str(output / (name + '.log'))


javac = ['C:/Program Files/Zulu/zulu-21/bin/javac.exe', '-proc:none', '--release', '21', '-encoding', 'UTF-8', '-cp', classpath]
run(javac + ['-d', str(output), *map(str, tests.glob('*.java'))], 'compile-tests')
java = ['C:/Program Files/Zulu/zulu-21/bin/java.exe', '-Xverify:all', '-cp', str(output) + ';' + classpath]
guard_report = BATCH / 'reports/actual-guard411.json'
asm_report = BATCH / 'reports/actual-asm411.json'
run(java + ['GuardTable411', str(prior / 'complete-classes'), str(current / 'complete-classes'), str(guard_report)], 'guards')
run(java + ['Asm411', str(current / 'complete-classes'), str(asm_report)], 'asm')
assert load(guard_report)['actualRejectedCounterexampleCount'] == 386
assert load(asm_report)['prototypeClassesChecked'] == 1376
restored_sources = []
manifest = []
for update in receipt['authorityBindingUpdates']:
    source = Path(update['source'])
    restored = source.read_text(encoding='utf-8')
    for line in update['addedGuardSourceLines']:
        assert restored.count(line) == 1
        restored = restored.replace(line, '')
    if update.get('guardRouteReplacement'):
        old_route, new_route = update['guardRouteReplacement']
        assert restored.count(new_route) == 1
        restored = restored.replace(new_route, old_route)
    for old_hash, new_hash in update['hashReplacements'].items():
        assert new_hash in restored
        restored = restored.replace(new_hash, old_hash)
    assert restored == Path(update['beforeSource']).read_text(encoding='utf-8'), source.name
    target = output / 'restored-source' / source.name
    target.parent.mkdir(exist_ok=True)
    target.write_text(restored, encoding='utf-8')
    restored_sources.append(target)
    relative = 'net/farmingtales/ftboss/item/' + source.name.replace('.java', '.class')
    manifest.append({'name': relative, 'beforePath': str(prior / 'complete-classes' / relative),
        'afterPath': str(output / 'restored-classes' / relative)})
assert len(manifest) == len(receipt['authorityBindingUpdates']) and len(manifest) >= 1
run(javac + ['-d', str(output / 'restored-classes'), *map(str, restored_sources)], 'compile-restored-authorities')
manifest_path = output / 'restored-manifest.json'
save(manifest_path, manifest)
retention_report = BATCH / 'reports/actual-authority-retention411.json'
run(java + ['Semantic411', str(manifest_path), str(retention_report), str(output / 'retention-diff')], 'retention')
assert all(row['skipDebugClassBytesEqual'] for row in load(retention_report)['entries'])
for name, expected in receipt['inputHashes'].items():
    assert digest(name) == expected, name
save(BATCH / 'reports/actual-check411-sealed-experimental.json', {
    'failures': 0, 'allGraphAsmChecks': 1376, 'guardCount': 193, 'guardMissingOrCorruptRejected': 386,
    'oldGuardKeysPreserved': 191, 'addedGuardKeys': added, 'reboundGuardKeys': changed,
    'authoritySourcesRestoredExactly': len(manifest), 'restoredAuthorityWholeClassesSkipDebugEqual': len(manifest),
    'actualRuntimeAccepted': False, 'positiveSignedSessionAccepted': False, 'installable': False,
    'inputHashes': {str(path): digest(path) for path in [binding, parent_guard, old_source, new_source,
        guard_report, asm_report, retention_report, Path(__file__), *tests.glob('*.java')]}})
print({'asmClasses': 1376, 'guards': 193, 'counterexamplesRejected': 386, 'restoredAuthorities': len(manifest)}, flush=True)
