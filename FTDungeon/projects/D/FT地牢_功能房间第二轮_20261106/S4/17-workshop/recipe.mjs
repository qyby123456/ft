import {tools} from '../common.mjs';
import {enhance} from '../enrich.mjs';
function build(api){
 const {structure,put,fill,masonry,floor,port,beam,lamp,vine,container,pool}=tools(api,[29,14,25]);
 floor([0,0,0],[28,2,24]);masonry([0,3,0],[28,10,1]);masonry([0,3,23],[28,10,24]);masonry([0,3,2],[1,10,22]);masonry([27,3,2],[28,10,22]);fill([0,11,0],[28,11,24],'dark_oak_planks');
 fill([3,3,3],[10,3,5],'dark_oak_planks');put([4,3,5],'crafting_table');put([6,3,5],'smithing_table');put([8,3,5],'fletching_table');fill([3,6,2],[10,7,2],'dark_oak_planks');for(const east of [4,7,10])fill([east,5,3],[east,6,3],'chain[axis=y]');
 masonry([12,3,2],[16,7,5]);put([14,3,5],'furnace[facing=south,lit=false]');put([14,4,5],'ochre_froglight');fill([14,8,3],[14,12,3],'stone_bricks');
 for(const height of [3,6,9]){fill([19,height,2],[25,height,4],'dark_oak_planks');for(const [east,block] of [[19,'cobblestone'],[22,'copper_block'],[25,'iron_block']])put([east,height+1,2],block);}
 put([9,3,9],'dark_oak_planks');put([9,4,9],'grindstone[face=floor,facing=south]');
 fill([11,2,14],[22,2,21],'dark_oak_planks');fill([14,3,16],[20,3,18],'dark_oak_planks');for(const east of [15,19])for(const south of [16,18])put([east,4,south],'dark_oak_fence');put([17,4,17],'iron_block');container([12,3,20]);container([23,3,18]);
 pool([3,2,16],[8,2,21]);fill([0,2,11],[28,2,13],'dark_oak_planks');
 for(const east of [3,11,19,26]){beam([east,10,0],[east,10,24]);vine(east,10,2,3);}for(const east of [6,23])lamp(east,8,12,10);
 port([0,3,11],[1,6,13]);port([27,3,11],[28,6,13]);
 return {structure,metadata:{name:'s4-17-workshop',reference:'S4-17',ports:[{id:'west',feet:[0.5,3,12.5],width:3,height:4},{id:'east',feet:[28.5,3,12.5],width:3,height:4}],start:[1.5,3,12.5],targets:[{label:'east exit',position:[27.5,3,12.5]},{label:'grinding wheel',position:[9.5,3,10.5]},{label:'furnace',position:[14.5,3,6.5]},{label:'project table',position:[17.5,3,19.5]},{label:'material shelves',position:[22.5,3,5.5]}],differences:['Native grindstone and crafting/smithing/fletching tables provide recognizable stations; no custom tool entities.','Half-finished project approximated by a fence frame and iron block, not functional machinery.','Hearth is unlit furnace plus froglight; no exposed flame or lava.','Timber work platform and dry transverse path replace unrestricted flooded floor; full roof retained.']}};
}
export default function revised(api){const result=build(api);if(typeof result.structure.getSize==='function')enhance(api,result.structure,result.metadata);return result;}
