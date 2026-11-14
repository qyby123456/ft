const fs = require('node:fs');
const path = require('node:path');
const { makeCanvas } = require('./lib_itemtex.cjs');
const spells = ['qizai_star_volley', 'qizai_starfall', 'qizai_crimson_prayer', 'qizai_starstep'];
const assets = path.resolve(process.argv[2] || path.join(__dirname, '../../src/main/resources/assets/ftboss'));
const output = path.join(assets, 'textures/gui/spell_icons');
fs.mkdirSync(output, { recursive: true });
const preview = makeCanvas(512, 128);
for (const [index, id] of spells.entries()) {
    const canvas = makeCanvas(64, 64);
    const pink = [245, 120, 180];
    const white = [255, 234, 248];
    const silver = [176, 162, 209];
    const dot = (centerX, centerY, radius, color) => {
        for (let pixelY = Math.floor(centerY - radius); pixelY <= centerY + radius; pixelY++) {
            for (let pixelX = Math.floor(centerX - radius); pixelX <= centerX + radius; pixelX++) {
                if (Math.hypot(pixelX - centerX, pixelY - centerY) <= radius) canvas.put(pixelX, pixelY, color);
            }
        }
    };
    const line = (startX, startY, endX, endY, color, width = 0.8) => {
        const count = Math.max(1, Math.ceil(Math.hypot(endX - startX, endY - startY) * 2));
        for (let point = 0; point <= count; point++) dot(startX + (endX - startX) * point / count,
            startY + (endY - startY) * point / count, width, color);
    };
    const ring = (centerX, centerY, radiusX, radiusY, color) => {
        for (let point = 0; point < 160; point++) {
            const angle = point * Math.PI * 2 / 160;
            dot(centerX + Math.cos(angle) * radiusX, centerY + Math.sin(angle) * radiusY, 0.8, color);
        }
    };
    const crystal = (centerX, centerY, size) => {
        for (let offsetY = -size; offsetY <= size; offsetY++) {
            for (let offsetX = -size / 2; offsetX <= size / 2; offsetX++) {
                if (Math.abs(offsetY) / size + Math.abs(offsetX) / (size / 2) <= 1)
                    canvas.put(Math.round(centerX + offsetX), Math.round(centerY + offsetY), offsetX < 0 ? white : pink);
            }
        }
    };
    for (let pixelY = 0; pixelY < 64; pixelY++) for (let pixelX = 0; pixelX < 64; pixelX++) {
        const distance = Math.hypot(pixelX - 31.5, pixelY - 31.5);
        if (distance <= 31) canvas.put(pixelX, pixelY, distance > 29 ? silver : [28, 19, 40]);
    }
    if (index === 0) {
        for (const [centerX, centerY] of [[21, 22], [35, 17], [46, 30]]) {
            line(centerX - 12, centerY + 21, centerX - 2, centerY + 5, pink, 1);
            crystal(centerX, centerY, 9);
        }
    } else if (index === 1) {
        ring(32, 46, 22, 9, pink);
        ring(32, 46, 15, 5, white);
        line(32, 8, 32, 25, silver);
        crystal(32, 30, 15);
        line(12, 25, 12, 36, pink);
        line(52, 25, 52, 36, pink);
    } else if (index === 2) {
        ring(32, 32, 24, 24, silver);
        for (const [centerX, centerY] of [[16, 38], [32, 20], [48, 38]]) {
            ring(centerX, centerY + 10, 9, 3, pink);
            crystal(centerX, centerY, 9);
        }
        line(16, 47, 32, 28, white);
        line(32, 28, 48, 47, white);
        line(48, 47, 16, 47, pink);
    } else {
        for (const [startX, startY, endX, endY] of [[9, 23, 30, 23], [13, 32, 39, 32], [9, 41, 27, 41]]) {
            line(startX, startY, endX, endY, pink, 1.2);
        }
        crystal(45, 28, 13);
        ring(41, 47, 14, 5, silver);
        line(39, 50, 47, 45, white);
    }
    canvas.save(path.join(output, id + '.png'));
    for (let pixelY = 0; pixelY < 128; pixelY++) for (let pixelX = 0; pixelX < 128; pixelX++) {
        const offset = (Math.floor(pixelY / 2) * 64 + Math.floor(pixelX / 2)) * 4;
        preview.put(index * 128 + pixelX, pixelY, Array.from(canvas.buf.slice(offset, offset + 4)));
    }
}
if (process.argv[3]) preview.save(path.resolve(process.argv[3]));
console.log(`Generated ${spells.length} Qizai spell icons`);
