import fs from 'node:fs/promises'
import path from 'node:path'
import crypto from 'node:crypto'
import {spawnSync} from 'node:child_process'
const output=path.resolve('assembly-reproduction'),hash=bytes=>crypto.createHash('sha256').update(bytes).digest('hex')
await fs.mkdir(`${output}/assembly`,{recursive:true})
for(let number=11;number<=20;number++){
 const id=`S8-${number}`;await fs.mkdir(`${output}/${id}`,{recursive:true})
 for(const file of await fs.readdir(id))if(file.endsWith('.nbt')||['inventory.json','SELF-QA.json'].includes(file))await fs.copyFile(`${id}/${file}`,`${output}/${id}/${file}`)
}
await fs.copyFile('assembly/build.mjs',`${output}/assembly/build.mjs`)
const result=spawnSync(process.execPath,[`${output}/assembly/build.mjs`],{encoding:'utf8',windowsHide:true})
await fs.writeFile(`${output}/generation.log`,result.stdout+result.stderr)
if(result.status!==0)throw Error('Assembly reproduction failed')
const before=hash(await fs.readFile('assembly/s8-functional-combined.nbt')),after=hash(await fs.readFile(`${output}/assembly/s8-functional-combined.nbt`))
if(before!==after)throw Error('Assembly reproduction hash mismatch')
await fs.writeFile('assembly-reproduction.json',JSON.stringify({source_sha256:before,reproduced_sha256:after,same_sha256:true,builder:path.resolve('assembly/build.mjs'),output,scope:'Producer fresh-output construction from ten current source NBTs through assembly builder, not merely re-reading merged NBT. Independent rerun still required.'},null,2))
console.log('Assembly fresh-output reproduced',before)
