'use strict';

const fs = require('node:fs');
const path = require('node:path');
const { encodePNG, decodePNG } = require('./png.cjs');
const space = require('./cube_space.cjs');
const { WINGS } = require('./cleaver_reference_motion.cjs');

const OUT = path.resolve(__dirname,'../../art/black_cleaver_reference');
const model = space.loadGeo(path.join(OUT,'black_cleaver.geo.json'),{ geckolib: true });
const texture = decodePNG(path.join(OUT,'black_cleaver_geo.png'));
const effectDefinition = JSON.parse(fs.readFileSync(path.join(OUT,'black_cleaver.effects.json'),'utf8'));
const effectTextures = Object.fromEntries(Object.entries(effectDefinition.textures).map(([channel,filename]) =>
  [channel,decodePNG(path.join(OUT,filename))]));
const cubes = space.listCubes(model);
const FACE_CORNERS = {
  south: [7,3,1,5], north: [2,6,4,0], east: [3,2,0,1], west: [6,7,5,4],
  up: [3,7,6,2], down: [0,4,5,1]
};
const normalize = vector => {
  const magnitude = Math.hypot(...vector);
  return vector.map(value => value / magnitude);
};
const dot = (left,right) => left.reduce((sum,value,axis) => sum+value*right[axis],0);
const mean = points => [0,1,2].map(axis => points.reduce((sum,point) => sum+point[axis],0)/points.length);

