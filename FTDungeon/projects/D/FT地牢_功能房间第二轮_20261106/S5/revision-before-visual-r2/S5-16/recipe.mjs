import {room} from '../tools.mjs'
export default function(api){
  const {put,fill,box,container,coral,finish}=room(api,[27,16,27])
  box([0,0,0],[26,12,26],'prismarine_bricks');fill([1,1,1],[25,1,25],'dark_prismarine');fill([1,2,1],[25,2,25],'prismarine')
  fill([8,2,8],[18,4,18],'mossy_stone_bricks');fill([9,2,9],[17,3,17],'water');fill([9,4,9],[17,4,17],'air')
  fill([9,4,9],[17,4,17],'cyan_stained_glass')
  for(const east of [8,18]){fill([east,5,9],[east,9,9],'oak_log',{axis:'y'});put([east,4,9],'dark_prismarine')}
  fill([8,10,9],[18,10,9],'oak_log',{axis:'x'});fill([13,7,9],[13,9,9],'chain',{axis:'y',waterlogged:'false'});container([13,6,9])
  for(const east of [3,22])for(const south of [3,22])container([east,3,south])
  fill([3,2,18],[5,2,22],'water');coral([4,1,20],'brain');fill([21,2,4],[23,2,8],'water');coral([22,1,6],'fire')
  for(const east of [0,26]){fill([east,3,10],[east,6,14],'air');put([east,7,12],'sea_lantern')}
  for(const east of [4,22])for(const south of [4,22])put([east,7,south],'sea_lantern')
  fill([2,12,2],[24,12,24],'cyan_stained_glass');for(const east of [6,13,20])fill([east,12,0],[east,12,26],'prismarine_bricks')
  return finish('S5-16','Central moss-rimmed cistern, supported timber pulley with chained bucket, perimeter maintenance ring.',[
    {name:'west',feet:[0,3,12],facing:'west',width:5,height:4},{name:'east',feet:[26,3,12],facing:'east',width:5,height:4}
  ],[{label:'north-maintenance',position:[13,3,6]},{label:'south-maintenance',position:[13,3,20]},{label:'west-maintenance',position:[6,3,13]},{label:'east-maintenance',position:[20,3,13]}],[
    'Clear cyan-glass safety lid at well rim prevents falls; water is two blocks deep and not a swimming objective.',
    'Pulley and bucket are connected block sculpture; no mechanical lowering or water extraction claim.',
    'Outer channels reduced to contained coral pools; dry maintenance ring and ports.'
  ],{cutHeight:12})
}
