import fs from 'node:fs'
import furnish from './furnish.mjs'
import specialize from './topology.mjs'

const rooms = JSON.parse(fs.readFileSync(new URL('./rooms.json', import.meta.url), 'utf8'))

export default function build(api) {
  const room = rooms.find(entry => entry.id === process.env.S7_ROOM_ID)
  if (!room) throw new Error('Set S7_ROOM_ID to a rooms.json ID')
  const [width, height, depth] = room.size
  const middleX = Math.floor(width / 2)
  const middleZ = Math.floor(depth / 2)
  const structure = api.createStructure(room.size)
  const occupied = new Map()
  const put = (blockX, blockY, blockZ, block, properties, nbt) => {
    let defaults = {}
    if (['mycelium', 'podzol'].includes(block)) defaults = { snowy: 'false' }
    if (['mushroom_stem', 'brown_mushroom_block', 'red_mushroom_block'].includes(block)) {
      defaults = Object.fromEntries(['up', 'down', 'east', 'west', 'north', 'south'].map(direction => [direction, 'true']))
    }
    api.setBlock(structure, [blockX, blockY, blockZ], `minecraft:${block}`, { ...defaults, ...properties }, nbt)
    occupied.set(`${blockX},${blockY},${blockZ}`, block)
  }
  const fill = (minimum, maximum, block, properties) => {
    for (let blockX = minimum[0]; blockX <= maximum[0]; blockX++) {
      for (let blockY = minimum[1]; blockY <= maximum[1]; blockY++) {
        for (let blockZ = minimum[2]; blockZ <= maximum[2]; blockZ++) put(blockX, blockY, blockZ, block, properties)
      }
    }
  }
  const isInside = (blockX, blockZ) => room.kind !== 'corner'
    || (blockX >= middleX - 3 && blockX <= middleX + 3 && blockZ <= middleZ + 3)
    || (blockZ >= middleZ - 3 && blockZ <= middleZ + 3 && blockX >= middleX - 3)
  const mainPath = (blockX, blockZ) => room.kind === 'corner'
    ? (Math.abs(blockX - middleX) <= 1 && blockZ <= middleZ + 1)
      || (Math.abs(blockZ - middleZ) <= 1 && blockX >= middleX - 1)
    : Math.abs(blockX - middleX) <= 1 || Math.abs(blockZ - middleZ) <= 1
  const isWall = (blockX, blockZ) => isInside(blockX, blockZ)
    && (blockX === 0 || blockX === width - 1 || blockZ === 0 || blockZ === depth - 1
      || [[1, 0], [-1, 0], [0, 1], [0, -1]].some(([deltaX, deltaZ]) => !isInside(blockX + deltaX, blockZ + deltaZ)))
  for (let blockX = 0; blockX < width; blockX++) {
    for (let blockZ = 0; blockZ < depth; blockZ++) {
      for (let blockY = 0; blockY < height; blockY++) put(blockX, blockY, blockZ, 'air')
      if (!isInside(blockX, blockZ)) continue
      const variation = (blockX * 31 + blockZ * 17) % 13
      put(blockX, 0, blockZ, mainPath(blockX, blockZ)
        ? (variation < 3 ? 'cracked_stone_bricks' : 'stone_bricks')
        : (variation < 7 ? 'mycelium' : variation < 10 ? 'podzol' : 'rooted_dirt'))
      if (isWall(blockX, blockZ)) {
        for (let blockY = 1; blockY < height - 1; blockY++) {
          put(blockX, blockY, blockZ, blockY === 3 || blockY === height - 3
            ? 'polished_deepslate' : (blockX + blockZ + blockY) % 7 < 2 ? 'mossy_stone_bricks' : 'stone_bricks')
        }
      }
      if (room.kind !== 'courtyard' || blockX < 4 || blockX > width - 5 || blockZ < 4 || blockZ > depth - 5) {
        put(blockX, height - 1, blockZ, variation < 5 ? 'rooted_dirt' : 'deepslate_tiles')
      }
    }
  }
  furnish({ api, room, put, fill, middleX, middleZ })
  specialize({ room, put, fill, middleX, middleZ })
  for (let blockX = 1; blockX < width - 1; blockX++) {
    for (let blockZ = 1; blockZ < depth - 1; blockZ++) {
      if (isInside(blockX, blockZ) && !isWall(blockX, blockZ) && !mainPath(blockX, blockZ)
        && occupied.get(`${blockX},1,${blockZ}`) === 'air' && (blockX * 7 + blockZ * 3) % 17 === 0) {
        put(blockX, 0, blockZ, 'mycelium')
        put(blockX, 1, blockZ, (blockX + blockZ) % 2 ? 'red_mushroom' : 'brown_mushroom')
      }
    }
  }
  for (let blockZ = 2; blockZ < depth - 2; blockZ += 4) {
    if (room.kind === 'corner') continue
    for (const blockX of [0, width - 1]) {
      if (room.exits.includes(blockX === 0 ? 'west' : 'east') && Math.abs(blockZ - middleZ) <= 2) continue
      put(blockX, 4, blockZ, 'shroomlight')
      put(blockX === 0 ? 1 : width - 2, 3, blockZ, 'glow_lichen', { [blockX === 0 ? 'west' : 'east']: 'true', waterlogged: 'false' })
    }
  }
  const openPort = direction => {
    const alongX = direction === 'north' || direction === 'south'
    const plane = direction === 'north' || direction === 'west' ? 0 : alongX ? depth - 1 : width - 1
    const center = alongX ? middleX : middleZ
    for (let lateral = -1; lateral <= 1; lateral++) {
      for (let blockY = 1; blockY <= 3; blockY++) put(alongX ? center + lateral : plane, blockY, alongX ? plane : center + lateral, 'air')
      put(alongX ? center + lateral : plane, 0, alongX ? plane : center + lateral, 'stone_bricks')
    }
    for (let lateral = -2; lateral <= 2; lateral++) put(alongX ? center + lateral : plane, 4, alongX ? plane : center + lateral, 'polished_deepslate')
    for (const lateral of [-2, 2]) {
      for (let blockY = 1; blockY <= 3; blockY++) put(alongX ? center + lateral : plane, blockY, alongX ? plane : center + lateral, 'chiseled_stone_bricks')
      put(alongX ? center + lateral : plane, 3, alongX ? plane : center + lateral, 'shroomlight')
    }
  }
  for (const direction of ['north', ...room.exits]) openPort(direction)
  return { structure, metadata: { name: 'room' } }
}
