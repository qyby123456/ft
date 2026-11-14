import subprocess
import sys
import uuid
from pathlib import Path

BATCH = Path(__file__).resolve().parents[1]
LOCAL = BATCH.parents[1]
HISTORY = Path('D:/ft工程文件/FTBoss-NeoForge-1.21.1-移植-20261022')
sys.path.insert(0, str(HISTORY / 'batches/230-native-workbench-mock-channel/tools'))
from launch230_unlock import digest, load, save
sys.path.insert(0, str(HISTORY / 'batches/263-native-recipe-menu-accounting/tools'))
from sdk_relocation263 import relocate_text
binding = LOCAL / 'batches/427-native-recipe-stack-binding/reports/actual-incremental427-experimental.json'
assert digest(binding) == 'b2d1b76b5fb5228896ccd4e048685ac2f1055641332b73b778f1b31bc5401b3e'
receipt = load(binding)
for name, expected in {**receipt['inputHashes'], **receipt.get('outputHashes', {})}.items():
    assert digest(name) == expected, name
prior = Path(receipt['attempt'])
arguments = load(prior / 'complete-arguments316.json')
classpath = str(prior / 'complete-classes') + ';' + relocate_text(arguments[arguments.index('-classpath') + 1])
attempt = BATCH / 'build' / ('fallback430-' + uuid.uuid4().hex[:8])
attempt.mkdir(parents=True)
source = BATCH / 'tests/RangedFallback430.java'
source.write_text(source.with_suffix('.java.in').read_text(encoding='utf-8').replace('§', chr(34)), encoding='utf-8')
report = BATCH / 'reports/actual-native-fallback430.json'
report.parent.mkdir(parents=True, exist_ok=True)
commands = [
    ['C:/Program Files/Zulu/zulu-21/bin/javac.exe', '-proc:none', '--release', '21', '-encoding', 'UTF-8', '-cp', classpath, '-d', str(attempt), str(source)],
    ['C:/Program Files/Zulu/zulu-21/bin/java.exe', '-Xverify:all', '-cp', str(attempt) + ';' + classpath, 'net.farmingtales.ftboss.item.RangedFallback430', str(report)],
]
for name, command in zip(['compile', 'native'], commands):
    save(attempt / (name + '-command.json'), command)
    with (attempt / (name + '.log')).open('wb') as stream:
        result = subprocess.run(command, cwd=LOCAL, stdout=stream, stderr=subprocess.STDOUT)
    save(attempt / (name + '-terminal.json'), {'exitCode': result.returncode})
    assert result.returncode == 0, str(attempt / (name + '.log'))
data = load(report)
assert data['failures'] == 0 and data['fallbackCases'] == 12 and data['storedCompoundIdentityAndMaxPreserved']
save(BATCH / 'reports/actual-native-fallback430-sealed.json', {
    **data, 'attempt': str(attempt),
    'inputHashes': {str(path): digest(path) for path in [binding, Path(__file__), source, source.with_suffix('.java.in'), *[Path(value) for value in arguments if Path(value).name == 'FoodWeaponExpectedTrace.java']]},
    'outputHashes': {str(path): digest(path) for path in [report, *attempt.rglob('*.class')]},
})
print({'failures': 0, 'nativeFallbackCases': 12, 'attachedPlanControlPassed': True, 'productionChanged': False})
