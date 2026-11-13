export default function furnish({ api, room, put, fill, middleX, middleZ }) {
  const [width, height, depth] = room.size
  const mushroom = (baseX, baseZ, stemHeight, radius, cap = 'red_terracotta', baseY = 1) => {
    const capY = baseY + stemHeight
    fill([baseX, baseY, baseZ], [baseX, capY, baseZ], 'mushroom_stem')
    for (let offsetX = -radius; offsetX <= radius; offsetX++) {
      for (let offsetZ = -radius; offsetZ <= radius; offsetZ++) {
        if (radius > 1 && Math.abs(offsetX) === radius && Math.abs(offsetZ) === radius) continue
        put(baseX + offsetX, capY, baseZ + offsetZ, cap === 'red_terracotta' && (offsetX * 3 + offsetZ * 5) % 4 === 1 ? 'white_terracotta' : cap)
        if (Math.abs(offsetX) === radius || Math.abs(offsetZ) === radius) put(baseX + offsetX, capY - 1, baseZ + offsetZ, cap)
      }
    }
    put(baseX, capY - 1, baseZ, 'shroomlight')
    if (capY + 1 < height - 1) put(baseX, capY + 1, baseZ, cap)
  }
  const crystal = (blockX, blockZ) => {
    put(blockX, 0, blockZ, 'amethyst_block')
    put(blockX, 1, blockZ, 'amethyst_cluster', { facing: 'up', waterlogged: 'false' })
  }
  const chest = (blockX, blockY, blockZ, facing = 'north') => {
    put(blockX, blockY, blockZ, 'chest', { facing, type: 'single', waterlogged: 'false' }, api.createBlockNbt({ id: 'minecraft:chest' }))
  }
  if (room.kind === 'large-chamber') {
    fill([middleX - 2, 1, middleZ - 2], [middleX + 2, 1, middleZ + 2], 'polished_deepslate')
    fill([middleX - 1, 2, middleZ - 1], [middleX + 1, 2, middleZ + 1], 'mycelium')
    mushroom(middleX, middleZ, 4, 3, 'red_terracotta', 3)
    for (const [blockX, blockZ] of [[width - 5, 4], [4, depth - 5]]) mushroom(blockX, blockZ, 6, 2)
  } else if (room.kind === 'medium-room') {
    fill([middleX - 1, 1, middleZ - 1], [middleX + 1, 1, middleZ + 1], 'polished_deepslate')
    put(middleX, 2, middleZ, 'amethyst_cluster', { facing: 'up', waterlogged: 'false' })
    mushroom(3, 4, 5, 2)
    mushroom(width - 4, depth - 5, 4, 2, 'brown_mushroom_block')
    chest(2, 1, depth - 3)
    chest(width - 3, 1, 3, 'west')
  } else if (room.kind === 'small-room') {
    mushroom(2, depth - 3, 4, 1)
    mushroom(width - 3, 2, 4, 1, 'brown_mushroom_block')
    chest(2, 1, depth - 2)
    crystal(width - 3, depth - 3)
  } else if (room.kind === 'great-hall') {
    for (const blockX of [5, width - 6]) {
      for (const blockZ of [4, depth - 5]) {
        fill([blockX - 1, 1, blockZ - 1], [blockX + 1, 1, blockZ + 1], 'polished_deepslate')
        fill([blockX, 2, blockZ], [blockX, height - 2, blockZ], 'mushroom_stem')
        put(blockX, 3, blockZ, 'shroomlight')
        fill([blockX - 2, height - 3, blockZ - 1], [blockX + 2, height - 2, blockZ + 1], 'red_mushroom_block')
      }
    }
    for (const blockZ of [4, depth - 5]) fill([5, height - 2, blockZ], [width - 6, height - 2, blockZ], 'dark_oak_log', { axis: 'x' })
  } else if (room.kind === 'corridor') {
    for (const blockZ of [3, 17, 21]) {
      mushroom(1, blockZ, 4, 1)
      mushroom(width - 2, blockZ + 2, 4, 1, 'brown_mushroom_block')
      fill([1, height - 2, blockZ], [width - 2, height - 2, blockZ], 'dark_oak_log', { axis: 'x' })
    }
  } else if (room.kind === 'corner') {
    mushroom(middleX + 2, middleZ + 2, 4, 1)
    put(middleX - 2, 1, middleZ + 2, 'polished_deepslate')
    put(middleX - 2, 2, middleZ + 2, 'shroomlight')
    put(middleX + 2, 1, 3, 'polished_deepslate')
    put(middleX + 2, 2, 3, 'shroomlight')
  } else if (room.kind === 'entrance') {
    mushroom(4, 4, 5, 2)
    mushroom(width - 5, 4, 5, 2)
    for (const blockX of [3, width - 4]) {
      fill([blockX - 1, 1, depth - 5], [blockX + 1, 2, depth - 4], 'polished_deepslate')
      put(blockX, 3, depth - 4, 'chiseled_stone_bricks')
      put(blockX, 4, depth - 4, 'shroomlight')
      chest(blockX, 1, depth - 7)
    }
  } else if (room.kind === 'courtyard') {
    for (let blockX = middleX - 3; blockX <= middleX + 3; blockX++) {
      for (let blockZ = middleZ - 3; blockZ <= middleZ + 3; blockZ++) {
        if (Math.abs(blockX - middleX) + Math.abs(blockZ - middleZ) <= 4) put(blockX, 1, blockZ, 'polished_deepslate')
      }
    }
    mushroom(middleX, middleZ, 4, 2, 'red_terracotta', 2)
    for (const [blockX, blockZ] of [[3, 4], [width - 4, 4], [3, depth - 5], [width - 4, depth - 5]]) mushroom(blockX, blockZ, 6, 2, 'brown_mushroom_block')
  } else if (room.kind === 'treasure') {
    for (let step = 0; step < 3; step++) fill([middleX - 2, 1 + step, depth - 7 + step], [middleX + 2, 1 + step, depth - 4], 'polished_deepslate')
    chest(middleX, 4, depth - 4)
    for (const blockX of [middleX - 4, middleX + 4]) {
      fill([blockX, 1, depth - 4], [blockX, height - 2, depth - 4], 'chiseled_stone_bricks')
      put(blockX, 4, depth - 4, 'shroomlight')
      crystal(blockX, depth - 6)
    }
    mushroom(4, 4, 6, 2)
    mushroom(width - 5, 4, 6, 2)
    chest(2, 1, depth - 4, 'east')
    chest(width - 3, 1, depth - 4, 'west')
  }
}
