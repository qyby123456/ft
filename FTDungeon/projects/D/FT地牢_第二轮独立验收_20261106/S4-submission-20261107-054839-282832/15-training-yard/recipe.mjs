import {tools} from '../common.mjs';
import {enhance} from '../enrich.mjs';
function build(api){
 const {structure,put,fill,masonry,floor,port,beam,lamp,vine}=tools(api,[33,13,27]);
 floor([0,0,0],[32,2,26]);masonry([0,3,0],[32,9,1]);masonry([0,3,25],[32,9,26]);masonry([0,3,2],[1,9,24]);masonry([31,3,2],[32,9,24]);
 fill([0,10,0],[32,10,26],'dark_oak_planks');
 fill([5,2,6],[22,2,19],'packed_mud');fill([6,2,7],[21,2,18],'gravel');
 for(const [east,south] of [[9,10],[17,11],[13,16]]){put([east,3,south],'dark_oak_log');put([east,4,south],'hay_block[axis=y]');put([east,5,south],'carved_pumpkin[facing=south]');fill([east-1,4,south],[east+1,4,south],'hay_block[axis=x]');}
 for(const east of [9,15,21]){fill([east,3,3],[east,4,3],'dark_oak_log');put([east,5,3],'target');}
 fill([3,3,6],[3,6,13],'dark_oak_planks');for(const south of [7,9,11])fill([4,4,south],[4,5,south],'iron_bars');
 for(const south of [7,13,19]){fill([25,3,south],[25,4,south],'dark_oak_log');fill([28,3,south],[28,4,south],'dark_oak_log');fill([26,3,south],[27,3,south],'dark_oak_fence');}
 fill([6,3,22],[13,3,22],'dark_oak_slab[type=bottom]');fill([16,3,22],[22,3,22],'dark_oak_slab[type=bottom]');
 for(const east of [2,11,22,30]){beam([east,9,0],[east,9,26]);lamp(east,7,20,9);}
 for(const east of [4,15,26])vine(east,9,2,3);
 port([0,3,19],[1,7,21]);port([31,3,19],[32,7,21]);
 return {structure,metadata:{name:'s4-15-training-yard',reference:'S4-15',ports:[{id:'west',feet:[0.5,3,20.5],width:3,height:5},{id:'east',feet:[32.5,3,20.5],width:3,height:5}],start:[1.5,3,20.5],targets:[{label:'east exit',position:[31.5,3,20.5]},{label:'dummy practice',position:[11.5,3,11.5]},{label:'target line',position:[15.5,3,5.5]},{label:'obstacle bypass',position:[29.5,3,16.5]},{label:'spectator benches',position:[16.5,3,23.5]}],differences:['Three hay-and-pumpkin dummies and native targets; no entity combat logic.','Obstacle rails are optional side course; all functions reachable on dry bypass without jumping.','Full timber canopy added because concept cutaway does not authorize missing roof.','Training floor kept dry; wetland identity in moss, mud and vines rather than flood over gravel.']}};
}
export default function revised(api){const result=build(api);if(typeof result.structure.getSize==='function')enhance(api,result.structure,result.metadata);return result;}
