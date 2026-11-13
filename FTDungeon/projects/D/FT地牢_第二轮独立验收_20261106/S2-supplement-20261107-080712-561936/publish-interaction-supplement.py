import hashlib
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent
QA = ROOT / 'qa/interaction-range-current'
RUNTIME = ROOT / 'runtime-interaction-r2'


def read(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def write(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2), encoding='utf-8')


def record(path):
    return {'path': str(path), 'relative_path': path.relative_to(ROOT).as_posix(), 'sha256': digest(path), 'bytes': path.stat().st_size}


def main():
    source = ROOT / 'assembly/s2-desert-functional-combined.nbt'
    current_hash = digest(source)
    assert current_hash == '33ceb33f633cfa6e797cc184cd3c5ff34ce9f0901e9b909da161782ec5cc0e6c'
    original_manifest = read(ROOT / 'artifact-manifest.json')
    for artifact in original_manifest['artifacts']:
        path = Path(artifact['path'])
        assert path.is_file() and digest(path) == artifact['sha256'] and path.stat().st_size == artifact['bytes'], path
    ready = read(ROOT / 'ready-for-review.json')
    assert ready['artifact_manifest']['sha256'] == digest(ROOT / 'artifact-manifest.json')
    for label in ['current_source', 'real_server_posttick']:
        evidence = read(QA / (label+'-interactions.json'))
        assert evidence['merged_candidate_sha256'] == current_hash
        assert len(evidence['fixtures']) == 40 and all(entry['pass'] and entry['standing_clear_and_supported'] and entry['native_outline_first_hit_is_fixture'] and entry['eye_to_hit_distance'] <= 4.5 for entry in evidence['fixtures'])
        assert sum(entry['requires_jump'] for entry in evidence['fixtures']) == 1
    runtime = read(RUNTIME / 'interaction-runtime.json')
    assert runtime['source_sha256'] == current_hash and runtime['pass']
    assert read(RUNTIME / 'exit.json')['exit_code'] == 0
    fixtures = runtime['fixtures']
    assert len(fixtures) == 40 and all(entry['pass'] and entry['first_hit_is_fixture'] and entry['within_survival_reach'] and entry['body_clear_at_use'] for entry in fixtures)
    containers = [entry for entry in fixtures if entry['fixture']['kind'] == 'container']
    doors = [entry for entry in fixtures if entry['fixture']['kind'] == 'door_lever']
    assert len(containers) == 32 and all(entry['server_menu_opened'] and entry['occupied_slots'] == 0 for entry in containers)
    assert len(doors) == 8 and all(entry['door_opened'] and entry['second_ray_valid'] and entry['door_restored'] for entry in doors)
    jump = [entry for entry in fixtures if entry['requires_jump']]
    assert len(jump) == 1 and jump[0]['fixture']['position'] == [197, 11, 93]
    runtime_input = read(RUNTIME / 'interaction-input.json')
    assert runtime_input['fixture_evidence_sha256'] == digest(QA / 'real_server_posttick-interactions.json')
    summary = {'style': 'S2', 'group': 'S2', 'source_sha256': current_hash, 'status': 'PRODUCER_INTERACTION_EVIDENCE_PENDING_INDEPENDENT_REVIEW', 'frozen_original_manifest': record(ROOT / 'artifact-manifest.json'), 'original_declared_files_rehashed_unchanged': len(original_manifest['artifacts']), 'source_and_posttick_static_fixture_counts': [40, 40], 'standing_interactions': 39, 'native_jump_interactions': 1, 'containers_opened': 32, 'doors_opened_and_restored': 8, 'max_actual_eye_hit_distance': max(entry['eye_hit_distance'] for entry in fixtures), 'runtime_ticks': runtime['ticks'], 'jump_fixture': jump[0], 'fixture_geometry': [record(QA / (label+'-interactions.json')) for label in ['current_source', 'real_server_posttick']], 'runtime': record(RUNTIME / 'interaction-runtime.json'), 'scope': 'Offline full native body collision paths connect all dry supported initial stations to entrance. Real isolated world reenacts each local interaction at those stations, using real Player eye and Level.clip then gameMode.useItemOn. One decorative well barrel requires a vanilla jump; its initial dry station is reachable, but it is not a stationary standing interaction. Per-fixture positioning is explicitly teleported; this is not a continuous world walk to each fixture, human control, client GUI, or independent world rerun.', 'earlier_failed_attempt': str(ROOT / 'runtime-interaction-r1'), 'earlier_failure': 'Native 0.6000000238-wide player touched well wall at the initial 0.2 subgrid offset. Final 0.19 offset leaves clearance; offline body width/height now conservatively 0.600002 x 1.800001. Only QA station planning changed; no structure, furniture or target changes.', 'parent_closed_be_classification': '26 default brick-sherd pots + 1 idle empty furnace defaults + 3 empty lectern IDs; original payload fields unchanged. Local duplicate classification retained as auxiliary, not a new blocker.'}
    write(QA / 'supplement-summary.json', summary)
    text = '# 33ceb33f 门拉杆与容器交互范围补证\n\n当前合并体SHA-256：`'+current_hash+'`。只新增补证文件；原ready-for-review.json及artifact-manifest.json未重写，原498项声明文件已全部重新核对且哈希不变。十房、合并体、15片及原目标均未修改。\n\n刻前结构及实际服务器刻后NBT，各40件均有从入口连通的完整碰撞路径、起始站位、眼睛坐标、命中点/面和原生outline首命中判定。31容器及8拉杆可站立交互；井中链吊空桶需要跳起，不将其记作干地站立可触及。每次射线命中距离限制为原版生存4.5格，细站位偏移最多每轴0.19格且做11点身体及地面支撑检查。\n\n新建runtime-interaction-r2隔离Forge 1.20.1世界，放置同哈希完整合并体并自然运行600刻，再逐件定位至上述入口连通站位，实际检查原生玩家碰撞箱、地面支撑、Player.getEyePosition及Level.clip(OUTLINE, NONE)。只有首命中正确、距离<=4.5格且身体无遮挡时才调用ServerPlayerGameMode.useItemOn：32容器服务端菜单全部打开，库存全空；8牢门经真实射线两次操作全部开关复位。最大实际眼睛至命中距离为'+format(summary['max_actual_eye_hit_distance'],'.9f')+'格。\n\n悬桶在合并坐标[197,11,93]，原生jumpFromGround后使用Player.travel推进，未注入向上速度，未在跳跃中moveTo。实测脚坐标'+str(jump[0]['actual_feet'])+'，眼睛'+str(jump[0]['actual_eye'])+'；实际射线距离'+format(jump[0]['eye_hit_distance'],'.9f')+'格并成功打开BarrelMenu。实际跳跃轨迹在interaction-runtime.json/jump_trace。离线报告中planned_jump_rise=0.9只表示候选几何，不冒充实测跃高。\n\n首次runtime-interaction-r1因0.2格细站位贴墙触发原生碰撞失败，原日志和源码保留；最终站位退为0.19格，离线身体尺度也改为保守0.600002×1.800001，随后实服通过。没有通过改平家具、删除目标、移动桶或改变实体位置解决问题。\n\n范围：每件起始站位仍由操作员定位；完整入口路径由离线原生碰撞检查证明。本次实服验证局部伸手交互和一次跳跃，不等于真人客户端GUI、全程逐件走路或父线程独立世界复跑。整组仍待独立验收。\n\n入口：qa/interaction-range-current/supplement-summary.json；逐件路径及射线：current_source-interactions.json与real_server_posttick-interactions.json；实服输入、源码、控制台、exit与报告：runtime-interaction-r2；全部新增证据路径/字节数/哈希：qa/interaction-range-current/supplement-manifest.json。\n'
    note = ROOT / 'INTERACTION-SUPPLEMENT-33ceb33f.md'
    note.write_text(text, encoding='utf-8')
    files = [ROOT / name for name in ['interaction-audit.py', 'prepare-interaction-runtime.py', 'publish-interaction-supplement.py', note.name]]
    files.extend(path for path in QA.iterdir() if path.is_file() and path.suffix in {'.json', '.java', '.log'} and path.name != 'supplement-manifest.json')
    for folder_name in ['runtime-interaction-r1', 'runtime-interaction-r2']:
        folder = ROOT / folder_name
        files.extend(path for path in folder.iterdir() if path.is_file())
        files.extend(path for path in (folder / 'native-test').iterdir() if path.is_file())
        files.extend(path for path in (folder / 'native-test/src').rglob('*') if path.is_file())
        files.append(folder / 'server/server.properties')
    write(QA / 'supplement-manifest.json', {'source_sha256': current_hash, 'original_frozen_manifest_unchanged': record(ROOT / 'artifact-manifest.json'), 'supplement_artifacts': [record(path) for path in sorted(set(files))], 'excluded': ['manifest self hash', 'runtime caches, compiled classes and world chunk internals'], 'status': 'PENDING_INDEPENDENT_REVIEW'})
    print('SUPPLEMENT_PUBLISHED',current_hash,'files',len(set(files)),'32 menus 8 levers; 39 standing + 1 native jump')


if __name__ == '__main__':
    main()
