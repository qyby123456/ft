const fs = require('fs');
const path = require('path');
const { encodePNG, decodePNG } = require('../../png.cjs');
const sub = (first, second) => first.map((value, axis) => value - second[axis]);
const dot = (first, second) => first.reduce((sum, value, axis) => sum + value * second[axis], 0);
const cross = (first, second) => [first[1]*second[2]-first[2]*second[1],first[2]*second[0]-first[0]*second[2],first[0]*second[1]-first[1]*second[0]];
const normalize = vector => vector.map(value => value / Math.hypot(...vector));
const clamp = value => Math.max(0,Math.min(255,Math.round(value)));

function transform(point, element) {
  if (!element.rotation) return [...point];
  const {axis,angle,origin,rescale} = element.rotation;
  const cosine = Math.cos(angle*Math.PI/180), sine = Math.sin(angle*Math.PI/180);
  const local = sub(point,origin);
  const [first,second] = {x:[1,2],y:[2,0],z:[0,1]}[axis];
  const oldFirst=local[first],oldSecond=local[second];
  local[first]=oldFirst*cosine-oldSecond*sine;
  local[second]=oldFirst*sine+oldSecond*cosine;
  if (rescale) { local[first]/=cosine; local[second]/=cosine; }
  return local.map((value,index)=>value+origin[index]);
}

function facesOf(model) {
  const result=[];
  for (const element of model.elements) {
    const [x0,y0,z0]=element.from,[x1,y1,z1]=element.to;
    const corners={
      north:[[x1,y1,z0],[x1,y0,z0],[x0,y0,z0],[x0,y1,z0]],
      south:[[x0,y1,z1],[x0,y0,z1],[x1,y0,z1],[x1,y1,z1]],
      east:[[x1,y1,z1],[x1,y0,z1],[x1,y0,z0],[x1,y1,z0]],
      west:[[x0,y1,z0],[x0,y0,z0],[x0,y0,z1],[x0,y1,z1]],
      up:[[x0,y1,z0],[x0,y1,z1],[x1,y1,z1],[x1,y1,z0]],
      down:[[x0,y0,z1],[x0,y0,z0],[x1,y0,z0],[x1,y0,z1]]
    };
    for (const [direction,face] of Object.entries(element.faces)) {
      const points=corners[direction].map(point=>transform(point,element));
      const normal=normalize(cross(sub(points[1],points[0]),sub(points[2],points[0])));
      const [u0,v0,u1,v1]=face.uv;
      const uv=[[u0,v0],[u0,v1],[u1,v1],[u1,v0]];
      const shift=(face.rotation||0)/90;
      result.push({points,normal,uv:uv.map((_,index)=>uv[(index+shift)%4]),name:element.name,direction});
    }
  }
  return result;
}

