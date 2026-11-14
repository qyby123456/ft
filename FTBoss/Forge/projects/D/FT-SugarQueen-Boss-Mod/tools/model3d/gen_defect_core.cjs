/* ============================================================================
 * 故障核心 (defect_core) 生成器 —— v3 人形版(2026-09-11)
 *
 * 参考: 用户给的《杀戮尖塔》故障机器人头像特写 + 官方人形轮廓
 *   金色裂壳头(青蓝玻璃大眼 + 小眼 + 锯齿残冠) / 深色裂开的躯干 + 胸口发光核心
 *   / 罗纹机械颈 / 金色肩甲 / **悬浮断开的机械手**(带爪指) / 青绿披风 / 深色分段腿
 *
 * 骨骼是**真正的父子层级**(root→torso→neck→head, torso→shoulder→arm→hand, root→leg),
 * 这样动躯干才会带动头/手/披风(扁平时会各自为政 —— v1 踩过)。
 * 材质分区与发光遮罩(glowmask)由生成器自动导出。
 * ========================================================================== */
const fs = require('fs');
const path = require('path');
const { Canvas } = require('./png.cjs');

const OUT = process.argv[2] || 'out';
const NAME = 'defect_core';
/** 2026-09-11 按用户要求"精细贴图": 256 → **512×512**(像素 ×4), 给铆钉/甲片缝/裂纹/织物/玻璃折射留像素 */
const TEX_W = 512, TEX_H = 512;

/* ---------------- 1. 材质 ---------------- */
const M = {
    GOLD: 0, GOLD_DARK: 1, CROWN: 2, BODY: 3, CRACK: 4, METAL: 5,
    GLASS: 6, RIM: 7, CLOTH: 8, TRIM: 9,
    CORE: 10, CORE_CRACK: 11,     // 胸口核心专用材质: 会按"当前球种"变色
    PALM: 12, VENT: 13            // 掌心接触垫 / 散热格栅(2026-09-11 逐个部件绘制时新增)
};
const cv = new Canvas(TEX_W, TEX_H);
const nz = (x, y, s) => (((x * 37 + y * 53 + s * 11) % 17) / 17);

/** 胸口核心的配色(按球种) —— 生成 5 张只有核心颜色不同的贴图 */
const CORE_TINTS = {
    glass: [110, 216, 248],
    lightning: [255, 238, 120],
    frost: [214, 242, 255],
    ion: [255, 150, 236],
    dark: [178, 92, 238]
};
let coreRgb = CORE_TINTS.glass;
let coreCrackRgb = [220, 250, 255];

/* ---------------- 1b. 贴图绘制(逐部件) ----------------
 * 2026-09-11 按用户要求"逐个贴图绘制": 绘制逻辑独立成 defect_core_paint.cjs,
 * 每个部件画设计好的图形(甲片包边/包角、眼球虹膜环+高光弧、披风折痕与下摆金缝线、掌心发光垫、散热格栅…)。
 * 需要读"当前球种的核心配色", 所以传 getCore 闭包。 */
const PAINT = require('./defect_core_paint.cjs').makePaint({ M, cv, nz, getCore: () => [coreRgb, coreCrackRgb] });

/* ---------------- 2. 装箱器 ---------------- */
const matMask = new Uint8Array(TEX_W * TEX_H).fill(255);
let penX = 0, penY = 0, rowH = 0;
function allocUV(w, h, d) {
    const fw = Math.ceil(2 * (w + d)), fh = Math.ceil(d + h);
    if (penX + fw > TEX_W) { penX = 0; penY += rowH + 1; rowH = 0; }
    if (penY + fh > TEX_H) throw new Error('贴图放不下: 需要更大贴图');
    const u = penX, v = penY;
    penX += fw + 1; rowH = Math.max(rowH, fh);
    return [u, v, fw, fh];
}

/* ---------------- 3. 立方体 ---------------- */
const C = [];
/** 只分配 UV 并记录(真正上色放到 paintPass, 因为要出 5 张核心变体) */
function cube(bone, o, s, mat) {
    const uv = allocUV(s[0], s[1], s[2]);
    C.push({ bone, o, s, mat, uv: [uv[0], uv[1]] });
}
/** 带旋转的 cube(2026-09-11 照 YSM 参考模型的手法: 专业作者 75% 的 cube 都用 rotation 做斜角/倒角) */
function cubeR(bone, o, s, mat, rot) {
    const uv = allocUV(s[0], s[1], s[2]);
    C.push({ bone, o, s, mat, uv: [uv[0], uv[1]], rot });
}
/** 带 inflate(外壳层)的 cube —— YSM 模型里 139 个 cube 用 inflate 做"壳盖在里面一层之上" */
function cubeI(bone, o, s, mat, inflate) {
    const uv = allocUV(s[0], s[1], s[2]);
    C.push({ bone, o, s, mat, uv: [uv[0], uv[1]], inflate });
}
/** 左右镜像地放一对 cube(2026-09-11 加: 保证严格对称, 免得手写镜像时算错零点几)
 *  kind: 'P' 普通 / 'R' 带旋转 / 'I' 带 inflate; 右件坐标按 x → -(x + w) 换算, 旋转按 [rx,-ry,-rz] 镜像 */
