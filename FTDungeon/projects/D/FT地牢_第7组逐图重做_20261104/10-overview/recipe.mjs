export const layout = {
  size: [79, 19, 81],
  zones: [
    { id: 'large-hall', name: '西北雕柱大殿', bounds: [2, 2, 32, 28], floor: 3, roof: 18, target: [17, 4, 16] },
    { id: 'north-grove', name: '东北巨菇菌室', bounds: [44, 2, 70, 25], floor: 1, roof: 15, target: [55, 2, 17] },
    { id: 'courtyard', name: '中央巨菇环水庭院', bounds: [28, 35, 55, 58], floor: 1, roof: 15, target: [34, 2, 46], skylight: [31, 38, 52, 55] },
    { id: 'supplies', name: '东侧储物小室', bounds: [60, 35, 76, 53], floor: 1, roof: 11, target: [67, 2, 46] },
    { id: 'relic-garden', name: '西南晶柱浅池侧院', bounds: [2, 48, 23, 74], floor: 1, roof: 14, target: [11, 2, 61] },
    { id: 'entrance', name: '东南石拱入口', bounds: [43, 65, 64, 80], floor: 1, roof: 10, target: [53, 2, 76] }
  ],
  corridor_rectangles: [
    { id: 'west-spine', cells: [15, 29, 19, 47] },
    { id: 'north-cross', cells: [20, 30, 68, 34] },
    { id: 'grove-link', cells: [51, 26, 55, 29] },
    { id: 'east-link', cells: [56, 44, 59, 48] },
    { id: 'courtyard-bend-a', cells: [44, 59, 48, 62] },
    { id: 'courtyard-bend-b', cells: [44, 61, 55, 64] },
    { id: 'south-link', cells: [24, 64, 42, 70] }
  ],
  ports: [
    { id: 'hall-south', zone: 'large-hall', corridor: 'west-spine', feet: [17, 4, 28], normal: [0, 0, 1] },
    { id: 'grove-south', zone: 'north-grove', corridor: 'grove-link', feet: [53, 2, 25], normal: [0, 0, 1] },
    { id: 'court-north', zone: 'courtyard', corridor: 'north-cross', feet: [41, 2, 35], normal: [0, 0, -1] },
    { id: 'court-east', zone: 'courtyard', corridor: 'east-link', feet: [55, 2, 46], normal: [1, 0, 0] },
    { id: 'court-south', zone: 'courtyard', corridor: 'courtyard-bend-a', feet: [46, 2, 58], normal: [0, 0, 1] },
    { id: 'supplies-north', zone: 'supplies', corridor: 'north-cross', feet: [67, 2, 35], normal: [0, 0, -1] },
    { id: 'supplies-west', zone: 'supplies', corridor: 'east-link', feet: [60, 2, 46], normal: [-1, 0, 0] },
    { id: 'garden-north', zone: 'relic-garden', corridor: 'west-spine', feet: [17, 2, 48], normal: [0, 0, -1] },
    { id: 'garden-east', zone: 'relic-garden', corridor: 'south-link', feet: [23, 2, 66], normal: [1, 0, 0] },
    { id: 'entry-north', zone: 'entrance', corridor: 'courtyard-bend-b', feet: [53, 2, 65], normal: [0, 0, -1] },
    { id: 'entry-west', zone: 'entrance', corridor: 'south-link', feet: [43, 2, 68], normal: [-1, 0, 0] },
    { id: 'entry-external', zone: 'entrance', corridor: null, feet: [53, 2, 80], normal: [0, 0, 1], external: true }
  ],
  junction_targets: [[18, 2, 34], [22, 2, 32], [53, 2, 32], [67, 2, 32], [57, 2, 46], [46, 2, 60], [48, 2, 62], [53, 2, 63], [31, 2, 66]],
  stair_targets: [[17, 4, 29], [17, 3.5, 30], [17, 3, 31], [17, 2.5, 32], [17, 2, 33]],
  cameras: [
    { mode: 'hall-eye', feet: [17, 4, 24], target: [17.5, 8, 7] },
    { mode: 'grove-eye', feet: [53, 2, 22], target: [57.5, 6, 10] },
    { mode: 'court-eye', feet: [46, 2, 55], target: [41.5, 6, 46.5] },
    { mode: 'supplies-eye', feet: [67, 2, 49], target: [68, 4, 38] },
    { mode: 'garden-eye', feet: [18, 2, 71], target: [10, 5, 56] },
    { mode: 'entrance-eye', feet: [53, 2, 78], target: [53.5, 4.5, 68] },
    { mode: 'corner-eye', feet: [53, 2, 63], target: [45, 3.5, 62] },
    { mode: 'reverse-court-eye', feet: [34, 2, 40], target: [46, 6, 50] },
    { mode: 'hall-stairs-eye', feet: [17, 2, 35], target: [17.5, 5.5, 24] }
  ],
  interactions: [
    { position: [67, 2, 38], kind: 'chest', access_feet: [67, 2, 39] },
    { position: [70, 2, 38], kind: 'chest', access_feet: [70, 2, 39] },
    ...[63, 64, 65, 72, 73, 74].map(blockX => ({ position: [blockX, 2, 40], kind: 'barrel', access_feet: [blockX, 2, 41] }))
  ]
}

