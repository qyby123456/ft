export default function build(api) {
  const size = [31, 18, 31]
  const structure = api.createStructure(size)
  const block = (east, up, south, name) => api.setBlock(structure, [east, up, south], `minecraft:${name}`)
  const box = (minimum, maximum, name) => api.fill(structure, minimum, maximum, `minecraft:${name}`)
  box([0, 0, 0], [30, 17, 30], 'water[level=0]')
  box([0, 0, 0], [30, 1, 30], 'dark_prismarine')
  for (let east = 0; east < 31; east++) for (let south = 0; south < 31; south++) {
    const corridor = east >= 19 || south >= 19
    const border = south >= 19 && (south === 21 || south === 27) || east >= 19 && (east === 21 || east === 27)
    block(east, 2, south, !corridor ? 'prismarine' : border ? 'dark_prismarine' : (east * 7 + south * 13) % 19 < 4 ? 'sandstone' : 'prismarine_bricks')
  }
  box([0, 3, 17], [18, 12, 18], 'prismarine_bricks')
  box([17, 3, 0], [18, 12, 18], 'prismarine_bricks')
  box([0, 3, 29], [30, 12, 30], 'prismarine_bricks')
  box([29, 3, 0], [30, 12, 30], 'prismarine_bricks')
  box([0, 12, 17], [30, 12, 30], 'prismarine_bricks')
  box([17, 12, 0], [30, 12, 30], 'prismarine_bricks')
  box([4, 12, 22], [8, 12, 26], 'water[level=0]')
  box([22, 12, 4], [26, 12, 8], 'water[level=0]')
  box([22, 12, 22], [26, 12, 26], 'water[level=0]')
  for (const along of [3, 11]) {
    for (const south of [19, 28]) {
      box([along, 3, south], [along + 1, 10, south], 'prismarine_bricks')
      box([along, 7, south], [along + 1, 7, south], 'sea_lantern')
      box([along, 11, 19], [along + 1, 11, 21], 'prismarine')
      box([along, 11, 27], [along + 1, 11, 28], 'prismarine')
    }
    for (const east of [19, 28]) {
      box([east, 3, along], [east, 10, along + 1], 'prismarine_bricks')
      box([east, 7, along], [east, 7, along + 1], 'sea_lantern')
      box([19, 11, along], [21, 11, along + 1], 'prismarine')
      box([27, 11, along], [28, 11, along + 1], 'prismarine')
    }
  }
  box([17, 3, 17], [19, 14, 19], 'prismarine_bricks')
  box([17, 7, 19], [19, 7, 19], 'sea_lantern')
  box([19, 7, 17], [19, 7, 19], 'sea_lantern')
  box([16, 11, 16], [20, 11, 20], 'prismarine')
  box([16, 14, 16], [20, 14, 20], 'prismarine_bricks')
  for (const side of ['west', 'north']) {
    const position = (across, up, depth) => side === 'west' ? [depth, up, 24 + across] : [24 + across, up, depth]
    for (let depth = 0; depth < 2; depth++) for (let across = -6; across <= 6; across++) {
      for (let up = 3; up <= 12; up++) {
        const ceiling = 10 - Math.max(0, Math.abs(across) - 1)
        api.setBlock(structure, position(across, up, depth), Math.abs(across) <= 2 && up < ceiling ? 'minecraft:water[level=0]' : 'minecraft:prismarine_bricks')
      }
      api.setBlock(structure, position(across, 2, depth), 'minecraft:prismarine_bricks')
    }
    api.setBlock(structure, position(0, 10, 0), 'minecraft:sea_lantern')
  }
  const coral = (east, up, south, type, height, branchEast, branchSouth) => {
    block(east, up - 1, south, 'prismarine')
    for (let rise = 0; rise < height; rise++) block(east, up + rise, south, `${type}_coral_block`)
    block(east, up + height, south, `${type}_coral[waterlogged=true]`)
    block(east + branchEast, up + height - 2, south + branchSouth, 'prismarine')
    block(east + branchEast, up + height - 1, south + branchSouth, `${type}_coral_block`)
    block(east + branchEast, up + height, south + branchSouth, `${type}_coral_fan[waterlogged=true]`)
  }
  for (const [index, along] of [7, 15].entries()) {
    for (const south of [19, 27]) {
      box([along - 1, 3, south], [along + 1, 4, south + 1], 'prismarine')
      coral(along, 5, south, south === 19 ? 'brain' : 'fire', 2 + index, 1, 0)
      block(along - 1, 5, south + 1, 'sea_pickle[pickles=2,waterlogged=true]')
    }
    for (const east of [19, 27]) {
      box([east, 3, along - 1], [east + 1, 4, along + 1], 'prismarine')
      coral(east, 5, along, east === 19 ? 'bubble' : 'brain', 2 + index, 0, 1)
      block(east + 1, 5, along - 1, 'sea_pickle[pickles=2,waterlogged=true]')
    }
  }
  box([17, 3, 20], [20, 4, 21], 'prismarine_bricks')
  box([20, 3, 17], [21, 4, 20], 'prismarine_bricks')
  coral(18, 5, 20, 'brain', 2, 1, 0)
  coral(20, 5, 18, 'fire', 2, 0, 1)
  block(20, 5, 20, 'sea_pickle[pickles=4,waterlogged=true]')
  coral(18, 15, 18, 'brain', 1, 1, 0)
  for (const [east, south, height] of [[9, 20, 8], [13, 28, 8], [20, 9, 9], [28, 13, 8], [22, 20, 7], [20, 22, 8]]) {
    for (let up = 3; up < height; up++) block(east, up, south, 'kelp_plant')
    block(east, height, south, 'kelp[age=25]')
  }
  for (const [east, south] of [[5, 21], [10, 27], [21, 5], [27, 10], [25, 27], [27, 25]]) block(east, 3, south, 'seagrass')
  for (const entry of structure.getBlocks()) {
    if (!entry.state.getName().toString().endsWith('_coral_block')) continue
    const wet = [[1, 0, 0], [-1, 0, 0], [0, 1, 0], [0, -1, 0], [0, 0, 1], [0, 0, -1]].some(offset => {
      const neighbor = structure.getBlock(entry.pos.map((coordinate, index) => coordinate + offset[index]))
      return neighbor && (neighbor.state.getName().toString() === 'minecraft:water' || neighbor.state.getProperties().waterlogged === 'true')
    })
    if (!wet) throw Error(`Coral missing water neighbor: ${entry.pos}`)
  }
  return {structure, metadata: {name: 's5-06-coral-lantern-corner', reference: 'S5-06-corner.png', size, ports: [{anchor: [0, 3, 24], outward: 'west', width: 5}, {anchor: [24, 3, 0], outward: 'north', width: 5}], turn: [24, 3, 24], cornerPier: [18, 7, 18], approximation: 'Two perpendicular covered arms around an opaque inner corner, corner lantern faces and coral niches, rooted kelp replacing unsupported hanging aquatic plants'}}
}
