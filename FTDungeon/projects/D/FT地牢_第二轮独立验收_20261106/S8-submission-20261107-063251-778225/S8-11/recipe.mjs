import {tools} from '../common.mjs'
import {reviseLibrary} from '../revise-library.mjs'
export default function build(api) {
  const size=[31,16,29]
  const t=tools(api,size)
  const {put,box,floor,pillar,crystal,hanging,container,port,desk}=t
  floor([0,0],[30,28]);box([0,0,0],[30,0,28],'smooth_basalt')
  for(let x=0;x<31;x++) for(let z=0;z<29;z++) {
    const edge=Math.min(x,30-x,z,28-z),roof=12+Math.min(2,edge)
    if(edge===0)for(let y=2;y<=roof;y++)put(x,y,z,y===5||y===10?'calcite':(x+z+y)%7===0?'amethyst_block':'cobbled_deepslate')
    put(x,roof,z,(x+z)%6===0?'amethyst_block':'deepslate_tiles')
  }
  box([3,2,2],[27,6,3],'bookshelf');box([2,2,4],[3,6,10],'bookshelf');box([27,2,4],[28,6,10],'bookshelf')
  box([3,7,2],[27,7,5],'dark_oak_planks');box([3,8,2],[27,10,2],'bookshelf');box([3,8,5],[27,8,5],'dark_oak_fence');box([16,8,5],[16,8,5],'air')
  box([16,2,6],[16,7,6],'ladder[facing=south,waterlogged=false]');box([16,2,5],[16,6,5],'dark_oak_planks')
  for(const x of [5,25])for(const z of [7,23])pillar(x,z,11)
  for(const x of [7,23])hanging(x,9,12,13)
  desk(10,12,5);desk(19,19,5)
  box([10,2,18],[14,2,18],'dark_oak_stairs[facing=north,half=bottom,shape=straight,waterlogged=false]')
  box([4,2,5],[6,4,6],'dark_oak_planks');container(5,2,7)
  crystal(2,2,25,3);crystal(28,2,25,3);crystal(14,8,3,2)
  for(let x=8;x<=23;x++)put(x,1,17,'purple_concrete')
  const ports=[port('west',16),port('east',16)]
  reviseLibrary(api,t.structure,size)
  return {structure:t.structure,metadata:{name:'s8-11-library',reference:'S8-11',size,ports,start:[0,2,16],targets:[{label:'east exit',position:[30,2,16]},{label:'reading desks',position:[17,2,14]},{label:'ladder base',position:[16,2,7]}],features:['two-tier rear archive','supported ladder to balcony','two reading desks','cabinet','calcite banded geode'],differences:['Concept open books represented by vanilla lecterns without invented book inventory','Ceiling and front wall restored in exported NBT','Archive ladder usable but climbing physics not certified by offline walking audit']}}
}