function render(model,texture,options={}) {
  const width=options.width||420,height=options.height||460;
  const image={w:width,h:height,data:Buffer.alloc(width*height*4)};
  if(options.background) for(let offset=0;offset<image.data.length;offset+=4) image.data.set([...options.background,255],offset);
  const depth=new Float64Array(width*height).fill(-Infinity);
  const camera=normalize(options.camera||[-0.65,0.38,-1]);
  const right=normalize(cross([0,1,0],camera)),up=normalize(cross(camera,right));
  const center=options.center||[8,8.15,8],roll=(options.roll||0)*Math.PI/180;
  const scale=options.scale||28,light=normalize([-0.65,1.2,-0.85]);
  const project=point=>{
    const local=sub(point,center),horizontal=dot(local,right),vertical=dot(local,up);
    return [width/2+scale*(horizontal*Math.cos(roll)-vertical*Math.sin(roll)),height/2-scale*(horizontal*Math.sin(roll)+vertical*Math.cos(roll)),dot(local,camera)];
  };
  for(const face of facesOf(model)) {
    if(dot(face.normal,camera)<=0.000001) continue;
    const points=face.points.map(project),shade=options.unlit?1:0.67+Math.max(0,dot(face.normal,light))*0.33;
    for(const triangle of [[0,1,2],[0,2,3]]) {
      const [first,second,third]=triangle.map(index=>points[index]);
      const denominator=(second[1]-third[1])*(first[0]-third[0])+(third[0]-second[0])*(first[1]-third[1]);
      if(Math.abs(denominator)<0.000001) continue;
      const minimumX=Math.max(0,Math.floor(Math.min(first[0],second[0],third[0]))),maximumX=Math.min(width-1,Math.ceil(Math.max(first[0],second[0],third[0])));
      const minimumY=Math.max(0,Math.floor(Math.min(first[1],second[1],third[1]))),maximumY=Math.min(height-1,Math.ceil(Math.max(first[1],second[1],third[1])));
      for(let screenY=minimumY;screenY<=maximumY;screenY++) for(let screenX=minimumX;screenX<=maximumX;screenX++) {
        const sampleX=screenX+0.5,sampleY=screenY+0.5;
        const firstWeight=((second[1]-third[1])*(sampleX-third[0])+(third[0]-second[0])*(sampleY-third[1]))/denominator;
        const secondWeight=((third[1]-first[1])*(sampleX-third[0])+(first[0]-third[0])*(sampleY-third[1]))/denominator,thirdWeight=1-firstWeight-secondWeight;
        if(Math.min(firstWeight,secondWeight,thirdWeight)<-0.00001) continue;
        const weights=[firstWeight,secondWeight,thirdWeight],pixel=screenY*width+screenX;
        const distance=first[2]*firstWeight+second[2]*secondWeight+third[2]*thirdWeight;
        if(distance<depth[pixel]-0.00001) continue;
        const uv=[0,1].map(axis=>triangle.reduce((sum,index,weightIndex)=>sum+face.uv[index][axis]*weights[weightIndex],0));
        const texX=Math.max(0,Math.min(texture.w-1,Math.floor(uv[0]*texture.w/16))),texY=Math.max(0,Math.min(texture.h-1,Math.floor(uv[1]*texture.h/16)));
        const textureOffset=(texY*texture.w+texX)*4,alpha=texture.data[textureOffset+3];
        if(alpha<1) continue;
        const offset=pixel*4;
        for(let channel=0;channel<3;channel++) image.data[offset+channel]=clamp(texture.data[textureOffset+channel]*shade);
        image.data[offset+3]=alpha; depth[pixel]=distance;
      }
    }
  }
  return image;
}

function rectangle(image,x,y,width,height,color) {
  for(let row=Math.max(0,y);row<Math.min(image.h,y+height);row++) for(let column=Math.max(0,x);column<Math.min(image.w,x+width);column++) image.data.set(color.length===4?color:[...color,255],(row*image.w+column)*4);
}
const FONT={A:"01110100011000111111100011000110001",B:"11110100011000111110100011000111110",C:"01111100001000010000100001000001111",D:"11110100011000110001100011000111110",E:"11111100001000011110100001000011111",F:"11111100001000011110100001000010000",G:"01111100001000010111100011000101111",H:"10001100011000111111100011000110001",I:"11111001000010000100001000010011111",J:"00111000100001000010100101001001100",K:"10001100101010011000101001001010001",L:"10000100001000010000100001000011111",M:"10001110111010110101100011000110001",N:"10001110011010110011100011000110001",O:"01110100011000110001100011000101110",P:"11110100011000111110100001000010000",Q:"01110100011000110001101011001001101",R:"11110100011000111110101001001010001",S:"01111100001000001110000010000111110",T:"11111001000010000100001000010000100",U:"10001100011000110001100011000101110",V:"10001100011000110001100010101000100",W:"10001100011000110101101011101110001",X:"10001100010101000100010101000110001",Y:"10001100010101000100001000010000100",Z:"11111000010001000100010001000011111","0":"01110100011001110101110011000101110","1":"00100011000010000100001000010001110","2":"01110100010000100010001000100011111","3":"11110000010000101110000010000111110","4":"00010001100101010010111110001000010","5":"11111100001000011110000010000111110","6":"01110100001000011110100011000101110","7":"11111000010001000100010000100001000","8":"01110100011000101110100011000101110","9":"01110100011000101111000010000101110","-":"00000000000000011111000000000000000",":":"00000001000010000000001000010000000","/":"00001000100001000100010000100010000",".":"00000000000000000000000000010000100","+":"00000001000010011111001000010000000"};
function text(image,label,x,y,size=2,color=[209,207,193]) {
  for(const character of label.toUpperCase()) {
    const glyph=FONT[character];
    if(glyph) for(let row=0;row<7;row++) for(let column=0;column<5;column++) if(glyph[row*5+column]==='1') rectangle(image,x+column*size,y+row*size,size,size,color);
    x+=6*size;
  }
}
function blank(width,height,color=[26,33,32]) {const image={w:width,h:height,data:Buffer.alloc(width*height*4)};rectangle(image,0,0,width,height,color);return image;}
function paste(destination,source,x,y,zoom=1) {
  for(let row=0;row<source.h;row++) for(let column=0;column<source.w;column++) {
    const offset=(row*source.w+column)*4;
    if(!source.data[offset+3]) continue;
    rectangle(destination,x+column*zoom,y+row*zoom,zoom,zoom,Array.from(source.data.subarray(offset,offset+4)));
  }
}
function save(file,image) {fs.writeFileSync(file,encodePNG(image.w,image.h,image.data));}

