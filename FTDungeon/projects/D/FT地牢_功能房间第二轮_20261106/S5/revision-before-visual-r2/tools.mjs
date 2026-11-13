import fs from 'node:fs'
import path from 'node:path'
import {fileURLToPath} from 'node:url'
export const root=path.dirname(fileURLToPath(import.meta.url))
export function room(api,size){
  const structure=api.createStructure(size)
  const put=(pos,block,properties,nbt)=>api.setBlock(structure,pos,block.includes(':')?block:`minecraft:${block}`,properties,nbt)
  const fill=(from,to,block,properties)=>api.fill(structure,from,to,block.includes(':')?block:`minecraft:${block}`,properties)
  fill([0,0,0],size.map(value=>value-1),'air')
  const box=(from,to,block)=>api.hollowBox(structure,from,to,`minecraft:${block}`)
  const container=(pos,block='barrel',facing='north')=>put(pos,block,{facing,...(block==='barrel'?{open:'false'}:block==='chest'?{type:'single',waterlogged:'false'}:{lit:'false'})},api.createBlockNbt({id:`minecraft:${block}`,Items:new api.lodestone.NbtList([],10)}))
  const coral=(pos,color='brain')=>{put(pos,`${color}_coral_block`);put([pos[0],pos[1]+1,pos[2]],`${color}_coral`,{waterlogged:'true'})}
  const finish=(id,description,ports,targets,differences,extra={})=>{
    const directory=path.join(root,id);fs.mkdirSync(directory,{recursive:true})
    fs.writeFileSync(path.join(directory,'design.json'),JSON.stringify({id,size,description,ports,targets,differences,water_policy:'Shallow contained display pools, raised dry traversable ports; no ocean placement survival claim.',...extra},null,2))
    return {structure,metadata:{name:id.toLowerCase()}}
  }
  return {structure,put,fill,box,container,coral,finish}
}
