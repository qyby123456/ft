export default function build(api) {
  const structure = api.createStructure([19, 11, 21])
  const put = (east, up, south, block, properties, entity) => api.setBlock(structure, [east, up, south], `minecraft:${block}`, properties, entity)
  const fill = (first, last, block) => api.fill(structure, first, last, `minecraft:${block}`)
  fill([0, 0, 0], [18, 10, 20], 'air')
  fill([0, 0, 4], [18, 1, 20], 'sandstone')
  for (let east = 0; east <= 18; east++) for (let south = 4; south <= 20; south++) {
    const cross = (east >= 7 && east <= 11 && south >= 8 && south <= 14) || (east >= 6 && east <= 12 && south >= 10 && south <= 12)
    put(east, 1, south, cross && (east + south) % 7 !== 0 ? 'orange_terracotta' : (east * 5 + south) % 19 === 0 ? 'cut_sandstone' : 'smooth_sandstone')
  }
  fill([6, 0, 0], [12, 0, 3], 'cut_sandstone')
  for (let east = 7; east <= 11; east++) put(east, 1, 2, 'smooth_sandstone_stairs', { facing: 'south', half: 'bottom', shape: 'straight', waterlogged: 'false' })
  fill([7, 1, 3], [11, 1, 3], 'smooth_sandstone')
  for (const east of [6, 12]) fill([east, 1, 1], [east, 2, 3], 'cut_sandstone')
  for (let up = 2; up <= 8; up++) {
    const material = up === 6 ? 'orange_terracotta' : up === 7 ? 'chiseled_sandstone' : 'sandstone'
    fill([1, up, 4], [2, up, 19], material)
    fill([16, up, 4], [17, up, 19], material)
    fill([1, up, 18], [17, up, 19], material)
  }
  for (const east of [3, 6, 12, 15]) for (let up = 2; up <= 8; up++) put(east, up, 17, up === 6 ? 'orange_terracotta' : 'cut_sandstone')
  for (const south of [5, 11, 16]) for (let up = 2; up <= 8; up++) put(15, up, south, up === 6 ? 'orange_terracotta' : 'cut_sandstone')
  fill([1, 2, 4], [6, 3, 4], 'sandstone')
  fill([12, 2, 4], [17, 3, 4], 'sandstone')
  for (const east of [2, 16]) {
    fill([east, 2, 4], [east, 8, 5], 'cut_sandstone')
    put(east, 5, 4, 'chiseled_red_sandstone')
    put(east, 6, 4, 'orange_terracotta')
  }
  for (const side of ['south', 'east']) for (let inset = 0; inset <= 3; inset++) for (let offset = -2; offset <= 2; offset++) for (let up = 2; up <= 7; up++) {
    const east = side === 'south' ? 9 + offset : 18 - inset
    const south = side === 'south' ? 20 - inset : 13 + offset
    const opening = up <= 5 ? Math.abs(offset) <= 1 : up === 6 && offset === 0
    put(east, up, south, opening ? 'air' : up === 6 ? 'orange_terracotta' : 'smooth_sandstone')
  }
  for (let south = 12; south <= 15; south++) {
    fill([2, 3, south], [3, 5, south], 'air')
    put(2, 3, south, 'smooth_sandstone')
    put(3, 3, south, 'smooth_sandstone_slab', { type: 'top', waterlogged: 'false' })
  }
  for (let south = 8; south <= 10; south++) {
    fill([16, 3, south], [16, 5, south], 'air')
    put(16, 3, south, 'smooth_sandstone')
  }
  for (const [east, up, south] of [[2, 4, 13], [2, 4, 15], [4, 2, 13], [4, 2, 16], [16, 4, 9], [14, 2, 17]]) {
    put(east, up, south, 'terracotta')
    put(east, up + 1, south, 'flower_pot')
  }
  for (const [east, south] of [[4, 7], [14, 7], [5, 17], [14, 16]]) {
    put(east, 1, south, 'sand')
    put(east, 2, south, 'dead_bush')
  }
  for (let east = 8; east <= 10; east++) for (let south = 10; south <= 12; south++) put(east, 2, south, 'smooth_sandstone_slab', { type: 'bottom', waterlogged: 'false' })
  put(9, 2, 11, 'cut_sandstone')
  put(9, 3, 11, 'chest', { facing: 'north', type: 'single', waterlogged: 'false' }, api.createBlockNbt({ id: 'minecraft:chest' }))
  for (let east = 1; east <= 17; east++) {
    put(east, 9, 18, 'smooth_sandstone')
    if (east % 3 === 0) put(east, 10, 18, 'cut_sandstone')
  }
  for (let south = 4; south <= 19; south++) for (const east of [1, 17]) {
    put(east, 9, south, 'smooth_sandstone')
    if (south % 4 === 0) put(east, 10, south, 'cut_sandstone')
  }
  for (const [east, south] of [[5, 17], [13, 17], [15, 10]]) for (let up = 7; up <= 8; up++) put(east, up, south, 'orange_terracotta')
  return { structure, metadata: { name: 'room' } }
}
