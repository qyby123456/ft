import {room} from '../lib.mjs'
export default function build(api) {
  const build=room(api,[31,20,31],'s6-19-watch-balcony')
  const {put,fill,pillar,light,chest,port,lava}=build
  fill([4,3,4],[26,6,12],'polished_blackstone_bricks')
  for(const east of [4,26]) fill([east,7,4],[east,8,12],'polished_blackstone_bricks')
  fill([4,7,12],[19,8,12],'polished_blackstone_bricks')
  fill([24,7,12],[26,8,12],'polished_blackstone_bricks')
  fill([5,7,3],[25,12,3],'orange_stained_glass')
  lava([5,8,1],[25,11,1])
  fill([5,8,2],[25,11,2],'orange_stained_glass')
  for(let south=13;south<=20;south++) {
    const step=20-south
    const height=3+Math.floor(step/2)
    if(height>3) fill([20,3,south],[23,height-1,south],'polished_blackstone_bricks')
    for(let east=20;east<=23;east++) put(east,height,south,'polished_blackstone_brick_slab',{type:step%2===0?'bottom':'top',waterlogged:'false'})
  }
  for(const east of [19,24]) fill([east,3,13],[east,9,20],'polished_blackstone_bricks')
  fill([8,7,7],[13,7,8],'polished_blackstone_bricks')
  put(10,8,7,'lightning_rod',{facing:'up',powered:'false',waterlogged:'false'})
  put(10,9,7,'lightning_rod',{facing:'west',powered:'false',waterlogged:'false'})
  put(12,8,7,'lantern',{hanging:'false'})
  fill([16,7,5],[21,7,5],'polished_blackstone_bricks')
  for(const east of [16,18,20]) fill([east,8,5],[east,10,5],'iron_bars')
  chest(25,7,6)
  for(const east of [3,27]) for(const south of [4,27]) pillar(east,south)
  light(9,18)
  light(26,23)
  port('west',25)
  port('east',25)
  return build.finish()
}
