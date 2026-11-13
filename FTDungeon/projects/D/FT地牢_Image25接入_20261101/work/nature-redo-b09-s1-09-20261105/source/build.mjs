import fs from 'node:fs/promises'
import path from 'node:path'
import {fileURLToPath} from 'node:url'
import {spawnSync} from 'node:child_process'
import {createHash} from 'node:crypto'
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..')
const id='nature-redo-b09-s1-09'
const folder=path.join(root,id)
const reference='D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S1-mossy-forest/out/S1-09-treasure.png'
const hash=bytes=>createHash('sha256').update(bytes).digest('hex')
const referenceHash=hash(await fs.readFile(reference))
if(referenceHash!=='a19244af10654092098d143d2bcb5ce557a15023b770d3af9e005b8e449afc1a')throw new Error('Reference changed')
await fs.mkdir(folder,{recursive:true})
const skill='C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
const result=spawnSync(process.execPath,[path.join(skill,'scripts/run-build.mjs'),path.join(root,'source/recipe.mjs'),'--out-dir',folder,'--name','room','--data-version','3465'],{cwd:skill,encoding:'utf8',windowsHide:true})
await fs.writeFile(path.join(folder,'build.log'),result.stdout+result.stderr)
if(result.status!==0)throw new Error(result.stderr)
const lower=new Map()
const strip=(minX,minZ,maxX,maxZ)=>{for(let across=minX;across<=maxX;across++)for(let forward=minZ;forward<=maxZ;forward++)lower.set(`${across},${forward}`,[across,2,forward])}
strip(13,18,15,28)
strip(3,18,25,20)
strip(3,7,5,18)
strip(23,7,25,18)
strip(0,7,5,9)
strip(23,7,28,9)
const upper=[]
for(let across=12;across<=16;across++)for(let forward=8;forward<=11;forward++)if(!(across===14&&forward===10))upper.push([across,5,forward])
const steps=[]
for(let forward=17;forward>=12;forward--)for(let across=12;across<=16;across++)steps.push([across,2+(18-forward)*.5,forward])
const fingerprint=hash(await fs.readFile(path.join(folder,'room.nbt')))
const metadata={id,number:'09',name:'双守卫高台宝藏室',size:[29,17,29],entrance:[14,2,28],interior:[14,2,20],facing:'south',feet_y:2,ports:[{side:'south',feet:[14,2,28],width:3,clearHeight:3},{side:'west',feet:[0,2,8],width:3,clearHeight:3},{side:'east',feet:[28,2,8],width:3,clearHeight:3}],route:[...lower.values()],body_route:[...lower.values(),...steps,...upper],upper_targets:upper,stair_targets:steps,roof_exceptions:[],fluid_exposure_whitelist:[],containers:[{position:[14,5,10],facing:'south',type:'single',inventory:'EMPTY',LootTable:null,front_feet:[14,5,11],lid_clearance_box:[[13,6,9],[15,8,11]]}],sha256:fingerprint,reference,reference_sha256:referenceHash,rotation_pivot:[0,0,0],minecraft:'1.20.1',status:'S1_09_PENDING_INDEPENDENT_REVIEW',deviations:['29x17x29 vanilla interpretation; real empty single chest on three-block high plinth, no loot assigned.','Six real half-step rises from feetY2 to5; five-wide stair and chest-front body route.','Paired guardians are solid stone statues with carved-pumpkin faces; no mobs or armor stands.','Two red wall banners plus terracotta hanging panels and gold trim; source image loose pots become potted mushrooms.','Complete closed roof at Y16 with canopy and roots beneath. Cutaway roof removal is display-only, no skylight or upper gallery.'],connections:'Rotate full occupied bounds and 3 supported dry 3-wide/3-high lower ports around [0,0,0]. No external upper port.'}
await fs.writeFile(path.join(folder,'room.json'),JSON.stringify(metadata,null,2)+'\n')
await fs.writeFile(path.join(root,'group.json'),JSON.stringify({id:'nature-redo-b09',group_fingerprint:referenceHash,mode:'ONE_IMAGE_ONLY',rooms:[{id,name:metadata.name,size:metadata.size,sha256:fingerprint,reference}],next_image:'S1-10',next_image_authorized_to_start:false,paid_image_requests:0,previous_accepted_sha256:'bfd3cbcda1e46ae8bf941c15a85b655b006b322d562e692fabf30f8958700463'},null,2)+'\n')
console.log(result.stdout)
