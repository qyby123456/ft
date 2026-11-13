import fs from 'node:fs/promises'
import path from 'node:path'
import crypto from 'node:crypto'
import {fileURLToPath}from 'node:url'
import {NbtFile,Structure}from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'

const root=path.dirname(fileURLToPath(import.meta.url))
const hash=bytes=>crypto.createHash('sha256').update(bytes).digest('hex')
const bytes=await fs.readFile(path.join(root,'room.nbt'))
const source=Structure.fromNbt(NbtFile.read(bytes).root)
const original=new Map(source.getBlocks().map(block=>[block.pos.join(','),block]))
const reassembled=new Structure(source.getSize())
const observed=new Set()
const directory=path.join(root,'fragments')
await fs.mkdir(directory,{recursive:true})
const fragments=[]
for(const south of[0,48])for(const east of[0,48]){
  const origin=[east,0,south]
  const size=[Math.min(48,89-east),25,Math.min(48,89-south)]
  const fragment=new Structure(size)
  for(const block of source.getBlocks())if(block.pos[0]>=east&&block.pos[0]<east+size[0]&&block.pos[2]>=south&&block.pos[2]<south+size[2]){
    const position=[block.pos[0]-east,block.pos[1],block.pos[2]-south]
    fragment.addBlock(position,block.state.getName(),block.state.getProperties(),block.nbt)
  }
  const file=`part-${fragments.length+1}.nbt`
  const encoded=fragment.writeNbt({dataVersion:3465,compression:'gzip'})
  await fs.writeFile(path.join(directory,file),encoded)
  const diskBytes=await fs.readFile(path.join(directory,file))
  const decoded=Structure.fromNbt(NbtFile.read(diskBytes).root)
  if(JSON.stringify(decoded.getSize())!==JSON.stringify(size)||size.some(axis=>axis>48))throw new Error('Fragment bounds failed')
  let blockEntities=0
  for(const block of decoded.getBlocks()){
    const position=[block.pos[0]+east,block.pos[1],block.pos[2]+south]
    const key=position.join(',')
    if(observed.has(key))throw new Error(`Overlap ${key}`)
    observed.add(key)
    const expected=original.get(key)
    if(!expected||block.state.toString()!==expected.state.toString()||block.nbt?.toString()!==expected.nbt?.toString())throw new Error(`Mismatch ${key}`)
    if(block.nbt)blockEntities++
    reassembled.addBlock(position,block.state.getName(),block.state.getProperties(),block.nbt)
  }
  fragments.push({order:fragments.length+1,file:`fragments/${file}`,origin,size,sha256:hash(diskBytes),records:decoded.getBlocks().length,block_entities:blockEntities,explicit_air_records:decoded.getBlocks().filter(block=>block.state.getName().toString()==='minecraft:air').length})
}
if(observed.size!==198025)throw new Error('Missing coordinates')
const reassembledBytes=reassembled.writeNbt({dataVersion:3465,compression:'gzip'})
await fs.writeFile(path.join(root,'reassembled.nbt'),reassembledBytes)
const readback=Structure.fromNbt(NbtFile.read(await fs.readFile(path.join(root,'reassembled.nbt'))).root)
for(const block of readback.getBlocks()){
  const expected=original.get(block.pos.join(','))
  if(block.state.toString()!==expected.state.toString()||block.nbt?.toString()!==expected.nbt?.toString())throw new Error('Reassembled disk readback failed')
}
const report={status:'PASS',source_sha256:hash(bytes),source_size:source.getSize(),fragments,all_dimensions_le48:true,order:'z-major then x-major; nonoverlapping so any complete order produces same offline structure',coordinate_coverage:observed.size,duplicate_coordinates:0,state_mismatches:0,block_entity_mismatches:0,reassembled_sha256:hash(reassembledBytes),reassembled_records:readback.getBlocks().length,reassembled_block_entities:readback.getBlocks().filter(block=>block.nbt).length,strategy:'Every cell including explicit air is copied; no structure_void. Validate entire 89x25x89 volume before any placement, reject occupation, never preclear. Fragment planes disjoint; block entities kept local as structure block NBT (no world x/y/z tags), translated by placement origin. Real world placement/neighbor updates/intermediate seam survival NOT_RUN; use atomic staging or engine-controlled batch updates if deployed later.',world_deployment:'NOT_RUN'}
await fs.writeFile(path.join(root,'fragments.json'),JSON.stringify(report,null,2))
console.log(JSON.stringify(report,null,2))
