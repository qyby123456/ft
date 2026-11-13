export default function build(api) {
  const structure = api.createStructure([25, 15, 25])
  const set = (across, vertical, forward, block, properties) => api.setBlock(structure, [across, vertical, forward], block, properties)
  const box = (start, end, block, properties) => api.fill(structure, start, end, block, properties)
  const stone = (across, vertical, forward) => ['mossy_stone_bricks', 'stone_bricks', 'mossy_stone_bricks', 'cracked_stone_bricks', 'stone_bricks'][(across * 11 + vertical * 3 + forward * 7) % 5]
  const leaf = {persistent: 'true', distance: '7', waterlogged: 'false'}
  box([0, 0, 0], [24, 14, 24], 'air')
  box([0, 0, 0], [24, 0, 24], 'stone_bricks')
  for (let across = 0; across < 25; across++) {
    for (let forward = 0; forward < 25; forward++) {
      const edge = across === 0 || forward === 0 || across === 24 || forward === 24
      set(across, 1, forward, (across * 3 + forward * 5) % 13 < 3 ? 'moss_block' : stone(across, 1, forward))
      for (let vertical = 2; vertical < 14; vertical++) if (edge) set(across, vertical, forward, stone(across, vertical, forward))
      set(across, 14, forward, 'mossy_stone_bricks')
    }
  }
  for (const forward of [5, 12, 19]) {
    for (const across of [1, 23]) {
      box([across, 2, forward], [across, 10, forward], 'stone_bricks')
      set(across, 2, forward, 'chiseled_stone_bricks')
      set(across, 11, forward, 'mossy_stone_bricks')
    }
    for (let across = 2; across <= 22; across++) {
      const rise = Math.min(3, Math.floor(Math.min(across - 2, 22 - across) / 2))
      set(across, 10 + rise, forward, 'mossy_stone_bricks')
    }
  }
  for (const across of [1, 23]) {
    for (const forward of [4, 19]) {
      box([across === 1 ? 1 : 22, 2, forward], [across === 1 ? 2 : 23, 10, forward + 1], 'stone_bricks')
      for (let offset = 0; offset < 2; offset++) set(across, 2, forward + offset, 'chiseled_stone_bricks')
    }
  }
  box([8, 2, 8], [16, 2, 16], 'mossy_stone_bricks')
  box([9, 3, 9], [15, 3, 15], 'stone_bricks')
  box([10, 4, 10], [14, 4, 14], 'mossy_stone_bricks')
  box([11, 5, 11], [13, 5, 13], 'chiseled_stone_bricks')
  set(12, 5, 12, 'moss_block')
  for (let across = 10; across <= 14; across++) {
    set(across, 2, 7, 'stone_brick_stairs', {facing: 'south', half: 'bottom', shape: 'straight', waterlogged: 'false'})
    set(across, 3, 8, 'stone_brick_stairs', {facing: 'south', half: 'bottom', shape: 'straight', waterlogged: 'false'})
    set(across, 4, 9, 'stone_brick_stairs', {facing: 'south', half: 'bottom', shape: 'straight', waterlogged: 'false'})
  }
  for (let forward = 10; forward <= 14; forward++) {
    set(17, 2, forward, 'stone_brick_stairs', {facing: 'west', half: 'bottom', shape: 'straight', waterlogged: 'false'})
    set(16, 3, forward, 'stone_brick_stairs', {facing: 'west', half: 'bottom', shape: 'straight', waterlogged: 'false'})
    set(15, 4, forward, 'stone_brick_stairs', {facing: 'west', half: 'bottom', shape: 'straight', waterlogged: 'false'})
  }
  for (const [across, forward] of [[8, 8], [16, 8], [8, 16], [16, 16]]) {
    box([across, 2, forward], [across, 4, forward], 'chiseled_stone_bricks')
    set(across, 5, forward, 'lantern', {hanging: 'false', waterlogged: 'false'})
  }
  for (let vertical = 2; vertical <= 12; vertical++) {
    const trunkX = vertical < 7 ? 12 : vertical < 10 ? 13 : 14
    const trunkZ = vertical < 9 ? 21 : 20
    const radiusX = vertical < 5 ? 3 : vertical < 9 ? 2 : 1.5
    const radiusZ = vertical < 5 ? 2.4 : 1.7
    for (let across = 8; across <= 17; across++) {
      for (let forward = 19; forward <= 23; forward++) {
        const irregularity = ((across * 7 + forward * 11 + Math.floor(vertical / 3)) % 5) * .055
        if (((across - trunkX) / radiusX) ** 2 + ((forward - trunkZ) / radiusZ) ** 2 <= 1.1 + irregularity) set(across, vertical, forward, 'dark_oak_log', {axis: 'y'})
      }
    }
  }
  const branch = (start, end, radius = 0) => {
    const steps = Math.max(...start.map((coordinate, index) => Math.abs(coordinate - end[index])))
    for (let step = 0; step <= steps; step++) {
      const point = start.map((coordinate, index) => Math.round(coordinate + (end[index] - coordinate) * step / Math.max(1, steps)))
      for (let deltaX = -radius; deltaX <= radius; deltaX++) {
        for (let deltaZ = -radius; deltaZ <= radius; deltaZ++) {
          const across = point[0] + deltaX
          const forward = point[2] + deltaZ
          if (across > 0 && across < 24 && forward > 0 && forward < 24) set(across, point[1], forward, 'dark_oak_log', {axis: Math.abs(end[0] - start[0]) > Math.abs(end[2] - start[2]) ? 'x' : 'z'})
        }
      }
    }
  }
  branch([11, 3, 21], [7, 2, 22], 1)
  branch([14, 3, 22], [21, 2, 23], 1)
  branch([10, 3, 20], [8, 2, 19])
  branch([15, 3, 20], [17, 2, 19])
  branch([12, 6, 20], [12, 5, 16])
  branch([11, 8, 21], [6, 10, 19], 1)
  branch([6, 10, 19], [3, 11, 16])
  branch([13, 9, 21], [19, 11, 22], 1)
  branch([19, 11, 22], [22, 12, 19])
  branch([14, 11, 20], [16, 12, 15], 1)
  branch([11, 10, 20], [9, 12, 16])
  const crowns = [[5, 11, 17, 3.7, 2.2, 3.4], [8, 11, 21, 4.2, 2.3, 3], [12, 12, 19, 4.5, 1.8, 4], [17, 11, 21, 4.4, 2.7, 3], [20, 12, 18, 3, 1.7, 3.6], [15, 11, 15, 3.3, 2.5, 3]]
  for (const [centerX, centerY, centerZ, radiusX, radiusY, radiusZ] of crowns) {
    for (let across = 1; across < 24; across++) for (let vertical = 7; vertical <= 13; vertical++) for (let forward = 11; forward < 24; forward++) {
      const shape = ((across - centerX) / radiusX) ** 2 + ((vertical - centerY) / radiusY) ** 2 + ((forward - centerZ) / radiusZ) ** 2
      const irregularity = ((across * 17 + vertical * 13 + forward * 7) % 11) / 35
      if (shape < .88 + irregularity && String(structure.getBlock([across, vertical, forward]).state.getName()) === 'minecraft:air') set(across, vertical, forward, 'oak_leaves', leaf)
    }
  }
  for (let across = 3; across <= 7; across++) {
    for (let forward = 3; forward <= 6; forward++) {
      const edge = across === 3 || across === 7 || forward === 3 || forward === 6
      set(across, 1, forward, edge ? 'mossy_stone_bricks' : 'water', edge ? undefined : {level: '0'})
    }
  }
  for (const [across, forward] of [[3, 15], [5, 20], [19, 3], [22, 7], [3, 8], [18, 21]]) {
    set(across, 2, forward, 'moss_block')
    set(across, 3, forward, 'oak_leaves', leaf)
  }
  const protectedLane = (across, forward) => across >= 11 && across <= 13 && forward <= 5 || across >= 8 && across <= 19 && forward >= 4 && forward <= 6 || across >= 4 && across <= 6 && forward >= 7 && forward <= 17 || across >= 18 && across <= 20 && forward >= 7 && forward <= 17 || across >= 5 && across <= 19 && forward >= 17 && forward <= 18 || across <= 6 && forward >= 11 && forward <= 13 || across >= 18 && forward >= 11 && forward <= 13
  for (let across = 1; across < 24; across++) for (let forward = 1; forward < 24; forward++) {
    if (protectedLane(across, forward) || !(across <= 3 || across >= 21 || forward <= 2 || forward >= 22)) continue
    const at = structure.getBlock([across, 2, forward])
    if (String(at.state.getName()) !== 'minecraft:air' || (across * 3 + forward * 7) % 5 === 0) continue
    set(across, 2, forward, 'oak_leaves', leaf)
    if ((across + forward) % 4 === 0) set(across, 3, forward, 'oak_leaves', leaf)
  }
  for (const [across, forward] of [[5, 18], [8, 20], [16, 20], [19, 18], [3, 10], [21, 10]]) {
    set(across, 12, forward, 'dark_oak_log', {axis: 'x'})
    set(across, 11, forward, 'cave_vines_plant', {berries: 'true'})
    set(across, 10, forward, 'cave_vines_plant', {berries: 'true'})
    set(across, 9, forward, 'cave_vines', {berries: 'true', age: '25'})
  }
  for (const side of ['north', 'west', 'east']) {
    const boundary = side === 'east' ? 24 : 0
    for (let offset = -2; offset <= 2; offset++) {
      const top = Math.abs(offset) === 2 ? 5 : Math.abs(offset) === 1 ? 6 : 7
      for (let vertical = 2; vertical <= top; vertical++) {
        if (side === 'north') set(12 + offset, vertical, 0, 'air')
        else {
          set(boundary, vertical, 12 + offset, 'air')
          set(side === 'west' ? 1 : 23, vertical, 12 + offset, 'air')
        }
      }
      if (side === 'north') set(12 + offset, top + 1, 0, 'chiseled_stone_bricks')
      else set(boundary, top + 1, 12 + offset, 'chiseled_stone_bricks')
    }
    for (const offset of [-3, 3]) {
      if (side === 'north') box([12 + offset, 2, 0], [12 + offset, 7, 0], 'stone_bricks')
      else box([boundary, 2, 12 + offset], [boundary, 7, 12 + offset], 'stone_bricks')
    }
  }
  for (const [across, vertical, forward] of [[4, 10, 5], [20, 10, 5], [4, 10, 12], [20, 10, 12], [6, 11, 5], [18, 11, 5], [6, 11, 12]]) set(across, vertical, forward, 'mossy_stone_bricks')
  for (const across of [3, 21]) set(across, 13, 10, 'dark_oak_log', {axis: 'y'})
  set(12, 5, 18, 'dark_oak_log', {axis: 'z'})
  const distances = new Map()
  let frontier = structure.getBlocks().filter(block => String(block.state.getName()).endsWith('_log')).map(block => block.pos)
  for (let distance = 1; distance <= 6; distance++) {
    const next = []
    for (const [across, vertical, forward] of frontier) {
      for (const [deltaX, deltaY, deltaZ] of [[1, 0, 0], [-1, 0, 0], [0, 1, 0], [0, -1, 0], [0, 0, 1], [0, 0, -1]]) {
        const position = [across + deltaX, vertical + deltaY, forward + deltaZ]
        const block = structure.getBlock(position)
        const key = position.join(',')
        if (!distances.has(key) && block && String(block.state.getName()).endsWith('_leaves')) { distances.set(key, distance); next.push(position) }
      }
    }
    frontier = next
  }
  for (const block of structure.getBlocks()) if (String(block.state.getName()).endsWith('_leaves')) api.setBlock(structure, block.pos, 'oak_leaves', {...leaf, distance: String(distances.get(block.pos.join(',')) ?? 7)})
  return {structure, metadata: {name: 'room'}}
}
