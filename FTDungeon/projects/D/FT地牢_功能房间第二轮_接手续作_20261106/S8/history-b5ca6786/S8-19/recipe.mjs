import {tools} from '../common.mjs'
import {architecture} from '../architecture.mjs'
export default function build(api){
 const size=[35,24,33],t=tools(api,size),{put,box,floor,pillar,crystal,hanging,container,port}=t
 box([0,0,0],[34,0,32],'smooth_basalt');floor([0,0],[34,32])
 for(let x=0;x<35;x++)for(let z=0;z<33;z++){
  const edge=Math.min(x,34-x,z,32-z)
  if(edge===0)for(let y=2;y<=21;y++)put(x,y,z,y===9||y===17?'calcite':(x+y+z)%13===0?'amethyst_block':'cobbled_deepslate')
  put(x,21+(edge>3?1:0),z,'smooth_basalt')
 }
 box([13,2,1],[33,5,14],'deepslate_bricks');box([13,6,2],[13,7,14],'deepslate_bricks');box([14,6,14],[24,6,14],'deepslate_brick_wall')
 box([30,6,14],[33,6,14],'deepslate_brick_wall')
 for(let step=0;step<8;step++){
  const z=22-step,halfHeight=5+step,base=Math.floor((halfHeight-1)/2)-1
  box([25,2,z],[29,base,z],'deepslate_bricks')
  box([25,base+1,z],[29,base+1,z],halfHeight%2?'polished_deepslate_slab[type=bottom,waterlogged=false]':'polished_deepslate')
 }
 box([15,5,5],[20,5,9],'purple_concrete');box([16,6,4],[20,6,5],'dark_oak_planks');put(17,7,4,'lightning_rod[facing=west,waterlogged=false,powered=false]')
 box([24,6,3],[28,6,3],'dark_oak_planks');box([24,9,3],[28,9,3],'dark_oak_planks');for(const x of [24,26,28])box([x,7,3],[x,8,3],'iron_bars')
 container(31,6,4);hanging(17,12,11,21)
 for(const [x,z,height] of [[4,5,7],[7,16,10],[4,27,6]])crystal(x,2,z,height)
 box([12,2,1],[12,5,30],'glass')
 const east=port('east',27),north={side:'north',foot:[27,6,0],width:3,height:4}
 box([26,6,0],[28,9,0],'air');box([26,2,0],[28,5,0],'deepslate_bricks')
 put(27,6,0,'dark_oak_door[facing=south,half=lower,hinge=left,open=true,powered=false]');put(27,7,0,'dark_oak_door[facing=south,half=upper,hinge=left,open=true,powered=false]')
 architecture(api,t.structure,size,{ports:[{...north,frame_depth:1},east],roof:(x,z,edge)=>21+(edge>3?1:0),veins:[[1,7,4,8],[3,28,6,5],[26,1,5,3]]})
 for(const z of [2,26]){
  box([10,2,z],[11,15,z+1],'cobbled_deepslate')
  box([9,4,z],[12,5,z+1],'calcite')
  box([9,13,z],[12,14,z+1],'amethyst_block')
 }
 for(let z=4;z<=25;z++){
  const arch=16+Math.min(3,Math.min(z-4,25-z))
  box([10,arch,z],[11,arch+1,z],'cobbled_deepslate')
  put(11,arch-1,z,`deepslate_brick_stairs[facing=${z<14?'south':'north'},half=top,shape=straight,waterlogged=false]`)
 }
 for(const [x,z,height] of [[2,11,4],[3,23,5],[8,29,7]])crystal(x,2,z,height)
 box([13,7,2],[13,7,14],'air')
 return {structure:t.structure,metadata:{name:'s8-19-watch',size,ports:[north,east],start:[34,2,27],targets:[{label:'upper rear door',position:[27,6,0]},{label:'watch desk',position:[21,6,6]},{label:'parapet outlook',position:[15,6,12]},{label:'stair landing',position:[27,6,14]}],features:['raised outlook above crystal cavern','four-block vertical rise','half-step stone staircase','parapet and tinted-glass fall guard','spyglass-shaped lightning rod','weapon rack','rear door'],differences:['Large distant cavern represented by enclosed crystal scenery inside module, not infinite void','Spyglass is decorative lightning rod, not a usable telescope','Stair is half-step slabs to permit no-jump offline route certification']}}
}
