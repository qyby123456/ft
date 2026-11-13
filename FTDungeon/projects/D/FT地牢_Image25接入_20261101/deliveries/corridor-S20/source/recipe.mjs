export const specification = {
  id: 'S20',
  name: '废弃矿道·浅水短廊',
  size: [11, 9, 16],
  entrance: [5, 2, 0],
  exit: [5, 2, 15],
  interior: [5, 2, 7],
  feetY: 2,
  routeX: [4, 6],
  supportStations: [1, 5, 10, 14],
  reference: 'D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/S20-abandoned-mine-flooded.png',
  reference_sha256: 'f29b7371cb7f60007dc799b199dd6ceca086fe3b97baaa757ec98ef13b033112',
}

export default function build(api) {
  const structure = api.createStructure(specification.size)
  const place = (position, block) => api.setBlock(structure, position, `minecraft:${block}`)
  const fill = (from, to, block) => api.fill(structure, from, to, `minecraft:${block}`)
  fill([0, 0, 0], [10, 8, 15], 'air')
  fill([0, 0, 0], [10, 0, 15], 'stone')
  fill([0, 8, 0], [10, 8, 15], 'stone_bricks')

  const stonePalette = ['stone', 'stone', 'stone_bricks', 'stone', 'cobblestone', 'cracked_stone_bricks']
  for (const wallX of [0, 10]) {
    for (let blockZ = 0; blockZ < 16; blockZ++) {
      for (let blockY = 1; blockY < 8; blockY++) {
        const weather = (wallX * 7 + blockZ * 3 + blockY * 11) % 17
        const block = blockY <= 3 && weather < 6
          ? (weather % 2 === 0 ? 'mossy_cobblestone' : 'mossy_stone_bricks')
          : stonePalette[(blockZ * 5 + blockY * 3 + wallX) % stonePalette.length]
        place([wallX, blockY, blockZ], block)
      }
    }
  }

  for (let blockZ = 0; blockZ < 16; blockZ++) {
    fill([3, 1, blockZ], [7, 1, blockZ], 'mossy_stone_bricks')
    fill([4, 1, blockZ], [6, 1, blockZ], blockZ % 5 === 4 ? 'dark_oak_log[axis=x]' : 'dark_oak_planks')
    place([5, 2, blockZ], 'rail[shape=north_south,waterlogged=false]')
    place([2, 7, blockZ], 'stripped_dark_oak_log[axis=z]')
    place([8, 7, blockZ], 'stripped_dark_oak_log[axis=z]')
    if (blockZ >= 2 && blockZ <= 13) {
      fill([1, 1, blockZ], [2, 1, blockZ], 'water[level=0]')
      fill([8, 1, blockZ], [9, 1, blockZ], 'water[level=0]')
    } else {
      fill([1, 1, blockZ], [2, 1, blockZ], 'cobblestone')
      fill([8, 1, blockZ], [9, 1, blockZ], 'cobblestone')
    }
  }

  for (const blockZ of [0, 15]) {
    fill([1, 2, blockZ], [9, 7, blockZ], 'stone_bricks')
    fill([4, 2, blockZ], [6, 4, blockZ], 'air')
    place([5, 2, blockZ], 'rail[shape=north_south,waterlogged=false]')
    fill([3, 2, blockZ], [3, 5, blockZ], 'dark_oak_log[axis=y]')
    fill([7, 2, blockZ], [7, 5, blockZ], 'dark_oak_log[axis=y]')
    fill([3, 5, blockZ], [7, 5, blockZ], 'stripped_dark_oak_log[axis=x]')
    place([2, 3, blockZ], 'cracked_stone_bricks')
    place([8, 3, blockZ], 'mossy_stone_bricks')
    place([4, 6, blockZ], 'cracked_stone_bricks')
    place([6, 6, blockZ], 'mossy_stone_bricks')
  }

  for (const station of specification.supportStations) {
    for (const postX of [1, 9]) {
      place([postX, 1, station], 'cracked_stone_bricks')
      fill([postX, 2, station], [postX, 6, station], 'dark_oak_log[axis=y]')
    }
    fill([1, 7, station], [9, 7, station], 'stripped_dark_oak_log[axis=x]')
    place([2, 5, station], 'stone_brick_stairs[facing=east,half=top,shape=straight,waterlogged=false]')
    place([8, 5, station], 'stone_brick_stairs[facing=west,half=top,shape=straight,waterlogged=false]')
    place([3, 6, station], 'stone_brick_stairs[facing=east,half=top,shape=straight,waterlogged=false]')
    place([7, 6, station], 'stone_brick_stairs[facing=west,half=top,shape=straight,waterlogged=false]')
    for (let archX = 4; archX <= 6; archX++) {
      if ((station === 5 && archX === 5) || (station === 10 && archX === 6)) continue
      place([archX, 6, station], 'stone_brick_slab[type=top,waterlogged=false]')
    }
    place([2, 4, station], 'wall_torch[facing=east]')
    place([8, 4, station], 'wall_torch[facing=west]')
  }

  const oreVeins = [
    [0, 3, 3, 'gold_ore'], [0, 4, 3, 'gold_ore'], [0, 3, 4, 'gold_ore'],
    [0, 2, 8, 'iron_ore'], [0, 3, 8, 'iron_ore'], [0, 2, 9, 'iron_ore'],
    [0, 4, 12, 'copper_ore'], [0, 4, 13, 'copper_ore'],
    [10, 3, 3, 'redstone_ore[lit=false]'], [10, 4, 3, 'redstone_ore[lit=false]'],
    [10, 2, 7, 'diamond_ore'], [10, 3, 7, 'diamond_ore'], [10, 2, 8, 'diamond_ore'],
    [10, 3, 12, 'iron_ore'], [10, 4, 12, 'iron_ore'],
  ]
  for (const [blockX, blockY, blockZ, block] of oreVeins) place([blockX, blockY, blockZ], block)
  for (const [blockX, blockZ] of [[2, 1], [8, 14]]) place([blockX, 1, blockZ], 'gravel')
  return { structure, metadata: { ...specification, name: 'room' } }
}
