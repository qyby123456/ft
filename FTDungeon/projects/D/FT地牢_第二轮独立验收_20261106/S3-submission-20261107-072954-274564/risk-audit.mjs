import fs from 'node:fs'
import path from 'node:path'
import crypto from 'node:crypto'
import {fileURLToPath} from 'node:url'
import {Structure,NbtFile} from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'
const root=path.dirname(fileURLToPath(import.meta.url)),results=[]
for(const identifier of [...Array.from({length:10},(_,index)=>`s3-${index+11}`),'s3-merged']){
  const bytes=fs.readFileSync(path.join(root,'rooms',identifier,identifier+'.nbt'))
  const source=Structure.fromNbt(NbtFile.read(bytes).root)
  const fluids=[],meltable=[],plants=[],gravity=[]
  for(const block of source.getBlocks()){
    const name=block.state.getName().toString(),properties=block.state.getProperties()
    if(['minecraft:water','minecraft:lava','minecraft:powder_snow'].includes(name)||properties.waterlogged==='true')fluids.push({pos:block.pos,state:block.state.toString()})
    if(['minecraft:ice','minecraft:frosted_ice','minecraft:snow'].includes(name))meltable.push({pos:block.pos,state:block.state.toString()})
    if(/sapling|flower|fern|grass|vine|bamboo|cactus|sugar_cane|crop|wheat|potatoes|carrots|bush/.test(name))plants.push({pos:block.pos,state:block.state.toString()})
    if(['minecraft:sand','minecraft:red_sand','minecraft:gravel'].includes(name)){
      const below=source.getBlock([block.pos[0],block.pos[1]-1,block.pos[2]])
      if(!below||below.state.getName().toString()==='minecraft:air')gravity.push({pos:block.pos,state:block.state.toString()})
    }
  }
  results.push({id:identifier,sha256:crypto.createHash('sha256').update(bytes).digest('hex'),fluid_or_waterlogged:fluids,meltable_ice_or_snow_layers:meltable,plant_like_records:plants,unsupported_sand_gravel:gravity,scope:'NBT actual properties + simple gravity adjacency only. Full canSurvive in qa/support.json. No neighbor/random/fluid tick simulation.'})
}
fs.writeFileSync(path.join(root,'risk-audit.json'),JSON.stringify({results,dynamic_world_tick:'NOT_RUN'},null,2))
console.log(JSON.stringify(results.map(({id,fluid_or_waterlogged,meltable_ice_or_snow_layers,plant_like_records,unsupported_sand_gravel})=>({id,fluids:fluid_or_waterlogged.length,meltable:meltable_ice_or_snow_layers.length,plants:plant_like_records.length,unsupported_gravity:unsupported_sand_gravel.length}))))
