import fs from 'node:fs/promises'
import path from 'node:path'
import {createRequire} from 'node:module'
import {pathToFileURL,fileURLToPath} from 'node:url'
import {inflateSync} from 'node:zlib'
const require=createRequire('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/package.json')
const {NbtFile}=await import(pathToFileURL(require.resolve('@mattzh72/lodestone')).href)
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..')
const invocation=JSON.parse(await fs.readFile(path.join(root,'qa/rejected-first-run/forge-invocation.json'),'utf8'))
const entityFolder=path.join(invocation.fresh_world,'world/entities')
const entities=[]
for(const filename of await fs.readdir(entityFolder)){
  const buffer=await fs.readFile(path.join(entityFolder,filename))
  if(buffer.length<8192)continue
  for(let entry=0;entry<1024;entry++){
    const sector=buffer.readUInt32BE(entry*4)>>>8
    if(!sector)continue
    const offset=sector*4096
    const length=buffer.readUInt32BE(offset)
    const compression=buffer[offset+4]
    const payload=buffer.subarray(offset+5,offset+4+length)
    const raw=compression===2?inflateSync(payload):payload
    const nbt=NbtFile.read(raw,{compression:'none'}).root
    for(const entity of nbt.getList('Entities').getItems()){
      if(entity.getString('id')==='minecraft:item')entities.push(entity.toJson())
    }
  }
}
await fs.writeFile(path.join(root,'qa/rejected-first-run/items-audit.json'),JSON.stringify(entities,null,2)+'\n')
const summary={}
for(const entity of entities){
  const item=entity.Item.value
  summary[item.id.value]=(summary[item.id.value]??0)+item.Count.value
}
console.log(JSON.stringify(summary,null,2))
