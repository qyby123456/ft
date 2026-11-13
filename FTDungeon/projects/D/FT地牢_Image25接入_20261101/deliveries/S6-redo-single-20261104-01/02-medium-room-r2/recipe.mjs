export default function build(api) {
  const structure = api.createStructure([25, 15, 25])
  const put = (blockX, blockY, blockZ, name, properties = {}, data) => api.setBlock(structure, [blockX, blockY, blockZ], `minecraft:${name}`, properties, data)
  const fill = (minimum, maximum, name) => api.fill(structure, minimum, maximum, `minecraft:${name}`)
  const stair = (blockX, blockY, blockZ, facing, half = 'bottom') => put(blockX, blockY, blockZ, 'polished_blackstone_brick_stairs', {facing, half, shape: 'straight', waterlogged: 'false'})
  fill([0, 0, 0], [24, 14, 24], 'air')
  fill([0, 0, 0], [24, 1, 24], 'basalt[axis=y]')
  for (let blockX = 0; blockX <= 24; blockX++) {
    for (let blockZ = 0; blockZ <= 24; blockZ++) {
      const grain = (blockX * 11 + blockZ * 17) % 13
      put(blockX, 2, blockZ, grain < 3 ? 'blackstone' : grain < 5 ? 'cracked_polished_blackstone_bricks' : 'polished_blackstone')
    }
  }
  for (let blockY = 3; blockY <= 14; blockY++) {
    for (let along = 0; along <= 24; along++) {
      for (const [blockX, blockZ] of [[0, along], [24, along], [along, 0], [along, 24]]) {
        put(blockX, blockY, blockZ, (along + blockY * 3) % 9 < 2 ? 'blackstone' : 'polished_blackstone_bricks')
      }
    }
  }
  fill([0, 14, 0], [24, 14, 24], 'blackstone')
  for (const edge of [1, 23]) {
    fill([edge, 12, 1], [edge, 13, 23], 'blackstone')
    fill([1, 12, edge], [23, 13, edge], 'blackstone')
  }
  for (const beamZ of [8, 20]) fill([1, 13, beamZ], [23, 13, beamZ], 'polished_blackstone_bricks')
  for (let blockX = 2; blockX <= 22; blockX++) {
    for (let blockZ = 2; blockZ <= 22; blockZ++) {
      const trench = blockX === 2 || (blockX === 22 && blockZ >= 15) || blockZ === 2 || blockZ === 22
      const bridge = (blockZ === 2 && blockX >= 11 && blockX <= 13) || (blockZ === 22 && blockX >= 16 && blockX <= 18)
      if (trench && !bridge) {
        put(blockX, 2, blockZ, 'lava', {level: '0'})
        put(blockX, 3, blockZ, 'orange_stained_glass')
      }
    }
  }
  for (const [supportX, supportZ] of [[3, 3], [3, 21], [21, 3], [21, 21]]) put(supportX, 3, supportZ, 'blackstone')
  for (const pillarX of [4, 20]) {
    fill([pillarX, 3, 8], [pillarX, 6, 8], 'obsidian')
    fill([pillarX, 7, 8], [pillarX, 10, 8], 'gilded_blackstone')
    fill([pillarX - 1, 11, 7], [pillarX + 1, 11, 9], 'polished_blackstone_bricks')
    fill([pillarX, 12, 8], [pillarX, 13, 8], 'smooth_basalt')
  }
  for (const wallX of [1, 23]) {
    for (const columnZ of [5, 13, 20]) {
      fill([wallX, 3, columnZ], [wallX, 11, columnZ], 'basalt[axis=y]')
      put(wallX, 8, columnZ, 'gilded_blackstone')
      stair(wallX, 11, columnZ - 1, 'south', 'top')
      stair(wallX, 11, columnZ + 1, 'north', 'top')
    }
  }
  for (let along = 3; along <= 21; along++) {
    if (along % 3 !== 1) {
      put(along, 12, 23, 'blackstone')
      put(1, 12, along, 'blackstone')
      put(23, 12, along, 'blackstone')
    }
    if (along % 5 === 0) {
      fill([1, 9, along], [1, 12, along], 'red_nether_bricks')
      fill([23, 10, along], [23, 12, along], 'red_nether_bricks')
    }
  }
  fill([21, 2, 16], [23, 12, 18], 'blackstone')
  for (let blockY = 3; blockY <= 11; blockY++) {
    put(22, blockY, 17, 'lava', {level: '0'})
    put(21, blockY, 17, 'orange_stained_glass')
  }
  put(22, 12, 17, 'gilded_blackstone')
  for (const [heatX, heatZ] of [[6, 7], [16, 6], [17, 14]]) {
    put(heatX, 2, heatZ, 'lava', {level: '0'})
    put(heatX, 3, heatZ, 'orange_stained_glass')
    put(heatX - 1, 3, heatZ, 'blackstone')
  }
  for (const [legX, legZ] of [[10, 10], [14, 10], [10, 12], [14, 12]]) put(legX, 3, legZ, 'polished_blackstone_bricks')
  fill([10, 4, 10], [14, 4, 12], 'polished_blackstone')
  for (const [blockX, blockZ] of [[10, 10], [14, 10], [10, 12], [14, 12]]) put(blockX, 4, blockZ, 'chiseled_polished_blackstone')
  put(11, 5, 11, 'amethyst_cluster', {facing: 'up', waterlogged: 'false'})
  put(14, 5, 11, 'small_amethyst_bud', {facing: 'up', waterlogged: 'false'})
  stair(9, 3, 11, 'east')
  stair(15, 3, 11, 'west')
  fill([23, 3, 6], [23, 3, 10], 'polished_blackstone_bricks')
  fill([22, 3, 7], [22, 5, 9], 'bookshelf')
  put(22, 3, 6, 'chest', {facing: 'west', type: 'single', waterlogged: 'false'}, api.createBlockNbt({id: 'minecraft:chest'}))
  put(22, 3, 11, 'barrel', {facing: 'west', open: 'false'}, api.createBlockNbt({id: 'minecraft:barrel'}))
  put(23, 4, 10, 'lantern', {hanging: 'false', waterlogged: 'false'})
  fill([4, 3, 22], [8, 7, 24], 'polished_blackstone_bricks')
  fill([5, 4, 22], [7, 6, 23], 'air')
  fill([4, 4, 21], [4, 6, 23], 'polished_blackstone_bricks')
  fill([8, 4, 21], [8, 6, 23], 'polished_blackstone_bricks')
  fill([4, 7, 21], [8, 7, 23], 'polished_blackstone_bricks')
  fill([4, 8, 22], [8, 8, 23], 'blackstone')
  fill([4, 3, 22], [8, 3, 23], 'chiseled_polished_blackstone')
  for (const frameX of [4, 8]) {
    put(frameX, 4, 21, 'chiseled_polished_blackstone')
    put(frameX, 6, 21, 'chiseled_polished_blackstone')
  }
  fill([5, 4, 24], [7, 5, 24], 'shroomlight')
  fill([5, 7, 21], [7, 7, 21], 'chiseled_polished_blackstone')
  for (const lampX of [5, 19]) {
    for (let chainY = 9; chainY <= 13; chainY++) put(lampX, chainY, 8, 'chain', {axis: 'y', waterlogged: 'false'})
    put(lampX, 8, 8, 'lantern', {hanging: 'true', waterlogged: 'false'})
  }
  for (let frameZ = 23; frameZ <= 24; frameZ++) {
    fill([14, 3, frameZ], [20, 9, frameZ], 'polished_blackstone_bricks')
    fill([16, 3, frameZ], [18, 7, frameZ], 'air')
    fill([15, 8, frameZ], [19, 8, frameZ], 'chiseled_polished_blackstone')
    put(14, 5, frameZ, 'gilded_blackstone')
    put(20, 5, frameZ, 'gilded_blackstone')
  }
  for (const lampX of [14, 20]) {
    put(lampX, 6, 22, 'polished_blackstone_bricks')
    put(lampX, 7, 22, 'lantern', {hanging: 'false', waterlogged: 'false'})
  }
  fill([11, 3, 0], [13, 7, 0], 'air')
  return {structure, metadata: {name: 'room', id: 'S6-REDO-02', reference: 'S6-02-medium-room.png', composition: 'crystal study table, storage corner, single framed rear portal and lit side niche; not reused peak dais'}}
}
