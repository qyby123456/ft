import build from './recipe.mjs'

export default function cutaway(api) {
  const { structure } = build(api)
  for (const block of structure.getBlocks()) {
    const [x, y, z] = block.pos
    if (y >= 20 || (y >= 15 && x > 4 && z > 4) || (y >= 3 && (x >= 30 || z >= 30))) api.setBlock(structure, block.pos, 'minecraft:air')
  }
  return { structure, metadata: { name: 'preview-only-cutaway' } }
}
