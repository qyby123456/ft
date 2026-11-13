import fs from 'node:fs/promises'
import path from 'node:path'
import crypto from 'node:crypto'
import {pathToFileURL} from 'node:url'
import {spawnSync} from 'node:child_process'
const root=process.cwd(),skill='C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
const {Structure,NbtFile}=await import(pathToFileURL(`${skill}/node_modules/@mattzh72/lodestone/lib/index.js`))
const read=async file=>JSON.parse(await fs.readFile(file,'utf8')),hash=bytes=>crypto.createHash('sha256').update(bytes).digest('hex')
const sourceRoot='D:/ft工程文件/FT地牢_功能房间第二轮_20261106/S8',reports=[]
for(let number=11;number<=20;number++){
 const id=`S8-${number}`,directory=path.join(root,id),inventory=await read(`${directory}/inventory.json`),original=await read(`${sourceRoot}/${id}/inventory.json`)
 if(JSON.stringify(inventory.metadata.targets)!==JSON.stringify(original.metadata.targets))throw Error(`Target drift ${id}`)
 if(JSON.stringify(inventory.metadata.ports)!==JSON.stringify(original.metadata.ports))throw Error(`Port drift ${id}`)
 const filename=(await fs.readdir(directory)).find(file=>file.endsWith('.nbt')),bytes=await fs.readFile(path.join(directory,filename))
 const current=Structure.fromNbt(NbtFile.read(bytes).root),old=Structure.fromNbt(NbtFile.read(await fs.readFile(`${sourceRoot}/${id}/${filename}`)).root)
 const blocks=new Map(current.getBlocks().map(block=>[block.pos.join(','),block]))
 const payloads=old.getBlocks().filter(block=>block.nbt)
 for(const block of payloads){
  const actual=blocks.get(block.pos.join(','))
  if(!actual||actual.state.toString()!==block.state.toString()||actual.nbt?.toString()!==block.nbt.toString())throw Error(`Original block entity changed ${id} ${block.pos}`)
 }
 const output=path.join(root,'reproduction',id)
 const result=spawnSync(process.execPath,[`${skill}/scripts/run-build.mjs`,`${directory}/recipe.mjs`,'--out-dir',output],{cwd:skill,encoding:'utf8',windowsHide:true})
 if(result.status!==0)throw Error(result.stderr)
 const rebuilt=await fs.readFile(path.join(output,filename))
 if(hash(rebuilt)!==hash(bytes))throw Error(`Recipe drift ${id}`)
 const native=(await read(`${directory}/qa/independent-native-frozen.json`))[0],routes=(await read(`${directory}/qa/independent-routes.json`))[0],support=(await read(`${directory}/qa/independent-all-support.json`))[0]
 for(const report of [native,routes,support])if(report.sha256!==hash(bytes))throw Error(`Stale QA ${id}`)
 if(support.failures.length||support.unsupported.length)throw Error(`Support failed ${id}`)
 if(routes.targets.some(target=>!target.reachable||!target.standing_clear))throw Error(`Routes failed ${id}`)
 reports.push({id,sha256:hash(bytes),recipe_reproduced_same_sha256:true,original_targets_exact:true,original_interfaces_exact:true,original_block_entity_states_and_payloads_preserved:payloads.length,native_nonair_support:support.nonair_checked,route_targets:routes.targets.length})
 console.log('Reproduction and preservation',id)
}
await fs.writeFile('source-evidence.json',JSON.stringify({scope:'Producer self-check, not independent acceptance',reports},null,2))
