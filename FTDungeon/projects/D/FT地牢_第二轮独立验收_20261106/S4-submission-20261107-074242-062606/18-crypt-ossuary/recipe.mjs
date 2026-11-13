import {tools} from '../common.mjs';
import {enhance} from '../enrich.mjs';
function build(api){
 const {structure,put,fill,masonry,floor,port,beam,lamp,vine,pool}=tools(api,[29,14,29]);
 floor([0,0,0],[28,2,28]);masonry([0,3,0],[28,10,2]);masonry([0,3,26],[28,10,28]);masonry([0,3,3],[2,10,25]);masonry([26,3,3],[28,10,25]);fill([0,11,0],[28,11,28],'deepslate_bricks');
 for(const east of [4,10,18,24]){masonry([east-1,3,2],[east+1,9,4]);for(const height of [4,6,8]){port([east-1,height,3],[east+1,height+1,4]);fill([east-1,height-1,3],[east+1,height-1,4],'stone_bricks');put([east,height,3],'skeleton_skull[rotation=0]');}}
 for(const [east,south] of [[5,7],[22,7],[5,20],[22,20]]){fill([east-1,3,south-1],[east+1,3,south+3],'stone_bricks');fill([east,4,south],[east,4,south+2],'mossy_stone_brick_slab[type=bottom]');put([east-1,4,south+3],'candle[lit=true,candles=2]');}
 fill([11,3,11],[17,3,17],'mossy_stone_bricks');fill([12,4,12],[16,4,16],'stone_bricks');fill([13,5,13],[15,5,15],'mossy_stone_bricks');for(const east of [12,16])put([east,5,12],'candle[lit=true,candles=3]');
 pool([9,2,21],[18,2,24]);for(const east of [9,19])for(const south of [9,19])lamp(east,7,south,10);
 for(const east of [6,14,22])vine(east,10,3,3);
 fill([0,2,13],[9,2,15],'dark_oak_planks');fill([19,2,13],[28,2,15],'dark_oak_planks');
 port([0,3,13],[2,7,15]);port([26,3,13],[28,7,15]);
 return {structure,metadata:{name:'s4-18-crypt-ossuary',reference:'S4-18',ports:[{id:'west',feet:[0.5,3,14.5],width:3,height:5},{id:'east',feet:[28.5,3,14.5],width:3,height:5}],start:[1.5,3,14.5],targets:[{label:'east exit around altar',position:[27.5,3,14.5]},{label:'altar approach',position:[14.5,3,10.5]},{label:'north ossuary niche',position:[10.5,3,5.5]},{label:'south sarcophagus',position:[21.5,3,24.5]}],differences:['Native skeleton-skull blocks used in recessed niches; no bone item entities or mobs.','Four sealed sarcophagi are solid decorative masonry, no loot interiors.','Central stepped altar is approached from dry perimeter; upper steps are decorative, not certified climb route.','No spectral effects or dynamic dripping; chains, vines, shallow south pool and full dark roof retained.']}};
}
export default function revised(api){const result=build(api);if(typeof result.structure.getSize==='function')enhance(api,result.structure,result.metadata);return result;}
