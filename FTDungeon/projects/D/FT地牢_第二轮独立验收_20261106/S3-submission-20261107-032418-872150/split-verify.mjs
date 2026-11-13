import fs from 'node:fs'
import path from 'node:path'
import crypto from 'node:crypto'
import {fileURLToPath} from 'node:url'
import {Structure,NbtFile} from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'
const root=path.dirname(fileURLToPath(import.meta.url)),folder=path.join(root,'rooms/s3-merged'),file=path.join(folder,'s3-merged.nbt')
const hash=bytes=>crypto.createHash('sha256').update(bytes).digest('hex')
const bytes=fs.readFileSync(file),source=Structure.fromNbt(NbtFile.read(bytes).root),size=source.getSize()
const signature=block=>JSON.stringify([block.state.getName().toString(),Object.entries(block.state.getProperties()).sort(),block.nbt?.toString()??null])
const actual=new Map(source.getBlocks().map(block=>[block.pos.join(','),signature(block)])),merged=new Map(),parts=[]
const output=path.join(root,'parts');fs.mkdirSync(output,{recursive:true})
for(let east=0;east<size[0];east+=48)for(let south=0;south<size[2];south+=48){
  const origin=[east,0,south],dimensions=[Math.min(48,size[0]-east),size[1],Math.min(48,size[2]-south)],piece=new Structure(dimensions)
  for(const block of source.getBlocks())if(block.pos[0]>=east&&block.pos[0]<east+dimensions[0]&&block.pos[2]>=south&&block.pos[2]<south+dimensions[2])piece.addBlock([block.pos[0]-east,block.pos[1],block.pos[2]-south],block.state.getName(),block.state.getProperties(),block.nbt)
  const name=`part-${String(east/48).padStart(2,'0')}-${String(south/48).padStart(2,'0')}.nbt`,destination=path.join(output,name),payload=piece.writeNbt({dataVersion:3465,compression:'gzip'})
  fs.writeFileSync(destination,payload)
  const reread=Structure.fromNbt(NbtFile.read(payload).root)
  for(const block of reread.getBlocks()){
    const global=block.pos.map((value,axis)=>value+origin[axis]).join(',')
    if(merged.has(global))throw Error('Overlapping parts '+global)
    merged.set(global,signature(block))
  }
  parts.push({path:destination,origin,size:dimensions,sha256:hash(payload),records:reread.getBlocks().length})
}
const failures=[]
for(const [position,value] of actual)if(merged.get(position)!==value)failures.push(position)
if(actual.size!==merged.size)failures.push('record count')
const assembly=JSON.parse(fs.readFileSync(path.join(root,'assembly.json'),'utf8')),sourceFailures=[]
for(const module of assembly.modules){
  const moduleBytes=fs.readFileSync(path.join(root,'rooms',module.id,module.id+'.nbt'))
  if(hash(moduleBytes)!==module.sha256)throw Error('Source hash drift '+module.id)
  const sourceModule=Structure.fromNbt(NbtFile.read(moduleBytes).root)
  for(const block of sourceModule.getBlocks()){
    const global=block.pos.map((value,axis)=>value+module.origin[axis]).join(',')
    if(actual.get(global)!==signature(block))sourceFailures.push(module.id+' '+global)
  }
}
let barrierChecked=0
for(let east=0;east<size[0];east++)for(let up=0;up<size[1];up++)for(let south=0;south<size[2];south++)if(east===0||up===0||south===0||east===size[0]-1||up===size[1]-1||south===size[2]-1){
  barrierChecked++
  if(!actual.get([east,up,south].join(','))?.includes('minecraft:barrier'))failures.push('envelope '+[east,up,south])
}
const report={source_path:file,source_sha256:hash(bytes),parts,parts_max_dimension:48,full_records:actual.size,reconstructed_records:merged.size,parts_comparison_includes:'Every actual record position + block identifier + sorted properties + full SNBT block entity payload; absent cells remain absent',parts_failures:failures,source_module_failures:sourceFailures,source_module_count:assembly.modules.length,barrier_envelope_checked:barrierChecked,world_placement:'NOT_RUN'}
fs.writeFileSync(path.join(folder,'qa/split-source-verification.json'),JSON.stringify(report,null,2))
fs.writeFileSync(path.join(root,'parts/manifest.json'),JSON.stringify({source_sha256:hash(bytes),parts},null,2))
if(failures.length||sourceFailures.length)throw Error('Verification failures '+JSON.stringify([failures.slice(0,4),sourceFailures.slice(0,4)]))
console.log(`Verified ${parts.length} pieces / ${actual.size} records / 10 frozen sources / full barrier envelope`)
