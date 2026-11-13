export default function build(api) {
  const structure = api.createStructure([15, 12, 41])
  const put = (east, up, south, block, properties) => api.setBlock(structure, [east, up, south], `minecraft:${block}`, properties)
  const fill = (first, last, block) => api.fill(structure, first, last, `minecraft:${block}`)
  fill([0, 0, 0], [14, 11, 40], 'air')
  fill([0, 0, 0], [14, 1, 40], 'sandstone')
  for (let east = 3; east <= 11; east++) for (let south = 0; south <= 40; south++) {
    const tile = east === 5 || east === 9 ? south % 4 <= 1 : east === 7 && south % 6 <= 2
    put(east, 1, south, tile ? south % 6 === 1 && east === 7 ? 'chiseled_red_sandstone' : 'orange_terracotta' : (east + south * 3) % 17 === 0 ? 'cut_sandstone' : 'smooth_sandstone')
  }
  for (let up = 2; up <= 9; up++) {
    const wall = up === 6 ? 'orange_terracotta' : up === 7 ? 'chiseled_sandstone' : 'sandstone'
    fill([0, up, 0], [2, up, 40], wall)
    fill([12, up, 0], [14, up, 40], wall)
  }
  fill([0, 10, 0], [14, 10, 40], 'cut_sandstone')
  for (let south = 0; south <= 40; south++) {
    for (const east of [3, 11]) put(east, 9, south, 'smooth_sandstone')
    for (const east of [5, 9]) if (south % 8 < 3) put(east, 10, south, 'orange_terracotta')
  }
  for (const south of [2, 10, 18, 26, 34, 38]) {
    for (const east of [3, 11]) {
      for (let up = 2; up <= 9; up++) put(east, up, south, up === 5 ? 'chiseled_sandstone' : up === 6 ? 'chiseled_red_sandstone' : 'cut_sandstone')
      put(east === 3 ? 4 : 10, 8, south, 'smooth_sandstone')
    }
    fill([3, 9, south], [11, 9, south], 'cut_sandstone')
  }
  for (const south of [0, 40]) for (let east = 3; east <= 11; east++) for (let up = 2; up <= 9; up++) {
    const radius = up <= 5 ? 2 : up <= 7 ? 1 : up === 8 ? 0 : -1
    put(east, up, south, Math.abs(east - 7) <= radius ? 'air' : up === 7 ? 'orange_terracotta' : 'smooth_sandstone')
  }
  for (const south of [6, 19, 32]) {
    put(7, 7, south, 'lantern', { hanging: 'true', waterlogged: 'false' })
    for (const up of [8, 9]) put(7, up, south, 'chain', { axis: 'y', waterlogged: 'false' })
  }
  for (const [east, south] of [[4, 5], [10, 13], [4, 21], [10, 29]]) {
    put(east, 2, south, 'terracotta')
    put(east, 3, south, 'flower_pot')
  }
  for (const [east, south] of [[4, 10], [10, 17], [4, 30], [10, 36]]) {
    put(east, 1, south, 'sand')
    put(east, 2, south, 'dead_bush')
  }
  for (const [east, south] of [[3, 7], [11, 15], [3, 23], [11, 33]]) {
    fill([east, 2, south], [east, 3, south+1], 'sandstone')
    put(east, 4, south, 'sand')
    const inner = east === 3 ? 4 : 10
    put(inner, 2, south, 'smooth_sandstone_slab', { type: 'bottom', waterlogged: 'false' })
    put(inner, 2, south+1, 'sandstone')
    put(inner, 3, south+1, 'sand')
  }
  for (const [east, south] of [[4, 16], [10, 24], [4, 35]]) put(east, 2, south, 'smooth_sandstone_slab', { type: 'bottom', waterlogged: 'false' })
  fill([3, 10, 5], [4, 10, 7], 'air')
  fill([10, 10, 29], [11, 10, 31], 'air')
  for (let south = 0; south <= 40; south++) if (south % 8 === 2) for (const east of [1, 13]) put(east, 11, south, 'cut_sandstone')
  return { structure, metadata: { name: 'room' } }
}
