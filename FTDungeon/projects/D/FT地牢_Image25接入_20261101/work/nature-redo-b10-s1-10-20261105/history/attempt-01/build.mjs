import fs from 'node:fs/promises'
import path from 'node:path'
import {fileURLToPath} from 'node:url'
import {spawnSync} from 'node:child_process'
import {createHash} from 'node:crypto'
import {size,regions,openings,inRegion} from './layout.mjs'
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..')
const id='nature-redo-b10-s1-10'
const folder=path.join(root,id)
const reference='D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S1-mossy-forest/out/S1-10-overview.png'
const hash=bytes=>createHash('sha256').update(bytes).digest('hex')
const referenceHash=hash(await fs.readFile(reference))
if(referenceHash!=='052422469ae57f99ab5ce0a3b11f877d725d0079b9b89be750f24620c6ca7833')throw new Error('Reference changed')
await fs.mkdir(folder,{recursive:true})
const skill='C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill'
const result=spawnSync(process.execPath,[path.join(skill,'scripts/run-build.mjs'),path.join(root,'source/recipe.mjs'),'--out-dir',folder,'--name','room','--data-version','3465'],{cwd:skill,encoding:'utf8',windowsHide:true})
await fs.writeFile(path.join(folder,'build.log'),result.stdout+result.stderr)
if(result.status!==0)throw new Error(result.stderr)
const lower=new Map()
const strip=(minX,minZ,maxX,maxZ)=>{for(let across=minX;across<=maxX;across++)for(let forward=minZ;forward<=maxZ;forward++)lower.set(`${across},${forward}`,[across,2,forward])}
strip(49,52,51,60);strip(45,51,51,53)
strip(45,33,47,51);strip(30,33,47,35);strip(30,33,32,47);strip(30,47,47,47)
strip(36,20,40,34);strip(32,19,54,21);strip(46,11,48,21)
strip(20,19,36,21);strip(20,12,22,30);strip(20,29,22,38)
strip(22,36,32,38);strip(20,38,22,42);strip(12,40,21,42);strip(11,42,13,47);strip(7,45,12,47)
const upper=[]
for(let across=46;across<=48;across++)for(let forward=8;forward<=9;forward++)upper.push([across,3,forward])
const steps=[46,47,48].map(across=>[across,2.5,10])
const roof=[]
for(let across=0;across<61;across++)for(let forward=0;forward<61;forward++)if(!regions.some(region=>region.roof&&inRegion(across,forward,region)))roof.push([across,forward])
const containers=[{position:[47,3,7],facing:'south',front_feet:[47,3,8],lid_clearance_box:[[46,4,6],[48,6,8]]},{position:[8,2,44],facing:'south',front_feet:[8,2,45],lid_clearance_box:[[7,3,43],[9,5,45]]}].map(item=>({...item,type:'single',inventory:'EMPTY',LootTable:null}))
const fingerprint=hash(await fs.readFile(path.join(folder,'room.nbt')))
const metadata={id,number:'10',name:'苔藓古林完整多区地牢总览',size,entrance:[50,2,60],interior:[38,2,34],facing:'south',feet_y:2,regions,openings,ports:[{side:'south',feet:[50,2,60],width:3,clearHeight:3}],route:[...lower.values()],body_route:[...lower.values(),...steps,...upper],upper_targets:upper,stair_targets:steps,roof_exceptions:roof,containers,sha256:fingerprint,reference,reference_sha256:referenceHash,rotation_pivot:[0,0,0],minecraft:'1.20.1',status:'S1_10_PENDING_INDEPENDENT_REVIEW',deviations:['61x13x61 single continuous source, six unequal destination rooms plus four corridor segments; not a montage or reuse of nine prior rooms.','True open central tree courtyard; other rooms and corridors fully roofed. Image cutaway represented only by explicit preview clipping.','No exterior forest/ravine/river background, loose illustrated props or shader rays. Grass foundation margin is inside source bounds.','Small water-shrine fall becomes sealed glazed source-water column. Main hall gold altar is one-block high and reached by two half-step transitions.','Two real empty chests, no loot or monsters. Entry is a permanently open arch; no doors/gates or open-only analysis.','Solid masonry/wood botanical interpretation of stone ornament and dining furniture; no gameplay runtime.'],connections:'One south external lower entrance, all six regions linked via three-wide supported internal routes including real west elbow. No disconnected modules; shards translate only, common source origin [0,0,0].'}
await fs.writeFile(path.join(folder,'room.json'),JSON.stringify(metadata,null,2)+'\n')
await fs.writeFile(path.join(root,'group.json'),JSON.stringify({id:'nature-redo-b10',group_fingerprint:referenceHash,mode:'ONE_IMAGE_ONLY_COMPLETE_OVERVIEW',rooms:[{id,name:metadata.name,size,sha256:fingerprint,reference}],next_image:'S1-11',next_image_authorized_to_start:false,paid_image_requests:0,previous_accepted_sha256:'694be553a2ddb517877ecde7b59380be666d85500cc1b912e6b69565b37d0f1f'},null,2)+'\n')
console.log(result.stdout)
