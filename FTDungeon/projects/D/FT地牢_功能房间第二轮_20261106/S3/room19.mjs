import {toolkit} from './common.mjs'
export default function build(api){
  const {put,box,lamp,icicle,finish}=toolkit(api,[35,23,31],'s3-19')
  box(0,0,0,34,1,30,'deepslate_tiles')
  box(0,2,0,34,20,1,'deepslate_bricks');box(0,2,29,34,20,30,'deepslate_bricks')
  box(0,2,2,1,20,28,'deepslate_bricks');box(33,2,2,34,20,28,'deepslate_bricks')
  box(0,21,0,34,21,30,'packed_ice');box(0,22,0,34,22,30,'snow_block')
  box(3,2,15,28,7,28,'stone_bricks');box(16,2,28,20,7,30,'stone_bricks')
  box(4,2,15,27,5,24,'air')
  for(const east of [4,26])box(east,2,24,east+1,6,25,'stone_bricks')
  for(let step=0;step<12;step++){
    const south=2+step,level=2+Math.floor(step/2)
    if(level>2)box(29,2,south,32,level-1,south,'stone_bricks')
    box(29,level,south,32,level,south,step%2===0?'stone_brick_slab[type=bottom]':'stone_bricks')
  }
  box(29,2,14,32,7,17,'stone_bricks')
  box(3,8,15,27,8,15,'spruce_fence');box(3,8,16,3,8,27,'spruce_fence')
  for(const east of [3,9,17,25]){put(east,8,15,'stone_brick_wall');put(east,9,15,'snow_block')}
  for(const [east,south,high] of [[5,5,8],[11,4,12],[18,7,6],[24,4,10]]){box(east,2,south,east+1,high,south+1,'packed_ice');put(east,high+1,south,'blue_ice')}
  for(const east of [10,18])put(east,8,18,'spruce_fence')
  box(10,9,18,18,9,20,'spruce_slab[type=top]');put(11,10,19,'lantern[hanging=false]')
  put(15,10,19,'lightning_rod[facing=west,powered=false]')
  put(16,10,19,'spruce_fence');put(16,11,19,'lightning_rod[facing=west,powered=false]')
  put(15,11,19,'dark_prismarine_slab[type=bottom]')
  box(5,8,26,10,11,26,'spruce_fence');for(const east of [6,8,10])put(east,12,26,'end_rod[facing=up]')
  put(22,8,26,'barrel[facing=up]',{id:'minecraft:barrel',Items:[]})
  box(6,8,17,6,14,17,'spruce_fence');box(6,14,17,9,14,17,'spruce_log[axis=x]');put(9,13,17,'chain[axis=y]');put(9,12,17,'lantern[hanging=true]')
  for(const east of [3,11,22,31])icicle(east,28,20,4)
  box(2,2,19,3,7,25,'packed_ice');box(25,8,25,27,11,27,'blue_ice')
  box(33,2,2,34,5,4,'air');box(17,8,29,19,11,30,'air')
  return finish({title:'冰渊高台瞭望所',reference:'S3-19',features:['高六格俯瞰平台','十二级半格阶梯','观测桌与望远镜意象','栏墙灯柱','下方冰渊景观'],ports:[{name:'lower-east',foot:[34,2,3],width:3,height:4,normal:[1,0,0]},{name:'upper-south',foot:[18,8,30],width:3,height:4,normal:[0,0,1]}],start:[34,2,3],targets:[{label:'upper exit',position:[18,8,30]},{label:'watch desk',position:[14,8,21]},{label:'parapet',position:[20,8,16]},{label:'stair landing',position:[30,8,16]}],differences:['望远镜以避雷针表现，不提供放大视野交互','概念无限深渊改成完整封闭冰窟，保留高台与实际六格高差','半格台阶可步行上行，非单纯装饰楼梯'],cutY:21})
}
