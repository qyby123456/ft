import fs from 'node:fs/promises'
import path from 'node:path'
import {fileURLToPath} from 'node:url'
import {spawnSync} from 'node:child_process'
import {createHash} from 'node:crypto'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const id = 'nature-redo-b05-s1-05'
const folder = path.join(root,id)
const reference = 'D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S1-mossy-forest/out/S1-05-corridor.png'
const sha256 = bytes => createHash('sha256').update(bytes).digest('hex')
const referenceHash = sha256(await fs.readFile(reference))
if (referenceHash !== '7a41bfca86ec529ac38a01ab59a2d3fd67ff60a6d2bc6baf746b3a285486dffe') throw new Error('Reference changed')
await fs.mkdir(folder,{recursive:true})
const skill = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
const execution = spawnSync(process.execPath,[path.join(skill,'scripts/run-build.mjs'),path.join(root,'source/recipe.mjs'),'--out-dir',folder,'--name','room','--data-version','3465'],{cwd:skill,encoding:'utf8',windowsHide:true})
await fs.writeFile(path.join(folder,'build.log'),execution.stdout + execution.stderr)
if (execution.status !== 0) throw new Error(execution.stderr)
const route = []
for (let across = 5; across <= 9; across++) for (let forward = 0; forward < 37; forward++) route.push([across,2,forward])
const fingerprint = sha256(await fs.readFile(path.join(folder,'room.nbt')))
const metadata = {id,number:'05',name:'盘根低顶直廊',size:[15,12,37],entrance:[7,2,36],interior:[7,2,18],facing:'south',feet_y:2,ports:[{side:'south',feet:[7,2,36],width:5,clearHeight:3},{side:'north',feet:[7,2,0],width:5,clearHeight:3}],route,roof_exceptions:[],sha256:fingerprint,reference,reference_sha256:referenceHash,rotation_pivot:[0,0,0],minecraft:'1.20.1',status:'S1_05_PENDING_INDEPENDENT_REVIEW',deviations:['15x12x37 integer interpretation, not exact image measurement.','Continuous closed roof; light shafts and outside forest omitted.','Potted mushrooms on solid mossy plinths replace naturally scattered mushrooms.','Stepped full-block roots replace sloping root outlines; source-water side pools are contained.','No containers, encounters, mechanisms, loot or branches.'],connections:'Transform ports and full bounding box around [0,0,0]; join opposing equal-height 5-wide ports with dry supported connector and at least 3 clear height; refuse occupied volume.'}
await fs.writeFile(path.join(folder,'room.json'),JSON.stringify(metadata,null,2)+'\n')
await fs.writeFile(path.join(root,'group.json'),JSON.stringify({id:'nature-redo-b05',group_fingerprint:referenceHash,mode:'ONE_IMAGE_ONLY',rooms:[{id,name:metadata.name,size:metadata.size,sha256:fingerprint,reference}],next_image:'S1-06',next_image_authorized_to_start:false,paid_image_requests:0,previous_accepted_sha256:'a7b46fb3d1e71bb41f743acdc3e72412c58b34a98d2e771805803697d5340265'},null,2)+'\n')
console.log(execution.stdout)
