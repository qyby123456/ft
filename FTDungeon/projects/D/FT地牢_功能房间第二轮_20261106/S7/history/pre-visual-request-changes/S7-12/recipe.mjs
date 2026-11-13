import {tools} from '../common.mjs'
export default function build(api){
 const {structure,put,box,ground,walls,mushroom,port,container}=tools(api,[41,16,33])
 ground();walls()
 box([2,2,13],[38,2,18],'coarse_dirt')
 for(const east of [6,17,28])for(const north of [true,false]){
  const back=north?3:29,front=north?11:21
  box([east,3,Math.min(back,front)],[east,7,Math.max(back,front)],'purple_terracotta')
  box([east+8,3,Math.min(back,front)],[east+8,7,Math.max(back,front)],'purple_terracotta')
  box([east+1,3,front],[east+7,6,front],'iron_bars')
  box([east,7,front],[east+8,7,front],'purple_terracotta')
  box([east+3,3,front],[east+4,5,front],'air')
  for(const [up,half] of [[3,'lower'],[4,'upper']])put([east+3,up,front],'iron_door',{half,facing:north?'south':'north',open:'true',hinge:'left',powered:'false'})
  box([east+2,3,north?5:26],[east+4,3,north?6:27],'red_wool')
  put([east+2,4,north?5:27],'white_carpet')
  put([east+6,5,north?3:29],'shroomlight')
 }
 mushroom(4,5,3,7,2);mushroom(37,28,3,7,2)
 mushroom(37,4,3,5,2)
 box([30,3,15],[34,3,16],'dark_oak_planks');container([35,3,16]);container([35,4,16])
 for(const east of [11,22,33]){box([east,10,16],[east,13,16],'chain');put([east,9,16],'lantern',{hanging:'true'})}
 for(const [east,south] of [[9,14],[20,17],[27,14]]){put([east,2,south],'blue_stained_glass');put([east,1,south],'water')}
 const ports=[port('west',16),port('east',16)]
 return {structure,metadata:{name:'s7-12-prison-block',ports}}
}