function render(options) {
  const width = options.width || 850;
  const height = options.height || 1000;
  const yaw = (options.yaw || 0)*Math.PI/180;
  const pitch = (options.pitch || 0)*Math.PI/180;
  const roll = (options.roll || 0)*Math.PI/180;
  const progress = options.progress || 0;
  const effects = options.effects || { core: 0.08+progress*0.4, rune: progress*0.42, edge: progress*0.28, seam: Math.min(1,progress*3)*0.55 };
  const camera = [Math.sin(yaw)*Math.cos(pitch),Math.sin(pitch),Math.cos(yaw)*Math.cos(pitch)];
  const light = normalize([-0.42,0.72,0.86]);
  const rim = normalize([0.72,0.2,-0.6]);
  const pose = options.pose || Object.fromEntries(Object.entries(WINGS).map(([name,sign]) => [name,{ rotation: [0,0,sign*11*progress] }]));
  const transform = point => {
    const horizontal = point[0]*Math.cos(yaw)-point[2]*Math.sin(yaw);
    const forward = point[0]*Math.sin(yaw)+point[2]*Math.cos(yaw);
    const vertical = point[1]*Math.cos(pitch)-forward*Math.sin(pitch);
    const depth = point[1]*Math.sin(pitch)+forward*Math.cos(pitch);
    return [horizontal*Math.cos(roll)-vertical*Math.sin(roll),horizontal*Math.sin(roll)+vertical*Math.cos(roll),depth];
  };
  const faces = [];
  const points = [];
  const scene = options.scene || cubes.map(info => ({ corners: space.cubeCorners(model,info,pose), uv: info.cube.uv }));
  for (const info of scene) {
    const corners = info.corners;
    const center = mean(corners);
    for (const [direction,indices] of Object.entries(FACE_CORNERS)) {
      const face = info.uv?.[direction] || (info.color ? { uv: [0,0],uv_size: [1,1] } : null);
      if (!face) continue;
      const vertices = indices.map(index => corners[index]);
      const faceCenter = mean(vertices);
      const normal = normalize(faceCenter.map((value,axis) => value-center[axis]));
      if (dot(normal,camera) < 1e-8) continue;
      const projected = vertices.map(transform);
      points.push(...projected);
      const lightAmount = 0.58+0.42*Math.max(0,dot(normal,light))+0.10*Math.max(0,dot(normal,rim));
      const startU = face.uv[0];
      const startV = face.uv[1];
      const endU = startU+face.uv_size[0];
      const endV = startV+face.uv_size[1];
      faces.push({ projected, uv: [[startU,startV],[endU,startV],[endU,endV],[startU,endV]], lightAmount, color: info.color });
    }
  }
  const bounds = [0,1].map(axis => [Math.min(...points.map(point => point[axis])),Math.max(...points.map(point => point[axis]))]);
  const scale = options.scale || Math.min(width*0.8/(bounds[0][1]-bounds[0][0]),height*0.85/(bounds[1][1]-bounds[1][0]));
  const centerX = options.centerX ?? (bounds[0][0]+bounds[0][1])/2;
  const centerY = options.centerY ?? (bounds[1][0]+bounds[1][1])/2;
  if (options.measureOnly) return { width,height,scale,centerX,centerY,bounds };
  const buffer = Buffer.alloc(width*height*4);
  const zbuffer = new Float64Array(width*height).fill(-Infinity);
  for (let pixelY = 0; pixelY < height; pixelY++) {
    for (let pixelX = 0; pixelX < width; pixelX++) {
      const offset = (pixelY*width+pixelX)*4;
      const vignette = Math.min(1,Math.hypot((pixelX-width/2)/width,(pixelY-height/2)/height));
      const base = options.lightBackground ? [224,225,228] : [27,32,43];
      for (let channel = 0; channel < 3; channel++) buffer[offset+channel] = base[channel]-Math.round(vignette*(options.lightBackground ? 8 : 9));
      buffer[offset+3] = 255;
    }
  }
  const edgeFunction = (first,second,horizontal,vertical) =>
    (horizontal-first[0])*(second[1]-first[1])-(vertical-first[1])*(second[0]-first[0]);
  function triangle(vertices,uv,lightAmount,color) {
    const area = edgeFunction(vertices[0],vertices[1],vertices[2][0],vertices[2][1]);
    if (Math.abs(area) < 1e-9) return;
    const minX = Math.max(0,Math.floor(Math.min(...vertices.map(point => point[0]))));
    const maxX = Math.min(width-1,Math.ceil(Math.max(...vertices.map(point => point[0]))));
    const minY = Math.max(0,Math.floor(Math.min(...vertices.map(point => point[1]))));
    const maxY = Math.min(height-1,Math.ceil(Math.max(...vertices.map(point => point[1]))));
    for (let pixelY = minY; pixelY <= maxY; pixelY++) {
      for (let pixelX = minX; pixelX <= maxX; pixelX++) {
        const firstWeight = edgeFunction(vertices[1],vertices[2],pixelX+0.5,pixelY+0.5)/area;
        const secondWeight = edgeFunction(vertices[2],vertices[0],pixelX+0.5,pixelY+0.5)/area;
        const thirdWeight = 1-firstWeight-secondWeight;
        if (Math.min(firstWeight,secondWeight,thirdWeight) < -1e-8) continue;
        const weights = [firstWeight,secondWeight,thirdWeight];
        const depth = dot(weights,vertices.map(point => point[2]));
        const pixel = pixelY*width+pixelX;
        if (depth < zbuffer[pixel]-1e-7) continue;
        const texX = Math.max(0,Math.min(texture.w-1,Math.floor(dot(weights,uv.map(point => point[0])))));
        const texY = Math.max(0,Math.min(texture.h-1,Math.floor(dot(weights,uv.map(point => point[1])))));
        const texel = (texY*texture.w+texX)*4;
        if (!color && texture.data[texel+3] < 128) continue;
        const offset = pixel*4;
        for (let channel = 0; channel < 3; channel++) {
          let value = color ? color[channel]*lightAmount : options.clay ? [159,168,183][channel]*lightAmount : texture.data[texel+channel]*lightAmount;
          if (!color && !options.clay) {
            for (const [effect,amount] of Object.entries(effects)) {
              const emission = effectTextures[effect];
              if (emission && amount > 0) value += emission.data[texel+channel]*(emission.data[texel+3]/255)*amount;
            }
          }
          buffer[offset+channel] = Math.max(0,Math.min(255,Math.round(value)));
        }
        zbuffer[pixel] = depth;
      }
    }
  }
  for (const face of faces) {
    const projected = face.projected.map(point => [width/2+(point[0]-centerX)*scale,height/2-(point[1]-centerY)*scale,point[2]]);
    for (const triangleIndices of [[0,1,2],[0,2,3]]) triangle(triangleIndices.map(index => projected[index]),triangleIndices.map(index => face.uv[index]),face.lightAmount,face.color);
  }
  if (options.name) {
    const target = path.join(OUT,options.name+'.png');
    fs.writeFileSync(target,encodePNG(width,height,buffer));
    if (!options.quiet) console.log(target);
  }
  return { width,height,scale,centerX,centerY,bounds,buffer,
    visiblePixels: zbuffer.reduce((sum,value) => sum+(Number.isFinite(value) ? 1 : 0),0) };
}

if (require.main === module) {
  const front = render({ name: 'front', width: 820, height: 1000 });
  render({ name: 'back', yaw: 180, width: 820, height: 1000 });
  render({ name: 'side', yaw: 90, width: 450, height: 1000, scale: front.scale, centerY: front.centerY });
  render({ name: 'hero', yaw: -28, pitch: 10, roll: -32, width: 1100, height: 1100 });
  render({ name: 'reference-angle', roll: -45, width: 1000, height: 1000 });
  render({ name: 'clay', yaw: -30, pitch: 12, width: 820, height: 1000, clay: true });
  render({ name: 'open', width: 820, height: 1000, progress: 1, scale: front.scale, centerX: front.centerX, centerY: front.centerY });
  render({ name: 'half-open', width: 820, height: 1000, progress: 0.5, scale: front.scale, centerX: front.centerX, centerY: front.centerY });
  render({ name: 'open-iso', width: 820, height: 1000, yaw: -30, pitch: 12, progress: 1 });
}

module.exports = { render,FACE_CORNERS };
