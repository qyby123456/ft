import {room} from '../tools.mjs'
export default function(api){
  const {put,fill,box,container,coral,finish}=room(api,[33,17,29])
  box([0,0,0],[32,11,28],'prismarine_bricks')
  fill([1,1,1],[31,1,27],'dark_prismarine');fill([1,2,1],[31,2,27],'prismarine')
  fill([2,2,21],[5,2,25],'water');fill([28,2,21],[30,2,25],'water')
  coral([3,1,23],'brain');coral([29,1,23],'fire')
  fill([4,3,1],[13,7,5],'prismarine_bricks');fill([5,3,4],[12,5,5],'air')
  for(const east of [6,10])container([east,3,4],'furnace','south')
  put([8,3,4],'water_cauldron',{level:'3'})
  fill([6,8,1],[11,15,4],'dark_prismarine');fill([7,8,2],[10,16,3],'air')
  fill([7,15,2],[10,15,3],'cyan_stained_glass')
  for(const east of [16,24]){
    fill([east,3,1],[east+5,3,3],'dark_oak_planks')
    for(const south of [1,3])fill([east,4,south],[east,7,south],'dark_oak_fence')
    for(const height of [5,8]){
      fill([east,height,1],[east+5,height,2],'dark_oak_planks')
      for(const offset of [1,3,5])put([east+offset,height+1,2],'flower_pot')
    }
    container([east+2,3,4])
  }
  fill([12,3,8],[19,3,10],'stripped_oak_log',{axis:'y'});put([15,4,9],'red_carpet')
  for(const start of [7,21]){
    fill([start,3,17],[start+6,3,19],'dark_oak_planks')
    fill([start,3,15],[start+6,3,15],'dark_oak_slab',{type:'bottom',waterlogged:'false'})
    fill([start,3,21],[start+6,3,21],'dark_oak_slab',{type:'bottom',waterlogged:'false'})
    put([start+3,4,18],'candle',{candles:'3',lit:'true',waterlogged:'false'})
  }
  for(const east of [0,32]){fill([east,3,10],[east,6,14],'air');put([east,7,12],'sea_lantern')}
  fill([2,11,7],[30,11,26],'cyan_stained_glass')
  for(const east of [3,16,29]){fill([east,11,0],[east,11,28],'prismarine_bricks');put([east,8,1],'sea_lantern')}
  return finish('S5-13','Monumental chimney hearth, paired storage racks, butcher island and two long mess tables with benches.',[
    {name:'west',feet:[0,3,12],facing:'west',width:5,height:4},{name:'east',feet:[32,3,12],facing:'east',width:5,height:4}
  ],[{label:'hearth',position:[8,3,6]},{label:'butcher',position:[15,3,11]},{label:'dining-west',position:[10,3,16]},{label:'dining-east',position:[24,3,16]},{label:'storage',position:[19,3,5]}],[
    'Underwater open fire replaced with unlit usable vanilla furnaces and water cauldron; no campfire/fire/lava hazard.',
    'Pottery approximates jars; no decorative food entities or actual inventory.',
    'Enclosed kitchen with glass roof and capped chimney; atmosphere is shallow-water rather than pressure-safe full-ocean deployment.'
  ],{cutHeight:11})
}
