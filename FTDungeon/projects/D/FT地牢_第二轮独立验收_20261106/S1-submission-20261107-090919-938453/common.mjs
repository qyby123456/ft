export function tools(api, size) {
  const structure = api.createStructure(size)
  const state = block => {let name=block.includes(':')?block:`minecraft:${block}`;if(!name.includes('[')&&(/_log$/.test(name)||name.endsWith(':hay_block')))name+='[axis=y]';return name}
  const put = (x,y,z,block) => api.setBlock(structure,[x,y,z],state(block))
  const box = (a,b,block) => api.fill(structure,a,b,state(block))
  const stone = (x,y,z) => ['mossy_stone_bricks','stone_bricks','cracked_stone_bricks'][(x*13+y*7+z*11)%5===0?2:(x+z)%4===0?1:0]
  const floor = (a,b,y=1) => {for(let x=a[0];x<=b[0];x++)for(let z=a[1];z<=b[1];z++)put(x,y,z,stone(x,y,z))}
  const wall = (a,b) => {for(let x=a[0];x<=b[0];x++)for(let y=a[1];y<=b[1];y++)for(let z=a[2];z<=b[2];z++)put(x,y,z,stone(x,y,z))}
  const lamp = (x,y,z,top) => {for(let up=y+1;up<=top;up++)put(x,up,z,'chain[axis=y]');put(x,y,z,'lantern[hanging=true]')}
  const root = points => {for(let index=0;index<points.length-1;index++)api.line(structure,points[index],points[index+1],'minecraft:dark_oak_log[axis=y]');}
  const leaf = (a,b) => box(a,b,'oak_leaves[persistent=true,distance=1]')
  const berry = (x,z,top,length) => {put(x,top+1,z,'dark_oak_log[axis=x]');for(let y=top;y>top-length;y--)put(x,y,z,y===top-length+1?'cave_vines[berries=true,age=0]':'cave_vines_plant[berries=true]')}
  const container = (x,y,z,kind='barrel',facing='south') => api.setBlock(structure,[x,y,z],`minecraft:${kind}[facing=${facing},open=false]`,undefined,api.createBlockNbt({id:`minecraft:${kind}`,Items:[]}))
  box([0,0,0],size.map(value=>value-1),'air')
  return {structure,put,box,stone,floor,wall,lamp,root,leaf,berry,container}
}
