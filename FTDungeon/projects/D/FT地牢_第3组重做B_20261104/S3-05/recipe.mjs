export const size = [21, 16, 49]
export const bays = [5, 13, 21, 29, 37, 45]

export default function build(api) {
  const structure = api.createStructure(size)
  const set = (east, height, south, block) => api.setBlock(structure, [east, height, south], `minecraft:${block}`)
  const box = (from, to, block) => api.fill(structure, from, to, `minecraft:${block}`)
  const random = (east, height, south) => ((east * 71 + height * 43 + south * 137 + east * south * 17) % 101)
  box([0, 0, 0], [20, 15, 48], 'air')
  box([0, 0, 0], [20, 1, 48], 'stone_bricks')
  for (let east = 2; east <= 18; east++) for (let south = 2; south < 47; south++) {
    const noise = random(east, 1, south)
    const patch = Math.sin(east * 0.65 + south * 0.22) + Math.cos(south * 0.43 - east * 0.18)
    set(east, 1, south, patch > 0.15 ? (noise < 55 ? 'packed_ice' : 'blue_ice') : patch < -0.35 ? 'snow_block' : noise < 50 ? 'cobblestone' : 'stone_bricks')
  }
  for (let east = 0; east < 21; east++) for (let south = 0; south < 49; south++) {
    const ceiling = 11 + Math.floor((10 - Math.abs(east - 10)) / 3)
    for (let height = 2; height < 16; height++) {
      if (east < 2 || east > 18 || south < 2 || south > 46 || height >= ceiling) {
        const noise = random(east, height, south)
        set(east, height, south, noise < 16 ? 'cobblestone' : noise < 36 ? 'cracked_stone_bricks' : noise < 49 && height >= 9 ? 'packed_ice' : 'stone_bricks')
      }
    }
  }
  for (const south of bays) {
    for (const east of [3, 17]) {
      box([east - 1, 2, south - 1], [east + 1, 4, south + 1], 'stone_bricks')
      box([east - 1, 5, south - 1], [east + 1, 5, south + 1], 'stone_brick_slab[type=bottom,waterlogged=false]')
      box([east, 5, south], [east, 11, south], 'spruce_log[axis=y]')
      box([east - 1, 11, south - 1], [east + 1, 12, south + 1], 'stone_bricks')
      const inner = east < 10 ? east + 1 : east - 1
      box([Math.min(east, inner), 10, south], [Math.max(east, inner), 10, south], 'spruce_log[axis=x]')
      set(inner, 9, south, 'chain[axis=y,waterlogged=false]')
      set(inner, 8, south, 'lantern[hanging=true,waterlogged=false]')
    }
    for (let east = 5; east <= 15; east++) {
      const height = 11 + Math.floor((10 - Math.abs(east - 10)) / 3)
      set(east, height - 1, south, 'stone_bricks')
    }
  }
  for (const south of [9, 17, 25, 33, 41]) {
    for (const east of [2, 18]) {
      box([east, 2, south - 1], [east, 3, south + 1], 'stone_bricks')
      set(east, 4, south, 'snow_block')
      const face = east === 2 ? 'west' : 'east'
      for (let depth = south - 1; depth <= south + 1; depth++) for (let height = 5; height <= 10; height++) set(east, height, depth, `glow_lichen[${face}=true,waterlogged=false]`)
    }
    for (const east of [4, 16]) {
      const tall = 2 + (south % 3)
      box([east, 2, south - 1], [east, 2, south + 1], 'packed_ice')
      box([east, 3, south], [east, tall + 2, south], 'blue_ice')
      box([east === 4 ? 2 : 18, 5, south + 2], [east === 4 ? 2 : 18, 9, south + 2], 'packed_ice')
    }
  }
  for (const south of [3, 10, 18, 26, 34, 42]) for (const east of [6, 14]) {
    const top = 11 + Math.floor((10 - Math.abs(east - 10)) / 3)
    box([east - 1, top - 1, south], [east + 1, top - 1, south + 1], 'packed_ice')
    box([east, top - 3 - south % 2, south], [east, top - 2, south], 'blue_ice')
  }
  for (const [east, south] of [[3, 3], [17, 9], [3, 31], [17, 43]]) {
    const inner = east < 10 ? east + 1 : east - 1
    set(inner, 2, south, 'snow_block')
    set(inner, 3, south, 'bone_block[axis=y]')
    set(inner, 2, south + 1, 'bone_block[axis=z]')
    set(inner, 3, south + 2, 'bone_block[axis=y]')
    set(inner, 2, south + 2, 'snow_block')
  }
  for (const south of [2, 46]) {
    box([6, 2, south], [7, 7, south], 'packed_ice')
    box([13, 2, south], [14, 7, south], 'packed_ice')
    box([7, 7, south], [13, 8, south], 'packed_ice')
    box([8, 8, south], [12, 9, south], 'blue_ice')
  }
  for (const south of [0, 1, 47, 48]) {
    box([8, 2, south], [12, 6, south], 'air')
    box([9, 7, south], [11, 7, south], 'air')
    box([8, 1, south], [12, 1, south], 'stone_bricks')
  }
  return { structure, metadata: { name: 'room' } }
}
