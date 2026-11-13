import fs from 'node:fs'
import path from 'node:path'
import crypto from 'node:crypto'
import {fileURLToPath} from 'node:url'
import {NbtFile,Structure} from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'
const root=path.dirname(fileURLToPath(import.meta.url))
const candidate=fs.readFileSync(path.join(root,'nbt/s5-06-coral-lantern-corner.nbt'))
const load=bytes=>Structure.fromNbt(NbtFile.read(bytes).root)
const source=load(candidate)
const require=(valid,message)=>{if(!valid)throw Error(message)}
const transform=(point,index)=>index===0?point:index===1?[30-point[2],point[1],point[0]]:index===2?[30-point[0],point[1],30-point[2]]:[point[2],point[1],30-point[0]]
const cases=[]
for(const [index,rotation] of ['NONE','CLOCKWISE_90','CLOCKWISE_180','COUNTERCLOCKWISE_90'].entries()){
  const capture=load(fs.readFileSync(path.join(root,'worldtest',rotation,'after-1200-ticks.nbt')))
  const nameAt=point=>{const rotated=transform(point,index);return capture.getBlock([rotated[0]+8,rotated[1],rotated[2]+8])?.state.getName().toString()}
  const passable=new Set(['minecraft:water','minecraft:kelp','minecraft:kelp_plant','minecraft:seagrass'])
  const clear=(east,south)=>passable.has(nameAt([east,3,south]))&&passable.has(nameAt([east,4,south]))
  const pending=[[0,24]],parents=new Map([['0,24',null]])
  for(let cursor=0;cursor<pending.length;cursor++){
    const current=pending[cursor]
    for(const offset of [[1,0],[-1,0],[0,1],[0,-1]]){
      const next=[current[0]+offset[0],current[1]+offset[1]],key=next.join(',')
      if(next.some(value=>value<0||value>30)||parents.has(key)||!clear(...next))continue
      parents.set(key,current);pending.push(next)
    }
  }
  require(parents.has('24,0'),'Full-bounds route missing')
  require(pending.every(([east,south])=>east>=19||south>=19),'NW water margin bypass reachable at feetY3')
  const route=[]
  for(let cursor=[24,0];cursor;cursor=parents.get(cursor.join(',')))route.push([cursor[0],3,cursor[1]])
  route.reverse()
  require(route.some(point=>point[0]>=19&&point[2]>=19),'Path bypasses turn quadrant')
  const headings=new Set(route.slice(1).map((point,step)=>[point[0]-route[step][0],point[2]-route[step][2]].join(',')))
  require(headings.has('1,0')&&headings.has('0,-1'),'No true90degree turn')
  const directBlockers=[]
  for(let step=1;step<96;step++){
    const east=Math.floor(0.5+24*step/96),south=Math.floor(24.5-24*step/96)
    const name=nameAt([east,3,south])
    if(name==='minecraft:prismarine_bricks')directBlockers.push([east,3,south])
  }
  require(directBlockers.length>0,'Straight gate-to-gate diagonal unobstructed')
  for(let along=0;along<=18;along++)for(let up=3;up<=12;up++){
    for(const point of [[along,up,17],[17,up,along]])require(['minecraft:prismarine_bricks','minecraft:prismarine','minecraft:sea_lantern'].includes(nameAt(point)),'Opaque inner wall missing')
  }
  const sourceReport=JSON.parse(fs.readFileSync(path.join(root,'worldtest',rotation,'report.json'),'utf8'))
  cases.push({rotation,full_bounds_without_L_mask:true,passable_whitelist:[...passable],reachable:pending.length,gate_path:route,steps:route.length-1,heading_vectors:[...headings],turn_quadrant_required:true,diagonal_masonry_blockers:[...new Set(directBlockers.map(point=>point.join(',')))],native_route_targets:sourceReport.settled_routes.targets.length,native_reachable:sourceReport.settled_routes.reachable_count})
}
require(source.getBlock([19,7,18]).state.getName().toString()==='minecraft:sea_lantern'&&source.getBlock([18,7,19]).state.getName().toString()==='minecraft:sea_lantern','Corner lamps missing')
fs.writeFileSync(path.join(root,'qa/turn-topology.json'),JSON.stringify({status:'PASS_PRODUCER_READBACK_NOT_INDEPENDENT_ACCEPTANCE',nbt_sha256:crypto.createHash('sha256').update(candidate).digest('hex'),cases,limit:'Conservative two-high route whitelist in full31x31 bounds at feetY3, no L-shaped allowed mask. Separate native GameTest checks exact block collision and14targets. Does not forbid swimming over roof or going around module outside bounds.'},null,2))
console.log('PASS actual90degree turn, blocked direct diagonal, opaque inner walls and no NW floor bypass: four settled captures, no L-shaped search restriction')
