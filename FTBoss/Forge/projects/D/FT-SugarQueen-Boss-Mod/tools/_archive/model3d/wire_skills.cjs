/*
 * 2026-09-12 收尾 + 技能第一批:
 *  A) 确认/补 BossMercy 的炫彩答辩超人石像掉落分支
 *  B) 技能①【抛粪】: 每 140~220t, 抛物线抛 3 坨答辩(带向上弧线)
 *  C) 技能②【沼爆蓄力】: 每 200~300t, 播 gas_charge(52t) -> 8 格范围爆炸 + 伤害 + 点燃 + 棕色粒子
 */
const fs = require('fs');
const path = require('path');
const ROOT = process.argv[2];
const read = p => fs.readFileSync(path.join(ROOT, p), 'utf8');
const write = (p, s) => fs.writeFileSync(path.join(ROOT, p), s);

/* A) 石像掉落 */
{
    const rel = 'src/main/java/net/farmingtales/ftboss/boss/ai/BossMercy.java';
    let s = read(rel);
    if (s.includes('COMPOST_BOSS') && s.includes('STATUE_COMPOST')) console.log('  A 石像掉落: 已在位 OK');
    else {
        const a = '            if (t == net.farmingtales.ftboss.entity.FTEntities.DEFECT_CORE.get())';
        if (s.includes(a)) {
            s = s.replace(a, `            if (t == net.farmingtales.ftboss.entity.FTEntities.COMPOST_BOSS.get())
                statue = net.farmingtales.ftboss.block.FTBlocks.STATUE_COMPOST.get().asItem();
            else if (t == net.farmingtales.ftboss.entity.FTEntities.DEFECT_CORE.get())`);
            write(rel, s);
            console.log('  A 石像掉落: 已补上 COMPOST 分支 OK');
        } else console.log('  A FAIL 找不到锚点');
    }
}
/* B+C) 技能 */
{
    const rel = 'src/main/java/net/farmingtales/ftboss/entity/CompostBossEntity.java';
    let s = read(rel);
    /* 字段 */
    if (!s.includes('private int gasCd')) {
        s = s.replace('    private int burstCd = 120;',
            `    private int burstCd = 120;
    /** skill timers + gas explosion fuse */
    private int gasCd = 200;
    private int lobCd = 160;
    private int gasFuse = -1;`);
        console.log('  B/C 字段已加');
    } else console.log('  B/C 字段已存在');
    /* tick 挂钩: 放在 burst 逻辑之前 */
    const anchor = '        /* fire burst: 4-5 normal poops, each 25% chance to be a puddle poop */';
    if (s.includes(anchor) && !s.includes('// SKILL: lobbed poop')) {
        const block = [
            '        // SKILL: lobbed poop (3 shots, upward arc)',
            '        if (this.lobCd > 0 && --this.lobCd <= 0 && this.getTarget() != null && this.getTarget().isAlive()) {',
            '            this.lobCd = 140 + this.random.nextInt(80);',
            '            this.playAction("cannon_fire", T_FIRE);',
            '            for (int i = 0; i < 3; i++) this.fireLob(i);',
            '        }',
            '        // SKILL: gas charge -> explosion (8 blocks)',
            '        if (this.gasCd > 0 && --this.gasCd <= 0 && this.getTarget() != null && this.getTarget().isAlive()) {',
            '            this.gasCd = 200 + this.random.nextInt(100);',
            '            this.playAction("gas_charge", 52.0);',
            '            this.gasFuse = 52;',
            '        }',
            '        if (this.gasFuse > 0 && --this.gasFuse == 0) this.gasExplode();',
            ''
        ].join('\n');
        s = s.replace(anchor, block + anchor);
        console.log('  B/C tick 挂钩已加');
    } else console.log('  B/C tick 挂钩已存在');
    /* 方法 */
    if (!s.includes('private void gasExplode()')) {
        const a = '    /** \u53d1\u5c04\u4e00\u53d1\u7b54\u8fa9';
        const m = s.indexOf('    private void firePoop() {');
        if (m > 0) {
            const methods = [
                '    /** lobbed poop: upward arc toward the target */',
                '    private void fireLob(int index) {',
                '        if (!(this.level() instanceof ServerLevel sl)) return;',
                '        LivingEntity t = this.getTarget();',
                '        if (t == null) return;',
                '        RainbowPoopEntity poop = new RainbowPoopEntity(sl, this);',
                '        poop.setRainbow(this.getPhase() >= 2);',
                '        poop.setPuddle(this.random.nextFloat() < 0.45f);',
                '        poop.setDamage(5.0f + this.getPhase() * 2.0f);',
                '        double yaw = Math.toRadians(this.getYRot());',
                '        double fx = -Math.sin(yaw), fz = Math.cos(yaw);',
                '        double sx = this.getX() + fx * 1.2, sy = this.getY() + 2.1, sz = this.getZ() + fz * 1.2;',
                '        poop.setPos(sx, sy, sz);',
                '        double dx = t.getX() - sx + (index - 1) * 1.2, dy = t.getEyeY() - sy, dz = t.getZ() - sz + (index - 1) * 1.2;',
                '        double len = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));',
                '        poop.shoot(dx / len, 0.55 + dy / Math.max(4.0, len * 2.0), dz / len, 1.05f, 1.2f);',
                '        sl.addFreshEntity(poop);',
                '        sl.playSound(null, sx, sy, sz, SoundEvents.SNOWBALL_THROW, SoundSource.HOSTILE, 1.0f, 1.2f);',
                '    }',
                '',
                '    /** gas charge explosion: 8 blocks, damage + ignite + brown particles */',
                '    private void gasExplode() {',
                '        if (!(this.level() instanceof ServerLevel sl)) return;',
                '        double x = this.getX(), y = this.getY() + 0.6, z = this.getZ();',
                '        sl.explode(this, x, y, z, 3.2f, net.minecraft.world.level.Level.ExplosionInteraction.NONE);',
                '        sl.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 1.8f, 0.6f);',
                '        net.farmingtales.ftboss.util.Fx.particles(sl, x, y, z, 60, ParticleTypes.ITEM_SLIME, 2.6, 0.15);',
                '        net.farmingtales.ftboss.util.Fx.particles(sl, x, y, z, 20, ParticleTypes.LARGE_SMOKE, 2.0, 0.08);',
                '        for (Player p : sl.players()) {',
                '            if (p.distanceToSqr(this) <= 64.0 && p.isAlive()) {',
                '                p.hurt(this.damageSources().mobAttack(this), 16.0f + this.getPhase() * 3.0f);',
                '                p.setSecondsOnFire(4);',
                '            }',
                '        }',
                '        FTBossMod.LOGGER.info("[FTBoss][CompostMan] gas explosion at {}, {}, {}", (int) x, (int) y, (int) z);',
                '    }',
                '',
                '    private void firePoop() {'
            ].join('\n');
            s = s.slice(0, m) + methods + s.slice(m + '    private void firePoop() {'.length);
            console.log('  B/C 方法已加(fireLob + gasExplode)');
        } else console.log('  FAIL 找不到 firePoop');
    } else console.log('  B/C 方法已存在');
    write(rel, s);
}
console.log('完成');
