import {tools} from '../common.mjs'
import {architecture} from '../architecture.mjs'
export default function build(api){
 const size=[35,16,31],t=tools(api,size),{put,box,floor,pillar,crystal,hanging,container,port}=t
 box([0,0,0],[34,0,30],'smooth_basalt');floor([0,0],[34,30]);for(let x=3;x<32;x++)for(let z=3;z<28;z++)put(x,1,z,Math.sin(x*0.38)+Math.cos(z*0.41)>0.8?'gravel':Math.sin(x*0.5+z*0.3)>0.8?'clay':(x*7+z*11)%19===0?'amethyst_block':'light_gray_terracotta')
 for(let x=0;x<35;x++)for(let z=0;z<31;z++){
  const edge=Math.min(x,34-x,z,30-z)
  if(edge===0)for(let y=2;y<=13;y++)put(x,y,z,y===8?'calcite':(x+z+y)%10===0?'amethyst_block':'cobbled_deepslate')
  put(x,13+(edge>2?1:0),z,'deepslate_tiles')
 }
 box([3,6,1],[13,9,1],'purple_stained_glass')
 for(const x of [4,11,23,30])pillar(x,2,12)
 for(const [x,z] of [[9,12],[16,9],[24,9]]){
  put(x,2,z,'oak_fence');put(x,3,z,'target');put(x,4,z,'stripped_birch_log[axis=y]')
  for(const dx of [-1,1])put(x+dx,3,z,'oak_fence')
 }
 for(const x of [17,24]){box([x,2,15],[x,3,15],'oak_fence');box([x+4,2,15],[x+4,3,15],'oak_fence');box([x+1,3,15],[x+3,3,15],'oak_fence')}
 box([24,2,20],[28,2,23],'oak_planks');box([24,3,21],[28,3,22],'oak_slab[type=bottom,waterlogged=false]')
 for(const z of [19,24])box([24,2,z],[28,2,z],'oak_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]')
 for(const x of [6,17]){box([x,2,26],[x+7,2,26],'dark_oak_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]');box([x,3,27],[x+7,3,27],'dark_oak_trapdoor[facing=north,half=bottom,open=true,powered=false,waterlogged=false]')}
 for(const x of [6,12,18]){box([x,2,3],[x+3,2,3],'dark_oak_planks');box([x,5,3],[x+3,5,3],'dark_oak_planks');for(let dx=0;dx<4;dx++)put(x+dx,3,3,'iron_bars')}
 container(29,2,4);container(31,2,4)
 for(const [x,z] of [[2,27],[32,27],[19,3]])crystal(x,2,z,2)
 hanging(7,9,8,13);hanging(28,9,12,13)
 const ports=[port('west',16),port('east',16,5,5)]
 put(0,2,16,'oak_door[facing=east,half=lower,hinge=left,open=true,powered=false]');put(0,3,16,'oak_door[facing=east,half=upper,hinge=left,open=true,powered=false]')
 architecture(api,t.structure,size,{ports,roof:(x,z,edge)=>13+(edge>2?1:0),veins:[[2,6,4,6],[31,26,5,5],[19,1,5,3]]})
 box([24,2,19],[28,4,24],'air')
 for(let x=24;x<=28;x++){
  put(x,2,19,'oak_stairs[facing=south,half=bottom,shape=straight,waterlogged=false]')
  put(x,3,20,'oak_stairs[facing=south,half=bottom,shape=straight,waterlogged=false]')
  put(x,4,21,'oak_slab[type=bottom,waterlogged=false]')
  put(x,3,22,'oak_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]')
  put(x,2,23,'oak_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]')
 }
 for(const x of [24,28])for(const z of [20,22])box([x,2,z],[x,2,z],'oak_fence')
 for(let x=24;x<=28;x++)put(x,3,21,'oak_log[axis=x]')
 box([0,2,16],[0,3,16],'air')
 box([2,2,14],[2,4,14],'air')
 put(2,2,14,'oak_door[facing=east,half=lower,hinge=left,open=true,powered=false]')
 put(2,3,14,'oak_door[facing=east,half=upper,hinge=left,open=true,powered=false]')
 return {structure:t.structure,metadata:{name:'s8-15-training',size,ports,start:[0,2,16],targets:[{label:'open east arch',position:[34,2,16]},{label:'training dummy lane',position:[15,2,12]},{label:'spectator benches',position:[15,2,25]},{label:'obstacle bypass',position:[30,2,21]}],features:['sand training floor','three target dummies','two hurdles','low ramp obstacle','weapon racks','two spectator benches','open arch opposite wood door'],differences:['No combat entities or training logic; target blocks retain only vanilla functionality','Obstacle jumping not certified; traversable bypass is checked','West door held open in candidate for route audit']}}
}
