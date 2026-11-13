import {tools} from '../common.mjs'
export default function build(api){
 const {structure,put,box,ground,walls,mushroom,port}=tools(api,[31,18,29])
 ground();walls()
 for(const south of [3,9])box([3,3,south],[3,10,south],'dark_oak_log')
 for(const up of [4,7,10]){
  box([3,up,3],[5,up,9],'dark_oak_planks')
  for(const south of [4,7])put([4,up+1,south],up===7?'terracotta':'calcite')
 }
 for(const east of [6,12]){box([east,3,3],[east,10,5],'dark_oak_log');for(const up of [4,7,10]){box([east,up,3],[east+5,up,5],'dark_oak_planks');for(const dx of [1,3])put([east+dx,up+1,4],up===7?'terracotta':'calcite')}}
 put([8,3,8],'grindstone',{face:'floor',facing:'south'});put([5,3,8],'crafting_table')
 box([20,3,3],[23,8,6],'stone_bricks');put([21,4,7],'furnace',{facing:'south',lit:'false'},api.createBlockNbt({id:'minecraft:furnace',Items:[],BurnTime:new api.NbtShort(0),CookTime:new api.NbtShort(0),CookTimeTotal:new api.NbtShort(200)}))
 for(const [east,south] of [[25,7],[24,24],[25,24],[4,22]])put([east,3,south],'barrel',{facing:'up',open:'false'},api.createBlockNbt({id:'minecraft:barrel',Items:[]}))
 for(const east of [12,18])for(const south of [14,18])put([east,3,south],'dark_oak_fence')
 box([12,4,14],[18,4,18],'dark_oak_slab')
 for(const east of [14,16])for(const south of [15,17])put([east,5,south],'oak_fence')
 box([14,6,15],[16,6,15],'oak_slab');box([14,6,17],[16,6,17],'oak_slab')
 put([17,5,18],'lantern')
 for(const [east,south]of [[13,20],[18,20]]){
  put([east,3,south],'dark_oak_fence');put([east,4,south],'dark_oak_slab')
 }
 mushroom(24,4,9,4,3);mushroom(5,25,3,5,3)
 box([11,2,23],[15,2,25],'blue_stained_glass');box([11,1,23],[15,1,25],'water')
 for(const [east,south] of [[4,12],[26,12]]){box([east,11,south],[east,15,south],'chain');put([east,10,south],'lantern',{hanging:'true'})}
 const ports=[port('west',15),port('east',15)]
 return {structure,metadata:{name:'s7-17-workshop',ports}}
}
