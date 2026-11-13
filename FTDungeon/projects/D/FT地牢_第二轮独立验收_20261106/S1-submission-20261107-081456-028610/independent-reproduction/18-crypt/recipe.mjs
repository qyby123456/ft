import {tools} from '../common.mjs'
import {enhance} from '../enhance.mjs'
import fs from 'node:fs'
export default function build(api){
 const {structure,put,box,floor,wall,lamp,root,leaf,berry}=tools(api,[31,18,31])
 box([0,0,0],[30,0,30],'stone');box([0,1,0],[30,2,30],'rooted_dirt');floor([0,0],[30,30],3);
 wall([0,4,0],[30,15,2]);wall([0,4,28],[30,15,30]);wall([0,4,3],[2,15,27]);wall([28,4,3],[30,15,27]);box([0,16,0],[30,16,30],'mossy_stone_bricks');
 for(const [left,right] of [[5,10],[13,23]]){box([left,6,2],[right,12,3],'air');for(const y of [6,10]){box([left,y,2],[right,y,3],'stone_brick_slab[type=top]');for(let x=left+1;x<right;x+=2){put(x,y+1,3,'skeleton_skull[rotation=0]');put(x,y+1,2,'bone_block[axis=x]');}}}
 for(const [x,z] of [[7,17],[21,20]]){wall([x,4,z],[x+3,5,z+5]);box([x,6,z],[x+3,6,z+5],'stone_brick_slab[type=bottom]');for(const dz of [1,3])put(x+1,6,z+dz,'chiseled_stone_bricks');}
 wall([12,4,9],[18,5,12]);box([11,6,8],[19,6,13],'stone_brick_slab[type=top]');for(const [x,z] of [[12,9],[18,9],[12,12],[18,12]])put(x,7,z,'candle[candles=3,lit=true]');
 for(const [x,z] of [[6,12],[23,15],[19,25]]){wall([x,4,z],[x,7,z]);put(x,8,z,'candle[candles=3,lit=true]');}
 box([25,4,4],[26,15,5],'dark_oak_log');root([[26,4,13],[25,7,11],[25,10,8],[25,14,5]]);leaf([23,14,4],[27,15,7]);berry(24,7,13,3);
 box([3,1,23],[10,1,26],'clay');box([3,2,23],[10,3,26],'water[level=0]');
 for(const [x,z] of [[5,7],[16,6],[25,12]])lamp(x,11,z,15);
 for(const x of [0,28])box([x,4,14],[x+2,7,16],'air');
 enhance(api,structure,JSON.parse(fs.readFileSync(new URL('./design.json',import.meta.url),'utf8')))
 return {structure,metadata:{name:'s1-18-crypt'}}
}
