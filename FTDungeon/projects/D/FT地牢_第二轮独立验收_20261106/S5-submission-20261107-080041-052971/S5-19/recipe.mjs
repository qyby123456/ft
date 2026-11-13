import {room} from '../tools.mjs'
export default function(api){
  const {put,fill,box,container,coral,finish}=room(api,[31,20,33])
  box([0,0,0],[30,17,32],'prismarine_bricks');fill([1,1,1],[29,2,31],'dark_prismarine')
  fill([1,3,1],[29,6,17],'prismarine_bricks')
  for(let step=0;step<8;step++){
    const south=25-step,height=3+Math.floor(step/2)
    fill([23,3,south],[27,height-1,south],'prismarine_bricks')
    fill([23,height,south],[27,height,south],step%2===0?'prismarine_brick_slab':'prismarine_bricks',step%2===0?{type:'bottom',waterlogged:'false'}:undefined)
  }
  fill([1,6,17],[21,6,17],'prismarine_bricks')
  for(let east=1;east<=21;east++)put([east,7,17],east%5===1?'prismarine_bricks':'prismarine_wall',east%5===1?undefined:{east:'low',west:'low',north:'none',south:'none',up:'true',waterlogged:'false'})
  fill([1,9,18],[21,15,18],'cyan_stained_glass')
  fill([2,7,1],[2,9,17],'prismarine_bricks');fill([2,10,2],[2,15,16],'cyan_stained_glass')
  fill([3,7,3],[29,7,3],'prismarine_bricks');fill([3,8,3],[29,8,3],'prismarine_wall',{east:'low',west:'low',north:'none',south:'none',up:'true',waterlogged:'false'});fill([3,9,2],[29,15,2],'cyan_stained_glass')
  fill([5,7,10],[13,7,13],'dark_oak_planks');put([8,8,11],'oak_fence');fill([7,9,11],[10,9,11],'gold_block');put([7,9,12],'black_stained_glass')
  fill([17,7,4],[25,7,5],'dark_oak_planks');for(const east of [17,25])fill([east,8,4],[east,11,4],'oak_fence');fill([17,10,4],[25,10,4],'dark_oak_planks');container([23,7,6])
  for(const east of [18,20,22,24]){put([east,8,5],'oak_fence');fill([east,9,5],[east,10,5],'iron_bars')}
  for(const east of [4,28]){fill([east,7,15],[east,10,15],'prismarine_bricks');put([east,11,15],'sea_lantern')}
  fill([4,2,23],[8,2,27],'water');coral([6,1,25],'brain')
  fill([13,7,0],[17,10,0],'air');fill([23,3,32],[27,6,32],'air')
  fill([13,7,1],[17,10,4],'air')
  fill([2,2,18],[21,15,32],'prismarine_bricks')
  fill([3,3,19],[20,14,31],'water')
  fill([3,3,18],[20,14,18],'cyan_stained_glass')
  fill([3,3,32],[20,14,32],'dark_prismarine')
  fill([3,3,19],[20,3,31],'sand');put([7,1,25],'water')
  for(const [east,south,height] of [[5,23,8],[11,27,11],[18,30,9]]){
    fill([east,4,south],[east+1,height,south+1],'mossy_stone_bricks')
    put([east,height+1,south],'sea_lantern')
  }
  fill([10,11,27],[19,12,28],'prismarine_bricks')
  for(const [east,south,color] of [[7,22,'brain'],[15,25,'fire'],[8,30,'bubble']]){coral([east,3,south],color);put([east+1,3,south],'water')}
  for(const [east,south] of [[4,25],[14,29],[19,22]]){put([east,4,south],'seagrass');put([east+1,4,south],'sea_pickle',{pickles:'3',waterlogged:'true'})}
  fill([1,17,1],[29,17,31],'cyan_stained_glass');for(const east of [3,15,28])fill([east,17,0],[east,17,32],'prismarine_bricks')
  return finish('S5-19','Raised enclosed watch balcony with telescopic sculpture, parapet, back access and eight-half-step descending stair to lower landing.',[
    {name:'south-low',feet:[25,3,32],facing:'south',width:5,height:4},{name:'north-high',feet:[15,7,0],facing:'north',width:5,height:4}
  ],[{label:'upper-landing',position:[25,7,16]},{label:'watch-desk',position:[10,7,14]},{label:'weapon-rack',position:[21,7,7]},{label:'observation',position:[6,7,7]}],[
    'Packaged enclosed 13-block-deep aquarium ruins behind the viewing glass; stepped ruined pillars, arch, live coral and seagrass are actual blocks in this NBT. Stair compartment stays dry.',
    'Telescope is gold/glass block sculpture, not a usable spyglass.',
    'Real four-block floor-height difference is traversed by 0.5-height slab steps; checked with native body collision.',
    'Upper platform dry; lower landing contains isolated shallow coral pool.'
  ],{cutHeight:17})
}
