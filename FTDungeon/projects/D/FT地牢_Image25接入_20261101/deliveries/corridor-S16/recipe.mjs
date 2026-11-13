export default function build(api) {
  const structure = api.createStructure([11, 10, 16])
  const set = (position, block, properties) => api.setBlock(structure, position, `minecraft:${block}`, properties)
  const masonry = (horizontal, vertical, depth) => {
    const pattern = (horizontal * 13 + vertical * 7 + depth * 11) % 17
    if (pattern < 5) return 'mossy_stone_bricks'
    if (pattern < 8) return 'cracked_stone_bricks'
    return 'stone_bricks'
  }
  api.fill(structure, [0, 0, 0], [10, 9, 15], 'minecraft:air')
  for (let depth = 0; depth < 16; depth += 1) {
    for (let horizontal = 0; horizontal < 11; horizontal += 1) {
      const border = horizontal < 3 || horizontal > 7
      const frost = (horizontal === 3 || horizontal === 7) && (horizontal + depth * 3) % 5 < 2
      const centerSeam = horizontal === 5 && depth % 5 === 0
      set([horizontal, 0, depth], frost ? 'packed_ice' : centerSeam ? 'polished_andesite' : masonry(horizontal, 0, depth))
      set([horizontal, 9, depth], masonry(horizontal, 9, depth))
      if (border) set([horizontal, 8, depth], masonry(horizontal, 8, depth))
    }
    for (const horizontal of [0, 1, 9, 10]) {
      for (let vertical = 1; vertical < 9; vertical += 1) set([horizontal, vertical, depth], masonry(horizontal, vertical, depth))
    }
  }
  for (const depth of [0, 5, 10, 15]) {
    for (const horizontal of [2, 8]) {
      for (let vertical = 1; vertical < 8; vertical += 1) set([horizontal, vertical, depth], vertical === 1 || vertical === 5 ? 'chiseled_stone_bricks' : masonry(horizontal, vertical, depth))
    }
    set([3, 6, depth], 'stone_brick_stairs', { facing: 'west', half: 'top', shape: 'straight', waterlogged: 'false' })
    set([7, 6, depth], 'stone_brick_stairs', { facing: 'east', half: 'top', shape: 'straight', waterlogged: 'false' })
    for (const horizontal of [3, 7]) set([horizontal, 7, depth], 'mossy_stone_bricks')
    set([4, 7, depth], 'stone_brick_stairs', { facing: 'west', half: 'top', shape: 'straight', waterlogged: 'false' })
    set([6, 7, depth], 'stone_brick_stairs', { facing: 'east', half: 'top', shape: 'straight', waterlogged: 'false' })
    for (let horizontal = 2; horizontal <= 8; horizontal += 1) set([horizontal, 8, depth], 'stone_bricks')
  }
  for (const start of [2, 7, 12]) {
    for (const horizontal of [1, 9]) {
      const edge = horizontal === 1 ? 2 : 8
      for (let depth = start; depth <= start + 1; depth += 1) {
        for (let vertical = 1; vertical <= 5; vertical += 1) {
          set([horizontal, vertical, depth], (depth + vertical) % 3 === 0 ? 'blue_ice' : 'packed_ice')
          if (vertical <= 4) set([edge, vertical, depth], 'iron_bars', { north: 'false', east: 'false', south: 'false', west: 'false', waterlogged: 'false' })
        }
        set([edge, 5, depth], 'packed_ice')
        set([edge, 7, depth], 'packed_ice')
      }
      set([edge, 6, start], 'blue_ice')
      set([edge, 8, start], 'packed_ice')
      for (let vertical = 6; vertical <= 8; vertical += 1) set([edge, vertical, start + 2], 'chain', { axis: 'y', waterlogged: 'false' })
      set([edge, 5, start + 2], 'blue_ice')
    }
  }
  for (const depth of [4, 9, 14]) {
    for (const horizontal of [2, 8]) {
      const backing = horizontal === 2 ? 1 : 9
      set([horizontal, 2, depth], 'chiseled_stone_bricks')
      set([backing, 3, depth], 'sea_lantern')
      set([backing, 4, depth], 'sea_lantern')
      set([horizontal, 3, depth], 'end_rod', { facing: 'up' })
      set([horizontal, 4, depth], 'end_rod', { facing: 'up' })
    }
  }
  for (const depth of [1, 6, 11]) {
    for (const horizontal of [2, 8]) {
      for (let vertical = 5; vertical <= 7; vertical += 1) set([horizontal, vertical, depth], 'vine', { east: horizontal === 8 ? 'true' : 'false', west: horizontal === 2 ? 'true' : 'false', up: 'false', north: 'false', south: 'false' })
    }
  }
  for (const depth of [3, 4, 7, 8, 12, 13]) {
    for (const horizontal of [3, 4, 6, 7]) {
      if ((horizontal + depth) % 3 !== 0) set([horizontal, 8, depth], 'vine', { east: 'false', west: 'false', up: 'true', north: 'false', south: 'false' })
    }
  }
  for (const depth of [0, 15]) {
    for (const horizontal of [2, 8]) set([horizontal, 8, depth], 'chiseled_stone_bricks')
    for (let horizontal = 3; horizontal <= 7; horizontal += 1) set([horizontal, 8, depth], 'iron_bars', { north: 'false', east: 'false', south: 'false', west: 'false', waterlogged: 'false' })
  }
  return { structure, metadata: { name: 'room' } }
}
