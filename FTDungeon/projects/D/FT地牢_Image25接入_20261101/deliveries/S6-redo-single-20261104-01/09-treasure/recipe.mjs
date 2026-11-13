export default function build(api) {
  const structure = api.createStructure([33, 18, 35])
  const put = (blockX, blockY, blockZ, name, properties = {}) => api.setBlock(structure, [blockX, blockY, blockZ], `minecraft:${name}`, properties)
  const fill = (from, to, name) => api.fill(structure, from, to, `minecraft:${name}`)
  fill([0, 0, 0], [32, 17, 34], 'air')
  fill([0, 0, 8], [32, 3, 34], 'blackstone')
  fill([0, 4, 8], [32, 4, 34], 'polished_blackstone')
  fill([12, 0, 0], [20, 2, 4], 'polished_blackstone_bricks')
  fill([11, 0, 5], [21, 3, 7], 'polished_blackstone_bricks')
  for (let blockX = 12; blockX <= 20; blockX++) {
    put(blockX, 3, 4, 'polished_blackstone_brick_stairs', {facing: 'south', half: 'bottom', shape: 'straight', waterlogged: 'false'})
    put(blockX, 4, 7, 'polished_blackstone_brick_stairs', {facing: 'south', half: 'bottom', shape: 'straight', waterlogged: 'false'})
  }
  for (const wallX of [0, 30]) fill([wallX, 5, 8], [wallX + 2, 16, 34], 'polished_blackstone_bricks')
  for (const wallZ of [8, 32]) fill([0, 5, wallZ], [32, 16, wallZ + 2], 'polished_blackstone_bricks')
  fill([15, 5, 8], [17, 9, 10], 'air')
  for (const wallX of [0, 30]) fill([wallX, 5, 19], [wallX + 2, 9, 21], 'air')
  fill([0, 16, 8], [32, 17, 34], 'polished_blackstone_bricks')
  for (const leftX of [5, 22]) {
    fill([leftX, 3, 12], [leftX + 5, 3, 17], 'lava')
    fill([leftX, 4, 12], [leftX + 5, 4, 17], 'orange_stained_glass')
  }
  for (const blockZ of [24, 25]) {
    for (let blockY = 6; blockY <= 12; blockY++) {
      put(1, blockY, blockZ, 'lava')
      put(2, blockY, blockZ, 'orange_stained_glass')
      put(31, blockY, blockZ, 'lava')
      put(30, blockY, blockZ, 'orange_stained_glass')
    }
  }
  fill([13, 5, 23], [19, 6, 29], 'chiseled_polished_blackstone')
  for (let blockX = 14; blockX <= 18; blockX++) {
    put(blockX, 5, 21, 'polished_blackstone_brick_stairs', {facing: 'south', half: 'bottom', shape: 'straight', waterlogged: 'false'})
    put(blockX, 5, 22, 'polished_blackstone_bricks')
    put(blockX, 6, 22, 'polished_blackstone_brick_stairs', {facing: 'south', half: 'bottom', shape: 'straight', waterlogged: 'false'})
    put(blockX, 6, 23, 'gilded_blackstone')
  }
  for (const blockX of [11, 21]) {
    fill([blockX - 1, 5, 25], [blockX + 1, 6, 27], 'obsidian')
    fill([blockX - 1, 7, 25], [blockX + 1, 7, 27], 'gilded_blackstone')
    fill([blockX, 8, 26], [blockX, 11, 26], 'chiseled_polished_blackstone')
    fill([blockX - 1, 10, 26], [blockX + 1, 10, 26], 'polished_blackstone_bricks')
    fill([blockX, 12, 25], [blockX, 13, 26], 'chiseled_polished_blackstone')
    put(blockX, 12, 25, 'shroomlight')
  }
  for (const [blockX, blockY, blockZ, label] of [[16, 7, 26, '熔岩宝库'], [5, 5, 28, '西侧物资'], [27, 5, 28, '东侧物资']]) {
    api.setBlock(structure, [blockX, blockY, blockZ], 'minecraft:chest', {facing: 'north', type: 'single', waterlogged: 'false'}, api.createBlockNbt({id: 'minecraft:chest', CustomName: JSON.stringify({text: label}), Items: new api.NbtList([], 10)}))
  }
  for (const blockX of [9, 23]) fill([blockX, 9, 31], [blockX, 14, 31], 'red_terracotta')
  for (let offset = -3; offset <= 3; offset++) {
    put(16 + offset, 12, 31, 'gold_block')
    put(16, 12 + offset, 31, 'gold_block')
  }
  put(16, 12, 31, 'shroomlight')
  for (const blockX of [4, 28]) {
    fill([blockX, 5, 30], [blockX + 1, 5, 30], 'chiseled_polished_blackstone')
    put(blockX, 6, 30, 'brown_terracotta')
    put(blockX + 1, 6, 30, 'terracotta')
  }
  for (const [blockX, blockZ] of [[4, 11], [28, 11], [7, 26], [25, 26], [12, 30], [20, 30]]) {
    put(blockX, 14, blockZ, 'lantern', {hanging: 'true', waterlogged: 'false'})
    put(blockX, 15, blockZ, 'chain', {axis: 'y', waterlogged: 'false'})
  }
  for (const blockX of [12, 20]) {
    put(blockX, 5, 18, 'chiseled_polished_blackstone')
    put(blockX, 6, 18, 'shroomlight')
  }
  for (const blockZ of [11, 16, 20]) put(16, 4, blockZ, 'shroomlight')
  for (const blockX of [7, 25]) put(blockX, 4, 22, 'gilded_blackstone')
  return {structure, metadata: {name: 'room', id: 'S6-REDO-09', reference: 'S6-09-treasure.png', composition: 'Central real chest dais, twin stone guardians, paired sealed pools and side arch ports'}}
}
