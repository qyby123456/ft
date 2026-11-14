/*
 * 2026-09-12
 *  ① 黑洞少用: 法术池按权重 —— 80% magic_missile, 20% black_hole
 *  ② 攻击附带**反胃 + 饥饿**: 投掷物直接命中 -> 缓慢 + 反胃 + 饥饿(固定三种)
 *  ③ 伤害增加: 炸弹 7->10(+阶段*3), 抛粪 5->7, 弹幕 6->9, 俯冲 14->18, 水坑不变
 *  ④ 二阶段专属 BGM: 同曲升 4 个半音 + 提速到 148 BPM + 更密鼓点 -> compost_p2.ogg
 *     并在进入二阶段时: 停掉一阶段曲 + 播二阶段曲(sounds.json + FTSounds + 切换逻辑)
 */
const fs = require('fs');
const path = require('path');
const ROOT = process.argv[2];
const J = 'src/main/java/net/farmingtales/ftboss';
const A = 'src/main/resources/assets/ftboss';
const read = p => fs.readFileSync(path.join(ROOT, p), 'utf8');
const write = (p, s) => { fs.mkdirSync(path.dirname(path.join(ROOT, p)), { recursive: true }); fs.writeFileSync(path.join(ROOT, p), s); };
function rep(rel, a, b, label) {
    const s = read(rel);
    if (!s.includes(a)) { console.log('  FAIL ' + label); return; }
    write(rel, s.replace(a, b));
    console.log('  OK   ' + label);
}
const CB = `${J}/entity/CompostBossEntity.java`;

/* ① 黑洞降频 */
rep(CB, `            String id = pool[this.random.nextInt(pool.length)];`,
    `            String id = this.random.nextFloat() < 0.20f ? "irons_spellbooks:black_hole" : "irons_spellbooks:magic_missile";   // 2026-09-12: 黑洞只用 20%`, '① 黑洞降频');
/* ② 命中反胃+饥饿 */
rep(`${J}/entity/RainbowPoopEntity.java`,
`            if (le instanceof net.minecraft.world.entity.player.Player) {
                le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 0));
                le.addEffect(new MobEffectInstance(this.level().getRandom().nextBoolean()
                        ? MobEffects.WEAKNESS : MobEffects.HUNGER, 120, 0));
            }`,
`            if (le instanceof net.minecraft.world.entity.player.Player) {
                /* 2026-09-12 按用户要求: 命中固定附带 **缓慢 + 反胃 + 饥饿** */
                le.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 0));
                le.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 140, 0));   // 反胃
                le.addEffect(new MobEffectInstance(MobEffects.HUNGER, 160, 0));      // 饥饿
            }`, '② 命中反胃饥饿');
