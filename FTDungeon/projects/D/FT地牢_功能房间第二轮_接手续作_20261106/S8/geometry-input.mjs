import fs from 'node:fs/promises'
for(let number=11;number<=20;number++){
 const room=`S8-${number}`,inventory=JSON.parse(await fs.readFile(`${room}/inventory.json`,'utf8')),volumes=[],stances=[]
 for(const port of inventory.metadata.ports){
  const alongEast=['north','south'].includes(port.side),center=alongEast?port.foot[0]:port.foot[2],start=center-Math.floor(port.width/2),up=port.foot[1],coordinate=alongEast?port.foot[2]:port.foot[0]
  const depth=coordinate===0?[0,1]:[coordinate,coordinate+1]
  const bounds=alongEast?[start+.001,up+.001,depth[0]+.001,start+port.width-.001,up+port.height-.001,depth[1]-.001]:[depth[0]+.001,up+.001,start+.001,depth[1]-.001,up+port.height-.001,start+port.width-.001]
  volumes.push({label:`${port.side} declared full ${port.width}x${port.height} aperture`,bounds})
 }
 if(number===11){
  for(let up=2;up<=8;up+=.25)volumes.push({label:`ladder vertical clearance y=${up}`,bounds:[16.201,up+.001,6.201,16.799,up+1.799,6.799]})
  stances.push({label:'upper balcony landing',position:[16.5,8,5.5]},{label:'upper archive reading aisle',position:[17.5,8,4.5]})
 }
 await fs.writeFile(`${room}/qa/geometry-input.json`,JSON.stringify({sha256:inventory.sha256,volumes,stances},null,2))
}
