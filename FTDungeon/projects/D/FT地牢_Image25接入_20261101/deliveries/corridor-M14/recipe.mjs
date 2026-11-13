export default function build(api) {
  const structure = api.createStructure([15, 12, 41])
  const blocks = new Map()
  const key = position => position.join(',')
  const put = (position, name) => blocks.set(key(position), { position, name })
  const box = (start, end, name) => {
    for (let blockX = start[0]; blockX <= end[0]; blockX++) {
      for (let blockY = start[1]; blockY <= end[1]; blockY++) {
        for (let blockZ = start[2]; blockZ <= end[2]; blockZ++) put([blockX, blockY, blockZ], name)
      }
    }
  }
  const noise = (blockX, blockY, blockZ) => ((blockX * 7349 + blockY * 7919 + blockZ * 1543 + blockZ * blockX * 13) >>> 0) % 100
  const rocks = ['stone', 'cobblestone', 'mossy_cobblestone', 'andesite', 'coarse_dirt', 'rooted_dirt', 'brown_mushroom_block']
  box([0, 0, 0], [14, 11, 40], 'air')
  for (let blockX = 0; blockX < 15; blockX++) {
    for (let blockZ = 0; blockZ < 41; blockZ++) {
      const wear = noise(blockX, 1, blockZ)
      put([blockX, 0, blockZ], 'deepslate_bricks')
      put([blockX, 1, blockZ], wear < 38 ? 'stone_bricks' : wear < 62 ? 'cracked_stone_bricks' : wear < 80 ? 'mossy_stone_bricks' : 'cobblestone')
      put([blockX, 10, blockZ], rocks[noise(blockX, 10, blockZ) % rocks.length])
      put([blockX, 11, blockZ], 'deepslate_tiles')
    }
  }
  for (let blockZ = 0; blockZ < 41; blockZ++) {
    for (let blockY = 2; blockY <= 10; blockY++) {
      for (const blockX of [0, 14]) put([blockX, blockY, blockZ], rocks[noise(blockX, blockY, blockZ) % rocks.length])
      for (const blockX of [1, 13]) {
        if (noise(blockX, blockY, blockZ) < 57 || blockY >= 8) put([blockX, blockY, blockZ], rocks[noise(blockX, blockY, blockZ) % rocks.length])
      }
    }
    if (blockZ > 2 && blockZ < 38) {
      for (const blockX of [1, 2, 12, 13]) put([blockX, 2, blockZ], (blockZ + blockX) % 5 ? 'podzol' : 'mossy_cobblestone')
      for (const blockX of [3, 11]) {
        if (blockZ % 7 < 4) put([blockX, 2, blockZ], 'mossy_stone_bricks')
      }
    }
    for (const blockX of [4, 10]) {
      if (blockZ > 2 && blockZ < 38 && blockZ % 7 >= 3 && blockZ % 7 <= 5) {
        put([blockX, 0, blockZ], 'shroomlight')
        put([blockX, 1, blockZ], 'orange_stained_glass')
      }
    }
  }
  for (const blockZ of [0, 40]) {
    box([0, 2, blockZ], [14, 10, blockZ], 'cracked_stone_bricks')
    box([5, 2, blockZ], [9, 8, blockZ], 'stone_bricks')
    box([6, 2, blockZ], [8, 6, blockZ], 'air')
    for (const blockX of [5, 9]) box([blockX, 2, blockZ], [blockX, 6, blockZ], 'chiseled_stone_bricks')
    for (const blockX of [4, 10]) box([blockX, 2, blockZ], [blockX, 7, blockZ], 'mossy_stone_bricks')
    box([6, 7, blockZ], [8, 7, blockZ], 'cracked_stone_bricks')
  }
  for (const blockZ of [5, 15, 25, 35]) {
    box([1, 10, blockZ], [13, 10, blockZ], 'polished_basalt[axis=x]')
    for (const blockX of [1, 13]) box([blockX, 2, blockZ], [blockX, 9, blockZ], 'polished_basalt[axis=y]')
    for (const centerX of [4, 10]) {
      box([centerX - 1, 6, blockZ - 1], [centerX + 1, 6, blockZ + 1], 'polished_blackstone_slab[type=top,waterlogged=false]')
      put([centerX, 6, blockZ], 'polished_andesite')
      for (let offsetX = -1; offsetX <= 1; offsetX++) {
        for (let offsetZ = -1; offsetZ <= 1; offsetZ++) {
          if (offsetX || offsetZ) put([centerX + offsetX, 7, blockZ + offsetZ], 'iron_bars')
          put([centerX + offsetX, 8, blockZ + offsetZ], 'iron_trapdoor[facing=north,half=top,open=false,powered=false,waterlogged=false]')
        }
      }
      put([centerX, 7, blockZ], 'lantern[hanging=false,waterlogged=false]')
      put([centerX, 9, blockZ], 'chain[axis=y,waterlogged=false]')
    }
  }
  const giantMushrooms = [[2, 8], [12, 10], [2, 20], [12, 22], [2, 31], [12, 32]]
  for (const [centerX, centerZ] of giantMushrooms) {
    put([centerX, 2, centerZ], 'podzol')
    box([centerX, 3, centerZ], [centerX, 5, centerZ], 'mushroom_stem')
    box([centerX - 1, 6, centerZ - 1], [centerX + 1, 6, centerZ + 1], 'red_mushroom_block')
    put([centerX, 7, centerZ], 'red_mushroom_block')
  }
  for (const [centerX, centerZ] of [[2, 13], [12, 7], [2, 25], [12, 19], [2, 36], [12, 30]]) {
    put([centerX, 2, centerZ], 'warped_planks')
    put([centerX, 3, centerZ], 'warped_stem[axis=y]')
    box([centerX - 1, 4, centerZ], [centerX + 1, 4, centerZ], 'warped_wart_block')
    put([centerX, 4, centerZ], 'sea_lantern')
    put([centerX, 5, centerZ], 'warped_wart_block')
  }
  for (const blockZ of [4, 17, 28]) {
    for (const left of [true, false]) {
      const backX = left ? 1 : 13
      const faceX = left ? 2 : 12
      box([backX, 3, blockZ], [backX, 5, blockZ + 1], 'shroomlight')
      box([faceX, 3, blockZ], [faceX, 5, blockZ + 1], 'orange_stained_glass')
      box([faceX, 2, blockZ], [faceX, 2, blockZ + 1], 'mossy_stone_bricks')
    }
  }
  for (const blockZ of [4, 11, 18, 25, 32, 37]) {
    for (const blockX of [3, 11]) {
      put([blockX, 2, blockZ], 'podzol')
      put([blockX, 3, blockZ], blockZ % 2 ? 'potted_red_mushroom' : 'warped_fungus')
    }
  }
  for (const blockZ of [3, 10, 17, 24, 31, 38]) {
    for (const blockX of [2, 12]) put([blockX, 9, blockZ], 'hanging_roots')
    put([blockZ % 2 ? 3 : 11, 9, blockZ], 'spore_blossom')
    for (const blockX of [1, 13]) put([blockX, 7, blockZ], 'cobweb')
  }
  for (const blockZ of [6, 16, 26, 36]) {
    put([1, 5, blockZ], 'glow_lichen[west=true,east=false,north=false,south=false,up=false,down=false,waterlogged=false]')
    put([13, 5, blockZ], 'glow_lichen[east=true,west=false,north=false,south=false,up=false,down=false,waterlogged=false]')
  }
  const directions = { west: [-1, 0, 0], east: [1, 0, 0], down: [0, -1, 0], up: [0, 1, 0], north: [0, 0, -1], south: [0, 0, 1] }
  for (const block of blocks.values()) {
    if (['mushroom_stem', 'red_mushroom_block', 'brown_mushroom_block'].includes(block.name)) {
      const properties = Object.fromEntries(Object.entries(directions).map(([direction, offset]) => {
        const neighbor = block.position.map((coordinate, axis) => coordinate + offset[axis])
        const visible = blocks.get(key(neighbor))?.name !== block.name && !(block.name === 'red_mushroom_block' && direction === 'down')
        return [direction, String(visible)]
      }))
      api.setBlock(structure, block.position, 'minecraft:' + block.name, properties)
    } else if (block.name === 'podzol') {
      api.setBlock(structure, block.position, 'minecraft:podzol', { snowy: 'false' })
    } else {
      api.setBlock(structure, block.position, 'minecraft:' + block.name)
    }
  }
  return { structure, metadata: { name: 'room', theme: 'M14 fungal cave aged', giantMushrooms } }
}
