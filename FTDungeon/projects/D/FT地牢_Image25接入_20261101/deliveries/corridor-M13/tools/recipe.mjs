export default function build(api) {
  const size = [13, 11, 40]
  const structure = api.createStructure(size)
  const put = (blockX, blockY, blockZ, block) => api.setBlock(structure, [blockX, blockY, blockZ], block)
  const masonry = ['stone_bricks', 'mossy_stone_bricks', 'mossy_stone_bricks', 'cracked_stone_bricks', 'stone_bricks', 'mossy_cobblestone']
  const hash = (blockX, blockY, blockZ) => Math.abs((blockX * 7349 + blockY * 9151 + blockZ * 1933) ^ (blockZ * 127 + blockX * blockY * 31))
  const stone = (blockX, blockY, blockZ) => `minecraft:${masonry[hash(blockX, blockY, blockZ) % masonry.length]}`
  const stair = (facing, mossy = true, half = 'top') => `minecraft:${mossy ? 'mossy_' : ''}stone_brick_stairs[facing=${facing},half=${half},shape=straight,waterlogged=false]`
  api.fill(structure, [0, 0, 0], [12, 10, 39], 'minecraft:air')
  for (let blockX = 0; blockX < 13; blockX++) {
    for (let blockZ = 0; blockZ < 40; blockZ++) {
      const floor = hash(blockX, 0, blockZ) % 13 === 0 && (blockX < 5 || blockX > 7) ? 'minecraft:moss_block' : stone(blockX, 0, blockZ)
      put(blockX, 0, blockZ, floor)
      put(blockX, 10, blockZ, stone(blockX, 10, blockZ))
      if (blockX < 2 || blockX > 10) {
        for (let blockY = 1; blockY < 10; blockY++) put(blockX, blockY, blockZ, stone(blockX, blockY, blockZ))
      }
      if (blockX < 3 || blockX > 9) put(blockX, 8, blockZ, stone(blockX, 8, blockZ))
      if (blockX < 4 || blockX > 8) put(blockX, 9, blockZ, stone(blockX, 9, blockZ))
    }
  }
  for (const wallX of [1, 11]) {
    for (const panelZ of [4, 12, 20, 28, 36]) {
      for (let blockZ = panelZ; blockZ <= panelZ + 1; blockZ++) {
        for (let blockY = 2; blockY <= 5; blockY++) put(wallX, blockY, blockZ, blockZ === panelZ ? 'minecraft:dark_oak_log[axis=y]' : 'minecraft:dark_oak_planks')
      }
    }
  }
  const archStations = [0, 8, 16, 24, 32, 39]
  for (const blockZ of archStations) {
    for (const pierX of [2, 3, 9, 10]) {
      for (let blockY = 1; blockY <= 6; blockY++) put(pierX, blockY, blockZ, stone(pierX, blockY, blockZ))
      put(pierX, 1, blockZ, 'minecraft:chiseled_stone_bricks')
      put(pierX, 6, blockZ, 'minecraft:mossy_stone_bricks')
    }
    put(4, 6, blockZ, stair('west'))
    put(8, 6, blockZ, stair('east'))
    for (const blockX of [3, 4, 8, 9]) put(blockX, 7, blockZ, stone(blockX, 7, blockZ))
    put(5, 7, blockZ, stair('west', false))
    put(7, 7, blockZ, stair('east', false))
    for (let blockX = 4; blockX <= 8; blockX++) put(blockX, 8, blockZ, stone(blockX, 8, blockZ))
    put(6, 8, blockZ, 'minecraft:chiseled_stone_bricks')
    if (blockZ === 16) {
      put(4, 6, blockZ, 'minecraft:air')
      put(4, 7, blockZ, 'minecraft:air')
      put(5, 7, blockZ, 'minecraft:mossy_stone_brick_slab[type=top,waterlogged=false]')
    }
    if (blockZ === 24) {
      put(8, 6, blockZ, 'minecraft:air')
      put(7, 7, blockZ, 'minecraft:air')
      put(8, 7, blockZ, 'minecraft:cracked_stone_bricks')
    }
    if (blockZ === 32) put(5, 7, blockZ, 'minecraft:air')
  }
  for (const blockZ of [0, 39]) {
    for (let blockX = 0; blockX < 13; blockX++) {
      for (let blockY = 1; blockY < 10; blockY++) {
        const opening = blockX >= 4 && blockX <= 8 && blockY <= 5 || blockX >= 5 && blockX <= 7 && blockY <= 6 || blockX === 6 && blockY === 7
        if (!opening) put(blockX, blockY, blockZ, stone(blockX, blockY, blockZ))
      }
    }
    put(4, 6, blockZ, stair('west', false))
    put(8, 6, blockZ, stair('east', false))
    put(5, 7, blockZ, stair('west'))
    put(7, 7, blockZ, stair('east'))
    put(6, 8, blockZ, 'minecraft:chiseled_stone_bricks')
  }
  for (let blockZ = 2; blockZ < 38; blockZ++) {
    for (const vineX of [2, 10]) {
      if (archStations.includes(blockZ)) continue
      const height = 3 + hash(vineX, 1, blockZ) % 5
      const firstY = 1 + hash(vineX, 3, blockZ) % 3
      const face = vineX === 2 ? 'west' : 'east'
      for (let blockY = firstY; blockY <= Math.min(7, firstY + height); blockY++) put(vineX, blockY, blockZ, `minecraft:vine[north=false,east=${face === 'east'},south=false,west=${face === 'west'},up=false]`)
    }
    for (const ledgeX of [3, 9]) {
      if (archStations.includes(blockZ) || hash(ledgeX, 1, blockZ) % 5 > 1) continue
      put(ledgeX, 1, blockZ, hash(ledgeX, 2, blockZ) % 2 ? 'minecraft:mossy_cobblestone' : stair(ledgeX === 3 ? 'west' : 'east', true, 'bottom'))
      if (hash(ledgeX, 2, blockZ) % 3 === 0) put(ledgeX, 2, blockZ, 'minecraft:moss_carpet')
    }
    for (const carpetX of [4, 8]) {
      if (blockZ % 6 < 2 && !archStations.includes(blockZ)) put(carpetX, 1, blockZ, 'minecraft:moss_carpet')
    }
    if (blockZ % 5 === 2) {
      for (const leafX of [3, 9]) put(leafX, 7, blockZ, 'minecraft:oak_leaves[distance=7,persistent=true,waterlogged=false]')
    }
  }
  for (const blockZ of [4, 12, 20, 28, 36]) {
    for (let blockY = 7; blockY <= 9; blockY++) put(6, blockY, blockZ, 'minecraft:chain[axis=y,waterlogged=false]')
    put(6, 6, blockZ, 'minecraft:lantern[hanging=true,waterlogged=false]')
  }
  return {structure, metadata: {name: 'room', id: 'M13', theme: 'overgrown ruins', archStations}}
}
