export default function build(api) {
  const structure = api.createStructure([11, 10, 16])
  const set = (blockX, height, blockZ, block, properties) => api.setBlock(structure, [blockX, height, blockZ], block, properties)
  const fill = (from, to, block, properties) => api.fill(structure, from, to, block, properties)
  const masonry = (blockX, height, blockZ) => {
    const sample = (blockX * 37 + height * 19 + blockZ * 43 + blockX * blockZ * 7) % 29
    if (sample < 10) return 'minecraft:mossy_stone_bricks'
    if (sample < 16) return 'minecraft:cracked_stone_bricks'
    if (sample === 17) return 'minecraft:mossy_cobblestone'
    return 'minecraft:stone_bricks'
  }
  fill([0, 0, 0], [10, 9, 15], 'minecraft:air')
  for (let blockX = 0; blockX < 11; blockX += 1) {
    for (let blockZ = 0; blockZ < 16; blockZ += 1) {
      set(blockX, 0, blockZ, masonry(blockX, 0, blockZ))
      set(blockX, 9, blockZ, masonry(blockX, 9, blockZ))
    }
  }
  for (let height = 1; height < 9; height += 1) {
    for (let blockZ = 0; blockZ < 16; blockZ += 1) {
      for (const blockX of [0, 1, 9, 10]) set(blockX, height, blockZ, masonry(blockX, height, blockZ))
    }
  }
  for (const blockX of [1, 9]) {
    for (const blockZ of [3, 8, 12]) fill([blockX, 1, blockZ], [blockX, 8, blockZ], 'minecraft:dark_oak_log', { axis: 'y' })
  }
  for (const blockZ of [0, 5, 10, 15]) {
    for (const blockX of [2, 8]) {
      fill([blockX, 1, blockZ], [blockX, 6, blockZ], 'minecraft:mossy_stone_bricks')
      set(blockX, 7, blockZ, 'minecraft:chiseled_stone_bricks')
    }
    for (const blockX of [3, 7]) set(blockX, 6, blockZ, 'minecraft:mossy_stone_brick_stairs', { facing: blockX === 3 ? 'west' : 'east', half: 'top', shape: 'straight', waterlogged: 'false' })
    fill([3, 7, blockZ], [7, 7, blockZ], 'minecraft:stone_bricks')
  }
  for (const blockZ of [0, 3, 5, 8, 10, 12, 15]) fill([1, 8, blockZ], [9, 8, blockZ], 'minecraft:dark_oak_log', { axis: 'x' })
  for (const blockZ of [0, 5, 10, 15]) {
    for (const blockX of [3, 7]) {
      set(blockX, 6, blockZ, 'minecraft:chain', { axis: 'y', waterlogged: 'false' })
      set(blockX, 5, blockZ, 'minecraft:soul_lantern', { hanging: 'true', waterlogged: 'false' })
    }
  }
  for (const [startX, startZ, anchorX] of [[2, 2, 2], [7, 7, 8], [2, 11, 2]]) {
    for (let blockX = startX; blockX < startX + 2; blockX += 1) {
      for (let blockZ = startZ; blockZ < startZ + 2; blockZ += 1) {
        set(blockX, 4, blockZ, 'minecraft:iron_trapdoor', { facing: 'north', half: 'bottom', open: 'false', powered: 'false', waterlogged: 'false' })
        set(blockX, 5, blockZ, 'minecraft:iron_bars', { north: 'false', east: 'false', south: 'false', west: 'false', waterlogged: 'false' })
        set(blockX, 6, blockZ, 'minecraft:iron_trapdoor', { facing: 'north', half: 'top', open: 'false', powered: 'false', waterlogged: 'false' })
      }
    }
    set(anchorX, 7, startZ + 1, 'minecraft:chain', { axis: 'y', waterlogged: 'false' })
  }
  for (const blockX of [2, 8]) {
    for (let blockZ = 1; blockZ < 15; blockZ += 1) {
      if ([5, 10].includes(blockZ)) continue
      const lowest = 1 + ((blockZ * 5 + blockX) % 3)
      for (let height = lowest; height <= 7; height += 1) {
        const current = structure.getBlock([blockX, height, blockZ])
        if (current?.state?.getName().toString() !== 'minecraft:air') continue
        set(blockX, height, blockZ, 'minecraft:vine', { north: 'false', south: 'false', up: 'false', west: blockX === 2 ? 'true' : 'false', east: blockX === 8 ? 'true' : 'false' })
      }
    }
  }
  for (const [blockX, blockZ] of [[4, 4], [5, 4], [6, 9], [5, 11], [4, 13], [7, 4]]) set(blockX, 8, blockZ, 'minecraft:vine', { north: 'false', east: 'false', south: 'false', west: 'false', up: 'true' })
  for (const [blockX, blockZ] of [[2, 4], [8, 9], [2, 13], [8, 3]]) set(blockX, 1, blockZ, 'minecraft:mossy_stone_brick_stairs', { facing: blockX === 2 ? 'west' : 'east', half: 'bottom', shape: 'straight', waterlogged: 'false' })
  for (const [blockX, blockZ] of [[3, 6], [7, 9], [3, 13], [7, 2]]) {
    set(blockX, 0, blockZ, 'minecraft:moss_block')
    set(blockX, 1, blockZ, 'minecraft:moss_carpet')
  }
  return { structure, metadata: { name: 'room', id: 'corridor-S03', theme: 'overgrown-ruins', lengthBlocks: 16 } }
}
