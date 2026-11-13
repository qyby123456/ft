import fs from 'node:fs/promises'
import path from 'node:path'
const layout=JSON.parse(await fs.readFile('assembly/layout.json','utf8')),exact=JSON.parse(await fs.readFile('assembly/qa/assembly-exactness.json','utf8'))
const root=path.resolve('deployment-plan'),commands=[],regions=[]
await fs.mkdir(`${root}/data/s8_takeover/functions`,{recursive:true});await fs.mkdir(`${root}/data/s8_takeover/structures/parts`,{recursive:true})
for(let east=0;east<layout.size[0];east+=48)for(let up=0;up<layout.size[1];up+=16)for(let south=0;south<layout.size[2];south+=40){
 const from=[east,up,south],to=[Math.min(east+47,layout.size[0]-1),Math.min(up+15,layout.size[1]-1),Math.min(south+39,layout.size[2]-1)]
 const cells=to.reduce((product,value,axis)=>product*(value-from[axis]+1),1)
 if(cells>32768)throw Error('Fill limit exceeded')
 regions.push({from,to,cells});commands.push(`fill ${from.map(value=>`~${value}`).join(' ')} ${to.map(value=>`~${value}`).join(' ')} minecraft:air replace`)
}
const total=regions.reduce((sum,region)=>sum+region.cells,0)
if(total!==layout.size.reduce((product,value)=>product*value,1))throw Error('Clear volume incomplete')
await fs.writeFile(`${root}/data/s8_takeover/functions/clear.mcfunction`,commands.join('\n')+'\n')
const placement=[]
for(const part of exact.parts){
 const filename=path.basename(part.path);await fs.copyFile(part.path,`${root}/data/s8_takeover/structures/parts/${filename}`)
 placement.push(`place template s8_takeover:parts/${filename.replace('.nbt','')} ${part.origin.map(value=>`~${value}`).join(' ')}`)
}
await fs.writeFile(`${root}/data/s8_takeover/functions/place_parts.mcfunction`,placement.join('\n')+'\n')
await fs.writeFile(`${root}/pack.mcmeta`,JSON.stringify({pack:{pack_format:15,description:'S8 review-only clearing and placement plan; not installed'}},null,2))
await fs.writeFile(`${root}/plan.json`,JSON.stringify({source_sha256:layout.sha256,size:layout.size,fully_cleared_volume:total,regions,parts:exact.parts,execution:'NOT EXECUTED; generated review artifact only',origin_contract:'Run both functions at the identical integer southwest-bottom origin with execute positioned X Y Z run function. Rotation none, mirror none. All rectangular volume including sparse omitted air is cleared before any part placement.',preconditions:['Future user deployment authorization required','Backup target world first; clear bounds intentionally replace all existing blocks','Check build height, loaded chunks and protected regions before executing','Use an unoccupied isolated region; block clearing does not remove existing entities','Do not run inside formal worlds during review'],order:['clear','place_parts']},null,2))
console.log('Clear plan complete',layout.size,total,commands.length,'commands; not executed')
