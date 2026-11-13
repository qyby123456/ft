export default function build(api) {
  const structure = api.createStructure([19, 14, 29])
  const put = (east, up, south, block, properties) => api.setBlock(structure, [east, up, south], block, properties)
  const fill = (from, to, block, properties) => api.fill(structure, from, to, block, properties)
  fill([0, 0, 0], [18, 13, 28], 'minecraft:air')
  fill([0, 0, 0], [18, 0, 28], 'minecraft:deepslate_bricks')
  fill([0, 1, 0], [18, 1, 28], 'minecraft:stone_bricks')
  fill([0, 2, 0], [18, 4, 6], 'minecraft:stone_bricks')
  fill([0, 2, 24], [18, 4, 28], 'minecraft:stone_bricks')
  fill([0, 2, 7], [5, 4, 23], 'minecraft:stone_bricks')
  fill([13, 2, 7], [18, 4, 23], 'minecraft:stone_bricks')
  fill([0, 2, 0], [0, 12, 28], 'minecraft:stone_bricks')
  fill([18, 2, 0], [18, 12, 28], 'minecraft:stone_bricks')
  fill([1, 5, 0], [17, 12, 0], 'minecraft:stone_bricks')
  fill([1, 5, 28], [17, 12, 28], 'minecraft:stone_bricks')
  fill([0, 13, 0], [18, 13, 28], 'minecraft:stone_bricks')
  for (const wallX of [0, 18]) {
    for (let south = 1; south < 28; south++) {
      for (let up = 3; up < 12; up++) {
        const motif = (south * 11 + up * 7 + wallX * 3) % 31
        if (motif === 0) put(wallX, up, south, 'minecraft:cracked_stone_bricks')
        if (motif === 1 && up <= 6) put(wallX, up, south, 'minecraft:mossy_stone_bricks')
      }
    }
  }
  for (const south of [0, 28]) {
    fill([8, 5, south], [10, 8, south], 'minecraft:air')
    for (const east of [7, 11]) fill([east, 5, south], [east, 9, south], 'minecraft:chiseled_stone_bricks')
    fill([7, 9, south], [11, 9, south], 'minecraft:polished_andesite')
  }
  for (const south of [5, 12, 20, 26]) {
    for (const east of [2, 15]) {
      fill([east, 5, south], [east + 1, 5, south + 1], 'minecraft:chiseled_stone_bricks')
      fill([east, 6, south], [east + 1, 10, south + 1], 'minecraft:stone_bricks')
      fill([east, 11, south], [east + 1, 12, south + 1], 'minecraft:polished_andesite')
    }
    fill([1, 12, south], [17, 12, south + 1], 'minecraft:polished_andesite')
    for (const east of [4, 14]) {
      put(east, 11, south, 'minecraft:chain', { axis: 'y', waterlogged: 'false' })
      put(east, 10, south, 'minecraft:chain', { axis: 'y', waterlogged: 'false' })
      put(east, 9, south, 'minecraft:lantern', { hanging: 'true', waterlogged: 'false' })
    }
  }
  for (const east of [5, 13]) fill([east, 5, 7], [east, 7, 23], 'minecraft:iron_bars')
  for (const [fromX, toX] of [[1, 5], [13, 17]]) {
    fill([fromX, 5, 15], [toX, 12, 15], 'minecraft:stone_bricks')
    fill([fromX, 7, 15], [toX, 7, 15], 'minecraft:chiseled_stone_bricks')
  }
  for (const startZ of [9, 14, 19]) {
    fill([8, 2, startZ], [10, 3, startZ + 2], 'minecraft:stone_bricks')
    fill([8, 4, startZ], [10, 4, startZ + 2], 'minecraft:polished_andesite')
    for (const east of [8, 10]) {
      for (const south of [startZ, startZ + 2]) put(east, 4, south, 'minecraft:chiseled_stone_bricks')
    }
  }
  for (const [fromX, toX] of [[6, 7], [11, 12]]) {
    fill([fromX, 2, 8], [toX, 2, 8], 'minecraft:polished_andesite')
    fill([fromX, 2, 7], [toX, 3, 7], 'minecraft:polished_andesite')
    for (let south = 9; south <= 23; south++) {
      for (let east = fromX; east <= toX; east++) {
        put(east, 1, south, south % 3 === 0 ? 'minecraft:calcite' : 'minecraft:polished_andesite')
      }
    }
  }
  for (const south of [7, 8, 12, 13, 17, 18, 22, 23]) {
    for (let east = 8; east <= 10; east++) put(east, 1, south, 'minecraft:deepslate_tiles')
  }
  for (const south of [2, 3, 4, 25, 26]) put(9, 4, south, 'minecraft:calcite')
  for (const east of [8, 10]) {
    put(east, 4, 4, 'minecraft:calcite')
    put(east, 4, 25, 'minecraft:calcite')
  }
  for (const [east, south] of [[1, 3], [2, 3], [16, 24], [17, 24]]) put(east, 5, south, 'minecraft:oak_planks')
  for (const [east, south] of [[1, 3], [17, 24]]) put(east, 6, south, 'minecraft:spruce_planks')
  for (const [east, facing] of [[1, 'east'], [17, 'west']]) {
    for (const south of [9, 22]) put(east, 8, south, 'minecraft:wall_torch', { facing })
  }
  for (const [east, south] of [[3, 25], [15, 3]]) {
    put(east, 5, south, 'minecraft:chiseled_stone_bricks')
    put(east, 6, south, 'minecraft:lantern', { hanging: 'false', waterlogged: 'false' })
  }
  return {
    structure,
    metadata: {
      name: 'room', id: 'P01', title: '跳坑跑酷 · 石柱试步厅',
      reference: 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/parkour/P01-parkour-gap-jump-pit.png',
      reference_sha256: 'd8809777350e5a681cd94c59af4d328631e02ef5bc2226f1ebe859a6e8de7603'
    }
  }
}
