const { decodePNG, Canvas } = require('./png.cjs');
const fs=require('fs');
const ts=['glass','lightning','dark','frost','ion'];
const imgs=ts.map(t=>decodePNG(`out_orbs/prev_${t}.png`));
const W=imgs.reduce((a,i)=>a+i.w,0)+ (imgs.length-1)*4;
const H=Math.max(...imgs.map(i=>i.h));
const out=new Canvas(W,H);
let x=0;
for(const im of imgs){
  for(let y=0;y<im.h;y++) for(let px=0;px<im.w;px++){
    const i=(y*im.w+px)*4;
    const a=im.data[i+3]/255;
    if(a<=0) continue;
    // 以白底合成
    out.set(x+px,y, im.data[i]*a+255*(1-a), im.data[i+1]*a+255*(1-a), im.data[i+2]*a+255*(1-a), 1);
  }
  x+=im.w+4;
}
out.scaled(2).save('C:/Users/YOUR_USERNAME/Desktop/FT-模型检查/五种充能球.png');
console.log('五种球合集已写出');
