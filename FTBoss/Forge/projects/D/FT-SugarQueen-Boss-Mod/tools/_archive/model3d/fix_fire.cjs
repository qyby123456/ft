/*
 * 2026-09-12 按用户要求:
 *  ① 完善 cannon_fire 抬手动画: 改成 1.6 秒三段式 —— 抬炮(0~0.35s) -> 瞄准保持+每发后坐抖动(0.35~1.1s) -> 放下(1.1~1.6s)
 *  ② 答辩从**手持土豆加农炮的炮口**射出(而不是身体中心/口部): 按朝向算右手+前方的炮口坐标, 并从该点瞄准
 */
const fs = require('fs');
const path = require('path');
const ROOT = process.argv[2];
const read = p => fs.readFileSync(path.join(ROOT, p), 'utf8');
const write = (p, s) => fs.writeFileSync(path.join(ROOT, p), s);

/* ---------- ① 动画: 重写 cannon_fire ---------- */
{
    const rel = 'src/main/resources/assets/ftboss/animations/compost_superman.animation.json';
    const j = JSON.parse(read(rel));
    const key = 'animation.compost_superman.cannon_fire';
    if (!j.animations[key]) { console.log('  FAIL 找不到 cannon_fire'); }
    else {
        j.animations[key] = {
            loop: false, animation_length: 1.6,
            bones: {
                /* 抬炮: 0.35s 内抬到位, 之后保持并做两次后坐抖动, 最后放下 */
                cannon: {
                    rotation: { '0.0': [0, 0, 0], '0.35': [-30, 0, 0], '0.5': [-22, 0, 0], '0.62': [-32, 0, 0], '0.78': [-22, 0, 0], '0.9': [-32, 0, 0], '1.1': [-24, 0, 0], '1.6': [0, 0, 0] },
                    position: { '0.0': [0, 0, 0], '0.35': [0, 0.6, 0], '0.5': [0, 0.6, 1.2], '0.62': [0, 0.6, 0.2], '0.78': [0, 0.6, 1.1], '0.9': [0, 0.6, 0.2], '1.1': [0, 0.4, 0.6], '1.6': [0, 0, 0] }
                },
                arm_r: { rotation: { '0.0': [0, 0, 0], '0.35': [-58, 0, 14], '0.62': [-52, 0, 12], '0.9': [-56, 0, 13], '1.1': [-46, 0, 11], '1.6': [0, 0, 0] } },
                forearm_r: { rotation: { '0.0': [0, 0, 0], '0.35': [34, 0, 0], '1.1': [28, 0, 0], '1.6': [0, 0, 0] } },
                hand_r: { rotation: { '0.0': [0, 0, 0], '0.35': [-16, 0, 0], '1.1': [-12, 0, 0], '1.6': [0, 0, 0] } },
                /* 左臂抬草束做"平衡/瞄准"姿势 */
                arm_l: { rotation: { '0.0': [0, 0, 0], '0.4': [-26, 0, -18], '1.1': [-20, 0, -14], '1.6': [0, 0, 0] } },
                /* 躯干后仰蓄力 -> 每次射击前倾 */
                torso: { rotation: { '0.0': [0, 0, 0], '0.35': [-9, 0, -3], '0.5': [10, 0, 2], '0.62': [-6, 0, -2], '0.78': [10, 0, 2], '0.9': [-6, 0, -2], '1.1': [6, 0, 1], '1.6': [0, 0, 0] } },
                head: { rotation: { '0.0': [0, 0, 0], '0.35': [-12, 0, 0], '0.5': [8, 0, 0], '0.9': [6, 0, 0], '1.6': [0, 0, 0] } },
                root: { position: { '0.0': [0, 0, 0], '0.35': [0, 0.35, 0], '0.52': [0, 0.1, 0.3], '0.9': [0, 0.25, 0], '1.1': [0, 0.1, 0.2], '1.6': [0, 0, 0] } },
                /* 稻草腿下蹲一点稳住后坐 */
                leg_l: { rotation: { '0.0': [0, 0, 0], '0.5': [-10, 0, -4], '1.6': [0, 0, 0] } },
                leg_r: { rotation: { '0.0': [0, 0, 0], '0.5': [-10, 0, 4], '1.6': [0, 0, 0] } }
            }
        };
        write(rel, JSON.stringify(j, null, 1));
        console.log('  ① cannon_fire 重写: 1.6s 三段式(抬炮 / 瞄准后坐抖动 / 放下)');
    }
}

/* ---------- ② 炮口发射 ---------- */
{
    const rel = 'src/main/java/net/farmingtales/ftboss/entity/CompostBossEntity.java';
    let s = read(rel);
    const old = `        double sx = this.getX(), sy = this.getY() + 1.6, sz = this.getZ();
        poop.setPos(sx, sy, sz);`;
    const neu = `        /* 2026-09-12: 从**手持土豆加农炮的炮口**发射(右手 + 前方), 而不是身体中心(否则像口吐) */
        double yaw = Math.toRadians(this.getYRot());
        double fx = -Math.sin(yaw), fz = Math.cos(yaw);      // 前方
        double rx = Math.cos(yaw), rz = Math.sin(yaw);       // 右侧
        double sx = this.getX() + rx * 1.15 + fx * 1.10;
        double sy = this.getY() + 1.85 + (this.actionTicks > 0 ? 0.35 : 0.0);   // 抬炮时炮口更高
        double sz = this.getZ() + rz * 1.15 + fz * 1.10;
        poop.setPos(sx, sy, sz);`;
    if (s.includes(old)) { s = s.replace(old, neu); console.log('  ② 炮口发射坐标: 已改为右手+前方偏移'); }
    else console.log('  ② FAIL 找不到发射坐标行');
    /* 抬炮时间与新动画对齐: 起爆前留 16t 抬炮 */
    if (s.includes('this.burstGap = 12;')) { s = s.replace('this.burstGap = 12;', 'this.burstGap = 16;'); console.log('  ② 抬炮时间: 12t -> 16t(与 1.6s 动画对齐)'); }
    /* 动作时长: 22 -> 32t(1.6s) */
    if (s.includes('private static final double T_FIRE = 22.0')) { s = s.replace('private static final double T_FIRE = 22.0', 'private static final double T_FIRE = 32.0'); console.log('  ② T_FIRE: 22t -> 32t'); }
    write(rel, s);
}
console.log('完成');
