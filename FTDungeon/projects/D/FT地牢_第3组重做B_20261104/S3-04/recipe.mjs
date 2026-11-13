export default function build(api) {
  const structure = api.createStructure([35, 27, 57])
  const put = (x, y, z, block) => api.setBlock(structure, [x, y, z], `minecraft:${block}`)
  const box = (first, last, block) => api.fill(structure, first, last, `minecraft:${block}`)
  const noise = (x, y, z) => (x * 67 + y * 13 + z * 31 + x * z * 7) % 101
  const stone = (x, y, z) => noise(x, y, z) < 19 ? 'cracked_stone_bricks' : noise(x, y, z) < 29 ? 'cobblestone' : 'stone_bricks'
  box([0, 0, 0], [34, 26, 56], 'air')
  for (let x = 0; x < 35; x++) for (let z = 0; z < 57; z++) {
    put(x, 0, z, 'stone_bricks')
    const central = x >= 14 && x <= 20
    const frozen = !central && ((x < 9 && z < 17) || (x > 25 && z > 23 && z < 48) || noise(x, 1, z) < 20)
    put(x, 1, z, central ? (x === 14 || x === 20 ? 'polished_andesite' : 'stone_bricks') : frozen ? noise(x, 2, z) < 35 ? 'blue_ice' : 'packed_ice' : stone(x, 1, z))
    if (!central && !frozen && noise(x, 3, z) < 15) put(x, 2, z, 'snow_block')
    for (let y = 2; y <= 22; y++) if (x < 2 || x > 32 || z < 2 || z > 54) put(x, y, z, stone(x, y, z))
    const ceiling = 18 + Math.floor((16 - Math.abs(x - 17)) / 3)
    for (let y = Math.max(18, ceiling); y <= 26; y++) put(x, y, z, y === 26 && noise(x, y, z) < 48 ? 'snow_block' : stone(x, y, z))
  }
  const bannerNbt = api.createBlockNbt({ id: 'minecraft:banner', Patterns: [{ Pattern: 'flo', Color: 0 }] })
  for (const z of [10, 22, 34, 46]) {
    for (const x of [9, 25]) {
      box([x - 2, 2, z - 2], [x + 2, 3, z + 2], 'stone_bricks')
      box([x - 1, 4, z - 1], [x + 1, 6, z + 1], 'chiseled_stone_bricks')
      box([x - 1, 7, z - 1], [x + 1, 17, z + 1], 'stone_bricks')
      box([x - 1, 10, z - 1], [x + 1, 10, z + 1], 'spruce_log[axis=x]')
      for (const y of [7, 14, 18]) {
        box([x - 2, y, z - 2], [x + 2, y, z + 2], 'stone_bricks')
        box([x - 2, y + 1, z - 2], [x + 2, y + 1, z + 2], 'stone_brick_slab[type=bottom]')
      }
      box([x - 1, 19, z - 1], [x + 1, 21, z + 1], 'stone_bricks')
      box([x - 3, 22, z - 2], [x + 3, 23, z + 2], 'stone_bricks')
      api.setBlock(structure, [x, 13, z - 2], 'minecraft:blue_wall_banner[facing=north]', undefined, bannerNbt)
      for (const offset of [-3, 3]) {
        const lampX = x + offset
        box([Math.min(x, lampX), 15, z], [Math.max(x, lampX), 15, z], 'spruce_log[axis=x]')
        box([lampX, 13, z], [lampX, 14, z], 'chain[axis=y]')
        put(lampX, 12, z, 'lantern[hanging=true]')
      }
      for (const offset of [-2, 2]) put(x + offset, 4, z, 'lantern[hanging=false]')
    }
    for (let x = 2; x <= 32; x++) {
      const bottom = 17 + Math.min(6, Math.floor((16 - Math.abs(x - 17)) / 2))
      box([x, bottom, z - 1], [x, 26, z + 1], 'stone_bricks')
      if (x > 11 && x < 23 && noise(x, 2, z) < 48) box([x, bottom - 3, z], [x, bottom - 1, z], 'packed_ice')
    }
  }
  for (const side of [0, 34]) for (const z of [20, 38]) {
    box([side === 0 ? 0 : 31, 2, z - 2], [side === 0 ? 3 : 34, 7, z + 2], 'air')
    for (const edge of [-3, 3]) box([side === 0 ? 2 : 32, 2, z + edge], [side === 0 ? 3 : 32, 8, z + edge], 'chiseled_stone_bricks')
    box([side === 0 ? 2 : 31, 8, z - 3], [side === 0 ? 3 : 32, 9, z + 3], 'stone_bricks')
  }
  for (const x of [3, 31]) for (const z of [6, 28, 50]) {
    box([x, 2, z], [x, 13, z], 'spruce_log[axis=y]')
    put(x + (x < 17 ? 1 : -1), 13, z, 'spruce_log[axis=x]')
    put(x + (x < 17 ? 1 : -1), 12, z, 'lantern[hanging=true]')
  }
  for (let z = 3; z <= 53; z++) for (const x of [2, 3, 31, 32]) {
    if (noise(x, 10, z) > 38) continue
    const bottom = 14 + noise(x, 7, z) % 4
    box([x, bottom, z], [x, 18, z], 'packed_ice')
  }
  for (const [x, z, height] of [[4, 8, 7], [29, 13, 6], [3, 43, 7], [30, 45, 8], [29, 7, 5]]) {
    box([x - 1, 2, z - 1], [x + 1, 3, z + 1], 'packed_ice')
    box([x, 4, z], [x, height, z], 'blue_ice')
    put(x, height + 1, z, 'packed_ice')
  }
  for (const z of [13, 15, 17]) {
    box([4, 2, z], [4, 3, z], 'bone_block[axis=y]')
    box([4, 4, z], [6, 4, z], 'bone_block[axis=x]')
    box([7, 2, z], [7, 4, z], 'bone_block[axis=y]')
  }
  for (let x = 7; x <= 27; x++) for (let y = 2; y <= 21; y++) {
    const distance = Math.abs(x - 17)
    const apertureHeight = 18 - Math.floor(distance * 0.8)
    if (distance > 6 || y > apertureHeight) put(x, y, 52, 'packed_ice')
  }
  for (const x of [11, 23]) box([x, 2, 52], [x, 12, 54], 'blue_ice')
  box([13, 2, 53], [21, 13, 54], 'air')
  for (const [x, z] of [[13, 53], [21, 53]]) box([x, 2, z], [x, 15, z], 'blue_ice')
  box([5, 2, 18], [6, 5, 50], 'air')
  for (const z of [18, 28, 38]) box([z === 28 ? 5 : 2, 2, z], [17, 5, z + 1], 'air')
  for (let z = 0; z <= 56; z++) box([15, 2, z], [19, 5, z], 'air')
  box([15, 2, 0], [19, 7, 2], 'air')
  box([15, 2, 54], [19, 7, 56], 'air')
  box([12, 8, 55], [22, 20, 56], 'packed_ice')
  box([14, 8, 55], [20, 18, 55], 'blue_ice')
  for (const side of [0, 34]) for (const z of [20, 38]) box([side === 0 ? 0 : 30, 2, z - 2], [side === 0 ? 4 : 34, 5, z + 2], 'air')
  return { structure, metadata: { name: 's3-04-frozen-colonnade-great-hall' } }
}
