export const specification = {
  id: 'N01', name: '四向枢纽 · 罗盘灯台', size: [25, 12, 25], minecraft: '1.20.1', data_version: 3465,
  reference: 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/hub/N01-hub-four-way-junction.png',
  reference_sha256: 'fc72ab0f0f5650c07a959cf31a1de9bbb263af60ca5d7356d6255d35fac954fc',
  floor_y: 1, walking_y: 2, route_width: 3, minimum_route_headroom: 5,
  center_safe: [12, 2, 9], safe_area: { from: [11, 2, 8], to: [13, 6, 10] },
  central_obstacle: { from: [11, 2, 11], to: [13, 4, 13] },
  ports: [
    { id: 'N', direction: 'north', normal: [0, 0, -1], center: [12, 2, 0], aperture: { from: [10, 2, 0], to: [14, 6, 0] }, throat: { from: [10, 2, 0], to: [14, 6, 2] }, floor_y: 1, width: 5, height: 5 },
    { id: 'E', direction: 'east', normal: [1, 0, 0], center: [24, 2, 12], aperture: { from: [24, 2, 10], to: [24, 6, 14] }, throat: { from: [22, 2, 10], to: [24, 6, 14] }, floor_y: 1, width: 5, height: 5 },
    { id: 'S', direction: 'south', normal: [0, 0, 1], center: [12, 2, 24], aperture: { from: [10, 2, 24], to: [14, 6, 24] }, throat: { from: [10, 2, 22], to: [14, 6, 24] }, floor_y: 1, width: 5, height: 5 },
    { id: 'W', direction: 'west', normal: [-1, 0, 0], center: [0, 2, 12], aperture: { from: [0, 2, 10], to: [0, 6, 14] }, throat: { from: [0, 2, 10], to: [2, 6, 14] }, floor_y: 1, width: 5, height: 5 },
  ],
  pillars: [
    { shaft: { from: [4, 3, 4], to: [5, 9, 5] }, base: { from: [3, 2, 3], to: [6, 2, 6] }, capital: { from: [3, 10, 3], to: [6, 10, 6] } },
    { shaft: { from: [19, 3, 4], to: [20, 9, 5] }, base: { from: [18, 2, 3], to: [21, 2, 6] }, capital: { from: [18, 10, 3], to: [21, 10, 6] } },
    { shaft: { from: [4, 3, 19], to: [5, 9, 20] }, base: { from: [3, 2, 18], to: [6, 2, 21] }, capital: { from: [3, 10, 18], to: [6, 10, 21] } },
    { shaft: { from: [19, 3, 19], to: [20, 9, 20] }, base: { from: [18, 2, 18], to: [21, 2, 21] }, capital: { from: [18, 10, 18], to: [21, 10, 21] } },
  ],
  hanging_lanterns: [[7, 7], [17, 7], [7, 17], [17, 17], [8, 3], [16, 3], [21, 8], [21, 16], [16, 21], [8, 21], [3, 16], [3, 8]],
  rails: [[19, 2, 17], [20, 2, 17], [21, 2, 17], [22, 2, 17]], deployment: 'NOT_DEPLOYED',
}

export function reservedRoute(blockX, blockZ) {
  const north = blockX >= 11 && blockX <= 13 && blockZ <= 10
  const south = blockX >= 11 && blockX <= 13 && blockZ >= 14
  const west = blockZ >= 11 && blockZ <= 13 && blockX <= 10
  const east = blockZ >= 11 && blockZ <= 13 && blockX >= 14
  const ring = blockX >= 8 && blockX <= 16 && blockZ >= 8 && blockZ <= 16
    && !(blockX >= 11 && blockX <= 13 && blockZ >= 11 && blockZ <= 13)
  return north || south || west || east || ring
}

