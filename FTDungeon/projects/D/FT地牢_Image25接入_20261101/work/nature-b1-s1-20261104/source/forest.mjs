import {readFileSync} from 'node:fs'

const parameters = JSON.parse(readFileSync(new URL('../evidence/parameters.json', import.meta.url), 'utf8'))

export const rooms = [
  {id: 'nature-b1-s1-01', number: '01', type: 'large-chamber', name: '古根祭坛大厅', size: [19, 12, 19], ports: ['north', 'south', 'east']},
  {id: 'nature-b1-s1-02', number: '02', type: 'medium-room', name: '苔石泉台', size: [15, 10, 15], ports: ['north', 'south']},
  {id: 'nature-b1-s1-03', number: '03', type: 'small-room', name: '林根守望龛', size: [9, 8, 9], ports: ['north', 'east']},
  {id: 'nature-b1-s1-04', number: '04', type: 'great-hall', name: '双列根柱长厅', size: [21, 12, 17], ports: ['north', 'south', 'west', 'east']},
  {id: 'nature-b1-s1-05', number: '05', type: 'corridor', name: '荧果根廊', size: [9, 9, 23], ports: ['north', 'south']},
  {id: 'nature-b1-s1-06', number: '06', type: 'corner', name: '古木转角', size: [15, 10, 15], ports: ['north', 'east']},
  {id: 'nature-b1-s1-07', number: '07', type: 'entrance', name: '古林门庭', size: [19, 11, 13], ports: ['north', 'south']},
  {id: 'nature-b1-s1-08', number: '08', type: 'courtyard', name: '根瀑天井', size: [23, 13, 23], ports: ['north', 'south', 'west', 'east'], sky: [8, 14, 8, 14]},
  {id: 'nature-b1-s1-09', number: '09', type: 'treasure', name: '林王封藏宝殿', size: [17, 11, 19], ports: ['north']},
].map(room => ({...room, size: parameters[room.number].size, source_evidence: parameters[room.number], ...(room.type === 'courtyard' ? {sky: [12, 18, 12, 18]} : {})}))

export function routeFor(room) {
  const [width, , depth] = room.size
  const centerX = Math.floor(width / 2)
  const centerZ = Math.floor(depth / 2)
  const cells = new Set()
  const strip = (startX, startZ, endX, endZ) => {
    for (let across = Math.min(startX, endX); across <= Math.max(startX, endX); across++) {
      for (let forward = Math.min(startZ, endZ); forward <= Math.max(startZ, endZ); forward++) cells.add(`${across},${forward}`)
    }
  }
  if (room.type === 'treasure') {
    strip(centerX - 1, 0, centerX + 1, 7)
  } else if (room.type === 'medium-room') {
    strip(centerX - 1, 0, centerX + 1, depth - 1)
    strip(1, centerZ - 1, width - 2, centerZ + 1)
  } else if (['corridor', 'great-hall', 'entrance'].includes(room.type)) {
    strip(centerX - 1, 0, centerX + 1, depth - 1)
    if (room.ports.includes('east') || room.ports.includes('west')) strip(0, centerZ - 1, width - 1, centerZ + 1)
  } else if (['corner', 'small-room'].includes(room.type)) {
    strip(centerX - 1, 0, centerX + 1, centerZ + 1)
    strip(centerX - 1, centerZ - 1, width - 1, centerZ + 1)
  } else {
    strip(centerX - 1, 0, centerX + 1, 4)
    strip(2, 2, width - 3, 4)
    strip(2, 2, 4, depth - 3)
    strip(width - 5, 2, width - 3, depth - 3)
    strip(2, depth - 5, width - 3, depth - 3)
    if (room.ports.includes('south')) strip(centerX - 1, depth - 5, centerX + 1, depth - 1)
    if (room.ports.includes('west')) strip(0, centerZ - 1, 4, centerZ + 1)
    if (room.ports.includes('east')) strip(width - 5, centerZ - 1, width - 1, centerZ + 1)
  }
  return [...cells].map(cell => cell.split(',').map(Number))
}

