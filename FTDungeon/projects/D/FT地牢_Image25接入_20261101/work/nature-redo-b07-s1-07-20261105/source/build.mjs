import fs from 'node:fs/promises'
import path from 'node:path'
import {fileURLToPath} from 'node:url'
import {spawnSync} from 'node:child_process'
import {createHash} from 'node:crypto'

const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..')
const id='nature-redo-b07-s1-07'
const folder=path.join(root,id)
const reference='D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S1-mossy-forest/out/S1-07-entrance.png'
const sha256=bytes=>createHash('sha256').update(bytes).digest('hex')
const referenceHash=sha256(await fs.readFile(reference))
if(referenceHash!=='00a437ef2e9271115bc91aa7d6e9cb4e8648065a7da540b8aa3ed99d8828202b')throw new Error('Reference changed')
await fs.mkdir(folder,{recursive:true})
const skill='C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
const result=spawnSync(process.execPath,[path.join(skill,'scripts/run-build.mjs'),path.join(root,'source/recipe.mjs'),'--out-dir',folder,'--name','room','--data-version','3465'],{cwd:skill,encoding:'utf8',windowsHide:true})
await fs.writeFile(path.join(folder,'build.log'),result.stdout+result.stderr)
if(result.status!==0)throw new Error(result.stderr)
const route=[]
for(let forward=28;forward>=0;forward--) {
  const feet=forward<=4?4:forward===5?3.5:forward<=23?3:forward===24?2.5:2
  for(const across of (forward>=22?[12,13]:[11,12,13]))route.push([across,feet,forward])
}
const doors=[]
for(const across of [12,13])for(const vertical of [3,4])doors.push([across,vertical,23])
const fingerprint=sha256(await fs.readFile(path.join(folder,'room.nbt')))
const metadata={id,number:'07',name:'双门阶庭入口',size:[27,17,29],entrance:[12,2,28],interior:[12,3,15],facing:'south',feet_y:2,ports:[{side:'south',feet:[12,2,28],width:2,clearHeight:3},{side:'north',feet:[12,4,0],width:3,clearHeight:3}],route,roof_exceptions:[],sha256:fingerprint,reference,reference_sha256:referenceHash,rotation_pivot:[0,0,0],minecraft:'1.20.1',status:'S1_07_PENDING_INDEPENDENT_REVIEW',doors:{positions:doors,default_open:false,default_powered:false,clear_width:2,clear_height:2,analysis:'Only set open=true on these four door halves in isolated test memory; validate, restore original state, then capture closed source. No open candidate NBT.'},steps:[{z:24,feet_y:2.5,rise:0.5},{z:23,feet_y:3,rise:0.5},{z:5,feet_y:3.5,rise:0.5},{z:4,feet_y:4,rise:0.5}],deviations:['27x17x29 integer interpretation; full protective roof and near outer shell added.','Two vanilla wooden doors replace wider illustrated gate; 2x2 door clearance explicitly declared, closed source blocks access.','Solid decorative wood crates replace loot containers; no inventories. Birch/moss wall mosaic approximates map, lantern pedestal approximates flame.','Two half-block rises at front and rear; total feet elevation 2 to 4. No full-block jump route.','No exterior forest, shader rays, particles, encounters or mechanisms.'],connections:'Transform full bounds/ports/steps with pivot [0,0,0]; south feetY2 versus north feetY4, do not silently level them. Doors closed by default; only analytical open test is not a candidate.'}
await fs.writeFile(path.join(folder,'room.json'),JSON.stringify(metadata,null,2)+'\n')
await fs.writeFile(path.join(root,'group.json'),JSON.stringify({id:'nature-redo-b07',group_fingerprint:referenceHash,mode:'ONE_IMAGE_ONLY',rooms:[{id,name:metadata.name,size:metadata.size,sha256:fingerprint,reference}],next_image:'S1-08',next_image_authorized_to_start:false,paid_image_requests:0,previous_accepted_sha256:'07bc6264e9e02738a2c55e5738abcd09122d2b118dba307fe6c5bf2bda4326e7'},null,2)+'\n')
console.log(result.stdout)