export default function build(api) {
  const structure = api.createStructure(layout.size)
  const put = (blockX, blockY, blockZ, block, properties = {}, data = undefined) => {
    const defaults = {}
    if (['mycelium', 'podzol'].includes(block)) defaults.snowy = 'false'
    if (['mushroom_stem', 'red_mushroom_block', 'brown_mushroom_block'].includes(block)) {
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
  box([0, 0, 0], [78, 18, 80], 'air')
  const roomCells = new Set()
  const floorMaterial = (blockX, blockZ) => {
    const pattern = (blockX * 7 + blockZ * 13 + Math.floor(blockX / 3)) % 23
    return pattern < 7 ? 'mycelium' : pattern < 12 ? 'purple_terracotta' : pattern < 16 ? 'podzol' : pattern < 19 ? 'rooted_dirt' : 'mossy_stone_bricks'
  }
  for (const zone of layout.zones) {
    const [minimumX, minimumZ, maximumX, maximumZ] = zone.bounds
    for (let blockX = minimumX; blockX <= maximumX; blockX++) {
      for (let blockZ = minimumZ; blockZ <= maximumZ; blockZ++) {
        roomCells.add(`${blockX},${blockZ}`)
        put(blockX, 0, blockZ, 'deepslate_bricks')
        box([blockX, 1, blockZ], [blockX, zone.floor, blockZ], floorMaterial(blockX, blockZ))
        if (blockX === minimumX || blockX === maximumX || blockZ === minimumZ || blockZ === maximumZ) {
          for (let blockY = zone.floor + 1; blockY < zone.roof; blockY++) {
            const pattern = (blockX * 11 + blockY * 7 + blockZ * 3) % 17
            put(blockX, blockY, blockZ, blockY >= zone.roof - 3 && pattern < 11 ? 'rooted_dirt' : pattern < 7 ? 'mossy_stone_bricks' : 'deepslate_bricks')
          }
        }
        const sky = zone.skylight
        if (!sky || blockX < sky[0] || blockX > sky[2] || blockZ < sky[1] || blockZ > sky[3]) put(blockX, zone.roof, blockZ, 'rooted_dirt')
      }
    }
  }
  const corridorCells = new Set()
  for (const rectangle of layout.corridor_rectangles) {
    const [minimumX, minimumZ, maximumX, maximumZ] = rectangle.cells
    for (let blockX = minimumX; blockX <= maximumX; blockX++) {
      for (let blockZ = minimumZ; blockZ <= maximumZ; blockZ++) corridorCells.add(`${blockX},${blockZ}`)
    }
  }
  for (const key of corridorCells) {
    const [blockX, blockZ] = key.split(',').map(Number)
    put(blockX, 0, blockZ, 'deepslate_bricks')
    put(blockX, 1, blockZ, floorMaterial(blockX, blockZ))
    put(blockX, 8, blockZ, 'rooted_dirt')
    for (const [offsetX, offsetZ] of [[1, 0], [-1, 0], [0, 1], [0, -1]]) {
      const wallX = blockX + offsetX
      const wallZ = blockZ + offsetZ
      const wallKey = `${wallX},${wallZ}`
      if (corridorCells.has(wallKey) || roomCells.has(wallKey)) continue
      put(wallX, 0, wallZ, 'deepslate_bricks')
      put(wallX, 1, wallZ, 'mossy_stone_bricks')
      box([wallX, 2, wallZ], [wallX, 8, wallZ], 'mossy_stone_bricks')
    }
  }
  box([15, 2, 29], [19, 3, 29], 'stone_bricks')
  box([15, 2, 30], [19, 2, 31], 'stone_bricks')
  box([15, 3, 30], [19, 3, 30], 'stone_brick_slab', { type: 'bottom', waterlogged: 'false' })
  box([15, 2, 32], [19, 2, 32], 'stone_brick_slab', { type: 'bottom', waterlogged: 'false' })
  const mushroom = (stemX, stemY, stemZ, height, radius, cap = 'red_mushroom_block') => {
    box([stemX, stemY, stemZ], [stemX, stemY + height - 1, stemZ], 'mushroom_stem')
    for (let offsetX = -radius; offsetX <= radius; offsetX++) {
      for (let offsetZ = -radius; offsetZ <= radius; offsetZ++) {
        if (Math.abs(offsetX) === radius && Math.abs(offsetZ) === radius) continue
        put(stemX + offsetX, stemY + height, stemZ + offsetZ, cap)
        if (Math.abs(offsetX) < radius && Math.abs(offsetZ) < radius) put(stemX + offsetX, stemY + height + 1, stemZ + offsetZ, cap)
      }
    }
  }
  const crystal = (blockX, blockY, blockZ) => {
    put(blockX, blockY, blockZ, 'amethyst_block')
    put(blockX, blockY + 1, blockZ, 'amethyst_cluster', { facing: 'up', waterlogged: 'false' })
  }
  const pond = (minimumX, minimumZ, maximumX, maximumZ) => {
    box([minimumX - 1, 1, minimumZ - 1], [maximumX + 1, 1, maximumZ + 1], 'polished_blackstone_bricks')
    box([minimumX, 1, minimumZ], [maximumX, 1, maximumZ], 'water', { level: '0' })
  }
  for (const pillarX of [6, 28]) {
    for (const pillarZ of [7, 15, 23]) {
      box([pillarX, 4, pillarZ], [pillarX, 17, pillarZ], 'mossy_stone_bricks')
      put(pillarX, 9, pillarZ + 1, 'shroomlight')
      put(pillarX, 13, pillarZ + 1, 'chiseled_stone_bricks')
    }
  }
  for (let blockX = 12; blockX <= 22; blockX++) {
    for (let blockZ = 12; blockZ <= 20; blockZ++) {
      if (Math.abs(blockX - 17) + Math.abs(blockZ - 16) <= 5) put(blockX, 3, blockZ, Math.abs(blockX - 17) + Math.abs(blockZ - 16) <= 2 ? 'red_terracotta' : 'stone_bricks')
    }
  }
  for (const [plantX, plantZ] of [[9, 5], [25, 5], [8, 25], [26, 25]]) crystal(plantX, 4, plantZ)
  mushroom(9, 4, 9, 5, 2)
  for (const [bannerX, color] of [[12, 'red'], [17, 'yellow'], [22, 'red']]) {
    put(bannerX, 11, 3, `${color}_wall_banner`, { facing: 'south' }, { id: 'minecraft:banner', Patterns: [{ Pattern: 'bs', Color: 4 }, { Pattern: 'mc', Color: 14 }] })
  }
  mushroom(57, 2, 10, 9, 4)
  mushroom(65, 2, 7, 5, 2, 'brown_mushroom_block')
  pond(60, 17, 65, 20)
  for (const [plantX, plantZ] of [[48, 7], [67, 12], [48, 21]]) crystal(plantX, 2, plantZ)
  box([39, 2, 44], [43, 2, 48], 'mossy_stone_bricks')
  mushroom(41, 3, 46, 8, 4)
  for (let offsetX = -6; offsetX <= 6; offsetX++) {
    for (let offsetZ = -6; offsetZ <= 6; offsetZ++) {
      if (Math.abs(offsetX) + Math.abs(offsetZ) > 10) continue
      const core = Math.max(Math.abs(offsetX), Math.abs(offsetZ)) <= 2
      const water = !core && Math.max(Math.abs(offsetX), Math.abs(offsetZ)) <= 5 && Math.abs(offsetX) + Math.abs(offsetZ) <= 8 && offsetX !== 0 && offsetZ !== 0
      put(41 + offsetX, 1, 46 + offsetZ, water ? 'water' : 'polished_blackstone_bricks', water ? { level: '0' } : {})
    }
  }
  for (const [plantX, plantZ] of [[31, 39], [52, 39], [31, 54], [51, 54]]) crystal(plantX, 2, plantZ)
  for (const operation of layout.interactions) {
    const [blockX, blockY, blockZ] = operation.position
    put(blockX, blockY, blockZ, operation.kind, operation.kind === 'chest' ? { facing: 'south', type: 'single', waterlogged: 'false' } : { facing: 'south', open: 'false' }, { id: `minecraft:${operation.kind}`, Items: [] })
  }
  box([72, 2, 37], [74, 2, 37], 'spruce_planks')
  put(72, 3, 37, 'potted_red_mushroom')
  put(74, 3, 37, 'potted_brown_mushroom')
  mushroom(7, 2, 55, 6, 3)
  pond(14, 65, 18, 68)
  box([8, 2, 62], [10, 2, 64], 'mossy_stone_bricks')
  box([9, 3, 63], [9, 5, 63], 'chiseled_stone_bricks')
  put(9, 6, 63, 'shroomlight')
  crystal(9, 7, 63)
  for (const lampX of [5, 20]) {
    box([lampX, 2, 60], [lampX, 4, 60], 'mossy_stone_bricks')
    put(lampX, 5, 60, 'shroomlight')
  }
  for (const pillarX of [49, 57]) box([pillarX, 2, 72], [pillarX, 7, 72], 'mossy_stone_bricks')
  box([49, 8, 72], [57, 8, 72], 'chiseled_stone_bricks')
  put(50, 7, 72, 'lantern', { hanging: 'true', waterlogged: 'false' })
  put(56, 7, 72, 'lantern', { hanging: 'true', waterlogged: 'false' })
  mushroom(47, 2, 76, 3, 2)
  crystal(61, 2, 76)
  for (const zone of layout.zones) {
    const [minimumX, minimumZ, maximumX, maximumZ] = zone.bounds
    for (const [lampX, lampZ] of [[minimumX + 2, minimumZ + 3], [maximumX - 2, maximumZ - 3]]) {
      if (zone.skylight && lampX >= zone.skylight[0] && lampX <= zone.skylight[2] && lampZ >= zone.skylight[1] && lampZ <= zone.skylight[3]) continue
      box([lampX, zone.roof - 4, lampZ], [lampX, zone.roof - 1, lampZ], 'chain', { axis: 'y', waterlogged: 'false' })
      put(lampX, zone.roof - 5, lampZ, 'lantern', { hanging: 'true', waterlogged: 'false' })
    }
    for (const wallX of [minimumX + 1, maximumX - 1]) {
      for (const vineZ of [minimumZ + 5, maximumZ - 5]) {
        for (let vineY = Math.max(zone.floor + 5, zone.roof - 7); vineY < zone.roof; vineY++) {
          put(wallX, vineY, vineZ, 'vine', { north: 'false', east: wallX === maximumX - 1 ? 'true' : 'false', south: 'false', west: wallX === minimumX + 1 ? 'true' : 'false', up: 'false' })
        }
      }
    }
    for (const [plantX, plantZ] of [[minimumX + 3, minimumZ + 3], [maximumX - 3, maximumZ - 3]]) {
      put(plantX, zone.floor, plantZ, 'mycelium')
      put(plantX, zone.floor + 1, plantZ, 'red_mushroom')
    }
    put(minimumX + 5, zone.floor + 4, minimumZ + 1, 'glow_lichen', { north: 'true', east: 'false', south: 'false', west: 'false', up: 'false', down: 'false', waterlogged: 'false' })
  }
  for (const [lampX, lampZ] of [[17, 38], [29, 32], [39, 32], [53, 28], [63, 32], [57, 46], [46, 60], [51, 63], [29, 66], [38, 66]]) {
    put(lampX, 7, lampZ, 'chain', { axis: 'y', waterlogged: 'false' })
    put(lampX, 6, lampZ, 'lantern', { hanging: 'true', waterlogged: 'false' })
  }
  for (const port of layout.ports) {
    const [feetX, feetY, feetZ] = port.feet
    const alongX = port.normal[2] !== 0
    for (let offset = -1; offset <= 1; offset++) {
      const blockX = feetX + (alongX ? offset : 0)
      const blockZ = feetZ + (alongX ? 0 : offset)
      box([blockX, feetY, blockZ], [blockX, feetY + 3, blockZ], 'air')
      put(blockX, feetY + 4, blockZ, 'chiseled_stone_bricks')
    }
    for (const offset of [-2, 2]) {
      const blockX = feetX + (alongX ? offset : 0)
      const blockZ = feetZ + (alongX ? 0 : offset)
      box([blockX, feetY, blockZ], [blockX, feetY + 4, blockZ], 'stone_bricks')
    }
  }
  return { structure, metadata: { name: 'room', id: 'S7R2-10', geometry_origin: 'Original overview plan; no previous room NBT or recipe geometry imported', layout } }
}
