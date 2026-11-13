import fs from 'node:fs'
import path from 'node:path'
import crypto from 'node:crypto'
import {fileURLToPath} from 'node:url'
const root=path.dirname(fileURLToPath(import.meta.url))
const hash=file=>crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex')
const files=[]
function walk(folder){
  for(const entry of fs.readdirSync(folder,{withFileTypes:true})){
    const file=path.join(folder,entry.name)
    const relative=path.relative(root,file).replaceAll('\\','/')
    if(relative==='qa/SHA256.json'||relative.startsWith('worldtest/native-test/build/')||relative.startsWith('worldtest/native-test/.gradle/'))continue
    if(entry.isDirectory())walk(file)
    else if(entry.isFile())files.push({path:relative,bytes:fs.statSync(file).size,sha256:hash(file)})
  }
}
walk(root)
files.sort((left,right)=>left.path.localeCompare(right.path))
const candidate='f85b42cc6d78b51d656c5829b46803cee6844903461a38b93fe23f290977e5cb'
for(const file of ['nbt/s5-05-coral-ribbed-corridor.nbt','worldtest/frozen.nbt','datapack/data/ft_s5_redo/structures/s5_05_coral_ribbed_corridor.nbt'])if(hash(path.join(root,file))!==candidate)throw Error('Candidate copy mismatch '+file)
fs.writeFileSync(path.join(root,'qa/SHA256.json'),JSON.stringify({nbt_sha256:candidate,status:'FROZEN_FOR_INDEPENDENT_REVIEW_NOT_ACCEPTANCE',excluded:['qa/SHA256.json (self)','worldtest/native-test/build/**','worldtest/native-test/.gradle/**'],files},null,2))
for(const entry of files)if(hash(path.join(root,entry.path))!==entry.sha256)throw Error('Manifest readback drift '+entry.path)
console.log(`PASS ${files.length} file hashes re-read; candidate/frozen/datapack identical`)

