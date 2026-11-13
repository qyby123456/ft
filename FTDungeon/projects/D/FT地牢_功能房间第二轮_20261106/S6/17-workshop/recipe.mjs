import {room} from '../lib.mjs'
export default function build(api) {
  const build=room(api,[29,16,27],'s6-17-workshop')
  const {put,fill,pillar,light,chest,port,lava}=build
  lava([5,1,20],[11,1,23])
  lava([17,1,20],[23,1,23])
  fill([16,3,2],[20,9,4],'polished_blackstone_bricks')
  lava([18,4,3],[18,5,3])
  fill([18,4,4],[18,5,4],'orange_stained_glass')
  fill([17,10,2],[19,13,3],'basalt')
  put(18,3,5,'furnace',{facing:'south',lit:'false'},api.createBlockNbt({id:'minecraft:furnace',Items:[],BurnTime:0,CookTime:0,CookTimeTotal:200}))
  for(const up of [3,6,9]) fill([8,up,2],[13,up,3],'polished_blackstone_bricks')
  for(const east of [8,13]) fill([east,3,2],[east,9,2],'polished_blackstone_bricks')
  for(const east of [9,11]) for(const up of [4,7]) put(east,up,2,'raw_iron_block')
  fill([3,3,5],[6,3,5],'polished_blackstone_bricks')
  put(4,4,5,'crafting_table')
  put(5,4,5,'smithing_table')
  put(23,3,6,'grindstone',{face:'floor',facing:'south'})
  for(const east of [11,17]) for(const south of [10,13]) put(east,3,south,'polished_blackstone_bricks')
  fill([10,4,10],[18,4,13],'polished_blackstone_bricks')
  put(13,5,11,'stonecutter',{facing:'south'})
  put(15,5,11,'raw_iron_block')
  put(17,5,11,'lantern',{hanging:'false'})
  for(const east of [4,24]) {
    chest(east,3,16)
    put(east,3,18,'barrel',{facing:'up',open:'false'},api.createBlockNbt({id:'minecraft:barrel',Items:[]}))
  }
  for(const east of [3,25]) for(const south of [3,24]) pillar(east,south)
  light(8,8)
  light(22,11)
  port('west',14)
  port('east',14)
  return build.finish()
}
