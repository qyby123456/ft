import {tools} from '../common.mjs'
export default function build(api){
 const {structure,put,box,ground,walls,mushroom,port}=tools(api,[35,21,33])
 ground();walls()
 box([11,2,10],[23,2,22],'mossy_stone_bricks')
 box([11,3,10],[23,5,10],'mossy_stone_bricks');box([11,3,22],[23,5,22],'mossy_stone_bricks');box([11,3,11],[11,5,21],'mossy_stone_bricks');box([23,3,11],[23,5,21],'mossy_stone_bricks')
 box([12,3,11],[22,3,21],'water');box([12,4,11],[22,4,21],'blue_stained_glass')
 for(const east of [11,23]){box([east,6,15],[east,10,15],'dark_oak_log');put([east,5,15],'stone_bricks')}
 box([10,11,15],[24,11,15],'dark_oak_log')
 box([17,7,15],[17,10,15],'chain');put([17,6,15],'barrel',{facing:'up',open:'false'},api.createBlockNbt({id:'minecraft:barrel',Items:[]}))
 box([17,2,23],[19,2,28],'water');box([17,3,23],[19,3,28],'blue_stained_glass');box([16,2,23],[16,3,29],'mossy_stone_bricks');box([20,2,23],[20,3,29],'mossy_stone_bricks');box([17,2,29],[19,3,29],'mossy_stone_bricks')
 for(const [east,south] of [[6,8],[7,8],[27,25]])put([east,3,south],'barrel',{facing:'up',open:'false'},api.createBlockNbt({id:'minecraft:barrel',Items:[]}))
 mushroom(5,5,3,11,3);mushroom(28,6,3,12,4);mushroom(5,28,3,5,2)
 box([5,3,20],[5,3,23],'dark_oak_fence');box([8,3,20],[8,3,23],'dark_oak_fence');box([5,4,20],[8,4,23],'dark_oak_slab');put([6,5,21],'lantern')
 const ports=[port('west',16),port('east',16)]
 return {structure,metadata:{name:'s7-16-well-cistern',ports}}
}
