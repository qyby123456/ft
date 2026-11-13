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
 for(const [east,south,width,depth,height]of [[3,11,3,2,5],[27,3,4,2,6],[28,23,3,3,4],[24,27,3,3,5],[9,27,2,3,4]]){
  box([east,3,south],[east+width-1,height,south+depth-1],'mossy_stone_bricks')
  box([east,height+1,south],[east+width-1,height+1,south+depth-1],'rooted_dirt')
  box([east,height+2,south],[east+width-1,height+2,south+depth-1],'mycelium')
  put([east,height+3,south],'red_mushroom')
  put([east+width-1,height,south+depth-1],'hanging_roots',{waterlogged:'false'})
 }
 for(const [east,south]of [[10,10],[24,10],[10,21],[24,21]]){
  put([east,3,south],'mossy_stone_bricks')
  put([east,4,south],'stone_brick_slab',{type:'bottom',waterlogged:'false'})
 }
 for(let step=0;step<6;step++){
  const east=30-step,foot=3+(step+1)*0.5
  box([east,2,20],[east,Math.floor(foot)-1,22],'mossy_stone_bricks')
  if(foot%1)for(let south=20;south<=22;south++)put([east,Math.floor(foot),south],'stone_brick_slab',{type:'bottom',waterlogged:'false'})
 }
 box([24,3,20],[24,5,22],'mossy_stone_bricks')
 const ports=[port('west',16),port('east',16)]
 return {structure,metadata:{name:'s7-16-well-cistern',ports}}
}
