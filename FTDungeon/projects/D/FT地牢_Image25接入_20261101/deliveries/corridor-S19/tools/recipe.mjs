export default function build(api) {
  const structure = api.createStructure([15, 11, 16])
  const put = (position, block, properties) => api.setBlock(structure, position, `minecraft:${block}`, properties)
  const fill = (start, end, block, properties) => api.fill(structure, start, end, `minecraft:${block}`, properties)
  fill([0, 0, 0], [14, 10, 15], 'air')
  fill([0, 0, 0], [14, 1, 15], 'polished_blackstone_bricks')
  fill([0, 2, 0], [1, 10, 15], 'obsidian')
  fill([13, 2, 0], [14, 10, 15], 'obsidian')
  fill([0, 9, 0], [14, 10, 15], 'polished_blackstone_bricks')
  for (let posZ = 1; posZ < 15; posZ++) {
    for (const posX of [2, 12]) {
      put([posX, 0, posZ], 'shroomlight')
      put([posX, 1, posZ], 'orange_stained_glass')
    }
    for (const posX of [5, 9]) put([posX, 1, posZ], posZ % 4 === 0 ? 'gold_block' : 'gilded_blackstone')
    for (let posX = 3; posX < 12; posX++) {
      if (posX !== 5 && posX !== 9) put([posX, 1, posZ], (posX + posZ) % 4 === 0 ? 'obsidian' : 'polished_blackstone_bricks')
    }
    for (const posX of [1, 13]) {
      put([posX, 2, posZ], 'gilded_blackstone')
      for (let posY = 3; posY < 9; posY++) {
        put([posX, posY, posZ], (posY + posZ) % 3 === 0 ? 'crying_obsidian' : 'obsidian')
      }
      put([posX, 8, posZ], posZ % 4 === 0 ? 'gold_block' : 'polished_blackstone_bricks')
    }
  }
  for (const centerZ of [4, 11]) {
    for (const [offsetX, offsetZ] of [[0, -1], [-1, 0], [1, 0], [0, 1]]) put([7 + offsetX, 1, centerZ + offsetZ], 'gold_block')
    put([7, 1, centerZ], 'crying_obsidian')
    for (const posX of [1, 13]) {
      fill([posX, 3, centerZ - 1], [posX, 7, centerZ + 1], 'polished_blackstone')
      for (const [offsetZ, posY] of [[0, 3], [-1, 4], [1, 4], [0, 5], [-1, 6], [1, 6], [0, 7]]) put([posX, posY, centerZ + offsetZ], 'gold_block')
    }
  }
  for (const posX of [1, 13]) {
    const backingX = posX === 1 ? 0 : 14
    fill([backingX, 3, 8], [backingX, 7, 8], 'shroomlight')
    fill([posX, 3, 8], [posX, 7, 8], 'orange_stained_glass')
  }
  for (const posZ of [2, 6, 10, 13]) {
    for (const posX of [2, 12]) {
      put([posX, 3, posZ], 'polished_blackstone')
      put([posX, 4, posZ], 'end_rod', { facing: 'up' })
      put([posX, 5, posZ], 'end_rod', { facing: 'up' })
    }
    put([7, 8, posZ], 'end_rod', { facing: 'down' })
  }
  for (const posZ of [0, 4, 8, 11, 15]) {
    fill([0, 9, posZ], [14, 9, posZ], 'polished_blackstone')
    if (posZ === 0 || posZ === 15) {
      for (const posX of [1, 13]) fill([posX, 2, posZ], [posX, 8, posZ], 'polished_blackstone_bricks')
    }
  }
  for (const centerX of [4, 10]) {
    for (const centerZ of [4, 11]) {
      fill([centerX - 1, 5, centerZ - 1], [centerX + 1, 5, centerZ + 1], 'polished_blackstone_slab', { type: 'top', waterlogged: 'false' })
      fill([centerX - 1, 7, centerZ - 1], [centerX + 1, 7, centerZ + 1], 'polished_blackstone_slab', { type: 'bottom', waterlogged: 'false' })
      for (let offsetX = -1; offsetX <= 1; offsetX++) {
        for (let offsetZ = -1; offsetZ <= 1; offsetZ++) {
          if (offsetX !== 0 || offsetZ !== 0) put([centerX + offsetX, 6, centerZ + offsetZ], 'iron_bars')
        }
      }
      put([centerX, 7, centerZ], 'iron_block')
      put([centerX, 8, centerZ], 'chain', { axis: 'y', waterlogged: 'false' })
    }
  }
  for (const posZ of [0, 15]) {
    fill([0, 2, posZ], [14, 8, posZ], 'polished_blackstone_bricks')
    fill([6, 2, posZ], [8, 5, posZ], 'air')
    put([7, 6, posZ], 'air')
    for (const posX of [5, 9]) {
      fill([posX, 2, posZ], [posX, 6, posZ], 'gilded_blackstone')
      for (const posY of [3, 5]) put([posX, posY, posZ], 'gold_block')
    }
    fill([6, 7, posZ], [8, 7, posZ], 'gold_block')
    put([6, 6, posZ], 'polished_blackstone_brick_stairs', { facing: 'west', half: 'top', shape: 'straight', waterlogged: 'false' })
    put([8, 6, posZ], 'polished_blackstone_brick_stairs', { facing: 'east', half: 'top', shape: 'straight', waterlogged: 'false' })
  }
  return { structure, metadata: { name: 'room' } }
}
