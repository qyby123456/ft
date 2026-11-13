import build from './recipe.mjs'

export const remove = ([x, y, z]) => {
  const crown = [9, 25].some(center => Math.abs(x - center) <= 3) && [10, 22, 34, 46].some(center => Math.abs(z - center) <= 2)
  return y >= 24 || (y >= 17 && x > 4 && x < 30 && !crown) || (y >= 3 && (z < 2 || x >= 33)) || (y >= 15 && (x >= 30 || z < 4))
}

export default function cutaway(api) {
  const { structure } = build(api)
  for (const block of [...structure.getBlocks()]) if (remove(block.pos)) api.setBlock(structure, block.pos, 'minecraft:air')
  return { structure, metadata: { name: 'preview-only-cutaway' } }
}
