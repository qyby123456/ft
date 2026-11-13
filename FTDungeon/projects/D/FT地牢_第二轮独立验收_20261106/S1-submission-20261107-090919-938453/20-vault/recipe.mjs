import {tools} from '../common.mjs'
import {enhance} from '../enhance.mjs'
import fs from 'node:fs'
export default function build(api){
 const {structure,put,box,floor,wall,lamp,root,leaf,berry,container}=tools(api,[31,16,27])
 box([0,0,0],[30,0,26],'rooted_dirt');floor([0,0],[30,26]);wall([0,2,0],[30,12,2]);wall([0,2,25],[30,12,26]);wall([0,2,3],[1,12,24]);wall([29,2,3],[30,12,24]);box([0,13,0],[30,13,26],'dark_oak_planks');
 wall([11,2,3],[13,10,18]);wall([3,2,6],[10,8,7]);wall([3,2,8],[4,8,13]);box([11,2,16],[13,5,18],'air');
 box([11,2,15],[13,6,15],'mossy_stone_bricks');box([11,2,19],[13,6,19],'mossy_stone_bricks');
 box([15,2,3],[27,2,5],'dark_oak_planks');
 api.setBlock(structure,[21,3,4],'minecraft:chest[facing=south,type=single,waterlogged=false]',undefined,api.createBlockNbt({id:'minecraft:chest',Items:[]}));
 for(const [x,z] of [[17,4],[18,5],[25,4],[26,5]]){put(x,3,z,'gold_block');put(x,4,z,'light_weighted_pressure_plate[power=0]');}
 put(25,4,3,'lever[face=wall,facing=south,powered=false]');put(26,8,3,'cobweb');container(27,2,6);
 box([6,2,9],[7,13,10],'dark_oak_log');root([[6,2,14],[7,5,12],[7,8,11],[7,12,10]]);leaf([5,12,8],[9,15,12]);berry(9,11,11,3);
 box([4,1,21],[10,1,23],'water[level=0]');box([22,1,21],[27,1,23],'water[level=0]');
 for(const [x,z] of [[5,17],[16,12],[26,12]])lamp(x,8,z,12);
 for(const x of [0,29])box([x,2,16],[x+1,5,18],'air');
 enhance(api,structure,JSON.parse(fs.readFileSync(new URL('./design.json',import.meta.url),'utf8')))
 return {structure,metadata:{name:'s1-20-vault'}}
}
