import hashlib
import json
import pathlib

ROOT = pathlib.Path(__file__).parent
OLD = pathlib.Path(r'D:\ft工程文件\FT地牢_功能房间第二轮_20261106\S1')

def save(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')

references = json.loads((ROOT / 'references.json').read_text(encoding='utf-8-sig'))
references['output_directory'] = str(ROOT)
save(ROOT / 'references.json', references)
progress = json.loads((ROOT / 'progress.json').read_text(encoding='utf-8-sig'))
progress['inheritance'] = {'source': str(OLD), 'manifest': 'inheritance-manifest.json', 'old_directory_read_only': True, 'process_audit': 'No S1 node/python/java processes found at takeover', 'independent_acceptance': False}
progress['current'] = 'S1-15 obstacle route remediation'
progress['merge'] = 'not_started'
progress['independent_acceptance'] = False
for room in sorted(ROOT.glob('[12][0-9]-*')):
    candidate = next(room.glob('*.nbt'))
    digest = hashlib.sha256(candidate.read_bytes()).hexdigest()
    old_digest = hashlib.sha256((OLD / room.name / candidate.name).read_bytes()).hexdigest()
    assert digest == old_digest
    entry = progress['rooms']['S1-' + room.name[:2]]
    entry.update(candidate=str(candidate), sha256=digest, inherited_sha256=old_digest, maker_static_self_check='inherited_evidence_needs_path_rebind', independent_acceptance=False)
    for report in (room / 'qa').glob('*.json'):
        text = report.read_text(encoding='utf-8-sig')
        text = text.replace(str(OLD).replace('\\', '\\\\'), str(ROOT).replace('\\', '\\\\'))
        report.write_text(text, encoding='utf-8')
save(ROOT / 'progress.json', progress)
training = ROOT / '15-training' / 'design.json'
design = json.loads(training.read_text(encoding='utf-8'))
for step in range(7):
    rise = min(step, 6-step)
    design['targets'].append({'label': 'A-ramp-step-' + str(step), 'position': [23.5, 2.5 + rise * 0.5, 22.5 + step]})
for lane in range(3):
    design['targets'].append({'label': 'hurdle-lane-' + str(lane), 'position': [27.5, 2, 11.5 + lane*4]})
design['differences'].append('A-ramp seven surface waypoints checked on half-block grid; hurdle lanes checked, jumping fence bars remains untested.')
save(training, design)
print('Inherited ten NBT hashes unchanged; output paths rebound; added seven ramp and three hurdle-lane targets.')
