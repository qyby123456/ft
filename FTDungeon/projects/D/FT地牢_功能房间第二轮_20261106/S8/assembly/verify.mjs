import fs from 'node:fs/promises'
import path from 'node:path'
import crypto from 'node:crypto'
import {pathToFileURL,fileURLToPath} from 'node:url'
const root=path.dirname(fileURLToPath(import.meta.url)),layout=JSON.parse(await fs.readFile(path.join(root,'layout.json'),'utf8'))
const {Structure,NbtFile}=await import(pathToFileURL('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'))
const bytes=await fs.readFile(path.join(root,'s8-functional-combined.nbt')),source=Structure.fromNbt(NbtFile.read(bytes).root),size=source.getSize()
const hash=bytes=>crypto.createHash('sha256').update(bytes).digest('hex')
if(hash(bytes)!==layout.sha256)throw Error('Assembly drift')
const blocks=new Map(source.getBlocks().map(block=>[block.pos.join(','),block])),signature=block=>JSON.stringify([block.state.toString(),block.nbt?.toString()??null])
let copied=0,boundary=0
for(const module of layout.modules){
 const bytes=await fs.readFile(module.source);if(hash(bytes)!==module.sha256)throw Error('Room drift')
 for(const block of Structure.fromNbt(NbtFile.read(bytes).root).getBlocks()){
  const position=block.pos.map((value,axis)=>value+module.origin[axis]),actual=blocks.get(position.join(','))
  if(!actual||signature(actual)!==signature(block))throw Error(`Module changed ${module.id} ${position}`)
  copied++
 }
}
for(let x=0;x<size[0];x++)for(let y=0;y<size[1];y++)for(let z=0;z<size[2];z++)if(x===0||y===0||z===0||x===size[0]-1||y===size[1]-1||z===size[2]-1){if(blocks.get([x,y,z].join(','))?.state.getName().toString()!=='minecraft:barrier')throw Error(`Envelope gap ${x},${y},${z}`);boundary++}
const parts=[],rebuilt=new Map();await fs.mkdir(path.join(root,'parts'),{recursive:true})
for(let east=0;east<size[0];east+=48)for(let south=0;south<size[2];south+=48){
 const origin=[east,0,south],partSize=[Math.min(48,size[0]-east),size[1],Math.min(48,size[2]-south)],part=new Structure(partSize)
 for(const block of source.getBlocks())if(block.pos[0]>=east&&block.pos[0]<east+partSize[0]&&block.pos[2]>=south&&block.pos[2]<south+partSize[2])part.addBlock(block.pos.map((value,axis)=>value-origin[axis]),block.state.getName(),block.state.getProperties(),block.nbt)
 const filename=`part-${east}-${south}.nbt`,data=part.writeNbt({dataVersion:3465,compression:'gzip'});await fs.writeFile(path.join(root,'parts',filename),data)
 const restored=Structure.fromNbt(NbtFile.read(data).root)
 for(const block of restored.getBlocks()){
  const global=block.pos.map((value,axis)=>value+origin[axis]),key=global.join(',')
  if(rebuilt.has(key)||signature(block)!==signature(blocks.get(key)))throw Error('Partition payload mismatch')
  rebuilt.set(key,true)
 }
 parts.push({path:path.join(root,'parts',filename),origin,size:partSize,sha256:hash(data),blocks:restored.getBlocks().length})
}
if(rebuilt.size!==blocks.size)throw Error('Missing partition cells')
const report={source_sha256:layout.sha256,source_blocks:blocks.size,room_records_exactly_preserved:copied,barrier_envelope_cells:boundary,partition_cells_compared:rebuilt.size,all_blockstates_and_block_entity_payloads_equal:true,maximum_part_extent:48,parts,entity_scope:'No structure entities; all original block entities preserved byte-semantic NBT payloads'}
await fs.mkdir(path.join(root,'qa'),{recursive:true});await fs.writeFile(path.join(root,'qa','assembly-exactness.json'),JSON.stringify(report,null,2));console.log('Exact assembly and parts verified',copied,rebuilt.size,parts.length)
