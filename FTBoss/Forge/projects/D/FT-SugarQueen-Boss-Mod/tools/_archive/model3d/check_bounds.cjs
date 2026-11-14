const fs=require('fs');
const A='D:/ft工程文件/FT-SugarQueen-Boss-Mod/src/main/resources/assets/ftboss/models/block';
for(const id of ['statue_sugar_queen','statue_butcher','statue_saya','statue_wine_barrel','statue_rat_swarm']){
  for(const half of ['lower','upper']){
    for(const paint of ['','_painted']){
      const f=`${A}/${id}_${half}${paint}.json`;
      const j=JSON.parse(fs.readFileSync(f,'utf8'));
      let mw=0,mh=0,md=0,mn=[1e9,1e9,1e9],mx=[-1e9,-1e9,-1e9],bad=0;
      for(const e of j.elements||[]){
        const s=[e.to[0]-e.from[0],e.to[1]-e.from[1],e.to[2]-e.from[2]];
        mw=Math.max(mw,s[0]);mh=Math.max(mh,s[1]);md=Math.max(md,s[2]);
        for(let i=0;i<3;i++){mn[i]=Math.min(mn[i],e.from[i]);mx[i]=Math.max(mx[i],e.to[i]);}
        if(s[0]<=0||s[1]<=0||s[2]<=0) bad++;
      }
      if(mw>16||mh>16||md>16||bad||mn[0]<-16||mn[1]<-16||mn[2]<-16||mx[0]>32||mx[1]>32||mx[2]>32)
        console.log(`⚠ ${id}_${half}${paint}: 最大cube ${mw.toFixed(1)}x${mh.toFixed(1)}x${md.toFixed(1)} 范围 x${mn[0].toFixed(1)}..${mx[0].toFixed(1)} y${mn[1].toFixed(1)}..${mx[1].toFixed(1)} z${mn[2].toFixed(1)}..${mx[2].toFixed(1)} 零尺寸=${bad}`);
    }
  }
}
console.log('检查完毕(没有输出=全部合规: 单 cube ≤16、坐标在 -16..32)');
