const fs = require('node:fs');
const path = require('node:path');
const { Canvas, decodePNG } = require('./model3d/png.cjs');
const root = path.resolve(__dirname, '..');
const target = path.join(root, 'src/main/resources/assets/ftboss/textures/mob_effect');
const patterns = {
  armor_break: ['....######....', '...########...', '...###.####...', '...##..####...', '...###.####...', '...####.###...', '....###..#....', '.....####.....', '......##......'],
  bone_fracture: ['..##......##..', '.####....####.', '..####..####..', '....###.##....', '.....#.##.....', '....##.###....', '..####..####..', '.####....####.', '..##......##..'],
  grievous_wound: ['..##..##..##..', '...##.##.##...', '...##.##.##...', '....#.##.#....', '....#.##.#....', '...##.##.##...', '...##.##.##...', '..##..##..##..', '......##......'],
  stun: ['......##......', '...#.####.#...', '....######....', '..##########..', '....######....', '...#.####.#...', '......##......', '..............', '..##......##..'],
  rooted: ['...########...', '..##......##..', '..##..##..##..', '..##########..', '......##......', '..##########..', '..##..##..##..', '..##......##..', '...########...'],
  ignite: ['.......#......', '......##......', '...#..###.....', '...##.###.....', '..#########...', '..#########...', '..####.####...', '...##...##....', '....#####.....'],
  airborne: ['......##......', '.....####.....', '....######....', '...########...', '......##......', '......##......', '..##..##..##..', '...########...', '....######....'],
  flickering_darkness: ['....######....', '..##########..', '.###......###.', '###..####..###', '.##..####..##.', '..##......##..', '....######....', '...#..#..#....', '..#...#...#...'],
  valor: ['......##......', '......##......', '..##########..', '..##########..', '......##......', '......##......', '...##.##.##...', '....######....', '.....####.....'],
};
const colors = [[184,190,199],[214,198,161],[226,68,86],[240,210,86],[146,181,100],[255,155,53],[162,212,229],[174,131,219],[250,205,89]];
fs.mkdirSync(target, { recursive: true });
const preview = new Canvas(9 * 144, 144);
Object.entries(patterns).forEach(([name, rows], index) => {
  const icon = new Canvas(18, 18);
  const color = colors[index];
  for (let row = 0; row < rows.length; row++) {
    for (let column = 0; column < rows[row].length; column++) {
      if (rows[row][column] !== '#') continue;
      icon.rect(column + 2, row + 5, 1, 1, ...color, 1);
      if (row === 0 || rows[row - 1][column] !== '#') icon.rect(column + 2, row + 4, 1, 1, ...color.map(value => Math.floor(value * 0.4)), 1);
    }
  }
  const file = path.join(target, name + '.png');
  icon.save(file);
  const decoded = decodePNG(file);
  if (decoded.w !== 18 || decoded.h !== 18) throw new Error('Invalid status icon');
  for (let row = 0; row < 18; row++) for (let column = 0; column < 18; column++) {
    const offset = (row * 18 + column) * 4;
    preview.rect(index * 144 + column * 8, row * 8, 8, 8, icon.d[offset], icon.d[offset + 1], icon.d[offset + 2], icon.d[offset + 3] / 255);
  }
});
const output = process.argv[2] || path.join(root, '_work/shared-combat-statuses-20261026/icons.png');
fs.mkdirSync(path.dirname(output), { recursive: true });
preview.save(output);
console.log('PASS nine 18x18 status icons: ' + output);
