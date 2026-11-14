const fs = require('node:fs');
const path = require('node:path');
const {Canvas, decodePNG} = require('./png.cjs');
const meshFile = process.argv[2];
const vertices = JSON.parse(fs.readFileSync(meshFile,'utf8'));
const resource = path.resolve(__dirname,'../../src/main/resources/assets/ftboss/textures');
const textures = [decodePNG(path.join(resource,'entity/butcher_v3.png')),decodePNG(path.join(resource,'item/turkey_legs.png'))];
const panel = 580;
const canvas = new Canvas(panel*2,panel*2);
const depth = new Float32Array(canvas.w*canvas.h).fill(Infinity);
const cameras = [[0,0],[90,0],[180,0],[330,12]];
for (let view = 0; view < cameras.length; view++) {
    const left = view%2*panel, top = Math.floor(view/2)*panel;
    canvas.rect(left,top,panel-3,panel-3,25,28,32,1);
    const yaw = cameras[view][0]*Math.PI/180, pitch = cameras[view][1]*Math.PI/180;
    const transformed = vertices.map(vertex => {
        const rotatedX = vertex[0]*Math.cos(yaw)-vertex[2]*Math.sin(yaw);
        const rotatedZ = vertex[0]*Math.sin(yaw)+vertex[2]*Math.cos(yaw);
        return [-rotatedX,vertex[1]*Math.cos(pitch)+rotatedZ*Math.sin(pitch),
            rotatedZ*Math.cos(pitch)-vertex[1]*Math.sin(pitch),...vertex.slice(3)];
    });
    const minX = Math.min(...transformed.map(vertex => vertex[0]));
    const maxX = Math.max(...transformed.map(vertex => vertex[0]));
    const minY = Math.min(...transformed.map(vertex => vertex[1]));
    const maxY = Math.max(...transformed.map(vertex => vertex[1]));
    const scale = Math.min(10,(panel-65)/(maxX-minX),(panel-70)/(maxY-minY));
    const projected = transformed.map(vertex => [left+panel/2+(vertex[0]-(minX+maxX)/2)*scale,
        top+panel/2-(vertex[1]-(minY+maxY)/2)*scale,...vertex.slice(2)]);
    for (let start = 0; start < projected.length; start += 4) {
        const points = projected.slice(start,start+4);
        const texture = textures[Math.round(points[0][5])];
        const raw = transformed.slice(start,start+4);
        const first = raw[1].slice(0,3).map((value,index) => value-raw[0][index]);
        const second = raw[2].slice(0,3).map((value,index) => value-raw[0][index]);
        const normal = [first[1]*second[2]-first[2]*second[1],first[2]*second[0]-first[0]*second[2],first[0]*second[1]-first[1]*second[0]];
        const length = Math.hypot(...normal) || 1;
        const shade = 0.76 + 0.24*Math.abs((normal[0]*0.3+normal[1]*0.7-normal[2]*0.65)/length);
        for (const indices of [[0,1,2],[0,2,3]]) {
            const [firstPoint,secondPoint,thirdPoint] = indices.map(index => points[index]);
            const denominator = (secondPoint[1]-thirdPoint[1])*(firstPoint[0]-thirdPoint[0])+(thirdPoint[0]-secondPoint[0])*(firstPoint[1]-thirdPoint[1]);
            if (Math.abs(denominator)<0.00001) continue;
            const lowerX = Math.max(left,Math.floor(Math.min(firstPoint[0],secondPoint[0],thirdPoint[0])));
            const upperX = Math.min(left+panel-1,Math.ceil(Math.max(firstPoint[0],secondPoint[0],thirdPoint[0])));
            const lowerY = Math.max(top,Math.floor(Math.min(firstPoint[1],secondPoint[1],thirdPoint[1])));
            const upperY = Math.min(top+panel-1,Math.ceil(Math.max(firstPoint[1],secondPoint[1],thirdPoint[1])));
            for (let pixelY = lowerY; pixelY <= upperY; pixelY++) {
                for (let pixelX = lowerX; pixelX <= upperX; pixelX++) {
                    const weightA = ((secondPoint[1]-thirdPoint[1])*(pixelX+0.5-thirdPoint[0])+(thirdPoint[0]-secondPoint[0])*(pixelY+0.5-thirdPoint[1]))/denominator;
                    const weightB = ((thirdPoint[1]-firstPoint[1])*(pixelX+0.5-thirdPoint[0])+(firstPoint[0]-thirdPoint[0])*(pixelY+0.5-thirdPoint[1]))/denominator;
                    const weightC = 1-weightA-weightB;
                    if (Math.min(weightA,weightB,weightC)<-0.0001) continue;
                    const interpolate = index => weightA*firstPoint[index]+weightB*secondPoint[index]+weightC*thirdPoint[index];
                    const currentDepth = interpolate(2), pixel = pixelY*canvas.w+pixelX;
                    if (currentDepth>=depth[pixel]) continue;
                    const texX = Math.min(texture.w-1,Math.max(0,Math.floor(interpolate(3)*texture.w)));
                    const texY = Math.min(texture.h-1,Math.max(0,Math.floor(interpolate(4)*texture.h)));
                    const offset = (texY*texture.w+texX)*4;
                    if (texture.data[offset+3]<128) continue;
                    depth[pixel] = currentDepth;
                    canvas.set(pixelX,pixelY,...[0,1,2].map(channel => texture.data[offset+channel]*shade),1);
                }
            }
        }
    }
}
canvas.save(meshFile.replace(/\.json$/,'.png'));
console.log('Rendered actual Java mesh: '+meshFile);
