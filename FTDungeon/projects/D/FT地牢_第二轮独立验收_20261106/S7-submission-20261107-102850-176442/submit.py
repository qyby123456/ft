import hashlib
import json
from datetime import datetime, timezone
from pathlib import Path

root = Path(__file__).resolve().parent
parent = root.parent / 'local-relief-v4'
prior = root.parent / 'interaction-traversal-v5'
def load(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))
def save(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2), encoding='utf-8')
def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

for directory in [parent, prior]:
    ready = load(directory / 'ready-for-review.json')
    assert all(digest(Path(entry['path'])) == entry['sha256'] for entry in ready['evidence_manifest'])
assembly = load(root / 'assembly/assembly.json')
sha = assembly['sha256']
assert digest(Path(assembly['candidate'])) == sha
access = load(root / 'access-change-audit.json')
assert access['status'] == 'AUTHOR_EXACT_ACCESS_ONLY_PASS'
assert sum(len(room['changes']) for room in access['rooms']) == 12
eye = load(root / 'worldqa-proof/eye-use-report.json')
installation = load(root / 'worldqa-proof/installation-report.json')
natural = load(root / 'naturalqa-complete/natural-motion-report.json')
world = load(root / 'worldupdates/world-report.json')
for directory in ['worldqa-proof', 'naturalqa-complete', 'worldupdates']:
    assert load(root / directory / 'exit.json')['exit_code'] == 0
assert eye['status'] == 'AUTHOR_FIRST_HIT_USE_PASS' and eye['sha256'] == sha and not eye['failures']
assert eye['functional_target_count'] == 39 and len(eye['targets']) == 39
assert all(target['verified'] and target['distance'] <= 4.5 for target in eye['targets'])
assert len(eye['door_controls']) == 6 and all(len(door['controls']) == 2 and all(control['opened'] and control['closed'] for control in door['controls']) for door in eye['door_controls'])
assert installation['status'] == 'AUTHOR_INSTALL_EXECUTED_PASS' and installation['sha256'] == sha
assert installation['type_differences_including_omitted_air'] == 0 and installation['barriers'] == 60115
assert natural['status'] == 'AUTHOR_WORLD_TICK_MOTION_PASS' and natural['sha256'] == sha
assert len(natural['courses']) == 13 and all(course['passed'] for course in natural['courses'])
assert world['status'] == 'EXECUTED_NO_LISTED_FAILURES' and len(world['rooms']) == 11
routes = load(root / 'assembly/qa/full-width-routes/summary.json')
continuous = load(root / 'assembly/qa/continuous-width.json')
assert routes['status'] == 'SELF_CHECK_PASS' and routes['sha256'] == sha
assert continuous['status'] == 'AUTHOR_CONTINUOUS_CONNECTOR_PASS' and continuous['sha256'] == sha
rows = []
for number in range(11,21):
    room = root / f'S7-{number}'
    summary = load(room / 'summary.json')
    candidate = Path(summary['nbtPath'])
    candidate_sha = digest(candidate)
    check = load(room / 'qa/self-check.json')
    assert check['sha256'] == candidate_sha and check['status'] == 'SELF_CHECK_PASS'
    assert next(entry for entry in world['rooms'] if entry['id'] == room.name)['sha256'] == candidate_sha
    summary['summaryPath'] = str(room / 'summary.json')
    summary['recipePath'] = str(room / 'recipe.mjs')
    if number in [15,16]:
        from qa import Structure
        structure = Structure.read(candidate)
        summary['blockCount'] = len(structure.blocks)
        summary['paletteCount'] = len(set(structure.blocks.values()))
    save(room / 'summary.json', summary)
    rows.append({'id':room.name,'candidate':str(candidate),'sha256':candidate_sha,'changed_in_v6':number in [15,16],'qa':str(room / 'qa/self-check.json'),'qa_provenance':'New check, current source' if number in [15,16] else 'Inherited exact-hash v4 check; old report paths retained; native v6 world run binds current candidate','recipe':str(room / 'recipe.mjs'),'images':[str(path) for path in sorted(room.glob('*.png'))]})
