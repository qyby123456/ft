export default function build(api) {
  const size = [13, 10, 16]
  const structure = api.createStructure(size)
  const put = (blockX, height, blockZ, block, properties) => api.setBlock(structure, [blockX, height, blockZ], block, properties)
  const fill = (from, to, block, properties) => api.fill(structure, from, to, block, properties)
  const weather = (blockX, height, blockZ) => (blockX * 17 + height * 11 + blockZ * 7) % 23
  fill([0, 0, 0], [12, 9, 15], 'minecraft:air')
  for (let blockX = 0; blockX < size[0]; blockX++) {
    for (let blockZ = 0; blockZ < size[2]; blockZ++) {
      let floor = 'minecraft:end_stone_bricks'
      if (blockX <= 3 || blockX >= 9) floor = 'minecraft:purpur_block'
      if (blockZ % 5 === 0 && (blockX === 4 || blockX === 8)) floor = 'minecraft:obsidian'
      if ((blockZ % 5 === 2 && blockX === 6) || (blockZ % 5 === 3 && (blockX === 4 || blockX === 8))) floor = 'minecraft:purpur_block'
      if ((blockX === 3 || blockX === 9) && blockZ % 5 === 4) floor = 'minecraft:moss_block'
      put(blockX, 0, blockZ, floor)
      put(blockX, 8, blockZ, (blockX >= 5 && blockX <= 7 && blockZ % 5 !== 0) ? 'minecraft:purpur_block' : 'minecraft:end_stone_bricks')
      put(blockX, 9, blockZ, (blockX === 0 || blockX === 12 || blockZ === 0 || blockZ === 15) ? 'minecraft:purpur_block' : 'minecraft:end_stone_bricks')
    }
  }
  for (const wallX of [0, 1, 2, 10, 11, 12]) {
    for (let height = 1; height <= 7; height++) {
      for (let blockZ = 0; blockZ < 16; blockZ++) {
        let block = height === 1 || height === 6 ? 'minecraft:purpur_block' : 'minecraft:end_stone_bricks'
        if (height >= 2 && height <= 5 && weather(wallX, height, blockZ) === 0) block = 'minecraft:end_stone'
        if ((wallX === 2 || wallX === 10) && weather(wallX, height, blockZ) <= 1) block = 'minecraft:moss_block'
        put(wallX, height, blockZ, block)
      }
    }
  }
  for (const endZ of [0, 15]) {
    fill([0, 1, endZ], [12, 7, endZ], 'minecraft:end_stone_bricks')
    fill([4, 1, endZ], [8, 5, endZ], 'minecraft:air')
    fill([4, 6, endZ], [8, 6, endZ], 'minecraft:purpur_block')
    put(6, 7, endZ, 'minecraft:obsidian')
  }
  for (const archZ of [0, 5, 10, 15]) {
    for (const pierX of [3, 9]) {
      put(pierX, 1, archZ, 'minecraft:obsidian')
      fill([pierX, 2, archZ], [pierX, 4, archZ], 'minecraft:purpur_pillar', { axis: 'y' })
      put(pierX, 5, archZ, 'minecraft:purpur_block')
      put(pierX, 6, archZ, 'minecraft:end_stone_bricks')
      put(pierX, 7, archZ, 'minecraft:obsidian')
    }
    put(4, 5, archZ, 'minecraft:purpur_stairs', { facing: 'west', half: 'top', shape: 'straight', waterlogged: 'false' })
    put(8, 5, archZ, 'minecraft:purpur_stairs', { facing: 'east', half: 'top', shape: 'straight', waterlogged: 'false' })
    fill([4, 6, archZ], [8, 6, archZ], 'minecraft:end_stone_bricks')
    fill([5, 5, archZ], [7, 5, archZ], 'minecraft:purpur_slab', { type: 'top', waterlogged: 'false' })
    fill([4, 7, archZ], [8, 7, archZ], 'minecraft:purpur_block')
    if (archZ === 5 || archZ === 10) {
      put(archZ === 5 ? 4 : 8, 6, archZ, 'minecraft:moss_block')
      put(archZ === 5 ? 5 : 7, 5, archZ, 'minecraft:air')
    }
  }
  for (const bayZ of [2, 7, 12]) {
    for (const side of [{ inner: 3, wall: 2, facing: 'east' }, { inner: 9, wall: 10, facing: 'west' }]) {
      fill([side.wall, 2, bayZ], [side.wall, 4, bayZ + 1], 'minecraft:obsidian')
      put(side.inner, 1, bayZ, 'minecraft:purpur_block')
      put(side.inner, 2, bayZ, 'minecraft:iron_bars', { waterlogged: 'false' })
      put(side.inner, 3, bayZ, 'minecraft:wall_torch', { facing: side.facing })
      put(side.inner, 1, bayZ + 1, 'minecraft:purpur_block')
      put(side.inner, 2, bayZ + 1, 'minecraft:end_rod', { facing: 'up' })
      put(side.inner, 3, bayZ + 1, 'minecraft:end_rod', { facing: 'up' })
      put(side.inner, 5, bayZ, 'minecraft:purpur_slab', { type: 'top', waterlogged: 'false' })
      put(side.inner, 5, bayZ + 1, 'minecraft:purpur_slab', { type: 'top', waterlogged: 'false' })
    }
  }
  for (const vineZ of [1, 4, 6, 9, 11, 14]) {
    for (const side of [{ inner: 3, attachment: 'west' }, { inner: 9, attachment: 'east' }]) {
      const minimum = vineZ % 3 === 1 ? 1 : 3
      for (let height = minimum; height <= 7; height++) {
        put(side.inner, height, vineZ, 'minecraft:vine', { north: 'false', east: 'false', south: 'false', west: 'false', up: 'false', [side.attachment]: 'true' })
      }
    }
  }
  for (const canopyZ of [2, 7, 12]) {
    for (const canopyX of [4, 8]) {
      put(canopyX, 7, canopyZ, 'minecraft:vine', { north: 'false', east: 'false', south: 'false', west: 'false', up: 'true' })
      put(canopyX, 7, canopyZ + 1, 'minecraft:hanging_roots')
    }
  }
  return { structure, metadata: { name: 'room', id: 'CORRIDOR_S14', sourceId: 'S14', theme: 'end-overgrown', lengthClass: 'short', size } }
}
