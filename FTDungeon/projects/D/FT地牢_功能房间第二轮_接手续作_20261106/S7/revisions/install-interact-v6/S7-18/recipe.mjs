import {tools} from '../common.mjs'
export default function build(api){
 const {structure,put,box,ground,walls,mushroom,port}=tools(api,[35,20,33])
 ground();walls()
 for(const [east,south]of [[12,14],[12,15],[13,14],[22,18],[22,19],[23,19],[4,22],[31,24]]){
  put([east,2,south],'mycelium');put([east,3,south],'red_mushroom')
 }
 for(const east of [7,23]){
  box([east,3,2],[east+6,11,4],'mossy_stone_bricks')
  for(const up of [4,7,10]){box([east+1,up,4],[east+5,up+1,4],'air');for(const dx of [1,3,5])put([east+dx,up,4],'skeleton_skull',{rotation:'0'},api.createBlockNbt({id:'minecraft:skull'}))}
 }
 for(const [east,south,width,depth] of [[14,15,7,5],[5,23,6,3],[25,23,5,3]]){
  box([east-1,3,south-1],[east+width,3,south+depth],'mossy_stone_bricks');box([east,4,south],[east+width-1,5,south+depth-1],'mossy_stone_bricks')
  for(const dx of [0,width-1])put([east+dx,6,south],'candle',{candles:'2',lit:'true',waterlogged:'false'})
  put([east+2,6,south+1],'skeleton_skull',{rotation:'0'},api.createBlockNbt({id:'minecraft:skull'}))
 }
 for(const [east,south] of [[4,7],[30,7]]){box([east-1,3,south-1],[east+1,10,south+1],'rooted_dirt');box([east-1,11,south-1],[east+1,11,south+1],'mycelium');mushroom(east,south,12,3,2)}
 for(const east of [9,25]){box([east,12,11],[east,17,11],'chain');put([east,11,11],'lantern',{hanging:'true'})}
 box([15,2,27],[19,2,29],'blue_stained_glass');box([15,1,27],[19,1,29],'water')
 const ports=[port('west',15),port('east',15)]
 for(const east of [7,23]){
  for(const dx of [0,6])box([east+dx,3,5],[east+dx,12,5],'stone_bricks')
  for(const up of [3,6,9,12])box([east,up,5],[east+6,up,5],'mossy_stone_bricks')
  box([east,13,3],[east+6,13,5],'rooted_dirt')
  box([east+1,14,3],[east+5,14,5],'mycelium')
 }
 for(const [east,south,width,depth]of [[14,15,7,5],[5,23,6,3],[25,23,5,3]]){
  for(const dx of [0,width-1]){
   put([east+dx,4,south-1],'chiseled_stone_bricks')
   put([east+dx,4,south+depth],'chiseled_stone_bricks')
  }
 }
 for(const [east,south]of [[11,25],[21,20],[3,19],[29,26]]){
  put([east,3,south],'rooted_dirt');put([east,4,south],'mycelium');put([east,5,south],'red_mushroom')
 }
 return {structure,metadata:{name:'s7-18-crypt-ossuary',ports}}
}
