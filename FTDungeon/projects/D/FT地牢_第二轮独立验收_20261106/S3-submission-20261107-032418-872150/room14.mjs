import {toolkit} from './common.mjs'
export default function build(api){
  const {put,box,icicle,finish}=toolkit(api,[27,14,25],'s3-14')
  box(0,0,0,26,1,24,'stone_bricks')
  box(0,2,0,26,11,1,'stone_bricks');box(0,2,23,26,11,24,'stone_bricks')
  box(0,2,2,1,11,22,'stone_bricks');box(25,2,2,26,11,22,'stone_bricks')
  box(0,12,0,26,12,24,'packed_ice');box(0,13,0,26,13,24,'snow_block')
  for(const east of [3,11,17,23])box(east,2,22,east,10,22,'spruce_log')
  box(3,9,22,23,10,22,'spruce_planks')
  box(12,2,20,16,7,22,'spruce_planks')
  put(14,4,19,'barrel[facing=north]',{id:'minecraft:barrel',Items:[],Lock:'Frost armory key'})
  for(const east of [4,7,10]){
    box(east,5,21,east,7,21,'spruce_fence');put(east,8,21,'end_rod[facing=up]')
    put(east,2,21,'barrel[facing=up]',{id:'minecraft:barrel',Items:[]})
  }
  box(23,3,16,23,6,21,'spruce_fence')
  for(const south of [16,18,20])put(22,5,south,'end_rod[facing=west]')
  for(const [east,material] of [[5,'iron_block'],[9,'diamond_block']]){
    box(east-1,2,17,east+1,2,19,'smooth_stone');put(east,3,18,'spruce_fence')
    put(east,4,18,material);put(east,5,18,'polished_andesite')
    put(east-1,4,18,'iron_bars');put(east+1,4,18,'iron_bars')
  }
  for(const east of [10,17])box(east,2,9,east,2,12,'spruce_fence')
  box(10,3,9,17,3,12,'spruce_slab[type=top]')
  put(13,4,10,'soul_lantern[hanging=false]');put(15,4,11,'heavy_weighted_pressure_plate[power=0]')
  put(6,2,7,'anvil[facing=east]');put(4,2,6,'barrel[facing=up]',{id:'minecraft:barrel',Items:[]})
  put(8,2,6,'grindstone[face=floor,facing=north]')
  box(19,1,3,23,1,5,'blue_ice');box(19,2,6,23,2,6,'snow_block')
  for(const east of [20,22]){put(east,2,4,'bone_block[axis=y]');put(east,3,5,'bone_block[axis=z]')}
  for(const [east,south] of [[5,14],[20,14],[13,18]]){box(east,10,south,east,11,south,'chain[axis=y]');put(east,9,south,'soul_lantern[hanging=true]')}
  box(0,11,0,26,11,1,'snow_block');box(0,11,23,26,11,24,'snow_block')
  for(const east of [3,10,18,24])icicle(east,22,11,3)
  box(0,2,13,1,5,15,'air');box(25,2,13,26,5,15,'air')
  return finish({title:'霜兵军械库',reference:'S3-14',features:['壁挂兵器意象架','两座盔甲雕塑','上锁空武器柜','铁砧砂轮','中央检验台','灵魂灯冷光'],ports:[{name:'west',foot:[0,2,14],width:3,height:4,normal:[-1,0,0]},{name:'east',foot:[26,2,14],width:3,height:4,normal:[1,0,0]}],start:[0,2,14],targets:[{label:'east exit',position:[26,2,14]},{label:'inspection',position:[13,2,7]},{label:'locked cabinet',position:[14,2,18]},{label:'anvil',position:[7,2,7]}],differences:['无实体盔甲架，使用铁块/钻石块雕塑，非可装备角色','末地烛表现武器与箭束，无武器物品或补给','上锁柜库存为空，无掉落表'],cutY:12})
}
