import {tools} from '../common.mjs';
import {enhance} from '../enrich.mjs';
function build(api) {
  const {structure,put,fill,beam,container,lamp,port,pool,masonry,floor,desk,vine}=tools(api,[29,15,25]);
  floor([0,0,0],[28,2,24]);
  masonry([0,3,0],[28,11,1]); masonry([0,3,23],[28,11,24]);
  masonry([0,3,2],[1,11,22]); masonry([27,3,2],[28,11,22]);
  fill([0,12,0],[28,12,24],'dark_oak_planks');
  for(let east=0;east<29;east+=7){beam([east,3,0],[east,12,0]);beam([east,12,0],[east,12,24]);}
  for(let east=3;east<=25;east++)for(let height=3;height<=10;height++)put([east,height,2],height===6?'dark_oak_log':'bookshelf');
  for(let south=3;south<=19;south++)for(let height=3;height<=9;height++)put([2,height,south],height===6?'dark_oak_log':'bookshelf');
  fill([4,7,3],[24,7,5],'dark_oak_planks');
  for(let east=4;east<=24;east++)if(east!==14)put([east,8,5],'dark_oak_fence');
  fill([14,3,3],[14,7,3],'ladder[facing=south]');
  desk(11,3,11,6);desk(20,3,8,3);desk(5,3,7,3);
  for(const east of [11,13,15])put([east,3,14],'dark_oak_slab[type=bottom]');
  container([21,3,4]);container([22,3,4]);container([21,4,4]);container([22,4,4]);
  pool([4,2,17],[10,2,21]);pool([18,2,15],[24,2,21]);
  fill([2,2,11],[26,2,13],'dark_oak_planks');
  for(const east of [8,22])lamp(east,8,11,11);
  for(const east of [4,11,18,25])vine(east,10,3,4);
  for(const south of [6,18]){beam([0,1,south],[3,4,south],'mangrove_roots');beam([25,1,south],[28,4,south],'mangrove_roots');}
  port([0,3,11],[2,6,13]);port([26,3,11],[28,6,13]);
  return {structure,metadata:{name:'s4-11-library',reference:'S4-11',ports:[{id:'west',feet:[0.5,3,12.5],width:3,height:4},{id:'east',feet:[28.5,3,12.5],width:3,height:4}],start:[1.5,3,12.5],targets:[{label:'east exit',position:[27.5,3,12.5]},{label:'reading table',position:[12.5,3,10.5]},{label:'archive cabinet',position:[22.5,3,5.5]}],differences:['Complete timber roof and front wall restored; display cutaway only.','Vanilla lecterns approximate illustrated open books; barrels are empty and not locked.','Dry 3-wide boardwalk added; water limited to shallow lined basins.','Gallery ladder is native climbable but upper-floor navigation requires later gameplay review.']}};
}
export default function revised(api){const result=build(api);if(typeof result.structure.getSize==='function')enhance(api,result.structure,result.metadata);return result;}
