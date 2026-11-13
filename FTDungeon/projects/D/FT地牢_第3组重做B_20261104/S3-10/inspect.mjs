import fs from 'node:fs/promises'
import path from 'node:path'
import crypto from 'node:crypto'
import {fileURLToPath}from 'node:url'
import {NbtFile,Structure}from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'
import {size,areas,cellAreas,plannedRoof,contains}from './recipe.mjs'
import {remove}from './cutaway.mjs'

const root=path.dirname(fileURLToPath(import.meta.url))
const hash=bytes=>crypto.createHash('sha256').update(bytes).digest('hex')
const bytes=await fs.readFile(path.join(root,'room.nbt'))
const sha256=hash(bytes)
const nbt=NbtFile.read(bytes).root
const structure=Structure.fromNbt(nbt)
const blocks=structure.getBlocks()
const grid=new Map(blocks.map(block=>[block.pos.join(','),block]))
const at=(east,height,south)=>grid.get([east,height,south].join(','))?.state.getName().toString()
const connections=[{name:'south-entry',facing:'south',position:[43,2,88],width:5,height:4}]
const targets=[
  {id:'south-entry',role:'external-port',pos:[43,2,88]},
  {id:'entrance-safe',role:'entrance',pos:[43,5,75]},
  {id:'wooden-entry-frame',role:'entrance',pos:[43,5,80]},
  {id:'spine-south',role:'corridor',pos:[43,5,62]},
  {id:'spine-middle',role:'corridor',pos:[43,5,50]},
  {id:'spine-north',role:'corridor',pos:[43,5,38]},
  {id:'hall-entry',role:'hall',pos:[43,5,34]},
  {id:'hall-west-bypass',role:'hall',pos:[34,5,20]},
  {id:'hall-east-bypass',role:'hall',pos:[60,5,20]},
  {id:'hall-front',role:'hall',pos:[47,5,30]},
  {id:'hall-rear-icewall',role:'hall',pos:[47,5,9]},
  {id:'crystal-observation',role:'landmark',pos:[47,5,25]},
  {id:'west-cross-corridor',role:'corridor',pos:[34,5,47]},
  {id:'west-right-angle-turn',role:'corner',pos:[25,5,54]},
  {id:'west-pool-rim',role:'pool-room',pos:[15,5,44]},
  {id:'west-pool-bottom',role:'pool-room',pos:[15,4,34]},
  {id:'west-pool-north',role:'pool-room',pos:[15,5,22]},
  {id:'crypt-entry',role:'crypt',pos:[25,5,59]},
  {id:'crypt-bones',role:'crypt',pos:[19,5,69]},
  {id:'crypt-rear',role:'crypt',pos:[20,5,77]},
  {id:'east-cross-corridor',role:'corridor',pos:[51,5,43]},
  {id:'east-right-angle-turn',role:'corner',pos:[57,5,49]},
  {id:'court-entry',role:'courtyard',pos:[61,5,52]},
  {id:'court-pool-rim',role:'courtyard',pos:[71,5,73]},
  {id:'court-pool-bottom',role:'courtyard',pos:[71,4,64]},
  {id:'treasury-short-link',role:'corridor',pos:[66,5,25]},
  {id:'treasury-room',role:'treasury',pos:[76,5,23]},
  {id:'treasury-chest-front',role:'container-access',pos:[76,7,17]},
  {id:'treasury-chest-back',role:'container-inspection',pos:[76,7,13]},
  {id:'outer-stair-middle',role:'stair',pos:[43,4,86]},
]
const interfaces=[
  {id:'entry-to-spine',position:[43,5,65]},
  {id:'spine-to-hall',position:[43,5,34]},
  {id:'spine-to-west-link',position:[41,5,47]},
  {id:'west-link-to-pool',position:[26,5,47]},
  {id:'west-link-to-turn',position:[25,5,49]},
  {id:'west-turn-to-crypt',position:[25,5,59]},
  {id:'spine-to-east-link',position:[45,5,43]},
  {id:'east-link-to-turn',position:[57,5,45]},
  {id:'east-turn-to-court',position:[59,5,51]},
  {id:'hall-to-treasury-link',position:[63,5,25]},
  {id:'treasury-link-to-room',position:[69,5,25]},
]
const palette={},states={}
for(const block of blocks){const name=block.state.getName().toString();palette[name]=(palette[name]||0)+1;states[block.state.toString()]=(states[block.state.toString()]||0)+1}
const roof=[]
const open=[]
for(let east=0;east<89;east++)for(let south=0;south<89;south++){
  if(plannedRoof(east,south)){
    const ceiling=Math.max(...cellAreas(east,south).filter(area=>area.ceiling!==null).map(area=>area.ceiling))
    roof.push({x:east,z:south,ceiling,solid_y:[ceiling,Math.min(24,ceiling+1)].filter(height=>at(east,height,south)!=='minecraft:air')})
  }else if(contains(areas.find(area=>area.id==='ice-court'),east,south,2))open.push([east,south])
}
const boundary=[]
for(const height of[2,5])for(let east=0;east<89;east++)for(let south=0;south<89;south++)if(east===0||east===88||south===0||south===88){
  if(at(east,height,south)==='minecraft:air'&&at(east,height+1,south)==='minecraft:air'&&at(east,height-1,south)!=='minecraft:air')boundary.push([east,height,south])
}
const previewBytes=await fs.readFile(path.join(root,'preview-only/preview-only-cutaway.nbt'))
const preview=Structure.fromNbt(NbtFile.read(previewBytes).root)
const previewGrid=new Map(preview.getBlocks().map(block=>[block.pos.join(','),block]))
const entities=blocks.filter(block=>block.nbt).map(block=>({pos:block.pos,state:block.state.toString(),nbt:block.nbt.toString()}))
const chest=grid.get('76,7,15')
const seen=new Set(['0,0,0']),queue=[[0,0,0]]
const occupied=blocks.filter(block=>block.state.getName().toString()!=='minecraft:air')
for(let index=0;index<queue.length;index++){
  const[east,height,south]=queue[index]
  for(const offset of[[1,0,0],[-1,0,0],[0,1,0],[0,-1,0],[0,0,1],[0,0,-1]]){
    const position=[east+offset[0],height+offset[1],south+offset[2]],key=position.join(',')
    if(!seen.has(key)&&grid.has(key)&&at(...position)!=='minecraft:air'){seen.add(key);queue.push(position)}
  }
}
const detached=occupied.filter(block=>!seen.has(block.pos.join(','))).map(block=>block.pos)
const removed=blocks.filter(block=>block.state.getName().toString()!=='minecraft:air'&&remove(block.pos))
const tests={
  size:JSON.stringify(structure.getSize())===JSON.stringify(size),
  dataversion:nbt.getNumber('DataVersion')===3465,
  full_unique_volume:blocks.length===198025&&grid.size===blocks.length,
  bounded:blocks.every(block=>structure.isInside(block.pos)),
  vanilla:blocks.every(block=>block.state.getName().toString().startsWith('minecraft:')),
  no_entities:nbt.getList('entities').length===0,
  no_commands_hazards_or_fluids:blocks.every(block=>!/command_block|jigsaw|structure_block|spawner|lava$|water$|powder_snow|fire$/.test(block.state.getName().toString())),
  no_snow_layers_ordinary_ice:!(palette['minecraft:snow']||palette['minecraft:ice']),
  one_external_supported_port:boundary.length===5&&boundary.every(([east,height,south])=>height===2&&south===88&&east>=41&&east<=45),
  port_clearance:[41,42,43,44,45].every(east=>[2,3,4,5].every(height=>at(east,height,88)==='minecraft:air')&&at(east,1,88)==='minecraft:stone_bricks'),
  all_internal_interface_centers_clear:interfaces.every(({position:[east,height,south]})=>[height,height+1,height+2,height+3].every(rise=>at(east,rise,south)==='minecraft:air')),
  actual_six_different_main_regions:areas.filter(area=>!['corridor','approach'].includes(area.role)).length===6&&new Set(areas.filter(area=>!['corridor','approach'].includes(area.role)).map(area=>(area.bounds[2]-area.bounds[0]+1)+'x'+(area.bounds[3]-area.bounds[1]+1))).size===6,
  roof_exact_planned_coverage:roof.every(column=>column.solid_y.length===2),
  court_real_open_sky:open.every(([east,south])=>[23,24].every(height=>at(east,height,south)==='minecraft:air')),
  cutaway_exact_air_only:previewGrid.size===grid.size&&blocks.every(block=>previewGrid.get(block.pos.join(','))?.state.toString()===(remove(block.pos)?'minecraft:air':block.state.toString())),
  cutaway_entities_unchanged:blocks.filter(block=>block.nbt).every(block=>previewGrid.get(block.pos.join(','))?.nbt?.toString()===block.nbt.toString()),
  one_empty_chest:entities.length===1&&chest.nbt?.getString('id')==='minecraft:chest'&&chest.nbt.getList('Items').length===0&&!/LootTable|Lock|Command/.test(chest.nbt.toString()),
  chest_lid_air:[8,9,10,11].every(height=>at(76,height,15)==='minecraft:air'),
  all_solids_face_connected:detached.length===0,
}
const reference='D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S3-frost-tundra/out/S3-10-overview.png'
const write=async(name,data)=>fs.writeFile(path.join(root,name),JSON.stringify(data,null,2))
await write('room.json',{id:'S3-10-REWORK-B',name:'霜原多室地牢总览',size,entrance:[43,2,88],interior:[43,5,75],origin:[0,0,0],facing:'south',connections,areas,interfaces,sha256,reference,reference_sha256:hash(await fs.readFile(reference)),runtime_slot:null,deployed:false,covered_rooms:true,courtyard_open_sky:true,not_a_preview_assembly:true})
await write('route-targets.json',{sha256,coordinate_system:'Integer feet cell variable Y; x+0.5/z+0.5 body centers',connections,boundary_foot_cells:boundary,interfaces,targets,start:'south-entry'})
await write('roof-columns.json',{sha256,coverage:'All room/corridor planned roofs; ice courtyard intentional sky; exterior unused columns explicit air above foundation',covered_columns:roof.length,columns:roof,court_open_columns:open})
await write('materials.json',{sha256,kind:'Exact final NBT inventory; no survival crafting or loot recipe',total_records:blocks.length,explicit_air:palette['minecraft:air'],by_block_id:palette,by_state:states,block_entities:entities,source_recipe:'recipe.mjs'})
await write('blocks.json',blocks.map(block=>({pos:block.pos,state:block.state.toString()})))
await write('attachment-check.json',{sha256,method:'non-air voxel face connectivity, not world physics',detached})
await write('snow-light-boundary.json',{sha256,ordinary_ice:0,snow_layers:0,water:0,lava:0,snow_blocks:palette['minecraft:snow_block'],packed_ice:palette['minecraft:packed_ice'],blue_ice:palette['minecraft:blue_ice'],lanterns:palette['minecraft:lantern'],lantern_emission:15,world_light_weather_random_ticks:'NOT_RUN',ice_sliding:'NOT_RUN'})
await write('container-check.json',{sha256,pos:[76,7,15],state:chest.state.toString(),nbt:chest.nbt.toString(),above_air_y:[8,9,10,11],access_targets:['treasury-chest-front','treasury-chest-back'],inventory:'EMPTY27',loot_table:'NONE',interaction_lid_gui_world_entities:'NOT_RUN'})
await write('cutaway-proof.json',{sha256,preview_sha256:hash(previewBytes),rule:'cutaway.mjs remove predicate: planned roofs at maximum covering ceiling; main room boundary walls at Y>=12; all changes to air only',removed_nonair_records:removed.length,removed_coordinates:removed.map(block=>({pos:block.pos,before:block.state.toString()})),block_entities_unchanged:tests.cutaway_entities_unchanged})
await write('static-qa.json',{sha256,status:Object.values(tests).every(Boolean)?'PASS':'FAIL',tests,palette,block_entities:entities,preview_sha256:hash(previewBytes),world_ticks:'NOT_RUN'})
await write('rotated-connectors.json',{sha256,pivot:'nonnegative bounds, account for actual engine pivot translation',rotations:[0,90,180,270].map(angle=>({angle,size,connections:connections.map(connection=>{
  const[east,height,south]=connection.position,directions=['north','east','south','west']
  return{...connection,facing:directions[(directions.indexOf(connection.facing)+angle/90)%4],position:angle===0?[east,height,south]:angle===90?[88-south,height,east]:angle===180?[88-east,height,88-south]:[south,height,88-east]}
})}))})
console.log(JSON.stringify({sha256,tests,palette,roof_columns:roof.length,removed_nonair_records:removed.length,detached},null,2))
if(!Object.values(tests).every(Boolean))process.exitCode=1
