'use strict';

const fs = require('fs');
const path = require('path');
const assert = require('assert/strict');
const { encodePNG } = require('./png.cjs');

const baseline = path.resolve(process.argv[2] || '');
const output = path.resolve(process.argv[3] || '');
assert(process.argv.length === 4, 'Usage: node rebuild_shadow_sword_v22.cjs <baseline-directory> <output-directory>');
assert.notEqual(baseline, output);
const original = JSON.parse(fs.readFileSync(path.join(baseline, 'shadow_sword.geo.json'), 'utf8'));
const model = structuredClone(original);
const geometry = model['minecraft:geometry'][0];
const bones = new Map(geometry.bones.map(bone => [bone.name, bone]));
for (const bone of bones.values()) bone.cubes = [];
const parts = [];
const round = value => Number(value.toFixed(5));

function add(boneName, origin, size, material, rotation) {
    const cube = { origin: origin.map(round), size: size.map(round), uv: {} };
    if (rotation) {
        cube.rotation = rotation;
        cube.pivot = origin.map((value, axis) => round(value + size[axis] / 2));
    }
    assert(bones.has(boneName), boneName);
    bones.get(boneName).cubes.push(cube);
    parts.push({ boneName, cube, material });
    return cube;
}

add('pommel', [-0.85,-4.55,-0.95], [1.7,0.35,1.9], 'metal');
add('pommel', [-1.12,-4.25,-1.12], [2.24,0.5,2.24], 'metal');
add('pommel', [-0.82,-3.8,-0.88], [1.64,1.5,1.76], 'metal');
add('crystal_link', [-0.18,-3.7,-0.18], [0.36,1.8,0.36], 'metal');
add('crystal', [-0.75,-3.52,-1.4], [1.5,0.7,2.8], 'crystal');
add('handle', [-0.67,-2.25,-0.76], [1.34,5.2,1.52], 'grip');
add('handle', [-0.87,-2.4,-0.95], [1.74,0.45,1.9], 'metal');
add('handle', [-0.91,2.73,-1.02], [1.82,0.6,2.04], 'metal');
add('handle', [-0.88,-1.2,-0.9], [1.76,0.42,1.8], 'wrap', [0,0,22.5]);
add('handle', [-0.88,0.95,-0.9], [1.76,0.42,1.8], 'wrap', [0,0,-22.5]);
add('guard', [-1.7,3.25,-1.2], [3.4,1.95,2.4], 'metal');
add('eye_mount', [-1.92,3.48,-1.4], [0.43,1.4,2.8], 'rim');
add('eye_mount', [1.49,3.48,-1.4], [0.43,1.4,2.8], 'rim');
add('eye_mount', [-1.49,4.68,-1.4], [2.98,0.4,2.8], 'rim');
add('eye_mount', [-1.49,3.12,-1.4], [2.98,0.4,2.8], 'rim');
add('eye', [-1.28,3.47,-1.49], [2.56,0.33,2.98], 'iris');
add('eye', [-1.57,3.77,-1.59], [3.14,0.68,3.18], 'iris');
add('eye', [-1.28,4.42,-1.49], [2.56,0.33,2.98], 'iris');
add('pupil', [-0.19,3.55,-1.7], [0.38,1.13,0.28], 'pupil');
add('pupil', [-0.19,3.55,1.42], [0.38,1.13,0.28], 'pupil');
add('claw_left', [-3.1,3.55,-0.75], [1.4,1.38,1.5], 'claw');
add('claw_right', [1.7,3.55,-0.75], [1.4,1.38,1.5], 'claw');
add('claw_left_tip', [-3.3,4.62,-0.6], [0.87,1.5,1.2], 'clawtip');
add('claw_right_tip', [2.43,4.62,-0.6], [0.87,1.5,1.2], 'clawtip');

const rows = [
    [5.03,7.4,-2.0,1.5,1.00],
    [7.4,10.15,-2.42,1.7,1.00],
    [10.15,12.9,-2.50,1.72,0.97],
    [12.9,15.35,-2.16,1.80,0.94],
    [15.35,17.4,-1.58,1.84,0.85],
    [17.4,18.8,-0.76,1.84,0.75],
    [18.8,19.75,0.1,1.75,0.65],
    [19.75,20.3,0.85,1.64,0.53]
];
const strips = [
    [0.10,0.28,'edge'],
    [0.18,0.62,'bevel'],
    [0.44,1.08,'plate'],
    [0.10,0.82,'groove'],
    [0.18,1.8,'spine']
];
for (const [bottom,top,left,right,taper] of rows) {
    let cursor = left;
    for (const [fraction,depth,material] of strips) {
        const width = (right-left)*fraction;
        add('blade', [cursor,bottom,-depth*taper/2], [width,top-bottom,depth*taper], material);
        cursor += width;
    }
}
add('blade', [0.8,19.62,-0.39], [1.0,1.0,0.78], 'tip', [0,0,45]);
add('blade', [1.45,9.25,-0.69], [0.9,1.2,1.38], 'claw', [0,0,22.5]);
add('blade', [1.55,13.62,-0.65], [0.82,1.03,1.3], 'claw', [0,0,22.5]);

