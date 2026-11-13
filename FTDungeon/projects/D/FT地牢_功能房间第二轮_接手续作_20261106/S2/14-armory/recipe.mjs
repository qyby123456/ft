import {tools} from '../kit.mjs'
export default function build(api){
  const {structure,put,fill,container,arch,shell,lamp,pot}=tools(api,[27,12,25]);shell(12,6)
  for(const east of [5,6,7,8,9,17,18,19,20,21]) {put([east,2,1],'dark_oak_planks');put([east,6,1],'dark_oak_planks');for(let up=3;up<=5;up++) put([east,up,1],east<12?'iron_bars':'chain',east<12?undefined:{axis:'y'})}
  for(const east of [12,15]) {put([east,2,2],'smooth_stone');fill([east,3,2],[east,4,2],east===12?'iron_block':'gold_block');put([east,5,2],east===12?'iron_block':'gold_block');put([east-1,4,2],'polished_andesite');put([east+1,4,2],'polished_andesite')}
  for(const east of [23,24]) for(let up=2;up<=5;up++) container([east,up,2])
  fill([9,1,8],[17,1,16],'orange_terracotta');fill([10,1,9],[16,1,15],'smooth_sandstone')
  for(const east of [10,16]) fill([east,2,10],[east,2,13],'dark_oak_planks')
  fill([10,3,10],[16,3,13],'dark_oak_planks');put([13,4,11],'lantern',{hanging:'false'});put([15,4,12],'stonecutter',{facing:'west'})
  fill([22,2,17],[24,2,19],'dark_oak_planks');put([23,3,18],'anvil',{facing:'east'})
  for(const [east,south] of [[3,16],[4,18],[22,21]]) container([east,2,south])
  pot(5,2,21);lamp(6,11,9);lamp(21,11,9)
  arch('west',7);arch('east',7)
  return {structure,metadata:{name:'s2-14-armory'}}
}
