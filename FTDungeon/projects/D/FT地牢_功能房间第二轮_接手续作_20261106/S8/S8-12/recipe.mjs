import {tools} from '../common.mjs'
import {architecture} from '../architecture.mjs'
export default function build(api){
 const size=[33,15,33],t=tools(api,size),{put,box,floor,pillar,crystal,hanging,port}=t
 box([0,0,0],[32,0,32],'smooth_basalt');floor([0,0],[32,32])
 for(let y=2;y<=12;y++){
  for(let x=0;x<33;x++){put(x,y,0,y===6?'calcite':'deepslate_bricks');put(x,y,32,y===6?'calcite':'deepslate_bricks')}
  for(let z=0;z<33;z++){put(0,y,z,y===6?'calcite':'cobbled_deepslate');put(32,y,z,y===6?'calcite':'cobbled_deepslate')}
 }
 for(let x=0;x<33;x++)for(let z=0;z<33;z++)put(x,12+(x>8&&x<24?1:0),z,(x+z)%7===0?'amethyst_block':'deepslate_tiles')
 for(const z of [3,10,17])for(const side of ['left','right']){
  const outer=side==='left'?1:25,front=side==='left'?8:24,facing=side==='left'?'east':'west'
  box([outer,2,z],[outer+6,8,z],'deepslate_bricks');box([outer,2,z+6],[outer+6,8,z+6],'deepslate_bricks')
  box([front,2,z+1],[front,6,z+5],'iron_bars');box([outer,8,z],[outer+6,8,z+6],'deepslate_tiles')
  put(front,2,z+3,`iron_door[facing=${facing},half=lower,hinge=left,open=false,powered=false]`)
  put(front,3,z+3,`iron_door[facing=${facing},half=upper,hinge=left,open=false,powered=false]`)
  put(front,2,z+2,'deepslate_bricks');put(front+(side==='left'?1:-1),2,z+2,`lever[face=wall,facing=${facing},powered=false]`)
  const bed=side==='left'?3:28
  for(const [offset,part] of [[0,'head'],[1,'foot']])put(bed,2,z+2+offset,`purple_bed[facing=north,part=${part},occupied=false]`,{id:'minecraft:bed',color:10})
  put(bed+1,2,z+4,'cauldron');box([bed,5,z+1],[bed,7,z+1],'chain[axis=y,waterlogged=false]')
 }
 for(const x of [9,23])for(const z of [3,10,17,23])pillar(x,z,10)
 for(const z of [7,15,23])hanging(16,9,z,12)
 box([13,2,26],[19,2,27],'dark_oak_planks');put(14,3,26,'lantern[hanging=false]');put(18,3,26,'grindstone[face=floor,facing=north]')
 for(let z=4;z<26;z++)put(16,1,z,'amethyst_block')
 box([14,4,0],[18,8,0],'iron_bars')
 crystal(3,9,4,2);crystal(28,9,18,2);crystal(3,2,29,3);crystal(29,2,29,3)
 const ports=[port('west',26),port('east',26)]
 architecture(api,t.structure,size,{ports,roof:(x,z)=>12+(x>8&&x<24?1:0),veins:[[3,5,5,6],[29,21,4,7],[17,1,6,3]]})
 for(const front of [8,24])for(const z of [3,10,17]){
  for(let across=z+1;across<z+6;across++)put(front,7,across,across===z+1||across===z+5?`deepslate_brick_stairs[facing=${across===z+1?'south':'north'},half=top,shape=straight,waterlogged=false]`:'deepslate_bricks')
  box([front-1,9,z],[front+1,9,z+6],'deepslate_tile_slab[type=bottom,waterlogged=false]')
 }
 return {structure:t.structure,metadata:{name:'s8-12-prison',size,ports,start:[0,2,26],targets:[{label:'east exit',position:[32,2,26]},{label:'central cell aisle',position:[16,2,6]},{label:'guard desk',position:[16,2,28]}],features:['six barred cells on both sides','iron doors with adjacent control levers','bed and cauldron in each cell','guard desk','barred rear window','overhead chains'],differences:['Keys represented by grindstone-shaped desk fixture, no invented key item','Closed cell interiors not certified as freely reachable; only aisle and exit routes checked','Full rear geode shell restored; side exits retained']}}
}
