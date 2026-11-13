import {room} from '../tools.mjs'
export default function(api){
  const {put,fill,box,container,coral,finish}=room(api,[27,12,33])
  box([0,0,0],[26,10,32],'prismarine_bricks')
  fill([1,1,1],[25,1,31],'dark_prismarine')
  fill([1,2,1],[25,2,31],'prismarine')
  fill([11,2,0],[15,2,32],'prismarine_bricks')
  fill([11,3,0],[15,6,0],'air');fill([11,3,32],[15,6,32],'air')
  for(const east of [9,17])for(const south of [4,12,20]){
    const left=east===9
    const back=left?1:25
    fill([Math.min(east,back),3,south],[Math.max(east,back),8,south],'prismarine_bricks')
    fill([Math.min(east,back),3,south+7],[Math.max(east,back),8,south+7],'prismarine_bricks')
    fill([east,3,south+1],[east,7,south+6],'iron_bars')
    const doorSouth=south+3
    for(const [height,half] of [[3,'lower'],[4,'upper']])put([east,height,doorSouth],'iron_door',{facing:left?'east':'west',half,hinge:'left',open:'true',powered:'true'})
    put([east,5,doorSouth],'prismarine_bricks')
    put([left?east+1:east-1,5,doorSouth],'lever',{face:'wall',facing:left?'east':'west',powered:'true'})
    put([left?east-1:east+1,5,doorSouth],'lever',{face:'wall',facing:left?'west':'east',powered:'false'})
    const bedEast=left?3:22
    fill([bedEast,3,south+2],[bedEast+1,3,south+4],'white_wool')
    container([bedEast,3,south+5])
    for(const rise of [6,7,8])put([left?6:20,rise,south+4],'chain',{axis:'y',waterlogged:'false'})
    put([left?6:20,9,south+4],'prismarine_bricks')
    fill([left?10:16,2,south+1],[left?10:16,2,south+6],'water')
    put([left?10:16,2,doorSouth],'prismarine_bricks')
    coral([left?10:16,1,south+5],left?'brain':'fire')
    put([east,8,south+2],'sea_lantern')
  }
  fill([3,3,29],[8,3,30],'dark_oak_planks');container([3,3,28])
  for(const east of [3,23])put([east,5,30],'sea_lantern')
  fill([2,10,2],[24,10,30],'cyan_stained_glass')
  for(const south of [4,12,20,28])fill([0,10,south],[26,10,south],'prismarine_bricks')
  return finish('S5-12','Six barred cells flank a five-wide central processional aisle; guard desk at rear flank and overhead chains.',[
    {name:'north',feet:[13,3,0],facing:'north',width:5,height:4},{name:'south',feet:[13,3,32],facing:'south',width:5,height:4}
  ],[...([7,15,23].flatMap(south=>[{label:`west-cell-${south}`,position:[7,3,south]},{label:`east-cell-${south}`,position:[19,3,south]}])),{label:'guard-desk',position:[7,3,28]}],[
    'Original perspective cutaway completed with roof and enclosing walls.',
    'Iron doors exported open for collision verification; adjacent levers are vanilla controls, door-cycle gameplay untested.',
    'Decorative keys absent; barrels empty; no prisoners, locks or spawn logic.',
    'Shallow coral gutters replace fully flooded corridor; end ports remain dry above waterline.'
  ],{cutHeight:9})
}
