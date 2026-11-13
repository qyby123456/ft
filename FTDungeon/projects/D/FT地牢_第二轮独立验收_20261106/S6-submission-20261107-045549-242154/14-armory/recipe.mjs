import {room} from '../lib.mjs'
export default function build(api) {
  const build=room(api,[27,14,25],'s6-14-armory')
  const {put,fill,pillar,light,chest,port,lava}=build
  lava([9,1,10],[17,1,15])
  for(const east of [4,22]) {
    fill([east,3,3],[east,3,7],'polished_blackstone_bricks')
    fill([east,7,3],[east,7,7],'polished_blackstone_bricks')
    for(const south of [3,5,7]) fill([east,4,south],[east,6,south],'iron_bars')
  }
  fill([11,3,1],[15,7,2],'chiseled_polished_blackstone')
  chest(12,3,3)
  chest(14,3,3)
  for(const east of [7,19]) {
    put(east,3,5,'polished_blackstone_bricks')
    put(east,4,5,'lightning_rod',{facing:'up',waterlogged:'false',powered:'false'})
    put(east,5,5,east===7?'gold_block':'iron_block')
    put(east,6,5,east===7?'gilded_blackstone':'polished_basalt')
  }
  for(const east of [10,16]) for(const south of [11,14]) put(east,3,south,'polished_blackstone_bricks')
  fill([9,4,10],[17,4,15],'polished_blackstone_bricks')
  put(12,5,12,'smithing_table')
  put(15,5,12,'lantern',{hanging:'false'})
  put(20,3,14,'anvil',{facing:'north'})
  for(const east of [3,23]) chest(east,3,19)
  for(const east of [2,24]) for(const south of [2,22]) pillar(east,south)
  light(8,8)
  light(19,8)
  for(const east of [7,19]) {
    const metal=east===7?'gold_block':'iron_block'
    for(const offset of [-1,1]) put(east+offset,5,5,metal)
    put(east,7,5,'polished_blackstone_brick_slab',{type:'bottom',waterlogged:'false'})
    put(east,6,6,'iron_bars')
    for(const offset of [-1,1]) put(east+offset,4,5,'polished_blackstone_brick_stairs',{facing:'south',half:'bottom',shape:'straight',waterlogged:'false'})
  }
  for(const east of [4,22]) {
    for(const south of [3,5,7]) {
      put(east,4,south,'lightning_rod',{facing:'up',waterlogged:'false',powered:'false'})
      put(east,5,south,'end_rod',{facing:'up'})
      put(east,6,south,'iron_bars')
    }
    for(const south of [2,8]) fill([east,3,south],[east,8,south],'polished_blackstone_bricks')
    for(let south=2;south<=8;south++) put(east,8,south,'polished_blackstone_brick_slab',{type:'bottom',waterlogged:'false'})
  }
  for(const east of [10,16]) fill([east,3,1],[east,9,2],'polished_blackstone_bricks')
  fill([10,9,1],[16,9,3],'polished_blackstone_bricks')
  for(let east=9;east<=17;east++) put(east,10,2,'polished_blackstone_brick_slab',{type:'bottom',waterlogged:'false'})
  for(const east of [1,25]) {
    for(const south of [14,20]) fill([east,3,south],[east,8,south],'polished_blackstone_bricks')
    fill([east,8,14],[east,9,20],'polished_blackstone_bricks')
    put(east,7,15,'polished_blackstone_brick_stairs',{facing:'south',half:'top',shape:'straight',waterlogged:'false'})
    put(east,7,19,'polished_blackstone_brick_stairs',{facing:'north',half:'top',shape:'straight',waterlogged:'false'})
  }
  port('west',17)
  port('east',17)
  return build.finish()
}
