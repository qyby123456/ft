import {room} from '../lib.mjs'
export default function build(api) {
  const build=room(api,[29,16,29],'s6-16-well-cistern')
  const {put,fill,pillar,light,chest,port,lava}=build
  lava([2,1,2],[3,1,5])
  fill([8,2,7],[20,4,18],'mossy_stone_bricks')
  fill([9,3,8],[19,4,17],'air')
  fill([9,3,8],[19,3,17],'water')
  fill([13,0,18],[15,2,25],'mossy_stone_bricks')
  fill([14,1,19],[14,1,24],'water')
  fill([14,2,19],[14,2,24],'light_blue_stained_glass')
  for(const east of [10,18]) for(let up=3;up<=10;up++) put(east,up,11,'spruce_log',{axis:'y'})
  for(let east=9;east<=19;east++) put(east,10,11,'spruce_log',{axis:'x'})
  fill([14,7,11],[14,9,11],'chain')
  put(14,6,11,'cauldron')
  for(const east of [8,20]) for(const south of [7,18]) put(east,5,south,'lantern',{hanging:'false'})
  for(const east of [6,22]) put(east,3,20,'barrel',{facing:'up',open:'false'},api.createBlockNbt({id:'minecraft:barrel',Items:[]}))
  chest(22,3,22)
  for(const east of [3,25]) for(const south of [7,25]) pillar(east,south)
  light(14,23)
  port('west',21)
  port('east',21)
  return build.finish()
}