assert next(entry for entry in world['rooms'] if entry['id'] == 'S7-assembly')['sha256'] == sha
plan = load(root / 'installation/install-plan.json')
for entry in plan['files']:
    assert digest(root / 'installation/fts7' / entry['path']) == entry['sha256']
plan['deployment'] = 'EXECUTED_IN_ISOLATED_FORGE_GAMETEST_ONLY; NOT_DEPLOYED_TO_PRODUCTION'
plan['execution_report'] = str(root / 'worldqa-proof/installation-report.json')
plan['tests'] = {'full_volume_native_type_comparison':1969040,'barrier_count':60115,'entry_and_return_permission_zero':True,'remote_trigger_blocked':True,'reinstall_did_not_duplicate_armor_stands':True,'network_scope':'Vanilla ServerGamePacketListenerImpl with Netty EmbeddedChannel replaces FakePlayer no-op teleport handler only for command test. Real server command teleport changes position; no TCP/client receive/ack test. Reflectively register test FakePlayer in private players list for @a selector; no production use.'}
save(root / 'installation/install-plan.json', plan)
pending = ['新v6的12块接近设施、新合并/15片、39项眼位交互、13项自然运动刻及安装数据包的独立复核。', '正式安装仍未执行，需世界维护者明确预留固定区域并批准部署；本次仅提供离线包与隔离世界执行证据。']
scope = ['仅Java1.20.1/Forge47.4.10原版方块行为、overworld、零旋转无镜像。', '眼位从起点可达半格集合选取并检查实时体型；每项重设到该站位，真实世界OUTLINE首命中后调用ServerPlayerGameMode.useItemOn，不是客户端网络包或人工输入。', '自然运动每个实际世界刻最多一次Player.aiStep，设置横向/跳跃输入，保留原生速度、重力和跳跃冷却；不手工注入-0.08，不在课程内传送。课程之间重置站位；不声明完整生存客户端连续遍历。', 'v4/v5独立已关闭项保留有效，特别是局部视觉厚度和v4独立配方复现；不冒称新v6独立通过。']
save(root / 'progress.json', {'state':'REQUESTED_AUTHOR_EVIDENCE_COMPLETE_AWAITING_INDEPENDENT_REVIEW','group_accepted':False,'changed_rooms':['S7-15','S7-16'],'added_air_cells':12,'original_goals_and_furniture_preserved':True,'functional_targets':39,'natural_world_tick_courses':13,'installation_isolated_test':'PASS','production_writes':False,'pending':pending,'scope':scope})
save(root / 'review-response.json', {'review':r'D:\ft工程文件\FT地牢_第二轮独立验收_20261106\S7-submission-20261107-083104-571437\REVIEW.md','closed_visual_scope':'All prior local massing/thickness closed; no redo. Only 4 approach blocks in 15 and 8 service-deck blocks in 16 added after concrete failed access tests. All parent nonair, BE, entities and goals retained.','v4_independent_reproduction':r'D:\ft工程文件\FT地牢_第二轮独立验收_20261106\S7-submission-20261107-083104-571437\independent-qa\reproduction.json','installation':str(root / 'worldqa-proof/installation-report.json'),'eye_interactions':str(root / 'worldqa-proof/eye-use-report.json'),'natural_motion':str(root / 'naturalqa-complete/natural-motion-report.json'),'world_updates':str(root / 'worldupdates/world-report.json'),'failures_preserved':{'worldqa/compile-first.log':'Missing import/ambiguous BlockPos getter method reference; fixed before world execution.','worldqa':'Old suspended barrel had zero reachable first-hit feet; door repeat test mistakenly aimed at lever block center; then unmodifiable PlayerList view raised test setup failure.','worldqa-final':'New access and all39 eye/use passed; install full-volume/barriers passed; FakePlayerNetHandler teleport no-op prevented entry, not a datapack command failure.','naturalqa':'Initial watch course started in wall and connector waypoint used wrong intermediate Y; beam/hurdle confirmed 2-block approach problem; fixed starts/Y, retained stair geometry, added four approach blocks.','worldqa-validated':'First successful install+39 eye-use run, retained.','naturalqa-final':'First successful10-course tick run; complete13-course run adds down/return/service legs.','reproduction':'First verify-access postprocess attempt lacked copied design.json; fixed setup and rebuilt only15/16 byte-identically.','qa_documentation':'Early15/16 QA computed PASS then missing references.json interrupted documentation; copied reference list and reran successfully.'},'scope':scope,'pending':pending})
instructions = ['# S7 安装包使用说明（未部署）','', '适用：Minecraft Java1.20.1，零旋转、无镜像，仅主世界。包为fts7-datapack.zip；内部pack.mcmeta位于压缩包根目录。安装会清除指定完整区域的所有方块及BE，必须先由维护者预留全新空闲区域。不会清除实体，必须确认无现存实体、建筑、玩家物品、地牢或其他用途；也需预留未被其他系统强加载的区块。', '', '|用途|位置/范围|','|---|---|','|结构最小角|10000 80 10000|','|完整清场包络|10000 80 10000 → 10325 119 10150，1969040格|','|外部落脚点|9992.5 85 10028.5|','|外部地板|9990 84 10026 → 9994 84 10030|','|外部头顶清空|9990 85 10026 → 9994 88 10030|','|内部进入/返回站位|10014.5 85 10028.5|', '', '1. 维护者批准预留位置后，将zip置入目标世界datapacks目录，执行/reload。加载仅创建两个trigger目标，不自动安装、不自动清场。', '2. 管理员执行/function fts7:install。它强加载安装区域，先执行110条fill清除完整包络，再放15个模板并建外部平台，设置安装标记，释放本次区域强加载。首次过程会占用服务器处理时间，请在维护窗口使用。installed标记使再次调用不会重复放置实体。', '3. 玩家到达外部落脚点（管理员可首次/tp玩家到该坐标），站在半径1.5格范围内执行/trigger fts7_enter。它将玩家送到内部入口，原外围屏障不打孔。', '4. 玩家沿房间返回内部入口站位，在半径1.5格内执行/trigger fts7_return，回到同一外部平台。', '5. 普通玩家仅使用trigger；function安装、enter、return是管理员命令权限范围。远处即便保留已启用trigger也不会传送，分数会清零。未提供重复强制安装或自动卸载命令，避免破坏已有探索内容。', '', '隔离GameTest已实际运行包内函数并验证：完整包络1969040格逐格方块类型一致、60115屏障保留、权限0进入/返回、远处触发无效、第二次install后仍3具装备架。测试使用嵌入式Netty通道支持原版服务器命令传送，未测试TCP客户端收包确认。installed-after.nbt保留安装后载荷。以上不是正式部署授权或独立验收。', '', '本包固定坐标，不可只移动15片而不同时修改所有函数、平台与触发范围。不得以分片包络1324800格代替完整1969040格清场；也不得直接放稀疏NBT而留下世界旧方块。']
(root / 'installation/README.md').write_text('\n'.join(instructions)+'\n',encoding='utf-8')
readme = ['# S7 install-interact-v6 待审交付','', '根据083104独立报告补齐三项作者证据：外部安装进入返回、全功能目标首命中使用复位、必要楼梯/梯子与训练障碍的自然运动刻。整组accepted=false，等待新哈希独立复核。', '', f"合并SHA256：{sha}；326×40×151；15片；201740条方块记录、45个BE、3具装备架，60115块屏障。", '', '仅12格空气新增设施：15号两组器械接近台阶共4块；16号吊桶检修板8块。未删除任何父版非空气方块、家具、BE、实体或原目标。闭合的视觉厚度整改与19洞深不重做。新15/16配方作者复建逐字节一致，其他八房哈希不变；v4十房/合并/15片26份独立复现已由父线程关闭，不重复冒称本轮独立复现。', '', f"39个功能目标（20箱桶、13拉杆、6铁门）均有起点可达脚点、实时净空、实际1.62眼位、OUTLINE首命中坐标/面、距离和use结果；最大距离{max(target['distance'] for target in eye['targets']):.6f}格。容器关闭后回到InventoryMenu、每槽空库存，桶OPEN恢复；每根拉杆切换并通过第二次射线使用恢复。铁门直接use正确返回PASS，另逐扇用内外两个拉杆通电开门/断电关门，12组控制通过。初始模板铁门OPEN=true无供电，复位采用原生断电关闭稳定态，不伪造setOpen。", '', 'naturalqa-complete：13课程全部通过，真实世界刻唯一编号约束每刻至多一次Player.aiStep，以xxa/zza/jumping输入驱动原生重力、速度、跳跃冷却；不手工注入下降量或正Y，不在课程内传送。课程覆盖图书馆攀梯上下及上层落脚、19石阶双向、16侧阶双向和检修板、合并坡道双向、半高踏石、平衡木、横栏和原侧边半砖。每课之间重置站位，仍是服务器模拟，不声明真人客户端或完整连续生存遍历。', '', 'worldupdates：新15/16和合并绑定新哈希，连同其余房间共11个输入各1200刻更新。完整体积含外围与省略空气扫描，类型变化/支撑失败/掉落/火/新增流体均零。原版属性更新单列，不将同类型属性变化误判成掉块。', '', f"新合并{routes['targets']}路线目标通过，336段连续3格宽几何检查通过。v5旧104路线及222030次同步碰撞探针仍以其原v4哈希有效，不改写为自然运动或自动转移到新哈希。", '', '安装数据包实际执行完整清场110命令及15模板；完整1969040格安装后比对类型零差异、60115屏障不改、3装备架不重复。权限0 trigger进入/返回、远处触发拦截通过。只在隔离世界执行，正式实例和生成器未改。安装包及范围、权限、外部平台与操作命令见installation/README.md。', '', '最终有效证据：worldqa-proof（39首命中/use/复位及安装）、naturalqa-complete（13自然刻课程）、worldupdates（11输入1200刻）；各目录exit.json=0。失败和中间成功运行均保留，说明见review-response.json。', '', '制作方复现：render.cjs与qa.py对应NBT；assemble.py、routecoverage.py、continuous.py、clearance.py；verify-access.py只复建新增15/16；prepare.py导出当前合并可达脚点；install-package.py生成固定坐标安装包。测试运行run.ps1拒绝覆盖已有世界，复跑须新建隔离目录保留其源码/inputs/配置。', '', '继承报告中的旧路径按准确SHA、文件名和尺寸绑定父版；只有15/16及合并重新静态检查。实际新世界报告为全部十房当前哈希执行。helpers仍依赖已安装builder/Lodestone、旧审查类/映射jar及nbtio，见父版依赖清单，不是完全自包含开发环境。', '', '适用范围：']
readme += ['- '+item for item in scope]
readme += ['', '仍待：']+['- '+item for item in pending]
(root / 'README.md').write_text('\n'.join(readme)+'\n',encoding='utf-8')
files = []
for path in root.rglob('*'):
    if not path.is_file() or path.name == 'ready-for-review.json':
        continue
    if any(part in {'server','build','.gradle','__pycache__'} for part in path.relative_to(root).parts):
        continue
    files.append(path)
