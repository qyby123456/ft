const size = [17, 13, 41]
const arches = [2, 9, 16, 23, 30, 38]
const lavaStations = [6, 13, 20, 27, 34]
const crystalStations = [4, 7, 11, 14, 18, 21, 25, 28, 32, 35]

function masonry(blockX, height, blockZ) {
  const variation = (blockX * 71 + height * 31 + blockZ * 43) % 29
  if (variation < 4) return 'minecraft:mossy_stone_bricks'
  if (variation < 7) return 'minecraft:cracked_stone_bricks'
  if (variation < 10) return 'minecraft:andesite'
  if (variation === 11) return 'minecraft:tuff'
  return 'minecraft:stone_bricks'
}

export default function build(api) {
  const structure = api.createStructure(size)
  const place = (blockX, height, blockZ, block, properties) => api.setBlock(structure, [blockX, height, blockZ], block, properties)
  const fill = (from, to, block, properties) => api.fill(structure, from, to, block, properties)
  fill([0, 0, 0], [16, 12, 40], 'minecraft:air')
  fill([0, 0, 0], [16, 1, 40], 'minecraft:deepslate_bricks')
  for (let blockX = 0; blockX < size[0]; blockX++) {
    for (let blockZ = 0; blockZ < size[2]; blockZ++) {
      place(blockX, 2, blockZ, masonry(blockX, 2, blockZ))
      place(blockX, 12, blockZ, blockZ % 7 === 2 ? 'minecraft:polished_deepslate' : 'minecraft:stone_bricks')
      for (let height = 3; height < 12; height++) {
        if (blockX <= 2 || blockX >= 14 || blockZ <= 1 || blockZ >= 39) place(blockX, height, blockZ, masonry(blockX, height, blockZ))
      }
    }
  }
  for (let blockZ = 2; blockZ <= 38; blockZ++) {
    for (let blockX = 3; blockX <= 13; blockX++) {
      const vaultHeight = 11 - Math.max(0, 6 - Math.min(blockX, 16 - blockX))
      for (let height = vaultHeight; height <= 11; height++) place(blockX, height, blockZ, masonry(blockX, height, blockZ))
    }
  }
  fill([3, 2, 3], [5, 2, 37], 'minecraft:water', { level: '0' })
  fill([11, 2, 3], [13, 2, 37], 'minecraft:water', { level: '0' })
  for (let blockZ = 0; blockZ <= 40; blockZ++) {
    for (let blockX = 6; blockX <= 10; blockX++) place(blockX, 2, blockZ, blockX === 6 || blockX === 10 ? 'minecraft:polished_deepslate' : masonry(blockX, 2, blockZ))
    if (blockZ % 7 === 5) place(8, 2, blockZ, 'minecraft:chiseled_stone_bricks')
  }
  for (const blockZ of arches) {
    for (const columnX of [3, 12]) {
      fill([columnX, 2, blockZ], [columnX + 1, 2, blockZ], 'minecraft:polished_deepslate')
      fill([columnX, 3, blockZ], [columnX + 1, 6, blockZ], 'minecraft:stone_bricks')
      place(columnX, 4, blockZ, blockZ === 16 || blockZ === 30 ? 'minecraft:mossy_stone_bricks' : 'minecraft:chiseled_stone_bricks')
      fill([columnX, 7, blockZ], [columnX + 1, 7, blockZ], 'minecraft:polished_andesite')
    }
    fill([4, 8, blockZ], [5, 8, blockZ], 'minecraft:stone_bricks')
    fill([11, 8, blockZ], [12, 8, blockZ], 'minecraft:stone_bricks')
    place(5, 7, blockZ, 'minecraft:stone_brick_stairs', { facing: 'west', half: 'top', shape: 'straight', waterlogged: 'false' })
    place(11, 7, blockZ, 'minecraft:stone_brick_stairs', { facing: 'east', half: 'top', shape: 'straight', waterlogged: 'false' })
    fill([5, 9, blockZ], [6, 9, blockZ], 'minecraft:stone_bricks')
    fill([10, 9, blockZ], [11, 9, blockZ], 'minecraft:stone_bricks')
    fill([6, 10, blockZ], [10, 10, blockZ], 'minecraft:stone_bricks')
    place(8, 10, blockZ, 'minecraft:chiseled_stone_bricks')
    if (blockZ === 16 || blockZ === 30) {
      place(5, 8, blockZ, 'minecraft:cracked_stone_bricks')
      place(11, 9, blockZ, 'minecraft:mossy_stone_bricks')
    }
  }
  for (const blockZ of lavaStations) {
    for (const wallX of [1, 15]) {
      const glassX = wallX === 1 ? 2 : 14
      fill([wallX, 2, blockZ], [wallX, 6, blockZ], 'minecraft:polished_blackstone')
      fill([wallX, 3, blockZ], [wallX, 5, blockZ], 'minecraft:lava', { level: '0' })
      fill([glassX, 3, blockZ], [glassX, 5, blockZ], 'minecraft:glass')
      place(glassX, 2, blockZ, 'minecraft:polished_blackstone_bricks')
      place(glassX, 6, blockZ, 'minecraft:chiseled_polished_blackstone')
    }
  }
  for (const [stationIndex, blockZ] of crystalStations.entries()) {
    for (const side of ['left', 'right']) {
      const supportX = side === 'left' ? 2 : 14
      const displayX = side === 'left' ? 3 : 13
      fill([supportX, 4, blockZ], [supportX, 6, blockZ], 'minecraft:amethyst_block')
      place(supportX, 7, blockZ, 'minecraft:pearlescent_froglight', { axis: 'y' })
      place(displayX, 7, blockZ, 'minecraft:purple_stained_glass')
      fill([displayX, 2, blockZ], [displayX, 3 + stationIndex % 2, blockZ], 'minecraft:amethyst_block')
      place(displayX, 4 + stationIndex % 2, blockZ, 'minecraft:amethyst_cluster', { facing: 'up', waterlogged: 'false' })
      place(displayX, 6, blockZ, stationIndex % 3 === 0 ? 'minecraft:large_amethyst_bud' : 'minecraft:amethyst_cluster', { facing: side === 'left' ? 'east' : 'west', waterlogged: 'false' })
    }
  }
  for (const blockZ of [5, 12, 19, 26, 33, 36]) {
    for (const side of ['left', 'right']) {
      const blockX = side === 'left' ? 3 : 13
      place(blockX, 4, blockZ, 'minecraft:glow_lichen', { down: 'false', east: side === 'right' ? 'true' : 'false', north: 'false', south: 'false', up: 'false', west: side === 'left' ? 'true' : 'false', waterlogged: 'false' })
    }
  }
  fill([6, 3, 0], [10, 7, 2], 'minecraft:air')
  fill([6, 3, 38], [10, 7, 40], 'minecraft:air')
  for (const blockZ of [0, 1, 39, 40]) {
    fill([5, 3, blockZ], [5, 7, blockZ], 'minecraft:chiseled_stone_bricks')
    fill([11, 3, blockZ], [11, 7, blockZ], 'minecraft:chiseled_stone_bricks')
    fill([6, 8, blockZ], [10, 8, blockZ], 'minecraft:polished_andesite')
    place(8, 9, blockZ, 'minecraft:amethyst_block')
  }
  return { structure, metadata: { name: 'room', design: 'M17', size, arches, lavaStations, crystalStations } }
}
