export const hall = {
  id: 'zone-H05', name: '穹顶大殿', size: [31, 27, 39],
  entrance: [15, 2, 0], safe: [15, 2, 5], dais: [15, 4, 35],
  piers: [9, 20, 31].flatMap(blockZ => [8, 22].map(blockX => [blockX, blockZ])),
  chandeliers: [[4, 12, 14], [26, 12, 14], [15, 16, 14], [15, 16, 27]],
  reference: 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/halls/H05-halls-vaulted-grand-hall.png',
  referenceSha256: '2985f763563c014582ded69a7fcd8b950098934ddf5452a5c5b7ade2ed6b805c'
}

export function roofHeight(blockX) {
  const nave = [16, 20, 21, 22, 23, 23, 24, 24, 24, 23, 23, 22, 21, 20, 16]
  const aisle = [13, 14, 16, 17, 17, 17, 16, 14, 13]
  if (blockX >= 8 && blockX <= 22) return nave[blockX - 8]
  return aisle[blockX < 8 ? blockX : 30 - blockX]
}

export default function build(api) {
  const structure = api.createStructure(hall.size)
  const put = (blockX, blockY, blockZ, block) => api.setBlock(structure, [blockX, blockY, blockZ], block)
  const box = (minimum, maximum, block) => api.fill(structure, minimum, maximum, block)
  const stone = (blockX, blockY, blockZ) => {
    const value = (blockX * 17 + blockY * 11 + blockZ * 23) % 41
    if (value < 3) return 'minecraft:cracked_stone_bricks'
    if (value === 8 && blockY < 6) return 'minecraft:mossy_stone_bricks'
    return 'minecraft:stone_bricks'
  }
  box([0, 0, 0], [30, 26, 38], 'minecraft:air')
  box([0, 0, 0], [30, 0, 38], 'minecraft:deepslate_bricks')
  for (let blockX = 0; blockX < 31; blockX++) {
    for (let blockZ = 0; blockZ < 39; blockZ++) {
      put(blockX, 1, blockZ, (blockX + blockZ) % 6 === 0 ? 'minecraft:polished_andesite' : stone(blockX, 1, blockZ))
      for (let blockY = 2; blockY <= roofHeight(blockX) + 1; blockY++) {
        if (blockX <= 1 || blockX >= 29 || blockZ <= 1 || blockZ >= 37) put(blockX, blockY, blockZ, stone(blockX, blockY, blockZ))
      }
      const upperRoof = Math.max(roofHeight(Math.max(0, blockX - 1)), roofHeight(blockX), roofHeight(Math.min(30, blockX + 1))) + 1
      for (let blockY = roofHeight(blockX); blockY <= upperRoof; blockY++) {
        put(blockX, blockY, blockZ, blockY === roofHeight(blockX) ? 'minecraft:deepslate_bricks' : 'minecraft:deepslate_tiles')
      }
    }
  }
  box([8, 14, 2], [8, 17, 36], 'minecraft:stone_bricks')
  box([22, 14, 2], [22, 17, 36], 'minecraft:stone_bricks')
  for (const [centerX, centerZ] of hall.piers) {
    box([centerX - 2, 2, centerZ - 2], [centerX + 2, 2, centerZ + 2], 'minecraft:polished_andesite')
    box([centerX - 1, 3, centerZ - 1], [centerX + 1, 14, centerZ + 1], 'minecraft:stone_bricks')
    box([centerX - 2, 4, centerZ - 2], [centerX + 2, 4, centerZ + 2], 'minecraft:stone_brick_slab[type=top,waterlogged=false]')
    box([centerX - 1, 4, centerZ - 1], [centerX + 1, 4, centerZ + 1], 'minecraft:stone_bricks')
    box([centerX - 2, 13, centerZ - 2], [centerX + 2, 13, centerZ + 2], 'minecraft:polished_andesite')
    box([centerX - 1, 15, centerZ - 1], [centerX + 1, 17, centerZ + 1], 'minecraft:stone_bricks')
    const innerX = centerX < 15 ? centerX + 2 : centerX - 2
    box([innerX, 6, centerZ - 1], [innerX, 11, centerZ + 1], 'minecraft:cyan_terracotta')
    box([innerX, 7, centerZ], [innerX, 10, centerZ], 'minecraft:yellow_terracotta')
    box([innerX, 9, centerZ - 1], [innerX, 9, centerZ + 1], 'minecraft:yellow_terracotta')
  }
  for (const centerZ of [9, 20, 31]) {
    for (let blockX = 0; blockX < 31; blockX++) {
      put(blockX, roofHeight(blockX) - 1, centerZ, 'minecraft:polished_andesite')
      put(blockX, roofHeight(blockX), centerZ, 'minecraft:stone_bricks')
    }
    for (const wallX of [0, 30]) box([wallX, 2, centerZ - 1], [wallX, roofHeight(wallX) + 2, centerZ + 1], 'minecraft:polished_andesite')
    for (let blockY = 14; blockY < 20; blockY++) {
      const offset = Math.max(0, blockY - 16)
      box([8, blockY, centerZ], [8 + offset, blockY, centerZ], 'minecraft:polished_andesite')
      box([22 - offset, blockY, centerZ], [22, blockY, centerZ], 'minecraft:polished_andesite')
    }
  }
  for (let blockZ = 0; blockZ < 39; blockZ++) {
    for (const blockX of [14, 15, 16]) put(blockX, 26, blockZ, 'minecraft:stone_brick_slab[type=bottom,waterlogged=false]')
  }
  const windowMaterial = (horizontal, vertical) => {
    if (horizontal === 0 || vertical === 4 || (Math.abs(horizontal) === 1 && vertical === 7)) return 'minecraft:yellow_stained_glass'
    return (horizontal + vertical) % 3 === 0 ? 'minecraft:light_blue_stained_glass' : 'minecraft:cyan_stained_glass'
  }
  for (const centerZ of [5, 15, 25, 35]) {
    for (const sideX of [0, 30]) {
      for (let offsetZ = -2; offsetZ <= 2; offsetZ++) {
        for (let blockY = 4; blockY <= 12; blockY++) {
          const pointed = blockY <= 10 || (blockY === 11 && Math.abs(offsetZ) <= 1) || (blockY === 12 && offsetZ === 0)
          if (!pointed) continue
          const border = Math.abs(offsetZ) === 2 || blockY === 4 || blockY === 12
          for (const depthX of sideX === 0 ? [0, 1] : [29, 30]) put(depthX, blockY, centerZ + offsetZ, border ? 'minecraft:polished_andesite' : windowMaterial(offsetZ, blockY - 4))
        }
      }
    }
  }
  for (let offsetX = -4; offsetX <= 4; offsetX++) {
    for (let blockY = 7; blockY <= 21; blockY++) {
      const top = 21 - Math.abs(offsetX) * 2
      if (blockY > top) continue
      const border = Math.abs(offsetX) === 4 || blockY === 7 || blockY === top
      for (const blockZ of [37, 38]) put(15 + offsetX, blockY, blockZ, border ? 'minecraft:polished_andesite' : windowMaterial(offsetX, blockY - 7))
    }
  }
  box([13, 1, 0], [17, 1, 30], 'minecraft:red_wool')
  for (let blockZ = 0; blockZ <= 30; blockZ++) {
    for (const blockX of [12, 18]) put(blockX, 1, blockZ, 'minecraft:yellow_terracotta')
    if (blockZ % 6 === 3) put(15, 1, blockZ, 'minecraft:red_terracotta')
  }
  for (let blockX = 11; blockX <= 19; blockX++) {
    for (let blockZ = 0; blockZ <= 1; blockZ++) {
      const clearance = 10 - Math.max(0, Math.abs(blockX - 15) - 1)
      for (let blockY = 2; blockY <= clearance; blockY++) put(blockX, blockY, blockZ, 'minecraft:air')
      put(blockX, clearance + 1, blockZ, 'minecraft:chiseled_stone_bricks')
    }
  }
  box([10, 2, 32], [20, 2, 36], 'minecraft:stone_bricks')
  box([10, 3, 34], [20, 3, 36], 'minecraft:polished_andesite')
  box([11, 2, 31], [19, 2, 31], 'minecraft:stone_brick_stairs[facing=south,half=bottom,shape=straight,waterlogged=false]')
  box([11, 3, 33], [19, 3, 33], 'minecraft:stone_brick_stairs[facing=south,half=bottom,shape=straight,waterlogged=false]')
  box([13, 3, 32], [17, 3, 32], 'minecraft:red_carpet')
  box([13, 4, 34], [17, 4, 36], 'minecraft:red_carpet')
  for (const [centerX, baseY, centerZ] of hall.chandeliers) {
    const radius = centerX === 15 ? 2 : 1
    for (let offsetX = -radius; offsetX <= radius; offsetX++) {
      for (let offsetZ = -radius; offsetZ <= radius; offsetZ++) {
        if (Math.abs(offsetX) === radius || Math.abs(offsetZ) === radius || offsetX === 0 || offsetZ === 0) put(centerX + offsetX, baseY, centerZ + offsetZ, 'minecraft:polished_blackstone')
      }
    }
    for (let blockY = baseY + 1; blockY < roofHeight(centerX); blockY++) put(centerX, blockY, centerZ, 'minecraft:chain[axis=y,waterlogged=false]')
    put(centerX, baseY - 1, centerZ, 'minecraft:lantern[hanging=true,waterlogged=false]')
    for (const [offsetX, offsetZ] of [[-radius, -radius], [-radius, radius], [radius, -radius], [radius, radius], [0, -radius], [0, radius], [-radius, 0], [radius, 0]]) put(centerX + offsetX, baseY + 1, centerZ + offsetZ, 'minecraft:candle[candles=3,lit=true,waterlogged=false]')
  }
  for (const centerX of [5, 25]) {
    for (const centerZ of [7, 30]) {
      box([centerX, 2, centerZ], [centerX, 3, centerZ], 'minecraft:polished_blackstone')
      put(centerX, 4, centerZ, 'minecraft:lantern[hanging=false,waterlogged=false]')
    }
  }
  for (const centerX of [11, 19]) {
    box([centerX, 3, 35], [centerX, 4, 35], 'minecraft:polished_blackstone')
    put(centerX, 5, 35, 'minecraft:candle[candles=4,lit=true,waterlogged=false]')
  }
  box([2, 2, 17], [2, 7, 19], 'minecraft:bookshelf')
  box([28, 2, 17], [28, 7, 19], 'minecraft:bookshelf')
  for (const tableX of [3, 25]) {
    for (let offsetX = 0; offsetX < 3; offsetX++) {
      for (const blockZ of [4, 5]) {
        if (offsetX !== 1) put(tableX + offsetX, 2, blockZ, 'minecraft:dark_oak_fence[waterlogged=false]')
        put(tableX + offsetX, 3, blockZ, 'minecraft:dark_oak_slab[type=top,waterlogged=false]')
      }
    }
    put(tableX + 1, 4, 4, 'minecraft:candle[candles=2,lit=true,waterlogged=false]')
  }
  for (const [blockX, blockZ] of [[3, 12], [27, 12], [3, 33], [27, 33]]) {
    put(blockX, 2, blockZ, 'minecraft:stripped_dark_oak_wood[axis=y]')
    put(blockX, 3, blockZ, 'minecraft:dark_oak_slab[type=bottom,waterlogged=false]')
  }
  return { structure, metadata: { name: 'room', ...hall, ceiling: 'continuous three-span barrel vault; six grounded piers; three transverse ribs', clearAisle: { x: [13, 17], feetY: 2, z: [0, 30], headroom: 4 }, safeZone: { min: [13, 2, 3], max: [17, 4, 7] }, noDeployment: true } }
}
