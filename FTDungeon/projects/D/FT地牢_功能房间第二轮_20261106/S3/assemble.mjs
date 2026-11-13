import fs from 'node:fs'
import path from 'node:path'
import crypto from 'node:crypto'
import {fileURLToPath} from 'node:url'
const root=path.dirname(fileURLToPath(import.meta.url))
export default function build(api){
  const {Structure,NbtFile}=api.lodestone
  const modules=[],cells=new Map(),connections=[],walks=[]
  const size=[207,35,184],structure=api.createStructure(size)
  const put=(east,up,south,state,nbt)=>cells.set([east,up,south].join(','),{pos:[east,up,south],state,nbt})
  function box(from,to,state){for(let east=from[0];east<=to[0];east++)for(let up=from[1];up<=to[1];up++)for(let south=from[2];south<=to[2];south++)put(east,up,south,state)}
  const positions={11:[20,0,20],12:[65,0,22],13:[108,0,16],14:[157,0,17],15:[149,0,78],16:[106,0,71],17:[65,0,79],18:[20,0,78],19:[20,0,133],20:[92,6,139]}
  for(let index=11;index<=20;index++){
    const id=`s3-${index}`,folder=path.join(root,'rooms',id)
    const design=JSON.parse(fs.readFileSync(path.join(folder,'design.json'),'utf8'))
    const report=JSON.parse(fs.readFileSync(path.join(folder,'qa/report.json'),'utf8'))
    const visual=JSON.parse(fs.readFileSync(path.join(folder,'qa/visual-review.json'),'utf8'))
    const bytes=fs.readFileSync(path.join(folder,id+'.nbt')),hash=crypto.createHash('sha256').update(bytes).digest('hex')
    if(report.self_check!=='PASS'||report.candidate_sha256!==hash||visual.candidate_sha256!==hash)throw Error('Unreviewed source '+id)
    const source=Structure.fromNbt(NbtFile.read(bytes).root)
    modules.push({id,origin:positions[index],size:design.size,design,source,sha256:hash})
  }
  const port=(number,name)=>{const module=modules.find(module=>module.id===`s3-${number}`),local=module.design.ports.find(port=>port.name===name);return local.foot.map((value,axis)=>value+module.origin[axis])}
  function tunnel(label,points){
    const route=[]
    for(let index=1;index<points.length;index++){
      const first=points[index-1],last=points[index],steps=Math.max(...last.map((value,axis)=>Math.abs(value-first[axis])))
      if(first[1]!==last[1])throw Error('Use source stairs for elevation')
      for(let step=0;step<=steps;step++)route.push(first.map((value,axis)=>Math.round(value+(last[axis]-value)*step/Math.max(1,steps))))
    }
    for(const [east,up,south] of route)box([east-2,up-1,south-2],[east+2,up+4,south+2],'minecraft:packed_ice')
    walks.push(route)
    connections.push({label,points,clear_width:3,clear_height:4,foot_y:points[0][1]})
  }
  function link(first,last,label,middle=[]){tunnel(label,[first,...middle,last])}
  for(let index=11;index<14;index++){
    const first=port(index,'east'),last=port(index+1,'west'),middleEast=Math.round((first[0]+last[0])/2)
    link(first,last,`${index}-${index+1}`,[[middleEast,2,first[2]],[middleEast,2,last[2]]])
  }
  link(port(14,'east'),port(15,'east'),'14-15',[[194,2,port(14,'east')[2]],[194,2,port(15,'east')[2]]])
  for(let index=15;index<18;index++){
    const first=port(index,'west'),last=port(index+1,'east'),middleEast=Math.round((first[0]+last[0])/2)
    link(first,last,`${index}-${index+1}`,[[middleEast,2,first[2]],[middleEast,2,last[2]]])
  }
  link(port(18,'west'),port(19,'lower-east'),'18-19',[[9,2,port(18,'west')[2]],[9,2,118],[65,2,118],[65,2,port(19,'lower-east')[2]]])
  link(port(19,'upper-south'),port(20,'west'),'19-20',[[port(19,'upper-south')[0],8,174],[80,8,174],[80,8,port(20,'west')[2]]])
  const arrival=[10,2,31],departure=[135,8,157]
  link(arrival,port(11,'west'),'arrival')
  link(port(20,'east'),departure,'departure')
  for(const route of walks)for(const [east,up,south] of route){
    box([east-1,up,south-1],[east+1,up+3,south+1],'minecraft:air')
    box([east-1,up-1,south-1],[east+1,up-1,south+1],'minecraft:stone_bricks')
  }
  for(const module of modules){
    for(const block of module.source.getBlocks()){
      const position=block.pos.map((value,axis)=>value+module.origin[axis])
      const state=block.state
      put(...position,{name:state.getName().toString(),properties:state.getProperties()},block.nbt)
    }
  }
  const raised=[...cells.values()];cells.clear()
  for(const cell of raised)put(cell.pos[0],cell.pos[1]+1,cell.pos[2],cell.state,cell.nbt)
  for(const module of modules)module.origin[1]++
  for(const connection of connections){connection.foot_y++;for(const point of connection.points)point[1]++}
  arrival[1]=3;departure[1]=9
  for(let east=0;east<size[0];east++)for(let south=0;south<size[2];south++){put(east,0,south,'minecraft:barrier');put(east,size[1]-1,south,'minecraft:barrier')}
  for(let up=0;up<size[1];up++)for(let east=0;east<size[0];east++){put(east,up,0,'minecraft:barrier');put(east,up,size[2]-1,'minecraft:barrier')}
  for(let up=0;up<size[1];up++)for(let south=0;south<size[2];south++){put(0,up,south,'minecraft:barrier');put(size[0]-1,up,south,'minecraft:barrier')}
  for(const cell of cells.values()){
    if(typeof cell.state==='string')api.setBlock(structure,cell.pos,cell.state)
    else api.setBlock(structure,cell.pos,cell.state.name,cell.state.properties,cell.nbt)
  }
  const folder=path.join(root,'rooms','s3-merged');fs.mkdirSync(folder,{recursive:true})
  const manifest=modules.map(({id,origin,size,sha256,design})=>({id,origin,size,sha256,ports:design.ports,targets:design.targets}))
  const targets=modules.flatMap(module=>module.design.targets.map(target=>({label:module.id+' '+target.label,position:target.position.map((value,axis)=>value+module.origin[axis])})))
  targets.push({label:'departure chamber',position:departure})
  const design={id:'s3-merged',size,title:'霜穴双廊回环与高台密库',modules:manifest,connections,ports:[],start:arrival,targets,cutY:26,differences:['合并采用双排折返，19高台真实六格抬升，20整间上移六格衔接','外围完整屏障包络，无内部屏障；到达/离开点在包络内，部署传送待后续指令','源模板为稀疏NBT，须在已清空体积部署；本轮不写世界','无战斗、掉落表、红石隐门或真实取水运行验收']}
  fs.writeFileSync(path.join(folder,'design.json'),JSON.stringify(design,null,2))
  fs.writeFileSync(path.join(folder,'cells.json'),JSON.stringify([...cells.values()].map(cell=>({pos:cell.pos,state:typeof cell.state==='string'?cell.state.replace('minecraft:',''):cell.state.name,entity:null}))))
  fs.writeFileSync(path.join(root,'assembly.json'),JSON.stringify(design,null,2))
  return {structure,metadata:{name:'s3-merged'}}
}
