/* 召唤物 / 掉落物 图标重绘(2026-09-12 按用户要求"优化召唤物品贴图")
 *
 * 原来 compost_summon / rainbow_fertilizer 就是几条纯色横杠(灰桶 + 三色条), 没有轮廓、
 * 没有明暗、认不出是什么。这里全部改成**手绘 16x16 像素图**: 深色描边 + 左上高光 + 右下暗部,
 * 保证在物品栏小尺寸下也有立体感和辨识度。
 *
 * 用法: node gen_summon_icons.cjs <resourcesRoot>
 */
const path = require('path');
const { Canvas } = require('./png.cjs');
const RES = process.argv[2];
const redrawIds = new Set(require('./summons_icons_v1/spec.cjs').summons.map(spec => spec.id));

/* 调色板: 每个图标自带的颜色 + 通用描边/高光/暗部 */
const P = {
  '.': null,
  K: [24, 18, 16],        // 描边(暖黑)
  k: [58, 44, 36],        // 描边浅一档(内部线)
  W: [240, 240, 232],     // 高光
  w: [196, 196, 188],     // 次高光
  // 桶/金属
  M: [152, 158, 166], m: [104, 110, 118], d: [70, 74, 82],
  // 肥料/麻袋
  B: [168, 126, 74], b: [126, 90, 50], n: [88, 60, 32],
  // 彩虹
  R: [220, 62, 62], O: [232, 138, 44], Y: [238, 206, 62], G: [96, 190, 84],
  C: [76, 178, 214], U: [132, 104, 214], V: [206, 96, 176],
  // 肉/心
  r: [186, 44, 52], p: [232, 130, 138],
  // 茶
  t: [74, 150, 72], T: [46, 104, 48], s: [150, 108, 62], S: [104, 72, 40],
  // 酒
  g: [64, 168, 156], G2: [34, 110, 104], q: [226, 226, 210],
  // 奶酪 / 鼠饵
  c: [232, 196, 92], e: [196, 152, 52], f: [150, 108, 34],
  // 机器人核心
  x: [92, 96, 108], X: [56, 60, 70], z: [168, 172, 184],
  v: [178, 122, 226], V2: [120, 70, 180],
  // 糖霜女皇水晶
  i: [232, 190, 226], I: [196, 132, 190], J: [150, 88, 148],
};

