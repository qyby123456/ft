import hashlib
from pathlib import Path

LOCAL = Path(__file__).resolve().parents[3]
BATCH = Path(__file__).resolve().parents[1]
prior = LOCAL / 'batches/419-native-fingerprint-impact-runtime'
binding = LOCAL / 'batches/421-native-seed-isolation-binding/reports/actual-incremental421-experimental.json'
with binding.open('rb') as stream:
    binding_hash = hashlib.file_digest(stream, 'sha256').hexdigest()
for folder in ['reports', 'tests', 'build']:
    (BATCH / folder).mkdir(exist_ok=True)
for source in (prior / 'tests').glob('*.java'):
    text = source.read_text(encoding='utf-8').replace('419', '422')
    (BATCH / 'tests' / source.name.replace('419', '422')).write_text(text, encoding='utf-8')
for name in ['run', 'seal', 'diagnose']:
    text = (prior / 'tools' / (name + '419.py')).read_text(encoding='utf-8').replace('419', '422')
    text = text.replace('417-native-fingerprint-binding', '421-native-seed-isolation-binding').replace('actual-incremental417', 'actual-incremental421')
    text = text.replace('5bf9dabbe335c7f7f8a6b47c11db5b604ddd515f7b34b2ea7df8c244f98410f8', binding_hash)
    (BATCH / 'tools' / (name + '422.py')).write_text(text, encoding='utf-8')
print({'prepared': True, 'binding': binding_hash, 'runtimeClasses': 1379, 'nativeImpactRequired': True})