function previews(root,output) {
  const resource=path.join(root,'src/main/resources/assets/ftboss');
  const model=JSON.parse(fs.readFileSync(path.join(resource,'models/item/boss_materials/shepherd_material.json'),'utf8'));
  const texture=decodePNG(path.join(resource,'textures/item/boss_materials/shepherd.png')),icon=decodePNG(path.join(resource,'textures/item/shepherd_material.png'));
  const sheet=blank(1120,1210);
  text(sheet,'BENGER / SHEPHERD MATERIAL',36,30,4,[236,225,190]);
  text(sheet,'FINAL RUNTIME JSON + TEXTURE / ORTHOGRAPHIC',36,78,2,[143,167,143]);
  const views=[['FRONT / NORTH',[0,0,-1]],['BACK / SOUTH',[0,0,1]],['LEFT / WEST',[-1,0,0]],['RIGHT / EAST',[1,0,0]]];
  views.forEach(([name,camera],index)=>{
    const x=28+(index%2)*550,y=120+Math.floor(index/2)*530;
    rectangle(sheet,x,y,514,505,[37,45,42]);
    const image=render(model,texture,{camera,width:480,height:440,scale:30});
    paste(sheet,image,x+17,y+22);text(sheet,name,x+25,y+470,3,[213,221,194]);
    save(path.join(output,['front.png','back.png','left.png','right.png'][index]),image);
  });
  text(sheet,'BONE / WOVEN GRASS STRAP / BRASS WHISTLE BLANK',36,1180,2,[144,164,145]);
  save(path.join(output,'four_views.png'),sheet);
  const beauty=blank(1280,1010);
  text(beauty,'PASTURE WHISTLE BONE',38,30,4,[236,225,190]);text(beauty,'BENGER / FTBOSS:SHEPHERD_MATERIAL',40,79,2,[143,167,143]);
  const hero=render(model,texture,{width:730,height:800,scale:53,camera:[-0.8,0.35,-1],roll:17});paste(beauty,hero,2,122);
  ['01 / CARVED CORTEX','02 / REAL STRAP DEPTH','03 / OPEN BUCKLE + REED','04 / EXPOSED MARROW CUT'].forEach((line,index)=>text(beauty,line,785,145+index*35,2));
  const underside=render(model,texture,{width:440,height:370,scale:26,camera:[0.6,-0.85,-1],center:[8,7.3,8]});paste(beauty,underside,790,300);
  text(beauty,'LOWER CUT / UNDERSIDE',800,680,2,[143,167,143]);paste(beauty,icon,824,740,5);paste(beauty,icon,1050,785);
  text(beauty,'32 PX / 5X',800,930,2,[143,167,143]);text(beauty,'NATIVE',1030,840,2,[143,167,143]);
  text(beauty,'EDITABLE BLOCKBENCH SOURCE / PIXEL UV / SINGLE AXIS JAVA ROTATIONS',38,982,2,[143,167,143]);save(path.join(output,'detail_and_icon.png'),beauty);
  const icons=blank(850,420);text(icons,'SAME OBJECT / INVENTORY ICON',30,26,3,[236,225,190]);paste(icons,icon,45,100,8);paste(icons,icon,372,220);
  const comparison=render(model,texture,{width:350,height:290,scale:21,camera:[-0.35,0.28,-1],roll:28});paste(icons,comparison,480,75);
  text(icons,'32 PX AT 8X',52,376,2);text(icons,'1X',375,278,2);text(icons,'RUNTIME MODEL',505,376,2);save(path.join(output,'icon_comparison.png'),icons);
}
module.exports={render,save,previews,facesOf,transform,blank,paste,text,rectangle};
if(require.main===module) previews(path.resolve(__dirname,'../../../..'),path.resolve(__dirname,'../../../../art/boss_materials/refine/shepherd'));
