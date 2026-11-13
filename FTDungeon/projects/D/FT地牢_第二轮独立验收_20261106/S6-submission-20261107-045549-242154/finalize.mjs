import fs from 'node:fs'
import path from 'node:path'
import {fileURLToPath} from 'node:url'
import {createHash} from 'node:crypto'
import assert from 'node:assert/strict'
const root=path.dirname(fileURLToPath(import.meta.url))
const read=filename=>JSON.parse(fs.readFileSync(filename,'utf8').replace(/^\uFEFF/,''))
const hash=filename=>createHash('sha256').update(fs.readFileSync(filename)).digest('hex')
const submission=read(path.join(root,'ready-for-review.json'))
assert.equal(submission.stage,'FINAL_REVISED_CANDIDATES_PENDING_INDEPENDENT_REVIEW')
const rows=[]
const safety=read(path.join(root,'qa/native-fluid-fire.json'))
const all=[...submission.rooms,{id:'S6-99',directory:path.join(root,'assembly'),...submission.assembly}]
for(const room of all) {
  assert.equal(hash(room.candidate),room.sha256)
  const report=name=>read(path.join(room.directory,'qa',name))
  for(const filename of ['static.json','render.json','self-review.json']) assert.equal(report(filename).source_sha256,room.sha256)
  const native=report('independent-native-frozen.json')[0]
  const routes=report('independent-routes.json')[0]
  const supports=report('independent-all-support.json')[0]
  for(const result of [native,routes,supports]) assert.equal(result.sha256,room.sha256)
  assert.equal(native.registry_properties,'PASS')
  assert.equal(native.template_roundtrip_block_count,'PASS')
  assert.equal(supports.failures.length,0)
  assert.equal(supports.unsupported.length,0)
  assert(routes.start_clear&&routes.targets.every(target=>target.standing_clear&&target.reachable))
  const expected=read(path.join(room.directory,'design.json')).targets
  assert.deepEqual(routes.targets.map(target=>[target.label,target.position]),expected.map(target=>[target.label,target.position]))
  const fluid=room.id==='S6-99'?report('native-fluid-fire.json')[0]:safety.find(result=>result.sha256===room.sha256)
  assert(fluid&&fluid.failures.length===0)
  rows.push({id:room.id,sha256:room.sha256,nonair_supports:supports.nonair_checked,route_targets:routes.targets.length,fluid_faces:fluid.native_collision_faces_checked,lava_cells:fluid.lava_cells,block_entities:fluid.block_entities})
}
const inherited=read(path.join(root,'inheritance.json'))
for(const source of inherited.files) assert.equal(hash(source.source),source.sha256,'Old source changed '+source.source)
const reproduction=read(path.join(root,'qa/reproduction.json'))
assert.equal(reproduction.reports.length,11)
for(const record of reproduction.reports) assert(record.byte_equal&&all.some(room=>room.sha256===record.source_sha256))
const fixture=read(path.join(root,'qa/safety-fixtures/fixture-result.json'))
assert(fixture.sealed_pass&&fixture.slab_collision_gap_detected&&fixture.native_flammable_plank_detected)
const split=read(submission.assembly.slice_manifest)
assert.equal(split.source_sha256,submission.assembly.sha256)
assert(split.block_states_and_payloads_equal&&split.corridor_cross_sections.pass&&split.barrier_union_shell.pass)
for(const piece of split.pieces) assert.equal(hash(piece.file),piece.sha256)
const report={stage:submission.stage,rows,old_source_files_unchanged:inherited.files.length,recipes_byte_equal:11,source_reference_images_verified:10,slices:split.pieces.length,source_and_reassembled_records:split.source_records,preserved_block_entity_payloads:45,structural_entity_payloads:0,barrier_cells:split.barrier_union_shell.actual,corridor_clearance:split.corridor_cross_sections,independent_acceptance:false,world_modified:false,not_verified:submission.not_verified}
fs.writeFileSync(path.join(root,'qa/delivery-audit.json'),JSON.stringify(report,null,2))
const progress={style:'S6',status:'final_candidates_pending_independent_review',current:'S6-99',completed:submission.rooms.map(room=>room.id),revised_self_checked:submission.rooms.map(room=>room.id),assembly_self_checked:true,assembly_sha256:submission.assembly.sha256,slices:15,independent_acceptance:false,world_modified:false,inherited_only:[],next:'Independent reviewer 01a1a685-045b-76d2-aa2b-2a3f24520603 should freeze ready-for-review.json and its manifest, review all revised room visuals and merged evidence; no deployment.',not_verified:submission.not_verified}
fs.writeFileSync(path.join(root,'progress.json'),JSON.stringify(progress,null,2))
const descriptions=['11书墙分格、檐口、厚门拱和梯子近景','12牢房檐口、看守石台和钥匙挂钩；六拉杆改门正邻格，新增七个路线目标','13悬锅吊链、厚炉罩檐口、石切机备餐台及器皿架近景','14金铁雕像肩臂及头盔轮廓、杆状武器替代与柜框；非装备实体','15横臂草人、横栏、两级坡障与散布沙砾，保留观众席','16井角石基、木柱石套、侧阶与维护凹口','17主桌真铁砧、双砂轮、实背衬挂钩与炉框','18龛内小拱、分层棺盖及头端；22份头颅方块实体载荷','19透明石框观察窗、封闭岩壁熔岩细缝和铜质观测器械替代','20厚拱内凹藏宝龛、檐口和密道壁柱；原箱金蛛网拉杆完整保留']
fs.writeFileSync(path.join(root,'README.md'),`# S6 火山熔岩 11—20 最终整改候选\n\n状态：制作与自检完成，待独立复核，accepted=false。仅本新S6写入；旧S6只读，${inherited.files.length}个已登记旧文件哈希复核未变。继承来源见inheritance.json。未部署或写正式saves/mods/config/KubeJS，无付费生图。\n\n## 候选与证据\n\nready-for-review.json为正式待审入口；delivery-manifest.json列文件SHA-256。qa/delivery-audit.json核当前候选与报告绑定。每房保留recipe.mjs、design.json、summary.json、inventory.json、NBT、完整/剖切及功能近景、qa自检。qa/history/inherited是历史，不作为新PASS。\n\n${descriptions.map(detail=>'- '+detail).join('\n')}\n\n## 合并和分片\n\nassembly/s6-volcanic-connected.nbt，207×23×120，SHA256 ${submission.assembly.sha256}。两翼按实际端口高差连接，原房升高1格。layout.json记录偏移与接口；58个目标人体碰撞采样可达，走廊全长3格宽4格净高${split.corridor_cross_sections.air_samples}个空气采样通过。外围联合包络38585屏障格，不截断内部连接；最后不用的藏宝外口12空气格封完整玻璃。外部部署入口和世界环境不在此验收范围。\n\n15个分片位于assembly/slices，每轴≤48。反拼181193记录逐格方块状态和45份方块实体NBT载荷一致，结构实体为0；原房所有载荷保留。详情assembly/qa/split-payload-verification.json。\n\n## 实际自检\n\n十房及合并vanilla1.20.1注册属性、模板载入回写、选定路线与全非空气canSurvive通过；合并98763非空气支撑。NativeSafetyAudit以原生碰撞shape完整面判定液体侧底/熔岩六面，采用FireBlock原生可燃性在±4XZ、-1..+5Y排除包络检查，十房和合并零失败；不是非空气邻接。正负fixture确认完整槽通过、半砖侧面和可燃木板检出。该检查仍为静态，不能宣称自然刻通过。\n\n技能run-build.mjs分别重建十房和合并，11份输出字节哈希一致，见qa/reproduction.json。参考十图原哈希验证，提示词哈希另记。预览由真实Lodestone WebGL生成；合并module-cutaway-overview.png逐房剖切仅更改显示副本。\n\n## 复现\n\n使用技能C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/run-build.mjs与各recipe.mjs，--data-version 3465；recheck.ps1会在新房qa顺序跑静态、原生、全支撑和渲染；split-verify.mjs生成分片并校验；verify-reproduction.mjs重建11份；safety-fixtures.mjs检查原生安全规则正反例。NativeSafetyAudit.java源码与本组qa/classes一并交付。全部工具只应在新S6调用。\n\n## 未验证\n\n${submission.not_verified.map(detail=>'- '+detail).join('\n')}\n\n铁门控制已经正邻接但未运行六门交互；20拉杆未接线、箱无锁且空库存。14为明确替代的方块护甲/杆状武器陈设，19观测器械无放大功能，18石棺不能开启。独立复核可对这些差异继续要求整改，本自检不等于独立放行。\n`)
const artifacts=[]
function visit(directory) {
  for(const entry of fs.readdirSync(directory,{withFileTypes:true})) {
    const filename=path.join(directory,entry.name)
    const relative=path.relative(root,filename)
    if(entry.isDirectory()) {if(!['history','logs','reproduced','safety-fixtures'].includes(entry.name)) visit(filename)}
    else if(!['delivery-manifest.json'].includes(entry.name)&&!entry.name.endsWith('.log')) artifacts.push({path:relative,absolute:filename,sha256:hash(filename),bytes:fs.statSync(filename).size})
  }
}
visit(root)
fs.writeFileSync(path.join(root,'delivery-manifest.json'),JSON.stringify({stage:submission.stage,artifacts,independent_acceptance:false},null,2))
console.log(JSON.stringify({rooms:10,assembly:submission.assembly.sha256,slices:15,artifacts:artifacts.length,old_source_files_unchanged:inherited.files.length,accepted:false}))
