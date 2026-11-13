import {room} from '../tools.mjs'
export default function(api){
  const {put,fill,box,container,coral,finish}=room(api,[31,13,25])
  box([0,0,0],[30,10,24],'prismarine_bricks');fill([1,1,1],[29,1,23],'dark_prismarine');fill([1,2,1],[29,2,23],'prismarine')
  for(const [east,end] of [[2,6],[24,28]]){fill([east,2,18],[end,2,21],'water');coral([east+1,1,19],east<10?'brain':'fire')}
  for(const [start,end] of [[2,8],[21,28]]){
    fill([start,3,1],[end,3,3],'dark_oak_planks')
    fill([start,4,1],[start,7,1],'dark_oak_fence');fill([end,4,1],[end,7,1],'dark_oak_fence')
    for(const rise of [5,7])fill([start,rise,1],[end,rise,2],'dark_oak_planks')
    for(let east=start+1;east<end;east+=2)fill([east,6,2],[east,6,2],'iron_bars')
    container([start+1,3,4])
  }
  for(const [east,material] of [[11,'iron_block'],[14,'gold_block'],[17,'diamond_block']]){
    put([east,3,2],'polished_andesite');fill([east,4,2],[east,5,2],material);put([east,6,2],material)
    put([east-1,5,2],'iron_bars');put([east+1,5,2],'iron_bars')
  }
  fill([2,3,6],[4,7,8],'dark_oak_planks');container([3,4,9]);put([26,3,7],'anvil',{facing:'north'})
  put([3,3,9],'dark_oak_planks')
  fill([11,3,10],[19,3,13],'dark_prismarine');fill([12,4,11],[18,4,12],'prismarine_bricks');put([16,5,11],'sea_lantern')
  for(const east of [0,30]){fill([east,3,10],[east,6,14],'air');put([east,7,12],'sea_lantern')}
  fill([2,10,3],[28,10,22],'cyan_stained_glass')
  for(const east of [5,15,25]){fill([east,10,0],[east,10,24],'prismarine_bricks');put([east,8,1],'sea_lantern')}
  return finish('S5-14','Inspection island framed by arrow/spear racks, three metallic armor effigies, cabinet and anvil.',[
    {name:'west',feet:[0,3,12],facing:'west',width:5,height:4},{name:'east',feet:[30,3,12],facing:'east',width:5,height:4}
  ],[{label:'inspection',position:[15,3,14]},{label:'armor-display',position:[14,3,5]},{label:'cabinet',position:[3,3,10]},{label:'anvil',position:[26,3,8]}],[
    'Armor silhouettes use supported metal-block effigies, not interactive equipped armor-stand entities.',
    'Racks use iron bars; no actual weapons/arrows or magical locked cabinet.',
    'Coral survives in shallow perimeter pools; complete glass-beamed roof, dry side ports.'
  ])
}
