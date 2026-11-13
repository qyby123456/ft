export const specification = {
  id: 'zone-R01', name: '塌塔基座', size: [25, 17, 25],
  entrance: [12, 2, 0], interior: [12, 2, 6], landing: [12, 2, 12],
  stair_lower_landing: [20, 2, 21], stair_upper_landing: [20, 11, 9], upper_safe_point: [20, 11, 5],
  reference: 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/ruins/R01-ruins-collapsed-watchtower-base.png',
  reference_sha256: 'cf5d8e795c7df413126bf76a2bb216637361a70807c43cf2e89987091a59e8c1',
}

export function groundRoute() {
  const cells = new Map()
  const add = (blockX, blockZ) => cells.set(`${blockX},${blockZ}`, [blockX + 0.5, 2, blockZ + 0.5])
  for (let blockZ = 0; blockZ <= 22; blockZ++) {
    for (let blockX = 11; blockX <= 13; blockX++) add(blockX, blockZ)
  }
  for (let blockZ = 20; blockZ <= 22; blockZ++) {
    for (let blockX = 11; blockX <= 21; blockX++) add(blockX, blockZ)
  }
  for (let blockZ = 10; blockZ <= 14; blockZ++) {
    for (let blockX = 10; blockX <= 14; blockX++) add(blockX, blockZ)
  }
  return [...cells.values()]
}

export function upperRoute() {
  const cells = []
  for (let blockZ = 2; blockZ <= 10; blockZ++) {
    for (let blockX = 19; blockX <= 21; blockX++) cells.push([blockX + 0.5, 11, blockZ + 0.5])
  }
  return cells
}

export function stairRoute() {
  const cells = []
  for (let stairIndex = 0; stairIndex < 9; stairIndex++) {
    for (let blockX = 19; blockX <= 21; blockX++) {
      cells.push([blockX + 0.5, 2.5 + stairIndex, 19.8 - stairIndex])
      cells.push([blockX + 0.5, 3 + stairIndex, 19.5 - stairIndex])
    }
  }
  return cells
}

