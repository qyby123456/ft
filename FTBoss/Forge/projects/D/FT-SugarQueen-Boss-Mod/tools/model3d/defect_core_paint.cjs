/* ============================================================================
 * 故障核心 · 逐部件贴图绘制(2026-09-11 按用户要求"进一步细化模型再逐个贴图绘制")
 *
 * 和旧版的区别: 旧版是"按材质刷噪点"(同一材质的每个 cube 长得一样);
 * 现在每个部件都画**设计过的图形**, 用 (x,y) 与矩形尺寸 fw/fh 做几何判定:
 *   金色甲片 → 深色包边 + 四角亮包角 + 固定走向的壳裂纹 + 铆钉 + 崩口
 *   眼球     → 圆外压暗 / 虹膜同心环 / 左上斜向高光弧 / 白色高光点
 *   胸口核心 → 按球种染色的径向渐变 + 3 道亮环
 *   披风     → 折痕带 + 编织纹 + 下摆压暗 + 一道金缝线
 *   掌心     → 深色掌心 + 中间发光接触垫 + 四角小垫
 *   散热格栅 → 深底 + 3 道亮叶片
 * 所有函数签名: (px, py, x, y, cube, fw, fh) —— px/py 是贴图绝对坐标, x/y 是部件内局部坐标。
 * ========================================================================== */
'use strict';

const clamp255 = v => Math.max(0, Math.min(255, v));

