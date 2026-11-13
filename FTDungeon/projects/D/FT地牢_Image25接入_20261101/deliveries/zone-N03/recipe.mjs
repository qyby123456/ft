export const blueprint = {
  id: 'N03', name: '八角回廊楼梯枢纽', size: [35, 18, 35], entrance: [17, 1, 0], interior: [17, 1, 2], facing: 'north',
  reference: 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/hub/N03-hub-stair-hub.png',
  reference_sha256: '821cea10d8f31cdf296845e3d0c66f94bafe00a6cda7f3ca16be9e1d82e29ba8',
  floor_levels: { lower: 0, hub: 6, upper: 12 },
  ports: [
    { id: 'north-lower', facing: 'north', feet: [17, 1, 0], width: 3, height: 3, floor_y: 0 },
    { id: 'south-lower', facing: 'south', feet: [17, 1, 34], width: 3, height: 3, floor_y: 0 },
    { id: 'east-upper', facing: 'east', feet: [34, 13, 17], width: 3, height: 3, floor_y: 12 },
    { id: 'west-upper', facing: 'west', feet: [0, 13, 17], width: 3, height: 3, floor_y: 12 },
  ],
  safe_points: [
    { id: 'entrance-refuge', feet: [17, 1, 2] }, { id: 'hub-north', feet: [17, 7, 11] },
    { id: 'hub-east', feet: [23, 7, 17] }, { id: 'hub-south', feet: [17, 7, 23] },
    { id: 'hub-west', feet: [11, 7, 17] }, { id: 'upper-east', feet: [33, 13, 17] },
    { id: 'upper-west', feet: [1, 13, 17] }, { id: 'lower-south', feet: [17, 1, 33] },
  ],
  stairs: [
    { id: 'north-rise', low: [17, 1, 2], high: [17, 7, 9], direction: [0, 1], facing: 'south', first: [17, 1, 3], steps: 6, width: 3 },
    { id: 'south-rise', low: [17, 1, 32], high: [17, 7, 25], direction: [0, -1], facing: 'north', first: [17, 1, 31], steps: 6, width: 3 },
    { id: 'east-rise', low: [25, 7, 17], high: [32, 13, 17], direction: [1, 0], facing: 'east', first: [26, 7, 17], steps: 6, width: 3 },
    { id: 'west-rise', low: [9, 7, 17], high: [2, 13, 17], direction: [-1, 0], facing: 'west', first: [8, 7, 17], steps: 6, width: 3 },
  ],
  shaft: { center: [17, 17], footprint: 'abs(x-17)<=3 && abs(z-17)<=3 && abs(x-17)+abs(z-17)<=5', floor_y: 0, guarded_floor_y: 6, ornamental_only: true, external_vertical_port: false },
  pillars: [[10, 10], [23, 10], [10, 23], [23, 23]],
  vertical_connections: [
    { from_feet_y: 1, to_feet_y: 7, flights: ['north-rise', 'south-rise'] },
    { from_feet_y: 7, to_feet_y: 13, flights: ['east-rise', 'west-rise'] },
  ], minimum_route_headroom: 3,
}

export function footprint(across, depth) {
  return across >= 0 && across <= 34 && depth >= 0 && depth <= 34 && across + depth >= 7 && across + 34 - depth >= 7 && 34 - across + depth >= 7 && 68 - across - depth >= 7
}

export function shaft(across, depth) {
  const deltaAcross = Math.abs(across - 17)
  const deltaDepth = Math.abs(depth - 17)
  return deltaAcross <= 3 && deltaDepth <= 3 && deltaAcross + deltaDepth <= 5
}

export function boundary(across, depth) {
  return footprint(across, depth) && [[1, 0], [-1, 0], [0, 1], [0, -1]].some(([offsetAcross, offsetDepth]) => !footprint(across + offsetAcross, depth + offsetDepth))
}

