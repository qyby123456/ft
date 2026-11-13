import hashlib
import json
import zipfile
from datetime import datetime, timezone
from pathlib import Path

root = Path(__file__).resolve().parent
frozen = Path(r'D:\ft工程文件\FT地牢_功能房间第二轮_接手续作_20261106\S7\revisions\install-interact-v6')
handoff = frozen.parent / 'automatic-hooks-v7-supplement'
failed = root.parent / 'automatic-hooks-20261107-v7'
def load(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))
def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()
def save(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2), encoding='utf-8')

ready = load(frozen / 'ready-for-review.json')
assert len(ready['evidence_manifest']) == 828
assert digest(frozen / 'ready-for-review.json') == 'c33269f92a4b4b697ec94aa4e34cbe62db77ae25771fa53f683d0c9e3272fdc4'
assert all(digest(Path(entry['path'])) == entry['sha256'] for entry in ready['evidence_manifest'])
report = load(root / 'automatic-hooks-report.json')
assert load(root / 'exit.json')['exit_code'] == 0
assert report['status'] == 'AUTHOR_EXACT_PACKAGE_AUTOMATIC_HOOKS_PASS'
assert all(report[name] for name in ['objectives_created_before_test_without_manual_load', 'entry_natural_tick_teleport', 'return_natural_tick_teleport', 'remote_stale_trigger_rejected_naturally', 'remote_disabled_trigger_rejected'])
assert report['manual_load_calls'] == report['manual_tick_calls'] == report['manual_enable_calls'] == 0
assert report['entry_arrival_tick'] > report['entry_enabled_naturally_tick']
assert 'fts7:load' in report['runtime_load_tag'] and 'fts7:tick' in report['runtime_tick_tag']
assert all(entry['command'] in {'function fts7:install','trigger fts7_enter set 1','trigger fts7_return set 1'} for entry in report['commands'])
assert all(entry['permission'] == (4 if entry['command']=='function fts7:install' else 0) for entry in report['commands'])
package = root / 'server/world/datapacks/fts7-datapack.zip'
assert digest(package) == digest(frozen / 'installation/fts7-datapack.zip') == report['package_sha256']
assembly = ready['assembly']
assert assembly['sha256'] == report['candidate_sha256']
with zipfile.ZipFile(package) as archive:
    tags = {name: json.loads(archive.read(f'data/minecraft/tags/functions/{name}.json')) for name in ['load','tick']}
    assert tags['load']['values'] == ['fts7:load'] and tags['tick']['values'] == ['fts7:tick']
    parts = []
    for index, part in enumerate(assembly['parts']):
        archive_name = f'data/fts7/structures/part_{index:02}.nbt'
        archived_digest = hashlib.sha256(archive.read(archive_name)).hexdigest()
        assert archived_digest == part['sha256']
        parts.append({'zip_entry':archive_name,'sha256':archived_digest})
