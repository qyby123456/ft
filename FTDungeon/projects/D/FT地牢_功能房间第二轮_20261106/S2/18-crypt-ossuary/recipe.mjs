import {tools} from '../kit.mjs'
export default function build(api){
  const {structure,put,fill,arch,shell,lamp,pot,banner}=tools(api,[27,13,27]);shell(13,6)
  for(const [first,last] of [[3,8],[18,23]]) {
    fill([first,2,1],[last,9,3],'sandstone')
    for(const up of [3,5,7]) {fill([first+1,up,2],[last-1,up+1,3],'air');for(const east of [first+1,last-1]) {put([east,up,2],'skeleton_skull',{rotation:'8'},{id:'minecraft:skull'});put([east+1,up,2],'bone_block',{axis:'x'})}}
  }
  fill([11,2,1],[15,8,2],'orange_terracotta');fill([10,2,1],[10,10,2],'chiseled_sandstone');fill([16,2,1],[16,10,2],'chiseled_sandstone')
  fill([9,2,8],[17,2,14],'stone_bricks');fill([10,3,9],[16,3,13],'smooth_sandstone');fill([11,4,10],[15,4,12],'chiseled_sandstone')
  for(const east of [11,15]) put([east,5,11],'candle',{candles:'3',lit:'true'})
  for(const east of [5,18]) {fill([east,2,18],[east+4,2,22],'stone_bricks');fill([east+1,3,18],[east+3,3,22],'chiseled_sandstone');fill([east+1,4,18],[east+3,4,22],'smooth_sandstone_slab');put([east,3,20],'candle',{candles:'2',lit:'true'})}
  for(const [east,south] of [[4,15],[23,15]]) pot(east,2,south)
  for(const east of [8,19]) lamp(east,12,7)
  for(const east of [4,22]) put([east,2,24],'skeleton_skull',{rotation:'8'},{id:'minecraft:skull'})
  arch('west',9);arch('east',9)
  banner(13,8,3)
  return {structure,metadata:{name:'s2-18-crypt-ossuary'}}
}
