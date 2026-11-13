import {tools} from '../kit.mjs'
export default function build(api){
  const {structure,put,fill,container,arch,shell,lamp,pot}=tools(api,[25,12,21]);shell(12,6)
  fill([6,2,4],[11,9,5],'cut_sandstone');fill([6,2,5],[6,8,14],'chiseled_sandstone')
  fill([7,2,13],[18,2,14],'cut_sandstone');fill([10,2,7],[11,7,11],'cut_sandstone')
  fill([6,2,8],[11,5,10],'air')
  for(const east of [12,18]) {fill([east,2,5],[east,8,5],'chiseled_sandstone');put([east,5,5],'orange_terracotta')}
  fill([12,8,5],[18,8,5],'cut_sandstone');fill([13,2,6],[17,2,10],'smooth_sandstone')
  fill([12,2,4],[18,8,4],'orange_terracotta');put([13,7,5],'sandstone_stairs',{facing:'east',half:'top'});put([17,7,5],'sandstone_stairs',{facing:'west',half:'top'})
  for(let east=13;east<=17;east++) put([east,2,11],'sandstone_stairs',{facing:'north'})
  container([15,3,7],'chest','south');put([14,3,7],'gold_block');put([17,3,8],'gold_block')
  fill([8,2,16],[10,2,18],'chiseled_sandstone');put([9,3,17],'lever',{face:'floor',facing:'north',powered:'false'})
  put([12,7,6],'cobweb');pot(20,2,15);pot(18,2,10)
  lamp(15,11,8);lamp(3,11,9);lamp(21,11,9)
  arch('west',9);arch('east',9)
  return {structure,metadata:{name:'s2-20-secret-vault'}}
}
