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
const candidate='b5c2cb4c96f3345ab93f4e609fd5aabc2b5a7061f58b6fbfad742eabe7e8fdf5'
for(const file of ['nbt/s5-07-coral-gate-vestibule.nbt','worldtest/frozen.nbt','datapack/data/ft_s5_redo/structures/s5_07_coral_gate_vestibule.nbt'])if(hash(path.join(root,file))!==candidate)throw Error('Candidate copy mismatch '+file)
fs.writeFileSync(path.join(root,'qa/SHA256.json'),JSON.stringify({nbt_sha256:candidate,status:'FROZEN_FOR_INDEPENDENT_REVIEW_NOT_ACCEPTANCE',excluded:['qa/SHA256.json (self)','worldtest/native-test/build/**','worldtest/native-test/.gradle/**'],files},null,2))
for(const entry of files)if(hash(path.join(root,entry.path))!==entry.sha256)throw Error('Manifest readback drift '+entry.path)
console.log(`PASS ${files.length} file hashes re-read; candidate/frozen/datapack identical`)



