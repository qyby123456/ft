export default function build(api) {
  const structure = api.createStructure([29, 17, 27])
  const put = (x, y, z, block) => api.setBlock(structure, [x, y, z], `minecraft:${block}`)
  const box = (start, end, block) => api.fill(structure, start, end, `minecraft:${block}`)
  const noise = (x, y, z) => Math.abs((x * 12347 + y * 7169 + z * 27581 + x * z * 131) % 97)
  const stone = (x, y, z) => noise(x, y, z) < 16 ? 'cracked_stone_bricks' : noise(x, y, z) < 29 ? 'cobblestone' : 'stone_bricks'
  box([0, 0, 0], [28, 16, 26], 'air')
  for (let x = 0; x < 29; x++) for (let z = 0; z < 27; z++) {
    put(x, 0, z, 'stone_bricks')
    const ice = ((x - 6) ** 2 / 20 + (z - 21) ** 2 / 9 < 1) || ((x - 20) ** 2 / 23 + (z - 8) ** 2 / 7 < 1)
    put(x, 1, z, ice ? (noise(x, 1, z) < 35 ? 'blue_ice' : 'packed_ice') : stone(x, 1, z))
    if (!ice && noise(x, 2, z) < 24) put(x, 2, z, 'snow[layers=1]')
  }
  for (let y = 2; y <= 13; y++) for (let x = 0; x < 29; x++) for (let z = 0; z < 27; z++) {
    if (x < 2 || z < 2 || x > 26 || z > 24) put(x, y, z, stone(x, y, z))
  }
  for (let x = 0; x < 29; x++) for (let z = 0; z < 27; z++) {
    const edge = Math.min(x, z, 28 - x, 26 - z)
    const underside = 13 + Math.min(2, Math.floor(edge / 4))
    for (let y = underside; y <= 16; y++) put(x, y, z, y === 16 && noise(x, y, z) < 50 ? 'snow_block' : stone(x, y, z))
  }
  const timberNorth = (centerX, width) => {
    const left = centerX - Math.floor(width / 2) - 1
    const right = centerX + Math.floor(width / 2) + 1
    for (const x of [left, right]) {
      box([x, 2, 2], [x + 1, 9, 3], 'spruce_log[axis=y]')
      box([x, 2, 2], [x + 1, 2, 3], 'spruce_planks')
      box([x, 4, 2], [x + 1, 4, 3], 'spruce_planks')
    }
    box([left, 10, 2], [right + 1, 11, 3], 'spruce_log[axis=x]')
    for (const x of [left + 2, right - 1]) put(x, 9, 3, `spruce_stairs[facing=${x < centerX ? 'west' : 'east'},half=top,shape=straight]`)
    box([centerX - 2, 2, 0], [centerX + 2, 7, 3], 'air')
  }
  timberNorth(14, 5)
  for (const z of [5, 12]) {
    box([2, 2, z], [3, 9, z + 1], 'spruce_log[axis=y]')
    box([2, 2, z], [3, 2, z + 1], 'spruce_planks')
    box([2, 4, z], [3, 4, z + 1], 'spruce_planks')
  }
  box([2, 10, 5], [3, 11, 13], 'spruce_log[axis=z]')
  box([0, 2, 7], [3, 7, 11], 'air')
  for (const [x, z] of [[9, 4], [20, 4], [4, 6], [4, 13]]) {
    box([x, 9, z], [x, 11, z], 'chain[axis=y]')
    put(x, 8, z, 'soul_lantern[hanging=true]')
    box([x, 12, z], [x, 14, z], 'stone_bricks')
  }
  for (let x = 2; x <= 26; x++) for (const z of [2, 3, 23, 24]) {
    if (noise(x, 12, z) > 30) continue
    const length = 1 + noise(x, 8, z) % 4
    for (let y = 13 - length; y < 13; y++) put(x, y, z, 'packed_ice')
  }
  for (let z = 4; z <= 22; z++) for (const x of [2, 3, 25, 26]) {
    if (noise(x, 11, z) > 30 || (x < 4 && z >= 5 && z <= 13)) continue
    const length = 1 + noise(x, 8, z) % 4
    for (let y = 13 - length; y < 13; y++) put(x, y, z, 'packed_ice')
  }
  box([5, 2, 2], [7, 4, 2], 'bookshelf')
  box([5, 2, 3], [7, 2, 3], 'spruce_planks')
  box([5, 5, 2], [7, 5, 2], 'spruce_slab[type=top]')
  put(6, 6, 2, 'candle[candles=2,lit=false]')
  for (let x = 8; x <= 10; x++) put(x, 2, 3, 'spruce_stairs[facing=north,half=bottom,shape=straight]')
  put(7, 2, 4, 'spruce_trapdoor[facing=west,half=bottom,open=true]')
  put(11, 2, 3, 'spruce_trapdoor[facing=east,half=bottom,open=true]')
  const entity = (x, y, z, block, id) => api.setBlock(structure, [x, y, z], `minecraft:${block}`, undefined, api.createBlockNbt({ id: `minecraft:${id}` }))
  entity(19, 2, 2, 'furnace[facing=south,lit=false]', 'furnace')
  entity(20, 2, 2, 'furnace[facing=south,lit=false]', 'furnace')
  for (const [x, z] of [[3, 15], [4, 16]]) entity(x, 2, z, 'barrel[facing=up,open=false]', 'barrel')
  box([24, 2, 3], [26, 2, 4], 'spruce_planks')
  for (const x of [24, 26]) box([x, 3, 3], [x, 4, 3], 'spruce_planks')
  box([24, 5, 3], [26, 5, 4], 'spruce_slab[type=top]')
  put(24, 6, 3, 'snow[layers=1]')
  put(25, 6, 3, 'blue_ice')
  put(26, 6, 3, 'snow[layers=1]')
  box([11, 2, 12], [19, 2, 20], 'stone_bricks')
  for (const [x, z] of [[11, 12], [11, 20], [19, 12], [19, 20]]) put(x, 2, z, 'air')
  box([12, 3, 13], [18, 3, 19], 'stone_bricks')
  box([14, 3, 15], [16, 3, 17], 'packed_ice')
  for (let x = 12; x <= 18; x++) for (let z = 13; z <= 19; z++) {
    if ((x === 12 || x === 18 || z === 13 || z === 19) && noise(x, 4, z) < 57) put(x, 4, z, 'snow[layers=1]')
  }
  for (let z = 13; z <= 17; z += 2) {
    box([23, 2, z], [23, 3, z], 'bone_block[axis=y]')
    box([23, 4, z], [25, 4, z], 'bone_block[axis=x]')
    box([26, 2, z], [26, 4, z], 'bone_block[axis=y]')
  }
  box([26, 2, 13], [26, 2, 18], 'bone_block[axis=z]')
  const crystal = (x, z, height) => {
    box([x - 1, 2, z], [x + 1, 2, z + 1], 'packed_ice')
    box([x, 3, z], [x, height, z], 'blue_ice')
    put(x, height + 1, z, 'packed_ice')
  }
  for (const [x, z, height] of [[3, 22, 6], [24, 23, 7], [5, 19, 4], [6, 12, 4]]) crystal(x, z, height)
  for (let x = 12; x <= 16; x++) box([x, 2, 0], [x, 5, 7], 'air')
  for (let z = 7; z <= 11; z++) box([0, 2, z], [5, 5, z], 'air')
  const snowSupport = new Set(['minecraft:stone_bricks', 'minecraft:cracked_stone_bricks', 'minecraft:cobblestone', 'minecraft:spruce_planks', 'minecraft:spruce_log', 'minecraft:bone_block', 'minecraft:bookshelf', 'minecraft:snow_block'])
  for (const block of [...structure.getBlocks()]) {
    if (block.state.getName().toString() !== 'minecraft:snow') continue
    const [x, y, z] = block.pos
    const under = structure.getBlock([x, y - 1, z])
    const safe = under && (snowSupport.has(under.state.getName().toString()) || under.state.toString().includes('slab') && under.state.toString().includes('type=top'))
    if (!safe) api.setBlock(structure, block.pos, 'minecraft:air')
  }
  return { structure, metadata: { name: 's3-02-frozen-rest-room' } }
}
