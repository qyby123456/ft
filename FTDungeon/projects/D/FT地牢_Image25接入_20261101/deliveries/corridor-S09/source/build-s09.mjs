export default function build(api) {
  const structure = api.createStructure([11, 9, 16])
  const place = (position, block, nbt) => api.setBlock(structure, position, block, undefined, nbt)
  const fill = (start, end, block) => api.fill(structure, start, end, block)
  const noise = (blockX, blockY, blockZ) => (blockX * 83 + blockY * 131 + blockZ * 47 + blockX * blockZ * 17) % 100
  const stone = (blockX, blockY, blockZ) => {
    const sample = noise(blockX, blockY, blockZ)
    if (sample < 15) return 'minecraft:cracked_stone_bricks'
    if (sample < 29) return 'minecraft:mossy_stone_bricks'
    if (sample < 35) return 'minecraft:andesite'
    return 'minecraft:stone_bricks'
  }
  fill([0, 0, 0], [10, 8, 15], 'minecraft:air')
  for (let blockX = 0; blockX < 11; blockX++) {
    for (let blockZ = 0; blockZ < 16; blockZ++) {
      place([blockX, 0, blockZ], stone(blockX, 0, blockZ))
      place([blockX, 8, blockZ], noise(blockX, 8, blockZ) < 25 ? 'minecraft:cracked_deepslate_tiles' : 'minecraft:deepslate_tiles')
      if (blockX === 3 || blockX === 7) place([blockX, 0, blockZ], 'minecraft:polished_andesite')
      if (blockX < 2 || blockX > 8) {
        for (let blockY = 1; blockY < 8; blockY++) place([blockX, blockY, blockZ], stone(blockX, blockY, blockZ))
      }
    }
  }
  for (const blockZ of [0, 15]) {
    for (let blockX = 2; blockX <= 8; blockX++) {
      for (let blockY = 1; blockY <= 7; blockY++) place([blockX, blockY, blockZ], stone(blockX, blockY, blockZ))
    }
    fill([4, 1, blockZ], [6, 3, blockZ], 'minecraft:air')
    place([5, 4, blockZ], 'minecraft:air')
    place([4, 4, blockZ], 'minecraft:stone_brick_stairs[facing=west,half=top,shape=straight,waterlogged=false]')
    place([6, 4, blockZ], 'minecraft:stone_brick_stairs[facing=east,half=top,shape=straight,waterlogged=false]')
    for (const blockX of [3, 7]) {
      fill([blockX, 1, blockZ], [blockX, 3, blockZ], 'minecraft:polished_andesite')
      place([blockX, 4, blockZ], 'minecraft:chiseled_stone_bricks')
    }
    fill([4, 5, blockZ], [6, 5, blockZ], 'minecraft:stone_bricks')
    place([5, 5, blockZ], 'minecraft:chiseled_stone_bricks')
  }
  for (const blockZ of [5, 10]) {
    for (const blockX of [2, 8]) {
      fill([blockX, 1, blockZ], [blockX, 5, blockZ], 'minecraft:stone_bricks')
      place([blockX, 2, blockZ], 'minecraft:cracked_stone_bricks')
      place([blockX, 5, blockZ], 'minecraft:chiseled_stone_bricks')
    }
  }
  for (const blockZ of [0, 3, 8, 12, 15]) {
    fill([0, 7, blockZ], [10, 7, blockZ], 'minecraft:polished_basalt[axis=x]')
    for (const blockX of [2, 8]) place([blockX, 6, blockZ], 'minecraft:polished_blackstone_bricks')
  }
  for (const wallX of [1, 9]) {
    for (const patchZ of [2, 6, 11, 13]) {
      for (let blockY = 1; blockY <= 4; blockY++) {
        for (let offsetZ = 0; offsetZ < 2; offsetZ++) {
          if (noise(wallX, blockY, patchZ + offsetZ) < 70) {
            place([wallX, blockY, patchZ + offsetZ], blockY === 1 ? 'minecraft:coal_block' : 'minecraft:cracked_deepslate_bricks')
          }
        }
      }
    }
    for (const emberZ of [6, 12]) {
      place([wallX, 1, emberZ], 'minecraft:shroomlight')
      place([wallX === 1 ? 2 : 8, 1, emberZ], 'minecraft:iron_bars[waterlogged=false]')
    }
  }
  for (const blockZ of [3, 8, 12]) {
    place([5, 6, blockZ], 'minecraft:chain[axis=y,waterlogged=false]')
    place([5, 5, blockZ], 'minecraft:soul_lantern[hanging=true,waterlogged=false]')
  }
  const cages = []
  for (const side of ['west', 'east']) {
    for (const startZ of [3, 11]) {
      const startX = side === 'west' ? 2 : 7
      const anchorX = side === 'west' ? 3 : 7
      const anchorZ = startZ === 3 ? 3 : 12
      for (let blockX = startX; blockX <= startX + 1; blockX++) {
        for (let blockZ = startZ; blockZ <= startZ + 1; blockZ++) {
          place([blockX, 2, blockZ], 'minecraft:iron_trapdoor[facing=north,half=top,open=false,powered=false,waterlogged=false]')
          fill([blockX, 3, blockZ], [blockX, 4, blockZ], 'minecraft:iron_bars[waterlogged=false]')
          place([blockX, 5, blockZ], 'minecraft:iron_block')
        }
      }
      place([anchorX, 6, anchorZ], 'minecraft:chain[axis=y,waterlogged=false]')
      cages.push({ side, minimum: [startX, 2, startZ], maximum: [startX + 1, 5, startZ + 1], suspension: [anchorX, 6, anchorZ], beam: [anchorX, 7, anchorZ] })
    }
  }
  const eggs = []
  for (const blockX of [2, 8]) {
    for (const blockZ of [6, 13]) {
      place([blockX, 3, blockZ], 'minecraft:mushroom_stem[down=true,east=true,north=true,south=true,up=true,west=true]')
      place([blockX, 4, blockZ], 'minecraft:smooth_sandstone_slab[type=bottom,waterlogged=false]')
      eggs.push([blockX, 3, blockZ])
    }
  }
  const skulls = []
  for (const blockX of [2, 8]) {
    for (const blockZ of [2, 9, 14]) {
      place([blockX, 1, blockZ], `minecraft:skeleton_skull[rotation=${blockX === 2 ? 5 : 11}]`, api.createBlockNbt({ id: 'minecraft:skull' }))
      skulls.push([blockX, 1, blockZ])
    }
    place([blockX, 1, 8], 'minecraft:bone_block[axis=z]')
    place([blockX, 1, 13], 'minecraft:quartz_slab[type=bottom,waterlogged=false]')
  }
  const empty = position => structure.getBlock(position)?.state.getName().toString() === 'minecraft:air'
  const web = position => { if (empty(position)) place(position, 'minecraft:cobweb') }
  for (let blockZ = 1; blockZ <= 14; blockZ++) {
    for (const blockX of [2, 8]) {
      for (const blockY of [5, 6]) web([blockX, blockY, blockZ])
      if (blockZ % 3 === 1) web([blockX, 2, blockZ])
    }
    if (blockZ % 3 !== 0) {
      web([3, 6, blockZ])
      web([7, 6, blockZ])
    }
  }
  for (const position of [[3, 1, 1], [7, 1, 14], [2, 1, 7], [8, 1, 7], [3, 4, 6], [7, 4, 13]]) web(position)
  const solidBlocks = new Set(['stone_bricks', 'cracked_stone_bricks', 'mossy_stone_bricks', 'andesite',
    'polished_andesite', 'chiseled_stone_bricks', 'coal_block', 'cracked_deepslate_bricks',
    'deepslate_tiles', 'cracked_deepslate_tiles', 'polished_basalt', 'polished_blackstone_bricks',
    'shroomlight', 'bone_block', 'mushroom_stem', 'iron_bars', 'iron_block'])
  for (const block of structure.getBlocks()) {
    if (block.state.getName().toString() !== 'minecraft:iron_bars') continue
    const properties = { waterlogged: 'false' }
    for (const [direction, offsetX, offsetZ] of [['north', 0, -1], ['east', 1, 0], ['south', 0, 1], ['west', -1, 0]]) {
      const neighbour = structure.getBlock([block.pos[0] + offsetX, block.pos[1], block.pos[2] + offsetZ])
      const neighbourName = neighbour?.state.getName().toString().replace('minecraft:', '')
      properties[direction] = String(solidBlocks.has(neighbourName))
    }
    api.setBlock(structure, block.pos, 'minecraft:iron_bars', properties)
  }
  return {
    structure,
    metadata: {
      name: 'room', id: 'corridor-S09', size: [11, 9, 16], cages, eggs, skulls,
      reference: 'D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/S09-webbed-spider-burnt.png',
      reference_sha256: 'eeb80b6a2cf8e64e3ec92740ed43119b714b7b9e72d671c339484cfd886d6c26',
    },
  }
}
