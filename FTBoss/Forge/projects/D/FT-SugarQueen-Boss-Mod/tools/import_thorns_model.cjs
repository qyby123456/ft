/* ============================================================================
 *  导入用户模型 thorns.json -> ftboss 物品模型 blooming_thorns.json (2026-09-12)
 *
 *  用户给的 thorns.json 是 Blockbench 导出的 Java 物品模型(39 个 element, 带自身旋转),
 *  但 **UV 写在 0..16 空间, texture_size 却写 64×64** —— MC 会把 UV 按 texture_size 归一化
 *  (uv / textureWidth), 于是只会采到左上角 16×16 的一小块, 武器会变成糊掉的一团。
 *
 *  实测: 用 tools/model3d/preview_3d.cjs 渲染两种解释
 *    A) UV 原样       -> 只有零星几块颜色
 *    B) UV ×4(即把 0..16 空间铺满 64×64)-> 藤枝 + 粉花 + 青刺, 完整的一把武器 ✓
 *  所以这里把每个 UV 值 ×4, 归一化到"像素空间", 和工程里其它模型(shadow_sword 等)一致。
 *
 *  用法: node import_thorns_model.cjs <thorns.json 路径> <tx.png 路径>
 * ==========================================================================*/
const fs = require('fs');
const path = require('path');

const SRC_JSON = process.argv[2];
const SRC_TEX = process.argv[3];
const ROOT = path.join(__dirname, '..');
const OUT_JSON = path.join(ROOT, 'src', 'main', 'resources', 'assets', 'ftboss', 'models', 'item', 'blooming_thorns.json');
const OUT_TEX = path.join(ROOT, 'src', 'main', 'resources', 'assets', 'ftboss', 'textures', 'item', 'blooming_thorns.png');

const m = JSON.parse(fs.readFileSync(SRC_JSON, 'utf8'));
const T = m.texture_size || [16, 16];
const SCALE = T[0] / 16;                      // 0..16 空间 -> 像素空间

let faces = 0;
for (const el of m.elements) {
    for (const key of Object.keys(el.faces || {})) {
        const f = el.faces[key];
        if (!f.uv) continue;
        f.uv = f.uv.map((v) => Math.round(v * SCALE * 100) / 100);
        faces++;
    }
}

m.credit = 'Blooming Thorns — 用户提供(Blockbench); 由 tools/import_thorns_model.cjs 导入: '
    + `UV ×${SCALE} 归一化到 ${T[0]}×${T[1]} 像素空间, 贴图改走 textures/item/`;
m.textures = { '0': 'ftboss:item/blooming_thorns', particle: 'ftboss:item/blooming_thorns' };

fs.mkdirSync(path.dirname(OUT_JSON), { recursive: true });
fs.mkdirSync(path.dirname(OUT_TEX), { recursive: true });
fs.writeFileSync(OUT_JSON, JSON.stringify(m, null, 2) + '\n');
fs.copyFileSync(SRC_TEX, OUT_TEX);

console.log('写出模型: ' + path.relative(ROOT, OUT_JSON));
console.log(`  元素 ${m.elements.length} 个 / 面 ${faces} 个 / UV ×${SCALE} / texture_size ${T.join('x')}`);
console.log('写出贴图: ' + path.relative(ROOT, OUT_TEX));
console.log('显示上下文: ' + Object.keys(m.display || {}).join(', '));
