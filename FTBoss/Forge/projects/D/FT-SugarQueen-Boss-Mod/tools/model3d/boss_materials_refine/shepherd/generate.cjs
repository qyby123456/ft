const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const {makeCanvas,writeSafe} = require('../../lib_itemtex.cjs');
const {encodePNG} = require('../../png.cjs');
const renderer = require('./render.cjs');
const root = path.resolve(__dirname,'../../../..');
const resource = path.join(root,'src/main/resources/assets/ftboss');
const output = path.join(root,'art/boss_materials/refine/shepherd');
const work = path.join(root,'_work/boss-materials-refine-20261021/shepherd');
const targets = [
  'src/main/resources/assets/ftboss/textures/item/shepherd_material.png',
  'src/main/resources/assets/ftboss/textures/item/boss_materials/shepherd.png',
  'src/main/resources/assets/ftboss/models/item/shepherd_material.json',
  'src/main/resources/assets/ftboss/models/item/boss_materials/shepherd_material.json',
  'src/main/resources/assets/ftboss/models/item/boss_materials/shepherd_material_gui.json',
  'art/boss_materials/blockbench/shepherd_material.bbmodel'
];
fs.mkdirSync(output,{recursive:true});
fs.mkdirSync(work,{recursive:true});
if(!fs.existsSync(path.join(work,'backup_manifest.json'))) {
  const backup=[];
  for(const relative of targets) {
    const source=path.join(root,relative),destination=path.join(work,'backup',relative);
    fs.mkdirSync(path.dirname(destination),{recursive:true});
    fs.copyFileSync(source,destination);
    backup.push({path:relative,sha256:crypto.createHash('sha256').update(fs.readFileSync(source)).digest('hex')});
  }
  fs.writeFileSync(path.join(work,'backup_manifest.json'),JSON.stringify(backup,null,2)+'\n');
}
const pieces=[];
const round=value=>+value.toFixed(4);
function cube(name,from,to,material='bone',rotation=null) {
  const element={name,from:from.map(round),to:to.map(round),material};
  if(rotation) element.rotation={axis:rotation[0],angle:rotation[1],origin:rotation[2],rescale:false};
  pieces.push(element);
}
function joint(label,center,size) {
  const [centerX,centerY,centerZ]=center,[width,height,depth]=size;
  cube(label+'_center',[centerX-width*.43,centerY-height*.37,centerZ-depth*.46],[centerX+width*.43,centerY+height*.37,centerZ+depth*.46]);
  cube(label+'_crown',[centerX-width*.33,centerY+height*.23,centerZ-depth*.34],[centerX+width*.33,centerY+height*.53,centerZ+depth*.34],'boneLight');
  cube(label+'_lower_lip',[centerX-width*.32,centerY-height*.52,centerZ-depth*.33],[centerX+width*.32,centerY-height*.22,centerZ+depth*.33]);
  cube(label+'_front_cartilage',[centerX-width*.34,centerY-height*.29,centerZ-depth*.56],[centerX+width*.34,centerY+height*.29,centerZ-depth*.36],'boneLight');
  cube(label+'_back_cartilage',[centerX-width*.34,centerY-height*.29,centerZ+depth*.36],[centerX+width*.34,centerY+height*.29,centerZ+depth*.56]);
  cube(label+'_outer_round',[centerX-width*.52,centerY-height*.2,centerZ-depth*.32],[centerX+width*.52,centerY+height*.2,centerZ+depth*.32],'boneLight');
  for(const sign of [-1,1]) cube(label+'_oblique_'+sign,[centerX+sign*width*.29-width*.15,centerY-height*.36,centerZ-depth*.36],[centerX+sign*width*.29+width*.15,centerY+height*.36,centerZ+depth*.36],'bone',['z',sign*22.5,[centerX+sign*width*.29,centerY,centerZ]]);
}
cube('cortex_lower_shaft',[7.25,4.45,6.9],[9.15,8.2,9.08],'bone',['z',22.5,[8.2,6.2,8]]);
cube('cortex_lower_front_bevel',[7.46,4.6,6.68],[8.94,8.08,7.1],'boneLight',['z',22.5,[8.2,6.2,8]]);
cube('cortex_lower_back_bevel',[7.46,4.6,8.89],[8.94,8.08,9.28],'bone',['z',22.5,[8.2,6.2,8]]);
cube('cortex_upper_shaft',[6.93,7.65,6.82],[8.93,11.72,9.14],'bone',['z',22.5,[7.93,9.65,8]]);
cube('cortex_upper_front_bevel',[7.16,7.8,6.58],[8.7,11.59,7.04],'boneLight',['z',22.5,[7.93,9.65,8]]);
cube('cortex_upper_back_bevel',[7.16,7.8,8.93],[8.7,11.59,9.35],'bone',['z',22.5,[7.93,9.65,8]]);
cube('upper_neck_flare',[6.45,10.48,6.9],[9.48,12.1,9.12],'bone',['z',22.5,[7.9,11.1,8]]);
joint('medial_condyle',[6.35,12.24,8.02],[2.55,2.5,2.65]);
joint('lateral_condyle',[9.01,12.74,8.08],[2.6,2.55,2.7]);
cube('intercondylar_saddle',[7.22,11.75,7.04],[8.29,12.43,9.02],'boneDark');
cube('joint_ridge_front',[7.21,11.48,6.76],[8.56,11.84,7.27],'boneLight',['z',22.5,[7.8,11.65,8]]);
cube('cortical_groove_side',[6.9,9.7,7.16],[7.15,10.85,8.69],'boneDark',['z',22.5,[7.93,9.65,8]]);
cube('cortical_flake',[7.44,5.24,6.48],[8.1,6.38,6.86],'boneLight',['z',22.5,[8.2,6.2,8]]);
cube('cut_neck_flare',[8.25,3.38,6.81],[10.36,5.11,9.2],'bone',['z',-22.5,[9.18,4.25,8]]);
cube('cut_shoulder_front',[8.13,3.56,6.57],[10.06,4.65,7.02],'boneLight');
cube('cut_shoulder_back',[8.13,3.56,9.02],[10.06,4.65,9.46]);
cube('marrow_recess',[8.44,3.18,7.02],[9.88,3.47,9.01],'marrow');
cube('cut_cortex_north',[8.08,2.95,6.57],[10.2,3.66,7.05],'boneLight');
cube('cut_cortex_south',[8.08,2.95,8.98],[10.2,3.66,9.46]);
cube('cut_cortex_west',[7.94,2.95,7.01],[8.5,3.66,9.03],'boneLight');
cube('cut_cortex_east',[9.86,2.95,7.01],[10.42,3.66,9.03]);
for(const [cornerX,cornerZ] of [[8.19,6.93],[10.16,6.93],[8.19,9.09],[10.16,9.09]]) cube('cut_bevel_'+cornerX+'_'+cornerZ,[cornerX-.33,2.99,cornerZ-.35],[cornerX+.33,3.7,cornerZ+.35],'bone',['y',45,[cornerX,3.3,cornerZ]]);
cube('trabecula_longitudinal',[8.63,3.08,7.08],[8.91,3.3,8.97],'spongy');
cube('trabecula_crosswise',[8.46,3.05,7.75],[9.91,3.3,8.02],'spongy');
cube('trabecula_oblique',[9.25,3.04,7.16],[9.49,3.3,8.85],'spongy',['y',22.5,[9.37,3.2,8]]);
cube('trabecula_chip',[9.18,3.02,8.42],[9.65,3.3,8.73],'boneDark');
cube('woven_strap_front',[6.28,7.18,6.29],[9.69,8.98,6.81],'strap');
cube('woven_strap_back',[6.28,7.18,9.12],[9.69,8.98,9.62],'strap');
cube('woven_strap_left',[6.13,7.18,6.65],[6.73,8.98,9.29],'strap');
cube('woven_strap_right',[9.18,7.18,6.65],[9.79,8.98,9.29],'strap');
for(const level of [7.2,8.78]) {
  cube('strap_bound_edge_front_'+level,[6.26,level,6.22],[9.71,level+.22,6.49],'strapEdge');
  cube('strap_bound_edge_back_'+level,[6.26,level,9.42],[9.71,level+.22,9.69],'strapEdge');
  cube('strap_bound_edge_left_'+level,[6.07,level,6.42],[6.32,level+.22,9.47],'strapEdge');
  cube('strap_bound_edge_right_'+level,[9.56,level,6.42],[9.82,level+.22,9.47],'strapEdge');
}
cube('folded_diagonal_tail',[6.49,5.94,6.03],[7.76,8.74,6.44],'strap',['z',22.5,[7.13,7.33,6.24]]);
cube('tail_bound_tip',[6.47,5.89,5.99],[7.78,6.14,6.47],'strapEdge',['z',22.5,[7.13,7.33,6.24]]);
cube('charcoal_leather_buckle_bed',[7.43,7.23,6.05],[9.35,9.01,6.39],'leather');
cube('brass_buckle_left',[7.52,7.32,5.74],[7.79,9.07,6.19],'metal');
cube('brass_buckle_right',[9.08,7.32,5.74],[9.35,9.07,6.19],'metal');
cube('brass_buckle_top',[7.75,8.82,5.74],[9.13,9.1,6.19],'metalLight');
cube('brass_buckle_bottom',[7.75,7.28,5.74],[9.13,7.56,6.19],'metal');
cube('threaded_buckle_webbing',[7.83,7.57,5.97],[9.04,8.8,6.24],'strap');
cube('buckle_tongue',[8.36,7.65,5.62],[8.61,8.89,5.91],'metalLight',['z',-22.5,[8.48,8.6,5.85]]);
for(const [rivetX,rivetY] of [[7.65,7.43],[9.21,8.94]]) cube('buckle_rivet_'+rivetX,[rivetX-.13,rivetY-.13,5.57],[rivetX+.13,rivetY+.13,5.86],'metalLight');
cube('rear_sewn_overlap',[6.73,7.37,9.53],[8.04,8.76,9.83],'strapEdge');
cube('rear_seam_webbing',[6.9,7.54,9.78],[7.87,8.59,9.94],'strap');
cube('whistle_attachment_loop_top',[9.51,8.44,6.34],[11.2,8.74,6.72],'metalLight');
cube('whistle_attachment_loop_bottom',[9.51,7.54,6.34],[11.2,7.85,6.72],'metal');
cube('whistle_attachment_loop_inner',[9.45,7.78,6.34],[9.76,8.52,6.72],'metal');
cube('whistle_attachment_loop_outer',[10.95,7.78,6.34],[11.27,8.52,6.72],'metalLight');
cube('whistle_neck',[10.59,7.19,6.13],[11.22,7.79,6.9],'metalLight');
cube('whistle_blank_body',[10.27,5.1,6.07],[11.58,7.33,7.21],'metal');
cube('whistle_front_bevel',[10.46,5.18,5.82],[11.41,7.25,6.25],'metalLight');
cube('whistle_back_bevel',[10.46,5.18,7.03],[11.41,7.25,7.43],'metal');
cube('whistle_low_ferrule',[10.2,5.04,5.98],[11.66,5.43,7.3],'metalLight');
cube('whistle_reed_recess',[10.58,6.3,5.74],[11.27,6.86,5.88],'leather');
cube('reed_window_left_rail',[10.44,6.22,5.58],[10.65,6.94,5.95],'metal');
cube('reed_window_right_rail',[11.2,6.22,5.58],[11.43,6.94,5.95],'metal');
cube('reed_window_upper_rail',[10.61,6.83,5.58],[11.24,7.03,5.95],'metalLight');
cube('reed_window_lower_lip',[10.61,6.16,5.58],[11.24,6.37,5.95],'metalLight');
cube('whistle_unfinished_reed',[10.7,5.87,5.61],[11.15,6.3,5.82],'metalLight',['x',-22.5,[10.9,6.18,5.8]]);
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
const model={ambientocclusion:false,gui_light:'side',textures:{atlas:'ftboss:item/boss_materials/shepherd',particle:'ftboss:item/shepherd_material'},elements,display};
writeSafe(path.join(resource,'textures/item/boss_materials/shepherd.png'),encodePNG(atlasSize,atlasSize,atlas.buf));
writeSafe(path.join(resource,'models/item/boss_materials/shepherd_material.json'),JSON.stringify(model,null,2)+'\n');
const icon=renderer.render(model,{w:atlasSize,h:atlasSize,data:atlas.buf},{width:32,height:32,scale:1.94,camera:[-.35,.28,-1],roll:28,center:[8.2,8.15,8]});
const outlined=Buffer.from(icon.data);
for(let iconY=0;iconY<32;iconY++) for(let iconX=0;iconX<32;iconX++) {
  const offset=(iconY*32+iconX)*4;
  if(icon.data[offset+3]) continue;
  const touches=[[1,0],[-1,0],[0,1],[0,-1]].some(([offsetX,offsetY])=>{const sampleX=iconX+offsetX,sampleY=iconY+offsetY;return sampleX>=0&&sampleX<32&&sampleY>=0&&sampleY<32&&icon.data[(sampleY*32+sampleX)*4+3]>0;});
  if(touches) outlined.set([40,46,37,255],offset);
}
icon.data=outlined;
writeSafe(path.join(resource,'textures/item/shepherd_material.png'),encodePNG(32,32,icon.data));
const uuid=label=>{const hash=crypto.createHash('sha256').update('shepherd-refine:'+label).digest('hex');return hash.slice(0,8)+'-'+hash.slice(8,12)+'-4'+hash.slice(13,16)+'-a'+hash.slice(17,20)+'-'+hash.slice(20,32);};
const bbElements=elements.map(element=>({
  name:element.name,type:'cube',uuid:uuid(element.name),from:element.from,to:element.to,origin:element.rotation?element.rotation.origin:[8,8,8],
  rotation:['x','y','z'].map(axis=>element.rotation&&element.rotation.axis===axis?element.rotation.angle:0),rescale:false,autouv:0,color:0,
  faces:Object.fromEntries(Object.entries(element.faces).map(([face,data])=>[face,{uv:data.uv.map(value=>round(value*atlasSize/16)),texture:0}]))
}));
const bbmodel={meta:{format_version:'4.10',model_format:'java_block',box_uv:false},name:'牧野哨骨 / Benger material',model_identifier:'shepherd_material',visible_box:[1,1,0],resolution:{width:atlasSize,height:atlasSize},elements:bbElements,outliner:bbElements.map(element=>element.uuid),display,textures:[{name:'shepherd.png',id:'0',uuid:uuid('texture'),mode:'bitmap',source:'data:image/png;base64,'+encodePNG(atlasSize,atlasSize,atlas.buf).toString('base64'),width:atlasSize,height:atlasSize,uv_width:atlasSize,uv_height:atlasSize,particle:true}]};
writeSafe(path.join(root,'art/boss_materials/blockbench/shepherd_material.bbmodel'),JSON.stringify(bbmodel,null,2)+'\n');
fs.writeFileSync(path.join(output,'uv_islands.json'),JSON.stringify(islands.map(({element,face,x,y,width,height})=>({element:element.name,material:element.material,face,x,y,width:round(width),height:round(height),density})),null,2)+'\n');
renderer.previews(root,output);
console.log(JSON.stringify({elements:elements.length,rotated:elements.filter(element=>element.rotation).length,atlas:atlasSize,atlasUsedHeight:shelfY+shelfHeight,faces:islands.length,output},null,2));
