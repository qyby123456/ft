import fs from 'node:fs/promises'
import path from 'node:path'
import crypto from 'node:crypto'
import {fileURLToPath} from 'node:url'
const scriptRoot=path.dirname(fileURLToPath(import.meta.url))
const out=process.argv[2]?path.resolve(scriptRoot,'..',process.argv[2]):scriptRoot
const root=path.dirname(out)
if(await fs.stat(`${out}/server`).catch(()=>null))throw Error('Preserve existing attempt; use a fresh directory')
const sha=data=>crypto.createHash('sha256').update(data).digest('hex')
const files=[]
async function copyTree(source,dest){await fs.mkdir(dest,{recursive:true});for(const entry of await fs.readdir(source,{withFileTypes:true})){const from=path.join(source,entry.name),to=path.join(dest,entry.name);if(entry.isDirectory())await copyTree(from,to);else{const data=await fs.readFile(from);await fs.writeFile(to,data);files.push({path:path.relative(out,to).replaceAll('\\','/'),sha256:sha(data),bytes:data.length})}}}
await fs.cp(`${root}/runtime-r4/native-test`,`${out}/native-test`,{recursive:true,filter:source=>!/(?:^|[\\/])(?:build|\.gradle)(?:[\\/]|$)/.test(source)})
const java=`${out}/native-test/src/main/java/net/farmingtales/s8qa`
for(const name of await fs.readdir(java))await fs.unlink(`${java}/${name}`)
await fs.copyFile(`${scriptRoot}/InstallTests.java`,`${java}/InstallTests.java`)
if(out!==scriptRoot){await fs.copyFile(`${scriptRoot}/InstallTests.java`,`${out}/InstallTests.java`);await fs.copyFile(fileURLToPath(import.meta.url),`${out}/prepare.mjs`)}
await fs.appendFile(`${out}/native-test/build.gradle`,"\nlayout.buildDirectory = file('../../runtime-install-r1/native-test/build')\n")
await fs.mkdir(`${out}/server`,{recursive:true})
await fs.writeFile(`${out}/server/server.properties`,'level-type=minecraft:flat\ngenerate-structures=false\nmax-tick-time=-1\n')
await fs.copyFile(`${root}/runtime-r4/run.ps1`,`${out}/run.ps1`)
const pack=`${out}/server/world/datapacks/s8_frozen_install`
await copyTree(`${root}/deployment-plan`,pack)
const access=`${out}/server/world/datapacks/s8_admin_access`
await fs.mkdir(`${access}/data/s8_admin/functions`,{recursive:true})
await fs.writeFile(`${access}/pack.mcmeta`,JSON.stringify({pack:{pack_format:15,description:'S8 administrator hosted entry and return for isolated installation'}}))
await fs.writeFile(`${access}/data/s8_admin/functions/enter.mcfunction`,'execute in minecraft:overworld run tp @s 1030.5 132 1079.5 0 0\n')
await fs.writeFile(`${access}/data/s8_admin/functions/return.mcfunction`,'execute in minecraft:overworld run tp @s 1000.5 129 1000.5 0 0\n')
await fs.writeFile(`${access}/data/s8_admin/functions/install.mcfunction`,'execute in minecraft:overworld positioned 1024 128 1024 run function s8_takeover:clear\nexecute in minecraft:overworld positioned 1024 128 1024 run function s8_takeover:place_parts\n')
const exact=JSON.parse(await fs.readFile(`${root}/assembly/qa/assembly-exactness.json`,'utf8'))
const candidate=JSON.parse(await fs.readFile(`${root}/ready-for-review.json`,'utf8')).assembly.source
await fs.writeFile(`${out}/install-input.json`,JSON.stringify({candidate:candidate.path,sha256:candidate.sha256,origin:[1024,128,1024],size:[240,29,145],external_hub:[1000.5,129,1000.5],entry:[1030.5,132,1079.5],frozen_pack_files:files,parts:exact.parts,scope:'Fresh isolated world, unchanged frozen clearing/20-part pack; administrator FakePlayer command source, no network client or human claim'},null,2))
console.log('Prepared fresh world datapacks',out,files.length)
