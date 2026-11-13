import hashlib
import json
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
OLD = [ROOT.parent / 'nature-b1-s1-20261104', ROOT.parent / 'nature-b1-s1-01-serial-20261104']
archive_root = ROOT / 'frozen-previous'
archive_root.mkdir(exist_ok=True)
records = []
for directory in OLD:
    archive_path = archive_root / (directory.name + '-excluded.zip')
    if archive_path.exists():
        raise RuntimeError('Never overwrite a frozen evidence archive')
    entries = []
    with zipfile.ZipFile(archive_path, 'x', zipfile.ZIP_DEFLATED) as archive:
        for path in sorted(directory.rglob('*')):
            if not path.is_file():
                continue
            relative = path.relative_to(directory)
            if any(part in {'server', 'native-test', '__pycache__', 'node_modules', '.gradle', 'delivery'} for part in relative.parts):
                continue
            if path.suffix == '.zip':
                entries.append({'path': str(path), 'sha256': hashlib.sha256(path.read_bytes()).hexdigest(), 'preserved_in_original_location': True})
                continue
            content = path.read_bytes()
            entries.append({'path': str(path), 'sha256': hashlib.sha256(content).hexdigest(), 'archive_entry': relative.as_posix()})
            archive.writestr(relative.as_posix(), content)
    with zipfile.ZipFile(archive_path) as archive:
        assert archive.testzip() is None
    records.append({'original_directory': str(directory), 'status': 'EXCLUDED_NOT_A_CANDIDATE', 'reason': 'User requested all modules of this batch be redone and accepted in small strict-serial batches.', 'archive': str(archive_path), 'archive_sha256': hashlib.sha256(archive_path.read_bytes()).hexdigest(), 'originals_unchanged': True, 'entries': entries})
value = {'status': 'ALL_PREVIOUS_VERSIONS_EXCLUDED', 'candidate_allowlist': [], 'old_versions': records, 'unified_candidate_rule': 'Accept only a redo version whose batch explicitly passed all acceptance gates; ignore old producer PASS states.', 'next_batch_locked': True}
(ROOT / 'EXCLUDED_VERSIONS.json').write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print('FROZEN', len(records), 'previous work directories; candidate allowlist empty')
