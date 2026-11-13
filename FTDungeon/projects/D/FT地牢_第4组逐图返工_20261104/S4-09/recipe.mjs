export default function build(api) {
  const structure = api.createStructure([31, 22, 31])
  const put = (east, up, south, block) => api.setBlock(structure, [east, up, south], `minecraft:${block}`)
  const fill = (start, end, block) => api.fill(structure, start, end, `minecraft:${block}`)
  const noise = (east, up, south) => Math.abs(Math.sin(east * 113.7 + up * 61.9 + south * 179.3) * 33971) % 1
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
  fill([0, 0, 0], [30, 0, 30], 'stone')
  fill([0, 1, 0], [30, 1, 30], 'packed_mud')
  stone([0, 2, 0], [30, 3, 30])
  fill([3, 3, 18], [27, 3, 27], 'water[level=0]')
  stone([0, 4, 0], [30, 5, 16])
  stone([0, 4, 0], [1, 12, 30])
  stone([29, 4, 0], [30, 12, 30])
  stone([0, 4, 29], [30, 6, 30])
  stone([0, 6, 0], [30, 18, 2])
  for (let east = 0; east <= 30; east++) {
    const top = 19 + Math.max(0, 2 - Math.floor(Math.abs(east - 15) / 4))
    stone([east, 19, 0], [east, top, 2])
  }
  for (const east of [0, 1, 29, 30]) {
    for (let south = 3; south <= 28; south++) {
      const top = Math.max(9, 19 - Math.floor(south / 3))
      stone([east, 6, south], [east, top, south])
    }
  }
  for (const east of [3, 10, 20, 27]) fill([east, 6, 3], [east, 18, 3], 'dark_oak_log[axis=y]')
  fill([3, 19, 3], [27, 19, 3], 'dark_oak_log[axis=x]')
  for (const east of [2, 28]) {
    for (const south of [9, 16, 25]) fill([east, 4, south], [east, 13, south], 'dark_oak_log[axis=y]')
    fill([east, 14, 4], [east, 14, 26], 'dark_oak_log[axis=z]')
  }
  for (const east of [0, 1, 2, 28, 29, 30]) {
    for (let south = 10; south <= 14; south++) {
      const top = Math.abs(south - 12) === 2 ? 10 : 12
      fill([east, 6, south], [east, top, south], 'air')
    }
  }
  stone([11, 6, 4], [19, 6, 12])
  stone([12, 7, 5], [18, 8, 9])
  for (const row of [[12, 6], [11, 7], [10, 8]]) {
    if (row[1] > 6) stone([13, 6, row[0]], [17, row[1] - 1, row[0]])
    for (let east = 13; east <= 17; east++) put(east, row[1], row[0], 'stone_brick_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]')
  }
  api.setBlock(structure, [15, 9, 7], 'minecraft:chest', { facing: 'south', type: 'single', waterlogged: 'false' }, api.createBlockNbt({ id: 'minecraft:chest', Items: [] }))
  put(15, 9, 5, 'ochre_froglight[axis=y]')
  for (const east of [9, 21]) {
    stone([east - 1, 6, 6], [east + 1, 7, 8])
    put(east, 8, 7, 'chiseled_stone_bricks')
    fill([east, 9, 7], [east, 11, 7], 'mossy_cobblestone_wall[up=true,north=none,east=none,south=none,west=none,waterlogged=false]')
    put(east, 10, 7, 'mossy_cobblestone_wall[up=true,north=none,east=low,south=none,west=low,waterlogged=false]')
    put(east - 1, 10, 7, 'mossy_stone_bricks')
    put(east + 1, 10, 7, 'mossy_stone_bricks')
    put(east, 12, 7, 'chiseled_stone_bricks')
    put(east, 12, 8, 'stone_button[face=wall,facing=south,powered=false]')
  }
  for (const east of [11, 19]) api.setBlock(structure, [east, 16, 3], 'minecraft:brown_wall_banner', { facing: 'south' }, api.createBlockNbt({ id: 'minecraft:banner', Patterns: [{ Pattern: 'bo', Color: 4 }, { Pattern: 'mr', Color: 4 }, { Pattern: 'mc', Color: 12 }] }))
  for (const east of [4, 8]) fill([east, 6, 4], [east, 14, 4], 'dark_oak_log[axis=y]')
  for (const up of [7, 10, 13]) {
    fill([4, up, 4], [8, up, 4], 'dark_oak_planks')
    for (const east of [5, 7]) put(east, up + 1, 4, 'flower_pot')
  }
  for (const point of [[5, 6, 7], [7, 6, 6], [24, 6, 8], [26, 6, 8]]) api.setBlock(structure, point, 'minecraft:barrel', { facing: 'up', open: 'false' }, api.createBlockNbt({ id: 'minecraft:barrel', Items: [] }))
  fill([23, 6, 5], [26, 7, 6], 'dark_oak_planks')
  put(24, 8, 5, 'flower_pot')
  put(26, 8, 5, 'flower_pot')
  for (const east of [4, 26]) stone([east, 3, 17], [east + 1, 3, 28])
  for (let east = 3; east <= 27; east++) {
    if (east >= 13 && east <= 17) continue
    put(east, 3, 17, 'stone_brick_slab[type=top,waterlogged=true]')
  }
  for (let south = 17; south <= 30; south++) {
    for (let east = 13; east <= 17; east++) put(east, 3, south, 'dark_oak_planks')
  }
  for (const row of [[18, 4], [17, 5]]) {
    stone([13, 3, row[0]], [17, row[1] - 1, row[0]])
    for (let east = 13; east <= 17; east++) put(east, row[1], row[0], 'stone_brick_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]')
  }
  fill([13, 4, 29], [17, 8, 30], 'air')
  for (const east of [12, 18]) {
    for (const south of [21, 27]) fill([east, 3, south], [east, 5, south], 'dark_oak_log[axis=y]')
  }
  for (const point of [[12, 8, 3], [22, 8, 3], [7, 16, 3], [23, 16, 3], [3, 10, 9], [27, 10, 9], [10, 7, 16], [20, 7, 16]]) {
    if (point[2] === 3) put(point[0], point[1], 2, 'dark_oak_log[axis=x]')
    if (point[0] === 3 || point[0] === 27) {
      put(point[0] === 3 ? 2 : 28, point[1], point[2], 'dark_oak_log[axis=z]')
    }
    if (point[2] === 16) stone([point[0], 4, 16], [point[0], 6, 16])
    put(...point, 'ochre_froglight[axis=y]')
    put(point[0], point[1] + 1, point[2], 'dark_oak_slab[type=bottom,waterlogged=false]')
  }
  for (const east of [5, 9, 13, 17, 21, 25]) {
    for (let up = 12; up <= 18; up++) if (!structure.getBlock([east, up, 3])) put(east, up, 3, 'vine[north=true]')
  }
  for (const south of [6, 18, 23]) {
    for (const east of [2, 28]) {
      for (let up = 6; up <= 12; up++) if (!structure.getBlock([east, up, south])) put(east, up, south, east === 2 ? 'vine[west=true]' : 'vine[east=true]')
    }
  }
  for (const east of [6, 24]) {
    put(east, 18, 3, 'dark_oak_log[axis=x]')
    fill([east, 15, 3], [east, 17, 3], 'cave_vines_plant[berries=false]')
    put(east, 14, 3, 'cave_vines[age=25,berries=false]')
  }
  for (const south of [19, 25]) {
    for (const east of [2, 28]) {
      put(east, 13, south, 'oak_leaves[persistent=true,distance=1]')
      put(east, 12, south, 'oak_leaves[persistent=true,distance=1]')
    }
  }
  for (let east = 3; east <= 27; east++) {
    for (let south = 18; south <= 27; south++) {
      if (structure.getBlock([east, 3, south])?.state.getName().toString() === 'minecraft:water' && !structure.getBlock([east, 4, south]) && noise(east, 4, south) < 0.10) put(east, 4, south, 'lily_pad')
    }
  }
  return { structure, metadata: { name: 's4-09-twin-warden-treasure-sanctum' } }
}
