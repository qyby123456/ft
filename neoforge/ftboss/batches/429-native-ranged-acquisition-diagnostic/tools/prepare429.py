from pathlib import Path

BATCH = Path(__file__).resolve().parents[1]
ROOT = BATCH.parents[1]
prior = ROOT / 'batches/428-native-recipe-stack-runtime'
for folder in ['reports', 'tests', 'build']:
    (BATCH / folder).mkdir(parents=True, exist_ok=True)
for source in (prior / 'tests').glob('*.java'):
    text = source.read_text(encoding='utf-8').replace('428', '429')
    if source.name == 'LoadedWeapon428.java':
        marker = '                row.put(§playerHealthBefore§, restored.getHealth());'.replace('§', chr(34))
        addition = '''
                row.put(§rangedRootPresentBefore429§, restored.getPersistentData().contains(§FTBossRangedProcTimers§, 10));
'''.replace('§', chr(34))
        assert text.count(marker) == 1
        text = text.replace(marker, marker + addition)
        marker = '                                Object actualBook = read(actualSession, §timerSemantics§);'.replace('§', chr(34))
        addition = '''
                                if (actualBook != null) {
                                    Object plannedRoot = read(actualBook, §rangedRoot§);
                                    Object plannedBuffer = read(actualBook, §rangedBuffer§);
                                    Map<String, Object> acquisition = new LinkedHashMap<>();
                                    acquisition.put(§plannedRootIsActualPersistentRoot§, plannedRoot == restored.getPersistentData());
                                    acquisition.put(§plannedBufferWasNull§, plannedBuffer == null);
                                    acquisition.put(§plannedBufferIsCurrentStoredBuffer§, plannedBuffer == restored.getPersistentData().get(§FTBossRangedProcTimers§));
                                    acquisition.put(§storedBufferPresentAfter§, restored.getPersistentData().contains(§FTBossRangedProcTimers§, 10));
                                    acquisition.put(§authorityFieldsWritten§, false);
                                    publicationRow.put(§rangedAcquisitionIdentity429§, acquisition);
                                }
'''.replace('§', chr(34))
        assert text.count(marker) == 1
        text = text.replace(marker, marker + addition)
    (BATCH / 'tests' / source.name.replace('428', '429')).write_text(text, encoding='utf-8')
for name in ['run', 'seal', 'diagnose']:
    text = (prior / 'tools' / (name + '428.py')).read_text(encoding='utf-8').replace('428', '429')
    (BATCH / 'tools' / (name + '429.py')).write_text(text, encoding='utf-8')
print({'prepared': True, 'candidateUnchanged': 427, 'authorityFieldsWritten': False})
