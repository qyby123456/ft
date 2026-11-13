export default function build(api) {
  const structure = api.createStructure([23, 14, 25])
  const put = (east, up, south, block, properties, entity) => api.setBlock(structure, [east, up, south], `minecraft:${block}`, properties, entity)
  const fill = (first, last, block) => api.fill(structure, first, last, `minecraft:${block}`)
  fill([0, 0, 0], [22, 13, 24], 'air')
  fill([0, 0, 4], [22, 1, 24], 'sandstone')
  for (let east = 0; east <= 22; east++) for (let south = 4; south <= 24; south++) {
    const ring = east === 5 || east === 17 || south === 8 || south === 20
    put(east, 1, south, ring && (east + south) % 4 !== 0 ? 'orange_terracotta' : (east + south * 7) % 17 === 0 ? 'cut_sandstone' : 'smooth_sandstone')
    put(east, 12, south, 'cut_sandstone')
    if (east <= 1 || east >= 21 || south >= 23 || south <= 5) for (let up = 2; up <= 11; up++) put(east, up, south, up === 5 || up === 9 ? 'orange_terracotta' : up === 6 ? 'chiseled_sandstone' : 'sandstone')
  }
  fill([7, 0, 0], [15, 0, 3], 'cut_sandstone')
  for (let east = 8; east <= 14; east++) put(east, 1, 2, 'smooth_sandstone_stairs', { facing: 'south', half: 'bottom', shape: 'straight', waterlogged: 'false' })
  fill([8, 1, 3], [14, 1, 3], 'smooth_sandstone')
  for (const east of [6, 16]) fill([east, 0, 1], [east, 2, 3], 'cut_sandstone')
  for (const side of ['north', 'south', 'west', 'east']) {
    const point = (along, inset) => side === 'north' ? [along, 4 + inset] : side === 'south' ? [along, 24 - inset] : side === 'west' ? [inset, 4 + along] : [22 - inset, 4 + along]
    const length = side === 'north' || side === 'south' ? 22 : 20
    for (const along of [3, 7, length - 7, length - 3]) {
      for (const inset of [1, 2]) {
        const [east, south] = point(along, inset)
        for (let up = 2; up <= 10; up++) put(east, up, south, up === 4 || up === 8 ? 'orange_terracotta' : up === 5 || up === 9 ? 'chiseled_sandstone' : 'cut_sandstone')
      }
      for (const offset of [-1, 0, 1]) {
        const [east, south] = point(along + offset, 2)
        put(east, 2, south, 'cut_sandstone')
        put(east, 10, south, 'chiseled_sandstone')
      }
    }
    for (let along = 0; along <= length; along++) for (const [up, inset] of [[10, 1], [11, 2]]) {
      const [east, south] = point(along, inset)
      put(east, up, south, 'cut_sandstone')
    }
  }
  const opening = (side, center, feet, outer) => {
    for (let offset = -3; offset <= 3; offset++) for (let up = feet; up <= feet + 6; up++) for (let inset = 0; inset <= 2; inset++) {
      const east = side === 'east' ? 22 - inset : center + offset
      const south = side === 'north' ? 4 + inset : side === 'south' ? 24 - inset : center + offset
      const half = up < feet + 3 ? 1 : up < feet + 5 ? 0 : -1
      put(east, up, south, Math.abs(offset) <= half ? 'air' : up === feet + 4 ? 'orange_terracotta' : 'smooth_sandstone')
    }
    if (outer) {
      if (side === 'south') {
        fill([center - 1, 2, 23], [center + 1, 2, 24], 'cut_sandstone')
        for (let east = center - 1; east <= center + 1; east++) put(east, 2, 22, 'smooth_sandstone_stairs', { facing: 'south', half: 'bottom', shape: 'straight', waterlogged: 'false' })
      } else {
        fill([21, 2, center - 1], [22, 2, center + 1], 'cut_sandstone')
        for (let south = center - 1; south <= center + 1; south++) put(20, 2, south, 'smooth_sandstone_stairs', { facing: 'east', half: 'bottom', shape: 'straight', waterlogged: 'false' })
      }
    }
  }
  opening('north', 11, 2, false)
  opening('south', 11, 3, true)
  opening('east', 15, 3, true)
  fill([1, 2, 16], [3, 7, 20], 'smooth_sandstone')
  fill([1, 2, 17], [3, 5, 19], 'air')
  for (const [east, up, south] of [[1, 2, 17], [2, 2, 17], [1, 3, 17], [1, 2, 19]]) put(east, up, south, 'barrel', { facing: 'north', open: 'false' }, api.createBlockNbt({ id: 'minecraft:barrel' }))
  fill([16, 2, 22], [19, 4, 22], 'bookshelf')
  for (const east of [16, 18]) put(east, 2, 21, 'chest', { facing: 'north', type: 'single', waterlogged: 'false' }, api.createBlockNbt({ id: 'minecraft:chest' }))
  for (const [east, south] of [[6, 21], [15, 21], [19, 8], [4, 9]]) {
    put(east, 2, south, 'terracotta')
    put(east, 3, south, 'flower_pot')
  }
  for (const [east, south] of [[4, 6], [18, 19]]) {
    put(east, 2, south, 'sand')
    put(east, 3, south, 'dead_bush')
  }
  fill([7, 2, 10], [15, 2, 16], 'cut_sandstone')
  for (let east = 8; east <= 14; east++) for (let south = 11; south <= 15; south++) {
    const edge = east === 8 || east === 14 || south === 11 || south === 15
    put(east, 3, south, edge ? (east + south) % 3 === 0 ? 'orange_terracotta' : 'chiseled_sandstone' : 'water', edge ? undefined : { level: '0' })
  }
  for (const [east, south] of [[6, 20], [15, 20], [19, 11]]) {
    if (south === 20) put(east, 11, south, 'chain', { axis: 'y', waterlogged: 'false' })
    put(east, 10, south, 'cut_sandstone')
    put(east, 9, south, 'chain', { axis: 'y', waterlogged: 'false' })
    put(east, 8, south, 'lantern', { hanging: 'true', waterlogged: 'false' })
  }
  for (let east = 0; east <= 22; east++) for (let south = 4; south <= 24; south++) if ((east === 0 || east === 22 || south === 4 || south === 24) && (east + south) % 5 <= 1) put(east, 13, south, 'cut_sandstone')
  return { structure, metadata: { name: 'room' } }
}
