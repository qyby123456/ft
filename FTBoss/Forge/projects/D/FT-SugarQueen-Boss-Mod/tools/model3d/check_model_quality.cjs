/* check_model_quality.cjs —— 模型质量检查器（只读，输出 JSON 报告）
 *
 * ★ 这是用户 2026-09-16 提供的 python 规格 `check_model_quality.py` 的 **Node 移植版**。
 *   为什么不用原版：本机 `python` 是微软商店占位程序、**没有 Pillow**（AGENTS.md 记过这条），
 *   工程既有规矩是"一切生成器/校验器都写成 node + 自写 png.cjs，无外部依赖"。
 *   检查项、三级分类（error/warning/review）、阈值、报告字段**逐条对应规格**，没有自行加减。
 *
 * 三级含义（照规格）：
 *   error    资源结构不合法或明确异常（父骨骼不存在、循环父级、UV 越界、动画引用缺失骨骼…）
 *   warning  可能影响显示，需要检查（重复几何、过薄零件、纹素密度过高、比例拉伸…）
 *   review   必须通过预览才能判断（好不好看、展开是否自然、握持是否正确…）
 *
 * ★ 规格明确要求的两条"不要误报"：
 *   · **不能仅凭方块相交就判定穿模** —— 轴盖/包箍/斧柄本来就可能合理嵌入（本检查器不做几何相交判定）；
 *   · **左右斧刃共享 UV 不是错误** —— 重复 UV 只作为 `intentional_uv_reuse_candidates` 列出供复查。
 *
 * 用法:
 *   node tools/model3d/check_model_quality.cjs <geo.json> <texture.png> \
 *        [--animation <anim.json>] [--target-density 2] [--out art/<名字>_quality.json]
 */
'use strict';

const fs = require('fs');
const path = require('path');
const { decodePNG } = require('./png.cjs');

/* ---------------- 参数 ---------------- */
const argv = process.argv.slice(2);
const positional = [];
const opt = (name, fallback) => {
  const i = argv.indexOf(name);
  return i >= 0 && argv[i + 1] !== undefined ? argv[i + 1] : fallback;
};
for (let i = 0; i < argv.length; i++) {
  if (argv[i].startsWith('--')) { i++; continue; }
  positional.push(argv[i]);
}
const GEO = positional[0];
const TEX = positional[1];
if (!GEO || !TEX) {
  console.error('用法: node check_model_quality.cjs <geo.json> <texture.png> [--animation f] [--target-density 2] [--out p]');
  process.exit(2);
}
const ANIM = opt('--animation', null);
const TARGET_DENSITY = Number(opt('--target-density', '2'));
const OUT = opt('--out', path.join(path.resolve(__dirname, '..', '..'), 'art', 'model_quality.json'));
if (!Number.isFinite(TARGET_DENSITY) || TARGET_DENSITY <= 0) {
  console.error('--target-density 必须是正的有限数值');
  process.exit(2);
}

/* ---------------- 小工具（对应规格里的 finite/vector/report） ---------------- */
const issues = [];
const metrics = { bones: 0, cubes: 0, declared_faces: 0, density_faces_checked: 0, animation_clips: 0 };
const faceMetrics = [];
const uvUsers = new Map();
const densitySamples = [];
const seenCubeShapes = new Map();

const report = (level, code, location, message) => issues.push({ level, code, location, message });
const finite = v => typeof v === 'number' && Number.isFinite(v);
const vector = (v, len = 3) => Array.isArray(v) && v.length === len && v.every(finite);

function readJson(p) {
  try { return JSON.parse(fs.readFileSync(p, 'utf8')); }
  catch (e) { console.error('无法读取 JSON：' + p + '\n' + e.message); process.exit(2); }
}

const document_ = readJson(GEO);
let texture;
try { texture = decodePNG(TEX); } catch (e) { console.error('无法读取 PNG：' + e.message); process.exit(2); }

const geometries = document_['minecraft:geometry'];
if (!Array.isArray(geometries) || geometries.length === 0) {
  console.error('未找到有效 minecraft:geometry 数组'); process.exit(2);
}

const allBoneNames = new Set();
const FACE_DIMENSIONS = {
  north: [0, 1], south: [0, 1], west: [2, 1], east: [2, 1], up: [0, 2], down: [0, 2]
};

