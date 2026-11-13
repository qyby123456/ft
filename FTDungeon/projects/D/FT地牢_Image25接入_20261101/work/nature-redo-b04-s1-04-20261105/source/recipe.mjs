export default function build(api) {
  const structure = api.createStructure([31, 18, 35])
  const set = (across, vertical, forward, block, properties, nbt) => api.setBlock(structure, [across, vertical, forward], block, properties, nbt)
  const box = (start, end, block, properties) => api.fill(structure, start, end, block, properties)
  const leaf = {persistent: 'true', distance: '7', waterlogged: 'false'}
  const stone = (across, vertical, forward) => ['mossy_stone_bricks', 'stone_bricks', 'cracked_stone_bricks', 'stone_bricks', 'mossy_stone_bricks', 'stone_bricks'][(across * 13 + vertical * 7 + forward * 11) % 6]
  box([0, 0, 0], [30, 17, 34], 'air')
  box([0, 0, 0], [30, 0, 34], 'stone_bricks')
  for (let across = 0; across < 31; across++) for (let forward = 0; forward < 35; forward++) {
    const pattern = (across * 7 + forward * 13) % 19
    set(across, 1, forward, pattern < 3 ? 'moss_block' : pattern === 6 && (across < 6 || across > 24) ? 'coarse_dirt' : stone(across, 1, forward))
    set(across, 17, forward, 'mossy_stone_bricks')
    if (across === 0 || across === 30 || forward === 0 || forward === 34) for (let vertical = 2; vertical < 17; vertical++) set(across, vertical, forward, stone(across, vertical, forward))
  }
  for (let across = 11; across <= 19; across++) for (let forward = 3; forward < 33; forward++) {
    const radius = Math.hypot(across - 15, forward - 18)
    const ring = radius >= 2.4 && radius < 3.7
    if (ring || radius < 1.2) set(across, 1, forward, 'moss_block')
    else if (radius < 4.3) set(across, 1, forward, 'chiseled_stone_bricks')
  }
  for (const across of [8, 22]) for (const forward of [8, 16, 24]) {
    box([across - 2, 2, forward - 2], [across + 2, 2, forward + 2], 'mossy_stone_bricks')
    box([across - 1, 3, forward - 1], [across + 1, 3, forward + 1], 'stone_bricks')
    box([across, 4, forward], [across + 1, 12, forward + 1], 'stone_bricks')
    box([across, 4, forward], [across + 1, 4, forward + 1], 'chiseled_stone_bricks')
    box([across - 1, 13, forward - 1], [across + 2, 13, forward + 2], 'mossy_stone_bricks')
    box([across - 1, 14, forward - 1], [across + 2, 14, forward + 2], 'stone_bricks')
    for (const [deltaX, deltaZ] of [[0,0], [1,1]]) for (const vertical of [6,10]) set(across + deltaX, vertical, forward + deltaZ, 'mossy_stone_bricks')
  }
  box([11, 2, 0], [19, 13, 1], 'mossy_stone_bricks')
  box([12, 2, 0], [18, 9, 1], 'air')
  box([13, 10, 0], [17, 10, 1], 'air')
  box([14, 11, 0], [16, 11, 1], 'air')
  for (const across of [10,19]) {
    box([across, 2, 1], [across + 1, 2, 3], 'mossy_stone_bricks')
    box([across, 3, 1], [across + 1, 11, 2], 'stone_bricks')
    box([across, 12, 1], [across + 1, 12, 3], 'chiseled_stone_bricks')
  }
  box([13, 2, 34], [17, 7, 34], 'air')
  box([12, 2, 34], [12, 8, 34], 'stone_bricks')
  box([18, 2, 34], [18, 8, 34], 'stone_bricks')
  box([12, 8, 34], [18, 8, 34], 'mossy_stone_bricks')
  for (const across of [0,30]) {
    box([across, 2, 19], [across, 5, 21], 'air')
    box([across, 2, 18], [across, 6, 18], 'stone_bricks')
    box([across, 2, 22], [across, 6, 22], 'stone_bricks')
    box([across, 6, 18], [across, 6, 22], 'mossy_stone_bricks')
  }
  for (const minX of [2,23]) for (let across = minX; across <= minX + 5; across++) for (let forward = 27; forward <= 32; forward++) {
    const rim = across === minX || across === minX + 5 || forward === 27 || forward === 32
    set(across, 1, forward, rim ? 'mossy_stone_bricks' : 'water', rim ? undefined : {level:'0'})
  }
  for (const across of [2,27]) for (const forward of [5,13,25]) {
    box([across, 2, forward], [across + 1, 11, forward + 1], 'dark_oak_log', {axis:'y'})
    box([across, 10, forward - 1], [across + 1, 13, forward + 2], 'dark_oak_log', {axis:'y'})
    if (across === 2) {
      box([3, 12, forward], [7, 13, forward + 1], 'dark_oak_log', {axis:'x'})
      box([1, 2, forward], [4, 2, forward + 2], 'dark_oak_log', {axis:'z'})
    } else {
      box([24, 12, forward], [28, 13, forward + 1], 'dark_oak_log', {axis:'x'})
      box([26, 2, forward], [29, 2, forward + 2], 'dark_oak_log', {axis:'z'})
    }
  }
  box([2, 13, 3], [3, 14, 28], 'dark_oak_log', {axis:'z'})
  box([27, 13, 3], [28, 14, 28], 'dark_oak_log', {axis:'z'})
  box([3, 14, 2], [27, 15, 3], 'dark_oak_log', {axis:'x'})
  for (const forward of [8,16,24]) for (const [minX,maxX] of [[3,10],[21,28]]) box([minX, 14, forward], [maxX, 14, forward + 1], 'dark_oak_log', {axis:'x'})
  for (let across = 1; across < 30; across++) for (let vertical = 12; vertical < 17; vertical++) for (let forward = 1; forward < 33; forward++) {
    const edge = across < 7 || across > 24 || forward < 5
    const uneven = (across * 3 + forward * 7 + vertical) % 9
    if (edge && uneven < 5 && String(structure.getBlock([across, vertical, forward]).state.getName()) === 'minecraft:air') {
      const supported = [[1,0,0],[-1,0,0],[0,-1,0],[0,0,1],[0,0,-1]].some(([deltaX,deltaY,deltaZ]) => String(structure.getBlock([across + deltaX,vertical + deltaY,forward + deltaZ])?.state.getName() ?? 'minecraft:air') !== 'minecraft:air')
      if (supported) set(across, vertical, forward, 'oak_leaves', leaf)
    }
  }
  for (const across of [4,26]) for (const forward of [6,14,26]) {
    set(across, 12, forward, 'dark_oak_log', {axis:'x'})
    for (let vertical = 11; vertical >= 8; vertical--) set(across, vertical, forward, vertical === 8 ? 'cave_vines' : 'cave_vines_plant', vertical === 8 ? {berries:'true',age:'25'} : {berries:'true'})
  }
  for (const across of [9,21]) for (const forward of [8,16,24]) {
    set(across, 12, forward, 'dark_oak_log', {axis:'x'})
    set(across, 11, forward, 'lantern', {hanging:'true',waterlogged:'false'})
  }
  for (const [across, facing] of [[10,'east'],[21,'west']]) {
    set(across, 8, 24, 'red_wall_banner', {facing}, api.createBlockNbt({id:'minecraft:banner',Patterns:[{Pattern:'bo',Color:4},{Pattern:'cs',Color:4}]}))
  }
  for (const [across,forward] of [[1,11],[5,11],[25,11],[29,11],[1,23],[5,23],[25,23],[29,23],[8,32],[22,32]]) {
    if (String(structure.getBlock([across,2,forward]).state.getName()) === 'minecraft:air') set(across,2,forward,'oak_leaves',leaf)
  }
  const distances = new Map()
  let frontier = structure.getBlocks().filter(block => String(block.state.getName()).endsWith('_log')).map(block => block.pos)
  for (let distance = 1; distance <= 6; distance++) {
    const next = []
    for (const [across, vertical, forward] of frontier) for (const [deltaX,deltaY,deltaZ] of [[1,0,0],[-1,0,0],[0,1,0],[0,-1,0],[0,0,1],[0,0,-1]]) {
      const point = [across + deltaX,vertical + deltaY,forward + deltaZ]
      const block = structure.getBlock(point)
      const key = point.join(',')
      if (!distances.has(key) && block && String(block.state.getName()).endsWith('_leaves')) {distances.set(key,distance); next.push(point)}
    }
    frontier = next
  }
  for (const block of structure.getBlocks()) if (String(block.state.getName()).endsWith('_leaves')) api.setBlock(structure,block.pos,'oak_leaves',{...leaf,distance:String(distances.get(block.pos.join(',')) ?? 7)})
  return {structure,metadata:{name:'room'}}
}
