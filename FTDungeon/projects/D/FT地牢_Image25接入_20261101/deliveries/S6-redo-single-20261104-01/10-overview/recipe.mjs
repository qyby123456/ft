export default function build(api) {
  const structure = api.createStructure([65, 18, 73])
  const put = (blockX, blockY, blockZ, name, properties = {}) => api.setBlock(structure, [blockX, blockY, blockZ], `minecraft:${name}`, properties)
  const fill = (from, to, name) => api.fill(structure, from, to, `minecraft:${name}`)
  fill([0, 0, 0], [64, 17, 72], 'air')
  const rooms = [
    ['entry', 27, 0, 37, 14, 9],
    ['court', 20, 14, 44, 38, 10],
    ['west-medium', 0, 24, 20, 46, 11],
    ['west-pool', 0, 48, 22, 72, 13],
    ['great-hall', 22, 42, 46, 72, 16],
    ['treasure', 46, 50, 64, 72, 13],
    ['east-small', 48, 14, 64, 30, 9],
    ['court-hall-link', 28, 38, 36, 44, 9],
    ['west-link', 7, 44, 13, 50, 8],
    ['L-west-arm', 42, 32, 60, 38, 9],
    ['L-east-arm', 54, 28, 60, 52, 9]
  ]
  for (const [name, leftX, frontZ, rightX, backZ, roofY] of rooms) {
    fill([leftX, 0, frontZ], [rightX, 1, backZ], 'blackstone')
    fill([leftX, 2, frontZ], [rightX, 2, backZ], 'polished_blackstone')
    for (const wallX of [leftX, rightX]) fill([wallX, 3, frontZ], [wallX, roofY, backZ], 'polished_blackstone_bricks')
    for (const wallZ of [frontZ, backZ]) fill([leftX, 3, wallZ], [rightX, roofY, wallZ], 'polished_blackstone_bricks')
    if (name !== 'court') fill([leftX, roofY, frontZ], [rightX, roofY + 1, backZ], 'polished_blackstone_bricks')
  }
  const passages = [
    [[31, 3, 0], [33, 7, 1]],
    [[31, 3, 13], [33, 7, 15]],
    [[19, 3, 31], [21, 7, 33]],
    [[9, 3, 43], [11, 7, 51]],
    [[31, 3, 37], [33, 7, 45]],
    [[42, 3, 34], [57, 7, 36]],
    [[56, 3, 28], [58, 7, 53]],
    [[45, 3, 59], [47, 7, 61]]
  ]
  for (const [from, to] of passages) fill(from, to, 'air')
  fill([9, 0, 47], [11, 2, 47], 'polished_blackstone_bricks')
  for (let blockX = 31; blockX <= 33; blockX++) {
    for (let blockZ = 39; blockZ <= 41; blockZ++) put(blockX, 2, blockZ, 'polished_blackstone')
  }
  for (let blockX = 45; blockX <= 53; blockX++) {
    for (let blockZ = 34; blockZ <= 36; blockZ++) put(blockX, 2, blockZ, 'polished_blackstone')
  }
  for (let blockZ = 28; blockZ <= 51; blockZ++) {
    for (let blockX = 56; blockX <= 58; blockX++) put(blockX, 2, blockZ, 'polished_blackstone')
  }
  const pool = (leftX, frontZ, rightX, backZ) => {
    fill([leftX - 1, 2, frontZ - 1], [rightX + 1, 2, backZ + 1], 'chiseled_polished_blackstone')
    fill([leftX, 1, frontZ], [rightX, 1, backZ], 'lava')
    fill([leftX, 2, frontZ], [rightX, 2, backZ], 'orange_stained_glass')
  }
  pool(27, 23, 35, 29)
  pool(5, 55, 14, 63)
  pool(51, 19, 54, 23)
  pool(59, 41, 59, 46)
  for (const [blockX, blockZ] of [[26, 48], [42, 48], [26, 65], [42, 65], [29, 55], [39, 55]]) {
    fill([blockX - 1, 3, blockZ - 1], [blockX + 1, 3, blockZ + 1], 'chiseled_polished_blackstone')
    fill([blockX, 4, blockZ], [blockX, 14, blockZ], 'obsidian')
    for (const blockY of [5, 12]) put(blockX, blockY, blockZ, 'gilded_blackstone')
  }
  fill([30, 3, 66], [38, 4, 69], 'chiseled_polished_blackstone')
  for (let blockX = 32; blockX <= 36; blockX++) {
    put(blockX, 3, 64, 'polished_blackstone_brick_stairs', {facing: 'south', half: 'bottom', shape: 'straight', waterlogged: 'false'})
    put(blockX, 3, 65, 'polished_blackstone_bricks')
    put(blockX, 4, 65, 'polished_blackstone_brick_stairs', {facing: 'south', half: 'bottom', shape: 'straight', waterlogged: 'false'})
  }
  fill([33, 5, 68], [35, 7, 69], 'obsidian')
  fill([34, 5, 68], [34, 6, 68], 'shroomlight')
  for (const [blockX, blockZ] of [[4, 28], [16, 28], [4, 41], [16, 41]]) {
    put(blockX, 3, blockZ, 'gilded_blackstone')
    fill([blockX, 4, blockZ], [blockX, 8, blockZ], 'obsidian')
  }
  fill([7, 3, 40], [13, 3, 42], 'chiseled_polished_blackstone')
  put(10, 4, 41, 'shroomlight')
  for (const [blockX, blockZ] of [[5, 53], [16, 53], [5, 67], [16, 67]]) {
    put(blockX, 3, blockZ, 'chiseled_polished_blackstone')
    put(blockX, 4, blockZ, 'gilded_blackstone')
  }
  for (const [blockX, blockZ, label] of [[55, 66, '总览宝库'], [5, 36, '西厅补给'], [59, 26, '东室补给']]) {
    api.setBlock(structure, [blockX, 3, blockZ], 'minecraft:chest', {facing: 'north', type: 'single', waterlogged: 'false'}, api.createBlockNbt({id: 'minecraft:chest', CustomName: JSON.stringify({text: label}), Items: new api.NbtList([], 10)}))
  }
  fill([50, 3, 69], [60, 8, 69], 'chiseled_polished_blackstone')
  for (const blockX of [51, 55, 59]) put(blockX, 7, 68, 'gold_block')
  fill([55, 7, 68], [55, 8, 68], 'gilded_blackstone')
  fill([55, 7, 69], [55, 8, 69], 'gilded_blackstone')
  for (const [blockX, blockZ] of [[24, 18], [40, 18], [24, 34], [40, 34], [30, 3], [34, 3]]) {
    put(blockX, 3, blockZ, 'chiseled_polished_blackstone')
    put(blockX, 4, blockZ, 'shroomlight')
  }
  for (const [name, leftX, frontZ, rightX, backZ, roofY] of rooms.slice(0, 7)) {
    if (name === 'court') continue
    for (const blockX of [leftX + 3, rightX - 3]) {
      const blockZ = frontZ + 4
      put(blockX, roofY - 1, blockZ, 'lantern', {hanging: 'true', waterlogged: 'false'})
    }
  }
  for (const [blockX, blockZ] of [[32, 7], [32, 17], [24, 32], [10, 32], [10, 50], [18, 67], [32, 41], [34, 60], [46, 60], [55, 62], [57, 35], [57, 46]]) put(blockX, 2, blockZ, 'shroomlight')
  return {structure, metadata: {name: 'room', id: 'S6-REDO-10', reference: 'S6-10-overview.png', rooms, composition: 'Complete connected dungeon: entry, court, medium shrine, pool chamber, great hall, treasure, small room and genuine L corridor'}}
}
