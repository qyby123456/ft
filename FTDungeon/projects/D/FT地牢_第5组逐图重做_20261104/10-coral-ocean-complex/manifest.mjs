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
    if(relative==='qa/SHA256.json'||/^worldtest\/native-test\/(?:build|\.gradle)\//.test(relative))continue
    if(entry.isDirectory())walk(file)
    else if(entry.isFile())files.push({path:relative,bytes:fs.statSync(file).size,sha256:hash(file)})
  }
}
walk(root)
files.sort((left,right)=>left.path.localeCompare(right.path))
const candidate='1cb69d8ec260dd3a97da75b2b3a6db3f11e140da351e18bcec10c1c35dd7f92b'
for(const file of ['nbt/s5-10-coral-ocean-complex.nbt','worldtest/frozen.nbt','datapack/data/ft_s5_redo/structures/s5_10_coral_ocean_complex_full.nbt'])if(hash(path.join(root,file))!==candidate)throw Error('Candidate copy drift')
const preview=JSON.parse(fs.readFileSync(path.join(root,'qa/preview.json'),'utf8'))
if(preview.status!=='PASS_RENDER_ONLY'||preview.views.length!==11||preview.views.some(view=>view.errors.length||view.port===5721&&view.served_sha256!==candidate))throw Error('Preview evidence incomplete')
fs.writeFileSync(path.join(root,'qa/SHA256.json'),JSON.stringify({nbt_sha256:candidate,status:'FROZEN_FOR_INDEPENDENT_REVIEW_NOT_ACCEPTANCE',excluded:['qa/SHA256.json (self)','worldtest/native-test/build/**','worldtest/native-test/.gradle/**'],files},null,2))
for(const entry of files)if(hash(path.join(root,entry.path))!==entry.sha256)throw Error('Manifest readback drift '+entry.path)
console.log(`PASS ${files.length} file hashes re-read; candidate/frozen/datapack identical;11verified preview requests`)
