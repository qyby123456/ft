import {tools} from '../common.mjs'
export default function build(api){
 const size=[33,18,31],t=tools(api,size),{put,box,floor,pillar,crystal,hanging,container,port}=t
 box([0,0,0],[32,0,30],'smooth_basalt');floor([0,0],[32,30])
 for(let x=0;x<33;x++)for(let z=0;z<31;z++){
  const edge=Math.min(x,32-x,z,30-z)
  if(edge===0)for(let y=2;y<=15;y++)put(x,y,z,y===6||y===12?'calcite':(x+y+z)%9===0?'amethyst_block':'cobbled_deepslate')
  put(x,15+(edge>2?1:0),z,'smooth_basalt')
 }
 box([10,2,11],[22,4,23],'mossy_stone_bricks');box([11,2,12],[21,4,22],'air');box([11,1,12],[21,1,22],'deepslate_tiles');box([11,2,12],[21,3,22],'water[level=0]')
 for(const x of [10,22]){box([x,5,16],[x,10,16],'dark_oak_log[axis=y]');put(x,9,16,'calcite')}
 box([9,11,16],[23,11,17],'dark_oak_log[axis=x]');box([16,7,16],[16,10,16],'chain[axis=y]');put(16,6,16,'cauldron')
 box([14,1,24],[16,1,28],'water[level=0]');box([14,0,24],[16,0,28],'deepslate_tiles')
 for(const x of [13,17])box([x,1,24],[x,1,29],'mossy_stone_bricks')
 box([14,1,29],[16,1,29],'mossy_stone_bricks');box([13,2,25],[17,2,26],'polished_deepslate_slab[type=bottom,waterlogged=false]')
 box([24,2,3],[30,4,9],'deepslate_bricks');box([24,5,3],[30,5,3],'dark_oak_fence');box([30,5,3],[30,5,9],'dark_oak_fence')
 for(let step=0;step<6;step++)box([24,2,10+step],[27,4-Math.floor(step/2),10+step],'polished_deepslate')
 for(const x of [25,28])container(x,5,5)
 for(const [x,z] of [[7,16],[7,19],[24,22]])container(x,2,z)
 crystal(16,2,4,5);crystal(3,2,26,3);crystal(29,2,26,3)
 for(const x of [5,27])hanging(x,10,12,15)
 const ports=[port('west',9),port('east',18)]
 return {structure:t.structure,metadata:{name:'s8-16-cistern',size,ports,start:[0,2,9],targets:[{label:'east exit',position:[32,2,18]},{label:'well front rim',position:[9,2,17]},{label:'dry rear bypass',position:[16,2,9]},{label:'channel bridge approach',position:[12,2,25]}],features:['raised moss-lined cistern','timber pulley frame and suspended cauldron','contained shallow water channel','stone bridge','maintenance ledge and stairs','giant rear crystal'],differences:['Bucket uses cauldron geometry suspended as a fixture, no functional pulley','Water is contained by full-block sides and bottom; no fluid tick simulation','Maintenance ledge stair climb not included in principal dry-route audit']}}
}
