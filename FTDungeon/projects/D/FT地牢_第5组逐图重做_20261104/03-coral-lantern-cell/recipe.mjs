export default function build(api) {
  const structure = api.createStructure([19, 16, 19])
  const block = (east, up, south, name) => api.setBlock(structure, [east, up, south], `minecraft:${name}`)
  const box = (minimum, maximum, name) => api.fill(structure, minimum, maximum, `minecraft:${name}`)
  box([0, 0, 0], [18, 15, 18], 'water[level=0]')
  box([0, 0, 0], [18, 1, 18], 'dark_prismarine')
  for (let east = 0; east < 19; east++) for (let south = 0; south < 19; south++) {
    const sand = (east === 5 && south >= 6 && south <= 14) || (south === 13 && east >= 5 && east <= 13)
    block(east, 2, south, sand ? 'sandstone' : (east * 17 + south * 7) % 13 < 3 ? 'prismarine' : 'prismarine_bricks')
  }
  for (let up = 3; up <= 11; up++) {
    for (let east = 0; east < 19; east++) for (const south of [0, 1, 2]) {
      const top = east < 4 ? 11 : east < 13 ? 10 : 9
      if (up <= top) block(east, up, south, (east * 3 + up * 11) % 17 < 4 ? 'prismarine' : 'prismarine_bricks')
    }
    for (let south = 0; south < 19; south++) {
      const top = south <= 3 ? 11 : south <= 12 ? 9 : 6
      if (up <= top) for (const east of [0, 1, 17, 18]) block(east, up, south, (south * 5 + up * 3) % 19 < 4 ? 'prismarine' : 'prismarine_bricks')
    }
    if (up <= 4) box([0, up, 17], [18, up, 18], 'prismarine_bricks')
  }
  for (const [east, south, height] of [[1, 1, 11], [0, 4, 10], [0, 10, 10], [16, 4, 10], [16, 13, 8]]) {
    box([east, 3, south], [east + 2, height, south + 1], 'prismarine_bricks')
    box([east, height, south], [east + 2, height, south + 1], 'prismarine')
    block(east + 1, Math.min(7, height - 1), south + 1, 'sea_lantern')
  }
  for (let depth = 0; depth <= 2; depth++) for (let south = 5; south <= 11; south++) {
    const offset = Math.abs(south - 8)
    for (let up = 3; up <= 8; up++) block(depth, up, south, offset === 3 || up >= 8 - Math.max(0, offset - 1) ? 'prismarine_bricks' : 'water[level=0]')
    block(depth, 2, south, 'prismarine_bricks')
  }
  for (let depth = 0; depth <= 2; depth++) for (let south = 9; south <= 13; south++) {
    const offset = Math.abs(south - 11)
    for (let up = 3; up <= 7; up++) block(18 - depth, up, south, offset === 2 || up >= 7 - Math.max(0, offset - 1) ? 'prismarine_bricks' : 'water[level=0]')
    block(18 - depth, 2, south, 'prismarine_bricks')
  }
  box([5, 5, 0], [13, 8, 0], 'dark_prismarine')
  box([5, 5, 1], [13, 8, 3], 'water[level=0]')
  box([5, 4, 1], [13, 4, 3], 'prismarine_bricks')
  box([5, 9, 1], [13, 9, 3], 'prismarine_bricks')
  box([4, 4, 1], [4, 9, 3], 'prismarine_bricks')
  box([14, 4, 1], [14, 9, 3], 'prismarine_bricks')
  box([9, 5, 1], [9, 8, 2], 'prismarine_bricks')
  block(9, 7, 3, 'prismarine')
  box([5, 5, 3], [8, 5, 3], 'sandstone')
  box([10, 4, 3], [13, 4, 3], 'sandstone')
  api.setBlock(structure, [12, 5, 2], 'minecraft:chest', {facing: 'south', type: 'single', waterlogged: 'true'}, api.createBlockNbt({id: 'minecraft:chest', Items: []}))
  box([8, 3, 9], [10, 3, 11], 'prismarine_bricks')
  block(9, 4, 10, 'prismarine')
  block(9, 5, 10, 'sea_lantern')
  box([2, 3, 14], [4, 3, 15], 'sandstone')
  box([14, 3, 15], [16, 3, 16], 'sandstone')
  const sprig = (east, up, south, type, height, branchEast, branchSouth) => {
    for (let rise = 0; rise < height; rise++) block(east, up + rise, south, `${type}_coral_block`)
    block(east, up + height, south, `${type}_coral[waterlogged=true]`)
    block(east + branchEast, up + height - 1, south + branchSouth, `${type}_coral_block`)
    block(east + branchEast, up + height, south + branchSouth, `${type}_coral_fan[waterlogged=true]`)
  }
  block(8, 4, 10, 'brain_coral[waterlogged=true]')
  block(10, 4, 10, 'brain_coral[waterlogged=true]')
  block(7, 6, 3, 'brain_coral[waterlogged=true]')
  block(8, 6, 3, 'fire_coral_fan[waterlogged=true]')
  sprig(10, 5, 2, 'bubble', 1, 0, 1)
  sprig(3, 4, 14, 'brain', 1, 0, 1)
  sprig(15, 4, 16, 'fire', 2, -1, 0)
  sprig(2, 11, 5, 'fire', 2, 0, -1)
  box([15, 10, 2], [17, 10, 3], 'prismarine_bricks')
  sprig(16, 11, 2, 'brain', 2, 0, 1)
  box([2, 3, 3], [3, 4, 4], 'prismarine')
  for (const [east, up, south] of [[3, 5, 4], [4, 4, 15], [15, 4, 15], [6, 3, 4]]) {
    block(east, up - 1, south, 'prismarine')
    block(east, up, south, 'sea_pickle[pickles=3,waterlogged=true]')
  }
  for (const [east, south, height] of [[3, 12, 7], [15, 6, 8], [6, 15, 6], [2, 2, 14], [17, 2, 13]]) {
    let start = 3
    while (start < height && structure.getBlock([east, start, south]).state.getName().toString() !== 'minecraft:water') start++
    if (start >= height) continue
    for (let up = start; up < height; up++) block(east, up, south, 'kelp_plant')
    block(east, height, south, 'kelp[age=25]')
  }
  for (const entry of structure.getBlocks()) {
    if (!entry.state.getName().toString().endsWith('_coral_block')) continue
    const wet = [[1, 0, 0], [-1, 0, 0], [0, 1, 0], [0, -1, 0], [0, 0, 1], [0, 0, -1]].some(offset => {
      const neighbor = structure.getBlock(entry.pos.map((coordinate, index) => coordinate + offset[index]))
      return neighbor && (neighbor.state.getName().toString() === 'minecraft:water' || neighbor.state.getProperties().waterlogged === 'true' || neighbor.state.getName().toString().includes('kelp'))
    })
    if (!wet) throw new Error(`Coral has no water neighbor: ${entry.pos}`)
  }
  return {structure, metadata: {name: 's5-03-coral-lantern-cell', reference: 'S5-03-small-room.png', ports: [{anchor: [0, 3, 8], outward: 'west', clearance: [3, 3, 3]}, {anchor: [18, 3, 11], outward: 'east', clearance: [3, 3, 3]}], landmarks: {lantern: [9, 5, 10], chest: [12, 5, 2]}}}
}
