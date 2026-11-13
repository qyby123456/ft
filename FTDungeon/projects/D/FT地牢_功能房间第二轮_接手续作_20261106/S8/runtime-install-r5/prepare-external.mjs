import fs from 'node:fs/promises'
import path from 'node:path'
import crypto from 'node:crypto'
import {fileURLToPath} from 'node:url'
const source=path.dirname(fileURLToPath(import.meta.url))
const allowed=path.resolve('C:/Users/YOUR_USERNAME/.codex/visualizations/2026/11/04/01a1a685-045b-76d2-aa2b-2a3f24520603/s8-maker-access-isolated')
const name=process.argv[2]??`fresh-${crypto.randomUUID()}`
if(!/^[a-zA-Z0-9][a-zA-Z0-9_-]{0,79}$/.test(name))throw Error('Use a new simple subdirectory name')
const out=path.resolve(allowed,name)
if(!out.startsWith(allowed+path.sep)||await fs.stat(out).catch(()=>null))throw Error('Fresh external directory required; never overwrite or create a D world')
const storage=JSON.parse(await fs.readFile(path.join(source,'STORAGE-MANIFEST.json'),'utf8'))
if(storage.status!=='MOVED_AND_ALL_FILE_HASHES_VERIFIED')throw Error('Storage transfer is incomplete')
const relocatedServer=path.join(storage.external_root,'runtime-install-r5/server')
for(const file of storage.files.filter(file=>file.path.startsWith(path.join(relocatedServer,'world/datapacks')))){
 const actual=crypto.createHash('sha256').update(await fs.readFile(file.path)).digest('hex')
 if(actual!==file.sha256)throw Error('Archived pack drift')
}
await fs.mkdir(out,{recursive:true})
await fs.cp(path.join(source,'native-test'),path.join(out,'native-test'),{recursive:true,filter:filename=>!/(?:^|[\\/])(?:build|\.gradle)(?:[\\/]|$)/.test(filename)})
const gradle=path.join(out,'native-test/build.gradle')
await fs.writeFile(gradle,(await fs.readFile(gradle,'utf8')).split('\n').filter(line=>!line.startsWith('layout.buildDirectory')).join('\n'))
await fs.cp(path.join(relocatedServer,'world/datapacks'),path.join(out,'server/world/datapacks'),{recursive:true})
await fs.copyFile(path.join(relocatedServer,'server.properties'),path.join(out,'server/server.properties'))
for(const filename of ['InstallTests.java','install-input.json','run.ps1'])await fs.copyFile(path.join(source,filename),path.join(out,filename))
await fs.writeFile(path.join(out,'PREPARATION.json'),JSON.stringify({source,storage_manifest:path.join(source,'STORAGE-MANIFEST.json'),new_world_root:path.join(out,'server/world'),build_output:path.join(out,'native-test/build'),production_writes:false,executed:false},null,2))
console.log('Prepared only; run the new C-directory run.ps1 to execute:',out)
