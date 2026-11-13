import buildRoom from './recipe.mjs'

export default function build(api) {
  const result = buildRoom(api)
  api.fill(result.structure, [0, 16, 6], [24, 17, 30], 'minecraft:air')
  api.fill(result.structure, [0, 4, 6], [3, 15, 30], 'minecraft:air')
  api.fill(result.structure, [0, 4, 6], [24, 15, 9], 'minecraft:air')
  return { structure: result.structure, metadata: { name: 'preview-only' } }
}
