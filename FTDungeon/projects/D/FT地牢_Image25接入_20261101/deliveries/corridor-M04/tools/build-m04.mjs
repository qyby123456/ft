export default function build(api) {
  const size = [13, 12, 41]
  const structure = api.createStructure(size)
  const block = (position, name, properties) => api.setBlock(structure, position, `minecraft:${name}`, properties)
  const fill = (from, to, name, properties) => api.fill(structure, from, to, `minecraft:${name}`, properties)
  fill([0, 0, 0], [12, 11, 40], 'air')
  fill([0, 0, 0], [12, 0, 40], 'end_stone_bricks')
  fill([0, 1, 0], [0, 10, 40], 'end_stone_bricks')
  fill([12, 1, 0], [12, 10, 40], 'end_stone_bricks')
  fill([0, 11, 0], [12, 11, 40], 'purpur_block')
  for (let depth = 0; depth <= 40; depth += 1) {
    for (const side of [0, 12]) {
      fill([side, 3, depth], [side, 5, depth], 'purpur_block')
      block([side, 8, depth], 'obsidian')
      block([side, 10, depth], 'purpur_block')
    }
    for (const side of [1, 11]) {
      block([side, 1, depth], 'purpur_block')
      block([side, 9, depth], 'purpur_block')
    }
    for (let width = 2; width <= 10; width += 1) {
      if (width < 5 || width > 7) {
        block([width, 0, depth], (width + depth) % 5 === 0 ? 'prismarine_bricks' : 'end_stone_bricks')
      }
    }
    for (let width = 5; width <= 7; width += 1) {
      block([width, 1, depth], width === 6 ? 'end_stone_bricks' : 'purpur_block')
    }
    if (depth % 8 === 4) block([6, 1, depth], 'sea_lantern')
  }
  for (const depth of [0, 1, 39, 40]) fill([1, 1, depth], [11, 1, depth], 'end_stone_bricks')
  fill([2, 1, 2], [4, 1, 38], 'water', { level: '0' })
  fill([8, 1, 2], [10, 1, 38], 'water', { level: '0' })
  for (const depth of [0, 8, 16, 24, 32, 40]) {
    for (const side of [1, 11]) {
      fill([side, 2, depth], [side, 9, depth], 'purpur_pillar', { axis: 'y' })
      fill([side, 7, depth], [side, 8, depth], 'obsidian')
      block([side, 2, depth], 'end_stone_bricks')
    }
    fill([1, 10, depth], [11, 10, depth], 'purpur_pillar', { axis: 'x' })
    for (const width of [2, 3, 9, 10]) block([width, 11, depth], 'end_stone_bricks')
  }
  for (const depth of [0, 40]) {
    fill([1, 2, depth], [11, 9, depth], 'end_stone_bricks')
    fill([3, 2, depth], [9, 7, depth], 'purpur_block')
    fill([4, 2, depth], [4, 5, depth], 'purpur_pillar', { axis: 'y' })
    fill([8, 2, depth], [8, 5, depth], 'purpur_pillar', { axis: 'y' })
    fill([5, 2, depth], [7, 5, depth], 'air')
    fill([5, 6, depth], [7, 6, depth], 'purpur_pillar', { axis: 'x' })
    block([6, 7, depth], 'sea_lantern')
  }
  for (const depth of [4, 12, 20, 28, 36]) {
    for (const side of [1, 11]) {
      block([side, 6, depth], 'purpur_block')
      block([side, 5, depth], 'end_rod', { facing: 'down' })
      block([side, 4, depth], 'end_rod', { facing: 'down' })
      block([side, 2, depth], 'mossy_stone_bricks')
    }
  }
  for (const depth of [8, 16, 24, 32]) {
    fill([6, 8, depth], [6, 9, depth], 'chain', { axis: 'y', waterlogged: 'false' })
    block([6, 7, depth], 'lantern', { hanging: 'true', waterlogged: 'false' })
  }
  for (const [side, depth] of [[2, 5], [9, 9], [2, 13], [9, 17], [2, 21], [9, 25], [2, 29], [9, 33]]) {
    fill([side, 6, depth], [side + 1, 6, depth + 1], 'purpur_slab', { type: 'top', waterlogged: 'false' })
    fill([side, 7, depth], [side + 1, 8, depth + 1], 'iron_bars', {
      north: 'false', south: 'false', east: 'false', west: 'false', waterlogged: 'false',
    })
    fill([side, 9, depth], [side + 1, 9, depth + 1], 'purpur_slab', { type: 'top', waterlogged: 'false' })
    block([side, 10, depth], 'chain', { axis: 'y', waterlogged: 'false' })
  }
  for (const depth of [3, 11, 19, 27, 35]) {
    for (const side of [1, 11]) {
      fill([side, 2, depth], [side, 4, depth], 'vine', {
        north: 'false', south: 'false', east: side === 11 ? 'true' : 'false',
        west: side === 1 ? 'true' : 'false', up: 'false',
      })
      block([side, 1, depth], 'mossy_stone_bricks')
    }
  }
  return {
    structure,
    metadata: {
      name: 'room', id: 'corridor-M04', title: '末地淹水吊笼长廊', size,
      straightAxis: '+Z', dryRoute: { width: 3, from: [5, 2, 0], to: [7, 2, 40] },
      design: 'Cream end-stone, lavender purpur, obsidian bands, white/cyan end-rod accents, warm lanterns, eight empty hanging cages and two contained shallow pools.',
    },
  }
}
