import fs from 'node:fs'
import path from 'node:path'
import {fileURLToPath} from 'node:url'
import {createHash} from 'node:crypto'
import assert from 'node:assert/strict'
import {NbtFile,Structure} from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'
const root=path.join(path.dirname(fileURLToPath(import.meta.url)),'assembly')
const summary=JSON.parse(fs.readFileSync(path.join(root,'summary.json')))
const bytes=fs.readFileSync(summary.nbtPath)
const hash=buffer=>createHash('sha256').update(buffer).digest('hex')
const file=NbtFile.read(bytes)
assert.equal(file.root.getList('entities').length,0,'Entity payload requires explicit implementation')
const source=Structure.fromNbt(file.root)
const encoded=block=>JSON.stringify([block.state.getName().toString(),Object.entries(block.state.getProperties()).sort(),block.nbt?.toJson()??null])
const cells=new Map(source.getBlocks().map(block=>[block.pos.join(','),encoded(block)]))
const layout=JSON.parse(fs.readFileSync(path.join(root,'layout.json')))
let corridorClearanceSamples=0
for(const corridor of layout.corridors) {
  const alongEast=corridor.from[0]!==corridor.to[0]
  for(const point of corridor.points) for(let across=-1;across<=1;across++) for(let rise=0;rise<4;rise++) {
    const position=[point[0]+(alongEast?0:across),4+rise,point[2]+(alongEast?across:0)]
    assert.equal(source.getBlock(position)?.state.getName().toString(),'minecraft:air','Corridor clearance '+corridor.label+' '+position)
    corridorClearanceSamples++
  }
}
const moduleReports=[]
for(const module of layout.modules) {
  const originalBytes=fs.readFileSync(module.source)
  assert.equal(hash(originalBytes),module.source_sha256)
  const original=Structure.fromNbt(NbtFile.read(originalBytes).root)
  let changed=0,payloads=0
  for(const block of original.getBlocks()) {
    if(block.nbt) payloads++
    const position=block.pos.map((value,axis)=>value+module.origin[axis])
    if(cells.get(position.join(','))!==encoded(block)) {
      const closure=layout.unused_ports_closed.find(closed=>closed.module===module.id)
      assert(closure&&position[0]===closure.port.feet[0]&&Math.abs(position[2]-closure.port.feet[2])<=1&&position[1]>=4&&position[1]<8,'Unexpected source modification '+module.id+' '+position)
      assert.equal(source.getBlock(position).state.getName().toString(),'minecraft:orange_stained_glass')
      assert.equal(block.state.getName().toString(),'minecraft:air')
      changed++
    }
  }
  moduleReports.push({id:module.id,sha256:module.source_sha256,records:original.getBlocks().length,block_entities_preserved:payloads,authorized_outer_port_closure_cells:changed,unexpected_changes:0})
}
const envelope=new Set()
for(const module of layout.modules) for(let east=0;east<module.size[0];east++) for(let up=0;up<module.size[1];up++) for(let south=0;south<module.size[2];south++) envelope.add([east+module.origin[0],up+module.origin[1],south+module.origin[2]].join(','))
for(const corridor of layout.corridors) for(const point of corridor.points) for(let east=-2;east<=2;east++) for(let south=-2;south<=2;south++) for(let up=1;up<=9;up++) if(point[0]+east>=1) envelope.add([point[0]+east,up,point[2]+south].join(','))
const expectedBarrier=new Set()
for(const key of envelope) {
  const position=key.split(',').map(Number)
  for(const delta of [[1,0,0],[-1,0,0],[0,1,0],[0,-1,0],[0,0,1],[0,0,-1]]) {
    const neighbor=position.map((value,axis)=>value+delta[axis]).join(',')
    if(!envelope.has(neighbor)) expectedBarrier.add(neighbor)
  }
}
const actualBarriers=source.getBlocks().filter(block=>block.state.getName().toString()==='minecraft:barrier')
assert.equal(actualBarriers.length,expectedBarrier.size)
for(const block of actualBarriers) assert(expectedBarrier.has(block.pos.join(',')))
const directory=path.join(root,'slices')
fs.mkdirSync(directory,{recursive:true})
const pieces=[]
const reconstructed=new Map()
for(let east=0;east<summary.size[0];east+=48) for(let up=0;up<summary.size[1];up+=48) for(let south=0;south<summary.size[2];south+=48) {
  const origin=[east,up,south]
  const size=summary.size.map((value,axis)=>Math.min(48,value-origin[axis]))
  const piece=new Structure(size)
  for(const block of source.getBlocks()) if(block.pos.every((value,axis)=>value>=origin[axis]&&value<origin[axis]+size[axis])) piece.addBlock(block.pos.map((value,axis)=>value-origin[axis]),block.state.getName(),block.state.getProperties(),block.nbt)
  const pieceBytes=piece.writeNbt({dataVersion:3465,compression:'gzip'})
  const filename=`s6-${east}-${up}-${south}.nbt`
  fs.writeFileSync(path.join(directory,filename),pieceBytes)
  const decoded=Structure.fromNbt(NbtFile.read(pieceBytes).root)
  let payloads=0
  for(const block of decoded.getBlocks()) {
    if(block.nbt) payloads++
    const global=block.pos.map((value,axis)=>value+origin[axis]).join(',')
    assert(!reconstructed.has(global),'Overlapping slices')
    reconstructed.set(global,encoded(block))
  }
  pieces.push({file:path.join(directory,filename),origin,size,records:decoded.getBlocks().length,block_entities:payloads,sha256:hash(pieceBytes)})
}
assert.equal(reconstructed.size,cells.size)
for(const [position,value] of cells) assert.equal(reconstructed.get(position),value,'Slice drift '+position)
const report={source_sha256:hash(bytes),size:summary.size,data_version:3465,slice_limit:48,pieces,module_preservation:moduleReports,source_records:cells.size,reassembled_records:reconstructed.size,block_states_and_payloads_equal:true,source_structure_entities:0,corridor_cross_sections:{width:3,clear_height:4,air_samples:corridorClearanceSamples,pass:true},barrier_union_shell:{expected:expectedBarrier.size,actual:actualBarriers.length,internal_barriers:0,pass:true},independent_acceptance:false}
fs.writeFileSync(path.join(root,'qa/split-payload-verification.json'),JSON.stringify(report,null,2))
console.log(JSON.stringify({slices:pieces.length,records:cells.size,payloads:pieces.reduce((sum,piece)=>sum+piece.block_entities,0),barriers:expectedBarrier.size,status:'self-check passed'}))
