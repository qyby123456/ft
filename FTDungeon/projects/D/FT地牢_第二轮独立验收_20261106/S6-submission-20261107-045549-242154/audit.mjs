import fs from 'node:fs/promises'
import path from 'node:path'
import {createHash} from 'node:crypto'
import assert from 'node:assert/strict'
import {NbtFile,Structure} from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'
const root=path.resolve(process.argv[2])
const summary=JSON.parse(await fs.readFile(path.join(root,'summary.json'),'utf8'))
const design=JSON.parse(await fs.readFile(path.join(root,'design.json'),'utf8'))
const bytes=await fs.readFile(summary.nbtPath)
const sha256=createHash('sha256').update(bytes).digest('hex')
const source=Structure.fromNbt(NbtFile.read(bytes).root)
const blocks=source.getBlocks()
const cells=new Map(blocks.map(block=>[block.pos.join(','),block]))
const name=position=>cells.get(position.join(','))?.state.getName().toString().replace('minecraft:','')??'air'
const inventory={}
const entities=[]
const failures=[]
const lava=[]
const water=[]
for(const block of blocks) {
  const identifier=block.state.getName().toString()
  inventory[identifier]=(inventory[identifier]??0)+1
  if(block.nbt) entities.push({position:block.pos,block:identifier,nbt:block.nbt.toJson()})
  if(identifier==='minecraft:lava') lava.push(block.pos)
  if(identifier==='minecraft:water') water.push(block.pos)
}
const waterWalls=new Set(['water','mossy_stone_bricks','light_blue_stained_glass','blackstone','polished_blackstone_bricks','obsidian','spruce_log'])
for(const [east,up,south] of water) for(const [dx,dy,dz] of [[1,0,0],[-1,0,0],[0,-1,0],[0,0,1],[0,0,-1]]) {
  const neighbor=name([east+dx,up+dy,south+dz])
  if(!waterWalls.has(neighbor)) failures.push({type:'water_sides_bottom_full_cube',position:[east,up,south],neighbor:[east+dx,up+dy,south+dz],block:neighbor})
}
const solidLavaBoundary=new Set(['obsidian','orange_stained_glass','blackstone','polished_blackstone_bricks','basalt','smooth_basalt','barrier'])
for(const [east,up,south] of lava) {
  for(const [dx,dy,dz] of [[1,0,0],[-1,0,0],[0,1,0],[0,-1,0],[0,0,1],[0,0,-1]]) {
    const neighbor=name([east+dx,up+dy,south+dz])
    if(neighbor!=='lava'&&!solidLavaBoundary.has(neighbor)) failures.push({type:'lava_full_cube_containment',position:[east,up,south],neighbor:[east+dx,up+dy,south+dz],block:neighbor})
  }
  for(let dx=-4;dx<=4;dx++) for(let dz=-4;dz<=4;dz++) for(let dy=-1;dy<=5;dy++) {
    const adjacent=name([east+dx,up+dy,south+dz])
    if(/bookshelf|planks|wool|carpet|hay_block|log|leaves/.test(adjacent)) failures.push({type:'conservative_fire_margin',lava:[east,up,south],position:[east+dx,up+dy,south+dz],block:adjacent})
  }
}
for(const port of design.ports) {
  for(let across=-1;across<=1;across++) for(let rise=0;rise<port.clearHeight;rise++) {
    const [east,up,south]=port.feet
    const position=[east+(['north','south'].includes(port.side)?across:0),up+rise,south+(['east','west'].includes(port.side)?across:0)]
    if(name(position)!=='air') failures.push({type:'port_clearance',position,block:name(position)})
  }
}
await fs.mkdir(path.join(root,'qa'),{recursive:true})
await fs.writeFile(path.join(root,'inventory.json'),JSON.stringify({source_sha256:sha256,size:summary.size,block_count:blocks.length,blocks:inventory,block_entities:entities},null,2))
await fs.writeFile(path.join(root,'qa/route-input.json'),JSON.stringify([{group:Number(design.id.split('-')[1]),candidate:summary.nbtPath,sha256,swimming:false,start:design.ports[0].feet,targets:design.targets}],null,2))
const report={source_sha256:sha256,self_check_only:true,independent_acceptance:false,failures,lava_cells:lava.length,water_cells:water.length,water_containment:'Four sides and bottom full-cube allowlist; open top allowed',lava_containment:'Six-neighbor full-cube allowlist, lava-to-lava interior allowed; no partial blocks accepted',fire_margin:'Conservative +/-4 XZ, -1..+5 Y combustible exclusion around every lava cell',block_entities:entities.length,runtime_fluid_ticks:false,gameplay_tested:false}
await fs.writeFile(path.join(root,'qa/static.json'),JSON.stringify(report,null,2))
assert.equal(failures.length,0,JSON.stringify(failures.slice(0,10)))
console.log(JSON.stringify({sha256,lava_cells:lava.length,entities:entities.length,status:'static checks passed'}))
