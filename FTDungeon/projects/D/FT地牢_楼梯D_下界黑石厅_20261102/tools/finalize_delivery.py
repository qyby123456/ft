import hashlib
import json
import subprocess
import sys
import zipfile
from datetime import datetime, timedelta, timezone
from pathlib import Path

from PIL import Image

from nbtio import Structure


ROOT = Path(__file__).resolve().parents[1]


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def read_json(relative):
    return json.loads((ROOT / relative).read_text(encoding='utf-8'))


def save_json(relative, value):
    (ROOT / relative).write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def main():
    timestamp = datetime.now(timezone(timedelta(hours=8))).isoformat(timespec='seconds')
    fingerprint = digest(ROOT / 'out/room.nbt')
    metadata = read_json('out/room.json')
    assert metadata['sha256'] == fingerprint
    result = subprocess.run([sys.executable, str(ROOT / 'tools/build_room.py')], cwd=ROOT, capture_output=True, text=True, encoding='utf-8')
    assert result.returncode == 0, result.stderr
    assert digest(ROOT / 'out/room.nbt') == fingerprint
    save_json('qa/reproducibility.json', {'status': 'PASS', 'timestamp_china': timestamp, 'generator_exit_code': result.returncode,
                                        'structure_sha256_before': fingerprint, 'structure_sha256_after': digest(ROOT / 'out/room.nbt'),
                                        'generator_summary': json.loads(result.stdout), 'deterministic_gzip_mtime': 0})
    static = read_json('qa/static.json')
    native = read_json('qa/native.json')
    captures = read_json('qa/capture-verification.json')
    renders = read_json('qa/render-manifest.json')
    for evidence in (static, native, captures):
        assert evidence['status'] == 'PASS' and evidence['structure_sha256'] == fingerprint
    assert renders['source_nbt_sha256'] == fingerprint
    assert len(native['rotations']) == 4 and native['records_checked_after_ticks'] == 88044 and native['stabilization_ticks'] == 200
    assert len(static['negative_probes']) == 7 and sum(native['negative_probes'].values()) == 20
    for rotation in native['rotations']:
        assert rotation['state_drift'] == 0 and rotation['survival_failures'] == 0 and rotation['sealed_lava_sources'] == 16
        assert rotation['undeclared_fluid_blocks'] == 0 and rotation['unexpected_world_entities'] == 0
        assert digest(ROOT / rotation['capture']) == rotation['capture_sha256']
    log = (ROOT / 'qa/forge-gametest.log').read_text(encoding='utf-8', errors='replace')
    assert 'STAIR_D_NATIVE_PASS rotations=4 records=88044 ticks=200 lava=64 source=' + fingerprint in log
    assert 'All 1 required tests passed' in log and 'BUILD SUCCESSFUL in 5m 49s' in log
    assert digest(ROOT / 'reference/design.png') == metadata['reference_sha256'] == digest(Path(metadata['reference']))
    room = Structure.read(ROOT / 'out/room.nbt')
    assert room.size == (29, 23, 33) and len(room.blocks) == 22011
    for output in renders['outputs']:
        path = ROOT / output['file']
        assert digest(path) == output['sha256']
        with Image.open(path) as image:
            assert list(image.size) == output['size']
            image.verify()
    save_json('qa/visual-review.json', {'status': 'PASS', 'timestamp_china': timestamp,
                                       'reviewed': ['out/preview.png', 'preview/four-views.png', 'preview/lower-section.png',
                                                    'preview/lower-profile.png', 'preview/lava-enclosure.png'],
                                       'method': 'Visual inspection of actual-NBT software renders and grid sections',
                                       'findings': ['Grand stair, gilt raised platform and upper stair present',
                                                    'Lower stair and corridor section exposes actual supported half-block descent',
                                                    'Lava closure section shows solid sides/bottom and full glass top; no open lava substituted for sealed design',
                                                    'Image titles and disclaimers distinguish conceptual reference, final NBT and native recapture'],
                                       'limitations': ['Software projection, not client screenshot', 'Banner, glass and light are approximations',
                                                       'Exact client lighting, full modpack gameplay and FPS not assessed']})
    save_json('qa/boundary-report.json', {'status': 'PASS', 'timestamp_china': timestamp, 'artifact_write_root': str(ROOT),
                                         'production_deployed': False, 'core_source_modified': False, 'player_save_opened': False,
                                         'other_room_modified': False, 'image_api_called': False, 'formal_registry_slot_assigned': False,
                                         'isolated_world': str(ROOT / 'qa/forge-run/world'),
                                         'test_guard': 'Server must be an instance of GameTestServer before fixture clearing or placement',
                                         'runtime_cache_note': 'Gradle shared dependency caches used normally; no blanket forensic comparison of unrelated project directories asserted'})
    evidence_paths = ['qa/static.json', 'qa/native.json', 'qa/capture-verification.json', 'qa/forge-gametest.log',
                      'qa/render-manifest.json', 'qa/visual-review.json', 'qa/reproducibility.json', 'qa/boundary-report.json']
    summary = {'status': 'PASS', 'timestamp_china': timestamp, 'id': metadata['id'], 'name': metadata['name'],
               'size': list(room.size), 'records': len(room.blocks), 'structure_sha256': fingerprint,
               'metadata_sha256': digest(ROOT / 'out/room.json'), 'preview_sha256': digest(ROOT / 'out/preview.png'),
               'reference_sha256': metadata['reference_sha256'], 'static_checks': static['check_count'], 'static_negative_rejections': 7,
               'native': {'minecraft': '1.20.1', 'forge': '47.4.10', 'java': native['java'], 'rotations': 4,
                          'stabilization_ticks': 200, 'records_checked_after_ticks': 88044, 'real_sealed_lava_sources_per_rotation': 16,
                          'state_drift': 0, 'fluid_leaks': 0, 'unexpected_entities': 0, 'negative_rejections': 20,
                          'native_body': [0.6, 1.8], 'route_sampling_blocks': 0.25, 'stairs_supported_per_rotation': 112,
                          'stair_lanes_bidirectionally_checked_per_rotation': 21},
               'interfaces': metadata['interfaces'], 'independent_world_capture_comparison': 'PASS', 'reproducibility': 'PASS',
               'integration': metadata['integration'], 'evidence': [{'file': relative, 'sha256': digest(ROOT / relative)} for relative in evidence_paths],
               'limitations': native['limitations'],
               'runtime_notes': ['Forge first startup generated default server.properties', 'Forge version check network timeout did not fail offline Gradle or GameTest',
                                 'One bulk fixture placement overload warning; no claim of client or multiplayer performance']}
    save_json('out/qa.json', summary)
    delivery = [{'file': str(ROOT / 'out' / name), 'bytes': (ROOT / 'out' / name).stat().st_size,
                 'sha256': digest(ROOT / 'out' / name)} for name in ('room.nbt', 'room.json', 'preview.png', 'qa.json')]
    handoff = ['# STAIR D · 下界黑石楼梯厅 · PASS', '', '独立候选，未部署、未注册槽位、未改核心或玩家存档。', '',
               '29×23×33；22,011记录；北入口(14,7,0)，内点(14,7,4)；南上出口(14,17,32)，南下出口(22,1,32)。',
               '静态292项+7负例；Forge四向，200 tick，88,044记录无漂移、16源/方向完全密封无外溢；20原生负例。',
               '四份200 tick世界回采已由独立脚本逐状态对照；NBT可确定性重建。', '', '## 四文件 SHA-256', '']
    handoff.extend(f"- `{entry['file']}`\n  - {entry['bytes']} B · `{entry['sha256']}`" for entry in delivery)
    handoff.extend(['', '## 证据', '', '- `qa/native.json` / `qa/forge-gametest.log` / `qa/native-placed-*.nbt`',
                    '- `qa/static.json` / `qa/capture-verification.json` / `qa/reproducibility.json`',
                    '- `preview/four-views.png` / `preview/native-world-capture.png` / `preview/lower-profile.png` / `preview/lava-enclosure.png`',
                    '', '软件渲染不是客户端截图；没有FT大厅路由/幂等/占用拒绝接入验收、正式部署、多人或FPS证据。',
                    '协调者需按主层+10/-6高差分别接上/下口，并对整个体积及周界做占用/流体/实体拒绝验证。'])
    (ROOT / 'HANDOFF.md').write_text('\n'.join(handoff) + '\n', encoding='utf-8')
    files = [ROOT / 'README.md', ROOT / 'HANDOFF.md', ROOT / 'build.gradle', ROOT / 'settings.gradle', ROOT / 'gradle.properties']
    for folder in ('out', 'preview', 'reference', 'src'):
        files.extend(path for path in (ROOT / folder).rglob('*') if path.is_file())
    files.extend(sorted((ROOT / 'tools').glob('*.py')))
    files.extend(path for path in (ROOT / 'qa').glob('*') if path.is_file() and path.name not in {'file-manifest.json', 'package-verification.json'})
    files = sorted(set(files))
    manifest = {'status': 'PASS', 'timestamp_china': timestamp, 'excludes': ['build', '.gradle', 'qa/forge-run', '__pycache__',
                                                                                     'self file-manifest.json', 'package-verification.json', 'delivery zip'],
                'files': [{'file': path.relative_to(ROOT).as_posix(), 'bytes': path.stat().st_size, 'sha256': digest(path)} for path in files]}
    save_json('qa/file-manifest.json', manifest)
    archive = ROOT / 'STAIR_D_NETHER_HALL-delivery.zip'
    with zipfile.ZipFile(archive, 'w', zipfile.ZIP_DEFLATED, compresslevel=6) as bundle:
        for path in [*files, ROOT / 'qa/file-manifest.json']:
            bundle.write(path, path.relative_to(ROOT).as_posix())
    with zipfile.ZipFile(archive) as bundle:
        assert bundle.testzip() is None
        for entry in manifest['files']:
            assert hashlib.sha256(bundle.read(entry['file'])).hexdigest() == entry['sha256']
    save_json('qa/package-verification.json', {'status': 'PASS', 'timestamp_china': timestamp, 'delivery': delivery,
                                               'zip': str(archive), 'zip_sha256': digest(archive), 'zip_bytes': archive.stat().st_size,
                                               'zip_members': len(files) + 1, 'every_manifest_member_bytes_verified': True,
                                               'formal_deployment': False})
    print(json.dumps({'status': 'PASS', 'delivery': delivery, 'zip': str(archive), 'zip_sha256': digest(archive)}, ensure_ascii=False, indent=2))


if __name__ == '__main__':
    main()
