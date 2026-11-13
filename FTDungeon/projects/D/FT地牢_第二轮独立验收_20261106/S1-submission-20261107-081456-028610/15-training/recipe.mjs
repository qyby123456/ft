import {tools} from '../common.mjs'
import {enhance} from '../enhance.mjs'
import fs from 'node:fs'
export default function build(api){
 const {structure,put,box,floor,wall,lamp,root,leaf,berry,container}=tools(api,[35,18,31])
 box([0,0,0],[34,0,30],'rooted_dirt');floor([0,0],[34,30]);box([6,1,7],[28,1,23],'sand');
 wall([0,2,0],[34,11,1]);wall([0,2,29],[34,11,30]);wall([0,2,2],[1,11,28]);wall([33,2,2],[34,11,28]);box([0,13,0],[34,13,30],'dark_oak_planks');box([3,13,3],[31,13,27],'green_stained_glass');
 for(const x of [7,16,25]){put(x,2,4,'oak_fence');put(x,3,4,'target');put(x,4,4,'target');}
 for(const [x,z] of [[10,11],[19,12],[13,17]]){put(x,2,z,'dark_oak_log');put(x,3,z,'hay_block');put(x,4,z,'hay_block');put(x-1,3,z,'oak_fence');put(x+1,3,z,'oak_fence');}
 for(const z of [8,15]){box([2,2,z],[2,6,z+3],'dark_oak_fence');box([3,2,z],[3,5,z+3],'iron_bars');box([2,6,z],[3,6,z+3],'dark_oak_planks');container(4,2,z+3);}
 for(const z of [13,17,21]){put(26,2,z,'oak_fence');put(29,2,z,'oak_fence');box([26,3,z],[29,3,z],'oak_fence');}
 for(let step=0;step<4;step++){box([22,2,22+step],[24,2+Math.floor(step/2),22+step],'dark_oak_planks');box([22,3+Math.floor(step/2),22+step],[24,3+Math.floor(step/2),22+step],'dark_oak_slab[type=bottom]');}
 for(const z of [24,27])box([5,2,z],[18,2,z],'dark_oak_slab[type=bottom]');
 box([27,0,26],[31,0,28],'clay');box([27,1,26],[31,1,28],'water[level=0]');
 box([3,2,3],[4,16,4],'dark_oak_log');leaf([2,14,2],[8,17,7]);root([[4,3,7],[5,6,6],[5,10,5],[4,13,4]]);berry(7,5,13,4);
 for(const [x,z] of [[8,8],[19,7],[30,12]])lamp(x,9,z,12);
 box([0,2,20],[1,5,22],'air');box([33,2,8],[34,6,12],'air');
 enhance(api,structure,JSON.parse(fs.readFileSync(new URL('./design.json',import.meta.url),'utf8')))
 return {structure,metadata:{name:'s1-15-training'}}
}
