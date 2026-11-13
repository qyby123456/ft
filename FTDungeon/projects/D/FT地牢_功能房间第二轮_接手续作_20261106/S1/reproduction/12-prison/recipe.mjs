import {tools} from '../common.mjs'
import {enhance} from '../enhance.mjs'
import fs from 'node:fs'
export default function build(api){
 const {structure,put,box,floor,wall,lamp,root,leaf,berry,container}=tools(api,[29,16,35])
 box([0,0,0],[28,0,34],'rooted_dirt');floor([0,0],[28,34]);
 wall([0,2,0],[28,11,1]);wall([0,2,33],[28,11,34]);wall([0,2,2],[1,11,32]);wall([27,2,2],[28,11,32]);box([0,12,0],[28,12,34],'mossy_stone_bricks');
 for(const x of [9,19]){
  wall([x,2,6],[x,2,27]);box([x,3,6],[x,7,27],'iron_bars');wall([x,8,6],[x,9,27]);
  for(const z of [6,13,20,27]){wall([x,2,z],[x,9,z]);wall([x===9?2:20,2,z],[x===9?8:26,8,z]);}
  for(const z of [9,16,23]){
   box([x,2,z],[x,4,z+1],'air');put(x,2,z,'iron_door[facing=east,half=lower,hinge=left,open=true]');put(x,3,z,'iron_door[facing=east,half=upper,hinge=left,open=true]');
   box([x===9?3:23,2,z],[x===9?5:25,2,z+1],'dark_oak_slab[type=bottom]');container(x===9?7:21,2,z+1);
  }
 }
 box([11,2,3],[17,2,4],'dark_oak_planks');put(12,3,3,'lantern');put(16,3,3,'flower_pot');container(18,2,3);
 for(const x of [5,23]){box([x,10,2],[x,14,3],'dark_oak_log');leaf([x-2,14,1],[x+2,15,5]);root([[x,10,3],[x+1,8,5],[x+2,5,6]]);}
 for(const x of [8,20])for(const z of [8,18,28]){lamp(x,8,z,11);berry(x===8?3:25,z,11,2);}
 box([22,1,29],[25,1,31],'water[level=0]');box([22,0,29],[25,0,31],'clay');
 for(const [x,z] of [[3,5],[4,28],[24,5],[25,26]]){box([x,1,z],[x,1,z+2],'moss_block');leaf([x,2,z],[x,3,z+1]);}
 for(const z of [0,33])box([13,2,z],[15,5,z+1],'air');
 enhance(api,structure,JSON.parse(fs.readFileSync(new URL('./design.json',import.meta.url),'utf8')))
 return {structure,metadata:{name:'s1-12-prison'}}
}
