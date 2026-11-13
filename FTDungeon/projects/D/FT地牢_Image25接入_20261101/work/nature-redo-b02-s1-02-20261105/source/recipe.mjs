export default function build(api) {
  const structure = api.createStructure([23, 13, 21])
  const set = (across, vertical, forward, block, properties, nbt) => api.setBlock(structure, [across, vertical, forward], block, properties, nbt)
  const box = (start, end, block, properties) => api.fill(structure, start, end, block, properties)
  const leaf = {persistent: 'true', distance: '7', waterlogged: 'false'}
  const masonry = (across, vertical, forward) => ['stone_bricks', 'mossy_stone_bricks', 'mossy_stone_bricks', 'cracked_stone_bricks', 'stone_bricks', 'chiseled_stone_bricks'][(across * 13 + vertical * 7 + forward * 11) % 6]
  box([0, 0, 0], [22, 12, 20], 'air')
  box([0, 0, 0], [22, 0, 20], 'stone_bricks')
  for (let across = 0; across < 23; across++) for (let forward = 0; forward < 21; forward++) {
    set(across, 1, forward, (across * 5 + forward * 3) % 11 < 3 ? 'moss_block' : masonry(across, 1, forward))
    set(across, 12, forward, 'mossy_stone_bricks')
    if (across === 0 || across === 22 || forward === 0 || forward === 20) for (let vertical = 2; vertical < 12; vertical++) set(across, vertical, forward, masonry(across, vertical, forward))
  }
  for (const across of [2, 8, 14, 20]) {
    box([across, 2, 1], [across + 1, 9, 2], 'stone_bricks')
    box([across, 2, 1], [across + 1, 2, 2], 'chiseled_stone_bricks')
    box([across, 10, 1], [across + 1, 11, 2], 'mossy_stone_bricks')
  }
  for (const forward of [6, 13]) for (const across of [1, 20]) {
    box([across, 2, forward], [across + 1, 9, forward + 1], 'mossy_stone_bricks')
    box([across, 2, forward], [across + 1, 2, forward + 1], 'chiseled_stone_bricks')
  }
  for (const forward of [0, 20]) {
    box([10, 2, forward], [12, 5, forward], 'air')
    box([9, 2, forward], [9, 6, forward], 'stone_bricks')
    box([13, 2, forward], [13, 6, forward], 'stone_bricks')
    box([9, 6, forward], [13, 6, forward], 'mossy_stone_bricks')
    box([10, 1, forward], [12, 1, forward], 'stone_bricks')
  }
  for (let across = 9; across <= 13; across++) for (let forward = 8; forward <= 12; forward++) {
    const rim = across === 9 || across === 13 || forward === 8 || forward === 12
    set(across, 2, forward, rim ? 'mossy_stone_bricks' : 'water', rim ? undefined : {level: '0'})
  }
  const pool = (minX, minZ, maxX, maxZ) => {
    for (let across = minX; across <= maxX; across++) for (let forward = minZ; forward <= maxZ; forward++) {
      const rim = across === minX || across === maxX || forward === minZ || forward === maxZ
      set(across, 1, forward, rim ? 'mossy_stone_bricks' : 'water', rim ? undefined : {level: '0'})
    }
  }
  pool(2, 15, 7, 18)
  pool(17, 14, 21, 18)
  box([19, 2, 15], [21, 5, 17], 'mossy_cobblestone')
  box([20, 2, 16], [20, 4, 16], 'water', {level: '0'})
  box([19, 2, 16], [19, 4, 16], 'glass')
  box([2, 2, 4], [5, 2, 5], 'dark_oak_planks')
  set(3, 3, 4, 'lantern', {hanging: 'false', waterlogged: 'false'})
  const container = (across, forward, type) => {
    set(across, 1, forward, 'stone_bricks')
    const properties = type === 'chest' ? {facing: 'south', type: 'single', waterlogged: 'false'} : {facing: 'up', open: 'false'}
    set(across, 2, forward, type, properties, api.createBlockNbt({id: 'minecraft:' + type}))
  }
  container(6, 3, 'barrel')
  container(15, 3, 'barrel')
  box([17, 2, 2], [19, 2, 2], 'dark_oak_planks')
  box([17, 3, 2], [19, 4, 2], 'bookshelf')
  container(18, 3, 'chest')
  box([19, 2, 3], [19, 3, 5], 'dark_oak_log', {axis: 'y'})
  box([20, 2, 3], [21, 8, 4], 'dark_oak_log', {axis: 'y'})
  box([19, 6, 3], [20, 9, 4], 'dark_oak_log', {axis: 'y'})
  box([16, 8, 2], [21, 9, 3], 'dark_oak_log', {axis: 'x'})
  box([2, 9, 2], [20, 10, 3], 'dark_oak_log', {axis: 'x'})
  box([1, 2, 9], [2, 8, 10], 'dark_oak_log', {axis: 'y'})
  box([1, 8, 3], [2, 9, 10], 'dark_oak_log', {axis: 'z'})
  box([20, 8, 4], [21, 9, 12], 'dark_oak_log', {axis: 'z'})
  box([19, 6, 9], [20, 8, 10], 'dark_oak_log', {axis: 'y'})
  box([20, 2, 10], [21, 7, 11], 'dark_oak_log', {axis: 'y'})
  const clumps = [[3,10,4,3,1.5,3], [9,11,2,3.5,1,2], [16,10,3,4,1.8,2], [20,9,7,2,2.5,4], [2,9,10,2,2,3]]
  for (const [centerX, centerY, centerZ, radiusX, radiusY, radiusZ] of clumps) {
    for (let across = 1; across < 22; across++) for (let vertical = 7; vertical < 12; vertical++) for (let forward = 1; forward < 20; forward++) {
      const radius = ((across - centerX) / radiusX) ** 2 + ((vertical - centerY) / radiusY) ** 2 + ((forward - centerZ) / radiusZ) ** 2
      if (radius < 1 + ((across + forward * 3) % 5) * .07 && String(structure.getBlock([across, vertical, forward]).state.getName()) === 'minecraft:air') set(across, vertical, forward, 'oak_leaves', leaf)
    }
  }
  for (const [across, forward, top, length] of [[4,3,8,3], [8,3,8,2], [16,3,8,3], [20,7,7,2], [2,9,7,3]]) {
    set(across, top + 1, forward, 'dark_oak_log', {axis: 'x'})
    for (let drop = 0; drop < length; drop++) set(across, top - drop, forward, drop === length - 1 ? 'cave_vines' : 'cave_vines_plant', drop === length - 1 ? {berries: 'true', age: '25'} : {berries: 'true'})
  }
  for (const [across, forward, top] of [[5,3,8], [15,3,8], [20,6,7]]) {
    set(across, top + 1, forward, 'dark_oak_log', {axis: 'x'})
    set(across, top, forward, 'lantern', {hanging: 'true', waterlogged: 'false'})
  }
  for (const [across, forward] of [[2,18], [7,19], [16,19], [21,19], [1,3], [21,12]]) {
    if (String(structure.getBlock([across, 2, forward]).state.getName()) !== 'minecraft:air') continue
    set(across, 2, forward, 'oak_leaves', leaf)
  }
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
