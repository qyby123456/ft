export default function build(api) {
  const structure = api.createStructure([35, 27, 35])
  const put = (east, up, south, block) => api.setBlock(structure, [east, up, south], `minecraft:${block}`)
  const fill = (start, end, block) => api.fill(structure, start, end, `minecraft:${block}`)
  const noise = (east, up, south) => Math.abs(Math.sin(east * 131.1 + up * 79.3 + south * 199.7) * 43271) % 1
  const stone = (start, end) => {
    for (let east = start[0]; east <= end[0]; east++) {
      for (let up = start[1]; up <= end[1]; up++) {
        for (let south = start[2]; south <= end[2]; south++) {
          const value = noise(east, up, south)
          put(east, up, south, value < 0.65 ? 'mossy_stone_bricks' : value < 0.82 ? 'cracked_stone_bricks' : 'stone_bricks')
        }
      }
    }
  }
  fill([0, 0, 0], [34, 0, 34], 'stone')
  fill([0, 1, 0], [34, 2, 34], 'packed_mud')
  stone([0, 3, 0], [34, 4, 34])
  fill([3, 4, 3], [31, 4, 31], 'water[level=0]')
  stone([0, 5, 0], [1, 20, 34])
  stone([0, 5, 0], [34, 20, 1])
  stone([0, 5, 33], [34, 7, 34])
  stone([33, 5, 0], [34, 7, 34])
  for (const south of [6, 14, 22, 30]) {
    fill([2, 4, south], [2, 20, south], 'dark_oak_log[axis=y]')
    stone([3, 3, south], [4, 6, south + 1])
    fill([5, 5, south], [5, 11, south], 'dark_oak_log[axis=y]')
    stone([4, 3, south], [6, 5, south + 1])
  }
  for (const east of [6, 14, 22, 30]) {
    fill([east, 4, 2], [east, 20, 2], 'dark_oak_log[axis=y]')
    stone([east, 3, 3], [east + 1, 6, 4])
    fill([east, 5, 5], [east, 11, 5], 'dark_oak_log[axis=y]')
    stone([east, 3, 4], [east + 1, 5, 6])
  }
  stone([2, 11, 2], [6, 12, 32])
  stone([2, 11, 2], [32, 12, 6])
  for (let south = 6; south <= 31; south++) put(6, 13, south, 'dark_oak_fence')
  for (let east = 6; east <= 31; east++) put(east, 13, 6, 'dark_oak_fence')
  fill([28, 13, 6], [30, 13, 6], 'air')
  fill([2, 20, 2], [2, 20, 33], 'dark_oak_log[axis=z]')
  fill([2, 20, 2], [33, 20, 2], 'dark_oak_log[axis=x]')
  for (let south = 0; south <= 34; south++) {
    if (south % 5 < 2) stone([0, 21, south], [1, 21, south])
  }
  for (let east = 0; east <= 34; east++) {
    if (east % 5 < 2) stone([east, 21, 0], [east, 21, 1])
  }
  for (let south = 7; south <= 13; south++) stone([28, 3, south], [30, 12, south])
  for (let south = 14; south <= 21; south++) {
    const up = 26 - south
    stone([28, 3, south], [30, up - 1, south])
    for (let east = 28; east <= 30; east++) put(east, up, south, 'stone_brick_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]')
  }
  for (let south = 7; south <= 21; south++) {
    const up = south < 14 ? 13 : 27 - south
    put(31, up - 1, south, 'mossy_stone_bricks')
    put(31, up, south, 'dark_oak_fence')
  }
  for (let east = 3; east <= 31; east++) {
    for (const south of [9, 10, 25, 26]) {
      if (east < 28 || south >= 25) put(east, 4, south, 'stone_brick_slab[type=top,waterlogged=true]')
    }
  }
  for (let south = 3; south <= 31; south++) {
    for (const east of [9, 10, 25, 26]) put(east, 4, south, 'stone_brick_slab[type=top,waterlogged=true]')
  }
  for (let south = 22; south <= 26; south++) {
    for (let east = 25; east <= 30; east++) put(east, 4, south, 'stone_brick_slab[type=top,waterlogged=true]')
  }
  stone([11, 3, 11], [23, 4, 23])
  for (let south = 23; south <= 25; south++) {
    for (const east of [20, 21]) put(east, 4, south, 'stone_brick_slab[type=top,waterlogged=true]')
  }
  stone([12, 5, 12], [22, 5, 22])
  fill([14, 6, 14], [20, 6, 20], 'rooted_dirt')
  for (let east = 14; east <= 20; east++) {
    for (let south = 14; south <= 20; south++) put(east, 7, south, noise(east, 7, south) < 0.55 ? 'moss_block' : 'rooted_dirt')
  }
  fill([16, 8, 16], [18, 21, 18], 'dark_oak_log[axis=y]')
  fill([15, 8, 17], [15, 15, 18], 'dark_oak_log[axis=y]')
  fill([19, 8, 16], [19, 17, 17], 'dark_oak_log[axis=y]')
  for (const root of [
    [[12, 5, 16], [15, 5, 16], 'x'], [[14, 6, 16], [16, 6, 16], 'x'], [[16, 7, 16], [16, 8, 16], 'y'],
    [[19, 5, 19], [23, 5, 19], 'x'], [[19, 6, 19], [21, 6, 19], 'x'], [[19, 7, 17], [19, 7, 19], 'z'],
    [[16, 5, 11], [16, 5, 14], 'z'], [[16, 6, 13], [16, 6, 16], 'z'],
    [[17, 5, 20], [17, 5, 23], 'z'], [[17, 6, 18], [17, 6, 21], 'z'], [[17, 7, 18], [17, 8, 18], 'y']
  ]) fill(root[0], root[1], `dark_oak_log[axis=${root[2]}]`)
  fill([13, 20, 16], [21, 20, 17], 'dark_oak_log[axis=x]')
  fill([12, 6, 16], [14, 6, 16], 'dark_oak_log[axis=x]')
  fill([14, 7, 16], [16, 7, 16], 'dark_oak_log[axis=x]')
  fill([15, 8, 16], [16, 8, 16], 'dark_oak_log[axis=x]')
  fill([19, 8, 17], [20, 8, 17], 'dark_oak_log[axis=x]')
  fill([20, 7, 17], [22, 7, 17], 'dark_oak_log[axis=x]')
  fill([22, 6, 17], [22, 6, 20], 'dark_oak_log[axis=z]')
  fill([21, 5, 20], [23, 5, 20], 'dark_oak_log[axis=x]')
  fill([17, 8, 19], [17, 8, 20], 'dark_oak_log[axis=z]')
  fill([17, 7, 20], [17, 7, 22], 'dark_oak_log[axis=z]')
  fill([17, 6, 22], [19, 6, 22], 'dark_oak_log[axis=x]')
  fill([16, 20, 13], [17, 20, 21], 'dark_oak_log[axis=z]')
  for (let up = 21; up <= 26; up++) {
    const radius = up <= 22 ? 6 : up <= 24 ? 4 : 2
    for (let east = 17 - radius; east <= 17 + radius; east++) {
      for (let south = 17 - radius; south <= 17 + radius; south++) {
        if (Math.abs(east - 17) + Math.abs(south - 17) > radius * 1.65) continue
        if (!structure.getBlock([east, up, south])) put(east, up, south, 'oak_leaves[persistent=true,distance=1]')
      }
    }
  }
  for (const point of [[11, 21, 16], [23, 21, 18], [16, 21, 11], [18, 21, 23]]) {
    put(...point, 'dark_oak_log[axis=y]')
    fill([point[0], 17, point[2]], [point[0], 20, point[2]], 'cave_vines_plant[berries=false]')
    put(point[0], 16, point[2], 'cave_vines[age=25,berries=false]')
  }
  for (const point of [[13, 6, 13], [21, 6, 15], [15, 6, 21]]) {
    put(...point, 'moss_block')
    put(point[0], 7, point[2], 'fern')
  }
  stone([6, 3, 27], [8, 6, 29])
  fill([6, 7, 25], [8, 7, 27], 'water[level=0]')
  stone([5, 4, 24], [5, 8, 28])
  stone([9, 5, 24], [9, 8, 28])
  stone([5, 6, 24], [9, 7, 24])
  fill([6, 6, 25], [8, 6, 26], 'stone_bricks')
  fill([6, 5, 25], [8, 5, 26], 'stone_bricks')
  fill([6, 4, 25], [8, 4, 26], 'stone_bricks')
  fill([6, 7, 28], [8, 7, 28], 'water[level=8]')
  fill([6, 4, 28], [8, 6, 28], 'water[level=8]')
  fill([6, 4, 29], [8, 4, 29], 'water[level=0]')
  fill([6, 5, 29], [8, 7, 29], 'air')
  for (const point of [[4, 8, 10], [10, 8, 4], [4, 16, 18], [20, 16, 4], [13, 6, 22], [28, 6, 30], [21, 6, 30]]) {
    put(point[0], point[1] + 1, point[2], 'dark_oak_log[axis=x]')
    put(...point, 'ochre_froglight[axis=y]')
    if (point[1] === 6) stone([point[0], 3, point[2]], [point[0], 5, point[2]])
  }
  fill([4, 9, 6], [4, 9, 10], 'dark_oak_log[axis=z]')
  fill([6, 9, 4], [10, 9, 4], 'dark_oak_log[axis=x]')
  fill([2, 17, 18], [4, 17, 18], 'dark_oak_log[axis=x]')
  fill([20, 17, 2], [20, 17, 4], 'dark_oak_log[axis=z]')
  for (const south of [4, 10, 18, 26, 32]) {
    for (let up = 6; up <= 19; up++) if (!structure.getBlock([2, up, south])) put(2, up, south, 'vine[west=true]')
  }
  for (const east of [4, 10, 18, 26, 32]) {
    for (let up = 6; up <= 19; up++) if (!structure.getBlock([east, up, 2])) put(east, up, 2, 'vine[north=true]')
  }
  for (const port of ['north', 'south', 'west', 'east']) {
    for (let offset = -1; offset <= 1; offset++) {
      for (let depth = 0; depth <= 3; depth++) {
        const east = port === 'west' ? depth : port === 'east' ? 34 - depth : 17 + offset
        const south = port === 'north' ? depth : port === 'south' ? 34 - depth : (port === 'east' ? 25 : 17) + offset
        stone([east, 3, south], [east, 4, south])
        fill([east, 5, south], [east, 9, south], 'air')
      }
    }
  }
  for (let south = 3; south <= 10; south++) {
    for (let east = 16; east <= 18; east++) put(east, 4, south, 'stone_brick_slab[type=top,waterlogged=true]')
  }
  for (let south = 25; south <= 31; south++) {
    for (let east = 16; east <= 18; east++) put(east, 4, south, 'stone_brick_slab[type=top,waterlogged=true]')
  }
  for (let east = 3; east <= 10; east++) {
    for (let south = 16; south <= 18; south++) put(east, 4, south, 'stone_brick_slab[type=top,waterlogged=true]')
  }
  for (let east = 25; east <= 31; east++) {
    for (let south = 16; south <= 18; south++) if (east < 28) put(east, 4, south, 'stone_brick_slab[type=top,waterlogged=true]')
  }
  for (let east = 3; east <= 31; east++) {
    for (let south = 3; south <= 31; south++) {
      if (structure.getBlock([east, 4, south])?.state.getName().toString() === 'minecraft:water' && !structure.getBlock([east, 5, south]) && noise(east, 5, south) < 0.08) put(east, 5, south, 'lily_pad')
    }
  }
  return { structure, metadata: { name: 's4-08-rooted-gallery-courtyard' } }
}
