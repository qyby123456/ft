/* ============================================================================
 *  lib_itemtex.cjs —— 物品模型贴图/UV 的公共库（2026-09-15 从暗影刀 v3~v10 的教训里合并出来的）
 *
 *  包含四件事:
 *   1) makeCanvas(w,h)   : 带 alpha 合成画布(put/mix/area/lines), **通道与插值系数全部 clamp**
 *                          —— 不 clamp 会因外插 >255 写进 Uint8Array 回绕, 贴图上凭空出现金色杂点;
 *   2) makePacker(bands): 把"侧/盖面"矩形按**自身比例**自动装箱放进空带
 *                          —— 逐面 UV 的前提是每个面有比例正确的块, 共用一条就会拉伸;
 *   3) outline(...)      : 1px 外描边(从透明侧向内只描一圈);
 *   4) checks            : 四项自检(暖色杂点 / 正反镜像 / 浮点噪声 / 面比例对应),
 *                          每个模型生成完都该跑一遍。
 *
 *  用法见 tools/model3d/regen_shadow_sword.cjs
 * ==========================================================================*/
const { encodePNG } = require('./png.cjs');
const nodeFs = require('fs');

/* ---------------- 0) 安全写入 ----------------
   Windows 上资源文件可能带"只读"属性(本工程里 gen_weapons.cjs 生成的那批就是),
   直接 writeFileSync 会 EPERM/EACCES; 这里先清只读再写。 */
function writeSafe(p, data) {
    try { nodeFs.chmodSync(p, 0o666); } catch (e) { /* 文件不存在时忽略 */ }
    try { nodeFs.writeFileSync(p, data); }
    catch (e) {
        if (e.code === 'EPERM' || e.code === 'EACCES') { nodeFs.chmodSync(p, 0o666); nodeFs.writeFileSync(p, data); }
        else throw e;
    }
}
/** 备份用: 目标若已存在且是只读(Windows 上 copyFile 会连属性一起继承), 先清掉再覆盖 */
function safeCopy(src, dst) {
    if (!nodeFs.existsSync(src)) return false;
    try { nodeFs.chmodSync(dst, 0o666); } catch (e) { /* 目标不存在时忽略 */ }
    try { nodeFs.copyFileSync(src, dst); }
    catch (e) {
        if (e.code === 'EPERM' || e.code === 'EACCES') { nodeFs.chmodSync(dst, 0o666); nodeFs.copyFileSync(src, dst); }
        else throw e;
    }
    try { nodeFs.chmodSync(dst, 0o666); } catch (e) { }
    return true;
}

/* ---------------- 1) 画布 ---------------- */
function makeCanvas(w, h) {
    const buf = new Uint8Array(w * h * 4);
    const cl = v => v < 0 ? 0 : v > 255 ? 255 : v;
    const put = (x, y, c, a) => {
        x = Math.round(x); y = Math.round(y);
        if (x < 0 || y < 0 || x >= w || y >= h) return;
        const o = (y * w + x) * 4;
        const k = Math.max(0, Math.min(1, (a === undefined ? (c[3] === undefined ? 255 : c[3]) : a) / 255));
        if (k <= 0) return;
        const ia = buf[o + 3] / 255, oa = k + ia * (1 - k);
        if (oa <= 0) return;
        buf[o] = cl((c[0] * k + buf[o] * ia * (1 - k)) / oa);
        buf[o + 1] = cl((c[1] * k + buf[o + 1] * ia * (1 - k)) / oa);
        buf[o + 2] = cl((c[2] * k + buf[o + 2] * ia * (1 - k)) / oa);
        buf[o + 3] = cl(oa * 255);
    };
    const mix = (a, b, t0) => {
        const t = Math.max(0, Math.min(1, t0));
        const aa = a[3] === undefined ? 255 : a[3], ba = b[3] === undefined ? 255 : b[3];
        return [a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t, a[2] + (b[2] - a[2]) * t, aa + (ba - aa) * t];
    };
    const alphaAt = (x, y) => buf[(y * w + x) * 4 + 3];
    /* 在像素矩形里逐像素画: fn(fx,fy) 返回颜色或 null */
    const rect = (px0, py0, pw, ph, fn) => {
        for (let j = 0; j < ph; j++) for (let i = 0; i < pw; i++) { const c = fn((i + 0.5) / pw, (j + 0.5) / ph); if (c) put(px0 + i, py0 + j, c); }
    };
    const save = p => writeSafe(p, encodePNG(w, h, Buffer.from(buf)));
    return { w, h, buf, put, mix, rect, alphaAt, save };
}

