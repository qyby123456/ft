import fs from 'node:fs'
import path from 'node:path'
import {fileURLToPath} from 'node:url'
import {createHash} from 'node:crypto'
import assert from 'node:assert/strict'
import {NbtFile,Structure} from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'
const root=path.dirname(fileURLToPath(import.meta.url))
const read=relative=>JSON.parse(fs.readFileSync(path.join(root,relative),'utf8'))
const hash=filename=>createHash('sha256').update(fs.readFileSync(filename)).digest('hex')
const write=(relative,value)=>fs.writeFileSync(path.join(root,relative),JSON.stringify(value,null,2)+'\n')
const walk=read('assembly/access/walk-interaction-result.json')
assert.equal(walk.status,'PASS')
assert.equal(walk.source_sha256,hash(read('assembly/summary.json').nbtPath))
assert.equal(walk.test_source_sha256,hash(path.join(root,'qa/runtime/src/main/java/net/farmingtales/s6qa/WalkInteractionTests.java')))
assert(fs.readFileSync(path.join(root,'qa/runtime/walk-run.log'),'utf8').includes('BUILD SUCCESSFUL'))
assert.equal(walk.mid_route_teleports,0)
assert(walk.maximum_tick_displacement<2)
assert(walk.natural_movement_ticks>1000)
assert.equal(hash(path.join(root,'assembly/access/after-walk.nbt')),walk.post_capture_sha256)
const source=Structure.fromNbt(NbtFile.read(fs.readFileSync(read('assembly/summary.json').nbtPath)).root)
const after=Structure.fromNbt(NbtFile.read(fs.readFileSync(path.join(root,'assembly/access/after-walk.nbt'))).root)
const containerNames=['minecraft:chest','minecraft:barrel','minecraft:smoker','minecraft:furnace']
const interactionNames=[...containerNames,'minecraft:lever']
const interactionBlocks=source.getBlocks().filter(block=>interactionNames.includes(block.state.getName().toString()))
const targetKey=position=>position.join(', ')
const events=walk.completed.filter(event=>event.native_use)
for(const event of events) {
  assert.equal(event.target,event.first_hit)
  assert(event.eye_hit_distance<=4.5&&event.eye_hit_distance>0)
  assert.equal(event.native_use,'CONSUME')
}
const functional=interactionBlocks.map(block=>{
  const position=targetKey(block.pos)
  const event=events.find(event=>event.label.startsWith('reach-use-reset ')&&event.target===position)
  assert(event,'Missing walked eye interaction '+position)
  const current=after.getBlock(block.pos)
  assert.equal(current.state.getName().toString(),block.state.getName().toString())
  if(block.state.getName().toString()==='minecraft:lever') assert.equal(current.state.getProperties().powered,'false')
  else {
    assert(current.nbt,'Container BE missing '+position)
    assert.equal(current.nbt.getList('Items').length,0,'Inventory not restored '+position)
    if(current.state.getName().toString()==='minecraft:barrel') assert.equal(current.state.getProperties().open,'false')
  }
  return {position:block.pos,id:block.state.getName().toString(),eye:event.eye,feet:event.feet,first_hit:event.first_hit,eye_hit_distance:event.eye_hit_distance,native_use:event.native_use,restored_in_capture:true}
})
assert.equal(functional.filter(item=>containerNames.includes(item.id)).length,23)
assert.equal(functional.filter(item=>item.id==='minecraft:lever').length,13)
const doorParts=source.getBlocks().filter(block=>block.state.getName().toString()==='minecraft:iron_door')
assert.equal(doorParts.length,12)
for(const block of doorParts)assert.equal(after.getBlock(block.pos).state.getProperties().open,'false','Door not reset '+block.pos.join(','))
const targets=read('assembly/design.json').targets
for(const target of targets) assert(walk.completed.some(event=>event.label===target.label&&event.arrived),'Missing continuous target '+target.label)
assert(walk.completed.some(event=>event.label==='library ladder native climb'&&event.highest_feet_y>=8&&event.climbable))
assert(walk.completed.some(event=>event.label==='library ladder descend and return'))
assert(walk.completed.some(event=>event.label==='all six temporary door controls restored'&&event.count===6))
await import('./finalize-access.mjs')
write('qa/walk-interaction-coverage.json',{status:'SELF_TESTED_PASS_PENDING_INDEPENDENT',source_sha256:walk.source_sha256,targets:targets.length,containers:23,levers:13,operations:events.length,functional,continuous_ticks:walk.natural_movement_ticks,distance_blocks:walk.distance_blocks,maximum_tick_displacement:walk.maximum_tick_displacement,mid_route_teleports:0,trace:path.join(root,'assembly/access/walk-trace.json'),post_capture:path.join(root,'assembly/access/after-walk.nbt'),post_capture_sha256:walk.post_capture_sha256,ladder:walk.completed.filter(event=>event.label.includes('ladder native')||event.label.includes('ladder descend')),water_scope:'Well and covered channel are sealed scenery. Continuous declared maintenance/ledge routes are walked; no swimming through solid glass is required or claimed.',limitations:walk.limits,independent_acceptance:false})
const ready=read('ready-for-review.json')
ready.updated=new Date().toISOString()
ready.open_review_items=['独立方复核新增连续原生移动、全部36功能方块实际眼位首命中/use/复位与刻后证据；已通过的接入、视觉及静态成果保持，制作方不自行放行。']
ready.walk_interaction={status:'SELF_TESTED_PASS_PENDING_INDEPENDENT',report:path.join(root,'qa/walk-interaction-coverage.json'),runtime:path.join(root,'assembly/access/walk-interaction-result.json'),targets:58,containers:23,levers:13,mid_route_teleports:0}
ready.not_verified=ready.not_verified.filter(detail=>!detail.startsWith('容器交互站位'))
ready.not_verified.push('箱桶客户端GUI及网络操作、完整生存战斗；当前为原生移动物理和眼位射线/use的模拟玩家自检，尚待独立复核。')
write('ready-for-review.json',ready)
const progress=read('progress.json');progress.status='walk_and_function_interactions_self_checked_pending_independent';progress.not_verified=ready.not_verified;progress.next='Independently review continuous movement and all 36 functional block eye-hit/use/reset evidence. No deployment.';write('progress.json',progress)
const audit=read('qa/delivery-audit.json');audit.walk_interaction={targets:58,containers:23,levers:13,mid_route_teleports:0,source_sha256:walk.source_sha256};audit.not_verified=ready.not_verified;write('qa/delivery-audit.json',audit)
fs.appendFileSync(path.join(root,'README.md'),`\n## 连续移动与功能交互补测\n\n当前合并原样放置后，从入口一次初始定位，使用原生Player.travel输入、重力、碰撞和台阶物理连续行走${walk.natural_movement_ticks}刻，共${walk.distance_blocks.toFixed(2)}格，中途传送0；每刻位移上限实测${walk.maximum_tick_displacement.toFixed(4)}格。58个保留目标均实际走到，包含跨房连接廊、观景台楼梯及井边维护路线。梯子实际爬升后离梯下降返回。全部23容器、13拉杆从走到的站位以实际眼高outline射线首命中自身且距离≤4.5格，再原生use；容器放入/取回后关菜单，拉杆操作后复位。六门通过可达外拉杆临时开启后完成内侧操作，最后走回外拉杆逐个原生use复位；刻后NBT确认所有13拉杆false、23容器空库存。详qa/walk-interaction-coverage.json及assembly/access/walk-interaction-result.json、walk-trace.json、after-walk.nbt。静态路径只选航点，未替代实际移动或到达。\n\n本项为制作方隔离自检，仍需独立确认。箱桶网络客户端GUI、生存战斗尚未认证；封闭井水与玻璃盖渠道仅作水景，已走井边维护路线，不声称能游入封闭水体。前文旧容器测试范围的说明仅针对旧报告，本新增报告提供实际移动/眼距首命中证据。\n`)
fs.appendFileSync(path.join(root,'README.md'),'\n当前汇总入口为finalize-walk.mjs；status.mjs和finalize.mjs均调用该脚本，依次核既有基础、接入及新增连续移动证据。刻后六扇铁门的12个上下半块均为open=false。\n')
fs.appendFileSync(path.join(root,'qa/revision-response.md'),'\n## 连续运动与箱桶/拉杆补充\n\n新增qa/walk-interaction-coverage.json：保持主候选原哈希，58目标连续原生移动到达、全部23容器及13拉杆真实停点眼位首命中距离/use/复位，含楼梯、梯子与封闭水景周边维护步道；中途传送0。刻后NBT验证空库存与所有拉杆false。此前静态或指定位置use的范围未被偷换，本次新增独立待审实测证据。仍非真实网络GUI或完整生存战斗，accepted=false。\n')
const manifest=read('delivery-manifest.json')
const artifacts=[];const excluded=[]
const excludedNames=new Set(['history','history-firstpass','.gradle','build','world-isolated','world-hooks-isolated','world-walk-isolated'])
function visit(directory){for(const entry of fs.readdirSync(directory,{withFileTypes:true}).sort((left,right)=>left.name.localeCompare(right.name))){const filename=path.join(directory,entry.name);const relative=path.relative(root,filename).replaceAll('\\','/');if(entry.isDirectory()){if(excludedNames.has(entry.name))excluded.push({path:relative,reason:entry.name.startsWith('history')?'Superseded evidence':'Regenerable build/cache or isolated world; sources, logs and captures included'});else visit(filename)}else if(relative!=='delivery-manifest.json')artifacts.push({path:relative,absolute:filename,sha256:hash(filename),bytes:fs.statSync(filename).size})}}
visit(root);write('delivery-manifest.json',{...manifest,created:ready.updated,artifacts,excluded,open_review_items:ready.open_review_items})
for(const row of artifacts)assert.equal(hash(row.absolute),row.sha256)
console.log(JSON.stringify({walk:'PASS',targets:targets.length,containers:23,levers:13,operations:events.length,source_sha256:walk.source_sha256,artifacts:artifacts.length,manifest_sha256:hash(path.join(root,'delivery-manifest.json')),accepted:false}))