export default function build(api) {
  const structure = api.createStructure(blueprint.size)
  const put = (across, height, depth, block, properties, nbt) => api.setBlock(structure, [across, height, depth], 'minecraft:' + block, properties, nbt)
  const fill = (from, to, block, properties) => api.fill(structure, from, to, 'minecraft:' + block, properties)
  const slab = { type: 'bottom', waterlogged: 'false' }
  fill([0, 0, 0], [34, 17, 34], 'air')
  for (let across = 0; across < 35; across++) {
    for (let depth = 0; depth < 35; depth++) {
      if (!footprint(across, depth)) continue
      const boundaryCell = boundary(across, depth)
      for (let height = 0; height <= 6; height++) {
        const weathering = (across * 17 + depth * 31 + height * 13) % 29
        put(across, height, depth, height === 6 ? (weathering < 3 ? 'mossy_stone_bricks' : weathering === 4 ? 'cracked_stone_bricks' : 'stone_bricks') : 'stone_bricks')
      }
      if (boundaryCell) {
        for (let height = 1; height < 17; height++) {
          const weathering = (across * 11 + depth * 19 + height * 7) % 23
          put(across, height, depth, height === 6 || height === 16 ? 'deepslate_bricks' : weathering < 4 ? 'mossy_stone_bricks' : weathering === 5 ? 'cracked_stone_bricks' : 'stone_bricks')
        }
      }
      put(across, 17, depth, across % 8 === 1 || depth % 8 === 1 || boundaryCell ? 'deepslate_bricks' : 'stone_bricks')
      if (shaft(across, depth)) fill([across, 1, depth], [across, 16, depth], 'air')
    }
  }
  for (const nearNorth of [true, false]) {
    const fromDepth = nearNorth ? 0 : 26
    const toDepth = nearNorth ? 8 : 34
    fill([16, 1, fromDepth], [18, 16, toDepth], 'air')
    for (const sideAcross of [15, 19]) {
      fill([sideAcross, 0, fromDepth], [sideAcross, 7, toDepth], 'stone_bricks')
      fill([sideAcross, 8, fromDepth], [sideAcross, 8, toDepth], 'stone_brick_slab', slab)
    }
  }
  const ring = []
  for (let across = 12; across <= 22; across++) {
    for (let depth = 12; depth <= 22; depth++) {
      if (shaft(across, depth)) continue
      if (![[1, 0], [-1, 0], [0, 1], [0, -1]].some(([offsetAcross, offsetDepth]) => shaft(across + offsetAcross, depth + offsetDepth))) continue
      ring.push([across, depth])
      put(across, 7, depth, (across + depth) % 4 === 0 ? 'chiseled_stone_bricks' : 'stone_bricks')
      put(across, 8, depth, 'stone_brick_slab', slab)
    }
  }
  for (const flight of blueprint.stairs) {
    const [deltaAcross, deltaDepth] = flight.direction
    const sideways = deltaAcross === 0 ? [1, 0] : [0, 1]
    for (let step = 0; step < 6; step++) {
      const centerAcross = flight.first[0] + deltaAcross * step
      const height = flight.first[1] + step
      const centerDepth = flight.first[2] + deltaDepth * step
      for (let lane = -1; lane <= 1; lane++) {
        const across = centerAcross + sideways[0] * lane
        const depth = centerDepth + sideways[1] * lane
        fill([across, 0, depth], [across, height - 1, depth], 'stone_bricks')
        put(across, height, depth, 'stone_brick_stairs', { facing: flight.facing, half: 'bottom', shape: 'straight', waterlogged: 'false' })
      }
      if (deltaAcross !== 0) {
        for (const side of [-2, 2]) {
          const across = centerAcross
          const depth = centerDepth + side
          fill([across, 6, depth], [across, height + 1, depth], 'stone_bricks')
          put(across, height + 2, depth, 'stone_brick_slab', slab)
        }
      }
    }
  }
  for (const east of [true, false]) {
    const fromAcross = east ? 32 : 0
    const toAcross = east ? 34 : 2
    fill([fromAcross, 0, 15], [toAcross, 12, 19], 'stone_bricks')
    fill([fromAcross, 13, 16], [toAcross, 16, 18], 'air')
    for (const sideDepth of [15, 19]) {
      fill([fromAcross, 13, sideDepth], [toAcross, 13, sideDepth], 'stone_bricks')
      fill([fromAcross, 14, sideDepth], [toAcross, 14, sideDepth], 'stone_brick_slab', slab)
    }
  }
  for (const [pillarAcross, pillarDepth] of blueprint.pillars) {
    for (let height = 6; height < 17; height++) fill([pillarAcross, height, pillarDepth], [pillarAcross + 1, height, pillarDepth + 1], height === 6 || height === 7 || height === 15 || height === 16 ? 'deepslate_bricks' : height === 11 ? 'chiseled_stone_bricks' : 'polished_andesite')
  }
  for (const depth of [0, 34]) fill([16, 4, depth], [18, 16, depth], 'stone_bricks')
  for (const across of [0, 34]) fill([across, 16, 16], [across, 16, 18], 'stone_bricks')
  const lamps = []
  const standLamp = (across, height, depth) => {
    put(across, height - 1, depth, 'chiseled_stone_bricks')
    put(across, height, depth, 'lantern', { hanging: 'false', waterlogged: 'false' })
    lamps.push({ position: [across, height, depth], support: [across, height - 1, depth], hanging: false })
  }
  for (const [across, depth] of [[14, 10], [20, 10], [14, 24], [20, 24], [10, 14], [10, 20], [24, 14], [24, 20]]) standLamp(across, 8, depth)
  for (const [across, depth] of [[9, 9], [25, 9], [9, 25], [25, 25]]) {
    put(across, 14, depth, 'lantern', { hanging: 'true', waterlogged: 'false' })
    fill([across, 15, depth], [across, 16, depth], 'chain', { axis: 'y', waterlogged: 'false' })
    lamps.push({ position: [across, 14, depth], support: [across, 15, depth], anchor: [across, 17, depth], hanging: true })
  }
  for (const depth of [1, 33]) {
    for (const across of [15, 19]) {
      put(across, 2, depth, 'chiseled_stone_bricks')
      put(across, 3, depth, 'lantern', { hanging: 'false', waterlogged: 'false' })
      lamps.push({ position: [across, 3, depth], support: [across, 2, depth], hanging: false })
    }
  }
  for (const across of [1, 33]) for (const depth of [15, 19]) standLamp(across, 14, depth)
  fill([4, 7, 22], [6, 7, 24], 'stone_bricks')
  for (let across = 4; across <= 6; across++) for (let depth = 22; depth <= 24; depth++) put(across, 8, depth, across === depth - 18 ? 'blue_carpet' : (across + depth) % 2 ? 'lime_carpet' : 'yellow_carpet')
  put(6, 8, 24, 'lantern', { hanging: 'false', waterlogged: 'false' })
  lamps.push({ position: [6, 8, 24], support: [6, 7, 24], hanging: false })
  fill([2, 7, 11], [2, 9, 14], 'bookshelf')
  fill([2, 10, 11], [2, 10, 14], 'stone_brick_slab', slab)
  for (const [across, depth] of [[3, 10], [3, 15], [31, 23], [31, 25]]) put(across, 7, depth, 'oak_planks')
  fill([33, 8, 22], [33, 10, 25], 'oak_planks')
  fill([32, 7, 23], [32, 8, 24], 'stone_bricks')
  fill([32, 9, 23], [32, 9, 24], 'cartography_table')
  const signs = [
    { position: [17, 4, 1], support: [17, 4, 0], facing: 'south', lines: ['N03 NORTH', 'LOWER +0', 'HUB +6', 'STAIRS AHEAD'] },
    { position: [17, 4, 33], support: [17, 4, 34], facing: 'north', lines: ['N03 SOUTH', 'LOWER +0', 'HUB +6', 'STAIRS AHEAD'] },
    { position: [33, 16, 17], support: [34, 16, 17], facing: 'west', lines: ['N03 EAST', 'UPPER +12', 'HUB +6', 'MIND THE EDGE'] },
    { position: [1, 16, 17], support: [0, 16, 17], facing: 'east', lines: ['N03 WEST', 'UPPER +12', 'HUB +6', 'MIND THE EDGE'] },
  ]
  for (const sign of signs) {
    put(...sign.support, 'stone_bricks')
    const blank = Array.from({ length: 4 }, () => JSON.stringify({ text: '' }))
    const nbt = api.createBlockNbt({ id: 'minecraft:sign', is_waxed: true, front_text: { color: 'black', has_glowing_text: false, messages: sign.lines.map(text => JSON.stringify({ text })) }, back_text: { color: 'black', has_glowing_text: false, messages: blank } })
    put(...sign.position, 'oak_wall_sign', { facing: sign.facing, waterlogged: 'false' }, nbt)
  }
  return { structure, metadata: { ...blueprint, name: 'room', title: blueprint.name, balustrade: ring, lamps, signs, map_table: { from: [4, 7, 22], to: [6, 8, 24] } } }
}
