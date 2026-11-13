export default function build(api) {
  const structure = api.createStructure([25, 16, 27])
  const put = (position, block, properties) => api.setBlock(structure, position, 'minecraft:' + block, properties)
  const fill = (from, to, block, properties) => api.fill(structure, from, to, 'minecraft:' + block, properties)
  const noise = (blockX, blockY, blockZ) => Math.abs((blockX * 53 + blockY * 31 + blockZ * 97 + blockX * blockZ * 7) % 37)
  const stone = (blockX, blockY, blockZ) => {
    const value = noise(blockX, blockY, blockZ)
    return value < 11 ? 'mossy_stone_bricks' : value < 16 ? 'cracked_stone_bricks' : value < 19 ? 'andesite' : 'stone_bricks'
  }
  const slab = position => put(position, 'mossy_stone_brick_slab', { type: 'bottom', waterlogged: 'false' })
  const stair = (position, facing, half = 'bottom') => put(position, 'mossy_stone_brick_stairs', { facing, half, shape: 'straight', waterlogged: 'false' })
  const vine = (position, face) => put(position, 'vine', { up: 'false', north: 'false', east: 'false', south: 'false', west: 'false', [face]: 'true' })
  const leaves = position => put(position, 'oak_leaves', { distance: '7', persistent: 'true', waterlogged: 'false' })
  fill([0, 0, 0], [24, 15, 26], 'air')
  for (let blockX = 0; blockX < 25; blockX += 1) {
    for (let blockZ = 0; blockZ < 27; blockZ += 1) {
      put([blockX, 0, blockZ], 'stone_bricks')
      const inAisle = blockX >= 10 && blockX <= 14 && blockZ <= 19
      const floorBlock = inAisle ? (noise(blockX, 1, blockZ) < 7 ? 'cracked_stone_bricks' : 'smooth_stone') : (noise(blockX, 1, blockZ) < 9 ? 'moss_block' : stone(blockX, 1, blockZ))
      put([blockX, 1, blockZ], floorBlock)
      const skylight = blockX >= 8 && blockX <= 16 && blockZ >= 6 && blockZ <= 20
      put([blockX, 13, blockZ], skylight ? 'glass' : stone(blockX, 13, blockZ))
      if (blockX <= 1 || blockX >= 23 || blockZ <= 1 || blockZ >= 25) put([blockX, 14, blockZ], stone(blockX, 14, blockZ))
      if ([5, 19].includes(blockX) || [9, 18].includes(blockZ)) put([blockX, 14, blockZ], 'mossy_stone_bricks')
    }
  }
  for (let blockY = 2; blockY <= 12; blockY += 1) {
    for (let blockX = 0; blockX < 25; blockX += 1) {
      for (const blockZ of [0, 26]) put([blockX, blockY, blockZ], stone(blockX, blockY, blockZ))
    }
    for (let blockZ = 0; blockZ < 27; blockZ += 1) {
      for (const blockX of [0, 24]) put([blockX, blockY, blockZ], stone(blockX, blockY, blockZ))
    }
  }
  fill([10, 2, 0], [14, 6, 1], 'air')
  for (const jambX of [8, 9, 15, 16]) {
    fill([jambX, 2, 0], [jambX, 7, 1], 'mossy_stone_bricks')
    put([jambX, 3, 0], 'chiseled_stone_bricks')
  }
  fill([8, 7, 0], [16, 8, 1], 'stone_bricks')
  stair([9, 6, 1], 'west', 'top')
  stair([15, 6, 1], 'east', 'top')
  put([12, 7, 0], 'chiseled_stone_bricks')
  for (const poolX of [3, 18]) {
    for (let blockX = poolX; blockX < poolX + 4; blockX += 1) {
      for (let blockZ = 4; blockZ <= 6; blockZ += 1) {
        put([blockX, 0, blockZ], (blockX + blockZ) % 5 === 0 ? 'sea_lantern' : 'mossy_stone_bricks')
        put([blockX, 1, blockZ], 'water', { level: '0' })
      }
    }
    for (let blockX = poolX - 1; blockX <= poolX + 4; blockX += 1) for (const blockZ of [3, 7]) put([blockX, 1, blockZ], 'chiseled_stone_bricks')
    for (const blockX of [poolX - 1, poolX + 4]) for (let blockZ = 4; blockZ <= 6; blockZ += 1) put([blockX, 1, blockZ], 'mossy_stone_bricks')
  }
  for (const stationZ of [9, 18]) {
    for (const columnX of [5, 19]) {
      fill([columnX - 1, 2, stationZ - 1], [columnX + 1, 2, stationZ + 1], 'mossy_stone_bricks')
      fill([columnX - 1, 3, stationZ], [columnX, 10, stationZ + 1], 'polished_andesite')
      for (const bandY of [3, 7, 10]) fill([columnX - 1, bandY, stationZ], [columnX, bandY, stationZ + 1], bandY === 7 ? 'mossy_stone_bricks' : 'chiseled_stone_bricks')
      fill([columnX - 2, 11, stationZ - 1], [columnX + 1, 11, stationZ + 1], 'stone_bricks')
      for (let blockZ = stationZ; blockZ <= stationZ + 1; blockZ += 1) {
        stair([columnX + 1, 10, blockZ], 'west', 'top')
        stair([columnX - 2, 10, blockZ], 'east', 'top')
      }
      for (let vineY = stationZ === 9 ? 6 : 8; vineY <= 10; vineY += 1) vine([columnX === 5 ? 6 : 17, vineY, stationZ], columnX === 5 ? 'west' : 'east')
    }
    fill([4, 12, stationZ], [20, 12, stationZ + 1], 'mossy_stone_bricks')
    for (const shoulderX of [6, 18]) stair([shoulderX, 11, stationZ], shoulderX < 12 ? 'west' : 'east', 'top')
    for (const lampX of [7, 17]) {
      for (const chainY of [10, 11]) put([lampX, chainY, stationZ], 'chain', { axis: 'y', waterlogged: 'false' })
      put([lampX, 9, stationZ], 'lantern', { hanging: 'true', waterlogged: 'false' })
    }
  }
  for (const beamX of [4, 5, 18, 19]) fill([beamX, 12, 9], [beamX, 12, 19], 'stone_bricks')
  for (const wallX of [0, 24]) {
    const surfaceX = wallX === 0 ? 1 : 23
    for (const stationZ of [3, 11, 16, 23]) for (let vineY = 5 + stationZ % 3; vineY <= 12; vineY += 1) vine([surfaceX, vineY, stationZ], wallX === 0 ? 'west' : 'east')
    for (let blockZ = 11; blockZ <= 14; blockZ += 1) {
      for (let blockY = 5; blockY <= 8; blockY += 1) put([surfaceX, blockY, blockZ], blockZ === 12 && blockY === 6 ? 'yellow_terracotta' : 'green_wool')
      put([surfaceX, 9, blockZ], 'polished_andesite')
    }
    for (const planterZ of [2, 16, 23]) {
      const planterX = wallX === 0 ? 2 : 22
      put([planterX, 2, planterZ], 'mossy_stone_bricks')
      put([planterX, 3, planterZ], 'potted_fern')
    }
  }
  for (let blockX = 1; blockX <= 23; blockX += 1) for (let blockZ = 1; blockZ <= 25; blockZ += 1) {
    if ((blockX <= 3 || blockX >= 21 || blockZ >= 24) && noise(blockX, 12, blockZ) < 17) leaves([blockX, 12, blockZ])
  }
  fill([8, 2, 21], [16, 2, 24], 'mossy_stone_bricks')
  fill([10, 3, 22], [14, 3, 24], 'chiseled_stone_bricks')
  fill([9, 4, 21], [15, 4, 24], 'mossy_stone_bricks')
  for (let stairX = 10; stairX <= 14; stairX += 1) stair([stairX, 2, 20], 'south')
  for (const lampX of [10, 14]) put([lampX, 5, 22], 'soul_lantern', { hanging: 'false', waterlogged: 'false' })
  put([12, 5, 23], 'chiseled_stone_bricks')
  put([12, 6, 23], 'potted_flowering_azalea_bush')
  for (let blockX = 8; blockX <= 16; blockX += 1) for (let blockY = 4; blockY <= 11; blockY += 1) {
    const radius = Math.hypot((blockX - 12) * 0.9, blockY - 7)
    if (radius >= 2.8 && radius <= 4.1) put([blockX, blockY, 25], noise(blockX, blockY, 25) < 15 ? 'mossy_stone_bricks' : 'chiseled_stone_bricks')
    else if (radius < 2.8) {
      put([blockX, blockY, 25], 'green_stained_glass')
      put([blockX, blockY, 26], 'sea_lantern')
    }
  }
  for (const shrineX of [7, 17]) {
    fill([shrineX, 2, 24], [shrineX, 9, 25], 'mossy_stone_bricks')
    put([shrineX, 6, 24], 'chiseled_stone_bricks')
    for (let vineY = 7; vineY <= 9; vineY += 1) vine([shrineX, vineY, 23], 'south')
  }
  for (const shrineX of [9, 15]) for (let vineY = 5; vineY <= 11; vineY += 1) {
    const anchor = structure.getBlock([shrineX, vineY, 25])?.state?.getName()?.toString()
    if (anchor && anchor !== 'minecraft:air' && anchor !== 'minecraft:green_stained_glass') vine([shrineX, vineY, 24], 'south')
  }
  for (let rubbleX = 2; rubbleX <= 7; rubbleX += 1) {
    put([rubbleX, 2, 13], rubbleX % 3 === 0 ? 'chiseled_stone_bricks' : 'polished_andesite')
    if (rubbleX === 2 || rubbleX === 7) slab([rubbleX, 3, 13])
  }
  for (let rubbleZ = 20; rubbleZ <= 23; rubbleZ += 1) put([20, 2, rubbleZ], rubbleZ === 21 ? 'cracked_stone_bricks' : 'polished_andesite')
  fill([3, 2, 20], [4, 4, 21], 'mossy_stone_bricks')
  put([3, 5, 20], 'cracked_stone_bricks')
  for (const position of [[7, 2, 14], [3, 2, 17], [7, 2, 21], [18, 2, 13], [21, 2, 20], [18, 2, 24]]) slab(position)
  for (const [blockX, blockZ] of [[2, 9], [8, 16], [22, 10], [22, 24], [7, 23], [18, 25]]) {
    put([blockX, 1, blockZ], 'moss_block')
    put([blockX, 2, blockZ], 'fern')
  }
  for (let blockX = 1; blockX <= 23; blockX += 1) for (let blockZ = 2; blockZ <= 25; blockZ += 1) {
    const current = structure.getBlock([blockX, 2, blockZ])?.state?.getName()?.toString()
    const under = structure.getBlock([blockX, 1, blockZ])?.state?.getName()?.toString()
    if (current === 'minecraft:air' && under && under !== 'minecraft:water' && !(blockX >= 9 && blockX <= 15 && blockZ <= 20) && noise(blockX, 2, blockZ) < 5) put([blockX, 2, blockZ], 'moss_carpet')
  }
  return { structure, metadata: { name: 'room' } }
}

