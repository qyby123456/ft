export default function build(api) {
  const structure = api.createStructure([13, 11, 16])
  const set = (pos, block, properties) => api.setBlock(structure, pos, `minecraft:${block}`, properties)
  const fill = (from, to, block, properties) => api.fill(structure, from, to, `minecraft:${block}`, properties)
  const copper = ['waxed_cut_copper', 'waxed_cut_copper', 'waxed_copper_block', 'waxed_exposed_cut_copper', 'waxed_oxidized_cut_copper']
  const masonry = (posX, posY, posZ) => {
    const variant = (posX * 19 + posY * 7 + posZ * 11) % 13
    return variant === 0 ? 'cracked_stone_bricks' : variant < 3 ? 'mossy_stone_bricks' : 'stone_bricks'
  }
  fill([0, 0, 0], [12, 10, 15], 'air')
  fill([0, 0, 0], [12, 0, 15], 'polished_deepslate')
  for (let posX = 0; posX < 13; posX += 1) {
    for (let posZ = 0; posZ < 16; posZ += 1) {
      set([posX, 1, posZ], copper[(posX * 3 + posZ * 2) % copper.length])
      set([posX, 10, posZ], posX === 0 || posX === 12 ? 'stone_bricks' : copper[(posX + posZ * 3) % copper.length])
    }
  }
  for (const posX of [0, 1, 11, 12]) {
    for (let posY = 2; posY < 10; posY += 1) {
      for (let posZ = 0; posZ < 16; posZ += 1) {
        const panel = (Math.floor(posZ / 3) + posY + posX) % 5
        set([posX, posY, posZ], posY === 2 || posY === 8 ? 'waxed_cut_copper' : panel < 3 ? 'waxed_oxidized_cut_copper' : 'waxed_exposed_cut_copper')
      }
    }
  }
  for (const posX of [3, 9]) {
    for (let posZ = 1; posZ < 15; posZ += 1) {
      if ([5, 10].includes(posZ)) continue
      set([posX, 0, posZ], 'waxed_oxidized_cut_copper')
      set([posX, 1, posZ], 'iron_trapdoor', {facing: 'north', half: 'top', open: 'false', powered: 'false', waterlogged: 'false'})
    }
  }
  for (const posZ of [0, 5, 10, 15]) {
    for (const posX of [2, 10]) {
      for (let posY = 2; posY <= 7; posY += 1) set([posX, posY, posZ], masonry(posX, posY, posZ))
      set([posX, 2, posZ], 'chiseled_stone_bricks')
    }
    for (let posX = 2; posX <= 10; posX += 1) set([posX, 9, posZ], masonry(posX, 9, posZ))
    for (const [posX, facing] of [[3, 'east'], [9, 'west']]) {
      if (!(posZ === 10 && posX === 9)) {
        set([posX, 7, posZ], 'stone_brick_stairs', {facing, half: 'top', shape: 'straight', waterlogged: 'false'})
      }
      set([posX, 8, posZ], masonry(posX, 8, posZ))
    }
    for (const [posX, facing] of [[4, 'east'], [8, 'west']]) {
      if (!(posZ === 5 && posX === 4)) set([posX, 8, posZ], 'stone_brick_stairs', {facing, half: 'top', shape: 'straight', waterlogged: 'false'})
    }
    fill([5, 8, posZ], [7, 8, posZ], 'stone_bricks')
    for (const posX of [0, 1, 11, 12]) fill([posX, 2, posZ], [posX, 9, posZ], 'stone_bricks')
  }
  for (const posX of [2, 10]) {
    const facing = posX === 2 ? 'east' : 'west'
    const buttonX = posX === 2 ? 3 : 9
    for (const posZ of [2, 7, 12]) {
      set([posX, 2, posZ], 'waxed_cut_copper')
      for (let posY = 3; posY <= 7; posY += 1) set([posX, posY, posZ], 'lightning_rod', {facing: 'up', powered: 'false', waterlogged: 'false'})
      set([posX, 4, posZ], 'gold_block')
      set([posX, 5, posZ], 'target')
      set([buttonX, 5, posZ], 'stone_button', {face: 'wall', facing, powered: 'false'})
      set([posX, 7, posZ], 'waxed_cut_copper')
      set([posX, 8, posZ], 'gold_block')
      set([posX, 9, posZ], 'waxed_oxidized_cut_copper')
    }
  }
  for (const posZ of [2, 7, 12]) {
    set([6, 9, posZ], 'sea_lantern')
    set([6, 8, posZ], 'end_rod', {facing: 'down'})
    for (const posX of [5, 7]) set([posX, 9, posZ], 'waxed_oxidized_cut_copper')
  }
  for (const [posX, posZ] of [[2, 4], [2, 9], [10, 6], [10, 13]]) {
    set([posX, 2, posZ], 'cobblestone_slab', {type: 'bottom', waterlogged: 'false'})
    const nextZ = posZ === 13 ? 14 : posZ + 1
    if (![5, 10].includes(nextZ)) set([posX, 2, nextZ], 'stone_brick_slab', {type: 'bottom', waterlogged: 'false'})
  }
  for (const posZ of [0, 15]) {
    fill([3, 2, posZ], [3, 6, posZ], 'chiseled_stone_bricks')
    fill([9, 2, posZ], [9, 6, posZ], 'chiseled_stone_bricks')
    fill([4, 7, posZ], [8, 7, posZ], 'stone_bricks')
    fill([4, 1, posZ], [8, 1, posZ], 'waxed_cut_copper')
  }
  return {structure, metadata: {name: 'room'}}
}
