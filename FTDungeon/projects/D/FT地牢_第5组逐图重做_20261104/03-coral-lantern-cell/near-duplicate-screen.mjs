import fs from 'node:fs'
import path from 'node:path'
import crypto from 'node:crypto'
import {fileURLToPath} from 'node:url'
import {NbtFile,Structure} from 'file:///C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/node_modules/@mattzh72/lodestone/lib/index.js'
const root=path.dirname(fileURLToPath(import.meta.url))
const hash=bytes=>crypto.createHash('sha256').update(bytes).digest('hex')
function read(file){
  const bytes=fs.readFileSync(file)
  const structure=Structure.fromNbt(NbtFile.read(bytes).root)
  const size=structure.getSize()
  const solids=structure.getBlocks().filter(entry=>!['minecraft:air','minecraft:water','minecraft:lava','minecraft:structure_void'].includes(entry.state.getName().toString())).map(entry=>entry.pos).filter(pos=>pos.every((coordinate,index)=>coordinate>=2&&coordinate<size[index]-2))
  const grids=[]
  for(let rotation=0;rotation<4;rotation++){
    grids.push(new Set(solids.map(pos=>{
      let east=(pos[0]-2)/(size[0]-4),south=(pos[2]-2)/(size[2]-4)
      for(let turn=0;turn<rotation;turn++)[east,south]=[1-south,east]
      return [east,(pos[1]-2)/(size[1]-4),south].map(value=>Math.min(15,Math.floor(value*16))).join(',')
    })))
  }
  return {file,sha256:hash(bytes),size,grids}
}
const audit=JSON.parse(fs.readFileSync('D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/file-audit.json','utf8'))
const current=read(path.join(root,'nbt/s5-03-coral-lantern-cell.nbt'))
const results=[]
for(const baseline of [...audit.groups,{group:'5-image2',candidate:'D:/ft工程文件/FT地牢_第5组逐图重做_20261104/02-coral-study/nbt/s5-02-coral-study.nbt',sha256:'29fb0a6261847b1817cc5edcbb72aa2824c7e2af1e527bb70d041a7fbd80d552'}]){
  if(!fs.existsSync(baseline.candidate))continue
  const other=read(baseline.candidate)
  let maximum=0
  for(const grid of other.grids){
    let intersection=0
    for(const cell of current.grids[0])if(grid.has(cell))intersection++
    maximum=Math.max(maximum,intersection/(current.grids[0].size+grid.size-intersection))
  }
  results.push({group:baseline.group,file:other.file,sha256:other.sha256,baseline_audit_sha256:baseline.sha256,changed_since_baseline:baseline.sha256!==other.sha256,size:other.size,best_four_rotation_jaccard:maximum})
}
fs.writeFileSync(path.join(root,'qa/near-duplicate-screen.json'),JSON.stringify({status:'SCREENING_ONLY_NOT_ACCEPTANCE',candidate_sha256:current.sha256,method:'Material-independent interior occupied cells: trim two blocks at all bounds, normalize to16x16x16, compare four yaw rotations. Paths read from prior independent audit; current hashes recorded, no other group files modified or promoted.',results,limit:'Coarse geometry screening is not route/semantic uniqueness certification; statuses of other candidates are not inferred.'},null,2))
console.log(results.map(result=>`group${result.group}: ${result.best_four_rotation_jaccard.toFixed(4)}`).join('\n'))
