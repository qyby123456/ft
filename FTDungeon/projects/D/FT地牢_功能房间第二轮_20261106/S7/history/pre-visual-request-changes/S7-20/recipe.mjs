import {tools} from '../common.mjs'
export default function build(api){
 const {structure,put,box,ground,walls,mushroom,port}=tools(api,[35,19,29])
 ground();walls()
 box([3,3,3],[17,12,12],'rooted_dirt')
 box([19,3,3],[31,11,12],'purple_terracotta');box([20,3,4],[30,9,11],'air')
 box([23,3,11],[27,7,12],'air');box([23,8,11],[27,8,12],'dark_oak_log')
 box([25,2,4],[29,2,7],'stone_bricks')
 put([27,3,6],'chest',{facing:'south',type:'single',waterlogged:'false'},api.createBlockNbt({id:'minecraft:chest',Items:[]}))
 for(const [east,south] of [[24,6],[29,8],[25,8]])put([east,3,south],'gold_block')
 put([30,8,4],'cobweb');put([29,4,12],'lever',{face:'wall',facing:'south',powered:'false'})
 for(const east of [21,29])put([east,6,4],'shroomlight')
 box([3,3,19],[28,8,22],'rooted_dirt');box([4,3,19],[27,6,20],'air')
 box([4,3,17],[6,6,19],'air');box([25,3,17],[27,6,19],'air')
 mushroom(8,6,13,2,3);mushroom(14,10,3,5,2);mushroom(29,24,3,5,2)
 box([12,2,15],[17,2,17],'blue_stained_glass');box([12,1,15],[17,1,17],'water')
 const ports=[port('west',15),port('east',15)]
 return {structure,metadata:{name:'s7-20-secret-vault',ports}}
}
