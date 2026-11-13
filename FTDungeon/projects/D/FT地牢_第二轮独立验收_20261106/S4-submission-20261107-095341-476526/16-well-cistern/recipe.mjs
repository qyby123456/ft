import {tools} from '../common.mjs';
import {enhance} from '../enrich.mjs';
function build(api){
 const {structure,put,fill,masonry,floor,port,beam,lamp,vine,container,pool}=tools(api,[25,15,25]);
 floor([0,0,0],[24,2,24]);masonry([0,3,0],[24,11,1]);masonry([0,3,23],[24,11,24]);masonry([0,3,2],[1,11,22]);masonry([23,3,2],[24,11,22]);fill([0,12,0],[24,12,24],'mud_bricks');
 pool([4,2,16],[20,2,21]);fill([2,2,11],[22,2,13],'dark_oak_planks');
 masonry([8,3,7],[16,4,15]);fill([9,2,8],[15,3,14],'water[level=0]');port([9,4,8],[15,4,14]);
 for(const east of [8,16]){beam([east,5,8],[east,9,8]);beam([east,5,14],[east,9,14]);}
 beam([8,9,8],[16,9,8]);beam([8,9,14],[16,9,14]);beam([12,9,8],[12,9,14]);fill([12,6,11],[12,8,11],'chain[axis=y]');container([12,5,11]);
 fill([4,3,4],[6,3,5],'dark_oak_planks');fill([19,3,4],[21,3,5],'dark_oak_planks');container([4,4,4]);container([6,4,4]);container([20,4,4]);
 for(const east of [5,20])lamp(east,8,11,11);for(const east of [3,12,21])vine(east,11,2,4);
 port([0,3,11],[1,6,13]);port([23,3,11],[24,6,13]);
 return {structure,metadata:{name:'s4-16-well-cistern',reference:'S4-16',ports:[{id:'west',feet:[0.5,3,12.5],width:3,height:4},{id:'east',feet:[24.5,3,12.5],width:3,height:4}],start:[1.5,3,12.5],targets:[{label:'east exit via ledge',position:[23.5,3,12.5]},{label:'north well service',position:[12.5,3,6.5]},{label:'west supply table',position:[5.5,3,6.5]},{label:'east supply table',position:[20.5,3,6.5]}],differences:['Central cistern is lined and raised, water fully contained; illustrated spillway omitted to avoid uncontrolled spreading.','Pulley bucket approximated by suspended empty barrel; static construction, no mechanism.','Dry maintenance loop skirts north and both sides of cistern; south lily pool is optional scenery.','Complete enclosing walls and roof retained in source.']}};
}
export default function revised(api){const result=build(api);if(typeof result.structure.getSize==='function')enhance(api,result.structure,result.metadata);return result;}
