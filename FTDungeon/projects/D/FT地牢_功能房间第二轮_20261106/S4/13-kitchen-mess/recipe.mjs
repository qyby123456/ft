import {tools} from '../common.mjs';
import {enhance} from '../enrich.mjs';
function build(api){
 const {structure,put,fill,masonry,floor,port,beam,lamp,vine,container,pool}=tools(api,[29,16,27]);
 floor([0,0,0],[28,2,26]);
 masonry([0,3,0],[28,10,1]);masonry([0,3,25],[28,10,26]);masonry([0,3,2],[1,10,24]);masonry([27,3,2],[28,10,24]);
 for(let inset=0;inset<=3;inset++)fill([inset,11+inset,0],[28-inset,11+inset,26],'dark_oak_planks');
 for(const east of [1,9,19,27])beam([east,3,1],[east,11,1]);
 masonry([3,3,2],[9,8,5]);port([4,3,4],[8,6,5]);fill([4,3,3],[8,3,3],'furnace[facing=south,lit=false]');
 fill([4,7,3],[8,14,4],'stone_bricks');
 put([6,4,4],'cauldron');fill([6,5,4],[6,6,4],'chain[axis=y]');put([6,3,4],'ochre_froglight');
 fill([12,3,2],[24,3,3],'dark_oak_planks');
 for(const height of [5,8]){fill([12,height,2],[24,height,3],'dark_oak_planks');for(const east of [12,15,18,21,24])put([east,height+1,2],'flower_pot');}
 for(const east of [12,16,20,24])container([east,3,4]);
 fill([6,3,9],[13,3,11],'dark_oak_planks');put([6,4,9],'red_carpet');put([7,4,9],'red_carpet');put([12,4,10],'flower_pot');
 for(const south of [15,21]){fill([13,3,south],[23,3,south+1],'dark_oak_planks');fill([13,3,south-2],[23,3,south-2],'dark_oak_slab[type=bottom]');fill([13,3,south+3],[23,3,south+3],'dark_oak_slab[type=bottom]');for(const east of [14,18,22])put([east,4,south],'flower_pot');}
 pool([3,2,17],[7,2,23]);pool([21,2,7],[25,2,10]);
 for(const pos of [[3,3,7],[4,3,7],[24,3,23],[25,3,23]])container(pos);
 for(const east of [10,24])lamp(east,8,13,10);
 for(const east of [3,10,19,25])vine(east,10,2,3);
 fill([0,2,12],[28,2,14],'dark_oak_planks');port([0,3,12],[1,6,14]);port([27,3,12],[28,6,14]);
 return {structure,metadata:{name:'s4-13-kitchen-mess',reference:'S4-13',ports:[{id:'west',feet:[0.5,3,13.5],width:3,height:4},{id:'east',feet:[28.5,3,13.5],width:3,height:4}],start:[1.5,3,13.5],targets:[{label:'east exit',position:[27.5,3,13.5]},{label:'hearth',position:[6.5,3,6.5]},{label:'butcher block',position:[9.5,3,8.5]},{label:'mess tables',position:[25.5,3,19.5]}],differences:['Hearth glow is enclosed froglight, not damaging live fire; ovens unlit and cauldron empty.','Terracotta flower pots approximate jars and plates; no item-frame entities.','Gabled full timber ceiling and chimney added; fluid kept in lined pools away from dining routes.','Timber benches are slabs; dining is decorative, furnaces and barrels remain empty.']}};
}
export default function revised(api){const result=build(api);if(typeof result.structure.getSize==='function')enhance(api,result.structure,result.metadata);return result;}
