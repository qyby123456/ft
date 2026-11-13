export default function build(api) {
  const structure = api.createStructure([19, 12, 19])
  const put = (blockX, blockY, blockZ, block, properties = {}, nbt) => api.setBlock(structure, [blockX, blockY, blockZ], block.startsWith('minecraft:') ? block : `minecraft:${block}`, properties, nbt)
  const fill = (from, to, block) => api.fill(structure, from, to, `minecraft:${block}`)
  fill([0, 0, 0], [18, 11, 18], 'air')
  fill([0, 0, 0], [18, 1, 18], 'basalt[axis=y]')
  for (let blockX = 0; blockX <= 18; blockX++) {
    for (let blockZ = 0; blockZ <= 18; blockZ++) {
      const grain = (blockX * 7 + blockZ * 5) % 11
      put(blockX, 2, blockZ, grain < 2 ? 'blackstone' : grain < 5 ? 'cracked_polished_blackstone_bricks' : 'polished_blackstone_bricks')
    }
  }
  for (let blockY = 3; blockY <= 11; blockY++) {
    for (let along = 0; along <= 18; along++) {
      for (const [blockX, blockZ] of [[0, along], [18, along], [along, 0], [along, 18]]) {
        put(blockX, blockY, blockZ, (along * 3 + blockY) % 8 < 2 ? 'blackstone' : 'polished_blackstone_bricks')
      }
    }
  }
  fill([0, 11, 0], [18, 11, 18], 'blackstone')
  for (const boundary of [1, 17]) {
    fill([boundary, 10, 1], [boundary, 10, 17], 'blackstone')
    fill([1, 10, boundary], [17, 10, boundary], 'blackstone')
  }
  for (const [pillarX, pillarZ] of [[1, 1], [1, 17], [17, 1], [17, 17]]) {
    fill([pillarX, 3, pillarZ], [pillarX, 9, pillarZ], 'obsidian')
    fill([pillarX - 1, 9, pillarZ - 1], [pillarX + 1, 10, pillarZ + 1], 'blackstone')
  }
  for (const along of [3, 8, 15]) {
    fill([17, 3, along], [17, 8, along], 'polished_blackstone_bricks')
    fill([along, 3, 17], [along, 8, 17], 'polished_blackstone_bricks')
    put(17, 8, along, 'gilded_blackstone')
    put(along, 8, 17, 'gilded_blackstone')
    fill([17, 9, along - 1], [17, 9, along + 1], 'blackstone')
    fill([along - 1, 9, 17], [along + 1, 9, 17], 'blackstone')
  }
  const pool = (minimumX, maximumX, minimumZ, maximumZ) => {
    for (let blockX = minimumX - 1; blockX <= maximumX + 1; blockX++) {
      for (let blockZ = minimumZ - 1; blockZ <= maximumZ + 1; blockZ++) {
        if (blockX >= minimumX && blockX <= maximumX && blockZ >= minimumZ && blockZ <= maximumZ) {
          put(blockX, 2, blockZ, 'lava', {level: '0'})
          put(blockX, 3, blockZ, 'orange_stained_glass')
        } else put(blockX, 3, blockZ, 'polished_blackstone_bricks')
      }
    }
  }
  pool(13, 15, 3, 6)
  pool(3, 6, 14, 15)
  fill([2, 3, 16], [4, 3, 16], 'air')
  for (let blockX = 7; blockX <= 11; blockX++) {
    for (let blockZ = 7; blockZ <= 11; blockZ++) {
      if (blockX === 7 || blockX === 11 || blockZ === 7 || blockZ === 11) {
        put(blockX, 2, blockZ, (blockX + blockZ) % 3 === 0 ? 'polished_blackstone' : 'shroomlight')
      }
    }
  }
  put(9, 2, 9, 'chiseled_polished_blackstone')
  put(9, 3, 9, 'chest', {facing: 'north', type: 'single', waterlogged: 'false'}, api.createBlockNbt({id: 'minecraft:chest'}))
  fill([16, 3, 4], [18, 7, 7], 'polished_blackstone_bricks')
  fill([16, 4, 5], [17, 6, 6], 'air')
  fill([18, 4, 5], [18, 5, 6], 'shroomlight')
  for (const edgeZ of [4, 7]) put(16, 5, edgeZ, 'chiseled_polished_blackstone')
  fill([7, 3, 16], [12, 6, 18], 'polished_blackstone_bricks')
  fill([8, 4, 16], [11, 5, 17], 'air')
  fill([8, 4, 18], [11, 5, 18], 'shroomlight')
  fill([7, 3, 16], [12, 3, 17], 'chiseled_polished_blackstone')
  fill([8, 6, 16], [11, 6, 16], 'chiseled_polished_blackstone')
  for (const [detailX, detailZ] of [[16, 8], [12, 16]]) {
    fill([detailX, 4, detailZ], [detailX, 5, detailZ], 'red_nether_bricks')
  }
  fill([17, 3, 10], [18, 8, 14], 'polished_blackstone_bricks')
  fill([17, 3, 11], [18, 6, 13], 'air')
  fill([3, 3, 17], [5, 7, 18], 'polished_blackstone_bricks')
  fill([4, 3, 17], [4, 5, 18], 'air')
  for (const [lampX, lampZ] of [[16, 12], [5, 16]]) {
    for (let chainY = 8; chainY <= 10; chainY++) put(lampX, chainY, lampZ, 'chain', {axis: 'y', waterlogged: 'false'})
    put(lampX, 7, lampZ, 'lantern', {hanging: 'true', waterlogged: 'false'})
  }
  fill([8, 3, 0], [10, 6, 0], 'air')
  return {structure, metadata: {name: 'room', id: 'S6-REDO-03', reference: 'S6-03-small-room.png', composition: 'single center chest, two recessed wall niches, asymmetric sealed pools, wide wall door and narrow side exit; new same-image geometry'}}
}
