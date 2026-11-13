import fs from 'node:fs'
import path from 'node:path'
import crypto from 'node:crypto'
import {createRequire} from 'node:module'
import {pathToFileURL} from 'node:url'
import {root} from './tools.mjs'
const require=createRequire('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/package.json')
const {NbtFile,Structure}=await import(pathToFileURL(require.resolve('@mattzh72/lodestone')).href)
const folder=path.join(root,'merged')
const manifest=JSON.parse(fs.readFileSync(path.join(folder,'parts.json')))
const hash=bytes=>crypto.createHash('sha256').update(bytes).digest('hex')
const sourceBytes=fs.readFileSync(path.join(folder,'merged.nbt'))
if(hash(sourceBytes)!==manifest.source_sha256)throw Error('Source hash mismatch')
const source=Structure.fromNbt(NbtFile.read(sourceBytes).root)
const signature=block=>JSON.stringify([block.state.getName(),Object.entries(block.state.getProperties()).sort(),block.nbt?.toJson()??null])
const expected=new Map(source.getBlocks().map(block=>[block.pos.join(','),signature(block)]))
let count=0,payloads=0
for(const part of manifest.parts){
  if(part.size.some(dimension=>dimension>48))throw Error('Oversized part')
  const bytes=fs.readFileSync(part.path)
  if(hash(bytes)!==part.sha256)throw Error('Part hash mismatch')
  const structure=Structure.fromNbt(NbtFile.read(bytes).root)
  for(const block of structure.getBlocks()){
    const key=block.pos.map((value,axis)=>value+part.origin[axis]).join(',')
    if(expected.get(key)!==signature(block))throw Error('Record/payload mismatch '+key)
    expected.delete(key);count++;if(block.nbt)payloads++
  }
}
if(expected.size)throw Error('Missing source records '+expected.size)
const report={source_sha256:manifest.source_sha256,passed:true,part_count:manifest.parts.length,records_compared:count,block_entity_payloads_compared:payloads,entity_count:0,scope:'Every block position, ID, sorted properties and full block NBT payload compared after inverse part translation. Source has no entities.'}
fs.writeFileSync(path.join(folder,'parts-reconstruction.json'),JSON.stringify(report,null,2))
console.log(report)
