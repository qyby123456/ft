import {tools} from '../common.mjs'
export default function build(api){
 const size=[31,16,29],t=tools(api,size),{put,box,floor,pillar,crystal,hanging,port}=t
 box([0,0,0],[30,0,28],'smooth_basalt');floor([0,0],[30,28])
 for(let x=0;x<31;x++)for(let z=0;z<29;z++){
  const edge=Math.min(x,30-x,z,28-z)
  if(edge===0)for(let y=2;y<=13;y++)put(x,y,z,y===10?'calcite':(x+y+z)%8===0?'amethyst_block':'deepslate_bricks')
  put(x,13+(edge>2?1:0),z,'deepslate_tiles')
 }
 for(const [start,end] of [[3,11],[19,27]]){
  box([start,2,1],[end,10,3],'deepslate_bricks')
  for(const y of [3,6,9]){box([start+1,y,2],[end-1,y+1,3],'air');for(const x of [start+2,end-2]){put(x,y,3,'skeleton_skull[rotation=8]',{id:'minecraft:skull'});put(x+1,y,3,'bone_block[axis=x]')}}
 }
 for(const x of [2,12,18,28])pillar(x,4,12)
 for(const x of [6,23]){box([x,2,13],[x+3,3,18],'polished_deepslate');box([x,4,13],[x+3,4,18],'deepslate_tile_slab[type=bottom,waterlogged=false]');put(x+1,4,15,'chiseled_deepslate')}
 box([12,2,9],[18,2,13],'deepslate_bricks');box([13,3,10],[17,4,12],'polished_deepslate');crystal(15,5,11,4);crystal(17,5,11,2)
 for(const x of [12,18]){put(x,3,11,'candle[candles=3,lit=true,waterlogged=false]')}
 for(const x of [3,27])for(const z of [9,23]){put(x,2,z,'polished_deepslate');put(x,3,z,'candle[candles=2,lit=true,waterlogged=false]')}
 for(const x of [7,23])hanging(x,10,8,13)
 box([14,1,16],[16,1,25],'purple_stained_glass');box([14,0,16],[16,0,25],'amethyst_block')
 crystal(3,2,25,3);crystal(27,2,25,3)
 const ports=[port('west',17),port('east',17)]
 return {structure:t.structure,metadata:{name:'s8-18-crypt',size,ports,start:[0,2,17],targets:[{label:'east exit',position:[30,2,17]},{label:'altar approach',position:[15,2,14]},{label:'ossuary rear aisle',position:[15,2,6]},{label:'front crystal aisle',position:[15,2,24]}],features:['two tiered skull niches','two stone sarcophagi','central crystal altar','candle plinths','hanging chains','glass amethyst inlay'],differences:['Bones represented by supported bone blocks rather than loose item entities','Sarcophagi are solid decorative masonry, not loot containers','No undead spawns or ritual function added']}}
}
