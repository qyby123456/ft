export default function build(api) {
  const size = [19, 12, 40]
  const structure = api.createStructure(size)
  const put = (position, block, properties) => api.setBlock(structure, position, `minecraft:${block}`, properties)
  const fill = (from, to, block, properties) => api.fill(structure, from, to, `minecraft:${block}`, properties)
  const slab = { type: 'top', waterlogged: 'false' }
  const stairs = facing => ({ facing, half: 'top', shape: 'straight', waterlogged: 'false' })
  const masonry = (blockX, blockY, blockZ) => {
    const variation = (blockX * 31 + blockY * 17 + blockZ * 13) % 59
    if (variation < 3) return 'mossy_stone_bricks'
    if (variation < 6) return 'cracked_stone_bricks'
    if (variation === 17) return 'stone'
    return 'stone_bricks'
  }
  fill([0, 0, 0], [18, 11, 39], 'air')
  fill([0, 0, 0], [18, 0, 39], 'deepslate_bricks')
  for (let blockX = 0; blockX < 19; blockX += 1) {
    for (let blockZ = 0; blockZ < 40; blockZ += 1) {
      put([blockX, 1, blockZ], masonry(blockX, 1, blockZ))
      put([blockX, 10, blockZ], 'stone_bricks')
      put([blockX, 11, blockZ], blockX === 0 || blockX === 18 ? 'chiseled_stone_bricks' : 'stone_bricks')
    }
  }
  for (let blockY = 2; blockY <= 9; blockY += 1) {
    for (let blockZ = 0; blockZ < 40; blockZ += 1) {
      for (const blockX of [0, 18]) put([blockX, blockY, blockZ], masonry(blockX, blockY, blockZ))
    }
  }
  for (const [minimumX, maximumX] of [[1, 5], [13, 17]]) {
    fill([minimumX, 8, 0], [maximumX, 9, 39], 'stone_bricks')
    fill([minimumX, 2, 0], [maximumX, 7, 2], 'stone_bricks')
    fill([minimumX, 2, 37], [maximumX, 7, 39], 'stone_bricks')
  }
  const pierStations = [2, 9, 16, 23, 30, 37]
  for (const blockZ of pierStations) {
    fill([1, 2, blockZ], [6, 8, blockZ], 'stone_bricks')
    fill([12, 2, blockZ], [17, 8, blockZ], 'stone_bricks')
    for (const blockX of [0, 6, 12, 18]) {
      put([blockX, 2, blockZ], 'chiseled_stone_bricks')
      put([blockX, 6, blockZ], 'chiseled_stone_bricks')
      put([blockX, 8, blockZ], 'chiseled_stone_bricks')
    }
    put([7, 7, blockZ], 'stone_brick_stairs', stairs('west'))
    put([11, 7, blockZ], 'stone_brick_stairs', stairs('east'))
    put([7, 8, blockZ], 'stone_bricks')
    put([11, 8, blockZ], 'stone_bricks')
    fill([8, 8, blockZ], [10, 8, blockZ], 'stone_brick_slab', slab)
    fill([6, 9, blockZ], [12, 9, blockZ], 'stone_bricks')
    if (blockZ > 2 && blockZ < 37) {
      for (let blockY = 2; blockY <= 5; blockY += 1) {
        put([5, blockY, blockZ], 'shroomlight')
        put([6, blockY, blockZ], 'orange_stained_glass')
        put([13, blockY, blockZ], 'shroomlight')
        put([12, blockY, blockZ], 'orange_stained_glass')
      }
    }
  }
  for (let blockZ = 0; blockZ < 40; blockZ += 1) {
    put([6, 9, blockZ], 'stone_bricks')
    put([12, 9, blockZ], 'stone_bricks')
    for (let blockX = 7; blockX <= 11; blockX += 1) {
      if (!pierStations.includes(blockZ)) put([blockX, 9, blockZ], 'stone_brick_slab', slab)
    }
    if (blockZ > 1 && blockZ < 38) {
      for (const blockX of [6, 12]) {
        put([blockX, 0, blockZ], 'shroomlight')
        put([blockX, 1, blockZ], 'orange_stained_glass')
      }
    }
    if (blockZ % 7 === 5) {
      for (let blockX = 8; blockX <= 10; blockX += 1) put([blockX, 1, blockZ], 'polished_andesite')
      put([9, 1, blockZ], 'chiseled_stone_bricks')
    }
  }
  const cells = []
  for (let bay = 0; bay < 5; bay += 1) {
    const startZ = 3 + bay * 7
    for (const side of ['west', 'east']) {
      const frontX = side === 'west' ? 5 : 13
      const facing = side === 'west' ? 'east' : 'west'
      const doorZ = startZ + 2
      for (let blockZ = startZ; blockZ < startZ + 6; blockZ += 1) {
        for (let blockY = 2; blockY <= 6; blockY += 1) {
          put([frontX, blockY, blockZ], 'iron_bars', { north: 'false', south: 'false', east: 'false', west: 'false', waterlogged: 'false' })
        }
        put([frontX, 7, blockZ], 'stone_bricks')
      }
      for (const [blockY, half] of [[2, 'lower'], [3, 'upper']]) {
        put([frontX, blockY, doorZ], 'iron_door', { facing, half, hinge: 'left', open: 'false', powered: 'false' })
      }
      const bedX = side === 'west' ? 2 : 16
      put([bedX, 2, startZ + 1], 'light_gray_bed', { facing: 'south', part: 'foot', occupied: 'false' })
      put([bedX, 2, startZ + 2], 'light_gray_bed', { facing: 'south', part: 'head', occupied: 'false' })
      put([side === 'west' ? 3 : 15, 2, startZ + 4], 'cauldron')
      put([side === 'west' ? 1 : 17, 4, startZ + 3], 'tripwire_hook', { facing, attached: 'false', powered: 'false' })
      cells.push({ side, bay: bay + 1, frontX, zRange: [startZ, startZ + 5], door: [frontX, 2, doorZ], accessibleFromMainRoute: false })
    }
  }
  for (const [index, blockZ] of [6, 13, 20, 27, 34].entries()) {
    const blockX = index % 2 === 0 ? 11 : 7
    put([blockX, 7, blockZ], 'lantern', { hanging: 'true', waterlogged: 'false' })
    put([blockX, 8, blockZ], 'chain', { axis: 'y', waterlogged: 'false' })
    put([blockX, 9, blockZ], 'chain', { axis: 'y', waterlogged: 'false' })
  }
  for (const blockZ of [0, 1, 38, 39]) {
    for (const blockX of [6, 12]) fill([blockX, 2, blockZ], [blockX, 8, blockZ], 'chiseled_stone_bricks')
    put([7, 7, blockZ], 'stone_brick_stairs', stairs('west'))
    put([11, 7, blockZ], 'stone_brick_stairs', stairs('east'))
    fill([7, 8, blockZ], [11, 9, blockZ], 'stone_bricks')
  }
  return {
    structure,
    metadata: {
      name: 'room', id: 'corridor-M05', label: 'M05 灰石铁狱直廊', kind: 'medium', size,
      entrance: [9, 2, 0], interior: [9, 2, 3], exit: [9, 2, 39], facing: 'north',
      reference: 'D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/M05-prison-cells-intact.png',
      referenceSha256: '40b8d7e4dd5dffbfcbfff257e9ba5e7235bdfb4d57d356d24de76bb21671c8ca',
      clearMainRoute: { xRange: [7, 11], feetY: 2, zRange: [0, 39], minimumHeight: 3 },
      cells, pierStations, roofLayers: [10, 11],
      adaptations: ['Safe orange-glass-covered shroomlight replaces exposed lava glow; no fluid or fire.', 'Ten closed side cells are decorative recesses, not branches.', 'Stepped corbels evoke the reference arches while preserving an intact supported shell.']
    }
  }
}
