import fs from 'node:fs/promises'
import path from 'node:path'
import crypto from 'node:crypto'
import { fileURLToPath } from 'node:url'
import { NbtFile, Structure } from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'
import { size, covered } from './recipe.mjs'
import { remove } from './cutaway.mjs'

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
  {name:'south-court',facing:'south',position:[21,5,42],width:5,height:4},
  {name:'west-underpass',facing:'west',position:[0,5,21],width:5,height:4},
  {name:'east-underpass',facing:'east',position:[42,5,21],width:5,height:4},
  {name:'north-upper',facing:'north',position:[21,14,0],width:5,height:4},
]
const targets=[
  ...connections.map(connection=>({id:connection.name,role:'connection',pos:connection.position})),
  {id:'safe-point',role:'safe-point',pos:[21,5,38]},
  {id:'tree-observation',role:'landmark',pos:[25,5,20]},
  {id:'west-tree-route',role:'bypass',pos:[11,5,25]},
  {id:'pool-rim',role:'rim',pos:[28,5,35]},
  {id:'pool-bottom',role:'ice-surface',pos:[28,4,29]},
  {id:'bone-observation',role:'landmark',pos:[35,5,27]},
  {id:'stair-bottom',role:'stair-approach',pos:[6,5,31]},
  {id:'stair-middle',role:'stair',pos:[6,10,25]},
  {id:'stair-top',role:'stair',pos:[6,14,21]},
  {id:'west-upper',role:'gallery',pos:[6,14,16]},
  {id:'rear-upper',role:'gallery',pos:[21,14,6]},
  {id:'east-upper',role:'gallery',pos:[36,14,26]},
]
const boundary=[]
for(const height of [5,14]) for(let east=0;east<43;east++) for(let south=0;south<43;south++) {
  if(east!==0&&east!==42&&south!==0&&south!==42)continue
  if(at(east,height,south)==='minecraft:air'&&at(east,height+1,south)==='minecraft:air'&&at(east,height-1,south)!=='minecraft:air')boundary.push([east,height,south])
}
const palette={}
for(const block of blocks){const name=block.state.getName().toString();palette[name]=(palette[name]||0)+1}
const roof=[]
for(let east=0;east<43;east++) for(let south=0;south<43;south++) if(covered(east,south)) {
  const solidY=[]
  for(let height=26;height<29;height++) if(at(east,height,south)!=='minecraft:air')solidY.push(height)
  roof.push({x:east,z:south,solid_y:solidY})
}
const previewBytes=await fs.readFile(path.join(root,'preview-only/preview-only-cutaway.nbt'))
const previewGrid=new Map(Structure.fromNbt(NbtFile.read(previewBytes).root).getBlocks().map(block=>[block.pos.join(','),block.state.toString()]))
const occupied=blocks.filter(block=>block.state.getName().toString()!=='minecraft:air')
const seen=new Set(['0,0,0'])
const queue=[[0,0,0]]
for(let index=0;index<queue.length;index++) {
  const [east,height,south]=queue[index]
  for(const offset of [[1,0,0],[-1,0,0],[0,1,0],[0,-1,0],[0,0,1],[0,0,-1]]) {
    const position=[east+offset[0],height+offset[1],south+offset[2]]
    const key=position.join(',')
    if(!seen.has(key)&&grid.has(key)&&at(...position)!=='minecraft:air'){seen.add(key);queue.push(position)}
  }
}
const detached=occupied.filter(block=>!seen.has(block.pos.join(','))).map(block=>block.pos)
const leaves=blocks.filter(block=>block.state.getName().toString()==='minecraft:spruce_leaves')
const tests={
  size:JSON.stringify(structure.getSize())===JSON.stringify(size),
  dataversion:nbt.getNumber('DataVersion')===3465,
  unique_complete_volume:blocks.length===53621&&grid.size===blocks.length,
  bounded:blocks.every(block=>structure.isInside(block.pos)),
  vanilla:blocks.every(block=>block.state.getName().toString().startsWith('minecraft:')),
  no_entities_or_block_entities:nbt.getList('entities').length===0&&blocks.every(block=>!block.nbt),
  no_hazards_containers_fluids:blocks.every(block=>!/command_block|jigsaw|structure_block|spawner|lava$|water$|powder_snow|fire$|chest$|barrel$/.test(block.state.getName().toString())),
  no_snow_layers_or_ordinary_ice:!(palette['minecraft:snow']||palette['minecraft:ice']),
  port_clearance:connections.every(connection=>{
    const[east,height,south]=connection.position
    const axis=connection.facing==='north'||connection.facing==='south'
    for(let offset=-2;offset<=2;offset++) {
      for(let rise=0;rise<4;rise++) if((axis?at(east+offset,height+rise,south):at(east,height+rise,south+offset))!=='minecraft:air')return false
      if((axis?at(east+offset,height-1,south):at(east,height-1,south+offset))==='minecraft:air')return false
    }
    return true
  }),
  roof_covers_gallery:roof.every(column=>column.solid_y.length>0),
  exact_supported_boundary_ports:boundary.length===20&&boundary.every(([east,height,south])=>height===14?(south===0&&east>=19&&east<=23):((south===42&&east>=19&&east<=23)||((east===0||east===42)&&south>=19&&south<=23))),
  central_skylight_real:Array.from({length:23},(_,index)=>index+10).every(east=>Array.from({length:33},(_,index)=>index+10).every(south=>[26,27,28].every(height=>at(east,height,south)==='minecraft:air'))),
  cutaway_exact_air_only:previewGrid.size===grid.size&&blocks.every(block=>previewGrid.get(block.pos.join(','))===(remove(block.pos)?'minecraft:air':block.state.toString())),
  actual_stairs:palette['minecraft:stone_brick_stairs']===52,
  permanent_tree_leaves:leaves.length>300&&leaves.every(block=>block.state.toString().includes('persistent=true')),
  tree_and_pool_distinct:at(17,9,17)==='minecraft:spruce_log'&&at(28,3,28)==='minecraft:blue_ice'&&at(28,4,28)==='minecraft:air',
  gallery_slab_and_railing:at(21,13,6)==='minecraft:stone_bricks'&&at(21,14,9)==='minecraft:spruce_fence'&&at(36,13,26)==='minecraft:stone_bricks',
  all_solids_face_connected:detached.length===0,
}
const reference='D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S3-frost-tundra/out/S3-08-courtyard.png'
const write=async(name,data)=>fs.writeFile(path.join(root,name),JSON.stringify(data,null,2))
await write('room.json',{id:'S3-08-REWORK-B',name:'雪杉冰池错层庭院',size,entrance:[21,5,42],interior:[21,5,38],facing:'south',origin:[0,0,0],connections,sha256,reference,reference_sha256:hash(await fs.readFile(reference)),runtime_slot:null,deployed:false,central_skylight:true,elevations:{lower_feet:5,ice_pool_feet:4,upper_feet:14}})
await write('route-targets.json',{sha256,coordinate_system:'Variable feet elevations; body centers x+0.5/z+0.5; +X east/+Z south',connections,boundary_foot_cells:boundary,targets,start:'south-court',landmarks:{tree_trunk:[17,8,17],tree_planter:[[12,5,12],[23,7,22]],ice_pool:[[23,3,23],[32,3,33]],upper_deck_y:13,stair:[[5,5,29],[8,13,21]],bones:[[36,5,25],[39,8,29]]}})
await write('roof-columns.json',{sha256,coverage:'U-shaped covered galleries; central skylight intentionally open',columns:roof})
await write('blocks.json',blocks.map(block=>({pos:block.pos,state:block.state.toString()})))
await write('attachment-check.json',{sha256,method:'non-air voxel face connectivity, not world physics',detached})
await write('snow-light-boundary.json',{sha256,ordinary_ice:0,snow_layers:0,snow_blocks:palette['minecraft:snow_block'],persistent_leaves:leaves.length,packed_ice:palette['minecraft:packed_ice'],blue_ice:palette['minecraft:blue_ice'],warm_lanterns:palette['minecraft:lantern'],world_light_ticks:'NOT_RUN',rationale:'Persistent spruce leaves with full snow blocks and nonmelting packed/blue ice; no unsupported thin snow layers or live water.'})
await write('static-qa.json',{sha256,status:Object.values(tests).every(Boolean)?'PASS':'FAIL',tests,palette,block_entities:[],preview_sha256:hash(previewBytes),world_ticks:'NOT_RUN'})
await write('rotated-connectors.json',{sha256,pivot:'nonnegative rotated bounds; account for engine pivot translation',rotations:[0,90,180,270].map(angle=>({angle,size,connections:connections.map(connection=>{
  const[east,height,south]=connection.position
  const directions=['north','east','south','west']
  return {...connection,facing:directions[(directions.indexOf(connection.facing)+angle/90)%4],position:angle===0?[east,height,south]:angle===90?[42-south,height,east]:angle===180?[42-east,height,42-south]:[south,height,42-east]}
})}))})
console.log(JSON.stringify({sha256,tests,palette,roof_columns:roof.length,detached},null,2))
if(!Object.values(tests).every(Boolean))process.exitCode=1
