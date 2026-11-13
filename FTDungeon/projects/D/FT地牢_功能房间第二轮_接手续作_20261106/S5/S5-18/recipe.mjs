import {room} from '../tools.mjs'
export default function(api){
  const {put,fill,box,coral,finish}=room(api,[31,15,29])
  box([0,0,0],[30,12,28],'dark_prismarine');fill([1,1,1],[29,1,27],'prismarine');fill([1,2,1],[29,2,27],'prismarine_bricks')
  for(const start of [3,21]){
    fill([start,3,1],[start+6,9,3],'prismarine_bricks')
    for(const height of [4,7]){
      fill([start+1,height,2],[start+5,height+1,3],'air')
      for(const east of [start+1,start+3,start+5])put([east,height,2],'skeleton_skull',{rotation:'8'})
    }
  }
  for(const [east,south] of [[5,9],[23,20]]){
    fill([east,3,south],[east+3,3,south+5],'mossy_stone_bricks');fill([east,4,south],[east+3,4,south+5],'stone_brick_slab',{type:'bottom',waterlogged:'false'})
    put([east+1,4,south+1],'chiseled_stone_bricks');put([east+1,5,south+1],'skeleton_skull',{rotation:'8'})
  }
  fill([11,3,11],[19,3,17],'prismarine_bricks');fill([12,4,12],[18,4,16],'mossy_stone_bricks');fill([13,5,13],[17,5,15],'chiseled_stone_bricks')
  for(const east of [12,18])for(const south of [12,16])put([east,5,south],'candle',{candles:'2',lit:'true',waterlogged:'false'})
  for(const east of [10,20]){fill([east,9,3],[east,11,3],'chain',{axis:'y',waterlogged:'false'});put([east,8,3],'sea_lantern')}
  fill([2,2,20],[6,2,24],'water');coral([4,1,22],'brain');fill([25,2,5],[28,2,8],'water');coral([27,1,7],'fire')
  for(const east of [0,30]){fill([east,3,10],[east,6,14],'air');put([east,7,12],'sea_lantern')}
  fill([13,6,1],[17,9,1],'chiseled_stone_bricks')
  fill([11,4,1],[19,10,2],'prismarine_bricks');fill([12,5,2],[18,9,2],'dark_prismarine')
  for(const [east,up] of [[13,8],[14,8],[16,8],[17,8],[13,7],[17,7],[14,6],[15,6],[16,6]])put([east,up,2],'bone_block',{axis:'z'})
  put([14,7,2],'black_concrete');put([16,7,2],'black_concrete');put([15,6,2],'black_concrete');put([15,4,3],'chiseled_stone_bricks');put([15,5,3],'skeleton_skull',{rotation:'8'})
  fill([12,2,4],[18,2,5],'water');put([13,1,4],'brain_coral_block');put([13,2,4],'brain_coral',{waterlogged:'true'});put([17,1,4],'fire_coral_block');put([17,2,4],'fire_coral',{waterlogged:'true'})
  fill([2,12,4],[28,12,26],'cyan_stained_glass');for(const east of [4,15,26])fill([east,12,0],[east,12,28],'dark_prismarine')
  return finish('S5-18','Paired bone niches, offset sealed sarcophagi, candle altar, carved rear memorial and chained sea lamps.',[
    {name:'west',feet:[0,3,12],facing:'west',width:5,height:4},{name:'east',feet:[30,3,12],facing:'east',width:5,height:4}
  ],[{label:'altar-south',position:[15,3,19]},{label:'altar-north',position:[15,3,9]},{label:'west-niches',position:[6,3,5]},{label:'east-niches',position:[24,3,5]},{label:'sarcophagus',position:[22,3,22]}],[
    'Vanilla skeleton skull blocks represent ossuary bones; no hostile entity or loot mechanic.',
    'Each sarcophagus skull rests on a full chiseled support block rather than floating above a lower slab.',
    'Contained water only at perimeter; candle altar dry, complete memorial roof.'
  ],{cutHeight:12})
}
