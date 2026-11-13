export default function build(api) {
  const structure = api.createStructure([19, 12, 17])
  const set = (across, vertical, forward, block, properties, nbt) => api.setBlock(structure, [across, vertical, forward], block, properties, nbt)
  const box = (start, end, block, properties) => api.fill(structure, start, end, block, properties)
  const leaf = {persistent: 'true', distance: '7', waterlogged: 'false'}
  const stone = (across, vertical, forward) => ['mossy_stone_bricks', 'stone_bricks', 'cracked_stone_bricks', 'mossy_stone_bricks', 'stone_bricks'][(across * 7 + vertical * 13 + forward * 3) % 5]
  box([0, 0, 0], [18, 11, 16], 'air')
  box([0, 0, 0], [18, 0, 16], 'stone_bricks')
  for (let across = 0; across < 19; across++) for (let forward = 0; forward < 17; forward++) {
    const ground = (across * 7 + forward * 11) % 17
    set(across, 1, forward, ground < 3 ? 'moss_block' : ground === 4 ? 'coarse_dirt' : stone(across, 1, forward))
    set(across, 11, forward, 'mossy_stone_bricks')
    if (across === 0 || across === 18 || forward === 0 || forward === 16) for (let vertical = 2; vertical < 11; vertical++) set(across, vertical, forward, stone(across, vertical, forward))
  }
  for (const across of [1, 8, 15]) {
    box([across, 2, 1], [across + 1, 8, 2], 'mossy_stone_bricks')
    box([across, 2, 1], [across + 1, 2, 2], 'chiseled_stone_bricks')
  }
  for (const [across, startZ] of [[0, 9], [18, 5]]) {
    box([across, 2, startZ], [across, 5, startZ + 2], 'air')
    box([across, 2, startZ - 1], [across, 6, startZ - 1], 'stone_bricks')
    box([across, 2, startZ + 3], [across, 6, startZ + 3], 'stone_bricks')
    box([across, 6, startZ - 1], [across, 6, startZ + 3], 'mossy_stone_bricks')
    box([across, 1, startZ], [across, 1, startZ + 2], 'stone_bricks')
  }
  box([9, 2, 8], [11, 2, 10], 'mossy_stone_bricks')
  set(10, 3, 9, 'chest', {facing: 'south', type: 'single', waterlogged: 'false'}, api.createBlockNbt({id: 'minecraft:chest'}))
  const pool = (minX, minZ, maxX, maxZ) => {
    for (let across = minX; across <= maxX; across++) for (let forward = minZ; forward <= maxZ; forward++) {
      const rim = across === minX || across === maxX || forward === minZ || forward === maxZ
      set(across, 1, forward, rim ? 'mossy_stone_bricks' : 'water', rim ? undefined : {level: '0'})
    }
  }
  pool(2, 5, 6, 8)
  pool(14, 12, 17, 15)
  const shelf = (minX, maxX, height) => {
    box([minX, 2, 1], [minX, height - 1, 2], 'dark_oak_log', {axis: 'y'})
    box([maxX, 2, 1], [maxX, height - 1, 2], 'dark_oak_log', {axis: 'y'})
    box([minX, height, 1], [maxX, height, 2], 'dark_oak_planks')
    set(minX, height + 1, 2, 'potted_red_mushroom')
    set(maxX, height + 1, 2, 'flower_pot')
  }
  shelf(2, 4, 3)
  shelf(10, 13, 4)
  box([5, 2, 2], [6, 7, 3], 'dark_oak_log', {axis: 'y'})
  box([5, 6, 2], [7, 9, 3], 'dark_oak_log', {axis: 'y'})
  box([7, 8, 2], [10, 9, 3], 'dark_oak_log', {axis: 'x'})
  box([10, 8, 2], [13, 8, 3], 'dark_oak_log', {axis: 'x'})
  box([13, 7, 2], [16, 8, 3], 'dark_oak_log', {axis: 'x'})
  box([16, 6, 2], [17, 7, 4], 'dark_oak_log', {axis: 'z'})
  box([17, 2, 3], [17, 6, 4], 'dark_oak_log', {axis: 'y'})
  box([3, 7, 2], [5, 8, 3], 'dark_oak_log', {axis: 'x'})
  box([1, 6, 2], [3, 7, 3], 'dark_oak_log', {axis: 'x'})
  box([4, 2, 3], [7, 2, 4], 'dark_oak_log', {axis: 'z'})
  set(7, 2, 5, 'dark_oak_log', {axis: 'z'})
  box([5, 2, 4], [6, 3, 4], 'dark_oak_log', {axis: 'y'})
  const clumps = [[3,8,2,3,2,2], [6,10,3,3,1,2], [10,9,2,3,1.5,2], [14,9,2,3,1.3,2], [17,7,4,1.5,2,2]]
  for (const [centerX, centerY, centerZ, radiusX, radiusY, radiusZ] of clumps) {
    for (let across = 1; across < 18; across++) for (let vertical = 6; vertical < 11; vertical++) for (let forward = 1; forward < 7; forward++) {
      const radius = ((across - centerX) / radiusX) ** 2 + ((vertical - centerY) / radiusY) ** 2 + ((forward - centerZ) / radiusZ) ** 2
      if (radius < 1 + ((across * 3 + forward) % 5) * .06 && String(structure.getBlock([across, vertical, forward]).state.getName()) === 'minecraft:air') set(across, vertical, forward, 'oak_leaves', leaf)
    }
  }
  for (const [across, forward, top, length] of [[3,3,6,2], [8,3,7,2], [12,3,7,2], [16,3,6,2]]) {
    set(across, top + 1, forward, 'dark_oak_log', {axis: 'x'})
    for (let drop = 0; drop < length; drop++) set(across, top - drop, forward, drop === length - 1 ? 'cave_vines' : 'cave_vines_plant', drop === length - 1 ? {berries: 'true', age: '25'} : {berries: 'true'})
  }
  for (const [across, forward, top] of [[4,3,6], [11,3,7], [17,4,5]]) {
    set(across, top + 1, forward, 'dark_oak_log', {axis: 'x'})
    set(across, top, forward, 'lantern', {hanging: 'true', waterlogged: 'false'})
  }
  for (const [across, forward] of [[2,4], [7,4], [1,14], [4,14], [16,10], [17,14]]) set(across, 2, forward, 'oak_leaves', leaf)
  const distances = new Map()
  let frontier = structure.getBlocks().filter(block => String(block.state.getName()).endsWith('_log')).map(block => block.pos)
  for (let distance = 1; distance <= 6; distance++) {
    const next = []
    for (const [across, vertical, forward] of frontier) for (const [deltaX, deltaY, deltaZ] of [[1,0,0],[-1,0,0],[0,1,0],[0,-1,0],[0,0,1],[0,0,-1]]) {
      const point = [across + deltaX, vertical + deltaY, forward + deltaZ]
      const block = structure.getBlock(point)
      const key = point.join(',')
      if (!distances.has(key) && block && String(block.state.getName()).endsWith('_leaves')) { distances.set(key, distance); next.push(point) }
    }
    frontier = next
  }
  for (const block of structure.getBlocks()) if (String(block.state.getName()).endsWith('_leaves')) api.setBlock(structure, block.pos, 'oak_leaves', {...leaf, distance: String(distances.get(block.pos.join(',')) ?? 7)})
  return {structure, metadata: {name: 'room'}}
}
