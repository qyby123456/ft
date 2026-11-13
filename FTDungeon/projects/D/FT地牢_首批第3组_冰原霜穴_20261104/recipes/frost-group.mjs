import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

export const rooms = [
  ['01', 'large-chamber', '冰晶主厅', [21, 12, 21], 'SE'],
  ['02', 'medium-room', '冻石休憩室', [17, 10, 17], 'S'],
  ['03', 'small-room', '霜封储藏室', [11, 8, 11], ''],
  ['04', 'great-hall', '双列冰柱大厅', [23, 12, 17], 'S'],
  ['05', 'corridor', '冰脊直廊', [9, 9, 25], 'S'],
  ['06', 'corner', '冰壁折角回廊', [17, 9, 17], 'E'],
  ['07', 'entrance', '霜门前厅', [17, 11, 15], 'S'],
  ['08', 'courtyard', '雪松冰瀑天井', [21, 14, 21], 'SE'],
  ['09', 'treasure', '霜晶宝库祭台', [17, 10, 17], ''],
]

export default function build(api) {
  const [code, type, name, , exits] = rooms.find(room => room[0] === process.env.FT_FROST_ROOM)
  const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
  const evidence = JSON.parse(fs.readFileSync(path.join(root, 'evidence/reference-evidence.json'), 'utf8')).entries.find(entry => entry.code === code)
  const size = evidence.size
  const [width, height, depth] = size
  const center = Math.floor(width / 2)
  const middle = Math.floor(depth / 2)
  const corner = type === 'corner'
  const exitDepth = corner ? depth - 4 : middle
  const structure = api.createStructure(size)
  const id = 'FTN3-' + code
  const footprint = (across, along) => across >= 0 && across < width && along >= 0 && along < depth &&
    (!corner || (across >= center - 3 && across <= center + 3) || (across >= center - 3 && along >= depth - 7))
  const boundary = (across, along) => footprint(across, along) &&
    [[1, 0], [-1, 0], [0, 1], [0, -1]].some(([deltaAcross, deltaAlong]) => !footprint(across + deltaAcross, along + deltaAlong))
  const put = (across, elevation, along, block, properties, nbt) => api.setBlock(structure, [across, elevation, along], 'minecraft:' + block, properties, nbt)
  const fill = (start, end, block, properties) => api.fill(structure, start, end, 'minecraft:' + block, properties)
  fill([0, 0, 0], [width - 1, height - 1, depth - 1], 'air')
  for (let across = 0; across < width; across++) {
    for (let along = 0; along < depth; along++) {
      if (!footprint(across, along)) continue
      const route = Math.abs(across - center) <= 1 || (exits.includes('E') && Math.abs(along - exitDepth) <= 1)
      put(across, 0, along, route ? 'polished_andesite' : (across + along) % 7 === 0 ? 'blue_ice' : (across * 3 + along) % 5 === 0 ? 'snow_block' : 'stone_bricks')
      const skylight = type === 'courtyard' && Math.abs(across - center) <= 3 && Math.abs(along - middle) <= 3
      if (!skylight) put(across, height - 1, along, 'stone_bricks')
      if (boundary(across, along)) for (let elevation = 1; elevation < height - 1; elevation++) {
        put(across, elevation, along, elevation >= height - 3 ? 'snow_block' : (across * 11 + along * 3) % 9 === 0 ? 'packed_ice' : (across + along + elevation) % 9 === 0 ? 'cracked_stone_bricks' : 'stone_bricks')
      }
    }
  }
  const post = (across, along) => {
    fill([across, 1, along], [across, height - 3, along], 'spruce_log', { axis: 'y' })
    put(across, 1, along, 'chiseled_stone_bricks')
    put(across, height - 3, along, 'chiseled_stone_bricks')
  }
  const lamp = (across, along, elevation = height - 5) => {
    fill([across, elevation + 1, along], [across, height - 2, along], 'chain', { axis: 'y', waterlogged: 'false' })
    put(across, elevation, along, 'soul_lantern', { hanging: 'true', waterlogged: 'false' })
  }
  const spike = (across, along, tall = 3) => {
    put(across, 1, along, 'snow_block')
    fill([across, 2, along], [across, tall + 1, along], 'packed_ice')
  }
  const chest = (across, elevation, along) => put(across, elevation, along, 'chest',
    { facing: 'north', type: 'single', waterlogged: 'false' }, api.createBlockNbt({ id: 'minecraft:chest', Items: [] }))
  const arch = (along) => {
    for (const across of [center - 2, center + 2]) fill([across, 1, along], [across, 5, along], 'chiseled_stone_bricks')
    fill([center - 2, 5, along], [center + 2, 5, along], 'polished_andesite')
    fill([center - 1, 1, along], [center + 1, 4, along], 'air')
  }
  if (!corner) {
    for (const along of type === 'corridor' ? [3, 9, 15, 21] : [3, depth - 4]) {
      const offset = width === 5 ? 0 : 1
      post(offset, along)
      post(width - 1 - offset, along)
      fill([offset, height - 3, along], [width - 1 - offset, height - 3, along], 'spruce_log', { axis: 'x' })
      for (const across of width === 5 ? [1, 3] : [2, width - 3]) lamp(across, along, Math.max(4, height - 5))
      for (const across of [3, width - 4]) if (Math.abs(across - center) > 1) fill([across, height - 5, along + 1], [across, height - 2, along + 1], 'packed_ice')
    }
    if (width >= 15) for (const across of [2, width - 3]) for (const along of [5, depth - 6]) spike(across, along, 2 + (across + along) % 2)
  }
  if (type === 'large-chamber') {
    fill([1, 1, middle - 4], [center + 2, 4, middle - 4], 'packed_ice')
    fill([center - 2, 1, middle + 4], [width - 2, 4, middle + 4], 'packed_ice')
    fill([center - 2, 1, middle - 2], [center + 2, 1, middle + 2], 'snow_block')
    fill([center - 1, 2, middle - 1], [center + 1, 3, middle + 1], 'blue_ice')
    fill([center, 4, middle], [center, 7, middle], 'packed_ice')
    for (const across of [center - 3, center + 3]) {
      fill([across, 1, middle - 1], [across, 3, middle - 1], 'packed_ice')
      put(across, 0, middle + 1, 'sea_lantern')
    }
    for (const along of [5, depth - 6]) for (const across of [5, width - 6]) {
      fill([across, 1, along], [across, height - 2, along], 'stone_bricks')
      put(across, 3, along, 'blue_ice')
    }
  }
  if (type === 'medium-room') {
    fill([1, 1, 5], [center + 1, 3, 5], 'packed_ice')
    fill([center - 1, 1, depth - 6], [width - 2, 3, depth - 6], 'packed_ice')
    fill([center - 1, 1, middle - 1], [center + 1, 1, middle + 1], 'stone_bricks')
    put(center, 2, middle, 'blue_ice')
    fill([2, 1, 7], [2, 3, 9], 'bookshelf')
    put(width - 3, 1, 7, 'furnace', { facing: 'west', lit: 'false' })
    chest(width - 3, 1, 9)
  }
  if (type === 'small-room') {
    fill([1, 1, depth - 3], [1, 3, depth - 2], 'bookshelf')
    chest(center, 1, middle + 1)
    spike(width - 3, depth - 3, 2)
  }
  if (type === 'great-hall') {
    fill([center - 1, 2, 5], [center + 1, 2, depth - 6], 'polished_andesite')
    for (const across of [center - 1, center, center + 1]) {
      put(across, 1, 3, 'stone_brick_stairs', { facing: 'south', half: 'bottom', shape: 'straight', waterlogged: 'false' })
      put(across, 2, 4, 'stone_brick_stairs', { facing: 'south', half: 'bottom', shape: 'straight', waterlogged: 'false' })
      put(across, 1, depth - 4, 'stone_brick_stairs', { facing: 'north', half: 'bottom', shape: 'straight', waterlogged: 'false' })
      put(across, 2, depth - 5, 'stone_brick_stairs', { facing: 'north', half: 'bottom', shape: 'straight', waterlogged: 'false' })
      put(across, 0, 3, 'stone_bricks')
      put(across, 1, 4, 'stone_bricks')
      put(across, 0, depth - 4, 'stone_bricks')
      put(across, 1, depth - 5, 'stone_bricks')
    }
    for (const across of [center - 2, center + 2]) {
      fill([across, 2, 5], [across, 2, depth - 6], 'stone_bricks')
      fill([across, 3, 5], [across, 3, depth - 6], 'stone_brick_wall')
      for (const along of [6, depth - 7]) fill([across, 1, along], [across, 1, along], 'stone_bricks')
    }
    for (const across of [6, width - 7]) for (const along of [4, 8, 12]) {
      fill([across - 1, 1, along - 1], [across + 1, 1, along + 1], 'chiseled_stone_bricks')
      fill([across, 2, along], [across, height - 2, along], 'stone_bricks')
      fill([across, 4, along], [across, 6, along], 'blue_ice')
      lamp(across + (across < center ? 2 : -2), along)
    }
  }
  if (corner) {
    for (const along of [3, 7, exitDepth]) {
      post(center - 3, along)
      fill([center - 3, height - 3, along], [center + 3, height - 3, along], 'spruce_log', { axis: 'x' })
      lamp(center - 2, along, 4)
    }
    for (const across of [width - 4, width - 2]) {
      post(across, depth - 1)
      fill([across, height - 3, depth - 7], [across, height - 3, depth - 1], 'spruce_log', { axis: 'z' })
      lamp(across, depth - 2, 4)
    }
    spike(center + 2, depth - 6, 2)
  }
  if (type === 'entrance') {
    put(2, 1, 8, 'stone_bricks')
    put(2, 2, 8, 'lever', { face: 'floor', facing: 'north', powered: 'false' })
    put(3, 1, 8, 'spruce_fence_gate', { facing: 'north', open: 'false', powered: 'false', in_wall: 'false' })
    put(4, 1, 8, 'stone_bricks')
    for (const across of [3, width - 4]) {
      fill([across - 1, 1, 5], [across + 1, 1, 7], 'polished_andesite')
      fill([across, 2, 6], [across, 4, 6], 'chiseled_stone_bricks')
      put(across, 5, 6, 'soul_lantern', { hanging: 'false', waterlogged: 'false' })
    }
    for (const across of [2, width - 3]) fill([across, 1, 9], [across, 1, 11], 'spruce_planks')
  }
  if (type === 'courtyard') {
    fill([center - 3, 1, middle - 3], [center + 3, 1, middle + 3], 'snow_block')
    fill([center - 1, 2, middle - 1], [center + 1, 2, middle + 1], 'stone_bricks')
    fill([center, 3, middle], [center, 10, middle], 'spruce_log', { axis: 'y' })
    for (const [elevation, radius] of [[5, 3], [7, 2], [9, 1]]) {
      for (let across = center - radius; across <= center + radius; across++) {
        for (let along = middle - radius; along <= middle + radius; along++) {
          if ((across === center && along === middle) || Math.abs(across - center) + Math.abs(along - middle) > radius + 1) continue
          put(across, elevation, along, 'spruce_leaves', { persistent: 'true', distance: '1', waterlogged: 'false' })
          put(across, elevation + 1, along, 'snow_block')
        }
      }
    }
    fill([width - 4, 1, middle - 2], [width - 3, 9, middle + 2], 'packed_ice')
    for (let along = 3; along < depth - 3; along++) {
      put(1, 7, along, 'stone_bricks')
      put(width - 2, 7, along, 'stone_bricks')
    }
  }
  if (type === 'treasure') {
    fill([center - 2, 1, middle + 1], [center + 2, 1, middle + 5], 'chiseled_stone_bricks')
    fill([center - 1, 2, middle + 2], [center + 1, 2, middle + 4], 'polished_andesite')
    for (const across of [center - 1, center, center + 1]) put(across, 1, middle, 'stone_brick_stairs', { facing: 'south', half: 'bottom', shape: 'straight', waterlogged: 'false' })
    for (const across of [center - 1, center, center + 1]) put(across, 2, middle + 1, 'stone_brick_stairs', { facing: 'south', half: 'bottom', shape: 'straight', waterlogged: 'false' })
    chest(center, 3, middle + 3)
    fill([center, 3, depth - 2], [center, 6, depth - 2], 'blue_ice')
    for (const across of [center - 3, center + 3]) {
      fill([across, 1, middle + 3], [across, 3, middle + 3], 'stone_bricks')
      put(across, 4, middle + 3, 'soul_lantern', { hanging: 'false', waterlogged: 'false' })
    }
  }
  arch(0)
  const connections = [{ name: 'north', facing: 'north', position: [center, 1, 0], width: 3, height: 4 }]
  if (exits.includes('S')) {
    arch(depth - 1)
    connections.push({ name: 'south', facing: 'south', position: [center, 1, depth - 1], width: 3, height: 4 })
  }
  if (exits.includes('E')) {
    fill([width - 1, 1, exitDepth - 1], [width - 1, 4, exitDepth + 1], 'air')
    for (const along of [exitDepth - 2, exitDepth + 2]) fill([width - 1, 1, along], [width - 1, 5, along], 'chiseled_stone_bricks')
    fill([width - 1, 5, exitDepth - 2], [width - 1, 5, exitDepth + 2], 'polished_andesite')
    connections.push({ name: 'east', facing: 'east', position: [width - 1, 1, exitDepth], width: 3, height: 4 })
  }
  const destination = path.join(root, 'incoming', id)
  fs.mkdirSync(destination, { recursive: true })
  fs.writeFileSync(path.join(destination, 'design.json'), JSON.stringify({ code, type, name, size, id, entrance: [center, 1, 0], interior: [center, 1, 2], facing: 'north', connections,
    courtyard_skylight: type === 'courtyard' ? { min: [center - 3, middle - 3], max: [center + 3, middle + 3] } : null,
    footprint: corner ? 'L shape: seven-block wide legs, north approach to east exit' : 'rectangle',
    source_evidence: evidence,
    reference: `D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S3-frost-tundra/out/S3-${code}-${type}.png`,
    maker_thread: '01a1a5df-7c46-7fc0-a33d-cfc5b059d4d3', runtime_slot: null,
  }, null, 2) + '\n')
  return { structure, metadata: { name: 'room' } }
}
