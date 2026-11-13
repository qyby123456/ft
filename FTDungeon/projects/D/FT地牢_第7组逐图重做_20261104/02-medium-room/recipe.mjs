export default function build(api) {
  const structure = api.createStructure([23, 14, 23])
  const place = (blockX, blockY, blockZ, identifier, properties = {}, data) => {
    const defaults = {}
    if (['mycelium', 'podzol'].includes(identifier)) defaults.snowy = 'false'
    if (['mushroom_stem', 'red_mushroom_block', 'brown_mushroom_block'].includes(identifier)) {
      for (const face of ['north', 'east', 'south', 'west', 'up', 'down']) defaults[face] = 'true'
    }
    api.setBlock(structure, [blockX, blockY, blockZ], `minecraft:${identifier}`, { ...defaults, ...properties }, data)
  }
  const box = (start, end, identifier, properties) => {
    for (let blockX = start[0]; blockX <= end[0]; blockX++) {
      for (let blockY = start[1]; blockY <= end[1]; blockY++) {
        for (let blockZ = start[2]; blockZ <= end[2]; blockZ++) place(blockX, blockY, blockZ, identifier, properties)
      }
    }
  }
  box([0, 0, 0], [22, 13, 22], 'air')
  box([0, 0, 0], [22, 0, 22], 'deepslate_bricks')
  for (let blockX = 0; blockX <= 22; blockX++) {
    for (let blockZ = 0; blockZ <= 22; blockZ++) {
      const patch = (blockX * 19 + blockZ * 37) % 29
      const walking = Math.abs(blockX - 11) < 2 || blockX >= 7 && blockX <= 15 && blockZ >= 7 && blockZ <= 15
      place(blockX, 1, blockZ, walking ? patch < 6 ? 'purple_terracotta' : 'stone_bricks' : patch < 14 ? 'mycelium' : patch < 22 ? 'rooted_dirt' : 'podzol')
      if (blockX === 0 || blockX === 22 || blockZ === 0 || blockZ === 22) {
        for (let blockY = 2; blockY <= 12; blockY++) {
          const wear = (blockX * 7 + blockY * 13 + blockZ * 17) % 23
          place(blockX, blockY, blockZ, wear < 4 ? 'mossy_stone_bricks' : wear < 7 ? 'cracked_deepslate_bricks' : 'deepslate_bricks')
        }
      }
      place(blockX, 13, blockZ, 'deepslate_tiles')
    }
  }
  for (let blockZ = 1; blockZ <= 21; blockZ++) {
    for (const blockX of [1, 21]) {
      place(blockX, 10, blockZ, 'rooted_dirt')
      place(blockX, 11, blockZ, 'mycelium')
      if ((blockZ + blockX) % 3 === 0) place(blockX, 12, blockZ, 'mossy_stone_bricks')
    }
  }
  box([1, 10, 1], [21, 10, 1], 'rooted_dirt')
  box([1, 11, 1], [21, 11, 1], 'mycelium')
  for (const blockX of [1, 21]) {
    for (const blockZ of [7, 13]) {
      box([blockX, 2, blockZ], [blockX, 8, blockZ], 'mossy_stone_bricks')
    }
    box([blockX, 6, 7], [blockX, 6, 13], 'dark_oak_log', { axis: 'z' })
    for (const blockZ of [9, 11]) {
      place(blockX, 2, blockZ, 'barrel', { facing: blockX === 1 ? 'east' : 'west', open: 'false' }, api.createBlockNbt({ id: 'minecraft:barrel' }))
    }
    place(blockX, 2, 10, 'crafting_table')
    box([blockX, 3, 8], [blockX, 3, 12], 'dark_oak_slab', { type: 'top', waterlogged: 'false' })
    place(blockX, 4, 10, 'lantern', { hanging: 'false', waterlogged: 'false' })
    place(blockX, 4, 8, 'potted_red_mushroom')
  }
  for (const blockX of [9, 13]) {
    for (const blockZ of [9, 13]) place(blockX, 2, blockZ, 'polished_blackstone_bricks')
  }
  box([9, 3, 9], [13, 3, 13], 'polished_blackstone_slab', { type: 'top', waterlogged: 'false' })
  place(11, 3, 11, 'amethyst_block')
  place(11, 4, 11, 'amethyst_cluster', { facing: 'up', waterlogged: 'false' })
  for (const blockX of [9, 13]) {
    for (const blockZ of [9, 13]) place(blockX, 4, blockZ, 'potted_red_mushroom')
  }
  const growMushroom = (baseX, baseZ, stem, radius) => {
    box([baseX, 2, baseZ], [baseX, stem + 1, baseZ], 'mushroom_stem')
    for (let offsetX = -radius; offsetX <= radius; offsetX++) {
      for (let offsetZ = -radius; offsetZ <= radius; offsetZ++) {
        if (Math.abs(offsetX) === radius && Math.abs(offsetZ) === radius) continue
        place(baseX + offsetX, stem + 2, baseZ + offsetZ, 'red_mushroom_block')
        if (Math.abs(offsetX) === radius || Math.abs(offsetZ) === radius) place(baseX + offsetX, stem + 1, baseZ + offsetZ, 'red_mushroom_block')
        if (Math.abs(offsetX) <= radius - 1 && Math.abs(offsetZ) <= radius - 1) place(baseX + offsetX, stem + 3, baseZ + offsetZ, 'red_mushroom_block')
      }
    }
    place(baseX, stem, baseZ, 'shroomlight')
  }
  growMushroom(4, 4, 7, 3)
  growMushroom(18, 4, 7, 3)
  growMushroom(4, 18, 3, 3)
  growMushroom(19, 9, 4, 2)
  for (const [baseX, baseZ, poolWidth, poolDepth] of [[10, 18, 3, 2], [18, 14, 2, 2]]) {
    box([baseX - 1, 1, baseZ - 1], [baseX + poolWidth, 1, baseZ + poolDepth], 'polished_blackstone_bricks')
    box([baseX, 1, baseZ], [baseX + poolWidth - 1, 1, baseZ + poolDepth - 1], 'water', { level: '0' })
  }
  for (const [blockX, blockZ] of [[3, 14], [18, 12], [7, 4], [16, 4], [7, 18], [19, 19]]) {
    place(blockX, 1, blockZ, 'amethyst_block')
    place(blockX, 2, blockZ, 'amethyst_cluster', { facing: 'up', waterlogged: 'false' })
  }
  for (const [blockX, blockZ] of [[2, 17], [20, 17], [7, 6], [15, 5], [8, 20]]) {
    place(blockX, 1, blockZ, 'mycelium')
    place(blockX, 2, blockZ, 'soul_lantern', { hanging: 'false', waterlogged: 'false' })
  }
  for (const [blockX, blockZ] of [[3, 7], [5, 8], [7, 17], [5, 21], [8, 18], [14, 19], [16, 17], [18, 17], [20, 6], [16, 7]]) {
    place(blockX, 1, blockZ, 'mycelium')
    place(blockX, 2, blockZ, (blockX + blockZ) % 2 ? 'red_mushroom' : 'brown_mushroom')
  }
  for (const blockX of [1, 21]) {
    for (const blockZ of [13, 16, 20]) {
      for (let blockY = 6; blockY <= 9; blockY++) {
        place(blockX, blockY, blockZ, 'vine', { [blockX === 1 ? 'west' : 'east']: 'true', up: 'false' })
      }
    }
  }
  for (const blockX of [7, 15]) {
    box([blockX, 2, 0], [blockX, 7, 0], 'stone_bricks')
    place(blockX, 4, 1, 'glow_lichen', { north: 'true', waterlogged: 'false' })
  }
  for (let lateral = -2; lateral <= 2; lateral++) {
    for (let blockY = 2; blockY <= 7; blockY++) {
      const open = Math.abs(lateral) <= 1 && blockY <= 5 || lateral === 0 && blockY === 6
      place(11 + lateral, blockY, 0, open ? 'air' : 'stone_bricks')
    }
    place(11 + lateral, 1, 0, 'stone_bricks')
  }
  for (const blockX of [8, 14]) {
    place(blockX, 6, 0, 'stone_bricks')
    place(blockX, 6, 1, 'lantern', { hanging: 'true', waterlogged: 'false' })
    place(blockX, 7, 1, 'dark_oak_log', { axis: 'z' })
    place(blockX, 7, 0, 'dark_oak_log', { axis: 'z' })
  }
  return { structure, metadata: { name: 'room' } }
}
