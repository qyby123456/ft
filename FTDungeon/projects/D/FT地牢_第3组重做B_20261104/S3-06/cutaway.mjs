import build from './recipe.mjs'

export const remove = ([east, height, south]) => height >= 16 || (height >= 14 && ((east >= 4 && east <= 12) || south >= 22)) || (height >= 5 && (east < 2 || south > 32))

export default function cutaway(api) {
  const { structure } = build(api)
  for (const block of [...structure.getBlocks()]) if (remove(block.pos)) api.setBlock(structure, block.pos, 'minecraft:air')
  return { structure, metadata: { name: 'preview-only-cutaway' } }
}
