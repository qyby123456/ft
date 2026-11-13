import {tools} from '../kit.mjs'
export default function build(api){
  const {structure,put,fill,container,arch,shell,lamp}=tools(api,[25,13,25]);shell(13,7)
  fill([4,2,1],[7,5,4],'stone_bricks');put([5,3,4],'furnace',{facing:'south',lit:'false'},{id:'minecraft:furnace',Items:new api.NbtList([],10)});put([6,3,4],'shroomlight');fill([5,6,1],[6,11,2],'stone_bricks')
  for(const east of [10,15]) fill([east,2,1],[east,2,4],'oak_planks');fill([10,3,1],[15,3,4],'oak_planks');put([11,4,3],'crafting_table');put([14,4,3],'stonecutter',{facing:'south'})
  for(const east of [19,22]) for(let up=2;up<=8;up++) put([east,up,1],'dark_oak_log',{axis:'y'})
  for(const up of [3,6,8]) {fill([19,up,1],[22,up,2],'dark_oak_planks');put([20,up+1,2],'smooth_stone_slab');put([21,up+1,2],'raw_iron_block')}
  put([20,2,6],'oak_planks');put([20,3,6],'grindstone',{face:'floor',facing:'south'})
  fill([10,1,10],[16,1,16],'orange_terracotta')
  for(const east of [10,16]) for(const south of [10,14]) put([east,2,south],'oak_fence')
  fill([10,3,10],[16,3,14],'oak_planks')
  for(const east of [12,14]) for(const south of [11,13]) put([east,4,south],'oak_fence')
  for(const east of [12,14]) for(let south=11;south<=13;south++) put([east,5,south],'oak_slab',{type:'top'})
  put([12,4,10],'stone_button',{face:'floor',facing:'north',powered:'false'});put([15,4,12],'stonecutter',{facing:'south'});put([11,4,13],'flower_pot')
  put([7,2,7],'oak_log',{axis:'y'});put([7,3,7],'anvil',{facing:'east'})
  for(const [east,south] of [[3,18],[21,18],[22,7]]) container([east,2,south])
  lamp(9,12,7);lamp(18,12,16);arch('west',7);arch('east',7)
  return {structure,metadata:{name:'s2-17-workshop'}}
}
