const fs = require('node:fs');
const path = require('node:path');
const zlib = require('node:zlib');
const root = path.resolve(__dirname, '..');
const renderDirectory = path.join(root, 'work', 'renders');
fs.mkdirSync(renderDirectory, {recursive: true});
const argumentsList = process.argv.slice(2);
function option(name, fallback) {
    const position = argumentsList.indexOf('--' + name);
    return position >= 0 ? argumentsList[position + 1] : fallback;
}
const prefix = option('prefix', 'runtime');
const grey = argumentsList.includes('--grey');
const modelPath = path.resolve(root, option('model', '运行资源/assets/ftboss/models/block/bloodthorn_brazier.json'));
const model = JSON.parse(fs.readFileSync(modelPath, 'utf8'));
const size = Number(option('size', '640'));
const frames = option('frames', '0').split(',').map(Number);
const views = option('views', 'front,back,left,right,top,underside,hero').split(',');
const colors = option('background', '37,38,48').split(',').map(Number);

const checksumTable = Array.from({length: 256}, (unused, number) => {
    let checksum = number;
    for (let bit = 0; bit < 8; bit++) checksum = (checksum & 1) ? 0xedb88320 ^ (checksum >>> 1) : checksum >>> 1;
    return checksum >>> 0;
});
function crc32(buffer) {
    let checksum = 0xffffffff;
    for (const byte of buffer) checksum = checksumTable[(checksum ^ byte) & 255] ^ (checksum >>> 8);
    return (checksum ^ 0xffffffff) >>> 0;
}
function chunk(type, buffer) {
    const name = Buffer.from(type);
    const header = Buffer.alloc(4);
    header.writeUInt32BE(buffer.length);
    const checksum = Buffer.alloc(4);
    checksum.writeUInt32BE(crc32(Buffer.concat([name, buffer])));
    return Buffer.concat([header, name, buffer, checksum]);
}
function writePng(filename, pixels, width, height) {
    const header = Buffer.alloc(13);
    header.writeUInt32BE(width, 0);
    header.writeUInt32BE(height, 4);
    header[8] = 8;
    header[9] = 6;
    const scanlines = Buffer.alloc(height * (width * 4 + 1));
    for (let row = 0; row < height; row++) pixels.copy(scanlines, row * (width * 4 + 1) + 1, row * width * 4, (row + 1) * width * 4);
    fs.writeFileSync(filename, Buffer.concat([Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]), chunk('IHDR', header), chunk('IDAT', zlib.deflateSync(scanlines)), chunk('IEND', Buffer.alloc(0))]));
}
function paeth(left, above, diagonal) {
    const predicted = left + above - diagonal;
    const leftDistance = Math.abs(predicted - left);
    const aboveDistance = Math.abs(predicted - above);
    const diagonalDistance = Math.abs(predicted - diagonal);
    return leftDistance <= aboveDistance && leftDistance <= diagonalDistance ? left : aboveDistance <= diagonalDistance ? above : diagonal;
}
function readPng(filename) {
    const png = fs.readFileSync(filename);
    let offset = 8;
    let width;
    let height;
    let channels;
    const pieces = [];
    while (offset < png.length) {
        const length = png.readUInt32BE(offset);
        const type = png.toString('ascii', offset + 4, offset + 8);
        const data = png.subarray(offset + 8, offset + 8 + length);
        if (type === 'IHDR') {
            width = data.readUInt32BE(0);
            height = data.readUInt32BE(4);
            channels = data[9] === 6 ? 4 : data[9] === 2 ? 3 : 0;
            if (data[8] !== 8 || !channels || data[12] !== 0) throw new Error('Only non-interlaced RGB/RGBA8 PNG is supported: ' + filename);
        }
        if (type === 'IDAT') pieces.push(data);
        offset += length + 12;
    }
    const encoded = zlib.inflateSync(Buffer.concat(pieces));
    const stride = width * channels;
    const decoded = Buffer.alloc(stride * height);
    for (let row = 0; row < height; row++) {
        const filter = encoded[row * (stride + 1)];
        for (let column = 0; column < stride; column++) {
            const left = column >= channels ? decoded[row * stride + column - channels] : 0;
            const above = row ? decoded[(row - 1) * stride + column] : 0;
            const diagonal = row && column >= channels ? decoded[(row - 1) * stride + column - channels] : 0;
            const prediction = filter === 0 ? 0 : filter === 1 ? left : filter === 2 ? above : filter === 3 ? Math.floor((left + above) / 2) : filter === 4 ? paeth(left, above, diagonal) : NaN;
            if (!Number.isFinite(prediction)) throw new Error('Unknown PNG filter');
            decoded[row * stride + column] = (encoded[row * (stride + 1) + column + 1] + prediction) & 255;
        }
    }
    return {width, height, channels, pixels: decoded, frames: height / width};
}
const textureCache = new Map();
function textureFor(reference) {
    while (reference.startsWith('#')) reference = model.textures[reference.slice(1)];
    if (textureCache.has(reference)) return textureCache.get(reference);
    const [namespace, resource] = reference.split(':');
    const filename = grey ? path.join(root, 'work/grey.png') : path.join(root, '运行资源/assets', namespace, 'textures', resource + '.png');
    const texture = readPng(filename);
    textureCache.set(reference, texture);
    return texture;
}
function normalize(vector) {
    const length = Math.hypot(...vector);
    return vector.map(value => value / length);
}
function cross(first, second) {
    return [first[1] * second[2] - first[2] * second[1], first[2] * second[0] - first[0] * second[2], first[0] * second[1] - first[1] * second[0]];
}
function dot(first, second) {
    return first.reduce((total, value, index) => total + value * second[index], 0);
}
const cameras = {front: [8, 8, -48], back: [8, 8, 64], left: [-48, 8, 8], right: [64, 8, 8], top: [8, 64, 8], underside: [8, -48, 8], hero: [37, 29, -35], rear_hero: [-25, 26, 40]};
const shade = {north: 0.8, south: 0.8, west: 0.6, east: 0.6, up: 1, down: 0.5};
const results = [];
for (const frame of frames) {
    for (const view of views) {
        const camera = cameras[view];
        if (!camera) throw new Error('Unknown view ' + view);
        const forward = normalize([8 - camera[0], 8 - camera[1], 8 - camera[2]]);
        const right = normalize(cross(forward, view === 'top' || view === 'underside' ? [0, 0, -1] : [0, 1, 0]));
        const up = cross(right, forward);
        const span = view.includes('hero') ? 13 : 10;
        const originAxes = [new Float64Array(size * size), new Float64Array(size * size), new Float64Array(size * size)];
        for (let row = 0; row < size; row++) {
            for (let column = 0; column < size; column++) {
                const horizontal = ((column + 0.5) / size * 2 - 1) * span;
                const vertical = (1 - (row + 0.5) / size * 2) * span;
                const pixel = row * size + column;
                for (let axis = 0; axis < 3; axis++) originAxes[axis][pixel] = camera[axis] + right[axis] * horizontal + up[axis] * vertical;
            }
        }
        const depth = new Float64Array(size * size).fill(Infinity);
        const output = Buffer.alloc(size * size * 4);
        for (let pixel = 0; pixel < size * size; pixel++) {
            output[pixel * 4] = colors[0];
            output[pixel * 4 + 1] = colors[1];
            output[pixel * 4 + 2] = colors[2];
            output[pixel * 4 + 3] = 255;
        }
        let culledFirstHits = 0;
        for (const element of model.elements) {
            if (element.rotation && element.rotation.angle) throw new Error('Rotated geometry is forbidden');
            const minimum = element.from;
            const maximum = element.to;
            const faceTextures = Object.fromEntries(Object.entries(element.faces).map(([name, face]) => [name, textureFor(face.texture)]));
            const screenHorizontal = [];
            const screenVertical = [];
            for (const horizontal of [minimum[0], maximum[0]]) for (const vertical of [minimum[1], maximum[1]]) for (const depthCoordinate of [minimum[2], maximum[2]]) {
                const relative = [horizontal - 8, vertical - 8, depthCoordinate - 8];
                screenHorizontal.push((dot(relative, right) / span + 1) * size / 2);
                screenVertical.push((1 - dot(relative, up) / span) * size / 2);
            }
            const firstColumn = Math.max(0, Math.floor(Math.min(...screenHorizontal)));
            const lastColumn = Math.min(size - 1, Math.ceil(Math.max(...screenHorizontal)));
            const firstRow = Math.max(0, Math.floor(Math.min(...screenVertical)));
            const lastRow = Math.min(size - 1, Math.ceil(Math.max(...screenVertical)));
            for (let row = firstRow; row <= lastRow; row++) {
                for (let column = firstColumn; column <= lastColumn; column++) {
                    const pixel = row * size + column;
                    let enter = -Infinity;
                    let exit = Infinity;
                    let entryAxis = -1;
                    let missed = false;
                    for (let axis = 0; axis < 3; axis++) {
                        const origin = originAxes[axis][pixel];
                        if (Math.abs(forward[axis]) < 1e-10) {
                            if (origin < minimum[axis] || origin > maximum[axis]) { missed = true; break; }
                        } else {
                            const first = (minimum[axis] - origin) / forward[axis];
                            const second = (maximum[axis] - origin) / forward[axis];
                            const near = Math.min(first, second);
                            if (near > enter) { enter = near; entryAxis = axis; }
                            exit = Math.min(exit, Math.max(first, second));
                        }
                    }
                    if (missed || enter > exit || enter < 0 || enter >= depth[pixel] - 1e-8) continue;
                    const faceName = entryAxis === 0 ? (forward[0] > 0 ? 'west' : 'east') : entryAxis === 1 ? (forward[1] > 0 ? 'down' : 'up') : (forward[2] > 0 ? 'north' : 'south');
                    const face = element.faces[faceName];
                    if (!face) { culledFirstHits++; continue; }
                    const hit = originAxes.map((coordinates, axis) => coordinates[pixel] + forward[axis] * enter);
                    let fractionHorizontal;
                    let fractionVertical;
                    if (entryAxis === 2) {
                        fractionHorizontal = (faceName === 'north' ? maximum[0] - hit[0] : hit[0] - minimum[0]) / (maximum[0] - minimum[0]);
                        fractionVertical = (maximum[1] - hit[1]) / (maximum[1] - minimum[1]);
                    } else if (entryAxis === 0) {
                        fractionHorizontal = (faceName === 'east' ? maximum[2] - hit[2] : hit[2] - minimum[2]) / (maximum[2] - minimum[2]);
                        fractionVertical = (maximum[1] - hit[1]) / (maximum[1] - minimum[1]);
                    } else {
                        fractionHorizontal = (hit[0] - minimum[0]) / (maximum[0] - minimum[0]);
                        fractionVertical = (faceName === 'up' ? hit[2] - minimum[2] : maximum[2] - hit[2]) / (maximum[2] - minimum[2]);
                    }
                    const texture = faceTextures[faceName];
                    const uv = face.uv;
                    const textureHorizontal = Math.max(0, Math.min(texture.width - 1, Math.floor((uv[0] + (uv[2] - uv[0]) * fractionHorizontal) / 16 * texture.width)));
                    const textureVertical = Math.max(0, Math.min(texture.width - 1, Math.floor((uv[1] + (uv[3] - uv[1]) * fractionVertical) / 16 * texture.width))) + (frame % texture.frames) * texture.width;
                    const textureIndex = (textureVertical * texture.width + textureHorizontal) * texture.channels;
                    if (texture.channels === 4 && texture.pixels[textureIndex + 3] < 128) continue;
                    const brightness = element.shade === false ? 1 : shade[faceName];
                    for (let channel = 0; channel < 3; channel++) output[pixel * 4 + channel] = Math.round(texture.pixels[textureIndex + channel] * brightness);
                    depth[pixel] = enter;
                }
            }
        }
        const filename = prefix + '_' + view + '_f' + String(frame).padStart(2, '0') + '.png';
        writePng(path.join(renderDirectory, filename), output, size, size);
        results.push({filename, sha256: require('node:crypto').createHash('sha256').update(output).digest('hex'), internal_culled_hits_skipped: culledFirstHits});
    }
}
const evidence = {model: modelPath, elements: model.elements.length, size, views, frames, results, source: 'Orthographic software raycast of actual exported Java cuboids and PNG UVs, nearest-pixel sampling; not image generation and not Minecraft client'};
fs.writeFileSync(path.join(root, 'work', 'software_' + prefix + '.json'), JSON.stringify(evidence, null, 2));
process.stdout.write(JSON.stringify({model: modelPath, rendered: results.length, source: evidence.source}) + '\n');
