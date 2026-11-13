import fs from 'node:fs'
import path from 'node:path'
import crypto from 'node:crypto'
import {fileURLToPath} from 'node:url'
import {NbtFile,Structure} from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'
const root=path.dirname(fileURLToPath(import.meta.url))
const bytes=fs.readFileSync(path.join(root,'nbt/s5-08-coral-skylight-courtyard.nbt'))
const load=buffer=>Structure.fromNbt(NbtFile.read(buffer).root)
const source=load(bytes)
const require=(valid,message)=>{if(!valid)throw Error(message)}
const name=entry=>entry?.state.getName().toString()
const transform=(point,index)=>index===0?point:index===1?[34-point[2],point[1],point[0]]:index===2?[34-point[0],point[1],34-point[2]]:[point[2],point[1],34-point[0]]
const offsets=[[1,0,0],[-1,0,0],[0,1,0],[0,-1,0],[0,0,1],[0,0,-1]]
const masonry=new Set(['minecraft:prismarine','minecraft:prismarine_bricks','minecraft:dark_prismarine','minecraft:sandstone','minecraft:sea_lantern'])
const solids=new Set(source.getBlocks().filter(entry=>masonry.has(name(entry))||name(entry).endsWith('_coral_block')).map(entry=>entry.pos.join(',')))
const connected=new Set(),pending=[]
for(const key of solids)if(key.split(',')[1]==='0'){connected.add(key);pending.push(key.split(',').map(Number))}
for(let cursor=0;cursor<pending.length;cursor++)for(const offset of offsets){
  const point=pending[cursor].map((coordinate,index)=>coordinate+offset[index]),key=point.join(',')
  if(solids.has(key)&&!connected.has(key)){connected.add(key);pending.push(point)}
}
require(connected.size===solids.size,'Disconnected architectural masonry')
const rails=source.getBlocks().filter(entry=>name(entry)==='minecraft:iron_bars')
require(rails.length===150&&rails.every(entry=>entry.state.getProperties().waterlogged==='true'),'Waterlogged upper rails')
const makePath=waypoints=>{
  const result=[waypoints[0]]
  for(const target of waypoints.slice(1)){
    const current=[...result.at(-1)]
    const changed=current.map((coordinate,index)=>coordinate!==target[index]).filter(Boolean).length
    require(changed===1,'Axis-aligned waypoints required')
    const axis=current.findIndex((coordinate,index)=>coordinate!==target[index])
    while(current[axis]!==target[axis]){current[axis]+=Math.sign(target[axis]-current[axis]);result.push([...current])}
  }
  return result
}
const paths={
  fountain_ring:makePath([[10,3,10],[24,3,10],[24,3,24],[10,3,24],[10,3,10]]),
  west_gallery:makePath([[10,3,17],[10,16,17],[4,16,17],[4,13,17]]),
  east_gallery:makePath([[24,3,17],[24,16,17],[30,16,17],[30,13,17]]),
  north_gallery:makePath([[17,3,10],[17,16,10],[17,16,4],[17,13,4]])
}
const cases=[]
for(const [index,rotation] of ['NONE','CLOCKWISE_90','CLOCKWISE_180','COUNTERCLOCKWISE_90'].entries()){
  const capture=load(fs.readFileSync(path.join(root,'worldtest-r2',rotation,'after-1200-ticks.nbt')))
  const at=point=>{const rotated=transform(point,index);return capture.getBlock([rotated[0]+8,rotated[1],rotated[2]+8])}
  const wet=point=>name(at(point))==='minecraft:water'&&at(point).state.getProperties().level==='0'
  for(const route of Object.values(paths))for(const point of route)for(let rise=0;rise<2;rise++)require(wet([point[0],point[1]+rise,point[2]]),`Body source water missing ${rotation} ${point} +${rise}`)
  let aperture=0
  for(let east=6;east<=28;east++)for(let south=6;south<=28;south++)for(let up=19;up<=21;up++){require(wet([east,up,south]),'Skylight source aperture');aperture++}
  for(const rail of rails)require(name(at(rail.pos))==='minecraft:iron_bars'&&at(rail.pos).state.getProperties().waterlogged==='true','Settled upper rail')
  const report=JSON.parse(fs.readFileSync(path.join(root,'worldtest-r2',rotation,'report.json'),'utf8'))
  require(report.wet_upper_rails===150,'Native rail verification')
  cases.push({rotation,source_aperture_records:aperture,native_waterlogged_rails:150,routes:Object.fromEntries(Object.entries(paths).map(([label,route])=>[label,{steps:route.length-1,source:route,rotated:route.map(point=>transform(point,index))}]))})
}
const metadata={name:'S5-08 coral skylight courtyard',reference:'reference/S5-08-courtyard.png',candidate:'nbt/s5-08-coral-skylight-courtyard.nbt',nbt_sha256:crypto.createHash('sha256').update(bytes).digest('hex'),size:[35,22,35],data_version:3465,records:26950,ports:[{anchor:[17,3,0],outward:'north',width:5},{anchor:[0,3,18],outward:'west',width:5},{anchor:[34,3,18],outward:'east',width:5}],central_feature:{footprint:[[13,3,13],[21,3,21]],tiers:[3,4,5,6],coral_peak_y:11,sea_lantern_base_corners:4,jet_or_bubble_effect:false},upper_gallery:{shape:'U north/west/east',floor_y:12,feet_y:13,railing_y:[13,14],waterlogged_iron_bars:150,access:'Swim tofeetY16, cross above railing, descend toY13; no stairs, no air or player-physics claim'},skylight:{roof_y:19,source_aperture_x:[6,28],source_aperture_z:[6,28],open_above_with_source_water:true},integration:{source_ring:8,source_cover_y:[22,29],solid_bed_y:[0,2],wet_gate_outside_depth:3,dry_tunnel_allowed:false},route_scope:'18 native six-neighbor3D collision-clearance targets, feetY3..18. Extra source-only two-high explicit ring and upper routes. No offline body sweep, player movement, AI, breathing or infinite-time certification.'}
fs.writeFileSync(path.join(root,'metadata.json'),JSON.stringify(metadata,null,2))
fs.writeFileSync(path.join(root,'qa/courtyard-features.json'),JSON.stringify({status:'PASS_PRODUCER_READBACK_PENDING_INDEPENDENT_ACCEPTANCE',nbt_sha256:metadata.nbt_sha256,masonry_support_graph:{six_face_connected_to_y0:true,connected:connected.size,total:solids.size,scope:'Prismarine/sandstone/sea-lantern and full coral blocks; geometric support continuity, not gravity/structural physics. NativecanSurvive separately checks all26950states.'},cases,limit:metadata.route_scope},null,2))
console.log('PASS courtyard central ring,3upper pure-source2high access paths,23x23source skylight,150wet rails and masonry support graph in4settled captures')
