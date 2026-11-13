export default function build(api) {
  const structure = api.createStructure([11, 10, 16])
  const put = (position, block, properties) => api.setBlock(structure, position, 'minecraft:' + block, properties)
  const fill = (start, end, block, properties) => api.fill(structure, start, end, 'minecraft:' + block, properties)
  fill([0, 0, 0], [10, 9, 15], 'air')
  fill([0, 0, 0], [10, 0, 15], 'deepslate_tiles')
  fill([0, 1, 0], [1, 8, 15], 'polished_blackstone_bricks')
  fill([9, 1, 0], [10, 8, 15], 'polished_blackstone_bricks')
  fill([0, 9, 0], [10, 9, 15], 'deepslate_tiles')
  for (let posZ = 0; posZ < 16; posZ += 1) {
    for (const posX of [1, 9]) {
      for (let posY = 1; posY <= 8; posY += 1) {
        const noise = (posZ * 31 + posY * 17 + posX * 7) % 19
        const block = posY <= 3 && noise < 3 ? 'mossy_stone_bricks'
          : noise === 6 ? 'cracked_polished_blackstone_bricks'
          : noise === 10 || noise === 11 ? 'deepslate_bricks'
          : posY === 1 || posY === 7 ? 'polished_blackstone' : 'polished_blackstone_bricks'
        put([posX, posY, posZ], block)
      }
    }
    for (let posX = 4; posX <= 6; posX += 1) {
      put([posX, 1, posZ], posX === 5 ? 'deepslate_tiles' : 'polished_blackstone')
    }
  }
  for (const posZ of [0, 15]) {
    fill([0, 1, posZ], [10, 1, posZ], 'polished_blackstone')
    fill([0, 2, posZ], [3, 8, posZ], 'deepslate_bricks')
    fill([7, 2, posZ], [10, 8, posZ], 'deepslate_bricks')
    fill([4, 6, posZ], [6, 8, posZ], 'polished_blackstone_bricks')
    for (const posX of [3, 7]) {
      fill([posX, 2, posZ], [posX, 4, posZ], 'polished_blackstone')
      put([posX, 3, posZ], 'chiseled_polished_blackstone')
      put([posX, 5, posZ], 'polished_blackstone_brick_stairs', {
        facing: posX === 3 ? 'east' : 'west', half: 'top', shape: 'straight', waterlogged: 'false',
      })
    }
  }
  for (let posZ = 1; posZ <= 14; posZ += 1) {
    for (const posX of [2, 3, 7, 8]) put([posX, 1, posZ], 'water', { level: '0' })
  }
  for (const posZ of [0, 5, 10, 15]) {
    fill([1, 8, posZ], [9, 8, posZ], 'polished_blackstone_bricks')
    for (const posX of [2, 8]) {
      fill([posX, 1, posZ], [posX, 7, posZ], 'deepslate_bricks')
      put([posX, 2, posZ], 'polished_blackstone')
      put([posX, 6, posZ], 'chiseled_polished_blackstone')
      put([posX === 2 ? 3 : 7, 7, posZ], 'polished_blackstone_brick_stairs', {
        facing: posX === 2 ? 'east' : 'west', half: 'top', shape: 'straight', waterlogged: 'false',
      })
    }
  }
  for (const posZ of [2, 7, 13]) {
    put([2, 4, posZ], 'wall_torch', { facing: 'east' })
    put([8, 4, posZ], 'wall_torch', { facing: 'west' })
    put([1, 4, posZ], 'chiseled_polished_blackstone')
    put([9, 4, posZ], 'chiseled_polished_blackstone')
  }
  for (const [cageX, cageZ] of [[2, 3], [7, 11]]) {
    for (let offsetX = 0; offsetX < 2; offsetX += 1) {
      for (let offsetZ = 0; offsetZ < 2; offsetZ += 1) {
        put([cageX + offsetX, 4, cageZ + offsetZ], 'iron_trapdoor', {
          facing: offsetX === 0 ? 'east' : 'west', half: 'top', open: 'false', powered: 'false', waterlogged: 'false',
        })
        for (const posY of [5, 6]) put([cageX + offsetX, posY, cageZ + offsetZ], 'iron_bars', {
          north: String(offsetZ === 1), south: String(offsetZ === 0),
          west: String(offsetX === 1), east: String(offsetX === 0), waterlogged: 'false',
        })
        if (offsetX === offsetZ) {
          for (const posY of [7, 8]) put([cageX + offsetX, posY, cageZ + offsetZ], 'chain', { axis: 'y', waterlogged: 'false' })
        } else {
          put([cageX + offsetX, 7, cageZ + offsetZ], 'iron_trapdoor', {
            facing: offsetX === 0 ? 'east' : 'west', half: 'bottom', open: 'false', powered: 'false', waterlogged: 'false',
          })
        }
      }
    }
  }
  return { structure, metadata: { name: 'room', design: 'S05', length_blocks: 16, route_clearance: [3, 3] } }
}
