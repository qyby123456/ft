export function tools(api, size) {
  const structure = api.createStructure(size);
  const defaults = block => {const name=block.replace('minecraft:','');if(name.includes('['))return block;const state=name.endsWith('_log')||name.endsWith('froglight')?'[axis=y]':name==='barrel'?'[open=false]':'';return `minecraft:${name}${state}`;};
  const put = (pos, block, properties, nbt) => api.setBlock(structure, pos, defaults(block), properties, nbt);
  const fill = (from, to, block) => api.fill(structure, from, to, defaults(block));
  const beam = (from, to, block = 'dark_oak_log') => api.line(structure, from, to, defaults(block));
  const container = (pos, block = 'barrel', facing = 'south') => put(pos, block, {facing}, api.createBlockNbt({id:`minecraft:${block}`,Items:[]}));
  const lamp = (east, height, south, ceiling) => {put([east,height,south],'ochre_froglight'); if(ceiling) fill([east,height+1,south],[east,ceiling,south],'chain[axis=y]');};
  const port = (from, to) => fill(from,to,'air');
  const pool = (from, to) => {fill(from,to,'water[level=0]'); for(let east=from[0]+1;east<to[0];east+=3) put([east,from[1]+1,from[2]+1],'lily_pad');};
  const masonry = (from, to) => {for(let east=from[0];east<=to[0];east++)for(let height=from[1];height<=to[1];height++)for(let south=from[2];south<=to[2];south++)put([east,height,south],(east*7+south*3+height)%5?'mossy_stone_bricks':'mud_bricks');};
  const floor = (from, to) => {fill(from,to,'mossy_stone_bricks'); for(let east=from[0];east<=to[0];east++)for(let south=from[2];south<=to[2];south++)if((east*13+south*7)%11<3)put([east,to[1],south],'mud_bricks');};
  const desk = (east,height,south,width=3) => {fill([east,height,south],[east+width-1,height,south+1],'dark_oak_planks');put([east,height+1,south],'lectern[facing=south]');put([east+width-1,height+1,south+1],'candle[lit=true,candles=2]');};
  const vine = (east, top, south, length, face='north') => fill([east,top-length+1,south],[east,top,south],`vine[${face}=true]`);
  return {structure,put,fill,beam,container,lamp,port,pool,masonry,floor,desk,vine};
}
