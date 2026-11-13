export default function build(api) {
  const size = [29, 16, 33]
  const structure = api.createStructure(size)
  const put = (horizontal, vertical, depth, block) => api.setBlock(structure, [horizontal, vertical, depth], block)
  const box = (start, end, block) => api.fill(structure, start, end, block)
  const aged = (horizontal, vertical, depth) => {
    const sample = (horizontal * 73 + vertical * 37 + depth * 61 + horizontal * depth * 11) % 23
    return sample < 3 ? 'minecraft:mossy_stone_bricks' : sample < 7 ? 'minecraft:cracked_stone_bricks' : 'minecraft:stone_bricks'
  }
  box([0, 0, 0], [28, 15, 32], 'minecraft:air')
  for (let horizontal = 0; horizontal < 29; horizontal++) {
    for (let depth = 0; depth < 33; depth++) {
      put(horizontal, 0, depth, aged(horizontal, 0, depth))
      put(horizontal, 14, depth, 'minecraft:deepslate_bricks')
      put(horizontal, 13, depth, 'minecraft:dark_oak_planks')
      for (let vertical = 1; vertical <= 12; vertical++) {
        if (horizontal < 2 || horizontal > 26 || depth < 2 || depth > 30) put(horizontal, vertical, depth, aged(horizontal, vertical, depth))
      }
    }
  }
  for (let depth = 2; depth < 31; depth++) {
    for (const horizontal of [0, 28]) {
      put(horizontal, 13, depth, 'minecraft:polished_andesite')
      put(horizontal, 15, depth, 'minecraft:stone_brick_slab[type=bottom,waterlogged=false]')
    }
  }
  for (let horizontal = 0; horizontal < 29; horizontal++) {
    for (const depth of [0, 32]) {
      put(horizontal, 13, depth, 'minecraft:polished_andesite')
      put(horizontal, 15, depth, 'minecraft:stone_brick_slab[type=bottom,waterlogged=false]')
    }
  }
  for (const horizontal of [0, 26]) {
    for (const depth of [0, 30]) {
      box([horizontal, 1, depth], [horizontal + 2, 14, depth + 2], 'minecraft:stone_bricks')
      box([horizontal, 15, depth], [horizontal + 2, 15, depth + 2], 'minecraft:stone_brick_slab[type=bottom,waterlogged=false]')
    }
  }
  box([13, 1, 0], [15, 4, 1], 'minecraft:air')
  for (const horizontal of [12, 16]) box([horizontal, 1, 0], [horizontal, 4, 1], 'minecraft:chiseled_stone_bricks')
  box([12, 5, 0], [16, 5, 1], 'minecraft:polished_andesite')
  box([13, 6, 0], [15, 6, 1], 'minecraft:stone_bricks')
  for (const horizontal of [0, 28]) {
    for (const depth of [5, 13, 21, 28]) {
      for (let offset = 0; offset < 2; offset++) {
        for (let vertical = 4; vertical <= 9; vertical++) {
          put(horizontal, vertical, depth + offset, 'minecraft:light_blue_stained_glass')
          put(horizontal === 0 ? 1 : 27, vertical, depth + offset, 'minecraft:air')
        }
      }
    }
  }
  for (const horizontal of [6, 21]) {
    box([horizontal, 4, 32], [horizontal + 1, 9, 32], 'minecraft:light_blue_stained_glass')
    box([horizontal, 4, 31], [horizontal + 1, 9, 31], 'minecraft:air')
  }
  for (const horizontal of [4, 24]) {
    for (const depth of [8, 16, 24]) {
      box([horizontal - 1, 1, depth - 1], [horizontal + 1, 1, depth + 1], 'minecraft:polished_andesite')
      box([horizontal, 2, depth], [horizontal, 10, depth], 'minecraft:stone_bricks')
      box([horizontal - 1, 11, depth - 1], [horizontal + 1, 11, depth + 1], 'minecraft:polished_andesite')
      box([horizontal - 1, 12, depth], [horizontal + 1, 12, depth], 'minecraft:chiseled_stone_bricks')
    }
  }
  for (const depth of [8, 16, 24]) {
    box([4, 12, depth], [24, 12, depth], 'minecraft:stripped_dark_oak_log[axis=x]')
    for (const horizontal of [5, 23]) {
      put(horizontal, 10, depth, `minecraft:stone_brick_stairs[facing=${horizontal === 5 ? 'east' : 'west'},half=top,shape=straight,waterlogged=false]`)
      put(horizontal, 11, depth, 'minecraft:stone_bricks')
    }
  }
  for (const horizontal of [4, 24]) box([horizontal, 12, 2], [horizontal, 12, 30], 'minecraft:stripped_dark_oak_log[axis=z]')
  for (let horizontal = 10; horizontal <= 18; horizontal++) {
    for (let depth = 9; depth <= 25; depth++) {
      const trim = horizontal === 10 || horizontal === 18 || depth === 9 || depth === 25
      put(horizontal, 0, depth, trim ? 'minecraft:yellow_terracotta' : (horizontal + depth) % 11 === 0 ? 'minecraft:brown_terracotta' : 'minecraft:red_wool')
    }
  }
  for (const horizontal of [13, 14, 15]) {
    for (let depth = 0; depth <= 8; depth++) put(horizontal, 0, depth, 'minecraft:polished_andesite')
  }
  for (const depth of [11, 17, 23]) {
    for (const horizontal of [12, 16]) put(horizontal, 1, depth, 'minecraft:stripped_spruce_log[axis=y]')
  }
  box([12, 2, 11], [16, 2, 23], 'minecraft:dark_oak_planks')
  box([14, 3, 11], [14, 3, 23], 'minecraft:red_carpet')
  for (const depth of [12, 15, 18, 21, 23]) {
    for (const horizontal of [12, 16]) put(horizontal, 3, depth, 'minecraft:white_carpet')
  }
  for (const depth of [13, 17, 21]) put(14, 3, depth, 'minecraft:candle[candles=3,lit=true,waterlogged=false]')
  for (const depth of [12, 15, 18, 21, 23]) {
    for (const horizontal of [11, 17]) {
      const facing = horizontal === 11 ? 'west' : 'east'
      const backFacing = horizontal === 11 ? 'east' : 'west'
      put(horizontal, 1, depth, `minecraft:spruce_stairs[facing=${facing},half=bottom,shape=straight,waterlogged=false]`)
      put(horizontal, 2, depth, `minecraft:spruce_trapdoor[facing=${backFacing},half=bottom,open=true,powered=false,waterlogged=false]`)
    }
  }
  for (const depth of [15, 19]) box([12, 9, depth], [16, 9, depth], 'minecraft:dark_oak_planks')
  for (const horizontal of [12, 16]) {
    box([horizontal, 9, 15], [horizontal, 9, 19], 'minecraft:dark_oak_planks')
    for (const depth of [15, 19]) box([horizontal, 10, depth], [horizontal, 12, depth], 'minecraft:chain[axis=y,waterlogged=false]')
    put(horizontal, 8, 17, 'minecraft:lantern[hanging=true,waterlogged=false]')
    put(horizontal, 10, 17, 'minecraft:candle[candles=2,lit=true,waterlogged=false]')
  }
  for (const depth of [15, 19]) {
    put(14, 8, depth, 'minecraft:lantern[hanging=true,waterlogged=false]')
    put(14, 10, depth, 'minecraft:candle[candles=2,lit=true,waterlogged=false]')
  }
  for (const horizontal of [3, 25]) {
    for (const depth of [11, 19, 27]) {
      box([horizontal, 5, depth], [horizontal, 12, depth], 'minecraft:chain[axis=y,waterlogged=false]')
      put(horizontal, 4, depth, 'minecraft:lantern[hanging=true,waterlogged=false]')
    }
  }
  for (const horizontal of [10, 18]) {
    box([horizontal, 6, 2], [horizontal, 12, 2], 'minecraft:chain[axis=y,waterlogged=false]')
    put(horizontal, 5, 2, 'minecraft:lantern[hanging=true,waterlogged=false]')
  }
  for (const horizontal of [2, 26]) {
    for (const depth of [10, 18, 26]) {
      box([horizontal, 5, depth], [horizontal, 9, depth + 1], 'minecraft:red_wool')
      box([horizontal, 4, depth], [horizontal, 4, depth + 1], 'minecraft:yellow_terracotta')
      box([horizontal, 10, depth], [horizontal, 10, depth + 1], 'minecraft:stripped_spruce_log[axis=z]')
    }
  }
  box([13, 4, 30], [15, 10, 30], 'minecraft:red_wool')
  box([13, 3, 30], [15, 3, 30], 'minecraft:yellow_terracotta')
  box([13, 11, 30], [15, 11, 30], 'minecraft:stripped_spruce_log[axis=x]')
  for (const horizontal of [2, 24]) {
    box([horizontal, 1, 11], [horizontal + 2, 1, 13], 'minecraft:spruce_planks')
    box([horizontal, 2, 11], [horizontal + 2, 2, 13], 'minecraft:dark_oak_planks')
    put(horizontal + 1, 3, 12, 'minecraft:candle[candles=3,lit=true,waterlogged=false]')
    box([horizontal, 1, 28], [horizontal + 1, 1, 29], 'minecraft:bookshelf')
    box([horizontal, 2, 28], [horizontal + 1, 2, 29], 'minecraft:spruce_slab[type=bottom,waterlogged=false]')
  }
  for (const [horizontal, depth] of [[3, 4], [24, 4], [4, 20], [24, 21], [3, 26], [25, 25], [10, 29], [18, 29]]) {
    put(horizontal, 1, depth, 'minecraft:mossy_cobblestone')
    put(horizontal + 1, 1, depth, 'minecraft:cobblestone_slab[type=bottom,waterlogged=false]')
  }
  for (const [horizontal, depth, facing] of [[3, 5, 'north'], [25, 6, 'south'], [3, 22, 'west'], [25, 23, 'east']]) {
    put(horizontal, 1, depth, 'minecraft:spruce_slab[type=bottom,waterlogged=false]')
    put(horizontal, 1, depth + 1, `minecraft:spruce_trapdoor[facing=${facing},half=bottom,open=false,powered=false,waterlogged=false]`)
  }
  for (const horizontal of [10, 18]) {
    put(horizontal, 1, 28, 'minecraft:dark_oak_planks')
    put(horizontal, 2, 28, 'minecraft:lantern[hanging=false,waterlogged=false]')
  }
  return { structure, metadata: { name: 'room', title: 'H02 废墟宴会厅', size, entrance: [14, 1, 0], interior: [14, 1, 5] } }
}
