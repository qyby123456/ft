import fs from 'node:fs'
import path from 'node:path'
import crypto from 'node:crypto'
import {NbtFile, Structure} from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'
import {fileURLToPath} from 'node:url'
const root=path.dirname(fileURLToPath(import.meta.url))
const candidate=path.join(root,'nbt/s5-03-coral-lantern-cell.nbt')
const hash=bytes=>crypto.createHash('sha256').update(bytes).digest('hex')
function load(file){
  const bytes=fs.readFileSync(file)
  const nbt=NbtFile.read(bytes)
  const structure=Structure.fromNbt(nbt.root)
  const size=structure.getSize()
  const entries=structure.getBlocks()
  const coordinates=new Set(entries.map(entry=>entry.pos.join(',')))
  if(coordinates.size!==entries.length)throw Error('Duplicate block coordinates')
  for(const entry of entries){
    if(entry.pos.some((coordinate,index)=>coordinate<0||coordinate>=size[index]))throw Error('Bounds')
    if(!entry.state.getName().toString().startsWith('minecraft:'))throw Error('Non-vanilla block')
  }
  const solid=entries.filter(entry=>!['minecraft:air','minecraft:water','minecraft:structure_void'].includes(entry.state.getName().toString()))
  const geometry=hash(JSON.stringify(solid.map(entry=>entry.pos.join(',')).sort()))
  const palette={}
  for(const entry of entries){const name=entry.state.getName().toString();palette[name]=(palette[name]||0)+1}
  return {file,size,bytes:bytes.length,sha256:hash(bytes),geometry,nonWaterBlocks:solid.length,records:entries.length,palette,structure}
}
const current=load(candidate)
if(current.size.join(',')!=='19,16,19'||current.records!==5776)throw Error('Wrong candidate volume')
const comparisons=[
  'D:/ft工程文件/FT地牢_第5组逐图重做_20261104/02-coral-study/nbt/s5-02-coral-study.nbt',
  'D:/ft工程文件/FT地牢_第5组逐图重做_20261104/01-coral-chamber/nbt/s5-01-coral-chamber-redo.nbt',
  'D:/ft工程文件/FT地牢_第2组重做B_20261104/S2-01/incoming/G2B-01/room.nbt',
  'D:/ft工程文件/FT地牢_第7组逐图重做_20261104/01-large-chamber/incoming/S7R2-01/room.nbt',
].filter(file=>fs.existsSync(file)).map(load)
for(const other of comparisons)if(other.sha256===current.sha256||other.geometry===current.geometry)throw Error('Exact duplicate')
const compact=({structure,...report})=>report
fs.writeFileSync(path.join(root,'qa/static.json'),JSON.stringify({status:'PASS_LODESTONE_ROUNDTRIP_AND_EXACT_DUPLICATE_ONLY',candidate:compact(current),compared:comparisons.map(compact),limit:'No near-duplicate/semantic acceptance claim. Native registry/state/survival/route and update checks are separate Forge evidence.'},null,2))
console.log('PASS static roundtrip, bounds, full volume and material-independent exact geometry distinction')
