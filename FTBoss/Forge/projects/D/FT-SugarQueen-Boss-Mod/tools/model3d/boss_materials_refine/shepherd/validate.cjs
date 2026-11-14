const fs = require('fs');
const path = require('path');
const crypto = require('crypto');
const assert = require('assert/strict');
const {decodePNG} = require('../../png.cjs');
const renderer = require('./render.cjs');
const root = path.resolve(__dirname,'../../../..');
const subtract=(first,second)=>first.map((value,index)=>value-second[index]);
const dot=(first,second)=>first.reduce((sum,value,index)=>sum+value*second[index],0);
const cross=(first,second)=>[first[1]*second[2]-first[2]*second[1],first[2]*second[0]-first[0]*second[2],first[0]*second[1]-first[1]*second[0]];
function box(element) {
  const points=[];
  for(const cornerX of [element.from[0],element.to[0]]) for(const cornerY of [element.from[1],element.to[1]]) for(const cornerZ of [element.from[2],element.to[2]]) points.push(renderer.transform([cornerX,cornerY,cornerZ],element));
  const axes=[[1,0,0],[0,1,0],[0,0,1]].map(axis=>{
    const origin=element.rotation?element.rotation.origin:[0,0,0];
    const rotated=renderer.transform(origin.map((value,index)=>value+axis[index]),element);
    const direction=subtract(rotated,origin),length=Math.hypot(...direction);
    return direction.map(value=>value/length);
  });
  return {points,axes};
}
function connected(first,second,tolerance=.055) {
  const axes=[...first.axes,...second.axes,...first.axes.flatMap(firstAxis=>second.axes.map(secondAxis=>cross(firstAxis,secondAxis)))];
  for(const rawAxis of axes) {
    const length=Math.hypot(...rawAxis);
    if(length<.00001) continue;
    const axis=rawAxis.map(value=>value/length);
    const firstValues=first.points.map(point=>dot(point,axis)),secondValues=second.points.map(point=>dot(point,axis));
    if(Math.min(...firstValues)>Math.max(...secondValues)+tolerance||Math.min(...secondValues)>Math.max(...firstValues)+tolerance) return false;
  }
  return true;
}
function rendererTests() {
  for(const axis of ['x','y','z']) {
    const fixture={rotation:{axis,angle:45,origin:[3,4,5],rescale:false}};
    const point=[4,6,8],rotated=renderer.transform(point,fixture);
    assert(Math.abs(Math.hypot(...subtract(point,[3,4,5]))-Math.hypot(...subtract(rotated,[3,4,5])))<1e-9);
    const inverse={rotation:{axis,angle:-45,origin:[3,4,5],rescale:false}};
    renderer.transform(rotated,inverse).forEach((value,index)=>assert(Math.abs(value-point[index])<1e-9));
  }
  const scaled=renderer.transform([1,0,0],{rotation:{axis:'z',angle:45,origin:[0,0,0],rescale:true}});
  assert(Math.abs(scaled[0]-1)<1e-9&&Math.abs(scaled[1]-1)<1e-9);
  const north={from:[0,0,0],to:[16,16,16],faces:{north:{uv:[0,0,16,16],texture:'#atlas'}}};
  const fixture={elements:[north]};
  const texture={w:2,h:2,data:Buffer.from([255,0,0,255,0,255,0,255,0,0,255,255,255,255,0,255])};
  const image=renderer.render(fixture,texture,{width:16,height:16,scale:1,center:[8,8,8],camera:[0,0,-1],unlit:true});
  assert.deepEqual(Array.from(image.data.subarray((2*16+2)*4,(2*16+2)*4+4)),[255,0,0,255]);
  assert.deepEqual(Array.from(image.data.subarray((2*16+13)*4,(2*16+13)*4+4)),[0,255,0,255]);
  assert.deepEqual(Array.from(image.data.subarray((13*16+2)*4,(13*16+2)*4+4)),[0,0,255,255]);
  north.faces.north.rotation=90;
  const rotatedImage=renderer.render(fixture,texture,{width:16,height:16,scale:1,center:[8,8,8],camera:[0,0,-1],unlit:true});
  assert.notDeepEqual(image.data,rotatedImage.data);
  texture.data[3]=0;
  const transparent=renderer.render(fixture,texture,{width:16,height:16,scale:1,center:[8,8,8],camera:[0,0,-1],unlit:true});
  assert(transparent.data.some((value,index)=>index%4===3&&value===0));
  return 'PASS: axis x/y/z + inverse + rescale + Java north corner UV + UV rotation + alpha';
}
function validate(options={}) {
  const asset=options.asset||'shepherd_material',folder=options.folder||'boss_materials';
  const resource=path.join(root,'src/main/resources/assets/ftboss');
  const modelPath=path.join(resource,'models/item',folder,asset+'.json');
  const texturePath=path.join(resource,'textures/item',folder,(options.texture||'shepherd')+'.png');
  const iconPath=path.join(resource,'textures/item',asset+'.png');
  const bbPath=path.join(root,'art',folder,'blockbench',asset+'.bbmodel');
  const model=JSON.parse(fs.readFileSync(modelPath,'utf8')),texture=decodePNG(texturePath),icon=decodePNG(iconPath),bbmodel=JSON.parse(fs.readFileSync(bbPath,'utf8'));
  const errors=[],warnings=[];
  const demand=(condition,message)=>{if(!condition) errors.push(message);};
  demand(!model.parent,'geometry must not inherit generated/handheld');
  const wrapper=JSON.parse(fs.readFileSync(path.join(resource,'models/item',asset+'.json'),'utf8'));
  demand(wrapper.loader==='forge:separate_transforms','separate transforms missing');
  demand(wrapper.base.parent==='ftboss:item/'+folder+'/'+asset,'base parent mismatch');
  demand(wrapper.perspectives.gui.parent==='ftboss:item/'+folder+'/'+asset+'_gui','gui parent mismatch');
  const gui=JSON.parse(fs.readFileSync(path.join(resource,'models/item',folder,asset+'_gui.json'),'utf8'));
  demand(gui.parent==='minecraft:item/generated'&&gui.textures.layer0==='ftboss:item/'+asset,'gui icon chain mismatch');
  for(const value of Object.values(model.textures)) {
    if(value.startsWith('#')) continue;
    const [namespace,relative]=value.split(':');
    demand(fs.existsSync(path.join(root,'src/main/resources/assets',namespace,'textures',relative+'.png')),'texture missing '+value);
  }
  let faces=0,transparentSamples=0,maximumDensityError=0;
  const bounds=[Infinity,Infinity,Infinity,-Infinity,-Infinity,-Infinity];
  for(const [index,element] of model.elements.entries()) {
    const size=element.to.map((value,axis)=>value-element.from[axis]);
    demand(size.every(value=>value>0),'zero volume '+element.name);
    if(element.rotation) demand(['x','y','z'].includes(element.rotation.axis)&&[-45,-22.5,0,22.5,45].includes(element.rotation.angle),'illegal rotation '+element.name);
    for(const point of box(element).points) point.forEach((value,axis)=>{bounds[axis]=Math.min(bounds[axis],value);bounds[axis+3]=Math.max(bounds[axis+3],value);});
    const bb=bbmodel.elements[index];
    demand(bb.name===element.name&&JSON.stringify(bb.from)===JSON.stringify(element.from)&&JSON.stringify(bb.to)===JSON.stringify(element.to),'bb geometry mismatch '+index);
    demand(JSON.stringify(bb.rotation)===JSON.stringify(['x','y','z'].map(axis=>element.rotation&&element.rotation.axis===axis?element.rotation.angle:0)),'bb rotation mismatch '+index);
    for(const [face,data] of Object.entries(element.faces)) {
      faces++;
      demand(data.uv.every(value=>Number.isFinite(value)&&value>=0&&value<=16),'uv bounds '+element.name);
      demand(data.texture==='#atlas','texture slot mismatch '+element.name);
      const desired=['north','south'].includes(face)?[size[0],size[1]]:['east','west'].includes(face)?[size[2],size[1]]:[size[0],size[2]];
      const pixels=[Math.abs(data.uv[2]-data.uv[0])*texture.w/16,Math.abs(data.uv[3]-data.uv[1])*texture.h/16];
      const density=pixels.map((value,axis)=>value/desired[axis]);
      maximumDensityError=Math.max(maximumDensityError,...density.map(value=>Math.abs(value-2)));
      demand(density.every(value=>Math.abs(value-2)<.009),'face density/aspect mismatch '+element.name+':'+face);
      const minimumX=Math.min(data.uv[0],data.uv[2])*texture.w/16,maximumX=Math.max(data.uv[0],data.uv[2])*texture.w/16;
      const minimumY=Math.min(data.uv[1],data.uv[3])*texture.h/16,maximumY=Math.max(data.uv[1],data.uv[3])*texture.h/16;
      for(let sampleY=0;sampleY<5;sampleY++) for(let sampleX=0;sampleX<5;sampleX++) {
        const pixelX=Math.floor(minimumX+(maximumX-minimumX)*(sampleX+.5)/5),pixelY=Math.floor(minimumY+(maximumY-minimumY)*(sampleY+.5)/5);
        if(texture.data[(pixelY*texture.w+pixelX)*4+3]!==255) transparentSamples++;
      }
      demand(data.uv.every((value,axis)=>Math.abs(bb.faces[face].uv[axis]-value*texture.w/16)<.0002),'bb uv mismatch '+element.name);
    }
  }
  demand(transparentSamples===0,'transparent UV samples '+transparentSamples);
  demand(Buffer.from(bbmodel.textures[0].source.split(',')[1],'base64').equals(fs.readFileSync(texturePath)),'bb embedded atlas mismatch');
  const boxes=model.elements.map(box),visited=new Set([0]);
  let changed=true;
  while(changed) {changed=false;for(let index=0;index<boxes.length;index++) if(!visited.has(index)&&[...visited].some(other=>connected(boxes[index],boxes[other]))) {visited.add(index);changed=true;}}
  const detached=model.elements.filter((element,index)=>!visited.has(index)).map(element=>element.name);
  demand(detached.length===0,'disconnected elements '+detached.join(', '));
  for(let pixelY=0;pixelY<icon.h;pixelY++) for(let pixelX=0;pixelX<icon.w;pixelX++) if(pixelX===0||pixelY===0||pixelX===icon.w-1||pixelY===icon.h-1) demand(icon.data[(pixelY*icon.w+pixelX)*4+3]===0,'icon clipping');
  const hashes={};
  for(const file of [modelPath,texturePath,iconPath,bbPath,path.join(resource,'models/item',asset+'.json'),path.join(resource,'models/item',folder,asset+'_gui.json')]) hashes[file]=crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');
  const report={status:errors.length?'FAIL':'PASS',asset,elements:model.elements.length,rotated:model.elements.filter(element=>element.rotation).length,faces,texture:[texture.w,texture.h],icon:[icon.w,icon.h],maximumDensityError,transparentSamples,connectedElements:visited.size,bounds:bounds.map(value=>+value.toFixed(4)),rendererTests:rendererTests(),errors,warnings,hashes};
  const destination=options.report||path.join(root,'_work/boss-materials-refine-20261021/shepherd/validation.json');
  fs.writeFileSync(destination,JSON.stringify(report,null,2)+'\n');
  console.log(JSON.stringify(report,null,2));
  if(errors.length) process.exitCode=1;
  return report;
}
module.exports={validate,rendererTests};
if(require.main===module) validate();
