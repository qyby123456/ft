import {toolkit} from './common.mjs'
export default function build(api){
  const {put,box,lamp,icicle,finish}=toolkit(api,[35,16,31],'s3-13')
  box(0,0,0,34,1,30,'stone_bricks')
  box(0,2,0,34,13,1,'deepslate_bricks');box(0,2,29,34,13,30,'deepslate_bricks')
  box(0,2,2,1,13,28,'deepslate_bricks');box(33,2,2,34,13,28,'deepslate_bricks')
  box(0,14,0,34,14,30,'packed_ice');box(0,15,0,34,15,30,'snow_block')
  box(2,2,18,32,2,28,'stone_bricks');box(3,2,17,31,2,17,'stone_brick_slab[type=bottom]')
  box(5,3,24,13,3,28,'stone_bricks');box(5,4,25,6,8,28,'stone_bricks');box(12,4,25,13,8,28,'stone_bricks')
  box(5,9,24,13,9,28,'stone_bricks');box(8,10,26,10,14,28,'stone_bricks')
  box(7,4,27,11,6,28,'shroomlight');put(9,4,25,'cauldron')
  box(7,8,24,11,8,24,'spruce_slab[type=top]')
  put(6,3,22,'furnace[facing=north,lit=false]',{id:'minecraft:furnace',Items:[]})
  box(18,3,26,30,3,27,'spruce_planks');box(18,6,26,30,6,27,'spruce_slab[type=top]')
  for(const east of [19,22,25,28]){put(east,4,26,'flower_pot');put(east,7,26,'flower_pot');put(east,3,28,'barrel[facing=north]',{id:'minecraft:barrel',Items:[]})}
  for(const east of [16,30])put(east,3,24,'barrel[facing=up]',{id:'minecraft:barrel',Items:[]})
  box(11,3,20,17,3,21,'spruce_fence');box(11,4,20,17,4,22,'spruce_slab[type=top]');put(12,5,21,'candle[lit=true,candles=2]')
  for(const south of [6,12]){
    for(const east of [10,23]){put(east,2,south,'spruce_fence');put(east,2,south+1,'spruce_fence')}
    box(10,3,south,23,3,south+1,'spruce_slab[type=top]')
    box(10,2,south-2,23,2,south-2,'spruce_slab[type=bottom]');box(10,2,south+3,23,2,south+3,'spruce_slab[type=bottom]')
    put(16,4,south,'lantern[hanging=false]');put(12,4,south,'flower_pot');put(21,4,south+1,'flower_pot')
  }
  box(3,1,3,7,1,7,'blue_ice');box(3,2,8,6,2,8,'spruce_fence')
  box(27,2,3,31,2,4,'snow_block');put(29,3,3,'blue_ice')
  for(const east of [4,16,26,31])icicle(east,28,13,4)
  lamp(15,25,14);lamp(28,22,14)
  box(0,2,14,1,5,16,'air');box(33,2,14,34,5,16,'air')
  return finish({title:'阶台厨房与双长桌食堂',reference:'S3-13',features:['后半厨房高一格','中心壁炉烟囱和空釜','双层餐具架','两张长桌与长凳','前方冻结池'],ports:[{name:'west',foot:[0,2,15],width:3,height:4,normal:[-1,0,0]},{name:'east',foot:[34,2,15],width:3,height:4,normal:[1,0,0]}],start:[0,2,15],targets:[{label:'east exit',position:[34,2,15]},{label:'hearth',position:[9,3,23]},{label:'pantry',position:[25,3,24]},{label:'dining',position:[25,2,9]}],differences:['火焰用发光菌块模拟暖炉，避免接触火和明火扩散','空炼药锅和花盆表现炊具，不投放食物','厨房高差用半砖连续过渡；冻结池无流动水'],cutY:14})
}
