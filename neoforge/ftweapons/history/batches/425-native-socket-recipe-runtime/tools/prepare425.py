import hashlib
import json
from pathlib import Path

LOCAL = Path(__file__).resolve().parents[3]
BATCH = Path(__file__).resolve().parents[1]
prior = LOCAL / 'batches/422-native-seed-isolation-runtime'
binding = LOCAL / 'batches/424-native-socket-recipe-binding/reports/actual-incremental424-experimental.json'
with binding.open('rb') as stream:
    binding_hash = hashlib.file_digest(stream, 'sha256').hexdigest()
for folder in ['reports', 'tests', 'build']:
    (BATCH / folder).mkdir(exist_ok=True)
for source in (prior / 'tests').glob('*.java'):
    text = source.read_text(encoding='utf-8').replace('422', '425')
    if source.name == 'LoadedWeapon422.java':
        marker = '            report.put(§retiredFoodEnchantment§, verifyRetiredFoodEnchantment425(event));'.replace('§', chr(34))
        assert text.count(marker) == 1
        text = text.replace(marker, marker + chr(10) + '            report.put(§socketRecipeParity425§, RecipeSocketParity425.run(event));'.replace('§', chr(34)))
    (BATCH / 'tests' / source.name.replace('422', '425')).write_text(text, encoding='utf-8')
(BATCH / 'tests/RecipeSocketParity425.java').write_text((BATCH / 'tests/RecipeSocketParity425.java.in').read_text(encoding='utf-8').replace('§', chr(34)), encoding='utf-8')
parent = json.loads((LOCAL / 'batches/421-native-seed-isolation-binding/reports/actual-incremental421-experimental.json').read_text(encoding='utf-8'))
arguments = json.loads((Path(parent['attempt']) / 'complete-arguments316.json').read_text(encoding='utf-8'))
original = next(Path(value) for value in arguments if Path(value).name == 'FoundationApprovedRules.java')
original_text = original.read_text(encoding='utf-8')
header = 'package net.farmingtales.ftboss.transaction;'
replaced_header = 'package ftboss.storage308;' + chr(10) + 'import net.farmingtales.ftboss.transaction.*;'
before = original_text.replace('FoundationApprovedRules', 'ApprovedRulesBefore425').replace(header, replaced_header)
assert before.replace(replaced_header, header).replace('ApprovedRulesBefore425', 'FoundationApprovedRules') == original_text
(BATCH / 'tests/ApprovedRulesBefore425.java').write_text(before, encoding='utf-8')
ledger = next(Path(value) for value in arguments if Path(value).name == 'FoodWeaponSourceLedger.java')
ledger_text = ledger.read_text(encoding='utf-8')
first = ledger_text.index('    public static String socketFingerprint(')
last = ledger_text.index('    private static String fingerprint(', first)
socket_method = ledger_text[first:last]
socket_class = '''package ftboss.storage308;
import net.minecraft.nbt.*;
import net.minecraft.world.item.ItemStack;
import net.farmingtales.ftboss.item.NativeFoundationItemData;
import net.farmingtales.ftboss.item.FoodWeaponSourceLedger;
import static net.farmingtales.ftboss.item.FoodWeaponSourceLedger.boundProfile;
public final class SocketBefore425 {
''' + socket_method + '''    private static String fingerprint(Tag value) {
        try {
            java.lang.reflect.Method method = FoodWeaponSourceLedger.class.getDeclaredMethod(§fingerprint§, Tag.class);
            method.setAccessible(true);
            return (String)method.invoke(null, value);
        } catch (java.lang.reflect.InvocationTargetException failure) {
            if (failure.getCause() instanceof RuntimeException runtime) throw runtime;
            if (failure.getCause() instanceof Error error) throw error;
            throw new IllegalStateException(failure.getCause());
        } catch (ReflectiveOperationException failure) {
            throw new IllegalStateException(failure);
        }
    }
}
'''.replace('§', chr(34))
(BATCH / 'tests/SocketBefore425.java').write_text(socket_class, encoding='utf-8')
for name in ['run', 'seal', 'diagnose']:
    text = (prior / 'tools' / (name + '422.py')).read_text(encoding='utf-8').replace('422', '425')
    text = text.replace('421-native-seed-isolation-binding', '424-native-socket-recipe-binding').replace('actual-incremental421', 'actual-incremental424')
    text = text.replace('6c456db9d79bb0d53928b5eefa4feb5734a99e8e24617c4c9b74a31610cf4140', binding_hash).replace('1379', '1381')
    if name == 'run':
        marker = 'str(BATCH / ' + repr('tests/WearRoundTrip348.java') + ')]'
        addition = ', '.join('str(BATCH / ' + repr('tests/' + item + '.java') + ')' for item in ['RecipeSocketParity425', 'ApprovedRulesBefore425', 'SocketBefore425'])
        assert text.count(marker) == 1
        text = text.replace(marker, marker[:-1] + ', ' + addition + ']')
        marker = 'source, BATCH / ' + repr('tests/Coverage425.java')
        addition = ', '.join('BATCH / ' + repr('tests/' + item + '.java') for item in ['RecipeSocketParity425', 'ApprovedRulesBefore425', 'SocketBefore425'])
        assert text.count(marker) == 1
        text = text.replace(marker, 'source, ' + addition + ', BATCH / ' + repr('tests/Coverage425.java'))
    if name == 'seal':
        marker = 'retirement = runtime[' + repr('probe') + '][' + repr('retiredFoodEnchantment') + ']'
        extra = '''parity = runtime['probe']['socketRecipeParity425']
assert parity['failures'] == 0 and parity['socketFingerprintCases'] == 160 and parity['approvedRuleFingerprintCases'] == 5
assert parity['nonemptyApprovedRuleFingerprints'] > 0 and len(parity['acceptedRecipeTypes']) >= 4
'''
        assert text.count(marker) == 1
        text = text.replace(marker, extra + marker)
        text = text.replace(''' 'sixWeaponScope': scope,''', ''' 'socketRecipeParity': parity, 'sixWeaponScope': scope,''')
    (BATCH / 'tools' / (name + '425.py')).write_text(text, encoding='utf-8')
origin = {'approvedRulesSource': str(original), 'socketSource': str(ledger), 'approvedRulesExactSourceRenamingOnly': True, 'socketMethodBodyCopiedExactly': True}
(BATCH / 'reports/golden-source-origins425.json').write_text(json.dumps(origin, ensure_ascii=False, indent=2), encoding='utf-8')
print({'prepared': True, 'binding': binding_hash, 'liveRecipeByteParityRequired': True, 'socketFingerprintCasesRequired': 160})
