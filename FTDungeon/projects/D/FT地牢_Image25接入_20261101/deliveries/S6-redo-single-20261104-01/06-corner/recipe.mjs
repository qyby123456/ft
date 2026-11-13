export default function build(api) {
  const structure = api.createStructure([29, 12, 29])
  const put = (blockX, blockY, blockZ, name, properties = {}) => api.setBlock(structure, [blockX, blockY, blockZ], `minecraft:${name}`, properties)
  const fill = (from, to, name) => api.fill(structure, from, to, `minecraft:${name}`)
  fill([0, 0, 0], [28, 11, 28], 'air')
  const footprint = (blockX, blockZ) => blockX <= 14 || blockZ >= 14
  const inside = (blockX, blockZ) => (blockX >= 3 && blockX <= 11 && blockZ >= 2 && blockZ <= 25) || (blockX >= 3 && blockX <= 26 && blockZ >= 17 && blockZ <= 25)
  for (let blockX = 0; blockX < 29; blockX++) {
    for (let blockZ = 0; blockZ < 29; blockZ++) {
      if (!footprint(blockX, blockZ)) continue
      for (let blockY = 0; blockY < 12; blockY++) {
        let name = blockY === 0 ? 'basalt' : 'polished_blackstone_bricks'
        if (blockY === 1) name = 'blackstone'
        if (blockY === 2) name = (blockX + blockZ) % 7 === 0 ? 'gilded_blackstone' : 'polished_blackstone'
        if (blockY >= 3 && blockY <= 9 && inside(blockX, blockZ)) name = 'air'
        if (blockY === 10 && (blockZ % 6 === 0 || blockX % 6 === 0)) name = 'chiseled_polished_blackstone'
        put(blockX, blockY, blockZ, name)
      }
    }
  }
  fill([6, 3, 0], [8, 7, 2], 'air')
  fill([26, 3, 20], [28, 7, 22], 'air')
  for (let blockZ = 2; blockZ <= 25; blockZ++) {
    for (const blockX of [5, 9]) put(blockX, 2, blockZ, 'polished_blackstone_bricks')
  }
  for (let blockX = 5; blockX <= 28; blockX++) {
    for (const blockZ of [19, 23]) put(blockX, 2, blockZ, 'polished_blackstone_bricks')
  }
  for (const [blockX, blockZ] of [[7, 4], [7, 10], [7, 16], [7, 22], [13, 21], [19, 21], [25, 21]]) {
    put(blockX, 2, blockZ, 'shroomlight')
  }
  for (const blockZ of [5, 11]) {
    for (const blockX of [3, 11]) {
      put(blockX, 3, blockZ, 'gilded_blackstone')
      for (let blockY = 4; blockY <= 8; blockY++) put(blockX, blockY, blockZ, 'obsidian')
      put(blockX, 9, blockZ, 'gilded_blackstone')
    }
  }
  for (const blockX of [17, 23]) {
    for (const blockZ of [18, 25]) {
      put(blockX, 3, blockZ, 'gilded_blackstone')
      for (let blockY = 4; blockY <= 8; blockY++) put(blockX, blockY, blockZ, 'obsidian')
      put(blockX, 9, blockZ, 'gilded_blackstone')
    }
  }
  for (const blockZ of [7, 8, 13, 14]) {
    for (let blockY = 3; blockY <= 7; blockY++) {
      put(2, blockY, blockZ, 'lava', {level: '0'})
      put(3, blockY, blockZ, 'orange_stained_glass')
    }
  }
  for (const blockX of [19, 20, 25, 26]) {
    for (let blockY = 3; blockY <= 7; blockY++) {
      put(blockX, blockY, 16, 'lava', {level: '0'})
      put(blockX, blockY, 17, 'orange_stained_glass')
    }
  }
  for (const [blockX, blockZ] of [[4, 5], [4, 11], [17, 24], [23, 24]]) {
    put(blockX, 3, blockZ, 'chiseled_polished_blackstone')
    put(blockX, 4, blockZ, 'gilded_blackstone')
    put(blockX, 5, blockZ, 'lantern', {hanging: 'false', waterlogged: 'false'})
  }
  fill([11, 3, 16], [13, 3, 17], 'chiseled_polished_blackstone')
  put(12, 4, 17, 'gilded_blackstone')
  put(12, 5, 17, 'lantern', {hanging: 'false', waterlogged: 'false'})
  put(12, 6, 17, 'air')
  put(12, 7, 17, 'gilded_blackstone')
  put(11, 7, 17, 'gilded_blackstone')
  put(13, 7, 17, 'gilded_blackstone')
  return {structure, metadata: {name: 'room', id: 'S6-REDO-06', reference: 'S6-06-corner.png', composition: 'Actual L corridor around solid inner corner; north and east ports'}}
}
