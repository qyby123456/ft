import build from './recipe.mjs'

export const remove = ([x, y, z]) => y >= 13 || (y >= 11 && x > 3 && z < 15) || (y >= 3 && (x >= 19 || z <= 1)) || (y >= 8 && (x >= 17 || z <= 3))

export default function cutaway(api) {
  const { structure } = build(api)
  for (const block of [...structure.getBlocks()]) if (remove(block.pos)) api.setBlock(structure, block.pos, 'minecraft:air')
  return { structure, metadata: { name: 'preview-only-cutaway' } }
}
