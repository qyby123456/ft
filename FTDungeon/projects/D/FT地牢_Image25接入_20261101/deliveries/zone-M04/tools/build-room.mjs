export default function build(api) {
  const structure = api.createStructure([25, 14, 25])
  const put = (width, height, depth, block) => api.setBlock(structure, [width, height, depth], `minecraft:${block}`)
  const box = (from, to, block) => api.fill(structure, from, to, `minecraft:${block}`)
  box([0, 0, 0], [24, 13, 24], 'air')
  box([0, 0, 0], [24, 0, 24], 'deepslate_tiles')
  box([0, 1, 0], [24, 2, 24], 'polished_blackstone_bricks')
  for (let width = 0; width < 25; width++) {
    for (let depth = 0; depth < 25; depth++) {
      const variation = (width * 31 + depth * 17) % 19
      if (variation < 4) put(width, 2, depth, variation < 2 ? 'deepslate_tiles' : 'cracked_deepslate_bricks')
    }
  }
  for (let height = 3; height <= 12; height++) {
    for (let offset = 0; offset < 25; offset++) {
      const material = (height + offset * 3) % 7 === 0 ? 'cracked_deepslate_bricks' : 'deepslate_bricks'
      put(offset, height, 0, material)
      put(offset, height, 24, material)
      put(0, height, offset, material)
      put(24, height, offset, material)
    }
  }
  box([0, 13, 0], [24, 13, 24], 'deepslate_tiles')
  for (const border of [0, 24]) {
    box([border, 12, 0], [border, 12, 24], 'polished_basalt[axis=y]')
    box([0, 12, border], [24, 12, border], 'polished_basalt[axis=y]')
  }
  box([11, 3, 0], [13, 6, 1], 'air')
  for (const width of [10, 14]) box([width, 3, 0], [width, 6, 1], 'polished_basalt[axis=y]')
  box([10, 7, 0], [14, 7, 1], 'chiseled_polished_blackstone')
  for (let depth = 0; depth <= 23; depth++) box([11, 2, depth], [13, 2, depth], 'polished_andesite')
  box([10, 2, 2], [14, 2, 6], 'polished_andesite')
  for (const width of [4, 20]) {
    for (const depth of [5, 20]) {
      put(width, 3, depth, 'polished_basalt[axis=y]')
      box([width, 4, depth], [width, 11, depth], 'stripped_spruce_log[axis=y]')
      for (const height of [5, 9]) put(width, height, depth, 'iron_block')
      put(width, 12, depth, 'polished_blackstone_bricks')
    }
    box([width, 12, 5], [width, 12, 20], 'stripped_spruce_log[axis=z]')
  }
  for (const depth of [5, 20]) box([4, 12, depth], [20, 12, depth], 'stripped_spruce_log[axis=x]')
  for (const [width, depth] of [[7, 5], [17, 5], [7, 20], [17, 20], [12, 10], [12, 18]]) {
    box([width, 9, depth], [width, 11, depth], 'chain[axis=y,waterlogged=false]')
    if (width === 12) put(width, 12, depth, 'chiseled_polished_blackstone')
    put(width, 8, depth, 'lantern[hanging=true,waterlogged=false]')
  }
  box([2, 1, 12], [22, 1, 16], 'polished_basalt[axis=y]')
  box([3, 1, 13], [21, 1, 15], 'lava[level=0]')
  box([3, 2, 13], [21, 2, 15], 'orange_stained_glass')
  box([11, 2, 12], [13, 2, 16], 'polished_andesite')
  for (const depth of [12, 16]) {
    for (let width = 2; width <= 22; width++) {
      if (width < 11 || width > 13) put(width, 3, depth, 'iron_bars[waterlogged=false]')
    }
  }
  for (const width of [2, 22, 10, 14]) {
    for (let depth = 13; depth <= 15; depth++) put(width, 3, depth, 'iron_bars[waterlogged=false]')
  }
  for (let depth = 3; depth <= 8; depth++) put(3, 3, depth, 'rail[shape=north_south,waterlogged=false]')
  put(3, 3, 9, 'rail[shape=north_east,waterlogged=false]')
  for (let width = 4; width <= 22; width++) put(width, 3, 9, 'rail[shape=east_west,waterlogged=false]')
  for (const [width, depth] of [[2, 4], [2, 5], [2, 21], [3, 22], [7, 22], [8, 22]]) put(width, 3, depth, 'barrel[facing=up,open=false]')
  put(2, 4, 5, 'barrel[facing=north,open=false]')
  box([6, 3, 21], [7, 3, 21], 'deepslate_iron_ore')
  put(6, 4, 21, 'deepslate_copper_ore')
  for (const depth of [5, 6, 7, 18, 19, 21]) {
    for (const height of [4, 5, 7]) {
      if ((depth + height) % 3 !== 0) {
        put(24, height, depth, (depth + height) % 2 ? 'deepslate_iron_ore' : 'deepslate_copper_ore')
        put(0, height, depth, (depth + height) % 2 ? 'deepslate_copper_ore' : 'deepslate_coal_ore')
      }
    }
  }
  for (const width of [3, 6, 8, 16, 19, 22]) put(width, 6, 24, 'deepslate_iron_ore')
  box([18, 3, 22], [22, 6, 23], 'polished_blackstone_bricks')
  put(18, 3, 21, 'furnace[facing=north,lit=false]')
  put(22, 3, 21, 'blast_furnace[facing=north,lit=false]')
  box([19, 3, 22], [21, 4, 22], 'glowstone')
  box([19, 3, 21], [21, 4, 21], 'orange_stained_glass')
  box([20, 7, 23], [20, 12, 23], 'polished_basalt[axis=y]')
  put(16, 3, 21, 'anvil[facing=east]')
  box([19, 3, 6], [21, 3, 7], 'polished_andesite')
  box([20, 4, 6], [20, 4, 7], 'coal_block')
  for (const width of [19, 21]) {
    for (const depth of [6, 7]) put(width, 4, depth, `spruce_trapdoor[facing=${width === 19 ? 'east' : 'west'},half=bottom,open=true,powered=false,waterlogged=false]`)
  }
  for (const depth of [6, 7]) {
    put(18, 3, depth, 'polished_blackstone_button[face=wall,facing=west,powered=false]')
    put(22, 3, depth, 'polished_blackstone_button[face=wall,facing=east,powered=false]')
  }
  for (const width of [8, 16]) {
    for (const depth of [3, 10, 18, 22]) put(width, 2, depth, 'glowstone')
  }
  return { structure, metadata: { name: 'zone-m04-lava-seam-mine', id: 'zone-M04', description: 'Sealed lava seam mine, dry three-wide bridge, four floor-to-roof timber supports, full roof.' } }
}
