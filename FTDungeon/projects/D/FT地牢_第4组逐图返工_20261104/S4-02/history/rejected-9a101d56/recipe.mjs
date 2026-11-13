export default function build(api) {
  const structure = api.createStructure([29, 18, 27])
  const put = (east, up, south, block) => api.setBlock(structure, [east, up, south], `minecraft:${block}`)
  const fill = (start, end, block) => api.fill(structure, start, end, `minecraft:${block}`)
  const noise = (east, up, south) => Math.abs(Math.sin(east * 91.3 + up * 17.7 + south * 231.2) * 21973.17) % 1
  const masonry = (east, up, south) => {
    const value = noise(east, up, south)
    return value < 0.55 ? 'mossy_stone_bricks' : value < 0.78 ? 'cracked_stone_bricks' : value < 0.93 ? 'stone_bricks' : 'mossy_cobblestone'
  }
  const stone = (start, end) => {
    for (let east = start[0]; east <= end[0]; east++) {
      for (let up = start[1]; up <= end[1]; up++) {
        for (let south = start[2]; south <= end[2]; south++) put(east, up, south, masonry(east, up, south))
      }
    }
  }
  fill([0, 0, 0], [28, 0, 26], 'stone')
  fill([0, 1, 0], [28, 1, 26], 'packed_mud')
  stone([0, 2, 0], [28, 3, 26])
  fill([3, 2, 3], [25, 3, 23], 'water[level=0]')
  for (let east = 3; east <= 25; east++) {
    for (let south = 3; south <= 23; south++) {
      put(east, 1, south, noise(east, 1, south) < 0.45 ? 'clay' : 'mud')
      if (!(east >= 10 && east <= 18 && south >= 11 && south <= 17) && noise(east, 4, south) < 0.13) put(east, 4, south, 'lily_pad')
    }
  }
  for (let east = 0; east <= 28; east++) {
    for (let south = 0; south <= 26; south++) {
      if (east < 4 || east > 24 || south < 4 || south > 22) {
        put(east, 3, south, masonry(east, 3, south))
        put(east, 4, south, 'air')
      }
    }
  }
  for (let along = 0; along <= 28; along++) {
    for (let depth = 0; depth < 2; depth++) {
      const height = along > 24 ? 13 : 14
      for (let up = 4; up <= height; up++) put(along, up, depth, up < 12 && up > 5 && along % 6 > 1 ? 'mud_bricks' : masonry(along, up, depth))
      if (along % 6 < 3) put(along, height + 1, depth, 'mossy_stone_bricks')
      for (let up = 4; up <= 5 + Math.floor(noise(along, 9, 26) * 3); up++) put(along, up, 26 - depth, masonry(along, up, 26 - depth))
    }
  }
  for (let south = 2; south <= 24; south++) {
    for (let depth = 0; depth < 2; depth++) {
      for (let up = 4; up <= 12 + Math.floor(south / 9); up++) put(depth, up, south, up > 5 && up < 11 && south % 5 < 3 ? 'mud_bricks' : masonry(depth, up, south))
      for (let up = 4; up <= 5 + Math.floor(noise(27, 4, south) * 3); up++) put(28 - depth, up, south, masonry(28 - depth, up, south))
    }
  }
  for (const south of [3, 11, 22]) {
    fill([2, 4, south], [2, 13, south], 'dark_oak_log[axis=y]')
    fill([0, 12, south], [2, 12, south], 'dark_oak_log[axis=x]')
  }
  for (const east of [3, 11, 25]) fill([east, 4, 2], [east, 14, 2], 'dark_oak_log[axis=y]')
  fill([2, 13, 2], [26, 13, 2], 'dark_oak_log[axis=x]')
  fill([2, 12, 3], [2, 12, 24], 'dark_oak_log[axis=z]')
  for (const [east, south] of [[27, 6], [27, 20], [5, 25], [23, 25]]) {
    fill([east, 4, south], [east, 7, south], 'dark_oak_log[axis=y]')
  }
  stone([15, 4, 0], [16, 11, 2])
  stone([22, 4, 0], [23, 11, 2])
  fill([17, 4, 0], [21, 10, 2], 'air')
  stone([17, 11, 0], [21, 12, 2])
  fill([16, 4, 1], [16, 9, 1], 'iron_bars')
  fill([22, 4, 1], [22, 9, 1], 'iron_bars')
  fill([11, 4, 24], [15, 9, 26], 'air')
  stone([9, 4, 24], [10, 8, 26])
  stone([16, 4, 24], [17, 7, 26])
  stone([2, 3, 5], [6, 3, 11])
  fill([2, 4, 5], [6, 4, 11], 'air')
  stone([2, 4, 5], [3, 8, 5])
  stone([2, 4, 11], [3, 8, 11])
  stone([2, 9, 5], [3, 9, 11])
  fill([2, 4, 6], [2, 7, 9], 'bookshelf')
  api.setBlock(structure, [3, 4, 10], 'minecraft:chest', { facing: 'east', type: 'single', waterlogged: 'false' }, api.createBlockNbt({ id: 'minecraft:chest' }))
  put(3, 4, 6, 'potted_fern')
  stone([22, 2, 3], [25, 3, 8])
  fill([22, 4, 3], [25, 4, 8], 'air')
  fill([23, 4, 4], [23, 4, 7], 'dark_oak_fence')
  fill([23, 5, 4], [24, 5, 7], 'dark_oak_slab[type=top]')
  put(23, 6, 4, 'potted_fern')
  put(23, 6, 7, 'flower_pot')
  api.setBlock(structure, [25, 4, 7], 'minecraft:chest', { facing: 'south', type: 'single', waterlogged: 'false' }, api.createBlockNbt({ id: 'minecraft:chest' }))
  const path = (start, end) => {
    stone([start[0], 2, start[1]], [end[0], 3, end[1]])
    fill([start[0], 4, start[1]], [end[0], 4, end[1]], 'air')
  }
  path([7, 18], [14, 19])
  path([7, 8], [8, 19])
  path([20, 8], [21, 15])
  path([12, 19], [14, 26])
  path([18, 0], [20, 9])
  path([5, 8], [8, 10])
  path([21, 8], [22, 9])
  stone([10, 2, 11], [18, 3, 17])
  stone([11, 4, 12], [17, 4, 16])
  stone([11, 5, 12], [17, 5, 16])
  put(14, 5, 14, 'verdant_froglight[axis=y]')
  for (const [east, south, facing] of [[10, 13, 'east'], [10, 14, 'east'], [10, 15, 'east'], [18, 13, 'west'], [18, 14, 'west'], [18, 15, 'west']]) {
    put(east, 4, south, `stone_brick_stairs[facing=${facing},half=bottom,shape=straight]`)
    put(east === 10 ? 11 : 17, 5, south, `stone_brick_stairs[facing=${facing},half=bottom,shape=straight]`)
  }
  path([8, 13], [9, 15])
  path([19, 13], [20, 15])
  for (const [east, south, top] of [[4, 8, 12], [8, 3, 13], [25, 3, 13]]) {
    put(east, top, south, 'dark_oak_log[axis=y]')
    for (let up = 10; up < top; up++) put(east, up, south, 'chain[axis=y]')
    put(east, 9, south, 'ochre_froglight[axis=y]')
    if (south === 8) fill([2, top, south], [east, top, south], 'dark_oak_log[axis=x]')
    else fill([east, top, 2], [east, top, south], 'dark_oak_log[axis=z]')
  }
  for (const east of [5, 9, 12, 24, 26]) {
    for (let up = 8 + east % 3; up <= 12; up++) put(east, up, 2, 'vine[north=true]')
  }
  for (const south of [4, 12, 15, 19]) {
    for (let up = 7 + south % 3; up <= 11; up++) put(2, up, south, 'vine[west=true]')
  }
  return { structure, metadata: { name: 's4-02-drowned-study' } }
}