/* ---------------- 主遍历 ---------------- */
geometries.forEach((geometry, gi) => {
  if (!geometry || typeof geometry !== 'object') {
    report('error', 'INVALID_GEOMETRY', String(gi), 'geometry 必须为对象');
    return;
  }
  const prefix = `geometry[${gi}]`;
  let description = geometry.description;
  if (!description || typeof description !== 'object') description = {};

  const tw = description.texture_width;
  const th = description.texture_height;
  const validDimensions = finite(tw) && finite(th) && tw > 0 && th > 0;

  if (!validDimensions) {
    report('error', 'TEXTURE_SIZE', prefix, '贴图声明宽高必须为正数');
  } else if (texture.w !== tw || texture.h !== th) {
    report('review', 'TEXTURE_RESOLUTION_DIFFERS', prefix,
      `实际 PNG 为 ${texture.w}x${texture.h}，声明为 ${tw}x${th}；可能是有意的高清替换，密度按实际 PNG 计算`);
  }

  const bones = geometry.bones;
  if (!Array.isArray(bones)) {
    report('error', 'INVALID_BONES', prefix, 'bones 必须为数组');
    return;
  }

  const byName = new Map();
  bones.forEach((bone, bi) => {
    const location = `${prefix}/bone[${bi}]`;
    if (!bone || typeof bone !== 'object') { report('error', 'INVALID_BONE', location, '骨骼必须为对象'); return; }
    const name = bone.name;
    if (typeof name !== 'string' || !name) { report('error', 'BONE_NAME', location, '缺少有效骨骼名称'); return; }
    metrics.bones++;
    if (byName.has(name)) report('error', 'DUPLICATE_BONE', location, `重复名称：${name}`);
    else byName.set(name, bone);
    allBoneNames.add(name);
  });

  for (const [name, bone] of byName) {
    const location = `${prefix}/${name}`;
    const parent = bone.parent;
    if (parent !== undefined && parent !== null) {
      if (typeof parent !== 'string' || !byName.has(parent)) {
        report('error', 'MISSING_PARENT', location, `父级无效或不存在：${parent}`);
      }
    }
    for (const field of ['pivot', 'rotation']) {
      if (field in bone && !vector(bone[field])) {
        report('error', 'BONE_TRANSFORM', location, `${field} 必须是三个有限数字`);
      }
    }
    /* 循环父级 */
    const visited = new Set();
    let current = name;
    while (typeof current === 'string' && byName.has(current)) {
      if (visited.has(current)) { report('error', 'PARENT_CYCLE', location, '骨骼父级存在循环'); break; }
      visited.add(current);
      current = byName.get(current).parent;
    }

    const cubes = bone.cubes;
    if (cubes !== undefined && !Array.isArray(cubes)) {
      report('error', 'INVALID_CUBES', location, 'cubes 必须为数组');
      continue;                       // 对应规格里的 continue，只跳过这一根骨骼
    }

    (cubes || []).forEach((cube, ci) => {
      const part = `${location}/cube[${ci}]`;
      if (!cube || typeof cube !== 'object') { report('error', 'INVALID_CUBE', part, 'cube 必须为对象'); return; }
      metrics.cubes++;

      const origin = cube.origin, size = cube.size;
      if (!vector(origin) || !vector(size)) {
        report('error', 'CUBE_DIMENSIONS', part, 'origin、size 必须是三个有限数字');
        return;
      }
      if (size.some(v => v < 0)) { report('error', 'NEGATIVE_SIZE', part, '尺寸不能为负数'); return; }

      const zeroAxes = size.filter(v => v === 0).length;
      if (zeroAxes >= 2) report('warning', 'DEGENERATE_CUBE', part, '至少两个方向尺寸为零，通常没有可见表面积');
      else if (zeroAxes === 1) report('review', 'PLANE_GEOMETRY', part, '零厚度面片：确认用途、背面显示与深度状态');
      else if (Math.min(...size) < 0.10) report('warning', 'THIN_GEOMETRY', part, '厚度小于 0.10 模型单位，远看可能消失或闪烁');

      for (const field of ['pivot', 'rotation']) {
        if (field in cube && !vector(cube[field])) {
          report('error', 'CUBE_TRANSFORM', part, `${field} 必须是三个有限数字`);
        }
      }

      const inflate = cube.inflate === undefined ? 0 : cube.inflate;
      if (!finite(inflate)) report('error', 'INVALID_INFLATE', part, 'inflate 必须是有限数字');
      else if (size.some(v => v + 2 * inflate < 0)) report('error', 'COLLAPSED_INFLATE', part, 'inflate 导致几何尺寸反转');
      else if (inflate !== 0) report('review', 'INFLATE_NOT_MEASURED', part, '像素密度基于原始 size，未计入 inflate');

      /* 同一骨骼内完全重合的几何 -> 只报 warning，不把普通交叠当错误 */
      const shape = JSON.stringify({
        origin, size,
        pivot: cube.pivot || [0, 0, 0],
        rotation: cube.rotation || [0, 0, 0],
        inflate
      });
      const shapeKey = gi + '|' + name + '|' + shape;
      if (seenCubeShapes.has(shapeKey)) {
        report('warning', 'DUPLICATE_GEOMETRY', part,
          '与同骨骼几何完全重合：' + seenCubeShapes.get(shapeKey) + '；确认是否为有意覆盖层');
      } else {
        seenCubeShapes.set(shapeKey, part);
      }

      const uv = cube.uv;
      if (!uv || typeof uv !== 'object' || Array.isArray(uv)) {
        report('review', 'UV_NOT_CHECKED', part, 'Box UV 或缺失 UV：本检查器未展开，不能视作通过');
        return;
      }

      for (const [side, face] of Object.entries(uv)) {
        const facePart = `${part}/${side}`;
        if (!(side in FACE_DIMENSIONS)) { report('review', 'UNKNOWN_FACE', facePart, '未识别的面名称'); continue; }
        if (!face || typeof face !== 'object') { report('error', 'INVALID_FACE', facePart, '逐面 UV 必须为对象'); continue; }

        metrics.declared_faces++;
        const start = face.uv, span = face.uv_size;
        if (!vector(start, 2) || !vector(span, 2)) {
          report('review', 'UV_NOT_MEASURED', facePart, '需要显式 uv 和 uv_size 才能计算');
          continue;
        }
        if (!validDimensions) continue;

        const [u, v] = start;
        const [uw, vh] = span;
        const left = Math.min(u, u + uw), right = Math.max(u, u + uw);
        const top = Math.min(v, v + vh), bottom = Math.max(v, v + vh);

        if (left < 0 || top < 0 || right > tw || bottom > th) {
          report('error', 'UV_OUT_OF_BOUNDS', facePart, 'UV 超出声明的贴图范围');
          continue;
        }
        if (uw === 0 || vh === 0) {
          report('warning', 'ZERO_UV', facePart, 'UV 面积为零，请确认是否有意取固定纹素');
          continue;
        }

        const uvKey = `${left},${top},${right},${bottom}`;
        if (!uvUsers.has(uvKey)) uvUsers.set(uvKey, []);
        uvUsers.get(uvKey).push(facePart);

        const [ai, bi] = FACE_DIMENSIONS[side];
        const width = size[ai], height = size[bi];
        if (width <= 0 || height <= 0) continue;

        const du = Math.abs(uw) * texture.w / tw / width;
        const dv = Math.abs(vh) * texture.h / th / height;
        const area = width * height;

        densitySamples.push([Math.max(du, dv), area]);
        metrics.density_faces_checked++;

        if (Math.max(du, dv) > TARGET_DENSITY * 1.5) {
          report('warning', 'HIGH_PIXEL_DENSITY', facePart, `像素密度 ${du.toFixed(2)} × ${dv.toFixed(2)} px/模型单位`);
        }
        if (Math.max(du, dv) / Math.min(du, dv) > 1.5) {
          report('warning', 'PIXEL_STRETCH', facePart, '横纵像素密度相差超过 1.5 倍');
        }

        const actualRect = [left * texture.w / tw, top * texture.h / th, right * texture.w / tw, bottom * texture.h / th];
        if (actualRect.some(p => Math.abs(p - Math.round(p)) > 1e-6)) {
          report('warning', 'SUBPIXEL_UV', facePart, 'UV 边界未对齐实际纹素');
        }

        /* 采样该 UV 区域：全透明 / 半透明 */
        const x0 = Math.floor(actualRect[0]), y0 = Math.floor(actualRect[1]);
        const x1 = Math.ceil(actualRect[2]), y1 = Math.ceil(actualRect[3]);
        let opaque = 0, partial = 0, total = 0;
        for (let y = Math.max(0, y0); y < Math.min(texture.h, y1); y++) {
          for (let x = Math.max(0, x0); x < Math.min(texture.w, x1); x++) {
            const a = texture.data[(y * texture.w + x) * 4 + 3];
            total++;
            if (a > 0) opaque++;
            if (a > 0 && a < 255) partial++;
          }
        }
        if (total > 0 && opaque === 0) {
          report('review', 'INVISIBLE_FACE', facePart, '该 UV 区域完全透明，确认此面是否必要');
        } else if (partial > 0) {
          report('review', 'PARTIAL_ALPHA', facePart, '存在半透明像素，检查实际材质的混合和排序');
        }

        faceMetrics.push({ part: facePart, density_u: +du.toFixed(3), density_v: +dv.toFixed(3), model_area: area });
      }
    });
  }
});

