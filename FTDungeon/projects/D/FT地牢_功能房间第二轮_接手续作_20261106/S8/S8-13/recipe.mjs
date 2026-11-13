import {tools} from '../common.mjs'
import {architecture} from '../architecture.mjs'
export default function build(api){
 const size=[35,16,29],t=tools(api,size),{put,box,floor,pillar,crystal,hanging,container,port}=t
 box([0,0,0],[34,0,28],'smooth_basalt');floor([0,0],[34,28])
 for(let x=0;x<35;x++)for(let z=0;z<29;z++){
  const edge=Math.min(x,34-x,z,28-z),top=12+Math.min(2,edge)
  if(edge===0)for(let y=2;y<=top;y++)put(x,y,z,y===7?'calcite':(x+y+z)%8===0?'amethyst_block':'cobbled_deepslate')
  put(x,top,z,(x+z)%13===0?'calcite':'deepslate_bricks')
 }
 box([4,2,2],[12,3,8],'deepslate_bricks');box([4,4,2],[5,8,7],'polished_deepslate');box([11,4,2],[12,8,7],'polished_deepslate')
 box([4,8,2],[12,9,7],'deepslate_bricks');box([6,10,2],[10,14,4],'smooth_basalt')
 box([6,4,2],[10,6,2],'shroomlight');box([6,4,3],[10,6,3],'orange_stained_glass');put(8,4,5,'cauldron')
 for(const x of [6,10])put(x,4,7,'smoker[facing=south,lit=false]',{id:'minecraft:smoker',Items:[]})
 for(const y of [2,5,8]){box([18,y,2],[29,y,4],'dark_oak_planks');for(const x of [19,23,27])container(x,y+1,3)}
 for(const x of [18,29])box([x,2,2],[x,9,2],'dark_oak_log[axis=y]')
 box([9,2,12],[16,2,14],'stripped_dark_oak_log[axis=x]');put(11,3,13,'red_carpet');put(14,3,13,'grindstone[face=floor,facing=south]')
 for(const [x,z] of [[8,21],[22,19]]){
  box([x,2,z],[x+7,2,z+1],'dark_oak_planks')
  for(const offset of [-2,3])box([x,2,z+offset],[x+7,2,z+offset],'dark_oak_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]')
  put(x+3,3,z,'lantern[hanging=false]');for(const dx of [0,6])put(x+dx,3,z,'white_carpet')
 }
 for(const [x,z] of [[2,9],[31,6],[3,23]])container(x,2,z)
 crystal(2,2,3,3);crystal(32,2,25,3);crystal(16,2,3,2)
 for(const x of [2,32])pillar(x,12,11)
 hanging(17,9,15,13)
 const ports=[port('west',15),port('east',12)]
 architecture(api,t.structure,size,{ports,roof:(x,z,edge)=>12+Math.min(2,edge),veins:[[2,4,4,5],[30,24,5,4],[20,1,5,3]]})
 box([6,7,5],[10,7,6],'deepslate_brick_slab[type=bottom,waterlogged=false]')
 box([8,5,5],[8,6,5],'chain[axis=y,waterlogged=false]')
 for(const x of [5,11])put(x,7,7,`deepslate_brick_stairs[facing=${x===5?'east':'west'},half=top,shape=straight,waterlogged=false]`)
 box([4,9,7],[12,9,8],'deepslate_brick_slab[type=bottom,waterlogged=false]')
 return {structure:t.structure,metadata:{name:'s8-13-kitchen',size,ports,start:[0,2,15],targets:[{label:'east exit',position:[34,2,12]},{label:'hearth access',position:[8,2,10]},{label:'dining aisle',position:[19,2,24]},{label:'pantry',position:[25,2,6]}],features:['large masonry hearth and enclosed chimney','cauldron and two smokers','three-level jar pantry','butcher block','two long dining tables with benches','barrel supplies'],differences:['Safe luminous glass hearth replaces open fire; no campfire damage or smoke simulation','Jars and sacks use empty vanilla barrels; plates use white carpet','No meat items or fabricated food inventory']}}
}
