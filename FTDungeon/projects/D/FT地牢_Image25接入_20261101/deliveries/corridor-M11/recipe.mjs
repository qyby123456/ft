export default function build(api) {
  const size = [15, 12, 40]
  const structure = api.createStructure(size)
  const place = (position, block, properties = {}, nbt) => api.setBlock(structure, position, `minecraft:${block}`, properties, nbt)
  const fill = (start, end, block, properties = {}) => api.fill(structure, start, end, `minecraft:${block}`, properties)
  const noise = (posX, posY, posZ) => Math.abs((posX * 73856093 ^ posY * 19349663 ^ posZ * 83492791) >>> 0) % 100
  const ribs = [0, 7, 14, 21, 28, 35, 39]
  const bays = [3, 10, 17, 24, 31, 37]
  const masonry = (posX, posY, posZ) => {
    const value = noise(posX, posY, posZ)
    return value < 27 ? 'mossy_stone_bricks' : value < 42 ? 'cracked_stone_bricks' : value < 48 ? 'cobblestone' : 'stone_bricks'
  }

  fill([0, 0, 0], [14, 11, 39], 'air')
  for (let posX = 0; posX < size[0]; posX += 1) {
    for (let posZ = 0; posZ < size[2]; posZ += 1) {
      place([posX, 0, posZ], 'polished_deepslate')
      place([posX, 1, posZ], masonry(posX, 1, posZ))
      place([posX, 10, posZ], masonry(posX, 10, posZ))
      place([posX, 11, posZ], 'stone_bricks')
      if (posX <= 4 || posX >= 10) {
        place([posX, 2, posZ], masonry(posX, 2, posZ))
      }
      if (posX <= 1 || posX >= 13) {
        for (let posY = 3; posY <= 9; posY += 1) {
          place([posX, posY, posZ], masonry(posX, posY, posZ))
        }
      }
      if (posX === 5 || posX === 9) {
        place([posX, 1, posZ], posZ % 7 === 0 ? 'chiseled_stone_bricks' : 'polished_andesite')
      }
    }
  }

  for (const side of ['west', 'east']) {
    const left = side === 'west'
    const wall = left ? 2 : 12
    const pier = left ? 3 : 11
    const curb = left ? 4 : 10
    const facing = left ? 'east' : 'west'
    for (let posZ = 0; posZ < 40; posZ += 1) {
      place([wall, 3, posZ], masonry(wall, 3, posZ))
      place([wall, 8, posZ], 'smooth_sandstone')
      place([wall, 9, posZ], masonry(wall, 9, posZ))
    }
    for (const center of bays) {
      const halfWidth = center === 37 ? 1 : 2
      fill([wall, 4, center - halfWidth], [wall, 7, center + halfWidth], 'air')
      for (let offset = -1; offset <= 1; offset += 1) {
        place([wall, 3, center + offset], 'chiseled_stone_bricks')
        fill([wall, 4, center + offset], [wall, 5, center + offset], 'stripped_dark_oak_log', { axis: 'y' })
        place([wall, 6, center + offset], 'smooth_sandstone')
      }
      place([pier, 4, center], 'dark_oak_trapdoor', { facing, half: 'bottom', open: 'true', powered: 'false', waterlogged: 'false' })
      place([pier, 5, center], 'dark_oak_trapdoor', { facing, half: 'top', open: 'true', powered: 'false', waterlogged: 'false' })
      place([wall, 7, center], 'skeleton_skull', { rotation: left ? '12' : '4' }, api.createBlockNbt({ id: 'minecraft:skull' }))
      for (const offset of [-1, 1]) {
        place([curb, 3, center + offset], 'candle', { candles: offset < 0 ? '2' : '3', lit: 'true', waterlogged: 'false' })
      }
      for (let offset = -1; offset <= 1; offset += 1) {
        place([pier, 1, center + offset], 'lava', { level: '0' })
        place([pier, 2, center + offset], 'orange_stained_glass')
      }
    }
    for (const posZ of ribs) {
      place([pier, 3, posZ], 'chiseled_stone_bricks')
      place([pier, 4, posZ], 'skeleton_skull', { rotation: left ? '12' : '4' }, api.createBlockNbt({ id: 'minecraft:skull' }))
      fill([wall, 4, posZ], [wall, 7, posZ], 'bone_block', { axis: 'y' })
      fill([pier, 5, posZ], [pier, 6, posZ], 'bone_block', { axis: 'y' })
      place([pier, 7, posZ], 'smooth_sandstone')
      place([curb, 8, posZ], 'bone_block', { axis: 'x' })
      place([left ? 5 : 9, 8, posZ], 'smooth_sandstone_stairs', { facing, half: 'top', shape: 'straight', waterlogged: 'false' })
      place([left ? 5 : 9, 9, posZ], 'bone_block', { axis: 'x' })
    }
    for (const center of bays.slice(0, 5)) {
      for (const offset of [-2, 2]) {
        const length = (center + offset + (left ? 1 : 0)) % 3 + 1
        for (let segment = 0; segment < length; segment += 1) {
          place([pier, 9 - segment, center + offset], segment === length - 1 ? 'cave_vines' : 'cave_vines_plant', segment === length - 1 ? { age: '25', berries: 'false' } : { berries: 'false' })
        }
        place([wall, 3, center + offset], 'moss_block')
      }
    }
  }

  for (const posZ of ribs) {
    fill([6, 9, posZ], [8, 9, posZ], 'bone_block', { axis: 'x' })
    place([7, 9, posZ], 'chiseled_sandstone')
    fill([6, 10, posZ], [8, 10, posZ], 'smooth_sandstone')
  }
  for (const posZ of [14, 28]) {
    const brokenSide = posZ === 14 ? 4 : 10
    place([brokenSide, 8, posZ], 'cracked_stone_bricks')
    place([brokenSide === 4 ? 5 : 9, 9, posZ], 'mossy_cobblestone')
  }
  for (const end of [0, 39]) {
    fill([2, 3, end], [4, 7, end], 'stone_bricks')
    fill([10, 3, end], [12, 7, end], 'stone_bricks')
    fill([4, 3, end], [4, 6, end], 'bone_block', { axis: 'y' })
    fill([10, 3, end], [10, 6, end], 'bone_block', { axis: 'y' })
    place([4, 7, end], 'chiseled_sandstone')
    place([10, 7, end], 'chiseled_sandstone')
    place([5, 7, end], 'smooth_sandstone_stairs', { facing: 'east', half: 'top', shape: 'straight', waterlogged: 'false' })
    place([9, 7, end], 'smooth_sandstone_stairs', { facing: 'west', half: 'top', shape: 'straight', waterlogged: 'false' })
  }

  fill([5, 2, 0], [9, 4, 39], 'air')
  return {
    structure,
    metadata: {
      name: 'room',
      id: 'corridor-M11',
      title: 'M11 蔓生白骨墓廊',
      referenceId: 'M11',
      size,
      entrance: [7, 2, 0],
      interior: [7, 2, 5],
      exit: [7, 2, 39],
      ribs,
      bays,
      mainLane: { minimum: [5, 2, 0], maximum: [9, 4, 39], width: 5 },
      features: ['sealed lava light slots', 'twelve coffin niches', 'bone rib vaults', 'two eroded arch trims', 'mature hanging cave vines', 'skulls and warm candle clusters'],
    },
  }
}
