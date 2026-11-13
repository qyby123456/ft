export default function build(api) {
  const structure = api.createStructure([21, 15, 19])
  const put = (x, y, z, block) => api.setBlock(structure, [x, y, z], `minecraft:${block}`)
  const box = (start, end, block) => api.fill(structure, start, end, `minecraft:${block}`)
  const noise = (x, y, z) => (x * 37 + y * 17 + z * 53 + x * z * 3) % 97
  const stone = (x, y, z) => noise(x, y, z) < 21 ? 'cracked_stone_bricks' : noise(x, y, z) < 30 ? 'cobblestone' : 'stone_bricks'
  box([0, 0, 0], [20, 14, 18], 'air')
  for (let x = 0; x < 21; x++) for (let z = 0; z < 19; z++) {
    put(x, 0, z, 'stone_bricks')
    const ice = ((x - 15) ** 2 / 16 + (z - 13) ** 2 / 8 < 1) || (x < 6 && z < 7 && z > 3)
    put(x, 1, z, ice ? noise(x, 1, z) < 40 ? 'blue_ice' : 'packed_ice' : stone(x, 1, z))
    if (!ice && noise(x, 2, z) < 23) put(x, 2, z, 'snow[layers=1]')
    for (let y = 2; y <= 12; y++) if (x < 2 || x > 18 || z < 2 || z > 16) put(x, y, z, stone(x, y, z))
    const edge = Math.min(x, z, 20 - x, 18 - z)
    const ceiling = 11 + Math.min(2, Math.floor(edge / 3))
    for (let y = ceiling; y <= 14; y++) put(x, y, z, y === 14 && noise(x, y, z) < 50 ? 'snow_block' : stone(x, y, z))
  }
  box([8, 2, 0], [12, 6, 3], 'air')
  for (const x of [7, 13]) box([x, 2, 1], [x, 7, 2], 'chiseled_stone_bricks')
  box([7, 7, 0], [13, 8, 2], 'stone_bricks')
  for (const x of [8, 12]) put(x, 6, 2, `stone_brick_stairs[facing=${x === 8 ? 'west' : 'east'},half=top,shape=straight]`)
  for (const z of [4, 15]) {
    box([2, 2, z], [2, 10, z], 'spruce_log[axis=y]')
    box([3, 9, z], [3, 10, z], 'spruce_planks')
  }
  for (const [first, last] of [[5, 8], [10, 13]]) {
    box([2, 2, first], [3, 3, last], 'stone_bricks')
    for (const y of [4, 7]) {
      box([2, y, first], [3, y, last], 'spruce_slab[type=top]')
      put(2, y + 1, first + 1, 'bone_block[axis=z]')
      put(2, y + 1, last, 'blue_ice')
      if (y === 4) put(3, y + 1, first + 2, 'bone_block[axis=x]')
    }
    box([2, 9, first], [3, 10, last], 'stone_bricks')
  }
  box([2, 2, 9], [3, 10, 9], 'chiseled_stone_bricks')
  for (const z of [5, 14]) {
    box([4, 8, z], [4, 10, z], 'chain[axis=y]')
    put(4, 7, z, 'soul_lantern[hanging=true]')
    put(4, 11, z, 'stone_bricks')
  }
  for (let x = 2; x <= 18; x++) for (const z of [2, 16]) {
    if (noise(x, 8, z) < 50) for (let y = 8 + noise(x, 4, z) % 3; y <= 11; y++) put(x, y, z, 'packed_ice')
  }
  for (let z = 3; z <= 15; z++) for (const x of [2, 18]) {
    if (noise(x, 9, z) < 48) for (let y = 8 + noise(x, 4, z) % 3; y <= 11; y++) put(x, y, z, 'packed_ice')
  }
  box([9, 2, 9], [13, 2, 13], 'stone_brick_slab[type=top]')
  api.setBlock(structure, [11, 3, 11], 'minecraft:chest[facing=east,type=single,waterlogged=false]', undefined, api.createBlockNbt({ id: 'minecraft:chest' }))
  api.setBlock(structure, [15, 2, 4], 'minecraft:barrel[facing=up,open=false]', undefined, api.createBlockNbt({ id: 'minecraft:barrel' }))
  put(15, 3, 4, 'potted_dead_bush')
  for (const z of [4, 14]) {
    box([4, 2, z], [5, 2, z + 1], 'cobblestone')
    put(4, 3, z, 'potted_dead_bush')
  }
  for (const [x, z, height] of [[6, 10, 4], [17, 14, 5], [3, 3, 5], [18, 5, 4]]) {
    box([x - 1, 2, z], [x + 1, 2, z + 1], 'packed_ice')
    box([x, 3, z], [x, height, z], 'blue_ice')
    put(x, height + 1, z, 'packed_ice')
  }
  box([11, 2, 15], [13, 2, 15], 'bone_block[axis=x]')
  put(14, 2, 15, 'bone_block[axis=y]')
  for (let x = 8; x <= 12; x++) box([x, 2, 0], [x, 5, 5], 'air')
  const safe = new Set(['minecraft:stone_bricks', 'minecraft:cracked_stone_bricks', 'minecraft:cobblestone', 'minecraft:snow_block', 'minecraft:chiseled_stone_bricks'])
  for (const block of [...structure.getBlocks()]) {
    if (block.state.getName().toString() !== 'minecraft:snow') continue
    const [x, y, z] = block.pos
    if (!safe.has(structure.getBlock([x, y - 1, z])?.state.getName().toString())) api.setBlock(structure, block.pos, 'minecraft:air')
  }
  return { structure, metadata: { name: 's3-03-frozen-bone-store' } }
}
