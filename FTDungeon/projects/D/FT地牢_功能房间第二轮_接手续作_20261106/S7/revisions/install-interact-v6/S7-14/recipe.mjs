import {tools} from '../common.mjs'
export default function build(api){
 const {structure,put,box,ground,walls,mushroom,port}=tools(api,[33,18,31])
 ground();walls()
 for(const [east,south] of [[4,5],[28,5],[4,26],[28,26]]){box([east-1,3,south-1],[east+1,7,south+1],'rooted_dirt');box([east-1,8,south-1],[east+1,8,south+1],'mycelium');mushroom(east,south,9,4,2)}
 for(const east of [6,8,10,12]){put([east,5,3],'end_rod',{facing:'up'});put([east,4,3],'iron_bars')}
 box([5,3,3],[13,3,4],'dark_oak_planks');box([5,7,3],[13,7,3],'dark_oak_planks')
 for(const [east,metal] of [[16,'iron_block'],[20,'gold_block']]){box([east-1,3,5],[east+1,3,7],'polished_andesite');put([east,4,6],'dark_oak_fence');box([east-1,5,6],[east+1,5,6],metal);put([east,6,6],metal);put([east,7,6],'polished_andesite')}
 box([23,3,3],[27,8,5],'dark_oak_planks');for(const east of [24,26])put([east,5,6],'barrel',{facing:'south',open:'false'},api.createBlockNbt({id:'minecraft:barrel',Items:[]}))
 for(const up of [4,7]){box([25,up,9],[29,up,10],'dark_oak_planks');for(const east of [25,27,29])put([east,up+1,9],'end_rod',{facing:'up'})}
 put([27,3,13],'anvil',{facing:'north'})
 box([13,2,16],[21,2,23],'purple_wool')
 for(const east of [13,21])for(const south of [17,21])put([east,3,south],'dark_oak_fence')
 box([13,4,17],[21,4,21],'dark_oak_slab');put([17,5,19],'lantern')
 for(const [east,south] of [[7,23],[8,23],[8,24],[9,24]])put([east,3,south],'barrel',{facing:'up',open:'false'},api.createBlockNbt({id:'minecraft:barrel',Items:[]}))
 for(const east of [10,24]){box([east,11,13],[east,15,13],'chain');put([east,10,13],'lantern',{hanging:'true'})}
 box([23,2,25],[25,2,27],'blue_stained_glass');box([23,1,25],[25,1,27],'water')
 const ports=[port('west',16),port('east',16)]
 for(const east of [5,13])box([east,3,2],[east,8,3],'dark_oak_log')
 box([5,8,2],[13,8,3],'mossy_stone_bricks')
 for(const east of [5,12]){
  box([east,9,2],[east+1,10,3],'rooted_dirt')
  box([east,11,2],[east+1,11,3],'mycelium')
 }
 for(const east of [2,3,29,30]){
  for(const south of [13,19])box([east,3,south],[east,7,south],'mossy_stone_bricks')
  box([east,8,13],[east,9,19],'rooted_dirt')
 }
 return {structure,metadata:{name:'s7-14-armory',ports}}
}
