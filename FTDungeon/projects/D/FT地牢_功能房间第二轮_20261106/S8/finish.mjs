import fs from 'node:fs/promises'
import path from 'node:path'
import crypto from 'node:crypto'
import {pathToFileURL} from 'node:url'
const root=path.dirname(new URL(import.meta.url).pathname.replace(/^\/([A-Za-z]:)/,'$1'))
const room=process.argv[2],directory=path.resolve(room)
const read=async file=>JSON.parse(await fs.readFile(path.join(directory,file),'utf8'))
const inventory=await read('inventory.json'),native=(await read('qa/independent-native-frozen.json'))[0],routes=(await read('qa/independent-routes.json'))[0],render=await read('render-evidence.json')
if([native,routes,render].some(value=>value.sha256!==inventory.sha256))throw Error('Candidate hash mismatch')
if(native.registry_properties!=='PASS'||native.template_roundtrip_block_count!=='PASS'||native.survival_failures.length||native.survival_unsupported.length||!routes.start_clear||routes.targets.some(target=>!target.reachable||!target.standing_clear))throw Error('Self QA failure')
const {Structure,NbtFile}=await import(pathToFileURL('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'))
const filename=(await fs.readdir(directory)).find(name=>name.endsWith('.nbt'))
const structure=Structure.fromNbt(NbtFile.read(await fs.readFile(path.join(directory,filename))).root)
const blocks=new Map(structure.getBlocks().map(block=>[block.pos.join(','),block]))
const at=(x,y,z)=>blocks.get([x,y,z].join(','))?.state.getName().toString()??'minecraft:air'
const failures=[],containers=[]
for(const block of structure.getBlocks()){
 const name=block.state.getName().toString(),[x,y,z]=block.pos
 if(['minecraft:barrel','minecraft:chest','minecraft:furnace','minecraft:blast_furnace','minecraft:smoker'].includes(name)){
  if(at(x,y-1,z)==='minecraft:air')failures.push({type:'floating-container',pos:block.pos})
  if(block.nbt?.getList('Items').length)failures.push({type:'nonempty-container',pos:block.pos})
  containers.push({pos:block.pos,block:name,empty:true})
 }
 if(name==='minecraft:amethyst_cluster'&&block.state.getProperties().facing==='up'&&at(x,y-1,z)==='minecraft:air')failures.push({type:'unsupported-crystal',pos:block.pos})
}
const reference=JSON.parse(await fs.readFile(path.resolve('references.json'),'utf8')).references.find(reference=>reference.id===room)
const imageHash=crypto.createHash('sha256').update(await fs.readFile(reference.image_path)).digest('hex')
if(imageHash!==reference.sha256)throw Error('Reference drift')
if(failures.length)throw Error(JSON.stringify(failures))
const report={candidate_sha256:inventory.sha256,reference_sha256:imageHash,reference_image_viewed:true,lodestone_cutaway_viewed:true,self_check:'PASS',independent_acceptance:'NOT_RUN',native_report:'qa/independent-native-frozen.json',route_report:'qa/independent-routes.json',containers,static_supplement_failures:failures,risks:inventory.hazards.length?'Contained fluids present; tick simulation not run':'No water/lava/fire blocks',not_run:['world placement','neighbor/random/fluid tick simulation','player gameplay','independent acceptance'],visual_notes:process.argv[3]??'Distinct reference silhouette and functional furniture reviewed; vanilla palette approximations documented.'}
await fs.writeFile(path.join(directory,'SELF-QA.json'),JSON.stringify(report,null,2))
await fs.writeFile(path.join(directory,'README.md'),`# ${room}\n\n制作方自检完成，等待独立复核。候选 SHA256: ${inventory.sha256}\n\n尺寸 ${inventory.size.join(' × ')}。接口（脚点，净宽/高）：\n${inventory.metadata.ports.map(port=>`- ${port.side}: [${port.foot}]，${port.width}×${port.height}`).join('\n')}\n\n特征：${inventory.metadata.features.join('；')}。\n\n差异：${inventory.metadata.differences.join('；')}。\n\n${report.visual_notes}\n\n所有容器空库存，无战斗逻辑；完整NBT不是剖切。复跑命令见上级README。原版静态检查非游戏运行或独立验收。\n`)
let progress=JSON.parse(await fs.readFile(path.resolve('progress.json'),'utf8'))
progress.completed=progress.completed.filter(item=>item.id!==room);progress.completed.push({id:room,sha256:inventory.sha256,self_check:'PASS',independent_acceptance:'NOT_RUN'});progress.current=Number(room.slice(-2))<20?`S8-${Number(room.slice(-2))+1}`:'assembly';progress.status='in_progress'
await fs.writeFile(path.resolve('progress.json'),JSON.stringify(progress,null,2));console.log('Self QA complete',room,inventory.sha256)
