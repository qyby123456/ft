export default function build(api) {
  const structure = api.createStructure([21, 11, 21])
  const put = (position, block, properties, nbt) => api.setBlock(structure, position, 'minecraft:' + block, properties, nbt)
  const fill = (from, to, block, properties) => api.fill(structure, from, to, 'minecraft:' + block, properties)
  fill([0, 0, 0], [20, 10, 20], 'air')
  fill([0, 0, 0], [20, 0, 20], 'deepslate_bricks')
  for (let across = 0; across <= 20; across++) {
    for (let depth = 0; depth <= 20; depth++) {
      const stain = (across * 19 + depth * 31) % 23
      const floor = across >= 9 && across <= 11 ? 'stone_bricks' : stain < 2 ? 'mossy_stone_bricks' : stain < 4 ? 'cracked_stone_bricks' : stain === 5 ? 'rooted_dirt' : 'stone_bricks'
      put([across, 1, depth], floor)
      put([across, 10, depth], 'stone_bricks')
      if (across === 0 || across === 20 || depth === 0 || depth === 20) {
        for (let height = 2; height <= 9; height++) {
          const wall = height === 9 ? 'spruce_planks' : (across * 13 + depth * 17 + height * 11) % 17 < 3 ? 'mossy_stone_bricks' : (across + depth + height) % 11 === 0 ? 'cracked_stone_bricks' : 'stone_bricks'
          put([across, height, depth], wall)
        }
      }
    }
  }
  for (const depth of [5, 13, 18]) {
    for (const across of [5, 15]) {
      fill([across, 2, depth], [across, 9, depth], 'stone_bricks')
      put([across, 2, depth], 'polished_deepslate')
      for (const height of [3, 6, 8]) put([across, height, depth], 'spruce_wood', { axis: 'y' })
    }
    fill([1, 9, depth], [19, 9, depth], 'spruce_log', { axis: 'x' })
  }
  for (const across of [5, 15]) fill([across, 9, 1], [across, 9, 19], 'spruce_log', { axis: 'z' })
  const emptyContainer = (position, block, facing) => {
    const properties = block === 'chest' ? { facing, type: 'single', waterlogged: 'false' } : { facing, open: 'false' }
    put(position, block, properties, api.createBlockNbt({ id: 'minecraft:' + block, Items: [] }))
  }
  for (const across of [1, 19]) {
    const front = across === 1 ? 2 : 18
    const facing = across === 1 ? 'east' : 'west'
    fill([across, 2, 3], [front, 2, 16], 'spruce_planks')
    for (const depth of [3, 7, 11, 16]) fill([front, 3, depth], [front, 8, depth], 'spruce_log', { axis: 'y' })
    for (const height of [3, 5, 7]) fill([across, height, 3], [across, height, 16], 'spruce_slab', { type: 'top', waterlogged: 'false' })
    for (const depth of [4, 8, 12]) {
      emptyContainer([across, 4, depth], 'barrel', facing)
      emptyContainer([across, 6, depth + 1], 'barrel', facing)
      for (const height of [4, 6]) put([across, height, depth + 2], 'flower_pot')
    }
    fill([across, 8, 3], [front, 8, 16], 'spruce_slab', { type: 'top', waterlogged: 'false' })
  }
  for (const [start, end] of [[3, 7], [13, 17]]) {
    fill([start, 2, 19], [end, 2, 19], 'spruce_planks')
    for (const across of [start, end]) fill([across, 3, 19], [across, 7, 19], 'spruce_log', { axis: 'y' })
    for (const height of [3, 5, 7]) fill([start + 1, height, 19], [end - 1, height, 19], 'spruce_slab', { type: 'top', waterlogged: 'false' })
    emptyContainer([start + 1, 4, 19], 'barrel', 'north')
    put([end - 1, 6, 19], 'flower_pot')
  }
  for (const position of [[4, 2, 3], [16, 2, 3], [6, 2, 17], [14, 2, 17]]) emptyContainer(position, 'chest', 'north')
  for (const position of [[3, 2, 14], [3, 3, 14], [17, 2, 14], [17, 3, 14]]) emptyContainer(position, 'barrel', position[0] < 10 ? 'east' : 'west')
  emptyContainer([17, 2, 6], 'barrel', 'south')
  for (const [across, depth] of [[4, 9], [16, 9], [4, 16]]) {
    put([across, 2, depth], 'spruce_planks')
    put([across, 3, depth], 'spruce_trapdoor', { facing: 'north', half: 'bottom', open: 'false', powered: 'false', waterlogged: 'false' })
  }
  for (const [across, depth] of [[7, 12], [7, 13], [8, 13]]) put([across, 1, depth], 'oxidized_copper')
  for (const [across, depth] of [[6, 9], [7, 17], [17, 10], [3, 6]]) put([across, 2, depth], 'cobblestone_slab', { type: 'bottom', waterlogged: 'false' })
  for (const position of [[1, 7, 2], [19, 7, 2], [3, 8, 19], [17, 8, 19]]) put(position, 'cobweb')
  for (const depth of [5, 13]) {
    put([10, 8, depth], 'chain', { axis: 'y', waterlogged: 'false' })
    put([10, 7, depth], 'lantern', { hanging: 'true', waterlogged: 'false' })
  }
  put([16, 2, 10], 'lantern', { hanging: 'false', waterlogged: 'false' })
  for (const across of [8, 12]) fill([across, 2, 0], [across, 6, 0], 'polished_andesite')
  fill([8, 6, 0], [12, 6, 0], 'chiseled_stone_bricks')
  fill([9, 2, 0], [11, 5, 0], 'air')
  fill([9, 2, 1], [11, 5, 1], 'air')
  fill([9, 1, 0], [11, 1, 19], 'stone_bricks')
  return { structure, metadata: { name: 'room', id: 'zone-A05', title: '废弃储藏室', entrance: [10, 2, 0], interior: [10, 2, 4], facing: 'north' } }
}
