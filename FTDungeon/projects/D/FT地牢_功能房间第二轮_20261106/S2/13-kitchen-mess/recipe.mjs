import {tools} from '../kit.mjs'
export default function build(api){
  const {structure,put,fill,container,arch,shell,lamp,pot}=tools(api,[29,13,25]);shell(13)
  fill([7,2,1],[13,3,5],'stone_bricks');fill([7,4,1],[7,8,5],'sandstone');fill([13,4,1],[13,8,5],'sandstone')
  fill([7,8,1],[13,8,5],'chiseled_sandstone');fill([8,9,1],[12,11,4],'cut_sandstone');fill([8,4,1],[12,7,1],'orange_terracotta')
  fill([9,4,2],[11,4,4],'shroomlight')
  for(let east=9;east<=11;east++) for(let south=2;south<=4;south++) put([east,5,south],east===10&&south===3?'water_cauldron':'polished_blackstone',east===10&&south===3?{level:'2'}:undefined)
  put([8,5,3],'polished_blackstone_button',{face:'wall',facing:'west',powered:'false'});put([12,5,3],'polished_blackstone_button',{face:'wall',facing:'east',powered:'false'})
  for(let up=9;up<=11;up++) fill([9,up,1],[11,up,2],'cut_sandstone')
  for(const up of [3,6]) {fill([18,up,1],[24,up,2],'dark_oak_planks');for(const east of [18,21,24]) pot(east,up+1,2)}
  for(const east of [18,24]) for(let up=2;up<=6;up++) put([east,up,1],'dark_oak_log',{axis:'y'})
  for(const [east,south] of [[3,4],[4,18],[25,5],[25,20]]) container([east,2,south])
  for(const [east,south,width] of [[9,7,5],[5,14,7],[17,14,7]]) {
    for(const offset of [0,width-1]) {put([east+offset,2,south],'oak_fence');put([east+offset,2,south+2],'oak_fence')}
    fill([east,3,south],[east+width-1,3,south+2],'oak_planks')
    if(south===14) {fill([east,2,south-1],[east+width-1,2,south-1],'oak_slab');fill([east,2,south+3],[east+width-1,2,south+3],'oak_slab');put([east+3,4,south+1],'flower_pot')}
    else put([east+2,4,south+1],'stonecutter',{facing:'south'})
  }
  for(let east=3;east<26;east++) if(east%3!==0) put([east,1,11],'orange_terracotta')
  for(const [east,south] of [[5,7],[22,8],[14,20]]) lamp(east,12,south)
  arch('west',8);arch('east',8)
  return {structure,metadata:{name:'s2-13-kitchen-mess'}}
}
