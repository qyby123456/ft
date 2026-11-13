export default function build(api) {
  const structure = api.createStructure([21, 17, 21])
  const put = (east, up, south, block) => api.setBlock(structure, [east, up, south], `minecraft:${block}`)
  const fill = (start, end, block) => api.fill(structure, start, end, `minecraft:${block}`)
  const noise = (east, up, south) => Math.abs(Math.sin(east * 43.7 + up * 191.3 + south * 72.9) * 18179.2) % 1
  const wall = (east, up, south) => {
    const value = noise(east, up, south)
    return value < 0.22 ? 'mossy_stone_bricks' : value < 0.35 ? 'rooted_dirt' : 'mud_bricks'
  }
  const stone = (start, end) => {
    for (let east = start[0]; east <= end[0]; east++) {
      for (let up = start[1]; up <= end[1]; up++) {
        for (let south = start[2]; south <= end[2]; south++) {
          const value = noise(east, up, south)
          put(east, up, south, value < 0.64 ? 'mossy_stone_bricks' : value < 0.84 ? 'cracked_stone_bricks' : 'mossy_cobblestone')
        }
      }
    }
  }
  fill([0, 0, 0], [20, 0, 20], 'stone')
  fill([0, 1, 0], [20, 1, 20], 'mud')
  stone([0, 2, 0], [20, 3, 20])
  fill([3, 2, 3], [17, 3, 17], 'water[level=0]')
  for (let east = 0; east <= 20; east++) {
    for (let depth = 0; depth < 2; depth++) {
      const height = east < 3 ? 11 : east > 17 ? 12 : 14
      for (let up = 4; up <= height; up++) put(east, up, depth, wall(east, up, depth))
      put(east, height + 1, depth, east % 4 < 2 ? 'dark_oak_log[axis=x]' : 'mossy_stone_bricks')
      stone([east, 4, 20 - depth], [east, 5 + (east % 7 < 2 ? 2 : 0), 20 - depth])
    }
  }
  for (let south = 2; south <= 18; south++) {
    for (let depth = 0; depth < 2; depth++) {
      const height = south < 13 ? 14 : 11
      for (let up = 4; up <= height; up++) put(depth, up, south, wall(depth, up, south))
      put(depth, height + 1, south, south % 5 < 3 ? 'dark_oak_log[axis=z]' : 'mossy_stone_bricks')
      stone([20 - depth, 4, south], [20 - depth, 5 + (south % 6 < 2 ? 2 : 0), south])
    }
  }
  fill([2, 13, 2], [17, 13, 2], 'dark_oak_log[axis=x]')
  fill([2, 13, 2], [2, 13, 13], 'dark_oak_log[axis=z]')
  for (const [east, south, top] of [[2, 2, 14], [10, 2, 13], [18, 2, 12], [2, 13, 13], [2, 18, 10], [18, 18, 7]]) fill([east, 4, south], [east, top, south], 'dark_oak_log[axis=y]')
  stone([4, 4, 0], [4, 9, 2])
  stone([8, 4, 0], [8, 9, 2])
  fill([5, 4, 0], [7, 8, 2], 'air')
  stone([5, 9, 0], [7, 10, 2])
  stone([18, 4, 12], [20, 8, 12])
  stone([18, 4, 16], [20, 8, 16])
  fill([18, 4, 13], [20, 7, 15], 'air')
  stone([18, 8, 13], [20, 9, 15])
  for (const [east, south] of [[3, 6], [16, 4]]) {
    stone([east, 2, south], [east, 5, south])
    put(east, 6, south, 'ochre_froglight[axis=y]')
  }
  for (const [east, south] of [[10, 3]]) {
    put(east, 13, south, 'dark_oak_log[axis=z]')
    for (let up = 11; up <= 12; up++) put(east, up, south, 'chain[axis=y]')
    put(east, 10, south, 'ochre_froglight[axis=y]')
  }
  for (const south of [7, 11]) {
    stone([0, 5, south], [2, 9, south])
  }
  fill([1, 6, 8], [1, 8, 10], 'air')
  fill([1, 5, 8], [2, 5, 10], 'dark_oak_slab[type=top]')
  put(1, 6, 8, 'potted_fern')
  put(1, 6, 10, 'flower_pot')
  fill([12, 6, 1], [14, 8, 1], 'air')
  fill([12, 5, 1], [14, 5, 2], 'dark_oak_slab[type=top]')
  put(13, 6, 1, 'flower_pot')
  for (const south of [5, 13]) {
    fill([3, 2, south], [4, 3, south], 'mangrove_roots[waterlogged=true]')
    fill([3, 4, south], [3, 5, south], 'dark_oak_log[axis=y]')
  }
  fill([16, 2, 6], [17, 3, 7], 'mangrove_roots[waterlogged=true]')
  fill([17, 4, 6], [17, 6, 6], 'dark_oak_log[axis=y]')
  fill([17, 4, 7], [17, 5, 7], 'dark_oak_log[axis=z]')
  put(17, 4, 8, 'dark_oak_log[axis=z]')
  fill([18, 3, 8], [18, 6, 9], 'dark_oak_log[axis=y]')
  put(18, 4, 10, 'dark_oak_log[axis=z]')
  for (const east of [3, 4, 9, 11, 15, 17]) {
    for (let up = 6 + east % 3; up <= 12; up++) put(east, up, 2, 'vine[north=true]')
  }
  for (const south of [3, 5, 12, 15, 17]) {
    const top = south > 13 ? 10 : 12
    for (let up = 6; up <= top; up++) put(2, up, south, 'vine[west=true]')
  }
  const footpath = (start, end) => {
    fill([start[0], 2, start[1]], [end[0], 2, end[1]], 'water[level=0]')
    fill([start[0], 3, start[1]], [end[0], 3, end[1]], 'stone_brick_slab[type=top,waterlogged=true]')
  }
  footpath([5, 3], [15, 3])
  footpath([15, 3], [15, 17])
  footpath([5, 17], [17, 17])
  footpath([5, 8], [5, 17])
  footpath([3, 8], [5, 10])
  footpath([13, 11], [15, 11])
  footpath([15, 14], [17, 14])
  stone([5, 2, 0], [7, 3, 3])
  stone([17, 2, 13], [20, 3, 15])
  stone([9, 2, 9], [13, 4, 13])
  for (let south = 10; south <= 12; south++) put(14, 4, south, 'stone_brick_stairs[facing=west,half=bottom,shape=straight]')
  api.setBlock(structure, [11, 5, 11], 'minecraft:chest', { facing: 'south', type: 'single', waterlogged: 'false' }, api.createBlockNbt({ id: 'minecraft:chest' }))
  for (let east = 4; east <= 17; east++) {
    for (let south = 4; south <= 17; south++) {
      const block = structure.getBlock([east, 3, south])
      if (block?.state.getName().toString() === 'minecraft:water' && noise(east, 4, south) < 0.06) put(east, 4, south, 'lily_pad')
    }
  }
  for (let east = 4; east <= 7; east++) put(east, 6, 19, 'dark_oak_fence')
  for (let east = 13; east <= 16; east++) put(east, 6, 19, 'dark_oak_fence')
  return { structure, metadata: { name: 's4-03-rootbound-cache' } }
}
