/* 大答辩小怪模型(2026-09-12): 7 层堆叠 + 番茄眼, 64x64 */
const fs=require('fs'),path=require('path');const {Canvas}=require('./png.cjs');
const OUT=process.argv[2]||'out_poop_minion';const W=128,H=128;   // 2026-09-12: 64 装不下 11 个方块 -> 128
const cv=new Canvas(W,H);for(let y=0;y<H;y++)for(let x=0;x<W;x++)cv.set(x,y,0,0,0,0);
const P={poop:[112,78,46],dark:[80,52,30],light:[148,108,66],eyeW:[246,246,240],tomato:[214,58,48]};
let px=1,py=1,rh=0;const C=[];
function alloc(w,h,d){const fw=Math.ceil(2*(w+d)),fh=Math.ceil(d+h);if(px+fw>W){px=1;py+=rh+2;rh=0;}const u=px,v=py;px+=fw+2;rh=Math.max(rh,fh);return[u,v];}
function cube(b,o,s,m,rot){const uv=alloc(s[0],s[1],s[2]);C.push({b,o,s,uv,m,rot});}
const L=[[10,3.0,9,6],[9.2,2.8,8.2,-8],[8.2,2.6,7.2,10],[7,2.4,6,-12],[5.6,2.2,4.8,14],[4,2.0,3.4,-16],[2.4,2.4,2.2,18]];
let hy=1.0;
L.forEach((l,i)=>{cube('body',[-l[0]/2,hy,-l[2]/2],[l[0],l[1],l[2]],i%3===0?'poop':(i%3===1?'dark':'light'),l[3]);hy+=l[1]*0.97;});
cube('body',[-2.6,3.6,-4.6],[2.0,2.0,0.6],'tomato');cube('body',[0.6,3.6,-4.6],[2.0,2.0,0.6],'tomato');
cube('body',[-2.1,4.2,-4.9],[0.8,0.8,0.3],'eyeW');cube('body',[1.1,4.2,-4.9],[0.8,0.8,0.3],'eyeW');
for(const c of C){const fw=Math.ceil(2*(c.s[0]+c.s[2])),fh=Math.ceil(c.s[2]+c.s[1]);const col=P[c.m];
 for(let x=0;x<fw;x++)for(let y=0;y<fh;y++)cv.set(c.uv[0]+x,c.uv[1]+y,col[0],col[1],col[2],1);}
const bones=[{name:'root',pivot:[0,0,0]},{name:'body',pivot:[0,1,0],parent:'root',cubes:C.map(c=>{const o={origin:c.o,size:c.s,uv:c.uv};if(c.rot)o.rotation=[0,c.rot,0];return o;})}];
const geo={format_version:'1.12.0','minecraft:geometry':[{description:{identifier:'geometry.poop_minion',texture_width:W,texture_height:H,visible_bounds_width:2,visible_bounds_height:2,visible_bounds_offset:[0,1,0]},bones}]};
const anim={format_version:'1.8.0',animations:{'animation.poop_minion.idle':{loop:true,animation_length:1.2,bones:{root:{position:{'0.0':[0,0,0],'0.6':[0,0.35,0],'1.2':[0,0,0]}},body:{rotation:{'0.0':[0,-6,0],'0.6':[0,6,0],'1.2':[0,-6,0]}}}}}};
fs.mkdirSync(OUT,{recursive:true});
fs.writeFileSync(path.join(OUT,'poop_minion.geo.json'),JSON.stringify(geo,null,1));
fs.writeFileSync(path.join(OUT,'poop_minion.animation.json'),JSON.stringify(anim,null,1));
cv.save(path.join(OUT,'poop_minion.png'));
console.log('大便小怪模型已生成: cube='+C.length);
