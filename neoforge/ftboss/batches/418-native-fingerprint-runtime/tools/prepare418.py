import hashlib
from pathlib import Path

LOCAL = Path(__file__).resolve().parents[3]
BATCH = Path(__file__).resolve().parents[1]
prior = LOCAL / 'batches/415-food-enchantment-runtime'
binding = LOCAL / 'batches/417-native-fingerprint-binding/reports/actual-incremental417-experimental.json'
with binding.open('rb') as stream:
    binding_hash = hashlib.file_digest(stream, 'sha256').hexdigest()
for folder in ['reports', 'tests', 'build']:
    (BATCH / folder).mkdir(exist_ok=True)
for source in (prior / 'tests').glob('*.java'):
    text = source.read_text(encoding='utf-8').replace('415', '418')
    (BATCH / 'tests' / source.name.replace('415', '418')).write_text(text, encoding='utf-8')
for name in ['run', 'seal', 'diagnose']:
    text = (prior / 'tools' / (name + '415.py')).read_text(encoding='utf-8').replace('415', '418')
    text = text.replace('414-food-enchantment-binding', '417-native-fingerprint-binding').replace('actual-incremental414', 'actual-incremental417')
    text = text.replace('fcd7eeaa6d1c604c6e9cc1c4e91bddb8352caf8084f06137a626a51fc5454289', binding_hash)
    text = text.replace('1376', '1379')
    (BATCH / 'tools' / (name + '418.py')).write_text(text, encoding='utf-8')
print({'prepared': True, 'binding': binding_hash, 'runtimeClasses': 1379})
