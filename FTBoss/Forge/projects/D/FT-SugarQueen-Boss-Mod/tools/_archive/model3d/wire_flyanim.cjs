/*
 * 2026-09-12
 *  ① 飞行动画(照机器人做法): 新增 float_move(悬浮起伏+腿脚悬垂) / turn_l / turn_r(由 turn_l 镜像) 三条
 *     并在 predicate 里: 二阶段飞行 -> float_move; 快速转向(|Δyaw|>1.2°) -> turn_l/turn_r
 *  ③ debuff 转强化: Boss **不免疫 debuff**; 每 40t 统计身上的负面效果数量 n, 给自己等额增益(力量/速度/抗性, 幅度 = min(4, n-1))
 *  ④ 水坑加强: 半径 3.2->4.0, 时长 200->260t, 各 debuff 时长与等级提升
 */
const fs = require('fs');
const path = require('path');
const ROOT = process.argv[2];
const read = p => fs.readFileSync(path.join(ROOT, p), 'utf8');
const write = (p, s) => fs.writeFileSync(path.join(ROOT, p), s);

/* ① 动画 JSON 追加三条 */
{
    const rel = 'src/main/resources/assets/ftboss/animations/compost_superman.animation.json';
    const j = JSON.parse(read(rel));
    const A = 'animation.compost_superman.';
    if (j.animations[A + 'float_move']) console.log('  ① 动画已存在');
    else {
        j.animations[A + 'float_move'] = {
            loop: true, animation_length: 1.4,
            bones: {
                root: { position: { '0.0': [0, 0, 0], '0.35': [0, 0.55, 0], '0.7': [0, 0, 0], '1.05': [0, 0.4, 0], '1.4': [0, 0, 0] } },
                torso: { rotation: { '0.0': [-6, 0, 0], '0.35': [-3, 0, 2], '0.7': [-6, 0, 0], '1.05': [-3, 0, -2], '1.4': [-6, 0, 0] } },
                head: { rotation: { '0.0': [4, 0, 0], '0.35': [8, 0, -3], '0.7': [4, 0, 0], '1.05': [8, 0, 3], '1.4': [4, 0, 0] } },
                arm_l: { rotation: { '0.0': [-8, 0, -16], '0.7': [-14, 0, -22], '1.4': [-8, 0, -16] } },
                arm_r: { rotation: { '0.0': [-8, 0, 16], '0.7': [-14, 0, 22], '1.4': [-8, 0, 16] } },
                leg_l: { rotation: { '0.0': [-16, 0, -4], '0.7': [-24, 0, -6], '1.4': [-16, 0, -4] } },
                leg_r: { rotation: { '0.0': [-14, 0, 4], '0.7': [-8, 0, 6], '1.4': [-14, 0, 4] } },
                pole: { rotation: { '0.0': [0, 0, 0], '0.7': [0, 0, 3], '1.4': [0, 0, 0] } }
            }
        };
        const turn = {
            loop: true, animation_length: 0.7,
            bones: {
                torso: { rotation: { '0.0': [0, -18, -6], '0.35': [0, -26, -8], '0.7': [0, -18, -6] } },
                head: { rotation: { '0.0': [0, -26, -4], '0.35': [0, -34, -5], '0.7': [0, -26, -4] } },
                arm_l: { rotation: { '0.0': [-10, 0, -22], '0.35': [-16, 0, -28], '0.7': [-10, 0, -22] } },
                arm_r: { rotation: { '0.0': [4, 0, 14], '0.35': [8, 0, 18], '0.7': [4, 0, 14] } },
                leg_l: { rotation: { '0.0': [-8, 0, -10], '0.7': [-14, 0, -12] } },
                leg_r: { rotation: { '0.0': [-12, 0, 10], '0.7': [-6, 0, 12] } }
            }
        };
        j.animations[A + 'turn_l'] = turn;
        /* turn_r = turn_l 镜像(绕 Y/Z 取反) */
        const mir = JSON.parse(JSON.stringify(turn));
        for (const b of Object.values(mir.bones)) {
            if (b.rotation) for (const t of Object.keys(b.rotation)) {
                const v = b.rotation[t];
                b.rotation[t] = [v[0], -v[1], -v[2]];
            }
        }
        j.animations[A + 'turn_r'] = mir;
        write(rel, JSON.stringify(j, null, 1));
        console.log('  ① 已追加 float_move / turn_l / turn_r(镜像)');
    }
}
/* ① 代码: 常量 + predicate + 转向检测 */
{
    const rel = 'src/main/java/net/farmingtales/ftboss/entity/CompostBossEntity.java';
    let s = read(rel);
    let n = 0;
    const f1 = '    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.compost_superman.idle");';
    const f1b = f1 + `
    private static final RawAnimation FLOAT_MOVE = RawAnimation.begin().thenLoop("animation.compost_superman.float_move");
    private static final RawAnimation TURN_L = RawAnimation.begin().thenLoop("animation.compost_superman.turn_l");
    private static final RawAnimation TURN_R = RawAnimation.begin().thenLoop("animation.compost_superman.turn_r");`;
    if (s.includes(f1) && !s.includes('FLOAT_MOVE')) { s = s.replace(f1, f1b); n++; console.log('  OK   动画常量'); }
    else console.log('  skip/FAIL 动画常量');
    const p1 = '        state.setAnimation(state.isMoving() ? WALK : IDLE);';
    const p1b = `        /* 2026-09-12: 二阶段飞行用 float_move; 快速转向用 turn_l/turn_r(照机器人做法) */
        boolean flying = this.isNoGravity();
        float dYaw = Math.abs(this.getYRot() - this.yRotO);
        if (dYaw > 1.2f) { state.setAnimation(this.getYRot() > this.yRotO ? TURN_L : TURN_R); return PlayState.CONTINUE; }
        state.setAnimation(flying ? FLOAT_MOVE : (state.isMoving() ? WALK : IDLE));`;
    if (s.includes(p1)) { s = s.replace(p1, p1b); n++; console.log('  OK   predicate 飞行/转向'); }
    else console.log('  FAIL predicate');
    write(rel, s);
    console.log(`  ① 代码修改 ${n}/2`);
}
/* ③ debuff -> 等额强化 */
{
    const rel = 'src/main/java/net/farmingtales/ftboss/entity/CompostBossEntity.java';
    let s = read(rel);
    if (s.includes('absorbed debuffs')) console.log('  ③ 已存在');
    else {
        const anchor = '        // attack speed decay: no hit on a player for 3s -> reset';
        const block = `        /* 2026-09-12: **不免疫 debuff**, 但每有一个负面效果就获得等额强化(力量/速度/抗性) */
        if (this.tickCount % 40 == 0) {
            int debuffs = 0;
            for (net.minecraft.world.effect.MobEffectInstance inst : this.getActiveEffects()) {
                if (!inst.getEffect().isBeneficial()) debuffs++;
            }
            if (debuffs > 0) {
                int amp = Math.min(4, debuffs - 1);
                this.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST, 90, amp, false, false, false));
                this.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 90, Math.min(3, amp), false, false, false));
                this.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, 90, Math.min(3, amp), false, false, false));
                FTBossMod.LOGGER.info("[FTBoss][CompostMan] absorbed {} debuffs -> buff amp {}", debuffs, amp);
            }
        }

`;
        if (s.includes(anchor)) { s = s.replace(anchor, block + anchor); write(rel, s); console.log('  ③ 已加 debuff->强化'); }
        else console.log('  ③ FAIL 锚点');
    }
}
/* ④ 水坑加强 */
{
    const rel = 'src/main/java/net/farmingtales/ftboss/entity/RainbowPoopEntity.java';
    let s = read(rel);
    let n = 0;
    const fixes = [
        ['cloud.setRadius(3.2f);', 'cloud.setRadius(4.0f);'],
        ['cloud.setDuration(200);                 // 10 秒', 'cloud.setDuration(260);                 // 13 秒'],
        ['cloud.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 0));', 'cloud.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 140, 1));'],
        ['cloud.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));', 'cloud.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 0));'],
        ['cloud.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 100, 0));', 'cloud.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 140, 0));'],
        ['cloud.addEffect(new MobEffectInstance(MobEffects.HUNGER, 120, 0));', 'cloud.addEffect(new MobEffectInstance(MobEffects.HUNGER, 200, 1));']
    ];
    for (const [a, b] of fixes) { if (s.includes(a)) { s = s.replace(a, b); n++; } else console.log('  ④ 未命中 ' + a.slice(0, 40)); }
    write(rel, s);
    console.log(`  ④ 水坑加强 ${n}/${fixes.length}`);
}
console.log('完成');
