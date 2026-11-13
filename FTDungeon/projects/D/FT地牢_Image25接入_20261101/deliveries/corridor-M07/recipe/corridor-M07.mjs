export default function build(api) {
  const size = [17, 13, 41]
  const structure = api.createStructure(size)
  const put = (position, block, properties) => api.setBlock(structure, position, `minecraft:${block}`, properties)
  const fill = (from, to, block, properties) => api.fill(structure, from, to, `minecraft:${block}`, properties)
  const stair = (position, facing, half = 'top') => put(position, 'polished_blackstone_brick_stairs', {
    facing, half, shape: 'straight', waterlogged: 'false',
  })
  const slab = position => put(position, 'deepslate_brick_slab', { type: 'bottom', waterlogged: 'false' })
  const hash = (blockX, blockY, blockZ) => Math.abs((blockX * 73 + blockY * 37 + blockZ * 131 + blockX * blockZ * 11) % 29)
  const archStations = [2, 8, 14, 20, 26, 32, 38]

  fill([0, 0, 0], [16, 12, 40], 'air')
  for (let blockX = 0; blockX < size[0]; blockX += 1) {
    for (let blockZ = 0; blockZ < size[2]; blockZ += 1) {
      const floorTile = hash(blockX, 0, blockZ)
      put([blockX, 0, blockZ], floorTile < 4 ? 'cracked_deepslate_tiles' : floorTile < 10 ? 'polished_blackstone_bricks' : 'deepslate_tiles')
      for (const roofY of [11, 12]) {
        put([blockX, roofY, blockZ], roofY === 12 ? 'deepslate_bricks' : hash(blockX, roofY, blockZ) < 8 ? 'obsidian' : 'polished_blackstone_bricks')
      }
    }
  }

  for (let blockZ = 0; blockZ < size[2]; blockZ += 1) {
    for (const blockX of [0, 1, 2, 14, 15, 16]) {
      for (let blockY = 1; blockY <= 10; blockY += 1) {
        const stone = hash(blockX, blockY, blockZ)
        put([blockX, blockY, blockZ], stone < 5 ? 'cracked_deepslate_bricks' : stone < 12 ? 'polished_blackstone_bricks' : 'deepslate_bricks')
      }
    }
    for (const wallX of [2, 14]) {
      for (const trimY of [2, 7]) put([wallX, trimY, blockZ], blockZ % 6 === 2 ? 'gold_block' : 'gilded_blackstone')
      put([wallX, 1, blockZ], 'polished_blackstone_bricks')
      put([wallX, 9, blockZ], 'obsidian')
    }
    for (const trimX of [5, 11]) put([trimX, 0, blockZ], blockZ % 6 === 2 || blockZ % 6 === 5 ? 'gold_block' : 'gilded_blackstone')
    fill([7, 0, blockZ], [9, 0, blockZ], 'obsidian')
    if (blockZ % 6 === 2) {
      put([8, 0, blockZ], 'red_glazed_terracotta', { facing: 'north' })
      put([7, 0, blockZ], 'red_nether_bricks')
      put([9, 0, blockZ], 'red_nether_bricks')
      if (blockZ > 0 && blockZ < 40) {
        put([8, 0, blockZ - 1], 'red_nether_bricks')
        put([8, 0, blockZ + 1], 'red_nether_bricks')
      }
    }
    if (blockZ % 6 === 5) for (const tileX of [4, 12]) put([tileX, 0, blockZ], 'obsidian')
  }

  for (const station of archStations) {
    for (const columnX of [3, 13]) {
      fill([columnX, 1, station - 1], [columnX, 1, station + 1], 'polished_blackstone_bricks')
      fill([columnX, 2, station], [columnX, 7, station], 'obsidian')
      for (const bandY of [2, 7]) put([columnX, bandY, station], 'gold_block')
      put([columnX, 3, station], 'gilded_blackstone')
      for (const runeY of [4, 5, 6]) {
        if (runeY === 5) put([columnX, runeY, station], 'red_nether_bricks')
        else put([columnX, runeY, station], 'red_glazed_terracotta', { facing: columnX === 3 ? 'east' : 'west' })
      }
      for (const shoulderZ of [station - 1, station + 1]) stair([columnX, 2, shoulderZ], shoulderZ < station ? 'south' : 'north', 'bottom')
    }
    for (let archX = 3; archX <= 13; archX += 1) {
      const inset = Math.min(archX - 3, 13 - archX)
      const baseY = 7 + Math.min(3, inset)
      fill([archX, baseY, station], [archX, 10, station], inset === 0 ? 'gilded_blackstone' : 'polished_blackstone_bricks')
      if (inset > 0 && inset < 4) stair([archX, baseY - 1, station], archX < 8 ? 'west' : 'east')
      if (inset === 1 || inset === 3) put([archX, baseY, station], 'gilded_blackstone')
    }
    put([8, 10, station], 'gold_block')
    for (const chainY of [8, 9]) put([8, chainY, station], 'chain', { axis: 'y', waterlogged: 'false' })
    put([8, 7, station], 'lantern', { hanging: 'true', waterlogged: 'false' })
  }

  for (let bay = 0; bay < 6; bay += 1) {
    const station = 5 + bay * 6
    for (const wallX of [2, 14]) {
      fill([wallX, 3, station - 1], [wallX, 6, station + 1], 'obsidian')
      for (const runeY of [3, 5]) put([wallX, runeY, station], 'red_glazed_terracotta', { facing: wallX === 2 ? 'east' : 'west' })
      put([wallX, 4, station], 'red_nether_bricks')
      put([wallX, 6, station], 'gilded_blackstone')
    }
  }

  const damagedRibs = [[4, 7, 8], [5, 8, 14], [12, 7, 20], [11, 8, 26], [4, 7, 32]]
  for (const position of damagedRibs) put(position, 'air')
  for (const position of [[4, 8, 8], [5, 9, 14], [12, 8, 20], [11, 9, 26], [4, 8, 32]]) put(position, 'cracked_polished_blackstone_bricks')
  for (const position of [[3, 1, 5], [4, 1, 11], [12, 1, 17], [13, 1, 23], [4, 1, 29], [12, 1, 35]]) slab(position)
  for (const [blockX, blockZ, facing] of [[3, 10, 'south'], [13, 16, 'north'], [3, 28, 'north'], [13, 34, 'south']]) stair([blockX, 1, blockZ], facing, 'bottom')

  for (const endZ of [0, 40]) {
    fill([3, 1, endZ], [13, 10, endZ], 'polished_blackstone_bricks')
    fill([6, 1, endZ], [10, 6, endZ], 'air')
    fill([7, 7, endZ], [9, 8, endZ], 'air')
    for (const jambX of [5, 11]) {
      fill([jambX, 1, endZ], [jambX, 6, endZ], 'obsidian')
      for (const bandY of [2, 6]) put([jambX, bandY, endZ], 'gold_block')
      put([jambX, 4, endZ], 'red_glazed_terracotta', { facing: endZ === 0 ? 'north' : 'south' })
    }
    for (const shoulderX of [6, 10]) stair([shoulderX, 7, endZ], shoulderX < 8 ? 'west' : 'east')
    put([8, 9, endZ], 'gilded_blackstone')
  }

  return { structure, metadata: { name: 'corridor-M07', title: '灾变暗铁残拱长廊', size, archStations, damagedRibs, entrance: [8, 1, 0], interior: [8, 1, 3], exit: [8, 1, 40] } }
}
