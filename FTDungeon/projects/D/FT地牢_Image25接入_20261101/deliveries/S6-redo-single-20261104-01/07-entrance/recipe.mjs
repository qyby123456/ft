export default function build(api) {
  const structure = api.createStructure([27, 16, 33])
  const put = (blockX, blockY, blockZ, name, properties = {}) => api.setBlock(structure, [blockX, blockY, blockZ], `minecraft:${name}`, properties)
  const fill = (from, to, name) => api.fill(structure, from, to, `minecraft:${name}`)
  fill([0, 0, 0], [26, 15, 32], 'air')
  fill([0, 0, 8], [26, 3, 32], 'blackstone')
  fill([10, 0, 0], [16, 2, 4], 'polished_blackstone_bricks')
  fill([9, 0, 5], [17, 3, 7], 'polished_blackstone_bricks')
  fill([0, 4, 8], [26, 4, 32], 'polished_blackstone')
  for (let blockX = 10; blockX <= 16; blockX++) {
    put(blockX, 3, 4, 'polished_blackstone_brick_stairs', {facing: 'south', half: 'bottom', shape: 'straight', waterlogged: 'false'})
    put(blockX, 4, 7, 'polished_blackstone_brick_stairs', {facing: 'south', half: 'bottom', shape: 'straight', waterlogged: 'false'})
  }
  for (const wallX of [0, 24]) fill([wallX, 5, 8], [wallX + 2, 10, 32], 'polished_blackstone_bricks')
  fill([0, 5, 8], [26, 10, 10], 'polished_blackstone_bricks')
  fill([12, 5, 8], [13, 8, 10], 'air')
  for (const blockX of [11, 14]) fill([blockX, 5, 7], [blockX, 9, 10], 'chiseled_polished_blackstone')
  fill([10, 10, 7], [15, 11, 10], 'chiseled_polished_blackstone')
  for (const blockX of [12, 13]) {
    const hinge = blockX === 12 ? 'left' : 'right'
    for (const [blockY, half] of [[5, 'lower'], [6, 'upper']]) put(blockX, blockY, 9, 'dark_oak_door', {facing: 'north', half, hinge, open: 'true', powered: 'false'})
  }
  fill([0, 5, 26], [26, 13, 28], 'polished_blackstone_bricks')
  fill([11, 5, 26], [15, 10, 28], 'air')
  fill([12, 11, 26], [14, 11, 28], 'air')
  fill([10, 5, 29], [10, 12, 32], 'polished_blackstone_bricks')
  fill([16, 5, 29], [16, 12, 32], 'polished_blackstone_bricks')
  fill([10, 13, 26], [16, 14, 32], 'polished_blackstone_bricks')
  fill([0, 5, 32], [9, 12, 32], 'polished_blackstone_bricks')
  fill([17, 5, 32], [26, 12, 32], 'polished_blackstone_bricks')
  for (const blockX of [9, 17]) fill([blockX, 5, 25], [blockX, 12, 28], 'chiseled_polished_blackstone')
  for (const [leftX, rightX] of [[3, 8], [18, 23]]) {
    fill([leftX, 5, 25], [rightX, 10, 25], 'polished_blackstone_bricks')
    fill([leftX, 10, 23], [rightX, 11, 25], 'polished_blackstone_bricks')
    fill([leftX, 5, 23], [leftX, 9, 24], 'chiseled_polished_blackstone')
    fill([rightX, 5, 23], [rightX, 9, 24], 'chiseled_polished_blackstone')
    for (const offset of [1, 3]) {
      put(leftX + offset, 5, 24, 'brown_terracotta')
      put(leftX + offset, 6, 24, 'chiseled_polished_blackstone')
    }
    put(leftX + 2, 9, 24, 'lantern', {hanging: 'true', waterlogged: 'false'})
  }
  for (const wallX of [1, 25]) {
    for (const blockZ of [9, 20, 28]) {
      fill([wallX, 5, blockZ], [wallX, 13, blockZ], 'obsidian')
      put(wallX, 4, blockZ, 'gilded_blackstone')
    }
  }
  for (const blockX of [5, 6, 20, 21]) {
    for (let blockZ = 14; blockZ <= 20; blockZ++) {
      put(blockX, 3, blockZ, 'lava', {level: '0'})
      put(blockX, 4, blockZ, 'orange_stained_glass')
    }
  }
  for (const blockZ of [16, 17]) {
    for (let blockY = 5; blockY <= 9; blockY++) {
      put(2, blockY, blockZ, 'lava', {level: '0'})
      put(3, blockY, blockZ, 'orange_stained_glass')
      put(24, blockY, blockZ, 'lava', {level: '0'})
      put(23, blockY, blockZ, 'orange_stained_glass')
    }
  }
  fill([3, 5, 12], [3, 8, 15], 'chiseled_polished_blackstone')
  for (let blockZ = 12; blockZ <= 15; blockZ++) {
    for (let blockY = 6; blockY <= 7; blockY++) put(4, blockY, blockZ, (blockY + blockZ) % 3 === 0 ? 'chiseled_sandstone' : 'smooth_sandstone')
  }
  fill([7, 5, 12], [8, 5, 13], 'chiseled_polished_blackstone')
  fill([7, 6, 12], [8, 6, 13], 'shroomlight')
  for (const [blockX, blockZ] of [[4, 11], [22, 11], [10, 25], [16, 25]]) {
    put(blockX, 8, blockZ, 'gilded_blackstone')
    put(blockX, 7, blockZ, 'lantern', {hanging: 'true', waterlogged: 'false'})
  }
  for (const blockX of [10, 16]) {
    for (let blockY = 5; blockY <= 7; blockY++) put(blockX, blockY, 7, 'orange_stained_glass')
    put(blockX, 8, 7, 'gilded_blackstone')
  }
  for (let blockX = 3; blockX <= 23; blockX++) {
    for (let blockZ = 11; blockZ <= 25; blockZ++) {
      if ((blockX * 7 + blockZ * 3) % 19 === 0 && ![5, 6, 20, 21].includes(blockX)) put(blockX, 4, blockZ, 'gilded_blackstone')
    }
  }
  for (const blockZ of [12, 18, 24, 29]) put(13, 4, blockZ, 'shroomlight')
  return {structure, metadata: {name: 'room', id: 'S6-REDO-07', reference: 'S6-07-entrance.png', composition: 'Actual paired door, two stair rises, open guard court, flanking niches and rear arch passage'}}
}