/* ③ 增伤 */
rep(CB, '        poop.setDamage(7.0f + this.getPhase() * 2.0f);', '        poop.setDamage(10.0f + this.getPhase() * 3.0f);', '③ 炸弹增伤');
rep(CB, '        poop.setDamage(5.0f + this.getPhase() * 2.0f);', '        poop.setDamage(7.0f + this.getPhase() * 2.5f);', '③ 抛粪增伤');
rep(CB, '                    poop.setDamage(6.0f + this.getPhase() * 2.0f);', '                    poop.setDamage(9.0f + this.getPhase() * 2.5f);', '③ 弹幕增伤');
rep(CB, '                        dp.hurt(this.damageSources().mobAttack(this), 14.0f + this.getPhase() * 3.0f);', '                        dp.hurt(this.damageSources().mobAttack(this), 18.0f + this.getPhase() * 4.0f);', '③ 俯冲增伤');
/* ④ 二阶段 BGM: 生成 + 注册 + 切换 */
{
    const { encode } = require(path.join(ROOT, 'tools/music/wav2ogg.cjs'));
    const SR = 44100, BPM = 148, SPB = 60 / BPM, BARS = 48, TR = 4;      // 升 4 个半音
    const N = Math.floor((BARS * 4 * SPB + 3) * SR);
    const L = new Float32Array(N), R = new Float32Array(N);
    const rnd = (() => { let s = 24680; return () => (s = (s * 1103515245 + 12345) & 0x7fffffff) / 0x7fffffff * 2 - 1; })();
    const hz = m => 440 * Math.pow(2, ((m + TR) - 69) / 12);
    const mix = (t, l, r) => { const i = Math.floor(t * SR); if (i >= 0 && i < N) { L[i] += l; R[i] += r; } };
    const env = (t, a, d, s2, r, dur) => t < a ? t / a : t < a + d ? 1 - (1 - s2) * (t - a) / d : t < dur - r ? s2 : Math.max(0, s2 * (dur - t) / r);
    const stac = (t0, midi, amp, dur, pan) => { const f = hz(midi), len = Math.floor(dur * SR); for (let i = 0; i < len; i++) { const t = i / SR; const v = (Math.sin(2 * Math.PI * f * t) * 0.5 + (((t * f) % 1) < 0.3 ? 0.4 : -0.25)) * env(t, 0.003, 0.04, 0.55, 0.04, dur) * amp; mix(t0 + t, v * (1 - pan), v * pan); } };
    const bass = (t0, midi, amp, dur) => { const f = hz(midi), len = Math.floor(dur * SR); for (let i = 0; i < len; i++) { const t = i / SR; const v = (Math.sin(2 * Math.PI * f * t) * 0.75 + Math.sin(2 * Math.PI * f * 2 * t) * 0.25) * env(t, 0.004, 0.05, 0.65, 0.04, dur) * amp; mix(t0 + t, v * 0.5, v * 0.5); } };
    const kick = t => Math.sin(2 * Math.PI * (150 * Math.exp(-t * 34) + 48) * t) * Math.exp(-t * 10);
    const clap = t => (rnd() * 0.85 + Math.sin(2 * Math.PI * 1900 * t) * 0.15) * Math.exp(-t * 34) * 0.55;
    const hat = t => rnd() * Math.exp(-t * 120) * 0.2;
    const CH = [[48, 52, 55, 60], [43, 47, 50, 55], [45, 48, 52, 57], [41, 45, 48, 53], [41, 45, 48, 53], [43, 47, 50, 55], [48, 52, 55, 60], [48, 52, 55, 60]];
    const MEL = [72, 76, 79, 76, 74, 72, 74, 76, 79, 81, 79, 76, 72, 74, 76, 72,
                 77, 81, 84, 81, 79, 77, 79, 81, 84, 86, 84, 81, 79, 77, 76, 72];
    for (let bar = 0; bar < BARS; bar++) {
        const t0 = bar * 4 * SPB, ch = CH[bar % CH.length];
        for (let b = 0; b < 4; b++) {
            const tb = t0 + b * SPB;
            if (b % 2 === 0) { const bl = Math.floor(0.3 * SR); for (let i = 0; i < bl; i++) { const v = kick(i / SR) * 1.0; mix(tb + i / SR, v * 0.5, v * 0.5); } }
            if (b === 1 || b === 3) { const bl = Math.floor(0.2 * SR); for (let i = 0; i < bl; i++) { const v = clap(i / SR) * 0.6; mix(tb + i / SR, v * 0.45, v * 0.55); } }
            for (let e = 0; e < 4; e++) { const bl = Math.floor(0.04 * SR); for (let i = 0; i < bl; i++) { const v = hat(i / SR); mix(tb + e * SPB / 4 + i / SR, v * 0.55, v * 0.45); } }   // 十六分镲(更密)
        }
        for (let e = 0; e < 8; e++) bass(t0 + e * SPB / 2, ch[0] + (e % 2 ? 7 : 0), 0.32, SPB * 0.45);
        if (bar % 2 === 0) for (const m of ch) stac(t0, m + 12, 0.05, SPB * 3.4, 0.5);
        if (bar >= 2) { const base = ((bar - 2) * 4) % MEL.length; for (let k = 0; k < 4; k++) stac(t0 + k * SPB, MEL[(base + k) % MEL.length], 0.18, SPB * 0.75, 0.5 + (k % 2 ? 0.12 : -0.12)); }
    }
    let peak = 0;
    for (let i = 0; i < N; i++) { L[i] = Math.tanh(L[i] * 1.1); R[i] = Math.tanh(R[i] * 1.1); peak = Math.max(peak, Math.abs(L[i]), Math.abs(R[i])); }
    const g = 0.92 / (peak || 1);
    for (let i = 0; i < N; i++) { L[i] *= g; R[i] *= g; }
    const ogg = encode({ channels: 2, sampleRate: SR, frames: N, data: [L, R] }, 0.15, { TITLE: 'Compost Carnival (Overdrive)', ARTIST: 'FTBoss' });
    fs.writeFileSync(path.join(ROOT, A, 'sounds/music/compost_p2.ogg'), ogg);
    console.log(`  ④ 二阶段 BGM: compost_p2.ogg ${(ogg.length / 1024).toFixed(0)} KB ${(N / SR).toFixed(1)}s (升 4 半音 / 148 BPM)`);
}
{
    const p = path.join(ROOT, A, 'sounds.json');
    const sj = JSON.parse(fs.readFileSync(p, 'utf8'));
    sj['boss_music_compost_p2'] = { subtitle: 'ftboss.subtitle.boss_music_compost_p2', sounds: [{ name: 'ftboss:music/compost_p2', stream: true }] };
    fs.writeFileSync(p, JSON.stringify(sj, null, 2));
    console.log('  ④ sounds.json 加 boss_music_compost_p2');
}
rep(`${J}/sound/FTSounds.java`, '    public static final RegistryObject<SoundEvent> BOSS_MUSIC_COMPOST = SOUNDS.register("boss_music_compost",',
`    /** 炫彩答辩超人 **二阶段** BGM(升调提速版) */
    public static final RegistryObject<SoundEvent> BOSS_MUSIC_COMPOST_P2 = SOUNDS.register("boss_music_compost_p2",
            () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(FTBossMod.MOD_ID, "boss_music_compost_p2")));

    public static final RegistryObject<SoundEvent> BOSS_MUSIC_COMPOST = SOUNDS.register("boss_music_compost",`, '④ FTSounds');
