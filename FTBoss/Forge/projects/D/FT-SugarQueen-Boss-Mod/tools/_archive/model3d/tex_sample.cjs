const {decodePNG}=require('./png.cjs');
const t=decodePNG('D:/ft工程文件/FT-SugarQueen-Boss-Mod/src/main/resources/assets/ftboss/textures/entity/gingerbread_king_v2.png');
console.log('贴图',t.w+'x'+t.h);
const px=(x,y)=>{const i=(y*t.w+x)*4;return `(${t.data[i]},${t.data[i+1]},${t.data[i+2]})`};
console.log('躯干 north 区域 [9,9]-[25,24] 采样:');
for(const y of [11,14,17,20,23]) console.log('  y='+y+' : '+[10,13,17,21,24].map(x=>px(x,y)).join(' '));
const px2=(x,y)=>{const i=(y*t.w+x)*4;return `(${t.data[i]},${t.data[i+1]},${t.data[i+2]})`};
console.log('斗篷 cape north 区域 [73.4,53.4]-[90.6,73.2] 采样:');
for(const y of [56,60,64,68,72]) console.log('  y='+y+' : '+[75,79,83,87,89].map(x=>px2(x,y)).join(' '));
