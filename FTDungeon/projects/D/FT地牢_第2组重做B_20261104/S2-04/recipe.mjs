export default function build(api) {
  const structure = api.createStructure([29, 21, 39])
  const put = (east, up, south, block, properties, entity) => api.setBlock(structure, [east, up, south], `minecraft:${block}`, properties, entity)
  const fill = (first, last, block) => api.fill(structure, first, last, `minecraft:${block}`)
  const roof = east => 17 + Math.floor(Math.min(east, 28 - east) / 5)
  const banner = (east, up, south, facing) => put(east, up, south, 'orange_wall_banner', { facing }, api.createBlockNbt({ id: 'minecraft:banner', Patterns: [{ Color: 4, Pattern: 'bo' }, { Color: 4, Pattern: 'mr' }, { Color: 1, Pattern: 'mc' }] }))
  fill([0, 0, 0], [28, 20, 38], 'air')
  fill([0, 0, 4], [28, 1, 38], 'sandstone')
  for (let east = 0; east <= 28; east++) for (let south = 4; south <= 38; south++) {
    const border = (east === 10 || east === 18) && south >= 7 && south <= 35
    const center = [11, 19, 27, 34].some(origin => Math.abs(east - 14) + Math.abs(south - origin) === 3 || east === 14 && south === origin)
    const side = (south === 14 || south === 25) && (east <= 9 || east >= 19)
    put(east, 1, south, border || center || side ? 'orange_terracotta' : (east * 3 + south) % 23 === 0 ? 'cut_sandstone' : 'smooth_sandstone')
    if (south >= 8) put(east, roof(east), south, 'cut_sandstone')
  }
  for (let up = 2; up <= 16; up++) {
    const wall = up === 7 || up === 12 ? 'orange_terracotta' : up === 8 || up === 15 ? 'chiseled_sandstone' : 'sandstone'
    fill([0, up, 4], [2, up, 38], wall)
    fill([26, up, 4], [28, up, 38], wall)
    fill([0, up, 36], [28, up, 38], wall)
  }
  for (let east = 0; east <= 28; east++) fill([east, 17, 36], [east, roof(east), 38], 'cut_sandstone')
  fill([0, 2, 4], [10, 3, 6], 'sandstone')
  fill([18, 2, 4], [28, 3, 6], 'sandstone')
  fill([9, 0, 0], [19, 0, 3], 'cut_sandstone')
  for (let east = 11; east <= 17; east++) put(east, 1, 2, 'smooth_sandstone_stairs', { facing: 'south', half: 'bottom', shape: 'straight', waterlogged: 'false' })
  fill([11, 1, 3], [17, 1, 3], 'smooth_sandstone')
  for (const east of [9, 19]) fill([east, 0, 1], [east, 2, 3], 'cut_sandstone')
  for (const east of [7, 21]) for (const south of [11, 22, 32]) {
    fill([east-1, 2, south-1], [east+1, 2, south+1], 'cut_sandstone')
    for (let up = 3; up <= 15; up++) fill([east, up, south], [east+1, up, south+1], up <= 4 || up === 10 ? 'orange_terracotta' : up === 5 || up === 14 ? 'chiseled_sandstone' : 'cut_sandstone')
    fill([east-1, 15, south-1], [east+2, 16, south+2], 'chiseled_sandstone')
    banner(east, 12, south-1, 'north')
  }
  for (const south of [11, 22, 32]) for (let east = 2; east <= 26; east++) {
    put(east, 16, south, 'cut_sandstone')
    for (let up = 17; up < roof(east); up++) put(east, up, south, 'smooth_sandstone')
  }
  for (const side of ['north', 'south', 'west', 'east']) {
    const center = side === 'north' || side === 'south' ? 14 : 19
    const feet = side === 'south' ? 3 : 2
    for (let inset = 0; inset <= 2; inset++) for (let offset = -4; offset <= 4; offset++) for (let up = feet; up <= feet+7; up++) {
      const east = side === 'west' ? inset : side === 'east' ? 28-inset : center+offset
      const south = side === 'north' ? 4+inset : side === 'south' ? 38-inset : center+offset
      const radius = up < feet+4 ? 2 : up < feet+6 ? 1 : up === feet+6 ? 0 : -1
      put(east, up, south, Math.abs(offset) <= radius ? 'air' : up === feet+5 ? 'orange_terracotta' : 'smooth_sandstone')
    }
  }
  fill([12, 2, 36], [16, 2, 38], 'cut_sandstone')
  for (let east = 12; east <= 16; east++) put(east, 2, 35, 'smooth_sandstone_stairs', { facing: 'south', half: 'bottom', shape: 'straight', waterlogged: 'false' })
  for (const [east, south, lampY] of [[5, 10, 8], [23, 10, 8], [5, 21, 9], [23, 21, 9], [5, 31, 10], [23, 31, 10], [14, 33, 13], [14, 12, 13]]) {
    for (let up = lampY+1; up < roof(east); up++) put(east, up, south, 'chain', { axis: 'y', waterlogged: 'false' })
    put(east, lampY, south, 'lantern', { hanging: 'true', waterlogged: 'false' })
  }
  for (const [east, south] of [[4, 8], [24, 9], [4, 28], [24, 29], [9, 34], [19, 34]]) {
    put(east, 2, south, 'terracotta')
    put(east, 3, south, 'flower_pot')
  }
  for (const [east, south] of [[4, 13], [24, 14], [4, 24], [24, 25], [9, 30], [19, 30]]) {
    put(east, 1, south, 'sand')
    put(east, 2, south, 'dead_bush')
  }
  for (const [east, south] of [[4, 16], [24, 16], [9, 25], [19, 25], [4, 33], [24, 33]]) put(east, 2, south, 'smooth_sandstone_slab', { type: 'bottom', waterlogged: 'false' })
  banner(14, 13, 35, 'north')
  banner(3, 11, 26, 'east')
  banner(25, 11, 26, 'west')
  return { structure, metadata: { name: 'room' } }
}
