import {tools} from '../common.mjs'
export default function build(api){
 const {structure,put,box,ground,walls,mushroom,port,container}=tools(api,[35,19,31])
 ground();walls()
 for(let east=3;east<=31;east++)for(let south=4;south<=26;south++)if(south===15||south===16||east===17||east===18)put([east,2,south],'dark_oak_planks')
 box([5,3,3],[29,9,4],'bookshelf')
 for(const east of [5,11,18,25,29])box([east,3,3],[east,14,4],'dark_oak_log')
 box([5,10,3],[29,10,7],'dark_oak_planks');box([6,11,3],[28,14,4],'bookshelf')
 for(let east=5;east<=29;east++)put([east,11,7],'dark_oak_fence')
 put([20,11,7],'air');box([20,3,5],[20,9,5],'dark_oak_planks')
 for(let up=3;up<=10;up++)put([20,up,6],'ladder',{facing:'south'})
 for(const [east,south] of [[8,10],[19,18],[27,24]]){
  for(const dx of [-2,2])for(const dz of [-1,1])put([east+dx,3,south+dz],'dark_oak_fence')
  box([east-2,4,south-1],[east+2,4,south+1],'dark_oak_slab')
  put([east,4,south],'lectern',{facing:'south',has_book:'true'},api.createBlockNbt({id:'minecraft:lectern',Book:{id:'minecraft:writable_book',Count:new api.NbtByte(1),tag:{pages:['Fungal archive']}},Page:0}))
  put([east+1,5,south],'lantern');put([east-2,3,south+3],'dark_oak_slab')
 }
 box([28,3,8],[30,6,9],'dark_oak_planks');container([29,4,10])
 mushroom(5,5,11,4,3);mushroom(29,5,11,4,3);mushroom(4,25,3,4,2)
 for(const [east,south] of [[3,9],[31,12],[5,28],[30,27]]){put([east,2,south],'shroomlight');put([east,3,south],'warped_nylium');put([east,4,south],'warped_fungus')}
 box([7,2,24],[10,2,26],'blue_stained_glass');box([7,1,24],[10,1,26],'water')
 for(const east of [12,24]){box([east,13,12],[east,16,12],'chain');put([east,12,12],'lantern',{hanging:'true'})}
 box([2,3,4],[3,8,11],'bookshelf');box([2,9,4],[3,9,11],'dark_oak_planks')
 const ports=[port('west',16),port('east',16)]
 return {structure,metadata:{name:'s7-11-library',ports}}
}
