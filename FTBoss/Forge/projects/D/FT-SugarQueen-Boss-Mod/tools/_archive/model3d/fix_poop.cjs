/* 修三处编译问题(纯 ASCII 锚点):
 *  ① FTClientSetup: RegisterRenderers 没有 getContext(), 改用 lambda 接收 ctx
 *  ② RainbowPoopEntity: AreaEffectCloud 没有 setColor(int) -> 删掉该行(默认颜色即可)
 *  ③ CompostBossEntity: 连射逻辑没插进去(锚点注释被清空) -> 改挂到 if (this.bossBar != null) 之前
 */
const fs = require('fs');
const path = require('path');
const ROOT = process.argv[2];
const J = path.join(ROOT, 'src/main/java/net/farmingtales/ftboss');
const rw = (p, fn) => { let s = fs.readFileSync(p, 'utf8'); const o = s; s = fn(s); if (s !== o) { fs.writeFileSync(p, s); return true; } return false; };

/* ① */
{
    const p = path.join(J, 'client/FTClientSetup.java');
    const ok = rw(p, s => s.replace(
        'new net.minecraft.client.renderer.entity.ThrownItemRenderer<>(e.getContext(), 1.0f, true)',
        'ctx -> new net.minecraft.client.renderer.entity.ThrownItemRenderer<>(ctx, 1.0f, true)'));
    console.log('① 渲染器 lambda: ' + (ok ? 'OK' : '未命中'));
}
/* ② */
{
    const p = path.join(J, 'entity/RainbowPoopEntity.java');
    const ok = rw(p, s => s.split('\n').filter(l => !l.includes('cloud.setColor(')).join('\n'));
    console.log('② 删除 setColor(int): ' + (ok ? 'OK' : '未命中'));
}
/* ③ */
{
    const p = path.join(J, 'entity/CompostBossEntity.java');
    const block = [
        '        /* fire burst: 4-5 normal poops, each 25% chance to be a puddle poop */',
        '        if (this.burstLeft > 0) {',
        '            if (--this.burstGap <= 0) {',
        '                this.burstGap = 4;',
        '                this.burstLeft--;',
        '                this.firePoop();',
        '            }',
        '        } else if (--this.burstCd <= 0 && this.getTarget() != null && this.getTarget().isAlive()) {',
        '            this.burstCd = 100 + this.random.nextInt(60);',
        '            this.burstLeft = 4 + this.random.nextInt(2);',
        '            this.burstGap = 1;',
        '            this.playAction("cannon_fire", T_FIRE);',
        '        }',
        ''
    ].join('\n');
    const ok = rw(p, s => {
        if (s.includes('fire burst: 4-5 normal poops')) return s;
        const anchor = '        if (this.bossBar != null) {';
        if (!s.includes(anchor)) return s;
        return s.replace(anchor, block + anchor);
    });
    console.log('③ 连射逻辑: ' + (ok ? 'OK' : '未命中/已存在'));
    const s2 = fs.readFileSync(p, 'utf8');
    console.log('   自检: firePoop 调用 ' + (s2.match(/this\.firePoop\(\)/g) || []).length + ' 处, burstLeft 出现 ' + (s2.match(/burstLeft/g) || []).length + ' 次');
}
