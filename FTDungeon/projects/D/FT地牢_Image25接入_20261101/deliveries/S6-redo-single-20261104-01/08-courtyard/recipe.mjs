export default function build(api) {
  const structure = api.createStructure([37, 23, 37])
  const put = (blockX, blockY, blockZ, name, properties = {}) => api.setBlock(structure, [blockX, blockY, blockZ], `minecraft:${name}`, properties)
  const fill = (from, to, name) => api.fill(structure, from, to, `minecraft:${name}`)
  fill([0, 0, 0], [36, 22, 36], 'air')
  fill([0, 0, 0], [36, 1, 36], 'blackstone')
  fill([0, 2, 0], [36, 2, 36], 'polished_blackstone')
  for (const wallX of [0, 34]) fill([wallX, 3, 0], [wallX + 2, 22, 36], 'polished_blackstone_bricks')
  for (const wallZ of [0, 34]) fill([0, 3, wallZ], [36, 22, wallZ + 2], 'polished_blackstone_bricks')
  for (const wallZ of [0, 34]) fill([17, 3, wallZ], [19, 7, wallZ + 2], 'air')
  for (let blockX = 3; blockX <= 33; blockX++) {
    for (let blockZ = 3; blockZ <= 33; blockZ++) {
      if (blockX <= 8 || blockX >= 28 || blockZ <= 8 || blockZ >= 28) put(blockX, 12, blockZ, 'polished_blackstone_bricks')
      if (blockX <= 9 || blockX >= 27 || blockZ <= 9 || blockZ >= 27) {
        put(blockX, 21, blockZ, 'polished_blackstone_bricks')
        put(blockX, 22, blockZ, 'blackstone')
      }
      if ((blockX * 3 + blockZ * 7) % 31 === 0) put(blockX, 2, blockZ, 'gilded_blackstone')
    }
  }
  for (const blockX of [9, 27]) {
    for (const blockZ of [9, 27]) {
      fill([blockX - 1, 3, blockZ - 1], [blockX + 1, 3, blockZ + 1], 'chiseled_polished_blackstone')
      fill([blockX, 4, blockZ], [blockX, 20, blockZ], 'obsidian')
      for (const blockY of [5, 11, 18]) put(blockX, blockY, blockZ, 'gilded_blackstone')
    }
  }
  for (let edge = 8; edge <= 28; edge++) {
    for (const fixed of [8, 28]) {
      put(edge, 13, fixed, 'nether_brick_fence')
      put(fixed, 13, edge, 'nether_brick_fence')
    }
  }
  for (const stairX of [4, 30]) {
    fill([stairX, 12, 8], [stairX + 2, 12, 17], 'air')
    for (let step = 0; step < 10; step++) {
      for (let blockX = stairX; blockX <= stairX + 2; blockX++) {
        if (step > 0) fill([blockX, 3, 8 + step], [blockX, 2 + step, 8 + step], 'polished_blackstone_bricks')
        put(blockX, 3 + step, 8 + step, 'polished_blackstone_brick_stairs', {facing: 'south', half: 'bottom', shape: 'straight', waterlogged: 'false'})
      }
    }
  }
  fill([13, 3, 13], [23, 3, 23], 'chiseled_polished_blackstone')
  fill([14, 3, 14], [22, 3, 22], 'orange_stained_glass')
  fill([14, 2, 14], [22, 2, 22], 'lava')
  for (let tier = 0; tier < 4; tier++) {
    fill([15 + tier, 3 + tier * 2, 15 + tier], [21 - tier, 4 + tier * 2, 21 - tier], tier % 2 === 0 ? 'obsidian' : 'crying_obsidian')
  }
  fill([18, 11, 18], [18, 14, 18], 'crying_obsidian')
  fill([18, 4, 14], [18, 10, 14], 'lava')
  fill([17, 4, 14], [17, 10, 14], 'obsidian')
  fill([19, 4, 14], [19, 10, 14], 'obsidian')
  fill([18, 4, 15], [18, 10, 15], 'obsidian')
  fill([18, 4, 13], [18, 10, 13], 'orange_stained_glass')
  put(18, 11, 14, 'obsidian')
  put(17, 11, 14, 'obsidian')
  for (const blockZ of [11, 25]) {
    for (let blockY = 4; blockY <= 19; blockY++) {
      put(1, blockY, blockZ, 'lava')
      put(2, blockY, blockZ, 'orange_stained_glass')
      put(35, blockY, blockZ, 'lava')
      put(34, blockY, blockZ, 'orange_stained_glass')
    }
  }
  for (const blockX of [11, 25]) {
    for (const blockZ of [7, 29]) {
      put(blockX, 2, blockZ, 'shroomlight')
      put(blockX, 19, blockZ, 'lantern', {hanging: 'true', waterlogged: 'false'})
      put(blockX, 20, blockZ, 'chain', {axis: 'y', waterlogged: 'false'})
    }
  }
  for (const [blockX, blockZ] of [[9, 7], [27, 7], [9, 29], [27, 29]]) {
    fill([blockX, 8, blockZ], [blockX, 12, blockZ], 'polished_blackstone_bricks')
    put(blockX, 8, blockZ, 'gilded_blackstone')
    put(blockX, 7, blockZ, 'lantern', {hanging: 'true', waterlogged: 'false'})
  }
  for (const [blockX, blockZ] of [[4, 21], [32, 21], [12, 5], [24, 5], [12, 31], [24, 31]]) {
    put(blockX, 13, blockZ, 'soul_soil')
    put(blockX, 14, blockZ, 'crimson_roots')
  }
  for (const blockZ of [5, 11, 25, 31]) put(18, 2, blockZ, 'shroomlight')
  return {structure, metadata: {name: 'room', id: 'S6-REDO-08', reference: 'S6-08-courtyard.png', composition: 'Central sealed tier fountain, dry ground ring, accessible upper gallery and real skylight'}}
}
