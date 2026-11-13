import {tools} from './common.mjs'
export default function build(api){
 const {structure,box,put,leaf}=tools(api,[35,30,64])
 box([0,0,0],[34,0,63],'rooted_dirt');box([0,1,0],[34,1,63],'moss_block');
 const trees=[[4,10,23],[17,8,26],[28,15,22],[7,29,20],[23,33,25],[30,46,21],[3,50,24],[9,61,18]];
 for(const [x,z,height] of trees){
  box([x,2,z],[x+1,height,z+1],'dark_oak_log[axis=y]');
  for(const [dx,dz] of [[-1,0],[2,0],[0,-1],[0,2]])put(x+dx,2,z+dz,'dark_oak_log[axis=y]');
  for(let layer=0;layer<4;layer++){
   const radius=layer===0||layer===3?2:3;
   for(let dx=-radius;dx<=radius;dx++)for(let dz=-radius;dz<=radius;dz++)if(Math.abs(dx)+Math.abs(dz)<=radius+1&&x+dx>=0&&x+dx<35&&z+dz>=0&&z+dz<64)put(x+dx,height-3+layer,z+dz,'oak_leaves[persistent=true,distance=1]');
  }
  put(x+2,height-3,z,'dark_oak_log[axis=x]');
 }
 for(const [x,z] of [[8,20],[19,24],[13,44],[25,51],[5,57]]){
  for(let step=0;step<4;step++)put(x+step,2+Math.floor(step/2),z,'mossy_cobblestone');
  leaf([x,2,z+1],[x+2,3,z+2]);
 }
 return {structure,metadata:{name:'s1-19-forest-depth'}}
}
