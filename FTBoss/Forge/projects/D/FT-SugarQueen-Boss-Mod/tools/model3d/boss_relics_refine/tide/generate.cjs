const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const { encodePNG } = require('../../png.cjs');
const { render } = require('../../boss_materials_refine/tide/render.cjs');
const root = path.resolve(__dirname, '../../../..');
const assets = path.join(root, 'src/main/resources/assets/ftboss');
const task = path.join(root, '_work/boss-relics-refine-20261021/tide');
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
box('backplate_vertical', [8,7.2,8.9], [4.7,6.7,1.45], 'shell');
box('backplate_horizontal', [8,7.2,8.82], [6.65,4.75,1.3], 'shell');
box('backplate_cross', [8,7.2,8.85], [4.85,4.85,1.5], 'shell', 45);
function octagon(name, radius, thickness, depth, centerY, centerZ, material) {
  const segmentLength = radius*0.828427+0.2;
  for(let index=0;index<8;index++) {
    const radians=index*Math.PI/4;
    const center=[8+Math.sin(radians)*radius,centerY+Math.cos(radians)*radius,centerZ+(index%2?0.14:-0.14)];
    const angle=[0,-45,0,45,0,-45,0,45][index];
    const size=index===2||index===6?[thickness,segmentLength,depth]:[segmentLength,thickness,depth];
    box(name+'_'+index,center,size,material,angle);
  }
}
octagon('sea_enamel_frame',3.6,0.95,1.85,7.2,8.2,'scale');
octagon('silver_outer_bead',3.98,0.28,1.15,7.2,8.08,'silver');
octagon('gold_inlaid_tide',3.55,0.19,0.35,7.2,7.16,'gold');
octagon('nacre_inner_rim',3.03,0.38,0.65,7.2,7.35,'nacre');
for(let index=0;index<13;index++) {
  const height=4.8+index*0.4;
  const radius=Math.sqrt(Math.max(0,2.65*2.65-(height-7.2)**2));
  box('pearl_body_'+index,[8,height,6.9],[radius*2,0.43,radius*1.73],'pearl');
}
for(const [index,horizontal,vertical,angle] of [[0,5.95,5.15,-45],[1,10.05,5.15,45],[2,5.95,9.25,45],[3,10.05,9.25,-45]]) {
  box('nacre_gripping_claw_'+index,[horizontal,vertical,6.65],[0.65,1.6,1.4],'silver',angle);
  box('gold_claw_tip_'+index,[horizontal,vertical+0.15,5.96],[0.44,0.62,0.38],'gold',angle);
}
for(let row=0;row<3;row++) for(let column=0;column<3;column++) {
  const horizontal=5.8+column*2.2+(row===1?0.15:0);
  const vertical=5.1+row*2.1;
  box('rear_scale_engraving_'+row+'_'+column,[horizontal,vertical,9.73+(column===1?0.22:0)],[2.05,1.65,0.35],'scale',column===0?-22.5:column===2?22.5:0);
}
box('rear_tide_seal',[8,7.1,10.25],[2.1,2.1,0.35],'nacre',45);
box('rear_seal_waterline',[8,7.15,10.5],[2.5,0.3,0.35],'silver');
box('rear_seal_current_l',[7.2,7.65,10.48],[0.3,1.1,0.3],'silver',-45);
box('rear_seal_current_r',[8.8,6.65,10.48],[0.3,1.1,0.3],'silver',-45);
box('bail_socket',[8,11.35,8.2],[1.15,1.45,1.6],'silver');
box('bail_socket_inlay',[8,11.5,7.23],[0.7,0.9,0.42],'gold');
octagon('wearable_bail',1.05,0.42,0.8,13.05,8.2,'gold');
box('bail_lower_bridge',[8,11.94,8.2],[0.62,0.7,0.9],'gold');
box('bail_crown_ferrule',[8,14.1,8.2],[0.75,0.85,1.05],'silver');
for(const [index,horizontal,angle] of [[0,6.8,-45],[1,7.4,-22.5],[2,8,0],[3,8.6,22.5],[4,9.2,45]]) {
  box('shell_fan_rib_'+index,[horizontal,3.5,6.9+index*0.22],[0.48,1.65,0.65],'nacre',angle);
}
box('drop_connector',[8,2.95,7.45],[0.75,0.9,0.9],'gold');
box('cut_tidal_drop',[8,1.95,7.4],[1.3,1.3,1.25],'crystal',45);
box('cut_drop_highlight',[7.88,2.05,6.75],[0.55,0.9,0.35],'crystal_edge',45);
const palettes = {
  shell: ['102c3b','174654','236475','3c8190','589da4'],
  scale: ['113b49','155566','1b7b87','389ba1','6ec2bc'],
  nacre: ['527b91','79a4b4','adced0','d5e7df','eef3d8'],
  crystal_dark: ['0c394f','10536d','147b99','299fb7','75d3d7'],
  crystal: ['135c79','218ba9','38bad0','7cdee2','c8f6ed'],
  crystal_edge: ['2a7d98','51b8cd','93e3e5','c1f1eb','e7fff0'],
  pearl: ['728fa6','a2b9c3','c5d8d5','e8ecdc','fff9e7'],
  rim: ['335c70','558b9a','8bb8bd','bed8cc','e1e5c9'],
  silver: ['3d697d','7299a5','aac6c5','d7e3d9','f4f3dc'],
  gold: ['77512e','a27c40','d2af69','f1d697','fff0be']
};
for (const key of Object.keys(palettes)) palettes[key] = palettes[key].map(hex => [0,2,4].map(offset => parseInt(hex.slice(offset,offset+2),16)));
const atlas = { w: atlasSize, h: atlasSize, data: new Uint8Array(atlasSize*atlasSize*4) };
const faces = [];
elements.forEach((element,elementIndex) => {
  const [width,height,depth] = element.to.map((value,axis) => round(value-element.from[axis]));
  const definitions = element.tideFace ? [['up',width,depth]] : [['north',width,height],['south',width,height],['west',depth,height],['east',depth,height],['up',width,depth],['down',width,depth]];
  for (const [face,faceWidth,faceHeight] of definitions) {
    if(faceWidth===0 || faceHeight===0) continue;
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
const model = {ambientocclusion:false,gui_light:'side',textures:{atlas:'ftboss:item/boss_relics/tide_pearl_pendant',particle:'ftboss:item/tide_pearl_pendant'},elements,display};
for (const element of elements) delete element.tideFace;
const json = value => JSON.stringify(value,null,2)+'\n';
const write = (file,data) => {fs.mkdirSync(path.dirname(file),{recursive:true});fs.writeFileSync(file,data);};
write(path.join(assets,'models/item/boss_relics/tide_pearl_pendant.json'),json(model));
write(path.join(assets,'textures/item/boss_relics/tide_pearl_pendant.png'),encodePNG(atlasSize,atlasSize,atlas.data));
const iconView = render(model,atlas,{size:32,yaw:-12,pitch:6,scale:1.9,center:[8,8,8],transparent:true});
const outlined = new Uint8Array(iconView.d);
for (let pixelY=1;pixelY<31;pixelY++) for (let pixelX=1;pixelX<31;pixelX++) {
  const index = (pixelY*32+pixelX)*4;
  if (iconView.d[index+3]) continue;
  if ([[0,1],[0,-1],[1,0],[-1,0]].some(([horizontal,vertical])=>iconView.d[((pixelY+vertical)*32+pixelX+horizontal)*4+3])) outlined.set([11,35,48,255],index);
}
write(path.join(assets,'textures/item/tide_pearl_pendant.png'),encodePNG(32,32,outlined));
const stableUuid = name => {
  const hash = crypto.createHash('sha256').update('ftboss:tide:'+name).digest('hex');
  return [hash.slice(0,8),hash.slice(8,12),'4'+hash.slice(13,16),'8'+hash.slice(17,20),hash.slice(20,32)].join('-');
};
const groups = ['01 Octagonal enamel setting','02 Thirteen tier pearl','03 Bail and tidal drop','04 Engraved scale reverse'].map(name=>({name,uuid:stableUuid(name),origin:[8,8,8],rotation:[0,0,0],export:true,isOpen:true,children:[]}));
const bbElements = elements.map(element=>{
  const uuid = stableUuid(element.name);
  const groupIndex = element.name.startsWith('pearl_')?1:element.name.includes('bail')||element.name.includes('drop_')?2:element.name.includes('rear_')?3:0;
  groups[groupIndex].children.push(uuid);
  const rotation=[0,0,0];
  if(element.rotation) rotation['xyz'.indexOf(element.rotation.axis)]=element.rotation.angle;
  return {name:element.name,type:'cube',uuid,from:element.from,to:element.to,origin:element.rotation?.origin||[8,8,8],rotation,rescale:false,autouv:0,color:groupIndex,faces:Object.fromEntries(Object.entries(element.faces).map(([face,value])=>[face,{uv:value.uv.map(coordinate=>round(coordinate*atlasSize/16)),texture:0}]))};
});
const atlasPng = encodePNG(atlasSize,atlasSize,atlas.data);
const bbModel = {meta:{format_version:'4.10',model_format:'java_block',box_uv:false},name:'潮汐珍珠坠 · 沧鳞海潮镶饰',model_identifier:'tide_pearl_pendant',visible_box:[1,1,0],resolution:{width:atlasSize,height:atlasSize},elements:bbElements,outliner:groups,display,textures:[{path:'',name:'tide_pearl_pendant.png',folder:'item/boss_relics',namespace:'ftboss',id:'atlas',particle:false,render_mode:'default',render_sides:'auto',frame_time:1,frame_order_type:'loop',frame_order:'',interpolate:false,visible:true,internal:true,saved:false,uuid:stableUuid('atlas'),source:'data:image/png;base64,'+atlasPng.toString('base64'),width:atlasSize,height:atlasSize,uv_width:atlasSize,uv_height:atlasSize}]};
write(path.join(root,'art/boss_relics/blockbench/tide_pearl_pendant.bbmodel'),json(bbModel));
write(path.join(task,'uv-islands.json'),json(faces));
write(path.join(task,'design-metrics.json'),json({item:'ftboss:tide_pearl_pendant',elements:elements.length,rotated:elements.filter(element=>element.rotation).length,faces:faces.length,atlas:[atlasSize,atlasSize],icon:[32,32],materials:Object.keys(palettes),usedAtlasRows:shelves.at(-1).top+shelves.at(-1).height}));
console.log(json({elements:elements.length,rotated:elements.filter(element=>element.rotation).length,faces:faces.length,atlas:atlasSize,usedAtlasRows:shelves.at(-1).top+shelves.at(-1).height}));

write(path.join(assets,'models/item/tide_pearl_pendant.json'),json({loader:'forge:separate_transforms',ambientocclusion:false,gui_light:'front',textures:{particle:'ftboss:item/tide_pearl_pendant'},base:{parent:'ftboss:item/boss_relics/tide_pearl_pendant'},perspectives:{gui:{parent:'ftboss:item/boss_relics/tide_pearl_pendant_gui'}}}));
write(path.join(assets,'models/item/boss_relics/tide_pearl_pendant_gui.json'),json({parent:'minecraft:item/generated',textures:{layer0:'ftboss:item/tide_pearl_pendant'}}));
