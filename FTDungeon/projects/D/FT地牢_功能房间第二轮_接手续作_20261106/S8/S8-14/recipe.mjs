import {tools} from '../common.mjs'
import {architecture} from '../architecture.mjs'
export default function build(api){
 const size=[29,14,27],t=tools(api,size),{put,box,floor,pillar,crystal,hanging,container,port}=t
 box([0,0,0],[28,0,26],'smooth_basalt');floor([0,0],[28,26])
 for(let y=2;y<=11;y++)for(let x=0;x<29;x++)for(let z=0;z<27;z++)if(x===0||x===28||z===0||z===26)put(x,y,z,y===8?'calcite':(x+z+y)%6===0?'amethyst_block':'deepslate_tiles')
 for(let x=0;x<29;x++)for(let z=0;z<27;z++)put(x,11+((x>3&&x<25&&z>3&&z<23)?1:0),z,(x+z)%9===0?'amethyst_block':'smooth_basalt')
 box([3,6,1],[25,7,1],'tinted_glass');box([1,6,3],[1,7,9],'purple_stained_glass')
 for(const [x,material] of [[4,'iron_block'],[8,'gold_block']]){
  box([x-1,2,5],[x+1,2,7],'polished_deepslate');put(x,3,6,'dark_oak_fence');put(x,4,6,material);put(x,5,6,material)
  put(x-1,4,6,'chain[axis=x]');put(x+1,4,6,'chain[axis=x]')
 }
 for(const [start,end] of [[3,10],[19,25]]){
  box([start,2,2],[end,2,3],'dark_oak_planks');box([start,6,2],[end,6,3],'dark_oak_planks')
  for(let x=start;x<=end;x+=2)box([x,3,2],[x,5,2],'iron_bars')
 }
 box([12,2,2],[16,6,4],'dark_oak_planks');container(14,3,5);box([14,2,5],[14,2,5],'dark_oak_planks')
 for(const y of [2,4,6]){box([24,y,6],[26,y,10],'dark_oak_planks');for(const z of [6,8,10])put(25,y+1,z,'end_rod[facing=up]')}
 put(23,2,12,'anvil[facing=north]')
 box([9,2,13],[18,2,16],'dark_oak_planks');box([10,3,14],[17,3,15],'purple_carpet');put(10,3,13,'lantern[hanging=false]')
 for(const [x,z] of [[22,21],[24,21],[26,21]])container(x,2,z)
 for(const x of [2,26])pillar(x,11,10)
 for(const x of [7,21])hanging(x,8,10,11)
 crystal(3,2,23,3);crystal(26,2,23,2);crystal(14,7,3,2)
 const ports=[port('west',15),port('east',15)]
 architecture(api,t.structure,size,{ports,roof:(x,z)=>11+((x>3&&x<25&&z>3&&z<23)?1:0),veins:[[2,5,5,5],[26,22,5,4],[14,1,6,3]]})
 for(let x=8;x<=19;x++)for(const z of [12,17])put(x,1,z,x%4===0?'calcite':'amethyst_block')
 for(let z=12;z<=17;z++)for(const x of [8,19])put(x,1,z,z%3===0?'calcite':'amethyst_block')
 return {structure:t.structure,metadata:{name:'s8-14-armory',size,ports,start:[0,2,15],targets:[{label:'east exit',position:[28,2,15]},{label:'armor displays',position:[7,2,9]},{label:'anvil',position:[22,2,12]},{label:'inspection table',position:[13,2,18]}],features:['silver and gold armor display sculptures','two blade racks','central wood cabinet','three-tier arrow rack','anvil','purple inspection table','supply barrels'],differences:['Armor displays are block sculptures, not equipable armor stand entities','Blades and arrows represented by iron bars/end rods without weapon items','Cabinet visually closed but not security-locked; all storage empty']}}
}
