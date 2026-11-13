import {tools} from '../kit.mjs'
export default function build(api){
  const {structure,put,fill,container,arch,shell,pillar,lamp,pot}=tools(api,[29,20,29]);shell(20,10)
  fill([1,7,1],[20,7,20],'cut_sandstone');fill([21,7,1],[26,7,4],'cut_sandstone')
  for(const east of [3,10,18]) for(const south of [3,18]) {fill([east,2,south],[east,6,south],'sandstone');put([east,6,south],'chiseled_sandstone')}
  for(const south of [3,18]) for(const [first,last] of [[3,10],[10,18]]) {
    for(const east of [first+1,last-1]) put([east,5,south],'sandstone_stairs',{facing:east===first+1?'east':'west',half:'top'})
    for(const east of [first+1,first+2,last-2,last-1]) put([east,6,south],'sandstone_stairs',{facing:east<first+3?'east':'west',half:'top'})
  }
  fill([1,8,20],[20,9,20],'sandstone');fill([20,8,5],[20,9,20],'sandstone')
  for(let step=0;step<6;step++) for(let east=22;east<=25;east++) {fill([east,2,5+step],[east,7-step,5+step],'cut_sandstone');put([east,7-step,5+step],'sandstone_stairs',{facing:'north'})}
  fill([26,2,5],[26,3,11],'sandstone');fill([21,2,5],[21,3,11],'sandstone')
  for(let south=5;south<=11;south++) for(const east of [21,26]) put([east,Math.max(3,12-south),south],'sandstone_wall')
  for(const east of [6,10]) fill([east,8,13],[east,8,15],'dark_oak_planks');fill([6,9,13],[10,9,15],'dark_oak_planks')
  put([9,10,14],'lantern',{hanging:'false'});put([6,10,14],'lightning_rod',{facing:'west',powered:'false',waterlogged:'false'})
  for(const east of [5,6,7,8]) {put([east,8,1],'oak_planks');put([east,12,1],'oak_planks');fill([east,9,1],[east,11,1],'iron_bars')}
  container([18,8,3]);pot(11,8,2);lamp(13,19,5)
  fill([2,8,28],[26,16,28],'gray_stained_glass');fill([0,8,3],[0,16,25],'gray_stained_glass')
  arch('north',14,8,3,4);arch('east',23,2,3,4)
  return {structure,metadata:{name:'s2-19-watch-balcony'}}
}
