export default function build(api) {
  const structure = api.createStructure([13, 12, 16])
  const put = (position, block, properties) => api.setBlock(structure, position, `minecraft:${block}`, properties)
  const fill = (from, to, block, properties) => api.fill(structure, from, to, `minecraft:${block}`, properties)
  const bars = { north: 'false', east: 'false', south: 'false', west: 'false', waterlogged: 'false' }
  const stairs = (facing) => ({ facing, half: 'top', shape: 'straight', waterlogged: 'false' })
  const hatch = (half, facing) => ({ facing, half, open: 'false', powered: 'false', waterlogged: 'false' })

  fill([0, 0, 0], [12, 11, 15], 'air')
  fill([0, 0, 0], [12, 2, 15], 'polished_blackstone_bricks')
  for (let depth = 0; depth < 16; depth++) {
    for (let horizontal = 0; horizontal < 13; horizontal++) {
      const floor = (horizontal + depth * 3) % 11 === 0 ? 'cracked_polished_blackstone_bricks'
        : (horizontal * 5 + depth) % 13 === 0 ? 'nether_bricks' : 'polished_blackstone_bricks'
      put([horizontal, 2, depth], floor)
      put([horizontal, 11, depth], (horizontal + depth * 3) % 7 < 2 ? 'cracked_polished_blackstone_bricks' : 'red_nether_bricks')
    }
    for (const horizontal of [3, 9]) put([horizontal, 2, depth], depth % 5 === 0 ? 'chiseled_polished_blackstone' : 'nether_bricks')
    for (const horizontal of [2, 10]) {
      if (depth > 0 && depth < 15 && depth % 5 !== 0) {
        put([horizontal, 1, depth], 'lava', { level: '0' })
        put([horizontal, 2, depth], 'orange_stained_glass')
        for (const neighbor of [horizontal - 1, horizontal + 1]) put([neighbor, 1, depth], 'obsidian')
        put([horizontal, 0, depth], 'obsidian')
      }
    }
    for (const horizontal of [0, 1, 11, 12]) {
      for (let height = 3; height <= 10; height++) {
        const soot = (depth * 5 + height * 7 + (horizontal < 6 ? 1 : 4)) % 17
        const band = height === 3 || height === 7
        const block = band ? 'polished_blackstone_bricks'
          : soot < 3 ? 'cracked_polished_blackstone_bricks' : soot < 5 ? 'nether_bricks' : 'red_nether_bricks'
        put([horizontal, height, depth], block)
      }
    }
  }

  for (const depth of [0, 5, 10, 15]) {
    fill([1, 10, depth], [11, 10, depth], 'nether_bricks')
    for (const horizontal of [2, 10]) {
      fill([horizontal, 3, depth], [horizontal, 9, depth], 'polished_blackstone_bricks')
      for (const height of [3, 7, 9]) put([horizontal, height, depth], 'chiseled_polished_blackstone')
    }
    fill([3, 9, depth], [9, 9, depth], 'red_nether_bricks')
    put([3, 8, depth], 'polished_blackstone_brick_stairs', stairs('east'))
    put([9, 8, depth], 'polished_blackstone_brick_stairs', stairs('west'))
  }

  for (const depth of [0, 15]) {
    fill([3, 3, depth], [3, 8, depth], 'polished_blackstone_bricks')
    fill([9, 3, depth], [9, 8, depth], 'polished_blackstone_bricks')
    for (const horizontal of [3, 9]) {
      for (const height of [4, 7]) put([horizontal, height, depth], 'chiseled_polished_blackstone')
    }
    fill([4, 8, depth], [8, 8, depth], 'polished_blackstone_bricks')
    put([4, 7, depth], 'polished_blackstone_brick_stairs', stairs('east'))
    put([8, 7, depth], 'polished_blackstone_brick_stairs', stairs('west'))
    put([6, 8, depth], 'chiseled_polished_blackstone')
  }

  for (const depth of [2, 7, 12]) {
    for (const horizontal of [2, 10]) {
      put([horizontal, 5, depth], 'iron_bars', bars)
      put([horizontal, 6, depth], 'wall_torch', { facing: horizontal === 2 ? 'east' : 'west' })
    }
  }

  for (const startX of [3, 8]) {
    for (const startZ of [2, 11]) {
      for (let offsetX = 0; offsetX < 2; offsetX++) {
        for (let offsetZ = 0; offsetZ < 2; offsetZ++) {
          const horizontal = startX + offsetX
          const depth = startZ + offsetZ
          put([horizontal, 6, depth], 'iron_trapdoor', hatch('bottom', startX === 3 ? 'east' : 'west'))
          put([horizontal, 7, depth], 'iron_bars', bars)
          put([horizontal, 8, depth], 'iron_bars', bars)
          put([horizontal, 9, depth], 'iron_trapdoor', hatch('top', startX === 3 ? 'east' : 'west'))
        }
      }
      put([startX, 10, startZ], 'chain', { axis: 'y', waterlogged: 'false' })
      put([startX + 1, 10, startZ + 1], 'chain', { axis: 'y', waterlogged: 'false' })
    }
  }

  return { structure, metadata: { name: 'room', id: 'S11', title: '烬狱吊笼短廊', referenceSha256: '2bdda4f75bf87f19ae80540a5de09af4b720dde05e515cdcd2631fba795d65a6' } }
}
