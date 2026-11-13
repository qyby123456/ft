export default function specialize({ room, put, fill, middleX, middleZ }) {
  const [width, , depth] = room.size
  if (room.kind === 'large-chamber') {
    for (let blockX = 1; blockX < width - 1; blockX++) {
      for (let blockZ = 1; blockZ < depth - 1; blockZ++) {
        if (blockX <= 3 || blockX >= width - 4 || blockZ <= 3 || blockZ >= depth - 4) put(blockX, 3, blockZ, 'mushroom_stem')
      }
    }
    for (const blockX of [1, width - 2]) {
      for (const blockZ of [2, middleZ - 3, middleZ + 3, depth - 3]) {
        fill([blockX, 1, blockZ], [blockX, 2, blockZ], 'mushroom_stem')
      }
    }
    for (const blockZ of [1, depth - 2]) {
      for (const blockX of [2, middleX - 3, middleX + 3, width - 3]) fill([blockX, 1, blockZ], [blockX, 2, blockZ], 'mushroom_stem')
    }
    fill([3, 3, 3], [5, 3, 5], 'mushroom_stem')
    fill([6, 1, 3], [6, 2, 5], 'mushroom_stem')
    fill([7, 1, 3], [7, 1, 5], 'mushroom_stem')
    fill([width - 6, 3, depth - 6], [width - 4, 3, depth - 4], 'mushroom_stem')
    fill([width - 7, 1, depth - 6], [width - 7, 2, depth - 4], 'mushroom_stem')
    fill([width - 8, 1, depth - 6], [width - 8, 1, depth - 4], 'mushroom_stem')
  } else if (room.kind === 'medium-room') {
    fill([1, 1, 5], [width - 5, 3, 5], 'rooted_dirt')
    fill([4, 1, depth - 6], [width - 2, 3, depth - 6], 'rooted_dirt')
    put(width - 5, 2, 5, 'shroomlight')
    put(4, 2, depth - 6, 'shroomlight')
  } else if (room.kind === 'small-room') {
    for (const [blockX, blockZ, note, lateral] of [[middleX, 3, 8, -1], [middleX, 6, 15, 1]]) {
      put(blockX, 1, blockZ, 'stone_pressure_plate', { powered: 'false' })
      put(blockX + lateral, 1, blockZ, 'note_block', { powered: 'false', instrument: 'harp', note: String(note) })
      put(blockX + lateral, 2, blockZ, 'air')
      put(blockX + lateral, 0, blockZ, 'mushroom_stem')
    }
  } else if (room.kind === 'corridor') {
    fill([2, 1, 6], [width - 3, 1, 6], 'mushroom_stem')
    fill([2, 1, 7], [width - 3, 2, 10], 'mushroom_stem')
    fill([2, 1, 11], [width - 3, 1, 11], 'mushroom_stem')
  } else if (room.kind === 'courtyard') {
    for (const blockX of [middleX - 4, middleX + 4]) {
      fill([blockX, 1, middleZ - 4], [blockX, 1, middleZ + 4], 'mushroom_stem')
    }
    fill([middleX - 4, 1, middleZ - 4], [middleX + 4, 1, middleZ - 4], 'mushroom_stem')
    fill([middleX - 4, 1, middleZ + 4], [middleX + 4, 1, middleZ + 4], 'mushroom_stem')
  }
}
