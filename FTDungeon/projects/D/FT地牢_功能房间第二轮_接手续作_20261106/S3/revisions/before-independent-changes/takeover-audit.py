import hashlib
import json
from pathlib import Path
from datetime import datetime
from zoneinfo import ZoneInfo
import subprocess

ROOT = Path(__file__).resolve().parent
SOURCE = Path('D:/ft工程文件/FT地牢_功能房间第二轮_20261106/S3')
SKILL = Path('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill')

def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

def read(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))

def save(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2), encoding='utf-8')

records = []
references = read(ROOT/'references.json')['references']
for number in range(11, 21):
    identifier = f's3-{number}'
    folder = ROOT/'rooms'/identifier
    original = SOURCE/'rooms'/identifier
    checksum = digest(folder/(identifier+'.nbt'))
    assert checksum == digest(original/(identifier+'.nbt'))
    reference = next(item for item in references if item['id'].lower() == identifier)
    assert digest(Path(reference['image_path'])) == reference['sha256']
    for relative, key in [('qa/report.json', 'candidate_sha256'), ('qa/support.json', 'sha256'), ('qa/render.json', 'sha256')]:
        assert read(folder/relative)[key] == checksum
    assert read(folder/'qa/report.json')['self_check'] == 'PASS'
    assert not read(folder/'qa/support.json')['failures']
    output = ROOT/'reproduction'/identifier
    output.mkdir(parents=True, exist_ok=True)
    command = ['node', str(SKILL/'scripts/run-build.mjs'), str(ROOT/f'room{number}.mjs'), '--out-dir', str(output)]
    result = subprocess.run(command, cwd=ROOT, capture_output=True, text=True, encoding='utf-8', errors='replace', creationflags=subprocess.CREATE_NO_WINDOW)
    (output/'build.log').write_text(result.stdout+'\n'+result.stderr, encoding='utf-8')
    assert result.returncode == 0, identifier+' reproduction failed'
    assert digest(output/(identifier+'.nbt')) == checksum, identifier+' reproduction hash differs'
    record = {'id': identifier, 'source': str(original/(identifier+'.nbt')), 'source_sha256': checksum, 'candidate_sha256': checksum, 'recipe_sha256': digest(ROOT/f'room{number}.mjs'), 'common_sha256': digest(ROOT/'common.mjs'), 'prompt_sha256': digest(Path(reference['prompt_path'])), 'reference_sha256': reference['sha256'], 'reproduction': {'command': command, 'sha256': digest(output/(identifier+'.nbt')), 'exact_bytes_match': True}, 'previews': [{'path': str(path), 'sha256': digest(path)} for path in sorted(folder.glob('*.png'))], 'checks': 'Fresh native registry/template, routes and all-block support rerun in takeover directory; copied unchanged-source previews hash-verified and personally viewed', 'independent_acceptance': 'PENDING'}
    records.append(record)
    print(identifier, 'hash/reproduction/fresh QA verified', flush=True)
save(ROOT/'takeover-provenance.json', {'source_read_only': str(SOURCE), 'destination': str(ROOT), 'timestamp': datetime.now(ZoneInfo('Asia/Shanghai')).isoformat(), 'rooms': records, 'merged_old_sha256': digest(SOURCE/'rooms/s3-merged/s3-merged.nbt'), 'merged_new_sha256': digest(ROOT/'rooms/s3-merged/s3-merged.nbt'), 'merge_fix': 'Remove corridor shell records inside every source bounding volume before copying frozen source records. Preserve explicit source connections using --no-block-state-updates.', 'independent_acceptance': 'PENDING'})
