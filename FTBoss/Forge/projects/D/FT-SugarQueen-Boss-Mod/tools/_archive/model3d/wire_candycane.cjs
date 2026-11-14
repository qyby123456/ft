/*
 * 2026-09-12 按用户要求: 把【糖果拐杖】做成姜饼王 Boss 的武器掉落
 *  ① 模型转换: Blockbench 的 elements 模型 -> Java 物品模型
 *       textures { "0": "candy_cane" } -> { "layer0": "ftboss:item/candy_cane" }
 *       每个面的 "#0" -> "#layer0"; 加 "parent": "item/handheld"(获得标准持握姿势)
 *  ② 贴图落地: textures/item/candy_cane.png (32x32)
 *  ③ 物品: CandyCaneItem(剑类, 钻石级基底) + 注册 + lang + 创造栏
 *  ④ 掉落: 姜饼王(SUGAR_QUEEN)死亡时掉一把(接在中央钩子 onBossDefeated 里)
 */
const fs = require('fs');
const path = require('path');
const ROOT = process.argv[2];
const MODEL_SRC = process.argv[3];
const TEX_SRC = process.argv[4];
const J = 'src/main/java/net/farmingtales/ftboss';
const A = 'src/main/resources/assets/ftboss';
const read = p => fs.readFileSync(path.join(ROOT, p), 'utf8');
const write = (p, s) => { fs.mkdirSync(path.dirname(path.join(ROOT, p)), { recursive: true }); fs.writeFileSync(path.join(ROOT, p), s); };
function rep(rel, a, b, label) {
    const s = read(rel);
    if (s.includes(b.trim().slice(0, 40))) { console.log('  skip ' + label); return; }
    if (!s.includes(a)) { console.log('  FAIL ' + label); return; }
    write(rel, s.replace(a, b));
    console.log('  OK   ' + label);
}

/* ① 模型转换 */
{
    const m = JSON.parse(fs.readFileSync(MODEL_SRC, 'utf8'));
    m.parent = 'item/handheld';
    m.textures = { layer0: 'ftboss:item/candy_cane', particle: 'ftboss:item/candy_cane' };
    let n = 0;
    for (const el of (m.elements || [])) {
        for (const f of Object.values(el.faces || {})) {
            if (f.texture === '#0') { f.texture = '#layer0'; n++; }
        }
    }
    if (m.display) delete m.display;      // 交给 item/handheld 的 display
    write(`${A}/models/item/candy_cane.json`, JSON.stringify(m, null, 2));
    console.log(`  ① 模型转换: elements=${(m.elements || []).length} 面改写=${n} -> models/item/candy_cane.json`);
}
/* ② 贴图 */
{
    const dst = path.join(ROOT, `${A}/textures/item/candy_cane.png`);
    fs.mkdirSync(path.dirname(dst), { recursive: true });
    fs.copyFileSync(TEX_SRC, dst);
    console.log('  ② 贴图落地: textures/item/candy_cane.png ' + fs.statSync(dst).size + ' B');
}

/* ③ 物品类 */
write(`${J}/item/CandyCaneItem.java`, `package net.farmingtales.ftboss.item;

import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;

/**
 * Candy Cane (2026-09-12): weapon drop of the Gingerbread King boss.
 * Diamond-tier base with slightly better reach-friendly speed; visually a striped candy cane.
 */
public class CandyCaneItem extends SwordItem {

    public CandyCaneItem(Properties props) {
        super(Tiers.DIAMOND, 3, -2.2f, props);
    }
}
`);
/* 注册(锚点: 私有构造函数) */
rep(`${J}/item/FTBossItems.java`, '    private FTBossItems() {}',
    `    /** 糖果拐杖: 姜饼王 Boss 的武器掉落(2026-09-12) */
    public static final RegistryObject<Item> CANDY_CANE =
            ITEMS.register("candy_cane", () -> new CandyCaneItem(new Item.Properties()));

    private FTBossItems() {}`, '物品注册');
/* lang + 创造栏 */
for (const [f, v] of [['zh_cn.json', '\u7cd6\u679c\u62d0\u6756'], ['en_us.json', 'Candy Cane']]) {
    const p = path.join(ROOT, A, 'lang', f);
    const j = JSON.parse(fs.readFileSync(p, 'utf8'));
    j['item.ftboss.candy_cane'] = v;
    // 顺手给个提示行
    fs.writeFileSync(p, JSON.stringify(j, null, 2));
}
console.log('  OK   lang');
rep(`${J}/item/FTCreativeTabs.java`,
    `                out.accept(FTBossItems.COMPOST_SUMMON.get());`,
    `                out.accept(FTBossItems.COMPOST_SUMMON.get());
                out.accept(FTBossItems.CANDY_CANE.get());`, '创造栏');

/* ④ 姜饼王掉落 */
rep(`${J}/boss/ai/BossMercy.java`, '        giveStatue(boss);',
    `        giveStatue(boss);
        giveWeaponDrop(boss);`, '挂武器掉落调用');
{
    const rel = `${J}/boss/ai/BossMercy.java`;
    let s = read(rel);
    if (s.includes('giveWeaponDrop')) {
        if (s.includes('private static void giveWeaponDrop')) console.log('  skip 武器掉落方法');
        else {
            const last = s.lastIndexOf('}');
            const method = `
    /**
     * 按 Boss 类型掉落专属武器(2026-09-12): 姜饼王 -> 糖果拐杖。
     * 与石像一样走中央钩子, 保证任何死亡路径都能掉。
     */
    private static void giveWeaponDrop(LivingEntity boss) {
        if (boss == null || boss.level().isClientSide) return;
        try {
            if (boss.getType() == net.farmingtales.ftboss.entity.FTEntities.SUGAR_QUEEN.get()) {
                boss.spawnAtLocation(new net.minecraft.world.item.ItemStack(
                        net.farmingtales.ftboss.item.FTBossItems.CANDY_CANE.get()));
                FTBossMod.LOGGER.info("[FTBoss] weapon drop: Candy Cane (Gingerbread King)");
            }
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss] weapon drop failed: {}", t.toString());
        }
    }
`;
            write(rel, s.slice(0, last) + method + s.slice(last));
            console.log('  OK   武器掉落方法 giveWeaponDrop');
        }
    } else console.log('  FAIL 调用未插入');
}
console.log('完成');
