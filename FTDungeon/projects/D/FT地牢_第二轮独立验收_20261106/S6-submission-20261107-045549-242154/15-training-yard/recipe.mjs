import {room} from '../lib.mjs'
export default function build(api) {
  const build=room(api,[35,16,31],'s6-15-training-yard')
  const {put,fill,pillar,light,chest,port,lava}=build
  fill([6,2,6],[28,2,24],'sand')
  for(let east=7;east<29;east+=3) for(let south=7;south<25;south+=4) put(east,2,south,'gravel')
  lava([2,1,2],[3,1,3])
  for(const east of [12,18,24]) {
    put(east,3,6,'spruce_fence')
    put(east,4,6,'target')
    put(east,3,12,'spruce_fence')
    put(east,4,12,'hay_block',{axis:'y'})
    put(east,5,12,'carved_pumpkin',{facing:'south'})
    put(east-1,4,12,'spruce_fence')
    put(east+1,4,12,'spruce_fence')
  }
  fill([9,3,18],[13,3,18],'polished_blackstone_brick_wall')
  fill([18,3,18],[21,3,18],'spruce_planks')
  fill([24,3,18],[26,4,20],'spruce_planks')
  for(const south of [24,27]) for(let east=8;east<=25;east++) put(east,3,south,'polished_blackstone_brick_stairs',{facing:'north',half:'bottom',shape:'straight',waterlogged:'false'})
  fill([7,3,3],[27,3,3],'polished_blackstone_bricks')
  for(const east of [8,10,26]) fill([east,4,3],[east,6,3],'iron_bars')
  chest(29,3,5)
  for(const east of [3,31]) for(const south of [5,27]) pillar(east,south)
  for(const east of [9,26]) light(east,16)
  for(let east=6;east<=28;east++) for(let south=6;south<=23;south++) {
    const seed=Math.imul(east+71,374761393)^Math.imul(south+29,668265263)
    const noise=(Math.imul(seed^(seed>>>13),1274126177)>>>0)%100
    put(east,2,south,noise<28?'gravel':noise<43?'coarse_dirt':'sand')
  }
  for(const east of [12,18,24]) fill([east-2,4,12],[east+2,4,12],'spruce_fence')
  for(const east of [17,22]) fill([east,3,18],[east,4,18],'spruce_fence')
  fill([18,4,18],[21,4,18],'spruce_fence')
  for(let east=24;east<=26;east++) {
    put(east,3,21,'spruce_stairs',{facing:'north',half:'bottom',shape:'straight',waterlogged:'false'})
    put(east,4,20,'spruce_stairs',{facing:'north',half:'bottom',shape:'straight',waterlogged:'false'})
  }
  for(const east of [1,33]) {
    for(const south of [12,18]) fill([east,3,south],[east,9,south],'polished_blackstone_bricks')
    fill([east,9,12],[east,10,18],'polished_blackstone_bricks')
    put(east,8,13,'polished_blackstone_brick_stairs',{facing:'south',half:'top',shape:'straight',waterlogged:'false'})
    put(east,8,17,'polished_blackstone_brick_stairs',{facing:'north',half:'top',shape:'straight',waterlogged:'false'})
  }
  for(const east of [8,26]) fill([east,3,1],[east,10,1],'polished_blackstone_bricks')
  fill([8,10,1],[26,10,2],'polished_blackstone_bricks')
  port('west',15)
  port('east',15)
  return build.finish()
}