export default function build(api) {
  const structure = api.createStructure(specification.size)
  const place = (position, block, properties, nbt) => api.setBlock(structure, position, block, properties, nbt)
  const box = (from, to, block, properties) => api.fill(structure, from, to, block, properties)
  const hash = (blockX, blockY, blockZ) => Math.abs((blockX * 73856093) ^ (blockY * 19349663) ^ (blockZ * 83492791)) % 100
  const stone = (blockX, blockY, blockZ) => {
    const sample = hash(blockX, blockY, blockZ)
    return sample < 15 ? 'minecraft:mossy_stone_bricks' : sample < 33 ? 'minecraft:cracked_stone_bricks' : sample < 40 ? 'minecraft:andesite' : 'minecraft:stone_bricks'
  }
  const masonry = (from, to) => {
    for (let blockX = from[0]; blockX <= to[0]; blockX++) {
      for (let blockY = from[1]; blockY <= to[1]; blockY++) {
        for (let blockZ = from[2]; blockZ <= to[2]; blockZ++) place([blockX, blockY, blockZ], stone(blockX, blockY, blockZ))
      }
    }
  }
  const slab = position => place(position, 'minecraft:stone_brick_slab', { type: 'bottom', waterlogged: 'false' })
  const railing = position => place(position, 'minecraft:spruce_fence', { waterlogged: 'false' })
  const log = (from, to, axis) => box(from, to, 'minecraft:stripped_spruce_log', { axis })

  box([0, 0, 0], [24, 16, 24], 'minecraft:air')
  masonry([0, 0, 0], [24, 1, 24])
  for (let blockX = 2; blockX <= 22; blockX++) {
    for (let blockZ = 2; blockZ <= 22; blockZ++) {
      if ((blockX < 6 || blockX > 20 || blockZ > 18) && hash(blockX, 1, blockZ) < 14) place([blockX, 1, blockZ], 'minecraft:coarse_dirt')
    }
  }
  for (let station = 0; station < 25; station++) {
    const northHeight = 13 + (station % 6 === 0 ? 2 : station % 6 === 3 ? -1 : 0)
    const southHeight = 10 + (station % 5 === 0 ? 2 : station % 5 === 2 ? -1 : 0)
    const westHeight = 12 + (station % 7 === 0 ? 2 : station % 7 === 3 ? -2 : 0)
    const eastHeight = 14 + (station % 6 === 0 ? 1 : station % 6 === 3 ? -2 : 0)
    masonry([station, 2, 0], [station, northHeight, 1])
    masonry([station, 2, 23], [station, southHeight, 24])
    masonry([0, 2, station], [1, westHeight, station])
    masonry([23, 2, station], [24, eastHeight, station])
  }
  box([11, 2, 0], [13, 5, 1], 'minecraft:air')
  log([10, 2, 1], [10, 5, 1], 'y')
  log([14, 2, 1], [14, 5, 1], 'y')
  log([10, 6, 1], [14, 6, 1], 'x')
  masonry([10, 7, 0], [14, 7, 1])
  for (const blockX of [5, 19]) {
    box([blockX, 5, 0], [blockX, 7, 1], 'minecraft:iron_bars', { waterlogged: 'false' })
    box([blockX, 5, 23], [blockX, 7, 24], 'minecraft:iron_bars', { waterlogged: 'false' })
  }
  for (const blockZ of [6, 18]) {
    box([0, 5, blockZ], [1, 7, blockZ], 'minecraft:iron_bars', { waterlogged: 'false' })
    box([23, 5, blockZ], [24, 7, blockZ], 'minecraft:iron_bars', { waterlogged: 'false' })
  }
  for (const [columnX, columnZ] of [[5, 10], [17, 10]]) {
    masonry([columnX - 1, 2, columnZ - 1], [columnX + 2, 2, columnZ + 2])
    masonry([columnX, 3, columnZ], [columnX + 1, 8, columnZ + 1])
    masonry([columnX - 1, 9, columnZ - 1], [columnX + 2, 9, columnZ + 2])
  }
  log([2, 9, 10], [22, 9, 10], 'x')
  for (const blockX of [3, 6, 9, 15, 18, 21]) log([blockX, 9, 1], [blockX, 9, 10], 'z')
  log([2, 9, 3], [22, 9, 3], 'x')
  for (let blockX = 2; blockX <= 22; blockX++) {
    for (let blockZ = 2; blockZ <= 10; blockZ++) {
      if (blockX >= 9 && blockX <= 14 && blockZ >= 5 && blockZ <= 8) continue
      place([blockX, 10, blockZ], hash(blockX, 10, blockZ) < 29 ? stone(blockX, 10, blockZ) : 'minecraft:spruce_planks')
    }
  }
  for (let blockX = 8; blockX <= 15; blockX++) {
    railing([blockX, 11, 4])
    railing([blockX, 11, 9])
  }
  for (let blockZ = 5; blockZ <= 8; blockZ++) {
    railing([8, 11, blockZ])
    railing([15, 11, blockZ])
  }
  for (let blockX = 2; blockX <= 18; blockX++) railing([blockX, 11, 10])
  for (let stepIndex = 0; stepIndex < 9; stepIndex++) {
    const stepY = 2 + stepIndex
    const stepZ = 19 - stepIndex
    masonry([18, 2, stepZ], [22, stepY, stepZ])
    for (let blockX = 19; blockX <= 21; blockX++) place([blockX, stepY, stepZ], 'minecraft:stone_brick_stairs', { facing: 'north', half: 'bottom', shape: 'straight', waterlogged: 'false' })
    railing([18, stepY + 1, stepZ])
    railing([22, stepY + 1, stepZ])
  }
  for (let blockZ = 2; blockZ <= 10; blockZ++) railing([22, 11, blockZ])
  for (const [rubbleX, rubbleZ, height] of [[3, 19, 1], [4, 19, 2], [5, 19, 1], [3, 20, 1], [6, 20, 1], [5, 21, 1], [16, 18, 1], [16, 19, 2], [17, 19, 1], [16, 16, 1], [3, 5, 1]]) {
    masonry([rubbleX, 2, rubbleZ], [rubbleX, height + 1, rubbleZ])
    slab([rubbleX, height + 2, rubbleZ])
  }
  log([3, 2, 22], [7, 2, 22], 'x')
  log([15, 2, 17], [17, 2, 17], 'x')
  for (const [crateX, crateZ] of [[3, 15], [5, 3]]) {
    log([crateX, 2, crateZ], [crateX + 1, 3, crateZ + 1], 'y')
    box([crateX, 4, crateZ], [crateX + 1, 4, crateZ + 1], 'minecraft:spruce_slab', { type: 'bottom', waterlogged: 'false' })
  }
  for (const position of [[3, 2, 17], [4, 2, 17], [4, 3, 17], [7, 2, 3]]) {
    place(position, 'minecraft:barrel', { facing: 'up', open: 'false' }, api.createBlockNbt({ id: 'minecraft:barrel', Items: [] }))
  }
  for (const bannerX of [7, 16]) {
    place([bannerX, 7, 2], 'minecraft:red_wall_banner', { facing: 'south' }, api.createBlockNbt({ id: 'minecraft:banner', Patterns: [{ Color: 0, Pattern: 'mr' }] }))
  }
  for (const [carpetX, carpetZ] of [[15, 18], [15, 19], [14, 18], [17, 18]]) place([carpetX, 2, carpetZ], 'minecraft:red_carpet')
  log([1, 8, 14], [2, 8, 14], 'x')
  log([22, 8, 21], [23, 8, 21], 'x')
  for (const [lampX, lampZ, topY] of [[8, 10, 9], [15, 10, 9], [2, 14, 8], [22, 21, 8]]) {
    box([lampX, 6, lampZ], [lampX, topY - 1, lampZ], 'minecraft:chain', { axis: 'y', waterlogged: 'false' })
    place([lampX, 5, lampZ], 'minecraft:lantern', { hanging: 'true', waterlogged: 'false' })
  }
  for (const position of [[3, 11, 3], [17, 11, 3]]) place(position, 'minecraft:lantern', { hanging: 'false', waterlogged: 'false' })
  for (const [mossX, mossZ] of [[2, 2], [3, 2], [4, 2], [22, 2], [2, 20], [22, 22]]) {
    const supportY = mossZ === 2 ? 13 : mossX === 2 ? 12 : 14
    masonry([mossX, 2, mossZ], [mossX, supportY, mossZ])
    place([mossX, supportY + 1, mossZ], 'minecraft:moss_block')
    if (supportY + 2 < 17) place([mossX, supportY + 2, mossZ], 'minecraft:oak_leaves', { distance: '7', persistent: 'true', waterlogged: 'false' })
  }
  return { structure, metadata: { name: 'room', id: specification.id, design: 'supported collapsed watchtower base; guarded broken mezzanine; open ruined roof' } }
}
