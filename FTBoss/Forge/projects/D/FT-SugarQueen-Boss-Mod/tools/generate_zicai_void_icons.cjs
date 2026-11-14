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

function png(size, pixel) {
  function chunk(type, data) {
    const name = Buffer.from(type);
    const length = Buffer.alloc(4);
    const checksum = Buffer.alloc(4);
    length.writeUInt32BE(data.length);
    checksum.writeUInt32BE(crc32(Buffer.concat([name, data])));
    return Buffer.concat([length, name, data, checksum]);
  }
  const header = Buffer.alloc(13);
  header.writeUInt32BE(size, 0);
  header.writeUInt32BE(size, 4);
  header[8] = 8;
  header[9] = 6;
  const rows = [];
  for (let row = 0; row < size; row++) {
    const data = Buffer.alloc(size * 4 + 1);
    for (let column = 0; column < size; column++) Buffer.from(pixel(column, row)).copy(data, column * 4 + 1);
    rows.push(data);
  }
  return Buffer.concat([Buffer.from([137, 80, 78, 71, 13, 10, 26, 10]), chunk('IHDR', header),
    chunk('IDAT', zlib.deflateSync(Buffer.concat(rows))), chunk('IEND', Buffer.alloc(0))]);
}

const output = process.argv[2] || path.resolve(__dirname, '../src/main/resources/assets/ftboss/textures/gui/spell_icons');
fs.mkdirSync(output, { recursive: true });
for (const fountain of [false, true]) {
  const file = fountain ? 'void_fountain_cascade' : 'faded_void_spiral';
  fs.writeFileSync(path.join(output, file + '.png'), png(32, (column, row) => {
    const horizontal = column - 15.5;
    const vertical = row - 15.5;
    const radius = Math.hypot(horizontal, vertical);
    if (radius > 15) return [0, 0, 0, 0];
    let white = radius > 13.4 && radius < 14.6;
    let gold = radius > 11.7 && radius < 12.6;
    if (fountain) {
      for (const offset of [-7, 0, 7]) {
        const height = offset === 0 ? 10 : 6;
        white ||= Math.abs(horizontal - offset) < 0.8 && vertical > -height && vertical < 7;
        gold ||= Math.abs(horizontal - offset) < 2 && vertical > -height + 2 && vertical < 8;
        white ||= Math.abs(Math.hypot((horizontal - offset) * 0.7, (vertical - 7) * 2) - 2) < 0.7;
      }
    } else {
      const heading = Math.atan2(vertical, horizontal);
      const spoke = Math.abs(Math.sin(heading * 4 + radius * 0.2));
      white ||= radius > 2 && radius < 11 && spoke < 0.15;
      gold ||= radius < 11 && spoke < 0.45;
      white ||= radius < 2;
    }
    return white ? [255, 247, 218, 255] : gold ? [239, 187, 75, 255] : [54, 37, 20, 240];
  }));
}
console.log('PASS generated both 32px spell icons');
