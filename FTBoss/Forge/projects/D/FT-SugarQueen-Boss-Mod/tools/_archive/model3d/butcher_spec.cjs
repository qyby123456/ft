// 屠夫模型规格(唯一真源): gen_butcher.cjs 出 Java 类+贴图, fit_check.cjs 按动画姿势查穿模
// 坐标系: Java 模型空间 —— y 向下, 脚底 y=24, 前端 = -Z, +X = 实体左侧
// pivot  : 相对模型原点
// boxes  : 相对该骨骼 pivot (x,y,z = 最小角)
// 骨骼层级: root -> body -> {head, arm_l, arm_r}; root -> {leg_l, leg_r}
const SPEC = [
  { name: 'root', parent: null, pivot: [0, 24, 0], boxes: [] },
  {
    name: 'body', parent: 'root', pivot: [0, 3, 0], boxes: [
      // 躯干压到 abs -3..10(原来到 19), 否则会把腿埋进身体里
      { x: -9.5, y: -6, z: -7.0, w: 19, h: 13, d: 14.5, mat: 'shirt' },   // 躯干 abs -3..10
      { x: -10, y: -8, z: -7.0, w: 20, h: 6, d: 15, mat: 'shirt' },       // 宽厚肩 abs -5..1 (23->20: 原来比躯干宽, 手臂插进肩里)
      { x: -4, y: -8, z: -4.0, w: 8, h: 3, d: 8, mat: 'skin' },           // 脖颈 abs -5..-2
      { x: -8.5, y: 1, z: -8.0, w: 17, h: 6, d: 16.5, mat: 'shirt' },     // 肚腩 abs 4..10
      { x: -9.5, y: 4, z: -7.5, w: 19, h: 3, d: 15, mat: 'leather' },     // 腰带 abs 7..10 (20->19: 与手臂留出间隙)
      // 围裙/口袋/挂钩整体上移收短: 走路时大腿前摆弧线在 abs 12 以下, 原来围裙垂到 abs 12 会被腿穿过去
      { x: -7, y: -4, z: -8.6, w: 14, h: 10, d: 1.5, mat: 'apron' },      // 围裙 abs -1..9
      { x: -4.5, y: 2, z: -9.2, w: 9, h: 4, d: 1.0, mat: 'apron' },       // 围裙口袋 abs 5..9
      { x: -8, y: 7, z: -7.8, w: 2, h: 5, d: 2, mat: 'metal' },           // 挂钩 abs 10..15 (挂在腰带下沿, 让开腿)
      { x: 6, y: 7, z: -7.8, w: 2, h: 5, d: 2, mat: 'metal' }             // 挂钩 abs 10..15
    ]
  },
  {
    name: 'head', parent: 'body', pivot: [0, -2, 0], boxes: [
      { x: -6, y: -10, z: -6.0, w: 12, h: 10, d: 12, mat: 'skin_head' },   // 头 abs -12..-2
      { x: -6.5, y: -13, z: -6.5, w: 13, h: 3, d: 13, mat: 'cap' },       // 布帽 abs -15..-12
      { x: -6.5, y: -10.5, z: -8.0, w: 13, h: 1.5, d: 3, mat: 'cap' },    // 帽檐
      { x: -5.5, y: -4, z: -7.0, w: 11, h: 2, d: 1.5, mat: 'skin_dark' }, // 眉骨
      { x: -4.5, y: -2, z: -7.5, w: 9, h: 2.5, d: 2.5, mat: 'skin_dark' } // 下颌
    ]
  },
  {
    name: 'arm_l', parent: 'body', pivot: [12, -3, 0], mirror: false, boxes: [
      { x: -3.0, y: -1, z: -4.0, w: 6, h: 12, d: 8, mat: 'shirt' },       // 上臂 abs -4..8 (枢轴 11->12, 宽 7->6: 消除插进躯干)
      { x: -3.0, y: 8, z: -3.5, w: 6, h: 8, d: 7, mat: 'skin' },          // 前臂 abs 8..16 (上移 2: 原来手垂到 abs 21 会压进靴子)
      { x: -3.0, y: 15, z: -3.0, w: 6, h: 4, d: 6, mat: 'skin' }          // 手 abs 15..19
    ]
  },
  {
    name: 'arm_r', parent: 'body', pivot: [-12, -3, 0], mirror: true, boxes: [
      { x: -3.0, y: -1, z: -4.0, w: 6, h: 12, d: 8, mat: 'shirt' },
      { x: -3.0, y: 8, z: -3.5, w: 6, h: 8, d: 7, mat: 'skin' },
      { x: -3.0, y: 15, z: -3.0, w: 6, h: 4, d: 6, mat: 'skin' }
    ]
  },
  {
    // 髋部上提到 abs 10, 腿长 14 单位(0.875 格) 可见 —— 原来只有 5 单位, 看着像缩进身体里
    name: 'leg_l', parent: 'root', pivot: [5, 10, 0], mirror: false, boxes: [
      { x: -4.0, y: 0, z: -4.5, w: 8, h: 10, d: 9, mat: 'trouser' },      // 大腿 abs 10..20 (前后各收 1: 减小前摆扫过的弧)
      { x: -4.5, y: 9, z: -5.0, w: 9, h: 5, d: 10, mat: 'boot' }          // 靴 abs 19..24
    ]
  },
  {
    name: 'leg_r', parent: 'root', pivot: [-5, 10, 0], mirror: true, boxes: [
      { x: -4.0, y: 0, z: -4.5, w: 8, h: 10, d: 9, mat: 'trouser' },
      { x: -4.5, y: 9, z: -5.0, w: 9, h: 5, d: 10, mat: 'boot' }
    ]
  }
];

/**
 * setupAnim() 的姿势(Java 空间, 弧度 / 单位)。
 * 与 ButcherBossModel.setupAnim 里的公式一一对应, 用来在动画幅度下查穿模 —— 静态姿势查不出这些。
 * limbSwingAmount 取 0.6(代码里的封顶值), 也就是摆幅最大时。
 */
const POSES = {
  // cos(sw)=1: 左腿前摆/右腿后摆, 双臂反向
  walk_a: { leg_l: [0.69, 0, 0], leg_r: [-0.69, 0, 0], arm_l: [-0.51, 0, -0.07], arm_r: [0.33, 0, 0.07], body: [0.10, 0, 0], head: [0.12, 0, 0] },
  // cos(sw)=-1: 反向
  walk_b: { leg_l: [-0.69, 0, 0], leg_r: [0.69, 0, 0], arm_l: [0.51, 0, -0.07], arm_r: [-0.33, 0, 0.07], body: [0.10, 0, 0], head: [0.12, 0, 0] },
  // 挥砍峰值: arm_r 抬到最高 + 身体前压
  attack: { leg_l: [0.69, 0, 0], leg_r: [-0.69, 0, 0], arm_l: [-0.86, 0, -0.07], arm_r: [-1.22, -0.35, 0.07], body: [0.28, 0.12, 0], head: [0.12, 0, 0] },
  // 呼吸最低点(body.y 基准 3.0 - 0.30)
  breathe_low: { body: [0.09, 0, 0], head: [0.12, 0, 0], bodyDy: 0.30 },
  // 抬头/低头极值
  look_up: { body: [0.10, 0, 0], head: [-0.48, 0, 0] },
  look_down: { body: [0.10, 0, 0], head: [0.72, 0, 0] }
};

module.exports = { SPEC, POSES };
