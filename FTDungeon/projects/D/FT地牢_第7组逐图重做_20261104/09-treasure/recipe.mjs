export default function build(api) {
  const structure = api.createStructure([31, 19, 29])
  const put = (blockX, blockY, blockZ, block, properties = {}, data = undefined) => {
    const defaults = {}
    if (['mycelium', 'podzol'].includes(block)) defaults.snowy = 'false'
    if (['mushroom_stem', 'red_mushroom_block'].includes(block)) {
      for (const face of ['north', 'east', 'south', 'west', 'up', 'down']) defaults[face] = 'true'
    }
    api.setBlock(structure, [blockX, blockY, blockZ], `minecraft:${block}`, { ...defaults, ...properties }, data === undefined ? undefined : api.createBlockNbt(data))
  }
  const box = (minimum, maximum, block, properties = {}) => {
    for (let blockX = minimum[0]; blockX <= maximum[0]; blockX++) {
      for (let blockY = minimum[1]; blockY <= maximum[1]; blockY++) {
        for (let blockZ = minimum[2]; blockZ <= maximum[2]; blockZ++) put(blockX, blockY, blockZ, block, properties)
      }
    }
  }
  box([0, 0, 0], [30, 18, 28], 'air')
  box([0, 0, 0], [30, 0, 28], 'deepslate_bricks')
  for (let blockX = 0; blockX <= 30; blockX++) {
    for (let blockZ = 0; blockZ <= 28; blockZ++) {
      const pattern = (blockX * 11 + blockZ * 7 + Math.floor(blockZ / 3)) % 19
      const floor = pattern < 6 ? 'mycelium' : pattern < 10 ? 'purple_terracotta' : pattern < 14 ? 'rooted_dirt' : 'mossy_stone_bricks'
      put(blockX, 1, blockZ, floor)
      if (blockZ <= 16) put(blockX, 2, blockZ, floor)
      if (blockX === 0 || blockX === 30 || blockZ === 0 || blockZ === 28) {
        for (let blockY = 2; blockY <= 17; blockY++) {
          const seam = (blockX * 3 + blockY * 5 + blockZ * 13) % 17
          put(blockX, blockY, blockZ, blockY > 12 && seam < 9 ? 'rooted_dirt' : seam < 7 ? 'mossy_stone_bricks' : 'deepslate_bricks')
        }
      }
      put(blockX, 18, blockZ, 'rooted_dirt')
    }
  }
  box([11, 2, 17], [19, 2, 17], 'stone_brick_slab', { type: 'bottom', waterlogged: 'false' })
  box([11, 2, 16], [19, 2, 16], 'stone_bricks')
  for (const [fromX, toX, fromZ, toZ] of [[1, 7, 2, 8], [23, 29, 2, 8]]) {
    box([fromX, 3, fromZ], [toX, 5, toZ], 'rooted_dirt')
    box([fromX, 6, fromZ], [toX, 6, toZ], 'mycelium')
  }
  box([22, 7, 1], [28, 9, 3], 'rooted_dirt')
  box([22, 10, 1], [28, 10, 3], 'mycelium')
  const mushroom = (stemX, stemY, stemZ, height, radius) => {
    box([stemX, stemY, stemZ], [stemX, stemY + height - 1, stemZ], 'mushroom_stem')
    const capY = stemY + height
    for (let offsetX = -radius; offsetX <= radius; offsetX++) {
      for (let offsetZ = -radius; offsetZ <= radius; offsetZ++) {
        if (Math.abs(offsetX) === radius && Math.abs(offsetZ) === radius) continue
        put(stemX + offsetX, capY, stemZ + offsetZ, 'red_mushroom_block')
        if (Math.abs(offsetX) < radius && Math.abs(offsetZ) < radius) put(stemX + offsetX, capY + 1, stemZ + offsetZ, 'red_mushroom_block')
      }
    }
  }
  mushroom(5, 7, 5, 5, 3)
  mushroom(25, 7, 6, 5, 3)
  mushroom(25, 11, 2, 3, 2)
  for (const pillarX of [8, 22]) {
    box([pillarX, 3, 2], [pillarX, 12, 3], 'mossy_stone_bricks')
    put(pillarX, 10, 4, 'chiseled_stone_bricks')
  }
  box([8, 13, 2], [22, 13, 3], 'chiseled_stone_bricks')
  box([12, 3, 3], [18, 4, 7], 'stone_bricks')
  box([12, 3, 10], [18, 3, 10], 'stone_brick_slab', { type: 'bottom', waterlogged: 'false' })
  box([12, 3, 8], [18, 3, 9], 'stone_bricks')
  box([12, 4, 8], [18, 4, 8], 'stone_brick_slab', { type: 'bottom', waterlogged: 'false' })
  for (const pillarX of [10, 20]) {
    box([pillarX, 3, 5], [pillarX, 6, 5], 'mossy_stone_bricks')
    put(pillarX, 7, 5, 'shroomlight')
    put(pillarX, 8, 5, 'amethyst_block')
    put(pillarX, 9, 5, 'stone_brick_slab', { type: 'bottom', waterlogged: 'false' })
  }
  box([13, 4, 1], [17, 9, 1], 'amethyst_block')
  for (const lampX of [12, 18]) put(lampX, 6, 1, 'shroomlight')
  put(15, 5, 5, 'chest', { facing: 'south', type: 'single', waterlogged: 'false' }, { id: 'minecraft:chest', Items: [], CustomName: '{"text":"菌庭秘藏"}' })
  for (const bannerX of [9, 11, 19, 21]) {
    put(bannerX, 11, 1, bannerX % 4 === 1 ? 'red_wall_banner' : 'yellow_wall_banner', { facing: 'south' }, { id: 'minecraft:banner', Patterns: [{ Pattern: 'bs', Color: 4 }, { Pattern: 'mc', Color: 14 }] })
  }
  for (const [barrelX, barrelY, barrelZ] of [[3, 3, 10], [4, 3, 10], [5, 3, 10], [3, 4, 10], [25, 3, 13], [26, 3, 13]]) {
    put(barrelX, barrelY, barrelZ, 'barrel', { facing: 'south', open: 'false' }, { id: 'minecraft:barrel', Items: [] })
  }
  box([25, 3, 10], [27, 3, 10], 'spruce_planks')
  for (const potX of [25, 27]) put(potX, 4, 10, 'potted_brown_mushroom')
  for (const poolX of [7, 21]) {
    box([poolX - 1, 1, 19], [poolX + 3, 1, 23], 'polished_blackstone_bricks')
    box([poolX, 1, 20], [poolX + 2, 1, 22], 'water', { level: '0' })
    for (const postX of [poolX - 1, poolX + 3]) {
      put(postX, 2, 23, 'mossy_stone_bricks')
      put(postX, 3, 23, 'stone_brick_slab', { type: 'bottom', waterlogged: 'false' })
    }
  }
  for (const lampX of [10, 20]) {
    box([lampX, 2, 17], [lampX, 3, 17], 'mossy_stone_bricks')
    put(lampX, 4, 17, 'shroomlight')
  }
  for (const [plantX, plantY, plantZ] of [[2, 7, 5], [6, 7, 7], [24, 7, 4], [28, 7, 7], [23, 11, 2], [2, 2, 23], [4, 2, 25], [26, 2, 25], [28, 2, 22], [10, 3, 10], [20, 3, 10], [7, 3, 14], [23, 3, 14]]) {
    put(plantX, plantY - 1, plantZ, 'mycelium')
    put(plantX, plantY, plantZ, plantX % 2 ? 'brown_mushroom' : 'red_mushroom')
  }
  for (const crystalX of [2, 28]) {
    put(crystalX, 7, 7, 'amethyst_block')
    put(crystalX, 8, 7, 'amethyst_cluster', { facing: 'up', waterlogged: 'false' })
  }
  for (const wallX of [1, 29]) {
    for (const vineZ of [3, 8, 19, 25]) {
      for (let vineY = 9; vineY <= 16; vineY++) put(wallX, vineY, vineZ, 'vine', { north: 'false', east: wallX === 29 ? 'true' : 'false', south: 'false', west: wallX === 1 ? 'true' : 'false', up: 'false' })
    }
  }
  for (const [lampX, lampZ] of [[2, 10], [2, 14], [28, 10], [28, 14], [12, 4], [18, 4]]) {
    box([lampX, 8, lampZ], [lampX, 17, lampZ], 'chain', { axis: 'y', waterlogged: 'false' })
    put(lampX, 7, lampZ, 'lantern', { hanging: 'true', waterlogged: 'false' })
  }
  for (const sideX of [0, 30]) {
    box([sideX, 3, 11], [sideX, 6, 13], 'air')
    for (const postZ of [10, 14]) box([sideX, 3, postZ], [sideX, 7, postZ], 'stone_bricks')
    box([sideX, 7, 11], [sideX, 7, 13], 'chiseled_stone_bricks')
  }
  box([14, 2, 28], [16, 5, 28], 'air')
  for (const postX of [13, 17]) box([postX, 2, 28], [postX, 6, 28], 'stone_bricks')
  box([14, 6, 28], [16, 6, 28], 'chiseled_stone_bricks')
  return { structure, metadata: { name: 'room', id: 'S7R2-09', description: 'Original treasure reference: stepped chest shrine, twin high red mushrooms, side supplies, three visible ground arches; closed source ceiling' } }
}
