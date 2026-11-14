import subprocess
import sys
import uuid
from pathlib import Path

ROOT = Path('D:/ft工程文件/FTBoss-NeoForge-1.21.1-移植-20261022')
BATCH = Path(__file__).resolve().parents[1]
LOCAL = BATCH.parents[1]
sys.path.insert(0, str(ROOT / 'batches/230-native-workbench-mock-channel/tools'))
from launch230_unlock import digest, load, save
sys.path.insert(0, str(ROOT / 'batches/263-native-recipe-menu-accounting/tools'))
from sdk_relocation263 import relocate_text
binding = LOCAL / 'batches/417-native-fingerprint-binding/reports/actual-incremental417-experimental.json'
diagnostic = LOCAL / 'batches/419-native-fingerprint-impact-runtime/reports/actual-first-rejection419-diagnostic.json'
for receipt in [binding, diagnostic]:
    for name, expected in load(receipt)['inputHashes'].items():
        assert digest(name) == expected, name
prior = Path(load(binding)['attempt'])
arguments = load(prior / 'complete-arguments316.json')
original = next(Path(value) for value in arguments if Path(value).name == 'NativeExpectedFieldObservation.java')
text = original.read_text(encoding='utf-8')
old = '|| values == null || values.getClass() != java.util.LinkedHashMap.class || (seed != null && !author)) {'
new = '|| values == null || values.getClass() != java.util.LinkedHashMap.class' + chr(10) + '            || (seed != null && (!author || seed.getClass() != CompoundTag.class))) {'
assert text.count(old) == 1
text = text.replace(old, new)
old = 'CompoundTag output = seed == null ? newReadCompound() : seed;'
assert text.count(old) == 1
text = text.replace(old, 'CompoundTag output = seed == null ? newReadCompound() : seed.copy();')
target = BATCH / 'proposal/net/farmingtales/ftboss/item/NativeExpectedFieldObservation.java'
target.parent.mkdir(parents=True, exist_ok=True)
target.write_text(text, encoding='utf-8')
attempt = BATCH / 'build' / ('seed-isolation420-' + uuid.uuid4().hex[:8])
attempt.mkdir(parents=True)
classpath = str(prior / 'complete-classes') + ';' + relocate_text(arguments[arguments.index('-classpath') + 1])
command = ['C:/Program Files/Zulu/zulu-21/bin/javac.exe', '-proc:none', '--release', '21', '-encoding', 'UTF-8', '-cp', classpath, '-d', str(attempt / 'prototype-classes'), str(target)]
save(attempt / 'compile-command420.json', command)
with (attempt / 'compile420.log').open('wb') as stream:
    terminal = subprocess.run(command, cwd=LOCAL, stdout=stream, stderr=subprocess.STDOUT)
save(attempt / 'compile-terminal420.json', {'exitCode': terminal.returncode})
assert terminal.returncode == 0, str(attempt / 'compile420.log')
save(BATCH / 'reports/actual-seed420-compiled-not-adopted.json', {
    'attempt': str(attempt), 'failures': 0,
    'changedExistingMethods': {'NativeExpectedFieldObservation.java': ['fingerprintCompoundForRead416']},
    'cumulativeCandidateUpdated': False, 'actualRuntimeTested': False, 'installable': False,
    'inputHashes': {str(path): digest(path) for path in [binding, diagnostic, original, target, Path(__file__)]},
    'outputHashes': {str(path): digest(path) for path in (attempt / 'prototype-classes').rglob('*.class')}})
print({'compiled': True, 'attempt': str(attempt), 'seedCopiedBeforeRawPopulation': True}, flush=True)
