import {tools} from '../common.mjs';
import {enhance} from '../enrich.mjs';
function build(api){
 const {structure,put,fill,masonry,floor,port,beam,lamp,vine,container,pool}=tools(api,[31,13,21]);
 floor([0,0,0],[30,2,20]);masonry([0,3,0],[30,9,1]);masonry([0,3,19],[30,9,20]);masonry([0,3,2],[1,9,18]);masonry([29,3,2],[30,9,18]);fill([0,10,0],[30,10,20],'mossy_stone_bricks');
 masonry([7,3,2],[8,9,12]);masonry([8,3,11],[19,9,12]);masonry([18,3,2],[19,9,10]);
 port([7,3,7],[8,6,9]);port([18,3,7],[19,6,9]);
 fill([2,2,7],[28,2,9],'dark_oak_planks');fill([10,2,8],[16,2,10],'mud_bricks');
 pool([3,2,14],[26,2,17]);floor([22,1,3],[27,2,7]);
 container([24,3,4],'chest','south');put([23,3,4],'gold_block');put([26,3,5],'gold_block');put([27,4,2],'cobweb');put([27,5,2],'cobweb');
 put([20,4,3],'lever[face=wall,facing=east,powered=false]');
 for(const east of [3,11,20,28]){beam([east,9,0],[east,9,20]);vine(east,9,2,3);}lamp(25,7,6,9);lamp(12,7,8,9);
 port([0,3,7],[1,6,9]);port([29,3,7],[30,6,9]);
 return {structure,metadata:{name:'s4-20-secret-vault',reference:'S4-20',ports:[{id:'west',feet:[0.5,3,8.5],width:3,height:4},{id:'east',feet:[30.5,3,8.5],width:3,height:4}],start:[1.5,3,8.5],targets:[{label:'east escape',position:[29.5,3,8.5]},{label:'hidden wall gap',position:[9.5,3,8.5]},{label:'empty treasure chest',position:[24.5,3,5.5]},{label:'lever access',position:[21.5,3,4.5]}],differences:['This is the tenth functional secret passage/vault room, not an overview.','Open disguised masonry gaps are permanent; decorative lever does not actuate a hidden door.','Chest is empty with no LootTable or Lock; gold block piles decorative only, not survival reward balancing.','Cobwebs restricted to upper remote corner; dry corridor bypasses lined lily pool. Full roof/walls retained.']}};
}
export default function revised(api){const result=build(api);if(typeof result.structure.getSize==='function')enhance(api,result.structure,result.metadata);return result;}
