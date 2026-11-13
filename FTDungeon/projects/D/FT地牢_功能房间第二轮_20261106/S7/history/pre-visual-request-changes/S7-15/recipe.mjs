import {tools} from '../common.mjs'
export default function build(api){
 const {structure,put,box,ground,walls,mushroom,port}=tools(api,[39,19,35])
 ground();walls()
 box([7,2,9],[31,2,25],'sandstone')
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
 for(const [east,south,up] of [[4,5,8],[33,5,9],[34,29,6]]){box([east-1,3,south-1],[east+1,up,south+1],'rooted_dirt');box([east-1,up+1,south-1],[east+1,up+1,south+1],'mycelium');mushroom(east,south,up+2,3,2)}
 box([29,3,4],[33,3,5],'dark_oak_planks');for(const east of [29,31,33])put([east,4,4],'end_rod',{facing:'up'})
 const ports=[port('west',18),port('east',18)]
 return {structure,metadata:{name:'s7-15-training-yard',ports}}
}
