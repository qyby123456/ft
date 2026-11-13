import {room} from '../lib.mjs'
export default function build(api) {
  const build=room(api,[29,15,29],'s6-18-crypt-ossuary')
  const {put,fill,pillar,light,port,lava}=build
  lava([4,1,22],[24,1,24])
  for(const east of [5,13,21]) {
    fill([east,3,1],[east,10,3],'obsidian')
    fill([east+5,3,1],[east+5,10,3],'polished_blackstone_bricks')
    for(const up of [3,6,9]) {
      fill([east+1,up,1],[east+4,up,3],'polished_blackstone_bricks')
      for(const offset of [1,3]) put(east+offset,up+1,2,'skeleton_skull',{rotation:'0'},api.createBlockNbt({id:'minecraft:skull'}))
      put(east+2,up+1,2,'bone_block',{axis:'x'})
    }
  }
  for(const [east,south] of [[5,9],[21,9],[5,17],[21,17]]) {
    fill([east,3,south],[east+2,4,south+3],'polished_blackstone_bricks')
    fill([east,5,south],[east+2,5,south+3],'chiseled_polished_blackstone')
    put(east+1,6,south+2,'candle',{candles:'3',lit:'true',waterlogged:'false'})
  }
  fill([11,3,12],[17,4,15],'polished_blackstone_bricks')
  fill([11,5,12],[17,5,15],'chiseled_polished_blackstone')
  for(const east of [11,17]) for(const south of [12,15]) put(east,6,south,'candle',{candles:'2',lit:'true',waterlogged:'false'})
  for(const east of [3,25]) for(const south of [5,25]) pillar(east,south)
  for(const east of [8,20]) light(east,8)
  port('west',14)
  port('east',14)
  return build.finish()
}
