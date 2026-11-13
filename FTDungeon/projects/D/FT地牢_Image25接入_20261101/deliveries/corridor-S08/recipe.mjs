export default function build(api) {
  const size = [11, 10, 16]
  const structure = api.createStructure(size)
  const set = (position, block, properties) => api.setBlock(structure, position, block, properties)
  const fill = (from, to, block, properties) => api.fill(structure, from, to, block, properties)
  fill([0, 0, 0], [10, 9, 15], 'minecraft:air')
  fill([0, 0, 0], [10, 0, 15], 'minecraft:stone_bricks')
  fill([0, 1, 0], [10, 1, 15], 'minecraft:packed_ice')
  fill([0, 2, 0], [0, 8, 15], 'minecraft:stone_bricks')
  fill([10, 2, 0], [10, 8, 15], 'minecraft:stone_bricks')
  fill([1, 2, 0], [1, 8, 15], 'minecraft:packed_ice')
  fill([9, 2, 0], [9, 8, 15], 'minecraft:packed_ice')
  fill([0, 9, 0], [10, 9, 15], 'minecraft:stone_bricks')
  fill([2, 8, 0], [8, 8, 15], 'minecraft:blue_ice')
  for (let depth = 0; depth < size[2]; depth++) {
    for (let across = 2; across <= 8; across++) {
      if ((across * 7 + depth * 3) % 9 < 3) set([across, 1, depth], 'minecraft:blue_ice')
    }
    for (const wall of [1, 9]) {
      for (let height = 2; height <= 7; height++) {
        if ((depth + height * 2) % 6 < 2) set([wall, height, depth], 'minecraft:blue_ice')
      }
    }
    if (![0, 5, 10, 15].includes(depth)) {
      for (const side of [2, 8]) {
        set([side, 7, depth], 'minecraft:packed_ice')
        set([side, 8, depth], 'minecraft:packed_ice')
      }
    }
  }
  for (const depth of [0, 5, 10, 15]) {
    for (const side of [2, 8]) {
      fill([side, 2, depth], [side, 6, depth], 'minecraft:stone_bricks')
      set([side, 2, depth], 'minecraft:chiseled_stone_bricks')
      set([side, 6, depth], 'minecraft:chiseled_stone_bricks')
    }
    fill([1, 7, depth], [9, 7, depth], 'minecraft:stone_bricks')
    fill([1, 8, depth], [9, 8, depth], 'minecraft:stone_bricks')
    set([3, 6, depth], 'minecraft:stone_brick_stairs', {facing: 'west', half: 'top', shape: 'straight', waterlogged: 'false'})
    set([7, 6, depth], 'minecraft:stone_brick_stairs', {facing: 'east', half: 'top', shape: 'straight', waterlogged: 'false'})
    set([5, 7, depth], 'minecraft:chiseled_stone_bricks')
    if (depth === 0 || depth === 15) {
      fill([3, 2, depth], [3, 5, depth], 'minecraft:stone_bricks')
      fill([7, 2, depth], [7, 5, depth], 'minecraft:stone_bricks')
      fill([4, 6, depth], [6, 6, depth], 'minecraft:stone_bricks')
      for (const side of [0, 1, 9, 10]) fill([side, 2, depth], [side, 8, depth], 'minecraft:stone_bricks')
    }
    for (const [across, facing] of [[3, 'east'], [7, 'west']]) {
      if (depth !== 0 && depth !== 15) set([across, 5, depth], 'minecraft:wall_torch', {facing})
    }
  }
  for (const depth of [1, 14]) {
    for (const [across, facing] of [[2, 'east'], [8, 'west']]) {
      set([across, 4, depth], 'minecraft:stone_bricks')
      set([across + (facing === 'east' ? 1 : -1), 4, depth], 'minecraft:wall_torch', {facing})
    }
  }
  for (const depth of [2, 3, 7, 8, 12, 13]) {
    for (const side of [2, 8]) {
      set([side, 1, depth], 'minecraft:smooth_quartz')
      if ([2, 8, 12].includes(depth)) set([side, 2, depth], 'minecraft:snow_block')
      set([side, 4, depth], 'minecraft:smooth_quartz_slab', {type: 'top', waterlogged: 'false'})
    }
  }
  for (const [across, depth, length] of [[2, 2, 2], [8, 3, 1], [2, 7, 1], [8, 7, 2], [2, 12, 2], [8, 13, 1]]) {
    for (let offset = 0; offset < length; offset++) {
      set([across, 6 - offset, depth], 'minecraft:light_blue_stained_glass_pane', {north: 'false', east: 'false', south: 'false', west: 'false', waterlogged: 'false'})
    }
  }
  return {structure, metadata: {name: 'room', id: 'corridor-S08', theme: 'ice-frost-intact', size}}
}
