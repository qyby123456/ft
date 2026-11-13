import hashlib
import html
import json
from pathlib import Path
import zipfile
from urllib.parse import quote

ROOT = Path(__file__).resolve().parent


def write(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    manifest = json.loads((ROOT / 'manifest.json').read_text(encoding='utf-8'))
    native = json.loads((ROOT / 'qa/native-review.json').read_text(encoding='utf-8'))
    assert native['status'] == 'PASS'
    cards = []
    hashes = []
    for room in manifest['rooms']:
        folder = ROOT / 'incoming' / room['id']
        assert sha(folder / 'room.nbt') == room['sha256']
        report = next(entry for entry in native['rooms'] if entry['id'] == room['id'])
        assert report['status'] == 'PASS' and report['sha256'] == room['sha256']
        qa = json.loads((folder / 'qa.json').read_text(encoding='utf-8'))
        qa['native_validation'] = {'status': 'PASS', 'report': str(ROOT / 'qa/native-review.json'), 'sha256': report['sha256']}
        qa['generated_views'] = [str(path) for path in sorted((folder / 'views').glob('*.png'))]
        assert len(qa['generated_views']) == 5
        write(folder / 'qa.json', qa)
        nbt_url = '/@fs/' + (folder / 'room.nbt').as_posix()
        url = 'http://127.0.0.1:5277/?nbt=' + quote(nbt_url, safe='') + '&name=' + quote(room['id'])
        room['lodestone_url'] = url
        cards.append('<article><img src="incoming/' + room['id'] + '/preview.png" alt="实际NBT剖视"><h2>' + html.escape(room['id'] + ' ' + room['name']) + '</h2><p>' + ' × '.join(map(str, room['size'])) + '</p><a href="' + html.escape(url, quote=True) + '">打开 Lodestone 交互预览</a></article>')
        hashes.append(room['reference_sha256'])
    hashes.append(manifest['reference_overview_sha256'])
    fingerprint = hashlib.sha256(('S7-first-batch-01-10\n' + '\n'.join(hashes)).encode()).hexdigest()
    manifest['group_fingerprint'] = fingerprint
    manifest['source_evidence'] = str(ROOT / 'qa/source-evidence.json')
    manifest['revision'] = 'differentiated-v2'
    manifest['revision_fingerprint'] = hashlib.sha256('\n'.join(room['sha256'] for room in manifest['rooms']).encode()).hexdigest()
    manifest['design_identity'] = str(ROOT / 'DESIGN_IDENTITY.json')
    manifest['design_audit'] = str(ROOT / 'qa/design-audit.json')
    write(ROOT / 'manifest.json', manifest)
    (ROOT / 'preview.html').write_text('<!doctype html><html lang="zh-CN"><meta charset="utf-8"><title>第7组 · 蘑菇真菌地牢</title><style>body{margin:32px;background:#111820;color:#e9e7dd;font-family:system-ui}main{display:grid;grid-template-columns:repeat(3,1fr);gap:24px}article{background:#1b2730;padding:15px;border-radius:12px}img{width:100%}a{color:#a9dfbd}h2{font-size:20px}</style><h1>第7组 · 9间实际Minecraft结构</h1><p>先运行 Start-LodestonePreview.ps1。图片为最终NBT剖视；交互预览仅显示模板，不执行玩法。</p><main>' + ''.join(cards) + '</main></html>', encoding='utf-8')
    handover = {'status': 'CANDIDATE_READY_FOR_REVIEW', 'thread_id': manifest['thread_id'], 'group': manifest['group'], 'group_fingerprint': fingerprint,
                'manifest': str(ROOT / 'manifest.json'), 'source_evidence': str(ROOT / 'qa/source-evidence.json'),
                'revision': manifest['revision'], 'revision_fingerprint': manifest['revision_fingerprint'],
                'design_identity': str(ROOT / 'DESIGN_IDENTITY.json'), 'design_audit': str(ROOT / 'qa/design-audit.json'),
                'room_ids': [room['id'] for room in manifest['rooms']], 'static': '9/9 PASS', 'native': '9/9 PASS',
                'rendered_views': 45, 'world_placement': 'NOT RUN', 'forge_gametest': 'NOT RUN', 'deployed': False,
                'pending': ['coordinator assigns unique world slots', 'isolated Forge actual placement and rotation/update stability', 'final per-room four-view acceptance and integrated route validation', 'all-group frozen topology/route repetition matrix', 'pressure-plate note trigger/reset world test'],
                'new_image_api_calls': 0, 'image_cleanup_required': False}
    write(ROOT / 'HANDOVER.json', handover)
    files = [path for path in ROOT.rglob('*') if path.is_file() and path.name != 'SHA256SUMS.json' and path.suffix != '.zip' and '__pycache__' not in path.parts and 'native-classes' not in path.parts]
    write(ROOT / 'SHA256SUMS.json', {str(path.relative_to(ROOT)): sha(path) for path in sorted(files)})
    archive = ROOT / 'NAT7-Fungal-Grove-FirstBatch.zip'
    with zipfile.ZipFile(archive, 'w', compression=zipfile.ZIP_DEFLATED) as package:
        for path in [*files, ROOT / 'SHA256SUMS.json']:
            package.write(path, path.relative_to(ROOT))
    with zipfile.ZipFile(archive) as package:
        assert package.testzip() is None
        sums = json.loads(package.read('SHA256SUMS.json'))
        for filename, expected in sums.items():
            assert hashlib.sha256(package.read(filename.replace('\\', '/'))).hexdigest() == expected
    print(json.dumps({'rooms': len(manifest['rooms']), 'native': native['status'], 'group_fingerprint': fingerprint, 'archive': str(archive), 'archive_sha256': sha(archive)}, ensure_ascii=False))


if __name__ == '__main__':
    main()
