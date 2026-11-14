const path = require('node:path');
const dependencies = process.env.FT_NODE_MODULES || 'C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules';
const { PNG } = require(path.join(dependencies, 'pngjs'));
const colors = {
  ink: '#202233', dark: '#34394d', slate: '#535e78', steel: '#8cacc2', silver: '#c7deea', white: '#fff2dc',
  goldDark: '#926039', gold: '#d6a451', goldLight: '#ffe3a0', woodDark: '#493337', wood: '#875241', woodLight: '#ba8060',
  redDark: '#661e3c', red: '#b53251', coral: '#ef6370', rose: '#ffc2bb', pinkDark: '#93466d', pink: '#e78eb5', pinkLight: '#ffe0ed',
  greenDark: '#234d4b', green: '#438768', lime: '#8ac56f', mint: '#ccebaa', yellow: '#f2c74d', lemon: '#fff294',
  blueDark: '#213d65', blue: '#3e6aac', cyan: '#49bfd0', ice: '#b6f1f3', purpleDark: '#36264f', purple: '#77539b', lilac: '#b992de',
  orange: '#df8747', cream: '#e6d7b4', void: '#110f23'
};
function rgba(color) { return [...(colors[color] || color).slice(1).match(/../g).map(channel => parseInt(channel, 16)), 255]; }
class Pixels {
  constructor(size) { this.image = new PNG({ width: size, height: size }); this.size = size; }
  dot(horizontal, vertical, color) {
    if (horizontal < 0 || vertical < 0 || horizontal >= this.size || vertical >= this.size) return;
    this.image.data.set(rgba(color), (vertical * this.size + horizontal) * 4);
  }
  rect(left, top, width, height, color) {
    for (let vertical = top; vertical < top + height; vertical++) for (let horizontal = left; horizontal < left + width; horizontal++) this.dot(horizontal, vertical, color);
  }
  poly(points, color) {
    for (let vertical = 0; vertical < this.size; vertical++) for (let horizontal = 0; horizontal < this.size; horizontal++) {
      let inside = false;
      for (let current = 0, previous = points.length - 1; current < points.length; previous = current++) {
        const [currentX, currentY] = points[current]; const [previousX, previousY] = points[previous];
        if ((currentY > vertical + 0.5) !== (previousY > vertical + 0.5) && horizontal + 0.5 < (previousX - currentX) * (vertical + 0.5 - currentY) / (previousY - currentY) + currentX) inside = !inside;
      }
      if (inside) this.dot(horizontal, vertical, color);
    }
  }
  line(startX, startY, endX, endY, color, width = 1) {
    const steps = Math.max(Math.abs(endX - startX), Math.abs(endY - startY));
    for (let step = 0; step <= steps; step++) this.rect(Math.round(startX + (endX - startX) * step / Math.max(1, steps)), Math.round(startY + (endY - startY) * step / Math.max(1, steps)), width, width, color);
  }
  disk(centerX, centerY, radius, color) {
    for (let vertical = 0; vertical < this.size; vertical++) for (let horizontal = 0; horizontal < this.size; horizontal++) if ((horizontal - centerX) ** 2 + (vertical - centerY) ** 2 <= radius ** 2) this.dot(horizontal, vertical, color);
  }
  stamp(rows, left, top, palette, scale = 1) {
    rows.forEach((row, vertical) => [...row].forEach((pixel, horizontal) => { if (palette[pixel]) this.rect(left + horizontal * scale, top + vertical * scale, scale, scale, palette[pixel]); }));
  }
  outline(color = 'ink') {
    const before = Buffer.from(this.image.data);
    for (let vertical = 1; vertical < this.size - 1; vertical++) for (let horizontal = 1; horizontal < this.size - 1; horizontal++) {
      if (before[(vertical * this.size + horizontal) * 4 + 3]) continue;
      if ([[0, -1], [-1, 0], [1, 0], [0, 1]].some(([offsetX, offsetY]) => before[((vertical + offsetY) * this.size + horizontal + offsetX) * 4 + 3])) this.dot(horizontal, vertical, color);
    }
  }
  buffer() { return PNG.sync.write(this.image); }
}
const glyphs = {
  crown: ['#..#..#', '#.###.#', '#######', '.#####.', '.#####.'],
  heart: ['.##.##.', '#######', '#######', '.#####.', '..###..', '...#...'],
  leaf: ['.....##', '...####', '..#####', '.#####.', '.####..', '.#.....', '#......'],
  glass: ['#######', '#.....#', '.#####.', '..###..', '...#...', '...#...', '.#####.'],
  cheese: ['......#', '....###', '..##.##', '#######', '##.####', '#####.#', '#######'],
  chip: ['.#.#.#.', '.#####.', '##...##', '.#.#.#.', '##...##', '.#####.', '.#.#.#.'],
  apple: ['....#..', '.##.##.', '#######', '#######', '#######', '.#####.', '..#.#..'],
  paw: ['.#...#.', '###.###', '.#.#.#.', '..###..', '.#####.', '.#####.', '..#.#..'],
  star: ['...#...', '...#...', '#######', '.#####.', '..###..', '.##.##.', '.#...#.'],
  moon: ['...###.', '..##...', '.##....', '.##....', '.##..#.', '..####.', '...##..'],
  axe: ['..#.#..', '#####..', '#####..', '..#.##.', '..#..##', '..#...#', '..#....'],
  cat: ['##...##', '###.###', '#######', '#.#.#.#', '#######', '.##.##.', '..###..'],
  trident: ['#..#..#', '#..#..#', '#######', '...#...', '...#...', '...#...', '..###..'],
  wheel: ['..###..', '.#.#.#.', '#..#..#', '#######', '#..#..#', '.#.#.#.', '..###..'],
  root: ['.#...#.', '..#.#..', '...#...', '..###..', '.#.#.#.', '#..#..#', '...#...'],
  fish: ['...#...', '.#####.', '.#.#.#.', '#######', '.#####.', '..###..', '...#...'],
  chef: ['.##.##.', '#######', '#######', '.#####.', '.#####.', '.#####.', '.#####.'],
  sprout: ['##...##', '###.###', '.##.##.', '..###..', '...#...', '...#...', '.#####.'],
  tower: ['#.#.#.#', '#######', '.#####.', '.##.##.', '.#####.', '.##.##.', '.##.##.'],
  swords: ['#.....#', '.#...#.', '..#.#..', '...#...', '..#.#..', '.#...#.', '#.....#'],
  dual: ['.##.##.', '#.#.#.#', '#.#.#.#', '.##.##.', '..#.#..', '.#...#.', '#.....#'],
  mask: ['#######', '#.....#', '##.#.##', '#.....#', '.#.#.#.', '..###..', '...#...']
};
module.exports = { dependencies, colors, rgba, Pixels, glyphs };
