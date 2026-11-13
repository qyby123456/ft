import fs from 'node:fs'
import path from 'node:path'
import crypto from 'node:crypto'
import {fileURLToPath} from 'node:url'
import {NbtFile,Structure} from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'
const root=path.dirname(fileURLToPath(import.meta.url))
const bytes=fs.readFileSync(path.join(root,'nbt/s5-07-coral-gate-vestibule.nbt'))
const source=Structure.fromNbt(NbtFile.read(bytes).root)
const require=(valid,message)=>{if(!valid)throw Error(message)}
const name=entry=>entry?.state.getName().toString()
const props=entry=>entry.state.getProperties()
const stairs=source.getBlocks().filter(entry=>name(entry)==='minecraft:prismarine_brick_stairs')
const panels=source.getBlocks().filter(entry=>name(entry)==='minecraft:warped_trapdoor')
const barrels=source.getBlocks().filter(entry=>name(entry)==='minecraft:barrel')
require(stairs.length===49&&stairs.every(entry=>props(entry).waterlogged==='true'),'Source stairs')
require(panels.length===24&&panels.every(entry=>props(entry).waterlogged==='true'&&props(entry).open==='true'&&props(entry).powered==='false'),'Source side-open panels')
require(barrels.length===5,'Source barrel count')
const profile=[]
for(let south=34;south>=0;south--){
  const feet=south===30||south===29||south===24||south===7||south===3?4:south>=25&&south<=28||south>=4&&south<=6?5:3
  profile.push([15,feet,south])
}
const axisPath=[profile[0]]
for(const target of profile.slice(1)){
  const previous=axisPath.at(-1)
  let height=previous[1]
  while(height<target[1]){height++;axisPath.push([15,height,previous[2]])}
  axisPath.push([15,height,target[2]])
  while(height>target[1]){height--;axisPath.push([15,height,target[2]])}
}
const transform=(point,index)=>index===0?point:index===1?[34-point[2],point[1],point[0]]:index===2?[30-point[0],point[1],34-point[2]]:[point[2],point[1],30-point[0]]
const cases=[]
for(const [index,rotation] of ['NONE','CLOCKWISE_90','CLOCKWISE_180','COUNTERCLOCKWISE_90'].entries()){
  const capture=Structure.fromNbt(NbtFile.read(fs.readFileSync(path.join(root,'worldtest',rotation,'after-1200-ticks.nbt'))).root)
  const at=point=>{const rotated=transform(point,index);return capture.getBlock([rotated[0]+8,rotated[1],rotated[2]+8])}
  for(const point of axisPath)for(let rise=0;rise<2;rise++){
    const entry=at([point[0],point[1]+rise,point[2]])
    require(name(entry)==='minecraft:water'&&props(entry).level==='0',`Axis body not pure source water: ${rotation} ${point} +${rise}`)
  }
  for(const entry of stairs)require(name(at(entry.pos))==='minecraft:prismarine_brick_stairs'&&props(at(entry.pos)).waterlogged==='true','Settled wet stair')
  for(const entry of panels)require(name(at(entry.pos))==='minecraft:warped_trapdoor'&&props(at(entry.pos)).waterlogged==='true'&&props(at(entry.pos)).open==='true','Settled side-open panel')
  for(const entry of barrels){
    const actual=at(entry.pos)
    require(name(actual)==='minecraft:barrel'&&actual.nbt,'Captured barrel entity missing')
    require(!['minecraft:water','minecraft:air'].includes(name(at([entry.pos[0],entry.pos[1]-1,entry.pos[2]]))),'Floating barrel')
  }
  const report=JSON.parse(fs.readFileSync(path.join(root,'worldtest',rotation,'report.json'),'utf8'))
  require(report.empty_barrels===5&&report.wet_stairs===49&&report.open_wet_gate_panels===24,'Native feature count')
  cases.push({rotation,axis_body_source_cells:axisPath.length*2,steps:axisPath.length-1,source_axis_path:axisPath,rotated_axis_path:axisPath.map(point=>transform(point,index)),native_empty_barrels:report.empty_barrels,native_wet_stairs:report.wet_stairs,native_open_panels:report.open_wet_gate_panels})
}
const metadata={name:'S5-07 coral gate vestibule',reference:'reference/S5-07-entrance.png',candidate:'nbt/s5-07-coral-gate-vestibule.nbt',nbt_sha256:crypto.createHash('sha256').update(bytes).digest('hex'),data_version:3465,size:[31,22,35],records:23870,ports:[{anchor:[15,3,34],outward:'south',width:7,height:3,internal_depth:3,outside_depth_required:3},{anchor:[15,3,0],outward:'north',width:7,height:3,internal_depth:3,outside_depth_required:3}],front_gate:{landing:'X12..18,Z26..28,solid floorY4,bodyfeetY5',panels:'24 waterlogged warped trapdoors, side-open in stable supplied configuration; not a coupled two-door mechanism',stairs:'Real waterlogged prismarine-brick stairs; approach rises fromfloorY2 toY4 and falls back toY2 inside'},rear_arch:{landing:'X12..18,Z4..6,solid floorY4,bodyfeetY5',interface:'Returns tofloorY2/feetY3 atZ0..2 for wet integration'},features:{guard_alcoves:4,empty_barrels:5,map:'Sandstone/cyan terracotta mosaic; not functional map entity',brazier:'Sea-lantern pedestal, no underwater fire'},integration:{source_ring:8,source_cover_y:[22,29],solid_bed_y:[0,2],dry_tunnel_allowed:false},route_scope:'Native six-neighbor3D BFS with two-high exact collision clearance, feetY3..7; extra explicit center axis is pure source water2high. Swimming route, not walking-step pathfinder/player animation/oxygen test.'}
fs.writeFileSync(path.join(root,'metadata.json'),JSON.stringify(metadata,null,2))
fs.writeFileSync(path.join(root,'qa/entrance-features.json'),JSON.stringify({status:'PASS_PRODUCER_READBACK_PENDING_INDEPENDENT_ACCEPTANCE',nbt_sha256:metadata.nbt_sha256,profile,cases,limit:'Producer settled-capture readback and native fixture reports. No interactive open/close simulation of the separate trapdoors; do not close them and reuse supplied-state evidence. No player or mobAI execution.'},null,2))
console.log(`PASS source and four settled captures:49 wet stairs,24 side-open panels,5 supported native empty barrels, pure-source2high center axis${axisPath.length-1} steps`)
