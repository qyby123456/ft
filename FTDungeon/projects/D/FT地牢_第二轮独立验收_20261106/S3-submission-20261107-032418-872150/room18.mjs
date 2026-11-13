import {toolkit} from './common.mjs'
export default function build(api){
  const {put,box,icicle,finish}=toolkit(api,[31,14,29],'s3-18')
  box(0,0,0,30,1,28,'deepslate_tiles')
  box(0,2,0,30,11,1,'deepslate_bricks');box(0,2,27,30,11,28,'deepslate_bricks')
  box(0,2,2,1,11,26,'deepslate_bricks');box(29,2,2,30,11,26,'deepslate_bricks')
  box(0,12,0,30,12,28,'packed_ice');box(0,13,0,30,13,28,'snow_block')
  for(const [left,right] of [[3,9],[12,18],[21,27]]){
    box(left,2,24,right,8,26,'polished_deepslate')
    for(const up of [3,6]){
      box(left+1,up,24,right-1,up+1,25,'air')
      for(let east=left+1;east<right;east+=2){put(east,up,25,'skeleton_skull[rotation=8]',{id:'minecraft:skull'});put(east+1,up,25,'bone_block[axis=x]')}
    }
  }
  box(12,2,12,19,2,18,'polished_deepslate');box(13,3,13,18,3,17,'deepslate_bricks');box(14,4,14,17,4,16,'deepslate_tile_slab[type=top]')
  for(const [east,south] of [[14,14],[17,14],[14,16],[17,16]])put(east,5,south,'candle[lit=true,candles=3]')
  for(const [east,south] of [[5,15],[8,6],[22,7]]){box(east,2,south,east+4,2,south+2,'chiseled_deepslate');box(east,3,south,east+4,3,south+2,'polished_deepslate_slab[type=bottom]')}
  box(3,1,3,6,1,7,'blue_ice');box(25,2,17,27,2,20,'snow_block')
  for(const [east,south] of [[6,20],[15,21],[24,20]]){box(east,9,south,east,11,south,'chain[axis=y]');put(east,8,south,'soul_lantern[hanging=true]')}
  for(const east of [3,10,20,27])icicle(east,26,11,4)
  box(0,2,10,1,5,12,'air');box(29,2,10,30,5,12,'air')
  return finish({title:'霜封墓窖与骨龛',reference:'S3-18',features:['三组双层骨龛','三具石棺','中央烛祭台','灵魂吊灯','冰池'],ports:[{name:'west',foot:[0,2,11],width:3,height:4,normal:[-1,0,0]},{name:'east',foot:[30,2,11],width:3,height:4,normal:[1,0,0]}],start:[0,2,11],targets:[{label:'east exit',position:[30,2,11]},{label:'altar',position:[15,2,10]},{label:'bone niches',position:[15,2,23]},{label:'sarcophagi',position:[21,2,10]}],differences:['骨龛使用原版骷髅头与骨块，非可刷怪装置','石棺为实心方块雕塑，无隐藏库存','烛火仅照明，无祭坛机关'],cutY:12})
}
