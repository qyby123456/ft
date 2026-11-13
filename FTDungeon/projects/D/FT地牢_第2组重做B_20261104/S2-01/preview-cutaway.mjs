import buildRoom from './build-room.mjs'

export default function build(api) {
  const result = buildRoom(api)
  api.fill(result.structure, [0, 13, 0], [24, 13, 24], 'minecraft:air')
  api.fill(result.structure, [0, 1, 0], [2, 12, 24], 'minecraft:air')
  api.fill(result.structure, [0, 1, 0], [24, 12, 2], 'minecraft:air')
  return { structure: result.structure, metadata: { name: 'preview-only-not-for-placement' } }
}
