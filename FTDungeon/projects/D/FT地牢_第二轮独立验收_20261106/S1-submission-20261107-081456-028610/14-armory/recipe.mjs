import {tools} from '../common.mjs'
import {enhance} from '../enhance.mjs'
import fs from 'node:fs'
export default function build(api){
 const {structure,put,box,floor,wall,lamp,root,leaf,berry,container}=tools(api,[29,15,27])
 box([0,0,0],[28,0,26],'rooted_dirt');floor([0,0],[28,26]);wall([0,2,0],[28,11,1]);wall([0,2,25],[28,11,26]);wall([0,2,2],[1,11,24]);wall([27,2,2],[28,11,24]);box([0,12,0],[28,12,26],'dark_oak_planks');
 for(const x of [3,24]){box([x,2,3],[x,11,3],'dark_oak_log');leaf([x-1,10,4],[x+1,11,5]);berry(x+1,5,9,3);}
 for(const left of [4,17,23]){box([left,2,3],[left+2,2,3],'dark_oak_planks');box([left,6,3],[left+2,6,3],'dark_oak_planks');for(const x of [left,left+2])box([x,3,3],[x,5,3],'dark_oak_fence');box([left+1,3,3],[left+1,5,3],'iron_bars');}
 container(12,2,3);container(12,3,3);container(13,2,3);container(13,3,3);box([11,2,4],[14,5,4],'dark_oak_trapdoor[open=true,facing=south]');
 for(const x of [8,21]){put(x,2,5,'smooth_stone_slab[type=bottom]');box([x,3,5],[x,4,5],'oak_fence');put(x,5,5,'iron_block');put(x,6,5,'carved_pumpkin[facing=south]');put(x-1,5,5,'iron_bars');put(x+1,5,5,'iron_bars');}
 put(18,2,7,'anvil[facing=east]');box([9,2,12],[18,2,12],'dark_oak_fence');box([9,3,11],[18,3,14],'dark_oak_slab[type=top]');put(11,4,12,'lantern');put(15,4,12,'lectern[facing=south]');
 container(23,2,21);container(24,2,21);container(24,3,21);box([4,0,20],[10,0,23],'clay');box([4,1,20],[10,1,23],'water[level=0]');
 root([[25,2,12],[24,4,11],[24,7,9],[24,10,5]]);for(const [x,z] of [[5,9],[16,7],[24,16]])lamp(x,8,z,11);
 for(const [x,z] of [[3,17],[12,23],[25,18]]){put(x,1,z,'moss_block');leaf([x,2,z],[x+1,2,z]);}
 for(const x of [0,27])box([x,2,14],[x+1,5,16],'air');
 enhance(api,structure,JSON.parse(fs.readFileSync(new URL('./design.json',import.meta.url),'utf8')))
 return {structure,metadata:{name:'s1-14-armory'}}
}
