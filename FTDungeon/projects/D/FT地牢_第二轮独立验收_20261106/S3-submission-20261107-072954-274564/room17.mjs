import {toolkit} from './common.mjs'
export default function build(api){
  const {put,box,lamp,icicle,finish}=toolkit(api,[27,13,27],'s3-17')
  box(0,0,0,26,1,26,'stone_bricks')
  box(0,2,0,26,10,1,'deepslate_bricks');box(0,2,25,26,10,26,'deepslate_bricks')
  box(0,2,2,1,10,24,'deepslate_bricks');box(25,2,2,26,10,24,'deepslate_bricks')
  box(0,11,0,26,11,26,'packed_ice');box(0,12,0,26,12,26,'snow_block')
  for(const [left,right] of [[5,11],[18,23]]){
    box(left,2,22,right,2,24,'spruce_planks');box(left,5,23,right,5,24,'spruce_slab[type=top]');box(left,8,23,right,8,24,'spruce_slab[type=top]')
    for(let east=left;east<=right;east+=2){put(east,3,23,'polished_andesite');put(east,6,23,'blue_ice');put(east,9,23,'flower_pot')}
  }
  box(13,2,23,15,5,24,'stone_bricks')
  box(12,2,24,12,8,25,'chiseled_stone_bricks')
  box(16,2,24,16,8,25,'chiseled_stone_bricks')
  box(12,7,23,16,8,25,'stone_brick_slab[type=top]')
  box(4,5,22,11,5,22,'spruce_slab[type=top]')
  for(const east of [5,7,9,11])put(east,6,22,'flower_pot')
  put(14,2,22,'furnace[facing=north,lit=false]',{id:'minecraft:furnace',Items:[]})
  put(14,3,22,'blast_furnace[facing=north,lit=false]',{id:'minecraft:blast_furnace',Items:[]})
  for(const east of [10,17]){put(east,2,11,'spruce_fence');put(east,2,14,'spruce_fence')}
  box(10,3,11,17,3,14,'spruce_slab[type=top]')
  put(13,4,12,'stonecutter[facing=north]');put(16,4,13,'polished_andesite_slab[type=bottom]')
  put(10,4,13,'lantern[hanging=false]');put(18,2,10,'crafting_table')
  put(6,2,18,'grindstone[face=floor,facing=north]')
  box(5,4,24,11,4,24,'spruce_fence')
  for(const [east,south] of [[3,19],[4,19],[21,20],[22,20],[19,7]])put(east,2,south,'barrel[facing=up]',{id:'minecraft:barrel',Items:[]})
  box(3,1,3,9,1,6,'blue_ice');box(19,2,3,23,2,5,'snow_block')
  for(const east of [20,22]){put(east,3,4,'bone_block[axis=y]');put(east,3,5,'bone_block[axis=z]')}
  lamp(8,18,11);lamp(20,18,11)
  for(const east of [3,9,16,23])icicle(east,24,10,3)
  box(0,2,9,1,5,11,'air');box(25,2,9,26,5,11,'air')
  return finish({title:'寒穴维修工坊',reference:'S3-17',features:['双边三层料架','双炉','砂轮','中央半成品切石桌','独立工作台','前冰池与骨架'],ports:[{name:'west',foot:[0,2,10],width:3,height:4,normal:[-1,0,0]},{name:'east',foot:[26,2,10],width:3,height:4,normal:[1,0,0]}],start:[0,2,10],targets:[{label:'east exit',position:[26,2,10]},{label:'project table',position:[13,2,9]},{label:'furnace',position:[14,2,20]},{label:'grinding',position:[7,2,18]}],differences:['熔炉与高炉为空且熄灭，未声称自动生产','材料架使用实体方块，无可领取材料','半成品由切石机及半砖表现，工具没有物品实体'],cutY:11})
}
