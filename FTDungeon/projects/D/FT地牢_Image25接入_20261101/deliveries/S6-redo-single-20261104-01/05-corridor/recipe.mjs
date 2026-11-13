export default function build(api) {
  const structure = api.createStructure([17, 12, 35])
  const put = (blockX, blockY, blockZ, block, properties = {}) => api.setBlock(structure, [blockX, blockY, blockZ], `minecraft:${block}`, properties)
  const fill = (from, to, block) => api.fill(structure, from, to, `minecraft:${block}`)
  fill([0, 0, 0], [16, 11, 34], 'air')
  fill([0, 0, 0], [16, 1, 34], 'basalt[axis=y]')
  for (let blockX = 0; blockX <= 16; blockX++) {
    for (let blockZ = 0; blockZ <= 34; blockZ++) {
      const grain = (blockX * 11 + blockZ * 3) % 13
      put(blockX, 2, blockZ, grain < 3 ? 'blackstone' : grain < 6 ? 'cracked_polished_blackstone_bricks' : 'polished_blackstone_bricks')
      if (blockX === 8 && blockZ % 5 === 0) put(blockX, 2, blockZ, 'polished_blackstone')
    }
  }
  for (let blockY = 3; blockY <= 11; blockY++) {
    for (let blockZ = 0; blockZ <= 34; blockZ++) {
      for (const wallX of [0, 16]) put(wallX, blockY, blockZ, (blockY + blockZ) % 9 < 3 ? 'blackstone' : 'polished_blackstone_bricks')
    }
    for (let blockX = 0; blockX <= 16; blockX++) {
      for (const wallZ of [0, 34]) put(blockX, blockY, wallZ, 'polished_blackstone_bricks')
    }
  }
  for (let blockX = 0; blockX <= 16; blockX++) {
    for (let blockZ = 0; blockZ <= 34; blockZ++) {
      const grain = (blockX * 5 + blockZ * 7) % 11
      put(blockX, 11, blockZ, grain < 3 ? 'obsidian' : grain < 6 ? 'smooth_basalt' : 'blackstone')
    }
  }
  for (const channelX of [2, 12]) {
    fill([channelX - 1, 3, 2], [channelX - 1, 3, 32], 'polished_blackstone_bricks')
    fill([channelX + 3, 3, 2], [channelX + 3, 3, 32], 'polished_blackstone_bricks')
    for (let blockX = channelX; blockX <= channelX + 2; blockX++) {
      for (let blockZ = 2; blockZ <= 32; blockZ++) {
        put(blockX, 2, blockZ, 'lava', {level: '0'})
        put(blockX, 3, blockZ, 'orange_stained_glass')
      }
    }
  }
  for (const pillarZ of [5, 13, 21, 29]) {
    for (const pillarX of [5, 11]) {
      fill([pillarX - 1, 2, pillarZ - 1], [pillarX + 1, 3, pillarZ + 1], 'polished_blackstone_bricks')
      fill([pillarX, 4, pillarZ], [pillarX, 9, pillarZ], 'polished_blackstone_bricks')
      put(pillarX, 5, pillarZ, 'gilded_blackstone')
      put(pillarX, 6, pillarZ, 'shroomlight')
      fill([pillarX - 1, 10, pillarZ - 1], [pillarX + 1, 10, pillarZ + 1], 'blackstone')
      put(pillarX, 9, pillarZ, 'obsidian')
      const lampX = pillarX === 5 ? 6 : 10
      for (let chainY = 8; chainY <= 10; chainY++) put(lampX, chainY, pillarZ, 'chain', {axis: 'y', waterlogged: 'false'})
      put(lampX, 7, pillarZ, 'lantern', {hanging: 'true', waterlogged: 'false'})
    }
    fill([1, 10, pillarZ], [15, 10, pillarZ], 'polished_blackstone_bricks')
  }
  for (const wallX of [1, 15]) {
    for (const pillarZ of [3, 11, 19, 27, 33]) {
      fill([wallX, 4, pillarZ], [wallX, 9, pillarZ], 'obsidian')
      put(wallX, 5, pillarZ, 'gilded_blackstone')
    }
    fill([wallX, 9, 1], [wallX, 10, 33], 'blackstone')
  }
  for (const fallZ of [8, 24]) {
    for (const fallX of [1, 15]) {
      fill([fallX - 1, 2, fallZ - 1], [fallX + 1, 9, fallZ + 1], 'blackstone')
      for (let blockY = 3; blockY <= 8; blockY++) {
        put(fallX, blockY, fallZ, 'lava', {level: '0'})
        put(fallX === 1 ? 2 : 14, blockY, fallZ, 'orange_stained_glass')
      }
      put(fallX, 9, fallZ, 'gilded_blackstone')
    }
  }
  for (const shelfZ of [16, 32]) {
    for (const wallX of [1, 15]) {
      const endX = wallX === 1 ? 2 : 15
      const startX = wallX === 1 ? 1 : 14
      fill([startX, 4, shelfZ - 1], [endX, 4, shelfZ + 1], 'polished_blackstone_bricks')
      put(wallX, 5, shelfZ, 'shroomlight')
    }
  }
  for (const [heatX, heatZ] of [[8, 3], [7, 9], [9, 15], [8, 19], [7, 25], [9, 31]]) put(heatX, 2, heatZ, 'shroomlight')
  for (const endZ of [0, 34]) {
    const startZ = endZ === 0 ? 0 : 33
    const lastZ = endZ === 0 ? 1 : 34
    fill([5, 3, startZ], [11, 9, lastZ], 'polished_blackstone_bricks')
    fill([7, 3, startZ], [9, 7, lastZ], 'air')
    fill([8, 8, startZ], [8, 8, lastZ], 'air')
    for (const jambX of [6, 10]) {
      put(jambX, 5, endZ, 'gilded_blackstone')
      put(jambX, 7, endZ, 'chiseled_polished_blackstone')
    }
  }
  return {structure, metadata: {name: 'room', id: 'S6-REDO-05', reference: 'S6-05-corridor.png', composition: 'straight low-roof corridor, four paired column/light bays, sealed channels and wall lava windows, only two end ports; independent recipe'}}
}
