import hashlib
import json
from datetime import datetime, timezone
from pathlib import Path

root = Path(__file__).resolve().parent
def load(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))
def save(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2), encoding='utf-8')
def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

ancestry = load(root / 'revision-parent.json')
assert all(digest(Path(entry['source'])) == entry['sha256'] for entry in ancestry['sources'])
assembly = load(root / 'assembly/assembly.json')
continuous = load(root / 'assembly/qa/continuous-width.json')
sampled = load(root / 'assembly/qa/full-width-routes/summary.json')
world = load(root / 'worldqa-validated/world-report.json')
assert continuous['status'] == 'AUTHOR_CONTINUOUS_CONNECTOR_PASS'
assert continuous['sha256'] == assembly['sha256'] == sampled['sha256']
assert sampled['status'] == 'SELF_CHECK_PASS'
assert load(root / 'worldqa-validated/exit.json')['exit_code'] == 0
assert world['status'] == 'EXECUTED_NO_LISTED_FAILURES'
assert all(entry['identical'] and entry['semantic_payload_equal'] for entry in load(root / 'reproduction.json'))
updates = {
    11: '加厚洞口石土门框、入口基脚，保留书架与攀梯；补上层落脚视图。',
    12: '六间牢房加厚石柱、木梁与菌土顶；每扇铁门内外各设拉杆，原生红石开关检验。',
    13: '炉体左右岩石扶壁、分层烟罩、加厚洞口，保留备餐桌与炉具。',
    14: '加厚武器架立柱与顶冠、加厚门框，保留三具完整装备盔甲架。',
    16: '五处阶梯石土基脚、小菌与垂根，池角石座及可走半级侧台阶。',
    17: '洞口纵深与货架上方菌岩挑檐，保留未完工项目台及材料工具。',
    18: '加厚龛口石柱横梁与菌土顶，石棺角部侧向凸石，增加地面小菌。'
}
rows = []
for number in range(11, 21):
    room = root / f'S7-{number}'
    summary = load(room / 'summary.json')
    candidate = Path(summary['nbtPath'])
    sha = digest(candidate)
    check = load(room / 'qa/self-check.json')
    assert check['status'] == 'SELF_CHECK_PASS' and check['sha256'] == sha
    report = next(entry for entry in world['rooms'] if entry['id'] == room.name)
    assert report['sha256'] == sha
    changed = number in updates
    if not changed:
        assert next(entry for entry in ancestry['sources'] if entry['id'] == room.name)['sha256'] == sha
    design = load(room / 'design.json')
    if changed:
        design['revision_4'] = {'changes': updates[number], 'world_evidence': str(root / 'worldqa-validated/world-report.json'), 'scope': '制作方检查，等待独立复核；服务器模拟交互不等于真人客户端遍历。'}
    design['inherited_reference_scope'] = 'Earlier revision_2 and takeover provenance fields were copied by the revision initializer and may contain relocated historical paths. Authoritative ancestry is revision-parent.json with exact source hashes; do not treat absent relocated history paths as new evidence.'
    save(room / 'design.json', design)
    rows.append({'id': room.name, 'candidate': str(candidate), 'sha256': sha, 'size': summary['size'], 'changed_in_v4': changed, 'changes': updates.get(number, '保持v3已独立复核候选哈希'), 'qa': str(room / 'qa/self-check.json'), 'world_report': str(root / 'worldqa-validated/world-report.json'), 'images': [str(path) for path in sorted(room.glob('*.png'))]})
