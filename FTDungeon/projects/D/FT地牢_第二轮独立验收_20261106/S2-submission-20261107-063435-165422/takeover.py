import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys

sys.dont_write_bytecode = True
ROOT = Path(__file__).resolve().parent
OLD = Path('D:/ft工程文件/FT地牢_功能房间第二轮_20261106/S2')
AUDIT = Path('D:/ft工程文件/FT地牢_第二轮独立验收_20261106')
JAVA = 'C:/Program Files/Common Files/Oracle/Java/javapath/java.exe'
JAVAC = 'C:/Program Files/Common Files/Oracle/Java/javapath/javac.exe'


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def write(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2), encoding='utf-8')


def initialize():
    evidence = ROOT / 'inherited-evidence'
    for snapshot in AUDIT.glob('S2-*'):
        for filename in ['visual-review.md', 'snapshot.json']:
            source = snapshot / filename
            if source.exists():
                target = evidence / snapshot.name
                target.mkdir(exist_ok=True)
                shutil.copyfile(source, target / filename)
    references = json.loads((ROOT / 'references.json').read_text(encoding='utf-8-sig'))
    references['inherited_output_directory'] = references['output_directory']
    references['output_directory'] = str(ROOT)
    write(ROOT / 'references.json', references)
    inventory = []
    for folder in sorted(ROOT.glob('[12][0-9]-*')):
        design = json.loads((folder / 'design.json').read_text(encoding='utf-8'))
        nbt = folder / design['nbt']
        files = []
        for path in sorted(folder.rglob('*')):
            if path.is_file():
                files.append({'file': str(path.relative_to(ROOT)), 'sha256': digest(path), 'bytes': path.stat().st_size})
        inventory.append({'room': folder.name, 'source_sha256': digest(nbt), 'files': files})
    write(evidence / 'frozen-copy-manifest.json', inventory)
    write(ROOT / 'progress.json', {'style': 'S2', 'phase': 'TAKEOVER_REVERIFY', 'writes_scope': str(ROOT), 'inherited_source': str(OLD), 'independent_acceptance': 'PENDING', 'current': '11-library', 'rooms': [{'folder': item['room'], 'candidate_sha256': item['source_sha256'], 'status': 'INHERITED_PENDING_REVALIDATION'} for item in inventory]})


def verify(room):
    folder = ROOT / room
    result = subprocess.run([sys.executable, '-B', str(ROOT / 'check.py'), room], cwd=ROOT)
    if result.returncode:
        raise RuntimeError(f'{room} self-check failed')
    qa = folder / 'qa'
    classpath = (qa / 'classpath.txt').read_text(encoding='utf-8')
    classes = ROOT / 'qa/classes'
    compiled = subprocess.run([JAVAC, '-encoding', 'UTF-8', '-cp', classpath, '-d', str(classes), str(AUDIT / 'IndependentAllSupportAudit.java')], cwd=qa, capture_output=True, creationflags=subprocess.CREATE_NO_WINDOW)
    (qa / 'compile-all-support.log').write_bytes(compiled.stdout + compiled.stderr)
    if compiled.returncode:
        raise RuntimeError('all-support compile failed')
    command = [JAVA, '-Xmx1536M', '-Djava.awt.headless=true', '-cp', str(classes) + os.pathsep + classpath, 'IndependentAllSupportAudit', str(qa)]
    result = subprocess.run(command, cwd=qa, capture_output=True, creationflags=subprocess.CREATE_NO_WINDOW)
    (qa / 'all-support-producer.log').write_bytes(result.stdout + result.stderr)
    if result.returncode:
        raise RuntimeError('all-support failed')
    reports = json.loads((qa / 'independent-all-support.json').read_text(encoding='utf-8'))
    assert all(not report['failures'] and not report['unsupported'] for report in reports), reports
    write(qa / 'takeover-verification.json', {'source_sha256': digest(folder / json.loads((folder / 'design.json').read_text(encoding='utf-8'))['nbt']), 'producer_run': True, 'independent_acceptance': False, 'all_support_tool_sha256': digest(AUDIT / 'IndependentAllSupportAudit.java'), 'argv': command, 'exit_code': result.returncode})
    print(room, 'TAKEOVER_NATIVE_VERIFIED', flush=True)


def record(room, observation):
    folder = ROOT / room
    design = json.loads((folder / 'design.json').read_text(encoding='utf-8'))
    sha256 = digest(folder / design['nbt'])
    render = json.loads((folder / 'qa/render.json').read_text(encoding='utf-8'))
    native = json.loads((folder / 'qa/takeover-verification.json').read_text(encoding='utf-8'))
    assert render['source_sha256'] == native['source_sha256'] == sha256
    reference = next(entry for entry in json.loads((ROOT / 'references.json').read_text(encoding='utf-8'))['references'] if entry['id'] == design['id'])
    assert digest(Path(reference['image_path'])) == reference['sha256']
    write(folder / 'qa/visual-review.json', {'source_sha256': sha256, 'reference_sha256': reference['sha256'], 'reference_prompt_sha256': digest(Path(reference['prompt_path'])), 'reviewer_role': 'TAKEOVER_PRODUCER', 'visual_review': observation, 'status': 'PRODUCER_SELF_CHECK_ONLY_PENDING_INDEPENDENT_REVIEW', 'views': {view['id']: digest(folder / (view['id'] + '.png')) for view in render['views']}, 'differences': design['differences']})
    progress = json.loads((ROOT / 'progress.json').read_text(encoding='utf-8'))
    for entry in progress['rooms']:
        if entry['folder'] == room:
            entry.update(candidate_sha256=sha256, status='TAKEOVER_SELF_CHECKED_PENDING_INDEPENDENT_REVIEW')
    progress['current'] = room
    progress['completed_takeover_self_checked'] = sum(entry['status'].startswith('TAKEOVER_SELF_CHECKED') for entry in progress['rooms'])
    write(ROOT / 'progress.json', progress)
    (ROOT / 'README.md').write_text('# S2 接手续作\n\n继承十房独立候选，旧目录只读；历史报告和完整文件哈希在 inherited-evidence。\n\n' + f"接手原生及目检复核完成 {progress['completed_takeover_self_checked']}/10。\n\n" + '\n'.join(f"- {entry['folder']}: {entry['status']} / {entry.get('candidate_sha256', entry.get('sha256'))}" for entry in progress['rooms']) + '\n\n独立验收未通过；实际世界放置、邻居/流体刻及生存玩法未验证。20号历史目检哈希失配，按实际9e363d版本重新核验。未修改正式世界、mods、config、KubeJS。\n', encoding='utf-8')


if __name__ == '__main__':
    if sys.argv[1] == 'initialize':
        initialize()
    elif sys.argv[1] == 'verify':
        for room in sys.argv[2:]:
            verify(room)
    elif sys.argv[1] == 'record':
        record(sys.argv[2], sys.argv[3])
