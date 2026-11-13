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
  port('west',15)
  port('east',15)
  return build.finish()
}
