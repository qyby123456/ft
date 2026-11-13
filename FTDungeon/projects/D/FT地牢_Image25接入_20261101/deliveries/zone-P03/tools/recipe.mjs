export default function build(api) {
  const structure = api.createStructure([27, 13, 29])
  const put = (position, block, properties) => api.setBlock(structure, position, `minecraft:${block}`, properties)
  const box = (from, to, block, properties) => api.fill(structure, from, to, `minecraft:${block}`, properties)
  box([0, 0, 0], [26, 12, 28], 'air')
  box([0, 0, 0], [26, 1, 28], 'polished_blackstone')
  box([0, 2, 0], [1, 10, 28], 'polished_blackstone_bricks')
  box([25, 2, 0], [26, 10, 28], 'polished_blackstone_bricks')
  box([2, 2, 0], [24, 10, 1], 'polished_blackstone_bricks')
  box([2, 2, 27], [24, 10, 28], 'polished_blackstone_bricks')
  box([0, 11, 0], [26, 12, 28], 'polished_blackstone_bricks')
  box([2, 2, 2], [24, 2, 26], 'glass')
  box([2, 2, 0], [24, 4, 5], 'polished_blackstone_bricks')
  box([2, 2, 23], [24, 4, 28], 'polished_blackstone_bricks')
  box([5, 1, 6], [21, 1, 22], 'lava', { level: '0' })
  for (const up of [4, 9]) {
    box([0, up, 0], [1, up, 28], 'red_nether_bricks')
    box([25, up, 0], [26, up, 28], 'red_nether_bricks')
    box([2, up, 0], [24, up, 1], 'red_nether_bricks')
    box([2, up, 27], [24, up, 28], 'red_nether_bricks')
  }
  for (const south of [3, 10, 18, 25]) {
    for (const east of [2, 23]) {
      box([east, 3, south], [east + 1, 10, south + 1], 'polished_basalt', { axis: 'y' })
      box([east, 4, south], [east + 1, 4, south + 1], 'red_nether_bricks')
      box([east, 9, south], [east + 1, 10, south + 1], 'red_nether_bricks')
      box([east, 11, south], [east + 1, 11, south + 1], 'chiseled_polished_blackstone')
    }
    box([4, 10, south], [22, 10, south], 'red_nether_bricks')
    box([4, 9, south], [5, 9, south], 'polished_blackstone_bricks')
    box([21, 9, south], [22, 9, south], 'polished_blackstone_bricks')
  }
  for (const south of [4, 12, 20, 24]) {
    for (const east of [6, 20]) {
      box([east, 9, south], [east, 10, south], 'chain', { axis: 'y', waterlogged: 'false' })
      put([east, 8, south], 'lantern', { hanging: 'true', waterlogged: 'false' })
    }
  }
  for (const [east, south] of [[10, 7], [13, 11], [10, 15], [13, 19]]) {
    box([east, 3, south], [east + 2, 3, south + 2], 'polished_basalt', { axis: 'y' })
    box([east, 4, south], [east + 2, 4, south + 2], 'polished_blackstone_bricks')
    put([east + 1, 4, south + 1], 'gilded_blackstone')
    put([east + 1, 3, south], 'shroomlight')
  }
  for (const south of [5, 23]) {
    box([8, 4, south], [18, 4, south], 'red_nether_bricks')
    for (const east of [10, 13, 16]) put([east, 4, south], 'gilded_blackstone')
  }
  for (const east of [5, 6, 7]) {
    put([east, 3, 6], 'polished_blackstone_brick_stairs', { facing: 'north', half: 'bottom', shape: 'straight', waterlogged: 'false' })
    put([east, 4, 5], 'polished_blackstone_brick_stairs', { facing: 'north', half: 'bottom', shape: 'straight', waterlogged: 'false' })
  }
  for (const south of [3, 25]) {
    for (const east of [5, 21]) {
      put([east, 5, south], 'chiseled_polished_blackstone')
      put([east, 6, south], 'polished_blackstone_slab', { type: 'bottom', waterlogged: 'false' })
    }
  }
  for (const south of [1, 27]) {
    for (const east of [8, 18]) {
      box([east, 5, south], [east, 8, south], 'red_nether_bricks')
      put([east, 7, south], 'shroomlight')
    }
    box([11, 9, south], [15, 9, south], 'red_nether_bricks')
  }
  box([12, 5, 0], [14, 8, 1], 'air')
  box([12, 5, 27], [14, 8, 28], 'air')
  for (const south of [0, 28]) {
    box([11, 5, south], [11, 8, south], 'chiseled_polished_blackstone')
    box([15, 5, south], [15, 8, south], 'chiseled_polished_blackstone')
    box([11, 9, south], [15, 9, south], 'red_nether_bricks')
  }
  return { structure, metadata: { name: 'room', id: 'P03', name_zh: '熔岩泡跳台' } }
}
