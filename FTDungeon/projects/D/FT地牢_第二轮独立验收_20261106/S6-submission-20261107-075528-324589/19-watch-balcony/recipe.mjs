import {room} from '../lib.mjs'
function buildBalcony(api) {
  const build=room(api,[31,20,31],'s6-19-watch-balcony')
  const {put,fill,pillar,light,chest,port,lava}=build
  fill([4,3,4],[26,6,12],'polished_blackstone_bricks')
  for(const east of [4,26]) fill([east,7,4],[east,8,12],'polished_blackstone_bricks')
  fill([4,7,12],[19,8,12],'polished_blackstone_bricks')
  fill([24,7,12],[26,8,12],'polished_blackstone_bricks')
  fill([5,7,3],[25,12,3],'orange_stained_glass')
  lava([5,8,1],[25,11,1])
  fill([5,8,2],[25,11,2],'orange_stained_glass')
  for(let south=13;south<=20;south++) {
    const step=20-south
    const height=3+Math.floor(step/2)
    if(height>3) fill([20,3,south],[23,height-1,south],'polished_blackstone_bricks')
    for(let east=20;east<=23;east++) put(east,height,south,'polished_blackstone_brick_slab',{type:step%2===0?'bottom':'top',waterlogged:'false'})
  }
  for(const east of [19,24]) fill([east,3,13],[east,9,20],'polished_blackstone_bricks')
  fill([8,7,7],[13,7,8],'polished_blackstone_bricks')
  put(10,8,7,'lightning_rod',{facing:'up',powered:'false',waterlogged:'false'})
  put(10,9,7,'lightning_rod',{facing:'west',powered:'false',waterlogged:'false'})
  put(12,8,7,'lantern',{hanging:'false'})
  fill([16,7,5],[21,7,5],'polished_blackstone_bricks')
  for(const east of [16,18,20]) fill([east,8,5],[east,10,5],'iron_bars')
  chest(25,7,6)
  for(const east of [3,27]) for(const south of [4,27]) pillar(east,south)
  light(9,18)
  light(26,23)
  for(let east=5;east<=25;east++) for(let up=8;up<=11;up++) {
    if(east%5!==0) {
      put(east,up,1,(east+up)%3===0?'smooth_basalt':'basalt')
      put(east,up,2,(east*3+up)%4===0?'gilded_blackstone':'blackstone')
    }
  }
  fill([5,7,3],[25,12,3],'glass')
  for(const east of [6,14,22]) {
    fill([east,7,3],[east,13,3],'polished_blackstone_bricks')
    put(east,11,3,'chiseled_polished_blackstone')
  }
  fill([5,13,3],[25,14,3],'polished_blackstone_bricks')
  for(let east=5;east<=25;east++) put(east,15,3,'polished_blackstone_brick_slab',{type:'bottom',waterlogged:'false'})
  put(10,9,7,'copper_block')
  put(9,9,7,'lightning_rod',{facing:'west',powered:'false',waterlogged:'false'})
  put(8,9,7,'end_rod',{facing:'west'})
  for(const east of [6,14]) fill([east,3,12],[east,6,12],'chiseled_polished_blackstone')
  fill([6,6,12],[14,6,12],'polished_blackstone_bricks')
  put(7,5,12,'polished_blackstone_brick_stairs',{facing:'east',half:'top',shape:'straight',waterlogged:'false'})
  put(13,5,12,'polished_blackstone_brick_stairs',{facing:'west',half:'top',shape:'straight',waterlogged:'false'})
  for(const east of [1,29]) {
    for(const south of [22,28]) fill([east,3,south],[east,10,south],'polished_blackstone_bricks')
    fill([east,10,22],[east,11,28],'polished_blackstone_bricks')
  }
  port('west',25)
  port('east',25)
  return build.finish()
}
export default function build(api) {
  const original=buildBalcony(api).structure
  const structure=api.createStructure([31,34,75])
  api.fill(structure,[0,0,0],[30,33,74],'minecraft:air')
  const put=(east,up,south,block)=>api.setBlock(structure,[east,up,south],`minecraft:${block}`,block.includes('basalt')?{axis:'y'}:undefined)
  const fill=(from,to,block)=>api.fill(structure,from,to,`minecraft:${block}`,block==='basalt'?{axis:'y'}:undefined)
  fill([0,0,0],[30,0,44],'blackstone')
  fill([0,1,0],[0,33,44],'basalt')
  fill([30,1,0],[30,33,44],'basalt')
  fill([0,1,0],[30,33,0],'blackstone')
  fill([0,33,0],[30,33,44],'blackstone')
  for(let east=1;east<30;east++) for(let south=1;south<44;south++) {
    const edge=Math.min(east,30-east)
    const terrace=edge<3?15+Math.floor(4*Math.sin(south/6)):edge<6?5+Math.floor(3*Math.sin(south/4)):0
    const far=south<6?11+Math.floor(7*(1+Math.sin(east/3))):0
    const height=Math.max(terrace,far)
    if(height>0) fill([east,1,south],[east,height,south],east%3===0?'basalt':'blackstone')
  }
  for(const [east,south,height] of [[8,10,25],[22,14,21],[6,29,19],[25,34,23],[18,4,28]]) {
    fill([east-1,1,south-1],[east+1,height,south+1],'basalt')
    fill([east,1,south],[east,height+2,south],'obsidian')
  }
  const sealedLava=(from,to)=>{
    fill(from.map(value=>value-1),to.map(value=>value+1),'obsidian')
    fill(from,to,'lava')
    fill([from[0],to[1]+1,from[2]],[to[0],to[1]+1,to[2]],'orange_stained_glass')
  }
  sealedLava([11,1,8],[12,1,37])
  sealedLava([18,1,12],[19,1,30])
  sealedLava([15,3,4],[15,23,4])
  fill([15,3,5],[15,23,5],'orange_stained_glass')
  fill([6,9,20],[24,10,21],'polished_blackstone_bricks')
  for(const east of [7,23]) fill([east,1,20],[east,8,21],'polished_blackstone_bricks')
  for(const block of original.getBlocks()) structure.addBlock([block.pos[0],block.pos[1],block.pos[2]+44],block.state.getName(),block.state.getProperties(),block.nbt)
  for(let east=5;east<=25;east++) for(let up=7;up<=12;up++) for(let south=44;south<=46;south++) put(east,up,south,'air')
  return {structure,metadata:{name:'s6-19-watch-balcony'}}
}
