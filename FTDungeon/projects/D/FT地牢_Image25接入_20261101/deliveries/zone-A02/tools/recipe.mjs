export const SIZE = [21, 12, 21]
export const PILLARS = [[3, 3], [17, 3], [3, 17], [17, 17]]
export const LIGHTS = [[6, 3], [14, 3], [6, 17], [14, 17]]
export const TARGETS = [[10, 2, 10], [8, 2, 8], [2, 2, 10], [15, 2, 13], [16, 2, 6], [10, 2, 20]]
export function roofHole(posX, posZ) {
  if (posX === 17 && posZ === 3) return false
  if (posX === 20 && posZ >= 1 && posZ <= 6) return true
  if (posZ === 0 && posX >= 15 && posX <= 19 && posX !== 17) return true
  return posX >= 13 && posX <= 19 && posZ >= 1 && posZ <= 6 && !(posX === 13 && posZ >= 5) && !(posX === 19 && posZ === 6)
}
export default function build(api) {
  const structure = api.createStructure(SIZE)
  const set = (posX, posY, posZ, block, entity) => api.setBlock(structure, [posX, posY, posZ], block, undefined, entity)
  const fill = (from, to, block) => api.fill(structure, from, to, block)
  const masonry = (posX, posY, posZ) => {
    const noise = (posX * 37 + posY * 13 + posZ * 23) % 23
    return noise < 6 ? 'minecraft:cracked_stone_bricks' : noise < 8 ? 'minecraft:mossy_stone_bricks' : 'minecraft:stone_bricks'
  }
  fill([0, 0, 0], [20, 11, 20], 'minecraft:air')
  fill([0, 0, 0], [20, 0, 20], 'minecraft:cobblestone')
  for (let posX = 0; posX <= 20; posX++) {
    for (let posZ = 0; posZ <= 20; posZ++) {
      const border = posX === 0 || posX === 20 || posZ === 0 || posZ === 20
      const worn = (posX * 11 + posZ * 19) % 29
      let floor = border || worn < 4 ? masonry(posX, 1, posZ) : worn < 7 ? 'minecraft:spruce_planks' : 'minecraft:dark_oak_planks'
      if (posX >= 8 && posX <= 12) floor = posX === 8 || posX === 12 ? 'minecraft:polished_andesite' : worn < 8 ? 'minecraft:andesite' : masonry(posX, 1, posZ)
      if (posX >= 7 && posX <= 13 && posZ >= 10 && posZ <= 14 && (posX + posZ * 3) % 11 >= 2) floor = posX === 7 || posX === 13 || posZ === 10 || posZ === 14 ? 'minecraft:brown_wool' : (posX + posZ) % 7 === 0 ? 'minecraft:light_gray_wool' : 'minecraft:red_wool'
      set(posX, 1, posZ, floor)
      if (!roofHole(posX, posZ)) {
        set(posX, 10, posZ, border ? masonry(posX, 10, posZ) : 'minecraft:dark_oak_planks')
        if (border) set(posX, 11, posZ, 'minecraft:stone_brick_slab[type=bottom,waterlogged=false]')
      }
    }
  }
  for (let posY = 2; posY <= 9; posY++) {
    for (let lateral = 0; lateral <= 20; lateral++) {
      for (const wall of [0, 20]) {
        set(wall, posY, lateral, masonry(wall, posY, lateral))
        set(lateral, posY, wall, masonry(lateral, posY, wall))
      }
    }
  }
  for (const wall of [0, 20]) {
    fill([9, 2, wall], [11, 5, wall], 'minecraft:air')
    fill([8, 2, wall], [8, 5, wall], 'minecraft:polished_andesite')
    fill([12, 2, wall], [12, 5, wall], 'minecraft:polished_andesite')
    fill([8, 6, wall], [12, 6, wall], 'minecraft:dark_oak_log[axis=x]')
    set(10, 7, wall, 'minecraft:chiseled_stone_bricks')
  }
  for (const [posX, posZ] of PILLARS) {
    set(posX, 2, posZ, 'minecraft:polished_andesite')
    fill([posX, 3, posZ], [posX, 7, posZ], 'minecraft:dark_oak_log[axis=y]')
    set(posX, 8, posZ, 'minecraft:polished_andesite')
  }
  for (const beam of [3, 17]) {
    fill([0, 9, beam], [20, 9, beam], 'minecraft:dark_oak_log[axis=x]')
    fill([beam, 9, 0], [beam, 9, 20], 'minecraft:dark_oak_log[axis=z]')
  }
  fill([0, 9, 0], [20, 9, 0], 'minecraft:dark_oak_log[axis=x]')
  fill([0, 9, 20], [20, 9, 20], 'minecraft:dark_oak_log[axis=x]')
  fill([0, 9, 1], [0, 9, 19], 'minecraft:dark_oak_log[axis=z]')
  fill([20, 9, 1], [20, 9, 19], 'minecraft:dark_oak_log[axis=z]')
  for (const [posX, posZ] of LIGHTS) {
    set(posX, 8, posZ, 'minecraft:chain[axis=y,waterlogged=false]')
    set(posX, 7, posZ, 'minecraft:lantern[hanging=true,waterlogged=false]')
  }
  for (let posX = 14; posX <= 19; posX++) {
    for (let posY = 8; posY <= 11; posY++) {
      if (posX + posY > 24 && posX !== 17) set(posX, posY, 0, 'minecraft:air')
    }
  }
  for (let posZ = 1; posZ <= 6; posZ++) {
    for (let posY = 8; posY <= 11; posY++) {
      if (posY >= 8 + posZ % 3) set(20, posY, posZ, 'minecraft:air')
    }
  }
  for (const [posX, posZ, height] of [[14, 2, 1], [15, 2, 2], [16, 2, 1], [18, 2, 2], [19, 2, 3], [19, 3, 2], [18, 4, 1], [19, 4, 2], [18, 5, 1], [19, 5, 1], [14, 4, 1], [15, 5, 1], [13, 6, 1]]) {
    for (let layer = 0; layer < height; layer++) set(posX, 2 + layer, posZ, layer === height - 1 ? 'minecraft:cobblestone_slab[type=bottom,waterlogged=false]' : 'minecraft:andesite')
  }
  fill([18, 2, 7], [19, 2, 8], 'minecraft:dark_oak_log[axis=z]')
  set(19, 3, 7, 'minecraft:dark_oak_slab[type=bottom,waterlogged=false]')
  for (const posX of [4, 5]) {
    const color = posX === 4 ? 'red' : 'white'
    set(posX, 2, 7, 'minecraft:' + color + '_bed[facing=north,part=head,occupied=false]', api.createBlockNbt({ id: 'minecraft:bed' }))
    set(posX, 2, 8, 'minecraft:' + color + '_bed[facing=north,part=foot,occupied=false]', api.createBlockNbt({ id: 'minecraft:bed' }))
  }
  fill([3, 2, 6], [6, 2, 6], 'minecraft:dark_oak_planks')
  for (const posX of [3, 6]) {
    set(posX, 3, 6, 'minecraft:dark_oak_log[axis=y]')
    for (const posZ of [7, 8]) set(posX, 2, posZ, 'minecraft:dark_oak_slab[type=bottom,waterlogged=false]')
  }
  set(3, 2, 9, 'minecraft:dark_oak_log[axis=x]')
  set(6, 2, 9, 'minecraft:dark_oak_slab[type=bottom,waterlogged=false]')
  set(4, 2, 9, 'minecraft:white_carpet')
  set(5, 2, 10, 'minecraft:red_carpet')
  set(2, 2, 11, 'minecraft:dark_oak_planks')
  set(2, 3, 11, 'minecraft:candle[candles=1,lit=false,waterlogged=false]')
  set(2, 2, 12, 'minecraft:dark_oak_trapdoor[facing=south,half=bottom,open=true,powered=false,waterlogged=false]')
  fill([0, 3, 7], [0, 7, 10], 'minecraft:dark_oak_planks')
  for (let posY = 4; posY <= 6; posY++) {
    for (let posZ = 8; posZ <= 9; posZ++) set(0, posY, posZ, posY + posZ === 13 ? 'minecraft:gray_stained_glass' : 'minecraft:light_gray_stained_glass')
  }
  set(1, 5, 13, 'minecraft:red_wall_banner[facing=east]', api.createBlockNbt({ id: 'minecraft:banner', Patterns: [{ Pattern: 'dls', Color: 15 }, { Pattern: 'bs', Color: 12 }] }))
  fill([17, 2, 9], [19, 2, 12], 'minecraft:dark_oak_planks')
  fill([19, 3, 9], [19, 3, 12], 'minecraft:dark_oak_log[axis=z]')
  for (let posZ = 9; posZ <= 12; posZ++) {
    set(17, 3, posZ, 'minecraft:dark_oak_trapdoor[facing=west,half=bottom,open=false,powered=false,waterlogged=false]')
    set(18, 3, posZ, posZ % 2 ? 'minecraft:dark_oak_slab[type=bottom,waterlogged=false]' : 'minecraft:dark_oak_trapdoor[facing=west,half=bottom,open=false,powered=false,waterlogged=false]')
  }
  for (const [posX, posZ, color] of [[16, 9, 'white'], [15, 10, 'red'], [16, 11, 'light_gray'], [15, 12, 'gray'], [14, 12, 'white']]) set(posX, 2, posZ, 'minecraft:' + color + '_carpet')
  fill([18, 2, 15], [19, 2, 15], 'minecraft:dark_oak_planks')
  fill([19, 3, 15], [19, 5, 15], 'minecraft:bookshelf')
  fill([18, 3, 15], [18, 5, 15], 'minecraft:dark_oak_log[axis=y]')
  fill([18, 6, 15], [19, 6, 15], 'minecraft:dark_oak_slab[type=bottom,waterlogged=false]')
  for (const [posX, posY, posZ] of [[1, 8, 1], [1, 7, 19], [19, 7, 19], [19, 6, 14], [1, 6, 14], [14, 8, 1]]) set(posX, posY, posZ, 'minecraft:cobweb')
  set(5, 2, 16, 'minecraft:dark_oak_log[axis=x]')
  set(6, 2, 16, 'minecraft:dark_oak_slab[type=bottom,waterlogged=false]')
  set(14, 2, 18, 'minecraft:cobblestone_slab[type=bottom,waterlogged=false]')
  set(15, 2, 18, 'minecraft:andesite')
  return { structure, metadata: { name: 'room', id: 'zone-A02', title: 'A02 塌毁卧室', entrance: [10, 2, 0], safePoint: [10, 2, 10] } }
}

