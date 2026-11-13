export default function build(api) {
  const structure = api.createStructure([19, 11, 19])
  const put = (position, block, properties, nbt) => api.setBlock(structure, position, `minecraft:${block}`, properties, nbt)
  const box = (from, to, block, properties) => api.fill(structure, from, to, `minecraft:${block}`, properties)
  box([0, 0, 0], [18, 10, 18], 'air')
  box([0, 0, 0], [18, 0, 18], 'stone_bricks')
  for (let across = 0; across < 19; across++) {
    for (let depth = 0; depth < 19; depth++) {
      const selector = (across * 17 + depth * 31) % 29
      put([across, 1, depth], selector === 0 ? 'cracked_stone_bricks' : selector === 1 ? 'mossy_stone_bricks' : 'stone_bricks')
      put([across, 9, depth], 'stone_bricks')
      put([across, 10, depth], across % 4 === 0 || depth % 4 === 0 ? 'smooth_stone' : 'stone_bricks')
    }
  }
  for (let height = 2; height <= 8; height++) {
    for (let edge = 0; edge < 19; edge++) {
      for (const position of [[edge, height, 0], [edge, height, 18], [0, height, edge], [18, height, edge]]) {
        const selector = (position[0] * 11 + position[2] * 19 + height * 7) % 37
        put(position, selector === 0 ? 'cracked_stone_bricks' : selector === 1 && height <= 3 ? 'mossy_stone_bricks' : 'stone_bricks')
      }
    }
  }
  for (const [across, depth] of [[0, 0], [17, 0], [0, 17], [17, 17]]) {
    box([across, 2, depth], [across + 1, 8, depth + 1], 'stone_bricks')
    box([across, 3, depth], [across + 1, 3, depth + 1], 'chiseled_stone_bricks')
    box([across, 8, depth], [across + 1, 8, depth + 1], 'chiseled_stone_bricks')
  }
  for (const [across, depth] of [[5, 5], [13, 5], [5, 13], [13, 13]]) {
    box([across, 2, depth], [across, 7, depth], 'stone_bricks')
    put([across, 2, depth], 'chiseled_stone_bricks')
    put([across, 6, depth], 'chiseled_stone_bricks')
    box([across - 1, 8, depth - 1], [across + 1, 8, depth + 1], 'stone_brick_slab', {type: 'top', waterlogged: 'false'})
  }
  for (const across of [5, 13]) box([across, 8, 1], [across, 8, 17], 'stone_bricks')
  for (const depth of [5, 13]) box([1, 8, depth], [17, 8, depth], 'stone_bricks')
  for (const edge of [6, 12]) {
    box([edge, 2, 1], [edge, 7, 1], 'stone_bricks')
    box([1, 2, edge], [1, 7, edge], 'stone_bricks')
    box([edge, 2, 17], [edge, 7, 17], 'stone_bricks')
    box([17, 2, edge], [17, 7, edge], 'stone_bricks')
    for (const position of [[edge, 6, 1], [1, 6, edge], [edge, 6, 17], [17, 6, edge]]) put(position, 'chiseled_stone_bricks')
  }
  for (const across of [7, 11]) {
    box([across, 2, 0], [across, 6, 1], 'stone_bricks')
    put([across, 5, 1], 'chiseled_stone_bricks')
  }
  for (const depth of [7, 11]) {
    box([17, 2, depth], [18, 6, depth], 'stone_bricks')
    put([17, 5, depth], 'chiseled_stone_bricks')
  }
  box([7, 6, 0], [11, 6, 1], 'chiseled_stone_bricks')
  box([17, 6, 7], [18, 6, 11], 'chiseled_stone_bricks')
  box([8, 2, 0], [10, 5, 1], 'air')
  box([17, 2, 8], [18, 5, 10], 'air')
  for (let depth = 0; depth <= 10; depth++) {
    for (let across = 8; across <= 10; across++) put([across, 1, depth], 'smooth_stone')
  }
  for (let across = 8; across <= 18; across++) {
    for (let depth = 8; depth <= 10; depth++) put([across, 1, depth], 'smooth_stone')
  }
  for (const position of [[9, 1, 1], [9, 1, 9], [17, 1, 9]]) put(position, 'chiseled_stone_bricks')
  for (const edge of [3, 15]) {
    for (let height = 4; height <= 6; height++) {
      const alongX = {north: 'false', east: 'true', south: 'false', west: 'true', waterlogged: 'false'}
      const alongZ = {north: 'true', east: 'false', south: 'true', west: 'false', waterlogged: 'false'}
      put([edge, height, 0], 'iron_bars', alongX)
      put([edge, height, 18], 'iron_bars', alongX)
      put([0, height, edge], 'iron_bars', alongZ)
      put([18, height, edge], 'iron_bars', alongZ)
    }
  }
  const emptyBarrel = () => api.createBlockNbt({id: 'minecraft:barrel', Items: []})
  for (const position of [[2, 2, 3], [2, 3, 3], [3, 2, 3], [15, 2, 16], [16, 2, 16], [16, 3, 16]]) {
    put(position, 'barrel', {facing: 'up', open: 'false'}, emptyBarrel())
  }
  for (const across of [4, 7]) put([across, 2, 16], 'spruce_fence', {north: 'false', east: 'false', south: 'false', west: 'false', waterlogged: 'false'})
  box([4, 3, 16], [7, 3, 16], 'spruce_slab', {type: 'top', waterlogged: 'false'})
  put([4, 4, 16], 'lantern', {hanging: 'false', waterlogged: 'false'})
  put([6, 2, 14], 'spruce_stairs', {facing: 'south', half: 'bottom', shape: 'straight', waterlogged: 'false'})
  put([6, 3, 14], 'spruce_trapdoor', {facing: 'north', half: 'bottom', open: 'true', powered: 'false', waterlogged: 'false'})
  put([8, 2, 16], 'cartography_table')
  box([3, 2, 16], [3, 3, 16], 'bookshelf')
  for (let depth = 13; depth <= 15; depth++) {
    box([1, 2, depth], [1, 5, depth], 'spruce_planks')
    box([2, 2, depth], [2, 3, depth], 'spruce_fence', {north: depth > 13 ? 'true' : 'false', south: depth < 15 ? 'true' : 'false', west: 'true', east: 'false', waterlogged: 'false'})
    put([2, 4, depth], 'lightning_rod', {facing: 'up', powered: 'false', waterlogged: 'false'})
  }
  const banner = api.createBlockNbt({id: 'minecraft:banner', Patterns: [{Pattern: 'mr', Color: 4}, {Pattern: 'bo', Color: 4}]})
  put([10, 5, 17], 'red_wall_banner', {facing: 'north'}, banner)
  box([8, 2, 12], [11, 2, 15], 'red_carpet')
  for (const position of [[8, 2, 12], [11, 2, 12], [8, 2, 15], [11, 2, 15], [9, 2, 13], [10, 2, 14]]) put(position, 'yellow_carpet')
  box([14, 2, 13], [16, 2, 13], 'spruce_fence', {north: 'false', south: 'false', east: 'true', west: 'true', waterlogged: 'false'})
  put([17, 2, 13], 'spruce_planks')
  put([1, 7, 16], 'cobweb')
  put([2, 7, 17], 'cobweb')
  const hangingLamp = (across, depth, bottom = 6) => {
    box([across, bottom + 1, depth], [across, 8, depth], 'chain', {axis: 'y', waterlogged: 'false'})
    put([across, bottom, depth], 'lantern', {hanging: 'true', waterlogged: 'false'})
  }
  for (const [across, depth] of [[9, 3], [15, 9], [5, 9], [13, 9], [5, 16], [2, 8]]) hangingLamp(across, depth)
  return {structure, metadata: {name: 'room', id: 'C01', title: '石角岗哨', entrance: [9, 2, 0], interior: [9, 2, 3], exit: [18, 2, 9]}}
}
