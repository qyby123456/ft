import {toolkit} from './common.mjs'
export default function build(api) {
  const {put,box,lamp,icicle,finish}=toolkit(api,[31,15,27],'s3-11',true)
  box(0,0,0,30,0,26,'deepslate_tiles');box(0,1,0,30,1,26,'stone_bricks')
  box(0,2,0,30,12,1,'stone_bricks');box(0,2,25,30,12,26,'stone_bricks')
  box(0,2,2,1,12,24,'stone_bricks');box(29,2,2,30,12,24,'stone_bricks')
  box(0,13,0,30,13,26,'packed_ice');box(0,14,0,30,14,26,'snow_block')
  for(const east of [3,12,21,27]) {box(east,2,23,east,11,24,'spruce_log');box(east,10,23,east,11,24,'spruce_planks')}
  for(const limits of [[4,11],[13,20],[22,26]])box(limits[0],2,23,limits[1],8,24,'bookshelf')
  box(27,2,8,28,8,21,'bookshelf');box(27,9,8,28,9,24,'spruce_planks');box(26,2,18,26,8,18,'ladder[facing=west]')
  box(26,2,5,28,4,6,'spruce_planks');put(26,3,4,'barrel[facing=north]',{id:'minecraft:barrel',Items:[],Lock:'Frost archive key'})
  box(10,2,9,23,2,18,'blue_carpet')
  for(const [east,south,width] of [[5,6,5],[13,13,7],[8,20,6]]) {
    put(east,2,south,'spruce_fence');put(east+width,2,south,'spruce_fence')
    box(east,3,south,east+width,3,south+1,'spruce_slab[type=top]')
    put(east+1,4,south,'candle[lit=true,candles=2]');put(east+3,4,south,'lectern[facing=north,has_book=false]')
    box(east+1,2,south-2,east+width-1,2,south-2,'spruce_slab[type=bottom]')
  }
  for(const [east,south] of [[7,15],[19,6],[21,20]])lamp(east,south,13)
  for(const east of [3,8,16,23,28])icicle(east,22,12,3+east%3)
  box(2,2,2,5,2,3,'snow_block');box(22,1,3,26,1,5,'blue_ice')
  for(let rib=0;rib<4;rib++){put(3+rib,2,18,'bone_block[axis=y]');put(3+rib,3,19,'bone_block[axis=z]')}
  box(0,2,10,1,5,12,'air');box(27,2,10,30,5,12,'air')
  box(0,12,0,30,12,1,'snow_block');box(0,12,25,30,12,26,'snow_block')
  box(0,12,2,1,12,24,'snow_block');box(29,12,2,30,12,24,'snow_block')
  for(const east of [2,10,19,28])icicle(east,24,11,4)
  for(const east of [3,12,21,27]){
    box(east,2,22,east,10,22,'spruce_log[axis=y]')
    put(east,7,21,'blue_wall_banner[facing=north]',{id:'minecraft:banner'})
  }
  box(3,9,22,27,9,22,'spruce_log[axis=x]')
  for(const [east,length] of [[4,5],[10,3],[18,6],[25,4]]){
    box(east,11,23,east+1,12,24,'packed_ice')
    box(east,13-length,23,east,10,23,'blue_ice')
    put(east,12-length,23,'light_blue_stained_glass_pane')
  }
  for(const [east,south,up] of [[2,4,3],[3,4,2],[2,5,2],[25,2,3],[24,2,2],[25,3,2],[5,24,3],[6,24,2]])put(east,up,south,'snow_block')
  return finish({title:'霜穴图书档案室',reference:'S3-11',features:['转角高书架','三处阅览桌','梯子','蓝毯','骨骸与冰池','钥匙锁空柜'],ports:[{name:'west',foot:[0,2,11],width:3,height:4,normal:[-1,0,0]},{name:'east',foot:[30,2,11],width:3,height:4,normal:[1,0,0]}],start:[0,2,11],targets:[{label:'east exit',position:[30,2,11]},{label:'back shelves',position:[16,2,21]},{label:'reading aisle',position:[12,2,8]},{label:'cabinet',position:[25,2,4]}],differences:['细霜纹简化为雪块，冰挂为阶梯冰柱','书以空讲台表现，无可读书籍；无散落物品','柜有原版Lock标签，库存为空'],cutY:13})
}
