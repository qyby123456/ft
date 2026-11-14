// 为 GeckoLib Boss 补齐铁魔法(Iron's Spells)期望的施法动画名
//   来源: irons_spellbooks 的 SpellAnimations / HumanoidRenderer 会按名查找 GeckoLib 动画,
//         找不到就刷 "Unable to find animation: xxx"。这里把缺失的名字补上, 用各 Boss 自己的骨骼。
// 用法: node gen_cast_anims.cjs <animation.json> <preset>
const fs = require('fs');
const file = process.argv[2];
const preset = process.argv[3];
const j = JSON.parse(fs.readFileSync(file, 'utf8'));
if (!j.animations) j.animations = {};

/* 铁魔法会查找的全部名字(缺一不可, 否则日志会刷) */
const IRON_NAMES = ['spell_cast', 'long_cast', 'long_cast_finish', 'instant_self',
  'instant_projectile', 'charged_throw', 'charge_spit_finish', 'cast_t_pose',
  'self_cast_two_hands', 'throw_item', 'blank'];

/* ---------- 预设: 姜饼人国王(人形, 有双臂与权杖) ---------- */
const GINGER = {
  blank: { loop: false, len: 0.1, bones: {} },
  spell_cast: { loop: false, len: 0.8, bones: {
    arm_l: { rotation: { '0': [0, 0, 0], '0.3': [-58, 0, -10], '0.8': [-58, 0, -10] } },
    arm_r: { rotation: { '0': [0, 0, 0], '0.3': [-58, 0, 10], '0.8': [-58, 0, 10] } },
    body: { rotation: { '0': [0, 0, 0], '0.3': [5, 0, 0], '0.8': [5, 0, 0] } },
    cape: { rotation: { '0': [0, 0, 0], '0.3': [9, 0, 0], '0.8': [9, 0, 0] } }
  } },
  long_cast: { loop: true, len: 1.2, bones: {
    arm_l: { rotation: { '0': [0, 0, 0], '0.4': [-72, 0, -14], '1.2': [-72, 0, -14] } },
    arm_r: { rotation: { '0': [0, 0, 0], '0.4': [-72, 0, 14], '1.2': [-72, 0, 14] } },
    scepter: { rotation: { '0': [0, 0, 0], '0.4': [-18, 0, 0], '1.2': [-18, 0, 0] } },
    head: { rotation: { '0': [0, 0, 0], '0.4': [-7, 0, 0], '1.2': [-7, 0, 0] } },
    body: { rotation: { '0': [0, 0, 0], '0.4': [7, 0, 0], '1.2': [7, 0, 0] } },
    cape: { rotation: { '0': [0, 0, 0], '0.4': [12, 0, 0], '1.2': [12, 0, 0] } }
  } },
  long_cast_finish: { loop: false, len: 0.4, bones: {
    arm_l: { rotation: { '0': [-72, 0, -14], '0.4': [-16, 0, -4] } },
    arm_r: { rotation: { '0': [-72, 0, 14], '0.4': [-16, 0, 4] } },
    body: { rotation: { '0': [7, 0, 0], '0.4': [-9, 0, 0] } },
    cape: { rotation: { '0': [12, 0, 0], '0.4': [-6, 0, 0] } }
  } },
  instant_self: { loop: false, len: 0.5, bones: {
    arm_l: { rotation: { '0': [0, 0, 0], '0.25': [-34, 0, -22], '0.5': [0, 0, 0] } },
    arm_r: { rotation: { '0': [0, 0, 0], '0.25': [-34, 0, 22], '0.5': [0, 0, 0] } },
    body: { rotation: { '0': [0, 0, 0], '0.25': [-4, 0, 0], '0.5': [0, 0, 0] } }
  } },
  instant_projectile: { loop: false, len: 0.5, bones: {
    arm_r: { rotation: { '0': [0, 0, 0], '0.15': [-88, 0, 8], '0.5': [-20, 0, 4] } },
    arm_l: { rotation: { '0': [0, 0, 0], '0.15': [-24, 0, -6], '0.5': [-8, 0, 0] } },
    body: { rotation: { '0': [0, 0, 0], '0.15': [4, 0, -6], '0.5': [0, 0, 0] } }
  } },
  charged_throw: { loop: 'hold_on_last_frame', len: 1.6, bones: {
    arm_r: { rotation: { '0': [0, 0, 0], '0.7': [-128, 0, 18], '1.2': [-38, 0, 10], '1.6': [-24, 0, 6] } },
    arm_l: { rotation: { '0': [0, 0, 0], '0.7': [-30, 0, -14], '1.6': [-14, 0, -6] } },
    scepter: { rotation: { '0': [0, 0, 0], '0.7': [-26, 0, 0], '1.6': [8, 0, 0] } },
    head: { rotation: { '0': [0, 0, 0], '0.7': [-10, 0, 0], '1.6': [6, 0, 0] } },
    body: { rotation: { '0': [0, 0, 0], '0.7': [10, 0, 0], '1.6': [-12, 0, 0] } },
    cape: { rotation: { '0': [0, 0, 0], '0.7': [16, 0, 0], '1.6': [-8, 0, 0] } }
  } },
  charge_spit_finish: { loop: false, len: 0.3, bones: {
    head: { rotation: { '0': [0, 0, 0], '0.15': [16, 0, 0], '0.3': [0, 0, 0] } },
    body: { rotation: { '0': [0, 0, 0], '0.15': [6, 0, 0], '0.3': [0, 0, 0] } }
  } },
  cast_t_pose: { loop: 'hold_on_last_frame', len: 0.5, bones: {
    arm_l: { rotation: { '0': [0, 0, 0], '0.3': [0, 0, -78], '0.5': [0, 0, -78] } },
    arm_r: { rotation: { '0': [0, 0, 0], '0.3': [0, 0, 78], '0.5': [0, 0, 78] } }
  } },
  self_cast_two_hands: { loop: false, len: 1.0, bones: {
    arm_l: { rotation: { '0': [0, 0, 0], '0.4': [-104, 0, -16], '1.0': [-96, 0, -16] } },
    arm_r: { rotation: { '0': [0, 0, 0], '0.4': [-104, 0, 16], '1.0': [-96, 0, 16] } },
    head: { rotation: { '0': [0, 0, 0], '0.4': [-12, 0, 0], '1.0': [-12, 0, 0] } },
    cape: { rotation: { '0': [0, 0, 0], '0.4': [10, 0, 0], '1.0': [10, 0, 0] } }
  } },
  throw_item: { loop: false, len: 0.6, bones: {
    arm_r: { rotation: { '0': [0, 0, 0], '0.2': [-140, 0, 16], '0.45': [-30, 0, 8], '0.6': [-10, 0, 4] } },
    body: { rotation: { '0': [0, 0, 0], '0.2': [8, 0, 0], '0.6': [-10, 0, 0] } }
  } }
};

