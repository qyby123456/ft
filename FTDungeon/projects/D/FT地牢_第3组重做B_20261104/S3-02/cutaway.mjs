import build from './recipe.mjs'

export const remove = ([x, y, z]) => y >= 15 || (y >= 12 && x > 3 && z > 3) || (y >= 3 && (x >= 27 || z >= 25)) || (y >= 9 && (x >= 25 || z >= 23))

export default function cutaway(api) {
  const { structure } = build(api)
  for (const block of [...structure.getBlocks()]) if (remove(block.pos)) api.setBlock(structure, block.pos, 'minecraft:air')
  return { structure, metadata: { name: 'preview-only-cutaway' } }
}
