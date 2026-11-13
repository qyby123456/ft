export default function build(api) {
  const size = [13, 11, 40]
  const structure = api.createStructure(size)
  const put = (position, name, properties) => api.setBlock(structure, position, `minecraft:${name}`, properties)
  const fill = (start, end, name, properties) => api.fill(structure, start, end, `minecraft:${name}`, properties)
  const ribs = [0, 7, 15, 23, 31, 39]
  const lamps = [4, 11, 19, 27, 35]
  const valves = [2, 8, 16, 24, 32, 37]
  const copper = ['waxed_weathered_cut_copper', 'waxed_oxidized_cut_copper', 'waxed_cut_copper']
  fill([0, 0, 0], [12, 10, 39], 'air')
  fill([0, 0, 0], [12, 0, 39], 'polished_deepslate')
  fill([0, 10, 0], [12, 10, 39], 'waxed_oxidized_cut_copper')
  for (let depth = 0; depth < size[2]; depth += 1) {
    for (let horizontal = 0; horizontal < size[0]; horizontal += 1) {
      const floor = horizontal >= 4 && horizontal <= 8
        ? ((depth + horizontal) % 7 === 0 ? 'waxed_exposed_cut_copper' : 'waxed_cut_copper')
        : copper[(Math.floor(depth / 2) + horizontal) % copper.length]
      put([horizontal, 1, depth], floor)
      put([horizontal, 9, depth], (horizontal + Math.floor(depth / 3)) % 4 === 0 ? 'waxed_cut_copper' : 'waxed_weathered_cut_copper')
    }
    for (const side of [0, 10]) {
      fill([side, 2, depth], [side + 2, 8, depth], 'waxed_weathered_cut_copper')
      for (let vertical = 2; vertical <= 8; vertical += 1) {
        put([side === 0 ? 2 : 10, vertical, depth], copper[(Math.floor(depth / 3) + vertical) % copper.length])
        put([side === 0 ? 0 : 12, vertical, depth], ribs.includes(depth) || vertical === 2 || vertical === 8
          ? 'waxed_cut_copper' : 'waxed_oxidized_cut_copper')
      }
    }
    if (depth > 0 && depth < 39) {
      for (const horizontal of [3, 9]) {
        put([horizontal, 7, depth], 'waxed_cut_copper_slab', { type: 'bottom', waterlogged: 'false' })
      }
    }
  }
  for (const depth of ribs) {
    for (const horizontal of [2, 10]) {
      fill([horizontal, 2, depth], [horizontal, 8, depth], 'waxed_cut_copper')
      put([horizontal, 2, depth], 'waxed_copper_block')
      put([horizontal, 8, depth], 'waxed_copper_block')
    }
    fill([2, 8, depth], [10, 8, depth], 'waxed_cut_copper')
    for (const horizontal of [3, 9]) {
      put([horizontal, 8, depth], 'waxed_copper_block')
    }
  }
  for (const depth of lamps) {
    for (const side of [2, 10]) {
      for (let vertical = 3; vertical <= 5; vertical += 1) {
        put([side, vertical, depth], 'orange_stained_glass')
        put([side === 2 ? 1 : 11, vertical, depth], 'shroomlight')
      }
      for (const offset of [-1, 0, 1]) {
        const horizontal = side === 2 ? 3 : 9
        put([horizontal, 0, depth + offset], 'shroomlight')
        put([horizontal, 1, depth + offset], 'orange_stained_glass')
      }
    }
  }
  for (const depth of valves) {
    for (const side of [2, 10]) {
      for (const vertical of [3, 4, 5]) {
        for (const offset of [-1, 0, 1]) {
          put([side, vertical, depth + offset], vertical === 4 && offset === 0
            ? 'smooth_quartz' : ((vertical + offset) % 2 === 0 ? 'gold_block' : 'waxed_copper_block'))
        }
      }
      put([side === 2 ? 3 : 9, 4, depth], 'polished_blackstone_button', {
        face: 'wall', facing: side === 2 ? 'east' : 'west', powered: 'false'
      })
    }
  }
  for (const depth of [5, 6, 13, 14, 21, 22, 29, 30, 37, 38]) {
    for (const horizontal of [5, 7]) {
      put([horizontal, 0, depth], 'waxed_oxidized_cut_copper')
      put([horizontal, 1, depth], 'iron_trapdoor', {
        half: 'top', facing: 'north', open: 'false', powered: 'false', waterlogged: 'false'
      })
    }
  }
  const cages = [[6, 7], [5, 15], [7, 23], [6, 31]]
  for (const [center, depth] of cages) {
    for (let horizontal = center - 1; horizontal <= center + 1; horizontal += 1) {
      for (let longitudinal = depth - 1; longitudinal <= depth + 1; longitudinal += 1) {
        for (const vertical of [5, 7]) {
          put([horizontal, vertical, longitudinal], 'iron_trapdoor', {
            half: vertical === 5 ? 'top' : 'bottom', facing: 'north', open: 'false', powered: 'false', waterlogged: 'false'
          })
        }
        if (horizontal !== center || longitudinal !== depth) {
          put([horizontal, 6, longitudinal], 'iron_bars', { waterlogged: 'false' })
        }
      }
    }
    put([center, 7, depth], 'iron_block')
    put([center, 6, depth], 'lantern', { hanging: 'true', waterlogged: 'false' })
    put([center, 8, depth], 'chain', { axis: 'y', waterlogged: 'false' })
  }
  for (const depth of [0, 39]) {
    fill([3, 2, depth], [9, 8, depth], 'waxed_cut_copper')
    fill([4, 2, depth], [8, 6, depth], 'air')
    for (const horizontal of [3, 9]) {
      fill([horizontal, 2, depth], [horizontal, 6, depth], 'waxed_copper_block')
      put([horizontal, 7, depth], 'gold_block')
    }
    fill([4, 7, depth], [8, 7, depth], 'waxed_exposed_cut_copper')
  }
  return {
    structure,
    metadata: {
      name: 'room', id: 'corridor-M20', theme: 'intact copper machinery',
      entrance: [6, 2, 0], exit: [6, 2, 39], interior: [6, 2, 3], dimensions: size,
      cageCenters: cages, lightPanels: lamps, gaugeStations: valves, dataVersion: 3465
    }
  }
}
