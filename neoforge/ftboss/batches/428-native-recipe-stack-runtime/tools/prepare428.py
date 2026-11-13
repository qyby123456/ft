import hashlib
import json
from pathlib import Path

LOCAL = Path(__file__).resolve().parents[3]
BATCH = Path(__file__).resolve().parents[1]
prior = LOCAL / 'batches/425-native-socket-recipe-runtime'
binding = LOCAL / 'batches/427-native-recipe-stack-binding/reports/actual-incremental427-experimental.json'
with binding.open('rb') as stream:
    binding_hash = hashlib.file_digest(stream, 'sha256').hexdigest()
for folder in ['reports', 'tests', 'build']:
    (BATCH / folder).mkdir(exist_ok=True)
for source in (prior / 'tests').glob('*.java'):
    text = source.read_text(encoding='utf-8').replace('425', '428')
    if source.name == 'ApprovedRulesBefore425.java':
        text = text.replace('NativeRecipeEvidenceData', 'RecipeDataBefore428').replace('CataclysmInfusionRecipeEvidence', 'InfusionEvidenceBefore428')
    if source.name == 'LoadedWeapon425.java':
        marker = '            report.put(§socketRecipeParity428§, RecipeSocketParity428.run(event));'.replace('§', chr(34))
        assert text.count(marker) == 1
        text = text.replace(marker, marker + chr(10) + '            report.put(§componentStackParity428§, StackParity428.run(event));'.replace('§', chr(34)))
    (BATCH / 'tests' / source.name.replace('425', '428')).write_text(text, encoding='utf-8')
(BATCH / 'tests/StackParity428.java').write_text((BATCH / 'tests/StackParity428.java.in').read_text(encoding='utf-8').replace('§', chr(34)), encoding='utf-8')
parent = json.loads((LOCAL / 'batches/424-native-socket-recipe-binding/reports/actual-incremental424-experimental.json').read_text(encoding='utf-8'))
arguments = json.loads((Path(parent['attempt']) / 'complete-arguments316.json').read_text(encoding='utf-8'))
for name, renamed in [('NativeRecipeEvidenceData', 'RecipeDataBefore428'), ('CataclysmInfusionRecipeEvidence', 'InfusionEvidenceBefore428')]:
    original = next(Path(value) for value in arguments if Path(value).name == name + '.java')
    source = original.read_text(encoding='utf-8')
    text = source.replace('package net.farmingtales.ftboss.transaction;', 'package ftboss.storage308;').replace(name, renamed)
    if name == 'CataclysmInfusionRecipeEvidence':
        text = text.replace('NativeRecipeEvidenceData', 'RecipeDataBefore428')
    restored = text.replace('package ftboss.storage308;', 'package net.farmingtales.ftboss.transaction;').replace(renamed, name)
    if name == 'CataclysmInfusionRecipeEvidence':
        restored = restored.replace('RecipeDataBefore428', 'NativeRecipeEvidenceData')
    assert restored == source
    (BATCH / 'tests' / (renamed + '.java')).write_text(text, encoding='utf-8')
for name in ['run', 'seal', 'diagnose']:
    text = (prior / 'tools' / (name + '425.py')).read_text(encoding='utf-8').replace('425', '428')
    text = text.replace('424-native-socket-recipe-binding', '427-native-recipe-stack-binding').replace('actual-incremental424', 'actual-incremental427')
    text = text.replace('bb7aa8a6c8cb6acadf8e3da85dd4726fe663bc82560a7594b820690096c0550c', binding_hash)
    additions = ['StackParity428', 'RecipeDataBefore428', 'InfusionEvidenceBefore428']
    if name == 'run':
        marker = 'str(BATCH / ' + repr('tests/SocketBefore428.java') + ')]'
        extra = ', '.join('str(BATCH / ' + repr('tests/' + item + '.java') + ')' for item in additions)
        assert text.count(marker) == 1
        text = text.replace(marker, marker[:-1] + ', ' + extra + ']')
        marker = 'source, BATCH / ' + repr('tests/RecipeSocketParity428.java')
        extra = ', '.join('BATCH / ' + repr('tests/' + item + '.java') for item in additions)
        assert text.count(marker) == 1
        text = text.replace(marker, 'source, ' + extra + ', BATCH / ' + repr('tests/RecipeSocketParity428.java'))
    if name == 'seal':
        marker = 'parity = runtime[' + repr('probe') + '][' + repr('socketRecipeParity428') + ']'
        extra = '''component_parity = runtime['probe']['componentStackParity428']
assert component_parity['failures'] == 0 and component_parity['componentStackByteParityCases'] == 192 and component_parity['emptyStackRejectionPreserved']
'''
        assert text.count(marker) == 1
        text = text.replace(marker, extra + marker)
        text = text.replace(''' 'socketRecipeParity': parity,''', ''' 'componentStackParity': component_parity, 'socketRecipeParity': parity,''')
    (BATCH / 'tools' / (name + '428.py')).write_text(text, encoding='utf-8')
print({'prepared': True, 'binding': binding_hash, 'frozenOldRecipeStackCodecUsedByGolden': True, 'componentStackByteCasesRequired': 192})
