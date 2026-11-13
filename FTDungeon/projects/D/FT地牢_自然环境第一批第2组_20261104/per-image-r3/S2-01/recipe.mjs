export default function build(api) {
  const size = [23, 13, 23]
  const structure = api.createStructure(size)
  const put = (blockX, blockY, blockZ, block, properties) => api.setBlock(structure, [blockX, blockY, blockZ], `minecraft:${block}`, properties)
  const fill = (minimum, maximum, block) => api.fill(structure, minimum, maximum, `minecraft:${block}`)
  fill([0, 0, 0], [22, 12, 22], 'air')
  for (let blockX = 0; blockX < 23; blockX++) for (let blockZ = 0; blockZ < 23; blockZ++) {
    const ring = Math.max(Math.abs(blockX - 11), Math.abs(blockZ - 11))
    const cross = (Math.abs(blockX - 11) <= 1 || Math.abs(blockZ - 11) <= 1) && ring > 6
    put(blockX, 0, blockZ, cross || ring === 9 && (blockX + blockZ) % 3 !== 0 ? 'orange_terracotta' : (blockX * 7 + blockZ * 11) % 13 === 0 ? 'cut_sandstone' : 'smooth_sandstone')
    put(blockX, 12, blockZ, 'cut_sandstone')
    if (blockX === 0 || blockX === 22 || blockZ === 0 || blockZ === 22) {
      for (let blockY = 1; blockY < 12; blockY++) put(blockX, blockY, blockZ, blockY === 4 || blockY === 9 ? 'orange_terracotta' : (blockX + blockY * 3 + blockZ) % 7 === 0 ? 'cut_sandstone' : 'sandstone')
    }
  }
  for (const wall of ['north', 'south', 'west', 'east']) {
    const coordinate = (along, inward) => wall === 'north' ? [along, inward] : wall === 'south' ? [along, 22 - inward] : wall === 'west' ? [inward, along] : [22 - inward, along]
    for (const along of [3, 7, 15, 19]) {
      const [blockX, blockZ] = coordinate(along, 1)
      for (let blockY = 1; blockY <= 10; blockY++) put(blockX, blockY, blockZ, blockY === 3 || blockY === 5 ? 'chiseled_sandstone' : blockY === 4 ? 'orange_terracotta' : 'cut_sandstone')
      const [lightX, lightZ] = coordinate(along, 2)
      put(lightX, 3, lightZ, 'cut_sandstone')
      put(lightX, 4, lightZ, 'lantern', { hanging: 'false', waterlogged: 'false' })
    }
    for (let along = 1; along <= 21; along++) {
      const [blockX, blockZ] = coordinate(along, 1)
      put(blockX, 10, blockZ, 'cut_sandstone')
      put(blockX, 11, blockZ, 'smooth_sandstone')
    }
    for (let offset = -3; offset <= 3; offset++) {
      for (let blockY = 1; blockY <= 7; blockY++) {
        const halfOpening = blockY <= 3 ? 2 : blockY <= 5 ? 1 : blockY === 6 ? 0 : -1
        for (const inward of [0, 1]) {
          const [blockX, blockZ] = coordinate(11 + offset, inward)
          put(blockX, blockY, blockZ, Math.abs(offset) <= halfOpening ? 'air' : Math.abs(offset) === 3 && blockY < 5 ? 'cut_sandstone' : 'chiseled_sandstone')
        }
      }
    }
  }
  fill([7, 1, 7], [15, 1, 15], 'cut_sandstone')
  fill([8, 2, 8], [14, 2, 14], 'smooth_sandstone')
  for (let blockX = 8; blockX <= 14; blockX++) for (let blockZ = 8; blockZ <= 14; blockZ++) {
    const ring = Math.max(Math.abs(blockX - 11), Math.abs(blockZ - 11))
    put(blockX, 2, blockZ, ring === 1 || ring === 3 && (blockX + blockZ) % 2 === 0 ? 'orange_terracotta' : 'smooth_sandstone')
  }
  put(11, 2, 11, 'chiseled_sandstone')
  for (const direction of ['north', 'south']) for (let blockX = 9; blockX <= 13; blockX++) {
    const approachZ = direction === 'north' ? 6 : 16
    const upperZ = direction === 'north' ? 7 : 15
    const facing = direction === 'north' ? 'south' : 'north'
    put(blockX, 1, approachZ, 'smooth_sandstone_stairs', { facing, half: 'bottom', shape: 'straight', waterlogged: 'false' })
    put(blockX, 2, upperZ, 'smooth_sandstone_stairs', { facing, half: 'bottom', shape: 'straight', waterlogged: 'false' })
  }
  for (const blockX of [7, 15]) for (const blockZ of [7, 15]) {
    put(blockX, 2, blockZ, 'chiseled_sandstone')
    put(blockX, 3, blockZ, 'cut_sandstone')
    put(blockX, 4, blockZ, 'lantern', { hanging: 'false', waterlogged: 'false' })
  }
  for (const [blockX, blockZ] of [[2, 5], [20, 17], [4, 20], [18, 2]]) {
    put(blockX, 1, blockZ, 'terracotta')
    put(blockX, 2, blockZ, 'flower_pot')
  }
  for (const [blockX, blockZ] of [[2, 17], [20, 5], [5, 2], [17, 20]]) {
    put(blockX, 1, blockZ, 'sand')
    put(blockX, 2, blockZ, 'dead_bush')
  }
  return { structure, metadata: { name: 'room' } }
}
