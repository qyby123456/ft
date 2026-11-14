const fs = require('node:fs');
const path = require('node:path');
const { makeCanvas } = require('./lib_itemtex.cjs');
const expansion = require('./boss_expansion_catalog.cjs');
const spells = [
  ['crystal_coronation', [160, 231, 255]], ['camellia_blood_bloom', [239, 101, 145]],
  ['vintage_vortex', [217, 149, 69]], ['capacitor_overload', [126, 220, 255]],
  ['prismatic_blight', [177, 232, 93]], ['gilded_verdict', [255, 220, 100]], ['tidal_edict', [80, 223, 201]],
  ['frostglass_lance', [210, 249, 255]], ['camellia_sanctuary', [255, 197, 217]],
  ['cask_rupture', [255, 147, 79]], ['repulsor_discharge', [176, 160, 255]],
  ['virulent_harvest', [220, 255, 120]], ['orchard_aegis', [255, 230, 168]], ['abyssal_geyser', [112, 192, 255]]];
spells.push(...expansion.spells.map(([id, color]) => [id, [color >> 16 & 255, color >> 8 & 255, color & 255]]));

function generate(assets, preview) {
  const directory = path.join(assets, 'textures/gui/spell_icons');
  fs.mkdirSync(directory, { recursive: true });
  const sheet = makeCanvas(7 * 128, Math.ceil(spells.length / 7) * 128);
  spells.forEach(([id, accent], index) => {
    const canvas = makeCanvas(64, 64);
    const bright = accent.map(value => Math.min(255, value * 0.4 + 155));
    const dark = accent.map(value => value * 0.25);
    for (let pixelY = 0; pixelY < 64; pixelY++) for (let pixelX = 0; pixelX < 64; pixelX++) {
      const distance = Math.hypot(pixelX - 31.5, pixelY - 31.5);
      if (distance > 31) continue;
      const edge = distance > 28.5;
      const noise = ((pixelX * 11 + pixelY * 17) % 9) / 9;
      canvas.put(pixelX, pixelY, edge ? accent.map(value => value * 0.5 + noise * 10)
        : [13 + noise * 5 + accent[0] * 0.03, 16 + noise * 5 + accent[1] * 0.03, 28 + noise * 7 + accent[2] * 0.03]);
    }
    function ellipse(centerX, centerY, radiusX, radiusY, color) {
      for (let pixelY = Math.floor(centerY - radiusY); pixelY <= centerY + radiusY; pixelY++) {
        for (let pixelX = Math.floor(centerX - radiusX); pixelX <= centerX + radiusX; pixelX++) {
          const distance = ((pixelX - centerX) / radiusX) ** 2 + ((pixelY - centerY) / radiusY) ** 2;
          if (distance <= 1) canvas.put(pixelX, pixelY, color.map(value => value * (0.62 + 0.38 * Math.sqrt(1 - distance))));
        }
      }
    }
    function line(startX, startY, endX, endY, width, color) {
      const steps = Math.ceil(Math.hypot(endX - startX, endY - startY) * 2);
      for (let step = 0; step <= steps; step++) ellipse(startX + (endX - startX) * step / Math.max(1, steps),
        startY + (endY - startY) * step / Math.max(1, steps), width, width, color);
    }
    function polygon(points, color) {
      for (let pixelY = 5; pixelY < 59; pixelY++) for (let pixelX = 5; pixelX < 59; pixelX++) {
        let inside = false;
        for (let current = 0, previous = points.length - 1; current < points.length; previous = current++) {
          const [currentX, currentY] = points[current];
          const [previousX, previousY] = points[previous];
          if ((currentY > pixelY) !== (previousY > pixelY)
              && pixelX < (previousX - currentX) * (pixelY - currentY) / (previousY - currentY) + currentX) inside = !inside;
        }
        if (inside) canvas.put(pixelX, pixelY, color.map(value => value * (0.8 + 0.2 * (64 - pixelY) / 64)));
      }
    }
    function arc(centerX, centerY, radius, begin, end, color, width = 1) {
      for (let step = 0; step < 100; step++) {
        const angle = begin + (end - begin) * step / 99;
        ellipse(centerX + Math.cos(angle) * radius, centerY + Math.sin(angle) * radius, width, width, color);
      }
    }
    if (index === 0) {
      polygon([[13, 24], [23, 34], [32, 11], [41, 34], [51, 24], [45, 48], [19, 48]], accent);
      line(21, 44, 43, 44, 1.5, bright);
      polygon([[32, 20], [36, 32], [32, 39], [28, 32]], bright);
      for (const column of [14, 49]) { line(column, 14, column, 20, 1, bright); line(column - 3, 17, column + 3, 17, 1, bright); }
    } else if (index === 1) {
      line(32, 34, 25, 52, 2, [71, 150, 97]);
      ellipse(39, 44, 9, 3, [94, 189, 109]);
      for (let petal = 0; petal < 6; petal++) {
        const angle = petal * Math.PI / 3;
        ellipse(32 + Math.cos(angle) * 9, 27 + Math.sin(angle) * 9, 8, 8, accent);
      }
      ellipse(32, 27, 7, 7, bright);
      ellipse(32, 27, 3, 3, [130, 16, 65]);
      polygon([[48, 38], [53, 48], [48, 54], [43, 48]], accent);
    } else if (index === 2) {
      ellipse(32, 31, 15, 20, accent);
      for (const column of [23, 31, 40]) line(column, 17, column, 45, 1, dark);
      line(19, 22, 45, 22, 2, bright); line(19, 40, 45, 40, 2, bright);
      arc(32, 35, 22, 0.2, 4.4, accent, 1.5);
      polygon([[11, 24], [11, 14], [20, 17]], bright);
    } else if (index === 3) {
      ellipse(32, 32, 13, 13, [73, 115, 176]); ellipse(32, 32, 7, 7, bright);
      polygon([[22, 9], [17, 26], [25, 24], [20, 42], [35, 21], [25, 23], [31, 9]], accent);
      arc(32, 32, 22, 0.3, 2.1, accent, 1.5); arc(32, 32, 22, 3.4, 5.2, accent, 1.5);
      for (const point of [[13, 43], [51, 21], [43, 51]]) ellipse(...point, 3, 3, bright);
    } else if (index === 4) {
      for (const [centerX, centerY, color] of [[21, 30, [226, 101, 160]], [43, 34, [118, 177, 255]], [32, 22, accent]]) {
        line(centerX, centerY, centerX, 48, 2, bright);
        ellipse(centerX, centerY, 10, 7, color);
        line(centerX - 8, centerY + 4, centerX + 8, centerY + 4, 1, dark);
      }
      for (const point of [[13, 17], [49, 16], [16, 48], [45, 49], [33, 54]]) ellipse(...point, 2, 2, accent);
    } else if (index === 5) {
      ellipse(25, 33, 12, 16, accent); ellipse(39, 33, 12, 16, accent);
      line(32, 20, 34, 11, 2, [137, 114, 70]); ellipse(41, 13, 7, 3, [134, 217, 121]);
      polygon([[33, 22], [36, 32], [46, 34], [36, 37], [33, 47], [30, 37], [20, 34], [30, 32]], bright);
      arc(32, 32, 24, 0.2, 2.9, accent); arc(32, 32, 24, 3.4, 5.9, accent);
    } else if (index === 6) {
      for (const height of [34, 42, 50]) {
        for (let column = 11; column <= 53; column++) ellipse(column, height + Math.sin(column * 0.15) * 3, 1.5, 1.5, accent);
      }
      line(32, 13, 32, 41, 2, bright); line(22, 17, 22, 28, 2, bright); line(42, 17, 42, 28, 2, bright);
      line(22, 28, 42, 28, 2, bright); polygon([[32, 8], [28, 17], [36, 17]], bright);
    } else if (index === 7) {
      line(16, 49, 41, 24, 2, accent);
      polygon([[20, 34], [49, 11], [43, 39], [36, 28]], bright);
      line(12, 37, 22, 47, 1, accent);
      polygon([[12, 17], [19, 20], [16, 28]], accent);
      polygon([[47, 44], [53, 47], [48, 53]], accent);
    } else if (index === 8) {
      arc(32, 33, 21, Math.PI, Math.PI * 2, accent, 2);
      line(11, 33, 11, 48, 2, accent); line(53, 33, 53, 48, 2, accent);
      for (let petal = 0; petal < 5; petal++) {
        const angle = petal * Math.PI * 2 / 5;
        ellipse(32 + Math.cos(angle) * 7, 31 + Math.sin(angle) * 7, 6, 6, accent);
      }
      line(32, 22, 32, 39, 2, bright); line(24, 31, 40, 31, 2, bright);
      arc(32, 37, 18, 0.1, 3, [102, 209, 151], 2);
    } else if (index === 9) {
      polygon([[12, 45], [16, 25], [24, 33], [30, 9], [39, 28], [48, 19], [52, 44], [33, 54]], accent);
      ellipse(32, 37, 10, 13, [127, 73, 40]);
      line(23, 30, 41, 30, 2, bright); line(23, 43, 41, 43, 2, bright);
      line(32, 29, 32, 46, 1, accent);
    } else if (index === 10) {
      for (const radius of [10, 18, 25]) arc(32, 32, radius, 0, Math.PI * 2, accent, 1.3);
      ellipse(32, 32, 6, 6, bright);
      for (const sign of [-1, 1]) polygon([[32 + sign * 27, 32], [32 + sign * 18, 25], [32 + sign * 18, 39]], bright);
    } else if (index === 11) {
      line(21, 50, 39, 18, 2, [154, 112, 70]);
      arc(34, 27, 15, 3.4, 6.5, bright, 3);
      polygon([[49, 27], [45, 42], [40, 34]], bright);
      polygon([[20, 21], [27, 34], [20, 41], [13, 34]], accent);
      ellipse(20, 32, 2, 2, bright);
    } else if (index === 12) {
      polygon([[12, 17], [32, 10], [52, 17], [48, 39], [32, 55], [16, 39]], accent);
      polygon([[18, 22], [32, 16], [46, 22], [42, 36], [32, 46], [22, 36]], dark);
      ellipse(28, 31, 6, 8, bright); ellipse(36, 31, 6, 8, bright);
      line(32, 23, 34, 19, 1, [126, 208, 101]);
    } else if (index === 13) {
      for (const offset of [-10, 0, 10]) line(32 + offset, 45, 32 + offset * 1.4, 20 + Math.abs(offset) * 0.4, 2, accent);
      polygon([[32, 10], [22, 25], [42, 25]], bright);
      arc(32, 43, 19, 0, Math.PI * 2, accent, 2);
      ellipse(32, 44, 10, 3, bright);
      for (const point of [[12, 22], [51, 22], [20, 13], [45, 12]]) ellipse(...point, 2, 3, accent);
    } else {
      expansion.paint(index - 14, { ellipse, line, polygon, arc, accent, bright, dark });
    }
    canvas.save(path.join(directory, id + '.png'));
    for (let pixelY = 0; pixelY < 128; pixelY++) for (let pixelX = 0; pixelX < 128; pixelX++) {
      const offset = (Math.floor(pixelY / 2) * 64 + Math.floor(pixelX / 2)) * 4;
      sheet.put((index % 7) * 128 + pixelX, Math.floor(index / 7) * 128 + pixelY, canvas.buf.subarray(offset, offset + 4));
    }
  });
  if (preview) { fs.mkdirSync(path.dirname(preview), { recursive: true }); sheet.save(preview); }
  console.log(`Generated ${spells.length} distinct boss spell icons: ${directory}`);
}

if (require.main === module) generate(path.resolve(process.argv[2] || path.join(__dirname, '../_archive/out/boss_spells/assets/ftboss')), process.argv[3]);
module.exports = { generate, spells };
