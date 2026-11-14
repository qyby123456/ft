/* 蛆小怪模型(2026-09-11): 7 段锥形身体, 链式骨骼做出"蠕动"动画; 64x64 贴图 */
const fs=require('fs'),path=require('path');
const {Canvas}=require('./png.cjs');
const OUT=process.argv[2]||'out_maggot';
const W=64,H=64;
const cv=new Canvas(W,H);
for(let y=0;y<H;y++)for(let x=0;x<W;x++)cv.set(x,y,0,0,0,0);
const nz=(x,y,s)=>(((x*37+y*53+s*11)%17)/17);
function body(px,py,x,y){const n=nz(x,y,3)*26-13;const ring=(y%7<2);cv.set(px,py,(ring?196:236)+n,(ring?168:222)+n,(ring?132:186)+n,1);}
function dark(px,py,x,y){const n=nz(x,y,5)*16-8;cv.set(px,py,62+n,48+n,40+n,1);}
function eye(px,py){cv.set(px,py,26,20,18,1);}
let penX=1,penY=1,rowH=0;
function alloc(w,h,d){const fw=Math.ceil(2*(w+d)),fh=Math.ceil(d+h);
  if(penX+fw>W){penX=1;penY+=rowH+2;rowH=0;}
  if(penY+fh>H)throw new Error('贴图放不下');const u=penX,v=penY;penX+=fw+2;rowH=Math.max(rowH,fh);return[u,v,fw,fh];}
const C=[];
function cube(bone,o,s,fn){const uv=alloc(s[0],s[1],s[2]);C.push({bone,o,s,uv,fn});}
/* 身体: 从尾到头逐段变粗(段名 seg1..seg7, 头是 seg7) */
const SEGS=[[1.6], [2.0], [2.4], [2.8], [3.0], [3.0], [2.6]];
for(let i=0;i<SEGS.length;i++){
  const wdt=SEGS[i][0], z=-0.8-i*1.55;
  cube('seg'+(i+1), [-wdt/2, 0.1+ (i>3?0.05:0), z-wdt/2], [wdt, wdt*0.85, 1.5], body);
}
cube('seg7',[-1.1,0.15,-13.6],[2.2,1.0,1.2],dark);          // 嘴
cube('seg7',[-0.9,1.0,-13.2],[0.5,0.5,0.5],eye);            // 眼
cube('seg7',[0.4,1.0,-13.2],[0.5,0.5,0.5],eye);
for(const c of C){const fw=Math.ceil(2*(c.s[0]+c.s[2])),fh=Math.ceil(c.s[2]+c.s[1]);
  for(let x=0;x<fw;x++)for(let y=0;y<fh;y++)c.fn(c.uv[0]+x,c.uv[1]+y,x,y);}
const PIV={root:[0,0,0]};const PAR={};
for(let i=0;i<7;i++){PIV['seg'+(i+1)]=[0,0.6,-0.8-i*1.55];PAR['seg'+(i+1)]=i===0?'root':'seg'+i;}
const bones=Object.keys(PIV).map(name=>{const cs=C.filter(c=>c.bone===name).map(c=>({origin:c.o,size:c.s,uv:[c.uv[0],c.uv[1]]}));
  const b={name,pivot:PIV[name]};if(cs.length)b.cubes=cs;if(PAR[name])b.parent=PAR[name];return b;});
const geo={format_version:'1.12.0','minecraft:geometry':[{description:{identifier:'geometry.maggot_minion',texture_width:W,texture_height:H,visible_bounds_width:2,visible_bounds_height:2,visible_bounds_offset:[0,0.5,0]},bones}]};
/* 蠕动动画: 每节错相位地左右摆 + 整体上下拱 */
const A='animation.maggot.';
const rot={};for(let i=0;i<7;i++){const p=i*0.075;rot['seg'+(i+1)]={rotation:{'0.0':[0,0,0],'0.25':[0,0,10],'0.5':[0,0,0],'0.75':[0,0,-10],'1.0':[0,0,0]}};
  const keys=rot['seg'+(i+1)].rotation;const nk={};for(const k of Object.keys(keys)){const t=Math.min(1.0,parseFloat(k)+p);nk[t.toFixed(2)]=keys[k];}rot['seg'+(i+1)]={rotation:nk};}
const anim={format_version:'1.8.0',animations:{[A+'crawl']:{loop:true,animation_length:1.0,bones:{root:{position:{'0.0':[0,0,0],'0.5':[0,0.5,0],'1.0':[0,0,0]}},...rot}}}};
fs.mkdirSync(OUT,{recursive:true});
fs.writeFileSync(path.join(OUT,'maggot_minion.geo.json'),JSON.stringify(geo,null,1));
fs.writeFileSync(path.join(OUT,'maggot_minion.animation.json'),JSON.stringify(anim,null,1));
cv.save(path.join(OUT,'maggot_minion.png'));
/* debuff 图标 16x16 */
const ic=new Canvas(16,16);
for(let y=0;y<16;y++)for(let x=0;x<16;x++)ic.set(x,y,0,0,0,0);
for(let i=0;i<7;i++){const w2=2+((i<4)?i:6-i);for(let x=0;x<w2;x++)for(let y=0;y<2;y++)ic.set(3+x,12-i*1.6-y,222,196,150,1);}
ic.set(3,4,30,24,20,1);ic.set(11,4,30,24,20,1);
fs.writeFileSync(path.join(OUT,'grub_curse.png'),require('fs').readFileSync(path.join(OUT,'maggot_minion.png')).length?path.join(OUT,'grub_curse_tmp.png'):path.join(OUT,'grub_curse_tmp.png'));
ic.save(path.join(OUT,'grub_curse.png'));
console.log('蛆模型已生成 -> '+OUT);
