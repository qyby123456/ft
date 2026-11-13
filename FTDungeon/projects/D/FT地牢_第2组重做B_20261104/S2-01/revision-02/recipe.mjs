import original from '../build-room.mjs'

export default function build(api) {
  const structure = api.createStructure([25, 18, 31])
  api.fill(structure, [0, 0, 0], [24, 17, 30], 'minecraft:air')
  const translate = point => [point[0], point[1] + 3, point[2] + 6]
  original({ ...api, createStructure: () => structure, setBlock: (target, point, ...rest) => api.setBlock(target, translate(point), ...rest), fill: (target, first, last, ...rest) => api.fill(target, translate(first), translate(last), ...rest) })
  const put = (east, up, south, block, properties) => api.setBlock(structure, [east, up, south], `minecraft:${block}`, properties)
  const fill = (first, last, block) => api.fill(structure, first, last, `minecraft:${block}`)
  fill([0, 0, 6], [24, 2, 30], 'sandstone')
  fill([8, 0, 0], [16, 0, 5], 'cut_sandstone')
  for (let east = 9; east <= 15; east++) for (let step = 0; step < 3; step++) {
    const south = 2 + step
    if (step > 0) fill([east, 1, south], [east, step, south], 'cut_sandstone')
    put(east, step + 1, south, 'smooth_sandstone_stairs', { facing: 'south', half: 'bottom', shape: 'straight', waterlogged: 'false' })
  }
  fill([9, 1, 5], [15, 3, 5], 'cut_sandstone')
  for (const east of [7, 17]) {
    fill([east, 0, 1], [east, 4, 5], 'cut_sandstone')
    put(east, 5, 5, 'chiseled_sandstone')
    put(east, 6, 5, 'lantern', { hanging: 'false', waterlogged: 'false' })
  }
  const local = (side, along, inward) => side === 'north' ? [along, 6 + inward] : side === 'south' ? [24 - along, 30 - inward] : side === 'west' ? [inward, 30 - along] : [24 - inward, 6 + along]
  for (const side of ['north', 'south', 'west', 'east']) {
    for (let along = 0; along <= 24; along++) {
      const [east, south] = local(side, along, 1)
      if (Math.abs(along - 12) > 4) for (let up = 4; up <= 13; up++) put(east, up, south, up === 7 ? 'orange_terracotta' : 'sandstone')
      for (const [up, inward] of [[13, 1], [14, 2], [15, 2]]) {
        const [ledgeEast, ledgeSouth] = local(side, along, inward)
        put(ledgeEast, up, ledgeSouth, up === 14 ? 'smooth_sandstone' : 'cut_sandstone')
      }
    }
    for (const along of [3, 8, 16, 21]) {
      for (const offset of [-1, 0, 1]) {
        const [east, south] = local(side, along + offset, 2)
        for (const up of [4, 5, 12, 13]) put(east, up, south, up === 12 ? 'chiseled_sandstone' : 'cut_sandstone')
      }
      const [east, south] = local(side, along, 2)
      for (let up = 6; up <= 11; up++) put(east, up, south, up === 7 || up === 10 ? 'orange_terracotta' : 'chiseled_sandstone')
      const [lightEast, lightSouth] = local(side, along, 3)
      put(lightEast, 7, lightSouth, 'cut_sandstone')
      put(lightEast, 8, lightSouth, 'lantern', { hanging: 'false', waterlogged: 'false' })
    }
    for (let offset = -4; offset <= 4; offset++) for (let up = 4; up <= 12; up++) {
      const opening = up <= 7 ? 2 : up <= 9 ? 1 : up === 10 ? 0 : -1
      const [east, south] = local(side, 12 + offset, 2)
      put(east, up, south, Math.abs(offset) <= opening ? 'air' : up === 8 ? 'orange_terracotta' : 'smooth_sandstone')
    }
  }
  for (let along = 0; along <= 24; along++) for (const side of ['north', 'south', 'west', 'east']) {
    const [east, south] = local(side, along, 0)
    if (along % 6 <= 2) put(east, 17, south, 'cut_sandstone')
    if ([3, 8, 16, 21].includes(along)) for (let up = 4; up <= 15; up++) put(east, up, south, up === 7 || up === 11 ? 'orange_terracotta' : 'cut_sandstone')
  }
  return { structure, metadata: { name: 'room' } }
}
