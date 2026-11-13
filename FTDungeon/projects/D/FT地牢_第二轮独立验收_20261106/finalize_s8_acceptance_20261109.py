import hashlib
import json
from datetime import datetime
from pathlib import Path
from audit_s8_submission import load

review = Path('D:/ft工程文件/FT地牢_第二轮独立验收_20261106')
workspace = Path('C:/Users/YOUR_USERNAME/.codex/visualizations/2026/11/04/01a1a685-045b-76d2-aa2b-2a3f24520603')
snapshots = workspace/'preserved-review-snapshots'
root = snapshots/'S8-vault-shell-revision-20261108'
prior = snapshots/'S8-interaction-revision-20261107-1355'
digest = '763608faa6bc65fc85f8483912b7df6aafbbf84ee207a97ec2db0e3795c9775d'
assert hashlib.sha256((root/'assembly/s8-functional-vault-shell.nbt').read_bytes()).hexdigest() == digest
revision = load(root/'REVISION.json')
assert revision['sha256'] == digest and revision['baseline_sha256'] == '6988e265a3fc6f89ce8a1b507088ffe8da664a38b28bcf8058b08eedc02d3fee'
frozen_counts = {}
for directory,names in [(root,['submission-snapshot.json']),(prior,['submission-snapshot.json']),(snapshots/'S8-submission-20261107-104351-507543',['submission-snapshot.json','supplemental-snapshot.json']),(snapshots/'S8-followup-20261107-1600',['submission-snapshot.json']),(snapshots/'S8-runtime-supplement-20261107-1340',['submission-snapshot.json']),(snapshots/'S8-install-supplement-20261107-1125',['submission-snapshot.json'])]:
    count = 0
    for name in names:
        for item in load(directory/name)['files']:
            actual = Path(item['snapshot'])
            assert actual.is_file() and hashlib.sha256(actual.read_bytes()).hexdigest() == item['sha256'],actual
            count += 1
    frozen_counts[directory.name] = count
assert frozen_counts[root.name] == 197 and sum(frozen_counts.values()) == 1105
completion = load(prior/'completion-audit.json')
assert len(completion['requirements']) == 12 and sum(item['status']=='PENDING_NOT_PROVEN' for item in completion['requirements']) == 1
delta = load(root/'independent-delta-parts-dependencies.json')
assert delta['source_sha256'] == digest and delta['changes'] == 1941 and delta['dependency_intersections'] == 0
assert len(delta['unchanged_nine_rooms']) == 9 and delta['preserved_targets'] == 58
assert delta['barrier_cells'] == 90282 and delta['preserved_block_entities'] == 56 and delta['parts'] == 20
reproduction = load(root/'independent-current-reproduction.json')
assert reproduction['source_sha256'] == digest and reproduction['all31_binary_equal'] and not reproduction['current_candidate_used_as_recipe_input']
qa = root/'independent-current-qa'
support = load(qa/'independent-all-support.json')
native = load(qa/'independent-native-frozen.json')
routes = load(qa/'independent-routes.json')
assert len(support) == len(native) == len(routes) == 4
assert all(not row['failures'] and not row['unsupported'] for row in support)
assert all(row['registry_properties']==row['template_roundtrip_block_count']=='PASS' and not row['survival_failures'] and not row['survival_unsupported'] for row in native)
assert all(row['start_clear'] and all(target['standing_clear'] and target['reachable'] for target in row['targets']) for row in routes)
assert sum(row['nonair_checked'] for row in support) == 189050 and sum(len(row['targets']) for row in routes) == 70
old_geometry = snapshots/'S8-submission-20261107-104351-507543/independent-geometry'
geometry_count = 0
for module in revision['modules']:
    samples = load(old_geometry/module['id']/'geometry-report.json')['samples']
    assert all(sample['native_clear'] for sample in samples)
    geometry_count += len(samples)
    for sample in samples:
        if module['id'] == 'S8-20':
            bounds = sample['bounds']
            for change in revision['changes']:
                point = change['local_position']
                assert not all(max(point[axis],bounds[axis]) < min(point[axis]+1,bounds[axis+3]) for axis in range(3))
assert geometry_count == 47
runtime = workspace/'s8-independent-vault-install-20261109'
run = load(root/'independent-install-run.json')
assert run['status']=='FINISHED' and run['exit_code']==0 and run['source_sha256']==digest
assert load(runtime/'execution-result.json')['pass']
clear = load(runtime/'clear-result.json')
assert clear['seeded_stone_cells']==clear['checked_cells']==1009200 and clear['remaining_non_air']==0
assert len(clear['commands'])==41 and sum(row['result'] for row in clear['commands'][:-1])==1009200
assert all(row['permission_2'] and row['source_player']=='S8InstallHost' for row in clear['commands'])
installed = load(runtime/'installation-result.json')
assert installed['source_sha256']==digest and installed['full_volume_checked']==1009200 and not installed['failures']
assert installed['source_records']==200453 and installed['implicit_air_verified']==808747 and installed['barriers']==90282
assert installed['block_entity_payloads_expected']==installed['block_entity_payloads_verified']==56
assert len(installed['neighbor_shape_changes'])==57 and len(load(runtime/'bed-normalization.json'))==12
access = load(runtime/'access-result.json')
assert access['barriers_before']==access['barriers_after']==90282 and len(access['access'])==4
for index,row in enumerate(access['access']):
    assert row['command_result']==1 and row['destination_correct'] and row['body_collision_free'] and row['supported_floor']
    assert row['function']==('s8_admin:enter' if index%2==0 else 's8_admin:return')
    if index>=2:
        assert not row['guest_has_permission_2']
