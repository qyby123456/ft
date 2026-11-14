// 把五首 Boss BGM 的 ogg(游戏用) + wav(母带) 导出到两个桌面成品目录
// 用法: node export_bgm.cjs
// 说明: 只复制/覆盖音频文件, 不动 曲目说明.txt(那份由人手写)
const fs = require('fs');
const path = require('path');

const root = path.resolve(__dirname, '..', '..');
const resMusic = path.join(root, 'src/main/resources/assets/ftboss/sounds/music');
const outDir = path.join(__dirname, 'out');
const dests = [
  'C:/Users/YOUR_USERNAME/Desktop/FT-Boss-BGM',
  'C:/Users/YOUR_USERNAME/OneDrive/桌面/FT-Boss-BGM'
];

/** 曲目: [源文件基名, 导出用中文名] */
const TRACKS = [
  ['saya', '沙耶-茶の唄-v6'],
  ['gingerbread', '姜饼王-木偶剧场加洛普-v5'],
  ['butcher', '屠夫-肉山祭坛-v1'],
  ['tavern', '酒桶王-醉乡酒馆-v1'],
  ['rat', '鼠群-鼠潮夜行-v1']
];

let ok = 0, miss = [];
for (const d of dests) {
  fs.mkdirSync(d, { recursive: true });
  for (const [base, cn] of TRACKS) {
    const ogg = path.join(resMusic, base + '.ogg');
    const wav = path.join(outDir, base + '.wav');
    if (fs.existsSync(ogg)) { fs.copyFileSync(ogg, path.join(d, cn + '.ogg')); ok++; }
    else miss.push(base + '.ogg');
    if (fs.existsSync(wav)) { fs.copyFileSync(wav, path.join(d, cn + '.wav')); ok++; }
    else miss.push(base + '.wav(保留目录里已有的一份)');
  }
  console.log('[OK] ' + d);
}
console.log(`复制 ${ok} 个文件; 缺失: ${[...new Set(miss)].join(', ') || '无'}`);
for (const d of dests) {
  console.log('\n' + d);
  for (const f of fs.readdirSync(d).sort()) {
    console.log(`  ${f.padEnd(34)} ${fs.statSync(path.join(d, f)).size.toLocaleString()} B`);
  }
}
