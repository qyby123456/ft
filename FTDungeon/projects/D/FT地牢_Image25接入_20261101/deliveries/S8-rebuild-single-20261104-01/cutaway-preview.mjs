import build from './recipe-r2.mjs'

export default function preview(api) {
  const result = build(api)
  api.fill(result.structure, [0, 20, 0], [34, 20, 34], 'minecraft:air')
  api.fill(result.structure, [0, 3, 0], [34, 19, 1], 'minecraft:air')
  api.fill(result.structure, [4, 19, 4], [30, 19, 6], 'minecraft:air')
  api.fill(result.structure, [0, 3, 0], [1, 19, 34], 'minecraft:air')
  api.fill(result.structure, [4, 19, 4], [6, 19, 30], 'minecraft:air')
  return { structure: result.structure, metadata: { name: 'inspection-cutaway-not-deliverable' } }
}
