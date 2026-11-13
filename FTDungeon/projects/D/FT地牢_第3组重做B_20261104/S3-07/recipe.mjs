export const size = [34, 24, 37]
export const roofed = (east, south) => south <= 8 && east >= 9 && east <= 24

export default function build(api) {
  const structure = api.createStructure(size)
  const set = (east, height, south, block) => api.setBlock(structure, [east, height, south], `minecraft:${block}`)
  const box = (from, to, block) => api.fill(structure, from, to, `minecraft:${block}`)
  const noise = (east, height, south) => (east * 71 + height * 43 + south * 137 + east * south * 17) % 101
  box([0, 0, 0], [33, 23, 36], 'air')
  box([0, 0, 0], [33, 1, 36], 'stone_bricks')
  for (let east = 0; east < 34; east++) for (let south = 0; south < 37; south++) {
    const deck = south <= 7 ? 7 : south <= 32 ? 4 : 1
    for (let height = 2; height <= deck; height++) set(east, height, south, noise(east, height, south) < 20 ? 'cobblestone' : 'stone_bricks')
    const patch = Math.sin(east * 0.58 + south * 0.2) + Math.cos(south * 0.42 - east * 0.2)
    set(east, deck, south, patch > 0.3 ? (noise(east, deck, south) < 65 ? 'packed_ice' : 'blue_ice') : patch < -0.2 ? 'snow_block' : 'stone_bricks')
    if (east < 4 || east > 29 || south < 4 || (south < 11 && (east < 11 || east > 22))) {
      const top = Math.min(22, (south < 11 ? 21 : 9) + noise(east, 8, south) % 3)
      for (let height = deck + 1; height <= top; height++) set(east, height, south, noise(east, height, south) < 28 ? 'cobblestone' : noise(east, height, south) < 45 ? 'cracked_stone_bricks' : 'stone_bricks')
      set(east, top + 1, south, 'snow_block')
    }
  }
  box([11, 8, 0], [22, 17, 8], 'air')
  box([9, 18, 0], [24, 21, 9], 'stone_bricks')
  box([9, 22, 0], [24, 22, 9], 'snow_block')
  for (const east of [10, 23]) box([east, 5, 8], [east, 16, 11], 'stone_bricks')
  box([10, 17, 8], [23, 19, 10], 'stone_bricks')
  box([11, 19, 9], [22, 20, 11], 'snow_block')
  for (const east of [12, 21]) {
    box([east, 5, 11], [east, 12, 11], 'stone_bricks')
    set(east, 13, 11, 'spruce_log[axis=x]')
    set(east, 14, 11, 'snow_block')
    set(east, 12, 12, 'spruce_log[axis=z]')
    set(east, 11, 12, 'chain[axis=y,waterlogged=false]')
    set(east, 10, 12, 'lantern[hanging=true,waterlogged=false]')
  }
  for (let south = 8; south <= 10; south++) {
    const height = 15 - south
    box([13, 2, south], [20, height - 1, south], 'stone_bricks')
    box([13, height, south], [20, height, south], 'stone_brick_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]')
  }
  for (let south = 33; south <= 35; south++) {
    const height = 37 - south
    box([14, 1, south], [19, height - 1, south], 'stone_bricks')
    box([14, height, south], [19, height, south], 'stone_brick_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]')
  }
  for (const east of [13, 20]) {
    box([east - 1, 5, 28], [east + 1, 10, 30], 'stone_bricks')
    box([east - 1, 11, 28], [east + 1, 11, 30], 'snow_block')
  }
  box([3, 5, 29], [12, 7, 30], 'stone_bricks')
  box([21, 5, 29], [30, 7, 30], 'stone_bricks')
  box([3, 8, 29], [12, 8, 30], 'snow_block')
  box([21, 8, 29], [30, 8, 30], 'snow_block')
  for (const east of [15, 18]) box([east, 5, 29], [east, 7, 29], 'spruce_planks')
  box([15, 7, 29], [18, 7, 29], 'spruce_log[axis=x]')
  for (const [east, hinge] of [[16, 'right'], [17, 'left']]) for (const [height, half] of [[5, 'lower'], [6, 'upper']]) set(east, height, 29, `spruce_door[facing=south,half=${half},hinge=${hinge},open=true,powered=false]`)
  for (const east of [6, 27]) {
    box([east, 5, 18], [east, 12, 18], 'spruce_log[axis=y]')
    box([east - 1, 12, 18], [east + 1, 12, 18], 'spruce_log[axis=x]')
    box([east - 1, 13, 18], [east + 1, 13, 18], 'snow_block')
    set(east, 11, 19, 'spruce_log[axis=z]')
    set(east, 10, 19, 'chain[axis=y,waterlogged=false]')
    set(east, 9, 19, 'lantern[hanging=true,waterlogged=false]')
  }
  for (const east of [5, 9]) box([east, 5, 21], [east, 5, 22], 'spruce_log[axis=y]')
  box([5, 6, 21], [9, 6, 22], 'spruce_slab[type=bottom,waterlogged=false]')
  box([5, 7, 21], [9, 8, 21], 'spruce_planks')
  for (const east of [6, 11]) box([east, 5, 14], [east, 11, 14], 'spruce_log[axis=y]')
  box([6, 11, 14], [11, 11, 14], 'spruce_log[axis=x]')
  box([7, 7, 14], [10, 10, 14], 'smooth_sandstone')
  box([6, 6, 14], [11, 6, 14], 'spruce_log[axis=x]')
  box([12, 5, 16], [13, 6, 17], 'stone_bricks')
  box([12, 7, 16], [13, 7, 17], 'stone_brick_slab[type=bottom,waterlogged=false]')
  api.setBlock(structure, [12,8,16], 'minecraft:campfire[facing=south,lit=true,signal_fire=false,waterlogged=false]', undefined, api.createBlockNbt({ id: 'minecraft:campfire' }))
  set(12, 7, 16, 'stone_bricks')
  box([3, 5, 12], [5, 8, 15], 'cobblestone')
  box([3, 9, 12], [5, 9, 15], 'snow_block')
  box([28, 5, 24], [30, 8, 27], 'cobblestone')
  box([28, 9, 24], [30, 9, 27], 'snow_block')
  for (const [east, south, floor] of [[4,13,10],[29,25,10],[5,32,5],[27,32,5],[6,35,2],[25,35,2]]) {
    box([east, floor, south], [east, floor + 3, south], 'packed_ice')
    set(east, floor + 4, south, 'blue_ice')
    box([east - 1, floor, south], [east + 1, floor + 1, south], 'packed_ice')
  }
  for (const [east, south] of [[26, 14], [28, 14], [27, 23], [11, 23]]) {
    set(east, 5, south, 'spruce_planks')
    set(east, 6, south, 'spruce_slab[type=bottom,waterlogged=false]')
  }
  for (const [east, south] of [[4, 5], [7, 8], [27, 6], [30, 11], [4, 27], [29, 27], [8, 31], [25, 31]]) {
    const floor = south <= 7 ? 22 : south <= 10 ? 20 : south <= 32 ? (east < 5 || east > 28 ? 10 : 4) : 1
    box([east, floor, south], [east, Math.min(23, floor + 2), south], 'packed_ice')
  }
  for (const east of [7, 12, 21, 27]) {
    box([east, 18, 10], [east, 20, 10], 'packed_ice')
    box([east, 15 + east % 2, 10], [east, 17, 10], 'blue_ice')
  }
  for (const east of [13, 20]) for (const south of [2, 5]) {
    box([east, 8, south], [east, 11, south], 'blue_ice')
    box([east, 15, south], [east, 18, south], 'packed_ice')
  }
  for (const [east, south] of [[5, 16], [28, 20], [7, 27], [26, 26]]) {
    box([east, 5, south], [east, 7, south], 'packed_ice')
    set(east, 8, south, 'blue_ice')
  }
  for (let east = 0; east < 34; east++) if (east < 14 || east > 19) box([east, 2, 36], [east, 3, 36], 'snow_block')
  box([14, 1, 36], [19, 1, 36], 'stone_bricks')
  box([9, 8, 0], [24, 17, 1], 'stone_bricks')
  box([14, 8, 0], [19, 12, 1], 'air')
  box([14, 7, 0], [19, 7, 1], 'stone_bricks')
  return { structure, metadata: { name: 'room' } }
}
