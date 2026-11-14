const {decodePNG}=require('./png.cjs');
function figBox(img){let mnx=1e9,mny=1e9,mxx=-1,mxy=-1;for(let y=0;y<img.h;y++)for(let x=0;x<img.w;x++){const i=(y*img.w+x)*4;if(img.data[i+3]>0&&!(img.data[i]<40&&img.data[i+1]<40&&img.data[i+2]<45)){if(x<mnx)mnx=x;if(x>mxx)mxx=x;if(y<mny)mny=y;if(y>mxy)mxy=y;}}return{mnx,mny,mxx,mxy};}
function scan(file,label,panel){
  const img=decodePNG(file);
  // 只取指定面板(前视图)区域
  const x0=panel?panel[0]:0,x1=panel?panel[2]:img.w,y0=panel?panel[1]:0,y1=panel?panel[3]:img.h;
  let mnx=1e9,mny=1e9,mxx=-1,mxy=-1;
  for(let y=y0;y<y1;y++)for(let x=x0;x<x1;x++){const i=(y*img.w+x)*4;const r=img.data[i],g=img.data[i+1],b=img.data[i+2];if(img.data[i+3]>0&&!(r<40&&g<40&&b<45)){if(x<mnx)mnx=x;if(x>mxx)mxx=x;if(y<mny)mny=y;if(y>mxy)mxy=y;}}
  const h=mxy-mny, w=mxx-mnx, midX=Math.round((mnx+mxx)/2);
  console.log(`${label}: 图形框 x${mnx}..${mxx} y${mny}..${mxy} (${w}x${h})`);
  for(const frac of [0.25,0.40,0.55,0.70]){
    const y=Math.round(mny+h*frac);
    const row=[];
    for(const fx of [0.35,0.5,0.65]){const x=Math.round(mnx+w*fx);const i=(y*img.w+x)*4;row.push(`(${img.data[i]},${img.data[i+1]},${img.data[i+2]})`);}
    console.log(`   高度${(frac*100)|0}%  y=${y}  左中右: ${row.join(' ')}`);
  }
}
scan('C:/Users/YOUR_USERNAME/Desktop/FT-模型检查/石像v3_statue_sugar_queen.png','我的(正面)',[6,6,164,300]);
scan('C:/Users/YOUR_USERNAME/Desktop/FT-模型检查/姜饼王_原始四视图.png','原始(正面)',[0,0,424,390]);
