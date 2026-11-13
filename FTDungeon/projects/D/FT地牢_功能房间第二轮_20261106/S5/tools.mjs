import fs from 'node:fs'
import path from 'node:path'
import {fileURLToPath} from 'node:url'
export const root=path.dirname(fileURLToPath(import.meta.url))
export function room(api,size){
  const structure=api.createStructure(size)
  const put=(pos,block,properties,nbt)=>api.setBlock(structure,pos,block.includes(':')?block:`minecraft:${block}`,properties,nbt)
  const fill=(from,to,block,properties)=>api.fill(structure,from,to,block.includes(':')?block:`minecraft:${block}`,properties)
  fill([0,0,0],size.map(value=>value-1),'air')
  const box=(from,to,block)=>api.hollowBox(structure,from,to,`minecraft:${block}`)
  const container=(pos,block='barrel',facing='north')=>put(pos,block,{facing,...(block==='barrel'?{open:'false'}:block==='chest'?{type:'single',waterlogged:'false'}:{lit:'false'})},api.createBlockNbt({id:`minecraft:${block}`,Items:new api.lodestone.NbtList([],10)}))
  const coral=(pos,color='brain')=>{put(pos,`${color}_coral_block`);put([pos[0],pos[1]+1,pos[2]],`${color}_coral`,{waterlogged:'true'})}
  const finish=(id,description,ports,targets,differences,extra={})=>{
    const roof={'S5-11':11,'S5-12':10,'S5-13':11,'S5-14':10,'S5-15':11,'S5-16':12,'S5-17':11,'S5-18':12,'S5-19':17,'S5-20':10}[id]
    for(const east of [1,size[0]-2])for(const south of [1,size[2]-2]){
      const feet=id==='S5-19'&&south===1?7:3
      fill([east,feet,south],[east,roof-1,south],'prismarine_bricks')
      put([east,feet+1,south],'chiseled_stone_bricks')
      put([east,feet+3,south],'sea_lantern')
    }
    for(const port of ports){
      const [east,up,south]=port.feet,half=Math.floor(port.width/2)
      if(['west','east'].includes(port.facing)){
        const inner=east===0?1:size[0]-2
        for(const offset of [-half-1,half+1]){
          fill([inner,up,south+offset],[inner,up+port.height,south+offset],'prismarine_bricks')
          put([inner,up+2,south+offset],'sea_lantern')
        }
        fill([inner,up+port.height,south-half-1],[inner,up+port.height,south+half+1],'prismarine_bricks')
      }else{
        const inner=south===0?1:size[2]-2
        for(const offset of [-half-1,half+1]){
          fill([east+offset,up,inner],[east+offset,up+port.height,inner],'prismarine_bricks')
          put([east+offset,up+2,inner],'sea_lantern')
        }
        fill([east-half-1,up+port.height,inner],[east+half+1,up+port.height,inner],'prismarine_bricks')
      }
    }
    for(let east=1;east<size[0]-1;east++){
      put([east,roof-1,1],'prismarine_bricks');put([east,roof-1,size[2]-2],'prismarine_bricks')
      if(east%5===1){put([east,roof,1],'dark_prismarine');put([east,roof,size[2]-2],'dark_prismarine')}
    }
    const beds={
      'S5-11':[[3,21],[23,21]],'S5-12':[[1,1],[21,1]],'S5-13':[[3,24],[25,24]],
      'S5-14':[[2,20],[24,20]],'S5-15':[[2,23],[28,23]],'S5-16':[[2,21],[20,21]],
      'S5-17':[[3,22],[22,22]],'S5-18':[[2,23],[24,23]],'S5-19':[[4,26],[13,26]],'S5-20':[[11,15],[21,15]]
    }[id]
    for(const [index,[east,south]] of beds.entries()){
      fill([east,2,south],[east+4,3,south+2],'prismarine_bricks')
      fill([east,4,south],[east+4,4,south+2],'cyan_stained_glass')
      for(let offset=1;offset<=3;offset++){
        put([east+offset,3,south+1],offset===2?'brain_coral_block':'sand')
        put([east+offset,4,south+1],offset===2?'brain_coral':offset===1?'fire_coral':'sea_pickle',offset===3?{pickles:'3',waterlogged:'true'}:{waterlogged:'true'})
      }
      put([east+(index?4:0),4,south],'sea_lantern')
    }
    const directory=path.join(root,id);fs.mkdirSync(directory,{recursive:true})
    fs.writeFileSync(path.join(directory,'design.json'),JSON.stringify({id,size,description,ports,targets,differences,revision:'visual-r2',water_policy:'DRY HABITABLE COMPARTMENT, contained wet aquarium planters and shallow pools; not a fully flooded ocean ruin. All route goals remain dry. Glass enclosure for deployment preparation, not certified deep-ocean pressure/oxygen safety.',...extra},null,2))
    return {structure,metadata:{name:id.toLowerCase()}}
  }
  return {structure,put,fill,box,container,coral,finish}
}