for (const boneName of ['charge_orb','charge_core','blade_glow','eye_glow','crystal_glow']) {
    const source = original['minecraft:geometry'][0].bones.find(bone => bone.name === boneName);
    for (const sourceCube of source.cubes) {
        const cube = structuredClone(sourceCube);
        cube.uv = {};
        bones.get(boneName).cubes.push(cube);
        parts.push({ boneName, cube, material: boneName === 'charge_orb' ? 'orb' : 'magic' });
    }
}

const tiles = [];
const cache = new Map();
function tileFor(part, faceType, width, height) {
    const density = ['iris','pupil','crystal','orb'].includes(part.material) ? 5 : 2;
    const pixelWidth = Math.max(1,Math.round(width*density));
    const pixelHeight = Math.max(1,Math.round(height*density));
    const unique = faceType === 'broad' ? ':' + parts.indexOf(part) : '';
    const key = [part.material,faceType,pixelWidth,pixelHeight].join(':') + unique;
    if (cache.has(key)) return cache.get(key);
    const tile = { width:pixelWidth, height:pixelHeight, part, faceType, index:tiles.length };
    tiles.push(tile);
    cache.set(key,tile);
    return tile;
}
for (const part of parts) {
    const [width,height,depth] = part.cube.size;
    part.tiles = {
        broad:tileFor(part,'broad',width,height),
        side:tileFor(part,'side',depth,height),
        cap:tileFor(part,'cap',width,depth)
    };
}

