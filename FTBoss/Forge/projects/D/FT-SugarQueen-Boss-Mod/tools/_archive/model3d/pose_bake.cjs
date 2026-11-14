/* 姿势烘焙: 把动画在某个时间点的姿势烘成一份**静态 geo**, 交给 render_tex.cjs 出图。
 * 用法: node pose_bake.cjs <in.geo.json> <in.animation.json> <动画名(可省 animation.defect_core. 前缀)> <时间秒> <out.geo.json>
 *
 * 只做线性插值(Bedrock 默认就是 linear), 数值型 rotation/position 三通道。
 * 目的: validate_anim.cjs 只查结构, 姿势好不好看必须出图看。
 * (骨骼 position 支持见 render_tex.cjs 的 walk(): 顺序 = 父级 · T(position) · T(pivot) · R · T(-pivot))
 */
const fs = require('fs');

const [, , geoPath, animPath, rawName, tStr, outPath] = process.argv;
const t = parseFloat(tStr);
const geo = JSON.parse(fs.readFileSync(geoPath, 'utf8'));
const animFile = JSON.parse(fs.readFileSync(animPath, 'utf8'));
const name = rawName.startsWith('animation.') ? rawName : `animation.defect_core.${rawName}`;
const anim = animFile.animations[name];
if (!anim) {
    console.error(`动画不存在: ${name}\n可选: ${Object.keys(animFile.animations).join(', ')}`);
    process.exit(1);
}

/** 在关键帧对象里按时间线性插值, 返回 [x,y,z] 或 null
 *  注意: 键的小数位数不固定(手写"0.6" 与自动生成的"0.60" 都有), 所以按数值排序后查表, 不做字符串匹配 */
function sample(channel) {
    if (!channel) return null;
    const keys = Object.keys(channel).map(k => [parseFloat(k), channel[k]]).filter(e => !isNaN(e[0]));
    if (!keys.length) return null;
    keys.sort((a, b) => a[0] - b[0]);
    if (t <= keys[0][0]) return keys[0][1];
    const last = keys[keys.length - 1];
    if (t >= last[0]) return last[1];
    for (let i = 0; i < keys.length - 1; i++) {
        const [ta, va] = keys[i], [tb, vb] = keys[i + 1];
        if (t >= ta && t <= tb) {
            const k = (t - ta) / Math.max(1e-6, tb - ta);
            return [0, 1, 2].map(j => va[j] + (vb[j] - va[j]) * k);
        }
    }
    return null;
}

const g = JSON.parse(JSON.stringify(geo));
let posed = 0;
for (const geom of g['minecraft:geometry']) {
    for (const bone of geom.bones) {
        const ch = anim.bones && anim.bones[bone.name];
        if (!ch) continue;
        const r = sample(ch.rotation);
        const p = sample(ch.position);
        const s = sample(ch.scale);
        if (r) { bone.rotation = r.map(v => Math.round(v * 1000) / 1000); posed++; }
        if (p) bone.position = p.map(v => Math.round(v * 1000) / 1000);
        if (s && s.some(v => Math.abs(v - 1) > 1e-3)) bone.scale = s.map(v => Math.round(v * 1000) / 1000);
    }
}
fs.writeFileSync(outPath, JSON.stringify(g, null, 1));
console.log(`${name} @ ${t}s -> ${outPath}  (有姿势的骨骼 ${posed} 个)`);
