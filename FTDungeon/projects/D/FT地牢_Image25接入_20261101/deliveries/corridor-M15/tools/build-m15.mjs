export default function build(api) {
  const size = [15, 12, 40]
  const structure = api.createStructure(size)
  const put = (position, block) => api.setBlock(structure, position, `minecraft:${block}`)
  const fill = (start, end, block) => api.fill(structure, start, end, `minecraft:${block}`)
  const noise = (horizontal, vertical, depth) => Math.abs((horizontal * 371 + vertical * 197 + depth * 941) ^ (depth * 53 + horizontal * 17)) % 100
  const arches = [1, 7, 13, 19, 25, 31, 38]
  fill([0, 0, 0], [14, 11, 39], 'air')
  for (let depth = 0; depth < 40; depth++) {
    for (let horizontal = 0; horizontal < 15; horizontal++) {
      const wear = noise(horizontal, 0, depth)
      let floor = wear < 12 ? 'cracked_deepslate_tiles' : 'deepslate_tiles'
      if (horizontal === 4 || horizontal === 10) floor = 'polished_blackstone_bricks'
      if (horizontal === 7 && depth % 4 === 0) floor = 'chiseled_deepslate'
      if ((horizontal < 4 || horizontal > 10) && wear < 17) floor = 'moss_block'
      put([horizontal, 0, depth], floor)
      put([horizontal, 10, depth], depth % 6 === 1 ? 'polished_blackstone_bricks' : 'deepslate_tiles')
      if (horizontal < 2 || horizontal > 12 || arches.includes(depth)) put([horizontal, 11, depth], 'polished_blackstone_bricks')
    }
    for (const horizontal of [0, 1, 13, 14]) {
      for (let height = 1; height < 10; height++) {
        const wear = noise(horizontal, height, depth)
        let wall = wear < 18 ? 'cracked_polished_blackstone_bricks' : 'polished_blackstone_bricks'
        if (height === 1 || height === 5 || height === 9) wall = 'deepslate_bricks'
        if (height === 7 && depth % 3 === 0) wall = 'chiseled_polished_blackstone'
        put([horizontal, height, depth], wall)
      }
    }
  }
  for (const depth of [0, 39]) {
    fill([2, 1, depth], [12, 9, depth], 'polished_blackstone_bricks')
    fill([5, 1, depth], [9, 5, depth], 'air')
    put([4, 4, depth], 'deepslate_brick_stairs[facing=west,half=top,shape=straight,waterlogged=false]')
    put([10, 4, depth], 'deepslate_brick_stairs[facing=east,half=top,shape=straight,waterlogged=false]')
    fill([5, 6, depth], [9, 6, depth], 'chiseled_deepslate')
  }
  for (const [archIndex, depth] of arches.entries()) {
    for (const horizontal of [2, 12]) {
      fill([horizontal, 1, depth], [horizontal, 6, depth], 'polished_blackstone_bricks')
      put([horizontal, 2, depth], 'chiseled_polished_blackstone')
      put([horizontal, 5, depth], 'chiseled_deepslate')
      fill([horizontal - 1, 6, depth], [horizontal + 1, 6, depth], 'deepslate_bricks')
      put([horizontal, 7, depth], 'cracked_deepslate_bricks')
    }
    put([3, 7, depth], 'deepslate_brick_stairs[facing=west,half=top,shape=straight,waterlogged=false]')
    put([11, 7, depth], 'deepslate_brick_stairs[facing=east,half=top,shape=straight,waterlogged=false]')
    put([4, 8, depth], 'deepslate_bricks')
    put([10, 8, depth], 'deepslate_bricks')
    for (let horizontal = 5; horizontal <= 9; horizontal++) {
      if ((archIndex === 2 && (horizontal === 6 || horizontal === 7)) || (archIndex === 4 && horizontal === 8)) continue
      put([horizontal, 9, depth], horizontal === 7 ? 'chiseled_deepslate' : 'cracked_deepslate_bricks')
    }
    if (archIndex === 2 || archIndex === 4) {
      const horizontal = archIndex === 2 ? 3 : 11
      put([horizontal, 1, depth + 1], 'cobbled_deepslate')
      put([horizontal, 1, depth + 2], 'cobbled_deepslate_slab[type=bottom,waterlogged=false]')
    }
  }
  for (const [nicheIndex, depth] of [4, 10, 16, 22, 28, 34].entries()) {
    for (const horizontal of [2, 12]) {
      fill([horizontal, 1, depth], [horizontal, 2, depth], 'polished_blackstone_bricks')
      put([horizontal, 1, depth], 'chiseled_polished_blackstone')
      const shelfFront = horizontal === 2 ? 3 : 11
      put([shelfFront, 1, depth], 'chiseled_polished_blackstone')
      put([shelfFront, 2, depth], 'polished_blackstone_bricks')
      put([shelfFront, 3, depth], `candle[candles=${nicheIndex % 2 === 0 ? 3 : 2},lit=true,waterlogged=false]`)
      put([horizontal, 5, depth], `polished_blackstone_brick_stairs[facing=${horizontal === 2 ? 'west' : 'east'},half=top,shape=straight,waterlogged=false]`)
      for (const offset of [-1, 1]) {
        fill([horizontal, 2, depth + offset], [horizontal, 4, depth + offset], 'deepslate_bricks')
      }
    }
  }
  for (let depth = 2; depth <= 37; depth++) {
    if (arches.includes(depth) || [3, 4, 5, 9, 10, 11, 15, 16, 17, 21, 22, 23, 27, 28, 29, 33, 34, 35].includes(depth)) continue
    for (const horizontal of [2, 12]) {
      const face = horizontal === 2 ? 'west' : 'east'
      const lowest = 2 + noise(horizontal, 5, depth) % 4
      for (let height = lowest; height <= 8; height++) put([horizontal, height, depth], `vine[${face}=true,north=false,south=false,up=false,${face === 'west' ? 'east' : 'west'}=false]`)
      put([horizontal, 9, depth], 'mangrove_roots[waterlogged=false]')
      if (depth % 2 === 0) put([horizontal === 2 ? 3 : 11, 8, depth], 'oak_leaves[distance=1,persistent=true,waterlogged=false]')
    }
  }
  for (let depth = 2; depth < 38; depth++) {
    for (const horizontal of [3, 11]) {
      if (noise(horizontal, 1, depth) < 36 && ![14, 15, 26, 27].includes(depth)) put([horizontal, 1, depth], 'moss_carpet')
    }
  }
  for (const depth of [6, 18, 30, 36]) {
    for (const horizontal of [3, 11]) {
      put([horizontal, 9, depth], 'polished_blackstone_bricks')
      for (let height = 6; height <= 8; height++) put([horizontal, height, depth], 'chain[axis=y,waterlogged=false]')
    }
  }
  return { structure, metadata: { name: 'room', id: 'corridor-M15', title: 'M15 苔蚀黑石断拱烛廊', size, straightAxis: 'z', reference: 'M15-blackstone-deepslate-overgrown.png' } }
}
