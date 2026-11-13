import fs from 'node:fs'
import path from 'node:path'
import crypto from 'node:crypto'
import {fileURLToPath} from 'node:url'
import build from './recipe.mjs'
import {NbtFile,Structure} from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'
const root=path.dirname(fileURLToPath(import.meta.url))
const hash=bytes=>crypto.createHash('sha256').update(bytes).digest('hex')
const require=(condition,message)=>{if(!condition)throw Error(message)}
const load=file=>Structure.fromNbt(NbtFile.read(fs.readFileSync(file)).root)
const json=file=>JSON.parse(fs.readFileSync(file,'utf8').replace(/^\uFEFF/,''))
const write=(name,value)=>fs.writeFileSync(path.join(root,'qa',name+'.json'),JSON.stringify(value,null,2))
const sourceFile=path.join(root,'nbt/s5-10-coral-ocean-complex.nbt')
const sourceHash=hash(fs.readFileSync(sourceFile))
require(sourceHash==='1cb69d8ec260dd3a97da75b2b3a6db3f11e140da351e18bcec10c1c35dd7f92b','Frozen source drift')
require(hash(fs.readFileSync(path.join(root,'worldtest/frozen.nbt')))===sourceHash,'Frozen world source drift')
const source=load(sourceFile)
const specification=build({createStructure:()=>null,setBlock:()=>{},fill:()=>{},createBlockNbt:value=>value}).metadata
const referenceFile=path.join(root,'reference/S5-10-overview.png')
const originalReference='D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S5-coral-ocean/out/S5-10-overview.png'
require(hash(fs.readFileSync(referenceFile))===hash(fs.readFileSync(originalReference)),'Wrong reference')
fs.writeFileSync(path.join(root,'metadata.json'),JSON.stringify({...specification,size:source.getSize(),nbt_sha256:sourceHash,reference:{original:originalReference,sha256:hash(fs.readFileSync(referenceFile))},scope:'S5-10 only. Pending independent acceptance. No deployment.'},null,2))
const name=entry=>entry?.state.getName().toString()
const wet=entry=>name(entry)==='minecraft:water'&&entry.state.getProperties().level==='0'
const solid=entry=>entry&&!['minecraft:water','minecraft:air','minecraft:kelp','minecraft:kelp_plant','minecraft:sea_pickle'].includes(name(entry))&&!name(entry).endsWith('_coral')
const signature=entry=>JSON.stringify([name(entry),Object.entries(entry.state.getProperties()).sort(),entry.nbt?.toJson()??null])
require(source.getSize().join(',')==='65,22,65'&&source.getBlocks().length===92950,'Incomplete source')
const counts={}
const containers=[]
let air=0
for(const entry of source.getBlocks()){
  counts[name(entry)]=(counts[name(entry)]||0)+1
  if(name(entry)==='minecraft:air'||name(entry)==='minecraft:structure_void')air++
  require(name(entry).startsWith('minecraft:'),'Nonvanilla namespace')
  if(['minecraft:chest','minecraft:barrel'].includes(name(entry))){
    require(entry.nbt?.getString('id')===name(entry)&&entry.nbt.getList('Items').length===0&&!entry.nbt.has('LootTable'),'Container entity contents')
    require(solid(source.getBlock([entry.pos[0],entry.pos[1]-1,entry.pos[2]])),'Unsupported container')
    containers.push({position:entry.pos,block:name(entry),nbt:entry.nbt.toJson()})
  }
}
require(air===0&&containers.length===6,'Air or missing containers')
write('materials',{nbt_sha256:sourceHash,total:92950,blocks:counts,containers})
const minimumRoute=3,maximumRoute=4
const allowed=point=>point[0]>=0&&point[0]<65&&point[2]>=0&&point[2]<65&&point[1]>=minimumRoute&&point[1]<=maximumRoute&&wet(source.getBlock(point))&&wet(source.getBlock([point[0],point[1]+1,point[2]]))
const directions=[[1,0,0],[-1,0,0],[0,1,0],[0,-1,0],[0,0,1],[0,0,-1]]
const start=specification.targets[0]
const pending=[start]
const parents=new Map([[start.join(','),null]])
for(let cursor=0;cursor<pending.length;cursor++)for(const direction of directions){
  const point=pending[cursor].map((value,axis)=>value+direction[axis])
  const key=point.join(',')
  if(!parents.has(key)&&allowed(point)){parents.set(key,pending[cursor]);pending.push(point)}
}
const paths=specification.targets.map(target=>{
  require(parents.has(target.join(',')),'Low-level wet room connectivity failed '+target)
  const points=[]
  for(let point=target;point;point=parents.get(point.join(',')))points.push(point)
  return {target,path:points.reverse()}
})
const containerApproaches=containers.map(container=>{
  const [east,up,south]=container.position
  const target=[east,container.block==='minecraft:barrel'?3:up,south+1]
  require(parents.has(target.join(',')),'Unreachable container front '+container.position)
  const points=[]
  for(let point=target;point;point=parents.get(point.join(',')))points.push(point)
  return {container:container.position,target,path:points.reverse()}
})
write('connectivity',{status:'PASS_ALL_27_TARGETS_AND_SIX_CONTAINER_FRONTS_IN_TWO_HIGH_SOURCE_WATER_NETWORK',feet_y:[minimumRoute,maximumRoute],reachable:parents.size,paths,container_fronts:containerApproaches,rooms:specification.rooms,scope:'Pure-source two-block body columns and six-neighbor connectivity. Confined below walls: cannot pass over room walls. No actual player swim physics, sweep, oxygen or interaction test.'})
const derivative=load(path.join(root,'preview-only/preview-only-s5-10-water-hidden-not-importable.nbt'))
require(derivative.getSize().join(',')===source.getSize().join(',')&&derivative.getBlocks().length===92950,'Cropped derivative')
let hiddenWater=0,unsetWaterlogged=0
for(const entry of source.getBlocks()){
  const preview=derivative.getBlock(entry.pos)
  if(wet(entry)){require(name(preview)==='minecraft:air'&&JSON.stringify(entry.nbt?.toJson()??null)===JSON.stringify(preview.nbt?.toJson()??null),'Derivative water mismatch');hiddenWater++}
  else if(entry.state.getProperties().waterlogged==='true'){
    require(name(preview)===name(entry)&&JSON.stringify(Object.entries(preview.state.getProperties()).sort())===JSON.stringify(Object.entries({...entry.state.getProperties(),waterlogged:'false'}).sort())&&JSON.stringify(entry.nbt?.toJson()??null)===JSON.stringify(preview.nbt?.toJson()??null),'Derivative wet state mismatch');unsetWaterlogged++
  }else require(signature(preview)===signature(entry),'Other derivative state/entity change')
}
write('geometry-only',{status:'PASS_EXACT_WHOLE_VOLUME_MAPPING_GEOMETRY_ONLY',source_sha256:sourceHash,derivative_sha256:hash(fs.readFileSync(path.join(root,'preview-only/preview-only-s5-10-water-hidden-not-importable.nbt'))),water_to_air:hiddenWater,waterlogged_true_to_false:unsetWaterlogged,other_state_entity_changes:0,cropped:false,warning:'Not importable; not water or world evidence.'})
require(json(path.join(root,'worldtest/exit.json')).exit_code===0,'Native exit code')
const junit=fs.readFileSync(path.join(root,'worldtest/tests.xml'),'utf8')
require((junit.match(/<testcase /g)||[]).length===4&&!junit.includes('<failure'),'Native tests not all passing')
const rotations=['NONE','CLOCKWISE_90','CLOCKWISE_180','COUNTERCLOCKWISE_90']
const transform=(point,index)=>index===0?point:index===1?[64-point[2],point[1],point[0]]:index===2?[64-point[0],point[1],64-point[2]]:[point[2],point[1],64-point[0]]
const cases=[]
for(const [index,rotation]of rotations.entries()){
  const folder=path.join(root,'worldtest',rotation)
  const report=json(path.join(folder,'report.json'))
  require(report.nbt_sha256===sourceHash&&report.server_ticks>=1200&&report.random_tick_speed===3&&!report.knownShape&&report.placement_flags===3,'Native parameters')
  for(const field of ['placement_expected_vs_actual','explicit_neighbor_update_diff','before_vs_1200_diff','after_update_vs_1200_diff'])require(report[field].length===0,'Native drift '+field)
  require(report.native_canSurvive_checked===92950&&report.empty_chests===2&&report.empty_barrels===4&&report.wet_stairs===0&&report.chest_lid_source_clearance_height===3,'Native support/container checks')
  for(const field of ['dead_coral','coral_identity_changes','invalid_aquatic_survival','original_water_no_longer_source_water'])require(report.settled[field]===0,'Aquatic failure '+field)
  require(report.settled.dropped_items.length===0&&report.random_tick_probes.lower_section_grown>0&&report.random_tick_probes.upper_section_grown>0,'Drops or random tick inactivity')
  require(report.initial_routes.targets.length===27&&report.settled_routes.targets.length===27,'Native route target coverage')
  const captures=[]
  for(const captureName of ['before-explicit-updates.nbt','after-explicit-updates.nbt','after-1200-ticks.nbt']){
    const file=path.join(folder,captureName)
    const capture=load(file)
    require(capture.getSize().join(',')==='81,30,81'&&capture.getBlocks().length===196830,'Incomplete expanded capture')
    const signatures=capture.getBlocks().map(entry=>[entry.pos.join(','),signature(entry)]).sort((left,right)=>left[0].localeCompare(right[0]))
    captures.push({file:captureName,sha256:hash(fs.readFileSync(file)),size:capture.getSize(),records:196830,states_and_block_entities_sha256:hash(JSON.stringify(signatures))})
    if(captureName==='after-1200-ticks.nbt'){
      const at=point=>capture.getBlock([point[0]+8,point[1],point[2]+8])
      let ring=0,cover=0,bed=0
      for(let east=-8;east<=72;east++)for(let south=-8;south<=72;south++){
        for(let up=0;up<=2;up++){require(solid(at([east,up,south])),'Missing continuous bed');bed++}
        if(east<0||east>64||south<0||south>64)for(let up=3;up<=21;up++){require(wet(at([east,up,south])),'Missing expanded ring');ring++}
        for(let up=22;up<=29;up++){require(wet(at([east,up,south])),'Missing expanded cover');cover++}
      }
      for(const port of specification.ports)for(let depth=-3;depth<=2;depth++)for(let across=-2;across<=2;across++){
        const point=port.outward==='south'?[36+across,3,64-depth]:[64-depth,3,36+across]
        require(solid(at(transform([point[0],2,point[2]],index))),'Missing wet port support')
        for(let rise=0;rise<3;rise++)require(wet(at(transform([point[0],3+rise,point[2]],index))),'Missing inside/outside wet port clearance')
      }
      for(const container of containers){
        const placed=at(transform(container.position,index))
        require(name(placed)===container.block&&placed.nbt?.getList('Items').length===0&&!placed.nbt.has('LootTable'),'Container payload drift')
        if(container.block==='minecraft:chest')for(let rise=1;rise<=3;rise++)require(wet(at(transform([container.position[0],container.position[1]+rise,container.position[2]],index))),'Blocked chest lid')
      }
      for(const route of [...paths,...containerApproaches])for(const point of route.path)for(let rise=0;rise<2;rise++)require(wet(at(transform([point[0],point[1]+rise,point[2]],index))),'Settled pure-water body route changed')
      report.readback_contract={ring_cells:ring,cover_cells:cover,bed_cells:bed,ports:2,port_width:5,port_height:3,outside_depth:3,inside_depth:3,verified_route_targets:27,additional_container_fronts:6}
    }
  }
  require(new Set(captures.map(capture=>capture.states_and_block_entities_sha256)).size===1,'Full capture state/entity drift')
  cases.push({rotation,ticks:report.server_ticks,source_placement_differences:0,native_canSurvive_checked:92950,native_reachable:report.settled_routes.reachable_count,probes:[report.random_tick_probes.lower_section_grown,report.random_tick_probes.upper_section_grown],contract:report.readback_contract,captures})
}
write('world-evidence-check',{status:'PASS_PRODUCER_EXECUTED_AND_READ_BACK_PENDING_INDEPENDENT_ACCEPTANCE',nbt_sha256:sourceHash,cases,scope:'Fresh isolated Forge1.20.1-47.4.10 natural server random ticks; not independent second server run. Finite artificial full-water tank only. Physics, actual GUI opening, AI, oxygen, gameplay and formal deployment NOT_RUN.'})
const previousCheck=json(path.join(root,'../09-coral-trident-vault/qa/integration.json')).preserved
const expectedPrior=[...previousCheck.map(entry=>({file:entry.file,expected:entry.expected})),{file:path.join(root,'../09-coral-trident-vault/nbt/s5-09-coral-trident-vault.nbt'),expected:'57bca6ec9a9cbaac058ef611a9cf518173cb4a6d0549fc3558182d2b38925c11'}]
const prior=expectedPrior.map(entry=>{
  const actual=hash(fs.readFileSync(entry.file))
  require(actual===entry.expected,'Prior accepted source drift '+entry.file)
  return {...entry,sha256:actual,unchanged:true}
})
require(prior.length===9,'Prior accepted source count')
write('preserved',{status:'PASS_PRIOR_NINE_HASHES_UNCHANGED',prior})
console.log('PASS whole source, 27 low-level two-high wet routes, 4x1200 natural ticks, 12x196830 full state/entity captures, full expanded ring/cover/bed, six container payloads, exact geometry derivative and prior nine sources')