function cubePair(kind, boneL, boneR, o, s, mat, rot, inflate) {
    const put = (bone, oo, rr) => {
        if (kind === 'R') cubeR(bone, oo, s, mat, rr);
        else if (kind === 'I') cubeI(bone, oo, s, mat, inflate);
        else cube(bone, oo, s, mat);
    };
    put(boneL, o, rot);
    put(boneR, [-(o[0] + s[0]), o[1], o[2]], rot ? [rot[0], -rot[1], -rot[2]] : null);
}

/** 按当前核心配色重画整张贴图(UV 布局与 cube 顺序一致 => 5 张变体只有核心颜色不同) */
function paintPass() {
    cv.d.fill(0);
    matMask.fill(255);
    for (const c of C) {
        const fw = Math.ceil(2 * (c.s[0] + c.s[2])), fh = Math.ceil(c.s[2] + c.s[1]);
        for (let x = 0; x < fw; x++) for (let y = 0; y < fh; y++) {
            // 多传 cube 与矩形尺寸: 玻璃眼球/核心要做"同心环 + 边缘压暗", 必须知道矩形中心
            PAINT[c.mat](c.uv[0] + x, c.uv[1] + y, x, y, c, fw, fh);
            matMask[(c.uv[1] + y) * TEX_W + (c.uv[0] + x)] = c.mat;
        }
    }
}

/* ================= v6 几何(2026-09-11 重做) =================
 * 相对 v5 的改动(用户要求"重做机器人模型动画", 并照参考图/§4.1 规格对齐):
 *   ① 头从"贴在脸上的方块屏" → **金色裂壳面具 + 4 层递进凸出的玻璃眼球**(真正读得出"眼球凸出来");
 *   ② 残冠 4 片等大 → **6 片高低错落**(中间最高), 面具加崩口暗块与壳缝(参考图的锯齿崩边);
 *   ③ 新增 `jaw` 下颌骨(施法张嘴) + 6 根**独立爪指骨** `claw_*`(能逐指收拢) → 可动骨骼 20 → 30;
 *   ④ 颈改 3 段罗纹, 躯干加侧肋/背甲/透光裂缝, 肩→臂→手之间**留缝**做"悬浮断开"的机械感;
 *   ⑤ 披风加下摆一层, 三片相位错开摆动。
 */

/* ---- v6.1: 两种下半身(FT_VARIANT=legs|float) ----
 * legs : 悬浮下垂的细机械腿(大腿/小腿留关节缝 + 尖脚甲)
 * float: **悬浮无腿**(参考图的那种形态): 下半身收成推进脊柱 + 两片反重力鳍 + 喷口,
 *        披风加长盖住下半身。两种方案**沿用同一批骨骼名**(leg_l/leg_r), 所以动画完全通用。 */
const VARIANT = process.env.FT_VARIANT || 'legs';
if (VARIANT === 'float') {
    cube('torso', [-3.6, 8.2, -3.0], [7.2, 5.4, 6.0], M.METAL);      // 推进脊柱
    cube('torso', [-2.6, 6.2, -2.2], [5.2, 2.4, 4.4], M.GOLD_DARK);  // 尾段
    cube('torso', [-1.4, 5.4, -1.4], [2.8, 1.6, 2.8], M.CRACK);      // 尾部发光缝
    cube('leg_l', [3.0, 4.4, -1.4], [2.2, 8.6, 3.4], M.GOLD);        // 左反重力鳍
    cube('leg_r', [-5.2, 4.4, -1.4], [2.2, 8.6, 3.4], M.GOLD);
    cube('leg_l', [2.5, 2.4, -2.2], [3.2, 2.0, 5.0], M.TRIM);        // 鳍下喷口
    cube('leg_r', [-5.7, 2.4, -2.2], [3.2, 2.0, 5.0], M.TRIM);
    cube('leg_l', [3.2, 6.6, -2.0], [1.8, 2.4, 4.6], M.METAL);       // 鳍上接件
    cube('leg_r', [-5.0, 6.6, -2.0], [1.8, 2.4, 4.6], M.METAL);
} else {
    for (const [bone, x0] of [['leg_l', 1.4], ['leg_r', -5.4]]) {
        cube(bone, [x0, 9.6, -2.2], [4.0, 5.4, 4.4], M.BODY);        // 大腿
        cube(bone, [x0 + 1.2, 9.8, -3.1], [1.6, 4.4, 1.4], M.GOLD);  // 大腿前护板
        cube(bone, [x0 + 3.2, 10.2, -1.2], [1.2, 4.2, 1.2], M.TRIM); // 外侧液压缸(v11 新增)
        cube(bone, [x0 + 0.2, 8.6, -2.0], [3.6, 1.0, 4.0], M.TRIM);  // 膝上环
        cube(bone, [x0 + 0.4, 6.8, -1.9], [3.2, 1.0, 3.6], M.METAL); // 膝下环(中间留缝 = 膝盖)
        cube(bone, [x0 + 0.5, 3.2, -1.8], [3.0, 3.6, 3.4], M.METAL); // 小腿(两段感)
        cube(bone, [x0 - 0.2, 1.6, -2.4], [4.4, 1.6, 4.8], M.GOLD);  // 踝甲
        cube(bone, [x0 - 0.6, 0.0, -3.0], [5.2, 1.6, 6.0], M.TRIM);  // 尖脚底
    }
}

