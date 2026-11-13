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
  for (const [across, forward] of [[2, 2], [22, 2], [2, 22], [22, 22]]) {
    box([across, 2, forward], [across, 12, forward], 'dark_oak_log', {axis: 'y'})
    for (let extension = 0; extension <= 3; extension++) {
      const inwardX = across < 12 ? across + extension : across - extension
      const inwardZ = forward < 12 ? forward + extension : forward - extension
      set(inwardX, 11, forward, 'dark_oak_log', {axis: 'x'})
      set(across, 12, inwardZ, 'dark_oak_log', {axis: 'z'})
      set(inwardX, 12, forward, 'oak_leaves', leaf)
      set(across, 13, inwardZ, 'oak_leaves', leaf)
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
  box([11, 2, 19], [13, 10, 21], 'dark_oak_log', {axis: 'y'})
  for (const [across, forward] of [[9, 20], [10, 19], [14, 19], [15, 20]]) {
    box([across, 2, forward], [across, across === 9 || across === 15 ? 3 : 5, forward], 'dark_oak_log', {axis: 'y'})
  }
  box([7, 10, 20], [17, 10, 20], 'dark_oak_log', {axis: 'x'})
  box([8, 9, 19], [10, 9, 19], 'dark_oak_log', {axis: 'x'})
  box([14, 8, 21], [18, 8, 21], 'dark_oak_log', {axis: 'x'})
  box([18, 8, 19], [18, 8, 23], 'dark_oak_log', {axis: 'z'})
  box([18, 9, 21], [21, 9, 21], 'dark_oak_log', {axis: 'x'})
  box([9, 9, 18], [9, 9, 22], 'dark_oak_log', {axis: 'z'})
  for (let across = 7; across <= 10; across++) for (let forward = 18; forward <= 22; forward++) if ((across + forward) % 3 !== 0) set(across, 10, forward, 'oak_leaves', leaf)
  for (let across = 17; across <= 21; across++) for (let forward = 19; forward <= 23; forward++) if ((across + forward) % 4 !== 0) set(across, 10, forward, 'oak_leaves', leaf)
  box([12, 11, 16], [12, 11, 23], 'dark_oak_log', {axis: 'z'})
  for (let across = 5; across <= 19; across++) {
    for (let forward = 16; forward <= 23; forward++) {
      const radius = Math.abs(across - 12) + Math.abs(forward - 20)
      if (radius <= 9) set(across, 12, forward, 'oak_leaves', leaf)
      if (radius <= 6) set(across, 13, forward, 'oak_leaves', leaf)
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
