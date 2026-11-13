import fs from 'node:fs'
import path from 'node:path'
import crypto from 'node:crypto'
import {fileURLToPath} from 'node:url'
import {NbtFile,Structure} from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'
const root=path.dirname(fileURLToPath(import.meta.url))
const hash=bytes=>crypto.createHash('sha256').update(bytes).digest('hex')
const load=file=>Structure.fromNbt(NbtFile.read(fs.readFileSync(file)).root)
const signature=entry=>JSON.stringify([entry.state.getName().toString(),Object.entries(entry.state.getProperties()).sort(),entry.nbt?.toJson()??null])
const sourceFile=path.join(root,'nbt/s5-10-coral-ocean-complex.nbt')
const source=load(sourceFile)
const destination=path.join(root,'datapack/data/ft_s5_redo/structures')
fs.mkdirSync(destination,{recursive:true})
fs.writeFileSync(path.join(root,'datapack/pack.mcmeta'),JSON.stringify({pack:{pack_format:15,description:'S5-10 coral ocean complex. Full-water integration only. NOT DEPLOYED.'}},null,2))
fs.copyFileSync(sourceFile,path.join(destination,'s5_10_coral_ocean_complex_full.nbt'))
const shards=[]
const reconstructed=new Map()
for(const [ordinal,[east,south,width,depth]] of [[0,0,33,33],[33,0,32,33],[0,33,33,32],[33,33,32,32]].entries()){
  const part=new Structure([width,22,depth])
  for(const entry of source.getBlocks())if(entry.pos[0]>=east&&entry.pos[0]<east+width&&entry.pos[2]>=south&&entry.pos[2]<south+depth)part.addBlock([entry.pos[0]-east,entry.pos[1],entry.pos[2]-south],entry.state.getName(),entry.state.getProperties(),entry.nbt)
  const name=`s5_10_part_${ordinal+1}`
  const file=path.join(destination,name+'.nbt')
  fs.writeFileSync(file,part.writeNbt({dataVersion:3465,compression:'gzip',updateBlockStates:false}))
  const decoded=load(file)
  if(decoded.getSize().some(dimension=>dimension>48))throw Error('Oversize shard')
  for(const entry of decoded.getBlocks()){
    const position=[entry.pos[0]+east,entry.pos[1],entry.pos[2]+south].join(',')
    if(reconstructed.has(position))throw Error('Overlapping shards')
    reconstructed.set(position,signature(entry))
  }
  shards.push({order:ordinal+1,id:`ft_s5_redo:${name}`,origin:[east,0,south],size:decoded.getSize(),records:decoded.getBlocks().length,sha256:hash(fs.readFileSync(file)),command_at_fixed_anchor:`execute positioned <X> <Y> <Z> run place template ft_s5_redo:${name} ~${east} ~0 ~${south}`})
}
if(reconstructed.size!==92950)throw Error('Missing shard positions')
for(const entry of source.getBlocks())if(reconstructed.get(entry.pos.join(','))!==signature(entry))throw Error('Shard state/entity mismatch at '+entry.pos)
fs.writeFileSync(path.join(root,'qa/shards.json'),JSON.stringify({status:'PASS_EXACT_STATE_AND_BLOCK_ENTITY_REASSEMBLY',source_sha256:hash(fs.readFileSync(sourceFile)),source_size:source.getSize(),source_records:92950,shards,overlap_cells:0,missing_cells:0,state_or_block_entity_mismatches:0,policy:'Fixed unrotated common anchor, order1..4. Complete disjoint explicit rectangular volumes, including source WATER and bed, no air or structure_void. They overwrite all existing blocks in their own cuboid. No ignore-air mode; do not place the geometry-only preview. Never update states inside isolated shards before assembly. Native four-rotation world tests use full source, shard runtime placement not executed.'},null,2))
console.log('PASS four <=48 shards: exact 92950 source state/entity reassembly')
