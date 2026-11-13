export default function build(api) {
  const structure = api.createStructure([27,17,29])
  const put = (east,up,south,block,properties,entity) => api.setBlock(structure,[east,up,south],`minecraft:${block}`,properties,entity)
  const fill = (first,last,block) => api.fill(structure,first,last,`minecraft:${block}`)
  fill([0,0,0],[26,16,28],'air')
  fill([0,0,4],[26,2,23],'sandstone')
  fill([9,0,0],[16,0,3],'cut_sandstone')
  for (let east=11; east<=14; east++) {
    put(east,1,1,'smooth_sandstone_stairs',{facing:'south',half:'bottom',shape:'straight',waterlogged:'false'})
    put(east,2,2,'smooth_sandstone_stairs',{facing:'south',half:'bottom',shape:'straight',waterlogged:'false'})
  }
  fill([11,1,2],[14,1,3],'sandstone')
  fill([11,2,3],[14,2,3],'smooth_sandstone')
  for (let east=0; east<=26; east++) for (let south=4; south<=23; south++) {
    const distance=Math.abs(east-13)+Math.abs(south-12)
    const inlay=distance===4 || distance<=1 || (east===11 || east===15) && south>=6 && south<=18
    put(east,2,south,inlay ? 'orange_terracotta' : (east+south*3)%17===0 ? 'cut_sandstone' : 'smooth_sandstone')
  }
  for (let up=3; up<=7; up++) {
    const wall=up===5 ? 'orange_terracotta' : up===6 ? 'chiseled_sandstone' : 'sandstone'
    fill([0,up,4],[1,up,23],wall)
    fill([25,up,4],[26,up,23],wall)
    fill([0,up,4],[11,up,4],wall)
    fill([14,up,4],[26,up,4],wall)
  }
  for (const east of [10,14]) {
    fill([east,3,3],[east+1,7,5],'cut_sandstone')
    fill([east,7,3],[east+1,7,5],'chiseled_sandstone')
    put(east,8,4,'smooth_sandstone_slab',{type:'bottom',waterlogged:'false'})
  }
  for (const [east,hinge] of [[12,'left'],[13,'right']]) for (const [up,half] of [[3,'lower'],[4,'upper']]) put(east,up,4,'dark_oak_door',{facing:'north',half,hinge,open:'false',powered:'false'})
  fill([0,3,20],[26,11,23],'sandstone')
  fill([0,7,20],[26,7,23],'orange_terracotta')
  fill([0,8,20],[26,8,23],'chiseled_sandstone')
  for (const first of [3,19]) fill([first,3,19],[first+4,7,20],'air')
  for (const east of [1,24]) {
    fill([east,3,18],[east+1,12,22],'cut_sandstone')
    fill([east,7,18],[east+1,8,22],'orange_terracotta')
    fill([east,12,18],[east+1,12,22],'chiseled_sandstone')
  }
  fill([9,0,20],[17,2,28],'sandstone')
  for (let south=20; south<=28; south++) {
    fill([9,3,south],[10,12,south],'cut_sandstone')
    fill([16,3,south],[17,12,south],'cut_sandstone')
    for (let east=11; east<=15; east++) put(east,2,south,east===13 && south%3===0 ? 'orange_terracotta' : 'smooth_sandstone')
    fill([9,12,south],[17,12,south],'cut_sandstone')
    if (south<=23) for (let east=11; east<=15; east++) for (let up=3; up<=11; up++) {
      const radius=up<=8 ? 2 : up===9 ? 1 : up===10 ? 0 : -1
      put(east,up,south,Math.abs(east-13)<=radius ? 'air' : 'smooth_sandstone')
    }
  }
  for (const east of [9,16]) {
    fill([east,3,19],[east+1,14,23],'cut_sandstone')
    fill([east,8,19],[east+1,10,19],'orange_terracotta')
    put(east,9,18,'chiseled_red_sandstone')
    fill([east-1,13,19],[east+2,14,23],'chiseled_sandstone')
    fill([east-1,15,19],[east+2,15,23],'cut_sandstone')
  }
  fill([9,13,21],[17,14,23],'cut_sandstone')
  for (const [east,south] of [[5,20],[21,20]]) {
    put(east,7,south,'lantern',{hanging:'true',waterlogged:'false'})
    put(east,8,south,'chain',{axis:'y',waterlogged:'false'})
  }
  put(13,9,24,'lantern',{hanging:'true',waterlogged:'false'})
  for (const up of [10,11]) put(13,up,24,'chain',{axis:'y',waterlogged:'false'})
  for (const [east,up] of [[4,3],[5,3],[4,4],[21,3],[22,3],[22,4]]) put(east,up,20,'barrel',{facing:'north',open:'false'},api.createBlockNbt({id:'minecraft:barrel'}))
  fill([18,5,18],[20,9,19],'sandstone')
  fill([18,6,17],[20,8,17],'dark_oak_planks')
  put(19,7,17,'birch_planks')
  fill([18,3,14],[20,3,16],'cut_sandstone')
  put(19,4,15,'campfire',{facing:'north',lit:'true',signal_fire:'false',waterlogged:'false'},api.createBlockNbt({id:'minecraft:campfire'}))
  for (const [east,south] of [[8,18],[18,18],[21,8],[5,9]]) {
    put(east,3,south,'terracotta')
    put(east,4,south,'flower_pot')
  }
  for (const [east,south] of [[3,7],[23,7],[3,15],[23,15]]) {
    put(east,2,south,'sand')
    put(east,3,south,'dead_bush')
  }
  for (const east of [9,17]) for (let up=10; up<=13; up++) put(east,up,18,'vine',{north:'false',east:'false',south:'true',west:'false',up:'false'})
  return {structure,metadata:{name:'room'}}
}