/* ---------- 预设: 酒桶王(桶形, 靠木塞/酒液/整体倾斜表达) ---------- */
const BARREL = {
  blank: { loop: false, len: 0.1, bones: {} },
  spell_cast: { loop: false, len: 0.8, bones: {
    body: { rotation: { '0': [0, 0, 0], '0.3': [-7, 0, 0], '0.8': [-7, 0, 0] } },
    wine_glow: { scale: { '0': [1, 1, 1], '0.3': [1.15, 1.15, 1.15], '0.8': [1.15, 1.15, 1.15] } }
  } },
  charged_throw: { loop: 'hold_on_last_frame', len: 2.0, bones: {
    body: { rotation: { '0': [0, 0, 0], '0.9': [-14, 0, 0], '1.5': [9, 0, 0], '2.0': [4, 0, 0] } },
    cork: { position: { '0': [0, 0, 0], '0.9': [0, 0.4, 1.6], '1.5': [0, 0, -5], '2.0': [0, 0, -2] } },
    cork_handle: { rotation: { '0': [0, 0, 0], '0.9': [0, 0, 30], '2.0': [0, 0, 30] } },
    wine_glow: { scale: { '0': [1, 1, 1], '0.9': [1.3, 1.3, 1.3], '1.5': [1.5, 1.5, 1.5], '2.0': [1.2, 1.2, 1.2] } }
  } },
  charge_spit_finish: { loop: false, len: 0.3, bones: {
    body: { rotation: { '0': [0, 0, 0], '0.15': [-10, 0, 0], '0.3': [0, 0, 0] } },
    cork: { position: { '0': [0, 0, 0], '0.15': [0, 0, -6], '0.3': [0, 0, 0] } }
  } },
  cast_t_pose: { loop: 'hold_on_last_frame', len: 0.5, bones: {
    root: { rotation: { '0': [0, 0, 0], '0.3': [0, 0, 0], '0.5': [0, 0, 0] } }
  } },
  self_cast_two_hands: { loop: false, len: 1.0, bones: {
    body: { rotation: { '0': [0, 0, 0], '0.4': [6, 0, 0], '1.0': [6, 0, 0] } },
    wine_glow: { scale: { '0': [1, 1, 1], '0.4': [1.4, 1.4, 1.4], '1.0': [1.35, 1.35, 1.35] } },
    cork: { position: { '0': [0, 0, 0], '0.4': [0, 0.6, 0], '1.0': [0, 0.6, 0] } }
  } },
  throw_item: { loop: false, len: 0.6, bones: {
    body: { rotation: { '0': [0, 0, 0], '0.2': [-22, 0, 0], '0.6': [8, 0, 0] } },
    root: { position: { '0': [0, 0, 0], '0.2': [0, 1.5, 0], '0.6': [0, 0, 0] } }
  } }
};

const PRESETS = { gingerbread: GINGER, barrel: BARREL };
const presetDef = PRESETS[preset];
if (!presetDef) { console.error('未知预设: ' + preset + '  (可用: ' + Object.keys(PRESETS).join(', ') + ')'); process.exit(1); }

const added = [], kept = [];
for (const name of IRON_NAMES) {
  if (j.animations[name]) { kept.push(name); continue; }
  const def = presetDef[name];
  if (!def) { console.warn('  预设里没有 ' + name); continue; }
  j.animations[name] = {
    loop: def.loop,
    animation_length: def.len,
    bones: def.bones
  };
  added.push(name);
}
fs.writeFileSync(file, JSON.stringify(j, null, 1));
console.log(`文件: ${file}`);
console.log(`  铁魔法动画名共 ${IRON_NAMES.length} 个: 新增 ${added.length} 个 [${added.join(', ')}], 已有保留 ${kept.length} 个 [${kept.join(', ')}]`);
console.log(`  动画总数: ${Object.keys(j.animations).length}`);
