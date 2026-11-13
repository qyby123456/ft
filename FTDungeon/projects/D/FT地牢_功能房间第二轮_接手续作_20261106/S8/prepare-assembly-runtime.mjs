import fs from 'node:fs/promises'
import path from 'node:path'
import {spawnSync} from 'node:child_process'
const root=process.cwd(),out=path.join(root,process.argv[2]??'runtime-assembly-r1'),s2='D:/ft工程文件/FT地牢_功能房间第二轮_接手续作_20261106/S2/runtime-assembly-r1'
if(await fs.stat(`${out}/server`).catch(()=>null))throw Error('Existing runtime must be preserved')
await fs.mkdir(`${out}/path-planning`,{recursive:true})
await fs.cp('runtime-r4/native-test',`${out}/native-test`,{recursive:true,filter:source=>!/(?:^|[\\/])(?:build|\.gradle)(?:[\\/]|$)/.test(source)})
const planner=path.join(out,'path-planning'),classpath=(await fs.readFile('assembly/qa/classpath.txt','utf8')).trim()
await fs.copyFile(`${s2}/path-planning/ProducerPathExport.java`,`${planner}/ProducerPathExport.java`)
let input=JSON.parse(await fs.readFile('assembly/qa/route-input.json','utf8'));input[0].targets=input[0].targets.slice(0,58)
await fs.writeFile(`${planner}/route-input.json`,JSON.stringify(input,null,2))
for(const [command,args] of [['javac',['-encoding','UTF-8','-cp',classpath,'-d',planner,`${planner}/ProducerPathExport.java`]],['java',['-Xmx1536m','-cp',`${planner};${classpath}`,'ProducerPathExport',planner]]]){
 const result=spawnSync(command,args,{cwd:planner,encoding:'utf8',windowsHide:true});await fs.writeFile(`${planner}/${command}.log`,result.stdout+result.stderr);if(result.status!==0)throw Error(`${command} failed`)
}
const report=JSON.parse(await fs.readFile(`${planner}/independent-routes.json`,'utf8'))[0],tree=new Map(),pointKey=point=>point.join(','),points=new Map()
let start
for(const target of report.targets){
 if(!target.reachable||!target.standing_clear)throw Error(`Target blocked ${target.label}`)
 start=target.path[0]
 for(let index=0;index<target.path.length-1;index++){
  const first=pointKey(target.path[index]),next=pointKey(target.path[index+1]);points.set(first,target.path[index]);points.set(next,target.path[index+1]);if(!tree.has(first))tree.set(first,new Set());tree.get(first).add(next)
 }
}
const movement=[start]
function visit(key){for(const child of tree.get(key)??[]){movement.push(points.get(child));visit(child);movement.push(points.get(key))}}
visit(pointKey(start))
await fs.writeFile(`${out}/assembly-input.json`,JSON.stringify({candidate:input[0].candidate,sha256:input[0].sha256,movement_route:movement,movement_target_count:report.targets.length,movement_scope:'Native half-grid tree traversal to all 58 targets and return; runtime Player.travel validates motion separately'},null,2))
const directory=`${out}/native-test/src/main/java/net/farmingtales/s8qa`
for(const name of ['RoomTests.java','LadderTests.java'])await fs.unlink(`${directory}/${name}`)
let java=await fs.readFile(`${s2}/native-test/src/main/java/net/farmingtales/s2qa/AssemblyTests.java`,'utf8')
java=java.replaceAll('s2qa','s8qa').replaceAll('fts2','fts8').replaceAll('S2','S8').replaceAll('69 targets','58 targets')
java=java.replace('for(int east=63;east<=80;east++)for(int south=63;south<=73;south++)','for(int east=63;east<=80;east++)for(int south=63;south<=74;south++)')
java=java.replace('position.offset(-1,1,-1),position.offset(1,1,1)','position.offset(-2,0,-2),position.offset(2,1,2)')
await fs.writeFile(`${directory}/AssemblyTests.java`,java)
await fs.copyFile('runtime-r4/run.ps1',`${out}/run.ps1`)
console.log('Prepared assembly runtime',movement.length,'waypoints',report.targets.length,'targets')
