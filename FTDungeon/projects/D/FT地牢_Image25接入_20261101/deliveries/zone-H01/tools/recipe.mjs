export default function build(api) {
  const structure = api.createStructure([29, 18, 33])
  const put = (east, up, south, block, nbt) => api.setBlock(structure, [east, up, south], 'minecraft:' + block, undefined, nbt)
  const box = (from, to, block) => api.fill(structure, from, to, 'minecraft:' + block)
  const stair = facing => `polished_andesite_stairs[facing=${facing},half=bottom,shape=straight,waterlogged=false]`
  box([0, 0, 0], [28, 17, 32], 'air')
  box([0, 0, 0], [28, 1, 32], 'stone_bricks')
  for (let east = 2; east <= 26; east++) {
    for (let south = 2; south <= 30; south++) {
      const choice = (east * 31 + south * 17 + east * south) % 37
      put(east, 1, south, choice < 3 ? 'mossy_stone_bricks' : choice < 7 ? 'cracked_stone_bricks' : 'stone_bricks')
    }
  }
  box([0, 2, 0], [1, 15, 32], 'stone_bricks')
  box([27, 2, 0], [28, 15, 32], 'stone_bricks')
  box([2, 2, 0], [26, 15, 1], 'stone_bricks')
  box([2, 2, 31], [26, 15, 32], 'stone_bricks')
  box([0, 16, 0], [28, 17, 32], 'deepslate_bricks')
  for (const up of [2, 12, 15]) {
    box([0, up, 0], [28, up, 0], 'polished_andesite')
    box([0, up, 32], [28, up, 32], 'polished_andesite')
    box([0, up, 0], [0, up, 32], 'polished_andesite')
    box([28, up, 0], [28, up, 32], 'polished_andesite')
  }
  box([12, 2, 0], [16, 7, 1], 'air')
  box([11, 8, 0], [17, 9, 1], 'chiseled_stone_bricks')
  for (const east of [10, 18]) {
    box([east, 2, 0], [east, 8, 1], 'polished_andesite')
    put(east, 7, 0, 'gold_block')
  }
  for (let south = 0; south <= 22; south++) {
    box([12, 1, south], [16, 1, south], 'red_wool')
    for (const east of [11, 17]) put(east, 1, south, 'yellow_terracotta')
    if (south % 5 === 2) for (const east of [11, 17]) put(east, 1, south, 'gold_block')
  }
  for (const south of [8, 17, 28]) {
    for (const east of [6, 22]) {
      box([east - 1, 2, south - 1], [east + 1, 3, south + 1], 'stone_bricks')
      put(east, 4, south, 'gold_block')
      box([east, 5, south], [east, 10, south], 'polished_andesite')
      put(east, 11, south, 'gold_block')
      box([east - 1, 12, south - 1], [east + 1, 13, south + 1], 'chiseled_stone_bricks')
      box([east - 1, 14, south - 1], [east + 1, 15, south + 1], 'stone_bricks')
    }
    box([2, 15, south], [26, 15, south], 'polished_andesite')
    for (let east = 8; east <= 20; east++) {
      const underside = 12 + Math.min(2, Math.floor(Math.min(east - 8, 20 - east) / 2))
      box([east, underside, south], [east, 14, south], 'stone_bricks')
    }
  }
  for (const east of [6, 22]) box([east, 15, 2], [east, 15, 30], 'stone_bricks')
  for (const east of [0, 1, 27, 28]) {
    for (const south of [11, 21]) box([east, 5, south], [east, 9, south + 2], 'light_blue_stained_glass')
    for (const south of [12, 22]) box([east, 5, south], [east, 9, south], 'polished_andesite')
    for (const south of [11, 21]) box([east, 7, south], [east, 7, south + 2], 'polished_andesite')
  }
  const banner = api.createBlockNbt({ id: 'minecraft:banner', Patterns: [{ Color: 4, Pattern: 'bo' }, { Color: 4, Pattern: 'mc' }] })
  for (const side of [{ east: 2, facing: 'east', bannerEast: 3 }, { east: 26, facing: 'west', bannerEast: 25 }]) {
    for (const south of [7, 17, 27]) {
      box([side.east, 6, south - 1], [side.east, 11, south + 1], 'red_wool')
      box([side.east, 11, south - 1], [side.east, 11, south + 1], 'yellow_terracotta')
      put(side.bannerEast, 10, south, `red_wall_banner[facing=${side.facing}]`, banner)
    }
  }
  for (let east = 8; east <= 20; east++) {
    put(east, 2, 23, stair('south'))
    put(east, 2, 24, 'stone_bricks')
    put(east, 3, 24, stair('south'))
  }
  box([8, 2, 25], [20, 3, 30], 'polished_andesite')
  box([12, 3, 25], [16, 3, 28], 'red_wool')
  for (const east of [11, 17]) box([east, 3, 25], [east, 3, 28], 'yellow_terracotta')
  box([12, 4, 28], [16, 4, 30], 'chiseled_stone_bricks')
  box([13, 5, 29], [15, 8, 29], 'polished_andesite')
  box([14, 6, 28], [14, 8, 28], 'red_wool')
  put(14, 5, 28, 'red_wool')
  put(14, 9, 29, 'gold_block')
  for (const east of [12, 16]) put(east, 5, 28, 'polished_andesite_slab[type=bottom,waterlogged=false]')
  box([10, 10, 30], [18, 12, 30], 'red_wool')
  box([9, 13, 30], [19, 13, 30], 'gold_block')
  for (const east of [10, 11, 17, 18]) box([east, east === 10 || east === 18 ? 6 : 8, 30], [east, 9, 30], 'red_wool')
  for (const [east, south, ground] of [[9, 6, 1], [19, 6, 1], [9, 27, 3], [19, 27, 3]]) {
    box([east, ground + 1, south], [east, ground + 2, south], 'chiseled_stone_bricks')
    put(east, ground + 3, south, 'campfire[facing=north,lit=true,signal_fire=false,waterlogged=false]')
  }
  for (const [east, south] of [[9, 8], [19, 8], [9, 17], [19, 17], [9, 28], [19, 28]]) {
    box([east, 13, south], [east, 15, south], 'polished_andesite')
    box([east, 10, south], [east, 12, south], 'chain[axis=y,waterlogged=false]')
    put(east, 9, south, 'lantern[hanging=true,waterlogged=false]')
  }
  for (const south of [11, 12, 13, 14]) {
    box([2, 2, south], [2, 4, south], 'bookshelf')
    put(2, 5, south, 'spruce_slab[type=bottom,waterlogged=false]')
  }
  const barrel = api.createBlockNbt({ id: 'minecraft:barrel' })
  for (const [east, south] of [[3, 4], [4, 4], [3, 5], [25, 5], [25, 25]]) put(east, 2, south, 'barrel[facing=up,open=false]', barrel)
  put(3, 3, 4, 'barrel[facing=up,open=false]', barrel)
  box([24, 2, 19], [26, 2, 22], 'polished_andesite')
  for (const south of [19, 21]) {
    box([25, 3, south], [25, 5, south], 'iron_bars[north=false,east=false,south=false,west=false,waterlogged=false]')
    put(25, 6, south, 'gold_block')
  }
  for (const [east, up, south] of [[2, 12, 4], [26, 12, 4], [2, 12, 29], [26, 12, 29], [13, 14, 31], [15, 14, 31]]) put(east, up, south, 'cobweb')
  return { structure, metadata: { name: 'room', id: 'H01', design: 'throne hall with complete roof; north entrance' } }
}
