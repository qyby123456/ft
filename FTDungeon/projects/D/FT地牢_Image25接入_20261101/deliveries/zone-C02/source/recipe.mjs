import { specification, onRoute, onGuard } from './spec.mjs'

export default function build(api) {
  const structure = api.createStructure(specification.size)
  const put = (blockX, blockY, blockZ, block, properties, nbt) => api.setBlock(structure, [blockX, blockY, blockZ], block, properties, nbt)
  const masonry = (blockX, blockY, blockZ) => {
    const pattern = (blockX * 31 + blockY * 19 + blockZ * 43) % 17
    return pattern < 5 ? 'minecraft:mossy_stone_bricks' : pattern < 7 ? 'minecraft:cracked_stone_bricks' : 'minecraft:stone_bricks'
  }
  api.fill(structure, [0, 0, 0], [18, 10, 18], 'minecraft:air')
  for (let blockX = 0; blockX <= 18; blockX++) {
    for (let blockZ = 0; blockZ <= 18; blockZ++) {
      put(blockX, 0, blockZ, masonry(blockX, 0, blockZ))
      put(blockX, 10, blockZ, masonry(blockX, 10, blockZ))
      const boundary = blockX === 0 || blockX === 18 || blockZ === 0 || blockZ === 18
      const ledge = blockX === 1 || blockX === 17 || blockZ === 1 || blockZ === 17 || (blockZ >= 15 && blockX <= 6)
      const pillar = specification.pillars.some(([pillarX, pillarZ]) => pillarX === blockX && pillarZ === blockZ)
      const solid = boundary || ledge || pillar || onRoute(blockX, blockZ) || onGuard(blockX, blockZ)
      put(blockX, 1, blockZ, solid ? masonry(blockX, 1, blockZ) : 'minecraft:water', solid ? undefined : { level: '0' })
      if (boundary) {
        for (let blockY = 2; blockY < 10; blockY++) put(blockX, blockY, blockZ, masonry(blockX, blockY, blockZ))
      }
      if (onRoute(blockX, blockZ)) put(blockX, 1, blockZ, 'minecraft:smooth_stone')
      if (onGuard(blockX, blockZ)) {
        put(blockX, 1, blockZ, 'minecraft:stone_bricks')
        put(blockX, 2, blockZ, 'minecraft:spruce_fence', { waterlogged: 'false' })
      }
    }
  }
  for (const [pillarX, pillarZ] of specification.pillars) {
    put(pillarX, 1, pillarZ, 'minecraft:mossy_stone_bricks')
    for (let blockY = 2; blockY <= 8; blockY++) put(pillarX, blockY, pillarZ, masonry(pillarX, blockY, pillarZ))
    api.fill(structure, [pillarX - 1, 9, pillarZ - 1], [pillarX + 1, 9, pillarZ + 1], 'minecraft:stone_bricks')
    for (let beamX = 1; beamX <= 17; beamX++) put(beamX, 9, pillarZ, 'minecraft:stone_bricks')
  }
  for (const station of [3, 7, 11, 15]) {
    for (let blockY = 2; blockY <= 9; blockY++) {
      for (const [blockX, blockZ] of [[1, station], [17, station], [station, 1], [station, 17]]) {
        if (!onRoute(blockX, blockZ) && !onGuard(blockX, blockZ)) put(blockX, blockY, blockZ, masonry(blockX, blockY,blockZ))
      }
    }
  }
  api.fill(structure, [2, 3, 1], [5, 5, 1], 'minecraft:iron_bars', { waterlogged: 'false' })
  api.fill(structure, [1, 3, 10], [1, 5, 13], 'minecraft:iron_bars', { waterlogged: 'false' })
  api.fill(structure, [8, 3, 17], [11, 5, 17], 'minecraft:iron_bars', { waterlogged: 'false' })
  api.fill(structure, [17, 3, 13], [17, 5, 15], 'minecraft:iron_bars', { waterlogged: 'false' })
  for (const [lightX, lightZ] of specification.hanging_lights) {
    for (let blockY = 7; blockY <= 9; blockY++) put(lightX, blockY, lightZ, 'minecraft:chain', { axis: 'y', waterlogged: 'false' })
    put(lightX, 6, lightZ, 'minecraft:lantern', { hanging: 'true', waterlogged: 'false' })
  }
  for (const [barrelX, barrelY, barrelZ] of specification.barrels) {
    put(barrelX, barrelY, barrelZ, 'minecraft:barrel', { facing: 'up', open: 'false' }, api.createBlockNbt({ id: 'minecraft:barrel', Items: [] }))
  }
  api.fill(structure, [12, 2, 16], [15, 2, 16], 'minecraft:stone_brick_stairs', { facing: 'south', half: 'bottom', shape: 'straight', waterlogged: 'false' })
  api.fill(structure, [12, 1, 16], [15, 1, 16], 'minecraft:mossy_stone_bricks')
  put(16, 1, 16, 'minecraft:mossy_stone_bricks')
  put(16, 2, 16, 'minecraft:chiseled_stone_bricks')
  put(16, 3, 16, 'minecraft:lantern', { hanging: 'false', waterlogged: 'false' })
  api.fill(structure, [8, 2, 0], [10, 5, 0], 'minecraft:air')
  api.fill(structure, [18, 2, 8], [18, 5, 10], 'minecraft:air')
  for (const gateX of [7, 11]) api.fill(structure, [gateX, 2, 0], [gateX, 5, 0], 'minecraft:chiseled_stone_bricks')
  for (const gateZ of [7, 11]) api.fill(structure, [18, 2, gateZ], [18, 5, gateZ], 'minecraft:chiseled_stone_bricks')
  api.fill(structure, [7, 6, 0], [11, 6, 0], 'minecraft:stone_bricks')
  api.fill(structure, [18, 6, 7], [18, 6, 11], 'minecraft:stone_bricks')
  return { structure, metadata: { name: 'room', id: specification.id, reference: specification.reference, reference_sha256: specification.reference_sha256 } }
}
