import {room} from '../lib.mjs'
export default function build(api) {
  const build = room(api,[29,15,25],'s6-11-library')
  const {put,fill,pillar,light,chest,port,lava}=build
  lava([4,1,19],[24,1,20])
  for(const x of [4,24]) for(const z of [3,17]) pillar(x,z)
  fill([5,3,1],[23,8,1],'bookshelf')
  fill([1,3,2],[1,8,12],'bookshelf')
  fill([27,3,2],[27,8,12],'bookshelf')
  for(const x of [5,11,17,23]) fill([x,3,1],[x,9,1],'polished_blackstone_bricks')
  fill([13,3,2],[15,6,2],'chiseled_polished_blackstone')
  chest(14,3,3)
  for(const x of [9,19]) {
    fill([x-2,3,9],[x+2,3,9],'polished_blackstone_bricks')
    put(x,4,9,'lectern',{facing:'south',has_book:'false',powered:'false'})
    for(const end of [x-2,x+2]) put(end,4,9,'lantern',{hanging:'false'})
    for(const seat of [x-1,x+1]) put(seat,3,11,'polished_blackstone_brick_stairs',{facing:'north',half:'bottom',shape:'straight',waterlogged:'false'})
  }
  fill([25,3,1],[25,9,1],'polished_blackstone_bricks')
  for(let y=3;y<=8;y++) put(25,y,2,'ladder',{facing:'south',waterlogged:'false'})
  for(const x of [8,20]) light(x,5)
  port('west',15)
  port('east',15)
  return build.finish()
}
