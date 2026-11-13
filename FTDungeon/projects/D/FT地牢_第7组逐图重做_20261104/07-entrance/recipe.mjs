export default function build(api) {
  const structure = api.createStructure([27, 16, 31])
  const put = (blockX, blockY, blockZ, block, properties = {}, data) => {
    const defaults = {}
    if (['mycelium', 'podzol'].includes(block)) defaults.snowy = 'false'
    if (['mushroom_stem', 'red_mushroom_block'].includes(block)) {
      for (const face of ['north', 'east', 'south', 'west', 'up', 'down']) defaults[face] = 'true'
    }
    api.setBlock(structure, [blockX, blockY, blockZ], `minecraft:${block}`, { ...defaults, ...properties }, data)
  }
  const fill = (minimum, maximum, block, properties = {}) => {
    for (let blockX = minimum[0]; blockX <= maximum[0]; blockX++) {
      for (let blockY = minimum[1]; blockY <= maximum[1]; blockY++) {
        for (let blockZ = minimum[2]; blockZ <= maximum[2]; blockZ++) put(blockX, blockY, blockZ, block, properties)
      }
    }
  }
  fill([0, 0, 0], [26, 15, 30], 'air')
  fill([0, 0, 0], [26, 0, 30], 'deepslate_bricks')
  for (let blockX = 0; blockX <= 26; blockX++) {
    for (let blockZ = 0; blockZ <= 25; blockZ++) {
      const patch = (blockX * 19 + blockZ * 7 + Math.floor(blockZ / 3)) % 23
      put(blockX, 1, blockZ, patch < 6 ? 'mycelium' : patch < 10 ? 'purple_terracotta' : patch < 15 ? 'rooted_dirt' : 'mossy_stone_bricks')
      if (blockX === 0 || blockX === 26 || blockZ === 0 || blockZ === 25) {
        for (let blockY = 2; blockY <= 14; blockY++) put(blockX, blockY, blockZ, blockY >= 10 ? 'rooted_dirt' : (blockX * 3 + blockY * 7 + blockZ) % 11 < 4 ? 'mossy_stone_bricks' : 'deepslate_bricks')
      }
      put(blockX, 15, blockZ, 'deepslate_tiles')
    }
  }
  for (const [fromX, toX] of [[1, 6], [20, 25]]) {
    fill([fromX, 7, 14], [toX, 8, 24], 'mossy_stone_bricks')
    fill([fromX, 9, 14], [toX, 9, 24], 'mycelium')
  }
  for (const postX of [2, 6, 20, 24]) fill([postX, 2, 15], [postX, 6, 15], 'stone_bricks')
  for (const deskX of [3, 21]) {
    fill([deskX, 2, 20], [deskX, 3, 21], 'dark_oak_planks')
    fill([deskX + 3, 2, 20], [deskX + 3, 3, 21], 'dark_oak_planks')
    fill([deskX, 4, 20], [deskX + 3, 4, 21], 'dark_oak_planks')
  }
  fill([23, 4, 17], [25, 7, 19], 'stripped_oak_log', { axis: 'y' })
  for (let blockY = 5; blockY <= 6; blockY++) {
    for (let blockZ = 17; blockZ <= 19; blockZ++) put(22, blockY, blockZ, blockY + blockZ === 23 || blockY + blockZ === 25 ? 'moss_block' : 'birch_planks')
  }
  put(1, 6, 20, 'red_wall_banner', { facing: 'east' }, api.createBlockNbt({ id: 'minecraft:banner' }))
  for (const [crateX, crateY, crateZ] of [[4, 2, 10], [5, 2, 10], [4, 3, 10], [5, 2, 11], [7, 2, 14], [22, 2, 13]]) put(crateX, crateY, crateZ, 'barrel', { facing: 'up', open: 'false' }, api.createBlockNbt({ id: 'minecraft:barrel' }))
  fill([18, 2, 16], [20, 2, 18], 'stone_bricks')
  put(19, 3, 17, 'shroomlight')
  put(19, 4, 17, 'campfire', { facing: 'north', lit: 'false', signal_fire: 'false', waterlogged: 'false' }, api.createBlockNbt({ id: 'minecraft:campfire' }))
  fill([19, 1, 22], [23, 1, 24], 'polished_blackstone_bricks')
  fill([20, 1, 23], [22, 1, 23], 'water', { level: '0' })
  const mushroom = (stemX, stemY, stemZ, height, radius) => {
    fill([stemX, stemY, stemZ], [stemX, stemY + height - 1, stemZ], 'mushroom_stem')
    for (let offsetX = -radius; offsetX <= radius; offsetX++) {
      for (let offsetZ = -radius; offsetZ <= radius; offsetZ++) {
        if (Math.abs(offsetX) === radius && Math.abs(offsetZ) === radius) continue
        put(stemX + offsetX, stemY + height, stemZ + offsetZ, 'red_mushroom_block')
        if (Math.abs(offsetX) < radius && Math.abs(offsetZ) < radius) put(stemX + offsetX, stemY + height + 1, stemZ + offsetZ, 'red_mushroom_block')
      }
    }
  }
  mushroom(4, 10, 18, 3, 3)
  mushroom(22, 10, 17, 2, 3)
  mushroom(23, 10, 23, 2, 1)
  for (const [lanternX, lanternY, lanternZ] of [[4, 6, 18], [22, 6, 21]]) put(lanternX, lanternY, lanternZ, 'lantern', { hanging: 'true', waterlogged: 'false' })
  for (const [lampX, lampZ] of [[1, 7], [25, 7], [8, 25], [18, 25]]) put(lampX, 5, lampZ, 'shroomlight')
  for (const [fungusX, fungusY, fungusZ] of [[2, 10, 16], [5, 10, 22], [21, 10, 21], [8, 2, 8], [18, 2, 9], [24, 2, 11], [2, 2, 24], [24, 2, 26]]) {
    put(fungusX, fungusY - 1, fungusZ, 'mycelium')
    put(fungusX, fungusY, fungusZ, 'red_mushroom')
  }
  for (const [crystalX, crystalZ] of [[2, 12], [24, 9], [5, 27], [21, 27]]) {
    put(crystalX, 1, crystalZ, 'mossy_stone_bricks')
    put(crystalX, 2, crystalZ, 'amethyst_block')
    put(crystalX, 3, crystalZ, 'amethyst_cluster', { facing: 'up', waterlogged: 'false' })
  }
  for (const [vineX, side] of [[1, 'west'], [25, 'east']]) {
    for (const vineZ of [5, 12]) {
      for (let blockY = 7; blockY <= 12; blockY++) put(vineX, blockY, vineZ, 'vine', { [side]: 'true', up: 'false' })
    }
  }
  for (let blockX = 10; blockX <= 16; blockX++) {
    for (let blockY = 2; blockY <= 8; blockY++) put(blockX, blockY, 0, blockX >= 12 && blockX <= 14 && blockY <= 5 || blockX === 13 && blockY === 6 ? 'air' : 'stone_bricks')
    put(blockX, 1, 0, 'stone_bricks')
  }
  for (const blockX of [11, 15]) {
    fill([blockX, 2, 1], [blockX, 5, 1], 'stone_bricks')
    put(blockX, 4, 2, 'lantern', { hanging: 'false', waterlogged: 'false' })
    put(blockX, 3, 2, 'stone_bricks')
  }
  fill([10, 1, 25], [15, 1, 25], 'stone_bricks')
  fill([11, 2, 25], [14, 6, 25], 'stone_bricks')
  fill([12, 2, 25], [13, 4, 25], 'air')
  for (const [doorX, hinge] of [[12, 'right'], [13, 'left']]) {
    for (const [doorY, half] of [[2, 'lower'], [3, 'upper']]) put(doorX, doorY, 25, 'dark_oak_door', { facing: 'south', hinge, half, open: 'true', powered: 'false' })
  }
  fill([10, 1, 26], [15, 1, 26], 'stone_brick_slab', { type: 'bottom', waterlogged: 'false' })
  for (const pillarX of [10, 15]) {
    fill([pillarX, 2, 25], [pillarX, 6, 26], 'stone_bricks')
    put(pillarX, 4, 27, 'lantern', { hanging: 'false', waterlogged: 'false' })
    put(pillarX, 3, 27, 'stone_bricks')
  }
  return { structure, metadata: { name: 'room' } }
}
