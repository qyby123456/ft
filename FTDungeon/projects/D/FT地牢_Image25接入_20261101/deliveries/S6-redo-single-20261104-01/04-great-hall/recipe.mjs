export default function build(api) {
  const structure = api.createStructure([27, 17, 37])
  const put = (blockX, blockY, blockZ, block, properties = {}) => api.setBlock(structure, [blockX, blockY, blockZ], `minecraft:${block}`, properties)
  const fill = (from, to, block) => api.fill(structure, from, to, `minecraft:${block}`)
  fill([0, 0, 0], [26, 16, 36], 'air')
  fill([0, 0, 0], [26, 1, 36], 'basalt[axis=y]')
  for (let blockX = 0; blockX <= 26; blockX++) {
    for (let blockZ = 0; blockZ <= 36; blockZ++) {
      const grain = (blockX * 13 + blockZ * 7) % 17
      put(blockX, 2, blockZ, grain < 3 ? 'blackstone' : grain < 6 ? 'cracked_polished_blackstone_bricks' : 'polished_blackstone_bricks')
      if (blockX === 13 && blockZ % 3 === 0) put(blockX, 2, blockZ, 'chiseled_polished_blackstone')
    }
  }
  for (let blockY = 3; blockY <= 16; blockY++) {
    for (let blockZ = 0; blockZ <= 36; blockZ++) {
      for (const wallX of [0, 26]) put(wallX, blockY, blockZ, (blockY + blockZ) % 11 < 2 ? 'blackstone' : 'polished_blackstone_bricks')
    }
    for (let blockX = 0; blockX <= 26; blockX++) {
      for (const wallZ of [0, 36]) put(blockX, blockY, wallZ, (blockY + blockX) % 13 < 3 ? 'blackstone' : 'polished_blackstone_bricks')
    }
  }
  fill([0, 16, 0], [26, 16, 36], 'blackstone')
  for (const wallX of [1, 25]) fill([wallX, 14, 1], [wallX, 15, 35], 'blackstone')
  for (const wallZ of [1, 35]) fill([1, 14, wallZ], [25, 15, wallZ], 'blackstone')
  for (const channelX of [3, 19]) {
    for (let blockX = channelX; blockX <= channelX + 4; blockX++) {
      for (let blockZ = 3; blockZ <= 30; blockZ++) {
        if (blockZ >= 10 && blockZ <= 12) continue
        put(blockX, 2, blockZ, 'lava', {level: '0'})
        put(blockX, 3, blockZ, 'orange_stained_glass')
      }
    }
    for (let blockZ = 3; blockZ <= 30; blockZ++) {
      if (blockZ >= 10 && blockZ <= 12) continue
      put(channelX - 1, 3, blockZ, 'polished_blackstone_bricks')
      put(channelX + 5, 3, blockZ, 'polished_blackstone_bricks')
    }
    for (const rimZ of [2, 31]) fill([channelX, 3, rimZ], [channelX + 4, 3, rimZ], 'polished_blackstone_bricks')
    put(channelX - 1, 3, 2, 'polished_blackstone_bricks')
    put(channelX + 5, 3, 2, 'polished_blackstone_bricks')
  }
  for (const pillarX of [7, 19]) {
    for (const pillarZ of [8, 17, 26]) {
      fill([pillarX - 1, 2, pillarZ - 1], [pillarX + 1, 4, pillarZ + 1], 'polished_blackstone_bricks')
      fill([pillarX, 5, pillarZ], [pillarX, 13, pillarZ], 'polished_blackstone_bricks')
      put(pillarX, 6, pillarZ, 'gilded_blackstone')
      put(pillarX, 12, pillarZ, 'gilded_blackstone')
      fill([pillarX - 1, 14, pillarZ - 1], [pillarX + 1, 14, pillarZ + 1], 'polished_blackstone_bricks')
      fill([pillarX, 15, pillarZ], [pillarX, 15, pillarZ], 'blackstone')
    }
    fill([pillarX, 15, 1], [pillarX, 15, 35], 'polished_blackstone_bricks')
    for (const bannerZ of [13, 22, 31]) {
      fill([pillarX, 7, bannerZ - 1], [pillarX, 12, bannerZ + 1], 'red_terracotta')
      fill([pillarX, 13, bannerZ], [pillarX, 14, bannerZ], 'blackstone')
      put(pillarX, 7, bannerZ - 1, 'black_terracotta')
      put(pillarX, 8, bannerZ, 'black_terracotta')
      put(pillarX, 7, bannerZ + 1, 'black_terracotta')
    }
  }
  for (const outerX of [2, 24]) fill([outerX, 3, 3], [outerX, 3, 30], 'air')
  for (const wallX of [1, 25]) {
    for (const columnZ of [4, 16, 28, 34]) {
      fill([wallX, 3, columnZ], [wallX, 13, columnZ], 'obsidian')
      put(wallX, 7, columnZ, 'gilded_blackstone')
      put(wallX, 13, columnZ, 'gilded_blackstone')
    }
  }
  for (const lampX of [9, 17]) {
    for (const lampZ of [4, 14, 23, 30]) {
      put(lampX, 3, lampZ, 'chiseled_polished_blackstone')
      put(lampX, 4, lampZ, 'lantern', {hanging: 'false', waterlogged: 'false'})
    }
  }
  for (const pillarX of [7, 19]) {
    const lampX = pillarX === 7 ? 8 : 18
    for (const lampZ of [8, 17, 26]) {
      for (let chainY = 10; chainY <= 15; chainY++) put(lampX, chainY, lampZ, 'chain', {axis: 'y', waterlogged: 'false'})
      put(lampX, 9, lampZ, 'lantern', {hanging: 'true', waterlogged: 'false'})
    }
  }
  for (const [heatX, heatZ] of [[11, 6], [15, 10], [13, 15], [11, 21], [15, 25], [13, 29]]) put(heatX, 2, heatZ, 'shroomlight')
  fill([9, 3, 32], [17, 3, 36], 'polished_blackstone_bricks')
  for (let stairX = 10; stairX <= 16; stairX++) put(stairX, 3, 31, 'polished_blackstone_brick_stairs', {facing: 'south', half: 'bottom', shape: 'straight', waterlogged: 'false'})
  fill([9, 4, 34], [17, 12, 36], 'polished_blackstone_bricks')
  fill([11, 4, 34], [15, 10, 36], 'air')
  fill([12, 11, 34], [14, 11, 36], 'air')
  for (const frameX of [10, 16]) {
    for (const bandY of [5, 9]) fill([frameX, bandY, 34], [frameX, bandY, 36], 'gilded_blackstone')
    put(frameX, 4, 33, 'chiseled_polished_blackstone')
    put(frameX, 5, 33, 'lantern', {hanging: 'false', waterlogged: 'false'})
  }
  for (const [archX, archY] of [[9, 8], [10, 10], [11, 11], [12, 12], [13, 12], [14, 12], [15, 11], [16, 10], [17, 8]]) put(archX, archY, 33, 'gilded_blackstone')
  fill([10, 3, 0], [16, 10, 1], 'polished_blackstone_bricks')
  fill([11, 3, 0], [15, 8, 1], 'air')
  fill([12, 9, 0], [14, 9, 1], 'air')
  for (const sideX of [0, 26]) {
    const startX = sideX === 0 ? 0 : 25
    const endX = sideX === 0 ? 1 : 26
    fill([startX, 3, 8], [endX, 9, 14], 'polished_blackstone_bricks')
    fill([startX, 3, 10], [endX, 7, 12], 'air')
    fill([startX, 8, 11], [endX, 8, 11], 'air')
  }
  return {structure, metadata: {name: 'room', id: 'S6-REDO-04', reference: 'S6-04-great-hall.png', composition: 'long central aisle, six tall pillars in two rows, paired sealed channels, red-black stone drapes, four portals and one-step elevated far arch; independent geometry'}}
}
