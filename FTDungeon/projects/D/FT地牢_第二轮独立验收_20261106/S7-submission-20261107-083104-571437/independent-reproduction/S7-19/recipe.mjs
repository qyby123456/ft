import {tools} from '../common.mjs'
export default function build(api){
 const raw=tools(api,[57,26,39])
 const {structure,ground,walls,port}=raw
 const shifted=position=>[position[0]+18,position[1],position[2]]
 const put=(position,...arguments_)=>raw.put(shifted(position),...arguments_)
 const box=(from,to,...arguments_)=>raw.box(shifted(from),shifted(to),...arguments_)
 const mushroom=(east,...arguments_)=>raw.mushroom(east+18,...arguments_)
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
 raw.box([1,7,3],[24,22,25],'air')
 for(let east=1;east<=23;east++)for(let south=3;south<=25;south++){
  const terrace=3+Math.floor(2+2*Math.sin(east*0.23+south*0.19))
  raw.box([east,3,south],[east,terrace,south],'rooted_dirt')
  raw.put([east,terrace+1,south],'mycelium')
 }
 for(const [east,south,base,height,radius]of [[19,7,9,6,2],[15,23,8,4,2],[10,13,7,7,3],[4,6,7,4,1],[3,21,7,3,1]]){
  raw.box([east-1,3,south-1],[east+1,base-1,south+1],'rooted_dirt')
  raw.mushroom(east,south,base,height,radius)
 }
 for(let south=3;south<=25;south++){
  const reach=1+Math.floor(1.5+1.5*Math.sin(south*0.45))
  raw.box([0,7,south],[reach,22,south],'brown_terracotta')
  raw.box([0,19,south],[reach+1,22,south],'rooted_dirt')
 }
 for(const [east,south]of [[7,5],[12,23],[21,8]]){
  raw.box([east,20,south],[east+2,22,south+1],'rooted_dirt')
  raw.put([east+1,19,south],'hanging_roots',{waterlogged:'false'})
 }
 box([6,10,5],[6,19,23],'glass')
 box([7,9,5],[7,10,23],'air')
 for(let south=5;south<=23;south++){put([7,9,south],'stone_brick_wall');if(south%5===0)box([7,9,south],[7,12,south],'mossy_stone_bricks')}
 const ports=[port('west',32,3),port('east',15,9)]
 return {structure,metadata:{name:'s7-19-watch-balcony',ports}}
}
