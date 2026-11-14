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
receipt_path = BATCH / 'reports/actual-scope410-compiled-not-adopted.json'
receipt = load(receipt_path)
for name, expected in {**receipt['inputHashes'], **receipt['outputHashes']}.items():
    assert digest(name) == expected, name
binding = LOCAL / 'batches/408-native-composition-binding/reports/actual-incremental408-experimental.json'
prior = Path(load(binding)['attempt'])
arguments = load(prior / 'complete-arguments316.json')
original = Path(next(value for value in arguments if value.endswith('FoodWeaponEvolution.java')))
restored = (BATCH / 'proposal/net/farmingtales/ftboss/item/FoodWeaponEvolution.java').read_text(encoding='utf-8')
for name in receipt['changedEvolutionMethods']:
    result = 'true' if name.endswith('isEvolutionExcluded') else 'null' if name.endswith('nativeProfile') else 'false'
    insertion = '\n        if (!WeaponSystemScope410.supports(var0)) return ' + result + ';'
    declaration = ('FoodWeaponEvolution.Profile ' if result == 'null' else 'boolean ') + name + '(ItemStack var0) {'
    marker = declaration + insertion
    assert restored.count(marker) == 1, name
    restored = restored.replace(marker, declaration)
assert restored == original.read_text(encoding='utf-8')
attempt = Path(receipt['attempt'])
output = attempt / ('checks-' + uuid.uuid4().hex[:8])
output.mkdir()
sources = [HISTORY / 'batches/382-native-private-read-parity/tests/Asm382.java', HISTORY / 'batches/360-native-guard-semantic-audit/tests/GuardSemantic360.java']
classpath = str(prior / 'complete-classes') + ';' + relocate_text(arguments[arguments.index('-classpath') + 1])
def run(command, name):
    with (output / (name + '.log')).open('wb') as stream:
        terminal = subprocess.run(command, cwd=LOCAL, stdout=stream, stderr=subprocess.STDOUT)
    save(output / (name + '-terminal.json'), {'exitCode': terminal.returncode})
    assert terminal.returncode == 0, str(output / (name + '.log'))
run(['C:/Program Files/Zulu/zulu-21/bin/javac.exe', '-proc:none', '--release', '21', '-encoding', 'UTF-8', '-cp', classpath, '-d', str(output), *map(str, sources)], 'compile')
java = ['C:/Program Files/Zulu/zulu-21/bin/java.exe', '-Xverify:all', '-cp', str(output) + ';' + classpath]
asm = BATCH / 'reports/actual-asm410.json'
semantic = BATCH / 'reports/actual-semantic410.json'
run(java + ['Asm382', str(attempt / 'prototype-classes'), str(asm)], 'asm')
manifest = output / 'manifest.json'
save(manifest, [{'name': path.relative_to(attempt / 'prototype-classes').as_posix(), 'beforePath': str(prior / 'complete-classes' / path.relative_to(attempt / 'prototype-classes')), 'afterPath': str(path)} for path in (attempt / 'prototype-classes').rglob('*.class') if (prior / 'complete-classes' / path.relative_to(attempt / 'prototype-classes')).exists()])
run(java + ['GuardSemantic360', str(manifest), str(semantic), str(output / 'diff')], 'semantic')
for row in load(semantic)['entries']:
    assert row['fieldsEqual'] and row['interfacesEqual'] and row['classAccessEqual'] and row['superClassEqual']
    assert row['removedMethods'] == [] and row['addedMethods'] == []
    if row['name'].endswith('/FoodWeaponEvolution.class'):
        assert len(row['changedMethods']) == 6, row
        assert {value.split('(')[0] for value in row['changedMethods']} == set(receipt['changedEvolutionMethods'])
    else:
        assert row['skipDebugClassBytesEqual'], row['name']
save(BATCH / 'reports/actual-scope410-sealed-not-adopted.json', {'failures': 0, 'retainedFamilies': receipt['retainedFamilies'], 'evolutionSourceRestoresExactly': True, 'changedEvolutionMethods': receipt['changedEvolutionMethods'], 'asmClasses': load(asm)['prototypeClassesChecked'], 'allOtherExistingEvolutionMethodsRetained': True, 'cumulativeCandidateUpdated': False, 'actualRuntimeAccepted': False, 'enchantmentRemovalComplete': False, 'installable': False, 'inputHashes': {str(path): digest(path) for path in [receipt_path, asm, semantic, Path(__file__), *sources]}})
print({'sealedPrototype': True, 'asmClasses': load(asm)['prototypeClassesChecked'], 'onlyEvolutionChanges': receipt['changedEvolutionMethods']})
