/* 一次性补丁: 把 gen_defect_core.cjs 里内联的 PAINT(按材质刷噪点) 换成
 * require('./defect_core_paint.cjs')(逐部件绘制)。用标记定位, 避免手抄整段出错。 */
const fs = require('fs');
const path = require('path');
const p = path.join(__dirname, 'gen_defect_core.cjs');
let s = fs.readFileSync(p, 'utf8');
const startMark = '/* ---------------- 1b.';
const endMark = '/* ---------------- 2. 装箱器 ---------------- */';
const i = s.indexOf(startMark);
const j = s.indexOf(endMark, i + 1);
if (i < 0 || j < 0 || j < i) {
    console.error('找不到标记', { i, j });
    process.exit(1);
}
const replacement =
    '/* ---------------- 1b. 贴图绘制(逐部件) ----------------\n' +
    ' * 2026-09-11 按用户要求"逐个贴图绘制": 绘制逻辑独立成 defect_core_paint.cjs,\n' +
    ' * 每个部件画设计好的图形(甲片包边/包角、眼球虹膜环+高光弧、披风折痕与下摆金缝线、掌心发光垫、散热格栅…)。\n' +
    ' * 需要读"当前球种的核心配色", 所以传 getCore 闭包。 */\n' +
    "const PAINT = require('./defect_core_paint.cjs').makePaint({ M, cv, nz, getCore: () => [coreRgb, coreCrackRgb] });\n\n";
s = s.slice(0, i) + replacement + s.slice(j);
fs.writeFileSync(p, s);
console.log(`已替换 ${j - i} 字符 -> require('./defect_core_paint.cjs')`);
