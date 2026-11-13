export default function build(api) {
  const structure = api.createStructure([21, 13, 21])
  const block = (blockX, blockY, blockZ, name, properties = {}) => api.setBlock(structure, [blockX, blockY, blockZ], `minecraft:${name}`, properties)
  const box = (minimum, maximum, name) => api.fill(structure, minimum, maximum, `minecraft:${name}`)
  const stair = (blockX, blockY, blockZ, facing, half = 'bottom') => block(blockX, blockY, blockZ, 'polished_blackstone_brick_stairs', {facing, half, shape: 'straight', waterlogged: 'false'})
  box([0, 0, 0], [20, 12, 20], 'air')
  box([0, 0, 0], [20, 0, 20], 'basalt[axis=y]')
  for (let blockX = 0; blockX <= 20; blockX++) {
    for (let blockZ = 0; blockZ <= 20; blockZ++) {
      const grain = (blockX * 17 + blockZ * 31 + blockX * blockZ) % 13
      block(blockX, 1, blockZ, grain < 2 ? 'cracked_polished_blackstone_bricks' : grain < 5 ? 'blackstone' : 'polished_blackstone')
    }
  }
  for (let blockY = 2; blockY <= 11; blockY++) {
    for (let offset = 0; offset <= 20; offset++) {
      const masonry = (offset * 3 + blockY * 7) % 11 < 3 ? 'cracked_polished_blackstone_bricks' : 'polished_blackstone_bricks'
      for (const [blockX, blockZ] of [[0, offset], [20, offset], [offset, 0], [offset, 20]]) block(blockX, blockY, blockZ, masonry)
    }
  }
  box([0, 12, 0], [20, 12, 20], 'blackstone')
  for (const offset of [1, 19]) {
    box([offset, 10, 1], [offset, 11, 19], 'blackstone')
    box([1, 10, offset], [19, 11, offset], 'blackstone')
  }
  for (const beamZ of [4, 10, 16]) {
    box([1, 11, beamZ], [19, 11, beamZ], 'polished_blackstone_bricks')
    for (const blockX of [2, 18]) block(blockX, 10, beamZ, 'smooth_basalt')
  }
  const lava = (blockX, blockY, blockZ) => block(blockX, blockY, blockZ, 'lava', {level: '0'})
  for (let blockX = 2; blockX <= 18; blockX++) {
    for (let blockZ = 2; blockZ <= 18; blockZ++) {
      const groove = (blockZ === 3 && blockX >= 3 && blockX <= 17) || (blockX === 3 && blockZ >= 3 && blockZ <= 17) || (blockX === 17 && blockZ >= 5 && blockZ <= 18) || (blockZ === 18 && blockX >= 3 && blockX <= 17)
      const bridge = (blockZ === 3 && blockX >= 9 && blockX <= 11) || (blockZ === 18 && ((blockX >= 4 && blockX <= 6) || (blockX >= 14 && blockX <= 16)))
      if (groove && !bridge) {
        lava(blockX, 1, blockZ)
        block(blockX, 2, blockZ, 'orange_stained_glass')
      }
    }
  }
  for (const [pillarX, pillarZ] of [[1, 1], [18, 1], [1, 18], [18, 18]]) {
    box([pillarX, 2, pillarZ], [pillarX + 1, 11, pillarZ + 1], 'basalt[axis=y]')
    box([pillarX, 6, pillarZ], [pillarX + 1, 6, pillarZ + 1], 'glowstone')
    box([pillarX, 7, pillarZ], [pillarX + 1, 7, pillarZ + 1], 'obsidian')
  }
  block(12, 2, 4, 'polished_blackstone_bricks')
  block(7, 2, 17, 'polished_blackstone_bricks')
  const cascade = (blockX, blockZ, height, windowSide) => {
    box([blockX - 1, 1, blockZ - 1], [blockX + 1, height + 1, blockZ + 1], 'blackstone')
    for (let blockY = 2; blockY <= height; blockY++) {
      lava(blockX, blockY, blockZ)
      block(blockX + windowSide, blockY, blockZ, 'orange_stained_glass')
    }
    block(blockX, height + 1, blockZ, 'gilded_blackstone')
  }
  cascade(2, 15, 9, 1)
  cascade(18, 10, 7, -1)
  for (let blockX = 6; blockX <= 14; blockX++) {
    for (let blockZ = 6; blockZ <= 14; blockZ++) {
      const diamond = Math.abs(blockX - 10) + Math.abs(blockZ - 10)
      if (diamond <= 6) block(blockX, 2, blockZ, 'polished_blackstone_bricks')
      if (blockX >= 7 && blockX <= 13 && blockZ >= 7 && blockZ <= 13 && diamond <= 4) block(blockX, 3, blockZ, 'polished_blackstone_bricks')
    }
  }
  for (let blockX = 9; blockX <= 11; blockX++) {
    stair(blockX, 2, 6, 'south')
    stair(blockX, 3, 7, 'south')
  }
  const thermalSpire = (blockX, blockZ, height) => {
    box([blockX - 1, 2, blockZ - 1], [blockX + 1, 3, blockZ + 1], 'polished_blackstone_bricks')
    box([blockX - 1, 4, blockZ - 1], [blockX + 1, height, blockZ + 1], 'obsidian')
    for (let blockY = 4; blockY < height; blockY++) {
      lava(blockX, blockY, blockZ)
      block(blockX, blockY, blockZ - 1, 'orange_stained_glass')
    }
    block(blockX, height + 1, blockZ, 'obsidian')
  }
  thermalSpire(9, 10, 6)
  thermalSpire(12, 11, 7)
  box([10, 4, 13], [10, 9, 13], 'obsidian')
  block(11, 4, 8, 'gilded_blackstone')
  for (const [blockX, blockZ, lampY] of [[5, 4, 7], [15, 4, 8], [5, 16, 8], [15, 16, 7]]) {
    for (let chainY = lampY + 1; chainY < 12; chainY++) block(blockX, chainY, blockZ, 'chain', {axis: 'y', waterlogged: 'false'})
    block(blockX, lampY, blockZ, 'lantern', {hanging: 'true', waterlogged: 'false'})
  }
  for (const centerX of [5, 15]) {
    box([centerX - 2, 2, 20], [centerX + 2, 7, 20], 'blackstone')
    box([centerX - 1, 2, 20], [centerX + 1, 4, 20], 'air')
    block(centerX, 5, 20, 'air')
    stair(centerX - 2, 5, 20, 'east', 'top')
    stair(centerX + 2, 5, 20, 'west', 'top')
    block(centerX, 6, 20, 'gilded_blackstone')
  }
  box([9, 2, 0], [11, 4, 0], 'air')
  return {structure, metadata: {name: 'room', id: 'S6-REDO-01', batch: 'S6-redo-single-20261104-01', reference: 'S6-01-large-chamber.png', construction: 'one fresh recipe, no archived structure imports'}}
}
