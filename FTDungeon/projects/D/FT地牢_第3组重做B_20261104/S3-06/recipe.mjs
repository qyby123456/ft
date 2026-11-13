export const size = [37, 19, 35]
export const footprint = (east, south) => east <= 18 || south >= 16
export const inside = (east, south) => (east >= 2 && east <= 14 && south >= 2 && south <= 32) || (east >= 2 && east <= 34 && south >= 20 && south <= 32)

export default function build(api) {
  const structure = api.createStructure(size)
  const set = (east, height, south, block) => api.setBlock(structure, [east, height, south], `minecraft:${block}`)
  const box = (from, to, block) => api.fill(structure, from, to, `minecraft:${block}`)
  const noise = (east, height, south) => (east * 71 + height * 43 + south * 137 + east * south * 17) % 101
  box([0, 0, 0], [36, 18, 34], 'air')
  for (let east = 0; east < 37; east++) for (let south = 0; south < 35; south++) {
    if (!footprint(east, south)) continue
    const distance = Math.min(Math.abs(east - 8), Math.abs(south - 26))
    const ceiling = 15 + Math.max(0, 2 - Math.floor(distance / 3))
    for (let height = 0; height < 19; height++) {
      if (height <= 4 || !inside(east, south) || height >= ceiling) {
        const value = noise(east, height, south)
        set(east, height, south, value < 16 ? 'cobblestone' : value < 34 ? 'cracked_stone_bricks' : 'stone_bricks')
      }
    }
    if (inside(east, south)) {
      const patch = Math.sin(east * 0.62 + south * 0.24) + Math.cos(south * 0.39 - east * 0.21)
      set(east, 4, south, patch > 0.2 ? (noise(east, 4, south) < 55 ? 'packed_ice' : 'blue_ice') : patch < -0.4 ? 'snow_block' : 'stone_bricks')
    }
  }
  for (const south of [5, 13]) for (const east of [2, 14]) {
    box([east, 5, south - 1], [east, 7, south + 1], 'stone_bricks')
    box([east, 8, south], [east, 14, south], 'spruce_log[axis=y]')
    const inner = east === 2 ? 3 : 13
    set(inner, 13, south, 'spruce_log[axis=x]')
    set(inner, 12, south, 'chain[axis=y,waterlogged=false]')
    set(inner, 11, south, 'lantern[hanging=true,waterlogged=false]')
    set(east, 15, south, 'snow_block')
  }
  for (const east of [23, 31]) for (const south of [20, 32]) {
    box([east - 1, 5, south], [east + 1, 7, south], 'stone_bricks')
    box([east, 8, south], [east, 14, south], 'spruce_log[axis=y]')
    const inner = south === 20 ? 21 : 31
    set(east, 13, inner, 'spruce_log[axis=z]')
    set(east, 12, inner, 'chain[axis=y,waterlogged=false]')
    set(east, 11, inner, 'lantern[hanging=true,waterlogged=false]')
    set(east, 15, south, 'snow_block')
  }
  box([14, 5, 17], [16, 15, 19], 'stone_bricks')
  box([13, 5, 17], [13, 14, 17], 'spruce_log[axis=y]')
  box([17, 5, 20], [17, 14, 20], 'spruce_log[axis=y]')
  box([13, 13, 18], [14, 13, 20], 'spruce_log[axis=x]')
  box([13, 14, 18], [14, 14, 20], 'snow_block')
  set(13, 12, 20, 'chain[axis=y,waterlogged=false]')
  set(13, 11, 20, 'lantern[hanging=true,waterlogged=false]')
  for (const south of [8, 17]) for (const east of [2, 14]) for (let height = 9; height <= 14; height++) {
    if (east === 14 && south === 17) continue
    set(east, height, south, `glow_lichen[${east === 2 ? 'west' : 'east'}=true,waterlogged=false]`)
  }
  for (const east of [20, 27]) for (const south of [20, 32]) for (let height = 8; height <= 14; height++) set(east, height, south, `glow_lichen[${south === 20 ? 'north' : 'south'}=true,waterlogged=false]`)
  for (const south of [7, 16, 23, 30]) for (const east of [3, 12]) {
    if (east === 12 && south === 23) continue
    box([east, 5, south], [east, 6 + south % 3, south], 'packed_ice')
    set(east, 7 + south % 3, south, 'blue_ice')
  }
  for (const east of [19, 27, 33]) for (const south of [21, 31]) {
    box([east, 5, south], [east, 7, south], 'packed_ice')
    set(east, 8, south, 'blue_ice')
  }
  for (const south of [4, 10, 18, 25, 31]) for (const east of [5, 11]) {
    box([east, 14, south], [east + 1, 16, south], 'packed_ice')
    box([east, 12 + south % 2, south], [east, 13, south], 'blue_ice')
  }
  for (const east of [18, 25, 32]) for (const south of [23, 29]) {
    box([east, 14, south], [east, 17, south], 'packed_ice')
    box([east, 12, south], [east, 13, south], 'blue_ice')
  }
  box([2, 1, 27], [14, 3, 30], 'air')
  box([2, 4, 27], [5, 4, 30], 'air')
  box([13, 4, 27], [14, 4, 30], 'air')
  box([6, 4, 27], [12, 4, 30], 'blue_ice')
  box([5, 4, 27], [5, 4, 30], 'packed_ice')
  box([2, 4, 30], [3, 4, 30], 'packed_ice')
  box([5, 5, 27], [5, 5, 30], 'snow_block')
  box([13, 5, 27], [13, 5, 30], 'snow_block')
  for (const south of [22, 23, 24]) {
    set(3, 5, south, 'bone_block[axis=z]')
    set(3, 6, south, 'bone_block[axis=y]')
    set(4, 5, south, 'bone_block[axis=x]')
  }
  for (const east of [6, 10]) box([east, 5, 2], [east, 10, 2], 'packed_ice')
  box([6, 10, 2], [10, 11, 2], 'packed_ice')
  for (const south of [0, 1]) box([6, 5, south], [10, 8, south], 'air')
  for (const east of [35, 36]) box([east, 5, 24], [east, 8, 28], 'air')
  return { structure, metadata: { name: 'room' } }
}