manifest = [{'path':str(path),'sha256':digest(path)} for path in sorted(files)]
save(root / 'ready-for-review.json', {'schema':5,'revision':'install-interact-v6','created_utc':datetime.now(timezone.utc).isoformat(),'status':'REQUESTED_AUTHOR_EVIDENCE_COMPLETE_AWAITING_INDEPENDENT_REVIEW','group_accepted':False,'parents':[{'path':str(directory / 'ready-for-review.json'),'sha256':digest(directory / 'ready-for-review.json')} for directory in [parent,prior]],'rooms':rows,'assembly':assembly,'access_change_audit':str(root / 'access-change-audit.json'),'installation_plan':str(root / 'installation/install-plan.json'),'installation_package':str(root / 'installation/fts7-datapack.zip'),'first_hit_use_report':str(root / 'worldqa-proof/eye-use-report.json'),'installation_execution_report':str(root / 'worldqa-proof/installation-report.json'),'natural_motion_report':str(root / 'naturalqa-complete/natural-motion-report.json'),'world_report':str(root / 'worldupdates/world-report.json'),'scope':scope,'pending':pending,'evidence_manifest':manifest})
assert all(digest(Path(entry['path'])) == entry['sha256'] for entry in manifest)
print(json.dumps({'ready':str(root / 'ready-for-review.json'),'files':len(manifest),'assembly_sha256':sha,'functional_targets':39,'natural_courses':13,'installation':'ISOLATED_PASS_NOT_DEPLOYED','group_accepted':False},ensure_ascii=False))
