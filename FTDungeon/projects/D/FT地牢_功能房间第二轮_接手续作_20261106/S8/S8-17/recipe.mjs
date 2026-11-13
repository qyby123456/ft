import {tools} from '../common.mjs'
import {architecture} from '../architecture.mjs'
export default function build(api){
 const size=[27,14,25],t=tools(api,size),{put,box,floor,pillar,crystal,hanging,container,port}=t
 box([0,0,0],[26,0,24],'smooth_basalt');floor([0,0],[26,24])
 for(let x=0;x<27;x++)for(let z=0;z<25;z++){
  const edge=Math.min(x,26-x,z,24-z)
  if(edge===0)for(let y=2;y<=11;y++)put(x,y,z,y===6?'calcite':(x+y+z)%5===0?'amethyst_block':'cobbled_deepslate')
  put(x,11+(edge>2?1:0),z,(x+z)%8===0?'amethyst_block':'deepslate_tiles')
 }
 box([6,2,3],[12,2,5],'dark_oak_planks');box([6,3,2],[12,5,2],'dark_oak_planks')
 for(const x of [6,8,10,12]){put(x,4,3,'lever[face=wall,facing=south,powered=false]');put(x,3,4,'stone_button[face=floor,facing=north,powered=false]')}
 put(5,2,5,'grindstone[face=floor,facing=south]');put(14,2,3,'furnace[facing=south,lit=false]',{id:'minecraft:furnace',Items:[]});box([14,3,2],[14,10,2],'smooth_basalt')
 for(const y of [2,4,6]){box([18,y,2],[23,y,3],'dark_oak_planks');for(const x of [18,20,22])put(x,y+1,3,(x+y)%4===0?'amethyst_block':'calcite')}
 for(const x of [18,23])box([x,2,2],[x,7,2],'dark_oak_log[axis=y]')
 box([9,2,12],[17,2,15],'dark_oak_planks');box([10,3,13],[14,3,14],'white_carpet');put(15,3,13,'amethyst_cluster[facing=up,waterlogged=false]');put(11,3,12,'stone_button[face=floor,facing=north,powered=false]')
 put(13,2,17,'dark_oak_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]')
 container(22,2,6);container(23,2,9);container(4,2,19)
 for(const x of [2,24])pillar(x,9,10)
 crystal(3,2,3,3);crystal(24,2,21,2);crystal(3,2,21,2)
 hanging(13,8,8,11)
 const ports=[port('west',13),port('east',13)]
 architecture(api,t.structure,size,{ports,roof:(x,z,edge)=>11+(edge>2?1:0),veins:[[2,3,4,5],[23,21,5,4],[12,1,4,3]]})
 for(let x=8;x<=18;x++)for(const z of [11,16])put(x,1,z,'purple_concrete')
 for(let z=11;z<=16;z++)for(const x of [8,18])put(x,1,z,'purple_concrete')
 return {structure:t.structure,metadata:{name:'s8-17-workshop',size,ports,start:[0,2,13],targets:[{label:'east exit',position:[26,2,13]},{label:'tool bench',position:[10,2,7]},{label:'furnace',position:[14,2,5]},{label:'project table',position:[18,2,16]}],features:['wall-mounted tool bench','grinding wheel','small furnace and chimney','three-tier raw materials shelf','central unfinished crystal project','supply crates'],differences:['Tool silhouettes use levers/buttons, not collectible tools','Furnace unlit and empty; ordinary vanilla crafting only','Plan uses white carpet instead of readable diagram']}}
}
