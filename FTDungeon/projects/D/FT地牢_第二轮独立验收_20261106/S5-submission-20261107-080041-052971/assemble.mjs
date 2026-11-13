import fs from 'node:fs'
import path from 'node:path'
import crypto from 'node:crypto'
import {root} from './tools.mjs'
const hash=bytes=>crypto.createHash('sha256').update(bytes).digest('hex')
const directions=['north','west','south','east']
function turnPosition(position,size,turns){
  let transformed=[...position],dimensions=[...size]
  for(let turn=0;turn<turns;turn++){
    transformed=[transformed[2],transformed[1],dimensions[0]-1-transformed[0]]
    dimensions=[dimensions[2],dimensions[1],dimensions[0]]
  }
  return transformed
}
function turnProperties(properties,turns){
  const transformed={}
  for(let [key,value] of Object.entries(properties)){
    if(directions.includes(key))key=directions[(directions.indexOf(key)+turns)%4]
    if(directions.includes(value))value=directions[(directions.indexOf(value)+turns)%4]
    else if(key==='axis'&&turns%2&&['x','z'].includes(value))value=value==='x'?'z':'x'
    else if(key==='rotation')value=String((Number(value)-4*turns+16)%16)
    transformed[key]=value
  }
  return transformed
}
export default function(api){
  const folder=path.join(root,'merged');fs.mkdirSync(folder,{recursive:true})
  const sources=[]
  for(let number=11;number<=20;number++){
    const id=`S5-${number}`,directory=path.join(root,id)
    const design=JSON.parse(fs.readFileSync(path.join(directory,'design.json')))
    const report=JSON.parse(fs.readFileSync(path.join(directory,'qa/self-check.json')))
    const bytes=fs.readFileSync(path.join(directory,id.toLowerCase()+'.nbt'))
    if(!report.self_check_passed||report.sha256!==hash(bytes))throw Error(`Unverified source ${id}`)
    const source=api.lodestone.Structure.fromNbt(api.lodestone.NbtFile.read(bytes).root)
    const turns=(number===12||number===19?1:0)+(number>=16?2:0)
    const size=turns%2?[design.size[2],design.size[1],design.size[0]]:design.size
    const ports=design.ports.map(port=>({...port,feet:turnPosition(port.feet,design.size,turns),facing:directions[(directions.indexOf(port.facing)+turns)%4]}))
    sources.push({id,source,design,turns,size,ports,sha256:hash(bytes),candidate:path.join(directory,id.toLowerCase()+'.nbt')})
  }
  let cursor=8,feetHeight=9
  for(const module of sources.slice(0,5)){
    const incoming=module.ports.find(port=>port.facing==='west')
    module.origin=[cursor,feetHeight-incoming.feet[1],23-incoming.feet[2]]
    cursor+=module.size[0]+8
    module.incoming=incoming;module.outgoing=module.ports.find(port=>port.facing==='east')
  }
  let right=sources[4].origin[0]+sources[4].size[0]-1
  for(const module of sources.slice(5)){
    const incoming=module.ports.find(port=>port.facing==='east')
    module.origin=[right-module.size[0]+1,feetHeight-incoming.feet[1],70-incoming.feet[2]]
    module.incoming=incoming;module.outgoing=module.ports.find(port=>port.facing==='west')
    feetHeight=module.origin[1]+module.outgoing.feet[1]
    right=module.origin[0]-9
  }
  const size=[cursor+8,29,94]
  const structure=api.createStructure(size)
  api.fill(structure,[0,0,0],size.map(value=>value-1),'minecraft:air')
  const world=(module,position)=>position.map((value,axis)=>value+module.origin[axis])
  const modules=sources.map(module=>({id:module.id,origin:module.origin,size:module.size,turns_ccw:module.turns,source:module.candidate,source_sha256:module.sha256,cutHeight:module.design.cutHeight??10,ports:module.ports.map(port=>({...port,feet:world(module,port.feet)}))}))
  const corridorCells=new Map()
  function corridor(position){
    const [east,up,south]=position
    if(modules.some(module=>position.every((value,axis)=>value>=module.origin[axis]&&value<module.origin[axis]+module.size[axis])))return
    for(let side=-3;side<=3;side++)for(let rise=-1;rise<=4;rise++){
      const cell=[east,up+rise,south+side]
      const block=rise===-1?'minecraft:prismarine_bricks':rise===4?'minecraft:cyan_stained_glass':Math.abs(side)===3?'minecraft:prismarine_bricks':'minecraft:air'
      const key=cell.join(',')
      const previous=corridorCells.get(key)
      if(!previous||block==='minecraft:air'||previous.block!=='minecraft:air')corridorCells.set(key,{cell,block})
    }
  }
  const edges=[]
  for(let index=0;index<sources.length-1;index++){
    const left=sources[index],next=sources[index+1]
    const start=world(left,left.outgoing.feet),end=world(next,next.incoming.feet)
    const points=index===4?[start,[size[0]-6,start[1],start[2]],[size[0]-6,end[1],end[2]],end]:[start,end]
    for(let segment=0;segment<points.length-1;segment++){
      const from=points[segment],to=points[segment+1],distance=Math.max(...from.map((value,axis)=>Math.abs(value-to[axis])))
      for(let step=0;step<=distance;step++){
        const position=from.map((value,axis)=>Math.round(value+(to[axis]-value)*step/Math.max(1,distance)))
        corridor(position)
        if(from[2]!==to[2]){
          for(let side=-3;side<=3;side++)for(let rise=-1;rise<=4;rise++){
            const cell=[position[0]+side,position[1]+rise,position[2]],key=cell.join(',')
            const block=rise===-1?'minecraft:prismarine_bricks':rise===4?'minecraft:cyan_stained_glass':Math.abs(side)===3?'minecraft:prismarine_bricks':'minecraft:air'
            const previous=corridorCells.get(key)
            if(!previous||block==='minecraft:air'||previous.block!=='minecraft:air')corridorCells.set(key,{cell,block})
          }
        }
      }
    }
    edges.push({from:left.id,to:next.id,start,end,centerline:points})
  }
  for(const {cell,block} of corridorCells.values())api.setBlock(structure,cell,block)
  for(const module of sources)for(const block of module.source.getBlocks()){
    const position=world(module,turnPosition(block.pos,module.design.size,module.turns))
    structure.addBlock(position,block.state.getName(),turnProperties(block.state.getProperties(),module.turns),block.nbt)
  }
  const first=sources[0],last=sources.at(-1)
  for(const module of [first,last]){
    const port=module===first?module.incoming:module.outgoing
    const [east,up,south]=world(module,port.feet)
    for(let offset=-2;offset<=2;offset++)for(let rise=0;rise<4;rise++)api.setBlock(structure,[east,up+rise,south+offset],'minecraft:cyan_stained_glass')
  }
  api.hollowBox(structure,[0,0,0],size.map(value=>value-1),'minecraft:barrier')
  const start=world(first,first.incoming.feet);start[0]+=2
  const end=world(last,last.outgoing.feet);end[0]+=2
  const targets=[]
  for(const module of sources){
    for(const target of module.design.targets)targets.push({label:module.id+'-'+target.label,position:world(module,turnPosition(target.position,module.design.size,module.turns))})
    for(const port of module.ports){
      if((module===first&&port===module.incoming)||(module===last&&port===module.outgoing))continue
      targets.push({label:module.id+'-'+port.name,position:world(module,port.feet)})
    }
  }
  const retired_interfaces=[{id:'S5-11-west',position:world(first,first.incoming.feet),reason:'sealed terminal cap',cap_cells:20,replacement:start},{id:'S5-20-east',position:world(last,last.outgoing.feet),reason:'sealed terminal cap',cap_cells:20,replacement:end}]
  const interface_contract={corridor_clear_width:5,minimum_door_throat_width:3,throat:{position:[166,9,23],clear_z:[22,23,24],occupied_edge_points:[[166,9,21],[166,9,25]],reason:'Source training-room embedded jambs retained; 5-wide corridor narrows to 3 at this throat.'},prior_independent_coverage:{total:886,passed:884,failed_edge_points:[[166,9,21],[166,9,25]],status:'Retained expected solid jambs, NOT a five-wide pass'},external_access:'No public external open entrance. Internal entry/finish are explicit handoff coordinates; teleport/deployment not configured.'}
  fs.writeFileSync(path.join(folder,'design.json'),JSON.stringify({id:'merged',size,modules,edges,start,targets,ports:[],cutHeight:15,entry:start,finish:end,retired_interfaces,interface_contract,description:'Two-wing U-shaped underwater archive/utility complex; high watch balcony steps down to final secret vault.',differences:['Terminal unused ports capped with cyan glass; ingress/egress requires intentional teleport into internal lobby.','All source rooms preserved under declared rotations/translations except 40 cap cells TOTAL: 20 at each terminal.','Global external barrier shell; no external ocean survival claims.']},null,2))
  return {structure,metadata:{name:'merged'}}
}