const ART = {
  /* 炫彩堆肥桶: 提手 + 桶沿 + 溢出的彩虹堆肥 + 金属桶身 */
  compost_summon: [
    '................',
    '.....KKKKKK.....',
    '....K......K....',
    '..KKKKKKKKKKKK..',
    '..KWWWWWWWWWWK..',
    '..KRRRROOOOYYYK.',
    '..KYYYGGGGCCCUK.',
    '..KUUUVVVVRRRRK.',
    '..KKKKKKKKKKKKK.',
    '..KMWmMWmMWmMmK.',
    '..KMWmMWmMWmMmK.',
    '..KMWmMWmMWmMmK.',
    '..KMmmmmmmmmmmK.',
    '..KKKKKKKKKKKKK.',
    '...K..........K.',
    '................',
  ],
  /* 炫彩肥料: 扎口的麻袋 + 中间一道彩虹 + 高光 */
  rainbow_fertilizer: [
    '................',
    '......KKKK......',
    '.....KnnnnK.....',
    '....KnBBBBnK....',
    '...KKKKKKKKKK...',
    '..KBBBBBBBBBBK..',
    '..KBWWBBBBBBbK..',
    '..KBRROOYYGGCK..',
    '..KBUUVVRROOgK..',
    '..KBBWWBBBBBBK..',
    '..KBBBBBBBBBBK..',
    '..KbBBBBBBBBbK..',
    '..KbbbbbbbbbbK..',
    '..KKKKKKKKKKKK..',
    '................',
    '................',
  ],
  /* 糖霜女皇水晶: 粉色水晶, 左上高光 + 内部亮线 */
  sugar_queen_crystal: [
    '................',
    '........KKK.....',
    '.......KiIiK....',
    '......KiWiIK....',
    '.....KiWiIiIK...',
    '....KiWiIiIIK...',
    '...KiWiIiIIIK...',
    '..KiWiIiIIIIK...',
    '..KiWiIiIIIJK...',
    '..KiIiIiIIJJK...',
    '..KIiIiIIJJJK...',
    '...KIIiIIJJK....',
    '....KIIIJJJK....',
    '.....KKJJKK.....',
    '.......KK.......',
    '................',
  ],
  /* 肉心: 生肉质感的心形 */
  meat_heart: [
    '................',
    '...KKK....KKK...',
    '..KrWrK..KrWrK..',
    '.KrWWrrKKrrWWrK.',
    '.KrWrrrrrrrrrrK.',
    '.KrrrrppprrrrrK.',
    '.KrrrppppprrrrK.',
    '.KrrrrppprrrrrK.',
    '..KrrrrrrrrrrK..',
    '..KrrrrrrrrrrK..',
    '...KrrrrrrrrK...',
    '....KrrrrrrK....',
    '.....KrrrrK.....',
    '......KrrK......',
    '.......KK.......',
    '................',
  ],
  /* 茶种: 一颗种子 + 嫩芽 + 小红花 */
  tea_seed: [
    '................',
    '.......KK.......',
    '......KRRK......',
    '.....KRWWRK.....',
    '.....KRRRRK.....',
    '......KttK......',
    '....KKKttKKK....',
    '..KKtTTttTTtKK..',
    '..KtTttWWttTtK..',
    '..KtttWWWWtttK..',
    '...KttttttttK...',
    '....KKKKKKKK....',
    '....KsSSSSsK....',
    '....KsWWSssK....',
    '....KssssssK....',
    '.....KKKKKK.....',
  ],
  /* 酒符: 青绿玻璃瓶 + 软木塞 + 高光 */
  wine_charm: [
    '................',
    '......KKKK......',
    '......KnnK......',
    '......KnnK......',
    '.....KKnnKK.....',
    '.....KgGGgK.....',
    '....KggGGggK....',
    '...KgWggggGgK...',
    '..KgWWggggGgGK..',
    '..KgWgggggGGK...',
    '..KgggggggGGK...',
    '..KggggggGGGK...',
    '..KGggggGGGGK...',
    '...KGGGGGGGK....',
    '....KKKKKKK.....',
    '................',
  ],
  /* 鼠饵: 带孔奶酪角 */
  rat_bait: [
    '................',
    '...........KKK..',
    '.........KKcccK.',
    '.......KKcWWccK.',
    '......KccWccccK.',
    '.....KccccceecK.',
    '....KccceeecccK.',
    '...KccceeecccK..',
    '..KcccceeccccK..',
    '..KccccccccccK..',
    '..KcceeccceecK..',
    '..KcceeccceecK..',
    '...KccccccccK...',
    '....KccccccK....',
    '.....KKKKKK.....',
    '................',
  ],
  /* 故障机器人: 金属核心, 中间紫色发光方形 */
  defect_core: [
    '................',
    '...KKKKKKKKKK...',
    '..KxzzzzzzzzxK..',
    '..KzXXXXXXXXzK..',
    '..KzXvvvvvvXzK..',
    '..KzXvVVVVvXzK..',
    '..KzXvVWWVvXzK..',
    '..KzXvVWWVvXzK..',
    '..KzXvVVVVvXzK..',
    '..KzXvvvvvvXzK..',
    '..KzXXXXXXXXzK..',
    '..KxzzzzzzzzxK..',
    '...KKKKKKKKKK...',
    '....KxXXXXxK....',
    '.....KKKKKK.....',
    '................',
  ],
};

const OUT = path.join(RES, 'assets/ftboss/textures/item');
let made = 0;
for (const name of Object.keys(ART)) {
  if (redrawIds.has(name)) continue;
  const rows = ART[name];
  const cv = new Canvas(16, 16);
  for (let y = 0; y < 16; y++) {
    const line = rows[y] || '................';
    for (let x = 0; x < 16; x++) {
      const c = P[line[x] || '.'];
      if (!c) continue;
      cv.set(x, y, c[0], c[1], c[2], 1);
    }
  }
  cv.save(path.join(OUT, name + '.png'));
  made++;
}
console.log('召唤物/掉落物图标已重绘: ' + made + ' 个 (16x16, 描边+高光+暗部)');
require('./summons_icons_v1/generate.cjs').generate(['summons'], RES);
