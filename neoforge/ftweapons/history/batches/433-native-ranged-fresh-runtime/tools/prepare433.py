import hashlib
from pathlib import Path

BATCH = Path(__file__).resolve().parents[1]
LOCAL = BATCH.parents[1]
prior = LOCAL / 'batches/429-native-ranged-acquisition-diagnostic'
binding = LOCAL / 'batches/432-native-ranged-fresh-binding/reports/actual-incremental432-experimental.json'
with binding.open('rb') as stream:
    binding_hash = hashlib.file_digest(stream, 'sha256').hexdigest()
for folder in ['reports', 'tests', 'build']:
    (BATCH / folder).mkdir(exist_ok=True)
for source in (prior / 'tests').glob('*.java'):
    text = source.read_text(encoding='utf-8').replace('429', '433')
    if source.name == 'LoadedWeapon429.java':
        marker = '                                    acquisition.put(§plannedBufferWasNull§, plannedBuffer == null);'.replace('§', chr(34))
        addition = '''
                                    Object acquired = read(actualBook, §acquiredRangedBuffer431§);
                                    acquisition.put(§freshBufferBound§, acquired != null);
                                    acquisition.put(§freshBufferIsCurrentStoredBuffer§, acquired != null && acquired == restored.getPersistentData().get(§FTBossRangedProcTimers§));
'''.replace('§', chr(34))
        assert text.count(marker) == 1
        text = text.replace(marker, marker + addition)
    (BATCH / 'tests' / source.name.replace('429', '433')).write_text(text, encoding='utf-8')
for name in ['run', 'seal', 'diagnose']:
    text = (prior / 'tools' / (name + '429.py')).read_text(encoding='utf-8').replace('429', '433')
    text = text.replace('427-native-recipe-stack-binding', '432-native-ranged-fresh-binding').replace('actual-incremental427', 'actual-incremental432')
    text = text.replace('b2d1b76b5fb5228896ccd4e048685ac2f1055641332b73b778f1b31bc5401b3e', binding_hash)
    (BATCH / 'tools' / (name + '433.py')).write_text(text, encoding='utf-8')
print({'prepared': True, 'candidate': 432, 'binding': binding_hash})
