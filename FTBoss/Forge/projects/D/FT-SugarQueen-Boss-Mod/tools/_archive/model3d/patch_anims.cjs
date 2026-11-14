/* 一次性补丁: 把 gen_defect_core.cjs 里那段巨型动画字面量换成 require('./defect_core_anims.cjs')
 * 用法: node patch_anims.cjs   (在 tools/model3d 下运行)
 * 用标记定位而不是整段匹配, 避免手抄 180 行出错。*/
const fs = require('fs');
const path = require('path');
const p = path.join(__dirname, 'gen_defect_core.cjs');
let s = fs.readFileSync(p, 'utf8');
const startMark = 'const A = `animation.${NAME}.`;';
const endMark = '/* ---------------- 6. 写出(含 glowmask) ---------------- */';
const i = s.indexOf(startMark);
const j = s.indexOf(endMark);
if (i < 0 || j < 0 || j < i) {
    console.error('找不到标记', { i, j });
    process.exit(1);
}
const replacement =
    '/* ---------------- 5. 动画 ----------------\n' +
    ' * 2026-09-11 重做: 动画数据搬到 defect_core_anims.cjs(手写关键帧, 30 骨骼全参与,\n' +
    ' * 新增 float_move / echo), 单独一个文件便于反复调姿势不用碰几何。 */\n' +
    "const anims = { format_version: '1.8.0', animations: require('./defect_core_anims.cjs') };\n\n";
s = s.slice(0, i) + replacement + s.slice(j);
fs.writeFileSync(p, s);
console.log(`已替换 ${j - i} 字符 -> require('./defect_core_anims.cjs')`);
