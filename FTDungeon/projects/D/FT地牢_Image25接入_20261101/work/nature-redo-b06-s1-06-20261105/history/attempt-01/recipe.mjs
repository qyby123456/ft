export default function build(api) {
  const structure = api.createStructure([25,12,25])
  const set = (across,vertical,forward,block,properties) => api.setBlock(structure,[across,vertical,forward],block,properties)
  const box = (start,end,block,properties) => api.fill(structure,start,end,block,properties)
  const leaf = {persistent:'true',distance:'7',waterlogged:'false'}
  const stone = (across,vertical,forward) => ['stone_bricks','mossy_stone_bricks','stone_bricks','mossy_stone_bricks','cracked_stone_bricks'][(across * 7 + vertical * 13 + forward * 11) % 5]
  box([0,0,0],[24,11,24],'air')
  box([0,0,0],[24,0,24],'stone_bricks')
  box([0,2,0],[14,10,14],'stone_bricks')
  for (let across = 0; across < 25; across++) for (let forward = 0; forward < 25; forward++) {
    const ground = (across * 3 + forward * 11) % 17
    set(across,1,forward,ground < 3 ? 'moss_block' : ground === 6 ? 'coarse_dirt' : stone(across,1,forward))
    set(across,11,forward,'mossy_stone_bricks')
    for (let vertical = 2; vertical < 11; vertical++) {
      if (across === 0 || across === 24 || forward === 0 || forward === 24 || across === 14 && forward <= 14 || forward === 14 && across <= 14) set(across,vertical,forward,stone(across,vertical,forward))
    }
  }
  box([0,2,17],[0,6,21],'air')
  box([0,7,18],[0,7,20],'air')
  box([17,2,0],[21,6,0],'air')
  box([18,7,0],[20,7,0],'air')
  box([0,1,17],[0,1,21],'stone_bricks')
  box([17,1,0],[21,1,0],'stone_bricks')
  for (const across of [4,10]) {
    box([across,2,15],[across + 1,8,15],'mossy_stone_bricks')
    box([across,9,14],[across + 1,10,16],'stone_bricks')
    box([across,2,22],[across + 1,9,23],'mossy_stone_bricks')
  }
  for (const forward of [4,10]) {
    box([15,2,forward],[15,8,forward + 1],'mossy_stone_bricks')
    box([14,9,forward],[16,10,forward + 1],'stone_bricks')
    box([22,2,forward],[23,9,forward + 1],'mossy_stone_bricks')
  }
  box([14,2,14],[15,9,15],'mossy_stone_bricks')
  box([14,2,15],[16,2,16],'mossy_stone_bricks')
  box([14,3,15],[15,8,15],'dark_oak_log',{axis:'y'})
  box([15,3,14],[15,8,14],'dark_oak_log',{axis:'y'})
  box([13,8,14],[16,9,15],'dark_oak_log',{axis:'x'})
  box([14,8,12],[15,9,16],'dark_oak_log',{axis:'z'})
  for (const across of [2,8]) {
    box([across,2,15],[across + 1,7,16],'dark_oak_log',{axis:'y'})
    box([across,7,14],[across + 1,9,16],'dark_oak_log',{axis:'y'})
    box([across,2,16],[across + 2,2,16],'dark_oak_log',{axis:'x'})
    box([across,9,15],[across + 1,9,23],'dark_oak_log',{axis:'z'})
  }
  for (const forward of [2,8]) {
    box([15,2,forward],[16,7,forward + 1],'dark_oak_log',{axis:'y'})
    box([14,7,forward],[16,9,forward + 1],'dark_oak_log',{axis:'y'})
    box([16,2,forward],[16,2,forward + 2],'dark_oak_log',{axis:'z'})
    box([15,9,forward],[23,9,forward + 1],'dark_oak_log',{axis:'x'})
  }
  box([1,9,15],[16,10,16],'dark_oak_log',{axis:'x'})
  box([15,9,1],[16,10,16],'dark_oak_log',{axis:'z'})
  box([22,9,1],[23,10,23],'dark_oak_log',{axis:'z'})
  box([1,9,22],[23,10,23],'dark_oak_log',{axis:'x'})
  box([22,2,16],[23,8,17],'dark_oak_log',{axis:'y'})
  box([16,2,22],[17,8,23],'dark_oak_log',{axis:'y'})
  for (let across = 2; across <= 7; across++) for (let forward = 21; forward <= 24; forward++) {
    const rim = across === 2 || across === 7 || forward === 21 || forward === 24
    set(across,1,forward,rim ? 'mossy_stone_bricks' : 'water',rim ? undefined : {level:'0'})
  }
  for (const [across,forward] of [[12,16],[16,12],[23,20]]) {
    set(across,2,forward,'mossy_stone_bricks')
    set(across,3,forward,'potted_red_mushroom')
  }
  for (const [across,forward] of [[7,16],[16,7],[1,23],[10,23],[23,12],[23,2]]) {
    if (String(structure.getBlock([across,2,forward]).state.getName()) === 'minecraft:air') set(across,2,forward,'oak_leaves',leaf)
  }
  for (const [across,forward] of [[4,16],[10,16],[16,4],[16,10],[15,16],[22,16],[16,22]]) {
    set(across,9,forward,'dark_oak_log',{axis:'x'})
    for (let vertical = 8; vertical >= 6; vertical--) set(across,vertical,forward,vertical === 6 ? 'cave_vines' : 'cave_vines_plant',vertical === 6 ? {berries:'true',age:'25'} : {berries:'true'})
  }
  for (const [across,forward] of [[5,16],[16,5],[15,16],[22,15],[15,22]]) {
    set(across,10,forward,'dark_oak_log',{axis:'x'})
    set(across,9,forward,'lantern',{hanging:'true',waterlogged:'false'})
  }
  for (let across = 1; across < 24; across++) for (let forward = 1; forward < 24; forward++) for (const vertical of [8,9,10]) {
    const edge = across >= 13 && across <= 16 || forward >= 13 && forward <= 16 || across >= 22 || forward >= 22
    if (!edge || (across * 3 + forward * 7 + vertical) % 5 > 2 || String(structure.getBlock([across,vertical,forward]).state.getName()) !== 'minecraft:air') continue
    const connected = [[1,0,0],[-1,0,0],[0,1,0],[0,-1,0],[0,0,1],[0,0,-1]].some(([deltaX,deltaY,deltaZ])=>String(structure.getBlock([across + deltaX,vertical + deltaY,forward + deltaZ])?.state.getName() ?? 'minecraft:air') !== 'minecraft:air')
    if (connected) set(across,vertical,forward,'oak_leaves',leaf)
  }
  const distances = new Map()
  let frontier = structure.getBlocks().filter(block=>String(block.state.getName()).endsWith('_log')).map(block=>block.pos)
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