/* ---- 骨盆 + 躯干 v11: **倒梯形**(肩宽腰细) + 腰部液压柱 + 四分片胸甲 ----
 * 旧版躯干是一整块 11.2 宽的方箱, 正面看就是"方块史蒂夫"。现在拆成三段递减
 * (胸 11.6 → 中 10.0 → 腹 8.4), 腰侧加两根 45° 液压柱, 胸甲分成左右上下四片并留中缝。 */
cube('torso', [-4.0, 13.4, -3.2], [8.0, 3.4, 6.4], M.BODY);       // 骨盆(更窄)
cube('torso', [-4.2, 16.8, -3.6], [8.4, 2.6, 7.2], M.BODY);       // 下腹
cube('torso', [-5.0, 19.4, -3.8], [10.0, 3.4, 7.6], M.BODY);      // 中段
cube('torso', [-5.8, 22.8, -3.8], [11.6, 4.2, 7.6], M.BODY);      // 胸部(最宽)
cubePair('P', 'torso', 'torso', [5.8, 22.8, -3.4], [1.2, 4.2, 6.8], M.METAL);              // 上段侧板
cubePair('P', 'torso', 'torso', [5.0, 19.4, -3.4], [1.2, 3.4, 6.8], M.METAL);              // 中段侧板
cubePair('P', 'torso', 'torso', [4.2, 16.8, -3.2], [1.2, 2.6, 6.4], M.METAL);              // 下段侧板
cubePair('R', 'torso', 'torso', [6.2, 20.0, -2.6], [1.4, 1.4, 5.2], M.TRIM, [0, 0, 45]);   // 腰部液压柱
cubePair('P', 'torso', 'torso', [0.4, 24.0, -4.4], [5.6, 3.4, 1.6], M.GOLD);               // 胸甲上片
cubePair('P', 'torso', 'torso', [0.4, 18.6, -4.4], [4.8, 3.0, 1.6], M.GOLD);               // 胸甲下片
cube('torso', [-0.4, 18.6, -4.6], [0.8, 8.8, 1.2], M.GOLD_DARK);                           // 中央竖缝
cube('torso', [-5.4, 16.8, 3.0], [10.8, 12.0, 1.0], M.GOLD_DARK);                          // 背甲
cube('torso', [-4.8, 17.4, -4.8], [0.9, 8.6, 0.7], M.CRACK);     // 透光裂缝
cube('torso', [3.2, 18.4, -4.8], [0.9, 6.6, 0.7], M.CRACK);
cube('torso', [-3.4, 23.0, -4.8], [6.8, 0.8, 0.7], M.CRACK);
/* ---- 胸口核心: 静止金属框(core_ring) + 自转发光核(core) ---- */
cube('core_ring', [-4.6, 22.8, -5.4], [9.2, 1.0, 1.2], M.RIM);
cube('core_ring', [-4.6, 17.6, -5.4], [9.2, 1.0, 1.2], M.RIM);
cube('core_ring', [-4.6, 18.6, -5.4], [1.0, 8.2, 1.2], M.RIM);
cube('core_ring', [3.6, 18.6, -5.4], [1.0, 8.2, 1.2], M.RIM);
cube('core', [-3.2, 19.0, -7.4], [6.4, 6.4, 1.8], M.CORE);        // 第 1 层
cube('core', [-2.2, 18.1, -8.2], [4.4, 8.2, 1.2], M.CORE);        // 第 2 层(前凸)
cube('core', [-1.4, 20.6, -8.8], [2.8, 2.8, 1.0], M.CORE);        // 高光面
cube('core', [-3.6, 21.0, -8.0], [7.2, 1.0, 1.2], M.CORE_CRACK);  // 十字刻线(自转时可见)
cube('core', [-0.5, 18.4, -8.0], [1.0, 7.4, 1.2], M.CORE_CRACK);

/* ---- 罗纹机械颈(3 段, 越上越细) ---- */
cube('neck', [-3.2, 28.2, -3.2], [6.4, 1.5, 6.4], M.METAL);
cube('neck', [-3.6, 29.7, -3.6], [7.2, 1.4, 7.2], M.METAL);
cube('neck', [-3.0, 31.1, -3.0], [6.0, 1.5, 6.0], M.METAL);

/* ---- 面具 v11: **裂开的金壳**(2026-09-11 优化) ----
 * 参考图是一颗"裂开的金蛋壳"。旧版是一整块方壳 + 一颗眼, 读起来像"金色电视机"。
 * 现在: 主壳拆成**上/下两片、上片向右后错位 0.6 单位**, 接缝露出**发光裂缝**;
 * 顶板拆成 3 片残片; 眼睛**放大到几乎占满整张脸**(参考图就是这样); 下颌上方留一道暗格栅当"嘴"。 */
