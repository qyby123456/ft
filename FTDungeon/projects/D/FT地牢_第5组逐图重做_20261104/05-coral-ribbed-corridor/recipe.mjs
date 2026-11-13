export default function build(api) {
  const size = [17, 18, 39]
  const structure = api.createStructure(size)
  const block = (east, up, south, name) => api.setBlock(structure, [east, up, south], `minecraft:${name}`)
  const box = (minimum, maximum, name) => api.fill(structure, minimum, maximum, `minecraft:${name}`)
  box([0, 0, 0], [16, 17, 38], 'water[level=0]')
  box([0, 0, 0], [16, 1, 38], 'dark_prismarine')
  for (let east = 0; east < 17; east++) for (let south = 0; south < 39; south++) {
    const tile = east === 5 || east === 11 ? 'dark_prismarine' : (east <= 4 || east >= 12) && (east * 7 + south * 3) % 11 < 4 ? 'sandstone' : (east + south * 3) % 17 === 0 ? 'prismarine' : 'prismarine_bricks'
    block(east, 2, south, tile)
  }
  for (const east of [0, 15]) {
    box([east, 3, 0], [east + 1, 4, 38], 'prismarine_bricks')
    box([east, 10, 0], [east + 1, 12, 38], 'prismarine_bricks')
  }
  for (const [minimum, maximum] of [[[0, 11, 0], [4, 12, 38]], [[12, 11, 0], [16, 12, 38]]]) box(minimum, maximum, 'prismarine_bricks')
  for (let south = 0; south < 39; south++) {
    const skylight = south >= 4 && south <= 8 || south >= 20 && south <= 24 || south >= 36 && south <= 36
    if (!skylight) box([5, 12, south], [11, 12, south], 'prismarine_bricks')
  }
  for (const south of [2, 10, 18, 26, 34]) {
    for (const east of [2, 13]) {
      box([east, 3, south], [east + 1, 9, south + 1], 'prismarine_bricks')
      box([east, 7, south], [east + 1, 7, south + 1], 'sea_lantern')
      box([east - 1, 10, south], [east + 2, 10, south + 1], 'prismarine')
    }
    box([4, 10, south], [5, 11, south + 1], 'prismarine_bricks')
    box([11, 10, south], [12, 11, south + 1], 'prismarine_bricks')
    box([6, 11, south], [10, 11, south + 1], 'prismarine_bricks')
    block(8, 11, south + 1, 'sea_lantern')
  }
  for (const south of [0, 37]) {
    box([0, 3, south], [16, 12, south + 1], 'prismarine_bricks')
    for (let east = 6; east <= 10; east++) {
      const ceiling = Math.abs(east - 8) === 2 ? 8 : Math.abs(east - 8) === 1 ? 9 : 10
      box([east, 3, south], [east, ceiling - 1, south + 1], 'water[level=0]')
    }
    block(8, 10, south, 'sea_lantern')
  }
  const coral = (east, up, south, type, height, branch) => {
    block(east, up - 1, south, 'prismarine')
    for (let rise = 0; rise < height; rise++) block(east, up + rise, south, `${type}_coral_block`)
    block(east, up + height, south, `${type}_coral[waterlogged=true]`)
    block(east + branch, up + height - 2, south, 'prismarine')
    block(east + branch, up + height - 1, south, `${type}_coral_block`)
    block(east + branch, up + height, south, `${type}_coral_fan[waterlogged=true]`)
  }
  for (const [index, south] of [6, 14, 22, 30].entries()) {
    for (const east of [2, 12]) {
      box([east, 3, south - 1], [east + 2, 3, south + 1], 'prismarine_bricks')
      box([east, 4, south - 1], [east + 2, 4, south + 1], 'sandstone')
      coral(east === 2 ? 2 : 14, 5, south, index % 2 === 0 ? east === 2 ? 'brain' : 'fire' : east === 2 ? 'bubble' : 'brain', index % 2 + 2, east === 2 ? 1 : -1)
      block(east + 1, 5, south + 1, 'sea_pickle[pickles=2,waterlogged=true]')
    }
    for (const east of [4, 12]) {
      const kelpSouth = south + 2
      const height = 8 + index % 2
      for (let up = 3; up < height; up++) block(east, up, kelpSouth, 'kelp_plant')
      block(east, height, kelpSouth, 'kelp[age=25]')
    }
    block(5, 3, south - 2, 'seagrass')
    block(11, 3, south + 2, 'seagrass')
  }
  for (const [east, south, type] of [[2, 5, 'brain'], [14, 21, 'fire'], [2, 29, 'bubble']]) coral(east, 13, south, type, 2, east < 8 ? 1 : -1)
  for (const entry of structure.getBlocks()) {
    if (!entry.state.getName().toString().endsWith('_coral_block')) continue
    const wet = [[1, 0, 0], [-1, 0, 0], [0, 1, 0], [0, -1, 0], [0, 0, 1], [0, 0, -1]].some(offset => {
      const neighbor = structure.getBlock(entry.pos.map((coordinate, index) => coordinate + offset[index]))
      return neighbor && (neighbor.state.getName().toString() === 'minecraft:water' || neighbor.state.getProperties().waterlogged === 'true')
    })
    if (!wet) throw Error(`Coral missing water neighbor: ${entry.pos}`)
  }
  return {structure, metadata: {name: 's5-05-coral-ribbed-corridor', reference: 'S5-05-corridor.png', size, ports: [{anchor: [8, 3, 0], outward: 'north', width: 5}, {anchor: [8, 3, 38], outward: 'south', width: 5}], ribs: [2, 10, 18, 26, 34], approximation: 'Five repeated attached pier pairs and roof ribs, source-water skylights, two axial wet gates, real rooted kelp rather than unsupported hanging aquatic plants'}}
}
