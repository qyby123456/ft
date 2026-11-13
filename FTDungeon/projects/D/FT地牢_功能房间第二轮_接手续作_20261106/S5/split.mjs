import fs from 'node:fs'
import path from 'node:path'
import crypto from 'node:crypto'
import {createRequire} from 'node:module'
import {pathToFileURL} from 'node:url'
import {root} from './tools.mjs'
const require=createRequire('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/package.json')
const {Structure,NbtFile}=await import(pathToFileURL(require.resolve('@mattzh72/lodestone')).href)
const folder=path.join(root,'merged'),sourcePath=path.join(folder,'merged.nbt')
const bytes=fs.readFileSync(sourcePath),source=Structure.fromNbt(NbtFile.read(bytes).root),size=source.getSize()
const parts=[]
fs.mkdirSync(path.join(folder,'parts'),{recursive:true})
for(let east=0;east<size[0];east+=48)for(let south=0;south<size[2];south+=48){
  const dimensions=[Math.min(48,size[0]-east),size[1],Math.min(48,size[2]-south)]
  parts.push({origin:[east,0,south],size:dimensions,structure:new Structure(dimensions)})
}
for(const block of source.getBlocks()){
  const part=parts.find(part=>block.pos.every((value,axis)=>value>=part.origin[axis]&&value<part.origin[axis]+part.size[axis]))
  part.structure.addBlock(block.pos.map((value,axis)=>value-part.origin[axis]),block.state.getName(),block.state.getProperties(),block.nbt)
}
const inventory=[]
for(const [index,part] of parts.entries()){
  const filename=`part-${String(index+1).padStart(2,'0')}.nbt`,output=part.structure.writeNbt({dataVersion:3465,compression:'gzip'})
  fs.writeFileSync(path.join(folder,'parts',filename),output)
  inventory.push({path:path.join(folder,'parts',filename),origin:part.origin,size:part.size,sha256:crypto.createHash('sha256').update(output).digest('hex')})
}
fs.writeFileSync(path.join(folder,'parts.json'),JSON.stringify({source_sha256:crypto.createHash('sha256').update(bytes).digest('hex'),parts:inventory},null,2))
console.log('Wrote',parts.length,'parts')
