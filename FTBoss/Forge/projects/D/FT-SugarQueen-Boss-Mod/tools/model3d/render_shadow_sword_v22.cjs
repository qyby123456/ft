'use strict';

const fs=require('fs');
const path=require('path');
const assert=require('assert/strict');
const space=require('./cube_space.cjs');
const {decodePNG,encodePNG}=require('./png.cjs');

const [geoFile,textureFile,animationFile,outputFile,clip='idle',timeText='0']=process.argv.slice(2);
assert(geoFile && textureFile && animationFile && outputFile,'Usage: <geo> <png> <animation> <output> [clip] [seconds]');
const model=space.loadGeo(geoFile);
const texture=decodePNG(textureFile);
const animations=JSON.parse(fs.readFileSync(animationFile,'utf8')).animations;
const animation=animations['animation.ftboss.shadow_sword.'+clip];
assert(animation,'Unknown animation: '+clip);
const time=Number(timeText);
assert(Number.isFinite(time));

function sample(channel,seconds) {
    if(Array.isArray(channel)) return channel;
    const keys=Object.entries(channel).map(([stamp,value])=>[Number(stamp),value]).sort((first,second)=>first[0]-second[0]);
    assert(keys.length && keys.every(entry=>Number.isFinite(entry[0]) && Array.isArray(entry[1])),'Only numeric linear tracks are supported.');
    if(seconds<=keys[0][0]) return keys[0][1];
    for(let index=1;index<keys.length;index++) {
        if(seconds>keys[index][0]) continue;
        const previous=keys[index-1], next=keys[index];
        const fraction=(seconds-previous[0])/(next[0]-previous[0]);
        return previous[1].map((value,axis)=>value+(next[1][axis]-value)*fraction);
    }
    return keys[keys.length-1][1];
}
const pose={};
for(const [name,tracks] of Object.entries(animations['animation.ftboss.shadow_sword.idle'].bones)) {
    pose[name]=Object.fromEntries(Object.entries(tracks).map(([channel,value])=>[channel,sample(value,0)]));
}
for(const [name,tracks] of Object.entries(animation.bones)) {
    pose[name]={...pose[name],...Object.fromEntries(Object.entries(tracks).map(([channel,value])=>[channel,sample(value,time)]))};
}
const hidden=new Set(Object.entries(pose).filter(([,state])=>state.scale && state.scale.every(value=>Math.abs(value)<0.001)).map(([name])=>name));
const faces={north:[2,6,4,0],south:[7,3,1,5],west:[3,2,0,1],east:[6,7,5,4],up:[3,7,6,2],down:[0,4,5,1]};
const coordinates=[[0,0],[1,0],[1,1],[0,1]];
const panelWidth=330, height=680, width=panelWidth*4, scale=22;
const pixels=Buffer.alloc(width*height*4);
const depths=new Float64Array(width*height).fill(Infinity);
for(let offset=0;offset<pixels.length;offset+=4) {
    pixels[offset]=242; pixels[offset+1]=241; pixels[offset+2]=246; pixels[offset+3]=255;
}
const radians=value=>value*Math.PI/180;
const project=(point,yaw,panel)=>{
    const horizontal=point[0]*Math.cos(yaw)+point[2]*Math.sin(yaw);
    const distance=-point[0]*Math.sin(yaw)+point[2]*Math.cos(yaw);
    const pitch=radians(9);
    return [panel*panelWidth+panelWidth/2-horizontal*scale,height/2-(point[1]*Math.cos(pitch)-distance*Math.sin(pitch)-8)*scale,point[1]*Math.sin(pitch)+distance*Math.cos(pitch)];
};
function triangle(vertices,uvs,shade,panel) {
    const [first,second,third]=vertices;
    const determinant=(second[1]-third[1])*(first[0]-third[0])+(third[0]-second[0])*(first[1]-third[1]);
    if(Math.abs(determinant)<1e-8) return;
    const minColumn=Math.max(panel*panelWidth,Math.floor(Math.min(...vertices.map(vertex=>vertex[0]))));
    const maxColumn=Math.min((panel+1)*panelWidth-1,Math.ceil(Math.max(...vertices.map(vertex=>vertex[0]))));
    const minRow=Math.max(0,Math.floor(Math.min(...vertices.map(vertex=>vertex[1]))));
    const maxRow=Math.min(height-1,Math.ceil(Math.max(...vertices.map(vertex=>vertex[1]))));
    for(let row=minRow;row<=maxRow;row++) for(let column=minColumn;column<=maxColumn;column++) {
        const firstWeight=((second[1]-third[1])*(column+0.5-third[0])+(third[0]-second[0])*(row+0.5-third[1]))/determinant;
        const secondWeight=((third[1]-first[1])*(column+0.5-third[0])+(first[0]-third[0])*(row+0.5-third[1]))/determinant;
        const thirdWeight=1-firstWeight-secondWeight;
        if(Math.min(firstWeight,secondWeight,thirdWeight)<0) continue;
        const weights=[firstWeight,secondWeight,thirdWeight];
        const depth=weights.reduce((sum,weight,index)=>sum+weight*vertices[index][2],0);
        const destination=row*width+column;
        if(depth>=depths[destination]) continue;
        const texel=([0,1]).map(axis=>Math.floor(weights.reduce((sum,weight,index)=>sum+weight*uvs[index][axis],0)));
        const source=(Math.max(0,Math.min(texture.h-1,texel[1]))*texture.w+Math.max(0,Math.min(texture.w-1,texel[0])))*4;
        if(texture.data[source+3]<128) continue;
        depths[destination]=depth;
        for(const axis of [0,1,2]) pixels[destination*4+axis]=Math.round(texture.data[source+axis]*shade);
    }
}
for(const [panel,yawDegrees] of [0,38,90,180].entries()) {
    for(const info of space.listCubes(model)) {
        if(space.isHiddenBone(model,info.bone,hidden)) continue;
        const corners=space.cubeCorners(model,info,pose);
        for(const [face,indices] of Object.entries(faces)) {
            const uv=info.cube.uv[face];
            if(!uv) continue;
            const vertices=indices.map(index=>project(corners[index],radians(yawDegrees),panel));
            const uvs=coordinates.map(([horizontal,vertical])=>[uv.uv[0]+horizontal*uv.uv_size[0],uv.uv[1]+vertical*uv.uv_size[1]]);
            const shade=face==='up'?1:face==='down'?0.69:['west','east'].includes(face)?0.84:0.96;
            for(const order of [[0,1,2],[0,2,3]]) triangle(order.map(index=>vertices[index]),order.map(index=>uvs[index]),shade,panel);
        }
    }
}
fs.mkdirSync(path.dirname(path.resolve(outputFile)),{recursive:true});
fs.writeFileSync(outputFile,encodePNG(width,height,pixels));
console.log(`Preview ${clip}@${time}s -> ${outputFile}; linear tracks only, no controller blending.`);
