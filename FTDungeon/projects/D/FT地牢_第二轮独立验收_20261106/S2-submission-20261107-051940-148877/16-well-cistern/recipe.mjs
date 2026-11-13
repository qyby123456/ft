import {tools} from '../kit.mjs'
export default function build(api){
  const {structure,put,fill,container,arch,shell,lamp,pot}=tools(api,[25,13,25]);shell(13,7)
  fill([8,2,8],[16,3,16],'sandstone');fill([9,2,9],[15,2,15],'water');fill([9,3,9],[15,3,15],'air')
  for(let east=8;east<=16;east++) for(const south of [8,16]) put([east,3,south],east%3===0?'mossy_stone_bricks':'cut_sandstone')
  for(const east of [8,16]) for(let up=4;up<=8;up++) put([east,up,12],'oak_log',{axis:'y'})
  for(let east=8;east<=16;east++) put([east,9,12],'dark_oak_log',{axis:'x'})
  for(let up=5;up<=8;up++) put([12,up,12],'chain',{axis:'y'});container([12,4,12],'barrel','up')
  fill([19,0,8],[21,0,20],'sandstone');fill([19,1,8],[21,1,20],'water');fill([18,1,8],[18,1,20],'sandstone');fill([22,1,8],[22,1,20],'sandstone')
  fill([19,1,7],[21,1,7],'sandstone');fill([19,1,21],[21,1,21],'sandstone')
  for(const south of [10,18]) for(let east=18;east<=22;east++) put([east,2,south],'sandstone_slab',{type:'bottom'})
  for(let step=0;step<6;step++) {fill([12+step,2,2],[12+step,2+step,4],'cut_sandstone');put([12+step,2+step,3],'sandstone_stairs',{facing:'east'})}
  fill([18,7,2],[20,7,4],'cut_sandstone');fill([18,8,2],[20,9,2],'sandstone')
  fill([3,2,2],[8,2,3],'cut_sandstone');container([4,3,2]);container([6,3,2]);pot(8,3,2);container([3,2,19]);pot(4,2,17)
  lamp(5,12,7);lamp(20,12,6);arch('west',7);arch('east',6)
  return {structure,metadata:{name:'s2-16-well-cistern'}}
}