cube('head', [-5.8, 32.6, -5.8], [11.6, 4.6, 10.6], M.GOLD);      // 下壳
cube('head', [-6.4, 37.2, -5.2], [12.6, 4.2, 10.0], M.GOLD);      // 上壳(向右后错位)
cube('head', [-6.2, 37.1, -6.3], [12.2, 0.7, 1.4], M.GOLD_DARK);  // 接缝暗线
cube('head', [-4.6, 37.2, -6.6], [9.2, 0.6, 0.9], M.CRACK);       // 接缝透光 —— "裂壳"的关键观感
cube('head', [-7.0, 33.4, -4.4], [1.2, 5.6, 8.4], M.GOLD);        // 左颊
cube('head', [5.6, 33.4, -4.4], [1.2, 5.6, 8.4], M.GOLD);         // 右颊
cube('head', [-7.0, 38.4, -3.8], [1.4, 2.4, 3.4], M.GOLD_DARK);   // 崩口(左上)
cube('head', [5.8, 39.0, 1.2], [1.4, 1.8, 2.8], M.GOLD_DARK);     // 崩口(右上)
cube('head', [4.4, 32.8, 3.0], [1.4, 1.4, 2.4], M.GOLD_DARK);     // 崩口(右下)
cube('head', [-5.9, 33.0, 3.4], [1.2, 2.0, 2.2], M.GOLD_DARK);    // 崩口(左下)
/* 顶板 → 3 片残片(不再是整块平板, 破壳感) */
cube('head', [-6.2, 41.2, -5.6], [6.4, 1.6, 5.6], M.GOLD);
cube('head', [-0.4, 41.2, -5.8], [6.8, 1.4, 5.2], M.GOLD);
cube('head', [-4.0, 41.2, -0.6], [7.2, 1.2, 6.0], M.GOLD_DARK);
cube('jaw', [-4.8, 31.4, -6.0], [9.6, 1.6, 10.2], M.GOLD_DARK);   // 下颌(张嘴用)
cube('jaw', [-4.4, 31.9, -6.5], [8.8, 0.6, 0.9], M.VENT);         // 嘴缝(暗格栅)
/* ---- 大玻璃眼球 v11.1: 外两层用**十字叠**切掉四角(正面看也是圆的), 里层保持方块 ----
 * 旧版是 4 层同心方片, 正面看就是一块方板。现在最大的两层玻璃拆成"横条 + 竖条",
 * 四角自然缺掉约 2 单位, 正面/侧面都读成球。*/
cube('eye', [-5.2, 33.2, -6.4], [10.4, 8.6, 1.4], M.RIM);         // 金属眼圈(方的, 当面罩边框)
cube('eye', [-4.5, 34.6, -7.6], [9.0, 4.8, 1.8], M.GLASS);        // 玻璃 1 横条
cube('eye', [-2.6, 33.9, -7.6], [5.2, 7.2, 1.8], M.GLASS);        // 玻璃 1 竖条(叠成圆角方)
cube('eye', [-3.8, 35.2, -8.8], [7.6, 4.0, 1.8], M.GLASS);        // 玻璃 2 横条
cube('eye', [-2.4, 34.5, -8.8], [4.8, 5.0, 1.8], M.GLASS);        // 玻璃 2 竖条
cube('eye', [-2.7, 35.5, -9.8], [5.4, 4.0, 1.6], M.GLASS);        // 玻璃 3(很小了, 保持方块)
cube('eye', [-1.6, 36.2, -10.6], [3.2, 2.6, 1.2], M.GLASS);       // 玻璃 4(高光中心)
/* ---- 小眼球(参考图右上方那颗小点) ---- */
cube('eye_small', [-3.4, 39.4, -6.0], [3.4, 3.4, 1.2], M.RIM);
cube('eye_small', [-2.9, 39.9, -6.8], [2.4, 2.4, 1.2], M.GLASS);
cube('eye_small', [-2.4, 40.4, -7.4], [1.4, 1.4, 0.8], M.GLASS);
/* ---- 头顶锯齿残冠: 6 片 **每片带旋转**(参差错落的"碎王冠", YSM 的斜置 cube 手法) ---- */
cubeR('crown', [-5.4, 42.4, -2.2], [3.2, 3.6, 2.2], M.CROWN, [0, 0, -12]);
cubeR('crown', [-1.4, 42.4, -2.8], [2.8, 5.6, 2.2], M.CROWN, [0, 0, 6]);
cubeR('crown', [2.6, 42.4, -1.6], [3.0, 4.4, 2.2], M.CROWN, [0, 0, 14]);
cubeR('crown', [-4.4, 42.4, 1.6], [2.4, 2.6, 2.0], M.CROWN, [10, 0, -8]);
cubeR('crown', [1.6, 42.4, 2.0], [2.6, 3.4, 2.0], M.CROWN, [-8, 0, 10]);
cubeR('crown', [-1.0, 42.4, 0.2], [1.8, 6.6, 1.8], M.CROWN, [0, 0, -5]);
cubeR('crown', [3.2, 42.4, 3.2], [1.6, 1.8, 1.6], M.CROWN, [14, 0, 18]);
/* ---- 倒角: 4 条 45° 斜轨把方壳四角"削掉"(不加方块、只转角度 —— 这是 YSM 精细感的来源) ---- */
cubeR('head', [-6.6, 39.8, -6.2], [2.4, 2.4, 11.0], M.GOLD, [0, 0, 45]);
cubeR('head', [4.2, 39.8, -6.2], [2.4, 2.4, 11.0], M.GOLD, [0, 0, 45]);
cubeR('head', [-6.6, 33.4, -6.2], [2.4, 2.4, 11.0], M.GOLD_DARK, [0, 0, 45]);
cubeR('head', [4.2, 33.4, -6.2], [2.4, 2.4, 11.0], M.GOLD_DARK, [0, 0, 45]);
/* ---- 外壳层(inflate 0.18): 顶板/胸甲/肩甲各压一层, 做出"壳盖在里层之上"的厚度 ---- */
cubeI('head', [-5.4, 40.2, -5.8], [10.8, 1.6, 10.2], M.GOLD, 0.18);
cubeI('torso', [-5.8, 24.0, -4.4], [11.6, 3.4, 1.6], M.GOLD, 0.16);
/* ---- 肩甲(带崩口) + **悬浮断开**的细机械臂 + 金手 + 3 根独立长爪 ----
 * 2026-09-11 照 YSM 手法加: 斜置护肩板 + 肩/肘/腕三个"关节球"(3 个斜置 cube 叠出球感) */
