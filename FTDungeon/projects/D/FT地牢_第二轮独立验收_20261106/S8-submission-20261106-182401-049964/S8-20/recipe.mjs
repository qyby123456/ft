import {tools} from '../common.mjs'
export default function build(api){
 const size=[31,14,23],t=tools(api,size),{put,box,floor,crystal,hanging,container,port}=t
 box([0,0,0],[30,0,22],'smooth_basalt');floor([0,0],[30,22])
 for(let x=0;x<31;x++)for(let z=0;z<23;z++){
  const edge=Math.min(x,30-x,z,22-z)
  if(edge===0)for(let y=2;y<=11;y++)put(x,y,z,y===6?'calcite':(x+y+z)%7===0?'amethyst_block':'cobbled_deepslate')
  put(x,11+(edge>2?1:0),z,'deepslate_tiles')
 }
 box([5,2,1],[8,7,16],'deepslate_bricks');box([22,2,1],[24,7,16],'deepslate_bricks')
 box([9,2,1],[21,9,1],'amethyst_block');box([9,8,2],[21,8,3],'polished_deepslate');box([9,9,2],[21,9,4],'deepslate_tile_slab[type=bottom,waterlogged=false]')
 for(const x of [9,21])box([x,2,2],[x,7,3],'polished_deepslate')
 box([1,2,1],[4,9,7],'cobbled_deepslate');box([1,2,14],[4,9,21],'cobbled_deepslate')
 box([1,2,8],[7,5,10],'air');box([5,2,9],[8,5,11],'air')
 box([9,2,3],[21,2,7],'polished_deepslate');box([10,3,3],[20,3,6],'calcite')
 box([14,2,8],[16,2,9],'polished_deepslate_slab[type=bottom,waterlogged=false]')
  box([14,2,8],[16,2,8],'polished_deepslate')
 box([14,3,7],[16,3,7],'polished_deepslate_slab[type=bottom,waterlogged=false]')
 container(15,4,5,'chest','south');put(15,3,5,'polished_deepslate')
 for(const x of [11,19]){put(x,4,5,'gold_block');put(x,4,6,'light_weighted_pressure_plate[power=0]')}
 crystal(10,3,4,2);crystal(21,3,4,2);crystal(13,2,19,3)
 put(9,3,8,'lever[face=wall,facing=east,powered=false]');put(21,8,3,'cobweb')
 box([22,2,10],[24,5,12],'air')
 box([25,2,1],[29,9,7],'deepslate_bricks');box([25,2,16],[29,9,21],'deepslate_bricks')
 hanging(3,8,12,11);hanging(18,9,10,11);hanging(27,8,12,11)
 const ports=[port('west',11),port('east',11)]
 return {structure:t.structure,metadata:{name:'s8-20-secret-vault',size,ports,start:[0,2,11],targets:[{label:'east passage exit',position:[30,2,11]},{label:'vault approach',position:[15,2,11]},{label:'chest dais',position:[15,4,6]},{label:'concealed lever',position:[10,2,8]}],features:['bent narrow disguised-wall passage','small treasure alcove','raised chest dais','gold coin sculptures','concealed lever','cobweb corner','two through exits'],differences:['Chest empty and unlocked; no fabricated loot or security mechanism','Lever decorative, no automatic secret piston door','Gold blocks/pressure plates stand in for coins','Hidden passage kept visibly open for traversal']}}
}
