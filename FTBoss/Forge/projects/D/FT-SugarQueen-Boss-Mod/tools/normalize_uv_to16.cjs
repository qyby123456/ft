/* ============================================================================
 *  把物品模型的 UV 从"64 像素空间"改成"16 空间"(2026-09-12 v65)
 *
 *  背景: 9 座**世界石像**用的是 "UV 0~16 + 不给 texture_size"(默认 16) —— 游戏里显示正常;
 *        而柚子手持石像用的是 "UV 0~64 + texture_size 64" —— 客户端把它当成"贴图铺满每个面"
 *        (用户描述"扁平一片、像贴图摊开")。
 *
 *  改法: 所有 UV 除以 4(64/16), 并把 texture_size 写成 [16,16]。
 *        这样无论加载器**是否**遵守 texture_size, 取样位置都完全相同(数学等价):
 *          uv16 / 16 = uv64 / 64
 *  顺带把贴图仍在 textures/item/ 下不动(图集必须收 item/ 目录)。
 *
 *  用法: node normalize_uv_to16.cjs <model.json> [<model2.json> ...]
 * ==========================================================================*/
const fs = require('fs');

for (const p of process.argv.slice(2)) {
    const m = JSON.parse(fs.readFileSync(p, 'utf8'));
    const T = m.texture_size || [16, 16];
    if (T[0] === 16 && T[1] === 16) {
        console.log(`跳过 ${p}(已经是 16 空间)`);
        continue;
    }
    const k = 16 / T[0];
    let n = 0;
    for (const el of m.elements || []) {
        for (const key of Object.keys(el.faces || {})) {
            const f = el.faces[key];
            if (!f.uv) continue;
            f.uv = f.uv.map((v) => Math.round(v * k * 1000) / 1000);
            n++;
        }
    }
    m.texture_size = [16, 16];
    fs.writeFileSync(p, JSON.stringify(m, null, 2) + '\n');
    console.log(`${p}: ${n} 个面的 UV ×${k} 归一到 0~16, texture_size -> [16,16]`);
}