cube('shoulder_l', [5.6, 24.4, -3.2], [5.2, 5.4, 7.0], M.TRIM);
cube('shoulder_r', [-10.8, 24.4, -3.2], [5.2, 5.4, 7.0], M.TRIM);
cube('shoulder_l', [6.2, 29.2, -2.8], [4.0, 1.6, 6.2], M.GOLD);
cube('shoulder_r', [-10.2, 29.2, -2.8], [4.0, 1.6, 6.2], M.GOLD);
cube('shoulder_l', [7.4, 23.8, 2.6], [3.0, 2.2, 1.2], M.GOLD_DARK);   // 肩后崩口
cube('shoulder_r', [-10.4, 23.8, 2.6], [3.0, 2.2, 1.2], M.GOLD_DARK);
/* ================= 肩 / 臂(2026-09-11 重做) =================
 * 用户反馈: "侧边悬浮模型要不就删掉, 要不就做好一点" —— 旧版的斜置护肩板(45° 长条)与悬空"关节球"
 * 在身侧就是两块飘着的零件。现在做成**有连接的机械肩关节**, 并顺带加厚手臂层次:
 *   躯干侧插座 → 关节球(贴住插座) → 护肩双层板(对齐肩部, 不再是 45° 长条) → 上臂金属内芯
 *   + 前/外侧护板 + 内侧透光缝 → 肩臂之间的落差用一根**发光能量链接**交代("悬浮断开"变成有理由的断开)
 *   → 肘环 → 腕环 → 金手(掌背在后/掌心发光垫在前) → 3 根爪 + 指节
 */