export default function build(api) {
  const structure = api.createStructure(specification.size)
  const put = (position, block, properties, nbt) => api.setBlock(structure, position, block, properties, nbt)
  const fill = (from, to, block, properties) => api.fill(structure, from, to, block, properties)
  fill([0, 0, 0], [24, 11, 24], 'minecraft:air')
  fill([0, 0, 0], [24, 0, 24], 'minecraft:deepslate_bricks')
  for (let blockX = 0; blockX < 25; blockX++) {
    for (let blockZ = 0; blockZ < 25; blockZ++) {
      const seed = (blockX * 59 + blockZ * 31 + blockX * blockZ * 7) % 47
      const corner = Math.min(blockX, 24 - blockX) < 7 && Math.min(blockZ, 24 - blockZ) < 7
      const material = corner && seed < 14 ? 'mossy_stone_bricks' : seed < 8 ? 'cracked_stone_bricks' : 'stone_bricks'
      put([blockX, 1, blockZ], `minecraft:${material}`)
      put([blockX, 11, blockZ], `minecraft:${(blockX + blockZ) % 9 === 0 ? 'cracked_stone_bricks' : 'stone_bricks'}`)
    }
  }
  fill([0, 2, 0], [1, 10, 24], 'minecraft:stone_bricks')
  fill([23, 2, 0], [24, 10, 24], 'minecraft:stone_bricks')
  fill([2, 2, 0], [22, 10, 1], 'minecraft:stone_bricks')
  fill([2, 2, 23], [22, 10, 24], 'minecraft:stone_bricks')
  for (let along = 2; along <= 22; along++) {
    for (let blockY = 2; blockY <= 9; blockY++) {
      if (along >= 9 && along <= 15) continue
      const seed = (along * 17 + blockY * 11) % 19
      const material = seed < 5 ? 'mossy_stone_bricks' : seed < 7 ? 'cracked_stone_bricks' : 'stone_bricks'
      for (const position of [[along, blockY, 1], [23, blockY, along], [along, blockY, 23], [1, blockY, along]]) put(position, `minecraft:${material}`)
    }
  }
  for (const pillar of specification.pillars) {
    fill(pillar.base.from, pillar.base.to, 'minecraft:polished_andesite')
    fill(pillar.shaft.from, pillar.shaft.to, 'minecraft:stone_bricks')
    fill(pillar.capital.from, pillar.capital.to, 'minecraft:polished_andesite')
    for (const blockX of [pillar.shaft.from[0], pillar.shaft.to[0]]) {
      for (const blockZ of [pillar.shaft.from[2], pillar.shaft.to[2]]) put([blockX, 6, blockZ], 'minecraft:chiseled_stone_bricks')
    }
  }
  for (const beam of [4, 5, 19, 20]) {
    fill([beam, 10, 4], [beam, 10, 20], 'minecraft:polished_andesite')
    fill([4, 10, beam], [20, 10, beam], 'minecraft:polished_andesite')
  }
  const transformNorth = (position, quarterTurns) => {
    let result = [...position]
    for (let turn = 0; turn < quarterTurns; turn++) result = [24 - result[2], result[1], result[0]]
    return result
  }
  const sign = (position, facing, lines) => {
    const messages = lines.map(text => JSON.stringify({ text }))
    const text = { messages, color: 'white', has_glowing_text: true }
    const nbt = api.createBlockNbt({ id: 'minecraft:sign', is_waxed: true, front_text: text, back_text: { messages: ['', '', '', ''].map(text => JSON.stringify({ text })), color: 'black', has_glowing_text: false } })
    put(position, 'minecraft:spruce_wall_sign', { facing, waterlogged: 'false' }, nbt)
  }
  const inward = ['south', 'west', 'north', 'east']
  const outward = ['north', 'east', 'south', 'west']
  for (let quarterTurns = 0; quarterTurns < 4; quarterTurns++) {
    for (const jamb of [9, 15]) {
      for (let blockY = 2; blockY <= 6; blockY++) {
        for (let depth = 0; depth <= 2; depth++) put(transformNorth([jamb, blockY, depth], quarterTurns), 'minecraft:polished_andesite')
      }
    }
    for (let along = 9; along <= 15; along++) {
      for (let depth = 0; depth <= 2; depth++) put(transformNorth([along, 7, depth], quarterTurns), 'minecraft:chiseled_stone_bricks')
    }
    for (let along = 10; along <= 14; along++) {
      for (const blockY of [8, 9]) put(transformNorth([along, blockY, 1], quarterTurns), 'minecraft:polished_blackstone_bricks')
    }
    const port = specification.ports[quarterTurns]
    sign(transformNorth([12, 8, 2], quarterTurns), inward[quarterTurns], ['↑  ↑  ↑', `PORT ${port.id}`, 'N01 HUB', '5 x 5'])
    sign(transformNorth([12, 8, 0], quarterTurns), outward[quarterTurns], ['↓  ↓  ↓', 'N01 HUB', `PORT ${port.id}`, '5 x 5'])
    fill(port.throat.from, port.throat.to, 'minecraft:air')
  }
  for (let blockX = 6; blockX <= 18; blockX++) {
    for (let blockZ = 6; blockZ <= 18; blockZ++) {
      const deltaX = blockX - 12
      const deltaZ = blockZ - 12
      const radius = Math.hypot(deltaX, deltaZ)
      if (radius >= 5.2 && radius <= 6.4) put([blockX, 1, blockZ], 'minecraft:polished_deepslate')
      if (radius < 5.2 && (Math.abs(deltaX) <= 1 || Math.abs(deltaZ) <= 1 || Math.abs(deltaX) === Math.abs(deltaZ))) put([blockX, 1, blockZ], 'minecraft:calcite')
      if (radius < 5.2 && (deltaX === 0 || deltaZ === 0)) put([blockX, 1, blockZ], 'minecraft:smooth_sandstone')
    }
  }
  for (const stripe of [10, 14]) {
    for (let along = 0; along <= 4; along++) {
      for (let quarterTurns = 0; quarterTurns < 4; quarterTurns++) put(transformNorth([stripe, 1, along], quarterTurns), 'minecraft:polished_deepslate')
    }
  }
  fill([11, 2, 11], [13, 2, 13], 'minecraft:polished_andesite')
  put([12, 3, 12], 'minecraft:dark_oak_log', { axis: 'y' })
  put([12, 4, 12], 'minecraft:lantern', { hanging: 'false', waterlogged: 'false' })
  const wayfinders = [
    { position: [12, 3, 11], facing: 'north', text: '↑ N' },
    { position: [13, 3, 12], facing: 'east', text: '↑ E' },
    { position: [12, 3, 13], facing: 'south', text: '↑ S' },
    { position: [11, 3, 12], facing: 'west', text: '↑ W' },
  ]
  for (const item of wayfinders) sign(item.position, item.facing, [item.text, 'N01', 'WAYFINDER', ''])
  for (const [blockX, blockZ] of specification.hanging_lanterns) {
    fill([blockX, 8, blockZ], [blockX, 10, blockZ], 'minecraft:chain', { axis: 'y', waterlogged: 'false' })
    put([blockX, 7, blockZ], 'minecraft:lantern', { hanging: 'true', waterlogged: 'false' })
  }
  for (const [blockX, blockZ] of [[3, 8], [4, 8], [21, 8], [22, 8], [20, 22], [21, 22], [3, 16]]) put([blockX, 2, blockZ], 'minecraft:spruce_planks')
  for (const position of [[3, 3, 8], [21, 3, 8], [21, 3, 22]]) put(position, 'minecraft:stripped_spruce_log', { axis: 'y' })
  for (const position of specification.rails) put(position, 'minecraft:rail', { shape: 'east_west', waterlogged: 'false' })
  put([18, 2, 17], 'minecraft:polished_blackstone_bricks')
  return { structure, metadata: { name: 'room', ...specification } }
}
