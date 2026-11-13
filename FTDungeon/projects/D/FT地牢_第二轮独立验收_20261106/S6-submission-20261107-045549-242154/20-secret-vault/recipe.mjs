import {room} from '../lib.mjs'
export default function build(api) {
  const build=room(api,[33,16,23],'s6-20-secret-vault')
  const {put,fill,pillar,light,chest,port,lava}=build
  lava([3,1,16],[29,1,19])
  fill([1,3,1],[10,10,8],'basalt')
  fill([1,3,10],[10,10,11],'polished_blackstone_bricks')
  fill([1,3,9],[10,6,9],'air')
  fill([22,3,1],[31,10,8],'basalt')
  fill([22,3,10],[31,10,11],'polished_blackstone_bricks')
  fill([22,3,9],[31,6,9],'air')
  fill([11,3,1],[21,9,2],'polished_blackstone_bricks')
  fill([11,3,3],[11,9,7],'obsidian')
  fill([21,3,3],[21,9,7],'obsidian')
  fill([12,3,3],[20,3,7],'chiseled_polished_blackstone')
  for(let east=13;east<=19;east++) put(east,3,8,'polished_blackstone_brick_slab',{type:'bottom',waterlogged:'false'})
  chest(16,4,4)
  put(14,4,4,'gold_block')
  put(14,4,5,'raw_gold_block')
  put(19,7,3,'cobweb')
  put(20,4,6,'lever',{face:'wall',facing:'west',powered:'false'})
  fill([3,3,12],[29,3,12],'polished_blackstone_bricks')
  fill([14,3,12],[18,3,12],'air')
  fill([3,3,13],[29,3,13],'air')
  for(const east of [12,20]) pillar(east,3)
  for(const east of [5,27]) light(east,9)
  light(16,9)
  for(const east of [12,20]) {
    fill([east,4,7],[east,8,7],'polished_blackstone_bricks')
    put(east,7,7,'chiseled_polished_blackstone')
  }
  fill([12,9,6],[20,10,7],'polished_blackstone_bricks')
  put(13,8,7,'polished_blackstone_brick_stairs',{facing:'east',half:'top',shape:'straight',waterlogged:'false'})
  put(19,8,7,'polished_blackstone_brick_stairs',{facing:'west',half:'top',shape:'straight',waterlogged:'false'})
  for(let east=11;east<=21;east++) put(east,11,7,'polished_blackstone_brick_slab',{type:'bottom',waterlogged:'false'})
  for(const east of [2,6,9,23,27,30]) {
    fill([east,3,10],[east,8,10],'polished_blackstone_bricks')
    put(east,6,10,'chiseled_polished_blackstone')
  }
  fill([1,8,10],[10,8,11],'polished_blackstone_bricks')
  fill([22,8,10],[31,8,11],'polished_blackstone_bricks')
  for(const east of [1,31]) {
    for(const south of [7,11]) fill([east,3,south],[east,7,south],'polished_blackstone_bricks')
    fill([east,7,7],[east,8,11],'polished_blackstone_bricks')
  }
  port('west',9)
  port('east',9)
  return build.finish()
}
