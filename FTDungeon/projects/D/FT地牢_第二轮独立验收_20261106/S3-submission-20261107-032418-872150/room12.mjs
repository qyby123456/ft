import {toolkit} from './common.mjs'
export default function build(api){
  const {put,box,lamp,icicle,finish}=toolkit(api,[29,13,33],'s3-12')
  box(0,0,0,28,1,32,'deepslate_tiles')
  box(0,2,0,28,10,1,'deepslate_bricks');box(0,2,31,28,10,32,'deepslate_bricks')
  box(0,2,2,1,10,30,'stone_bricks');box(27,2,2,28,10,30,'stone_bricks')
  box(0,11,0,28,11,32,'packed_ice');box(0,12,0,28,12,32,'snow_block')
  for(let east=2;east<27;east++)for(let south=2;south<31;south++)if((east+south)%5===0)put(east,1,south,'stone_bricks')
  for(const [left,right] of [[3,10],[18,25]]){
    for(const south of [14,20,26,30])box(left,2,south,right,5,south,'stone_bricks')
    const bars=left===3?right:left
    box(bars,2,14,bars,6,30,'iron_bars')
    for(const south of [17,23,28]){
      put(bars,2,south,'iron_door[facing=east,half=lower,hinge=left,open=true,powered=false]')
      put(bars,3,south,'iron_door[facing=east,half=upper,hinge=left,open=true,powered=false]')
      box(left+2,2,south-1,left+4,2,south-1,'white_wool')
      put(left+2,2,south,'bone_block[axis=z]')
    }
    for(const south of [14,20,26,30]){box(bars,2,south,bars,8,south,'spruce_log');put(bars,9,south,'snow_block')}
  }
  box(12,2,30,16,8,30,'iron_bars');box(12,2,31,16,8,31,'blue_ice')
  box(18,2,5,25,3,6,'spruce_planks');put(20,4,5,'lantern[hanging=false]')
  put(24,2,7,'barrel[facing=north]',{id:'minecraft:barrel',Items:[]});put(18,4,5,'candle[lit=true,candles=2]')
  box(19,2,8,24,2,8,'spruce_slab[type=bottom]')
  box(3,1,4,8,1,7,'blue_ice');box(3,2,9,5,2,10,'snow_block')
  for(const east of [3,6,9])put(east,2,11,'bone_block[axis=x]')
  for(const [east,south] of [[12,16],[16,24],[14,7]])lamp(east,south,11)
  for(const south of [5,12,20,28]){icicle(2,south,10,3);icicle(26,south,10,4)}
  box(0,2,8,1,5,10,'air');box(27,2,8,28,5,10,'air')
  return finish({title:'双列冰牢与值守台',reference:'S3-12',features:['双列六间牢房','中央巡视通道','打开的铁门','后端冰封铁栅','前侧值守桌'],ports:[{name:'west',foot:[0,2,9],width:3,height:4,normal:[-1,0,0]},{name:'east',foot:[28,2,9],width:3,height:4,normal:[1,0,0]}],start:[0,2,9],targets:[{label:'east exit',position:[28,2,9]},{label:'patrol end',position:[14,2,28]},{label:'guard desk',position:[22,2,4]},{label:'left cell',position:[8,2,18]}],differences:['牢门保持打开保证静态路线；未配置红石门禁','钥匙和镣铐用柜与铁链意象表达，无掉落物','冰封池为蓝冰，无可流动水'],cutY:11})
}
