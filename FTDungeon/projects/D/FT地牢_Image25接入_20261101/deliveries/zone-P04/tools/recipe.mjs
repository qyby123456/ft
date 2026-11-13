export default function build(api) {
  const structure = api.createStructure([23, 16, 27])
  const put = (pos, block) => api.setBlock(structure, pos, block)
  const fill = (from, to, block) => api.fill(structure, from, to, block)
  const masonry = (pos, offset = 0) => {
    const noise = (pos[0] * 71 + pos[1] * 29 + pos[2] * 43 + offset) % 29
    return noise < 5 ? 'minecraft:mossy_stone_bricks' : noise < 9 ? 'minecraft:cracked_stone_bricks' : 'minecraft:stone_bricks'
  }
  fill([0, 0, 0], [22, 15, 26], 'minecraft:air')
  fill([0, 0, 0], [22, 1, 26], 'minecraft:deepslate_bricks')
  for (let posX = 0; posX < 23; posX += 1) {
    for (let posZ = 0; posZ < 27; posZ += 1) {
      put([posX, 2, posZ], masonry([posX, 2, posZ], 13))
      put([posX, 15, posZ], masonry([posX, 15, posZ]))
      if (posX < 2 || posX > 20 || posZ < 2 || posZ > 24) {
        for (let posY = 3; posY < 15; posY += 1) put([posX, posY, posZ], masonry([posX, posY, posZ]))
      }
    }
  }
  for (const posZ of [2, 14, 24]) {
    for (const posX of [2, 20]) {
      fill([posX, 3, posZ], [posX, 14, posZ], 'minecraft:stone_bricks')
      for (const posY of [3, 8, 13]) put([posX, posY, posZ], 'minecraft:chiseled_stone_bricks')
    }
    fill([2, 14, posZ], [20, 14, posZ], 'minecraft:stone_bricks')
  }
  fill([2, 3, 0], [20, 4, 5], 'minecraft:stone_bricks')
  for (let posX = 2; posX <= 20; posX += 1) {
    for (let posZ = 2; posZ <= 5; posZ += 1) put([posX, 4, posZ], masonry([posX, 4, posZ]))
  }
  const ledges = [
    {from: 6, to: 7, floor: 4, width: 4},
    {from: 9, to: 11, floor: 4, width: 4},
    {from: 13, to: 15, floor: 5, width: 6},
    {from: 17, to: 19, floor: 5, width: 4},
    {from: 21, to: 23, floor: 5, width: 6}
  ]
  for (const ledge of ledges) {
    for (let posZ = ledge.from; posZ <= ledge.to; posZ += 1) {
      for (let posX = 2; posX <= ledge.width; posX += 1) put([posX, ledge.floor, posZ], masonry([posX, ledge.floor, posZ], 9))
      put([2, ledge.floor - 1, posZ], 'minecraft:stone_bricks')
    }
  }
  fill([2, 5, 23], [20, 5, 24], 'minecraft:stone_bricks')
  fill([7, 5, 22], [20, 5, 22], 'minecraft:stone_bricks')
  fill([2, 4, 24], [20, 4, 24], 'minecraft:stone_bricks')
  fill([10, 3, 25], [12, 4, 26], 'minecraft:stone_bricks')
  fill([10, 5, 0], [12, 7, 1], 'minecraft:air')
  fill([10, 5, 25], [12, 7, 26], 'minecraft:air')
  fill([10, 5, 25], [12, 5, 25], 'minecraft:stone_brick_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]')
  for (let posX = 17; posX <= 19; posX += 1) {
    put([posX, 3, 7], 'minecraft:stone_brick_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]')
    put([posX, 3, 6], 'minecraft:stone_bricks')
    put([posX, 4, 6], 'minecraft:stone_brick_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]')
  }
  for (let posX = 7; posX <= 16; posX += 1) put([posX, 5, 5], 'minecraft:dark_oak_fence')
  for (let posZ = 13; posZ <= 15; posZ += 1) put([6, 6, posZ], 'minecraft:dark_oak_fence')
  for (let posX = 7; posX <= 19; posX += 1) put([posX, 6, 22], 'minecraft:dark_oak_fence')
  for (const [posX, posZ] of [[3, 3], [3, 14], [3, 23], [19, 3], [19, 14], [19, 23]]) {
    put([posX, 14, posZ], 'minecraft:stone_bricks')
    fill([posX, 11, posZ], [posX, 13, posZ], 'minecraft:chain[axis=y,waterlogged=false]')
    put([posX, 10, posZ], 'minecraft:lantern[hanging=true,waterlogged=false]')
  }
  for (const posZ of [7, 18]) {
    fill([0, 9, posZ], [1, 11, posZ + 1], 'minecraft:iron_bars')
    fill([21, 9, posZ], [22, 11, posZ + 1], 'minecraft:iron_bars')
  }
  for (const posZ of [8, 20]) put([2, 8, posZ], 'minecraft:wall_torch[facing=east]')
  for (const posZ of [8, 20]) put([20, 8, posZ], 'minecraft:wall_torch[facing=west]')
  fill([21, 7, 15], [21, 10, 16], 'minecraft:red_wool')
  for (const posX of [10, 12]) put([posX, 8, 0], 'minecraft:chiseled_stone_bricks')
  for (const posX of [10, 12]) put([posX, 8, 26], 'minecraft:chiseled_stone_bricks')
  for (const [posX, posZ] of [[15, 2], [16, 2], [18, 24]]) put([posX, posZ === 24 ? 6 : 5, posZ], 'minecraft:stripped_spruce_log[axis=y]')
  for (const [posX, posZ] of [[8, 10], [13, 15], [16, 19], [9, 20]]) {
    put([posX, 3, posZ], 'minecraft:stone_brick_slab[type=bottom,waterlogged=false]')
    put([posX + 1, 2, posZ], 'minecraft:cracked_stone_bricks')
  }
  for (const [posX, posY, posZ] of [[3, 4, 6], [3, 4, 10], [4, 5, 14], [3, 5, 18], [4, 5, 22]]) put([posX, posY, posZ], 'minecraft:chiseled_stone_bricks')
  put([11, 4, 3], 'minecraft:chiseled_stone_bricks')
  put([11, 4, 26], 'minecraft:chiseled_stone_bricks')
  return {structure, metadata: {name: 'room'}}
}