/* ① 躯干侧插座: 把肩"接"回身体 */
cubePair('P', 'torso', 'torso', [5.4, 23.6, -2.6], [1.8, 5.0, 5.6], M.METAL);
/* ② 关节球(贴住插座外缘, 不再是悬空的球) */
cubePair('R', 'shoulder_l', 'shoulder_r', [6.4, 24.2, -2.2], [3.0, 3.5, 4.6], M.TRIM, [0, 0, 45]);
cubePair('P', 'shoulder_l', 'shoulder_r', [6.8, 24.6, -1.8], [2.2, 2.6, 3.8], M.METAL);
/* ③ 护肩: 外层大板 + 顶棱 + 内层斜板(层次感) */
cubePair('P', 'shoulder_l', 'shoulder_r', [5.2, 26.6, -3.4], [6.0, 3.4, 7.6], M.GOLD);
cubePair('P', 'shoulder_l', 'shoulder_r', [5.6, 29.4, -3.0], [5.2, 1.4, 6.8], M.TRIM);
cubePair('R', 'shoulder_l', 'shoulder_r', [5.8, 27.4, -3.6], [4.0, 2.0, 8.0], M.GOLD_DARK, [0, 0, 18]);
cubePair('I', 'shoulder_l', 'shoulder_r', [5.0, 26.2, -3.6], [6.4, 3.8, 8.0], M.GOLD, null, 0.12);
/* ④ 肩后崩口 */
cubePair('P', 'shoulder_l', 'shoulder_r', [7.4, 23.8, 2.6], [3.0, 2.2, 1.2], M.GOLD_DARK);
/* ⑤ 上臂(肩 → 肘): 金属内芯 + 前/外侧护板 + 内侧透光缝 */
cubePair('P', 'arm_l', 'arm_r', [6.6, 21.0, -1.8], [3.6, 3.4, 4.0], M.METAL);
cubePair('P', 'arm_l', 'arm_r', [6.9, 21.2, -2.5], [3.2, 2.8, 1.7], M.GOLD);
cubePair('P', 'arm_l', 'arm_r', [9.4, 21.0, -1.6], [1.2, 3.2, 3.4], M.GOLD_DARK);
cubePair('P', 'arm_l', 'arm_r', [7.7, 21.0, -1.9], [0.8, 3.2, 4.2], M.CRACK);
cubePair('P', 'arm_l', 'arm_r', [6.5, 23.6, -1.6], [3.8, 1.2, 3.8], M.TRIM);              // 上臂顶环
cubePair('P', 'shoulder_l', 'shoulder_r', [6.6, 22.6, -1.4], [2.4, 2.2, 2.8], M.CRACK);   // 肩↔臂能量链接
/* ⑥ 肘关节 v11: 上下各一环、中间留 1.2 缝 —— 前臂是**独立骨骼** forearm_l/r, 所以施法时能屈肘 */
cubePair('R', 'arm_l', 'arm_r', [6.4, 20.2, -1.9], [3.8, 1.4, 4.2], M.TRIM, [0, 0, 45]);
cubePair('R', 'forearm_l', 'forearm_r', [6.5, 19.0, -1.9], [3.6, 1.4, 4.0], M.METAL, [0, 0, 45]);
/* ⑦ 前臂(肘 → 腕) */
cubePair('P', 'forearm_l', 'forearm_r', [6.8, 16.4, -1.8], [3.4, 2.6, 3.8], M.METAL);
cubePair('P', 'forearm_l', 'forearm_r', [7.0, 16.6, -2.4], [3.0, 2.2, 1.6], M.GOLD);
cubePair('P', 'forearm_l', 'forearm_r', [7.2, 16.4, -1.9], [1.2, 2.4, 3.9], M.CRACK);
/* ⑧ 手: 金手 + 腕环(整圈) + 掌背板(后) + 掌心发光垫(前) */
cubePair('P', 'hand_l', 'hand_r', [6.9, 12.6, -2.2], [3.8, 4.0, 4.6], M.GOLD);
cubePair('P', 'hand_l', 'hand_r', [7.2, 16.0, -2.4], [3.2, 1.4, 4.8], M.TRIM);
cubePair('R', 'hand_l', 'hand_r', [7.1, 13.2, 1.4], [3.4, 3.0, 2.2], M.TRIM, [0, 0, 45]);  // 掌背(后)
cubePair('P', 'hand_l', 'hand_r', [7.4, 13.0, -3.0], [2.8, 3.0, 0.9], M.PALM);             // 掌心发光垫(前)
for (let i = 0; i < 3; i++) {
    cube(`claw_l${i + 1}`, [7.3 + i * 1.5, 8.6, -1.0 - i * 0.8], [1.1, 4.2, 1.1], M.TRIM);   // 长爪(向前收)
    cube(`claw_r${i + 1}`, [-8.4 - i * 1.5, 8.6, -1.0 - i * 0.8], [1.1, 4.2, 1.1], M.TRIM);
}
cubeR(`claw_l1`, [7.3, 10.2, -1.0], [1.3, 1.3, 4.0], M.GOLD, [0, 0, 45]);    // 指节
cubeR(`claw_l2`, [8.8, 10.2, -1.8], [1.3, 1.3, 4.0], M.GOLD, [0, 0, 45]);
cubeR(`claw_l3`, [10.3, 10.2, -2.6], [1.3, 1.3, 4.0], M.GOLD, [0, 0, 45]);
cubeR(`claw_r1`, [-8.4, 10.2, -1.0], [1.3, 1.3, 4.0], M.GOLD, [0, 0, 45]);
cubeR(`claw_r2`, [-9.9, 10.2, -1.8], [1.3, 1.3, 4.0], M.GOLD, [0, 0, 45]);
cubeR(`claw_r3`, [-11.4, 10.2, -2.6], [1.3, 1.3, 4.0], M.GOLD, [0, 0, 45]);
/* ---- 青绿披风: **只留背后那一片**(2026-09-11 按用户要求"去除第三只手"):
 * 原来左右各挂两片, 位置和手臂同高、同样垂着, 在游戏里看起来就是"第三条手臂/第三只手" —— 去掉。
 * 背后那片保持 3 段分段布料, 并加宽一点, 保证正面也能看到披风轮廓。 ---- */
