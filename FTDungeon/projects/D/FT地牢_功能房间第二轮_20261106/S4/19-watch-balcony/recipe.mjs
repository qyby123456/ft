import {tools} from '../common.mjs';
import {enhance} from '../enrich.mjs';
function build(api){
 const {structure,put,fill,masonry,floor,port,beam,lamp,vine,container}=tools(api,[29,19,25]);
 floor([0,0,0],[28,2,24]);masonry([0,3,0],[28,15,1]);masonry([0,3,23],[28,15,24]);masonry([0,3,2],[1,15,22]);masonry([27,3,2],[28,15,22]);fill([0,16,0],[28,16,24],'dark_oak_planks');
 fill([3,5,9],[26,6,22],'mossy_stone_bricks');fill([12,5,3],[26,6,8],'mossy_stone_bricks');
 for(let step=0;step<8;step++){const east=4+step;const height=3+Math.floor(step/2);fill([east,3,5],[east,height,7],step%2?'dark_oak_planks':'dark_oak_slab[type=bottom]');}
 fill([3,7,9],[3,8,22],'mossy_stone_bricks');fill([4,7,22],[26,8,22],'dark_oak_fence');
 fill([15,6,11],[21,6,16],'water[level=0]');put([17,7,13],'lily_pad');put([20,7,15],'lily_pad');
 fill([7,7,17],[12,7,18],'dark_oak_planks');put([9,8,17],'lightning_rod[facing=east,powered=false]');
 fill([4,7,10],[4,10,14],'dark_oak_planks');for(const south of [11,13])fill([5,8,south],[5,9,south],'iron_bars');container([24,7,5]);
 fill([5,9,23],[24,13,24],'gray_stained_glass');
 for(const pos of [[4,8,20],[24,8,20]]){put(pos,'dark_oak_log');lamp(pos[0],pos[1]+1,pos[2]);}
 for(const east of [7,23])lamp(east,12,8,15);for(const east of [3,14,25])vine(east,15,2,5);
 port([0,3,11],[1,6,13]);port([27,7,11],[28,11,13]);fill([26,6,11],[28,6,13],'dark_oak_planks');
 return {structure,metadata:{name:'s4-19-watch-balcony',reference:'S4-19',ports:[{id:'west',feet:[0.5,3,12.5],width:3,height:4},{id:'east',feet:[28.5,7,12.5],width:3,height:5}],start:[1.5,3,12.5],targets:[{label:'upper east exit',position:[27.5,7,12.5]},{label:'watch desk',position:[10.5,7,19.5]},{label:'outlook parapet',position:[20.5,7,21.5]},{label:'ramp upper landing',position:[12.5,7,6.5]}],differences:['Raised deck is four blocks above lower approach; eight half-block ramp steps are collision-tested without jumping.','Enclosing gray glass behind parapet provides a real complete outer boundary; transparent outlook replaces open concept cutaway.','Spyglass approximated by horizontal lightning rod on desk; no optical item function claimed.','Upper east interface has feet Y7, lower west Y3; combined building must explicitly ramp adjacent connector.']}};
}
export default function revised(api){const result=build(api);if(typeof result.structure.getSize==='function')enhance(api,result.structure,result.metadata);return result;}
