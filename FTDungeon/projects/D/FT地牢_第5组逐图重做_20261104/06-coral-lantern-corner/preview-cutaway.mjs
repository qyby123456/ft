import build from './recipe.mjs'
export default function preview(api) {
  const result = build(api)
  for (const entry of result.structure.getBlocks()) {
    if (entry.state.getName().toString() === 'minecraft:water') api.setBlock(result.structure, entry.pos, 'minecraft:air')
    else if (entry.state.getProperties().waterlogged === 'true') api.setBlock(result.structure, entry.pos, entry.state.getName().toString(), {...entry.state.getProperties(), waterlogged: 'false'}, entry.nbt)
  }
  return {structure: result.structure, metadata: {name: 'preview-only-s5-06-water-hidden-not-importable'}}
}



