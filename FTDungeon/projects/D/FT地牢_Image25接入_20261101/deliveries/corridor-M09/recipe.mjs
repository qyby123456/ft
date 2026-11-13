export default function build(api) {
  const size = [15, 12, 41]
  const structure = api.createStructure(size)
  const set = (posX, posY, posZ, block) => api.setBlock(structure, [posX, posY, posZ], `minecraft:${block}`)
  const fill = (from, to, block) => api.fill(structure, from, to, `minecraft:${block}`)
  const noise = (posX, posY, posZ) => Math.abs(posX * 193 + posY * 71 + posZ * 139 + posX * posZ * 17) % 101
  const ribs = [1, 8, 15, 22, 29, 38]
  fill([0, 0, 0], [14, 11, 40], 'air')
  fill([0, 0, 0], [14, 0, 40], 'obsidian')
  for (let posX = 0; posX < 15; posX++) {
    for (let posZ = 0; posZ < 41; posZ++) {
      const value = noise(posX, 1, posZ)
      set(posX, 1, posZ, value < 14 ? 'cracked_deepslate_tiles' : value < 31 ? 'polished_blackstone_bricks' : 'deepslate_tiles')
      set(posX, 11, posZ, posX < 3 || posX > 11 ? 'obsidian' : posZ % 7 < 2 ? 'polished_blackstone_bricks' : 'deepslate_tiles')
    }
  }
  for (const [outer, inner] of [[0, 2], [12, 14]]) {
    fill([outer, 2, 0], [inner, 10, 40], 'obsidian')
  }
  for (let posZ = 0; posZ < 41; posZ++) {
    for (const wallX of [2, 12]) {
      for (let posY = 2; posY <= 10; posY++) {
        if ((posZ % 7 === 3 || posZ % 7 === 4) && posY !== 5) {
          set(wallX, posY, posZ, 'crying_obsidian')
        } else if (noise(wallX, posY, posZ) < 12) {
          set(wallX, posY, posZ, 'cracked_polished_blackstone_bricks')
        }
      }
      set(wallX, 5, posZ, 'polished_blackstone_bricks')
      set(wallX, 9, posZ, 'polished_blackstone')
    }
    for (const sideX of [0, 14]) {
      set(sideX, 2, posZ, 'polished_deepslate')
      set(sideX, 10, posZ, 'polished_blackstone_bricks')
    }
  }
  for (const ribZ of ribs) {
    for (let depth = 0; depth < 2; depth++) {
      const posZ = ribZ + depth
      for (const posX of [3, 11]) {
        fill([posX, 2, posZ], [posX, 6, posZ], 'crying_obsidian')
        set(posX, 2, posZ, 'polished_deepslate')
        set(posX, 5, posZ, depth === 0 ? 'gold_block' : 'gilded_blackstone')
        set(posX, 6, posZ, 'chiseled_polished_blackstone')
      }
      fill([3, 7, posZ], [4, 7, posZ], 'deepslate_bricks')
      fill([10, 7, posZ], [11, 7, posZ], 'deepslate_bricks')
      fill([4, 8, posZ], [5, 8, posZ], 'deepslate_bricks')
      fill([9, 8, posZ], [10, 8, posZ], 'deepslate_bricks')
      fill([5, 9, posZ], [9, 9, posZ], 'deepslate_bricks')
      fill([4, 10, posZ], [10, 10, posZ], 'polished_deepslate')
      set(4, 6, posZ, 'deepslate_brick_stairs[facing=west,half=top,shape=straight,waterlogged=false]')
      set(10, 6, posZ, 'deepslate_brick_stairs[facing=east,half=top,shape=straight,waterlogged=false]')
      set(6, 8, posZ, 'deepslate_brick_stairs[facing=west,half=top,shape=straight,waterlogged=false]')
      set(8, 8, posZ, 'deepslate_brick_stairs[facing=east,half=top,shape=straight,waterlogged=false]')
      for (const sideX of [0, 14]) {
        fill([sideX, 3, posZ], [sideX, 9, posZ], 'polished_deepslate')
        set(sideX, 6, posZ, 'chiseled_polished_blackstone')
      }
      set(7, 11, posZ, 'gilded_blackstone')
    }
    set(4, 5, ribZ, 'polished_blackstone_button[face=wall,facing=east,powered=false]')
    set(10, 5, ribZ, 'polished_blackstone_button[face=wall,facing=west,powered=false]')
    set(7, 9, ribZ, 'gilded_blackstone')
  }
  for (const posZ of [0, 40]) {
    fill([3, 2, posZ], [11, 10, posZ], 'obsidian')
    fill([5, 2, posZ], [9, 6, posZ], 'air')
    fill([6, 7, posZ], [8, 7, posZ], 'air')
    for (const posX of [4, 10]) {
      fill([posX, 2, posZ], [posX, 6, posZ], 'deepslate_bricks')
      set(posX, 5, posZ, 'gilded_blackstone')
    }
    fill([4, 7, posZ], [5, 7, posZ], 'deepslate_bricks')
    fill([9, 7, posZ], [10, 7, posZ], 'deepslate_bricks')
    fill([5, 8, posZ], [9, 8, posZ], 'deepslate_bricks')
    set(7, 8, posZ, 'gilded_blackstone')
  }
  const candleBays = [4, 11, 18, 25, 32, 36]
  for (const posZ of candleBays) {
    for (const side of [-1, 1]) {
      const wallX = side < 0 ? 2 : 12
      const backingX = side < 0 ? 1 : 13
      const ledgeX = side < 0 ? 3 : 11
      for (let posY = 2; posY <= 8; posY++) {
        set(backingX, posY, posZ, 'shroomlight')
        set(wallX, posY, posZ, 'orange_stained_glass')
      }
      set(ledgeX, 2, posZ, 'polished_blackstone_bricks')
      set(ledgeX, 3, posZ, `candle[candles=${posZ % 2 === 0 ? 3 : 4},lit=true,waterlogged=false]`)
      set(wallX, 5, posZ, 'gold_block')
      set(ledgeX, 5, posZ, `polished_blackstone_button[face=wall,facing=${side < 0 ? 'east' : 'west'},powered=false]`)
    }
  }
  for (const seamX of [4, 10]) {
    for (let posZ = 3; posZ <= 37; posZ++) {
      if (posZ % 7 >= 3 && posZ % 7 <= 5) {
        set(seamX, 0, posZ, 'shroomlight')
        set(seamX, 1, posZ, 'orange_stained_glass')
      }
    }
  }
  for (const centerZ of [7, 21, 35]) {
    fill([6, 1, centerZ - 1], [8, 1, centerZ + 1], 'polished_blackstone')
    for (const [posX, posZ] of [[7, centerZ - 1], [6, centerZ], [8, centerZ], [7, centerZ + 1]]) {
      set(posX, 1, posZ, 'gilded_blackstone')
    }
    set(7, 1, centerZ, 'gold_block')
  }
  for (const [posX, posZ] of [[4, 12], [10, 17], [4, 26], [10, 33], [3, 20], [11, 28]]) {
    set(posX, 2, posZ, 'deepslate_brick_slab[type=bottom,waterlogged=false]')
    set(posX === 4 ? 3 : posX === 10 ? 11 : posX, 2, posZ + 1, 'cracked_deepslate_bricks')
  }
  for (const [posX, posZ] of [[4, 15], [10, 23], [5, 29]]) {
    set(posX, posX === 5 ? 8 : 7, posZ, 'air')
    set(posX, posX === 5 ? 9 : 8, posZ, 'cracked_deepslate_bricks')
  }
  for (const [posX, posZ] of [[3, 15], [11, 22], [5, 30], [9, 16]]) {
    set(posX, posX === 3 || posX === 11 ? 6 : 9, posZ, 'cracked_deepslate_bricks')
  }
  return {
    structure,
    metadata: {
      name: 'room', design: 'M09-obsidian-trial-damaged', corridorLength: 41,
      entrance: [7, 2, 0], interior: [7, 2, 3], exit: [7, 2, 40],
      theme: 'Obsidian trial, damaged arches, warm candles, safe encapsulated orange seams',
    },
  }
}
