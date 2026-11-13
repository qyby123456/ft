import {tools} from '../common.mjs'
export default function build(api){
 const {structure,put,box,ground,walls,mushroom,port}=tools(api,[39,26,39])
 ground();walls()
 box([7,3,5],[32,8,23],'rooted_dirt');box([7,8,5],[32,8,23],'mossy_stone_bricks')
 for(let step=0;step<12;step++){
  const south=36-step,foot=3+(step+1)*0.5
  box([29,2,south],[32,Math.floor(foot)-1,south],'stone_bricks')
  if(foot%1)for(let east=29;east<=32;east++)put([east,Math.floor(foot),south],'stone_brick_slab',{type:'bottom',waterlogged:'false'})
 }
 box([29,3,24],[32,8,24],'stone_bricks')
 box([7,9,5],[7,10,23],'mossy_stone_bricks');box([8,9,23],[28,10,23],'mossy_stone_bricks')
 box([32,9,5],[32,10,12],'mossy_stone_bricks');box([32,9,18],[32,10,23],'mossy_stone_bricks')
 box([33,8,13],[38,8,17],'stone_bricks')
 for(const east of [14,19]){put([east,9,18],'dark_oak_fence');put([east,9,20],'dark_oak_fence')}
 box([14,10,18],[19,10,20],'dark_oak_slab');put([18,11,19],'lantern');put([15,11,19],'end_rod',{facing:'west'})
 box([23,9,6],[29,9,7],'dark_oak_planks');for(const east of [24,26,28])put([east,10,6],'end_rod',{facing:'up'})
 mushroom(10,8,9,9,3);mushroom(33,6,9,8,2);mushroom(4,29,3,6,2)
 box([0,10,5],[1,20,23],'purple_stained_glass')
 const ports=[port('west',32,3),port('east',15,9)]
 return {structure,metadata:{name:'s7-19-watch-balcony',ports}}
}
