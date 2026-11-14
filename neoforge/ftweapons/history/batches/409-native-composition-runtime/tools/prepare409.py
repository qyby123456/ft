import hashlib
from pathlib import Path

ROOT = Path('D:/ft工程文件/FTBoss-NeoForge-1.21.1-移植-20261022')
BATCH = Path(__file__).resolve().parents[1]
LOCAL = BATCH.parents[1]
binding = LOCAL / 'batches/408-native-composition-binding/reports/actual-incremental408-experimental.json'
binding_hash = hashlib.file_digest(binding.open('rb'), 'sha256').hexdigest()
prior = ROOT / 'batches/406-native-foundation-output-runtime'
for folder in ['reports', 'tests', 'build']:
    (BATCH / folder).mkdir(exist_ok=True)
for source in (prior / 'tests').glob('*.java'):
    (BATCH / 'tests' / source.name.replace('406', '409')).write_text(source.read_text(encoding='utf-8').replace('406', '409'), encoding='utf-8')
for name in ['run', 'seal', 'diagnose']:
    text = (prior / 'tools' / (name + '406.py')).read_text(encoding='utf-8').replace('406', '409')
    text = text.replace('ROOT = Path(__file__).resolve().parents[3]', 'ROOT = Path(' + repr(str(ROOT)) + ')')
    text = text.replace("ROOT / 'batches/405-native-foundation-output-binding/reports/actual-incremental405-experimental.json'", 'Path(' + repr(str(binding)) + ')')
    text = text.replace('3de02711d5a006631ae571e4348e2d5e0a3ee0d59aa5c7571e5aaad680a92bcb', binding_hash)
    text = text.replace('== 1373', '== 1374').replace('cwd=ROOT', 'cwd=BATCH')
    (BATCH / 'tools' / (name + '409.py')).write_text(text, encoding='utf-8')
print({'binding': binding_hash, 'classes': 1374, 'runtimeOutputs': str(BATCH), 'historicalInputsReadOnly': str(ROOT)})
