/*
 * 2026-09-12
 *  A) 空中改为"移动攻击"(光之女王式): 悬停 -> **绕玩家环绕/突进移动**, 并在移动中**向下投掷答辩炸弹**
 *     · 环绕: 以玩家为中心, 半径 6~9 波动, 高度 地面+4, 速度 0.22/tick, 每 40t 反向一次(来回穿插)
 *     · 炸弹: 每 24t 从身下竖直投下 1 发(带轻微下压), 落点=玩家当前位置; 20% 水坑
 *     · 保留: 激光射线 / 俯冲撞击 / 铁魔法 / 扇形弹幕(改为移动中向前投)
 *  B) 音乐改欢快: 重写《堆肥·冬夜》-> 《堆肥狂欢》(C 大调, 132 BPM, 跳跃低音 + 明亮断奏旋律 + 拍手)
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

/* A) 移动攻击: 替换悬停块里的"定住"逻辑 */
rep(CB, `            double wantY = gp.getY() + 3.2 + Math.sin(this.tickCount * 0.08) * 0.35;
            double cy = this.getY();
            this.setDeltaMovement(0, 0, 0);
            this.setPos(this.getX(), cy + Math.max(-0.3, Math.min(0.3, wantY - cy)), this.getZ());`,
`            /* 2026-09-12 按用户要求: 空中改为**移动攻击**(光之女王式) —— 绕玩家环绕/穿插, 高度保持在地面上方 4 格 */
            double wantY = gp.getY() + 4.0 + Math.sin(this.tickCount * 0.08) * 0.3;
            double cy = this.getY();
            this.setDeltaMovement(0, 0, 0);
            this.setPos(this.getX(), cy + Math.max(-0.3, Math.min(0.3, wantY - cy)), this.getZ());
            if (this.getTarget() != null) {
                LivingEntity mt = this.getTarget();
                this.orbitDir = (this.tickCount / 40) % 2 == 0 ? 1 : -1;      // 每 40t 换向 -> 来回穿插
                double ang = this.tickCount * 0.045 * this.orbitDir;
                double radius = 6.5 + Math.sin(this.tickCount * 0.02) * 2.0;   // 半径 4.5~8.5 波动
                double tx = mt.getX() + Math.cos(ang) * radius;
                double tz = mt.getZ() + Math.sin(ang) * radius;
                double dx = tx - this.getX(), dz = tz - this.getZ();
                double dl = Math.max(0.001, Math.sqrt(dx * dx + dz * dz));
                double step = Math.min(0.22, dl);
                this.setPos(this.getX() + dx / dl * step, this.getY(), this.getZ() + dz / dl * step);
                /* 移动中**向下投掷答辩炸弹**(每 24t) */
                if (--this.bombCd <= 0) {
                    this.bombCd = 24;
                    this.dropBomb(mt, fsl);
                }
            }`, 'A 移动攻击/投弹');

/* A2) 炸弹方法 + 字段 */
rep(CB, '    private int spellCd = 140;',
`    private int spellCd = 140;
    /** 空中移动攻击: 环绕方向 / 投弹计时 */
    private int orbitDir = 1;
    private int bombCd = 20;`, 'A 字段');
rep(CB, '    /** lobbed poop: upward arc toward the target */',
`    /** 向下投掷答辩炸弹(移动攻击用): 竖直下压落向玩家当前位置, 轻微散布 */
    private void dropBomb(LivingEntity target, ServerLevel sl) {
        RainbowPoopEntity bomb = new RainbowPoopEntity(sl, this);
        bomb.setRainbow(this.getPhase() >= 2);
        bomb.setPuddle(this.random.nextFloat() < 0.20f);
        bomb.setDamage(7.0f + this.getPhase() * 2.0f);
        bomb.setPos(this.getX(), this.getY() - 0.4, this.getZ());
        double dx = (target.getX() + (this.random.nextDouble() - 0.5) * 2.0) - this.getX();
        double dz = (target.getZ() + (this.random.nextDouble() - 0.5) * 2.0) - this.getZ();
        double dl = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
        bomb.shoot(dx / dl * 0.35, -1.0, dz / dl * 0.35, 1.1f, 0.4f);   // 主要向下
        sl.addFreshEntity(bomb);
        sl.playSound(null, this.getX(), this.getY(), this.getZ(), net.minecraft.sounds.SoundEvents.SNOWBALL_THROW,
                net.minecraft.sounds.SoundSource.HOSTILE, 0.8f, 0.7f);
    }

    /** lobbed poop: upward arc toward the target */`, 'A 炸弹方法');

