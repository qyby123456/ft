import fs from 'node:fs'
import path from 'node:path'
import {fileURLToPath} from 'node:url'
import {createHash} from 'node:crypto'
const root=path.dirname(fileURLToPath(import.meta.url))
export default function build(api) {
  const parent=path.dirname(root)
  const folders=fs.readdirSync(parent).filter(name=>/^\d\d-/.test(name)).sort()
  if(folders.length!==10) throw new Error('Ten individually checked rooms are required')
  const rooms=folders.map(folder=>{
    const directory=path.join(parent,folder)
    const summary=JSON.parse(fs.readFileSync(path.join(directory,'summary.json')))
    const design=JSON.parse(fs.readFileSync(path.join(directory,'design.json')))
    const review=JSON.parse(fs.readFileSync(path.join(directory,'qa/self-review.json')))
    const bytes=fs.readFileSync(summary.nbtPath)
    const sha256=createHash('sha256').update(bytes).digest('hex')
    if(review.source_sha256!==sha256) throw new Error('Room review drift '+design.id)
    return {folder,summary,design,sha256,source:api.Structure.fromNbt(api.lodestone.NbtFile.read(bytes).root)}
  })
  const rows=[rooms.slice(0,5),rooms.slice(5).reverse()]
  for(let row=0;row<2;row++) {
    let east=5
    for(const room of rows[row]) {
      room.origin=[east,1,(row===0?36:91)-room.design.ports[0].feet[2]]
      room.ports=room.design.ports.map(port=>({...port,feet:port.feet.map((value,axis)=>value+room.origin[axis])}))
      east+=room.summary.size[0]+9
    }
  }
  const maxEast=Math.max(...rooms.map(room=>room.origin[0]+room.summary.size[0]))
  const turnEast=maxEast+7
  const size=[turnEast+4,23,120]
  const structure=api.createStructure(size)
  const envelope=new Set()
  const key=position=>position.join(',')
  const addEnvelope=(from,to)=>{
    for(let east=from[0];east<=to[0];east++) for(let up=from[1];up<=to[1];up++) for(let south=from[2];south<=to[2];south++) envelope.add(key([east,up,south]))
  }
  const put=(position,block,properties)=>api.setBlock(structure,position,`minecraft:${block}`,properties)
  for(const room of rooms) {
    const from=room.origin
    const to=room.summary.size.map((value,axis)=>value+from[axis]-1)
    addEnvelope(from,to)
    for(const block of room.source.getBlocks()) {
      const position=block.pos.map((value,axis)=>value+from[axis])
      structure.addBlock(position,block.state.getName(),block.state.getProperties(),block.nbt)
    }
  }
  const paths=[]
  const segment=(from,to,label)=> {
    if(from[0]!==to[0]&&from[2]!==to[2]) throw new Error('Axis-aligned corridor required')
    const points=[]
    const length=Math.max(Math.abs(to[0]-from[0]),Math.abs(to[2]-from[2]))
    for(let index=0;index<=length;index++) points.push([from[0]+Math.sign(to[0]-from[0])*index,4,from[2]+Math.sign(to[2]-from[2])*index])
    paths.push({label,from,to,points})
  }
  for(let row=0;row<2;row++) for(let column=0;column<4;column++) segment(rows[row][column].ports[1].feet,rows[row][column+1].ports[0].feet,`wing-${row+1}-link-${column+1}`)
  const upperEnd=rows[0][4].ports[1].feet
  const lowerEnd=rows[1][4].ports[1].feet
  segment(upperEnd,[turnEast,4,36],'upper-turn')
  segment([turnEast,4,36],[turnEast,4,91],'east-spine')
  segment([turnEast,4,91],lowerEnd,'lower-turn')
  const entry=[2,4,36]
  segment(entry,rows[0][0].ports[0].feet,'entry-vestibule')
  const corridorCells=new Map()
  for(const corridor of paths) for(const point of corridor.points) {
    const [east,,south]=point
    for(let dx=-2;dx<=2;dx++) for(let dz=-2;dz<=2;dz++) for(let up=1;up<=9;up++) {
      const position=[east+dx,up,south+dz]
      if(position[0]<1) continue
      const distance=Math.max(Math.abs(dx),Math.abs(dz))
      const boundary=distance===2
      const material=up<=3?'polished_blackstone_bricks':up===9?'polished_blackstone_bricks':boundary?'orange_stained_glass':'air'
      const existing=corridorCells.get(key(position))
      if(!existing||material==='air'||up<=3||up===9) corridorCells.set(key(position),{position,material})
      envelope.add(key(position))
    }
  }
  for(const {position,material} of corridorCells.values()) {
    const module=rooms.find(room=>position.every((value,axis)=>value>=room.origin[axis]&&value<room.origin[axis]+room.summary.size[axis]))
    if(module) {
      const local=position.map((value,axis)=>value-module.origin[axis])
      const wall=local[0]===0||local[0]===module.summary.size[0]-1
      const port=module.ports.find(port=>Math.abs(position[2]-port.feet[2])<=1&&position[1]>=4&&position[1]<=7&&position[0]===port.feet[0])
      if(!wall||!port) continue
      put(position,'air')
    } else put(position,material)
  }
  const unusedPort=rows[1][0].ports[0]
  for(let across=-1;across<=1;across++) for(let up=0;up<4;up++) put([unusedPort.feet[0],4+up,unusedPort.feet[2]+across],'orange_stained_glass')
  const shell=new Set()
  for(const encoded of envelope) {
    const position=encoded.split(',').map(Number)
    for(const delta of [[1,0,0],[-1,0,0],[0,1,0],[0,-1,0],[0,0,1],[0,0,-1]]) {
      const adjacent=position.map((value,axis)=>value+delta[axis])
      const identifier=key(adjacent)
      if(!envelope.has(identifier)) shell.add(identifier)
    }
  }
  for(const encoded of shell) put(encoded.split(',').map(Number),'barrier')
  const modules=rooms.map(room=>({id:room.design.id,folder:room.folder,source:room.summary.nbtPath,source_sha256:room.sha256,origin:room.origin,size:room.summary.size,ports:room.ports}))
  const targets=rooms.flatMap(room=>room.design.targets.map(target=>({label:`${room.design.id} ${target.label}`,position:target.position.map((value,axis)=>value+room.origin[axis])})))
  targets.push({label:'east connecting spine',position:[turnEast,4,64]})
  const layout={size,entry,modules,corridors:paths,unused_ports_closed:[{module:rows[1][0].design.id,port:unusedPort}],barrier_cells:shell.size,envelope_cells:envelope.size,scope:'New two-wing connected gallery. Lower wing traversed in reverse, end portal sealed with full glass; outside union shell is barrier, no internal separating barrier.'}
  fs.writeFileSync(path.join(root,'layout.json'),JSON.stringify(layout,null,2))
  fs.writeFileSync(path.join(root,'design.json'),JSON.stringify({id:'S6-99',size,floor_feet:4,ports:[{side:'west',feet:entry,width:3,clearHeight:4}],targets,differences:['Two purpose-built wings linked by an east gallery; module local heights preserved.','Entry vestibule stages inside the barrier envelope; no deployment or exterior world access certified.','Only the unused final outer portal is glass sealed; all internal interfaces remain open.']},null,2))
  return {structure,metadata:{name:'s6-volcanic-connected'}}
}
