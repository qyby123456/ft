import fs from 'node:fs/promises'
import path from 'node:path'
import {fileURLToPath} from 'node:url'
import {spawnSync} from 'node:child_process'
import {createHash} from 'node:crypto'

const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..')
const id='nature-redo-b08-s1-08'
const folder=path.join(root,id)
const reference='D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S1-mossy-forest/out/S1-08-courtyard.png'
const hash=bytes=>createHash('sha256').update(bytes).digest('hex')
const referenceHash=hash(await fs.readFile(reference))
if(referenceHash!=='0de45b63b23f7fd704eb68cae1db65ba5b1856e370ab91852d8a0c5fc63a8489')throw new Error('Reference changed')
await fs.mkdir(folder,{recursive:true})
const skill='C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
const result=spawnSync(process.execPath,[path.join(skill,'scripts/run-build.mjs'),path.join(root,'source/recipe.mjs'),'--out-dir',folder,'--name','room','--data-version','3465'],{cwd:skill,encoding:'utf8',windowsHide:true})
await fs.writeFile(path.join(folder,'build.log'),result.stdout+result.stderr)
if(result.status!==0)throw new Error(result.stderr)
const lower=new Map()
const strip=(minX,minZ,maxX,maxZ)=>{for(let across=minX;across<=maxX;across++)for(let forward=minZ;forward<=maxZ;forward++)lower.set(`${across},${forward}`,[across,2,forward])}
strip(9,0,11,10)
strip(8,8,34,10)
strip(0,25,10,27)
strip(8,10,10,31)
strip(8,29,32,31)
strip(30,10,32,31)
strip(16,31,18,34)
strip(3,25,10,27)
strip(8,19,18,21)
const upper=new Map()
const platform=(minX,minZ,maxX,maxZ)=>{for(let across=minX;across<=maxX;across++)for(let forward=minZ;forward<=maxZ;forward++)upper.set(`${across},${forward}`,[across,10,forward])}
platform(3,3,31,5)
platform(3,5,5,8)
platform(29,5,31,31)
platform(3,29,31,31)
const steps=[]
for(let forward=24;forward>=9;forward--)for(let across=3;across<=5;across++)steps.push([across,2+(25-forward)*.5,forward])
const roof=[]
for(let across=8;across<=26;across++)for(let forward=8;forward<=26;forward++)roof.push([across,forward])
const fingerprint=hash(await fs.readFile(path.join(folder,'room.nbt')))
const metadata={id,number:'08',name:'古树天窗回廊庭院',size:[35,22,35],entrance:[10,2,0],interior:[10,2,20],facing:'north',feet_y:2,ports:[{side:'north',feet:[10,2,0],width:3,clearHeight:3},{side:'west',feet:[0,2,26],width:3,clearHeight:3},{side:'east',feet:[34,2,9],width:3,clearHeight:3},{side:'south',feet:[17,2,34],width:3,clearHeight:3}],route:[...lower.values()],body_route:[...lower.values(),...steps,...upper.values()],upper_targets:[...upper.values()],stair_targets:steps,roof_exceptions:roof,sky_sample:[22,2,15],fluid_exposure_whitelist:[],containers:[],sha256:fingerprint,reference,reference_sha256:referenceHash,rotation_pivot:[0,0,0],minecraft:'1.20.1',status:'S1_08_PENDING_INDEPENDENT_REVIEW',upper_boundary:'North-east-south U-shaped upper gallery only, feetY10. West is a real 16-half-step stair, no continuous west upper gallery or external upper port.',deviations:['35x22x35 integer interpretation; true central 19x19 skylight, not a preview-only opening.','Explicit added western staircase: x3..5,z24..9,16 half steps rising feet2 to feet10; U gallery rather than unconnected decorative upper floor.','Illustrated short waterfall represented by sealed glazed source-water column adjoining pool; no open flowing waterfall.','Full-block stepped roots/stone geometry; loose mushrooms omitted. No exterior forest, shader rays, containers, entities or encounters.'],connections:'Rotate full 35x22x35 occupied bounds and 4 lower port feet around [0,0,0]; join opposite equal-height dry supported 3-wide/3-high ports. Upper gallery has no external connection.'}
await fs.writeFile(path.join(folder,'room.json'),JSON.stringify(metadata,null,2)+'\n')
await fs.writeFile(path.join(root,'group.json'),JSON.stringify({id:'nature-redo-b08',group_fingerprint:referenceHash,mode:'ONE_IMAGE_ONLY',rooms:[{id,name:metadata.name,size:metadata.size,sha256:fingerprint,reference}],next_image:'S1-09',next_image_authorized_to_start:false,paid_image_requests:0,previous_accepted_sha256:'8f693bd558354d9444e654300197c0f74b1b3870af9069d51897bb510de63133'},null,2)+'\n')
console.log(result.stdout)
