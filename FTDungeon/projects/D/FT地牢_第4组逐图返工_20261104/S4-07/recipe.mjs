export default function build(api) {
  const structure = api.createStructure([29, 20, 29])
  const put = (east, up, south, block) => api.setBlock(structure, [east, up, south], `minecraft:${block}`)
  const fill = (start, end, block) => api.fill(structure, start, end, `minecraft:${block}`)
  const noise = (east, up, south) => Math.abs(Math.sin(east * 71.9 + up * 113.3 + south * 49.7) * 29491) % 1
  const stone = (start, end) => {
    for (let east = start[0]; east <= end[0]; east++) {
      for (let up = start[1]; up <= end[1]; up++) {
        for (let south = start[2]; south <= end[2]; south++) {
          const value = noise(east, up, south)
          put(east, up, south, value < 0.64 ? 'mossy_stone_bricks' : value < 0.83 ? 'cracked_stone_bricks' : 'stone_bricks')
        }
      }
    }
  }
  fill([0, 0, 0], [28, 0, 28], 'stone')
  fill([0, 1, 0], [28, 2, 25], 'packed_mud')
  stone([0, 3, 0], [28, 4, 25])
  fill([3, 4, 5], [25, 4, 23], 'water[level=0]')
  stone([0, 5, 3], [2, 8, 25])
  stone([26, 5, 3], [28, 8, 25])
  stone([0, 5, 24], [28, 7, 25])
  for (const east of [0, 1, 2, 26, 27, 28]) {
    for (let south = 1; south <= 20; south++) {
      const top = Math.max(9, 17 - Math.floor(south / 3))
      stone([east, 5, south], [east, top, south])
    }
  }
  stone([0, 5, 0], [28, 16, 2])
  for (let east = 0; east <= 28; east++) {
    const top = 17 + Math.max(0, 2 - Math.floor(Math.abs(east - 14) / 3))
    if (noise(east, 18, 1) > 0.15) stone([east, 17, 0], [east, top, 2])
  }
  for (let east = 11; east <= 17; east++) {
    const top = east === 11 || east === 17 ? 12 : east === 12 || east === 16 ? 14 : 15
    fill([east, 5, 0], [east, top, 3], 'air')
  }
  stone([11, 3, 0], [17, 4, 5])
  for (const east of [10, 18]) fill([east, 5, 2], [east, 15, 2], 'dark_oak_log[axis=y]')
  fill([10, 16, 3], [18, 16, 3], 'dark_oak_log[axis=x]')
  for (let south = 5; south <= 23; south++) {
    for (let east = 12; east <= 16; east++) {
      if (east === 14 && south > 9 && south < 20) continue
      put(east, 4, south, 'stone_brick_slab[type=top,waterlogged=true]')
    }
  }
  stone([11, 3, 22], [17, 4, 25])
  fill([13, 5, 24], [14, 8, 25], 'air')
  stone([13, 7, 24], [14, 8, 25])
  for (const east of [12, 15]) {
    stone([east, 3, 24], [east, 8, 25])
    put(east, 7, 26, 'ochre_froglight[axis=y]')
    put(east, 6, 26, 'mossy_stone_bricks')
  }
  for (const east of [13, 14]) {
    for (const half of ['lower', 'upper']) {
      put(east, half === 'lower' ? 5 : 6, 25, `dark_oak_door[facing=south,half=${half},hinge=${east === 13 ? 'right' : 'left'},open=false,powered=false]`)
    }
  }
  for (let south = 26; south <= 28; south++) {
    const up = 30 - south
    stone([11, 1, south], [16, up - 1, south])
    for (let east = 11; east <= 16; east++) put(east, up, south, 'stone_brick_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]')
  }
  for (const east of [3, 25]) {
    fill([east, 5, 6], [east, 12, 6], 'dark_oak_log[axis=y]')
    fill([east, 5, 14], [east, 10, 14], 'dark_oak_log[axis=y]')
    fill([east, 11, 6], [east, 11, 15], 'dark_oak_log[axis=z]')
    put(east, 10, 10, 'dark_oak_log[axis=y]')
  }
  stone([3, 3, 7], [6, 5, 14])
  stone([22, 3, 7], [25, 5, 14])
  for (const point of [[4, 6, 8], [5, 6, 10], [4, 6, 12], [5, 7, 10], [23, 6, 8], [24, 6, 10], [23, 6, 12], [24, 7, 10]]) {
    api.setBlock(structure, point, 'minecraft:barrel', { facing: 'up', open: 'false' }, api.createBlockNbt({ id: 'minecraft:barrel', Items: [] }))
  }
  for (const east of [4, 24]) {
    put(east, 10, 10, 'dark_oak_log[axis=x]')
    put(east, 9, 10, 'ochre_froglight[axis=y]')
    put(east, 8, 10, 'dark_oak_slab[type=top,waterlogged=false]')
  }
  stone([6, 3, 4], [10, 5, 5])
  for (const east of [6, 10]) fill([east, 6, 4], [east, 11, 4], 'dark_oak_log[axis=y]')
  fill([6, 6, 4], [10, 6, 4], 'dark_oak_log[axis=x]')
  fill([6, 11, 4], [10, 11, 4], 'dark_oak_log[axis=x]')
  fill([7, 7, 4], [9, 10, 4], 'birch_planks')
  for (const point of [[7, 8, 4], [8, 9, 4], [9, 8, 4]]) put(...point, 'chiseled_sandstone')
  stone([8, 3, 16], [10, 5, 18])
  put(9, 6, 17, 'stone_bricks')
  api.setBlock(structure, [9, 7, 17], 'minecraft:campfire', { facing: 'north', lit: 'true', signal_fire: 'false', waterlogged: 'false' }, api.createBlockNbt({ id: 'minecraft:campfire', Items: [] }))
  for (const point of [[10, 10, 3], [18, 10, 3]]) {
    put(...point, 'ochre_froglight[axis=y]')
    put(point[0], point[1] - 1, point[2], 'mossy_stone_bricks')
  }
  for (const east of [4, 8, 20, 24]) {
    for (let up = 8; up <= 14; up++) put(east, up, 3, 'vine[north=true]')
  }
  for (const south of [5, 9, 13, 17]) {
    for (const east of [3, 25]) {
      const face = east === 3 ? 'west' : 'east'
      for (let up = 6; up <= 10; up++) {
        if (!structure.getBlock([east, up, south])) put(east, up, south, `vine[${face}=true]`)
      }
    }
  }
  for (const east of [11, 17]) {
    put(east, 15, 3, 'cave_vines_plant[berries=false]')
    put(east, 14, 3, 'cave_vines[age=25,berries=false]')
  }
  for (const east of [3, 7, 10, 18, 22, 25]) {
    for (let up = 3; up <= 6; up++) put(east, up, 26, 'vine[north=true]')
  }
  for (let east = 3; east <= 25; east++) {
    for (let south = 6; south <= 23; south++) {
      if (structure.getBlock([east, 4, south])?.state.getName().toString() === 'minecraft:water' && !structure.getBlock([east, 5, south]) && noise(east, 5, south) < 0.10) put(east, 5, south, 'lily_pad')
    }
  }
  return { structure, metadata: { name: 's4-07-swamp-gate-courtyard' } }
}
