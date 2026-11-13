export default function build(api) {
  const size = [13, 10, 16]
  const structure = api.createStructure(size)
  const put = (position, block) => api.setBlock(structure, position, block)
  const fill = (from, to, block) => api.fill(structure, from, to, block)
  const masonry = (posX, posY, posZ) => {
    const patch = (Math.floor(posZ / 3) * 7 + Math.floor(posY / 2) * 3 + posX * 5) % 13
    return patch < 4 ? 'minecraft:mossy_stone_bricks' : 'minecraft:stone_bricks'
  }
  fill([0, 0, 0], [12, 9, 15], 'minecraft:air')
  for (let posX = 0; posX < size[0]; posX++) {
    for (let posZ = 0; posZ < size[2]; posZ++) {
      put([posX, 0, posZ], masonry(posX, 0, posZ))
      put([posX, 8, posZ], masonry(posX, 8, posZ))
      put([posX, 9, posZ], masonry(posX, 9, posZ))
      if (posX < 3 || posX > 9) {
        for (let posY = 1; posY < 8; posY++) {
          put([posX, posY, posZ], masonry(posX, posY, posZ))
        }
      }
    }
  }
  fill([5, 1, 0], [7, 1, 15], 'minecraft:stone_bricks')
  for (const posZ of [0, 1, 14, 15]) {
    for (let posX = 3; posX <= 9; posX++) {
      put([posX, 1, posZ], masonry(posX, 1, posZ))
    }
  }
  for (let posZ = 2; posZ <= 13; posZ++) {
    for (const posX of [3, 4, 8, 9]) {
      put([posX, 1, posZ], 'minecraft:water[level=0]')
    }
  }
  for (const posZ of [0, 4, 8, 12, 15]) {
    for (const posX of [3, 9]) {
      fill([posX, 1, posZ], [posX, 6, posZ], 'minecraft:stone_bricks')
      put([posX, 2, posZ], 'minecraft:mossy_stone_bricks')
      put([posX, 5, posZ], 'minecraft:chiseled_stone_bricks')
      put([posX, 7, posZ], 'minecraft:stone_bricks')
    }
    for (const [posX, posY, facing] of [[4, 6, 'west'], [5, 7, 'west'], [8, 6, 'east'], [7, 7, 'east']]) {
      put([posX, posY, posZ], `minecraft:stone_brick_stairs[facing=${facing},half=top,shape=straight,waterlogged=false]`)
    }
    put([4, 7, posZ], 'minecraft:stone_bricks')
    put([8, 7, posZ], 'minecraft:stone_bricks')
    put([6, 7, posZ], 'minecraft:chiseled_stone_bricks')
  }
  for (const posZ of [0, 15]) {
    fill([0, 2, posZ], [3, 8, posZ], 'minecraft:stone_bricks')
    fill([9, 2, posZ], [12, 8, posZ], 'minecraft:stone_bricks')
    put([3, 3, posZ], 'minecraft:mossy_stone_bricks')
    put([9, 3, posZ], 'minecraft:mossy_stone_bricks')
    put([3, 5, posZ], 'minecraft:chiseled_stone_bricks')
    put([9, 5, posZ], 'minecraft:chiseled_stone_bricks')
  }
  for (const posZ of [2, 6, 10, 13]) {
    for (const posX of [3, 9]) {
      put([posX, 7, posZ], 'minecraft:chain[axis=y,waterlogged=false]')
      put([posX, 6, posZ], 'minecraft:chain[axis=y,waterlogged=false]')
      put([posX, 5, posZ], 'minecraft:soul_lantern[hanging=true,waterlogged=false]')
    }
  }
  for (const [posX, posZ, bottom] of [[3, 3, 3], [9, 3, 4], [3, 7, 4], [9, 7, 2], [3, 11, 2], [9, 11, 3]]) {
    const facing = posX === 3 ? 'west' : 'east'
    for (let posY = bottom; posY <= 6; posY++) {
      put([posX, posY, posZ], `minecraft:vine[east=${facing === 'east'},north=false,south=false,up=false,west=${facing === 'west'}]`)
    }
  }
  for (let posZ = 0; posZ < 16; posZ++) {
    put([6, 1, posZ], posZ % 4 === 3 ? 'minecraft:chiseled_stone_bricks' : posZ % 3 === 0 ? 'minecraft:mossy_stone_bricks' : 'minecraft:stone_bricks')
  }
  return {
    structure,
    metadata: {
      name: 'room', id: 'corridor-S06', title: '浅淹苔石短拱廊',
      corridorAxis: 'north-south', lengthBlocks: 16,
      entrance: [6, 2, 0], interior: [6, 2, 2], exit: [6, 2, 15],
      dryRoute: { min: [5, 2, 0], max: [7, 4, 15] },
      water: { columns: [3, 4, 8, 9], elevation: 1, northDam: [0, 1], southDam: [14, 15] },
    },
  }
}
