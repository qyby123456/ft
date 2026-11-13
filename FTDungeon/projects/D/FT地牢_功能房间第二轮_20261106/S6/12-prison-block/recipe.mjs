import {room} from '../lib.mjs'
export default function build(api) {
  const build=room(api,[33,14,31],'s6-12-prison-block')
  const {put,fill,pillar,light,chest,port,lava}=build
  lava([3,1,3],[29,1,3])
  lava([3,1,27],[29,1,27])
  for(const south of [6,19]) {
    for(const east of [5,13,21]) {
      fill([east,3,south],[east,8,south+5],'polished_blackstone_bricks')
      fill([east+7,3,south],[east+7,8,south+5],'polished_blackstone_bricks')
      const front=south===6?south+5:south
      const back=south===6?south:south+5
      fill([east,3,back],[east+7,8,back],'polished_blackstone_bricks')
      fill([east+1,3,front],[east+6,7,front],'iron_bars')
      fill([east,8,front],[east+7,8,front],'polished_blackstone_bricks')
      for(const half of ['lower','upper']) put(east+3,half==='lower'?3:4,front,'iron_door',{facing:south===6?'south':'north',half,hinge:'left',open:'false',powered:'false'})
      put(east+2,3,front+(south===6?1:-1),'lever',{face:'floor',facing:'east',powered:'false'})
      fill([east+1,3,south+2],[east+2,3,south+3],'polished_blackstone_bricks')
      put(east+5,3,south+2,'cauldron')
      fill([east+5,6,south+3],[east+5,9,south+3],'chain')
      put(east+5,10,south+3,'polished_blackstone_bricks')
    }
  }
  for(const east of [3,29]) {
    fill([east,3,12],[east+1,3,13],'polished_blackstone_bricks')
    put(east,4,12,'lantern',{hanging:'false'})
    chest(east,3,17)
  }
  for(const east of [5,29]) for(const south of [5,25]) pillar(east,south)
  for(const east of [8,16,24]) light(east,15)
  port('west',15)
  port('east',15)
  return build.finish()
}
