export default function build(api) {
  const structure = api.createStructure([35, 22, 35])
  const block = (east, up, south, name) => api.setBlock(structure, [east, up, south], `minecraft:${name}`)
  const box = (minimum, maximum, name) => api.fill(structure, minimum, maximum, `minecraft:${name}`)
  const stone = (east, up, south) => (east * 37 + up * 19 + south * 23) % 29 < 6 ? 'prismarine' : 'prismarine_bricks'
  box([0, 0, 0], [34, 21, 34], 'air')
  box([0, 0, 0], [34, 1, 34], 'dark_prismarine')
  for (let east = 0; east < 35; east++) for (let south = 0; south < 35; south++) block(east, 2, south, (east * 31 + south * 17 + east * south) % 37 < 6 ? 'sandstone' : stone(east, 2, south))
  box([2, 3, 2], [32, 3, 32], 'water[level=0]')
  for (let up = 3; up <= 17; up++) for (let along = 0; along < 35; along++) {
    const backHeight = 13 + (along >= 10 && along <= 24 ? 3 : 0) + (along >= 14 && along <= 20 ? 1 : 0)
    if (up <= backHeight) for (const south of [0, 1]) block(along, up, south, stone(along, up, south))
    const sideHeight = along < 10 ? 13 : along < 22 ? 10 : 7
    if (up <= sideHeight) for (const east of [0, 1, 33, 34]) block(east, up, along, stone(east, up, along))
    if (up <= 4) for (const south of [33, 34]) block(along, up, south, stone(along, up, south))
  }
  for (const [east, south, top] of [[2, 2, 17], [10, 1, 18], [22, 1, 18], [30, 2, 16], [1, 13, 13], [31, 13, 13], [0, 28, 9], [32, 28, 9]]) {
    box([east, 3, south], [east + 2, top, south + 2], 'prismarine_bricks')
    box([east + 1, 4, south + 2], [east + 1, top - 2, south + 2], 'dark_prismarine')
    box([east, top - 1, south], [east + 2, top - 1, south + 2], 'prismarine')
    block(east + 1, top, south + 1, 'sea_lantern')
  }
  for (const [side, center, height] of [['north', 17, 8], ['west', 10, 7], ['east', 10, 7]]) {
    const position = (along, up, depth) => side === 'north' ? [along, up, depth] : side === 'west' ? [depth, up, along] : [34 - depth, up, along]
    for (let depth = 0; depth <= 2; depth++) for (let offset = -4; offset <= 4; offset++) {
      for (let up = 3; up <= height + 5; up++) {
        const frame = Math.abs(offset) === 4 || up >= 3 + height - Math.max(0, Math.abs(offset) - 1)
        api.setBlock(structure, position(center + offset, up, depth), frame ? 'minecraft:prismarine_bricks' : up === 3 ? 'minecraft:water[level=0]' : 'minecraft:air')
      }
      api.setBlock(structure, position(center + offset, 2, depth), 'minecraft:prismarine_bricks')
    }
    api.setBlock(structure, position(center, height + 4, 2), 'minecraft:sea_lantern')
  }
  for (const [east, south, width, depth] of [[3, 17, 3, 5], [29, 18, 3, 5], [4, 4, 4, 2], [25, 4, 4, 2], [3, 28, 4, 3], [28, 28, 4, 3]]) {
    box([east, 3, south], [east + width - 1, 4, south + depth - 1], 'prismarine')
    box([east, 5, south], [east + width - 1, 5, south + depth - 1], 'sandstone')
  }
  for (let east = 10; east <= 24; east++) for (let south = 13; south <= 25; south++) {
    const distance = Math.abs(east - 17) + Math.abs(south - 19)
    if (distance <= 10) block(east, 3, south, 'sandstone')
    if (distance <= 8) block(east, 4, south, 'prismarine_bricks')
    if (distance <= 6) block(east, 5, south, (east + south) % 4 ? 'prismarine' : 'purple_terracotta')
    if (distance <= 4) block(east, 6, south, (east * south) % 3 ? 'moss_block' : 'purple_terracotta')
    if (distance <= 2) block(east, 7, south, 'prismarine')
  }
  for (const [east, south] of [[12, 19], [22, 19], [17, 24], [17, 14]]) block(east, 5, south, 'sea_lantern')
  box([3, 13, 0], [7, 13, 3], 'prismarine_bricks')
  box([26, 13, 0], [30, 13, 3], 'prismarine_bricks')
  box([15, 17, 0], [19, 17, 3], 'prismarine_bricks')
  const coral = (east, up, south, tone, height) => {
    const material = tone === 'pink' ? 'brain_coral_block' : tone === 'orange' ? 'fire_coral_block' : 'bubble_coral_block'
    const living = tone === 'pink' ? 'brain_coral' : tone === 'orange' ? 'fire_coral' : 'bubble_coral'
    for (let rise = 0; rise < height; rise++) block(east, up + rise, south, material)
    for (const [deltaEast, deltaSouth, rise] of [[-1, 0, 1], [1, 0, 2], [0, -1, 2], [0, 1, 1]]) {
      const joint = up + Math.min(rise, height - 1)
      block(east + deltaEast, joint, south + deltaSouth, material)
      block(east + deltaEast * 2, joint, south + deltaSouth * 2, material)
      block(east + deltaEast * 2, joint + 1, south + deltaSouth * 2, material)
      block(east + deltaEast * 2, joint + 2, south + deltaSouth * 2, `${living}[waterlogged=true]`)
    }
    block(east, up + height, south, `${living}_fan[waterlogged=true]`)
  }
  for (const [east, up, south, tone, height] of [[17, 8, 19, 'pink', 4], [13, 6, 21, 'orange', 2], [21, 6, 18, 'orange', 3], [19, 6, 23, 'purple', 2], [5, 6, 19, 'pink', 2], [30, 6, 20, 'purple', 2], [6, 6, 5, 'orange', 2], [27, 6, 5, 'purple', 2], [5, 6, 29, 'pink', 2], [29, 6, 29, 'orange', 2], [5, 14, 2, 'pink', 2], [28, 14, 2, 'orange', 2], [17, 18, 2, 'purple', 1]]) coral(east, up, south, tone, height)
  for (const [east, south] of [[7, 24], [27, 24], [9, 5], [25, 5], [8, 28], [26, 28], [5, 15], [29, 15]]) {
    block(east, 3, south, 'sea_pickle[pickles=3,waterlogged=true]')
    block(east + 1, 3, south, 'seagrass')
  }
  for (const [east, south, top] of [[6, 22, 7], [28, 23, 8], [6, 26, 6], [28, 26, 7], [9, 3, 8], [25, 3, 9], [7, 3, 12], [27, 3, 11], [7, 27, 9], [27, 27, 10]]) {
    for (let up = 3; up < top; up++) block(east, up, south, 'kelp_plant')
    block(east, top, south, 'kelp[age=25]')
  }
  for (const [east, south] of [[9, 4], [25, 4], [4, 23], [30, 23], [8, 31], [26, 31]]) {
    block(east, 3, south, 'prismarine_bricks')
    block(east, 4, south, 'sea_lantern')
  }
  for (let east = 14; east <= 20; east++) for (let south = 24; south <= 26; south++) {
    const up = 29 - south
    block(east, up, south, `prismarine_brick_stairs[facing=north,half=bottom,shape=straight,waterlogged=${up === 3}]`)
  }
  for (const entry of structure.getBlocks()) {
    if (entry.state.getName().toString() === 'minecraft:air') api.setBlock(structure, entry.pos, 'minecraft:water[level=0]')
  }
  return {structure, metadata: {name: 's5-01-coral-chamber-redo', reference: 'S5-01-large-chamber.png', origin: [0, 0, 0], exits: [[17, 3, 0], [0, 3, 10], [34, 3, 10]]}}
}
