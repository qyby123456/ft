import fs from 'node:fs'
import path from 'node:path'
import crypto from 'node:crypto'
import {fileURLToPath} from 'node:url'
import {NbtFile,Structure} from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'
const root=path.dirname(fileURLToPath(import.meta.url))
const hash=bytes=>crypto.createHash('sha256').update(bytes).digest('hex')
const bytes=fs.readFileSync(path.join(root,'nbt/s5-09-coral-trident-vault.nbt'))
const load=buffer=>Structure.fromNbt(NbtFile.read(buffer).root)
const source=load(bytes)
const require=(valid,message)=>{if(!valid)throw Error(message)}
const name=entry=>entry?.state.getName().toString()
const transform=(point,index)=>index===0?point:index===1?[30-point[2],point[1],point[0]]:index===2?[30-point[0],point[1],30-point[2]]:[point[2],point[1],30-point[0]]
const containers=source.getBlocks().filter(entry=>['minecraft:chest','minecraft:barrel'].includes(name(entry)))
require(containers.length===5,'Container count')
for(const entry of containers)require(entry.nbt&&entry.nbt.getString('id')===name(entry),'Source container entity id')
const route=[[15,3,18],[15,3,17],[15,3,16],[15,3,15],[15,3,14],[15,4,14],[15,4,13],[15,5,13],[15,5,12],[15,5,11],[15,6,11],[15,6,10],[15,6,9],[15,6,8]]
const cases=[]
for(const [index,rotation] of ['NONE','CLOCKWISE_90','CLOCKWISE_180','COUNTERCLOCKWISE_90'].entries()){
  const capture=load(fs.readFileSync(path.join(root,'worldtest',rotation,'after-1200-ticks.nbt')))
  const at=point=>{const rotated=transform(point,index);return capture.getBlock([rotated[0]+8,rotated[1],rotated[2]+8])}
  const wet=point=>name(at(point))==='minecraft:water'&&at(point).state.getProperties().level==='0'
  for(const point of route)for(let rise=0;rise<2;rise++)require(wet([point[0],point[1]+rise,point[2]]),'Approach body source water')
  for(let rise=1;rise<=3;rise++)require(wet([15,6+rise,7]),'Chest lid source-water clearance')
  const entities=[]
  for(const container of containers){
    const actual=at(container.pos)
    require(name(actual)===name(container)&&actual.nbt,'Settled container missing')
    require(actual.nbt.getList('Items').length===0&&!actual.nbt.has('LootTable'),'Container nonempty/loot table')
    const below=at([container.pos[0],container.pos[1]-1,container.pos[2]])
    require(!['minecraft:water','minecraft:air'].includes(name(below)),'Container support')
    entities.push({source:container.pos,rotated:transform(container.pos,index),name:name(actual),block_entity:actual.nbt.toJson()})
  }
  const report=JSON.parse(fs.readFileSync(path.join(root,'worldtest',rotation,'report.json'),'utf8'))
  require(report.empty_chests===1&&report.empty_barrels===4&&report.wet_stairs===15&&report.chest_lid_source_clearance_height===3,'Native vault counts')
  cases.push({rotation,approach_source:route,approach_rotated:route.map(point=>transform(point,index)),lid_source_water_y:[7,8,9],native_unblocked_menu_provider:true,native_empty_chests:1,native_empty_barrels:4,native_wet_stairs:15,entities})
}
const derivativeFile=path.join(root,'preview-only/preview-only-s5-09-water-hidden-not-importable.nbt')
const derivativeBytes=fs.readFileSync(derivativeFile),derivative=load(derivativeBytes)
require(derivative.getSize().join(',')===source.getSize().join(',')&&derivative.getBlocks().length===source.getBlocks().length,'Derivative volume')
let removedWater=0,changedWaterlogged=0
for(const entry of source.getBlocks()){
  const actual=derivative.getBlock(entry.pos),properties={...entry.state.getProperties()}
  if(name(entry)==='minecraft:water'){require(name(actual)==='minecraft:air','Unexpected derivative water');removedWater++}
  else{
    require(name(actual)===name(entry),'Derivative material changed')
    if(properties.waterlogged==='true'){properties.waterlogged='false';changedWaterlogged++}
    require(JSON.stringify(Object.entries(properties).sort())===JSON.stringify(Object.entries(actual.state.getProperties()).sort()),'Unexpected derivative properties')
  }
  require(JSON.stringify(entry.nbt?.toJson()??null)===JSON.stringify(actual.nbt?.toJson()??null),'Derivative entity changed')
}
const reference=path.join(root,'reference/S5-09-treasure.png')
const materials={}
for(const entry of source.getBlocks())materials[name(entry)]=(materials[name(entry)]??0)+1
const metadata={name:'S5-09 coral trident vault',nbt_sha256:hash(bytes),size:[31,22,31],records:21142,data_version:3465,reference:{path:'reference/S5-09-treasure.png',sha256:hash(fs.readFileSync(reference))},ports:[{anchor:[0,3,18],outward:'west',width:5},{anchor:[30,3,18],outward:'east',width:5}],chest:{position:[15,6,7],facing:'south',type:'single',waterlogged:true,approach:[15,6,8],source_lid_clearance_y:[7,9],loot_table:null,contents:[]},guardians:[[8,8,7],[22,8,7]],features:{wet_stairs:15,empty_barrels:4,map_or_banner_entities:false,urns:'Sandstone and waterlogged sandstone-wall approximations, not decorated-pot entities',trident:'Prismarine wall mosaic, not collectible weapon'},integration:{source_ring:8,source_cover_y:[22,29],solid_bed_y:[0,2],wet_gate_outside_depth:3,dry_tunnel_allowed:false},route_scope:'14native3Dcollision-clearance targets feetY3..9; source-only two-high explicit chest approach; no player physics/half-grid sweep/oxygen/AI/opening animation certification'}
fs.writeFileSync(path.join(root,'metadata.json'),JSON.stringify(metadata,null,2))
fs.writeFileSync(path.join(root,'qa/materials.json'),JSON.stringify({nbt_sha256:hash(bytes),total:source.getBlocks().length,materials},null,2))
fs.writeFileSync(path.join(root,'qa/vault-features.json'),JSON.stringify({status:'PASS_PRODUCER_READBACK_PENDING_INDEPENDENT_ACCEPTANCE',nbt_sha256:hash(bytes),cases,geometry_only_derivative:{path:path.relative(root,derivativeFile),sha256:hash(derivativeBytes),source_sha256:hash(bytes),removed_water:removedWater,changed_waterlogged:changedWaterlogged,other_state_or_block_entity_changes:0,cropped:false,importable:false},limit:metadata.route_scope},null,2))
console.log(`PASS vault containers/lid/approach4settled captures; exact GEOMETRY_ONLY derivative:${removedWater}water→air,${changedWaterlogged}waterloggedfalse,no other changes`)
