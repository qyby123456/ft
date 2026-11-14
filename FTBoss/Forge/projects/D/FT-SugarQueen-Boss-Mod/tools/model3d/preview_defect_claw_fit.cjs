const fs = require('node:fs');
const path = require('node:path');
const space = require('./cube_space.cjs');
const {armPoint} = require('./puffer_bat_hand.cjs');
const {Canvas,decodePNG} = require('./png.cjs');
const assets = path.resolve(__dirname,'../../src/main/resources/assets/ftboss');
const before = JSON.parse(fs.readFileSync(process.argv[2],'utf8'));
const after = JSON.parse(fs.readFileSync(path.join(assets,'models/item/defect_claw.json'),'utf8'));
const texture = decodePNG(path.join(assets,'textures/item/defect_claw.png'));
const geometry = {geckolib:false,byName:new Map([['root',{name:'root'}]])};
const faceIndices = {
    north:[6,4,0,2],south:[3,1,5,7],west:[2,0,1,3],east:[7,5,4,6],up:[3,7,6,2],down:[0,4,5,1]
};
const panel = 600, canvas = new Canvas(panel*2,panel*2);
const depth = new Float64Array(canvas.w*canvas.h).fill(Infinity);
const cases = [
    {model:before,yaw:65,left:false,color:[170,110,74]},
    {model:after,yaw:65,left:false,color:[97,173,148]},
    {model:after,yaw:15,left:false,color:[97,173,148]},
    {model:after,yaw:-15,left:true,color:[97,173,148]}
];

function corners(element) {
    const cube = {origin:element.from,size:space.sub(element.to,element.from)};
    if (element.rotation) {
        cube.pivot = element.rotation.origin;
        cube.rotation = ['x','y','z'].map(axis => element.rotation.axis === axis ? element.rotation.angle : 0);
    }
    return space.cubeCorners(geometry,{bone:'root',name:element.name || 'reference',cube});
}

function referenceCube(origin,size,color,transform = point => point) {
    const points = corners({from:origin,to:space.add(origin,size)}).map(transform);
    return Object.entries(faceIndices).map(([direction,indices]) => ({points:indices.map(index => points[index]),direction,color}));
}

for (const [view,settings] of cases.entries()) {
    const left = view%2*panel, top = Math.floor(view/2)*panel;
    canvas.rect(left,top,panel-3,panel-3,26,30,36,1);
    canvas.rect(left+20,top+20,panel-43,5,...settings.color,1);
    const side = settings.left ? 1 : -1;
    const armTransform = point => space.add(space.rotateXYZ(point,[-20,0,0],[0,0,0]),[side*5,2,0]);
    const faces = [
        ...referenceCube([-4,0,-2],[8,12,4],[60,100,94]),
        ...referenceCube([-4,-8,-4],[8,8,8],[96,138,132]),
        ...referenceCube([-4,12,-2],[4,12,4],[50,64,69]),
        ...referenceCube([0,12,-2],[4,12,4],[50,64,69]),
        ...referenceCube([-2+side,-2,-2],[4,8,4],[96,146,140],armTransform),
        ...referenceCube([-2+side,6,-2],[4,4,4],[188,146,121],armTransform),
        ...referenceCube([-2-side,-2,-2],[4,12,4],[72,115,110],point => space.add(point,[-side*5,2,0]))
    ];
    const display = settings.model.display[settings.left ? 'thirdperson_lefthand' : 'thirdperson_righthand'];
    for (const element of settings.model.elements) {
        const points = corners(element).map(point => armTransform(armPoint(point,display,settings.left)));
        for (const [direction,face] of Object.entries(element.faces)) {
            const [minU,minV,maxU,maxV] = face.uv;
            const uv = [[minU,minV],[minU,maxV],[maxU,maxV],[maxU,minV]];
            for (let turn=0;turn<(face.rotation||0)/90;turn++) uv.unshift(uv.pop());
            faces.push({points:faceIndices[direction].map(index=>points[index]),direction,uv});
        }
    }
    const yaw = settings.yaw*Math.PI/180;
    const project = point => {
        const horizontal = point[0]*Math.cos(yaw)-point[2]*Math.sin(yaw);
        const distance = point[0]*Math.sin(yaw)+point[2]*Math.cos(yaw);
        return [left+panel/2-horizontal*14,top+170+point[1]*14,distance];
    };
    for (const face of faces) {
        const points = face.points.map(project);
        const shade = {north:1,south:0.82,up:1.08,down:0.64,east:0.92,west:0.9}[face.direction];
        for (const indices of [[0,1,2],[0,2,3]]) {
            const vertices = indices.map(index=>points[index]);
            const [first,second,third] = vertices;
            const denominator = (second[1]-third[1])*(first[0]-third[0])+(third[0]-second[0])*(first[1]-third[1]);
            if (Math.abs(denominator)<0.00001) continue;
            const minX = Math.max(left,Math.floor(Math.min(...vertices.map(vertex=>vertex[0]))));
            const maxX = Math.min(left+panel-1,Math.ceil(Math.max(...vertices.map(vertex=>vertex[0]))));
            const minY = Math.max(top,Math.floor(Math.min(...vertices.map(vertex=>vertex[1]))));
            const maxY = Math.min(top+panel-1,Math.ceil(Math.max(...vertices.map(vertex=>vertex[1]))));
            for (let row=minY;row<=maxY;row++) for (let column=minX;column<=maxX;column++) {
                const weightA = ((second[1]-third[1])*(column+0.5-third[0])+(third[0]-second[0])*(row+0.5-third[1]))/denominator;
                const weightB = ((third[1]-first[1])*(column+0.5-third[0])+(first[0]-third[0])*(row+0.5-third[1]))/denominator;
                const weights = [weightA,weightB,1-weightA-weightB];
                if (Math.min(...weights)<-0.00001) continue;
                const distance = weights.reduce((total,weight,index)=>total+weight*vertices[index][2],0);
                const offset = row*canvas.w+column;
                if (distance>=depth[offset]) continue;
                let color = face.color;
                if (face.uv) {
                    const uv = [0,1].map(axis=>weights.reduce((total,weight,index)=>total+weight*face.uv[indices[index]][axis],0));
                    const texX = Math.max(0,Math.min(texture.w-1,Math.floor(uv[0]*texture.w/16)));
                    const texY = Math.max(0,Math.min(texture.h-1,Math.floor(uv[1]*texture.h/16)));
                    const texel = (texY*texture.w+texX)*4;
                    if (texture.data[texel+3]<128) continue;
                    color = [0,1,2].map(channel=>texture.data[texel+channel]);
                }
                depth[offset] = distance;
                canvas.set(column,row,...color.map(channel=>channel*shade),1);
            }
        }
    }
}
canvas.save(process.argv[3]);
console.log('Preview: top-left before / top-right after / bottom-left right hand / bottom-right left hand.');
