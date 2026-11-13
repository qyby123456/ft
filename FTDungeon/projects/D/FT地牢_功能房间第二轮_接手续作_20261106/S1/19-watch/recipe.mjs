import {tools} from '../common.mjs'
import {enhance} from '../enhance.mjs'
import fs from 'node:fs'
export default function build(api){
 const {structure,put,box,floor,wall,lamp,root,leaf,berry,container}=tools(api,[35,24,33])
 box([0,0,0],[34,0,32],'deepslate_bricks');floor([0,0],[34,32]);
 wall([0,2,0],[34,20,1]);wall([0,2,2],[1,20,32]);box([33,2,2],[34,20,32],'black_stained_glass');box([2,2,31],[32,20,32],'black_stained_glass');box([0,21,0],[34,21,32],'black_stained_glass');
 box([2,9,2],[31,9,12],'stone_bricks');box([2,9,13],[26,9,24],'mossy_stone_bricks');
 for(const x of [4,13,24])wall([x,2,4],[x+1,8,5]);
 box([2,10,24],[26,11,24],'dark_oak_fence');box([26,10,13],[26,11,23],'dark_oak_fence');
 for(const x of [3,10,18,25]){wall([x,10,24],[x,12,24]);put(x,13,24,'lantern');}
 for(let step=0;step<16;step++){const z=28-step;const y=2+Math.floor(step/2);box([28,1,z],[30,y-1,z],'stone_bricks');box([28,y,z],[30,y,z],step%2===0?'stone_brick_slab[type=bottom]':'stone_bricks');}
 box([27,10,12],[31,10,12],'air');
 for(const x of [6,11])for(const z of [17,19])put(x,10,z,'dark_oak_fence');box([6,11,17],[11,11,19],'dark_oak_slab[type=top]');put(7,12,18,'lightning_rod[facing=west]');put(10,12,18,'lectern[facing=south]');
 box([18,10,3],[21,10,3],'dark_oak_planks');box([18,11,3],[21,13,3],'iron_bars');box([18,14,3],[21,14,3],'dark_oak_planks');container(23,10,4);
 box([19,8,18],[23,8,21],'water[level=0]');box([19,9,18],[23,9,21],'water[level=0]');box([19,7,18],[23,7,21],'clay');
 box([3,10,3],[4,21,4],'dark_oak_log');leaf([2,20,2],[8,23,7]);root([[4,10,10],[5,13,8],[5,17,6],[4,20,4]]);berry(7,6,19,4);
 lamp(13,17,7,20);lamp(25,17,8,20);
 box([13,10,0],[15,13,1],'air');box([28,2,31],[30,5,32],'air');
 enhance(api,structure,JSON.parse(fs.readFileSync(new URL('./design.json',import.meta.url),'utf8')))
 box([2,11,0],[11,18,1],'glass');
 for(const x of [2,11]){box([x,10,1],[x,17,2],'dark_oak_log[axis=y]');put(x,18,1,'stone_brick_stairs[facing=south,half=top,shape=straight]')}
 box([2,19,1],[11,19,1],'dark_oak_log[axis=x]');
 for(const [x,y,z] of [[2,12,2],[2,13,2],[3,16,2],[10,17,2]])put(x,y,z,'oak_leaves[persistent=true,distance=1]');
 return {structure,metadata:{name:'s1-19-watch'}}
}