rep(CB, '            this.entityData.set(DATA_PHASE, want);',
`            this.entityData.set(DATA_PHASE, want);
            /* 2026-09-12: 进入二阶段时切换为**升调提速版**BGM */
            if (want >= 2 && this.level() instanceof ServerLevel p2sl) {
                for (net.minecraft.server.level.ServerPlayer sp : p2sl.players()) {
                    if (sp.distanceToSqr(this) <= 64.0 * 64.0) {
                        sp.connection.send(new net.minecraft.network.protocol.game.ClientboundStopSoundPacket(
                                net.farmingtales.ftboss.sound.FTSounds.BOSS_MUSIC_COMPOST.get().getLocation(),
                                net.minecraft.sounds.SoundSource.MUSIC));
                    }
                }
                p2sl.playSound(null, this.getX(), this.getY(), this.getZ(),
                        net.farmingtales.ftboss.sound.FTSounds.BOSS_MUSIC_COMPOST_P2.get(),
                        net.minecraft.sounds.SoundSource.MUSIC, 4.0f, 1.0f);
            }`, '④ 二阶段切歌');
for (const [f, v] of [['zh_cn.json', '\u6218\u6597BGM: \u300a\u5806\u80a5\u72c2\u6b22\u00b7\u8fc7\u8f7d\u300b'], ['en_us.json', 'BGM: Compost Carnival (Overdrive)']]) {
    const p = path.join(ROOT, A, 'lang', f);
    const j = JSON.parse(fs.readFileSync(p, 'utf8'));
    j['ftboss.subtitle.boss_music_compost_p2'] = v;
    fs.writeFileSync(p, JSON.stringify(j, null, 2));
}
console.log('完成');
