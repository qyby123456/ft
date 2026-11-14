/* v4: 去掉模型里的土豆加农炮方块(改为代码里手持 create:potato_cannon 道具)
 *  - 在 Node 里以 UTF-8 读写, 避免 shell 编码破坏
 *  - 只删 cube('cannon', ...) 的整行; cannon 骨骼保留(空骨骼) -> 8 条动画仍有效
 *  - 删完直接跑生成器
 * 用法: node make_compost_v4.cjs <项目根>
 */
const fs = require('fs');
const path = require('path');
const { spawnSync } = require('child_process');
const ROOT = process.argv[2];
const f = path.join(ROOT, 'tools/model3d/gen_compost_boss_v3.cjs');
let s = fs.readFileSync(f, 'utf8').replace(/^\uFEFF/, '');
const lines = s.split('\n');
const kept = lines.filter(l => !l.trim().startsWith("cube('cannon',"));
fs.writeFileSync(f, kept.join('\n'));
console.log(`删除 cannon 方块行: ${lines.length - kept.length}`);
/* 顺手确认 cannon 骨骼还在(空骨骼) */
const hasBone = kept.some(l => l.includes("cannon:"));
console.log(`cannon 骨骼保留: ${hasBone ? '是(空骨骼, 动画兼容)' : '否 ⚠'}`);
const g = spawnSync('node', ['gen_compost_boss_v3.cjs', 'out_compost_boss'], {
    cwd: path.join(ROOT, 'tools/model3d'), stdio: 'inherit'
});
console.log('生成器退出码: ' + g.status);
