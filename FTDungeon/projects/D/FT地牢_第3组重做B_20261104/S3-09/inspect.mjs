import fs from 'node:fs/promises'
import path from 'node:path'
import crypto from 'node:crypto'
import {fileURLToPath} from 'node:url'
import {NbtFile,Structure} from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'
import {size} from './recipe.mjs'
import {remove} from './cutaway.mjs'

const root=path.dirname(fileURLToPath(import.meta.url))
const hash=bytes=>crypto.createHash('sha256').update(bytes).digest('hex')
const bytes=await fs.readFile(path.join(root,'room.nbt'))
const sha256=hash(bytes)
const nbt=NbtFile.read(bytes).root
const structure=Structure.fromNbt(nbt)
const blocks=structure.getBlocks()
const grid=new Map(blocks.map(block=>[block.pos.join(','),block]))
const at=(east,height,south)=>grid.get([east,height,south].join(','))?.state.getName().toString()
const connections=[
  {name:'south-bridge-approach',facing:'south',position:[29,5,36],width:5,height:4},
  {name:'west-arch',facing:'west',position:[0,5,16],width:5,height:4},
  {name:'east-arch',facing:'east',position:[36,5,16],width:5,height:4},
]
const targets=[
  ...connections.map(connection=>({id:connection.name,role:'connection',pos:connection.position})),
  {id:'safe-point',role:'safe-point',pos:[28,5,33]},
  {id:'bridge',role:'bridge',pos:[28,5,28]},
  {id:'inner-lobby',role:'lobby',pos:[18,5,22]},
  {id:'west-statue',role:'landmark-approach',pos:[8,5,16]},
  {id:'east-statue',role:'landmark-approach',pos:[28,5,16]},
  {id:'dais-step-low',role:'stair',pos:[18,6,17]},
  {id:'dais-step-middle',role:'stair',pos:[18,7,16]},
  {id:'dais-step-high',role:'stair',pos:[18,8,15]},
  {id:'chest-front',role:'container-access',pos:[18,8,12]},
  {id:'chest-back',role:'container-inspection',pos:[18,8,8]},
  {id:'west-pots',role:'decoration-approach',pos:[7,5,11]},
]
const palette={}
for(const block of blocks){const name=block.state.getName().toString();palette[name]=(palette[name]||0)+1}
const roof=[]
for(let east=0;east<37;east++)for(let south=0;south<37;south++)roof.push({x:east,z:south,solid_y:[23,24].filter(height=>at(east,height,south)!=='minecraft:air')})
const boundary=[]
for(let east=0;east<37;east++)for(let south=0;south<37;south++)if((east===0||east===36||south===0||south===36)&&at(east,5,south)==='minecraft:air'&&at(east,6,south)==='minecraft:air')boundary.push([east,5,south])
const previewBytes=await fs.readFile(path.join(root,'preview-only/preview-only-cutaway.nbt'))
const previewGrid=new Map(Structure.fromNbt(NbtFile.read(previewBytes).root).getBlocks().map(block=>[block.pos.join(','),block.state.toString()]))
const entities=blocks.filter(block=>block.nbt).map(block=>({pos:block.pos,state:block.state.toString(),nbt:block.nbt.toString()}))
const occupied=blocks.filter(block=>block.state.getName().toString()!=='minecraft:air')
const seen=new Set(['0,0,0'])
const queue=[[0,0,0]]
for(let index=0;index<queue.length;index++){
  const[east,height,south]=queue[index]
  for(const offset of [[1,0,0],[-1,0,0],[0,1,0],[0,-1,0],[0,0,1],[0,0,-1]]){
    const position=[east+offset[0],height+offset[1],south+offset[2]]
    const key=position.join(',')
    if(!seen.has(key)&&grid.has(key)&&at(...position)!=='minecraft:air'){seen.add(key);queue.push(position)}
  }
}
const detached=occupied.filter(block=>!seen.has(block.pos.join(','))).map(block=>block.pos)
const chest=grid.get('18,8,10')
const tests={
  size:JSON.stringify(structure.getSize())===JSON.stringify(size),
  dataversion:nbt.getNumber('DataVersion')===3465,
  unique_complete_volume:blocks.length===34225&&grid.size===blocks.length,
  bounded:blocks.every(block=>structure.isInside(block.pos)),
  vanilla:blocks.every(block=>block.state.getName().toString().startsWith('minecraft:')),
  no_entities:nbt.getList('entities').length===0,
  no_commands_hazards_or_fluids:blocks.every(block=>!/command_block|jigsaw|structure_block|spawner|lava$|water$|powder_snow|fire$/.test(block.state.getName().toString())),
  no_snow_layers_or_ordinary_ice:!(palette['minecraft:snow']||palette['minecraft:ice']),
  port_clearance:connections.every(connection=>{
    const[east,height,south]=connection.position
    const axis=connection.facing==='south'
    for(let offset=-2;offset<=2;offset++){
      for(let rise=0;rise<4;rise++)if((axis?at(east+offset,height+rise,south):at(east,height+rise,south+offset))!=='minecraft:air')return false
      if((axis?at(east+offset,height-1,south):at(east,height-1,south+offset))==='minecraft:air')return false
    }
    return true
  }),
  exact_boundary_ports:boundary.length===15&&boundary.every(([east,,south])=>(south===36&&east>=27&&east<=31)||((east===0||east===36)&&south>=14&&south<=18)),
  full_roof:roof.every(column=>column.solid_y.length===2),
  cutaway_exact_air_only:previewGrid.size===grid.size&&blocks.every(block=>previewGrid.get(block.pos.join(','))===(remove(block.pos)?'minecraft:air':block.state.toString())),
  source_block_entities_untouched_in_cut:blocks.filter(block=>block.nbt).every(block=>Structure.fromNbt(NbtFile.read(previewBytes).root).getBlock(block.pos)?.nbt?.toString()===block.nbt.toString()),
  one_empty_chest_and_three_banners:entities.length===4&&entities.filter(entity=>entity.state.startsWith('minecraft:chest')).length===1&&entities.filter(entity=>entity.state.startsWith('minecraft:blue_wall_banner')).length===3&&chest.nbt.getString('id')==='minecraft:chest'&&chest.nbt.getList('Items').length===0&&entities.every(entity=>!/LootTable|Command|Lock/.test(entity.nbt)),
  chest_lid_clear: [9,10,11,12].every(height=>at(18,height,10)==='minecraft:air'),
  single_chest_no_join: [[17,8,10],[19,8,10],[18,8,9],[18,8,11]].every(position=>at(...position)!=='minecraft:chest'),
  two_bone_statues: [11,25].every(east=>at(east,12,12)==='minecraft:bone_block'&&at(east,14,12)==='minecraft:bone_block'),
  actual_stairs:palette['minecraft:stone_brick_stairs']===15,
  all_solids_face_connected:detached.length===0,
}
const reference='D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S3-frost-tundra/out/S3-09-treasure.png'
const write=async(name,data)=>fs.writeFile(path.join(root,name),JSON.stringify(data,null,2))
await write('room.json',{id:'S3-09-REWORK-B',name:'霜旗骨卫宝藏室',size,entrance:[29,5,36],interior:[28,5,33],facing:'south',origin:[0,0,0],connections,sha256,reference,reference_sha256:hash(await fs.readFile(reference)),runtime_slot:null,deployed:false,container:{pos:[18,8,10],facing:'south',type:'single',items:[],loot_table:null},elevations:{floor_feet:5,ice_channel_surface_feet:4,dais_feet:8}})
await write('route-targets.json',{sha256,coordinate_system:'Variable feet elevations; body centers x+0.5/z+0.5; +X east/+Z south',connections,boundary_foot_cells:boundary,targets,start:'south-bridge-approach',landmarks:{chest:[18,8,10],statues:[[11,8,12],[25,8,12]],bridge:[[26,4,25],[30,4,32]],dais_stairs:[[16,5,17],[20,7,15]]}})
await write('roof-columns.json',{sha256,coverage:'Full final roof; reference is cutaway concept and final view has separately declared air-only cut',columns:roof})
await write('blocks.json',blocks.map(block=>({pos:block.pos,state:block.state.toString()})))
const states={}
for(const block of blocks){const state=block.state.toString();states[state]=(states[state]||0)+1}
await write('materials.json',{sha256,kind:'Exact final NBT block inventory, not survival crafting recipe or automated reward recipe',total_records:blocks.length,explicit_air:palette['minecraft:air'],solid_and_decorative_records:blocks.length-palette['minecraft:air'],by_block_id:palette,by_state:states,block_entities:entities,source_recipe:'recipe.mjs'})
await write('attachment-check.json',{sha256,method:'non-air voxel face connectivity, not world physics',detached})
await write('snow-light-boundary.json',{sha256,ordinary_ice:0,snow_layers:0,snow_blocks:palette['minecraft:snow_block'],packed_ice:palette['minecraft:packed_ice'],blue_ice:palette['minecraft:blue_ice'],warm_lanterns:palette['minecraft:lantern'],world_light_ticks:'NOT_RUN'})
await write('container-check.json',{sha256,chest_position:[18,8,10],state:chest.state.toString(),nbt:chest.nbt.toString(),above_air_y:[9,10,11,12],approach_target:'chest-front',inventory:'EMPTY',loot_table:'NONE',actual_open_close_and_gui:'NOT_RUN',entity_obstruction:'No entities in template; target world must be checked separately'})
await write('static-qa.json',{sha256,status:Object.values(tests).every(Boolean)?'PASS':'FAIL',tests,palette,block_entities:entities,preview_sha256:hash(previewBytes),world_ticks:'NOT_RUN'})
await write('rotated-connectors.json',{sha256,pivot:'nonnegative rotated bounds; account for engine pivot translation',rotations:[0,90,180,270].map(angle=>({angle,size,connections:connections.map(connection=>{
  const[east,height,south]=connection.position
  const directions=['north','east','south','west']
  return {...connection,facing:directions[(directions.indexOf(connection.facing)+angle/90)%4],position:angle===0?[east,height,south]:angle===90?[36-south,height,east]:angle===180?[36-east,height,36-south]:[south,height,36-east]}
})}))})
console.log(JSON.stringify({sha256,tests,palette,detached},null,2))
if(!Object.values(tests).every(Boolean))process.exitCode=1
