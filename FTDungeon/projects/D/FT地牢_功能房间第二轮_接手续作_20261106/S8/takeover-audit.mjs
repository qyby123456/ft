import fs from 'node:fs/promises'
import path from 'node:path'
import crypto from 'node:crypto'
import {fileURLToPath} from 'node:url'
const root=path.dirname(fileURLToPath(import.meta.url)),frozen='D:/ft工程文件/FT地牢_第二轮独立验收_20261106/S8-submission-20261106-182401-049964'
const hash=data=>crypto.createHash('sha256').update(data).digest('hex')
const snapshot=JSON.parse(await fs.readFile(path.join(frozen,'submission-snapshot.json'),'utf8')),files=[]
for(const entry of snapshot.files) {
  const actual=hash(await fs.readFile(entry.source))
  if(actual!==entry.sha256)throw Error(`Old source drift ${entry.source}`)
  files.push({source:entry.source,frozen_sha256:entry.sha256,actual_sha256:actual})
}
const references=JSON.parse(await fs.readFile(path.join(root,'references.json'),'utf8'));references.output_directory=root
for(const reference of references.references)if(hash(await fs.readFile(reference.image_path))!==reference.sha256)throw Error('Reference drift')
await fs.writeFile(path.join(root,'references.json'),JSON.stringify(references,null,2))
await fs.writeFile(path.join(root,'inheritance.json'),JSON.stringify({frozen,frozen_manifest_sha256:snapshot.manifest_sha256,actual_files_verified:files.length,files,inherited_acceptance:'REQUEST_CHANGES',old_directory_read_only:true},null,2))
await fs.writeFile(path.join(root,'progress.json'),JSON.stringify({style:'S8',current:'S8-11 visual geometry revision',status:'in_progress',independent_acceptance:'REQUEST_CHANGES',completed:[],inherited_rooms:Array.from({length:10},(_,index)=>({id:`S8-${11+index}`,status:'inherited candidate; visual corrections pending'})),world_writes:false,pending:['ten room visual revisions and preview inspection','rebuild assembly and <=48 partitions','fresh native support and routes','full-width interfaces','dynamic interactions NOT VERIFIED']},null,2))
console.log('Frozen inheritance verified',files.length)
