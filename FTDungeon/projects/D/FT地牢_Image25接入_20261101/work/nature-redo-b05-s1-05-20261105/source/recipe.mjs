export default function build(api) {
  const structure = api.createStructure([15, 12, 37])
  const set = (across, vertical, forward, block, properties) => api.setBlock(structure, [across, vertical, forward], block, properties)
  const box = (start, end, block, properties) => api.fill(structure, start, end, block, properties)
  const leaf = {persistent:'true',distance:'7',waterlogged:'false'}
  const stone = (across, vertical, forward) => ['stone_bricks','mossy_stone_bricks','stone_bricks','cracked_stone_bricks','mossy_stone_bricks'][(across * 13 + vertical * 11 + forward * 7) % 5]
  box([0,0,0],[14,11,36],'air')
  box([0,0,0],[14,0,36],'stone_bricks')
  for (let across = 0; across < 15; across++) for (let forward = 0; forward < 37; forward++) {
    const border = across < 5 || across > 9
    const ground = (across * 11 + forward * 3) % 13
    set(across,1,forward,border && ground < 4 ? 'moss_block' : border && ground === 5 ? 'coarse_dirt' : stone(across,1,forward))
    set(across,11,forward,stone(across,11,forward))
    if (across === 0 || across === 14 || forward === 0 || forward === 36) for (let vertical = 2; vertical < 11; vertical++) set(across,vertical,forward,stone(across,vertical,forward))
  }
  for (const forward of [0,36]) {
    box([4,2,forward],[10,8,forward],'mossy_stone_bricks')
    box([5,2,forward],[9,6,forward],'air')
    box([6,7,forward],[8,7,forward],'air')
    set(7,8,forward,'air')
    box([5,1,forward],[9,1,forward],'stone_bricks')
  }
  for (const forward of [6,14,22,30]) for (const across of [1,12]) {
    box([across,2,forward],[across + 1,8,forward + 1],'mossy_stone_bricks')
    box([across,2,forward],[across + 1,2,forward + 1],'chiseled_stone_bricks')
    box([across,9,forward],[across + 1,10,forward + 1],'stone_bricks')
  }
  for (const forward of [7,15,23,31]) {
    box([1,9,forward],[13,9,forward],'dark_oak_log',{axis:'x'})
    box([4,10,forward],[10,10,forward + 1],'mossy_stone_bricks')
  }
  for (const [across,forward] of [[2,3],[2,11],[2,19],[2,27],[11,7],[11,15],[11,23],[11,31]]) {
    box([across,2,forward],[across + 1,7,forward + 1],'dark_oak_log',{axis:'y'})
    box([across,6,forward - 1],[across + 1,8,forward + 2],'dark_oak_log',{axis:'y'})
    box([across - 1,2,forward],[across + 1,2,forward + 2],'dark_oak_log',{axis:'z'})
    if (across === 2) box([1,8,forward],[4,9,forward + 1],'dark_oak_log',{axis:'x'})
    else box([10,8,forward],[13,9,forward + 1],'dark_oak_log',{axis:'x'})
  }
  box([1,9,2],[2,10,34],'dark_oak_log',{axis:'z'})
  box([12,9,2],[13,10,34],'dark_oak_log',{axis:'z'})
  const pool = (minX,minZ,maxX,maxZ) => {
    for (let across = minX; across <= maxX; across++) for (let forward = minZ; forward <= maxZ; forward++) {
      const rim = across === minX || across === maxX || forward === minZ || forward === maxZ
      set(across,1,forward,rim ? 'mossy_stone_bricks' : 'water',rim ? undefined : {level:'0'})
    }
  }
  pool(1,30,4,34)
  pool(10,18,13,22)
  for (const [across,forward] of [[1,33],[13,29],[1,17],[13,13],[1,9],[13,5]]) {
    set(across,2,forward,'mossy_stone_bricks')
    set(across,3,forward,forward % 3 === 0 ? 'potted_red_mushroom' : 'potted_brown_mushroom')
  }
  for (const across of [1,13]) for (const forward of [4,12,20,28,34]) {
    if (String(structure.getBlock([across,2,forward]).state.getName()) === 'minecraft:air') set(across,2,forward,'oak_leaves',leaf)
  }
  for (const forward of [5,13,21,29]) for (const across of [3,11]) {
    box([across,8,forward],[across,9,forward],'dark_oak_log',{axis:'x'})
    for (let drop = 0; drop < 3; drop++) set(across,7 - drop,forward,drop === 2 ? 'cave_vines' : 'cave_vines_plant',drop === 2 ? {berries:'true',age:'25'} : {berries:'true'})
    set(across,9,forward + 1,'dark_oak_log',{axis:'x'})
    set(across,8,forward + 1,'lantern',{hanging:'true',waterlogged:'false'})
  }
  for (let forward = 2; forward <= 34; forward++) for (const across of [1,2,3,11,12,13]) for (const vertical of [8,9,10]) {
    if ((across * 7 + forward * 3 + vertical) % 5 > 2 || String(structure.getBlock([across,vertical,forward]).state.getName()) !== 'minecraft:air') continue
    const connected = [[1,0,0],[-1,0,0],[0,1,0],[0,-1,0],[0,0,1],[0,0,-1]].some(([deltaX,deltaY,deltaZ]) => String(structure.getBlock([across + deltaX,vertical + deltaY,forward + deltaZ])?.state.getName() ?? 'minecraft:air') !== 'minecraft:air')
    if (connected) set(across,vertical,forward,'oak_leaves',leaf)
  }
  const distances = new Map()
  let frontier = structure.getBlocks().filter(block => String(block.state.getName()).endsWith('_log')).map(block => block.pos)
  for (let distance = 1; distance <= 6; distance++) {
    const next = []
    for (const [across,vertical,forward] of frontier) for (const [deltaX,deltaY,deltaZ] of [[1,0,0],[-1,0,0],[0,1,0],[0,-1,0],[0,0,1],[0,0,-1]]) {
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