assert next(entry for entry in world['rooms'] if entry['id'] == 'S7-assembly')['sha256'] == assembly['sha256']
pending = ['当前v4七房外形、全部源房间及合并世界报告的独立复核。', '真人客户端完整遍历、图书馆攀梯上层、训练障碍跳跃与宝库到达后的实际交互。', '本轮世界验证仅零旋转；其他朝向未认证，交付范围限制为零旋转。', '当前原生服务器交互使用Forge FakePlayer，不声明真人客户端或网络包链路通过。']
save(root / 'review-response.json', {'review': r'D:\ft工程文件\FT地牢_第二轮独立验收_20261106\S7-submission-20261107-061916-548777\REVIEW.md', 'room_changes': updates, '19_depth': '按既有独立意见保留v3实质洞深，不重做。15/19/20哈希未变。', 'continuous_width': str(root / 'assembly/qa/continuous-width.json'), 'world': str(root / 'worldqa-validated/world-report.json'), 'earlier_failed_runs': {'worldqa': 'GameTest mock server player initialization failed: null Netty channel; no behavior pass claimed.', 'worldqa-final': 'Initial open=true iron doors invalidated a before/after-toggle assumption. Corrected final test explicitly asserts on=open and off=closed; no direct door.setOpen.'}, 'pending': pending})
save(root / 'progress.json', {'state': 'V4_AUTHOR_CHECKS_COMPLETE_INDEPENDENT_AND_CLIENT_REVIEW_OPEN', 'group_accepted': False, 'changed_rooms': [f'S7-{number}' for number in updates], 'parent_unchanged': True, 'production_writes': False, 'pending': pending})
interaction_count = sum(len(entry['server_simulated_player_interactions']) for entry in world['rooms'])
scanned = sum(entry['scanned_cells_including_margin'] for entry in world['rooms'])
readme = ['# S7 local-relief-v4', '', '本轮完成11/12/13/14/16/17/18局部结构细化，保留v3的15/19/20与19号已获独立认可的洞深。当前为制作方检查完成、等待独立复核及客户端遍历，整组accepted=false。', '', f"合并SHA256：{assembly['sha256']}；326×40×151，15个分块，201728条方块记录、45个BE、3具完整装备实体；反拼逐项一致。", '', '|房间|本轮改动|SHA256|', '|---|---|---|']
readme += [f"|{entry['id']}|{entry['changes']}|{entry['sha256']}|" for entry in rows]
readme += ['', f'原生静态检查覆盖全部修改候选与合并；合并1137个路线目标通过。连续宽度检查以原版碰撞盒验证336段3格宽扫掠区域，包括转角，逐碰撞边界分割计算地板覆盖，最高有效地面之上1.8格净空、相邻区域地面高差≤0.5。已排除半砖下方埋藏底板误计高度；源码与原始报告保留。这是作者几何证明，等待独立复核。', '', f'隔离Forge1.20.1世界：10房及合并均绑定新哈希，1200刻默认randomTickSpeed=3、doFireTick=true；扫描全部体积及外扩边界共{scanned}格，包括省略空气。方块类型变化、支撑失败、掉落物、火和新增流体均计入失败；同类型属性变化单列。执行{interaction_count}项原生use交互，牢房拉杆必须通电开门/断电关门，箱桶必须实际创建服务器菜单。使用Forge FakePlayer，不以直接改OPEN或setOpen代替交互，不声明真人客户端通过。', '', '两次早期失败运行原样保留：worldqa为模拟玩家网络初始化错误；worldqa-final为初始开启铁门导致旧断言不成立。最终有效运行仅worldqa-validated，exit.json及world-report.json均须成功。', '', '十房从配方重建均字节哈希一致。reproduction.json记录命令与输出；reproduction/保存重建产物。合并Lodestone全景、剖切和俯视预览均绑定当前哈希。', '', '清场文件110条fill覆盖1969040格完整外包，单条≤32768且不重叠；仅离线生成，未执行。清场只涉及方块，不能移除已有世界实体；零旋转且锚点为完整最小角。未改正式存档、模组、配置或共享生成器。', '', '依次复现：run-build.mjs room/recipe.mjs；revise.py NN；render.cjs candidate；qa.py candidate；assemble.py；render.cjs assembly候选；qa.py assembly候选；routecoverage.py；continuous.py；clearance.py；reproduce.py。worldqa-validated/run.ps1拒绝覆盖已有世界，复跑应在新隔离目录保留源码并使用同输入。', '', '继承报告可能保留旧路径，只有revision-parent.json绑定的相同哈希允许继承；已修改七房与合并重新检查。更早历史路径字段可能经初始化器重定位，不能当成存在的新证据。', '', '仍待完成：']
readme += ['- ' + item for item in pending]
(root / 'README.md').write_text('\n'.join(readme) + '\n', encoding='utf-8')
files = []
for path in root.rglob('*'):
    if not path.is_file() or path.name == 'ready-for-review.json':
        continue
    relative = path.relative_to(root)
    if any(part in {'server', 'build', '.gradle', '__pycache__', 'reproduction'} for part in relative.parts):
        continue
    files.append(path)
manifest = [{'path': str(path), 'sha256': digest(path)} for path in sorted(files)]
save(root / 'ready-for-review.json', {'schema': 3, 'revision': 'local-relief-v4', 'created_utc': datetime.now(timezone.utc).isoformat(), 'group_accepted': False, 'status': 'AUTHOR_CHECKS_COMPLETE_CLIENT_AND_INDEPENDENT_REVIEW_OPEN', 'rooms': rows, 'assembly': assembly, 'world_report': str(root / 'worldqa-validated/world-report.json'), 'continuous_width': str(root / 'assembly/qa/continuous-width.json'), 'pending': pending, 'evidence_manifest': manifest})
assert all(digest(Path(entry['path'])) == entry['sha256'] for entry in manifest)
print(json.dumps({'ready': str(root / 'ready-for-review.json'), 'evidence_files': len(manifest), 'interaction_checks': interaction_count, 'scanned_cells': scanned, 'group_accepted': False}, ensure_ascii=False))