/* ---------------- 2) 装箱器 ---------------- */
/* bands = [{x, w, top?, bottom?}]; 每行从左往右放, 放不下换行; 主带满了换下一带 */
function makePacker(bands) {
    const rects = {};
    const st = bands.map(b => ({ ...b, rows: [], cur: null }));
    function alloc(name, w, h) {
        w = Math.max(0.2, w); h = Math.max(0.2, h);
        for (const b of st) {
            const bottom = b.bottom === undefined ? 16 : b.bottom;
            if (!b.cur || b.cur.x + w > b.x + b.w + 1e-6) {
                const prev = b.rows.length ? b.rows[b.rows.length - 1] : null;
                const y = prev ? prev.y + prev.h + 0.06 : (b.top === undefined ? 0.05 : b.top);
                if (y + h > bottom) continue;
                b.cur = { x: b.x, y, h };
                b.rows.push(b.cur);
            }
            const row = b.cur;
            if (row.y + h > bottom) continue;
            const x = row.x; row.x += w + 0.06; row.h = Math.max(row.h, h);
            rects[name] = [+x.toFixed(4), +row.y.toFixed(4), +(x + w).toFixed(4), +(row.y + h).toFixed(4)];
            return rects[name];
        }
        throw new Error('装箱失败(空位不够): ' + name + ' ' + w + 'x' + h);
    }
    return { alloc, rects };
}

/* ---------------- 3) 描边 ---------------- */
function outline(cv, color) {
    const hits = [];
    for (let y = 0; y < cv.h; y++) for (let x = 0; x < cv.w; x++) {
        if (cv.alphaAt(x, y) > 8) continue;
        for (const [dx, dy] of [[1, 0], [-1, 0], [0, 1], [0, -1]]) {
            const nx = x + dx, ny = y + dy;
            if (nx < 0 || ny < 0 || nx >= cv.w || ny >= cv.h) continue;
            if (cv.alphaAt(nx, ny) > 8) { hits.push([x, y]); break; }
        }
    }
    hits.forEach(([x, y]) => cv.put(x, y, color));
    return hits.length;
}

/* ---------------- 4) 自检 ---------------- */
const checks = {
    /** 暖色像素(仅作参考: 木质/铜钉/南瓜这类武器本来就是暖色, 不能当错误) */
    warmPixels(cv) {
        let n = 0;
        for (let i = 0; i < cv.buf.length; i += 4) if (cv.buf[i + 3] > 20 && cv.buf[i] > cv.buf[i + 1] + 12 && cv.buf[i + 1] > cv.buf[i + 2] + 12) n++;
        return n;
    },
    /** ★ 溢出回绕杂点的真正特征: **饱和金色**(蓝通道被顶爆回绕成很小值), 例如 [222,192,31] */
    goldArtifacts(cv) {
        let n = 0;
        for (let i = 0; i < cv.buf.length; i += 4) if (cv.buf[i + 3] > 20 && cv.buf[i] > 200 && cv.buf[i + 1] > 150 && cv.buf[i + 2] < 60) n++;
        return n;
    },
    /** 正反宽面必须互为镜像: north.u1 > south.u1 且 north.u2 < south.u2 */
    mirror(elements, skip = /^rivet/) {
        const bad = [];
        elements.forEach(e => {
            if (skip.test(e.name)) return;
            const n = e.faces.north.uv, s = e.faces.south.uv;
            if (!(n[0] > s[0] && n[2] < s[2])) bad.push(e.name);
        });
        return bad;
    },
    /** 浮点噪声: JSON 里出现 5 位以上小数 */
    noise(elements) { return elements.filter(e => /\.\d{5,}/.test(JSON.stringify(e))).map(e => e.name); },
    /** 面比例是否和贴图块比例一致(逐面 UV 不拉伸的前提) */
    aspect(parts, rects, sideNameOf, tol = 0.02) {
        const bad = [];
        parts.forEach(p => {
            const key = sideNameOf(p);
            const r = rects[key];
            if (!r || key.startsWith('SIDE_')) return;
            // 装箱器有 0.2 UV 的最小块下限; 被它钳过的极小面(通常还是隐藏面)比例必然对不上, 跳过
            if ((r[2] - r[0]) <= 0.21 || (r[3] - r[1]) <= 0.21) return;
            const want = p.d / (p.y1 - p.y0), got = (r[2] - r[0]) / (r[3] - r[1]);
            if (Math.abs(want - got) > tol) bad.push(p.n + ' 面=' + want.toFixed(3) + ' 块=' + got.toFixed(3));
        });
        return bad;
    },
};

module.exports = { makeCanvas, makePacker, outline, checks, writeSafe, safeCopy };
