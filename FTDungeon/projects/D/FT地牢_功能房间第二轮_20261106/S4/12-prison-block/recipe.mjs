import {tools} from '../common.mjs';
import {enhance} from '../enrich.mjs';
function build(api){
  const {structure,put,fill,masonry,floor,port,beam,lamp,vine,container}=tools(api,[31,13,25]);
  floor([0,0,0],[30,2,24]);
  masonry([0,3,0],[30,9,1]);masonry([0,3,23],[30,9,24]);masonry([0,3,2],[1,9,22]);masonry([29,3,2],[30,9,22]);
  fill([0,10,0],[30,10,24],'mossy_stone_bricks');
  for(const east of [2,10,18,28]){masonry([east,3,2],[east,8,9]);masonry([east,3,15],[east,8,22]);beam([east,9,0],[east,9,24]);}
  for(const south of [9,15]){
    fill([3,3,south],[27,7,south],'iron_bars');
    for(const east of [6,14,23]){
      port([east-1,3,south],[east+1,6,south]);
      fill([east-1,2,south===9?10:14],[east+1,2,south===9?10:14],'dark_oak_planks');
      put([east-2,3,south],`iron_door[facing=east,half=lower,hinge=left,open=true,powered=true]`);
      put([east-2,4,south],`iron_door[facing=east,half=upper,hinge=left,open=true,powered=true]`);
    }
  }
  for(const south of [10,14]){fill([3,2,south],[27,2,south],'water[level=0]');for(const east of [6,14,23])fill([east-1,2,south],[east+1,2,south],'dark_oak_planks');}
  fill([0,2,11],[30,2,13],'dark_oak_planks');
  for(const east of [4,12,20]){fill([east,3,3],[east+3,3,4],'dark_oak_planks');put([east,4,3],'white_carpet');fill([east,3,20],[east+3,3,21],'dark_oak_planks');container([east,3,6]);}
  fill([23,3,18],[26,3,19],'dark_oak_planks');container([25,3,21]);lamp(24,4,18);fill([24,5,22],[26,6,22],'chain[axis=y]');
  for(const east of [6,14,23]){lamp(east,7,12,9);vine(east,9,2,3);}
  for(const east of [6,14,23]){port([east-1,7,0],[east+1,8,1]);fill([east-1,7,1],[east+1,8,1],'iron_bars');}
  port([0,3,11],[1,6,13]);port([29,3,11],[30,6,13]);
  return {structure,metadata:{name:'s4-12-prison-block',reference:'S4-12',ports:[{id:'west',feet:[0.5,3,12.5],width:3,height:4},{id:'east',feet:[30.5,3,12.5],width:3,height:4}],start:[1.5,3,12.5],targets:[{label:'east exit',position:[29.5,3,12.5]},... [6,14,23].flatMap(east=>[{label:`north cell ${east}`,position:[east+0.5,3,7.5]},{label:`south cell ${east}`,position:[east+0.5,3,16.5]}]),{label:'guard desk',position:[24.5,3,17.5]}],differences:['Six cells on both sides rather than perspective-compressed L-shaped illustration.','Cell doorways have 3-wide open clearance; powered-open iron doors remain at their edge, no keyed lock behavior.','Dry central boardwalk and bridges across lined water gutters.','Guard keys approximated by supported chains; cell bunks are timber with carpet, no functional beds.']}};
}
export default function revised(api){const result=build(api);if(typeof result.structure.getSize==='function')enhance(api,result.structure,result.metadata);return result;}
