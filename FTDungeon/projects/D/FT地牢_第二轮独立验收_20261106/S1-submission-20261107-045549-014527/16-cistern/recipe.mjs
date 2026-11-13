import {tools} from '../common.mjs'
import {enhance} from '../enhance.mjs'
import fs from 'node:fs'
export default function build(api){
 const {structure,put,box,floor,wall,lamp,root,leaf,berry,container}=tools(api,[29,18,29])
 box([0,0,0],[28,0,28],'clay');box([0,1,0],[28,2,28],'stone_bricks');floor([0,0],[28,28],3);
 wall([0,4,0],[28,13,1]);wall([0,4,27],[28,13,28]);wall([0,4,2],[1,13,26]);wall([27,4,2],[28,13,26]);box([0,14,0],[28,14,28],'dark_oak_planks');
 wall([8,4,9],[20,5,9]);wall([8,4,19],[20,5,19]);wall([8,4,10],[8,5,18]);wall([20,4,10],[20,5,18]);
 box([9,1,10],[19,3,18],'water[level=0]');
 for(const x of [8,20])box([x,6,14],[x,10,14],'dark_oak_log');box([8,11,14],[20,11,14],'dark_oak_log[axis=x]');
 box([14,7,14],[14,10,14],'chain[axis=y]');put(14,6,14,'cauldron');
 box([4,3,23],[23,3,24],'air');box([4,2,23],[23,2,24],'water[level=0]');for(const x of [7,21])box([x,3,22],[x+1,3,25],'dark_oak_planks');
 box([22,4,3],[25,4,5],'dark_oak_planks');container(23,5,4);put(25,5,4,'lantern');container(6,4,18);container(23,4,18);
 box([3,4,3],[4,16,4],'dark_oak_log');leaf([2,13,2],[7,17,7]);root([[4,4,8],[5,7,7],[5,10,5],[4,13,4]]);berry(7,6,13,4);
 for(const [x,z] of [[6,12],[23,12],[14,22]])lamp(x,10,z,13);
 for(const x of [0,27])box([x,4,11],[x+1,7,13],'air');
 enhance(api,structure,JSON.parse(fs.readFileSync(new URL('./design.json',import.meta.url),'utf8')))
 return {structure,metadata:{name:'s1-16-cistern'}}
}
