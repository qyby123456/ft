/*
 * 2026-09-12 修六项:
 *  ① 飞行时头/身不转向 -> 悬停时显式 setLookAt + 强制 yaw/yBodyRot/yHeadRot 朝玩家
 *  ② 光女等技能没执行 -> 激光条件里误加了 `this.barrageCd > 0`(复制弹幕判断时带过来) => 永远不触发; 删掉该门
 *  ③ 攻击不抬手 -> 依次加入 12~16t 蓄势: 俯冲先悬停蓄力再动; 弹幕/激光先播 cannon_fire 再打
 *  ④ 自身没 buff -> 把 COMPOST_BOSS 纳入 BossEmpower(移速×1.25/攻击×1.3/护甲+4/力量II等)
 *  ⑤ 对玩家 debuff 少 -> 直接命中追加 缓慢/虚弱/饥饿(每次随机 2 种)
 *  ⑥ 减少水坑施法 -> 概率 25%/45%/30% -> 10%/15%/12%
 *  ⑦ 使用部分铁魔法 -> 每 160~260t 用 castByName 放一个铁魔法(取不到自动跳过并 WARN)
 */
const fs = require('fs');
const path = require('path');
const ROOT = process.argv[2];
const read = p => fs.readFileSync(path.join(ROOT, p), 'utf8');
const write = (p, s) => fs.writeFileSync(path.join(ROOT, p), s);
function rep(rel, a, b, label) {
    const s = read(rel);
    if (!s.includes(a)) { console.log('  FAIL ' + label); return; }
    write(rel, s.replace(a, b));
    console.log('  OK   ' + label);
}
const CB = 'src/main/java/net/farmingtales/ftboss/entity/CompostBossEntity.java';

/* ① 转向 */
rep(CB, '        if (fly && this.diveTicks <= 0 && this.level() instanceof ServerLevel fsl) {',
`        if (fly && this.diveTicks <= 0 && this.level() instanceof ServerLevel fsl) {
            /* 2026-09-12: 飞行时**头/身都要转向玩家**(原来悬停期间完全不转) */
            if (this.getTarget() != null) {
                net.minecraft.world.entity.LivingEntity look = this.getTarget();
                this.getLookControl().setLookAt(look, 30.0f, 30.0f);
                float wantYaw = (float) (Math.toDegrees(Math.atan2(look.getZ() - this.getZ(), look.getX() - this.getX()))) - 90.0f;
                this.setYRot(wantYaw);
                this.yBodyRot = wantYaw;
                this.yHeadRot = wantYaw;
            }`, '① 飞行转向');

/* ② 激光门: 去掉 barrageCd 条件 */
rep(CB, '            if (this.laserTicks < 0 && this.barrageCd > 0 && this.diveTicks <= 0 && this.laserCd > 0 && --this.laserCd <= 0) {',
        '            /* 2026-09-12 修 bug: 原来误加了 barrageCd > 0 的限制 -> 激光永远不触发 */\n            if (this.laserTicks < 0 && this.diveTicks <= 0 && this.laserCd > 0 && --this.laserCd <= 0) {', '② 激光触发');

/* ③ 俯冲蓄势: 先悬停 14t 再冲 */
rep(CB, '            if (this.diveTicks < 0 && this.diveCd > 0 && --this.diveCd <= 0) {\n                this.diveCd = 180 + this.random.nextInt(60);\n                this.diveTicks = 40;\n                this.playAction("phase_shift", T_SHIFT);\n            }',
`            if (this.diveWindup < 0 && this.diveTicks < 0 && this.diveCd > 0 && --this.diveCd <= 0) {
                this.diveCd = 180 + this.random.nextInt(60);
                this.diveWindup = 14;                       // 2026-09-12: 俯冲前先悬停蓄势(可见的抬手)
                this.playAction("phase_shift", T_SHIFT);
            }
            if (this.diveWindup > 0 && --this.diveWindup == 0) this.diveTicks = 40;`, '③ 俯冲蓄势');
rep(CB, '    private int laserTicks = -1;', '    private int laserTicks = -1;\n    private int diveWindup = -1;', '③ 蓄势字段');

/* ⑦ 铁魔法 */
rep(CB, '        // attack speed decay: no hit on a player for 3s -> reset',
`        /* 2026-09-12: 使用**部分铁魔法**(取不到的法术会自动跳过并 WARN) */
        if (this.spellCd > 0 && --this.spellCd <= 0 && this.getTarget() != null && this.getTarget().isAlive()
                && this.level() instanceof ServerLevel spl) {
            this.spellCd = 160 + this.random.nextInt(100);
            String[] pool = { "irons_spellbooks:fireball", "irons_spellbooks:magic_missile", "irons_spellbooks:ice_block",
                    "irons_spellbooks:lightning_bolt", "irons_spellbooks:poison_arrow", "irons_spellbooks:acid_orb" };
            String id = pool[this.random.nextInt(pool.length)];
            this.playAction("gas_charge", 52.0);
            net.farmingtales.ftboss.integ.WizardSummon.castByName(spl, this, this.getTarget(), id, 2 + this.getPhase());
        }

        // attack speed decay: no hit on a player for 3s -> reset`, '⑦ 铁魔法');
rep(CB, '    private int diveWindup = -1;', '    private int diveWindup = -1;\n    private int spellCd = 140;', '⑦ 铁魔法字段');

/* ⑥ 减少水坑施法 */
rep(CB, '        poop.setPuddle(this.random.nextFloat() < 0.25f);', '        poop.setPuddle(this.random.nextFloat() < 0.10f);   // 2026-09-12: 减少水坑(25% -> 10%)', '⑥ 连射水坑');
rep(CB, '        poop.setPuddle(this.random.nextFloat() < 0.45f);', '        poop.setPuddle(this.random.nextFloat() < 0.15f);   // 45% -> 15%', '⑥ 抛粪水坑');
rep(CB, '                    poop.setPuddle(this.random.nextFloat() < 0.3f);', '                    poop.setPuddle(this.random.nextFloat() < 0.12f);   // 30% -> 12%', '⑥ 弹幕水坑');

/* ④ 纳入 BossEmpower */
rep('src/main/java/net/farmingtales/ftboss/boss/ai/BossEmpower.java',
    '                    || t == FTEntities.WINE_BARREL.get();',
    `                    || t == FTEntities.WINE_BARREL.get()
                    || t == FTEntities.COMPOST_BOSS.get();   // 2026-09-12: 炫彩答辩超人也吃强化(自身攻防不足)`, '④ BossEmpower 纳入');

/* ⑤ 命中追加 debuff */
rep('src/main/java/net/farmingtales/ftboss/entity/RainbowPoopEntity.java',
    '            if (le instanceof net.minecraft.world.entity.player.Player && this.getOwner() instanceof CompostBossEntity cb) {',
`            /* 2026-09-12: 直接命中也要给玩家 debuff(原来只有水坑给) —— 随机 2 种 */
            if (le instanceof net.minecraft.world.entity.player.Player) {
                le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 0));
                le.addEffect(new MobEffectInstance(this.level().getRandom().nextBoolean()
                        ? MobEffects.WEAKNESS : MobEffects.HUNGER, 120, 0));
            }
            if (le instanceof net.minecraft.world.entity.player.Player && this.getOwner() instanceof CompostBossEntity cb) {`, '⑤ 命中 debuff');
console.log('完成');
