import build from './recipe.mjs'

export const remove=([east,height,south])=>height>=23||(height>=8&&south>=34)||(height>=15&&east>=34)

export default function cutaway(api){
  const {structure}=build(api)
  for(const block of [...structure.getBlocks()])if(remove(block.pos))api.setBlock(structure,block.pos,'minecraft:air')
  return {structure,metadata:{name:'preview-only-cutaway'}}
}