module.exports.makePaint = function makePaint({ M, cv, nz, getCore }) {
    /** 竖直拉丝(±0.5) */
    const brush = (x, y, s) => (nz(x, Math.floor(y / 3), s) - 0.5);
    /** 斜向裂纹: 命中返回 true(2px 宽, 每 period 行换一次偏移) */
    function crackLine(x, y, s, period) {
        const off = ((Math.floor(y / period) * 73 + s * 29) % 251) / 251 * period;
        return ((x + off) % period) < 2;
    }
    /** 铆钉: 每 step 一颗, 2=高光点 1=钉身 0=不是钉 */
    function rivet(x, y, step) {
        const cx = Math.floor(x / step) * step + (step >> 1);
        const cy = Math.floor(y / step) * step + (step >> 1);
        const d = Math.abs(x - cx) + Math.abs(y - cy);
        return d === 0 ? 2 : (d === 1 ? 1 : 0);
    }
    /** 甲片包边 */
    const onFrame = (x, y, fw, fh, t) => x < t || y < t || x >= fw - t || y >= fh - t;
    /** 四角包角区域 */
    const inCorner = (x, y, fw, fh, len) =>
        (x < len || x >= fw - len) && (y < len || y >= fh - len);
    /** 到矩形中心的归一化距离 */
    function ndist(x, y, fw, fh) {
        const dx = (x - fw / 2) / Math.max(1, fw / 2), dy = (y - fh / 2) / Math.max(1, fh / 2);
        return Math.sqrt(dx * dx + dy * dy);
    }

    const PAINT = {
        /* ---- 金色甲片: 拉丝底 + 包边 + 包角 + 裂纹 + 铆钉 + 崩口 ---- */
        [M.GOLD]: (px, py, x, y, c, fw, fh) => {
            const n = nz(x, y, 1) * 18 - 9 + brush(x, y, 1) * 16;
            let r = 206 + n, g = 176 + n, b = 116 + n;
            if (onFrame(x, y, fw, fh, 2)) { r -= 48; g -= 44; b -= 30; }
            if (inCorner(x, y, fw, fh, Math.max(3, Math.round(Math.min(fw, fh) * 0.2)))) { r += 24; g += 22; b += 14; }
            if (crackLine(x, y, 21, 22)) { r -= 72; g -= 68; b -= 48; }
            if (nz(x * 2, y * 3, 2) > 0.93) { r -= 56; g -= 52; b -= 38; }
            const rv = rivet(x, y, 22);
            if (rv === 2) { r += 42; g += 40; b += 26; } else if (rv === 1) { r -= 22; g -= 20; b -= 14; }
            cv.set(px, py, clamp255(r), clamp255(g), clamp255(b), 1);
        },
        /* ---- 暗金(下颌/背甲/崩口件): 烟熏 + 划痕 ---- */
        [M.GOLD_DARK]: (px, py, x, y, c, fw, fh) => {
            const n = nz(x, y, 3) * 16 - 8 + brush(x, y, 3) * 12;
            let r = 166 + n, g = 136 + n, b = 84 + n;
            if (onFrame(x, y, fw, fh, 2)) { r -= 30; g -= 28; b -= 20; }
            if (crackLine(x, y, 31, 30)) { r -= 44; g -= 40; b -= 30; }
            if (rivet(x, y, 26) === 1) { r -= 18; g -= 16; b -= 12; }
            cv.set(px, py, clamp255(r), clamp255(g), clamp255(b), 1);
        },
        /* ---- 残冠: 高光金 + 边缘成片崩口 + 顶棱提亮 ---- */
        [M.CROWN]: (px, py, x, y, c, fw, fh) => {
            const n = nz(x, y, 9) * 18 - 9;
            const edge = Math.min(x, y, fw - 1 - x, fh - 1 - y);
            const chip = edge < Math.max(2, Math.min(fw, fh) * 0.22) && nz(x * 3, y * 5, 10) > 0.55;
            let r = (chip ? 164 : 228) + n, g = (chip ? 134 : 198) + n, b = (chip ? 80 : 134) + n;
            if (y < 2) { r += 20; g += 18; b += 12; }                 // 顶棱提亮
            if (crackLine(x, y, 17, 18)) { r -= 40; g -= 38; b -= 26; }
            cv.set(px, py, clamp255(r), clamp255(g), clamp255(b), 1);
        },
        /* ---- 深色内壳: 碳纹 + 横向散热槽 + 内嵌面板 + 下方透光条 ---- */
        [M.BODY]: (px, py, x, y, c, fw, fh) => {
            const n = nz(x, y, 11) * 12 - 6;
            let r = 44 + n, g = 50 + n, b = 66 + n;
            if (((x + y) & 7) < 2) { r -= 8; g -= 8; b -= 10; }       // 碳纤维斜纹
            if ((y & 7) === 0) { r -= 14; g -= 14; b -= 16; }         // 横向散热槽
            if (onFrame(x, y, fw, fh, 1)) { r -= 10; g -= 10; b -= 12; }
            if (y > fh * 0.62 && ((x >> 1) & 3) === 0) { r += 40; g += 92; b += 112; }  // 透光条
            cv.set(px, py, clamp255(r), clamp255(g), clamp255(b), 1);
        },
        /* ---- 透光缝: 亮芯 + 青边 ---- */
        [M.CRACK]: (px, py, x, y) => {
            const core = ((x + y) % 6) < 2;
            const n = nz(x, y, 13) * 16 - 8;
            if (core) cv.set(px, py, 228, 252, 255, 1);
            else cv.set(px, py, 92 + n, 210 + n, 246, 1);
        },
        /* ---- 罗纹机械件: 细罗纹 + 两端铆钉排 + 油污 ---- */
        [M.METAL]: (px, py, x, y, c, fw, fh) => {
            const rib = (y % 6) < 3;
            const n = nz(x, y, 6) * 12 - 6;
            let r = (rib ? 42 : 62) + n, g = (rib ? 50 : 72) + n, b = (rib ? 64 : 88) + n;
            const nearEnd = y < 3 || y >= fh - 3;
            if (nearEnd) { r -= 12; g -= 12; b -= 14; }
            if (rndAt(x, y) > 0.94) { r -= 14; g -= 14; b -= 16; }
            const rv = rivet(x, y, 18);
            if (rv === 2) { r += 28; g += 30; b += 34; } else if (rv === 1) { r -= 14; g -= 14; b -= 16; }
            cv.set(px, py, clamp255(r), clamp255(g), clamp255(b), 1);
        },
        /* ---- 玻璃眼球: 圆外压暗 / 虹膜同心环 / 左上高光弧 / 白色高光点 ---- */
        [M.GLASS]: (px, py, x, y, c, fw, fh) => {
            const d = ndist(x, y, fw, fh);
            const n = nz(x, y, 4) * 12 - 6;
            if (d > 0.94) { cv.set(px, py, 40 + n, 108 + n, 150 + n, 1); return; }   // 眼圈外的压暗
            const ring = Math.abs(Math.sin(d * 7.0)) > 0.8;                          // 虹膜同心环
            // 左上斜向高光弧: 以左上偏移点为圆心的环带
            const hx = (x - fw * 0.3) / Math.max(1, fw / 2), hy = (y - fh * 0.3) / Math.max(1, fh / 2);
            const hd = Math.sqrt(hx * hx + hy * hy);
            const spec = hd > 0.42 && hd < 0.56;
            const dot = Math.abs(x - fw * 0.24) <= 1.2 && Math.abs(y - fh * 0.22) <= 1.2;
            let r, g, b;
            if (dot) { r = 248; g = 253; b = 255; }
            else if (spec) { r = 244; g = 253; b = 255; }
            else if (ring) { r = 188 - n; g = 240 - n; b = 252; }
            else { r = 92 + n + d * 44; g = 188 + n - d * 30; b = 236 + n - d * 16; }
            cv.set(px, py, clamp255(r), clamp255(g), clamp255(b), 1);
        },
        /* ---- 金属眼圈: 青铜底 + 内侧亮环 ---- */
        [M.RIM]: (px, py, x, y, c, fw, fh) => {
            const d = ndist(x, y, fw, fh);
            const n = nz(x, y, 5) * 12 - 6 + brush(x, y, 5) * 10;
            let r = 54 + n, g = 130 + n, b = 184 + n;
            if (d < 0.72) { r += 26; g += 34; b += 30; }             // 内侧亮环
            if (d > 0.94) { r -= 18; g -= 20; b -= 22; }
            cv.set(px, py, clamp255(r), clamp255(g), clamp255(b), 1);
        },
        /* ---- 披风: 折痕带 + 编织纹 + 下摆压暗 + 金缝线 ---- */
        [M.CLOTH]: (px, py, x, y, c, fw, fh) => {
            const weave = (((x >> 1) + (y >> 1)) & 1) === 0;
            const fold = (x % 12) < 3;
            const n = nz(x, y, 7) * 12 - 6;
            let r = (fold ? 50 : 74) + n, g = (fold ? 114 : 152) + n, b = (fold ? 122 : 160) + n;
            if (!weave) { r -= 6; g -= 8; b -= 8; }
            if (y >= fh - 5) { r -= 20; g -= 24; b -= 24; }           // 下摆压暗
            if (y === fh - 7 || y === fh - 6) { r = 210; g = 182; b = 120; }  // 金缝线
            if (y < 2) { r += 16; g += 18; b += 12; }                 // 上缘提亮
            cv.set(px, py, clamp255(r), clamp255(g), clamp255(b), 1);
        },
        /* ---- 抛光金饰: 顶斜面提亮 + 拉丝 + 底缘压暗 ---- */
        [M.TRIM]: (px, py, x, y, c, fw, fh) => {
            const n = nz(x, y, 14) * 16 - 8 + brush(x, y, 14) * 14;
            let r = 224 + n, g = 194 + n, b = 130 + n;
            if (y < Math.max(1, fh * 0.16)) { r += 22; g += 20; b += 14; }
            if (y > fh * 0.86) { r -= 28; g -= 26; b -= 18; }
            if (onFrame(x, y, fw, fh, 1)) { r -= 14; g -= 12; b -= 10; }
            cv.set(px, py, clamp255(r), clamp255(g), clamp255(b), 1);
        },
        /* ---- 胸口核心: 按球种染色的径向渐变 + 3 道亮环 ---- */
        [M.CORE]: (px, py, x, y, c, fw, fh) => {
            const d = ndist(x, y, fw, fh);
            const [coreRgb] = getCore();
            const n = nz(x, y, 4) * 12 - 6;
            const ring = Math.abs(Math.sin(d * 6.0)) > 0.78;
            const k = clamp255((ring ? 1.32 : (d < 0.42 ? 1.15 : 0.82)) - d * 0.14);
            cv.set(px, py,
                clamp255(coreRgb[0] * k / 255 * 255 + n),
                clamp255(coreRgb[1] * k / 255 * 255 + n),
                clamp255(coreRgb[2] * k / 255 * 255 + n), 1);
        },
        [M.CORE_CRACK]: (px, py, x, y) => {
            const n = nz(x, y, 13) * 12 - 6;
            if (((x + y) % 5) < 2) cv.set(px, py, 250, 255, 255, 1);
            else { const [coreRgb, coreCrackRgb] = getCore(); void coreRgb; cv.set(px, py, clamp255(coreCrackRgb[0] + n), clamp255(coreCrackRgb[1] + n), clamp255(coreCrackRgb[2] + n), 1); }
        },
        /* ---- 掌心: 深色掌心 + 中间发光接触垫 + 四角小垫 ---- */
        [M.PALM]: (px, py, x, y, c, fw, fh) => {
            const d = ndist(x, y, fw, fh);
            const n = nz(x, y, 23) * 12 - 6;
            if (d < 0.44) { cv.set(px, py, 96 + n, 214 + n, 246, 1); return; }   // 发光接触垫
            if (d < 0.56) { cv.set(px, py, 62 + n, 128 + n, 172, 1); return; }   // 垫圈
            let r = 58 + n, g = 62 + n, b = 74 + n;                              // 深色掌心
            if (inCorner(x, y, fw, fh, 2)) { r = 96; g = 150; b = 176; }         // 四角小垫
            cv.set(px, py, clamp255(r), clamp255(g), clamp255(b), 1);
        },
        /* ---- 散热格栅: 深底 + 3 道亮叶片 ---- */
        [M.VENT]: (px, py, x, y, c, fw, fh) => {
            const n = nz(x, y, 27) * 10 - 5;
            const blade = fh >= 8 && ((y % 4) < 2);
            let r = 30 + n, g = 34 + n, b = 44 + n;
            if (blade) { r = 150; g = 226; b = 250; }
            if (onFrame(x, y, fw, fh, 1)) { r -= 12; g -= 12; b -= 12; }
            cv.set(px, py, clamp255(r), clamp255(g), clamp255(b), 1);
        }
    };
    return PAINT;
};

/** 供 METAL 用的油污噪声 */
function rndAt(x, y) { return ((x * 73 + y * 151 + 7 * 29) % 251) / 251; }