/* v11 优化: 披风改 **4 段**(上窄下宽) + 一层深色内衬 —— 旧版 3 段等宽, 背面就是一块平板 */
cube('cape_back1', [-6.4, 24.0, 5.0], [12.8, 4.6, 1.6], M.CLOTH);      // 肩披(最窄)
cube('cape_back2', [-7.2, 19.6, 5.4], [14.4, 4.6, 1.5], M.CLOTH);
cube('cape_back3', [-8.2, 15.0, 5.8], [16.4, 4.8, 1.4], M.CLOTH);
cube('cape_back4', [-9.4, 9.8, 6.2], [18.8, 5.4, 1.3], M.CLOTH);       // 下摆(最宽)
cube('cape_back4', [-6.4, 7.6, 6.6], [12.8, 2.4, 1.1], M.CLOTH);       // 尾角
cube('cape_back2', [-6.8, 19.8, 4.6], [13.6, 4.4, 0.8], M.GOLD_DARK);  // 深色内衬
cube('cape_back3', [-7.8, 15.2, 5.0], [15.6, 4.6, 0.8], M.GOLD_DARK);
cube('cape_back4', [-9.0, 10.0, 5.4], [18.0, 5.2, 0.8], M.GOLD_DARK);
/* ---- 细化(2026-09-11): 散热格栅 / 背部散热片 / 关节环 / 踝甲细节 ---- */
cubeI('torso', [-6.2, 19.0, -4.6], [2.0, 3.4, 1.2], M.VENT, 0.10);     // 左胸散热格栅
cubeI('torso', [4.2, 19.0, -4.6], [2.0, 3.4, 1.2], M.VENT, 0.10);      // 右胸散热格栅
/* ---- 腰/腹/腿: 层次感(2026-09-11 按用户要求"身体层次感多一点"), 并去掉腰侧飘件 ---- */
cube('torso', [-5.0, 13.2, -3.8], [10.0, 1.2, 7.0], M.TRIM);                      // 腰环(平放)
cubePair('P', 'torso', 'torso', [5.6, 12.6, -3.0], [1.6, 2.6, 6.0], M.GOLD_DARK); // 胯侧甲(贴住腰, 不再悬空)
cube('torso', [-4.6, 15.0, -3.9], [9.2, 1.0, 0.9], M.CRACK);                      // 腹缝透光
cubePair('I', 'torso', 'torso', [-5.6, 17.4, -4.0], [5.0, 4.0, 1.4], M.GOLD, null, 0.10); // 胸甲内层(左右各半)
cube('torso', [-4.4, 22.4, -4.3], [8.8, 1.6, 1.0], M.METAL);                      // 胸口内嵌面板
cubePair('P', 'torso', 'torso', [2.6, 16.8, -4.6], [3.0, 3.0, 0.9], M.VENT);      // 腹部格栅(镜像一对)
cubeR('torso', [-4.2, 26.6, 2.2], [8.4, 2.6, 1.4], M.METAL, [0, 0, 45]);          // 背部散热片
cubeR('torso', [-3.0, 25.0, 2.4], [6.0, 1.6, 1.2], M.VENT, [0, 0, 45]);
/* 腿: 大腿前后护板 + 膝环 + 小腿护板 + 踝环(层叠) */
cubePair('P', 'leg_l', 'leg_r', [1.6, 8.6, -3.0], [3.6, 4.6, 1.4], M.GOLD);       // 大腿前护板
cubePair('P', 'leg_l', 'leg_r', [1.2, 8.6, 1.6], [4.2, 4.6, 1.2], M.GOLD_DARK);   // 大腿后护板
cubePair('R', 'leg_l', 'leg_r', [1.6, 6.6, -2.0], [3.6, 1.4, 4.0], M.TRIM, [0, 0, 45]);   // 膝环
cubePair('P', 'leg_l', 'leg_r', [1.9, 3.6, -2.5], [2.8, 3.6, 1.3], M.METAL);      // 小腿前护板
cubePair('R', 'leg_l', 'leg_r', [1.8, 2.0, -2.0], [3.4, 1.2, 3.6], M.METAL, [0, 0, 45]);  // 踝环
/* ---- 护颈环(垫在金壳与罗纹颈之间的过渡件) ---- */
cubeR('collar', [-4.4, 30.0, -4.4], [8.8, 1.8, 8.8], M.TRIM, [0, 0, 45]);
cubeI('collar', [-4.0, 30.4, -4.0], [8.0, 1.2, 8.0], M.GOLD, 0.14);

/* ---- 球槽悬浮锚点(头顶上方) ---- */
cube('orb_socket', [-2.0, 46.8, -2.0], [4.0, 1.2, 4.0], M.RIM);
cube('orb_socket', [-1.4, 47.8, -1.4], [2.8, 1.6, 2.8], M.GLASS);

