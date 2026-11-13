import build,{areas,contains}from './recipe.mjs'

export const remove=([east,height,south])=>{
  const covered=areas.filter(area=>area.ceiling!==null&&contains(area,east,south))
  const ceiling=covered.length?Math.max(...covered.map(area=>area.ceiling)):null
  return (ceiling!==null&&height>=ceiling)||(height>=12&&areas.some(area=>area.role!=='corridor'&&contains(area,east,south)&&!contains(area,east,south,2)))
}

export default function cutaway(api){
  const {structure}=build(api)
  for(const block of [...structure.getBlocks()])if(remove(block.pos))api.setBlock(structure,block.pos,'minecraft:air')
  return{structure,metadata:{name:'preview-only-cutaway'}}
}
