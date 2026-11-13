export default function build(api) {
  const structure = api.createStructure([37, 23, 49])
  const put = (east, up, south, block) => api.setBlock(structure, [east, up, south], `minecraft:${block}`)
  const fill = (start, end, block) => api.fill(structure, start, end, `minecraft:${block}`)
  const noise = (east, up, south) => Math.abs(Math.sin(east * 127.9 + up * 57.1 + south * 231.7) * 49739.2) % 1
  const stone = (start, end) => {
    for (let east = start[0]; east <= end[0]; east++) {
      for (let up = start[1]; up <= end[1]; up++) {
        for (let south = start[2]; south <= end[2]; south++) {
          const value = noise(east, up, south)
          put(east, up, south, value < 0.6 ? 'mossy_stone_bricks' : value < 0.78 ? 'cracked_stone_bricks' : value < 0.9 ? 'stone_bricks' : 'mossy_cobblestone')
        }
      }
    }
  }
  fill([0, 0, 0], [36, 0, 48], 'stone')
  fill([0, 1, 0], [36, 1, 48], 'packed_mud')
  stone([0, 2, 0], [36, 4, 48])
  fill([3, 2, 3], [33, 3, 45], 'water[level=0]')
  fill([3, 4, 3], [33, 4, 45], 'air')
  stone([15, 2, 0], [21, 4, 48])
  for (const south of [12, 24, 36]) {
    for (let east = 17; east <= 19; east++) {
      for (let offset = -2; offset <= 2; offset++) put(east, 4, south + offset, 'chiseled_stone_bricks')
    }
  }
  for (let south = 2; south <= 46; south++) {
    for (const east of [0, 1, 35, 36]) {
      const height = 18 + (Math.floor(south / 6) % 3 === 0 ? 1 : 0)
      stone([east, 5, south], [east, height, south])
      if (south % 8 < 4) put(east, height + 1, south, 'mossy_stone_bricks')
    }
  }
  stone([0, 5, 0], [36, 19, 2])
  stone([0, 5, 46], [36, 7, 48])
  fill([2, 17, 2], [2, 17, 45], 'dark_oak_log[axis=z]')
  fill([34, 17, 2], [34, 17, 45], 'dark_oak_log[axis=z]')
  fill([2, 18, 2], [34, 18, 2], 'dark_oak_log[axis=x]')
  for (const south of [6, 18, 30, 42]) {
    for (const east of [2, 34]) {
      fill([east, 4, south], [east, 17, south], 'dark_oak_log[axis=y]')
      const inside = east === 2 ? 3 : 33
      for (let up = 11 + south % 3; up <= 16; up++) put(inside, up, south, east === 2 ? 'vine[west=true]' : 'vine[east=true]')
      if (south !== 30) {
        stone([inside, 2, south + 1], [inside, 6, south + 1])
        put(inside, 7, south + 1, 'ochre_froglight[axis=y]')
      }
    }
  }
  for (const south of [12, 24, 36]) {
    for (const east of [9, 27]) {
      stone([east - 2, 2, south - 2], [east + 2, 4, south + 2])
      stone([east - 1, 5, south - 1], [east + 1, 6, south + 1])
      fill([east, 7, south], [east + 1, 15, south + 1], 'dark_oak_log[axis=y]')
      stone([east - 1, 16, south - 1], [east + 2, 17, south + 2])
      put(east, 18, south, 'ochre_froglight[axis=y]')
      api.setBlock(structure, [east, 13, south + 2], 'minecraft:green_wall_banner', { facing: 'south' }, api.createBlockNbt({ id: 'minecraft:banner', Patterns: [{ Pattern: 'bo', Color: 4 }, { Pattern: 'mr', Color: 4 }, { Pattern: 'flo', Color: 4 }] }))
      for (const offset of [-1, 2]) {
        put(east + offset, 7, south - 1, 'mossy_stone_bricks')
        put(east + offset, 8, south - 1, 'ochre_froglight[axis=y]')
      }
    }
  }
  for (let east = 12; east <= 24; east++) {
    const distance = Math.abs(east - 18)
    const top = distance <= 3 ? 14 : distance <= 4 ? 13 : distance <= 5 ? 11 : 8
    fill([east, 5, 0], [east, top, 2], 'air')
    stone([east, top + 1, 0], [east, top + 2, 2])
  }
  for (const east of [11, 25]) stone([east, 5, 0], [east, 15, 3])
  for (const east of [9, 27]) {
    fill([east, 8, 3], [east, 15, 3], 'dark_oak_log[axis=y]')
    api.setBlock(structure, [east, 14, 4], 'minecraft:green_wall_banner', { facing: 'south' }, api.createBlockNbt({ id: 'minecraft:banner', Patterns: [{ Pattern: 'bo', Color: 4 }, { Pattern: 'mr', Color: 4 }] }))
  }
  fill([15, 5, 45], [21, 10, 48], 'air')
  for (const east of [13, 23]) stone([east, 5, 46], [east, 9, 48])
  for (const east of [0, 1, 2, 3, 33, 34, 35, 36]) {
    for (let south = 27; south <= 33; south++) {
      const top = Math.abs(south - 30) <= 2 ? 10 : 8
      fill([east, 5, south], [east, top, south], 'air')
      put(east, top + 1, south, 'mossy_stone_bricks')
    }
  }
  for (const range of [[[0, 2, 28], [6, 4, 32]], [[30, 2, 28], [36, 4, 32]]]) stone(range[0], range[1])
  for (let east = 6; east <= 30; east++) {
    if (east >= 15 && east <= 21) continue
    fill([east, 2, 30], [east, 3, 30], 'water[level=0]')
    put(east, 4, 30, 'stone_brick_slab[type=top,waterlogged=true]')
  }
  for (const south of [6, 18, 30, 42]) {
    for (const east of [3, 33]) {
      if (south === 30) continue
      const sign = east === 3 ? 1 : -1
      fill([east, 3, south], [east, 6, south], 'dark_oak_log[axis=y]')
      put(east + sign, 4, south, 'dark_oak_log[axis=x]')
      put(east + sign, 3, south, 'mangrove_roots[waterlogged=true]')
    }
  }
  for (let east = 4; east <= 32; east++) {
    for (let south = 4; south <= 44; south++) {
      if (structure.getBlock([east, 3, south])?.state.getName().toString() === 'minecraft:water' && structure.getBlock([east, 4, south])?.state.getName().toString() === 'minecraft:air' && noise(east, 5, south) < 0.065) put(east, 4, south, 'lily_pad')
    }
  }
  return { structure, metadata: { name: 's4-04-drowned-banner-hall' } }
}
