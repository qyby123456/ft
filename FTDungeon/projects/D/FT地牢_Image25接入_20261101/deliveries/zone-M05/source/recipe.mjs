export const specification = {
  id: 'zone-M05', name: '水晶矿窟', size: [27, 15, 27],
  entrance: [13, 2, 0], interior: [13, 2, 5],
  reference: 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/mines/M05-mines-crystal-mine-grotto.png',
  referenceSha256: '288230505f527d38f4045a44a1d57707de14d7b380bd939fdb3d935087413bb6',
  pillars: [[5, 7], [20, 7], [5, 19], [20, 19]],
  hangingLamps: [[9, 8, 7], [17, 8, 7], [9, 8, 20], [17, 8, 20]],
  blueCrystals: [[22, 3, 4], [18, 2, 10], [23, 3, 17], [18, 3, 23]],
  violetCrystals: [[17, 2, 4], [23, 3, 11], [23, 2, 23], [8, 2, 23]],
  waterRows: { 15: [17, 22], 16: [17, 22], 17: [17, 21], 18: [17, 22], 19: [17, 19], 20: [17, 19], 21: [17, 22], 22: [17, 22] }
}

export default function build(api) {
  const structure = api.createStructure(specification.size)
  const put = (east, up, south, state) => api.setBlock(structure, [east, up, south], state)
  const box = (from, to, state) => api.fill(structure, from, to, state)
  const rock = ['stone', 'andesite', 'tuff', 'stone', 'cobbled_deepslate', 'stone']
  box([0, 0, 0], [26, 14, 26], 'minecraft:air')
  box([0, 0, 0], [26, 0, 26], 'minecraft:deepslate')
  for (let east = 0; east < 27; east++) for (let south = 0; south < 27; south++) {
    put(east, 1, south, 'minecraft:' + rock[(east * 17 + south * 11) % rock.length])
    for (let up = 2; up <= 14; up++) {
      const depth = Math.min(east, south, 26 - east, 26 - south)
      if (depth < 2 || up >= 13 || (up === 12 && depth < 4) || (depth === 2 && (east * 11 + south * 7) % 5 < 2)) put(east, up, south, 'minecraft:' + rock[(east * 7 + south * 13 + up * 3) % rock.length])
    }
  }
  box([11, 1, 0], [15, 1, 24], 'minecraft:polished_andesite')
  box([3, 1, 12], [23, 1, 13], 'minecraft:polished_andesite')
  for (let south = 2; south <= 24; south += 3) put(13, 1, south, 'minecraft:chiseled_stone_bricks')
  box([11, 2, 0], [15, 6, 2], 'minecraft:air')
  box([10, 2, 0], [10, 7, 2], 'minecraft:stone_bricks')
  box([16, 2, 0], [16, 7, 2], 'minecraft:stone_bricks')
  box([10, 7, 0], [16, 8, 2], 'minecraft:stone_bricks')
  for (const [east, south] of specification.pillars) {
    box([east, 2, south], [east + 1, 2, south + 1], 'minecraft:polished_deepslate')
    box([east, 3, south], [east + 1, 12, south + 1], 'minecraft:stripped_spruce_log[axis=y]')
    for (const up of [5, 9]) box([east, up, south], [east + 1, up, south + 1], 'minecraft:polished_deepslate')
  }
  for (const south of [7, 8, 19, 20]) box([3, 12, south], [23, 12, south], 'minecraft:spruce_log[axis=x]')
  for (const east of [5, 6, 20, 21]) box([east, 12, 3], [east, 12, 23], 'minecraft:spruce_log[axis=z]')
  for (const [east, up, south] of specification.hangingLamps) {
    box([east, up + 1, south], [east, 11, south], 'minecraft:chain[axis=y,waterlogged=false]')
    put(east, up, south, 'minecraft:lantern[hanging=true,waterlogged=false]')
  }
  for (const [southText, [minimum, maximum]] of Object.entries(specification.waterRows)) for (let east = minimum; east <= maximum; east++) put(east, 1, Number(southText), 'minecraft:water[level=0]')
  box([22, 2, 3], [24, 2, 6], 'minecraft:calcite')
  box([22, 2, 16], [24, 2, 18], 'minecraft:calcite')
  box([17, 2, 23], [19, 2, 24], 'minecraft:calcite')
  box([22, 2, 9], [24, 2, 12], 'minecraft:amethyst_block')
  box([22, 2, 22], [24, 2, 24], 'minecraft:amethyst_block')
  for (const [east, up, south] of specification.blueCrystals) {
    box([east, 2, south], [east, up, south], 'minecraft:calcite')
    put(east, up, south, 'minecraft:sea_lantern')
    box([east, up + 1, south], [east, up + 3, south], 'minecraft:light_blue_stained_glass')
    for (const [offsetEast, offsetSouth] of [[-1, 0], [0, 1], [1, 0]]) {
      if (east + offsetEast > 24 || south + offsetSouth > 24) continue
      box([east + offsetEast, 2, south + offsetSouth], [east + offsetEast, up, south + offsetSouth], 'minecraft:calcite')
      put(east + offsetEast, up, south + offsetSouth, 'minecraft:calcite')
      put(east + offsetEast, up + 1, south + offsetSouth, 'minecraft:cyan_stained_glass')
      if (offsetEast === -1) put(east + offsetEast, up + 2, south + offsetSouth, 'minecraft:light_blue_stained_glass')
    }
  }
  for (const [east, up, south] of specification.violetCrystals) {
    put(east, up, south, 'minecraft:amethyst_block')
    put(east, up + 1, south, 'minecraft:amethyst_block')
    put(east, up + 2, south, 'minecraft:amethyst_cluster[facing=up,waterlogged=false]')
    for (const [offsetEast, offsetSouth] of [[-1, 0], [0, 1]]) {
      put(east + offsetEast, up, south + offsetSouth, 'minecraft:amethyst_block')
      put(east + offsetEast, up + 1, south + offsetSouth, 'minecraft:large_amethyst_bud[facing=up,waterlogged=false]')
    }
  }
  for (const [up, south] of [[5, 5], [7, 10], [5, 22]]) {
    put(24, up, south, 'minecraft:amethyst_block')
    put(23, up, south, 'minecraft:amethyst_cluster[facing=west,waterlogged=false]')
    box([24, 2, south], [24, up - 1, south], 'minecraft:amethyst_block')
  }
  box([2, 2, 8], [2, 7, 8], 'minecraft:spruce_log[axis=y]')
  box([2, 2, 12], [2, 7, 12], 'minecraft:spruce_log[axis=y]')
  box([2, 7, 8], [2, 7, 12], 'minecraft:spruce_log[axis=z]')
  box([1, 2, 9], [1, 6, 11], 'minecraft:deepslate_tiles')
  for (let east = 2; east <= 10; east++) {
    put(east, 1, 10, east % 2 ? 'minecraft:spruce_planks' : 'minecraft:gravel')
    put(east, 2, 10, 'minecraft:rail[shape=east_west,waterlogged=false]')
  }
  box([8, 2, 8], [10, 2, 8], 'minecraft:polished_blackstone_slab[type=bottom,waterlogged=false]')
  put(8, 2, 9, 'minecraft:stone_brick_stairs[facing=east,half=bottom,shape=straight,waterlogged=false]')
  put(10, 2, 9, 'minecraft:stone_brick_stairs[facing=west,half=bottom,shape=straight,waterlogged=false]')
  put(9, 2, 9, 'minecraft:polished_blackstone_slab[type=bottom,waterlogged=false]')
  put(7, 2, 9, 'minecraft:deepslate_diamond_ore')
  box([3, 2, 15], [4, 3, 16], 'minecraft:spruce_planks')
  box([6, 2, 16], [7, 2, 17], 'minecraft:spruce_planks')
  put(3, 4, 15, 'minecraft:lantern[hanging=false,waterlogged=false]')
  put(6, 3, 16, 'minecraft:lantern[hanging=false,waterlogged=false]')
  put(4, 2, 21, 'minecraft:chipped_anvil[facing=north]')
  box([4, 2, 23], [6, 2, 23], 'minecraft:spruce_planks')
  box([4, 3, 23], [6, 3, 23], 'minecraft:spruce_slab[type=bottom,waterlogged=false]')
  box([3, 2, 23], [3, 5, 23], 'minecraft:stripped_spruce_log[axis=y]')
  for (let up = 2; up <= 5; up++) put(3, up, 22, 'minecraft:ladder[facing=north,waterlogged=false]')
  for (const [east, up, south] of [[1, 4, 5], [1, 6, 18], [25, 7, 8], [23, 9, 25], [8, 5, 25]]) put(east, up, south, 'minecraft:deepslate_diamond_ore')
  for (let east = 11; east <= 15; east++) for (let south = 0; south <= 24; south++) box([east, 2, south], [east, 5, south], 'minecraft:air')
  return { structure, metadata: { name: 'room', id: specification.id, title: specification.name, size: specification.size, entrance: specification.entrance, interior: specification.interior, reference: specification.reference, referenceSha256: specification.referenceSha256, entity_free_cart: true, roof: 'complete y=14; all cuts are preview-only' } }
}
