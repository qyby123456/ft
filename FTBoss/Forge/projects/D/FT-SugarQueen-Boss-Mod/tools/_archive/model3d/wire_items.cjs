/* 正确插入两个物品(锚点: 私有构造函数), 加创造栏, 恢复配方 */
const fs = require('fs');
const path = require('path');
const ROOT = process.argv[2];
const J = 'src/main/java/net/farmingtales/ftboss';
const read = p => fs.readFileSync(path.join(ROOT, p), 'utf8');
const write = (p, s) => { fs.mkdirSync(path.dirname(path.join(ROOT, p)), { recursive: true }); fs.writeFileSync(path.join(ROOT, p), s); };

/* ① 物品注册: 插在 private FTBossItems() {} 之前(整行锚点, 绝不会插进别的声明中间) */
{
    const rel = `${J}/item/FTBossItems.java`;
    let s = read(rel);
    const anchor = '    private FTBossItems() {}';
    if (s.includes('RAINBOW_FERTILIZER')) console.log('  skip 物品(已注册)');
    else if (!s.includes(anchor)) console.log('  FAIL 找不到锚点 private FTBossItems()');
    else {
        const block = [
            '    /** 炫彩肥料: 炫彩答辩超人的掉落(可当骨粉用, 并回复使用者 1 点血) */',
            '    public static final RegistryObject<Item> RAINBOW_FERTILIZER =',
            '            ITEMS.register("rainbow_fertilizer", () -> new RainbowFertilizerItem(new Item.Properties()));',
            '',
            '    /** 炫彩堆肥桶: 炫彩答辩超人的召唤物 */',
            '    public static final RegistryObject<Item> COMPOST_SUMMON =',
            '            ITEMS.register("compost_summon", () -> new CompostSummonItem(new Item.Properties().stacksTo(1)));',
            '',
            anchor
        ].join('\n');
        write(rel, s.replace(anchor, block));
        console.log('  OK   物品注册(RAINBOW_FERTILIZER + COMPOST_SUMMON)');
    }
}
/* ② 创造栏 */
{
    const rel = `${J}/item/FTCreativeTabs.java`;
    let s = read(rel);
    const anchor = '                out.accept(FTBossItems.DEFECT_FRAGMENT.get());';
    if (s.includes('RAINBOW_FERTILIZER')) console.log('  skip 创造栏');
    else if (!s.includes(anchor)) console.log('  FAIL 创造栏锚点');
    else {
        write(rel, s.replace(anchor, anchor + '\n                out.accept(FTBossItems.RAINBOW_FERTILIZER.get());\n                out.accept(FTBossItems.COMPOST_SUMMON.get());'));
        console.log('  OK   创造栏两行');
    }
}
/* ③ 配方(与物品 id 对应) */
{
    const recipe = {
        type: 'minecraft:crafting_shaped',
        pattern: ['PBP', 'BCB', 'PBP'],
        key: {
            P: { item: 'create:precision_mechanism' },
            B: { item: 'minecraft:bone_meal' },
            C: { item: 'minecraft:composter' }
        },
        result: { item: 'ftboss:compost_summon', count: 1 }
    };
    write('src/main/resources/data/ftboss/recipes/compost_summon.json', JSON.stringify(recipe, null, 2));
    console.log('  OK   配方 compost_summon.json');
}
/* ④ lang(若之前被回退过就补上) */
for (const [f, fert, sum] of [['zh_cn.json', '\u70ab\u5f69\u80a5\u6599', '\u70ab\u5f69\u5806\u80a5\u6876'], ['en_us.json', 'Rainbow Fertilizer', 'Rainbow Compost Bucket']]) {
    const p = path.join(ROOT, 'src/main/resources/assets/ftboss/lang', f);
    const j = JSON.parse(fs.readFileSync(p, 'utf8'));
    j['item.ftboss.rainbow_fertilizer'] = fert;
    j['item.ftboss.compost_summon'] = sum;
    fs.writeFileSync(p, JSON.stringify(j, null, 2));
}
console.log('  OK   lang 两个物品');
console.log('完成');
