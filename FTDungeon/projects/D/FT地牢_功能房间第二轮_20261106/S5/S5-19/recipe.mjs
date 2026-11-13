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
  fill([1,7,17],[21,8,17],'prismarine_bricks')
  fill([2,7,1],[2,9,17],'prismarine_bricks');fill([2,10,2],[2,15,16],'cyan_stained_glass')
  fill([3,7,3],[29,9,3],'prismarine_bricks');fill([3,10,3],[29,15,3],'cyan_stained_glass')
  fill([5,7,10],[13,7,13],'dark_oak_planks');put([8,8,11],'oak_fence');fill([7,9,11],[10,9,11],'gold_block');put([7,9,12],'black_stained_glass')
  fill([17,7,4],[25,7,5],'dark_oak_planks');for(const east of [17,25])fill([east,8,4],[east,11,4],'oak_fence');fill([17,10,4],[25,10,4],'dark_oak_planks');container([23,7,6])
  for(const east of [4,28]){fill([east,7,15],[east,10,15],'prismarine_bricks');put([east,11,15],'sea_lantern')}
  fill([4,2,23],[8,2,27],'water');coral([6,1,25],'brain')
  fill([13,7,0],[17,10,0],'air');fill([23,3,32],[27,6,32],'air')
  fill([13,7,1],[17,10,4],'air')
  fill([1,17,1],[29,17,31],'cyan_stained_glass');for(const east of [3,15,28])fill([east,17,0],[east,17,32],'prismarine_bricks')
  return finish('S5-19','Raised enclosed watch balcony with telescopic sculpture, parapet, back access and eight-half-step descending stair to lower landing.',[
    {name:'south-low',feet:[25,3,32],facing:'south',width:5,height:4},{name:'north-high',feet:[15,7,0],facing:'north',width:5,height:4}
  ],[{label:'upper-landing',position:[25,7,16]},{label:'watch-desk',position:[10,7,14]},{label:'weapon-rack',position:[21,7,7]},{label:'observation',position:[6,7,7]}],[
    'Viewing parapet backed by cyan glass and full canopy; no actual ocean surroundings packaged.',
    'Telescope is gold/glass block sculpture, not a usable spyglass.',
    'Real four-block floor-height difference is traversed by 0.5-height slab steps; checked with native body collision.',
    'Upper platform dry; lower landing contains isolated shallow coral pool.'
  ],{cutHeight:17})
}
