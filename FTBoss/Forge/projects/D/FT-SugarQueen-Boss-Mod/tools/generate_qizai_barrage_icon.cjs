const fs = require('node:fs');
const path = require('node:path');
const zlib = require('node:zlib');

function crc32(buffer) {
  let crc = 0xffffffff;
  for (const byte of buffer) {
    crc ^= byte;
    for (let bit = 0; bit < 8; bit++) crc = crc >>> 1 ^ (crc & 1 ? 0xedb88320 : 0);
  }
  return (crc ^ 0xffffffff) >>> 0;
}

function chunk(type, data) {
  const name = Buffer.from(type);
  const length = Buffer.alloc(4);
  const checksum = Buffer.alloc(4);
  length.writeUInt32BE(data.length);
  checksum.writeUInt32BE(crc32(Buffer.concat([name, data])));
  return Buffer.concat([length, name, data, checksum]);
}

const size = 32;
const rows = [];
for (let row = 0; row < size; row++) {
  const data = Buffer.alloc(size * 4 + 1);
  for (let column = 0; column < size; column++) {
    const horizontal = column - 15.5;
    const vertical = row - 15.5;
    const radius = Math.hypot(horizontal, vertical);
    let color = radius > 15 ? [0, 0, 0, 0] : [35, 16, 66, 255];
    if (radius > 13 && radius < 14.5) color = [197, 141, 255, 255];
    for (let index = 0; index < 6; index++) {
      const angle = index * Math.PI / 3;
      const along = horizontal * Math.cos(angle) + vertical * Math.sin(angle);
      const across = -horizontal * Math.sin(angle) + vertical * Math.cos(angle);
      if (along > 2 && along < 12 && Math.abs(across) < 1.4) color = [240, 85, 180, 255];
      if (along > 2 && along < 12 && Math.abs(across) < 0.55) color = [255, 237, 255, 255];
      if (Math.abs(along - 10) + Math.abs(across) * 0.8 < 2.6) color = [160, 86, 227, 255];
      if (Math.abs(along - 10) + Math.abs(across) * 0.8 < 1.1) color = [255, 228, 255, 255];
    }
    if (Math.abs(horizontal) + Math.abs(vertical) < 3) color = [255, 249, 255, 255];
    Buffer.from(color).copy(data, column * 4 + 1);
  }
  rows.push(data);
}
const header = Buffer.alloc(13);
header.writeUInt32BE(size, 0);
header.writeUInt32BE(size, 4);
header[8] = 8;
header[9] = 6;
const output = path.resolve(__dirname, '../src/main/resources/assets/ftboss/textures/gui/spell_icons/qizai_laser_barrage.png');
fs.writeFileSync(output, Buffer.concat([Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]), chunk('IHDR', header),
  chunk('IDAT', zlib.deflateSync(Buffer.concat(rows))), chunk('IEND', Buffer.alloc(0))]));
console.log('PASS 32px prism barrage spell icon');
