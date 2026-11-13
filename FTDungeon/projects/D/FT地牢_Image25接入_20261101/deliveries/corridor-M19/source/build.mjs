export default function build(api) {
  const size = [15, 12, 41]
  const structure = api.createStructure(size)
  const place = (position, block, properties, nbt) => api.setBlock(structure, position, block, properties, nbt)
  const fill = (start, end, block, properties) => api.fill(structure, start, end, block, properties)
  const masonry = (blockX, blockY, blockZ) => {
    const noise = ((blockX * 193 + blockY * 359 + blockZ * 719) ^ (blockZ * blockZ * 37)) >>> 0
    const mossLimit = blockY <= 4 ? 46 : 19
    return noise % 100 < mossLimit ? 'minecraft:mossy_stone_bricks'
      : noise % 100 < mossLimit + 18 ? 'minecraft:cracked_stone_bricks' : 'minecraft:stone_bricks'
  }

  fill([0, 0, 0], [14, 11, 40], 'minecraft:air')
  fill([0, 0, 0], [14, 0, 40], 'minecraft:stone_bricks')
  fill([0, 11, 0], [14, 11, 40], 'minecraft:stone_bricks')

  for (let blockZ = 0; blockZ < 41; blockZ++) {
    for (let blockX = 0; blockX < 15; blockX++) {
      const apron = blockZ <= 2 || blockZ >= 38
      const walkway = blockX >= 6 && blockX <= 8
      const rim = blockX <= 2 || blockX >= 12
      if (apron || walkway || rim) {
        const floor = walkway && blockZ % 8 === 0 ? 'minecraft:chiseled_stone_bricks'
          : walkway && blockX === 7 ? 'minecraft:polished_andesite' : masonry(blockX, 1, blockZ)
        place([blockX, 1, blockZ], floor)
      } else {
        place([blockX, 1, blockZ], 'minecraft:water', { level: '0' })
      }
    }
    for (let blockY = 2; blockY <= 10; blockY++) {
      for (const blockX of [0, 1, 13, 14]) {
        place([blockX, blockY, blockZ], masonry(blockX, blockY, blockZ))
      }
    }
    fill([2, 10, blockZ], [12, 10, blockZ], 'minecraft:stone_bricks')
    fill([2, 9, blockZ], [3, 9, blockZ], 'minecraft:stone_bricks')
    fill([11, 9, blockZ], [12, 9, blockZ], 'minecraft:stone_bricks')
    place([2, 8, blockZ], masonry(2, 8, blockZ))
    place([12, 8, blockZ], masonry(12, 8, blockZ))
  }

  for (const blockZ of [0, 8, 16, 24, 32, 40]) {
    for (const blockX of [2, 3, 11, 12]) {
      fill([blockX, 1, blockZ], [blockX, 6, blockZ], 'minecraft:stone_bricks')
      place([blockX, 2, blockZ], 'minecraft:mossy_stone_bricks')
      place([blockX, 5, blockZ], 'minecraft:chiseled_stone_bricks')
    }
    fill([2, 7, blockZ], [3, 10, blockZ], 'minecraft:stone_bricks')
    fill([11, 7, blockZ], [12, 10, blockZ], 'minecraft:stone_bricks')
    fill([4, 8, blockZ], [4, 10, blockZ], 'minecraft:cracked_stone_bricks')
    fill([10, 8, blockZ], [10, 10, blockZ], 'minecraft:stone_bricks')
    fill([5, 9, blockZ], [9, 10, blockZ], 'minecraft:stone_bricks')
    if (blockZ !== 16) {
      place([4, 7, blockZ], 'minecraft:stone_brick_stairs', { facing: 'east', half: 'top', shape: 'straight', waterlogged: 'false' })
    }
    if (blockZ !== 32) {
      place([10, 7, blockZ], 'minecraft:stone_brick_stairs', { facing: 'west', half: 'top', shape: 'straight', waterlogged: 'false' })
    }
    place([5, 8, blockZ], 'minecraft:stone_brick_stairs', { facing: 'east', half: 'top', shape: 'straight', waterlogged: 'false' })
    place([9, 8, blockZ], 'minecraft:stone_brick_stairs', { facing: 'west', half: 'top', shape: 'straight', waterlogged: 'false' })
    place([7, 9, blockZ], 'minecraft:chiseled_stone_bricks')
  }

  for (const blockZ of [0, 40]) {
    fill([0, 2, blockZ], [4, 10, blockZ], 'minecraft:stone_bricks')
    fill([10, 2, blockZ], [14, 10, blockZ], 'minecraft:stone_bricks')
    fill([5, 7, blockZ], [9, 10, blockZ], 'minecraft:stone_bricks')
    fill([6, 7, blockZ], [8, 7, blockZ], 'minecraft:air')
    place([5, 6, blockZ], 'minecraft:stone_brick_stairs', { facing: 'east', half: 'top', shape: 'straight', waterlogged: 'false' })
    place([9, 6, blockZ], 'minecraft:stone_brick_stairs', { facing: 'west', half: 'top', shape: 'straight', waterlogged: 'false' })
    place([7, 8, blockZ], 'minecraft:chiseled_stone_bricks')
  }

  for (const blockZ of [4, 12, 20, 28, 36]) {
    fill([7, 8, blockZ], [7, 9, blockZ], 'minecraft:chain', { axis: 'y', waterlogged: 'false' })
    place([7, 7, blockZ], 'minecraft:lantern', { hanging: 'true', waterlogged: 'false' })
    for (const side of ['west', 'east']) {
      const mirror = (blockX) => side === 'west' ? blockX : 14 - blockX
      for (let nicheZ = blockZ - 1; nicheZ <= blockZ + 1; nicheZ++) {
        fill([mirror(1), 2, nicheZ], [mirror(1), 6, nicheZ], 'minecraft:air')
        place([mirror(0), 4, nicheZ], 'minecraft:cracked_stone_bricks')
      }
      const eggBay = (blockZ + (side === 'west' ? 0 : 8)) % 16 === 4
      if (eggBay) {
        place([mirror(1), 2, blockZ], 'minecraft:calcite')
        place([mirror(1), 3, blockZ], 'minecraft:quartz_slab', { type: 'bottom', waterlogged: 'false' })
        place([mirror(2), 2, blockZ + 1], 'minecraft:calcite')
        place([mirror(2), 3, blockZ + 1], 'minecraft:quartz_slab', { type: 'bottom', waterlogged: 'false' })
        place([mirror(2), 4, blockZ], 'minecraft:cobweb')
      } else {
        place([mirror(2), 2, blockZ], 'minecraft:bone_block', { axis: 'z' })
        place([mirror(1), 2, blockZ - 1], 'minecraft:bone_block', { axis: 'x' })
        place([mirror(2), 3, blockZ], 'minecraft:skeleton_skull', { rotation: side === 'west' ? '12' : '4' }, api.createBlockNbt({ id: 'minecraft:skull' }))
        place([mirror(2), 2, blockZ + 1], 'minecraft:quartz_slab', { type: 'bottom', waterlogged: 'false' })
      }
      const silk = [[1, 4, 0], [1, 5, 0], [1, 6, 0], [2, 6, 0], [2, 7, 0], [3, 7, 0], [3, 8, 0], [4, 8, 0], [4, 9, 0], [2, 7, -1], [3, 8, 1], [4, 9, 1], [1, 5, 1], [2, 6, -1]]
      for (const [silkX, silkY, silkZ] of silk) {
        place([mirror(silkX), silkY, blockZ + silkZ], 'minecraft:cobweb')
      }
    }
  }

  return {
    structure,
    metadata: {
      name: 'room',
      id: 'corridor-M19',
      title: 'M19 苔砖蛛巢积水直廊',
      size,
      entrance: [7, 2, 0],
      interior: [7, 2, 4],
      exit: [7, 2, 40],
      reference: 'D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/M19-webbed-spider-flooded.png',
      reference_sha256: '8f3c423cf62ca1d8fd47704f0635c2e478d037295f5a6531a16468ba2a5f927f',
      safety_adaptation: 'Three-block-wide dry central route; source water in sealed flanking basins.',
    },
  }
}
