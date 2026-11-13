export const design = {
  id: 'zone-R04', name: '焦毁营房', size: [25, 13, 25],
  entrance: [12, 2, 0], interior: [12, 2, 5], exit: [12, 2, 24],
  pillars: [[6, 6], [17, 6], [6, 17], [17, 17]],
  bunks: [{ minX: 2, maxX: 4, minZ: 9, maxZ: 15, ladder: [5, 9, 'east'] }, { minX: 20, maxX: 22, minZ: 9, maxZ: 15, ladder: [19, 15, 'west'] }],
  fallen: [[3, 2, 4], [4, 2, 4], [5, 3, 4], [6, 3, 4], [7, 4, 4], [18, 2, 20], [19, 2, 20], [20, 3, 20], [21, 3, 20], [22, 4, 20]],
  hanging: [[7, 11], [7, 14], [17, 11], [17, 14]],
  standing: [[3, 3, 18], [21, 3, 6]], embers: [[5, 5], [20, 19]],
  reference: 'D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/ruins/R04-ruins-burnt-ruined-barracks.png',
  reference_sha256: 'e04fa19324a4906d5110666f5b274868dd296e3f14f4d4657e2811de960b506a'
}

export default function build(api) {
  const structure = api.createStructure(design.size)
  const put = (posX, posY, posZ, block) => api.setBlock(structure, [posX, posY, posZ], 'minecraft:' + block)
  const fill = (start, end, block) => api.fill(structure, start, end, 'minecraft:' + block)
  const noise = (posX, posY, posZ) => (posX * 73 + posY * 151 + posZ * 37 + posX * posZ * 11) % 100
  const masonry = ['stone_bricks', 'cracked_stone_bricks', 'deepslate_bricks', 'polished_andesite', 'blackstone']
  fill([0, 0, 0], [24, 12, 24], 'air')
  fill([0, 0, 0], [24, 0, 24], 'deepslate_tiles')
  for (let posX = 0; posX < 25; posX++) for (let posZ = 0; posZ < 25; posZ++) {
    const lane = posX >= 10 && posX <= 14
    put(posX, 1, posZ, lane ? ((posX + posZ) % 7 === 0 ? 'polished_andesite' : 'stone_bricks') : masonry[Math.floor(noise(posX, 1, posZ) / 20)])
    for (let posY = 2; posY <= 10; posY++) if (posX === 0 || posX === 24 || posZ === 0 || posZ === 24) put(posX, posY, posZ, masonry[Math.floor(noise(posX, posY, posZ) / 20)])
    const skylight = (posX >= 2 && posX <= 5 && posZ >= 17 && posZ <= 21) || (posX >= 19 && posX <= 22 && posZ >= 3 && posZ <= 7)
    put(posX, 11, posZ, skylight ? 'light_blue_stained_glass' : (noise(posX, 11, posZ) < 24 ? 'blackstone' : 'deepslate_tiles'))
    if ((posX === 0 || posX === 24 || posZ === 0 || posZ === 24) && noise(posX, 12, posZ) > 38) put(posX, 12, posZ, 'cracked_deepslate_bricks')
  }
  for (const posZ of [0, 24]) {
    fill([11, 2, posZ], [13, 5, posZ], 'air')
    for (const posX of [10, 14]) fill([posX, 2, posZ], [posX, 6, posZ], 'polished_basalt[axis=y]')
    fill([10, 6, posZ], [14, 6, posZ], 'polished_blackstone_bricks')
    for (const posX of [9, 15]) fill([posX, 2, posZ], [posX, 8, posZ], 'deepslate_bricks')
    fill([9, 8, posZ], [15, 8, posZ], 'deepslate_tiles')
  }
  for (const [posX, posZ] of design.pillars) {
    fill([posX, 2, posZ], [posX + 1, 2, posZ + 1], 'polished_blackstone_bricks')
    for (let posY = 3; posY <= 8; posY++) fill([posX, posY, posZ], [posX + 1, posY, posZ + 1], posY % 3 === 0 ? 'cracked_stone_bricks' : 'stone_bricks')
    fill([posX, 9, posZ], [posX + 1, 9, posZ + 1], 'deepslate_bricks')
  }
  for (const posZ of [6, 7, 17, 18]) fill([0, 10, posZ], [24, 10, posZ], 'dark_oak_log[axis=x]')
  for (const posX of [6, 7, 17, 18]) fill([posX, 10, 0], [posX, 10, 24], 'dark_oak_log[axis=z]')
  for (const posX of [3, 12, 21]) fill([posX, 10, 0], [posX, 10, 24], 'deepslate_bricks')
  for (const bunk of design.bunks) {
    for (const posX of [bunk.minX, bunk.maxX]) for (const posZ of [bunk.minZ, bunk.maxZ]) fill([posX, 2, posZ], [posX, 7, posZ], 'dark_oak_log[axis=y]')
    for (const posY of [2, 5]) {
      fill([bunk.minX, posY, bunk.minZ + 1], [bunk.maxX, posY, bunk.maxZ - 1], 'dark_oak_slab[type=top,waterlogged=false]')
      for (const posZ of [bunk.minZ, bunk.maxZ]) fill([bunk.minX, posY, posZ], [bunk.maxX, posY, posZ], 'dark_oak_log[axis=x]')
      for (const posZ of [10, 11, 12, 14]) {
        put(bunk.minX + 1, posY + 1, posZ, posZ === 10 ? 'white_carpet' : posZ === 14 ? 'gray_carpet' : 'light_gray_carpet')
        if (posZ === 11 || posZ === 12) put(bunk.minX, posY + 1, posZ, 'gray_carpet')
      }
    }
    const [ladderX, ladderZ, facing] = bunk.ladder
    for (let posY = 2; posY <= 6; posY++) put(ladderX, posY, ladderZ, `ladder[facing=${facing},waterlogged=false]`)
    for (let posZ = 10; posZ <= 14; posZ++) if (posZ !== 13) put(bunk.maxX, 7, posZ, 'dark_oak_slab[type=bottom,waterlogged=false]')
  }
  for (const [posX, posY, posZ] of design.fallen) {
    for (let supportY = 2; supportY < posY; supportY++) put(posX, supportY, posZ, supportY % 2 === 0 ? 'cobbled_deepslate' : 'blackstone')
    put(posX, posY, posZ, 'dark_oak_log[axis=x]')
  }
  for (const [posX, posZ] of [[2, 3], [3, 5], [8, 21], [9, 20], [16, 22], [21, 21], [22, 18], [3, 20]]) put(posX, 2, posZ, 'cobbled_deepslate_slab[type=bottom,waterlogged=false]')
  for (const [posX, posZ] of [[4, 6], [8, 5], [9, 19], [16, 20], [20, 21], [22, 17], [3, 17]]) put(posX, 2, posZ, 'light_gray_carpet')
  put(8, 2, 20, 'bone_block[axis=x]')
  put(9, 2, 21, 'bone_block[axis=z]')
  put(5, 2, 19, 'waxed_cut_copper_stairs[facing=east,half=bottom,shape=straight,waterlogged=false]')
  for (const [posX, posZ] of design.hanging) {
    put(posX, 9, posZ, 'chain[axis=y,waterlogged=false]')
    put(posX, 8, posZ, 'chain[axis=y,waterlogged=false]')
    put(posX, 7, posZ, 'lantern[hanging=true,waterlogged=false]')
  }
  for (const [posX, posY, posZ] of design.standing) {
    fill([posX, 2, posZ], [posX, posY - 1, posZ], 'dark_oak_planks')
    put(posX, posY, posZ, 'lantern[hanging=false,waterlogged=false]')
  }
  for (const [posX, posZ] of design.embers) {
    put(posX, 1, posZ, 'shroomlight')
    put(posX, 2, posZ, 'orange_stained_glass')
    put(posX, 3, posZ, 'iron_trapdoor[facing=north,half=bottom,open=false,powered=false,waterlogged=false]')
  }
  fill([7, 9, 23], [10, 9, 23], 'dark_oak_log[axis=x]')
  for (let posX = 8; posX <= 10; posX++) for (let posY = 5; posY <= 8; posY++) {
    if (posY === 5 && posX === 9) continue
    put(posX, posY, 23, noise(posX, posY, 23) < 38 ? 'black_wool' : 'red_wool')
  }
  for (const [posX, posZ] of [[3, 0], [20, 0], [0, 5], [24, 19], [5, 24], [19, 24]]) {
    fill([posX, 2, posZ], [posX, 4, posZ], 'blackstone')
    put(posX, 5, posZ, 'coal_block')
  }
  for (const posZ of [3, 21]) for (const posX of [10, 14]) put(posX, 1, posZ, 'chiseled_stone_bricks')
  return { structure, metadata: { ...design, name: 'room', title: design.name, roof: 'Continuous sealed charred roof with two glass-covered cold skylights', safety: 'No real fire, fluids, entities, block entities or loot' } }
}
