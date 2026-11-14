'use strict';

const { makeCanvas } = require('../lib_itemtex.cjs');
const palette = {
  brass: [181, 141, 64], edge: [227, 188, 103], bronze: [105, 78, 43],
  iron: [57, 67, 72], joint: [33, 43, 51], steel: [108, 128, 130],
  jade: [38, 119, 106], lining: [27, 67, 65], seam: [69, 160, 132],
  eye: [64, 204, 231], white: [200, 251, 246], core: [98, 220, 236]
};

function atlas(size, density = 1.5) {
  const rectangles = [];
  let cursorX = 2, cursorY = 2, rowHeight = 0;
  function face(width, height, material, name) {
    const pixelsWide = Math.max(1, Math.ceil(width * density));
    const pixelsHigh = Math.max(1, Math.ceil(height * density));
    if (cursorX + pixelsWide + 2 > size) {
      cursorX = 2; cursorY += rowHeight + 3; rowHeight = 0;
    }
    if (cursorY + pixelsHigh + 2 > size) throw new Error('Texture atlas overflow');
    const rectangle = { left: cursorX, top: cursorY, width: pixelsWide, height: pixelsHigh, material, name };
    rectangles.push(rectangle);
    cursorX += pixelsWide + 3;
    rowHeight = Math.max(rowHeight, pixelsHigh);
    return { uv: [rectangle.left, rectangle.top], uv_size: [pixelsWide, pixelsHigh] };
  }
  function uv(dimensions, material, name) {
    const [width, height, depth] = dimensions;
    return Object.fromEntries(Object.entries({ north: [width, height], south: [width, height], east: [depth, height], west: [depth, height], up: [width, depth], down: [width, depth] })
      .map(([direction, extent]) => [direction, face(...extent, material, name + '_' + direction)]));
  }
  function paint(tint = palette.core) {
    const base = makeCanvas(size, size), glow = makeCanvas(size, size);
    for (const rectangle of rectangles) {
      const { left, top, width, height, material, name } = rectangle;
      const color = material === 'core' ? tint : palette[material];
      for (let row = -1; row <= height; row++) for (let column = -1; column <= width; column++) {
        const pixelX = Math.max(0, Math.min(width - 1, column));
        const pixelY = Math.max(0, Math.min(height - 1, row));
        const border = pixelX === 0 || pixelX === width - 1 || pixelY === 0 || pixelY === height - 1;
        let brightness = 0.86 + (1 - pixelY / Math.max(1, height - 1)) * 0.18;
        if (border && Math.min(width, height) > 3) brightness *= pixelY === 0 ? 1.16 : 0.72;
        if (['brass', 'iron', 'steel', 'bronze', 'edge'].includes(material)) {
          if (pixelX === 1 && width > 4) brightness += 0.17;
          if (width > 6 && height > 6 && pixelY === 2 && (pixelX === 2 || pixelX === width - 3)) brightness = 1.32;
          if (/shell|plate|bracer/.test(name) && width > 7 && pixelY === height - 3 && pixelX > width / 2) brightness *= 0.7;
        }
        if (['jade', 'lining', 'seam'].includes(material)) {
          brightness *= [0.86, 0.98, 1.12, 1.03][Math.floor(pixelX / 3) % 4];
          if (pixelY === height - 2 && pixelX % 3 !== 0) brightness *= 1.2;
        }
        if (['eye', 'core', 'white'].includes(material)) {
          const distance = Math.hypot((pixelX + 0.5) / width - 0.4, (pixelY + 0.5) / height - 0.38);
          brightness = distance < 0.23 ? 1.26 : distance < 0.42 ? 1.03 : 0.68;
        }
        const painted = color.map(channel => channel * brightness);
        base.put(left + column, top + row, painted);
        if (['eye', 'core', 'white'].includes(material)) glow.put(left + column, top + row, painted);
      }
    }
    return { base, glow };
  }
  return { size, uv, paint, rectangles };
}

module.exports = { atlas, palette };
