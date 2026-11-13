export default function build(api) {
  const structure = api.createStructure([25, 13, 25])
  const put = (east, up, south, block, properties) => api.setBlock(structure, [east, up, south], `minecraft:${block}`, properties)
  const fill = (first, last, block) => api.fill(structure, first, last, `minecraft:${block}`)
  const footprint = (east, south) => east >= 1 && south <= 23 && (east <= 13 || south >= 11)
  fill([0, 0, 0], [24, 12, 24], 'air')
  for (let east = 1; east <= 24; east++) for (let south = 0; south <= 23; south++) if (footprint(east, south)) {
    put(east, 0, south, 'sandstone')
    const center = south <= 17 ? east === 7 : false
    const turn = east >= 7 && south === 17
    const border = south <= 17 && (east === 5 || east === 9) || east >= 7 && (south === 15 || south === 19)
    put(east, 1, south, (center || turn) && (east+south)%4 !== 0 || border && (east+south)%3 === 0 ? 'orange_terracotta' : (east*3+south)%19 === 0 ? 'cut_sandstone' : 'smooth_sandstone')
    put(east, 11, south, 'cut_sandstone')
  }
  for (let up = 2; up <= 10; up++) {
    const wall = up === 6 ? 'orange_terracotta' : up === 7 ? 'chiseled_sandstone' : 'sandstone'
    fill([1, up, 0], [2, up, 23], wall)
    fill([1, up, 22], [24, up, 23], wall)
    fill([11, up, 0], [13, up, 13], wall)
    fill([11, up, 11], [24, up, 13], wall)
  }
  for (const south of [2, 8]) {
    for (const east of [3, 10]) for (let up = 2; up <= 10; up++) put(east, up, south, up === 6 ? 'chiseled_red_sandstone' : up === 5 ? 'chiseled_sandstone' : 'cut_sandstone')
    fill([3,10,south], [10,10,south], 'cut_sandstone')
    put(4,9,south,'smooth_sandstone')
    put(9,9,south,'smooth_sandstone')
  }
  for (const east of [17, 22]) {
    for (const south of [14, 21]) for (let up = 2; up <= 10; up++) put(east,up,south,up === 6 ? 'chiseled_red_sandstone' : up === 5 ? 'chiseled_sandstone' : 'cut_sandstone')
    fill([east,10,14], [east,10,21], 'cut_sandstone')
    put(east,9,15,'smooth_sandstone')
    put(east,9,20,'smooth_sandstone')
  }
  fill([10,2,12], [10,10,13], 'cut_sandstone')
  fill([11,2,14], [12,4,14], 'cut_sandstone')
  fill([11,9,14], [12,10,14], 'cut_sandstone')
  put(11,6,14,'lantern',{hanging:'true',waterlogged:'false'})
  for (const up of [7,8]) put(11,up,14,'chain',{axis:'y',waterlogged:'false'})
  put(10,6,12,'chiseled_red_sandstone')
  put(12,6,14,'chiseled_red_sandstone')
  for (const [east,south] of [[7,5],[20,17]]) {
    put(east,8,south,'lantern',{hanging:'true',waterlogged:'false'})
    for (const up of [9,10]) put(east,up,south,'chain',{axis:'y',waterlogged:'false'})
  }
  for (const side of ['north','east']) for (let offset=-4; offset<=4; offset++) for (let up=2; up<=10; up++) {
    const east = side === 'north' ? 7+offset : 24
    const south = side === 'north' ? 0 : 17+offset
    const radius = up<=5 ? 2 : up<=7 ? 1 : up===8 ? 0 : -1
    put(east,up,south,Math.abs(offset)<=radius ? 'air' : up===7 ? 'orange_terracotta' : 'smooth_sandstone')
  }
  for (const [east,south] of [[9,12],[19,14],[4,5],[21,20]]) {
    put(east,2,south,'terracotta')
    put(east,3,south,'flower_pot')
  }
  for (const [east,south] of [[4,10],[12,15],[16,20],[23,15]]) {
    put(east,1,south,'sand')
    put(east,2,south,'dead_bush')
  }
  for (const [east,south] of [[10,10],[14,14],[3,7],[18,21]]) {
    fill([east,2,south],[east,3,south],'sandstone')
    put(east,4,south,'sand')
  }
  for (const [east,south] of [[9,10],[13,15],[4,7],[18,20]]) put(east,2,south,'smooth_sandstone_slab',{type:'bottom',waterlogged:'false'})
  fill([3,11,4],[4,11,6],'air')
  for (const south of [0,8,16]) put(1,12,south,'cut_sandstone')
  for (const east of [8,16,24]) put(east,12,23,'cut_sandstone')
  return {structure,metadata:{name:'room'}}
}
