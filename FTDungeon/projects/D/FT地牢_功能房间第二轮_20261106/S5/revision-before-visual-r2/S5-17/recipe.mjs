import {room} from '../tools.mjs'
export default function(api){
  const {put,fill,box,container,coral,finish}=room(api,[29,15,27])
  box([0,0,0],[28,11,26],'prismarine_bricks');fill([1,1,1],[27,1,25],'dark_prismarine');fill([1,2,1],[27,2,25],'prismarine')
  fill([4,3,1],[12,3,3],'dark_oak_planks');put([5,4,2],'grindstone',{face:'floor',facing:'south'});put([10,4,2],'crafting_table')
  for(const east of [4,12])fill([east,4,1],[east,7,1],'dark_oak_fence')
  fill([4,7,1],[12,7,2],'dark_oak_planks');for(const east of [6,9])container([east,8,1])
  fill([15,3,1],[20,8,4],'polished_andesite');container([17,3,5],'furnace','south');fill([16,9,1],[19,13,3],'prismarine_bricks')
  for(const height of [3,5,7,9]){fill([23,height,1],[26,height,2],'dark_oak_planks');for(const east of [23,26])fill([east,height+1,1],[east,height+1,1],'dark_oak_fence')}
  container([24,3,3]);put([24,6,2],'copper_block');put([24,8,2],'quartz_block')
  fill([10,3,11],[18,3,15],'dark_oak_planks');fill([12,4,12],[16,4,14],'prismarine_bricks');put([14,5,13],'prismarine');put([14,6,13],'sea_lantern')
  container([4,3,20],'chest');container([23,3,20]);put([23,3,17],'anvil',{facing:'north'})
  for(const [east,south,color] of [[4,21,'brain'],[24,22,'fire']]){fill([east-1,2,south-1],[east+1,2,south+1],'water');coral([east,1,south],color)}
  for(const east of [0,28]){fill([east,3,10],[east,6,14],'air');put([east,7,12],'sea_lantern')}
  fill([2,11,5],[26,11,24],'cyan_stained_glass');for(const east of [5,14,23])fill([east,11,0],[east,11,26],'prismarine_bricks')
  return finish('S5-17','Tool wall and grindstone, corner furnace chimney, tall materials rack, miniature prismarine project on central workbench.',[
    {name:'west',feet:[0,3,12],facing:'west',width:5,height:4},{name:'east',feet:[28,3,12],facing:'east',width:5,height:4}
  ],[{label:'grindstone',position:[5,3,4]},{label:'furnace',position:[17,3,6]},{label:'materials',position:[24,3,4]},{label:'project',position:[14,3,16]},{label:'chest',position:[4,3,19]}],[
    'Vanilla grindstone/crafting table/anvil/furnace provided, but player interaction untested.',
    'Unlit furnace avoids fictional underwater flame; no fuel, recipes or loot inserted.',
    'Miniature project is a block sculpture; glass canopy completes original cutaway.'
  ],{cutHeight:11})
}
