const fs = require('node:fs');
const { decodePNG, encodePNG } = require('./png.cjs');
const texture = decodePNG(process.argv[2]);
if (texture.w !== 64 || texture.h !== 64) throw new Error('Qizai reference must be a 64x64 skin');
const overlays = [[32, 0, 32, 16], [0, 32, 56, 16], [0, 48, 16, 16], [48, 48, 16, 16]];
let transparent = 0;
for (const [left, top, width, height] of overlays) {
    for (let row = top; row < top + height; row++) for (let column = left; column < left + width; column++) {
        const offset = (row * 64 + column) * 4;
        const colors = Array.from(texture.data.subarray(offset, offset + 3));
        if (Math.min(...colors) >= 225 && Math.max(...colors) - Math.min(...colors) <= 24) {
            texture.data[offset + 3] = 0;
            transparent++;
        }
    }
}
fs.writeFileSync(process.argv[3], encodePNG(64, 64, texture.data));
console.log('Converted JPEG reference to PNG and restored ' + transparent + ' blank overlay pixels to transparency; base skin unchanged.');
