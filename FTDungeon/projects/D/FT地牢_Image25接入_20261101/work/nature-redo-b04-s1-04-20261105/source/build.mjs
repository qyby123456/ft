import fs from 'node:fs/promises'
import path from 'node:path'
import {fileURLToPath} from 'node:url'
import {spawnSync} from 'node:child_process'
import {createHash} from 'node:crypto'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const id = 'nature-redo-b04-s1-04'
const folder = path.join(root,id)
const reference = 'D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S1-mossy-forest/out/S1-04-great-hall.png'
const sha256 = bytes => createHash('sha256').update(bytes).digest('hex')
const referenceHash = sha256(await fs.readFile(reference))
if (referenceHash !== '359d4f72296c68591d5249bbabd49c0a38c85f8334bd403e3d7d78873dc34c4d') throw new Error('Reference changed')
await fs.mkdir(folder,{recursive:true})
const skill = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
const execution = spawnSync(process.execPath,[path.join(skill,'scripts/run-build.mjs'),path.join(root,'source/recipe.mjs'),'--out-dir',folder,'--name','room','--data-version','3465'],{cwd:skill,encoding:'utf8',windowsHide:true})
await fs.writeFile(path.join(folder,'build.log'),execution.stdout + execution.stderr)
if (execution.status !== 0) throw new Error(execution.stderr)
const route = new Set()
const strip = (startX,startZ,endX,endZ) => {
  for (let across = startX; across <= endX; across++) for (let forward = startZ; forward <= endZ; forward++) route.add(`${across},${forward}`)
}
strip(13,0,17,34)
strip(0,19,30,21)
strip(11,4,19,32)
const fingerprint = sha256(await fs.readFile(path.join(folder,'room.nbt')))
const metadata = {id,number:'04',name:'盘根柱廊大殿',size:[31,18,35],entrance:[15,2,34],interior:[15,2,18],facing:'south',feet_y:2,ports:[{side:'south',feet:[15,2,34],width:5,clearHeight:3},{side:'north',feet:[15,2,0],width:5,clearHeight:3},{side:'west',feet:[0,2,20],width:3,clearHeight:3},{side:'east',feet:[30,2,20],width:3,clearHeight:3}],route:[...route].map(cell=>{const [across,forward]=cell.split(',').map(Number);return[across,2,forward]}),roof_exceptions:[],sha256:fingerprint,reference,reference_sha256:referenceHash,rotation_pivot:[0,0,0],minecraft:'1.20.1',status:'S1_04_PENDING_INDEPENDENT_REVIEW',deviations:['31x18x35 integer interpretation; not exact measured geometry.','Full roof/near walls enclose the image cutaway. Front steps replaced by level connector threshold.','Full-block stepped column bases, roots and arched head approximate sloped edges.','Two patterned vanilla wall banners approximate red/gold hangings; no containers or generated loot.','No exterior forest, shader sunbeams, particles, encounters or mechanisms.'],connections:'Transform ports and bounding box around [0,0,0]; join opposing equal-height ports with dry supported connector at least 3x3 clear; refuse occupied volume.'}
await fs.writeFile(path.join(folder,'room.json'),JSON.stringify(metadata,null,2)+'\n')
await fs.writeFile(path.join(root,'group.json'),JSON.stringify({id:'nature-redo-b04',group_fingerprint:referenceHash,mode:'ONE_IMAGE_ONLY',rooms:[{id,name:metadata.name,size:metadata.size,sha256:fingerprint,reference}],next_image:'S1-05',next_image_authorized_to_start:false,paid_image_requests:0,previous_accepted_sha256:'c135fdaeb27306b9a018dca393f00c21225d7dcb7595341b6a2f466f6deac2d3'},null,2)+'\n')
console.log(execution.stdout)
