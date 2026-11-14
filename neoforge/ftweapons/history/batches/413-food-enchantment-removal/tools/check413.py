import subprocess
import sys
import uuid
from pathlib import Path

LOCAL = Path(__file__).resolve().parents[3]
BATCH = Path(__file__).resolve().parents[1]
HISTORY = Path('D:/ft工程文件/FTBoss-NeoForge-1.21.1-移植-20261022')
sys.path.insert(0, str(HISTORY / 'batches/230-native-workbench-mock-channel/tools'))
from launch230_unlock import digest, load, save
sys.path.insert(0, str(HISTORY / 'batches/263-native-recipe-menu-accounting/tools'))
from sdk_relocation263 import relocate_text
receipt_path = BATCH / 'reports/actual-enchantment413-compiled-not-adopted.json'
receipt = load(receipt_path)
for name, expected in {**receipt['inputHashes'], **receipt['outputHashes']}.items():
    assert digest(name) == expected, name
prior = Path(load(LOCAL / 'batches/411-six-weapon-binding/reports/actual-incremental411-experimental.json')['attempt'])
attempt = Path(receipt['attempt'])
arguments = load(prior / 'complete-arguments316.json')
classpath = str(prior / 'complete-classes') + ';' + relocate_text(arguments[arguments.index('-classpath') + 1])
output = attempt / ('checks-' + uuid.uuid4().hex[:8])
output.mkdir()
sources = [HISTORY / 'batches/382-native-private-read-parity/tests/Asm382.java', HISTORY / 'batches/360-native-guard-semantic-audit/tests/GuardSemantic360.java', BATCH / 'tests/Retired413.java', BATCH / 'tests/Differential413.java']
def run(command, name):
    with (output / (name + '.log')).open('wb') as stream:
        terminal = subprocess.run(command, cwd=LOCAL, stdout=stream, stderr=subprocess.STDOUT)
    save(output / (name + '-terminal.json'), {'exitCode': terminal.returncode})
    assert terminal.returncode == 0, str(output / (name + '.log'))
run(['C:/Program Files/Zulu/zulu-21/bin/javac.exe', '-proc:none', '--release', '21', '-encoding', 'UTF-8', '-cp', classpath, '-d', str(output), *map(str, sources)], 'compile')
java = ['C:/Program Files/Zulu/zulu-21/bin/java.exe', '-Xverify:all', '-cp', str(output) + ';' + classpath]
asm = BATCH / 'reports/actual-asm413.json'
semantic = BATCH / 'reports/actual-semantic413.json'
retired = BATCH / 'reports/actual-retired413.json'
business = BATCH / 'reports/actual-business413.json'
run(java + ['Asm382', str(attempt / 'prototype-classes'), str(asm)], 'asm')
run(java + ['Retired413', str(attempt / 'prototype-classes'), str(retired)], 'retirement')
run(java + ['Differential413', str(prior / 'complete-classes'), str(attempt / 'prototype-classes'), str(business)], 'business')
manifest = output / 'manifest.json'
save(manifest, [{'name': path.relative_to(attempt / 'prototype-classes').as_posix(), 'beforePath': str(prior / 'complete-classes' / path.relative_to(attempt / 'prototype-classes')), 'afterPath': str(path)} for path in (attempt / 'prototype-classes').rglob('*.class') if (prior / 'complete-classes' / path.relative_to(attempt / 'prototype-classes')).exists()])
run(java + ['GuardSemantic360', str(manifest), str(semantic), str(output / 'diff')], 'semantic')
changed = [{'class': row['name'], 'changedMethods': row['changedMethods'], 'removedMethods': row['removedMethods'], 'addedMethods': row['addedMethods']} for row in load(semantic)['entries'] if not row['skipDebugClassBytesEqual']]
save(BATCH / 'reports/actual-enchantment413-checked-not-adopted.json', {'failures': 0, 'asmClasses': load(asm)['prototypeClassesChecked'], 'retirementBytecodeChecks': load(retired), 'rawDifferential': changed, 'businessDifferentialAccepted': True, 'exactLambdaBijectionAudit': load(business), 'actualRuntimeAccepted': False, 'cumulativeCandidateUpdated': False, 'installable': False, 'inputHashes': {str(path): digest(path) for path in [receipt_path, asm, semantic, retired, business, Path(__file__), *sources]}})
print({'asmClasses': load(asm)['prototypeClassesChecked'], 'retiredMethodsChecked': load(retired)['checkedMethods'], 'rawChangedClasses': len(changed), 'businessDifferentialAccepted': True})
