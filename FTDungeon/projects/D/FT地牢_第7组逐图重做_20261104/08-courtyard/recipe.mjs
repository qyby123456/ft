export default function build(api) {
  const structure = api.createStructure([33, 23, 33])
  const put = (blockX, blockY, blockZ, block, properties = {}) => {
    const defaults = {}
    if (['mycelium', 'podzol'].includes(block)) defaults.snowy = 'false'
    if (['mushroom_stem', 'red_mushroom_block'].includes(block)) {
      for (const face of ['north', 'east', 'south', 'west', 'up', 'down']) defaults[face] = 'true'
    }
    api.setBlock(structure, [blockX, blockY, blockZ], `minecraft:${block}`, { ...defaults, ...properties })
  }
  const box = (minimum, maximum, block, properties = {}) => {
    for (let blockX = minimum[0]; blockX <= maximum[0]; blockX++) {
      for (let blockY = minimum[1]; blockY <= maximum[1]; blockY++) {
        for (let blockZ = minimum[2]; blockZ <= maximum[2]; blockZ++) put(blockX, blockY, blockZ, block, properties)
      }
    }
  }
  box([0, 0, 0], [32, 22, 32], 'air')
  box([0, 0, 0], [32, 0, 32], 'deepslate_bricks')
  for (let blockX = 0; blockX <= 32; blockX++) {
    for (let blockZ = 0; blockZ <= 32; blockZ++) {
      const mosaic = (blockX * 13 + blockZ * 17 + Math.floor(blockX / 4)) % 23
      put(blockX, 1, blockZ, mosaic < 6 ? 'mycelium' : mosaic < 11 ? 'purple_terracotta' : mosaic < 15 ? 'rooted_dirt' : 'mossy_stone_bricks')
      if (blockX === 0 || blockX === 32 || blockZ === 0 || blockZ === 32) {
        for (let blockY = 2; blockY <= 21; blockY++) {
          const seam = (blockX * 7 + blockY * 11 + blockZ * 19) % 17
          put(blockX, blockY, blockZ, blockY >= 15 && seam < 10 ? 'rooted_dirt' : seam < 6 ? 'mossy_stone_bricks' : 'deepslate_bricks')
        }
      }
      if (blockX < 8 || blockX > 24 || blockZ < 8 || blockZ > 24) put(blockX, 22, blockZ, 'rooted_dirt')
    }
  }
  box([1, 10, 1], [31, 10, 5], 'stone_bricks')
  box([1, 10, 6], [5, 10, 29], 'stone_bricks')
  box([27, 10, 6], [31, 10, 29], 'stone_bricks')
  for (let railX = 5; railX <= 27; railX++) put(railX, 11, 5, 'oak_fence', { waterlogged: 'false' })
  for (const railX of [5, 27]) {
    for (let railZ = 6; railZ <= 29; railZ++) put(railX, 11, railZ, 'oak_fence', { waterlogged: 'false' })
    box([railX, 11, 30], [railX, 12, 30], 'stone_bricks')
  }
  for (const postX of [3, 29]) {
    for (const postZ of [5, 14, 28]) box([postX, 2, postZ], [postX, 9, postZ], 'mossy_stone_bricks')
  }
  for (const [fromX, toX, fromZ, toZ] of [[1, 5, 11, 17], [27, 31, 19, 25]]) {
    box([fromX, 6, fromZ], [toX, 6, toZ], 'rooted_dirt')
    box([fromX, 7, fromZ], [toX, 7, toZ], 'mycelium')
  }
  const mushroom = (stemX, stemY, stemZ, height, radius) => {
    box([stemX, stemY, stemZ], [stemX, stemY + height - 1, stemZ], 'mushroom_stem')
    const capY = stemY + height
    for (let offsetX = -radius; offsetX <= radius; offsetX++) {
      for (let offsetZ = -radius; offsetZ <= radius; offsetZ++) {
        if (Math.abs(offsetX) === radius && Math.abs(offsetZ) === radius) continue
        put(stemX + offsetX, capY, stemZ + offsetZ, 'red_mushroom_block')
        if (Math.abs(offsetX) < radius && Math.abs(offsetZ) < radius) put(stemX + offsetX, capY + 1, stemZ + offsetZ, 'red_mushroom_block')
      }
    }
  }
  mushroom(3, 8, 14, 6, 3)
  mushroom(29, 8, 22, 5, 3)
  mushroom(17, 11, 3, 7, 3)
  put(17, 15, 3, 'shroomlight')
  mushroom(3, 2, 28, 3, 2)
  for (let offsetX = -6; offsetX <= 6; offsetX++) {
    for (let offsetZ = -6; offsetZ <= 6; offsetZ++) {
      if (Math.abs(offsetX) + Math.abs(offsetZ) > 10) continue
      const blockX = 16 + offsetX
      const blockZ = 16 + offsetZ
      put(blockX, 1, blockZ, 'polished_blackstone_bricks')
      const inner = Math.max(Math.abs(offsetX), Math.abs(offsetZ)) <= 5 && Math.abs(offsetX) + Math.abs(offsetZ) <= 8
      const core = Math.max(Math.abs(offsetX), Math.abs(offsetZ)) <= 2
      put(blockX, 2, blockZ, !inner || core ? 'mossy_stone_bricks' : 'water', inner && !core ? { level: '0' } : {})
      if (core) put(blockX, 3, blockZ, 'mossy_stone_bricks')
    }
  }
  box([15, 4, 15], [17, 4, 17], 'amethyst_block')
  box([16, 5, 15], [16, 6, 17], 'amethyst_block')
  box([15, 5, 16], [17, 5, 16], 'amethyst_block')
  box([16, 7, 16], [16, 8, 16], 'amethyst_block')
  for (const [crystalX, crystalY, crystalZ] of [[16, 9, 16], [15, 5, 15], [17, 5, 17], [14, 4, 16], [18, 4, 16], [16, 4, 14], [16, 4, 18]]) put(crystalX, crystalY, crystalZ, 'amethyst_cluster', { facing: 'up', waterlogged: 'false' })
  box([22, 1, 24], [27, 1, 29], 'polished_blackstone_bricks')
  box([23, 1, 25], [26, 1, 28], 'water', { level: '0' })
  for (const [markerX, markerZ] of [[10, 16], [22, 16], [16, 10], [16, 22]]) put(markerX, 2, markerZ, 'shroomlight')
  for (const [ledgeX, ledgeZ] of [[2, 7], [7, 3], [25, 3], [30, 15], [7, 30], [30, 29]]) {
    put(ledgeX, 2, ledgeZ, 'mycelium')
    put(ledgeX, 3, ledgeZ, 'red_mushroom')
  }
  for (const [fungusX, fungusY, fungusZ] of [[2, 8, 12], [4, 8, 16], [28, 8, 20], [30, 8, 24], [8, 11, 3], [22, 11, 3], [3, 11, 8], [29, 11, 16]]) {
    put(fungusX, fungusY - 1, fungusZ, 'mycelium')
    put(fungusX, fungusY, fungusZ, 'brown_mushroom')
  }
  for (const [vineX, side] of [[1, 'west'], [31, 'east']]) {
    for (const vineZ of [8, 18, 27]) {
      for (let blockY = 12; blockY <= 18; blockY++) put(vineX, blockY, vineZ, 'vine', { [side]: 'true', up: 'false' })
    }
  }
  for (const vineX of [6, 12, 24, 29]) {
    for (let blockY = 12; blockY <= 17; blockY++) put(vineX, blockY, 1, 'vine', { north: 'true', up: 'false' })
  }
  for (const [lampX, lampZ] of [[4, 4], [28, 4], [4, 25], [28, 28]]) {
    box([lampX, 18, lampZ], [lampX, 21, lampZ], 'chain', { axis: 'y', waterlogged: 'false' })
    put(lampX, 17, lampZ, 'shroomlight')
  }
  for (const [lampX, lampZ] of [[0, 7], [0, 22], [32, 18], [23, 0]]) put(lampX, 5, lampZ, 'shroomlight')
  for (let blockX = 7; blockX <= 11; blockX++) {
    for (let blockY = 2; blockY <= 8; blockY++) put(blockX, blockY, 0, blockX >= 8 && blockX <= 10 && blockY <= 5 || blockX === 9 && blockY === 6 ? 'air' : 'stone_bricks')
    put(blockX, 1, 0, 'stone_bricks')
  }
  for (let blockZ = 7; blockZ <= 11; blockZ++) {
    for (let blockY = 2; blockY <= 8; blockY++) put(32, blockY, blockZ, blockZ >= 8 && blockZ <= 10 && blockY <= 5 || blockZ === 9 && blockY === 6 ? 'air' : 'stone_bricks')
    put(32, 1, blockZ, 'stone_bricks')
  }
  return { structure, metadata: { name: 'room' } }
}