saved = load(workspace/'s8-independent-vault-saved-readback-20261109/saved-readback.json')
assert saved['pass'] and saved['source_sha256']==digest and saved['chunks_read']==150 and saved['full_volume_checked']==1009200
assert saved['payloads_verified']==56 and saved['barriers']==90282 and not saved['failures']
assert len(saved['neighbor_shape_changes'])==57 and len(saved['bed_normalizations'])==12
actual_visual = load(review/'S8-ACTUAL-VISUAL-REVIEW-20261108.json')
for room in actual_visual['rooms']:
    if room['id']!='S8-20':
        assert room['status']=='PASS_SCOPED_VISUAL' and room['visual_inspected']
        assert hashlib.sha256(next((root/room['id']).glob('*.nbt')).read_bytes()).hexdigest()==room['source_sha256']
render = load(root/'S8-20/render-evidence.json')
assert render['sha256']==revision['new_room_sha256']
assert hashlib.sha256((root/'S8-20/reference.png').read_bytes()).hexdigest()=='a77e3a8f14eb79a2789725745423c6dae0511fb66601b67b298c95e60bdff111'
observed = ['reference.png','complete.png','crown-detail.png','cutaway.png','interior.png']
visual = dict(status='PASS_SCOPED_ACTUAL_VISUAL_REVIEW',source_sha256=digest,room_sha256=revision['new_room_sha256'],reviewed_at=datetime.now().isoformat(),actual_observation=True,images=[dict(path=str(root/'S8-20'/name),sha256=hashlib.sha256((root/'S8-20'/name).read_bytes()).hexdigest()) for name in observed],observation='实际完整图与晶冠近景可见高低错落的多组紫晶柱/芽，根部有方解石环带，上部岩肩由起伏阶梯取代旧大片平顶；剖切显示厚通道墙和岩肩关系，室内箱龛、两侧金块、晶簇、炉纹台阶仍可辨。原先平顶及宽扁晶体叠层的局部退修项关闭。下部矩形占地和厚直墙按保留约束未改，不声称逐像素复刻概念图。13/16/19沿用前次实际观察且本轮字节相同，其他六房沿用已冻结的旧逐房观察。')
(root/'independent-visual-review.json').write_text(json.dumps(visual,ensure_ascii=False,indent=2),encoding='utf-8')
requirements = []
for original in completion['requirements']:
    item = dict(original)
    if item['status']=='PENDING_NOT_PROVEN':
        item.update(status='PASS',evidence=[str(root/'independent-visual-review.json'),str(review/'S8-ACTUAL-VISUAL-REVIEW-20261108.json')],scope='Actual source-bound observations; current20 local correction and byte-identical13/16/19 covered')
        item.pop('reason',None)
    else:
        item['status']='PASS_WITH_EXPLICIT_CURRENT_DELTA_AND_PRIOR_SCOPE'
        item['current_delta_evidence']=str(root/'independent-delta-parts-dependencies.json')
    requirements.append(item)
report = dict(group='S8',accepted=True,status='ACCEPTED',completed_at=datetime.now().isoformat(),source_sha256=digest,room20_sha256=revision['new_room_sha256'],frozen_file_counts=frozen_counts,rooms=10,parts=20,full_volume=1009200,barrier_cells=90282,preserved_block_entities=56,reproduced_exact_nbt=31,native_support_checks_current=189050,native_route_targets_current=70,geometry_samples_inherited=47,actual_eye_rays_inherited=168,functional_targets_inherited=105,actual_menu_open_count_inherited=38,natural_added_facility_updates_inherited=1082,old_whole_map_waypoints_inherited=4413,source_delta_cells=1941,unchanged_rooms=9,requirements=requirements,current_package_installation=str(runtime),persisted_world_readback=str(workspace/'s8-independent-vault-saved-readback-20261109/saved-readback.json'),unresolved_required_items=[],production_deployed=False,limits=['Building acceptance only under second-round WORK_ORDER; combat/spawn/elite/Boss and generator owned by separate thread','Named admin and admin-hosted nonOP guest native enter/return; no autonomous permission0 hook or network client UI certification','Current7636 full installation and saved readback independently rerun; previous105 interactions/168 eye rays and1082 natural course updates inherited only after actual parent-trace coordinate/swept-body/target/fluid dependencies proved disjoint','Old4d 600-tick wait and4413-waypoint whole-map traversal retain old source scope; no claim of current7636 whole-map continuous natural walkthrough','57 lawful neighbor shape changes and12 bed legacy color normalizations retained explicitly; upper roof illumination intentionally changed','Reference concept contains stylized forms; current voxel adaptation passes feature/proportion scope, not pixel identity; original reference archive retention is separately audited'])
(root/'ACCEPTANCE.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
text=['# S8独立验收通过','', '当前精确候选：'+digest,'','十个11—20功能房及合并、20分片通过本轮建筑验收。20号的阶梯岩壳与顶部晶体已实际看图核对，其他九房字节保持。','', '新源31NBT独立重建一致；189050项原生支撑和70目标通过；新ZIP全1009200格实心污染/清空/安装、具名管理员及非OP访客托管进出通过，关服后150区块/56实体/90282屏障回读通过。','', '1941格上部装饰改动与父线程原交互射线、身体节点/扫掠、必要自然运动和流体依赖零交集；原105功能目标/168眼位use、1082新增设施自然更新及旧全图证据按明确源范围继承。未声称当前全图自然重走或客户端实测。','', '1105当前与继承冻结文件重核；详细12项要求、证据和限制见ACCEPTANCE.json。未修改正式实例或生成器。']
(root/'ACCEPTANCE.md').write_text(chr(10).join(text),encoding='utf-8')
print(json.dumps(dict(group='S8',accepted=True,source_sha256=digest,frozen_files=sum(frozen_counts.values()),requirements=len(requirements),support=189050,routes=70),ensure_ascii=False))
