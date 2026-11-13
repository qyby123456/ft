export default function build(api) {
  const structure = api.createStructure([25, 14, 25])
  const put = (east, up, south, block, properties) => api.setBlock(structure, [east, up, south], `minecraft:${block}`, properties)
  const volume = (first, last, block) => api.fill(structure, first, last, `minecraft:${block}`)
  volume([0, 0, 0], [24, 13, 24], 'air')
  for (let east = 0; east <= 24; east++) for (let south = 0; south <= 24; south++) {
    const distance = Math.max(Math.abs(east - 12), Math.abs(south - 12))
    const motif = distance === 10 && (east + south) % 3 !== 0 || distance > 6 && (Math.abs(east - 12) <= 1 || Math.abs(south - 12) <= 1)
    put(east, 0, south, motif ? 'orange_terracotta' : (east * 5 + south * 3) % 17 === 0 ? 'sandstone' : 'smooth_sandstone')
    put(east, 13, south, 'cut_sandstone')
    if (east === 0 || east === 24 || south === 0 || south === 24) for (let up = 1; up <= 12; up++) put(east, up, south, up === 4 || up === 10 ? 'orange_terracotta' : (east + south + up) % 8 === 0 ? 'cut_sandstone' : 'sandstone')
  }
  const local = (side, along, inward) => side === 'front' ? [along, inward] : side === 'back' ? [24 - along, 24 - inward] : side === 'left' ? [inward, 24 - along] : [24 - inward, along]
  for (const side of ['front', 'back', 'left', 'right']) {
    for (const along of [3, 8, 16, 21]) {
      const [east, south] = local(side, along, 1)
      for (let up = 1; up <= 11; up++) put(east, up, south, up === 4 || up === 7 ? 'chiseled_sandstone' : up === 5 ? 'orange_terracotta' : 'cut_sandstone')
      const [lampEast, lampSouth] = local(side, along, 2)
      put(lampEast, 4, lampSouth, 'cut_sandstone')
      put(lampEast, 5, lampSouth, 'lantern', { hanging: 'false', waterlogged: 'false' })
    }
    for (let along = 1; along <= 23; along++) for (const up of [11, 12]) {
      const [east, south] = local(side, along, 1)
      put(east, up, south, up === 11 ? 'smooth_sandstone' : 'cut_sandstone')
    }
    for (let offset = -4; offset <= 4; offset++) for (let up = 1; up <= 9; up++) for (const inward of [0, 1]) {
      const [east, south] = local(side, 12 + offset, inward)
      const opening = up <= 4 ? 2 : up <= 6 ? 1 : up === 7 ? 0 : -1
      put(east, up, south, Math.abs(offset) <= opening ? 'air' : up === 5 ? 'orange_terracotta' : Math.abs(offset) === opening + 1 ? 'smooth_sandstone' : 'chiseled_sandstone')
    }
  }
  volume([7, 1, 7], [17, 1, 17], 'cut_sandstone')
  volume([8, 2, 8], [16, 2, 16], 'smooth_sandstone')
  volume([9, 3, 9], [15, 3, 15], 'cut_sandstone')
  for (let east = 9; east <= 15; east++) for (let south = 9; south <= 15; south++) {
    const radius = Math.max(Math.abs(east - 12), Math.abs(south - 12))
    put(east, 3, south, radius === 1 || radius === 3 && (east + south) % 2 === 0 ? 'orange_terracotta' : 'smooth_sandstone')
  }
  put(12, 3, 12, 'chiseled_sandstone')
  for (const approach of ['north', 'south']) for (let east = 10; east <= 14; east++) for (let step = 0; step < 3; step++) {
    const south = approach === 'north' ? 6 + step : 18 - step
    if (step > 0) volume([east, 1, south], [east, step, south], 'cut_sandstone')
    put(east, step + 1, south, 'smooth_sandstone_stairs', { facing: approach === 'north' ? 'south' : 'north', half: 'bottom', shape: 'straight', waterlogged: 'false' })
  }
  for (const east of [8, 16]) for (const south of [8, 16]) {
    put(east, 3, south, 'chiseled_sandstone')
    put(east, 4, south, 'cut_sandstone')
    put(east, 5, south, 'lantern', { hanging: 'false', waterlogged: 'false' })
  }
  for (const [east, south] of [[2, 6], [22, 18], [6, 22], [18, 2], [3, 18]]) {
    put(east, 1, south, 'terracotta')
    put(east, 2, south, 'flower_pot')
  }
  for (const [east, south] of [[2, 19], [22, 5], [5, 2], [19, 22]]) {
    put(east, 1, south, 'sand')
    put(east, 2, south, 'dead_bush')
  }
  return { structure, metadata: { name: 'room' } }
}
