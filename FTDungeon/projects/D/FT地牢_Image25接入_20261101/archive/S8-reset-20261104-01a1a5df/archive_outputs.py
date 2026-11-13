import hashlib
import json
from pathlib import Path
import zipfile

ROOT = Path(__file__).resolve().parent
DELIVERIES = ROOT.parents[1] / 'deliveries'
NAMES = ['nature-b1-S8-crystal', 'nature-b1-S8-crystal-r2', 'nature-S8-sequential']
THREAD = '01a1a5df-ead1-7fa3-ba71-2cf0013e1e97'


def digest(target):
    return hashlib.sha256(target.read_bytes()).hexdigest()


def create_json(target, value):
    with target.open('x', encoding='utf-8') as stream:
        json.dump(value, stream, ensure_ascii=False, indent=2)
        stream.write('\n')


def main():
    assert not (ROOT / 'archive-manifest.json').exists(), 'Archive already exists; do not overwrite'
    records = []
    for name in NAMES:
        folder = (DELIVERIES / name).resolve(strict=True)
        assert folder.parent == DELIVERIES.resolve(strict=True)
        marker = folder / 'NOT_A_CANDIDATE.json'
        assert not marker.exists(), 'Do not overwrite an existing status marker'
        originals = []
        for target in sorted(folder.rglob('*')):
            if target.is_file():
                assert target.resolve().is_relative_to(folder), 'Unexpected external file link'
                originals.append({'relative_path': target.relative_to(folder).as_posix(), 'bytes': target.stat().st_size, 'sha256': digest(target)})
        create_json(marker, {'status': 'ARCHIVED_REJECTED_NOT_A_CANDIDATE', 'candidate_eligible': False, 'runtime_eligible': False, 'reason': 'User instructed entire batch to be redone from zero; do not reuse these structures or treat previous PASS checks as batch acceptance.', 'thread': THREAD, 'archive_root': str(ROOT), 'supersedes_old_candidate_status': True, 'instruction': 'Keep original files unchanged; do not load, integrate, dispatch or deploy. Wait for coordinator to assign a new batch and specific reference image.'})
        archive = ROOT / (name + '.zip')
        with zipfile.ZipFile(archive, 'x', compression=zipfile.ZIP_DEFLATED, compresslevel=6) as bundle:
            for entry in originals:
                bundle.write(folder / entry['relative_path'], name + '/' + entry['relative_path'])
            bundle.write(marker, name + '/' + marker.name)
        with zipfile.ZipFile(archive) as bundle:
            assert bundle.testzip() is None, 'ZIP CRC failed'
            for entry in originals:
                archived = bundle.read(name + '/' + entry['relative_path'])
                assert hashlib.sha256(archived).hexdigest() == entry['sha256']
                assert digest(folder / entry['relative_path']) == entry['sha256'], 'Original output changed'
        records.append({'source_root': str(folder), 'candidate_eligible': False, 'archive': str(archive), 'archive_sha256': digest(archive), 'original_files': len(originals), 'original_bytes': sum(entry['bytes'] for entry in originals), 'original_files_unchanged': True, 'zip_crc_and_per_file_sha256': 'PASS', 'files': originals})
    create_json(ROOT / 'archive-manifest.json', {'status': 'ARCHIVED_REJECTED_NOT_A_CANDIDATE', 'thread': THREAD, 'date': '2026-11-04', 'timezone': 'Asia/Shanghai', 'candidate_eligible': False, 'waiting_for': ['coordinator-assigned new batch', 'specific reference image', 'current small-batch acceptance before continuing'], 'new_batch_started': False, 'deployment': 'NOT_DEPLOYED', 'preview_services': {'5288': 'STOPPED', '5388': 'STOPPED', '5488': 'STOPPED'}, 'roots': records})
    with (ROOT / 'README.md').open('x', encoding='utf-8') as stream:
        stream.write('# 第8组旧输出封存：禁止进入候选\n\n本目录保存本线程三套旧输出的原样ZIP；逐文件SHA256与原件一致，原目录未删除、未移动、原有文件未覆盖。每个原目录新增NOT_A_CANDIDATE.json，明确撤销旧候选状态；以前的PASS仅是旧文件校验证据，不是验收或接入许可。\n\n不允许把这些结构、预览或元数据复用为新批次模板、候选或正式内容。旧预览5288/5388/5488已关闭。未修改Mod、玩家存档、原参考图片或其他线程产物。\n\n等待协调线程分配新批次及具体参考图，届时从零逐图生成，一个结构完成NBT、预览、连接点、原生加载及重复度自检，当前小批验收后才继续。当前没有启动新批次。\n\narchive-manifest.json包含原始文件路径/大小/SHA256、ZIP哈希及CRC和逐文件验证结果。\n')
    print(json.dumps({'status': 'ARCHIVED_REJECTED_NOT_A_CANDIDATE', 'root_count': len(records), 'original_file_count': sum(record['original_files'] for record in records), 'originals_preserved': True, 'manifest': str(ROOT / 'archive-manifest.json'), 'waiting_for_new_assignment': True}, ensure_ascii=False))


if __name__ == '__main__':
    main()
