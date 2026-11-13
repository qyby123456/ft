import {tools} from '../common.mjs'
import {enhance} from '../enhance.mjs'
import fs from 'node:fs'
export default function build(api){
 const {structure,put,box,floor,wall,lamp,root,leaf,berry,container}=tools(api,[27,17,25])
 box([0,0,0],[26,0,24],'rooted_dirt');floor([0,0],[26,24]);for(let x=4;x<23;x++)for(let z=5;z<21;z++)if((x*7+z*3)%11<3)put(x,1,z,'coarse_dirt');
 wall([0,2,0],[26,11,1]);wall([0,2,23],[26,11,24]);wall([0,2,2],[1,11,22]);wall([25,2,2],[26,11,22]);box([0,12,0],[26,12,24],'dark_oak_planks');
 for(const x of [3,23]){box([x,2,3],[x,13,3],'dark_oak_log');root([[x,2,6],[x,5,5],[x+1,8,4],[x,11,3]]);leaf([x-1,13,2],[x+1,15,5]);berry(x+1,5,11,3);}
 box([3,11,3],[23,11,3],'dark_oak_log[axis=x]');
 box([11,2,2],[17,2,3],'dark_oak_planks');for(const y of [4,7]){box([11,y,2],[17,y,3],'dark_oak_planks');put(12,y+1,3,'raw_iron_block');put(15,y+1,3,'cobblestone_slab[type=bottom]');}
 put(7,2,4,'grindstone[face=floor,facing=south]');box([5,2,2],[9,2,2],'crafting_table');box([5,5,2],[9,5,2],'iron_bars');container(4,2,6);
 api.setBlock(structure,[20,2,3],'minecraft:furnace[facing=south,lit=false]',undefined,api.createBlockNbt({id:'minecraft:furnace',Items:[]}));wall([19,3,2],[21,8,3]);put(20,4,4,'shroomlight');
 for(const x of [8,17])for(const z of [11,14])put(x,2,z,'dark_oak_log');box([8,3,11],[17,3,14],'oak_slab[type=top]');put(12,4,12,'smooth_stone_slab[type=bottom]');put(13,4,12,'anvil[facing=east]');put(16,4,12,'flower_pot');
 container(21,2,19);container(22,2,19);box([4,1,19],[9,1,21],'water[level=0]');
 for(const [x,z] of [[6,10],[14,7],[21,11]])lamp(x,8,z,11);
 for(const [x,z] of [[3,17],[11,21],[23,21]]){put(x,1,z,'moss_block');leaf([x,2,z],[x+1,2,z]);}
 for(const x of [0,25])box([x,2,10],[x+1,5,12],'air');
 enhance(api,structure,JSON.parse(fs.readFileSync(new URL('./design.json',import.meta.url),'utf8')))
 return {structure,metadata:{name:'s1-17-workshop'}}
}
