const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const {makeCanvas,writeSafe} = require('../../lib_itemtex.cjs');
const {encodePNG} = require('../../png.cjs');
const renderer = require('./render.cjs');
const root = path.resolve(__dirname,'../../../..');
const resource = path.join(root,'src/main/resources/assets/ftboss');
const output = path.join(root,'art/boss_relics/refine/shepherd');
const work = path.join(root,'_work/boss-relics-refine-20261021/shepherd');
const targets = [
  'src/main/resources/assets/ftboss/textures/item/shepherd_whistle_charm.png',
  'src/main/resources/assets/ftboss/textures/item/boss_relics/shepherd_whistle_charm.png',
  'src/main/resources/assets/ftboss/models/item/shepherd_whistle_charm.json',
  'src/main/resources/assets/ftboss/models/item/boss_relics/shepherd_whistle_charm.json',
  'src/main/resources/assets/ftboss/models/item/boss_relics/shepherd_whistle_charm_gui.json',
  'art/boss_relics/blockbench/shepherd_whistle_charm.bbmodel'
];
fs.mkdirSync(output,{recursive:true});
fs.mkdirSync(work,{recursive:true});
if(!fs.existsSync(path.join(work,'backup_manifest.json'))) {
  const backup=[];
  for(const relative of targets) {
    const source=path.join(root,relative),destination=path.join(work,'backup',relative);
    fs.mkdirSync(path.dirname(destination),{recursive:true});
    if(!fs.existsSync(source)) { backup.push({path:relative,existed:false}); continue; }
    fs.copyFileSync(source,destination);
    backup.push({path:relative,sha256:crypto.createHash('sha256').update(fs.readFileSync(source)).digest('hex')});
  }
  fs.writeFileSync(path.join(work,'backup_manifest.json'),JSON.stringify(backup,null,2)+'\n');
}
fs.mkdirSync(path.join(root,'art/boss_relics/blockbench'),{recursive:true});
const pieces=[];
const round=value=>+value.toFixed(4);
function cube(name,from,to,material='bone',rotation=null) {
  const element={name,from:from.map(round),to:to.map(round),material};
  if(rotation) element.rotation={axis:rotation[0],angle:rotation[1],origin:rotation[2],rescale:false};
  pieces.push(element);
}
require('./shape.cjs')(cube);
const palette={
  bone:[[111,105,82],[158,152,123],[198,193,162],[225,221,195],[244,240,217]],
  boneLight:[[130,123,95],[182,175,143],[217,213,183],[239,235,209],[251,246,224]],
  boneDark:[[78,78,61],[118,115,88],[157,150,116],[187,179,141],[213,204,167]],
  marrow:[[57,55,42],[88,77,54],[119,103,73],[153,132,93],[178,157,114]],
  spongy:[[111,97,67],[157,138,98],[185,170,124],[214,194,146],[235,218,176]],
  strap:[[30,57,39],[50,83,47],[76,108,54],[111,139,72],[148,165,100]],
  strapEdge:[[35,62,40],[59,84,44],[93,120,58],[124,146,79],[153,167,105]],
  metal:[[55,58,51],[99,89,57],[154,131,74],[201,177,109],[239,219,153]],
  metalLight:[[71,71,57],[123,109,68],[180,156,94],[222,198,132],[250,231,172]],
  leather:[[20,27,29],[30,39,40],[47,56,54],[70,75,65],[102,103,84]]
};
const atlasSize=128,density=2,atlas=makeCanvas(atlasSize,atlasSize),islands=[];
for(const element of pieces) {
  const [width,height,depth]=element.to.map((value,index)=>value-element.from[index]);
  for(const face of ['north','south','east','west','up','down']) {
    const dimensions=['north','south'].includes(face)?[width,height]:['east','west'].includes(face)?[depth,height]:[width,depth];
    islands.push({element,face,width:dimensions[0]*density,height:dimensions[1]*density});
  }
}
islands.sort((first,second)=>Math.ceil(second.height)-Math.ceil(first.height)||Math.ceil(second.width)-Math.ceil(first.width));
let shelfX=1,shelfY=1,shelfHeight=0;
function paint(island) {
  const {element,face,width,height,x,y}=island,colors=palette[element.material];
  for(let pixelY=Math.floor(y)-1;pixelY<=Math.ceil(y+height);pixelY++) for(let pixelX=Math.floor(x)-1;pixelX<=Math.ceil(x+width);pixelX++) {
    const localX=Math.max(0,Math.min(width-.001,pixelX-x+.5)),localY=Math.max(0,Math.min(height-.001,pixelY-y+.5));
    let shade=2;
    const horizontal=localX/width,vertical=localY/height;
    if(horizontal<.18) shade=3;
    if(horizontal>.78) shade=1;
    if(vertical<.13) shade=Math.min(4,shade+1);
    if(vertical>.89) shade=Math.max(0,shade-1);
    if(element.material.startsWith('bone')) {
      if(width>=3&&height>=5&&Math.floor(localX)===Math.floor(width*.61)&&localY>height*.26&&localY<height*.77) shade=1;
      if(width>=4&&height>=4&&Math.floor(localY)===Math.floor(height*.48)&&Math.floor(localX)===Math.floor(width*.3)) shade=4;
      if(face==='down'||element.name.includes('cut_cortex')) {if((Math.floor(localX)+2*Math.floor(localY))%5===0) shade=Math.max(1,shade-1);}
    } else if(element.material.startsWith('strap')) {
      shade=Math.max(0,Math.min(4,shade+((Math.floor(localX)+Math.floor(localY))%3===0?1:0)));
      if((localY<.9||localY>height-.9)&&Math.floor(localX)%3===1) shade=4;
      if(Math.floor(localX)%4===2&&Math.floor(localY)%2===1) shade=Math.max(0,shade-1);
    } else if(element.material.startsWith('metal')) {
      if(localX<.75||localY<.65) shade=4;
      if(localY>height-.6) shade=0;
      if(width>2&&height>2&&Math.floor(localY)===Math.floor(height*.65)&&Math.floor(localX)===Math.floor(width*.45)) shade=1;
      if(element.name==='whistle_low_ferrule'&&face==='down'&&horizontal>.25&&horizontal<.75&&vertical>.23&&vertical<.77) shade=0;
    } else if(element.material==='marrow') shade=(Math.floor(localX*1.7)+Math.floor(localY*1.7)*3)%4;
    atlas.put(pixelX,pixelY,colors[shade],255);
  }
}
for(const island of islands) {
  const boxWidth=Math.ceil(island.width)+2,boxHeight=Math.ceil(island.height)+2;
  if(shelfX+boxWidth>=atlasSize) {shelfX=1;shelfY+=shelfHeight;shelfHeight=0;}
  if(shelfY+boxHeight>=atlasSize) throw new Error('128 pixel atlas overflow');
  island.x=shelfX+1;island.y=shelfY+1;shelfX+=boxWidth;shelfHeight=Math.max(shelfHeight,boxHeight);
  paint(island);
  if(!island.element.faces) island.element.faces={};
  const uv=[island.x,island.y,island.x+island.width,island.y+island.height].map(value=>round(value/atlasSize*16));
  if(island.face==='north') [uv[0],uv[2]]=[uv[2],uv[0]];
  island.element.faces[island.face]={uv,texture:'#atlas'};
}
const display={
  gui:{rotation:[26,-30,-28],translation:[0,0,0],scale:[.9,.9,.9]},
  ground:{rotation:[0,0,18],translation:[0,2.7,0],scale:[.55,.55,.55]},
  fixed:{rotation:[0,180,-26],translation:[0,0,-.2],scale:[.85,.85,.85]},
  head:{rotation:[0,0,0],translation:[0,10,0],scale:[.65,.65,.65]},
  firstperson_righthand:{rotation:[0,-65,24],translation:[1.2,2.6,.7],scale:[.74,.74,.74]},
  firstperson_lefthand:{rotation:[0,65,-24],translation:[1.2,2.6,.7],scale:[.74,.74,.74]},
  thirdperson_righthand:{rotation:[0,90,24],translation:[0,3,1],scale:[.62,.62,.62]},
  thirdperson_lefthand:{rotation:[0,-90,-24],translation:[0,3,1],scale:[.62,.62,.62]}
};
const elements=pieces.map(({material,...element})=>element);
const model={ambientocclusion:false,gui_light:'side',textures:{atlas:'ftboss:item/boss_relics/shepherd_whistle_charm',particle:'ftboss:item/shepherd_whistle_charm'},elements,display};
writeSafe(path.join(resource,'textures/item/boss_relics/shepherd_whistle_charm.png'),encodePNG(atlasSize,atlasSize,atlas.buf));
writeSafe(path.join(resource,'models/item/boss_relics/shepherd_whistle_charm.json'),JSON.stringify(model,null,2)+'\n');
const icon=renderer.render(model,{w:atlasSize,h:atlasSize,data:atlas.buf},{width:32,height:32,scale:1.94,camera:[-.35,.28,-1],roll:-12,center:[8,8.6,8]});
const outlined=Buffer.from(icon.data);
for(let iconY=0;iconY<32;iconY++) for(let iconX=0;iconX<32;iconX++) {
  const offset=(iconY*32+iconX)*4;
  if(icon.data[offset+3]) continue;
  const touches=[[1,0],[-1,0],[0,1],[0,-1]].some(([offsetX,offsetY])=>{const sampleX=iconX+offsetX,sampleY=iconY+offsetY;return sampleX>=0&&sampleX<32&&sampleY>=0&&sampleY<32&&icon.data[(sampleY*32+sampleX)*4+3]>0;});
  if(touches) outlined.set([40,46,37,255],offset);
}
icon.data=outlined;
writeSafe(path.join(resource,'textures/item/shepherd_whistle_charm.png'),encodePNG(32,32,icon.data));
const uuid=label=>{const hash=crypto.createHash('sha256').update('shepherd-relic-refine:'+label).digest('hex');return hash.slice(0,8)+'-'+hash.slice(8,12)+'-4'+hash.slice(13,16)+'-a'+hash.slice(17,20)+'-'+hash.slice(20,32);};
const bbElements=elements.map(element=>({
  name:element.name,type:'cube',uuid:uuid(element.name),from:element.from,to:element.to,origin:element.rotation?element.rotation.origin:[8,8,8],
  rotation:['x','y','z'].map(axis=>element.rotation&&element.rotation.axis===axis?element.rotation.angle:0),rescale:false,autouv:0,color:0,
  faces:Object.fromEntries(Object.entries(element.faces).map(([face,data])=>[face,{uv:data.uv.map(value=>round(value*atlasSize/16)),texture:0}]))
}));
const bbmodel={meta:{format_version:'4.10',model_format:'java_block',box_uv:false},name:'牧野哨坠 / Benger relic',model_identifier:'shepherd_whistle_charm',visible_box:[1,1,0],resolution:{width:atlasSize,height:atlasSize},elements:bbElements,outliner:bbElements.map(element=>element.uuid),display,textures:[{name:'shepherd_whistle_charm.png',id:'0',uuid:uuid('texture'),mode:'bitmap',source:'data:image/png;base64,'+encodePNG(atlasSize,atlasSize,atlas.buf).toString('base64'),width:atlasSize,height:atlasSize,uv_width:atlasSize,uv_height:atlasSize,particle:true}]};
writeSafe(path.join(root,'art/boss_relics/blockbench/shepherd_whistle_charm.bbmodel'),JSON.stringify(bbmodel,null,2)+'\n');
fs.writeFileSync(path.join(output,'uv_islands.json'),JSON.stringify(islands.map(({element,face,x,y,width,height})=>({element:element.name,material:element.material,face,x,y,width:round(width),height:round(height),density})),null,2)+'\n');
const wrapper={loader:'forge:separate_transforms',ambientocclusion:false,gui_light:'front',textures:{particle:'ftboss:item/shepherd_whistle_charm'},base:{parent:'ftboss:item/boss_relics/shepherd_whistle_charm'},perspectives:{gui:{parent:'ftboss:item/boss_relics/shepherd_whistle_charm_gui'}}};
const gui={parent:'minecraft:item/generated',textures:{layer0:'ftboss:item/shepherd_whistle_charm'}};
writeSafe(path.join(resource,'models/item/shepherd_whistle_charm.json'),JSON.stringify(wrapper,null,2)+'\n');
writeSafe(path.join(resource,'models/item/boss_relics/shepherd_whistle_charm_gui.json'),JSON.stringify(gui,null,2)+'\n');
renderer.previews(root,output);
console.log(JSON.stringify({elements:elements.length,rotated:elements.filter(element=>element.rotation).length,atlas:atlasSize,atlasUsedHeight:shelfY+shelfHeight,faces:islands.length,output},null,2));
