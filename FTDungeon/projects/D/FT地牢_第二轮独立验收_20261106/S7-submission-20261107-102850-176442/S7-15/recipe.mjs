import {tools} from '../common.mjs'
export default function build(api){
 const {structure,put,box,ground,walls,mushroom,port}=tools(api,[39,19,35])
 ground();walls()
 for(let east=7;east<=31;east++)for(let south=9;south<=25;south++){
  const edge=Math.min(east-7,31-east,south-9,25-south)
  const drift=Math.sin(east*0.53)+Math.cos(south*0.47)
  if(edge>2+drift)put([east,2,south],drift>1.2?'coarse_dirt':drift<-0.9?'gravel':'sandstone')
 }
 for(const [east,south] of [[12,13],[25,13],[12,22],[25,22]]){
  put([east,3,south],'dark_oak_fence');for(const dx of [-1,0,1])put([east+dx,4,south],'hay_block',{axis:'y'});put([east,5,south],'hay_block',{axis:'y'})
 }
 for(const east of [13,19,25]){
  box([east,3,4],[east,5,4],'dark_oak_log');box([east-1,5,3],[east+1,7,3],'white_wool');put([east,6,4],'target',{power:'0'})
 }
 for(const [east,south] of [[11,29],[22,29]]){box([east,3,south],[east+7,3,south],'dark_oak_slab');box([east,4,south+1],[east+7,4,south+1],'dark_oak_planks')}
 for(let east=8;east<32;east++)put([east,3,27],'dark_oak_fence')
 for(const east of [7,32]){box([east,3,27],[east,4,27],'dark_oak_fence');put([east,5,27],'lantern')}
 for(let south=10;south<=22;south+=4){put([5,3,south],'dark_oak_slab');put([5,3,south+1],'dark_oak_slab')}
 for(const south of [12,16])put([30,3,south],'dark_oak_fence')
 box([30,4,12],[30,4,16],'dark_oak_log')
 for(const east of [30,33])put([east,3,21],'dark_oak_fence')
 for(let east=30;east<=33;east++)put([east,4,21],'dark_oak_log',{axis:'x'})
 for(const south of [12,15,18])put([34,3,south],'stone_brick_slab',{type:'bottom',waterlogged:'false'})
 for(const [east,south,up] of [[4,5,8],[33,5,9],[34,29,6]]){box([east-1,3,south-1],[east+1,up,south+1],'rooted_dirt');box([east-1,up+1,south-1],[east+1,up+1,south+1],'mycelium');mushroom(east,south,up+2,3,2)}
 box([29,3,4],[33,3,5],'dark_oak_planks');for(const east of [29,31,33])put([east,4,4],'end_rod',{facing:'up'})
 put([28,3,14],'dark_oak_slab',{type:'bottom',waterlogged:'false'});put([29,3,14],'dark_oak_planks');put([31,3,22],'dark_oak_planks');put([30,3,22],'dark_oak_slab',{type:'bottom',waterlogged:'false'})
 const ports=[port('west',18),port('east',18)]
 return {structure,metadata:{name:'s7-15-training-yard',ports}}
}
