export default function build(api) {
  const size = [15, 10, 16]
  const structure = api.createStructure(size)
  const put = (position, block, properties) => api.setBlock(structure, position, block, properties)
  const fill = (from, to, block, properties) => api.fill(structure, from, to, block, properties)
  const stone = (position, floor = false) => {
    const value = (position[0] * 137 + position[1] * 53 + position[2] * 83) % 31
    if (value < (floor ? 4 : 6)) return 'minecraft:mossy_stone_bricks'
    if (value < 12) return 'minecraft:cracked_stone_bricks'
    return 'minecraft:stone_bricks'
  }
  const blueprint = { cells: [], lamps: [], cages: [], beams: [], clear_passage: [], safe_points: [[7, 1, 2], [7, 1, 13]] }
  fill([0, 0, 0], [14, 9, 15], 'minecraft:air')
  for (let posX = 0; posX < size[0]; posX += 1) {
    for (let posZ = 0; posZ < size[2]; posZ += 1) {
      put([posX, 0, posZ], stone([posX, 0, posZ], true))
      put([posX, 8, posZ], stone([posX, 8, posZ]))
      put([posX, 9, posZ], stone([posX, 9, posZ]))
    }
  }
  for (let posY = 1; posY < 8; posY += 1) {
    for (let posZ = 0; posZ < size[2]; posZ += 1) {
      for (const posX of [0, 14]) put([posX, posY, posZ], stone([posX, posY, posZ]))
    }
    for (let posX = 1; posX < 14; posX += 1) {
      for (const posZ of [0, 15]) put([posX, posY, posZ], stone([posX, posY, posZ]))
    }
  }
  for (const posZ of [0, 5, 10, 15]) {
    for (const posX of [4, 10]) {
      fill([posX, 1, posZ], [posX, 6, posZ], 'minecraft:stone_bricks')
      put([posX, 6, posZ], 'minecraft:chiseled_stone_bricks')
    }
    if (posZ === 5 || posZ === 10) {
      for (const bounds of [[1, 3], [11, 13]]) {
        for (let posX = bounds[0]; posX <= bounds[1]; posX += 1) {
          for (let posY = 1; posY <= 7; posY += 1) put([posX, posY, posZ], stone([posX, posY, posZ]))
        }
      }
    }
    fill([1, 7, posZ], [13, 7, posZ], 'minecraft:stripped_dark_oak_log', { axis: 'x' })
    blueprint.beams.push({ from: [1, 7, posZ], to: [13, 7, posZ], supports: [[4, 1, posZ], [10, 1, posZ]] })
  }
  for (const startZ of [1, 6, 11]) {
    for (const side of ['west', 'east']) {
      const frontX = side === 'west' ? 4 : 10
      const doorZ = startZ + 1
      fill([frontX, 1, startZ], [frontX, 3, startZ + 3], 'minecraft:iron_bars')
      fill([frontX, 4, startZ], [frontX, 6, startZ + 3], 'minecraft:stone_bricks')
      const door = { facing: side === 'west' ? 'east' : 'west', hinge: 'left', open: 'false', powered: 'false' }
      put([frontX, 1, doorZ], 'minecraft:iron_door', { ...door, half: 'lower' })
      put([frontX, 2, doorZ], 'minecraft:iron_door', { ...door, half: 'upper' })
      const bunkX = side === 'west' ? 1 : 12
      fill([bunkX, 1, startZ + 2], [bunkX + 1, 1, startZ + 3], 'minecraft:dark_oak_planks')
      fill([bunkX, 2, startZ + 2], [bunkX + 1, 2, startZ + 2], 'minecraft:light_gray_carpet')
      fill([bunkX, 2, startZ + 3], [bunkX + 1, 2, startZ + 3], 'minecraft:white_carpet')
      const potX = side === 'west' ? 1 : 13
      put([potX, 1, startZ], 'minecraft:cauldron')
      put([potX, 6, startZ], 'minecraft:cobweb')
      blueprint.cells.push({ side, bounds: [[side === 'west' ? 1 : 11, 1, startZ], [side === 'west' ? 3 : 13, 6, startZ + 3]], door: [frontX, 1, doorZ], bunk: [bunkX, 1, startZ + 2], access: 'closed_decorative_cell_not_a_branch' })
    }
  }
  for (const posZ of [0, 15]) {
    fill([6, 1, posZ], [8, 4, posZ], 'minecraft:air')
    fill([5, 1, posZ], [5, 4, posZ], 'minecraft:stone_bricks')
    fill([9, 1, posZ], [9, 4, posZ], 'minecraft:stone_bricks')
    fill([5, 5, posZ], [9, 5, posZ], 'minecraft:chiseled_stone_bricks')
    for (const posX of [3, 11]) fill([posX, 1, posZ], [posX, 3, posZ], 'minecraft:iron_bars')
  }
  for (const posZ of [5, 10]) {
    for (const posX of [5, 9]) {
      put([posX, 6, posZ], 'minecraft:chain', { axis: 'y', waterlogged: 'false' })
      put([posX, 5, posZ], 'minecraft:soul_lantern', { hanging: 'true', waterlogged: 'false' })
      blueprint.lamps.push({ lamp: [posX, 5, posZ], chain: [[posX, 6, posZ]], anchor: [posX, 7, posZ] })
    }
  }
  for (const [startX, startZ] of [[6, 7], [7, 12]]) {
    fill([startX, 4, startZ], [startX + 1, 4, startZ + 1], 'minecraft:polished_deepslate')
    fill([startX, 5, startZ], [startX + 1, 6, startZ + 1], 'minecraft:iron_bars')
    fill([startX, 7, startZ], [startX + 1, 7, startZ + 1], 'minecraft:polished_deepslate')
    const suspension = []
    for (let posX = startX; posX <= startX + 1; posX += 1) {
      for (let posZ = startZ; posZ <= startZ + 1; posZ += 1) {
        put([posX, 8, posZ], 'minecraft:chain', { axis: 'y', waterlogged: 'false' })
        suspension.push({ base: [posX, 4, posZ], sides: [[posX, 5, posZ], [posX, 6, posZ]], cap: [posX, 7, posZ], chain: [posX, 8, posZ], anchor: [posX, 9, posZ] })
      }
    }
    blueprint.cages.push({ bounds: [[startX, 4, startZ], [startX + 1, 7, startZ + 1]], suspension, note: 'Four roof-anchored chains; no cage entities or items; bottom above three-block clearance.' })
  }
  for (let posZ = 0; posZ < size[2]; posZ += 1) {
    for (let posX = 6; posX <= 8; posX += 1) {
      for (let posY = 1; posY <= 3; posY += 1) blueprint.clear_passage.push([posX, posY, posZ])
    }
  }
  return { structure, metadata: { name: 'room', id: 'corridor-S15', display_name: 'S15 陈旧监牢短廊', blueprint, design: 'prison-cells / aged / soul-lanterns / hanging-cages' } }
}
