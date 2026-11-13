import fs from 'node:fs'
import { fileURLToPath } from 'node:url'

export default function build(api) {
  const layout = JSON.parse(fs.readFileSync(fileURLToPath(new URL('./layout.json', import.meta.url)), 'utf8'))
  const structure = api.createStructure(layout.size)
  const put = (blockX, blockY, blockZ, name, properties) => api.setBlock(structure, [blockX, blockY, blockZ], `minecraft:${name}`, properties)
  const fill = (first, last, name, properties) => api.fill(structure, first, last, `minecraft:${name}`, properties)
  const mineral = (blockX,blockZ) => ((Math.imul(blockX+37,73428767)^Math.imul(blockZ+91,912931))>>>0)%101
  fill([0,0,0], [66,18,72], 'air')
  fill([0,0,0], [66,0,72], 'deepslate_bricks')
  for (let blockX=0; blockX<67; blockX++) for (let blockZ=0; blockZ<73; blockZ++) {
    const deposit=mineral(blockX,blockZ)
    put(blockX,1,blockZ,deposit<6?'amethyst_block':deposit<9?'calcite':'deepslate_tiles')
    if (blockX===0 || blockX===66 || blockZ===0 || blockZ===72 && Math.abs(blockX-33)>2) fill([blockX,2,blockZ],[blockX,5,blockZ],'deepslate_bricks')
  }
  for (const region of layout.regions) {
    const [minX,minZ,maxX,maxZ]=region.box
    fill([minX,1,minZ],[maxX,region.feet-1,maxZ],'deepslate_bricks')
    for (let blockX=minX;blockX<=maxX;blockX++) for(let blockZ=minZ;blockZ<=maxZ;blockZ++) {
      const deposit=mineral(blockX,blockZ)
      put(blockX,region.feet-1,blockZ,deposit<6?'amethyst_block':deposit<9?'calcite':'deepslate_tiles')
      if (blockX===minX || blockX===maxX || blockZ===minZ || blockZ===maxZ) for(let blockY=region.feet;blockY<region.roof;blockY++) {
        put(blockX,blockY,blockZ,(blockY===region.feet+3 || blockY===region.roof-2) && (blockX+blockZ)%9<3?'calcite':(Math.floor(blockX/3)+Math.floor(blockZ/3)+Math.floor(blockY/3))%8<2?'amethyst_block':'deepslate_bricks')
      }
    }
    fill([minX,region.roof,minZ],[maxX,region.roof,maxZ],'deepslate_tiles')
    if (maxX-minX>10 && maxZ-minZ>9) for(const blockX of [minX+1,maxX-1]) for(const blockZ of [minZ+1,maxZ-1]) {
      fill([blockX,region.feet,blockZ],[blockX,region.roof-1,blockZ],'polished_deepslate')
      put(blockX,region.feet+4,blockZ,'calcite')
    }
  }
  for(const portal of layout.portals) {
    const [minX,minY,minZ,maxX,maxY,maxZ]=portal.box
    fill([minX,minY,minZ],[maxX,maxY,maxZ],'air')
  }
  fill([32,3,25],[34,3,25],'deepslate_bricks')
  fill([32,3,26],[34,3,26],'deepslate_brick_stairs',{facing:'north',half:'bottom',shape:'straight',waterlogged:'false'})
  fill([32,2,26],[34,2,26],'deepslate_bricks')
  fill([32,2,27],[34,2,27],'deepslate_brick_stairs',{facing:'north',half:'bottom',shape:'straight',waterlogged:'false'})
  fill([47,3,16],[47,3,18],'deepslate_brick_stairs',{facing:'west',half:'bottom',shape:'straight',waterlogged:'false'})
  fill([47,2,16],[47,2,18],'deepslate_bricks')
  fill([48,2,16],[48,2,18],'deepslate_brick_stairs',{facing:'west',half:'bottom',shape:'straight',waterlogged:'false'})
  const crystal=(blockX,feetY,blockZ,height,radius=1)=>{
    fill([blockX-radius,feetY,blockZ-radius],[blockX+radius,feetY,blockZ+radius],'polished_deepslate')
    fill([blockX,feetY+1,blockZ],[blockX,feetY+height,blockZ],'amethyst_block')
    put(blockX,feetY+height+1,blockZ,'amethyst_cluster',{facing:'up',waterlogged:'false'})
    for(const [offsetX,offsetZ] of [[-1,0],[1,0],[0,-1],[0,1]]) {
      put(blockX+offsetX,feetY+1,blockZ+offsetZ,'amethyst_block')
      put(blockX+offsetX,feetY+2,blockZ+offsetZ,'amethyst_cluster',{facing:'up',waterlogged:'false'})
    }
  }
  crystal(33,2,41,6,2)
  crystal(33,4,7,8,2)
  crystal(56,2,47,5,1)
  for(let blockX=28;blockX<=38;blockX++)for(let blockZ=13;blockZ<=19;blockZ++){
    const ring=Math.max(Math.abs(blockX-33),Math.abs(blockZ-16))
    if(ring===3||ring===2)put(blockX,3,blockZ,ring===3?'amethyst_block':'calcite')
  }
  fill([6,1,34],[14,1,42],'polished_deepslate')
  fill([7,1,35],[13,1,41],'water',{level:'0'})
  fill([9,1,37],[11,2,39],'polished_deepslate')
  crystal(10,3,38,5,1)
  for(const [blockX,blockZ] of [[22,32],[44,32],[22,50],[44,50],[22,5],[44,5],[22,22],[44,22],[4,31],[16,31],[4,46],[16,46],[51,12],[62,23],[51,42],[62,52],[27,56],[39,62],[7,15],[15,23]]) {
    const feetY=blockZ<25 && blockX>=20 && blockX<=46?4:2
    crystal(blockX,feetY,blockZ,2,0)
  }
  for(const [blockX,blockZ] of [[26,68],[40,68],[3,6],[62,3],[3,61],[62,64],[18,7],[45,59]])crystal(blockX,2,blockZ,4,1)
  for(const blockX of [53,59]) {
    api.setBlock(structure,[blockX,2,9],'minecraft:chest',{facing:'south',type:'single',waterlogged:'false'},api.createBlockNbt({id:'minecraft:chest',Items:[]}))
    fill([blockX-1,2,7],[blockX+1,6,7],'bookshelf')
  }
  for(const [blockX,blockZ,facing] of [[61,22,'up'],[8,16,'up']])api.setBlock(structure,[blockX,2,blockZ],'minecraft:barrel',{facing,open:'false'},api.createBlockNbt({id:'minecraft:barrel',Items:[]}))
  fill([7,2,15],[10,4,15],'bookshelf')
  for(const region of layout.regions.filter(region=>region.box[2]-region.box[0]>10)) {
    const [minX,minZ,maxX,maxZ]=region.box
    for(const [blockX,blockZ] of [[minX+3,minZ+3],[maxX-3,maxZ-3]]) {
      fill([blockX,region.roof-3,blockZ],[blockX,region.roof-1,blockZ],'chain',{axis:'y',waterlogged:'false'})
      put(blockX,region.roof-4,blockZ,'lantern',{hanging:'true',waterlogged:'false'})
    }
  }
  for(const blockX of [26,40]) {
    fill([blockX,2,65],[blockX,10,65],'polished_deepslate')
    fill([blockX,2,65],[blockX,3,65],'calcite')
    put(blockX,7,65,'calcite')
    fill([blockX,8,67],[blockX,10,67],'chain',{axis:'y',waterlogged:'false'})
    put(blockX,7,67,'lantern',{hanging:'true',waterlogged:'false'})
    fill([blockX,11,65],[blockX,11,67],'deepslate_bricks')
  }
  fill([26,11,65],[40,11,65],'deepslate_bricks')
  for(const blockX of [29,37])fill([blockX,11,65],[blockX+1,11,65],'calcite')
  return {structure,metadata:{name:'overview'}}
}
