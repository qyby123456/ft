/*
 * 2026-09-12 二阶段飞行攻击(参考"会飞的 Boss"通用打法):
 *  ① 悬浮游走: 已有(地面 +3.2 起伏)
 *  ② 空中弹幕: 每 80~120t, 悬停时朝玩家扇形发射 3 发答辩(向下压角)
 *  ③ 俯冲撞击: 每 180~240t, 短暂蓄势后以 0.55/tick 俯冲目标, 命中(1.8 格内)造成 14+阶段*3 伤害并击退, 40t 后自动拉回高度
 *  注: 俯冲期间**跳过悬浮钳制**, 否则会被每 tick 的 setDeltaMovement(0,0,0) 抵消
 */
const fs = require('fs');
const path = require('path');
const ROOT = process.argv[2];
const read = p => fs.readFileSync(path.join(ROOT, p), 'utf8');
const write = (p, s) => fs.writeFileSync(path.join(ROOT, p), s);
const rel = 'src/main/java/net/farmingtales/ftboss/entity/CompostBossEntity.java';
let s = read(rel);
let n = 0;
const rep2 = (a, b, label) => { if (s.includes(a)) { s = s.replace(a, b); n++; console.log('  OK   ' + label); } else console.log('  FAIL ' + label); };

/* 字段 */
rep2('    private int gasFuse = -1;',
    `    private int gasFuse = -1;
    /** flight attacks (phase 2+): barrage + dive */
    private int barrageCd = 90;
    private int diveCd = 200;
    private int diveTicks = -1;`, '飞行攻击字段');

/* 悬浮块: 俯冲时跳过 */
rep2('        if (fly && this.level() instanceof ServerLevel fsl) {',
    '        if (fly && this.diveTicks <= 0 && this.level() instanceof ServerLevel fsl) {', '俯冲时跳过悬浮钳制');

/* 飞行攻击逻辑: 插在悬浮块之后 */
rep2('        // attack speed decay: no hit on a player for 3s -> reset',
    `        /* ===== 飞行攻击(阶段 2+): 空中弹幕 + 俯冲撞击 ===== */
        if (fly && this.getTarget() != null && this.getTarget().isAlive()) {
            LivingEntity ft = this.getTarget();
            /* ② 空中弹幕: 3 发扇形(向下压角) */
            if (this.diveTicks <= 0 && this.barrageCd > 0 && --this.barrageCd <= 0 && this.level() instanceof ServerLevel bsl2) {
                this.barrageCd = 80 + this.random.nextInt(40);
                this.playAction("cannon_fire", T_FIRE);
                for (int i = -1; i <= 1; i++) {
                    RainbowPoopEntity poop = new RainbowPoopEntity(bsl2, this);
                    poop.setRainbow(true);
                    poop.setPuddle(this.random.nextFloat() < 0.3f);
                    poop.setDamage(6.0f + this.getPhase() * 2.0f);
                    double yaw = Math.toRadians(this.getYRot());
                    double sx = this.getX() - Math.sin(yaw) * 1.1, sy = this.getY() + 1.2, sz = this.getZ() + Math.cos(yaw) * 1.1;
                    poop.setPos(sx, sy, sz);
                    double dx = ft.getX() - sx + i * 1.5, dy = ft.getEyeY() - sy, dz = ft.getZ() - sz + i * 1.5;
                    double len = Math.max(0.1, Math.sqrt(dx * dx + dy * dy + dz * dz));
                    poop.shoot(dx / len, Math.min(-0.15, dy / len), dz / len, 1.3f, 0.8f);   // 向下压角
                    bsl2.addFreshEntity(poop);
                }
                bsl2.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.HOSTILE, 1.2f, 1.1f);
            }
            /* ③ 俯冲撞击 */
            if (this.diveTicks < 0 && this.diveCd > 0 && --this.diveCd <= 0) {
                this.diveCd = 180 + this.random.nextInt(60);
                this.diveTicks = 40;
                this.playAction("phase_shift", T_SHIFT);
            }
            if (this.diveTicks > 0) {
                this.diveTicks--;
                double dx = ft.getX() - this.getX(), dy = ft.getEyeY() - this.getY(), dz = ft.getZ() - this.getZ();
                double len = Math.max(0.1, Math.sqrt(dx * dx + dy * dy + dz * dz));
                this.setDeltaMovement(dx / len * 0.55, dy / len * 0.55, dz / len * 0.55);
                for (Player dp : this.level().players()) {
                    if (dp.isAlive() && this.distanceToSqr(dp) < 3.24) {
                        dp.hurt(this.damageSources().mobAttack(this), 14.0f + this.getPhase() * 3.0f);
                        dp.knockback(1.2, this.getX() - dp.getX(), this.getZ() - dp.getZ());
                        dp.setSecondsOnFire(3);
                        this.diveTicks = 0;      // 撞到就结束这次俯冲
                    }
                }
                if (this.diveTicks == 0 && this.level() instanceof ServerLevel dsl) {
                    net.farmingtales.ftboss.util.Fx.particles(dsl, this.getX(), this.getY(), this.getZ(), 24, ParticleTypes.ITEM_SLIME, 1.6, 0.1);
                    dsl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.2f, 0.8f);
                }
            }
        }

        // attack speed decay: no hit on a player for 3s -> reset`, '飞行攻击逻辑');
write(rel, s);
console.log(`共修改 ${n} 处`);
