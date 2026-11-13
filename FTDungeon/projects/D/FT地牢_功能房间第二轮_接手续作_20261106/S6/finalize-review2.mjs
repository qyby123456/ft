import fs from 'node:fs'
import path from 'node:path'
import {fileURLToPath} from 'node:url'
import {createHash} from 'node:crypto'
import assert from 'node:assert/strict'

const root=path.dirname(fileURLToPath(import.meta.url))
const read=filename=>JSON.parse(fs.readFileSync(filename,'utf8').replace(/^\uFEFF/,''))
const hash=filename=>createHash('sha256').update(fs.readFileSync(filename)).digest('hex')
const write=(relative,value)=>fs.writeFileSync(path.join(root,relative),JSON.stringify(value,null,2)+'\n')
const runtime=read(path.join(root,'qa/runtime/result.json'))
assert.equal(runtime.status,'PASS')
assert.equal(runtime.fixtures.length,11)
assert(fs.readFileSync(path.join(root,'qa/runtime/run.log'),'utf8').includes('BUILD SUCCESSFUL'))
const fluidReports=read(path.join(root,'qa/native-fluid-fire.json'))
const folders=fs.readdirSync(root).filter(folder=>/^\d\d-/.test(folder)).sort()
assert.equal(folders.length,10)
const rows=[]
const candidates=[...folders,'assembly'].map(folder=>{
  const directory=path.join(root,folder)
  const summary=read(path.join(directory,'summary.json'))
  const design=read(path.join(directory,'design.json'))
  const sha256=hash(summary.nbtPath)
  const report=name=>read(path.join(directory,'qa',name))
  for(const name of ['static.json','render.json','self-review.json']) assert.equal(report(name).source_sha256,sha256,name)
  const native=report('independent-native-frozen.json')[0]
  const routes=report('independent-routes.json')[0]
  const supports=report('independent-all-support.json')[0]
  for(const result of [native,routes,supports]) assert.equal(result.sha256,sha256)
  assert.equal(native.registry_properties,'PASS')
  assert.equal(native.template_roundtrip_block_count,'PASS')
  assert.equal(report('static.json').failures.length,0)
  assert.equal(supports.failures.length,0)
  assert.equal(supports.unsupported.length,0)
  assert(routes.start_clear&&routes.targets.every(target=>target.standing_clear&&target.reachable))
  assert.deepEqual(routes.targets.map(target=>[target.label,target.position]),design.targets.map(target=>[target.label,target.position]))
  const fluid=folder==='assembly'?report('native-fluid-fire.json')[0]:fluidReports.find(result=>result.sha256===sha256)
  assert(fluid&&fluid.failures.length===0)
  const live=runtime.fixtures.find(result=>result.id===design.id)
  assert(live)
  assert.equal(live.source_sha256,sha256)
  assert.equal(live.server_ticks,400)
  assert.equal(live.random_tick_dispatches,20)
  assert.equal(live.external_fluid_cells,0)
  assert.equal(live.dangerous.length,0)
  assert.equal(hash(live.capture),live.capture_sha256)
  const doors=live.interactions.filter(event=>event.door)
  if(folder==='assembly'||design.id==='S6-12') {
    assert.equal(doors.length,6)
    assert(doors.every(event=>event.reach_and_native_outline_ray&&event.same_side_controls===2&&event.native_use.length===2))
  }
  const containers=live.interactions.filter(event=>event.container)
  assert(containers.every(event=>event.slot_insert_remove_restored))
  rows.push({id:design.id,sha256,nonair_supports:supports.nonair_checked,route_targets:routes.targets.length,lava_cells:fluid.lava_cells,block_entities:fluid.block_entities,server_ticks:live.server_ticks,doors_both_sides:doors.length,containers_used:containers.length,post_tick_capture:live.capture,post_tick_sha256:live.capture_sha256,neighbor_state_changes:live.changes})
  return {id:design.id,directory,candidate:summary.nbtPath,sha256,size:summary.size,status:'REVISED_SELF_CHECKED_PENDING_INDEPENDENT',independent_acceptance:false}
})
const rooms=candidates.slice(0,10)
const assembly=candidates[10]
const splitFile=path.join(root,'assembly/qa/split-payload-verification.json')
const split=read(splitFile)
assert.equal(split.source_sha256,assembly.sha256)
assert(split.block_states_and_payloads_equal&&split.corridor_cross_sections.pass&&split.barrier_union_shell.pass)
assert.equal(split.source_records,split.reassembled_records)
for(const piece of split.pieces) {
  assert.equal(hash(piece.file),piece.sha256)
  assert(piece.size.every(value=>value<=48))
}
assert.deepEqual(fs.readdirSync(path.join(root,'assembly/slices')).filter(name=>name.endsWith('.nbt')).sort(),split.pieces.map(piece=>path.basename(piece.file)).sort())
const reproduction=read(path.join(root,'qa/reproduction.json'))
assert.equal(reproduction.reports.length,11)
for(const record of reproduction.reports) assert(record.byte_equal&&candidates.some(candidate=>candidate.sha256===record.source_sha256))
const frozen=read(path.join(root,'qa/frozen-assembly-reproduction/result.json'))
assert.equal(frozen.status,'PASS')
assert.equal(frozen.source_sha256,assembly.sha256)
assert.equal(frozen.reproduced_sha256,assembly.sha256)
assert(frozen.compressed_bytes_equal)
for(const input of frozen.frozen_inputs) {
  assert.equal(hash(input.original),input.original_sha256)
  assert.equal(hash(input.copy),input.copy_sha256)
}
const inherited=read(path.join(root,'inheritance.json'))
for(const source of inherited.files) assert.equal(hash(source.source),source.sha256,'Old source changed '+source.source)
const fixture=read(path.join(root,'qa/safety-fixtures/fixture-result.json'))
assert(fixture.sealed_pass&&fixture.slab_collision_gap_detected&&fixture.native_flammable_plank_detected)
const external=[]
for(const reference of read(path.join(root,'references.json')).references) {
  assert.equal(hash(reference.image_path),reference.sha256)
  for(const [kind,filename] of [['reference-image',reference.image_path],['reference-prompt',reference.prompt_path]]) external.push({id:reference.id,kind,absolute:filename,sha256:hash(filename),bytes:fs.statSync(filename).size})
}
const layout=read(path.join(root,'assembly/layout.json'))
const notVerified=['外部世界到封闭屏障内的步行接入；当前entry仅是内部接入脚点，未交付外部步行开口或传送机制。','真实网络客户端移动、攀梯、跳跃障碍及战斗/生存通关。','长期运行、多朝向旋转、随机种子与整合包全部模组共同运行。','独立方对当前冻结候选的视觉、外部接入及最终放行。']
const submission={group:'S6',stage:'FINAL_REVISED_CANDIDATES_PENDING_INDEPENDENT_REVIEW',updated:new Date().toISOString(),rooms,assembly:{...assembly,slices:split.pieces.length,slice_manifest:splitFile,block_entities:split.pieces.reduce((sum,piece)=>sum+piece.block_entities,0),routes:rows[10].route_targets,barriers:split.barrier_union_shell.actual},manifest:path.join(root,'delivery-manifest.json'),runtime:path.join(root,'qa/runtime/result.json'),frozen_assembly_reproduction:path.join(root,'qa/frozen-assembly-reproduction/result.json'),independent_acceptance:false,formal_world_modified:false,not_verified:notVerified,open_review_items:['审查意见3中的外部步行接入未关闭；内部走廊全截面与模块门缝自检通过，仍待独立复核。'],inherited_source:path.join(root,'inheritance.json')}
write('ready-for-review.json',submission)
write('qa/runtime-coverage.json',{source:'qa/runtime/result.json',runtime:runtime.runtime,limits:runtime.limits,fixtures:rows.map(row=>({id:row.id,sha256:row.sha256,server_ticks:row.server_ticks,doors_both_sides:row.doors_both_sides,containers_used:row.containers_used,neighbor_state_changes:row.neighbor_state_changes,post_tick_capture:row.post_tick_capture,post_tick_sha256:row.post_tick_sha256})),independent_acceptance:false})
write('qa/external-access.json',{source_sha256:assembly.sha256,entry:layout.entry,coordinate_system:'Structure-local integer feet coordinates; world feet = placement origin + entry + [0.5,0,0.5]',internal_routes:rows[10].route_targets,internal_corridor:split.corridor_cross_sections,external_walk_in_verified:false,external_aperture_delivered:false,teleport_mechanism_delivered:false,barrier_shell:split.barrier_union_shell,scope:'Closed standalone candidate. Internal entry is not a walkable opening from outside. External integration remains open for review; do not infer deployment readiness from static internal routes.'})
write('qa/delivery-audit.json',{stage:submission.stage,rows,old_source_files_unchanged:inherited.files.length,recipes_byte_equal:11,frozen_assembly_byte_equal:true,source_reference_images_verified:10,slices:split.pieces.length,source_and_reassembled_records:split.source_records,preserved_block_entity_payloads:submission.assembly.block_entities,barrier_cells:split.barrier_union_shell.actual,corridor_clearance:split.corridor_cross_sections,independent_acceptance:false,formal_world_modified:false,not_verified:notVerified})
write('progress.json',{style:'S6',status:'revised_candidates_pending_independent_review_with_open_external_access',current:'S6-99',completed:rooms.map(room=>room.id),assembly_self_checked:true,assembly_sha256:assembly.sha256,slices:split.pieces.length,independent_acceptance:false,formal_world_modified:false,next:'Freeze this manifest for independent re-review; external walking access remains an explicit open review item. No deployment.',not_verified:notVerified})
fs.writeFileSync(path.join(root,'README.md'),`# S6 火山熔岩 11—20 修订待审包\n\n状态：当前候选制作、自检和有限隔离运行完成；待独立复核，accepted=false。审查意见3的外部步行接入仍未关闭。未写正式 saves/mods/config/KubeJS；隔离测试世界仅位于本包qa/runtime内。旧S6登记的${inherited.files.length}个文件哈希未变，无付费生图。\n\n入口：ready-for-review.json；delivery-manifest.json绑定当前文件及外部参考图/提示词；qa/delivery-audit.json汇总11份候选的当前哈希检查。历史目录、构建缓存及隔离世界不作为正式候选。\n\n## 本轮修订\n\n- 15训练场移除规则棋盘噪声，改连续沙地和连片踩踏斑块，保留草人、横栏、坡障及绕行。\n- 19观景台随包加入44格峡谷纵深，具有近侧台地、中景岩柱和石桥、远处熔岩瀑；原台体整体z+44，所有旧目标同步平移而未删除。player-canyon.png为未剖切玩家眼高视图；canyon-overview.png展示环境全貌。\n- 12六扇铁门每门内外各设一个同侧可触及拉杆，原生射线和距离检查后分别开闭通过。\n- 其余房间保持先前修订。14为方块武器护甲陈设；18头颅使用真实方块实体，石棺不可开启；19铜质观测器无放大功能；20拉杆无线路、宝箱为空且无锁。\n\n## 合并\n\nassembly/s6-volcanic-connected.nbt：${assembly.size.join('×')}；SHA256 ${assembly.sha256}。下翼移至z145以容纳峡谷。layout.json登记实际偏移及端口。${rows[10].route_targets}个内部路线目标静态可达，走廊3×4全截面${split.corridor_cross_sections.air_samples}空气采样通过。联合外壳${split.barrier_union_shell.actual}屏障格；唯一源改动例外是20退役外口12空气格封玻璃。\n\n${split.pieces.length}个分片每轴≤48，assembly/slices目录仅保留当前分片。反拼${split.source_records}记录的状态及${submission.assembly.block_entities}份BE载荷逐项一致，结构实体0。旧分片归档。\n\n## 当前哈希实测\n\n十房及合并注册属性、模板回写、全非空气canSurvive、原生液体碰撞面/可燃性静态自检通过。11份配方重建压缩字节一致；另从复制并冻结的十房输入重建合并，输出字节一致且原输入未变，见qa/frozen-assembly-reproduction/result.json。\n\n真实Forge47.4.10/MC1.20.1隔离GameTestServer逐份放置11份候选，每份400实际服务端刻，doFireTick=true、randomTickSpeed=3、强制流体排程和邻居更新，另执行20次随机刻分派。无新火、外溢流体、危险方块变化或刻后失去支撑。15和合并各5处普通墙连接状态随邻居更新变化，完整列于result.json，未冒称刻后逐字节不变。六门内外控制在单房及合并均实际开闭；所有容器以FakePlayer原生use和槽位放入/取回验证。日志、源代码和11份刻后NBT均随包；刻后NBT仅为证据，正式候选仍以ready-for-review.json为准。\n\n## 复现与清单\n\n技能run-build.mjs配合各recipe.mjs，--data-version 3465；recheck.ps1跑静态、原生和渲染；split-verify.mjs核反拼；verify-reproduction.mjs核11份重建；reproduce-frozen-assembly.mjs核冻结输入合并；qa/runtime包含独立Forge测试工程、run.log、result.json与after-S6-*.nbt。finalize-review2.mjs重新核绑定并生成此清单；status.mjs与finalize.mjs均转入该脚本。\n\n文件名带independent的兼容报告由制作方调用原生工具生成，不代表独立验收。每房qa/self-review.json为静态阶段记录，后续运行结论由本轮qa/runtime-coverage.json按当前哈希补充。清单完整列入非历史的工具、配方、设计、参考索引、渲染、QA、日志、冻结副本、运行工程及20分片。排除项和理由明确记入manifest；manifest自身不自哈希。\n\n## 尚未验证\n\n${notVerified.map(detail=>'- '+detail).join('\n')}\n\n内部入口脚点为${layout.entry.join(',')}，其与外部之间仍有封闭屏障。未认证步行外部入口，未实现传送机制；不得据此部署或宣布整组放行。\n`)
fs.writeFileSync(path.join(root,'qa/revision-response.md'),`# 对20261107-045549独立审查的第二次响应\n\n当前合并：${assembly.sha256}；accepted=false。\n\n1. 15棋盘已改为连续沙地和不规则连片磨损，已目检comparison-15-16.jpg；19加入实际44格深峡谷及未剖切玩家视角，已目检player-canyon.png、canyon-overview.png。视觉结论待独立方确认。\n2. 本次manifest覆盖所有当前非历史文件及20份外部参考/提示词。旧15分片不再是正式集，改为20片并精确反拼。\n3. 内部全宽全高和模块门缝已逐格核对；${split.corridor_cross_sections.air_samples}空气采样通过，12格退役接口封口例外保持。外部步行接入没有实现或认证，此项仍开放，详qa/external-access.json。\n4. 当前哈希十房加合并已在真实隔离GameTestServer放置并运行，每份400刻；流体、燃烧、邻居更新、六门双侧同侧拉杆与容器操作均有日志及刻后NBT。此为有限时长制作方自检，不等于长期安全、真实网络玩家或独立验收。\n5. 从复制冻结的十房输入，运行原样复制的合并配方，压缩NBT字节一致且原输入哈希未变，见qa/frozen-assembly-reproduction/result.json。\n\n仅本新S6内写入。未操作正式世界或旧S6，未向其他线程发送消息。\n`)
const excludedNames=new Set(['history','history-firstpass','.gradle','build','world-isolated','world-hooks-isolated','world-walk-isolated'])
const artifacts=[]
const excluded=[]
function visit(directory) {
  for(const entry of fs.readdirSync(directory,{withFileTypes:true}).sort((left,right)=>left.name.localeCompare(right.name))) {
    const filename=path.join(directory,entry.name)
    const relative=path.relative(root,filename).replaceAll('\\','/')
    if(entry.isDirectory()) {
      if(excludedNames.has(entry.name)) excluded.push({path:relative,reason:entry.name.startsWith('history')?'Superseded evidence; never current acceptance':'Regenerable build/cache or isolated world; source, logs, reports and post-tick captures included separately'})
      else visit(filename)
    } else if(relative!=='delivery-manifest.json') artifacts.push({path:relative,absolute:filename,sha256:hash(filename),bytes:fs.statSync(filename).size})
  }
}
visit(root)
write('delivery-manifest.json',{stage:submission.stage,created:submission.updated,hash_algorithm:'SHA-256',manifest_self_excluded:true,artifacts,external_references:external,excluded,independent_acceptance:false,open_review_items:submission.open_review_items})
for(const artifact of artifacts) assert.equal(hash(artifact.absolute),artifact.sha256)
console.log(JSON.stringify({rooms:rooms.length,assembly:assembly.sha256,size:assembly.size,slices:split.pieces.length,artifacts:artifacts.length,external_references:external.length,runtime:'PASS',frozen_assembly:'PASS',open_external_access:true,accepted:false}))
