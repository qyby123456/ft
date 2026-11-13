export const specification = {
  id: 'corridor-S17',
  sourceId: 'S17',
  name: '灾变暗铁·绯纹悬笼短廊',
  size: [15, 13, 16],
  entrance: [7, 1, 0],
  interior: [7, 1, 3],
  exit: [7, 1, 15],
  reference: 'D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/S17-cataclysm-dark-iron-intact.png',
  referenceSha256: '18a1c608777d9f99e6e4faaaa95f556bbf3017db07944b47868dd329ebcd8ddb',
  cageCenters: [[4, 3], [10, 3], [4, 12], [10, 12]],
  ribs: [0, 5, 10, 15],
  torches: [2, 7, 13],
}

export default function build(api) {
  const structure = api.createStructure(specification.size)
  const place = (position, block, properties) => api.setBlock(structure, position, `minecraft:${block}`, properties)
  const fill = (from, to, block, properties) => api.fill(structure, from, to, `minecraft:${block}`, properties)
  fill([0, 0, 0], [14, 12, 15], 'air')
  fill([0, 0, 0], [14, 0, 15], 'polished_deepslate')
  fill([0, 12, 0], [14, 12, 15], 'deepslate_tiles')
  for (let positionZ = 0; positionZ < 16; positionZ++) {
    for (let positionY = 1; positionY < 12; positionY++) {
      for (const positionX of [0, 1, 13, 14]) {
        const variation = (positionY * 7 + positionZ * 3 + positionX) % 9
        place([positionX, positionY, positionZ], variation < 2 ? 'deepslate_tiles' : variation === 2 ? 'polished_blackstone_bricks' : 'deepslate_bricks')
      }
    }
    for (const positionX of [1, 13]) {
      fill([positionX, 1, positionZ], [positionX, 2, positionZ], 'obsidian')
      place([positionX, 3, positionZ], 'gold_block')
      place([positionX, 9, positionZ], 'gold_block')
      fill([positionX, 10, positionZ], [positionX, 11, positionZ], 'polished_blackstone_bricks')
    }
    for (let positionX = 5; positionX <= 9; positionX++) place([positionX, 0, positionZ], 'obsidian')
    for (const positionX of [4, 10]) place([positionX, 0, positionZ], 'gold_block')
    for (const positionX of [2, 3, 11, 12]) place([positionX, 0, positionZ], (positionX + positionZ) % 4 === 0 ? 'chiseled_polished_blackstone' : 'polished_deepslate')
  }
  for (const centerZ of [3, 8, 13]) {
    for (const [offsetX, offsetZ] of [[0, -2], [-1, -1], [1, -1], [-2, 0], [2, 0], [-1, 1], [1, 1], [0, 2]]) {
      const positionZ = centerZ + offsetZ
      if (positionZ >= 0 && positionZ < 16) place([7 + offsetX, 0, positionZ], Math.abs(offsetX) === 2 ? 'red_nether_bricks' : 'redstone_block')
    }
    place([7, 0, centerZ], 'chiseled_polished_blackstone')
  }
  for (const wallX of [1, 13]) {
    for (const startZ of [1, 6, 11]) {
      fill([wallX, 4, startZ], [wallX, 8, startZ + 3], 'black_concrete')
      for (const [offsetY, offsetZ] of [[0, 1], [0, 2], [1, 0], [1, 3], [2, 1], [2, 2], [3, 0], [3, 3], [4, 1], [4, 2]]) place([wallX, 4 + offsetY, startZ + offsetZ], offsetY % 2 === 0 ? 'redstone_block' : 'red_nether_bricks')
    }
  }
  for (const ribZ of specification.ribs) {
    for (const pillarX of [2, 12]) {
      fill([pillarX, 1, ribZ], [pillarX, 9, ribZ], 'polished_blackstone_bricks')
      place([pillarX, 1, ribZ], 'obsidian')
      place([pillarX, 3, ribZ], 'gold_block')
      place([pillarX, 7, ribZ], 'chiseled_polished_blackstone')
    }
    for (let positionX = 2; positionX <= 12; positionX++) {
      const lowerY = Math.min(11, 12 - Math.abs(positionX - 7))
      fill([positionX, lowerY, ribZ], [positionX, 11, ribZ], 'polished_blackstone_bricks')
      place([positionX, lowerY, ribZ], 'gold_block')
    }
  }
  for (const portalZ of [0, 15]) {
    fill([2, 1, portalZ], [3, 7, portalZ], 'polished_blackstone_bricks')
    fill([11, 1, portalZ], [12, 7, portalZ], 'polished_blackstone_bricks')
    for (const positionX of [3, 11]) {
      place([positionX, 2, portalZ], 'chiseled_polished_blackstone')
      place([positionX, 4, portalZ], 'gold_block')
      place([positionX, 6, portalZ], 'chiseled_polished_blackstone')
    }
    place([7, 10, portalZ], 'redstone_block')
    place([6, 10, portalZ], 'red_nether_bricks')
    place([8, 10, portalZ], 'red_nether_bricks')
  }
  for (const torchZ of specification.torches) {
    for (const [torchX, facing] of [[2, 'east'], [12, 'west']]) {
      place([torchX, 4, torchZ], 'polished_blackstone_brick_wall', { up: 'true', north: 'none', east: torchX === 12 ? 'low' : 'none', south: 'none', west: torchX === 2 ? 'low' : 'none', waterlogged: 'false' })
      place([torchX, 5, torchZ], 'wall_torch', { facing })
    }
  }
  for (const [centerX, centerZ] of specification.cageCenters) {
    for (let offsetX = -1; offsetX <= 1; offsetX++) {
      for (let offsetZ = -1; offsetZ <= 1; offsetZ++) {
        const positionX = centerX + offsetX
        const positionZ = centerZ + offsetZ
        place([positionX, 6, positionZ], 'iron_trapdoor', { facing: 'north', half: 'top', open: 'false', powered: 'false', waterlogged: 'false' })
        place([positionX, 9, positionZ], 'polished_blackstone')
        if (Math.abs(offsetX) === 1 || Math.abs(offsetZ) === 1) fill([positionX, 7, positionZ], [positionX, 8, positionZ], 'iron_bars', { north: 'false', east: 'false', south: 'false', west: 'false', waterlogged: 'false' })
      }
    }
    place([centerX, 6, centerZ], 'red_nether_bricks')
    place([centerX, 7, centerZ], 'lantern', { hanging: 'true', waterlogged: 'false' })
    place([centerX, 8, centerZ], 'chain', { axis: 'y', waterlogged: 'false' })
    fill([centerX, 10, centerZ], [centerX, 11, centerZ], 'chain', { axis: 'y', waterlogged: 'false' })
  }
  return { structure, metadata: { name: 'room', specification } }
}
