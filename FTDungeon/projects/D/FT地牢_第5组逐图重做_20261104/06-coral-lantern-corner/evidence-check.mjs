import fs from 'node:fs'
import path from 'node:path'
import crypto from 'node:crypto'
import {fileURLToPath} from 'node:url'
import {NbtFile,Structure} from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'
const root=path.dirname(fileURLToPath(import.meta.url))
const expected='12c4d28d023b7e6e28ad27f631e222f50d2040bb325daa0b7433dbff7eac890c'
const hash=bytes=>crypto.createHash('sha256').update(bytes).digest('hex')
const require=(valid,message)=>{if(!valid)throw Error(message)}
require(hash(fs.readFileSync(path.join(root,'nbt/s5-06-coral-lantern-corner.nbt')))===expected,'Candidate drift')
require(hash(fs.readFileSync(path.join(root,'worldtest/frozen.nbt')))===expected,'World fixture NBT mismatch')
const exit=JSON.parse(fs.readFileSync(path.join(root,'worldtest/exit.json'),'utf8').replace(/^\uFEFF/,''))
require(exit.exit_code===0,'World test exit')
const junit=fs.readFileSync(path.join(root,'worldtest/tests.xml'),'utf8')
require((junit.match(/<testcase /g)||[]).length===4&&!junit.includes('<failure'),'Four test success not confirmed')
const rotations=['NONE','CLOCKWISE_90','CLOCKWISE_180','COUNTERCLOCKWISE_90']
const cases=[]
for(const rotation of rotations){
  const folder=path.join(root,'worldtest',rotation)
  const report=JSON.parse(fs.readFileSync(path.join(folder,'report.json'),'utf8'))
  require(report.nbt_sha256===expected&&report.server_ticks>=1200&&report.random_tick_speed===3&&report.knownShape===false&&report.placement_flags===3,'World test parameters')
  for(const field of ['placement_expected_vs_actual','explicit_neighbor_update_diff','before_vs_1200_diff','after_update_vs_1200_diff'])require(report[field].length===0,`${rotation} ${field} not empty`)
  for(const field of ['dead_coral','coral_identity_changes','invalid_aquatic_survival','original_water_no_longer_source_water'])require(report.settled[field]===0,`${rotation} ${field}`)
  require(report.settled.dropped_items.length===0&&report.empty_chests===0&&report.native_canSurvive_checked===17298,'Native survival or entity contents')
  require(report.random_tick_probes.lower_section_grown>0&&report.random_tick_probes.upper_section_grown>0,'No natural random tick evidence')
  require(report.initial_routes.targets.length===14&&report.settled_routes.targets.length===14&&report.settled_routes.reachable_count>100,'Route target coverage')
  const captures=[]
  for(const name of ['before-explicit-updates.nbt','after-explicit-updates.nbt','after-1200-ticks.nbt']){
    const file=path.join(folder,name)
    const bytes=fs.readFileSync(file)
    const structure=Structure.fromNbt(NbtFile.read(bytes).root)
    require(structure.getSize().join(',')==='59,30,59'&&structure.getBlocks().length===104430,'Incomplete whole-tank capture')
    const signature=structure.getBlocks().map(entry=>[entry.pos.join(','),entry.state.getName().toString(),Object.entries(entry.state.getProperties()).sort(),entry.nbt?.toJson()??null]).sort((left,right)=>left[0].localeCompare(right[0]))
    captures.push({name,sha256:hash(bytes),states_and_block_entities_hash:hash(JSON.stringify(signature)),size:structure.getSize(),records:signature.length})
  }
  require(new Set(captures.map(capture=>capture.states_and_block_entities_hash)).size===1,'Capture state/entity difference')
  cases.push({rotation,ticks:report.server_ticks,source_vs_placement_diff:0,neighbor_update_diff:0,tick_diff:0,random_probes:[report.random_tick_probes.lower_section_grown,report.random_tick_probes.upper_section_grown],reachable:report.settled_routes.reachable_count,empty_chests:0,captures})
}
fs.writeFileSync(path.join(root,'qa/world-evidence-check.json'),JSON.stringify({status:'PASS_PRODUCER_EVIDENCE_CHECK_PENDING_INDEPENDENT_ACCEPTANCE',nbt_sha256:expected,cases,scope:'Readback of producer-executed isolated Forge test; not an independent server run or formal integration.'},null,2))
console.log('PASS 4x1200tick evidence, 12x104430 native capture decodes, unchanged block states/entities and routes')



