/* 修加载卡条: GeckoLib 加载 compost_superman_p2.geo.json 失败
 *  做法: 二阶段**只换贴图不换几何**(两套几何本来完全相同) -> 模型永久指向棕色 geo, 贴图按阶段切 _p2
 *  另外把 _p2.geo.json 覆盖成棕色几何的副本(任何残留引用也都能安全加载)
 */
const fs = require('fs');
const path = require('path');
const ROOT = process.argv[2];
const A = path.join(ROOT, 'src/main/resources/assets/ftboss');

/* ① 用棕色几何覆盖 p2 几何(消除那个加载失败的文件) */
const src = path.join(A, 'geo/compost_superman.geo.json');
const dst = path.join(A, 'geo/compost_superman_p2.geo.json');
if (fs.existsSync(src)) {
    fs.copyFileSync(src, dst);
    console.log('① p2 几何已覆盖为棕色几何副本: ' + fs.statSync(dst).size + ' B');
} else console.log('① ❌ 找不到棕色几何');

/* ② 模型类: getModelResource 永远用棕色几何 */
const p = path.join(ROOT, 'src/main/java/net/farmingtales/ftboss/client/model/CompostGeoModel.java');
let s = fs.readFileSync(p, 'utf8');
const old = 'return rl(e.getPhase() >= 2 ? "geo/compost_superman_p2.geo.json" : "geo/compost_superman.geo.json");';
const neu = 'return rl("geo/compost_superman.geo.json");   // 2026-09-12: 二阶段只换贴图, 不换几何(pr geo 曾导致 GeckoLib 加载失败 -> 卡加载条)'.replace('pr geo', 'p2 geo');
if (s.includes(old)) { s = s.replace(old, neu); fs.writeFileSync(p, s); console.log('② CompostGeoModel: 几何固定为棕色版'); }
else if (s.includes('geo/compost_superman.geo.json");   // 2026-09-12')) console.log('② 已是固定几何');
else console.log('② ❌ 未命中几何切换行');
/* 贴图切换保持不变(阶段 2 起用 _p2) */
console.log('   贴图切换行: ' + (s.includes('compost_superman_p2.png') ? '保留 OK' : '⚠ 缺失'));
