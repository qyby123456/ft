export default function build(api) {
  const structure = api.createStructure([29, 18, 29])
  const put = (blockX, blockY, blockZ, name, properties = {}) => api.setBlock(structure, [blockX, blockY, blockZ], `minecraft:${name}`, properties)
  const fill = (minimum, maximum, name) => api.fill(structure, minimum, maximum, `minecraft:${name}`)
  const step = (blockX, blockY, blockZ, facing, half = 'bottom') => put(blockX, blockY, blockZ, 'polished_blackstone_brick_stairs', {facing, half, shape: 'straight', waterlogged: 'false'})
  fill([0, 0, 0], [28, 17, 28], 'air')
  fill([0, 0, 0], [28, 0, 28], 'basalt[axis=y]')
  for (let blockX = 0; blockX < 29; blockX++) {
    for (let blockZ = 0; blockZ < 29; blockZ++) {
      const grain = (blockX * 13 + blockZ * 19 + blockX * blockZ) % 17
      put(blockX, 1, blockZ, grain < 3 ? 'blackstone' : grain < 5 ? 'cracked_polished_blackstone_bricks' : 'polished_blackstone')
    }
  }
  for (let blockY = 2; blockY < 18; blockY++) {
    for (let offset = 0; offset < 29; offset++) {
      const material = (offset + blockY * 3) % 9 < 2 ? 'blackstone' : 'polished_blackstone_bricks'
      for (const [blockX, blockZ] of [[0, offset], [28, offset], [offset, 0], [offset, 28]]) put(blockX, blockY, blockZ, material)
    }
  }
  fill([0, 17, 0], [28, 17, 28], 'blackstone')
  for (const edge of [1, 27]) {
    fill([edge, 15, 1], [edge, 16, 27], 'blackstone')
    fill([1, 15, edge], [27, 16, edge], 'blackstone')
  }
  for (const beamZ of [6, 14, 22]) fill([1, 16, beamZ], [27, 16, beamZ], 'polished_blackstone_bricks')
  for (const [pillarX, pillarZ] of [[1, 1], [26, 1], [1, 26], [26, 26]]) {
    fill([pillarX, 2, pillarZ], [pillarX + 1, 16, pillarZ + 1], 'basalt[axis=y]')
    fill([pillarX, 9, pillarZ], [pillarX + 1, 9, pillarZ + 1], 'glowstone')
    fill([pillarX, 10, pillarZ], [pillarX + 1, 10, pillarZ + 1], 'obsidian')
  }
  for (let blockX = 3; blockX <= 25; blockX++) {
    for (let blockZ = 3; blockZ <= 25; blockZ++) {
      const groove = blockX === 3 || blockX === 25 || blockZ === 3 || blockZ === 25
      const bridge = (blockZ === 3 && blockX >= 13 && blockX <= 15) || (blockZ === 25 && ((blockX >= 6 && blockX <= 8) || (blockX >= 20 && blockX <= 22)))
      if (groove && !bridge) {
        put(blockX, 1, blockZ, 'lava', {level: '0'})
        put(blockX, 2, blockZ, 'orange_stained_glass')
      }
    }
  }
  for (const [blockX, blockZ] of [[9, 4], [9, 24], [24, 12], [4, 12]]) put(blockX, 2, blockZ, 'polished_blackstone_bricks')
  const cascade = (blockX, blockZ, top, side) => {
    fill([blockX - 1, 1, blockZ - 1], [blockX + 1, top + 1, blockZ + 1], 'blackstone')
    for (let blockY = 2; blockY <= top; blockY++) {
      put(blockX, blockY, blockZ, 'lava', {level: '0'})
      put(blockX + side, blockY, blockZ, 'orange_stained_glass')
    }
    put(blockX, top + 1, blockZ, 'gilded_blackstone')
  }
  cascade(2, 21, 12, 1)
  cascade(26, 16, 10, -1)
  for (let blockX = 10; blockX <= 18; blockX++) {
    for (let blockZ = 10; blockZ <= 18; blockZ++) {
      const radius = Math.abs(blockX - 14) + Math.abs(blockZ - 14)
      if (radius <= 6) put(blockX, 2, blockZ, 'polished_blackstone_bricks')
      if (blockX >= 11 && blockX <= 17 && blockZ >= 11 && blockZ <= 17 && radius <= 4) put(blockX, 3, blockZ, 'polished_blackstone_bricks')
    }
  }
  for (let blockX = 13; blockX <= 15; blockX++) {
    step(blockX, 2, 10, 'south')
    step(blockX, 3, 11, 'south')
  }
  fill([13, 2, 14], [15, 3, 16], 'polished_blackstone_bricks')
  fill([13, 4, 14], [15, 6, 16], 'obsidian')
  put(14, 5, 15, 'lava', {level: '0'})
  put(14, 5, 14, 'orange_stained_glass')
  const ridges = [
    [12, 13, 5], [12, 14, 6], [13, 13, 7], [13, 16, 7],
    [14, 16, 9], [14, 17, 6], [15, 13, 5], [15, 17, 5],
    [16, 14, 6], [16, 15, 7], [16, 16, 5], [17, 15, 5],
  ]
  for (const [blockX, blockZ, height] of ridges) {
    fill([blockX, 2, blockZ], [blockX, 3, blockZ], 'polished_blackstone_bricks')
    fill([blockX, 4, blockZ], [blockX, height, blockZ], 'obsidian')
    if (height < 8) put(blockX, height, blockZ, (blockX + blockZ) % 2 ? 'shroomlight' : 'gilded_blackstone')
  }
  put(14, 10, 16, 'obsidian')
  for (const [blockX, blockZ, lampY] of [[7, 6, 10], [21, 6, 11], [7, 22, 11], [21, 22, 9]]) {
    for (let chainY = lampY + 1; chainY < 17; chainY++) put(blockX, chainY, blockZ, 'chain', {axis: 'y', waterlogged: 'false'})
    put(blockX, lampY, blockZ, 'lantern', {hanging: 'true', waterlogged: 'false'})
  }
  for (const centerX of [7, 21]) {
    fill([centerX - 2, 2, 28], [centerX + 2, 9, 28], 'blackstone')
    fill([centerX - 1, 2, 28], [centerX + 1, 6, 28], 'air')
    put(centerX, 7, 28, 'air')
    step(centerX - 2, 7, 28, 'east', 'top')
    step(centerX + 2, 7, 28, 'west', 'top')
    put(centerX, 8, 28, 'gilded_blackstone')
  }
  fill([13, 2, 0], [15, 6, 0], 'air')
  return {structure, metadata: {name: 'room', id: 'S6-REDO-01-R2', reference: 'S6-01-large-chamber.png', revision: 'tapered-peaks-and-door-scale-review'}}
}
