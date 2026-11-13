export default function build(api) {
  const size = [31, 22, 35]
  const structure = api.createStructure(size)
  const block = (east, up, south, name) => api.setBlock(structure, [east, up, south], `minecraft:${name}`)
  const box = (minimum, maximum, name) => api.fill(structure, minimum, maximum, `minecraft:${name}`)
  box([0, 0, 0], [30, 21, 34], 'water[level=0]')
  box([0, 0, 0], [30, 1, 34], 'dark_prismarine')
  for (let east = 0; east < 31; east++) for (let south = 0; south < 35; south++) block(east, 2, south, (east < 7 || east > 23) && (east * 7 + south * 11) % 17 < 6 ? 'sandstone' : east === 11 || east === 19 ? 'dark_prismarine' : 'prismarine_bricks')
  box([0, 3, 0], [1, 13, 30], 'prismarine_bricks')
  box([29, 3, 0], [30, 13, 30], 'prismarine_bricks')
  box([0, 3, 0], [30, 15, 1], 'prismarine_bricks')
  box([0, 3, 29], [30, 7, 30], 'prismarine_bricks')
  for (const east of [0, 27]) for (const south of [0, 27]) box([east, 3, south], [east + 3, 15, south + 3], 'prismarine_bricks')
  for (let east = 0; east < 31; east++) {
    const rise = 15 + (east >= 7 && east <= 23 ? 1 : 0) + (east >= 11 && east <= 19 ? 1 : 0)
    box([east, 14, 0], [east, rise, 2], 'prismarine')
  }
  box([12, 3, 29], [18, 11, 30], 'water[level=0]')
  box([12, 3, 26], [18, 4, 28], 'prismarine_bricks')
  box([12, 3, 25], [18, 3, 25], 'prismarine_bricks')
  for (let east = 12; east <= 18; east++) {
    block(east, 3, 29, 'prismarine_brick_stairs[facing=north,half=bottom,shape=straight,waterlogged=true]')
    block(east, 4, 28, 'prismarine_brick_stairs[facing=north,half=bottom,shape=straight,waterlogged=true]')
    block(east, 4, 25, 'prismarine_brick_stairs[facing=south,half=bottom,shape=straight,waterlogged=true]')
    block(east, 3, 24, 'prismarine_brick_stairs[facing=south,half=bottom,shape=straight,waterlogged=true]')
  }
  for (const east of [10, 19]) {
    box([east, 3, 26], [east + 1, 10, 29], 'prismarine_bricks')
    box([east, 10, 26], [east + 1, 10, 29], 'sea_lantern')
  }
  for (let up = 5; up <= 8; up++) for (let south = 26; south <= 28; south++) {
    block(11, up, south, 'warped_trapdoor[facing=east,half=bottom,open=true,powered=false,waterlogged=true]')
    block(19, up, south, 'warped_trapdoor[facing=west,half=bottom,open=true,powered=false,waterlogged=true]')
  }
  for (let east = 12; east <= 18; east++) {
    const ceiling = 14 - Math.max(0, Math.abs(east - 15) - 1)
    box([east, 3, 0], [east, ceiling - 1, 2], 'water[level=0]')
  }
  box([11, 5, 4], [19, 15, 5], 'prismarine_bricks')
  for (let east = 12; east <= 18; east++) {
    const ceiling = 14 - Math.max(0, Math.abs(east - 15) - 1)
    box([east, 5, 4], [east, ceiling - 1, 5], 'water[level=0]')
  }
  box([12, 3, 4], [18, 4, 6], 'prismarine_bricks')
  for (let east = 12; east <= 18; east++) {
    block(east, 3, 7, 'prismarine_brick_stairs[facing=north,half=bottom,shape=straight,waterlogged=true]')
    block(east, 4, 6, 'prismarine_brick_stairs[facing=north,half=bottom,shape=straight,waterlogged=true]')
    block(east, 3, 3, 'prismarine_brick_stairs[facing=south,half=bottom,shape=straight,waterlogged=true]')
  }
  for (const east of [9, 20]) {
    box([east, 3, 2], [east + 1, 14, 4], 'prismarine_bricks')
    box([east, 9, 4], [east + 1, 9, 4], 'sea_lantern')
  }
  for (const side of ['west', 'east']) for (const south of [8, 18]) {
    const east = side === 'west' ? 2 : 26
    box([east, 3, south], [east + 2, 4, south + 5], 'sandstone')
    box([east, 5, south], [east + 2, 10, south], 'prismarine')
    box([east, 5, south + 5], [east + 2, 10, south + 5], 'prismarine')
    box([east, 11, south], [east + 2, 11, south + 5], 'prismarine_bricks')
    block(side === 'west' ? 2 : 28, 8, south + 2, 'sea_lantern')
  }
  box([4, 3, 9], [5, 3, 10], 'dark_prismarine')
  box([25, 3, 9], [26, 3, 10], 'dark_prismarine')
  block(26, 3, 11, 'dark_prismarine')
  for (const [east, south] of [[4, 10], [5, 10], [25, 10], [26, 10], [26, 11]]) block(east, 4, south, 'barrel[facing=up,open=false]')
  for (const east of [5, 10]) box([east, 3, 3], [east, 8, 3], 'dark_prismarine')
  box([5, 6, 3], [10, 11, 3], 'dark_prismarine')
  for (let east = 6; east <= 9; east++) for (let up = 7; up <= 10; up++) block(east, up, 4, (east * 3 + up * 7) % 5 < 2 ? 'cyan_terracotta' : 'sandstone')
  box([24, 3, 16], [26, 3, 18], 'dark_prismarine')
  block(25, 4, 17, 'prismarine')
  block(25, 5, 17, 'sea_lantern')
  const coral = (east, up, south, type, height, branch) => {
    block(east, up - 1, south, 'prismarine')
    for (let rise = 0; rise < height; rise++) block(east, up + rise, south, `${type}_coral_block`)
    block(east, up + height, south, `${type}_coral[waterlogged=true]`)
    block(east + branch, up + height - 2, south, 'prismarine')
    block(east + branch, up + height - 1, south, `${type}_coral_block`)
    block(east + branch, up + height, south, `${type}_coral_fan[waterlogged=true]`)
  }
  for (const [east, south, type, branch] of [[8, 14, 'brain', -1], [23, 22, 'fire', 1], [4, 24, 'bubble', 1], [26, 24, 'brain', -1]]) {
    box([east - 1, 3, south - 1], [east + 1, 3, south + 1], 'prismarine')
    coral(east, 4, south, type, 2, branch)
  }
  for (const [east, south, up, type, branch] of [[4, 1, 16, 'brain', 1], [25, 1, 16, 'fire', -1], [1, 15, 14, 'fire', 1], [29, 15, 14, 'brain', -1], [4, 29, 8, 'brain', 1], [26, 29, 8, 'fire', -1]]) coral(east, up, south, type, 2, branch)
  for (const [east, south, height] of [[7, 7, 8], [23, 7, 8], [6, 16, 8], [27, 15, 7], [5, 25, 8], [25, 25, 8], [8, 32, 7], [22, 32, 8]]) {
    for (let up = 3; up < height; up++) block(east, up, south, 'kelp_plant')
    block(east, height, south, 'kelp[age=25]')
  }
  for (const [east, south] of [[6, 6], [24, 6], [8, 18], [22, 18], [5, 23], [25, 23], [9, 31], [21, 31]]) block(east, 3, south, 'sea_pickle[pickles=3,waterlogged=true]')
  for (const entry of structure.getBlocks()) {
    if (!entry.state.getName().toString().endsWith('_coral_block')) continue
    const wet = [[1, 0, 0], [-1, 0, 0], [0, 1, 0], [0, -1, 0], [0, 0, 1], [0, 0, -1]].some(offset => {
      const neighbor = structure.getBlock(entry.pos.map((coordinate, index) => coordinate + offset[index]))
      return neighbor && (neighbor.state.getName().toString() === 'minecraft:water' || neighbor.state.getProperties().waterlogged === 'true')
    })
    if (!wet) throw Error(`Coral missing water neighbor: ${entry.pos}`)
  }
  return {structure, metadata: {name: 's5-07-coral-gate-vestibule', reference: 'S5-07-entrance.png', size, ports: [{anchor: [15, 3, 34], outward: 'south', width: 7}, {anchor: [15, 3, 0], outward: 'north', width: 7}], gate: {sideOpenWaterloggedPanels: [[11, 5, 26], [19, 5, 26]], centralClearance: '7 wide, feetY5 at raised landing'}, mapSubstitute: 'Sandstone/cyan terracotta mosaic on dark-prismarine frame; not map entity', brazierSubstitute: 'Sea-lantern pedestal, no fire', storage: 'Five native empty barrels', route: 'Source-water swimming path rises over real waterlogged prismarine stairs and descends to Y3 at both external wet interfaces'}}
}
