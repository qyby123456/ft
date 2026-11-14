const { decodePNG, Canvas } = require('./png.cjs');
const ts=['glass','lightning','frost','ion','dark'];
const imgs=ts.map(t=>decodePNG(`out_orbs/${t}_orb.png`));
const W=imgs.reduce((a,i)=>a+i.w,0)+ (imgs.length-1)*4;
const H=Math.max(...imgs.map(i=>i.h));
const out=new Canvas(W,H);
let x=0;
for(const im of imgs){
  for(let y=0;y<im.h;y++) for(let px=0;px<im.w;px++){
    const i=(y*im.w+px)*4;
    out.set(x+px,y, im.data[i],im.data[i+1],im.data[i+2],1);
  }
  x+=im.w+4;
}
out.scaled(3).save('C:/Users/YOUR_USERNAME/Desktop/FT-模型检查/五种球_球面贴图.png');
console.log('球面贴图平铺已写出');
