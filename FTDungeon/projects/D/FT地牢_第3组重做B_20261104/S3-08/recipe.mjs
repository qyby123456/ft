export const size = [43, 29, 43]
export const covered = (east, south) => east <= 9 || east >= 33 || south <= 9

export default function build(api) {
  const structure = api.createStructure(size)
  const set = (east, height, south, block) => api.setBlock(structure, [east, height, south], `minecraft:${block}`)
  const box = (from, to, block) => api.fill(structure, from, to, `minecraft:${block}`)
  const noise = (east, height, south) => (east * 71 + height * 43 + south * 137 + east * south * 17) % 101
  box([0,0,0], [42,28,42], 'air')
  box([0,0,0], [42,4,42], 'stone_bricks')
  for (let east = 0; east < 43; east++) for (let south = 0; south < 43; south++) {
    const patch = Math.sin(east * 0.53 + south * 0.25) + Math.cos(south * 0.4 - east * 0.12)
    set(east,4,south,patch > 0.4 ? 'packed_ice' : patch < -0.2 ? 'snow_block' : 'stone_bricks')
    if (east < 3 || east > 39 || south < 3) {
      const top = 24 + noise(east,5,south) % 3
      for (let height = 5; height <= top; height++) set(east,height,south,noise(east,height,south)<20?'cobblestone':noise(east,height,south)<36?'cracked_stone_bricks':'stone_bricks')
      set(east,top+1,south,'snow_block')
    }
    if (covered(east,south)) {
      set(east,13,south,'stone_bricks')
      set(east,14,south, 'air')
      box([east,26,south],[east,27,south],'stone_bricks')
      set(east,28,south,'snow_block')
    }
  }
  for (const east of [4,9,33,38]) for (const south of [5,13,29,37]) {
    box([east,5,south],[east,12,south],'spruce_log[axis=y]')
    box([east-1,5,south-1],[east+1,6,south+1],'stone_bricks')
    box([east-1,12,south-1],[east+1,12,south+1],'stone_bricks')
  }
  for (let south = 3; south <= 39; south++) {
    if (south <= 9) continue
    if (south >= 20 && south <= 30) continue
    set(9,14,south,'spruce_fence[waterlogged=false]')
    set(33,14,south,'spruce_fence[waterlogged=false]')
  }
  for (let east = 10; east <= 32; east++) set(east,14,9,'spruce_fence[waterlogged=false]')
  box([33,14,20],[33,14,30],'spruce_fence[waterlogged=false]')
  box([3,14,42],[9,14,42],'spruce_fence[waterlogged=false]')
  box([33,14,42],[39,14,42],'spruce_fence[waterlogged=false]')
  for (const south of [3,10,17,24,31,38]) for (const east of [4,38]) {
    box([east,14,south],[east,23,south],'spruce_log[axis=y]')
    const inner = east === 4 ? 6 : 36
    box([Math.min(east,inner),23,south],[Math.max(east,inner),23,south],'spruce_log[axis=x]')
    set(inner,22,south,'chain[axis=y,waterlogged=false]')
    set(inner,21,south,'lantern[hanging=true,waterlogged=false]')
  }
  for (const east of [14,21,28]) {
    box([east,23,3],[east,23,5],'spruce_log[axis=z]')
    set(east,22,5,'chain[axis=y,waterlogged=false]')
    set(east,21,5,'lantern[hanging=true,waterlogged=false]')
  }
  for (let south=21;south<=29;south++) {
    const height=34-south
    box([5,5,south],[8,height-1,south],'stone_bricks')
    box([5,height,south],[8,height,south],'stone_brick_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]')
  }
  box([5,13,19],[8,13,20],'stone_bricks')
  box([5,14,19],[8,16,20],'air')
  box([5,13,22],[8,13,29],'air')
  box([9,14,19],[9,14,29],'air')
  box([10,5,30],[11,6,31],'stone_bricks')
  box([23,4,23],[32,4,33],'air')
  box([23,3,23],[32,3,33],'blue_ice')
  box([22,4,22],[33,4,22],'snow_block')
  box([22,4,34],[33,4,34],'snow_block')
  box([22,4,23],[22,4,33],'stone_bricks')
  box([33,4,23],[33,4,33],'stone_bricks')
  for (let east=24;east<=31;east++) set(east,3,33,'stone_brick_stairs[facing=south,half=bottom,shape=straight,waterlogged=false]')
  for (let east=24;east<=31;east++) set(east,4,34,'stone_brick_stairs[facing=south,half=bottom,shape=straight,waterlogged=false]')
  box([12,5,12],[23,6,22],'stone_bricks')
  box([13,7,13],[22,7,21],'snow_block')
  box([16,8,16],[18,19,18],'spruce_log[axis=y]')
  for (const [height,radius] of [[12,7],[16,5],[20,3],[23,1]]) {
    const centerEast=height===16?18:height===20?16:17
    const centerSouth=height===16?16:17
    for (let east=centerEast-radius;east<=centerEast+radius;east++) for (let south=centerSouth-radius;south<=centerSouth+radius;south++) if (Math.abs(east-centerEast)+Math.abs(south-centerSouth)<=radius+2 && !(Math.abs(east-centerEast)>radius-1 && noise(east,height,south)<25)) {
      if (east>=16&&east<=18&&south>=16&&south<=18&&height<=19) continue
      set(east,height,south,'spruce_leaves[persistent=true,distance=1,waterlogged=false]')
      if (noise(east,height,south)<58) set(east,height+1,south,'snow_block')
      if (noise(east,height,south)<32) set(east,height-1,south,'spruce_leaves[persistent=true,distance=1,waterlogged=false]')
    }
    if (height>19) box([17,19,17],[17,height,17],'spruce_log[axis=y]')
  }
  for (const [east,south] of [[13,17],[21,17],[17,13],[17,21]]) box([Math.min(17,east),11,Math.min(17,south)],[Math.max(17,east),11,Math.max(17,south)],'spruce_log[axis=x]')
  for (const [east,south,height] of [[29,14,15],[30,15,11],[28,13,10],[14,29,7],[36,36,8],[6,35,6]]) {
    box([east,5,south],[east,height,south],'packed_ice')
    box([east-1,5,south],[east+1,7,south],'packed_ice')
    set(east,height+1,south,'blue_ice')
  }
  for (const east of [5,9,34,38]) for (const south of [6,16,28,36]) {
    box([east,24,south],[east,26,south],'packed_ice')
    box([east,21,south],[east,23,south],'blue_ice')
  }
  for (const [east,south] of [[6,11],[35,8],[12,36],[38,32],[25,9]]) {
    box([east,5,south],[east+2,6,south+2],'spruce_leaves[persistent=true,distance=1,waterlogged=false]')
    box([east,7,south],[east+2,7,south+2],'snow_block')
  }
  for (const south of [25,27,29]) {
    box([36,5,south],[39,5,south],'bone_block[axis=x]')
    box([36,6,south],[36,7,south],'bone_block[axis=y]')
    box([39,6,south],[39,7,south],'bone_block[axis=y]')
    box([37,8,south],[38,8,south],'bone_block[axis=x]')
    box([37,7,south],[38,7,south],'bone_block[axis=x]')
  }
  for (const east of [0,1,2,40,41,42]) {
    box([east,5,19],[east,10,23],'air')
    box([east,10,20],[east,11,22],'air')
  }
  box([19,5,40],[23,8,42],'air')
  box([19,4,40],[23,4,42],'stone_bricks')
  for (let east=0;east<43;east++) if(east<19||east>23) {
    box([east,5,41],[east,6,42],'stone_bricks')
    box([east,7,41],[east,7,42],'snow_block')
  }
  box([19,14,0],[23,18,2],'air')
  box([19,13,0],[23,13,2],'stone_bricks')
  return { structure, metadata: { name:'room' } }
}
