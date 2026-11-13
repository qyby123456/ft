import fs from 'node:fs/promises'
import path from 'node:path'
import crypto from 'node:crypto'
import {pathToFileURL} from 'node:url'
const root=path.resolve(process.argv[2])
const {Structure,NbtFile}=await import(pathToFileURL('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'))
const filename=(await fs.readdir(root)).find(name=>name.endsWith('.nbt'))
const data=await fs.readFile(path.join(root,filename)),sha256=crypto.createHash('sha256').update(data).digest('hex')
const structure=Structure.fromNbt(NbtFile.read(data).root)
const recipe=await import(pathToFileURL(path.join(root,'recipe.mjs')))
const metadata=path.basename(root)==='assembly'?JSON.parse(await fs.readFile(path.join(root,'layout.json'),'utf8')).metadata:recipe.default({createStructure:()=>({}),setBlock(){},fill(){},createBlockNbt:value=>value}).metadata
const counts={},entities=[],hazards=[]
for(const block of structure.getBlocks()){const name=block.state.getName().toString();counts[name]=(counts[name]||0)+1;if(block.nbt)entities.push({pos:block.pos,block:name,nbt:block.nbt.toString()});if(['minecraft:lava','minecraft:fire','minecraft:water'].includes(name))hazards.push({pos:block.pos,block:name})}
await fs.mkdir(path.join(root,'qa'),{recursive:true})
await fs.writeFile(path.join(root,'inventory.json'),JSON.stringify({sha256,size:structure.getSize(),counts,entities,hazards,metadata},null,2))
const specification={group:8,candidate:path.join(root,filename),sha256,size:structure.getSize(),start:metadata.start,targets:metadata.targets,swimming:false}
await fs.writeFile(path.join(root,'qa','route-input.json'),JSON.stringify([specification],null,2))
console.log(JSON.stringify({root,sha256,blocks:structure.getBlocks().length,entities:entities.length}))
