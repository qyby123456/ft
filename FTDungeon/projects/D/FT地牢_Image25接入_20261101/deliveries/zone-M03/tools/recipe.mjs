export default function build(api) {
  const size = [23, 12, 25]
  const structure = api.createStructure(size)
  const put = (position, block, properties, nbt) => api.setBlock(structure, position, block, properties, nbt)
  const box = (start, end, block, properties) => api.fill(structure, start, end, block, properties)
  const rock = ['stone', 'stone', 'andesite', 'tuff', 'cobblestone', 'stone']
  const frames = [3, 9, 15, 21]
  box([0, 0, 0], [22, 11, 24], 'minecraft:air')
  for (let across = 0; across < size[0]; across++) {
    for (let depth = 0; depth < size[2]; depth++) {
      const random = (across * 37 + depth * 19 + across * depth * 7) % 29
      put([across, 0, depth], 'minecraft:' + rock[random % rock.length])
      for (let elevation = 1; elevation < size[1]; elevation++) {
        if (across <= 1 || across >= 21 || depth <= 1 || depth >= 23 || elevation >= 10) {
          put([across, elevation, depth], 'minecraft:' + rock[(random + elevation * 11) % rock.length])
        }
      }
    }
  }
  for (const depth of [0, 23]) {
    box([9, 1, depth], [13, 4, depth + 1], 'minecraft:air')
    for (const across of [8, 14]) box([across, 1, depth], [across, 5, depth + 1], 'minecraft:stone_bricks')
    box([8, 5, depth], [14, 5, depth + 1], 'minecraft:spruce_log', { axis: 'x' })
  }
  box([9, 0, 0], [13, 0, 24], 'minecraft:andesite')
  box([10, 0, 0], [12, 0, 24], 'minecraft:polished_andesite')
  for (const depth of frames) {
    for (const across of [3, 19]) {
      put([across, 1, depth], 'minecraft:stone_bricks')
      box([across, 2, depth], [across, 7, depth], 'minecraft:spruce_log', { axis: 'y' })
      put([across === 3 ? 4 : 18, 7, depth], 'minecraft:spruce_stairs', { facing: across === 3 ? 'west' : 'east', half: 'top', shape: 'straight', waterlogged: 'false' })
    }
    box([3, 8, depth], [19, 8, depth], 'minecraft:spruce_log', { axis: 'x' })
    for (const across of [9, 17]) {
      put([across, 7, depth], 'minecraft:chain', { axis: 'y', waterlogged: 'false' })
      put([across, 6, depth], 'minecraft:lantern', { hanging: 'true', waterlogged: 'false' })
    }
  }
  for (const across of [3, 19]) box([across, 9, 2], [across, 9, 22], 'minecraft:spruce_log', { axis: 'z' })
  for (const depth of [9, 21]) {
    put([8, 1, depth], 'minecraft:stone_bricks')
    box([8, 2, depth], [8, 7, depth], 'minecraft:spruce_log', { axis: 'y' })
  }
  box([8, 8, 9], [8, 8, 21], 'minecraft:spruce_log', { axis: 'z' })
  for (let across = 2; across <= 7; across++) {
    for (let depth = 13; depth <= 22; depth++) {
      if (across === 3 && frames.includes(depth)) continue
      const distance = Math.abs(across - 3) * 0.65 + (22 - depth) * 0.48
      const height = Math.max(1, 6 - Math.floor(distance))
      for (let elevation = 1; elevation <= height; elevation++) put([across, elevation, depth], 'minecraft:' + ['cobblestone', 'andesite', 'tuff'][(across + depth + elevation) % 3])
    }
  }
  for (let step = 0; step < 4; step++) {
    const across = 3 + step
    const elevation = 5 - step
    box([across, 1, 17], [across, elevation - 1, 17], 'minecraft:cobblestone')
    put([across, elevation, 17], 'minecraft:spruce_log', { axis: 'x' })
    if (step < 3) put([across + 1, elevation, 17], 'minecraft:cobblestone')
  }
  box([5, 1, 12], [5, 1, 14], 'minecraft:cobblestone')
  box([5, 2, 12], [5, 2, 14], 'minecraft:spruce_log', { axis: 'z' })
  for (const depth of [10, 11, 12, 13, 14, 16, 17, 18, 19, 20]) put([8, 1, depth], 'minecraft:spruce_fence', { north: 'false', south: 'false', east: 'false', west: 'false', waterlogged: 'false' })
  for (let depth = 3; depth <= 10; depth++) put([5, 1, depth], 'minecraft:rail', { shape: 'north_south', waterlogged: 'false' })
  put([5, 1, 11], 'minecraft:cauldron')
  for (const position of [[2, 1, 5], [2, 1, 6], [18, 1, 5], [18, 1, 6], [18, 2, 5], [18, 1, 20]]) put(position, 'minecraft:barrel', { facing: 'up', open: 'false' }, api.createBlockNbt({ id: 'minecraft:barrel', Items: [] }))
  put([18, 1, 19], 'minecraft:crafting_table')
  for (const position of [[6, 1, 12], [7, 1, 10], [6, 1, 11], [16, 1, 22]]) put(position, 'minecraft:cobblestone_slab', { type: 'bottom', waterlogged: 'false' })
  for (const position of [[1, 3, 4], [1, 4, 5], [1, 3, 17], [21, 3, 7], [21, 4, 8], [21, 2, 20], [5, 3, 23]]) put(position, 'minecraft:coal_ore')
  for (const position of [[1, 2, 8], [21, 2, 14], [21, 3, 14], [6, 2, 23]]) put(position, 'minecraft:iron_ore')
  return { structure, metadata: { name: 'zone-m03-collapsed-mine' } }
}