export function buildRoom(api, room) {
  const [width, height, depth] = room.size
  const centerX = Math.floor(width / 2)
  const centerZ = Math.floor(depth / 2)
  const lane = new Set(routeFor(room).map(cell => cell.join(',')))
  const structure = api.createStructure(room.size)
  const set = (across, vertical, forward, block, properties, nbt) => api.setBlock(structure, [across, vertical, forward], block, properties, nbt)
  const box = (start, end, block, properties) => api.fill(structure, start, end, block, properties)
  const decor = (across, vertical, forward, block, properties, nbt) => {
    if (vertical >= 2 && vertical <= 4 && lane.has(`${across},${forward}`)) return
    set(across, vertical, forward, block, properties, nbt)
  }
  const stone = (across, vertical, forward) => {
    const pattern = (across * 19 + vertical * 7 + forward * 13) % 17
    return pattern < 7 ? 'mossy_stone_bricks' : pattern < 13 ? 'stone_bricks' : pattern < 16 ? 'cracked_stone_bricks' : 'mossy_cobblestone'
  }
  const log = {axis: 'y'}
  const leaves = {persistent: 'true', distance: '1', waterlogged: 'false'}
  box([0, 0, 0], [width - 1, height - 1, depth - 1], 'air')
  box([0, 0, 0], [width - 1, 0, depth - 1], 'stone_bricks')
  for (let across = 0; across < width; across++) {
    for (let forward = 0; forward < depth; forward++) {
      const path = lane.has(`${across},${forward}`)
      set(across, 1, forward, path ? stone(across, 1, forward) : (across * 7 + forward * 3) % 6 < 2 ? 'moss_block' : stone(across, 1, forward))
      const skylight = room.sky && across >= room.sky[0] && across <= room.sky[1] && forward >= room.sky[2] && forward <= room.sky[3]
      if (!skylight) set(across, height - 1, forward, stone(across, height - 1, forward))
      for (let vertical = 2; vertical < height - 1; vertical++) {
        if (across === 0 || forward === 0 || across === width - 1 || forward === depth - 1) set(across, vertical, forward, stone(across, vertical, forward))
      }
    }
  }
  const hanging = (across, forward, length) => {
    const top = height - 3
    set(across, top + 1, forward, 'dark_oak_log', {axis: 'x'})
    for (let drop = 0; drop < length; drop++) set(across, top - drop, forward, drop === length - 1 ? 'cave_vines' : 'cave_vines_plant', drop === length - 1 ? {berries: 'true', age: '25'} : {berries: 'true'})
  }
  const pillar = (across, forward) => {
    decor(across, 2, forward, 'chiseled_stone_bricks')
    for (let vertical = 3; vertical < height - 2; vertical++) decor(across, vertical, forward, 'stone_bricks')
    set(across, height - 2, forward, 'mossy_stone_bricks')
  }
  const lamp = (across, forward) => {
    if (lane.has(`${across},${forward}`)) return
    decor(across, 2, forward, 'chiseled_stone_bricks')
    decor(across, 3, forward, 'lantern', {hanging: 'false', waterlogged: 'false'})
  }
  const chest = (across, forward) => {
    if (lane.has(`${across},${forward}`)) throw new Error('Chest intersects route')
    set(across, 1, forward, 'stone_bricks')
    set(across, 2, forward, 'chest', {facing: 'north', type: 'single', waterlogged: 'false'}, api.createBlockNbt({id: 'minecraft:chest'}))
  }
  const pond = (startX, startZ, pondWidth, pondDepth) => {
    for (let across = startX; across < startX + pondWidth; across++) {
      for (let forward = startZ; forward < startZ + pondDepth; forward++) {
        const edge = across === startX || across === startX + pondWidth - 1 || forward === startZ || forward === startZ + pondDepth - 1
        if (!edge && lane.has(`${across},${forward}`)) throw new Error('Pond intersects route')
        set(across, 1, forward, edge ? 'mossy_stone_bricks' : 'water', edge ? undefined : {level: '0'})
      }
    }
  }
  for (let forward = 2; forward < depth - 1; forward += 4) {
    pillar(1, forward)
    pillar(width - 2, forward)
    box([1, height - 2, forward], [width - 2, height - 2, forward], 'dark_oak_log', {axis: 'x'})
    hanging(2, forward, Math.min(3, height - 6))
    hanging(width - 3, forward, Math.min(2 + forward % 2, height - 6))
    for (const across of [1, width - 2]) set(across, height - 3, forward + 1 < depth - 1 ? forward + 1 : forward, 'oak_leaves', leaves)
  }
  for (const [across, forward] of [[1, 1], [width - 2, 1], [1, depth - 2], [width - 2, depth - 2]]) {
    for (let vertical = 2; vertical < height - 1; vertical++) decor(across, vertical, forward, 'dark_oak_log', log)
    for (let spread = 0; spread < 3; spread++) {
      const branchX = across === 1 ? across + spread : across - spread
      set(branchX, height - 3, forward, 'dark_oak_log', {axis: 'x'})
      set(branchX, height - 2, forward, 'oak_leaves', leaves)
    }
  }
  if (room.type === 'large-chamber') {
    pond(5, 5, 4, 4)
    box([centerX - 2, 2, centerZ - 2], [centerX + 2, 2, centerZ + 2], 'mossy_stone_bricks')
    box([centerX - 1, 3, centerZ - 1], [centerX + 1, 3, centerZ + 1], 'stone_bricks')
    set(centerX, 4, centerZ, 'chiseled_stone_bricks')
    for (const across of [centerX - 2, centerX + 2]) for (const forward of [centerZ - 2, centerZ + 2]) lamp(across, forward)
    chest(width - 2, depth - 4)
  } else if (room.type === 'medium-room') {
    box([2, 2, 3], [4, 2, 5], 'mossy_stone_bricks')
    set(3, 2, 4, 'water', {level: '0'})
    box([centerX + 3, 2, 2], [centerX + 3, height - 2, depth - 3], 'mossy_stone_bricks')
    box([centerX + 3, 2, centerZ - 1], [centerX + 3, 4, centerZ + 1], 'air')
    chest(width - 2, depth - 3)
    lamp(1, 5)
    lamp(width - 2, 5)
  } else if (room.type === 'small-room') {
    box([1, 2, depth - 3], [3, 2, depth - 2], 'mossy_stone_bricks')
    set(2, 3, depth - 2, 'lantern', {hanging: 'false', waterlogged: 'false'})
    chest(width - 3, depth - 2)
    set(1, 2, 2, 'moss_carpet')
  } else if (room.type === 'great-hall') {
    for (const across of [5, width - 6]) {
      for (const forward of [3, 6, 10, 13]) {
        if (lane.has(`${across},${forward}`)) continue
        pillar(across, forward)
        set(across + (across < centerX ? 1 : -1), height - 3, forward, 'stone_brick_stairs', {facing: across < centerX ? 'west' : 'east', half: 'top', shape: 'straight', waterlogged: 'false'})
        set(across, height - 3, forward + 1, 'lantern', {hanging: 'true', waterlogged: 'false'})
        set(across, height - 2, forward + 1, 'dark_oak_log', {axis: 'z'})
      }
    }
    pond(1, 5, 3, 3)
  } else if (room.type === 'corridor') {
    for (let forward = 4; forward < depth - 2; forward += 6) {
      for (const across of [1, width - 2]) {
        set(across, 2, forward, 'dark_oak_log', log)
        set(across, 3, forward, 'lantern', {hanging: 'false', waterlogged: 'false'})
      }
    }
    pond(1, 8, 3, 4)
    pond(width - 4, 15, 3, 4)
  } else if (room.type === 'corner') {
    box([3, 2, 10], [5, 2, 12], 'mossy_stone_bricks')
    for (let vertical = 3; vertical < height - 2; vertical++) set(4, vertical, 11, 'dark_oak_log', log)
    box([3, height - 3, 10], [5, height - 3, 12], 'oak_leaves', leaves)
    pond(9, 10, 4, 3)
    lamp(2, 3)
    lamp(11, 3)
  } else if (room.type === 'entrance') {
    for (const across of [4, width - 5]) {
      box([across - 1, 2, 5], [across + 1, 2, 7], 'mossy_stone_bricks')
      for (let vertical = 3; vertical < height - 2; vertical++) set(across, vertical, 6, 'dark_oak_log', log)
      lamp(across, 4)
      set(across, 3, 7, 'chiseled_stone_bricks')
      set(across, 4, 7, 'stone_brick_wall', {up: 'true', north: 'low', south: 'none', west: 'none', east: 'none', waterlogged: 'false'})
    }
    chest(2, depth - 3)
    chest(width - 3, depth - 3)
  } else if (room.type === 'courtyard') {
    pond(centerX - 5, centerZ - 5, 11, 11)
    for (let across = centerX - 2; across <= centerX + 2; across++) for (let forward = centerZ - 2; forward <= centerZ + 2; forward++) set(across, 1, forward, 'moss_block')
    for (let vertical = 2; vertical <= 8; vertical++) set(centerX, vertical, centerZ, 'dark_oak_log', log)
    for (let across = centerX - 3; across <= centerX + 3; across++) {
      for (let forward = centerZ - 3; forward <= centerZ + 3; forward++) {
        if (Math.abs(across - centerX) + Math.abs(forward - centerZ) <= 5) set(across, 9, forward, 'oak_leaves', leaves)
      }
    }
    box([centerX - 3, 7, centerZ], [centerX + 3, 7, centerZ], 'dark_oak_log', {axis: 'x'})
    box([centerX, 8, centerZ - 3], [centerX, 8, centerZ + 3], 'dark_oak_log', {axis: 'z'})
    for (const across of [centerX - 5, centerX + 5]) for (const forward of [centerZ - 5, centerZ + 5]) lamp(across, forward)
  } else if (room.type === 'treasure') {
    box([1, 2, 8], [width - 2, height - 2, 8], 'stone_bricks')
    box([centerX - 1, 2, 8], [centerX + 1, 5, 8], 'air')
    for (const across of [centerX - 2, centerX + 2]) box([across, 2, 7], [across, 6, 7], 'chiseled_stone_bricks')
    box([centerX - 2, 2, 9], [centerX + 2, 2, 13], 'mossy_stone_bricks')
    box([centerX - 1, 3, 10], [centerX + 1, 3, 12], 'chiseled_stone_bricks')
    set(centerX, 4, 11, 'chest', {facing: 'north', type: 'single', waterlogged: 'false'}, api.createBlockNbt({id: 'minecraft:chest'}))
    for (const across of [centerX - 3, centerX + 3]) {
      for (let vertical = 2; vertical <= 6; vertical++) decor(across, vertical, depth - 2, 'dark_oak_log', log)
      set(across, 5, depth - 3, 'red_wool')
      set(across, 6, depth - 3, 'yellow_wool')
      lamp(across, 8)
    }
    pond(1, 10, 3, 3)
  }
  for (const side of room.ports) {
    const alongX = side === 'north' || side === 'south'
    const boundary = side === 'north' || side === 'west' ? 0 : alongX ? depth - 1 : width - 1
    const middle = alongX ? centerX : centerZ
    for (let offset = -1; offset <= 1; offset++) {
      for (let vertical = 2; vertical <= 4; vertical++) {
        if (alongX) set(middle + offset, vertical, boundary, 'air')
        else set(boundary, vertical, middle + offset, 'air')
      }
      if (alongX) set(middle + offset, 1, boundary, 'stone_bricks')
      else set(boundary, 1, middle + offset, 'stone_bricks')
    }
    for (const offset of [-2, 2]) {
      for (let vertical = 2; vertical <= 5; vertical++) {
        if (alongX) set(middle + offset, vertical, boundary, vertical === 2 ? 'chiseled_stone_bricks' : 'stone_bricks')
        else set(boundary, vertical, middle + offset, vertical === 2 ? 'chiseled_stone_bricks' : 'stone_bricks')
      }
    }
    for (let offset = -1; offset <= 1; offset++) {
      if (alongX) set(middle + offset, 5, boundary, offset === 0 ? 'chiseled_stone_bricks' : 'mossy_stone_bricks')
      else set(boundary, 5, middle + offset, offset === 0 ? 'chiseled_stone_bricks' : 'mossy_stone_bricks')
    }
  }
  const distances = new Map()
  let frontier = structure.getBlocks().filter(block => String(block.state.getName()).endsWith('_log')).map(block => block.pos)
  for (let distance = 1; distance <= 6; distance++) {
    const next = []
    for (const [across, vertical, forward] of frontier) {
      for (const [deltaX, deltaY, deltaZ] of [[1, 0, 0], [-1, 0, 0], [0, 1, 0], [0, -1, 0], [0, 0, 1], [0, 0, -1]]) {
        const position = [across + deltaX, vertical + deltaY, forward + deltaZ]
        const key = position.join(',')
        const block = structure.getBlock(position)
        if (!distances.has(key) && block && String(block.state.getName()).endsWith('_leaves')) {
          distances.set(key, distance)
          next.push(position)
        }
      }
    }
    frontier = next
  }
  for (const block of structure.getBlocks()) {
    if (String(block.state.getName()).endsWith('_leaves')) api.setBlock(structure, block.pos, 'oak_leaves', {...leaves, distance: String(distances.get(block.pos.join(',')) ?? 7)})
  }
  return {structure, metadata: {name: 'room'}}
}
