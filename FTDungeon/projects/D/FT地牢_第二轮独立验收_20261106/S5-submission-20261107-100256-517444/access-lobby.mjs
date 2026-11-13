export default function build(api) {
  const structure=api.createStructure([9,5,9])
  api.fill(structure,[0,0,0],[8,4,8],'minecraft:glass')
  api.fill(structure,[1,1,1],[7,3,7],'minecraft:air')
  api.fill(structure,[0,0,0],[8,0,8],'minecraft:prismarine_bricks')
  api.setBlock(structure,[4,0,4],'minecraft:sea_lantern')
  api.setBlock(structure,[4,4,4],'minecraft:sea_lantern')
  return {structure,metadata:{name:'access-lobby',size:[9,5,9],feet:[4.5,1,4.5],purpose:'Sealed external teleport lobby; independent of unchanged merged barrier'}}
}
