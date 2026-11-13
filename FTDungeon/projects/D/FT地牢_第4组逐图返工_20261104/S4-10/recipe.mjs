export default function build(api) {
  const structure = api.createStructure([73, 22, 73])
  const put = (east, up, south, block) => api.setBlock(structure, [east, up, south], `minecraft:${block}`)
  const fill = (start, end, block) => api.fill(structure, start, end, `minecraft:${block}`)
  const noise = (east, up, south) => Math.abs(Math.sin(east * 137.7 + up * 73.1 + south * 197.3) * 43758.5) % 1
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
  const rooms = [
    { id: 'library', bounds: [3, 7, 23, 25], height: 10 },
    { id: 'north-hall', bounds: [26, 1, 50, 23], height: 12 },
    { id: 'treasure', bounds: [54, 8, 70, 27], height: 10 },
    { id: 'courtyard', bounds: [24, 29, 52, 46], height: 8 },
    { id: 'flooded-hall', bounds: [1, 33, 25, 59], height: 10 },
    { id: 'workshop', bounds: [52, 36, 70, 59], height: 10 },
    { id: 'entrance', bounds: [30, 47, 47, 63], height: 9 }
  ]
  const corridors = [[17, 23, 21, 31], [17, 29, 28, 33], [36, 21, 40, 32], [48, 19, 57, 23], [48, 20, 52, 33], [22, 39, 28, 43], [49, 39, 56, 43], [36, 44, 40, 52], [36, 60, 40, 72]]
  fill([0, 0, 0], [72, 21, 72], 'air')
  const footprint = new Set()
  const footprintRect = (west, north, east, south) => {
    for (let column = west; column <= east; column++) {
      for (let row = north; row <= south; row++) footprint.add(`${column},${row}`)
    }
  }
  for (const room of rooms) footprintRect(...room.bounds)
  for (const [west, north, east, south] of corridors) footprintRect(Math.max(0, west - 1), Math.max(0, north - 1), Math.min(72, east + 1), Math.min(72, south + 1))
  for (const coordinate of footprint) {
    const [east, south] = coordinate.split(',').map(Number)
    put(east, 0, south, 'stone')
    put(east, 1, south, 'packed_mud')
    stone([east, 2, south], [east, 4, south])
  }
  for (const room of rooms) {
    const [west, north, east, south] = room.bounds
    stone([west, 5, north], [east, room.height, north + 1])
    stone([west, 5, south - 1], [east, room.height, south])
    stone([west, 5, north], [west + 1, room.height, south])
    stone([east - 1, 5, north], [east, room.height, south])
    for (const column of [west + 2, east - 2]) {
      for (const row of [north + 2, south - 2]) fill([column, 5, row], [column, room.height - 1, row], 'dark_oak_log[axis=y]')
    }
    fill([west + 2, room.height, north + 2], [east - 2, room.height, north + 2], 'dark_oak_log[axis=x]')
    fill([west + 2, room.height, south - 2], [east - 2, room.height, south - 2], 'dark_oak_log[axis=x]')
    for (const row of [north + 5, south - 5]) {
      if (row <= north + 2 || row >= south - 2) continue
      put(west + 2, 8, row, 'ochre_froglight[axis=y]')
      put(east - 2, 8, row, 'ochre_froglight[axis=y]')
      put(west + 2, 9, row, 'dark_oak_slab[type=bottom,waterlogged=false]')
      put(east - 2, 9, row, 'dark_oak_slab[type=bottom,waterlogged=false]')
    }
    for (let column = west + 4; column <= east - 4; column += 4) {
      for (let up = 7; up < room.height; up++) put(column, up, north + 2, 'vine[north=true,east=false,south=false,west=false,up=false]')
    }
    for (let row = north + 5; row <= south - 4; row += 5) {
      for (let up = 6; up < room.height - 1; up++) put(west + 2, up, row, 'vine[west=true,east=false,north=false,south=false,up=false]')
    }
    for (const [column, row] of [[west + 1, north + 2], [east - 1, south - 3]]) {
      fill([column, room.height + 1, row], [column + 1, room.height + 1, row + 2], 'oak_leaves[persistent=true,distance=7]')
    }
  }
  for (const [west, north, east, south] of corridors) {
    for (let column = west - 1; column <= east + 1; column++) {
      for (let row = north - 1; row <= Math.min(72, south + 1); row++) {
        const vertical = south - north >= east - west
        if ((vertical && (column < west || column > east) && row >= north && row <= south) || (!vertical && (row < north || row > south) && column >= west && column <= east)) stone([column, 5, row], [column, 7, row])
      }
    }
  }
  for (const [west, north, east, south] of corridors) {
    fill([west, 5, north], [east, 10, south], 'air')
    stone([west, 4, north], [east, 4, south])
  }
  fill([36, 4, 23], [40, 4, 31], 'dark_oak_planks')
  fill([36, 4, 64], [40, 4, 72], 'dark_oak_planks')
  fill([35, 5, 64], [35, 7, 72], 'air')
  fill([41, 5, 64], [41, 7, 72], 'air')
  for (const row of [65, 70]) {
    for (const column of [35, 41]) fill([column, 4, row], [column, 6, row], 'dark_oak_log[axis=y]')
  }
  const pools = [[5, 37, 21, 55], [29, 33, 47, 42], [29, 5, 46, 18], [57, 12, 66, 23], [56, 41, 66, 54]]
  for (const [west, north, east, south] of pools) {
    for (let column = west; column <= east; column++) {
      for (let row = north; row <= south; row++) {
        if (noise(column, 4, row) < (west === 5 ? 0.96 : 0.37)) put(column, 4, row, 'water[level=0]')
      }
    }
  }
  for (const [west, north, east, south] of [[5, 40, 25, 42], [11, 36, 14, 56], [29, 36, 48, 38], [36, 29, 40, 46], [36, 4, 40, 23], [32, 14, 38, 16], [28, 19, 49, 21], [56, 19, 67, 21], [54, 40, 62, 42], [59, 39, 62, 56]]) stone([west, 4, north], [east, 4, south])
  fill([36, 4, 29], [40, 4, 35], 'dark_oak_planks')
  for (const row of [29, 35]) {
    for (const column of [35, 41]) fill([column, 4, row], [column, 5, row], 'dark_oak_log[axis=y]')
  }
  for (const column of [8, 19]) {
    for (const row of [39, 51]) {
      stone([column - 1, 4, row - 1], [column + 1, 5, row + 1])
      fill([column, 6, row], [column, 9, row], 'mossy_stone_bricks')
      put(column, 10, row, 'chiseled_stone_bricks')
    }
  }
  stone([33, 4, 33], [35, 6, 35])
  fill([34, 7, 34], [34, 9, 34], 'mossy_stone_bricks')
  put(34, 10, 34, 'chiseled_stone_bricks')
  put(34, 10, 35, 'stone_button[face=wall,facing=south,powered=false]')
  stone([32, 5, 10], [35, 5, 14])
  stone([33, 6, 11], [34, 6, 13])
  for (const column of [29, 47]) {
    stone([column - 1, 5, 20], [column + 1, 6, 22])
    fill([column, 7, 21], [column, 9, 21], 'mossy_stone_bricks')
    put(column, 10, 21, 'chiseled_stone_bricks')
  }
  fill([6, 5, 9], [18, 8, 9], 'bookshelf')
  fill([5, 5, 10], [5, 8, 20], 'bookshelf')
  fill([8, 5, 15], [12, 5, 18], 'dark_oak_planks')
  for (const column of [8, 12]) {
    for (const row of [15, 18]) put(column, 6, row, 'oak_pressure_plate[powered=false]')
  }
  stone([59, 5, 13], [64, 5, 17])
  for (const column of [60, 61, 62, 63]) put(column, 5, 18, 'stone_brick_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]')
  api.setBlock(structure, [62, 6, 15], 'minecraft:chest', { facing: 'south', type: 'single', waterlogged: 'false' }, api.createBlockNbt({ id: 'minecraft:chest', Items: [] }))
  fill([55, 5, 38], [57, 5, 39], 'dark_oak_planks')
  fill([63, 5, 38], [66, 5, 39], 'dark_oak_planks')
  for (const point of [[56, 6, 39], [57, 6, 39], [64, 6, 38]]) put(...point, 'flower_pot')
  for (const point of [[65, 5, 42], [66, 5, 43], [55, 5, 52], [7, 5, 22]]) api.setBlock(structure, point, 'minecraft:barrel', { facing: 'up', open: 'false' }, api.createBlockNbt({ id: 'minecraft:barrel', Items: [] }))
  for (const column of [33, 43]) {
    fill([column, 5, 61], [column, 11, 61], 'dark_oak_log[axis=y]')
    stone([column - 1, 5, 62], [column + 1, 10, 63])
    put(column, 8, 60, 'ochre_froglight[axis=y]')
    put(column, 9, 60, 'dark_oak_slab[type=bottom,waterlogged=false]')
  }
  stone([32, 12, 61], [44, 13, 63])
  stone([34, 14, 61], [42, 14, 63])
  fill([34, 11, 61], [42, 11, 61], 'dark_oak_log[axis=x]')
  fill([36, 5, 60], [40, 10, 63], 'air')
  for (const [west, north, east, south] of pools) {
    for (let column = west; column <= east; column++) {
      for (let row = north; row <= south; row++) {
        if (structure.getBlock([column, 4, row])?.state.getName().toString() === 'minecraft:water' && structure.getBlock([column, 5, row])?.state.getName().toString() === 'minecraft:air' && noise(column, 5, row) < 0.14) put(column, 5, row, 'lily_pad')
      }
    }
  }
  return { structure, metadata: { name: 's4-10-swamp-sanctum-complex' } }
}