const atlasWidth = 128;
const shelves = [];
for (const tile of [...tiles].sort((first,second) => second.height-first.height || second.width-first.width || first.index-second.index)) {
    let shelf = shelves.find(entry => entry.height >= tile.height+2 && entry.cursor+tile.width+2 <= atlasWidth);
    if (!shelf) {
        const top = shelves.reduce((sum,entry) => sum+entry.height,0);
        shelf = { top, height:tile.height+2, cursor:0 };
        shelves.push(shelf);
    }
    tile.x = shelf.cursor+1;
    tile.y = shelf.top+1;
    shelf.cursor += tile.width+2;
}
const usedHeight = shelves.reduce((sum,entry) => sum+entry.height,0);
const atlasHeight = 2 ** Math.ceil(Math.log2(usedHeight));
assert(atlasHeight <= 256, 'Atlas unexpectedly large.');
geometry.description.texture_width = atlasWidth;
geometry.description.texture_height = atlasHeight;
const normal = Buffer.alloc(atlasWidth*atlasHeight*4);
const ready = Buffer.alloc(normal.length);
const emissive = Buffer.alloc(normal.length);
const colors = {
    metal:[45,38,55], rim:[58,46,70], grip:[39,30,44], wrap:[76,56,70],
    claw:[49,41,60], clawtip:[80,63,93], edge:[128,113,144], bevel:[84,71,99],
    plate:[49,39,61], groove:[29,25,39], spine:[66,52,79], tip:[83,68,100],
    iris:[130,63,181], pupil:[20,15,30], crystal:[119,62,162], orb:[114,57,157], magic:[169,108,220]
};
const crackPath = [[-0.65,6.1],[-0.55,8.0],[0.0,9.5],[-0.22,11.1],[0.32,12.7],[0.08,14.0],[0.66,15.5],[0.71,17.3],[1.0,18.2]];
const branches = [[[-0.22,11.1],[-1.18,11.8],[-1.38,12.5]], [[0.08,14.0],[0.82,14.5],[1.1,15.4]]];
function lineDistance(position, points) {
    let nearest = Infinity;
    for (let index=1;index<points.length;index++) {
        const start=points[index-1], end=points[index];
        const delta=end.map((value,axis)=>value-start[axis]);
        const factor=Math.max(0,Math.min(1,((position[0]-start[0])*delta[0]+(position[1]-start[1])*delta[1])/(delta[0]**2+delta[1]**2)));
        nearest=Math.min(nearest,Math.hypot(position[0]-start[0]-factor*delta[0],position[1]-start[1]-factor*delta[1]));
    }
    return nearest;
}
function pixel(tile, column, row) {
    const {part,faceType,width,height}=tile;
    const horizontal=(column+0.5)/width, vertical=(row+0.5)/height;
    const material=part.material;
    let color=colors[material].slice();
    let glowing=['iris','crystal','orb','magic'].includes(material);
    let light=faceType==='side' ? -8 : faceType==='cap' ? 5 : 0;
    if (height>3 && row===0) light+=14;
    if (height>3 && row===height-1) light-=8;
    if (width>3 && column===0) light+=7;
    if (width>3 && column===width-1) light-=6;
    if (material==='grip') light+=Math.floor(vertical*4)%2 ? 4 : -2;
    if (material==='wrap') light+=Math.floor(horizontal*4+vertical*2)%2 ? 8 : -6;
    if (['plate','groove','spine','bevel'].includes(material) && faceType==='broad') {
        const position=[part.cube.origin[0]+horizontal*part.cube.size[0],part.cube.origin[1]+(1-vertical)*part.cube.size[1]];
        const distance=Math.min(lineDistance(position,crackPath),...branches.map(points=>lineDistance(position,points)));
        if (distance<0.21) {
            color=distance<0.10 && Math.floor(position[1]*2)%5===0 ? [169,108,220] : [86,51,116];
            glowing=true;
        }
    }
    if (material==='iris' && faceType==='broad') {
        const horizontalWorld=part.cube.origin[0]+horizontal*part.cube.size[0];
        const verticalWorld=part.cube.origin[1]+(1-vertical)*part.cube.size[1];
        const radius=Math.hypot(horizontalWorld/1.65,(verticalWorld-4.12)/0.75);
        color=radius>0.9 ? [76,38,104] : radius>0.65 ? [117,54,164] : radius>0.3 ? [169,108,220] : [204,155,235];
        light=0;
        if(horizontalWorld < -0.42 && horizontalWorld > -0.7 && verticalWorld>4.2) color=[234,213,245];
    }
    if (['crystal','orb'].includes(material)) {
        const distance=Math.abs(horizontal-0.43)+Math.abs(vertical-0.35);
        color=distance<0.2 ? [215,181,238] : distance<0.52 ? [169,108,220] : distance<0.8 ? [116,63,163] : [67,38,91];
        light=0;
    }
    if(material==='pupil') { color=[16,14,24]; light=0; }
    if(material==='magic') light=0;
    color=color.map(value=>Math.max(0,Math.min(255,Math.round((value+light)/3)*3)));
    return {color,glowing};
}
for (const tile of tiles) {
    for (let row=-1;row<=tile.height;row++) for (let column=-1;column<=tile.width;column++) {
        const sample=pixel(tile,Math.max(0,Math.min(tile.width-1,column)),Math.max(0,Math.min(tile.height-1,row)));
        const offset=((tile.y+row)*atlasWidth+tile.x+column)*4;
        for (const axis of [0,1,2]) {
            normal[offset+axis]=sample.color[axis];
            ready[offset+axis]=sample.glowing ? Math.max(sample.color[axis],Math.round(sample.color[axis]+([234,213,245][axis]-sample.color[axis])*0.25)) : sample.color[axis];
            emissive[offset+axis]=sample.glowing ? sample.color[axis] : 0;
        }
        normal[offset+3]=ready[offset+3]=255;
        emissive[offset+3]=sample.glowing ? 255 : 0;
    }
}
for(const part of parts) {
    const {broad,side,cap}=part.tiles;
    const uv=tile=>({uv:[tile.x,tile.y],uv_size:[tile.width,tile.height]});
    part.cube.uv={
        north:{uv:[broad.x+broad.width,broad.y],uv_size:[-broad.width,broad.height]},
        south:uv(broad),east:uv(side),west:uv(side),up:uv(cap),down:uv(cap)
    };
}
for (const [index,bone] of geometry.bones.entries()) {
    const prior=original['minecraft:geometry'][0].bones[index];
    for(const key of ['name','parent','pivot','rotation']) assert.deepEqual(bone[key],prior[key]);
}
const write=(relative,data)=>{
    const destination=path.join(output,relative);
    fs.mkdirSync(path.dirname(destination),{recursive:true});
    fs.writeFileSync(destination,data);
};
write('geo/shadow_sword.geo.json',JSON.stringify(model,null,2)+'\n');
write('textures/item/shadow_sword.png',encodePNG(atlasWidth,atlasHeight,normal));
write('textures/item/shadow_sword_ready.png',encodePNG(atlasWidth,atlasHeight,ready));
write('textures/item/shadow_sword_e.png',encodePNG(atlasWidth,atlasHeight,emissive));
const item=JSON.parse(fs.readFileSync(path.join(baseline,'shadow_sword.json'),'utf8'));
item.display.thirdperson_righthand.rotation=[0,90,20];
item.display.thirdperson_lefthand.rotation=[0,-90,-20];
item.credit='V22 噬影之刃·裂瞳：依据原七项设计重建单刃轮廓、钩爪竖瞳、斜缠带与紫晶柄首；逐面低密度UV。保留22骨骼接口和9段动画；第三人称刃口翻转180度，保持握点与倾斜。由rebuild_shadow_sword_v22.cjs生成。';
write('models/item/shadow_sword.json',JSON.stringify(item,null,2)+'\n');
write('rebuild-manifest.json',JSON.stringify({version:22,bones:bones.size,cubes:parts.length,texture:[atlasWidth,atlasHeight],tiles:tiles.length,materials:colors,baseline},null,2)+'\n');
console.log(JSON.stringify({bones:bones.size,cubes:parts.length,texture:[atlasWidth,atlasHeight],tiles:tiles.length,output}));
