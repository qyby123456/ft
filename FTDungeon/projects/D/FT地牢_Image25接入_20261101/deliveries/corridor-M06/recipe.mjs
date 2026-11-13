export default function build(api) {
  const size = [15, 13, 40]
  const structure = api.createStructure(size)
  const put = (position, block, properties) => api.setBlock(structure, position, block, properties)
  const fill = (from, to, block, properties) => api.fill(structure, from, to, block, properties)
  const stone = ['stone_bricks', 'stone_bricks', 'cracked_stone_bricks', 'andesite', 'polished_andesite']
  fill([0, 0, 0], [14, 12, 39], 'minecraft:air')
  fill([0, 0, 0], [14, 0, 39], 'minecraft:deepslate_bricks')
  for (let depth = 0; depth < 40; depth++) {
    for (let across = 0; across < 15; across++) {
      put([across, 1, depth], `minecraft:${stone[(across * 3 + depth * 7) % stone.length]}`)
      put([across, 12, depth], 'minecraft:stone_bricks')
    }
    for (const across of [0, 14]) {
      for (let height = 2; height < 12; height++) {
        put([across, height, depth], `minecraft:${stone[(height + depth * 3) % stone.length]}`)
      }
    }
  }
  fill([1, 11, 0], [13, 11, 39], 'minecraft:cracked_stone_bricks')
  for (const edge of [0, 39]) {
    fill([1, 2, edge], [13, 10, edge], 'minecraft:stone_bricks')
    fill([5, 2, edge], [9, 6, edge], 'minecraft:air')
    fill([4, 2, edge], [4, 8, edge], 'minecraft:chiseled_stone_bricks')
    fill([10, 2, edge], [10, 8, edge], 'minecraft:chiseled_stone_bricks')
    fill([5, 7, edge], [9, 7, edge], 'minecraft:polished_andesite')
    put([5, 6, edge], 'minecraft:stone_brick_stairs', { facing: 'west', half: 'top', shape: 'straight', waterlogged: 'false' })
    put([9, 6, edge], 'minecraft:stone_brick_stairs', { facing: 'east', half: 'top', shape: 'straight', waterlogged: 'false' })
  }
  for (let depth = 1; depth < 39; depth++) {
    for (const left of [true, false]) {
      const rear = left ? 1 : 13
      const ice = left ? 3 : 11
      const front = left ? 4 : 10
      fill([rear, 2, depth], [rear, 10, depth], 'minecraft:polished_basalt', { axis: 'y' })
      fill([ice, 2, depth], [ice, 7, depth], 'minecraft:packed_ice')
      fill([front, 2, depth], [front, 2, depth], 'minecraft:stone_bricks')
      fill([front, 3, depth], [front, 6, depth], 'minecraft:iron_bars', { waterlogged: 'false' })
      fill([front, 7, depth], [front, 8, depth], 'minecraft:polished_andesite')
      if ((depth + (left ? 0 : 2)) % 7 === 0) {
        fill([ice, 3, depth], [ice, 6, depth], 'minecraft:blue_ice')
      }
      if (depth % 6 === 0) {
        fill([front, 3, depth], [front, 6, depth], 'minecraft:packed_ice')
      }
    }
    if (depth % 4 === 2) {
      put([5, 1, depth], 'minecraft:packed_ice')
      put([9, 1, 39 - depth], 'minecraft:packed_ice')
    }
  }
  for (const depth of [1, 9, 17, 25, 33, 38]) {
    for (const across of [4, 10]) {
      fill([across, 2, depth], [across, 10, depth], 'minecraft:stone_bricks')
      put([across, 2, depth], 'minecraft:chiseled_stone_bricks')
      put([across, 8, depth], 'minecraft:chiseled_stone_bricks')
    }
    fill([4, 10, depth], [10, 10, depth], 'minecraft:polished_andesite')
    put([5, 9, depth], 'minecraft:stone_brick_stairs', { facing: 'west', half: 'top', shape: 'straight', waterlogged: 'false' })
    put([9, 9, depth], 'minecraft:stone_brick_stairs', { facing: 'east', half: 'top', shape: 'straight', waterlogged: 'false' })
  }
  for (const depth of [2, 10, 22, 34, 37]) {
    for (const across of [5, 9]) {
      put([across, 2, depth], 'minecraft:polished_andesite')
      put([across, 3, depth], 'minecraft:candle', { candles: across === 5 ? '3' : '4', lit: 'true', waterlogged: 'false' })
    }
  }
  for (const depth of [5, 15, 27]) {
    for (const center of [5, 9]) {
      fill([center - 1, 6, depth - 1], [center + 1, 6, depth + 1], 'minecraft:deepslate_tile_slab', { type: 'top', waterlogged: 'false' })
      fill([center - 1, 9, depth - 1], [center + 1, 9, depth + 1], 'minecraft:deepslate_tile_slab', { type: 'double', waterlogged: 'false' })
      for (let across = center - 1; across <= center + 1; across++) {
        for (let cageDepth = depth - 1; cageDepth <= depth + 1; cageDepth++) {
          if (across === center && cageDepth === depth) continue
          fill([across, 7, cageDepth], [across, 8, cageDepth], 'minecraft:iron_bars', { waterlogged: 'false' })
        }
      }
      fill([center, 10, depth], [center, 10, depth], 'minecraft:chain', { axis: 'y', waterlogged: 'false' })
    }
  }
  for (const [across, depth] of [[4, 4], [10, 12], [4, 23], [10, 31]]) {
    for (let offset = -1; offset <= 1; offset++) {
      fill([across, 2, depth + offset], [across, 5 + (offset === 0 ? 1 : 0), depth + offset], 'minecraft:blackstone')
    }
    const inside = across === 4 ? 3 : 11
    put([inside, 5, depth], 'minecraft:shroomlight')
    put([across, 5, depth], 'minecraft:iron_bars', { waterlogged: 'false' })
  }
  for (const depth of [6, 14, 26, 35]) {
    for (const across of [4, 5, 9, 10]) {
      if (across === 5 && depth === 6) continue
      put([across, 1, depth], 'minecraft:blackstone')
      put([across, 11, depth], 'minecraft:blackstone')
      put([across, 11, depth + 1], 'minecraft:coal_block')
    }
  }
  const connects = position => {
    const neighbor = structure.getBlock(position)
    if (!neighbor) return false
    const name = neighbor.state.getName().toString()
    if (name === 'minecraft:iron_bars') return true
    if (['minecraft:air', 'minecraft:candle', 'minecraft:chain'].includes(name) || name.endsWith('_stairs')) return false
    return !name.endsWith('_slab') || neighbor.state.getProperties().type === 'double'
  }
  const directions = { north: [0, 0, -1], east: [1, 0, 0], south: [0, 0, 1], west: [-1, 0, 0] }
  for (const block of structure.getBlocks()) {
    if (block.state.getName().toString() !== 'minecraft:iron_bars') continue
    const properties = { waterlogged: 'false' }
    for (const [direction, offset] of Object.entries(directions)) {
      properties[direction] = String(connects(block.pos.map((coordinate, axis) => coordinate + offset[axis])))
    }
    put(block.pos, 'minecraft:iron_bars', properties)
  }
  return {
    structure,
    metadata: {
      name: 'room', id: 'corridor-M06', title: '焚痕冰封牢狱直廊', size,
      entrance: [7, 2, 0], interior: [7, 2, 4], exit: [7, 2, 39],
      corridorLengthBlocks: 40, hangingCages: 6, candleLedges: 10,
      nativeCompatibleExplicitIronBarStates: true,
    },
  }
}