/* ---------------- 4. geo(父子层级) ---------------- */
const PIVOT = {
    root: [0, 0, 0],
    leg_l: [3.7, 15, 0], leg_r: [-3.7, 15, 0],
    torso: [0, 14, 0],
    core_ring: [0, 22, -5.4],        // 静止的金属框
    core: [0, 22, -6.4],             // 自转的发光核(枢轴 = 球心 => 原地转不穿模)
    neck: [0, 28.2, 0],
    head: [0, 32.6, 0],
    jaw: [0, 33.0, 0],
    eye: [0, 37.2, -6.6], eye_small: [-1.9, 40.7, -6.6],
    crown: [0, 42.2, 0],
    shoulder_l: [8.8, 26.8, 0], shoulder_r: [-8.8, 26.8, 0],
    arm_l: [8.9, 24.6, 0], arm_r: [-8.9, 24.6, 0],       // 肩关节(转动轴心)
    forearm_l: [8.9, 20.4, 0], forearm_r: [-8.9, 20.4, 0], // 肘关节(v11 新增: 前臂可独立屈伸)
    hand_l: [9.4, 16.6, 0], hand_r: [-9.4, 16.6, 0],     // 腕关节
    fingers_l: [9.4, 13.0, 0], fingers_r: [-9.4, 13.0, 0], // 爪指总关节(本身无 cube, 只当父级)
    claw_l1: [8.3, 13.0, -1.3], claw_l2: [10.0, 13.0, -0.7], claw_l3: [11.7, 13.0, -0.1],
    claw_r1: [-8.3, 13.0, -1.3], claw_r2: [-10.0, 13.0, -0.7], claw_r3: [-11.7, 13.0, -0.1],
    cape_back1: [0, 28.6, 5.0], cape_back2: [0, 24.0, 5.4], cape_back3: [0, 19.6, 5.8], cape_back4: [0, 15.0, 6.2],
    collar: [0, 30.9, 0],
    orb_socket: [0, 47.4, 0]
};
const PARENT = {
    leg_l: 'root', leg_r: 'root', torso: 'root', orb_socket: 'root',
    core_ring: 'torso', core: 'torso', neck: 'torso', collar: 'torso',
    shoulder_l: 'torso', shoulder_r: 'torso',
    cape_back1: 'torso', cape_back2: 'cape_back1', cape_back3: 'cape_back2', cape_back4: 'cape_back3',
    head: 'neck', jaw: 'head', eye: 'head', eye_small: 'head', crown: 'head',
    arm_l: 'shoulder_l', arm_r: 'shoulder_r',
    forearm_l: 'arm_l', forearm_r: 'arm_r',
    hand_l: 'forearm_l', hand_r: 'forearm_r',
    fingers_l: 'hand_l', fingers_r: 'hand_r',
    claw_l1: 'fingers_l', claw_l2: 'fingers_l', claw_l3: 'fingers_l',
    claw_r1: 'fingers_r', claw_r2: 'fingers_r', claw_r3: 'fingers_r'
};
const bones = Object.keys(PIVOT).map(name => {
    const cs = C.filter(c => c.bone === name).map(c => {
        const out = { origin: c.o, size: c.s, uv: c.uv };
        if (c.rot) out.rotation = c.rot;      // 斜置/倒角 cube(YSM 手法)
        if (c.inflate) out.inflate = c.inflate; // 外壳层(YSM 手法)
        return out;
    });
    const b = { name, pivot: PIVOT[name] };
    if (cs.length) b.cubes = cs;      // 纯关节骨骼(如 fingers_l)不写空 cubes 数组
    if (PARENT[name]) b.parent = PARENT[name];
    return b;
});
const geo = {
    format_version: '1.12.0',
    'minecraft:geometry': [{
        description: {
            identifier: `geometry.${NAME}`,
            texture_width: TEX_W, texture_height: TEX_H,
            visible_bounds_width: 4, visible_bounds_height: 5, visible_bounds_offset: [0, 1.4, 0]
        },
        bones
    }]
};

/* ---------------- 5. 动画(名字与实体代码一致: animation.defect_core.*) ----------------
 * 2026-09-11 按用户要求"动画做完善": idle 加入呼吸/摆首/披风相位差/核心持续自转;
 * 新增 phase_shift(阶段突破) ; cast/hurt/death 都加了手臂·手·披风·残冠的联动。 */
/* ---------------- 5. 动画 ----------------
 * 2026-09-11 重做: 动画数据搬到 defect_core_anims.cjs(手写关键帧, 30 骨骼全参与,
 * 新增 float_move / echo), 单独一个文件便于反复调姿势不用碰几何。 */
const anims = { format_version: '1.8.0', animations: require('./defect_core_anims.cjs') };

/* ---------------- 6. 写出(含 glowmask) ---------------- */
fs.mkdirSync(OUT, { recursive: true });
fs.writeFileSync(path.join(OUT, `${NAME}.geo.json`), JSON.stringify(geo, null, 1));
fs.writeFileSync(path.join(OUT, `${NAME}.animation.json`), JSON.stringify(anims, null, 1));

const GLOW_MATS = new Set([M.GLASS, M.RIM, M.CRACK, M.CROWN, M.CORE, M.CORE_CRACK]);
/** 逐球种出贴图: <name>_<type>.png 及其 _glowmask.png —— 只有胸口核心颜色不同(方块方案下用它实现"核心变色") */
for (const [key, tint] of Object.entries(CORE_TINTS)) {
    coreRgb = tint;
    coreCrackRgb = [Math.min(255, tint[0] * 0.55 + 120), Math.min(255, tint[1] * 0.55 + 120), Math.min(255, tint[2] * 0.55 + 120)];
    paintPass();
    const base = `${NAME}_${key}`;
    cv.save(path.join(OUT, `${base}.png`));
    const glow = new Canvas(TEX_W, TEX_H);
    for (let y = 0; y < TEX_H; y++) for (let x = 0; x < TEX_W; x++) {
        const i = y * TEX_W + x;
        if (!GLOW_MATS.has(matMask[i])) continue;
        glow.set(x, y, cv.d[i * 4], cv.d[i * 4 + 1], cv.d[i * 4 + 2], 1);
    }
    glow.save(path.join(OUT, `${base}_glowmask.png`));
}
coreRgb = CORE_TINTS.glass;
coreCrackRgb = [220, 250, 255];
paintPass();
cv.save(path.join(OUT, `${NAME}.png`));
const g0 = new Canvas(TEX_W, TEX_H);
let glowPx = 0;
for (let y = 0; y < TEX_H; y++) for (let x = 0; x < TEX_W; x++) {
    const i = y * TEX_W + x;
    if (!GLOW_MATS.has(matMask[i])) continue;
    g0.set(x, y, cv.d[i * 4], cv.d[i * 4 + 1], cv.d[i * 4 + 2], 1);
    glowPx++;
}
g0.save(path.join(OUT, `${NAME}_glowmask.png`));
console.log(`已生成 ${NAME}: cube=${C.length} 骨骼=${bones.length} 贴图=${TEX_W}x${TEX_H} 核心变体=${Object.keys(CORE_TINTS).length} 发光像素=${glowPx}`);