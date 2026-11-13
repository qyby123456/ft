import {room} from '../tools.mjs'
export default function(api){
  const {put,fill,box,container,coral,finish}=room(api,[35,13,31])
  box([0,0,0],[34,11,30],'prismarine_bricks');fill([1,1,1],[33,1,29],'dark_prismarine');fill([1,2,1],[33,2,29],'prismarine_bricks')
  for(const [east,south] of [[5,6],[14,19],[26,8],[18,25]])fill([east,2,south],[east+2,2,south+1],'sandstone')
  fill([3,2,3],[7,2,7],'water');fill([28,2,5],[31,2,8],'water')
  for(const [east,south] of [[9,10],[18,13],[27,17]]){
    put([east,3,south],'polished_andesite');put([east,4,south],'oak_fence');put([east,5,south],'hay_block',{axis:'y'});put([east,6,south],'carved_pumpkin',{facing:'south'})
    for(const offset of [-1,1])put([east+offset,5,south],'oak_fence')
  }
  for(const east of [12,22]){fill([east,3,2],[east,4,2],'oak_fence');put([east,5,2],'target',{power:'0'});put([east,6,1],'sea_lantern')}
  fill([2,3,1],[7,3,3],'dark_oak_planks');fill([2,4,1],[2,7,1],'oak_fence');fill([7,4,1],[7,7,1],'oak_fence');fill([2,6,1],[7,6,1],'dark_oak_planks');container([4,3,4])
  for(const [start,south] of [[5,16],[22,22]])fill([start,3,south],[start+6,3,south],'oak_fence')
  for(const start of [4,21]){fill([start,3,27],[start+9,3,27],'oak_slab',{type:'bottom',waterlogged:'false'});fill([start,3,28],[start+9,4,28],'oak_fence')}
  fill([1,2,20],[2,2,24],'water');coral([1,1,22],'brain');fill([32,2,4],[33,2,8],'water');coral([33,1,6],'fire')
  for(const east of [0,34]){fill([east,3,10],[east,6,14],'air');put([east,7,12],'sea_lantern')}
  for(const [up,half] of [[3,'lower'],[4,'upper']])put([0,up,10],'oak_door',{facing:'east',half,hinge:'left',open:'true',powered:'false'})
  fill([1,11,1],[33,11,29],'cyan_stained_glass');for(const east of [4,17,30])fill([east,11,0],[east,11,30],'prismarine_bricks')
  return finish('S5-15','Prismarine wet-edge drill courtyard, three straw dummies, paired targets, hurdle lanes and two spectator benches.',[
    {name:'west-open-arch',feet:[0,3,12],facing:'west',width:3,height:4},{name:'east-open-arch',feet:[34,3,12],facing:'east',width:5,height:4}
  ],[{label:'firing-line',position:[17,3,6]},{label:'dummy-1',position:[9,3,12]},{label:'dummy-2',position:[18,3,15]},{label:'dummy-3',position:[27,3,19]},{label:'spectators',position:[17,3,26]},{label:'obstacle-bypass',position:[17,3,22]}],[
    'Open-yard illusion enclosed by glass canopy for complete template; no outside ocean pressure claim.',
    'Straw figures are blocks, not damageable combat targets; target blocks remain vanilla usable.',
    'Obstacles intentionally have bypasses; no jumping certification. Small side door is decorative open leaf, main arch stays clear.'
  ],{cutHeight:10})
}
