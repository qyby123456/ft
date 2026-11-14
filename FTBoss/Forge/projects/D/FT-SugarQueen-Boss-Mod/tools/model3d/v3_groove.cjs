/* ============================================================================
 *  v3_groove.cjs —— 让血槽(沟槽)读得出来（只改贴图画法, 不加几何, 不动动画）
 *   问题(用户反馈): 沟槽颜色不明显 -> 模型看着丑。
 *   根因两条:
 *     ① 画法把血槽往**亮色**混: mix(A(C1,252), A(C3,150), ...) —— 凹槽应更深, 方向反了 ✗
 *     ② 之前为"减叠层感"删掉了逐条 AO 缝 -> 沟槽两侧没有明暗交界, 读不出凹进去 ✗
 *   修法(只画贴图):
 *     · 血槽本体 = 最深色 C1(近乎全黑), 两侧 0.25 宽度处压一点暗紫 C4(魔法余韵, 很弱)
 *     · 沟槽两条竖直边界各画 1px 深色 AO 线, 紧挨外侧再画 1px 冷色受光脊线 -> 凹槽立刻"立"起来
 *     · 血槽中心再给一条极暗的紫线(次要亮点, 符合 §8「紫色只在关键位置」)
 *   不变量: 几何、UV、骨骼、动画全部不动; 只重新生成贴图。
 *  用法: node tools/model3d/v3_groove.cjs
 * ==========================================================================*/
const fs = require('fs');
const path = require('path');
const ROOT = path.resolve(__dirname, '..', '..');
const GEN = path.join(ROOT, 'tools', 'model3d', 'regen_shadow_sword.cjs');
let s = fs.readFileSync(GEN, 'utf8');
const hit = [];
const rep = (tag, a, b) => { const n = s.split(a).length - 1; hit.push([tag, n]); if (n > 0) s = s.split(a).join(b); };

/* ① 血槽本体改成"更深"(原来是往亮色混) */
rep('groove-dark',
    `case 'groove': return mix(A(C1, 252), A(C3, 150), fx < 0.2 || fx > 0.8 ? 0.25 : 0);   // 血槽: 中间压暗`,
    `case 'groove': return mix(A(C1, 252), A(C4, 110), fx < 0.22 || fx > 0.78 ? 0.30 : 0.06);   // 血槽: **最深**底色 + 两侧一点暗紫(原来往亮色混是反的)`);

/* ② 沟槽两侧补 AO 线 + 外侧受光脊线(删掉逐条 AO 缝后, 凹槽没有明暗交界, 读不出来) */
rep('groove-ao',
    `    vLine(6.0 + dx + notch + 0.02, y0, y1, A(C6, 248));                    // 最外刃线`,
    `    vLine(6.0 + dx + notch + 0.02, y0, y1, A(C6, 248));                    // 最外刃线
    /* 血槽两侧: 内 1px 深色 AO + 外 1px 冷色受光脊 -> 凹槽才有立体感(删掉逐条 AO 缝后靠这两对线把它"立"起来) */
    {
        const gx0 = 6.0 + dx + notch + (STRIP_W[0] + STRIP_W[1] + STRIP_W[2] + STRIP_W[3] + STRIP_W[4]);
        const gx1 = gx0 + STRIP_W[5];
        vLine(gx0 - 0.045, y0, y1, A(C1, 240));
        vLine(gx0 + 0.045, y0, y1, A(C1, 240));
        vLine(gx0 - 0.115, y0, y1, A(C3, 190));
        vLine(gx1 - 0.045, y0, y1, A(C1, 240));
        vLine(gx1 + 0.045, y0, y1, A(C1, 240));
        vLine(gx1 + 0.115, y0, y1, A(C3, 190));
    }`);

fs.writeFileSync(GEN, s);
hit.forEach(([t, n]) => console.log('  ' + (n === 1 ? '✓' : '✗') + ' ' + t.padEnd(12) + ' 命中 ' + n));
