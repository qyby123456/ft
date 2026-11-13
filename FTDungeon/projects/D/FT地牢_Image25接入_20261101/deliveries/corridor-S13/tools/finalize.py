import json
import zipfile
from datetime import datetime, timedelta, timezone
from pathlib import Path

from PIL import Image

from nbtio import Structure, plain
from prepare import digest, save


ROOT = Path(__file__).resolve().parents[1]


def evidence(path):
    return {'path': str(path.relative_to(ROOT)).replace(chr(92), '/'), 'sha256': digest(path), 'bytes': path.stat().st_size}


def main():
    metadata = json.loads((ROOT / 'room.json').read_text(encoding='utf-8'))
    static = json.loads((ROOT / 'qa/static.json').read_text(encoding='utf-8'))
    native = json.loads((ROOT / 'qa/native.json').read_text(encoding='utf-8'))
    render = json.loads((ROOT / 'qa/render-manifest.json').read_text(encoding='utf-8'))
    fingerprint = digest(ROOT / 'room.nbt')
    assert metadata['sha256'] == static['nbt_sha256'] == native['structure_sha256'] == render['source_nbt_sha256'] == fingerprint
    assert static['status'] == native['status'] == render['status'] == 'PASS'
    assert len(native['rotations']) == 4 and native['stabilization_ticks'] >= 300
    assert {entry['rotation'] for entry in native['rotations']} == {'NONE', 'CLOCKWISE_90', 'CLOCKWISE_180', 'COUNTERCLOCKWISE_90'}
    assert all(entry['status'] == 'PASS' for entry in native['rotations'])
    assert metadata['reference_sha256'] == digest(metadata['reference'])
    assert Path(native['isolated_world']).resolve().is_relative_to((ROOT / 'qa/forge-runs').resolve())
    source = Structure.read(ROOT / 'room.nbt')
    capture = Structure.read(ROOT / 'qa/native-placed-none.nbt')
    assert source.size == capture.size and source.blocks == capture.blocks
    assert len(capture.block_entities) == len(source.block_entities) == 8
    for position, data in source.block_entities.items():
        source_payload = plain(data)
        capture_payload = plain(capture.block_entities[position])
        assert source_payload['Patterns'] == capture_payload['Patterns']
    log = Path((ROOT / 'qa/latest-forge-log.txt').read_text(encoding='utf-8'))
    log_text = log.read_text(encoding='utf-8', errors='replace')
    assert 'BUILD SUCCESSFUL' in log_text and 'CORRIDOR_S13_NATIVE_PASS: rotations=4/4 records=10560 ticks=300 source=' + fingerprint in log_text
    for entry in native['rotations']:
        assert digest(ROOT / entry['world_capture']) == entry['world_capture_sha256']
    findings = {
        ('north', 'interior'): '北端入口及直通路清楚；四笼位于侧边，三宽主路无占用；柱梁连接连续。',
        ('south', 'interior'): '南端门栅升起至Y=5，脚位及三格净高不受阻；红砖纹样为实心地面。',
        ('west', 'interior'): '可见东侧旗帜、烛台、灯座和吊笼；灯座接墙、蜡烛有底座，笼链接木梁。',
        ('east', 'interior'): '可见西侧对应构件；近侧墙的预览剖除使挂旗脱离背景，最终NBT墙体完整。',
        ('north', 'exterior'): '北口中央开口及门楣完整，屋顶和两侧封闭，无多余接口。',
        ('south', 'exterior'): '南口与北口同轴同高，升起门栅未封路，顶部盖板连续。',
        ('west', 'exterior'): '西墙全长封闭、屋顶完整，无穿出外壳的家具或液体。',
        ('east', 'exterior'): '东墙全长封闭、屋顶完整，陈旧石材纹理连续。',
    }
    images = []
    assert len(render['views']) == 8
    for view in render['views']:
        path = Path(view['path'])
        assert digest(path) == view['sha256'] and view['source_nbt_sha256'] == fingerprint
        with Image.open(path) as image:
            image.load()
            assert image.size == (1500, 1200)
        images.append({**evidence(path), 'direction': view['direction'], 'kind': view['kind'], 'status': 'PASS', 'review': findings[(view['direction'], view['kind'])]})
    review = {
        'status': 'PASS', 'id': metadata['id'], 'reviewer': 'corridor-S13 producer / direct eight-image visual inspection',
        'review_scope': 'Producer review of actual final room.nbt; receiving coordinator must independently review frozen files.',
        'nbt_sha256': fingerprint, 'reference_sha256': metadata['reference_sha256'], 'images': images,
        'preview': evidence(ROOT / 'preview.png'), 'blockers': [],
        'reference_comparison': {'retained': ['灰黑陈旧石墙与裂砖地面', '血红地砖纹样及红底黑纹旗帜', '木梁铁链与四个空铁吊笼', '原版末地烛及仪式蜡烛', '蛛网与近端门拱'], 'adaptations': metadata['design_adaptations']},
        'revision': {'previous_nbt_sha256': 'f87b408891231fcdadd664ffd4e2d81a1b995133c48c078f3b5a87e66638aeb3', 'change': '红旗从Y=7下移至Y=5，改善其与吊笼的视觉分离；最终哈希已重新执行静态、八视图与Forge原生测试。'},
        'limits': render['limitations'],
    }
    save(ROOT / 'qa/four-view-review.json', review)
    test_evidence = {
        'status': 'PASS', 'id': metadata['id'], 'nbt_sha256': fingerprint, 'exit_code': 0,
        'actual_gametest_methods': 1, 'rotations_checked_in_method': 4, 'stable_ticks': 300,
        'rotation_results': native['rotations'], 'records_checked': 10560,
        'source_to_native_capture_equal': True, 'banner_patterns_roundtrip_equal': True,
        'report': evidence(ROOT / 'qa/native.json'), 'log': evidence(log),
        'world_directory': native['isolated_world'], 'isolated_world': True,
        'scope': 'One executed native GameTest method performs all four rotations; not four independent test methods or receiver acceptance.',
    }
    save(ROOT / 'qa/native-result.json', test_evidence)
    qa = {
        'id': metadata['id'], 'status': 'PASS', 'scope': 'Producer delivery: static + eight-image visual review + isolated native Forge QA',
        'nbt_sha256': fingerprint, 'reference_sha256': metadata['reference_sha256'],
        'static': {'status': 'PASS', 'report': evidence(ROOT / 'qa/static.json'), 'checks': static['check_count']},
        'four_view': {'status': 'PASS', 'report': evidence(ROOT / 'qa/four-view-review.json'), 'view_count': 8},
        'forge_native': {'status': 'PASS', 'report': evidence(ROOT / 'qa/native-result.json'), 'minecraft': '1.20.1', 'forge': '47.4.10', 'rotations': [0, 90, 180, 270], 'stable_ticks': 300, 'records': 10560},
        'producer_ready_for_receiver_review': True, 'ready_for_unified_integration': False,
        'receiver_acceptance': 'PENDING_INDEPENDENT_FREEZE_REVIEW_AND_RETEST',
        'deployment': 'NOT_DEPLOYED', 'production_save_accessed': False, 'blockers': [],
        'limitations': native['limitations'],
    }
    save(ROOT / 'qa.json', qa)
    quartet = {name: evidence(ROOT / name) for name in ('room.nbt', 'room.json', 'preview.png', 'qa.json')}
    import hashlib
    bundle = hashlib.sha256(json.dumps({name: value['sha256'] for name, value in quartet.items()}, sort_keys=True).encode()).hexdigest()
    handoff = {
        'id': metadata['id'], 'name': metadata['name'], 'delivery_directory': str(ROOT),
        'created_at': datetime.now(timezone(timedelta(hours=8))).isoformat(), 'nbt_sha256': fingerprint,
        'reference': metadata['reference'], 'reference_sha256': metadata['reference_sha256'],
        'four_piece_bundle_sha256': bundle, 'files': quartet,
        'size': metadata['size'], 'entrance': metadata['entrance'], 'interior': metadata['interior'], 'exit': metadata['exit'],
        'producer_status': 'COMPLETE', 'producer_qa': 'PASS', 'receiver_review_required': True,
        'ready_for_unified_integration': False, 'not_deployed': True,
        'template_candidate': metadata['template_candidate'], 'slot_status': 'coordinator_only_no_slot_or_catalog_changes',
    }
    save(ROOT / 'HANDOFF.json', handoff)
    selected = [ROOT / name for name in ('room.nbt', 'room.json', 'preview.png', 'qa.json', 'HANDOFF.json', 'README.md', 'summary.json', 'build.gradle', 'settings.gradle', 'gradle.properties')]
    for directory in ('source', 'tools', 'src', 'views'):
        selected.extend(path for path in (ROOT / directory).rglob('*') if path.is_file() and '__pycache__' not in path.parts)
    selected.extend(path for path in (ROOT / 'qa').iterdir() if path.is_file() and path.suffix in {'.json', '.nbt'})
    selected.append(log)
    selected = sorted(set(selected), key=lambda path: str(path.relative_to(ROOT)))
    checksum_text = ''.join(digest(path) + '  ' + str(path.relative_to(ROOT)).replace(chr(92), '/') + chr(10) for path in selected)
    (ROOT / 'sha256sums.txt').write_text(checksum_text, encoding='utf-8')
    archive = ROOT / 'corridor-S13-delivery.zip'
    with zipfile.ZipFile(archive, 'w', compression=zipfile.ZIP_DEFLATED) as output:
        for path in selected + [ROOT / 'sha256sums.txt']:
            output.write(path, str(path.relative_to(ROOT)))
    with zipfile.ZipFile(archive) as output:
        assert output.testzip() is None
    save(ROOT / 'archive.json', {'archive': evidence(archive), 'file_count': len(selected) + 1, 'nbt_sha256': fingerprint, 'four_piece_bundle_sha256': bundle, 'excludes': ['Gradle build/cache', 'isolated test worlds', 'superseded revision'], 'complete': True})
    print(json.dumps({'delivery': 'COMPLETE', 'qa': 'PASS', 'nbt_sha256': fingerprint, 'bundle_sha256': bundle, 'archive': str(archive)}, ensure_ascii=False))


if __name__ == '__main__':
    main()
