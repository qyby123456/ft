/* YSM 参考模型/动画探针: node ysm_probe.cjs <model.json> [animation.json]
 * 目的: 量化专业作者的 Bedrock 资产手法(骨骼深度/cube 密度/旋转用法/关键帧密度/lerp_mode/音效帧),
 * 好把可借鉴的部分搬到本工程的 Boss 模型与动画上。 */
const fs = require('fs');

function probeModel(p) {
    const j = JSON.parse(fs.readFileSync(p, 'utf8'));
    const geos = j['minecraft:geometry'] || [];
    console.log(`\n=== 模型 ${p} ===`);
    for (const g of geos) {
        const d = g.description || {};
        const bones = g.bones || [];
        let cubes = 0, withRot = 0, withInflate = 0, withPerFaceUV = 0, maxDepth = 0;
        const parent = {};
        for (const b of bones) { if (b.parent) parent[b.name] = b.parent; }
        for (const b of bones) {
            const cs = b.cubes || [];
            cubes += cs.length;
            for (const c of cs) {
                if (c.rotation) withRot++;
                if (c.inflate) withInflate++;
                if (c.uv && !Array.isArray(c.uv)) withPerFaceUV++;
            }
            let depth = 0, cur = b.name;
            while (parent[cur]) { depth++; cur = parent[cur]; if (depth > 64) break; }
            maxDepth = Math.max(maxDepth, depth);
        }
        console.log(`  贴图 ${d.texture_width}x${d.texture_height} | 骨骼 ${bones.length} | cube ${cubes}` +
            ` | 平均 cube/骨 ${(cubes / Math.max(1, bones.length)).toFixed(1)} | 最大层级深度 ${maxDepth}`);
        console.log(`  cube 带 rotation ${withRot} | 带 inflate ${withInflate} | 逐面 UV ${withPerFaceUV}`);
        if (d.visible_bounds_width) console.log(`  bounds ${d.visible_bounds_width}x${d.visible_bounds_height}`);
    }
}

function probeAnim(p) {
    const j = JSON.parse(fs.readFileSync(p, 'utf8'));
    const anims = j.animations || {};
    const names = Object.keys(anims);
    console.log(`\n=== 动画 ${p} ===`);
    console.log(`  条目 ${names.length} 条`);
    let catmull = 0, totalKeys = 0, withSound = 0, withParticle = 0, molang = 0, nonNumeric = 0;
    const loops = {};
    for (const n of names) {
        const a = anims[n];
        loops[String(a.loop)] = (loops[String(a.loop)] || 0) + 1;
        for (const ch of Object.values(a.bones || {})) {
            for (const key of ['rotation', 'position', 'scale']) {
                const kk = ch[key];
                if (!kk) continue;
                for (const v of Object.values(kk)) {
                    totalKeys++;
                    if (v && typeof v === 'object' && v.lerp_mode) { if (v.lerp_mode === 'catmullrom') catmull++; }
                    if (Array.isArray(v)) for (const x of v) if (typeof x === 'string') molang++;
                    if (typeof v === 'string') nonNumeric++;
                }
            }
        }
        if (a.sound_effects) withSound += Object.keys(a.sound_effects).length;
        if (a.particle_effects) withParticle += Object.keys(a.particle_effects).length;
    }
    console.log(`  关键帧总数 ${totalKeys} | catmullrom 键 ${catmull} | 含字符串(Molang)值 ${molang} | 直接字符串值 ${nonNumeric}`);
    console.log(`  sound_effects 帧 ${withSound} | particle_effects 帧 ${withParticle} | loop 分布 ${JSON.stringify(loops)}`);
    // 抽一条"攻击/施法"类动画看它的结构
    const pick = names.find(n => /attack|cast|skill|magic|sword|slash|punch|shoot/i.test(n)) || names[0];
    const a = anims[pick];
    if (a) {
        const bones = Object.keys(a.bones || {});
        console.log(`  样例 "${pick}": length ${a.animation_length}s loop=${a.loop} 骨骼 ${bones.length}`);
        const first = bones[0];
        if (first) {
            const ch = a.bones[first];
            const k = Object.keys(ch.rotation || ch.position || {});
            console.log(`    骨 "${first}" 关键帧时刻: ${k.slice(0, 14).join(', ')}${k.length > 14 ? ' …' : ''}`);
            const rk = ch.rotation && Object.values(ch.rotation)[1];
            console.log(`    第二个关键帧写法示例: ${JSON.stringify(rk)}`);
        }
    }
}

probeModel(process.argv[2]);
if (process.argv[3]) probeAnim(process.argv[3]);
