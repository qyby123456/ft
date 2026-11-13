export default function build(api) {
  const structure = api.createStructure([15, 11, 16])
  const place = (position, block, properties, data) => api.setBlock(structure, position, block, properties, data)
  const fill = (minimum, maximum, block, properties) => api.fill(structure, minimum, maximum, block, properties)
  const stone = ['stone_bricks', 'stone_bricks', 'cracked_stone_bricks', 'cracked_stone_bricks', 'cobbled_deepslate', 'polished_andesite']
  fill([0, 0, 0], [14, 10, 15], 'minecraft:air')
  for (let posX = 0; posX < 15; posX += 1) {
    for (let posZ = 0; posZ < 16; posZ += 1) {
      const floorIndex = (posX * 17 + posZ * 31 + posX * posZ * 3) % stone.length
      place([posX, 0, posZ], `minecraft:${stone[floorIndex]}`)
      place([posX, 10, posZ], 'minecraft:deepslate_tiles')
    }
  }
  for (let posY = 1; posY <= 9; posY += 1) {
    for (let posZ = 0; posZ < 16; posZ += 1) {
      for (const posX of [0, 14]) place([posX, posY, posZ], `minecraft:${stone[(posY * 7 + posZ * 11 + posX) % 5]}`)
    }
  }
  for (const posZ of [0, 15]) {
    fill([0, 1, posZ], [14, 9, posZ], 'minecraft:stone_bricks')
    fill([6, 1, posZ], [8, 5, posZ], 'minecraft:air')
    for (const posX of [5, 9]) {
      fill([posX, 1, posZ], [posX, 4, posZ], 'minecraft:chiseled_stone_bricks')
      place([posX, 5, posZ], 'minecraft:polished_deepslate_slab', { type: 'top', waterlogged: 'false' })
    }
    fill([5, 6, posZ], [9, 6, posZ], 'minecraft:polished_deepslate')
    fill([6, 7, posZ], [8, 7, posZ], 'minecraft:chiseled_deepslate')
  }
  fill([6, 5, 15], [8, 5, 15], 'minecraft:iron_bars')
  for (let posZ = 1; posZ < 15; posZ += 1) {
    place([7, 0, posZ], posZ % 4 === 0 ? 'minecraft:terracotta' : 'minecraft:red_nether_bricks')
    if (posZ % 3 !== 0) {
      place([5, 0, posZ], 'minecraft:red_terracotta')
      place([9, 0, posZ], 'minecraft:red_nether_bricks')
    }
  }
  for (const posX of [6, 8]) {
    for (const posZ of [7, 8]) place([posX, 0, posZ], 'minecraft:red_nether_bricks')
  }
  for (const posZ of [0, 4, 8, 11, 15]) {
    for (const posX of [1, 13]) {
      fill([posX, 1, posZ], [posX, 2, posZ], 'minecraft:polished_deepslate')
      fill([posX, 3, posZ], [posX, 7, posZ], 'minecraft:polished_basalt', { axis: 'y' })
      place([posX, 8, posZ], 'minecraft:chiseled_stone_bricks')
    }
    fill([1, 9, posZ], [13, 9, posZ], 'minecraft:dark_oak_log', { axis: 'x' })
  }
  for (const posX of [3, 11]) {
    for (const centerZ of [4, 11]) {
      for (let offsetX = -1; offsetX <= 1; offsetX += 1) {
        for (let offsetZ = -1; offsetZ <= 1; offsetZ += 1) {
          const cageX = posX + offsetX
          const cageZ = centerZ + offsetZ
          place([cageX, 4, cageZ], 'minecraft:polished_deepslate_slab', { type: 'top', waterlogged: 'false' })
          place([cageX, 7, cageZ], 'minecraft:iron_trapdoor', { facing: 'north', half: 'bottom', open: 'false', powered: 'false', waterlogged: 'false' })
          if (Math.abs(offsetX) === 1 || Math.abs(offsetZ) === 1) fill([cageX, 5, cageZ], [cageX, 6, cageZ], 'minecraft:iron_bars')
        }
      }
      place([posX, 7, centerZ], 'minecraft:iron_block')
      place([posX, 8, centerZ], 'minecraft:chain', { axis: 'y', waterlogged: 'false' })
    }
  }
  for (const posX of [1, 13]) {
    for (const posZ of [2, 6, 9, 13]) {
      const banner = api.createBlockNbt({ id: 'minecraft:banner', Patterns: [{ Color: 15, Pattern: 'sku' }, { Color: 15, Pattern: 'bo' }] })
      place([posX, 5, posZ], 'minecraft:red_wall_banner', { facing: posX === 1 ? 'east' : 'west' }, banner)
    }
    for (const posZ of [1, 5, 10, 14]) {
      place([posX, 3, posZ], 'minecraft:chiseled_stone_bricks')
      place([posX, 4, posZ], 'minecraft:end_rod', { facing: 'up' })
    }
  }
  for (const posX of [2, 12]) {
    for (const posZ of [2, 6, 9, 13]) {
      place([posX, 1, posZ], 'minecraft:chiseled_stone_bricks')
      place([posX, 2, posZ], 'minecraft:candle', { candles: String(2 + (posZ % 3)), lit: 'true', waterlogged: 'false' })
      place([posX, 0, posZ], 'minecraft:red_nether_bricks')
    }
  }
  for (const posX of [4, 10]) {
    for (const posZ of [2, 13]) place([posX, 1, posZ], 'minecraft:candle', { candles: '1', lit: 'true', waterlogged: 'false' })
  }
  for (const posX of [1, 13]) {
    for (const posZ of [1, 7, 14]) place([posX, 8, posZ], 'minecraft:cobweb')
  }
  for (const position of [[2, 8, 3], [12, 8, 12], [2, 1, 14], [12, 1, 1]]) place(position, 'minecraft:cobweb')
  return { structure, metadata: { name: 'room', id: 'corridor-S13', theme: 'aged blood ritual' } }
}