/* ---------------- 动画 ---------------- */
if (ANIM) {
  const animDoc = readJson(ANIM);
  let clips = animDoc.animations;
  if (!clips || typeof clips !== 'object' || Array.isArray(clips)) {
    report('error', 'INVALID_ANIMATIONS', 'animations', 'animations 必须为对象');
    clips = {};
  }
  for (const [clipName, clip] of Object.entries(clips)) {
    metrics.animation_clips++;
    if (!clip || typeof clip !== 'object') { report('error', 'INVALID_CLIP', clipName, '动画必须为对象'); continue; }

    const length = clip.animation_length;
    if (length !== undefined && (!finite(length) || length < 0)) {
      report('error', 'INVALID_DURATION', clipName, '动画时长无效');
    }

    const animatedBones = clip.bones;
    if (!animatedBones || typeof animatedBones !== 'object') {
      report('error', 'INVALID_ANIMATED_BONES', clipName, '动画 bones 必须为对象');
      continue;
    }

    for (const [name, channels] of Object.entries(animatedBones)) {
      const location = `${clipName}/${name}`;
      if (!allBoneNames.has(name)) report('error', 'MISSING_ANIMATED_BONE', location, '动画引用不存在的骨骼');
      if (!channels || typeof channels !== 'object') { report('error', 'INVALID_CHANNELS', location, '骨骼动画通道必须为对象'); continue; }

      for (const channel of ['rotation', 'position', 'scale']) {
        const value = channels[channel];
        if (!value || typeof value !== 'object' || Array.isArray(value)) continue;
        for (const timestamp of Object.keys(value)) {
          const time = Number(timestamp);
          if (!Number.isFinite(time)) { report('review', 'UNPARSED_KEYFRAME', location, `未解析关键帧字段：${timestamp}`); continue; }
          if (time < 0) report('error', 'INVALID_KEY_TIME', location, `关键帧时间无效：${timestamp}`);
          else if (finite(length) && time > length + 1e-6) {
            report('warning', 'KEY_AFTER_END', location, `关键帧 ${timestamp} 超过动画时长`);
          }
        }
      }
    }
  }
}

