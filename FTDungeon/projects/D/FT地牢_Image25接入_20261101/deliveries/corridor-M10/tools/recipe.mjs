export default function build(api) {
  const structure = api.createStructure([15, 11, 40])
  const put = (position, block) => api.setBlock(structure, position, `minecraft:${block}`)
  const fill = (from, to, block) => api.fill(structure, from, to, `minecraft:${block}`)
  const noise = (posX, posY, posZ) => ((posX * 73856093 ^ posY * 19349663 ^ posZ * 83492791) >>> 0) % 100
  fill([0, 0, 0], [14, 10, 39], 'air')
  fill([0, 0, 0], [14, 0, 39], 'deepslate[axis=y]')
  for (let posX = 0; posX < 15; posX++) {
    for (let posZ = 0; posZ < 40; posZ++) {
      const tone = noise(posX, 1, posZ)
      put([posX, 1, posZ], tone < 12 && (posX < 6 || posX > 8) ? 'gravel' : tone < 30 ? 'cobbled_deepslate' : tone < 48 ? 'andesite' : 'stone')
      put([posX, 9, posZ], tone < 28 ? 'blackstone' : tone < 44 ? 'deepslate_tiles' : 'dark_oak_planks')
      put([posX, 10, posZ], 'deepslate[axis=y]')
    }
  }
  for (const side of [0, 1, 13, 14]) {
    for (let posY = 2; posY < 9; posY++) {
      for (let posZ = 0; posZ < 40; posZ++) {
        const tone = noise(side, posY, posZ)
        put([side, posY, posZ], tone < 14 ? 'coal_ore' : tone < 30 ? 'cobbled_deepslate' : tone < 43 ? 'tuff' : 'stone')
      }
    }
  }
  const frames = [1, 7, 13, 19, 25, 31, 38]
  for (const posZ of frames) {
    for (const posX of [2, 12]) {
      put([posX, 2, posZ], 'polished_deepslate')
      fill([posX, 3, posZ], [posX, 7, posZ], 'dark_oak_log[axis=y]')
      if (posZ === 13 || posZ === 31) put([posX, 4, posZ], 'blackstone')
    }
    fill([2, 8, posZ], [12, 8, posZ], 'dark_oak_log[axis=x]')
    put([3, 7, posZ], 'dark_oak_stairs[facing=west,half=top,shape=straight,waterlogged=false]')
    put([11, 7, posZ], 'dark_oak_stairs[facing=east,half=top,shape=straight,waterlogged=false]')
    for (const posX of posZ === 1 || posZ === 38 ? [4, 10] : [frames.indexOf(posZ) % 2 ? 4 : 10]) {
      fill([posX, 6, posZ], [posX, 7, posZ], 'chain[axis=y,waterlogged=false]')
      put([posX, 5, posZ], 'lantern[hanging=true,waterlogged=false]')
    }
  }
  for (const posX of [3, 11]) fill([posX, 8, 1], [posX, 8, 38], 'dark_oak_log[axis=z]')
  for (let posZ = 2; posZ <= 37; posZ++) {
    put([7, 2, posZ], 'rail[shape=north_south,waterlogged=false]')
    if (posZ % 3 === 0) for (const posX of [6, 8]) put([posX, 1, posZ], 'dark_oak_planks')
  }
  for (const [posX, posY, posZ, block] of [[1, 4, 4, 'gold_ore'], [1, 3, 15, 'iron_ore'], [1, 5, 23, 'copper_ore'], [1, 3, 34, 'gold_ore'], [13, 3, 5, 'iron_ore'], [13, 4, 17, 'diamond_ore'], [13, 5, 28, 'copper_ore'], [13, 3, 35, 'gold_ore']]) {
    for (const [offsetY, offsetZ] of [[0, 0], [1, 0], [0, 1], [-1, 1]]) put([posX, posY + offsetY, posZ + offsetZ], block)
  }
  for (const [centerX, centerZ] of [[10, 10], [4, 22], [10, 34]]) {
    for (let offsetX = -1; offsetX <= 1; offsetX++) {
      for (let offsetZ = -1; offsetZ <= 1; offsetZ++) {
        const posX = centerX + offsetX
        const posZ = centerZ + offsetZ
        put([posX, 4, posZ], 'iron_trapdoor[facing=north,half=top,open=false,powered=false,waterlogged=false]')
        put([posX, 7, posZ], 'iron_trapdoor[facing=north,half=bottom,open=false,powered=false,waterlogged=false]')
        if (offsetX !== 0 || offsetZ !== 0) fill([posX, 5, posZ], [posX, 6, posZ], 'iron_bars[waterlogged=false]')
      }
    }
    put([centerX, 7, centerZ], 'iron_block')
    put([centerX, 8, centerZ], 'chain[axis=y,waterlogged=false]')
    put([centerX, 6, centerZ], 'chain[axis=y,waterlogged=false]')
    put([centerX, 5, centerZ], 'lantern[hanging=true,waterlogged=false]')
  }
  for (const [posX, posZ] of [[2, 5], [12, 16], [2, 27], [12, 36]]) {
    put([posX, 1, posZ], 'shroomlight')
    put([posX, 2, posZ], 'orange_stained_glass')
    put([posX, 3, posZ], 'iron_trapdoor[facing=north,half=bottom,open=false,powered=false,waterlogged=false]')
    const direction = posX === 2 ? 1 : -1
    put([posX + direction, 2, posZ + 1], 'cobbled_deepslate')
    put([posX + direction, 2, posZ], 'polished_blackstone_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]')
    put([posX, 2, posZ - 1], 'coal_block')
  }
  for (const posZ of [0, 39]) {
    fill([2, 2, posZ], [12, 8, posZ], 'cobbled_deepslate')
    fill([5, 2, posZ], [9, 6, posZ], 'air')
    fill([6, 7, posZ], [8, 7, posZ], 'air')
    put([5, 7, posZ], 'polished_deepslate_stairs[facing=west,half=top,shape=straight,waterlogged=false]')
    put([9, 7, posZ], 'polished_deepslate_stairs[facing=east,half=top,shape=straight,waterlogged=false]')
    fill([4, 2, posZ], [4, 7, posZ], 'polished_deepslate')
    fill([10, 2, posZ], [10, 7, posZ], 'polished_deepslate')
  }
  return { structure, metadata: { name: 'room' } }
}
