export default function build(api) {
  const size = [11, 9, 16]
  const structure = api.createStructure(size)
  const masonry = (posX, posY, posZ) => {
    const variation = (posX * 43 + posY * 29 + posZ * 71 + posX * posZ * 13) % 101
    if (variation < 17) return 'minecraft:cracked_stone_bricks'
    if (variation < 23) return 'minecraft:andesite'
    if (variation < 26) return 'minecraft:mossy_stone_bricks'
    return 'minecraft:stone_bricks'
  }
  api.fill(structure, [0, 0, 0], [10, 8, 15], 'minecraft:air')
  for (let posX = 0; posX < size[0]; posX += 1) {
    for (let posZ = 0; posZ < size[2]; posZ += 1) {
      api.setBlock(structure, [posX, 0, posZ], masonry(posX, 0, posZ))
      api.setBlock(structure, [posX, 8, posZ], masonry(posX, 8, posZ))
    }
  }
  for (let posZ = 0; posZ < size[2]; posZ += 1) {
    for (let posY = 1; posY < 8; posY += 1) {
      for (const posX of [0, 10]) api.setBlock(structure, [posX, posY, posZ], masonry(posX, posY, posZ))
    }
    for (let posX = 1; posX <= 9; posX += 1) {
      const spring = posX === 1 || posX === 9 ? 6 : 7
      for (let posY = spring; posY <= 7; posY += 1) {
        api.setBlock(structure, [posX, posY, posZ], masonry(posX, posY, posZ))
      }
    }
    for (const posX of [1, 9]) api.setBlock(structure, [posX, 1, posZ], masonry(posX, 1, posZ))
  }
  for (const archZ of [0, 5, 10, 15]) {
    for (const pillarX of [1, 9]) {
      for (let posY = 1; posY <= 5; posY += 1) {
        api.setBlock(structure, [pillarX, posY, archZ], masonry(pillarX, posY, archZ))
      }
      api.setBlock(structure, [pillarX, 1, archZ], 'minecraft:chiseled_stone_bricks')
    }
    for (const [archX, archY, facing] of [[2, 4, 'west'], [3, 5, 'west'], [7, 5, 'east'], [8, 4, 'east']]) {
      for (let posY = archY + 1; posY <= 7; posY += 1) {
        api.setBlock(structure, [archX, posY, archZ], masonry(archX, posY, archZ))
      }
      if (!(archZ === 5 && archX === 3) && !(archZ === 10 && archX === 8)) {
        api.setBlock(structure, [archX, archY, archZ], 'minecraft:stone_brick_stairs', { facing, half: 'top', shape: 'straight', waterlogged: 'false' })
      }
    }
    for (let posX = 4; posX <= 6; posX += 1) {
      api.setBlock(structure, [posX, 6, archZ], posX === 5 ? 'minecraft:chiseled_stone_bricks' : 'minecraft:stone_bricks')
    }
  }
  for (const torchZ of [2, 7, 12]) {
    for (const [torchX, facing] of [[1, 'east'], [9, 'west']]) {
      api.setBlock(structure, [torchX, 2, torchZ], 'minecraft:iron_bars')
      api.setBlock(structure, [torchX, 3, torchZ], 'minecraft:wall_torch', { facing })
    }
  }
  for (const [webX, webZ] of [[1, 1], [9, 1], [1, 6], [9, 11], [1, 14], [9, 14]]) {
    api.setBlock(structure, [webX, 5, webZ], 'minecraft:cobweb')
  }
  for (const [rubbleX, rubbleZ] of [[2, 3], [8, 11]]) {
    api.setBlock(structure, [rubbleX, 1, rubbleZ], 'minecraft:stone_brick_slab', { type: 'bottom', waterlogged: 'false' })
  }
  return { structure, metadata: { name: 'room', id: 'corridor-S02', theme: 'aged stone-brick arches' } }
}
