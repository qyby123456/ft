export default function build(api) {
  const structure = api.createStructure([27, 16, 29])
  const put = (position, block, properties, nbt) => api.setBlock(structure, position, block, properties, nbt)
  const box = (start, end, block, properties) => api.fill(structure, start, end, block, properties)
  const stair = facing => `minecraft:stone_brick_stairs[facing=${facing},half=bottom,shape=straight,waterlogged=false]`
  box([0, 0, 0], [26, 15, 28], 'minecraft:air')
  box([0, 0, 0], [26, 0, 28], 'minecraft:stone_bricks')
  const stone = ['stone_bricks', 'stone_bricks', 'stone_bricks', 'cracked_stone_bricks', 'andesite', 'mossy_stone_bricks']
  for (let blockX = 0; blockX < 27; blockX++) {
    for (let blockZ = 0; blockZ < 29; blockZ++) {
      put([blockX, 1, blockZ], `minecraft:${stone[(blockX * 31 + blockZ * 17 + blockX * blockZ) % stone.length]}`)
      for (let height = 2; height <= 13; height++) {
        if (blockX <= 1 || blockX >= 25 || blockZ <= 1 || blockZ >= 27) put([blockX, height, blockZ], `minecraft:${stone[(blockX * 7 + blockZ * 13 + height * 11) % stone.length]}`)
      }
    }
  }
  box([0, 14, 0], [26, 14, 28], 'minecraft:dark_oak_planks')
  box([0, 15, 0], [26, 15, 28], 'minecraft:deepslate_tiles')
  for (const ridge of [0, 6, 13, 20, 26]) box([ridge, 15, 0], [ridge, 15, 28], 'minecraft:polished_deepslate')
  for (const edgeX of [0, 1, 25, 26]) box([edgeX, 12, 0], [edgeX, 12, 28], 'minecraft:chiseled_stone_bricks')
  for (const edgeZ of [0, 1, 27, 28]) box([0, 12, edgeZ], [26, 12, edgeZ], 'minecraft:chiseled_stone_bricks')
  for (const edgeX of [2, 24]) box([edgeX, 13, 2], [edgeX, 13, 26], 'minecraft:dark_oak_log', { axis: 'z' })
  for (const edgeZ of [2, 26]) box([2, 13, edgeZ], [24, 13, edgeZ], 'minecraft:dark_oak_log', { axis: 'x' })
  for (const [pillarX, pillarZ] of [[5, 7], [20, 7], [5, 20], [20, 20]]) {
    box([pillarX - 1, 2, pillarZ - 1], [pillarX + 2, 2, pillarZ + 2], 'minecraft:stone_bricks')
    box([pillarX, 3, pillarZ], [pillarX + 1, 3, pillarZ + 1], 'minecraft:chiseled_stone_bricks')
    box([pillarX, 4, pillarZ], [pillarX + 1, 10, pillarZ + 1], 'minecraft:dark_oak_log', { axis: 'y' })
    box([pillarX, 11, pillarZ], [pillarX + 1, 12, pillarZ + 1], 'minecraft:chiseled_stone_bricks')
    box([pillarX - 1, 12, pillarZ - 1], [pillarX + 2, 12, pillarZ + 2], 'minecraft:stone_bricks')
  }
  for (const beamZ of [7, 8, 20, 21]) box([2, 13, beamZ], [24, 13, beamZ], 'minecraft:dark_oak_log', { axis: 'x' })
  for (const beamX of [5, 6, 20, 21]) box([beamX, 13, 2], [beamX, 13, 26], 'minecraft:dark_oak_log', { axis: 'z' })
  for (const [lampX, lampZ] of [[9, 7], [18, 7], [9, 20], [18, 20]]) {
    box([lampX, 10, lampZ], [lampX, 12, lampZ], 'minecraft:chain', { axis: 'y', waterlogged: 'false' })
    put([lampX, 9, lampZ], 'minecraft:lantern', { hanging: 'true', waterlogged: 'false' })
  }
  for (let blockX = 2; blockX <= 7; blockX++) {
    for (let blockZ = 10; blockZ <= 17; blockZ++) {
      if (blockX >= 6 && blockZ >= 13 && blockZ <= 15) continue
      put([blockX, 7, blockZ], 'minecraft:dark_oak_planks')
      if (blockZ === 10 || blockZ === 17 || blockX === 7) put([blockX, 8, blockZ], 'minecraft:dark_oak_fence', { north: 'false', east: 'false', south: 'false', west: 'false', waterlogged: 'false' })
    }
  }
  box([2, 6, 10], [2, 6, 17], 'minecraft:dark_oak_log', { axis: 'z' })
  for (const supportZ of [11, 16]) box([3, 2, supportZ], [3, 6, supportZ], 'minecraft:dark_oak_log', { axis: 'y' })
  box([3, 6, 11], [7, 6, 11], 'minecraft:dark_oak_log', { axis: 'x' })
  box([3, 6, 16], [7, 6, 16], 'minecraft:dark_oak_log', { axis: 'x' })
  for (const rubble of [[6, 2, 13], [7, 2, 13], [8, 2, 14], [7, 2, 15], [6, 2, 16], [8, 2, 16], [9, 2, 15], [5, 2, 14]]) put(rubble, 'minecraft:cracked_stone_bricks')
  put([7, 3, 13], stair('west'))
  put([8, 3, 14], 'minecraft:cobblestone_slab', { type: 'bottom', waterlogged: 'false' })
  put([6, 3, 16], stair('north'))
  put([9, 2, 16], stair('east'))
  box([4, 2, 17], [8, 2, 17], 'minecraft:dark_oak_log', { axis: 'x' })
  box([24, 2, 21], [24, 5, 25], 'minecraft:bookshelf')
  box([24, 6, 21], [24, 6, 25], 'minecraft:dark_oak_slab', { type: 'bottom', waterlogged: 'false' })
  box([24, 2, 20], [24, 6, 20], 'minecraft:dark_oak_planks')
  box([24, 2, 26], [24, 6, 26], 'minecraft:dark_oak_planks')
  for (const [startX, endX] of [[3, 8], [18, 23]]) {
    box([startX, 2, 3], [endX, 2, 3], 'minecraft:dark_oak_planks')
    box([startX, 2, 4], [endX, 2, 4], 'minecraft:bookshelf')
    box([startX, 3, 3], [endX, 3, 4], 'minecraft:dark_oak_slab', { type: 'bottom', waterlogged: 'false' })
  }
  const makeTable = (startX, endX, startZ, endZ, missingCorner) => {
    for (let blockX = startX; blockX <= endX; blockX++) {
      for (let blockZ = startZ; blockZ <= endZ; blockZ++) {
        if (missingCorner && blockX === endX && blockZ === endZ) continue
        put([blockX, 3, blockZ], 'minecraft:dark_oak_planks')
        if ((blockX === startX || blockX === endX) && (blockZ === startZ || blockZ === endZ)) put([blockX, 2, blockZ], 'minecraft:dark_oak_log', { axis: 'y' })
      }
    }
  }
  makeTable(8, 10, 23, 25, false)
  makeTable(16, 18, 15, 17, true)
  for (const manuscript of [[9, 4, 24], [9, 4, 25], [17, 4, 15]]) put(manuscript, 'minecraft:white_carpet')
  put([8, 4, 23], 'minecraft:candle', { candles: '3', lit: 'false', waterlogged: 'false' })
  put([18, 4, 15], 'minecraft:candle', { candles: '2', lit: 'false', waterlogged: 'false' })
  for (const chair of [[8, 2, 26], [17, 2, 18]]) put(chair, 'minecraft:dark_oak_stairs', { facing: 'north', half: 'bottom', shape: 'straight', waterlogged: 'false' })
  put([10, 2, 26], 'minecraft:dark_oak_slab', { type: 'bottom', waterlogged: 'false' })
  for (let blockX = 16; blockX <= 18; blockX++) {
    for (let blockZ = 9; blockZ <= 12; blockZ++) if ((blockX + blockZ) % 5 !== 0) put([blockX, 2, blockZ], 'minecraft:red_carpet')
  }
  for (const [paperX, paperZ] of [[3, 6], [8, 5], [10, 6], [17, 5], [22, 10], [22, 17], [3, 22], [6, 25], [19, 25], [16, 23]]) put([paperX, 2, paperZ], 'minecraft:white_carpet')
  for (const [bookX, bookZ, color] of [[9, 9, 'brown'], [17, 7, 'green'], [19, 18, 'blue'], [10, 19, 'brown'], [16, 25, 'green'], [22, 6, 'blue']]) put([bookX, 2, bookZ], `minecraft:${color}_carpet`)
  box([25, 4, 12], [26, 10, 16], 'minecraft:light_blue_stained_glass')
  for (const windowZ of [12, 14, 16]) box([24, 4, windowZ], [24, 10, windowZ], 'minecraft:iron_bars', { north: 'false', east: 'true', south: 'false', west: 'false', waterlogged: 'false' })
  box([24, 3, 11], [24, 3, 17], 'minecraft:stone_bricks')
  box([24, 11, 11], [24, 11, 17], 'minecraft:stone_bricks')
  for (const lightZ of [13, 15]) put([24, 3, lightZ], 'minecraft:sea_lantern')
  for (const [barrelX, barrelZ] of [[3, 24], [23, 18]]) {
    const data = api.createBlockNbt({ id: 'minecraft:barrel' })
    data.set('Items', new api.NbtList())
    put([barrelX, 2, barrelZ], 'minecraft:barrel', { facing: 'up', open: 'false' }, data)
  }
  for (const [bannerX, bannerZ, facing] of [[8, 2, 'south'], [18, 2, 'south'], [2, 23, 'east']]) {
    const data = api.createBlockNbt({ id: 'minecraft:banner' })
    const patterns = new api.NbtList()
    for (const [pattern, color] of [['bo', 12], ['hh', 12], ['ls', 15]]) {
      const patternTag = new api.NbtCompound()
      patternTag.set('Pattern', new api.NbtString(pattern))
      patternTag.set('Color', new api.NbtInt(color))
      patterns.add(patternTag)
    }
    data.set('Patterns', patterns)
    put([bannerX, 10, bannerZ], 'minecraft:red_wall_banner', { facing }, data)
  }
  for (const cobweb of [[2, 11, 3], [2, 9, 18], [24, 9, 24], [24, 7, 22], [2, 5, 13], [2, 9, 10]]) put(cobweb, 'minecraft:cobweb')
  for (let blockX = 11; blockX <= 15; blockX++) {
    for (let blockZ = 0; blockZ <= 26; blockZ++) put([blockX, 1, blockZ], (blockX === 11 || blockX === 15) ? 'minecraft:polished_andesite' : ((blockZ % 6 === 0) ? 'minecraft:chiseled_stone_bricks' : 'minecraft:stone_bricks'))
  }
  box([11, 2, 0], [15, 6, 1], 'minecraft:air')
  box([10, 2, 0], [10, 6, 1], 'minecraft:chiseled_stone_bricks')
  box([16, 2, 0], [16, 6, 1], 'minecraft:chiseled_stone_bricks')
  box([10, 7, 0], [16, 7, 1], 'minecraft:stone_bricks')
  return { structure, metadata: { name: 'room' } }
}
