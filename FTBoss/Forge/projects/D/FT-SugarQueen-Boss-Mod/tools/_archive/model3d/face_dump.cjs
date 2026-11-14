const {decodePNG}=require('./png.cjs');
const t=decodePNG('D:/ft工程文件/FT-SugarQueen-Boss-Mod/src/main/resources/assets/ftboss/textures/entity/gingerbread_king_v2.png');
// 头部 north 面区域 [76,38]..[87,49]
console.log('头部 north 面区域 [76,38]-[87,49] 的明暗图(# = 暗/眼, . = 亮/糖霜, 其他=中间):');
for(let y=38;y<49;y++){
  let row='';
  for(let x=76;x<87;x++){
    const i=(y*t.w+x)*4; const l=(t.data[i]+t.data[i+1]+t.data[i+2])/3;
    row += l<90?'#':(l>200?'.':'o');
  }
  console.log(`  y=${y}  ${row}`);
}
