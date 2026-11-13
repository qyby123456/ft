export default function build(api) {
  const size = [21, 11, 21]
  const structure = api.createStructure(size)
  const set = (position, block, properties, data) => api.setBlock(structure, position, `minecraft:${block}`, properties, data ? api.createBlockNbt(data) : undefined)
  const fill = (from, to, block, properties) => api.fill(structure, from, to, `minecraft:${block}`, properties)
  const masonry = (horizontal, vertical, depth) => {
    const wear = (horizontal * 17 + vertical * 11 + depth * 23) % 29
    return wear < 5 ? 'mossy_stone_bricks' : wear < 9 ? 'cracked_stone_bricks' : 'stone_bricks'
  }
  const slab = { type: 'top', waterlogged: 'false' }
  const stair = facing => ({ facing, half: 'bottom', shape: 'straight', waterlogged: 'false' })
  const barrel = facing => ({ facing, open: 'false' })
  const bars = { north: 'false', east: 'false', south: 'false', west: 'false', waterlogged: 'false' }
  const lantern = hanging => ({ hanging: String(hanging), waterlogged: 'false' })
  fill([0, 0, 0], [20, 10, 20], 'air')
  for (let depth = 0; depth < 21; depth += 1) {
    for (let horizontal = 0; horizontal < 21; horizontal += 1) {
      const paving = (horizontal * 3 + depth * 7) % 17
      set([horizontal, 0, depth], paving === 0 ? 'polished_andesite' : masonry(horizontal, 0, depth))
      set([horizontal, 9, depth], masonry(horizontal, 9, depth))
      const rim = horizontal < 2 || horizontal > 18 || depth < 2 || depth > 18
      set([horizontal, 10, depth], rim ? 'polished_andesite' : masonry(horizontal, 10, depth))
      if (horizontal < 2 || horizontal > 18 || depth < 2 || depth > 18) {
        for (let vertical = 1; vertical < 9; vertical += 1) {
          set([horizontal, vertical, depth], vertical === 3 ? 'red_terracotta' : masonry(horizontal, vertical, depth))
        }
      }
    }
  }
  for (const horizontal of [4, 16]) {
    for (const depth of [4, 16]) {
      fill([horizontal - 1, 1, depth - 1], [horizontal + 1, 1, depth + 1], 'polished_deepslate')
      for (let vertical = 2; vertical <= 7; vertical += 1) {
        set([horizontal, vertical, depth], vertical === 3 || vertical === 6 ? 'red_terracotta' : masonry(horizontal, vertical, depth))
      }
      fill([horizontal - 1, 7, depth - 1], [horizontal + 1, 7, depth + 1], 'stone_bricks')
      set([horizontal, 7, depth], 'chiseled_stone_bricks')
    }
    fill([horizontal, 8, 1], [horizontal, 8, 19], 'polished_andesite')
  }
  for (const depth of [4, 16]) fill([1, 8, depth], [19, 8, depth], 'polished_andesite')
  for (const horizontal of [2, 18]) {
    for (const depth of [2, 7, 13, 18]) {
      for (let vertical = 1; vertical <= 7; vertical += 1) {
        set([horizontal, vertical, depth], vertical === 1 ? 'polished_deepslate' : vertical === 3 ? 'red_terracotta' : masonry(horizontal, vertical, depth))
      }
      set([horizontal, 7, depth], 'chiseled_stone_bricks')
    }
  }
  fill([9, 1, 0], [11, 3, 1], 'air')
  for (const horizontal of [8, 12]) {
    fill([horizontal, 1, 0], [horizontal, 4, 1], 'chiseled_stone_bricks')
    set([horizontal, 3, 0], 'red_terracotta')
  }
  fill([8, 4, 0], [12, 4, 1], 'polished_andesite')
  for (let depth = 0; depth <= 6; depth += 1) {
    for (let horizontal = 9; horizontal <= 11; horizontal += 1) set([horizontal, 0, depth], depth === 0 || horizontal === 10 ? 'polished_andesite' : 'stone_bricks')
  }
  for (const horizontal of [7, 13]) fill([horizontal, 2, 19], [horizontal, 6, 19], 'stripped_spruce_log', { axis: 'y' })
  fill([7, 6, 19], [13, 6, 19], 'stripped_spruce_log', { axis: 'x' })
  fill([8, 3, 19], [12, 5, 20], 'air')
  fill([8, 3, 20], [12, 5, 20], 'iron_bars', bars)
  for (const vertical of [3, 5]) {
    for (let horizontal = 8; horizontal <= 12; horizontal += 1) {
      set([horizontal, vertical, 19], 'spruce_trapdoor', { facing: 'south', half: 'bottom', open: 'true', powered: 'false', waterlogged: 'false' })
    }
  }
  fill([8, 2, 18], [12, 2, 19], 'stone_bricks')
  fill([19, 1, 8], [19, 5, 12], 'air')
  fill([20, 1, 8], [20, 5, 12], 'dark_oak_planks')
  for (const depth of [7, 13]) fill([19, 1, depth], [19, 6, depth], 'stripped_dark_oak_log', { axis: 'y' })
  fill([19, 6, 7], [19, 6, 13], 'stripped_dark_oak_log', { axis: 'z' })
  for (const vertical of [1, 3, 5]) {
    for (let depth = 8; depth <= 12; depth += 1) {
      set([19, vertical, depth], 'spruce_trapdoor', { facing: 'east', half: 'bottom', open: 'true', powered: 'false', waterlogged: 'false' })
    }
  }
  for (const depth of [8, 11]) {
    set([2, 5, depth], 'red_wall_banner', { facing: 'east' }, { id: 'minecraft:banner', Patterns: [{ Pattern: 'cr', Color: 0 }, { Pattern: 'bo', Color: 15 }] })
  }
  set([3, 1, 9], 'chest', { facing: 'east', type: 'single', waterlogged: 'false' }, { id: 'minecraft:chest' })
  set([3, 1, 11], 'barrel', barrel('east'), { id: 'minecraft:barrel' })
  for (const depth of [5, 6]) {
    set([2, 1, depth], 'bookshelf')
    set([2, 2, depth], 'spruce_slab', slab)
  }
  for (const depth of [14, 16]) fill([2, 1, depth], [2, 3, depth], 'stripped_spruce_log', { axis: 'y' })
  set([2, 1, 15], 'stripped_spruce_log', { axis: 'z' })
  for (const vertical of [2, 3]) set([2, vertical, 15], 'iron_bars', bars)
  fill([2, 4, 14], [2, 4, 16], 'spruce_slab', slab)
  set([3, 1, 15], 'grindstone', { face: 'floor', facing: 'east' })
  for (const horizontal of [8, 12]) {
    for (const depth of [10, 12]) {
      set([horizontal, 1, depth], 'spruce_fence', { north: 'false', east: 'false', south: 'false', west: 'false', waterlogged: 'false' })
    }
  }
  fill([8, 2, 10], [12, 2, 12], 'spruce_slab', slab)
  fill([9, 2, 10], [11, 2, 11], 'birch_slab', slab)
  set([10, 3, 11], 'white_carpet')
  set([9, 3, 11], 'red_carpet')
  set([12, 3, 10], 'lantern', lantern(false))
  set([11, 3, 12], 'flower_pot')
  set([10, 1, 8], 'spruce_stairs', stair('south'))
  set([10, 1, 14], 'spruce_stairs', stair('north'))
  set([15, 1, 10], 'red_carpet')
  fill([15, 1, 11], [17, 1, 12], 'red_carpet')
  set([16, 1, 10], 'brown_carpet')
  set([17, 1, 10], 'red_carpet')
  for (const position of [[17, 1, 14], [18, 1, 14], [18, 2, 14]]) set(position, 'barrel', barrel('north'), { id: 'minecraft:barrel' })
  set([4, 1, 10], 'chiseled_stone_bricks')
  set([4, 2, 10], 'campfire', { facing: 'north', lit: 'false', signal_fire: 'false', waterlogged: 'false' }, { id: 'minecraft:campfire' })
  for (const horizontal of [4, 16]) {
    for (const depth of [7, 13]) {
      fill([horizontal, 6, depth], [horizontal, 8, depth], 'chain', { axis: 'y', waterlogged: 'false' })
      set([horizontal, 5, depth], 'lantern', lantern(true))
    }
  }
  for (const position of [[2, 7, 2], [18, 7, 2], [18, 7, 18]]) set(position, 'cobweb')
  for (const position of [[6, 1, 18], [14, 1, 18], [3, 1, 18], [17, 1, 3]]) set(position, 'mossy_stone_bricks')
  for (const position of [[5, 1, 18], [6, 1, 19], [14, 1, 19], [15, 1, 18], [18, 1, 3]]) set(position, 'cobblestone_slab', { type: 'bottom', waterlogged: 'false' })
  return { structure, metadata: { name: 'room' } }
}
