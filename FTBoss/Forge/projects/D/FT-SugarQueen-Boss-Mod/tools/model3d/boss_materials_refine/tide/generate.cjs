const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const { encodePNG } = require('../../png.cjs');
const { render } = require('./render.cjs');
const root = path.resolve(__dirname, '../../../..');
const assets = path.join(root, 'src/main/resources/assets/ftboss');
const task = path.join(root, '_work/boss-materials-refine-20261021/tide');
const atlasSize = 256;
const elements = [];
const materials = new Map();
const masks = new Map();
const round = value => Math.round(value * 10000) / 10000;
function box(name, center, size, material, angle = 0, axis = 'z', origin = center) {
  const element = { name, from: center.map((value, index) => round(value - size[index] / 2)), to: center.map((value, index) => round(value + size[index] / 2)), faces: {} };
  if (angle) element.rotation = { origin: origin.map(round), axis, angle, rescale: false };
  elements.push(element);
  materials.set(name, material);
}
box('abyssal_shell_heel', [8,3.15,8.3], [6.8,1.8,4.9], 'shell');
box('shell_left_bevel', [4.8,4.75,8.2], [2.8,4.2,4.2], 'shell', -45);
box('shell_right_bevel', [11.2,4.75,8.2], [2.8,4.2,4.2], 'shell', 45);
box('shell_rear_heart', [8,5.1,9.65], [4.8,3.5,2.7], 'shell');
box('shell_rear_shoulder_l', [5.3,7.05,9.45], [3,3,1.9], 'scale', -22.5);
box('shell_rear_shoulder_r', [10.7,7.05,9.45], [3,3,1.9], 'scale', 22.5);
box('inner_nacre_floor', [8,4.35,7.2], [5.5,1.05,3.5], 'nacre');
box('inner_nacre_l', [5.6,5.3,6.65], [1.1,3.7,2.1], 'nacre', -45);
box('inner_nacre_r', [10.4,5.3,6.65], [1.1,3.7,2.1], 'nacre', 45);
function shard(name, center, width, length, depth, angle) {
  const half = width/2;
  const top = center[1]+length/2;
  box(name + '_dark_prism', center, [width,length,width], 'crystal_dark');
  delete elements.at(-1).faces.up;
  const tipSlope = width/Math.SQRT2;
  for (const [side,axis,slopeAngle,origin,from,to,mask] of [
    ['north','x',-45,[center[0],top,center[2]-half],[center[0]-half,top,center[2]-half],[center[0]+half,top,center[2]-half+tipSlope],'tip_north'],
    ['south','x',45,[center[0],top,center[2]+half],[center[0]-half,top,center[2]+half-tipSlope],[center[0]+half,top,center[2]+half],'tip_south'],
    ['west','z',45,[center[0]-half,top,center[2]],[center[0]-half,top,center[2]-half],[center[0]-half+tipSlope,top,center[2]+half],'tip_west'],
    ['east','z',-45,[center[0]+half,top,center[2]],[center[0]+half-tipSlope,top,center[2]-half],[center[0]+half,top,center[2]+half],'tip_east']
  ]) {
    const facetName = name+'_tip_'+side;
    const element = {name:facetName,from:from.map(round),to:to.map(round),rotation:{origin:origin.map(round),axis,angle:slopeAngle,rescale:false},faces:{},tideFace:'up'};
    elements.push(element);
    materials.set(facetName,side==='north'||side==='west'?'crystal_edge':'crystal');
    masks.set(facetName,mask);
  }
  box(name+'_cleavage_front', [center[0]-width*0.13,center[1]+length*0.06,center[2]-width*0.45], [width*0.48,length*0.78,width*0.18], 'crystal');
  box(name+'_cleavage_side', [center[0]+width*0.45,center[1]-length*0.04,center[2]+width*0.03], [width*0.18,length*0.7,width*0.6], 'crystal', 0);
}
shard('crown_primary', [9.6,9.8,8.9], 2.9,5.4,2.9,0);
shard('crown_left', [5.1,8.3,8.15], 2.2,4.2,2.2,0);
shard('outer_right', [11.8,6.8,7.55], 1.9,3.1,1.9,0);
shard('outer_left', [3.8,6.3,7.2], 1.7,2.5,1.7,0);
shard('rear_shard', [7.2,8.9,10.4], 1.8,3.0,1.8,0);
box('pearl_socket_dark', [8,5.6,6.25], [4.6,2.35,2.65], 'scale');
box('pearl_socket_nacre', [8,5.9,5.75], [3.9,1.65,2.15], 'nacre');
const pearlLayers = Array.from({length:9}, (unused,index) => {
  const height = 5.6+index*0.4;
  const radius = Math.sqrt(1.8*1.8-(height-7.2)**2);
  return [height,radius*2,radius*1.7,0.43];
});
pearlLayers.forEach(([height,width,depth,thickness], index) => box('pearl_nacre_tier_' + index, [8,height,5.8], [width,thickness,depth], 'pearl'));
const scales = [[5.45,3.85,5.65,-22.5],[7.8,3.2,5.45,0],[10.25,3.9,5.65,22.5],[4.15,5.1,6.15,-45],[11.85,5.15,6.15,45],[5.6,4.25,10.5,-22.5],[8,3.9,10.85,0],[10.4,4.25,10.5,22.5],[5.6,6.5,10.9,22.5],[8,6.2,11.12,0],[10.4,6.5,10.9,-22.5]];
scales.forEach(([horizontal,vertical,depth,angle], index) => {
  box('overlap_scale_' + index, [horizontal,vertical,depth], [2.6,1.5,0.62], 'scale', angle);
  box('scale_nacre_rim_' + index, [horizontal,vertical-0.57,depth-(depth<8?0.17:-0.17)], [2.15,0.28,0.65], 'rim', angle, 'z', [horizontal,vertical,depth]);
});
box('broken_scale_lip_l', [6.1,5.25,4.8], [1.1,1.8,0.9], 'rim', -22.5);
box('broken_scale_lip_r', [9.9,5.25,4.8], [1.1,1.8,0.9], 'rim', 22.5);
const palettes = {
  shell: ['102c3b','174654','236475','3c8190','589da4'],
  scale: ['113b49','155566','1b7b87','389ba1','6ec2bc'],
  nacre: ['527b91','79a4b4','adced0','d5e7df','eef3d8'],
  crystal_dark: ['0c394f','10536d','147b99','299fb7','75d3d7'],
  crystal: ['135c79','218ba9','38bad0','7cdee2','c8f6ed'],
  crystal_edge: ['2a7d98','51b8cd','93e3e5','c1f1eb','e7fff0'],
  pearl: ['728fa6','a2b9c3','c5d8d5','e8ecdc','fff9e7'],
  rim: ['335c70','558b9a','8bb8bd','bed8cc','e1e5c9']
};
for (const key of Object.keys(palettes)) palettes[key] = palettes[key].map(hex => [0,2,4].map(offset => parseInt(hex.slice(offset,offset+2),16)));
const atlas = { w: atlasSize, h: atlasSize, data: new Uint8Array(atlasSize*atlasSize*4) };
const faces = [];
elements.forEach((element,elementIndex) => {
  const [width,height,depth] = element.to.map((value,axis) => round(value-element.from[axis]));
  const definitions = element.tideFace ? [['up',width,depth]] : [['north',width,height],['south',width,height],['west',depth,height],['east',depth,height],['up',width,depth],['down',width,depth]];
  for (const [face,faceWidth,faceHeight] of definitions) {
    const density = Math.max(masks.has(element.name)?8:2,1/Math.min(faceWidth,faceHeight));
    faces.push({ elementIndex,face,faceWidth,faceHeight,density,width:faceWidth*density,height:faceHeight*density,material:materials.get(element.name) });
  }
});
faces.sort((first,second) => Math.ceil(second.height)-Math.ceil(first.height) || Math.ceil(second.width)-Math.ceil(first.width));
const shelves = [];
for (const island of faces) {
  const packedWidth = Math.ceil(island.width)+2;
  const packedHeight = Math.ceil(island.height)+2;
  let shelf = shelves.find(row => row.height>=packedHeight && row.left+packedWidth<=atlasSize);
  if (!shelf) {
    const top = shelves.length?shelves.at(-1).top+shelves.at(-1).height:0;
    if (top+packedHeight>atlasSize) throw new Error('Tide atlas exhausted');
    shelf = { top,height:packedHeight,left:0 };
    shelves.push(shelf);
  }
  island.left = shelf.left+1;
  island.top = shelf.top+1;
  shelf.left += packedWidth;
  const palette = palettes[island.material];
  for (let pixelY=-1;pixelY<=Math.ceil(island.height);pixelY++) for (let pixelX=-1;pixelX<=Math.ceil(island.width);pixelX++) {
    const horizontal = Math.max(0,Math.min(island.width-0.01,pixelX+0.5))/island.width;
    const vertical = Math.max(0,Math.min(island.height-0.01,pixelY+0.5))/island.height;
    const edge = Math.min(horizontal,1-horizontal,vertical,1-vertical);
    let tone = 2;
    if (island.material.startsWith('crystal')) {
      tone = horizontal<0.18?4:horizontal<0.49?3:horizontal<0.78?2:1;
      if (vertical<0.12) tone=Math.min(4,tone+1);
      if (Math.abs(horizontal-(0.23+vertical*0.46))<0.035 && vertical>0.23 && vertical<0.82) tone=4;
      if (Math.abs(vertical-0.61+horizontal*0.12)<0.025) tone=Math.max(1,tone-1);
    } else if (island.material==='pearl') {
      const element = elements[island.elementIndex];
      const worldY = element.to[1]-vertical*(element.to[1]-element.from[1]);
      const worldX = element.from[0]+horizontal*(element.to[0]-element.from[0]);
      const radius = Math.hypot((worldX-7.45)/3.1,(worldY-7.9)/3.3);
      tone=Math.max(0,Math.min(4,4-Math.floor(radius*4)));
      if (worldX>8.5 && worldY<7.1) tone=Math.max(1,tone-1);
    } else if (island.material==='scale' || island.material==='shell') {
      const row = Math.floor(vertical*island.faceHeight/1.4);
      const phase = (horizontal*island.faceWidth/1.7+row%2*0.5)%1;
      const arc = (vertical*island.faceHeight/1.4)%1+0.5*Math.abs(phase-0.5);
      tone=arc>0.92?0:arc>0.68?3:2;
      if (horizontal<0.12) tone=Math.min(4,tone+1);
      if (island.material==='shell' && vertical>0.6) tone=Math.max(0,tone-1);
    } else {
      tone=vertical<0.19?4:horizontal<0.33?3:horizontal>0.85?1:2;
      if (Math.floor((horizontal*0.5+vertical)*13)%4===0) tone=Math.max(1,tone-1);
    }
    if (edge<0.045 && island.width>3 && island.height>3) tone=Math.max(0,tone-1);
    if (island.face==='south' || island.face==='down') tone=Math.max(0,tone-1);
    if (island.face==='up' && island.material!=='pearl') tone=Math.min(4,tone+1);
    const color = [...palette[tone]];
    if (island.material==='pearl' && elements[island.elementIndex].from[1]<6.8) {color[0]=Math.min(255,color[0]+9);color[1]=Math.max(0,color[1]-5);color[2]=Math.min(255,color[2]+10);}
    const mask = masks.get(elements[island.elementIndex].name);
    let alpha = 255;
    if (mask) {
      const progression = mask==='tip_north'?vertical:mask==='tip_south'?1-vertical:mask==='tip_west'?horizontal:1-horizontal;
      const transverse = mask==='tip_north'||mask==='tip_south'?horizontal:vertical;
      const margin = 0.5/Math.max(island.width,island.height);
      if (transverse<progression/2-margin || transverse>1-progression/2+margin) alpha=0;
      if (pixelX<0 || pixelY<0 || pixelX>=Math.ceil(island.width) || pixelY>=Math.ceil(island.height)) alpha=255;
    }
    atlas.data.set([...color,alpha],((island.top+pixelY)*atlasSize+island.left+pixelX)*4);
  }
  elements[island.elementIndex].faces[island.face] = { uv:[island.left,island.top,island.left+island.width,island.top+island.height].map(value=>round(value*16/atlasSize)),texture:'#atlas' };
}
const display = {
  gui:{rotation:[18,-22,0],translation:[0,0,0],scale:[0.9,0.9,0.9]},
  ground:{rotation:[0,0,0],translation:[0,2.5,0],scale:[0.45,0.45,0.45]},
  fixed:{rotation:[0,180,0],translation:[0,0.5,0],scale:[0.72,0.72,0.72]},
  head:{rotation:[0,180,0],translation:[0,10,0],scale:[0.7,0.7,0.7]},
  thirdperson_righthand:{rotation:[0,-90,0],translation:[0,1,-1],scale:[0.56,0.56,0.56]},
  thirdperson_lefthand:{rotation:[0,90,0],translation:[0,1,-1],scale:[0.56,0.56,0.56]},
  firstperson_righthand:{rotation:[0,-25,0],translation:[1.5,1,0],scale:[0.65,0.65,0.65]},
  firstperson_lefthand:{rotation:[0,25,0],translation:[1.5,1,0],scale:[0.65,0.65,0.65]}
};
const model = {ambientocclusion:false,gui_light:'side',textures:{atlas:'ftboss:item/boss_materials/tide',particle:'ftboss:item/tide_material'},elements,display};
for (const element of elements) delete element.tideFace;
const json = value => JSON.stringify(value,null,2)+'\n';
const write = (file,data) => {fs.mkdirSync(path.dirname(file),{recursive:true});fs.writeFileSync(file,data);};
write(path.join(assets,'models/item/boss_materials/tide_material.json'),json(model));
write(path.join(assets,'textures/item/boss_materials/tide.png'),encodePNG(atlasSize,atlasSize,atlas.data));
const iconView = render(model,atlas,{size:32,yaw:-16,pitch:10,scale:1.87,center:[8,8,8],transparent:true});
const outlined = new Uint8Array(iconView.d);
for (let pixelY=1;pixelY<31;pixelY++) for (let pixelX=1;pixelX<31;pixelX++) {
  const index = (pixelY*32+pixelX)*4;
  if (iconView.d[index+3]) continue;
  if ([[0,1],[0,-1],[1,0],[-1,0]].some(([horizontal,vertical])=>iconView.d[((pixelY+vertical)*32+pixelX+horizontal)*4+3])) outlined.set([11,35,48,255],index);
}
write(path.join(assets,'textures/item/tide_material.png'),encodePNG(32,32,outlined));
const stableUuid = name => {
  const hash = crypto.createHash('sha256').update('ftboss:tide:'+name).digest('hex');
  return [hash.slice(0,8),hash.slice(8,12),'4'+hash.slice(13,16),'8'+hash.slice(17,20),hash.slice(20,32)].join('-');
};
const groups = ['01 Abyssal shell and nacre','02 Five cut crystal growths','03 Pearl core and socket','04 Overlapping scales and lips'].map(name=>({name,uuid:stableUuid(name),origin:[8,8,8],rotation:[0,0,0],export:true,isOpen:true,children:[]}));
const bbElements = elements.map(element=>{
  const uuid = stableUuid(element.name);
  const groupIndex = element.name.includes('crown_')||element.name.startsWith('outer_')||element.name.startsWith('rear_shard')?1:element.name.startsWith('pearl_')?2:element.name.includes('scale_')||element.name.includes('lip_')?3:0;
  groups[groupIndex].children.push(uuid);
  const rotation=[0,0,0];
  if(element.rotation) rotation['xyz'.indexOf(element.rotation.axis)]=element.rotation.angle;
  return {name:element.name,type:'cube',uuid,from:element.from,to:element.to,origin:element.rotation?.origin||[8,8,8],rotation,rescale:false,autouv:0,color:groupIndex,faces:Object.fromEntries(Object.entries(element.faces).map(([face,value])=>[face,{uv:value.uv.map(coordinate=>round(coordinate*atlasSize/16)),texture:0}]))};
});
const atlasPng = encodePNG(atlasSize,atlasSize,atlas.data);
const bbModel = {meta:{format_version:'4.10',model_format:'java_block',box_uv:false},name:'潮汐晶材 · 沧鳞珍珠晶核',model_identifier:'tide_material',visible_box:[1,1,0],resolution:{width:atlasSize,height:atlasSize},elements:bbElements,outliner:groups,display,textures:[{path:'',name:'tide.png',folder:'item/boss_materials',namespace:'ftboss',id:'atlas',particle:false,render_mode:'default',render_sides:'auto',frame_time:1,frame_order_type:'loop',frame_order:'',interpolate:false,visible:true,internal:true,saved:false,uuid:stableUuid('atlas'),source:'data:image/png;base64,'+atlasPng.toString('base64'),width:atlasSize,height:atlasSize,uv_width:atlasSize,uv_height:atlasSize}]};
write(path.join(root,'art/boss_materials/blockbench/tide_material.bbmodel'),json(bbModel));
write(path.join(task,'uv-islands.json'),json(faces));
write(path.join(task,'design-metrics.json'),json({item:'ftboss:tide_material',elements:elements.length,rotated:elements.filter(element=>element.rotation).length,faces:faces.length,atlas:[atlasSize,atlasSize],icon:[32,32],materials:Object.keys(palettes),usedAtlasRows:shelves.at(-1).top+shelves.at(-1).height}));
console.log(json({elements:elements.length,rotated:elements.filter(element=>element.rotation).length,faces:faces.length,atlas:atlasSize,usedAtlasRows:shelves.at(-1).top+shelves.at(-1).height}));
