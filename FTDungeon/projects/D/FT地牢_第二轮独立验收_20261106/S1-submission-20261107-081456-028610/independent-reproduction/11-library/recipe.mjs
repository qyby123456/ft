import {tools} from '../common.mjs'
import {enhance} from '../enhance.mjs'
import fs from 'node:fs'
export default function build(api){
  const {structure,put,box,floor,wall,lamp,root,leaf,berry,container}=tools(api,[31,16,27])
  box([0,0,0],[30,0,26],'deepslate_bricks');floor([0,0],[30,26]);
  wall([0,2,0],[30,12,1]);wall([0,2,25],[30,12,26]);wall([0,2,2],[1,12,24]);wall([29,2,2],[30,12,24]);
  box([0,13,0],[30,13,26],'dark_oak_planks');wall([0,14,0],[30,14,1]);wall([0,14,25],[30,14,26]);
  for(const x of [3,14,27]){box([x,2,2],[x,12,3],'dark_oak_log[axis=y]');leaf([x-1,14,1],[x+1,15,4]);}
  box([2,11,2],[28,11,3],'dark_oak_log[axis=x]');
  for(const [left,right] of [[5,12],[16,21],[25,27]]){box([left,2,2],[right,9,2],'bookshelf');for(const y of [5,10])box([left,y,3],[right,y,3],'dark_oak_slab[type=top]');}
  box([2,2,6],[2,7,12],'bookshelf');box([3,8,6],[3,8,12],'dark_oak_slab[type=top]');
  box([23,2,2],[23,10,2],'dark_oak_planks');for(let y=2;y<=10;y++)put(23,y,3,'ladder[facing=south]');
  container(26,2,4);container(26,3,4);box([25,2,5],[27,5,5],'dark_oak_trapdoor[facing=south,half=bottom,open=true]');
  for(const [left,front] of [[8,11],[18,15]]){
    for(const x of [left,left+4])for(const z of [front,front+2])put(x,2,z,'dark_oak_fence');box([left,3,front],[left+4,3,front+2],'dark_oak_slab[type=top]');
    put(left+1,4,front+1,'lectern[facing=south]');put(left+3,4,front+1,'candle[candles=2,lit=true]');box([left,2,front+3],[left+4,2,front+3],'oak_slab[type=bottom]');
  }
  box([4,1,19],[12,1,22],'clay');box([5,1,20],[11,1,21],'water[level=0]');
  for(const [x,z] of [[4,19],[12,20],[6,22],[11,22],[3,15],[27,20]])put(x,2,z,'moss_carpet');
  root([[2,2,15],[3,5,12],[4,8,10],[7,10,6],[7,12,2]]);root([[27,2,22],[28,5,21],[28,9,22],[28,12,24]]);
  for(const [x,z] of [[7,4],[19,4],[27,8]]){leaf([x,12,z],[x+1,12,z+1]);berry(x,z,11,3);}
  for(const [x,z] of [[5,7],[15,7],[25,12],[16,22]])lamp(x,8,z,12);
  for(const [x,z] of [[3,18],[13,22],[26,21],[27,10]]){box([x,1,z],[x+1,1,z+1],'moss_block');leaf([x,2,z],[x+1,2,z]);}
  root([[4,2,14],[5,4,12],[6,6,10],[7,8,8],[8,10,6]]);
  for(const x of [0,29])box([x,2,15],[x+1,5,17],'air');
 enhance(api,structure,JSON.parse(fs.readFileSync(new URL('./design.json',import.meta.url),'utf8')))
 put(2,2,15,'air');
  return {structure,metadata:{name:'s1-11-library'}}
}
