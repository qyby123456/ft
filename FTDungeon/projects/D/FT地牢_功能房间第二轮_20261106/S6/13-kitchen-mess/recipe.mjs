import {room} from '../lib.mjs'
export default function build(api) {
  const build=room(api,[31,16,28],'s6-13-kitchen-mess')
  const {put,fill,pillar,light,chest,port,lava}=build
  fill([12,3,1],[18,11,5],'polished_blackstone_bricks')
  fill([14,10,1],[16,14,3],'basalt')
  lava([15,4,3],[15,6,3])
  fill([15,4,4],[15,6,4],'orange_stained_glass')
  fill([15,4,5],[15,6,5],'orange_stained_glass')
  fill([13,3,6],[17,3,6],'polished_blackstone_bricks')
  put(15,4,6,'water_cauldron',{level:'3'})
  for(const east of [5,23]) {
    fill([east,3,2],[east+2,3,3],'spruce_planks')
    fill([east,6,2],[east+2,6,3],'spruce_planks')
    for(const offset of [0,2]) put(east+offset,4,2,'flower_pot')
    chest(east,3,5)
    put(east+2,3,5,'barrel',{facing:'up',open:'false'},api.createBlockNbt({id:'minecraft:barrel',Items:[]}))
  }
  for(const south of [13,21]) {
    for(const east of [7,22]) fill([east,3,south],[east,3,south+1],'spruce_fence')
    for(let east=6;east<=23;east++) for(let offset=0;offset<=1;offset++) put(east,4,south+offset,'spruce_slab',{type:'top',waterlogged:'false'})
    for(const bench of [south-2,south+3]) for(let east=6;east<=23;east++) put(east,3,bench,'spruce_slab',{type:'bottom',waterlogged:'false'})
    for(const east of [10,19]) put(east,5,south,'lantern',{hanging:'false'})
  }
  fill([10,3,7],[12,3,7],'polished_blackstone_bricks')
  put(11,4,7,'smoker',{facing:'south',lit:'false'},api.createBlockNbt({id:'minecraft:smoker',Items:[],BurnTime:0,CookTime:0,CookTimeTotal:200}))
  for(const east of [3,27]) for(const south of [3,24]) pillar(east,south)
  for(const east of [8,22]) light(east,9)
  port('west',9)
  port('east',9)
  return build.finish()
}
