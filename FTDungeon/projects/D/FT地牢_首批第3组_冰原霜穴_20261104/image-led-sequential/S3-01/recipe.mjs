export default function build(api) {
  const structure = api.createStructure([23, 14, 23])
  const put = (across, elevation, along, block, properties) => api.setBlock(structure, [across, elevation, along], 'minecraft:' + block, properties)
  const box = (start, end, block, properties) => api.fill(structure, start, end, 'minecraft:' + block, properties)
  box([0, 0, 0], [22, 13, 22], 'air')
  for (let across = 0; across <= 22; across++) for (let along = 0; along <= 22; along++) {
    const edge = Math.min(across, along, 22 - across, 22 - along)
    const distance = Math.max(Math.abs(across - 11), Math.abs(along - 12))
    let floor = distance >= 4 && distance <= 5 ? 'blue_ice' : (across * 7 + along * 3) % 11 < 3 ? 'snow_block' : 'stone_bricks'
    if (Math.abs(across - 11) <= 1 || Math.abs(along - 12) <= 1) floor = 'polished_andesite'
    put(across, 0, along, floor)
    const ceiling = 10 + Math.min(2, edge)
    put(across, ceiling, along, 'stone_bricks')
    if (edge <= 2 || (across + along) % 4 === 0) put(across, ceiling + 1, along, 'snow_block')
    if (edge === 0) for (let elevation = 1; elevation < ceiling; elevation++) {
      put(across, elevation, along, (across * 5 + along * 11) % 13 < 2 ? 'packed_ice' : (across + along + elevation) % 7 === 0 ? 'cracked_stone_bricks' : 'stone_bricks')
    }
  }
  for (const across of [1, 21]) for (const along of [4, 18]) {
    box([across, 1, along], [across, 9, along], 'spruce_log', { axis: 'y' })
    for (const elevation of [1, 4, 8]) put(across, elevation, along, 'chiseled_stone_bricks')
  }
  for (const across of [3, 19]) for (const along of [5, 19]) {
    box([across, 1, along], [across, 10, along], 'stone_bricks')
    box([across, 3, along], [across, 8, along], 'spruce_log', { axis: 'y' })
    for (const elevation of [2, 6, 9]) put(across, elevation, along, 'snow_block')
  }
  for (const across of [1, 21]) {
    box([across, 7, 3], [across, 7, 19], 'stone_bricks')
    box([across, 8, 3], [across, 8, 19], 'stone_brick_slab', { type: 'bottom', waterlogged: 'false' })
    for (const along of [3, 8, 16, 20]) {
      box([across, 6, along], [across, 6, along], 'chiseled_stone_bricks')
    }
  }
  for (const across of [3, 6, 10, 16, 19]) for (const along of [2, 21]) {
    const length = 1 + (across + along) % 3
    const ceiling = 10 + Math.min(2,across,along,22-across,22-along)
    box([across, ceiling - length, along], [across, ceiling - 1, along], 'packed_ice')
  }
  for (const across of [2, 20]) for (const along of [6, 9, 16]) {
    box([across, 8, along], [across, 11, along], 'packed_ice')
  }
  box([8, 1, 9], [14, 1, 15], 'stone_bricks')
  box([9, 2, 10], [13, 2, 14], 'snow_block')
  box([10, 3, 11], [12, 3, 13], 'blue_ice')
  for (const [across, along, peak] of [[11,12,8],[9,12,6],[13,13,5],[11,14,4],[10,10,5]]) {
    box([across, 3, along], [across, peak, along], 'packed_ice')
    put(across, peak, along, 'blue_ice')
  }
  for (const [across, along, peak] of [[3,3,3],[19,3,4],[4,15,3],[19,18,3],[6,20,2]]) {
    put(across, 1, along, 'snow_block')
    box([across, 2, along], [across, peak, along], 'packed_ice')
  }
  for (const along of [4,6,8]) {
    box([4, 1, along], [4, 2, along], 'bone_block', { axis: 'y' })
    put(5, 3, along, 'bone_block', { axis: 'x' })
    put(6, 2, along, 'bone_block', { axis: 'x' })
    put(7, 1, along, 'bone_block', { axis: 'y' })
    put(4, 3, along, 'bone_block', { axis: 'y' })
    put(6, 3, along, 'bone_block', { axis: 'x' })
  }
  for (const across of [4,18]) for (const along of [7,18]) {
    box([across, 8, along], [across, 11, along], 'chain', { axis: 'y', waterlogged: 'false' })
    put(across, 7, along, 'soul_lantern', { hanging: 'true', waterlogged: 'false' })
  }
  const north = () => {
    box([10,1,0],[12,4,0],'air')
    for (const across of [9,13]) box([across,1,0],[across,5,0],'chiseled_stone_bricks')
    box([9,5,0],[13,5,0],'stone_bricks')
  }
  north()
  for (const across of [0,22]) {
    box([across,1,11],[across,4,13],'air')
    for (const along of [10,14]) box([across,1,along],[across,5,along],'chiseled_stone_bricks')
    box([across,5,10],[across,5,14],'stone_bricks')
  }
  return { structure, metadata: { name: 'room' } }
}