/* B) 欢快版 BGM */
{
    const { encode } = require(path.join(ROOT, 'tools/music/wav2ogg.cjs'));
    const SR = 44100, BPM = 132, SPB = 60 / BPM, BARS = 48;
    const N = Math.floor((BARS * 4 * SPB + 3) * SR);
    const L = new Float32Array(N), R = new Float32Array(N);
    const rnd = (() => { let s = 97531; return () => (s = (s * 1103515245 + 12345) & 0x7fffffff) / 0x7fffffff * 2 - 1; })();
    const hz = m => 440 * Math.pow(2, (m - 69) / 12);
    const mix = (t, l, r) => { const i = Math.floor(t * SR); if (i >= 0 && i < N) { L[i] += l; R[i] += r; } };
    const env = (t, a, d, s2, r, dur) => t < a ? t / a : t < a + d ? 1 - (1 - s2) * (t - a) / d : t < dur - r ? s2 : Math.max(0, s2 * (dur - t) / r);
    function stac(t0, midi, amp, dur, pan) {          // 明亮断奏(三角+方波混合)
        const f = hz(midi), len = Math.floor(dur * SR);
        for (let i = 0; i < len; i++) {
            const t = i / SR;
            const v = (Math.sin(2 * Math.PI * f * t) * 0.55 + (((t * f) % 1) < 0.3 ? 0.35 : -0.2))
                * env(t, 0.004, 0.05, 0.5, 0.05, dur) * amp;
            mix(t0 + t, v * (1 - pan), v * pan);
        }
    }
    function bass(t0, midi, amp, dur) {
        const f = hz(midi), len = Math.floor(dur * SR);
        for (let i = 0; i < len; i++) {
            const t = i / SR;
            const v = (Math.sin(2 * Math.PI * f * t) * 0.7 + Math.sin(2 * Math.PI * f * 2 * t) * 0.2) * env(t, 0.005, 0.06, 0.6, 0.05, dur) * amp;
            mix(t0 + t, v * 0.5, v * 0.5);
        }
    }
    const kick = t => Math.sin(2 * Math.PI * (130 * Math.exp(-t * 30) + 45) * t) * Math.exp(-t * 9) * 1.0;
    const clap = t => (rnd() * 0.8 + Math.sin(2 * Math.PI * 1750 * t) * 0.2) * Math.exp(-t * 30) * 0.5;
    const hat = t => rnd() * Math.exp(-t * 110) * 0.18;
    /* C 大调欢快和声: C - G - Am - F  |  F - G - C - C */
    const CH = [[48, 52, 55, 60], [43, 47, 50, 55], [45, 48, 52, 57], [41, 45, 48, 53],
                [41, 45, 48, 53], [43, 47, 50, 55], [48, 52, 55, 60], [48, 52, 55, 60]];
    /* 明亮旋律(原创, 大调跳跃) */
    const MEL = [72, 76, 79, 76, 74, 72, 74, 76, 79, 81, 79, 76, 72, 74, 76, 72,
                 77, 81, 84, 81, 79, 77, 79, 81, 84, 86, 84, 81, 79, 77, 76, 72];
    for (let bar = 0; bar < BARS; bar++) {
        const t0 = bar * 4 * SPB, ch = CH[bar % CH.length];
        /* 鼓: 四踩 + 2/4 拍手 + 八分闭镲 */
        for (let b = 0; b < 4; b++) {
            const tb = t0 + b * SPB;
            if (b % 2 === 0) { const bl = Math.floor(0.3 * SR); for (let i = 0; i < bl; i++) mix(tb + i / SR, kick(i / SR) * 0.9 * 0.5, kick(i / SR) * 0.9 * 0.5); }
            if (b === 1 || b === 3) { const bl = Math.floor(0.2 * SR); for (let i = 0; i < bl; i++) { const v = clap(i / SR) * 0.55; mix(tb + i / SR, v * 0.45, v * 0.55); } }
        }
        for (let e = 0; e < 8; e++) { const bl = Math.floor(0.05 * SR); for (let i = 0; i < bl; i++) { const v = hat(i / SR) * 0.9; mix(t0 + e * SPB / 2 + i / SR, v * (e % 2 ? 0.6 : 0.4), v * (e % 2 ? 0.4 : 0.6)); } }
        /* 跳跃低音: 根-五 八分 */
        for (let e = 0; e < 8; e++) bass(t0 + e * SPB / 2, ch[0] + (e % 2 ? 7 : 0), 0.30, SPB * 0.45);
        /* 和弦垫(短) */
        if (bar % 2 === 0) for (const m of ch) stac(t0, m + 12, 0.045, SPB * 3.4, 0.5);
        /* 主旋律: 第 4 小节起, 每小节 4 个音 */
        if (bar >= 4) {
            const base = ((bar - 4) * 4) % MEL.length;
            for (let k = 0; k < 4; k++) stac(t0 + k * SPB, MEL[(base + k) % MEL.length], 0.16, SPB * 0.8, 0.5 + (k % 2 ? 0.12 : -0.12));
        }
        /* 每 8 小节来一次上行小过门 */
        if (bar % 8 === 7) for (let k = 0; k < 4; k++) stac(t0 + k * SPB, 72 + k * 3, 0.12, SPB * 0.5, 0.5);
    }
    let peak = 0;
    for (let i = 0; i < N; i++) { L[i] = Math.tanh(L[i] * 1.05); R[i] = Math.tanh(R[i] * 1.05); peak = Math.max(peak, Math.abs(L[i]), Math.abs(R[i])); }
    const g = 0.92 / (peak || 1);
    for (let i = 0; i < N; i++) { L[i] *= g; R[i] *= g; }
    const ogg = encode({ channels: 2, sampleRate: SR, frames: N, data: [L, R] }, 0.15, { TITLE: 'Compost Carnival', ARTIST: 'FTBoss' });
    fs.writeFileSync(path.join(ROOT, 'src/main/resources/assets/ftboss/sounds/music/compost.ogg'), ogg);
    console.log(`  B 欢快版 BGM: compost.ogg ${(ogg.length / 1024).toFixed(0)} KB  ${(N / SR).toFixed(1)}s  (C 大调 132 BPM)`);
}
console.log('完成');
