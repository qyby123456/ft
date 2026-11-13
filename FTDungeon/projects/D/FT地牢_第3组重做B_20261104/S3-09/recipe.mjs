export const size = [37,25,37]

export default function build(api) {
  const structure=api.createStructure(size)
  const set=(east,height,south,block)=>api.setBlock(structure,[east,height,south],`minecraft:${block}`)
  const box=(from,to,block)=>api.fill(structure,from,to,`minecraft:${block}`)
  const noise=(east,height,south)=>(east*71+height*43+south*137+east*south*17)%101
  box([0,0,0],[36,24,36],'air')
  box([0,0,0],[36,4,36],'stone_bricks')
  for(let east=0;east<37;east++) for(let south=0;south<37;south++) {
    const patch=Math.sin(east*0.6+south*0.23)+Math.cos(south*0.43-east*0.21)
    set(east,4,south,patch>0.7?'packed_ice':patch< -0.6?'snow_block':'stone_bricks')
    for(let height=5;height<25;height++) if(east<3||east>33||south<3||south>33||height>=23) set(east,height,south,noise(east,height,south)<17?'cobblestone':noise(east,height,south)<34?'cracked_stone_bricks':'stone_bricks')
  }
  box([4,4,26],[32,4,31],'air')
  box([4,3,26],[32,3,31],'blue_ice')
  box([26,4,25],[30,4,32],'spruce_planks')
  box([25,5,25],[25,5,32],'spruce_fence[waterlogged=false]')
  box([31,5,25],[31,5,32],'spruce_fence[waterlogged=false]')
  box([14,5,6],[22,7,14],'stone_bricks')
  box([13,5,7],[23,6,13],'stone_bricks')
  for(const south of [15,16,17]) {
    const height=22-south
    box([16,5,south],[20,height-1,south],'stone_bricks')
    box([16,height,south],[20,height,south],'stone_brick_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]')
  }
  box([15,7,8],[21,7,13],'polished_andesite')
  api.setBlock(structure,[18,8,10],'minecraft:chest[facing=south,type=single,waterlogged=false]',undefined,api.createBlockNbt({id:'minecraft:chest',Items:[]}))
  for(const east of [11,25]) {
    box([east-2,5,10],[east+2,6,14],'stone_bricks')
    box([east-1,7,11],[east+1,7,13],'polished_andesite')
    for(const leg of [east-1,east+1]) box([leg,8,12],[leg,9,12],'bone_block[axis=y]')
    box([east-1,10,12],[east+1,10,12],'bone_block[axis=x]')
    box([east,11,12],[east,12,12],'bone_block[axis=y]')
    box([east-2,12,12],[east+2,12,12],'bone_block[axis=x]')
    box([east-2,10,12],[east-2,11,12],'bone_block[axis=y]')
    box([east+2,10,12],[east+2,11,12],'bone_block[axis=y]')
    box([east-1,13,11],[east+1,14,13],'bone_block[axis=y]')
    for(const eye of [east-1,east+1])set(eye,14,14,'polished_blackstone_button[face=wall,facing=south,powered=false]')
    set(east,13,14,'polished_blackstone_button[face=wall,facing=south,powered=false]')
  }
  const banner=api.createBlockNbt({id:'minecraft:banner',Patterns:[{Pattern:'flo',Color:0}]})
  for(const east of [7,18,29]) {
    box([east-1,10,3],[east+1,20,3],'chiseled_stone_bricks')
    api.setBlock(structure,[east,16,4],'minecraft:blue_wall_banner[facing=south]',undefined,banner)
    box([east-1,19,4],[east+1,19,4],'spruce_log[axis=x]')
  }
  for(const east of [14,22]) {
    box([east,9,4],[east,20,4],'spruce_log[axis=y]')
    box([east-1,20,4],[east+1,20,5],'spruce_log[axis=x]')
    set(east,19,5,'chain[axis=y,waterlogged=false]')
    set(east,18,5,'lantern[hanging=true,waterlogged=false]')
  }
  for(const east of [3,33]) {
    for(const south of [12,20])box([east,5,south],[east,14,south],'spruce_log[axis=y]')
    box([east,14,12],[east,14,20],'spruce_log[axis=z]')
    const inner=east===3?4:32
    set(inner,14,16,'spruce_log[axis=x]')
    set(inner,13,16,'chain[axis=y,waterlogged=false]')
    set(inner,12,16,'lantern[hanging=true,waterlogged=false]')
  }
  for(const east of [0,1,2,34,35,36]) {
    box([east,5,14],[east,10,18],'air')
    box([east,11,15],[east,11,17],'air')
  }
  box([27,5,34],[31,8,36],'air')
  box([27,4,34],[31,4,36],'stone_bricks')
  for(const [east,south] of [[5,8],[7,8],[6,10],[30,9],[30,11],[7,19],[30,20]]) {
    set(east,5,south,'spruce_planks')
    set(east,6,south,'spruce_slab[type=bottom,waterlogged=false]')
  }
  for(const [east,south] of [[6,8],[30,9]]) {
    set(east,5,south,'spruce_planks')
    set(east,6,south,'spruce_planks')
    box([east,7,south],[east,8,south],'orange_terracotta')
    set(east,9,south,'flower_pot')
  }
  for(const [east,south,top] of [[5,25,10],[9,30,9],[22,32,10],[32,30,8],[6,5,12],[31,5,12]]) {
    box([east-1,4,south],[east+1,4,south],'packed_ice')
    box([east,5,south],[east,top,south],'packed_ice')
    box([east-1,5,south],[east+1,6,south],'packed_ice')
    set(east,top+1,south,'blue_ice')
  }
  for(const east of [5,12,23,31]) for(const south of [5,19,30]) {
    box([east,21,south],[east,23,south],'packed_ice')
    box([east,18+south%2,south],[east,20,south],'blue_ice')
  }
  for(const east of [8,12,23,32]) for(const south of [22,33])set(east,5,south,'snow_block')
  for(const south of [33,34]) {
    set(11,5,south,'bone_block[axis=z]')
    set(12,5,south,'bone_block[axis=x]')
  }
  return {structure,metadata:{name:'room'}}
}
