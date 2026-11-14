/*
 * 2026-09-12 飞行攻击对齐参考 mod:
 *   光之女王(Stellarity empress_of_light): dash + projectile  -> 已有"俯冲撞击"(dash) 与"弹幕"(projectile)
 *   炼狱游者(Incendium hovering_inferno): shield_lasers / firework spread / summon
 *   本次补: ① 悬停**激光射线**(shield_lasers 风格)  ② 扇形弹幕 3 -> 5 发(更宽)
 */
const fs = require('fs');
const path = require('path');
const ROOT = process.argv[2];
const rel = 'src/main/java/net/farmingtales/ftboss/entity/CompostBossEntity.java';
const p = path.join(ROOT, rel);
let s = fs.readFileSync(p, 'utf8');
let n = 0;
const rep2 = (a, b, label) => { if (s.includes(a)) { s = s.replace(a, b); n++; console.log('  OK   ' + label); } else console.log('  FAIL ' + label); };

/* 字段 */
rep2('    private int diveTicks = -1;',
    `    private int diveTicks = -1;
    /** hovering laser ray (Hovering Inferno style) */
    private int laserCd = 120;
    private int laserTicks = -1;`, '激光字段');

/* 扇形弹幕 3 -> 5 发 + 更宽散布 */
rep2('                for (int i = -1; i <= 1; i++) {',
    '                for (int i = -2; i <= 2; i++) {   // 2026-09-12: 3 -> 5 发(更宽的扇形)', '弹幕 5 发');
rep2('                    double dx = ft.getX() - sx + i * 1.5, dy = ft.getEyeY() - sy, dz = ft.getZ() - sz + i * 1.5;',
    '                    double dx = ft.getX() - sx + i * 1.9, dy = ft.getEyeY() - sy, dz = ft.getZ() - sz + i * 1.9;', '弹幕散布');

/* 激光射线: 插在弹幕之后 */
rep2('            /* ③ 俯冲撞击 */',
    `            /* ②b 悬停激光射线(炼狱游者 shield_lasers 风格): 锁定玩家, 每 2t 伤害一次, 持续 20t */
            if (this.laserTicks < 0 && this.barrageCd > 0 && this.diveTicks <= 0 && this.laserCd > 0 && --this.laserCd <= 0) {
                this.laserCd = 150 + this.random.nextInt(60);
                this.laserTicks = 20;
                this.playAction("cannon_fire", T_FIRE);
                if (this.level() instanceof ServerLevel lsl) {
                    lsl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 2.0f, 1.4f);
                }
            }
            if (this.laserTicks > 0) {
                this.laserTicks--;
                if (this.level() instanceof ServerLevel lsl) {
                    /* 从本体朝玩家画一条射线: 每 0.6 格一个光点, 命中半径 1.3 */
                    double lx = ft.getX() - this.getX(), ly = ft.getEyeY() - (this.getY() + 1.4), lz = ft.getZ() - this.getZ();
                    double len = Math.max(0.1, Math.sqrt(lx * lx + ly * ly + lz * lz));
                    for (double d = 1.0; d <= Math.min(24.0, len + 1.0); d += 0.6) {
                        net.farmingtales.ftboss.util.Fx.particles(lsl,
                                this.getX() + lx / len * d, this.getY() + 1.4 + ly / len * d, this.getZ() + lz / len * d,
                                1, ParticleTypes.END_ROD, 0.03, 0.0);
                    }
                    if (this.laserTicks % 2 == 0) {
                        for (Player lp : lsl.players()) {
                            if (!lp.isAlive()) continue;
                            /* 点到直线距离判定 */
                            double px = lp.getX() - this.getX(), py = lp.getEyeY() - (this.getY() + 1.4), pz = lp.getZ() - this.getZ();
                            double t = (px * lx + py * ly + pz * lz) / (len * len);
                            if (t < 0.0 || t > 1.0) continue;
                            double cx = px - lx * t, cy = py - ly * t, cz = pz - lz * t;
                            if (cx * cx + cy * cy + cz * cz <= 1.7) {
                                lp.invulnerableTime = 0;
                                lp.hurt(this.damageSources().mobAttack(this), 9.0f + this.getPhase() * 2.0f);
                            }
                        }
                    }
                }
            }

            /* ③ 俯冲撞击 */`, '激光射线');
fs.writeFileSync(p, s);
console.log(`修改 ${n} 处`);
