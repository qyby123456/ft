export default function build(api) {
  const structure = api.createStructure([15, 12, 43])
  const set = (blockX, blockY, blockZ, block, properties = {}) => {
    const defaults = {}
    if (['mycelium', 'podzol'].includes(block)) defaults.snowy = 'false'
    if (['mushroom_stem', 'red_mushroom_block'].includes(block)) {
      for (const face of ['north', 'east', 'south', 'west', 'up', 'down']) defaults[face] = 'true'
    }
    api.setBlock(structure, [blockX, blockY, blockZ], `minecraft:${block}`, { ...defaults, ...properties })
  }
  const fill = (minimum, maximum, block, properties = {}) => {
    for (let blockX = minimum[0]; blockX <= maximum[0]; blockX++) {
      for (let blockY = minimum[1]; blockY <= maximum[1]; blockY++) {
        for (let blockZ = minimum[2]; blockZ <= maximum[2]; blockZ++) set(blockX, blockY, blockZ, block, properties)
      }
    }
  }
  fill([0, 0, 0], [14, 11, 42], 'air')
  fill([0, 0, 0], [14, 0, 42], 'deepslate_bricks')
  for (let blockX = 0; blockX <= 14; blockX++) {
    for (let blockZ = 0; blockZ <= 42; blockZ++) {
      const speckle = (blockX * 31 + blockZ * 7 + Math.floor(blockZ / 4) * 3) % 19
      set(blockX, 1, blockZ, speckle < 4 ? 'mossy_stone_bricks' : speckle < 8 ? 'purple_terracotta' : speckle < 14 ? 'podzol' : 'rooted_dirt')
      if (blockX === 0 || blockX === 14 || blockZ === 0 || blockZ === 42) {
        for (let blockY = 2; blockY < 11; blockY++) {
          const weathering = (blockX * 11 + blockY * 7 + blockZ * 13) % 17
          set(blockX, blockY, blockZ, blockY >= 8 ? weathering < 10 ? 'rooted_dirt' : 'mycelium' : weathering < 7 ? 'rooted_dirt' : weathering < 10 ? 'mossy_stone_bricks' : 'deepslate_bricks')
        }
      }
      const roofPatch = (blockX * 7 + Math.floor(blockZ / 3) * 5) % 13
      set(blockX, 11, blockZ, roofPatch < 4 ? 'red_mushroom_block' : roofPatch < 8 ? 'mycelium' : 'rooted_dirt')
    }
  }
  for (const stemX of [2, 12]) {
    for (const stemZ of [5, 13, 21, 29, 37]) {
      fill([stemX - 1, 2, stemZ - 1], [stemX + 1, 2, stemZ + 1], 'mossy_stone_bricks')
      set(stemX, 2, stemZ, 'mycelium')
      fill([stemX, 3, stemZ], [stemX, 8, stemZ], 'mushroom_stem')
      set(stemX, 6, stemZ, 'shroomlight')
      const radius = (stemX === 2 ? stemZ === 5 || stemZ === 29 : stemZ === 13 || stemZ === 37) ? 2 : 1
      for (let offsetX = -radius; offsetX <= radius; offsetX++) {
        for (let offsetZ = -radius; offsetZ <= radius; offsetZ++) {
          if (Math.abs(offsetX) === radius && Math.abs(offsetZ) === radius) continue
          set(stemX + offsetX, 9, stemZ + offsetZ, 'red_mushroom_block')
          if (Math.abs(offsetX) < radius && Math.abs(offsetZ) < radius) set(stemX + offsetX, 10, stemZ + offsetZ, 'red_mushroom_block')
        }
      }
      const innerX = stemX === 2 ? 3 : 11
      set(innerX, 2, stemZ, 'amethyst_block')
      set(innerX, 3, stemZ, 'amethyst_cluster', { facing: 'up', waterlogged: 'false' })
      const outerX = stemX === 2 ? 1 : 13
      set(outerX, 2, stemZ + 1, 'mycelium')
      set(outerX, 3, stemZ + 1, 'red_mushroom')
    }
  }
  for (const [poolX, poolZ, length] of [[3, 8, 3], [3, 24, 2], [10, 16, 2], [10, 32, 3]]) {
    fill([poolX - 1, 1, poolZ - 1], [poolX + 2, 1, poolZ + length], 'polished_blackstone_bricks')
    fill([poolX, 1, poolZ], [poolX + 1, 1, poolZ + length - 1], 'water', { level: '0' })
  }
  for (const [ledgeX, ledgeZ] of [[1, 3], [13, 10], [1, 18], [13, 26], [1, 34], [13, 40]]) {
    const fromX = ledgeX === 1 ? 1 : 12
    const toX = ledgeX === 1 ? 2 : 13
    fill([fromX, 2, ledgeZ], [toX, 3, ledgeZ], 'rooted_dirt')
    fill([fromX, 4, ledgeZ], [toX, 4, ledgeZ], 'mycelium')
    set(ledgeX, 5, ledgeZ, 'brown_mushroom')
  }
  for (const [vineX, side] of [[1, 'west'], [13, 'east']]) {
    for (const vineZ of [8, 16, 24, 32, 40]) {
      for (let blockY = 7; blockY <= 10; blockY++) set(vineX, blockY, vineZ, 'vine', { [side]: 'true', up: 'false' })
    }
    for (const lichenZ of [4, 12, 20, 28, 36]) set(vineX, 5, lichenZ, 'glow_lichen', { [side]: 'true', waterlogged: 'false' })
  }
  for (const [fungusX, fungusZ] of [[4, 3], [10, 7], [4, 12], [10, 20], [4, 28], [10, 39]]) {
    set(fungusX, 1, fungusZ, 'mycelium')
    set(fungusX, 2, fungusZ, (fungusX + fungusZ) % 2 ? 'red_mushroom' : 'brown_mushroom')
  }
  for (const doorZ of [0, 42]) {
    for (let blockX = 4; blockX <= 10; blockX++) {
      for (let blockY = 2; blockY <= 8; blockY++) {
        const clear = blockX >= 6 && blockX <= 8 && blockY <= 5 || blockX === 7 && blockY === 6
        set(blockX, blockY, doorZ, clear ? 'air' : 'stone_bricks')
      }
      set(blockX, 1, doorZ, 'stone_bricks')
    }
    set(5, 6, doorZ, 'shroomlight')
    set(9, 6, doorZ, 'shroomlight')
    set(7, 8, doorZ, 'shroomlight')
  }
  return { structure, metadata: { name: 'room' } }
}
