import build from './recipe.mjs'

export const remove = ([east, height, south]) => height >= 13 || (height >= 11 && east >= 5 && east <= 15) || (height >= 3 && (east >= 19 || south < 2))

export default function cutaway(api) {
  const { structure } = build(api)
  for (const block of [...structure.getBlocks()]) if (remove(block.pos)) api.setBlock(structure, block.pos, 'minecraft:air')
  return { structure, metadata: { name: 'preview-only-cutaway' } }
}
