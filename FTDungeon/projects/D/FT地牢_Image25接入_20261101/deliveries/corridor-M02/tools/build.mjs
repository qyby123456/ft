export const specification = {
  id: 'corridor-M02',
  sourceId: 'M02',
  name: 'M02 · 浸水档案长廊',
  size: [17, 13, 41],
  entrance: [8, 2, 0],
  interior: [8, 2, 3],
  exit: [8, 2, 40],
  facing: 'north',
  reference: 'D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/M02-archive-library-flooded.png',
  reference_sha256: '96e45b7d9176fdc57b70e99af2dfde21638295e60f263d90b3cdb9099c8d6ccc',
  main_path: { x: [7, 9], feet_y: 2, clear_y: [2, 4], z: [0, 40], width: 3 },
  water_channels: [{ x: [4, 6], y: 1, z: [2, 38] }, { x: [10, 12], y: 1, z: [2, 38] }],
  cages: [4, 12, 20, 28, 36].map((posZ, index) => ({ center: [index % 2 ? 11 : 5, 7, posZ], floor_y: 5, top_y: 8, chain_y: 9, anchor_y: 10 })),
  adaptations: ['A level 3-wide dry causeway keeps the flooded design safely traversable without stairs.', 'Shroomlights behind orange glass reproduce lava-glow without live lava or fire beside wooden shelves.'],
}

export default function build(api) {
  const structure = api.createStructure(specification.size)
  const put = (posX, posY, posZ, block, properties) => api.setBlock(structure, [posX, posY, posZ], block.startsWith('minecraft:') ? block : `minecraft:${block}`, properties)
  const fill = (from, to, block, properties) => api.fill(structure, from, to, `minecraft:${block}`, properties)
  const stone = (posX, posY, posZ) => {
    const choice = (posX * 59 + posY * 37 + posZ * 17 + posX * posZ * 7) % 31
    return choice < 5 ? 'mossy_stone_bricks' : choice < 8 ? 'cracked_stone_bricks' : 'stone_bricks'
  }
  fill([0, 0, 0], [16, 12, 40], 'air')
  for (let posX = 0; posX < 17; posX++) {
    for (let posZ = 0; posZ < 41; posZ++) {
      put(posX, 0, posZ, stone(posX, 0, posZ))
      put(posX, 1, posZ, stone(posX, 1, posZ))
      put(posX, 11, posZ, stone(posX, 11, posZ))
      put(posX, 12, posZ, posX === 0 || posX === 16 || posZ === 0 || posZ === 40 ? 'polished_andesite' : 'stone_bricks')
      for (let posY = 2; posY < 11; posY++) {
        if (posX <= 2 || posX >= 14 || posZ === 0 || posZ === 40) put(posX, posY, posZ, stone(posX, posY, posZ))
      }
    }
  }
  for (const channel of specification.water_channels) fill([channel.x[0], 1, 2], [channel.x[1], 1, 38], 'water', { level: '0' })
  for (let posZ = 0; posZ < 41; posZ++) {
    for (let posX = 7; posX <= 9; posX++) put(posX, 1, posZ, posX === 8 ? (posZ % 4 === 0 ? 'chiseled_stone_bricks' : 'polished_andesite') : 'stone_bricks')
  }
  for (let bay = 0; bay < 5; bay++) {
    const start = bay * 8
    for (const side of [3, 13]) {
      fill([side, 2, start + 2], [side, 9, start + 6], 'bookshelf')
      for (const shelfY of [2, 6, 9]) fill([side, shelfY, start + 2], [side, shelfY, start + 6], 'dark_oak_planks')
      for (const frameZ of [start + 2, start + 6]) fill([side, 2, frameZ], [side, 9, frameZ], 'dark_oak_log', { axis: 'y' })
      fill([side, 4, start + 3], [side, 5, start + 5], 'air')
      for (const scrollZ of [start + 3, start + 4]) put(side, 4, scrollZ, 'stripped_birch_log', { axis: 'x' })
      put(side, 4, start + 5, 'candle', { candles: '2', lit: 'true', waterlogged: 'false' })
      const backing = side === 3 ? 2 : 14
      for (const seamZ of [start + 1, start + 7]) {
        fill([backing, 3, seamZ], [backing, 7, seamZ], 'shroomlight')
        fill([side, 3, seamZ], [side, 7, seamZ], 'orange_stained_glass')
        put(side, 2, seamZ, 'chiseled_stone_bricks')
        put(side, 8, seamZ, 'chiseled_stone_bricks')
        put(side, 9, seamZ, 'stone_bricks')
      }
      fill([side, 10, start], [side, 10, start + 7], 'dark_oak_log', { axis: 'z' })
    }
  }
  for (let beamZ = 0; beamZ <= 40; beamZ += 4) {
    fill([3, 10, beamZ], [13, 10, beamZ], 'dark_oak_log', { axis: 'x' })
    if (beamZ % 8 === 0) {
      for (const side of [3, 13]) {
        for (let posY = 1; posY < 10; posY++) put(side, posY, beamZ, posY % 3 === 0 ? 'chiseled_stone_bricks' : stone(side, posY, beamZ))
        const corbel = side === 3 ? 4 : 12
        put(corbel, 9, beamZ, 'stone_brick_slab', { type: 'top', waterlogged: 'false' })
        put(corbel, 10, beamZ, 'polished_andesite')
      }
    }
  }
  for (const cage of specification.cages) {
    const [centerX, , centerZ] = cage.center
    for (let posX = centerX - 1; posX <= centerX + 1; posX++) {
      for (let posZ = centerZ - 1; posZ <= centerZ + 1; posZ++) {
        put(posX, 5, posZ, 'iron_trapdoor', { facing: 'north', half: 'top', open: 'false', powered: 'false', waterlogged: 'false' })
        put(posX, 8, posZ, 'iron_trapdoor', { facing: 'north', half: 'bottom', open: 'false', powered: 'false', waterlogged: 'false' })
        if (posX !== centerX || posZ !== centerZ) {
          for (const posY of [6, 7]) put(posX, posY, posZ, 'iron_bars', { east: 'false', west: 'false', north: 'false', south: 'false', waterlogged: 'false' })
        }
      }
    }
    put(centerX, 8, centerZ, 'iron_block')
    put(centerX, 9, centerZ, 'chain', { axis: 'y', waterlogged: 'false' })
  }
  for (const portalZ of [0, 40]) {
    fill([6, 2, portalZ], [10, 5, portalZ], 'air')
    fill([7, 6, portalZ], [9, 6, portalZ], 'air')
    put(8, 7, portalZ, 'air')
    for (const jambX of [5, 11]) fill([jambX, 2, portalZ], [jambX, 6, portalZ], 'chiseled_stone_bricks')
    for (const capX of [6, 10]) put(capX, 6, portalZ, 'polished_andesite')
    for (const capX of [7, 9]) put(capX, 7, portalZ, 'polished_andesite')
    put(8, 8, portalZ, 'chiseled_stone_bricks')
  }
  return { structure, metadata: { ...specification, name: 'room' } }
}
