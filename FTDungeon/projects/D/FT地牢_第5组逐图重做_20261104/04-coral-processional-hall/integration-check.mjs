import fs from 'node:fs'
import path from 'node:path'
import crypto from 'node:crypto'
import {fileURLToPath} from 'node:url'
import {NbtFile,Structure} from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'
const root=path.dirname(fileURLToPath(import.meta.url))
const hash=bytes=>crypto.createHash('sha256').update(bytes).digest('hex')
const source=path.join(root,'nbt/s5-04-coral-processional-hall.nbt')
const sourceHash=hash(fs.readFileSync(source))
const load=file=>Structure.fromNbt(NbtFile.read(fs.readFileSync(file)).root)
const require=(valid,message)=>{if(!valid)throw Error(message)}
const doors=[{name:'north',anchor:[17,3,0],direction:[0,0,-1],width:7},{name:'south-open-cutaway',anchor:[17,3,42],direction:[0,0,1],width:7},{name:'west',anchor:[0,3,20],direction:[-1,0,0],width:5},{name:'east',anchor:[34,3,20],direction:[1,0,0],width:5}]
const rotations=['NONE','CLOCKWISE_90','CLOCKWISE_180','COUNTERCLOCKWISE_90']
const transform=(point,index)=>index===0?point:index===1?[42-point[2],point[1],point[0]]:index===2?[34-point[0],point[1],42-point[2]]:[point[2],point[1],34-point[0]]
const direction=(point,index)=>index===0?point:index===1?[-point[2],point[1],point[0]]:index===2?[-point[0],point[1],-point[2]]:[point[2],point[1],-point[0]]
const cases=[]
for(const [index,rotation] of rotations.entries()){
  const capture=load(path.join(root,'worldtest',rotation,'after-1200-ticks.nbt'))
  const at=point=>capture.getBlock([point[0]+8,point[1],point[2]+8])
  const wet=point=>{const entry=at(point);return entry?.state.getName().toString()==='minecraft:water'&&entry.state.getProperties().level==='0'}
  const solid=point=>{const name=at(point)?.state.getName().toString();return name&&!['minecraft:water','minecraft:air'].includes(name)}
  const dimensions=index%2?[43,20,35]:[35,20,43]
  let ring=0,cover=0,bed=0
  for(let east=-8;east<=dimensions[0]+7;east++)for(let south=-8;south<=dimensions[2]+7;south++){
    for(let up=0;up<=2;up++){require(solid([east,up,south]),'Solid bed');bed++}
    if(east<0||east>=dimensions[0]||south<0||south>=dimensions[2])for(let up=3;up<20;up++){require(wet([east,up,south]),'Eight-wide source ring');ring++}
    for(let up=20;up<28;up++){require(wet([east,up,south]),'Eight-high source cover');cover++}
  }
  const gates=[]
  for(const door of doors){
    let wetCells=0,sillCells=0
    for(let depth=1;depth<=3;depth++)for(let across=-Math.floor(door.width/2);across<=Math.floor(door.width/2);across++){
      const local=[door.anchor[0]+door.direction[0]*depth+(door.direction[2]?across:0),3,door.anchor[2]+door.direction[2]*depth+(door.direction[0]?across:0)]
      require(solid(transform([local[0],2,local[2]],index)),'Outside door sill');sillCells++
      for(let rise=0;rise<3;rise++){require(wet(transform([local[0],3+rise,local[2]],index)),'Outside door water');wetCells++}
    }
    gates.push({...door,rotated_anchor:transform(door.anchor,index),rotated_direction:direction(door.direction,index),outside_depth:3,wet_cells:wetCells,supported_sill_cells:sillCells})
  }
  cases.push({rotation,dimensions,source_ring_cells:ring,source_cover_cells:cover,solid_bed_cells:bed,gates})
}
const previous=[['01-coral-chamber','s5-01-coral-chamber-redo','9450884a348eda61ea55f0f11a8dae87a0f7d5183312b1528d9a519449f6b9a2'],['02-coral-study','s5-02-coral-study','29fb0a6261847b1817cc5edcbb72aa2824c7e2af1e527bb70d041a7fbd80d552'],['03-coral-lantern-cell','s5-03-coral-lantern-cell','8b037a9e1a1fa594b47ccfbb86f984f117fbd0885ac9cd370a046afe8a42c621']]
const preserved=previous.map(([folder,name,expected])=>{const file=path.join(root,'..',folder,'nbt',name+'.nbt');const actual=hash(fs.readFileSync(file));return {file,sha256:actual,expected,unchanged:actual===expected}})
require(preserved.every(entry=>entry.unchanged),'Prior accepted NBT drift')
fs.writeFileSync(path.join(root,'qa/integration.json'),JSON.stringify({status:'PASS_PRODUCER_CAPTURE_READBACK_PENDING_INDEPENDENT_REVIEW',nbt_sha256:sourceHash,cases,preserved,restriction:'Only sealed full-water integration, solid bed Y0..2, outside water ring8 and overhead source cover8. Extend each door source water and supported sill at least3 outward; never connect dry tunnels. Artificial tank, finite1200tick evidence only.'},null,2))
console.log('PASS four rotations: 8-wide water ring, 8-high cover, continuous bed, four outside wet door extensions; prior accepted hashes unchanged')
