export default function build(api) {
  const size = [11, 10, 16]
  const structure = api.createStructure(size)
  const put = (position, name, properties) => api.setBlock(structure, position, `minecraft:${name}`, properties)
  const fill = (from, to, name, properties) => api.fill(structure, from, to, `minecraft:${name}`, properties)
  const masonry = (blockX, blockY, blockZ) => {
    const sample = (blockX * 41 + blockY * 71 + blockZ * 29 + blockX * blockZ * 7) % 31
    return sample < 4 ? 'mossy_stone_bricks' : sample < 9 ? 'cracked_stone_bricks' : 'stone_bricks'
  }
  const stair = (position, facing, half = 'top') => put(position, 'stone_brick_stairs', {
    facing, half, shape: 'straight', waterlogged: 'false',
  })
  const slab = (position, name = 'stone_brick_slab', type = 'bottom') => put(position, name, {
    type, waterlogged: 'false',
  })
  fill([0, 0, 0], [10, 9, 15], 'air')
  for (let blockX = 0; blockX < size[0]; blockX += 1) {
    for (let blockZ = 0; blockZ < size[2]; blockZ += 1) {
      put([blockX, 0, blockZ], masonry(blockX, 0, blockZ))
      put([blockX, 9, blockZ], blockX === 0 || blockX === 10 ? 'stone_bricks' : 'deepslate_tiles')
    }
  }
  for (let blockZ = 0; blockZ < size[2]; blockZ += 1) {
    for (let blockY = 1; blockY <= 8; blockY += 1) {
      for (const blockX of [0, 10]) put([blockX, blockY, blockZ], masonry(blockX, blockY, blockZ))
    }
    for (const blockX of [1, 9]) put([blockX, 6, blockZ], 'stone_bricks')
    for (const blockX of [1, 2, 8, 9]) put([blockX, 7, blockZ], masonry(blockX, 7, blockZ))
    for (let blockX = 1; blockX <= 9; blockX += 1) put([blockX, 8, blockZ], 'stone_bricks')
    if (![0, 5, 10, 15].includes(blockZ)) {
      for (const blockX of [1, 9]) {
        put([blockX, 1, blockZ], 'dark_oak_planks')
        for (let blockY = 2; blockY <= 5; blockY += 1) put([blockX, blockY, blockZ], 'bookshelf')
      }
    }
  }
  for (const blockZ of [0, 5, 10, 15]) {
    for (const blockX of [1, 2, 8, 9]) {
      for (let blockY = 1; blockY <= 5; blockY += 1) {
        put([blockX, blockY, blockZ], blockY === 1 || blockY === 4 ? 'chiseled_stone_bricks' : masonry(blockX, blockY, blockZ))
      }
      put([blockX, 6, blockZ], masonry(blockX, 6, blockZ))
    }
    stair([3, 5, blockZ], 'west')
    stair([7, 5, blockZ], 'east')
    put([3, 6, blockZ], masonry(3, 6, blockZ))
    put([7, 6, blockZ], masonry(7, 6, blockZ))
    stair([4, 6, blockZ], 'west')
    stair([6, 6, blockZ], 'east')
    for (let blockX = 3; blockX <= 7; blockX += 1) put([blockX, 7, blockZ], masonry(blockX, 7, blockZ))
  }
  put([5, 7, 5], 'air')
  put([6, 6, 5], 'air')
  stair([7, 6, 10], 'east')
  slab([4, 7, 10], 'stone_brick_slab', 'top')
  for (const blockX of [1, 9]) {
    const nicheX = blockX === 1 ? 2 : 8
    for (let blockZ = 6; blockZ <= 9; blockZ += 1) {
      for (let blockY = 1; blockY <= 5; blockY += 1) put([blockX, blockY, blockZ], 'dark_oak_planks')
      put([blockX, 1, blockZ], 'stripped_dark_oak_log', { axis: 'y' })
    }
    for (const blockZ of [6, 9]) {
      fill([nicheX, 1, blockZ], [nicheX, 4, blockZ], 'stone_bricks')
      put([nicheX, 4, blockZ], 'chiseled_stone_bricks')
    }
    for (let blockZ = 6; blockZ <= 9; blockZ += 1) slab([nicheX, 5, blockZ], 'dark_oak_slab', 'top')
    api.setBlock(structure, [nicheX, 4, 7], 'minecraft:white_wall_banner', {
      facing: blockX === 1 ? 'east' : 'west',
    }, api.createBlockNbt({ id: 'minecraft:banner' }))
    put([nicheX, 1, 8], 'chiseled_stone_bricks')
    put([nicheX, 2, 8], 'soul_lantern', { hanging: 'false', waterlogged: 'false' })
  }
  for (const blockZ of [3, 12]) {
    for (const blockX of [3, 7]) {
      for (const blockY of [6, 7]) put([blockX, blockY, blockZ], 'chain', { axis: 'y', waterlogged: 'false' })
      put([blockX, 5, blockZ], 'soul_lantern', { hanging: 'true', waterlogged: 'false' })
    }
  }
  for (const position of [[2, 1, 2], [8, 1, 3], [3, 1, 4], [7, 1, 11], [2, 1, 13], [8, 1, 12]]) {
    slab(position, position[2] % 2 ? 'cobblestone_slab' : 'stone_brick_slab')
  }
  slab([2, 1, 11], 'dark_oak_slab')
  slab([8, 1, 4], 'dark_oak_slab')
  for (const blockZ of [0, 15]) {
    fill([0, 1, blockZ], [1, 8, blockZ], 'stone_bricks')
    fill([9, 1, blockZ], [10, 8, blockZ], 'stone_bricks')
    for (let blockX = 4; blockX <= 6; blockX += 1) {
      fill([blockX, 1, blockZ], [blockX, 4, blockZ], 'air')
    }
  }
  return { structure, metadata: { name: 'room', theme: 'S12 damaged archive library corridor' } }
}
