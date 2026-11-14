import hashlib
import re
from pathlib import Path

WORK = Path(__file__).resolve().parents[1]
ROOT = WORK.parents[1]
BATCH = WORK / 'runtime'
prior = ROOT / 'batches/436-native-revocation-origin-runtime'
binding = WORK / 'reports/actual-current.json'
with binding.open('rb') as stream:
    binding_hash = hashlib.file_digest(stream, 'sha256').hexdigest()
for folder in ['reports', 'tests', 'tools', 'build']:
    (BATCH / folder).mkdir(parents=True, exist_ok=True)
for source in (prior / 'tests').glob('*.java'):
    text = source.read_text(encoding='utf-8')
    if source.name == 'LoadedWeapon436.java':
        for line in ['            report.put(§sixWeaponScope§, verifySixWeaponScope436());',
            '            report.put(§retiredFoodEnchantment§, verifyRetiredFoodEnchantment436(event));',
            '            report.put(§socketRecipeParity436§, RecipeSocketParity436.run(event));',
            '            report.put(§componentStackParity436§, StackParity436.run(event));',
            '                    if (sample == 0) ItemStackProof361.run(row);',
            '                    if (sample == 0) Coverage436.run(row);']:
            line = line.replace('§', chr(34))
            assert text.count(line) == 1, line
            text = text.replace(line, '')
        start = text.index('                    row.put(§fingerprintGraphChecks§'.replace('§', chr(34)))
        end = text.index('                    int arrowsBefore', start)
        text = text[:start] + text[end:]
        start = text.index('                                Class<?> timerAuthority = ')
        end = text.index('                                publicationRow.put(§sequenceDetail436§'.replace('§', chr(34)), start)
        text = text[:start] + text[end:]
        text = text.replace('for (int sample = 0; sample < 2; sample++)', 'for (int sample : new int[]{0, 1, 3, 4})')
        marker = '                original.getPersistentData().putInt(§ft317_player§, sample + 31);'.replace('§', chr(34))
        addition = '''
                if (sample == 3) {
                    CompoundTag timers = new CompoundTag();
                    timers.putLong(§LastSkillProc.blood_feast§, 5L);
                    original.getPersistentData().put(§FTBossRangedProcTimers§, timers);
                }
                if (sample == 4) original.getPersistentData().putInt(§FTBossRangedProcTimers§, 19);
'''.replace('§', chr(34))
        assert text.count(marker) == 1
        text = text.replace(marker, marker + addition)
    (BATCH / 'tests' / source.name).write_text(text, encoding='utf-8')
text = (prior / 'tools/run436.py').read_text(encoding='utf-8')
text = re.sub(r'(?m)^binding = .+$', 'binding = Path(' + repr(binding.as_posix()) + ')', text)
text = re.sub(r'(?m)^assert digest\(binding\) == .+$', 'assert digest(binding) == ' + repr(binding_hash), text)
(BATCH / 'tools/run.py').write_text(text, encoding='utf-8')
print({'prepared': True, 'binding': binding_hash, 'retainedFocusedCases': ['missing', 'unknown_join_rejected', 'existing_compound', 'wrong_type'], 'fullRegressionRepeated': False})
