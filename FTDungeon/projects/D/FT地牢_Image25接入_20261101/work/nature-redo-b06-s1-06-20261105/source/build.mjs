import fs from 'node:fs/promises'
import path from 'node:path'
import {fileURLToPath} from 'node:url'
import {spawnSync} from 'node:child_process'
import {createHash} from 'node:crypto'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..')
const id = 'nature-redo-b06-s1-06'
const folder = path.join(root,id)
const reference = 'D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S1-mossy-forest/out/S1-06-corner.png'
const sha256 = bytes=>createHash('sha256').update(bytes).digest('hex')
const referenceHash = sha256(await fs.readFile(reference))
if (referenceHash !== '5536e9592a1210cc9057667aedaac6f94832bfecb02d2bdc60133043ed64c468') throw new Error('Reference changed')
await fs.mkdir(folder,{recursive:true})
const skill = 'C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
const execution = spawnSync(process.execPath,[path.join(skill,'scripts/run-build.mjs'),path.join(root,'source/recipe.mjs'),'--out-dir',folder,'--name','room','--data-version','3465'],{cwd:skill,encoding:'utf8',windowsHide:true})
await fs.writeFile(path.join(folder,'build.log'),execution.stdout + execution.stderr)
if (execution.status !== 0) throw new Error(execution.stderr)
const route = new Map()
const strip = (minX,minZ,maxX,maxZ)=>{for(let across=minX;across<=maxX;across++)for(let forward=minZ;forward<=maxZ;forward++)route.set(`${across},${forward}`,[across,2,forward])}
strip(0,17,21,21)
strip(17,0,21,21)
const fingerprint = sha256(await fs.readFile(path.join(folder,'room.nbt')))
const metadata = {id,number:'06',name:'盘根厚墙转角',size:[25,12,25],entrance:[0,2,19],interior:[19,2,19],facing:'west',feet_y:2,ports:[{side:'west',feet:[0,2,19],width:5,clearHeight:3},{side:'north',feet:[19,2,0],width:5,clearHeight:3}],route:[...route.values()],roof_exceptions:[],sha256:fingerprint,reference,reference_sha256:referenceHash,rotation_pivot:[0,0,0],minecraft:'1.20.1',status:'S1_06_PENDING_INDEPENDENT_REVIEW',solid_core:{from:[0,2,0],to:[14,10,14],purpose:'Opaque inner-corner masonry; fully reserved occupied volume, not an extra room or carve-out.'},deviations:['25x12x25 integer interpretation; world orientation west-to-north is an explicit convention.','Opaque solid inner-corner core and complete 625-column roof; no hidden room or diagonal shortcut.','Potted mushrooms on solid plinths replace naturally scattered mushrooms; full blocks approximate sloped roots.','Source-water side pool contained; no exterior forest, light shafts, particles, containers, loot or mechanisms.'],connections:'Rotate full occupied 25x12x25 bounds including solid core and both port feet around [0,0,0]; join opposite equal-height 5-wide dry supported ports with 3-high clearance; refuse overlaps.'}
await fs.writeFile(path.join(folder,'room.json'),JSON.stringify(metadata,null,2)+'\n')
await fs.writeFile(path.join(root,'group.json'),JSON.stringify({id:'nature-redo-b06',group_fingerprint:referenceHash,mode:'ONE_IMAGE_ONLY',rooms:[{id,name:metadata.name,size:metadata.size,sha256:fingerprint,reference}],next_image:'S1-07',next_image_authorized_to_start:false,paid_image_requests:0,previous_accepted_sha256:'c5d7a612b19b3eea25c33bcc3cafacff820cdaf7242cc6abb74fe78471e6c38d'},null,2)+'\n')
console.log(execution.stdout)
