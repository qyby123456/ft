export function deepCavern(api, structure) {
  const put=(x,y,z,block)=>api.setBlock(structure,[x,y,z],`minecraft:${block}`)
  for(let z=33;z<=64;z++)for(let y=0;y<24;y++)for(const x of [0,34])put(x,y,z,'cobbled_deepslate')
  for(let z=31;z<=63;z++)for(let x=1;x<=33;x++) {
    const left=1+Math.round((Math.sin(z*.29)+1)*1.4),right=31-Math.round((Math.cos(z*.37)+1)*2)
    const floor=1+Math.max(0,Math.round(Math.sin(x*.4+z*.21)))
    const roof=18+Math.round(2*Math.sin(x*.22+z*.25))
    for(let y=0;y<=22;y++) {
      const rock=x<=left||x>=right||y<=floor||y>=roof||z>=62
      const vein=Math.sin(x*.38+z*.24+y*.43)>.74
      put(x,y,z,rock?(vein?'amethyst_block':(x+y+z)%17===0?'calcite':'cobbled_deepslate'):'air')
    }
  }
  for(let x=9;x<=12;x++)for(let y=2;y<=15;y++)for(let z=26;z<=27;z++)put(x,y,z,'air')
  for(const [x,z,base,height] of [[5,36,2,5],[11,43,2,9],[23,45,2,5],[6,51,2,11],[18,54,2,7],[26,58,2,10],[11,59,2,5]]) {
    for(let y=base;y<base+height;y++)put(x,y,z,'amethyst_block')
    put(x,base+height,z,'amethyst_cluster[facing=up,waterlogged=false]')
    for(const [dx,dz] of [[-1,0],[1,0],[0,1]]){
      put(x+dx,base,z+dz,'amethyst_block')
      put(x+dx,base+1,z+dz,'amethyst_cluster[facing=up,waterlogged=false]')
    }
  }
  for(const [x,z] of [[3,40],[17,47],[29,54]]) {
    for(let y=13;y<=17;y++)put(x,y,z,'cobbled_deepslate')
    put(x,12,z,'amethyst_block')
    put(x,11,z,'amethyst_cluster[facing=down,waterlogged=false]')
  }
}
