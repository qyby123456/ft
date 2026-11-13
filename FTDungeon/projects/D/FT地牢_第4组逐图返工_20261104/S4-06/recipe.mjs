export default function build(api) {
  const structure = api.createStructure([25, 15, 25])
  const put = (east, up, south, block) => api.setBlock(structure, [east, up, south], `minecraft:${block}`)
  const fill = (start, end, block) => api.fill(structure, start, end, `minecraft:${block}`)
  const noise = (east, up, south) => Math.abs(Math.sin(east * 173.3 + up * 43.7 + south * 97.1) * 36791) % 1
  const stone = (start, end) => {
    for (let east = start[0]; east <= end[0]; east++) {
      for (let up = start[1]; up <= end[1]; up++) {
        for (let south = start[2]; south <= end[2]; south++) {
          const value = noise(east, up, south)
          put(east, up, south, value < 0.67 ? 'mossy_stone_bricks' : value < 0.84 ? 'cracked_stone_bricks' : 'stone_bricks')
        }
      }
    }
  }
  fill([0, 0, 0], [24, 0, 24], 'stone')
  fill([0, 1, 0], [24, 1, 24], 'packed_mud')
  stone([0, 2, 0], [24, 2, 24])
  fill([2, 3, 1], [23, 3, 22], 'water[level=0]')
  stone([0, 3, 0], [1, 4, 24])
  stone([0, 3, 23], [24, 4, 24])
  stone([2, 3, 0], [9, 10, 0])
  stone([24, 3, 15], [24, 10, 22])
  fill([4, 4, 0], [6, 8, 0], 'air')
  fill([24, 4, 18], [24, 8, 20], 'air')
  stone([4, 2, 0], [6, 3, 2])
  stone([22, 2, 18], [24, 3, 20])
  stone([10, 3, 0], [24, 11, 14])
  fill([11, 12, 0], [24, 12, 13], 'dirt')
  fill([10, 12, 0], [10, 12, 14], 'moss_block')
  fill([10, 12, 14], [24, 12, 14], 'moss_block')
  for (let east = 11; east <= 24; east++) {
    for (let south = 0; south <= 13; south++) {
      const value = noise(east, 13, south)
      put(east, 13, south, value < 0.66 ? 'moss_block' : value < 0.86 ? 'mossy_cobblestone' : 'rooted_dirt')
      if (value < 0.16) put(east, 14, south, 'mossy_cobblestone')
    }
  }
  for (let south = 3; south <= 19; south++) {
    for (const east of [5, 6]) put(east, 3, south, 'stone_brick_slab[type=top,waterlogged=true]')
  }
  for (let east = 5; east <= 21; east++) {
    for (const south of [18, 19]) put(east, 3, south, 'stone_brick_slab[type=top,waterlogged=true]')
  }
  for (const south of [3, 9]) {
    fill([9, 3, south], [9, 10, south], 'dark_oak_log[axis=y]')
    stone([8, 3, south], [8, 5, south])
  }
  for (const east of [16, 22]) {
    fill([east, 3, 15], [east, 10, 15], 'dark_oak_log[axis=y]')
    stone([east, 3, 16], [east, 5, 16])
  }
  fill([9, 11, 0], [9, 11, 15], 'dark_oak_log[axis=z]')
  fill([9, 11, 15], [24, 11, 15], 'dark_oak_log[axis=x]')
  fill([10, 3, 15], [10, 10, 15], 'dark_oak_log[axis=y]')
  stone([9, 3, 15], [9, 6, 15])
  put(9, 7, 15, 'verdant_froglight[axis=y]')
  put(9, 8, 15, 'dark_oak_slab[type=bottom,waterlogged=false]')
  for (const point of [[8, 7, 3], [22, 7, 16]]) {
    stone([point[0], 3, point[2]], [point[0], 6, point[2]])
    put(...point, 'verdant_froglight[axis=y]')
    put(point[0], 8, point[2], 'dark_oak_slab[type=bottom,waterlogged=false]')
  }
  for (const south of [1, 5, 7, 11, 13]) {
    for (let up = 5; up <= 10; up++) put(9, up, south, 'vine[east=true]')
  }
  for (const east of [12, 14, 18, 20, 24]) {
    for (let up = 5; up <= 10; up++) put(east, up, 15, 'vine[north=true]')
  }
  for (const point of [[9, 10, 6], [9, 10, 12], [13, 10, 15], [19, 10, 15]]) {
    fill([point[0], 8, point[2]], point, 'cave_vines_plant[berries=false]')
    put(point[0], 7, point[2], 'cave_vines[age=25,berries=false]')
  }
  put(8, 3, 16, 'moss_block')
  put(8, 4, 16, 'rooted_dirt')
  put(8, 5, 16, 'fern')
  for (const south of [4, 13, 21]) {
    const top = south === 4 ? 7 : 5
    fill([1, 3, south], [1, top, south], 'dark_oak_log[axis=y]')
    fill([0, top + 1, south], [2, top + 1, south], 'oak_leaves[persistent=true,distance=1]')
    put(2, 5, south, 'vine[west=true]')
    put(2, 3, south, 'mangrove_roots[waterlogged=true]')
    put(2, 4, south, 'dark_oak_log[axis=x]')
  }
  for (const east of [4, 13, 21]) {
    const top = east === 21 ? 6 : 5
    fill([east, 3, 23], [east, top, 23], 'dark_oak_log[axis=y]')
    fill([east, top + 1, 22], [east, top + 1, 24], 'oak_leaves[persistent=true,distance=1]')
    put(east, 5, 22, 'vine[south=true]')
    put(east, 3, 22, 'mangrove_roots[waterlogged=true]')
    put(east, 4, 22, 'dark_oak_log[axis=z]')
  }
  for (let east = 2; east <= 23; east++) {
    for (let south = 2; south <= 22; south++) {
      if (structure.getBlock([east, 3, south])?.state.getName().toString() === 'minecraft:water' && !structure.getBlock([east, 4, south]) && noise(east, 4, south) < 0.09) put(east, 4, south, 'lily_pad')
    }
  }
  return { structure, metadata: { name: 's4-06-mosslit-wetland-corner' } }
}