source_files = list((root / 'native-test/src/main/resources').rglob('*'))
assert not any(path.is_file() and ('functions' in path.parts or path.suffix == '.mcfunction') for path in source_files)
validation = {'status':'AUTHOR_AUTOMATIC_HOOKS_SUPPLEMENT_VALIDATED','frozen_v6_ready_sha256':digest(frozen / 'ready-for-review.json'),'frozen_v6_files':828,'frozen_v6_hash_mismatches':0,'same_zip_sha256':digest(package),'candidate_sha256':report['candidate_sha256'],'package_changed':False,'package_old_new_diff':[],'tags_in_exact_zip':tags,'runtime_tags':{'load':report['runtime_load_tag'],'tick':report['runtime_tick_tag']},'zip_parts_bound_to_exact_assembly':parts,'no_fts7_functions_or_hooks_in_test_mod_resources':True,'successful_root':str(root),'failed_root':str(failed),'failed_reason':'First isolated startup omitted the test mod pack.mcmeta, so GameTest could not locate its empty template and never ran. Original log/crash/exit remain untouched. Second fresh world includes test mod pack metadata, exact installed zip unchanged.','source_world':str(root / 'server/world'),'production_writes':False,'group_accepted':False}
save(root / 'validation.json', validation)
readme = ['# S7 v7 自动钩子补证（原v6安装包不变）','', '只补自动load/tick调度缺口，不修改v6冻结828文件、建筑、交互和自然运动课程。制作方补证通过，整组accepted=false，等待独立复核。', '', '原v6 ready SHA256：'+validation['frozen_v6_ready_sha256'], '原安装ZIP SHA256：'+validation['same_zip_sha256'], '合并NBT SHA256：'+report['candidate_sha256'], '', '原ZIP已含data/minecraft/tags/functions/load.json与tick.json；上轮worldqa-proof测试模组没有安装这些标签，而是人工调用函数。因此此前证据仅支持函数机理和安装，本轮修复证据，不修改安装包版本。旧新ZIP差异为空，15个zip模板的压缩NBT哈希逐一等于冻结合并分片。', '', '本轮直接将原ZIP按原字节放入全新世界world/datapacks，再启动Forge；测试模组资源只含空模板、mods.toml和pack.mcmeta，不包含fts7函数或minecraft函数标签。运行时minecraft:load与minecraft:tick标签实际解析为fts7:load与fts7:tick，且测试进入前两个trigger目标已自动创建。', '', '测试仅管理员显式执行function fts7:install。随后在不同真实world tick等待entry自然解锁、用权限0 trigger提交，下一自然tick进入；等待return自然解锁，权限0 trigger提交，下一自然tick返回；在平台自然取得的旧entry许可带到远处提交，自动tick拒绝传送并清零锁定；远处再试未启用trigger也拒绝。全过程没有显式load/tick调用、直接函数管理器execute或人工scoreboard enable。源码、命令账本和逐刻轨迹保留。', '', '有效运行：'+str(root), '实际世界：'+str(root / 'server/world'), '有效原始报告：'+str(root / 'automatic-hooks-report.json'), '', '首轮失败保留：'+str(failed)+'。该轮漏带测试模组pack.mcmeta，服务器在GameTest空模板查找阶段失败，无自动钩子通过结论。第二轮在全新目录补齐测试模组元数据后运行，原安装ZIP未改。', '', '范围：服务器GameTest作者模拟。使用FakePlayer注册到服务器玩家集合，并为命令传送使用原版ServerGamePacketListenerImpl和Netty EmbeddedChannel；权限0是实际CommandSourceStack权限0与原生trigger命令派发。未声称真实TCP客户端输入、收包或确认；但load/tick函数完全由服务器自然调度，不由测试手工调用。只限既有零旋转安装方案。', '', 'D盘仅保存小型报告、索引与哈希清单；世界、构建、日志和原包都在指定C盘独占子目录。run.ps1遇已初始化世界拒绝覆盖。复跑应复制最小测试源码、mod元数据、empty模板、构建设置、server.properties和原ZIP到新的独占目录后运行。', '', '本轮没有关闭独立验收项，也未部署正式实例。']
(root / 'README.md').write_text('\n'.join(readme)+'\n',encoding='utf-8')
files = [root / name for name in ['automatic-hooks-report.json','validation.json','README.md','tests.xml','exit.json','console.log','run.ps1','finalize.py','server/server.properties','server/world/datapacks/fts7-datapack.zip','native-test/build.gradle','native-test/settings.gradle','native-test/gradle.properties']]
files += [path for path in (root / 'native-test/src').rglob('*') if path.is_file()]
files += [path for path in (root / 'native-test/build/classes').rglob('*.class') if path.is_file()]
for name in ['exit.json','console.log','run.ps1','native-test/src/main/java/net/farmingtales/s7qa/FungalTests.java']:
    files.append(failed / name)
manifest = [{'path':str(path),'sha256':digest(path),'bytes':path.stat().st_size} for path in sorted(files)]
supplement = {'schema':1,'revision':'automatic-hooks-v7-supplement','created_utc':datetime.now(timezone.utc).isoformat(),'status':'AUTHOR_EXACT_FROZEN_ZIP_AUTOMATIC_HOOKS_PASS','group_accepted':False,'parent_ready':{'path':str(frozen / 'ready-for-review.json'),'sha256':validation['frozen_v6_ready_sha256'],'frozen_files_reverified':828},'candidate_sha256':report['candidate_sha256'],'package_sha256':report['package_sha256'],'package_changed':False,'successful_root':str(root),'world_path':str(root / 'server/world'),'automatic_report':str(root / 'automatic-hooks-report.json'),'failed_root':str(failed),'evidence_manifest':manifest,'pending':['父线程独立核查本轮自动调度补证；其余v6独立复核由父线程继续。'],'production_writes':False}
save(root / 'ready-for-review.json', supplement)
save(handoff / 'ready-for-review.json', supplement)
save(handoff / 'automatic-hooks-report.json', report)
save(handoff / 'validation.json', validation)
(handoff / 'README.md').write_text('\n'.join(readme)+'\n',encoding='utf-8')
assert all(digest(Path(entry['path'])) == entry['sha256'] for entry in manifest)
print(json.dumps({'handoff':str(handoff / 'ready-for-review.json'),'handoff_sha256':digest(handoff / 'ready-for-review.json'),'C_world':str(root / 'server/world'),'C_report_sha256':digest(root / 'automatic-hooks-report.json'),'package_sha256':digest(package),'evidence_files':len(manifest),'frozen828_unchanged':True,'group_accepted':False},ensure_ascii=False))
