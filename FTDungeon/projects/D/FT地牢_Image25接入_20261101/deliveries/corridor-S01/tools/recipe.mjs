export const SIZE = [13, 11, 16]

export default function build(api) {
  const structure = api.createStructure(SIZE)
  const set = (posX, posY, posZ, block, entity) => api.setBlock(structure, [posX, posY, posZ], block, undefined, entity)
  const fill = (from, to, block) => api.fill(structure, from, to, block)
  const masonry = (posX, posY, posZ) => {
    const noise = (posX * 31 + posY * 17 + posZ * 13) % 19
    return noise < 5 ? 'minecraft:mossy_stone_bricks' : noise < 8 ? 'minecraft:cracked_stone_bricks' : 'minecraft:stone_bricks'
  }
  const skull = (posX, posY, posZ, rotation) => set(posX, posY, posZ, `minecraft:skeleton_skull[rotation=${rotation}]`, api.createBlockNbt({ id: 'minecraft:skull' }))
  fill([0, 0, 0], [12, 10, 15], 'minecraft:air')
  fill([0, 0, 0], [12, 0, 15], 'minecraft:stone_bricks')
  for (let posX = 0; posX < 13; posX++) {
    for (let posZ = 0; posZ < 16; posZ++) {
      let floor = masonry(posX, 1, posZ)
      if (posX === 4 || posX === 8) floor = 'minecraft:polished_andesite'
      if (posX === 6 && [3, 4, 11, 12].includes(posZ)) floor = 'minecraft:bone_block[axis=y]'
      if (posX === 6 && [2, 5, 10, 13].includes(posZ)) floor = 'minecraft:chiseled_stone_bricks'
      set(posX, 1, posZ, floor)
      set(posX, 10, posZ, masonry(posX, 10, posZ))
    }
  }
  for (let posY = 2; posY < 10; posY++) {
    for (let posZ = 0; posZ < 16; posZ++) {
      for (const posX of [0, 1, 2, 10, 11, 12]) set(posX, posY, posZ, masonry(posX, posY, posZ))
    }
    for (const posZ of [0, 15]) {
      for (let posX = 0; posX < 13; posX++) set(posX, posY, posZ, masonry(posX, posY, posZ))
    }
  }
  for (const posZ of [0, 5, 10, 15]) {
    fill([2, 9, posZ], [10, 9, posZ], 'minecraft:dark_oak_log[axis=x]')
    for (const posX of [3, 9]) {
      fill([posX, 2, posZ], [posX, 3, posZ], 'minecraft:chiseled_stone_bricks')
      fill([posX, 4, posZ], [posX, 6, posZ], 'minecraft:bone_block[axis=y]')
      set(posX, 7, posZ, 'minecraft:bone_block[axis=x]')
    }
    set(4, 7, posZ, 'minecraft:stone_brick_stairs[facing=west,half=top,shape=straight,waterlogged=false]')
    set(8, 7, posZ, 'minecraft:stone_brick_stairs[facing=east,half=top,shape=straight,waterlogged=false]')
    set(5, 8, posZ, 'minecraft:stone_brick_stairs[facing=west,half=top,shape=straight,waterlogged=false]')
    set(7, 8, posZ, 'minecraft:stone_brick_stairs[facing=east,half=top,shape=straight,waterlogged=false]')
    set(6, 9, posZ, 'minecraft:bone_block[axis=x]')
  }
  for (const posZ of [0, 15]) {
    fill([5, 2, posZ], [7, 5, posZ], 'minecraft:air')
    for (const posX of [4, 8]) {
      fill([posX, 2, posZ], [posX, 3, posZ], 'minecraft:chiseled_stone_bricks')
      fill([posX, 4, posZ], [posX, 6, posZ], 'minecraft:bone_block[axis=y]')
    }
    fill([4, 7, posZ], [8, 7, posZ], 'minecraft:bone_block[axis=x]')
    set(5, 6, posZ, 'minecraft:stone_brick_stairs[facing=west,half=top,shape=straight,waterlogged=false]')
    set(7, 6, posZ, 'minecraft:stone_brick_stairs[facing=east,half=top,shape=straight,waterlogged=false]')
    skull(6, 8, posZ, posZ === 0 ? 8 : 0)
  }
  for (const side of [{ inner: 2, back: 1, facing: 'west', rotation: 12 }, { inner: 10, back: 11, facing: 'east', rotation: 4 }]) {
    for (const startZ of [2, 13]) {
      fill([side.inner, 3, startZ], [side.inner, 7, startZ + 1], 'minecraft:air')
      for (let posZ = startZ; posZ <= startZ + 1; posZ++) {
        set(side.inner, 2, posZ, 'minecraft:mossy_stone_bricks')
        set(side.inner, 5, posZ, 'minecraft:chiseled_stone_bricks')
        set(side.inner, 8, posZ, 'minecraft:bone_block[axis=z]')
        set(side.inner, 3, posZ, `minecraft:candle[candles=${posZ === startZ ? 3 : 2},lit=true,waterlogged=false]`)
      }
      skull(side.inner, 6, startZ, side.rotation)
      set(side.inner, 6, startZ + 1, 'minecraft:bone_block[axis=x]')
    }
    for (const posZ of [7, 8]) {
      fill([side.back, 2, posZ], [side.back, 6, posZ], 'minecraft:dark_oak_planks')
      for (let posY = 2; posY <= 5; posY++) set(side.inner, posY, posZ, `minecraft:dark_oak_trapdoor[facing=${side.facing},half=bottom,open=true,powered=false,waterlogged=false]`)
      set(side.inner, 6, posZ, 'minecraft:bone_block[axis=y]')
    }
  }
  for (const centerX of [3, 9]) {
    for (const centerZ of [4, 11]) {
      for (let offsetX = -1; offsetX <= 1; offsetX++) {
        for (let offsetZ = -1; offsetZ <= 1; offsetZ++) {
          const posX = centerX + offsetX
          const posZ = centerZ + offsetZ
          set(posX, 6, posZ, 'minecraft:iron_trapdoor[facing=north,half=top,open=false,powered=false,waterlogged=false]')
          set(posX, 8, posZ, 'minecraft:iron_trapdoor[facing=north,half=bottom,open=false,powered=false,waterlogged=false]')
          if (offsetX !== 0 || offsetZ !== 0) set(posX, 7, posZ, 'minecraft:iron_bars[north=false,east=false,south=false,west=false,waterlogged=false]')
        }
      }
      set(centerX, 6, centerZ, 'minecraft:bone_block[axis=y]')
      skull(centerX, 7, centerZ, centerX === 3 ? 12 : 4)
      set(centerX, 8, centerZ, 'minecraft:iron_block')
      set(centerX, 9, centerZ, 'minecraft:chain[axis=y,waterlogged=false]')
    }
  }
  for (const posX of [3, 9]) {
    for (const posZ of [6, 9]) {
      fill([posX, 2, posZ], [posX, 3, posZ], 'minecraft:chiseled_stone_bricks')
      set(posX, 4, posZ, 'minecraft:end_rod[facing=up]')
      set(posX, 5, posZ, 'minecraft:end_rod[facing=up]')
    }
    const face = posX === 3 ? 'west' : 'east'
    for (const posZ of [1, 2, 7, 8, 13, 14]) {
      if (![2, 7, 8, 13, 14].includes(posZ)) {
        for (const posY of [3, 4, 5]) set(posX, posY, posZ, `minecraft:glow_lichen[down=false,east=${face === 'east'},north=false,south=false,up=false,west=${face === 'west'},waterlogged=false]`)
      }
      set(posX, 8, posZ, 'minecraft:azalea_leaves[distance=7,persistent=true,waterlogged=false]')
      if ([1, 7, 14].includes(posZ)) set(posX, 2, posZ, 'minecraft:moss_carpet')
    }
  }
  for (const posX of [4, 8]) {
    for (const posZ of [2, 7, 8, 13]) set(posX, 9, posZ, 'minecraft:glow_lichen[down=false,east=false,north=false,south=false,up=true,west=false,waterlogged=false]')
  }
  for (let posX = 5; posX <= 7; posX++) {
    for (let posY = 2; posY <= 4; posY++) {
      for (let posZ = 0; posZ < 16; posZ++) set(posX, posY, posZ, 'minecraft:air')
    }
  }
  return { structure, metadata: { name: 'room', id: 'S01', size: SIZE, entrance: [6, 2, 0], interior: [6, 2, 2], exit: [6, 2, 15] } }
}
