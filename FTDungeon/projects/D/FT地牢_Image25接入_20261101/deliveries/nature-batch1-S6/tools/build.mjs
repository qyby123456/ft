export const rooms = [
  ['large-chamber', '熔核大殿', [37, 12, 25]],
  ['medium-room', '玄武岩议事室', [15, 10, 15]],
  ['small-room', '余烬密室', [9, 8, 9]],
  ['great-hall', '黑曜石柱廊大厅', [29, 12, 15]],
  ['corridor', '熔脉直廊', [9, 9, 21]],
  ['corner', '玄武岩折廊', [15, 9, 15]],
  ['entrance', '熔岩门庭', [13, 12, 19]],
  ['courtyard', '火山天井', [15, 12, 15]],
  ['treasure', '烬金祭坛宝库', [15, 10, 17]],
]

export default function build(api) {
  const [kind, title, size] = rooms[Number(process.env.S6_ROOM_INDEX ?? 0)]
  const [width, height, depth] = size
  const centerX = Math.floor(width / 2)
  const centerZ = Math.floor(depth / 2)
  const structure = api.createStructure(size)
  const set = (blockX, blockY, blockZ, name, properties = {}, data) => api.setBlock(structure, [blockX, blockY, blockZ], `minecraft:${name}`, properties, data)
  const fill = (minimum, maximum, name) => api.fill(structure, minimum, maximum, `minecraft:${name === 'basalt' ? 'basalt[axis=y]' : name}`)
  fill([0, 0, 0], [width - 1, height - 1, depth - 1], 'air')
  fill([0, 0, 0], [width - 1, 0, depth - 1], 'basalt')
  for (let blockX = 0; blockX < width; blockX++) {
    for (let blockZ = 0; blockZ < depth; blockZ++) {
      set(blockX, 1, blockZ, (blockX + blockZ) % 7 === 0 ? 'polished_blackstone_bricks' : 'polished_blackstone')
    }
  }
  fill([0, 2, 0], [0, height - 1, depth - 1], 'blackstone')
  fill([width - 1, 2, 0], [width - 1, height - 1, depth - 1], 'blackstone')
  fill([0, 2, 0], [width - 1, height - 1, 0], 'nether_bricks')
  fill([0, 2, depth - 1], [width - 1, height - 1, depth - 1], 'nether_bricks')
  fill([0, 3, 0], [width - 1, 3, 0], 'red_nether_bricks')
  fill([0, 3, depth - 1], [width - 1, 3, depth - 1], 'red_nether_bricks')
  fill([0, height - 1, 0], [width - 1, height - 1, depth - 1], 'polished_blackstone_bricks')
  for (let beamZ = 3; beamZ < depth - 2; beamZ += 5) fill([1, height - 2, beamZ], [width - 2, height - 2, beamZ], 'smooth_basalt')
  const pillar = (blockX, blockZ) => {
    set(blockX, 2, blockZ, 'polished_blackstone_bricks')
    fill([blockX, 3, blockZ], [blockX, height - 3, blockZ], 'obsidian')
    set(blockX, height - 2, blockZ, 'gilded_blackstone')
    set(blockX, 4, blockZ, 'shroomlight')
  }
  for (const blockX of [1, width - 2]) {
    for (let blockZ = 2; blockZ < depth - 1; blockZ += 4) pillar(blockX, blockZ)
  }
  if (kind !== 'corner') {
    for (const blockX of width >= 15 ? [3, width - 4] : [2, width - 3]) {
      for (let blockZ = 3; blockZ <= depth - 4; blockZ++) {
        set(blockX, 1, blockZ, 'lava', {level: '0'})
        set(blockX, 2, blockZ, 'orange_stained_glass')
      }
    }
  }
  const monument = (blockZ, tall = false) => {
    fill([centerX - 2, 2, blockZ - 2], [centerX + 2, 2, blockZ + 2], 'polished_blackstone_bricks')
    fill([centerX - 1, 3, blockZ - 1], [centerX + 1, 3, blockZ + 1], 'obsidian')
    set(centerX, 3, blockZ, 'lava', {level: '0'})
    set(centerX, 4, blockZ, 'orange_stained_glass')
    for (const offsetX of [-2, 2]) {
      fill([centerX + offsetX, 3, blockZ], [centerX + offsetX, tall ? 6 : 4, blockZ], 'obsidian')
      set(centerX + offsetX, tall ? 7 : 5, blockZ, 'shroomlight')
    }
  }
  if (['large-chamber', 'courtyard'].includes(kind)) monument(centerZ, true)
  if (kind === 'medium-room') {
    monument(centerZ + 1)
    fill([width - 3, 2, 4], [width - 2, 3, 5], 'bookshelf')
  }
  if (kind === 'small-room') {
    set(centerX, 2, depth - 3, 'chest', {facing: 'north', type: 'single', waterlogged: 'false'}, api.createBlockNbt({id: 'minecraft:chest'}))
    set(centerX, 3, depth - 2, 'gilded_blackstone')
  }
  if (kind === 'great-hall') {
    for (const blockX of [6, width - 7]) {
      for (let blockZ = 4; blockZ < depth - 3; blockZ += 4) pillar(blockX, blockZ)
    }
    fill([centerX - 2, 2, depth - 4], [centerX + 2, 2, depth - 3], 'gilded_blackstone')
    fill([centerX - 1, 3, depth - 3], [centerX + 1, 4, depth - 3], 'obsidian')
  }
  if (kind === 'entrance') {
    for (const blockX of [centerX - 3, centerX + 3]) {
      fill([blockX, 2, 2], [blockX, height - 3, 2], 'smooth_basalt')
      set(blockX, 4, 2, 'shroomlight')
    }
    fill([centerX - 3, height - 3, 2], [centerX + 3, height - 3, 2], 'gilded_blackstone')
  }
  if (kind === 'treasure') {
    fill([centerX - 3, 2, depth - 5], [centerX + 3, 2, depth - 3], 'gilded_blackstone')
    fill([centerX - 1, 3, depth - 4], [centerX + 1, 3, depth - 3], 'obsidian')
    set(centerX, 4, depth - 4, 'chest', {facing: 'north', type: 'single', waterlogged: 'false'}, api.createBlockNbt({id: 'minecraft:chest'}))
    set(centerX - 2, 3, depth - 4, 'shroomlight')
    set(centerX + 2, 3, depth - 4, 'shroomlight')
  }
  if (kind === 'courtyard') {
    fill([centerX - 3, height - 2, centerZ - 3], [centerX + 3, height - 1, centerZ + 3], 'air')
    for (const blockX of [5, width - 6]) {
      fill([blockX, 2, 4], [blockX, height - 2, 4], 'smooth_basalt')
      fill([blockX, 2, depth - 5], [blockX, height - 2, depth - 5], 'smooth_basalt')
    }
  }
  if (kind === 'corner') {
    for (let blockX = 1; blockX < width - 1; blockX++) {
      for (let blockZ = 1; blockZ < depth - 1; blockZ++) {
        const path = (Math.abs(blockX - centerX) <= 2 && blockZ <= centerZ + 2) || (blockX >= centerX - 2 && Math.abs(blockZ - centerZ) <= 2)
        if (!path) fill([blockX, 2, blockZ], [blockX, height - 2, blockZ], 'blackstone')
      }
    }
    set(centerX - 3, 4, centerZ, 'shroomlight')
    set(centerX, 4, centerZ + 3, 'shroomlight')
  }
  if (kind === 'large-chamber') {
    fill([centerX - 1, 2, 5], [centerX + 1, height - 3, depth - 5], 'obsidian')
    for (let blockZ = 6; blockZ < depth - 5; blockZ += 3) {
      set(centerX - 1, 4, blockZ, 'shroomlight')
      set(centerX + 1, 4, blockZ, 'shroomlight')
    }
    fill([centerX, 5, 5], [centerX, height - 3, 5], 'gilded_blackstone')
  }
  if (kind === 'medium-room') {
    fill([4, 2, 5], [width - 2, 5, 5], 'polished_blackstone_bricks')
    set(4, 4, 5, 'shroomlight')
  }
  if (kind === 'great-hall') {
    for (const [wallZ, westGap] of [[4, true], [7, false], [10, true]]) {
      const startX = westGap ? 8 : 1
      const endX = westGap ? width - 2 : width - 9
      fill([startX, 2, wallZ], [endX, 5, wallZ], 'polished_blackstone_bricks')
      set(westGap ? startX : endX, 4, wallZ, 'shroomlight')
    }
  }
  if (kind === 'corridor') {
    for (let blockZ = 5; blockZ <= 15; blockZ++) {
      const floorY = Math.min(4, blockZ - 3, 17 - blockZ)
      fill([1, 2, blockZ], [width - 2, floorY, blockZ], 'polished_blackstone_bricks')
      if ([5, 6, 14, 15].includes(blockZ)) {
        for (let blockX = 1; blockX < width - 1; blockX++) {
          set(blockX, floorY, blockZ, 'polished_blackstone_brick_stairs', {facing: blockZ < 10 ? 'south' : 'north', half: 'bottom', shape: 'straight', waterlogged: 'false'})
        }
      }
    }
  }
  if (kind === 'courtyard') {
    for (const blockX of [4, width - 5]) {
      for (let blockZ = 3; blockZ <= depth - 4; blockZ++) {
        const floorY = Math.min(4, blockZ - 1, depth - blockZ - 2)
        fill([blockX, 2, blockZ], [blockX, floorY, blockZ], 'polished_blackstone_bricks')
        if (floorY < 4) set(blockX, floorY, blockZ, 'polished_blackstone_brick_stairs', {facing: blockZ < centerZ ? 'south' : 'north', half: 'bottom', shape: 'straight', waterlogged: 'false'})
      }
    }
  }
  if (kind === 'treasure') {
    fill([4, 2, 5], [width - 5, 4, 5], 'obsidian')
    set(4, 4, 5, 'shroomlight')
    set(width - 5, 4, 5, 'shroomlight')
  }
  if (kind === 'entrance' || kind === 'small-room') {
    set(2, 2, depth - 3, 'redstone_lamp', {lit: 'false'})
    set(2, 3, depth - 3, 'lever', {face: 'floor', facing: 'north', powered: 'false'})
  }
  fill([centerX - 1, 2, 0], [centerX + 1, 4, 0], 'air')
  if (['corridor', 'great-hall', 'entrance', 'courtyard'].includes(kind)) fill([centerX - 1, 2, depth - 1], [centerX + 1, 4, depth - 1], 'air')
  if (['large-chamber', 'medium-room', 'corner'].includes(kind)) fill([width - 1, 2, centerZ - 1], [width - 1, 4, centerZ + 1], 'air')
  if (kind === 'large-chamber') fill([0, 2, centerZ - 1], [0, 4, centerZ + 1], 'air')
  return {structure, metadata: {name: 'room', title, kind, group: 'nature-batch1-S6'}}
}
