export default function build(api) {
  const structure = api.createStructure([15, 14, 39])
  const put = (east, up, south, block) => api.setBlock(structure, [east, up, south], `minecraft:${block}`)
  const fill = (start, end, block) => api.fill(structure, start, end, `minecraft:${block}`)
  const moss = (east, up, south) => Math.abs(Math.sin(east * 91.3 + up * 31.1 + south * 137.7) * 19731) % 1
  const masonry = (start, end) => {
    for (let east = start[0]; east <= end[0]; east++) {
      for (let up = start[1]; up <= end[1]; up++) {
        for (let south = start[2]; south <= end[2]; south++) {
          const value = moss(east, up, south)
          put(east, up, south, value < 0.65 ? 'mossy_stone_bricks' : value < 0.82 ? 'cracked_stone_bricks' : 'stone_bricks')
        }
      }
    }
  }
  fill([0, 0, 0], [14, 0, 38], 'stone')
  fill([0, 1, 0], [14, 1, 38], 'packed_mud')
  masonry([0, 2, 0], [14, 2, 38])
  masonry([0, 3, 0], [1, 10, 38])
  masonry([13, 3, 0], [14, 10, 38])
  masonry([0, 11, 0], [14, 13, 38])
  fill([2, 3, 1], [12, 3, 37], 'water[level=0]')
  for (let south = 0; south <= 38; south++) {
    const center = south < 4 || south > 34 ? 7 : Math.floor(south / 6) % 2 === 0 ? 7 : 8
    for (const east of [center - 1, center]) put(east, 3, south, 'stone_brick_slab[type=top,waterlogged=true]')
    if (south < 3 || south > 35) masonry([5, 2, south], [9, 3, south])
  }
  for (const south of [0, 38]) {
    masonry([0, 3, south], [14, 11, south])
    for (let east = 5; east <= 9; east++) {
      const top = east === 5 || east === 9 ? 7 : 9
      fill([east, 4, south], [east, top, south], 'air')
    }
    masonry([5, 2, south], [9, 3, south])
  }
  for (const south of [3, 11, 19, 27, 35]) {
    for (const east of [2, 12]) {
      fill([east, 3, south], [east, 10, south], 'dark_oak_log[axis=y]')
      put(east, 3, south - 1, 'mangrove_roots[waterlogged=true]')
      put(east, 4, south - 1, 'dark_oak_log[axis=z]')
      put(east, 4, south + 1, 'mossy_stone_bricks')
      const inside = east === 2 ? 3 : 11
      put(inside, 8, south, 'dark_oak_log[axis=x]')
      put(inside, 7, south, 'ochre_froglight[axis=y]')
      put(inside, 6, south, 'dark_oak_slab[type=top,waterlogged=false]')
      put(inside, 5, south, 'dark_oak_fence')
      const facing = east === 2 ? 'west' : 'east'
      for (let up = 4; up <= 9; up++) put(east, up, south + 2, `vine[${facing}=true]`)
    }
    fill([2, 10, south], [12, 10, south], 'dark_oak_log[axis=x]')
    for (const east of [4, 10]) {
      fill([east, 9, south + 1], [east, 10, south + 1], 'cave_vines_plant[berries=false]')
      put(east, 8, south + 1, 'cave_vines[age=25,berries=false]')
    }
  }
  for (let south = 2; south <= 36; south += 3) {
    for (const east of [2, 12]) {
      if (structure.getBlock([east, 3, south])?.state.getName().toString() === 'minecraft:water') put(east, 4, south, 'lily_pad')
    }
    for (const east of [4, 10]) {
      if (south % 2 === 0 && structure.getBlock([east, 4, south]) == null) put(east, 4, south, 'lily_pad')
    }
  }
  return { structure, metadata: { name: 's4-05-flooded-timber-corridor' } }
}
