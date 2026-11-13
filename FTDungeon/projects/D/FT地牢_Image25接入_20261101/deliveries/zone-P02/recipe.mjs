export const plan = {
  size: [17, 11, 39],
  reference: 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/parkour/P02-parkour-water-channel-jumps.png',
  referenceSha256: '518f9e89fadea35943d46096f0fb7d5eae409ef8718eaf15f1151cb9ef4a22c9',
  entrance: [8, 3, 0], interior: [8, 3, 3], exit: [8, 3, 38], checkpoint: [8, 3, 19],
  archPlanes: [4, 12, 20, 28, 35], recoveryPlanes: [8, 16, 24, 32],
  platforms: [
    { id: 'start', min: [5, 2, 1], max: [11, 2, 4] },
    { id: 'A', min: [6, 2, 6], max: [8, 2, 8] },
    { id: 'B', min: [8, 2, 10], max: [10, 2, 12] },
    { id: 'C', min: [6, 2, 14], max: [8, 2, 16] },
    { id: 'checkpoint', min: [5, 2, 18], max: [11, 2, 21] },
    { id: 'D', min: [8, 2, 23], max: [10, 2, 25] },
    { id: 'E', min: [6, 2, 27], max: [8, 2, 29] },
    { id: 'F', min: [8, 2, 31], max: [10, 2, 33] },
    { id: 'finish', min: [7, 2, 35], max: [9, 2, 37] },
  ],
}

export default function build(api) {
  const structure = api.createStructure(plan.size)
  const put = (position, block, properties, nbt) => api.setBlock(structure, position, block, properties, nbt)
  const fill = (from, to, block, properties) => api.fill(structure, from, to, block, properties)
  const stone = (posX, posY, posZ) => {
    const pattern = (posX * 37 + posY * 19 + posZ * 23) % 17
    return pattern < 5 ? 'minecraft:mossy_stone_bricks' : pattern === 6 ? 'minecraft:cracked_stone_bricks' : 'minecraft:stone_bricks'
  }
  fill([0, 0, 0], [16, 10, 38], 'minecraft:air')
  fill([0, 0, 0], [16, 0, 38], 'minecraft:deepslate_bricks')
  for (let posX = 0; posX < 17; posX += 1) {
    for (let posZ = 0; posZ < 39; posZ += 1) {
      put([posX, 1, posZ], stone(posX, 1, posZ))
      put([posX, 10, posZ], stone(posX, 10, posZ))
      if (posX <= 4 || posX >= 12 || posZ <= 4 || posZ >= 35) put([posX, 2, posZ], stone(posX, 2, posZ))
      for (let posY = 2; posY < 10; posY += 1) {
        if (posX === 0 || posX === 16 || posZ === 0 || posZ === 38) put([posX, posY, posZ], stone(posX, posY, posZ))
      }
    }
  }
  fill([5, 2, 5], [11, 2, 34], 'minecraft:water', { level: '0' })
  for (const platform of plan.platforms) {
    for (let posX = platform.min[0]; posX <= platform.max[0]; posX += 1) {
      for (let posZ = platform.min[2]; posZ <= platform.max[2]; posZ += 1) {
        put([posX, 2, posZ], platform.id === 'checkpoint' ? 'minecraft:chiseled_stone_bricks' : 'minecraft:mossy_stone_bricks')
      }
    }
  }
  for (const posX of [4, 12]) {
    for (let posZ = 5; posZ <= 34; posZ += 1) {
      if (plan.recoveryPlanes.some(plane => Math.abs(posZ - plane) <= 1)) {
        put([posX, 2, posZ], 'minecraft:stone_brick_stairs', { facing: posX === 4 ? 'west' : 'east', half: 'bottom', shape: 'straight', waterlogged: 'true' })
      } else {
        put([posX, 3, posZ], 'minecraft:dark_oak_fence', { north: 'false', east: 'false', south: 'false', west: 'false', waterlogged: 'false' })
      }
    }
  }
  for (const posZ of plan.archPlanes) {
    for (const posX of [2, 14]) {
      for (let posY = 3; posY <= 9; posY += 1) put([posX, posY, posZ], posY === 3 || posY === 8 ? 'minecraft:chiseled_stone_bricks' : stone(posX, posY, posZ))
    }
    fill([2, 9, posZ], [14, 9, posZ], 'minecraft:stone_bricks')
    put([3, 8, posZ], 'minecraft:stone_brick_stairs', { facing: 'west', half: 'top', shape: 'straight', waterlogged: 'false' })
    put([13, 8, posZ], 'minecraft:stone_brick_stairs', { facing: 'east', half: 'top', shape: 'straight', waterlogged: 'false' })
    for (const posX of [3, 13]) {
      fill([posX, 7, posZ], [posX, 9, posZ], 'minecraft:chain', { axis: 'y', waterlogged: 'false' })
      put([posX, 6, posZ], 'minecraft:lantern', { hanging: 'true', waterlogged: 'false' })
    }
  }
  for (const posZ of [0, 38]) {
    for (const posX of [6, 10]) fill([posX, 3, posZ], [posX, 7, posZ], 'minecraft:chiseled_stone_bricks')
    fill([6, 6, posZ], [10, 6, posZ], 'minecraft:stone_bricks')
    fill([7, 3, posZ], [9, 5, posZ], 'minecraft:air')
    put([8, 2, posZ], 'minecraft:polished_andesite')
  }
  for (const posZ of [3, 19, 36]) {
    for (const posX of [5, 11]) {
      put([posX, 3, posZ], 'minecraft:chiseled_stone_bricks')
      put([posX, 4, posZ], 'minecraft:lantern', { hanging: 'false', waterlogged: 'false' })
    }
  }
  fill([1, 3, 35], [6, 5, 35], 'minecraft:stone_bricks')
  fill([10, 3, 35], [15, 5, 35], 'minecraft:stone_bricks')
  for (let posY = 3; posY <= 6; posY += 1) {
    put([2, posY, 36], 'minecraft:mossy_stone_bricks')
    put([4, posY, 36], 'minecraft:mossy_stone_bricks')
    put([3, posY, 37], 'minecraft:mossy_stone_bricks')
    put([3, posY, 35], 'minecraft:glass')
    put([3, posY, 36], 'minecraft:water', { level: '0' })
  }
  fill([2, 7, 35], [4, 7, 37], 'minecraft:chiseled_stone_bricks')
  for (const position of [[1, 3, 2], [1, 4, 2], [15, 3, 30], [15, 3, 31]]) {
    put(position, 'minecraft:barrel', { facing: 'up', open: 'false' }, api.createBlockNbt({ id: 'minecraft:barrel', Items: [] }))
  }
  for (const posZ of [7, 23, 33]) {
    put([1, 6, posZ], 'minecraft:cyan_wall_banner', { facing: 'east' }, api.createBlockNbt({ id: 'minecraft:banner', Patterns: [] }))
  }
  return { structure, metadata: { name: 'room', id: 'zone-P02', description: 'Mossy water-channel parkour with eight short jumps, a midpoint rest platform and shallow-water recovery.' } }
}
