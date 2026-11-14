const fs = require('node:fs');
const path = require('node:path');
const { spawnSync } = require('node:child_process');

const root = path.resolve(__dirname, '..');
const output = path.join(root, 'build/reports/verification');
fs.mkdirSync(output, { recursive: true });
const checks = [
  ['project-layout', 'tools/verification/check_project_layout.cjs'],
  ['food-weapon-summaries', 'tools/verification/check_food_weapon_summaries.cjs'],
  ['sparrow-schedule', 'tools/verification/check_sparrow_schedule.cjs'],
  ['manor-chase-sparrow', 'tools/verification/check_manor_chase_sparrow.cjs'],
  ['weapon-inspect', 'tools/verification/check_weapon_inspect.cjs'],
  ['dora-weapon-models', 'tools/verification/check_dora_weapon_models.cjs'],
  ['manor-iron-spells', 'tools/verification/check_manor_iron_spells.cjs'],
  ['boss-iron-spells', 'tools/verification/check_boss_iron_spells.cjs'],
  ['xuanlan-blades', 'tools/verification/check_xuanlan_blades.cjs'],
  ['xuanlan-motion', 'tools/verification/check_xuanlan_motion.cjs'],
  ['xuanlan-client', 'tools/verification/check_xuanlan_client.cjs'],
  ['performance', 'tools/verification/check_performance.cjs'],
  ['tide-fish', 'tools/verification/check_tide_fish.cjs'],
  ['manor-teleport', 'tools/verification/check_manor_teleport.cjs'],
  ['dead-eat-guard', 'tools/verification/check_dead_eat_guard.cjs'],
  ['knockback-lemon', 'tools/verification/check_knockback_and_lemon.cjs'],
  ['lemon-summon-sacrifice', 'tools/verification/check_lemon_summon_sacrifice.cjs'],
  ['weapon-drops', 'tools/verification/check_boss_weapon_drops.cjs'],
  ['forbidden-apple-recipe', 'tools/verification/check_forbidden_apple_recipe.cjs'],
  ['compost-weapon', 'tools/verification/check_compost_weapon.cjs'],
  ['forbidden-apple-pack', 'tools/verification/check_forbidden_apple_pack.cjs'],
  ['easter-egg-advancement', 'tools/verification/check_easter_egg_advancement.cjs'],
  ['manor-no-giant', 'tools/verification/check_manor_no_giant.cjs'],
  ['spell-scroll-bonus', 'tools/verification/check_spell_scroll_bonus.cjs'],
  ['boss-cast-time', 'tools/verification/check_boss_cast_time.cjs'],
  ['trade-config', 'tools/verification/check_trade_config.cjs'],
  ['xuanlan-beam', 'tools/verification/check_xuanlan_beam.cjs'],
  ['qizai-sweep', 'tools/verification/check_qizai_sweep.cjs'],
  ['qizai-staff-pickup', 'tools/verification/check_qizai_staff_pickup.cjs'],
  ['qizai-buffs', 'tools/verification/check_qizai_buffs.cjs'],
  ['qizai-liveness', 'tools/verification/check_qizai_spell_liveness.cjs'],
  ['qingyi-nerf', 'tools/verification/check_qingyi_nerf.cjs'],
  ['zheergen-nerf', 'tools/verification/check_zheergen_nerf.cjs'],
  ['dora-xueli-boost', 'tools/verification/check_dora_xueli_boost.cjs'],
  ['qizai-final', 'tools/verification/check_qizai_final_difficulty.cjs'],
  ['manor-player-damage', 'tools/verification/check_manor_player_damage.cjs'],
  ['assets', 'tools/model3d/check_assets.cjs', 'src/main/resources'],
  ['models', 'tools/model3d/validate.cjs', '--all'],
  ['animations', 'tools/model3d/validate_anim.cjs', '--all'],
  ['regular-boss-cast-animations', 'tools/verification/check_regular_boss_cast_animations.cjs'],
  ['weapon-evolution-skill-pool', 'tools/verification/check_weapon_evolution_skill_pool.cjs'],
  ['statue-hub-ui', 'tools/verification/check_statue_hub_ui.cjs'],
  ['evolution-reset-details', 'tools/verification/check_evolution_reset_details.cjs'],
  ['food-blessing-offerings', 'tools/verification/check_food_blessing_offerings.cjs'],
  ['non-manor-finale', 'tools/verification/check_non_manor_finale.cjs'],
  ['removed-items', 'tools/verification/check_removed_items.cjs'],
  ['retired-gourmet-arsenal', 'tools/verification/check_retired_gourmet_arsenal.cjs'],
  ['gourmet-blank-blade', 'tools/verification/check_gourmet_blank_blade.cjs'],
  ['model-cache', 'tools/verification/check_model_resources.cjs'],
  /* ★ 2026-09-24 加：jar 的 zip 条目必须能被 Forge 的 securejarhandler 读（"STORED + data descriptor"
     会让 ModLauncher 在打出第一行 Forge 日志之前直接退出；详见该脚本头部与 CHANGELOG 的构建合并一节） */
  ['jar-zip-rules', 'tools/verification/check_jar_zip_rules.cjs'],
  /* ★ 2026-09-24 加：爱意滑翔飞行必须先注册 Attributes.FLYING_SPEED（缺了就是服务端崩溃，
     见 check_haley_flight.cjs 头部与 CHANGELOG） */
  ['haley-flight', 'tools/verification/check_haley_flight.cjs'],
  /* ★ 2026-09-24 加：黑切右键裂地斩必须清无敌帧后结算，否则"打中了但血怒·伪不涨"
     （见 check_cleaver_slam_rage.cjs 头部） */
  ['cleaver-slam-rage', 'tools/verification/check_cleaver_slam_rage.cjs'],
  /* ★ 2026-09-24 加：允许 Boss 砸"与自己同水平段"的基岩，但世界底层/上层基岩仍不动
     （见 check_bedrock_break.cjs 头部） */
  ['bedrock-break', 'tools/verification/check_bedrock_break.cjs'],
  /* ★ 2026-09-24 加：TnS 附赠条目（血腥契约 + 其余 6 把三叉戟 + 6 把长矛）**只进创造栏**，
     不许并进 ENTRIES（那会连带进庄园凯旋宝箱）—— 见 check_tns_creative_extras.cjs 头部 */
  ['tns-creative-extras', 'tools/verification/check_tns_creative_extras.cjs'],
  /* ★ 2026-09-24 加：庄园禁掉止心术（两档难度）与饱腹代偿（两档难度），
     橡肤/滋养/饱腹的原有难度边界不动 —— 见 check_manor_spell_bans.cjs 头部 */
  ['manor-spell-bans', 'tools/verification/check_manor_spell_bans.cjs'],
  /* ★ 2026-09-24 加：血腥契约**只在对敌对生物造成伤害时吸血**（两套回血档位 + 敌对判据三条 +
     两条命中路径 + 注册路径 + 合并清单多方核对）—— 见 check_sanguine_lifesteal.cjs 头部 */
  ['sanguine-lifesteal', 'tools/verification/check_sanguine_lifesteal.cjs'],
  /* ★ 2026-09-24 加：糖霜女皇每波召唤减半 + 并发上限（实战日志：一场 230 波 / 同时百只在场，
     玩家被小姜饼人连杀 5 次触发怜悯清场）—— 见 check_sugar_queen_summons.cjs 头部 */
  ['sugar-queen-summons', 'tools/verification/check_sugar_queen_summons.cjs'],
  /* ★ 2026-09-24 加：庄园 boss 战场地区块常加载（自定义票据，五个生命周期挂点）
     —— 见 check_manor_arena_chunks.cjs 头部 */
  ['manor-arena-chunks', 'tools/verification/check_manor_arena_chunks.cjs'],
  /* ★ 2026-09-24 加：折耳根「暗影突袭」——传送目标放开到本场参战者（原来只认召唤者，
     多人局整场闪不出来）+ 技能冷却单独给值绕过近战位 ×2 —— 见 check_zheergen_shadow_teleport.cjs */
  ['zheergen-shadow-teleport', 'tools/verification/check_zheergen_shadow_teleport.cjs'],
  /* ★ 2026-09-24 加：折耳根传送后 10% 留虚影，延迟复现一次技能再消散
     （虚影走 mini 体系、无敌 NoAI、半透明渲染、效果与本体共用一份）—— 见 check_zheergen_phantom.cjs */
  ['zheergen-phantom', 'tools/verification/check_zheergen_phantom.cjs'],
  /* ★ 2026-09-24 加：提高 boss 水中移速（瞬时修饰符 + 上限同步放宽 + 彩蛋系水阻尼 0.96）
     —— 见 check_boss_water_speed.cjs 头部 */
  ['boss-water-speed', 'tools/verification/check_boss_water_speed.cjs'],
  /* ★ 2026-09-28 加：自动清除玩家的无敌 buff 与超 20 分钟的 buff（两道闸 + 豁免顺序 + 无限时长）
     —— 见 check_player_buff_guard.cjs 头部 */
  ['player-buff-guard', 'tools/verification/check_player_buff_guard.cjs']
];
let failures = 0;
for (const [name, ...args] of checks) {
  const result = spawnSync(process.execPath, args, { cwd: root, encoding: 'utf8', maxBuffer: 16 * 1024 * 1024 });
  const log = (result.stdout || '') + (result.stderr || '') + (result.error ? result.error.stack : '');
  fs.writeFileSync(path.join(output, name + '.log'), log);
  const passed = result.status === 0;
  console.log(`${passed ? 'PASS' : 'FAIL'} ${name}: build/reports/verification/${name}.log`);
  if (!passed) {
    failures++;
    console.error(log.slice(-4000));
  }
}
process.exitCode = failures === 0 ? 0 : 1;

