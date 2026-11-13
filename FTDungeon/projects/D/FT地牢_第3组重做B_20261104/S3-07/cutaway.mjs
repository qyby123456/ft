import build from './recipe.mjs'

export const remove = ([east, height, south]) => (height >= 18 && south <= 8 && east >= 10 && east <= 23) || (height >= 8 && east > 29) || (height >= 8 && east < 3)

export default function cutaway(api) {
  const { structure } = build(api)
  for (const block of [...structure.getBlocks()]) if (remove(block.pos)) api.setBlock(structure, block.pos, 'minecraft:air')
  return { structure, metadata: { name: 'preview-only-cutaway' } }
}
