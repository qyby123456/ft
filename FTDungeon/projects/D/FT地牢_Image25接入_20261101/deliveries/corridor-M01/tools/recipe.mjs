export default function build(api) {
  const size = [15, 13, 40]
  const structure = api.createStructure(size)
  const put = (east, up, south, block) => api.setBlock(structure, [east, up, south], block)
  const fill = (start, end, block) => api.fill(structure, start, end, block)
  fill([0, 0, 0], [14, 12, 39], 'minecraft:air')
  fill([0, 0, 0], [14, 0, 39], 'minecraft:blackstone')
  fill([0, 1, 0], [14, 1, 39], 'minecraft:polished_blackstone_bricks')
  fill([0, 2, 0], [1, 11, 39], 'minecraft:red_nether_bricks')
  fill([13, 2, 0], [14, 11, 39], 'minecraft:red_nether_bricks')
  fill([0, 12, 0], [14, 12, 39], 'minecraft:nether_bricks')
  fill([2, 11, 0], [12, 11, 39], 'minecraft:red_nether_bricks')
  for (let south = 0; south < 40; south++) {
    for (const east of [0, 1, 13, 14]) {
      put(east, 2, south, 'minecraft:polished_blackstone_bricks')
      put(east, 3, south, 'minecraft:polished_blackstone_bricks')
      put(east, 10, south, 'minecraft:polished_blackstone_bricks')
      if (south % 4 === 2) put(east, 6, south, 'minecraft:chiseled_nether_bricks')
    }
    for (let east = 5; east <= 9; east++) {
      const redRunner = east >= 6 && east <= 8
      const stone = redRunner
        ? (south % 4 === 0 ? 'minecraft:nether_bricks' : 'minecraft:red_nether_bricks')
        : (south % 4 === 0 ? 'minecraft:polished_blackstone' : 'minecraft:polished_blackstone_bricks')
      put(east, 1, south, stone)
    }
    for (const east of [2, 12]) {
      put(east, 2, south, 'minecraft:polished_blackstone_brick_slab[type=bottom,waterlogged=false]')
      put(east, 10, south, 'minecraft:nether_brick_stairs[facing=' + (east === 2 ? 'west' : 'east') + ',half=top,shape=straight,waterlogged=false]')
    }
    if (south >= 2 && south <= 37 && south % 8 >= 2 && south % 8 <= 6) {
      for (const east of [4, 10]) put(east, 1, south, 'minecraft:lava[level=0]')
    }
  }
  for (const south of [0, 8, 16, 24, 32, 39]) {
    for (const east of [2, 12]) {
      fill([east, 2, south], [east, 9, south], 'minecraft:nether_bricks')
      fill([east, 2, south], [east, 3, south], 'minecraft:polished_blackstone_bricks')
      put(east, 4, south, 'minecraft:chiseled_polished_blackstone')
      put(east, 9, south, 'minecraft:polished_blackstone_bricks')
    }
    for (const east of [3, 11]) {
      fill([east, 2, south], [east, 3, south], 'minecraft:polished_blackstone_bricks')
      fill([east, 8, south], [east, 10, south], 'minecraft:polished_blackstone_bricks')
    }
    for (const east of [4, 10]) {
      put(east, 8, south, 'minecraft:polished_blackstone_brick_stairs[facing=' + (east === 4 ? 'west' : 'east') + ',half=top,shape=straight,waterlogged=false]')
      fill([east, 9, south], [east, 11, south], 'minecraft:polished_blackstone_bricks')
    }
    for (const east of [5, 9]) put(east, 9, south, 'minecraft:polished_blackstone_brick_stairs[facing=' + (east === 5 ? 'west' : 'east') + ',half=top,shape=straight,waterlogged=false]')
    fill([5, 10, south], [9, 11, south], 'minecraft:polished_blackstone_bricks')
    fill([0, 12, south], [14, 12, south], 'minecraft:polished_blackstone_bricks')
    for (const east of [0, 14]) fill([east, 2, south], [east, 11, south], 'minecraft:polished_blackstone_bricks')
  }
  for (const south of [0, 39]) {
    fill([2, 2, south], [4, 7, south], 'minecraft:nether_bricks')
    fill([10, 2, south], [12, 7, south], 'minecraft:nether_bricks')
    fill([3, 2, south], [4, 3, south], 'minecraft:polished_blackstone_bricks')
    fill([10, 2, south], [11, 3, south], 'minecraft:polished_blackstone_bricks')
    fill([5, 2, south], [9, 8, south], 'minecraft:air')
  }
  for (const south of [4, 12, 20, 28, 36]) {
    for (const east of [3, 11]) {
      put(east, 11, south, 'minecraft:polished_blackstone_bricks')
      fill([east, 8, south], [east, 10, south], 'minecraft:chain[axis=y,waterlogged=false]')
      put(east, 7, south, 'minecraft:lantern[hanging=true,waterlogged=false]')
    }
  }
  return { structure, metadata: { name: 'room' } }
}
