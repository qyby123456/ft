import fs from 'node:fs/promises'
import path from 'node:path'
import crypto from 'node:crypto'
import {fileURLToPath,pathToFileURL} from 'node:url'
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..'),out=path.join(root,'assembly')
const {Structure,NbtFile}=await import(pathToFileURL('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'))
const size=[240,29,145],structure=new Structure(size),modules=[],paths=[],targets=[]
const put=(position,name,properties,nbt)=>structure.addBlock(position,`minecraft:${name}`,properties,nbt)
const digest=bytes=>crypto.createHash('sha256').update(bytes).digest('hex')
for(let index=0;index<10;index++){
 const id=`S8-${11+index}`,directory=path.join(root,id),inventory=JSON.parse(await fs.readFile(path.join(directory,'inventory.json'),'utf8'))
 const filename=(await fs.readdir(directory)).find(name=>name.endsWith('.nbt')),bytes=await fs.readFile(path.join(directory,filename))
 const self=JSON.parse(await fs.readFile(path.join(directory,'SELF-QA.json'),'utf8'));if(self.candidate_sha256!==digest(bytes))throw Error('Room snapshot drift')
 const source=Structure.fromNbt(NbtFile.read(bytes).root),origin=[10+(index%5)*45,2,index<5?10:75]
 for(const block of source.getBlocks())structure.addBlock(block.pos.map((coordinate,axis)=>coordinate+origin[axis]),block.state.getName(),block.state.getProperties(),block.nbt)
 const ports=inventory.metadata.ports.map(port=>({...port,foot:port.foot.map((value,axis)=>value+origin[axis])}))
 modules.push({id,origin,size:source.getSize(),source:path.join(directory,filename),sha256:digest(bytes),ports})
 for(const target of inventory.metadata.targets)targets.push({label:`${id}: ${target.label}`,position:target.position.map((value,axis)=>value+origin[axis])})
 for(const port of ports){
  const [east,up,south]=port.foot
  if(port.side==='north'){
   const points=[];for(let z=55;z<south;z++)points.push([east,z,Math.max(4,up-(south-z)*0.5)])
   paths.push({room:id,side:port.side,points})
  }else{
   const gap=port.side==='west'?origin[0]-4:origin[0]+source.getSize()[0]+3,points=[]
   for(let x=Math.min(east,gap);x<=Math.max(east,gap);x++)points.push([x,south,up])
   for(let z=Math.min(south,55);z<=Math.max(south,55);z++)points.push([gap,z,up])
   paths.push({room:id,side:port.side,points})
  }
 }
}
paths.push({room:'spine',points:Array.from({length:223},(_,index)=>[6+index,55,4])})
const inRoom=(x,z)=>modules.some(module=>x>=module.origin[0]&&x<module.origin[0]+module.size[0]&&z>=module.origin[2]&&z<module.origin[2]+module.size[2])
const cells=new Map()
for(const route of paths)for(const [east,south,height]of route.points)for(let dx=-2;dx<=2;dx++)for(let dz=-2;dz<=2;dz++){
 const x=east+dx,z=south+dz;if(inRoom(x,z))continue
 const interior=Math.abs(dx)<2&&Math.abs(dz)<2,key=`${x},${z}`,existing=cells.get(key)
 if(!existing||interior)cells.set(key,{x,z,height,interior})
}
for(const {x,z,height,interior}of cells.values()){
 const foot=Math.ceil(height),floor=foot-1
 put([x,3,z],'polished_deepslate')
 if(height%1)put([x,floor,z],'polished_deepslate_slab',{type:'bottom',waterlogged:'false'})
 else put([x,floor,z],(x+z)%9===0?'amethyst_block':'polished_deepslate')
 for(let y=foot;y<foot+5;y++)put([x,y,z],interior?'air':y===foot+2?'purple_stained_glass':'deepslate_bricks')
 put([x,foot+5,z],'deepslate_tiles')
}
for(let x=0;x<size[0];x++)for(let z=0;z<size[2];z++){put([x,0,z],'barrier');put([x,size[1]-1,z],'barrier')}
for(let y=1;y<size[1]-1;y++){
 for(let x=0;x<size[0];x++){put([x,y,0],'barrier');put([x,y,size[2]-1],'barrier')}
 for(let z=1;z<size[2]-1;z++){put([0,y,z],'barrier');put([size[0]-1,y,z],'barrier')}
}
const bytes=structure.writeNbt({dataVersion:3465,compression:'gzip'}),filename='s8-functional-combined.nbt'
await fs.writeFile(path.join(out,filename),bytes)
const sha256=digest(bytes),metadata={name:'s8-functional-combined',size,ports:[],start:[6,4,55],targets,features:['ten reference-specific rooms','central glazed geode gallery','twenty branch connections','watch-balcony half-step elevation adapter','outer barrier envelope'],differences:['Gallery is new assembly design, not reference room20','No external deployment gate: enclosed review artifact only']}
await fs.writeFile(path.join(out,'layout.json'),JSON.stringify({sha256,size,modules,paths,metadata},null,2))
await fs.writeFile(path.join(out,'summary.json'),JSON.stringify({name:metadata.name,dataVersion:3465,nbtPath:path.join(out,filename),size,blockCount:structure.getBlocks().length},null,2))
console.log('Combined',sha256,structure.getBlocks().length)
