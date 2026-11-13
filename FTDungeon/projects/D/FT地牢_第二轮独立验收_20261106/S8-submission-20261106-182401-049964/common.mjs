export function tools(api, size) {
  const structure = api.createStructure(size)
  const put = (x,y,z,block,nbt) => {
    const boundary=x===0||z===0||x===size[0]-1||z===size[2]-1
    const vein=(x<5&&z<9)||(x>size[0]-7&&z>size[2]-10)||(z===0&&x>size[0]*0.48&&x<size[0]*0.66)
    if(boundary&&y>=5&&block==='calcite')block=vein&&y%5<3?'calcite':'cobbled_deepslate'
    if(boundary&&block==='amethyst_block'&&!vein)block='cobbled_deepslate'
    api.setBlock(structure,[x,y,z],`minecraft:${block}`,undefined,nbt?api.createBlockNbt(nbt):undefined)
  }
  const box = (from,to,block) => api.fill(structure,from,to,`minecraft:${block}`)
  const floor = (min,max,y=1) => { for(let x=min[0];x<=max[0];x++) for(let z=min[1];z<=max[1];z++) {
    const vein=Math.abs(z-(size[2]*0.35+Math.sin(x*0.35)*3))<1.4&&x<size[0]*0.7
    put(x,y,z,vein?(x%4===0?'calcite':'amethyst_block'):(x*13+z*7)%11<3?'cobbled_deepslate':'polished_deepslate')
  } }
  const pillar = (x,z,top) => {box([x,2,z],[x,top,z],'polished_deepslate');put(x,4,z,'calcite');put(x,top-1,z,'amethyst_block')}
  const crystal = (x,y,z,height=2) => {box([x,y,z],[x,y+height-1,z],'amethyst_block');put(x,y+height,z,'amethyst_cluster[facing=up,waterlogged=false]'); if(x>1){put(x-1,y,z,'amethyst_block');put(x-1,y+1,z,'amethyst_cluster[facing=up,waterlogged=false]')}}
  const hanging = (x,y,z,ceiling) => {box([x,y+1,z],[x,ceiling,z],'chain[axis=y,waterlogged=false]');put(x,y,z,'lantern[hanging=true,waterlogged=false]')}
  const container = (x,y,z,type='barrel',facing='north') => put(x,y,z,`${type}[facing=${facing}${type==='chest'?',type=single,waterlogged=false':',open=false'}]`,{id:`minecraft:${type}`,Items:[]})
  const port = (side,center,width=3,height=4) => {
    const coordinate=side==='west'?0:side==='east'?size[0]-1:side==='north'?0:size[2]-1
    const start=center-Math.floor(width/2)
    for(let across=start-1;across<=start+width;across++)for(let depth=0;depth<2;depth++)for(let y=2;y<=height+3;y++){
      const frame=across===start-1||across===start+width||y>=height+2
      if(!frame)continue
      const inset=coordinate===0?depth:coordinate-depth
      const material=y===height+3?'deepslate_tile_slab[type=bottom,waterlogged=false]':'polished_deepslate'
      side==='west'||side==='east'?put(inset,y,across,material):put(across,y,inset,material)
    }
    for(let across=start;across<start+width;across++)for(let y=2;y<2+height;y++) side==='west'||side==='east'?put(coordinate,y,across,'air'):put(across,y,coordinate,'air')
    return {side,foot:side==='west'||side==='east'?[coordinate,2,center]:[center,2,coordinate],width,height}
  }
  const desk=(x,z,length=4) => {box([x,2,z],[x+length-1,2,z+1],'dark_oak_planks');put(x+1,3,z,'lectern[facing=south,has_book=false,powered=false]');put(x+length-1,3,z+1,'candle[candles=3,lit=true,waterlogged=false]')}
  return {structure,put,box,floor,pillar,crystal,hanging,container,port,desk}
}
