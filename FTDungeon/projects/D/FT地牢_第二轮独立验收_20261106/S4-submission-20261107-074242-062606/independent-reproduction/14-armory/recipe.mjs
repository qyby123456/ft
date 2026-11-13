import {tools} from '../common.mjs';
import {enhance} from '../enrich.mjs';
function build(api){
 const {structure,put,fill,masonry,floor,port,beam,lamp,vine,container,pool}=tools(api,[27,13,25]);
 floor([0,0,0],[26,2,24]);
 masonry([0,3,0],[26,9,1]);masonry([0,3,23],[26,9,24]);masonry([0,3,2],[1,9,22]);masonry([25,3,2],[26,9,22]);fill([0,10,0],[26,10,24],'dark_oak_planks');
 for(const east of [1,8,18,25])beam([east,9,0],[east,9,24]);
 pool([6,2,7],[20,2,20]);floor([9,1,10],[17,2,15]);fill([0,2,11],[26,2,13],'dark_oak_planks');
 fill([10,3,10],[16,3,11],'dark_oak_planks');fill([11,4,10],[15,4,10],'white_carpet');put([10,4,11],'lantern[hanging=false]');
 fill([3,3,2],[13,7,2],'dark_oak_planks');for(const east of [4,6,8,10,12]){put([east,4,3],'iron_bars');put([east,5,3],'iron_bars');put([east,6,3],'chain[axis=y]');}
 fill([17,3,2],[23,7,3],'dark_oak_planks');for(const east of [17,18,22,23])container([east,3,4]);fill([19,3,4],[21,6,4],'dark_oak_planks');put([20,4,5],'tripwire_hook[facing=south,attached=false,powered=false]');
 put([22,3,7],'anvil[facing=east]');
 for(const east of [3,5]){put([east,3,6],'smooth_stone');put([east,4,6],east===3?'iron_block':'gold_block');put([east,5,6],east===3?'iron_block':'gold_block');put([east,6,6],'stone_slab[type=bottom]');}
 for(const pos of [[3,3,18],[4,3,18],[3,4,18],[22,3,19],[23,3,19]])container(pos);
 for(const east of [6,21])lamp(east,7,12,9);for(const east of [3,15,24])vine(east,9,2,3);
 port([0,3,11],[1,6,13]);port([25,3,11],[26,6,13]);
 return {structure,metadata:{name:'s4-14-armory',reference:'S4-14',ports:[{id:'west',feet:[0.5,3,12.5],width:3,height:4},{id:'east',feet:[26.5,3,12.5],width:3,height:4}],start:[1.5,3,12.5],targets:[{label:'east exit',position:[25.5,3,12.5]},{label:'inspection island',position:[13.5,3,13.5]},{label:'anvil',position:[23.5,3,7.5]},{label:'weapons cabinet',position:[21.5,3,6.5]}],differences:['Iron and gold block mannequins replace equipped armor-stand entities; iron-bar/chain racks replace item weapons.','Empty cabinet is decorative, no locks, keys or weapon rewards.','Dry three-wide boardwalk crosses the illustrated surrounding water to preserve accessibility.','Full roof restored, missing wall only in display copy; no enemy or combat configuration.']}};
}
export default function revised(api){const result=build(api);if(typeof result.structure.getSize==='function')enhance(api,result.structure,result.metadata);return result;}