/* ---------------- 汇总 ---------------- */
const counts = { error: 0, warning: 0, review: 0 };
for (const i of issues) counts[i.level] = (counts[i.level] || 0) + 1;

const totalArea = densitySamples.reduce((s, [, a]) => s + a, 0);
const highArea = densitySamples.filter(([d]) => d > TARGET_DENSITY * 1.5).reduce((s, [, a]) => s + a, 0);

const manualReview = [
  '正面、背面、侧面、45°：检查轮廓、厚度和零件连接',
  '纯色剪影：检查斧刃/剑身、握柄和装饰能否清晰区分',
  '正常手持画面尺寸：检查细纹是否碎杂、闪烁或消失',
  '贴图：检查像素块大小、材质明暗和跨面接缝',
  '动作起止与中间帧：检查穿插、脱节和姿态跳变',
  'Buff 开合与 slam/recover 组合：检查四片刃翼与轴盖',
  '第一/第三人称左右手：检查握点、手臂遮挡和近裁剪',
  '多人、物品栏、展示框：检查共享模型姿态与发光残留'
];

let status;
if (counts.error) status = '发现结构错误，先修复';
else if (counts.warning) status = '存在优化警告，需结合预览复查';
else status = '未发现已覆盖规则的错误；外观仍待验证';

const result = {
  status,
  counts,
  metrics: {
    ...metrics,
    texture_size: [texture.w, texture.h],
    high_density_area_ratio: totalArea ? +(highArea / totalArea).toFixed(4) : null
  },
  issues,
  highest_density_faces: faceMetrics
    .slice()
    .sort((a, b) => Math.max(b.density_u, b.density_v) - Math.max(a.density_u, a.density_v))
    .slice(0, 30),
  intentional_uv_reuse_candidates: [...uvUsers.entries()]
    .filter(([, users]) => users.length > 1)
    .map(([rect, users]) => ({ uv_rect: rect.split(',').map(Number), faces: users })),
  manual_review: manualReview,
  limitations: [
    '不判定审美质量，不自动修改模型',
    '不做变换后的几何碰撞、接触或可见性计算',
    '不计骨骼缩放、动画姿态和 display 对纹素密度的影响',
    '高密度统计包含内部面；不等于屏幕可见面积占比',
    '不区分高清细节与最近邻放大的重复像素',
    'Box UV 和部分省略字段未展开，均单独标记',
    '动画仅检查骨骼引用和显式时间，不验证完整插值/Molang',
    '多个 geometry 的动画引用按骨骼名称并集检查'
  ]
};

fs.mkdirSync(path.dirname(OUT), { recursive: true });
fs.writeFileSync(OUT, JSON.stringify(result, null, 2) + '\n', 'utf8');

console.log('报告：' + OUT);
console.log('判断：' + status);
console.log('错误：' + (counts.error || 0) + '　警告：' + (counts.warning || 0) + '　复查：' + (counts.review || 0));
console.log('模型：骨骼 ' + metrics.bones + ' / 立方体 ' + metrics.cubes + ' / 逐面 UV 面数 ' + metrics.declared_faces
  + '　贴图 ' + texture.w + 'x' + texture.h);
process.exit(counts.error ? 1 : 0);
