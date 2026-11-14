import hashlib
from pathlib import Path

BATCH = Path(__file__).resolve().parents[1]
LOCAL = BATCH.parents[1]
prior = LOCAL / 'batches/433-native-ranged-fresh-runtime'
binding = LOCAL / 'batches/435-native-revocation-origin-binding/reports/actual-incremental435-experimental.json'
with binding.open('rb') as stream:
    binding_hash = hashlib.file_digest(stream, 'sha256').hexdigest()
for folder in ['reports', 'tests', 'build']:
    (BATCH / folder).mkdir(exist_ok=True)
for source in (prior / 'tests').glob('*.java'):
    (BATCH / 'tests' / source.name.replace('433', '436')).write_text(source.read_text(encoding='utf-8').replace('433', '436'), encoding='utf-8')
for name in ['run', 'seal', 'diagnose', 'seal_ranged']:
    source = prior / 'tools' / (name + '433.py')
    text = source.read_text(encoding='utf-8').replace('433', '436')
    text = text.replace('432-native-ranged-fresh-binding', '435-native-revocation-origin-binding').replace('actual-incremental432', 'actual-incremental435')
    text = text.replace('a555ebe084a11eff668519a3839ea199e5c12b8a150109f6d57cdce83117f85a', binding_hash)
    if name == 'diagnose':
        marker = "trace = failure.get('trace', '') if isinstance(failure, dict) else ''"
        assert text.count(marker) == 1
        text = text.replace(marker, "origin = session.get('firstRevocationSite434')" + chr(10) + "trace_source = failure if isinstance(failure, dict) else origin" + chr(10) + "trace = trace_source.get('trace', '') if isinstance(trace_source, dict) else ''")
        text = text.replace("'firstFailure': failure, 'firstProjectFrames': project_frames,", "'firstFailure': failure, 'firstRevocationOrigin': origin, 'firstProjectFrames': project_frames,")
    (BATCH / 'tools' / (name + '436.py')).write_text(text, encoding='utf-8')
print({'prepared': True, 'candidate': 435, 'binding': binding_hash})
