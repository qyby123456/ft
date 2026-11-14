const fs = require('node:fs');
const path = require('node:path');
const crypto = require('node:crypto');
const assert = require('node:assert/strict');
const { decodePNG } = require('../../png.cjs');
const { transform, corners } = require('./render.cjs');
const root = path.resolve(__dirname,'../../../..');
const assets = path.join(root,'src/main/resources/assets/ftboss');
const task = path.join(root,'_work/boss-materials-refine-20261021/tide');
const read = relative => JSON.parse(fs.readFileSync(path.join(root,relative),'utf8'));
const modelRel = 'src/main/resources/assets/ftboss/models/item/boss_materials/tide_material.json';
const atlasRel = 'src/main/resources/assets/ftboss/textures/item/boss_materials/tide.png';
const iconRel = 'src/main/resources/assets/ftboss/textures/item/tide_material.png';
const bbRel = 'art/boss_materials/blockbench/tide_material.bbmodel';
const model = read(modelRel);
const wrapper = read('src/main/resources/assets/ftboss/models/item/tide_material.json');
const gui = read('src/main/resources/assets/ftboss/models/item/boss_materials/tide_material_gui.json');
const bb = read(bbRel);
const image = decodePNG(path.join(root,atlasRel));
const icon = decodePNG(path.join(root,iconRel));
assert.equal(wrapper.loader,'forge:separate_transforms');
assert.equal(wrapper.base.parent,'ftboss:item/boss_materials/tide_material');
assert.equal(wrapper.perspectives.gui.parent,'ftboss:item/boss_materials/tide_material_gui');
assert.equal(gui.textures.layer0,'ftboss:item/tide_material');
assert.equal(model.parent,undefined);
assert.equal(bb.elements.length,model.elements.length);
assert.equal(Buffer.compare(Buffer.from(bb.textures[0].source.split(',')[1],'base64'),fs.readFileSync(path.join(root,atlasRel))),0);
assert.deepEqual(bb.display,model.display);
assert.equal(icon.w,32);
assert.equal(icon.h,32);
let faces = 0;
let maskedFaces = 0;
const rectangles = [];
const densities = [];
const bounds = [[Infinity,Infinity,Infinity],[-Infinity,-Infinity,-Infinity]];
for(let elementIndex=0;elementIndex<model.elements.length;elementIndex++) {
  const element=model.elements[elementIndex];
  const editable=bb.elements[elementIndex];
  assert.deepEqual(editable.from,element.from);
  assert.deepEqual(editable.to,element.to);
  if(element.rotation) {
    assert.ok([-45,-22.5,0,22.5,45].includes(element.rotation.angle));
    assert.equal(editable.rotation['xyz'.indexOf(element.rotation.axis)],element.rotation.angle);
    assert.deepEqual(editable.origin,element.rotation.origin);
  }
  const [width,height,depth]=element.to.map((value,axis)=>value-element.from[axis]);
  for(const [faceName,face] of Object.entries(element.faces)) {
    faces++;
    assert.equal(face.texture,'#atlas');
    const dimensions={north:[width,height],south:[width,height],west:[depth,height],east:[depth,height],up:[width,depth],down:[width,depth]}[faceName];
    const pixelUV=face.uv.map((value,index)=>value*(index%2?image.h:image.w)/16);
    const densityU=(pixelUV[2]-pixelUV[0])/dimensions[0];
    const densityV=(pixelUV[3]-pixelUV[1])/dimensions[1];
    assert.ok(Math.abs(densityU-densityV)/densityU<0.003,element.name+' aspect');
    densities.push(densityU);
    assert.ok(face.uv.every(value=>value>=0 && value<=16));
    const editableUV=editable.faces[faceName].uv;
    assert.ok(editableUV.every((value,index)=>Math.abs(value-pixelUV[index])<0.0002));
    rectangles.push({name:element.name+'.'+faceName,uv:pixelUV});
    let opaque=0;
    const samples=40;
    for(let sampleY=0;sampleY<samples;sampleY++) for(let sampleX=0;sampleX<samples;sampleX++) {
      const pixelX=Math.floor(pixelUV[0]+(sampleX+0.5)/samples*(pixelUV[2]-pixelUV[0]));
      const pixelY=Math.floor(pixelUV[1]+(sampleY+0.5)/samples*(pixelUV[3]-pixelUV[1]));
      if(image.data[(pixelY*image.w+pixelX)*4+3]===255) opaque++;
    }
    const coverage=opaque/(samples*samples);
    if(element.name.includes('_tip_')) {maskedFaces++;assert.ok(coverage>0.45&&coverage<0.75,element.name+' designed triangular mask');}
    else assert.equal(coverage,1,element.name+' opaque UV');
    for(const point of corners(element,faceName)) for(let axis=0;axis<3;axis++) {
      bounds[0][axis]=Math.min(bounds[0][axis],point[axis]);
      bounds[1][axis]=Math.max(bounds[1][axis],point[axis]);
    }
  }
}
for(let first=0;first<rectangles.length;first++) for(let second=first+1;second<rectangles.length;second++) {
  const firstUV=rectangles[first].uv;
  const secondUV=rectangles[second].uv;
  const overlapX=Math.min(firstUV[2],secondUV[2])-Math.max(firstUV[0],secondUV[0]);
  const overlapY=Math.min(firstUV[3],secondUV[3])-Math.max(firstUV[1],secondUV[1]);
  assert.ok(overlapX<=0.001||overlapY<=0.001,'UV overlap '+rectangles[first].name+' / '+rectangles[second].name);
}
const subtract=(first,second)=>first.map((value,axis)=>value-second[axis]);
const dot=(first,second)=>first.reduce((sum,value,axis)=>sum+value*second[axis],0);
const cross=(first,second)=>[first[1]*second[2]-first[2]*second[1],first[2]*second[0]-first[0]*second[2],first[0]*second[1]-first[1]*second[0]];
const boxes=model.elements.map(element=>{
  const center=element.from.map((value,axis)=>(value+element.to[axis])/2);
  const transformed=transform(center,element);
  const axes=[[1,0,0],[0,1,0],[0,0,1]].map(vector=>subtract(transform(center.map((value,axis)=>value+vector[axis]),element),transformed));
  return {center:transformed,axes,half:element.from.map((value,axis)=>(element.to[axis]-value)/2)};
});
const touches=(first,second)=>{
  const offset=subtract(second.center,first.center);
  const axes=[...first.axes,...second.axes,...first.axes.flatMap(firstAxis=>second.axes.map(secondAxis=>cross(firstAxis,secondAxis)))];
  return axes.every(axis=>Math.hypot(...axis)<1e-8 || Math.abs(dot(offset,axis))<=first.axes.reduce((sum,basis,index)=>sum+Math.abs(dot(basis,axis))*first.half[index],0)+second.axes.reduce((sum,basis,index)=>sum+Math.abs(dot(basis,axis))*second.half[index],0)+0.0005);
};
const reached=new Set([0]);
let changed=true;
while(changed) {
  changed=false;
  for(let index=0;index<boxes.length;index++) if(!reached.has(index)&&[...reached].some(neighbor=>touches(boxes[index],boxes[neighbor]))) {reached.add(index);changed=true;}
}
const disconnected=model.elements.filter((element,index)=>!reached.has(index)).map(element=>element.name);
assert.equal(disconnected.length,0,'disconnected geometry '+disconnected.join(', '));
for(let position=0;position<32;position++) for(const index of [position,31*32+position,position*32,position*32+31]) assert.equal(icon.data[index*4+3],0,'icon clipping');
const facePlanes=[];
for(const element of model.elements) for(const faceName of Object.keys(element.faces)) {
  const points=corners(element,faceName);
  const horizontal=subtract(points[1],points[0]);
  const vertical=subtract(points[2],points[0]);
  const rawNormal=cross(horizontal,vertical);
  const magnitude=Math.hypot(...rawNormal);
  if(magnitude<1e-8) continue;
  const normal=rawNormal.map(value=>value/magnitude);
  facePlanes.push({name:element.name+'.'+faceName,element:element.name,points,normal,plane:dot(normal,points[0])});
}
const area2D=polygon=>Math.abs(polygon.reduce((sum,point,index)=>sum+point[0]*polygon[(index+1)%polygon.length][1]-point[1]*polygon[(index+1)%polygon.length][0],0))/2;
const cross2D=(first,second)=>first[0]*second[1]-first[1]*second[0];
const clipPolygon=(subject,boundary)=>{
  let output=subject;
  const orientation=boundary.reduce((sum,point,index)=>sum+cross2D(point,boundary[(index+1)%boundary.length]),0)>0?1:-1;
  for(let edgeIndex=0;edgeIndex<boundary.length;edgeIndex++) {
    const edgeStart=boundary[edgeIndex];
    const edgeEnd=boundary[(edgeIndex+1)%boundary.length];
    const direction=[edgeEnd[0]-edgeStart[0],edgeEnd[1]-edgeStart[1]];
    const signed=point=>orientation*cross2D(direction,[point[0]-edgeStart[0],point[1]-edgeStart[1]]);
    const input=output;
    output=[];
    for(let pointIndex=0;pointIndex<input.length;pointIndex++) {
      const current=input[pointIndex];
      const previous=input[(pointIndex+input.length-1)%input.length];
      const currentDistance=signed(current);
      const previousDistance=signed(previous);
      if((currentDistance>=0)!==(previousDistance>=0)) {
        const ratio=previousDistance/(previousDistance-currentDistance);
        output.push(previous.map((value,axis)=>value+(current[axis]-value)*ratio));
      }
      if(currentDistance>=0) output.push(current);
    }
    if(!output.length) break;
  }
  return output;
};
const coplanar=[];
for(let first=0;first<facePlanes.length;first++) for(let second=first+1;second<facePlanes.length;second++) {
  const firstFace=facePlanes[first];
  const secondFace=facePlanes[second];
  if(firstFace.element===secondFace.element || dot(firstFace.normal,secondFace.normal)<0.999999) continue;
  if(Math.abs(firstFace.plane-secondFace.plane)>0.00001) continue;
  const ignoredAxis=firstFace.normal.map(Math.abs).indexOf(Math.max(...firstFace.normal.map(Math.abs)));
  const project=point=>point.filter((value,axis)=>axis!==ignoredAxis);
  const intersection=clipPolygon(firstFace.points.map(project),secondFace.points.map(project));
  const area=area2D(intersection);
  if(area>0.001) coplanar.push({first:firstFace.name,second:secondFace.name,area});
}
assert.equal(coplanar.length,0,'coplanar overlapping faces '+JSON.stringify(coplanar));
const files=[modelRel,atlasRel,iconRel,bbRel,'src/main/resources/assets/ftboss/models/item/tide_material.json','src/main/resources/assets/ftboss/models/item/boss_materials/tide_material_gui.json'];
const hashes=files.map(file=>({path:file,sha256:crypto.createHash('sha256').update(fs.readFileSync(path.join(root,file))).digest('hex')}));
const result={status:'PASS',elements:model.elements.length,rotated:model.elements.filter(element=>element.rotation).length,faces,maskedFaces,uvOverlap:0,coplanarOverlap:coplanar.length,aspectErrors:0,opaqueFaceCoverage:1,bbmodelRuntimeMatch:true,embeddedTextureMatch:true,connectedElements:reached.size,bounds,densityRange:[Math.min(...densities),Math.max(...densities)],hashes};
fs.writeFileSync(path.join(task,'verification.json'),JSON.stringify(result,null,2)+'\n');
console.log(JSON.stringify(result,null,2));
