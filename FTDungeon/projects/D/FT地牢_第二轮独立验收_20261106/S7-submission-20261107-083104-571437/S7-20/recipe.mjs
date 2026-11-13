import {tools} from '../common.mjs'
export default function build(api){
 const {structure,put,box,ground,walls,mushroom,port}=tools(api,[35,19,29])
 ground();walls()
 for(let east=3;east<=17;east++)for(let south=3;south<=12;south++){
  const edge=Math.min(east-3,17-east,south-3,12-south)
  const crown=east>=5&&east<=11&&south>=4&&south<=8
  const top=crown?12:Math.min(12,7+edge+Math.floor(1+Math.sin(east*0.7+south*0.4)))
  box([east,3,south],[east,top,south],edge<1?'mossy_stone_bricks':'rooted_dirt')
  put([east,top+1,south],'mycelium')
 }
 box([19,3,3],[31,11,12],'purple_terracotta');box([20,3,4],[30,9,11],'air')
 box([23,3,11],[27,7,12],'air');box([23,8,11],[27,8,12],'dark_oak_log')
 box([25,2,4],[29,2,7],'stone_bricks')
 put([27,3,6],'chest',{facing:'south',type:'single',waterlogged:'false'},api.createBlockNbt({id:'minecraft:chest',Items:[]}))
 for(const [east,south] of [[24,6],[29,8],[25,8]])put([east,3,south],'gold_block')
 put([30,8,4],'cobweb');put([29,4,11],'stone_bricks');put([29,4,12],'lever',{face:'wall',facing:'south',powered:'false'})
 for(const east of [21,29])put([east,6,4],'shroomlight')
 box([3,3,19],[28,8,22],'rooted_dirt');box([4,3,19],[27,6,20],'air')
 box([4,3,17],[6,6,19],'air');box([25,3,17],[27,6,19],'air')
 mushroom(8,6,13,2,3);mushroom(14,10,3,5,2);mushroom(29,24,3,5,2)
 box([12,2,15],[17,2,17],'blue_stained_glass');box([12,1,15],[17,1,17],'water')
 box([19,11,3],[31,11,12],'air')
 box([19,10,3],[31,10,12],'mycelium')
 box([23,11,4],[28,11,7],'rooted_dirt');box([23,12,4],[28,12,7],'mycelium')
 for(const [east,south] of [[20,4],[30,9],[24,6]])put([east,11,south],'red_mushroom')
 for(const east of [19,20,30,31])for(const up of [7,8,9])put([east,up,12],'rooted_dirt')
 for(const east of [21,29]){put([east,8,12],'mushroom_stem');put([east,9,12],'red_mushroom_block')}
 box([20,3,3],[30,8,3],'mossy_stone_bricks')
 for(const east of [22,28])box([east,3,12],[east,7,12],'dark_oak_log')
 box([22,9,12],[28,9,12],'mossy_stone_bricks')
 for(const east of [20,30]){
  put([east,8,11],'rooted_dirt');put([east,7,11],'hanging_roots',{waterlogged:'false'})
 }
 for(const [east,south,width,depth,height]of [[17,4,2,4,6],[17,9,2,3,8],[32,4,1,3,7],[32,9,1,3,5]]){
  box([east,3,south],[east+width-1,height,south+depth-1],'mossy_stone_bricks')
  box([east,height+1,south],[east+width-1,height+2,south+depth-1],'rooted_dirt')
  box([east,height+3,south],[east+width-1,height+3,south+depth-1],'mycelium')
 }
 for(const [east,south,top]of [[19,4,13],[20,8,14],[29,4,13],[30,9,12],[24,10,12]]){
  box([east,11,south],[east+1,top,south+1],'rooted_dirt')
  box([east,top+1,south],[east+1,top+1,south+1],'mycelium')
  put([east,top+2,south],'red_mushroom')
 }
 const ports=[port('west',15),port('east',15)]
 return {structure,metadata:{name:'s7-20-secret-vault',ports}}
}
