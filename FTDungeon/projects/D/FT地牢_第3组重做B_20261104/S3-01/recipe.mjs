import { readFileSync } from 'node:fs'

export default function build(api) {
  const structure = api.createStructure([35, 22, 35])
  const put = (x, y, z, block) => api.setBlock(structure, [x, y, z], `minecraft:${block}`)
  const fill = (start, end, block) => api.fill(structure, start, end, `minecraft:${block}`)
  const noise = (x, y, z) => Math.abs((x * 73856093 ^ y * 19349663 ^ z * 83492791) % 101)
  const masonry = (x, y, z) => noise(x, y, z) < 24 ? 'cracked_stone_bricks' : noise(x, y, z) < 34 ? 'cobblestone' : 'stone_bricks'
  fill([0, 0, 0], [34, 21, 34], 'air')
  for (let x = 0; x < 35; x++) for (let z = 0; z < 35; z++) {
    put(x, 0, z, masonry(x, 0, z))
    const frozen = Math.sin(x / 4) + Math.cos(z / 3) + Math.sin((x + z) / 5) > -0.1
    put(x, 1, z, frozen ? noise(x, 1, z) < 30 ? 'blue_ice' : 'packed_ice' : masonry(x, 1, z))
    if (Math.sin(x / 2.3) * Math.cos(z / 2.8) > 0.42 || ((x < 4 || z < 4 || x > 30 || z > 30) && noise(x, 2, z) < 48)) put(x, 2, z, 'snow[layers=1]')
  }
  for (let y = 2; y <= 17; y++) for (let span = 0; span < 35; span++) for (const depth of [0, 1, 33, 34]) {
    put(span, y, depth, masonry(span, y, depth))
    put(depth, y, span, masonry(depth, y, span))
  }
  for (let x = 2; x <= 32; x++) for (let z = 2; z <= 32; z++) {
    const edge = Math.min(x, z, 34 - x, 34 - z)
    const roof = Math.min(20, 15 + Math.floor(edge / 2))
    for (let y = roof; y <= 21; y++) put(x, y, z, y === 21 && noise(x, y, z) < 50 ? 'snow_block' : masonry(x, y, z))
    if (edge < 4 && noise(x, 0, z) < 22) {
      const drop = 1 + noise(x, 4, z) % 4
      for (let y = roof - drop; y < roof; y++) put(x, y, z, 'packed_ice')
    }
  }
  for (const coord of [5, 13, 23, 29]) {
    for (const [x, z] of [[coord, 3], [3, coord], [31, coord], [coord, 31]]) {
      fill([x - 1, 2, z - 1], [x + 1, 3, z + 1], 'stone_bricks')
      fill([x, 4, z], [x, 14, z], 'spruce_log[axis=y]')
      for (const y of [6, 10, 14]) {
        fill([x - 1, y, z - 1], [x + 1, y, z + 1], 'stone_brick_slab[type=top]')
        fill([x - 1, y + 1, z - 1], [x + 1, y + 1, z + 1], 'snow[layers=1]')
        put(x, y, z, 'spruce_log[axis=y]')
        put(x, y + 1, z, 'spruce_log[axis=y]')
      }
    }
  }
  for (const side of [3, 31]) {
    fill([side - 1, 9, 4], [side + 1, 9, 30], 'stone_bricks')
    fill([side - 1, 10, 4], [side + 1, 10, 30], 'stone_brick_slab[type=bottom]')
    for (const z of [7, 17, 26]) {
      fill([side, 14, z], [side, 15, z], 'packed_ice')
      fill([side, 11, z], [side, 13, z], 'blue_ice')
    }
  }
  for (const side of [0, 34]) {
    const inside = side === 0 ? 2 : 30
    for (const z of [9, 17]) fill([Math.min(side, inside), 2, z], [Math.max(side, inside + 2), 6, z + 1], 'stone_bricks')
    fill([Math.min(side, inside), 7, 9], [Math.max(side, inside + 2), 8, 18], 'stone_bricks')
    fill([Math.min(side, inside), 2, 11], [Math.max(side, inside + 2), 6, 15], 'air')
    for (const z of [10, 16]) fill([inside, 5, z], [inside + 2, 6, z], `stone_brick_stairs[facing=${z === 10 ? 'south' : 'north'},half=top,shape=straight]`)
  }
  fill([15, 2, 0], [19, 6, 3], 'air')
  fill([14, 2, 0], [14, 7, 2], 'chiseled_stone_bricks')
  fill([20, 2, 0], [20, 7, 2], 'chiseled_stone_bricks')
  fill([14, 7, 0], [20, 7, 2], 'stone_bricks')
  for (let x = 10; x <= 24; x++) for (let z = 12; z <= 25; z++) {
    const radius = ((x - 17) / 7.7) ** 2 + ((z - 18.5) / 6.7) ** 2
    if (radius > 1) continue
    const height = radius < 0.23 ? 5 : radius < 0.58 ? 4 : 3
    for (let y = 2; y <= height; y++) put(x, y, z, masonry(x, y, z))
    if (noise(x, height, z) < 68) put(x, height + 1, z, 'snow[layers=1]')
  }
  const crystal = (centerX, centerZ, base, height, radius) => {
    for (let rise = 0; rise < height; rise++) {
      const width = Math.max(0, Math.floor(radius * (1 - rise / height) + 0.6))
      for (let offsetX = -width; offsetX <= width; offsetX++) for (let offsetZ = -width; offsetZ <= width; offsetZ++) {
        if (Math.abs(offsetX) + Math.abs(offsetZ) > width + 1) continue
        put(centerX + offsetX, base + rise, centerZ + offsetZ, offsetX <= 0 ? 'packed_ice' : 'blue_ice')
      }
    }
    put(centerX, base + height, centerZ, 'packed_ice')
  }
  crystal(17, 18, 5, 9, 2)
  crystal(13, 17, 4, 6, 1)
  crystal(21, 19, 4, 7, 1)
  crystal(17, 22, 4, 4, 1)
  crystal(21, 15, 3, 5, 1)
  for (const [x, z, height] of [[7, 22, 4], [28, 24, 6], [26, 30, 4], [8, 30, 3], [6, 7, 3]]) crystal(x, z, 2, height, 1)
  for (const coord of [8, 18, 26]) {
    for (const [x, z] of [[2, coord], [coord, 2], [32, coord], [coord, 32]]) {
      fill([x, 3, z], [x, 12 + noise(x, 5, z) % 3, z], 'packed_ice')
      put(x, 13 + noise(x, 5, z) % 3, z, 'snow_block')
    }
  }
  for (let z = 25; z <= 29; z += 2) {
    fill([5, 2, z], [5, 4, z], 'bone_block[axis=y]')
    fill([6, 4, z], [7, 4, z], 'bone_block[axis=x]')
    fill([8, 2, z], [8, 4, z], 'bone_block[axis=y]')
  }
  fill([8, 2, 25], [8, 2, 30], 'bone_block[axis=z]')
  for (const [x, z] of [[9, 6], [25, 6], [6, 24], [28, 24]]) {
    fill([x, 11, z], [x, 16, z], 'chain[axis=y]')
    put(x, 10, z, 'soul_lantern[hanging=true]')
    fill([x, 17, z], [x, 20, z], 'stone_bricks')
  }
  for (const x of [15, 16, 17, 18, 19]) for (let z = 0; z <= 8; z++) fill([x, 2, z], [x, 5, z], 'air')
  for (const x of [0, 1, 2, 3, 31, 32, 33, 34]) for (let z = 11; z <= 15; z++) fill([x, 2, z], [x, 5, z], 'air')
  const removals = JSON.parse(readFileSync(new URL('./snow-removals.json', import.meta.url), 'utf8').replace(/^\uFEFF/, ''))
  for (const removal of removals) {
    const block = structure.getBlock(removal.position)
    if (block.state.getName().toString() !== 'minecraft:snow') throw new Error(`Expected snow at ${removal.position}`)
    api.setBlock(structure, removal.position, 'minecraft:air')
  }
  return { structure, metadata: { name: 's3-01-frost-crystal-hall' } }
}
