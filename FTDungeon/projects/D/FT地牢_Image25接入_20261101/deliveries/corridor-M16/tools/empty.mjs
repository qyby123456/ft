export default function build(api) {
  const structure = api.createStructure([112, 20, 112])
  api.setBlock(structure, [0, 0, 0], 'minecraft:air')
  return { structure, metadata: { name: 'empty' } }
}
