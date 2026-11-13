export default function build(api) {
  const structure = api.createStructure([19, 12, 19])
  const put = (blockX, blockY, blockZ, name, properties = {}) => api.setBlock(structure, [blockX, blockY, blockZ], `minecraft:${name}`, properties)
  const fill = (minimum, maximum, name) => api.fill(structure, minimum, maximum, `minecraft:${name}`)
  fill([0, 0, 0], [18, 11, 18], 'air')
  fill([0, 0, 0], [18, 0, 18], 'basalt[axis=y]')
  for (let blockX = 0; blockX < 19; blockX++) {
    for (let blockZ = 0; blockZ < 19; blockZ++) {
      put(blockX, 1, blockZ, (blockX * 7 + blockZ * 11) % 9 < 2 ? 'blackstone' : 'polished_blackstone_bricks')
    }
  }
  for (let blockY = 2; blockY < 12; blockY++) {
    for (let horizontal = 0; horizontal < 19; horizontal++) {
      for (const [blockX, blockZ] of [[horizontal, 0], [horizontal, 18], [0, horizontal], [18, horizontal]]) {
        put(blockX, blockY, blockZ, (horizontal + blockY * 3) % 8 === 0 ? 'cracked_polished_blackstone_bricks' : 'polished_blackstone_bricks')
      }
    }
  }
  fill([0, 11, 0], [18, 11, 18], 'blackstone')
  for (const beam of [3, 9, 15]) {
    fill([1, 10, beam], [17, 10, beam], 'polished_blackstone_bricks')
    fill([beam, 10, 1], [beam, 10, 17], 'polished_blackstone_bricks')
  }
  for (let blockX = 2; blockX <= 16; blockX++) {
    for (let blockZ = 2; blockZ <= 16; blockZ++) {
      const edge = blockX === 2 || blockX === 16 || blockZ === 2 || blockZ === 16
      const bridge = (Math.abs(blockX - 9) <= 1 && blockZ === 2) || (blockZ === 16 && (Math.abs(blockX - 5) <= 1 || Math.abs(blockX - 13) <= 1))
      if (edge && !bridge) {
        put(blockX, 1, blockZ, 'lava', {level: '0'})
        put(blockX, 2, blockZ, 'orange_stained_glass')
      }
    }
  }
  for (const blockX of [1, 16]) {
    for (const blockZ of [1, 16]) {
      fill([blockX, 2, blockZ], [blockX + 1, 10, blockZ + 1], 'basalt[axis=y]')
      fill([blockX, 6, blockZ], [blockX + 1, 6, blockZ + 1], 'glowstone')
      fill([blockX, 9, blockZ], [blockX + 1, 9, blockZ + 1], 'polished_blackstone_bricks')
    }
  }
  for (const [blockX, blockZ] of [[2, 12], [16, 12]]) {
    fill([blockX - 1, 1, blockZ - 1], [blockX + 1, 9, blockZ + 1], 'blackstone')
    fill([blockX, 2, blockZ], [blockX, 8, blockZ], 'lava[level=0]')
    fill([blockX, 2, blockZ - 1], [blockX, 8, blockZ - 1], 'orange_stained_glass')
    put(blockX, 9, blockZ, 'obsidian')
  }
  for (let blockX = 6; blockX <= 12; blockX++) {
    for (let blockZ = 6; blockZ <= 12; blockZ++) {
      if ((blockX === 6 || blockX === 12) && (blockZ === 6 || blockZ === 12)) continue
      put(blockX, 2, blockZ, 'polished_blackstone_bricks')
    }
  }
  fill([7, 3, 7], [11, 3, 11], 'polished_blackstone_bricks')
  for (let blockX = 8; blockX <= 10; blockX++) {
    put(blockX, 2, 6, 'polished_blackstone_brick_stairs', {facing: 'south', half: 'bottom', shape: 'straight', waterlogged: 'false'})
    put(blockX, 3, 7, 'polished_blackstone_brick_stairs', {facing: 'south', half: 'bottom', shape: 'straight', waterlogged: 'false'})
  }
  fill([8, 4, 8], [10, 7, 10], 'obsidian')
  fill([9, 4, 9], [9, 6, 9], 'lava[level=0]')
  fill([9, 4, 8], [9, 6, 8], 'orange_stained_glass')
  fill([7, 4, 10], [7, 5, 10], 'obsidian')
  fill([11, 4, 9], [11, 6, 9], 'obsidian')
  fill([9, 4, 11], [9, 8, 11], 'obsidian')
  for (const [blockX, blockZ] of [[5, 5], [13, 5], [5, 13], [13, 13]]) {
    fill([blockX, 8, blockZ], [blockX, 10, blockZ], 'chain[axis=y,waterlogged=false]')
    put(blockX, 7, blockZ, 'lantern', {hanging: 'true', waterlogged: 'false'})
  }
  for (const centerX of [5, 13]) {
    fill([centerX - 2, 2, 18], [centerX + 2, 6, 18], 'blackstone')
    fill([centerX - 1, 2, 18], [centerX + 1, 4, 18], 'air')
    fill([centerX, 5, 18], [centerX, 5, 18], 'air')
    put(centerX - 2, 5, 18, 'polished_blackstone_brick_stairs', {facing: 'east', half: 'top', shape: 'straight', waterlogged: 'false'})
    put(centerX + 2, 5, 18, 'polished_blackstone_brick_stairs', {facing: 'west', half: 'top', shape: 'straight', waterlogged: 'false'})
    put(centerX, 6, 18, 'gilded_blackstone')
  }
  fill([8, 2, 0], [10, 4, 0], 'air')
  return {structure, metadata: {name: 'room', reference: 'S6-01-large-chamber.png', revision: 'S6-01-R3-image-first-single'}}
}
