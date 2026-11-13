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
  for(const x of [5,11,17,23]) {
    fill([x,3,1],[x,10,2],'polished_blackstone_bricks')
    put(x,8,2,'chiseled_polished_blackstone')
  }
  fill([2,10,1],[26,10,2],'polished_blackstone_bricks')
  for(let x=2;x<=26;x++) put(x,11,2,'polished_blackstone_brick_slab',{type:'bottom',waterlogged:'false'})
  for(const x of [1,27]) {
    for(const z of [13,17]) {
      fill([x,3,z],[x,9,z],'polished_blackstone_bricks')
      put(x,7,z,'chiseled_polished_blackstone')
    }
    fill([x,9,13],[x,10,17],'polished_blackstone_bricks')
    put(x,8,14,'polished_blackstone_brick_stairs',{facing:'south',half:'top',shape:'straight',waterlogged:'false'})
    put(x,8,16,'polished_blackstone_brick_stairs',{facing:'north',half:'top',shape:'straight',waterlogged:'false'})
    fill([x,11,12],[x,11,18],'polished_blackstone_bricks')
  }
  for(const z of [18,21]) for(let x=3;x<=25;x++) put(x,3,z,'polished_blackstone_brick_slab',{type:'bottom',waterlogged:'false'})
  for(const x of [3,8,14,20,25]) for(const z of [18,21]) fill([x,3,z],[x,4,z],'chiseled_polished_blackstone')
  port('west',15)
  port('east',15)
  return build.finish()
}
