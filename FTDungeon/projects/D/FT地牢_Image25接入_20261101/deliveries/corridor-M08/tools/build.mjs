export default function build(api) {
  const size = [17, 13, 41]
  const structure = api.createStructure(size)
  const put = (position, block) => api.setBlock(structure, position, `minecraft:${block}`)
  const fill = (start, end, block) => api.fill(structure, start, end, `minecraft:${block}`)
  const noise = (column, height, depth) => ((column * 7349 + height * 9151 + depth * 3191) ^ (depth * 143)) >>> 0
  const masonry = ['polished_blackstone_bricks', 'polished_blackstone_bricks', 'cracked_polished_blackstone_bricks', 'blackstone', 'polished_blackstone_bricks', 'black_concrete']
  fill([0, 0, 0], [16, 12, 40], 'air')
  fill([0, 0, 0], [16, 0, 40], 'polished_basalt[axis=y]')
  fill([0, 1, 0], [16, 2, 40], 'blackstone')
  for (let column = 0; column < 17; column += 1) {
    for (let depth = 0; depth < 41; depth += 1) {
      const score = noise(column, 2, depth) % 17
      const tile = score < 2 ? 'obsidian' : score < 6 ? 'cracked_polished_blackstone_bricks' : score < 9 ? 'polished_basalt[axis=y]' : 'polished_blackstone'
      put([column, 2, depth], tile)
      for (const height of [11, 12]) {
        put([column, height, depth], masonry[noise(column, height, depth) % masonry.length])
      }
    }
  }
  for (const column of [0, 1, 2, 14, 15, 16]) {
    for (let height = 3; height <= 10; height += 1) {
      for (let depth = 0; depth <= 40; depth += 1) {
        put([column, height, depth], masonry[noise(column, height, depth) % masonry.length])
      }
    }
  }
  for (const depth of [0, 1, 39, 40]) {
    for (let column = 3; column <= 13; column += 1) {
      for (let height = 3; height <= 10; height += 1) {
        if (column < 6 || column > 10 || height > (column === 6 || column === 10 ? 7 : 8)) {
          put([column, height, depth], 'polished_blackstone_bricks')
        }
      }
    }
    for (const column of [5, 11]) {
      fill([column, 3, depth], [column, 7, depth], 'polished_basalt[axis=y]')
    }
    fill([6, 9, depth], [10, 9, depth], 'chiseled_polished_blackstone')
  }
  for (const column of [4, 12]) {
    for (let depth = 3; depth <= 37; depth += 1) {
      put([column, 1, depth], 'lava[level=0]')
      put([column, 2, depth], 'orange_stained_glass')
    }
  }
  for (const depth of [2, 11, 20, 29, 38]) {
    for (const column of [3, 13]) {
      fill([column, 3, depth], [column, 9, depth], 'polished_basalt[axis=y]')
      put([column, 3, depth], 'chiseled_polished_blackstone')
      put([column, 4, depth], 'polished_blackstone_bricks')
      put([column, 9, depth], 'chiseled_polished_blackstone')
    }
    fill([3, 10, depth], [13, 10, depth], 'polished_basalt[axis=x]')
    for (const column of [4, 12]) put([column, 9, depth], 'polished_blackstone_bricks')
    for (const column of [5, 11]) put([column, 10, depth], 'chiseled_polished_blackstone')
  }
  for (const depth of [6, 15, 24, 33]) {
    for (const side of ['west', 'east']) {
      const hotColumn = side === 'west' ? 1 : 15
      const windowColumn = side === 'west' ? 2 : 14
      const grilleColumn = side === 'west' ? 3 : 13
      const anvilColumn = side === 'west' ? 4 : 12
      for (let height = 4; height <= 6; height += 1) {
        for (let bayDepth = depth - 1; bayDepth <= depth + 1; bayDepth += 1) {
          put([hotColumn, height, bayDepth], 'lava[level=0]')
          put([windowColumn, height, bayDepth], 'orange_stained_glass')
          put([grilleColumn, height, bayDepth], 'iron_bars[waterlogged=false]')
        }
      }
      for (const height of [3, 7]) {
        fill([windowColumn, height, depth - 2], [windowColumn, height, depth + 2], 'chiseled_polished_blackstone')
      }
      put([anvilColumn, 3, depth], `anvil[facing=${side === 'west' ? 'east' : 'west'}]`)
      put([grilleColumn, 3, depth - 2], 'polished_blackstone_bricks')
      put([grilleColumn, 3, depth + 2], 'polished_blackstone_bricks')
      put([windowColumn, 8, depth], 'gilded_blackstone')
      put([windowColumn, 9, depth], 'black_concrete')
    }
  }
  for (const centerDepth of [7, 16, 25, 34]) {
    fill([3, 10, centerDepth], [13, 10, centerDepth], 'waxed_exposed_cut_copper')
    for (const centerColumn of [4, 12]) {
      for (let column = centerColumn - 1; column <= centerColumn + 1; column += 1) {
        for (let depth = centerDepth - 1; depth <= centerDepth + 1; depth += 1) {
          put([column, 5, depth], 'iron_trapdoor[facing=north,half=bottom,open=false,powered=false,waterlogged=false]')
          put([column, 8, depth], 'iron_trapdoor[facing=north,half=top,open=false,powered=false,waterlogged=false]')
          if (column !== centerColumn || depth !== centerDepth) {
            for (const height of [6, 7]) put([column, height, depth], 'iron_bars[waterlogged=false]')
          } else {
            for (const height of [6, 7]) put([column, height, depth], 'air')
          }
        }
      }
      put([centerColumn, 8, centerDepth], 'iron_block')
      put([centerColumn, 9, centerDepth], 'chain[axis=y,waterlogged=false]')
    }
  }
  for (const depth of [10, 19, 28, 37]) {
    for (const column of [3, 13]) {
      put([column, 3, depth], 'polished_blackstone_bricks')
      put([column, 4, depth], 'lantern[hanging=false,waterlogged=false]')
    }
  }
  for (const depth of [0, 1, 2, 11, 20, 29, 38, 39, 40]) {
    fill([5, 2, depth], [11, 2, depth], 'polished_basalt[axis=x]')
    for (const column of [4, 12]) {
      if (depth < 3 || depth > 37) put([column, 2, depth], 'polished_blackstone')
    }
  }
  return { structure, metadata: { name: 'room', id: 'corridor-M08', theme: 'burnt-lava-forge', size, feetY: 3 } }
}
