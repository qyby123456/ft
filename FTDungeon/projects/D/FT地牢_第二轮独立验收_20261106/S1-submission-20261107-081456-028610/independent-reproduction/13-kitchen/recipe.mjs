import {tools} from '../common.mjs'
import {enhance} from '../enhance.mjs'
import fs from 'node:fs'
export default function build(api){
 const {structure,put,box,floor,wall,lamp,root,leaf,berry,container}=tools(api,[33,17,29])
 box([0,0,0],[32,0,28],'rooted_dirt');floor([0,0],[32,28]);
 wall([0,2,0],[32,11,1]);wall([0,2,27],[32,11,28]);wall([0,2,2],[1,11,26]);wall([31,2,2],[32,11,26]);box([0,12,0],[32,12,28],'dark_oak_planks');
 wall([12,2,2],[20,3,6]);wall([12,4,2],[13,10,6]);wall([19,4,2],[20,10,6]);wall([12,8,2],[20,10,6]);wall([14,11,2],[18,16,5]);
 box([14,4,3],[18,4,5],'shroomlight');put(16,5,4,'cauldron');put(16,6,4,'chain');put(16,7,4,'chain');
 for(const left of [4,23]){box([left,2,2],[left+5,3,3],'dark_oak_planks');for(const y of [5,8]){box([left,y,2],[left+5,y,3],'dark_oak_planks');for(const x of [left+1,left+3])put(x,y+1,3,'flower_pot');}container(left,2,5);container(left+2,2,5);}
 for(const [x,z] of [[7,12],[20,17]]){for(const dx of [0,7])for(const dz of [0,2])put(x+dx,2,z+dz,'dark_oak_fence');box([x,3,z],[x+7,3,z+2],'oak_slab[type=top]');box([x,2,z-1],[x+7,2,z-1],'dark_oak_slab[type=bottom]');box([x,2,z+3],[x+7,2,z+3],'dark_oak_slab[type=bottom]');put(x+2,4,z+1,'flower_pot');put(x+5,4,z+1,'cake');}
 box([5,0,22],[10,0,25],'clay');box([5,1,22],[10,1,25],'water[level=0]');
 for(const x of [3,29]){box([x,2,3],[x,11,3],'dark_oak_log');leaf([x-1,10,4],[x+1,11,6]);root([[x,2,7],[x,5,6],[x+1,8,4]]);berry(x+1,6,9,3);}
 for(const [x,z] of [[9,8],[24,9],[16,23]])lamp(x,8,z,11);
 for(const [x,z] of [[3,20],[12,25],[29,23]]){put(x,1,z,'moss_block');leaf([x,2,z],[x+1,2,z]);}
 for(const x of [0,31])box([x,2,13],[x+1,5,15],'air');
 enhance(api,structure,JSON.parse(fs.readFileSync(new URL('./design.json',import.meta.url),'utf8')))
 return {structure,metadata:{name:'s1-13-kitchen'}}
}
