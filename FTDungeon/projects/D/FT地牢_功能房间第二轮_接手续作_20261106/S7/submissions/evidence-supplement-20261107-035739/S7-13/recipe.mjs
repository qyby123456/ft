import {tools} from '../common.mjs'
export default function build(api){
 const {structure,put,box,ground,walls,mushroom,port}=tools(api,[37,19,33])
 ground();walls()
 box([7,3,2],[17,4,7],'stone_bricks')
 box([7,5,3],[9,10,7],'stone_bricks');box([15,5,3],[17,10,7],'stone_bricks')
 box([7,10,3],[17,11,7],'stone_bricks');box([10,12,2],[14,17,5],'stone_bricks')
 box([10,5,3],[14,5,6],'shroomlight');put([12,6,5],'cauldron')
 box([12,8,5],[12,9,5],'chain')
 for(const east of [20,27]){
  box([east,3,3],[east,9,4],'dark_oak_log');box([east+5,3,3],[east+5,9,4],'dark_oak_log')
  for(const up of [4,7,10]){box([east,up,3],[east+5,up,5],'dark_oak_planks');for(const dx of [1,3])put([east+dx,up+1,4],'flower_pot')}
 }
 for(const [east,south] of [[22,18],[28,26]]){
  for(const dx of [-4,4])for(const dz of [-1,1])put([east+dx,3,south+dz],'dark_oak_fence')
  box([east-4,4,south-1],[east+4,4,south+1],'dark_oak_slab')
  for(const dz of [-3,3])box([east-4,3,south+dz],[east+4,3,south+dz],'dark_oak_slab')
  for(const dx of [-2,0,2])put([east+dx,5,south],'polished_blackstone_pressure_plate',{powered:'false'})
  put([east+3,5,south],'lantern')
 }
 box([8,3,12],[8,3,14],'dark_oak_fence');box([14,3,12],[14,3,14],'dark_oak_fence');box([8,4,12],[14,4,14],'dark_oak_slab');put([11,5,13],'red_wool')
 for(const [east,south] of [[6,13],[18,6],[19,6]])put([east,3,south],'barrel',{facing:'up',open:'false'},api.createBlockNbt({id:'minecraft:barrel',Items:[]}))
 mushroom(5,5,3,9,3);mushroom(31,9,3,10,3)
 box([5,2,25],[9,2,28],'blue_stained_glass');box([5,1,25],[9,1,28],'water')
 for(const [east,south,up] of [[3,22,7],[34,22,8],[21,2,12]]){box([east,up,south],[east+1,up,south+1],'mycelium');put([east,up+1,south],'red_mushroom')}
 const ports=[port('west',18),port('east',18)]
 return {structure,metadata:{name:'s7-13-kitchen-mess',ports}}
}
